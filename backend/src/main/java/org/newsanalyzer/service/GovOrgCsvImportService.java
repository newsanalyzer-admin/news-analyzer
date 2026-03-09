package org.newsanalyzer.service;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvException;
import org.newsanalyzer.dto.CsvImportResult;
import org.newsanalyzer.model.GovernmentOrganization;
import org.newsanalyzer.model.GovernmentOrganization.GovernmentBranch;
import org.newsanalyzer.model.GovernmentOrganization.OrganizationType;
import org.newsanalyzer.repository.GovernmentOrganizationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Service for importing government organizations from CSV files.
 *
 * Supports importing Legislative and Judicial branch organizations
 * that are not available in the Federal Register API.
 *
 * Features:
 * - CSV parsing with validation
 * - Merge strategy (match by acronym, then name)
 * - Parent organization linking by UUID or acronym
 * - Audit trail with "csv-import" source
 *
 * @author James (Dev Agent)
 * @since 2.0.0
 */
@Service
@Transactional
public class GovOrgCsvImportService {

    private static final Logger log = LoggerFactory.getLogger(GovOrgCsvImportService.class);

    private static final String AUDIT_SOURCE = "csv-import";

    // Expected CSV headers
    private static final String[] EXPECTED_HEADERS = {
            "officialName", "acronym", "branch", "orgType", "orgLevel",
            "parentId", "establishedDate", "dissolvedDate", "websiteUrl", "jurisdictionAreas"
    };

    // UUID pattern for parentId validation
    private static final Pattern UUID_PATTERN = Pattern.compile(
            "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$");

    private final GovernmentOrganizationRepository repository;

    public GovOrgCsvImportService(GovernmentOrganizationRepository repository) {
        this.repository = repository;
    }

    /**
     * Import government organizations from a CSV file.
     *
     * @param csvStream Input stream of the CSV file
     * @return CsvImportResult with statistics and any validation errors
     */
    public CsvImportResult importFromCsv(InputStream csvStream) {
        CsvImportResult result = CsvImportResult.builder()
                .success(false)
                .added(0)
                .updated(0)
                .skipped(0)
                .errors(0)
                .validationErrors(new ArrayList<>())
                .errorMessages(new ArrayList<>())
                .build();

        log.info("Starting CSV import");

        List<String[]> allRows;
        try (CSVReader reader = new CSVReaderBuilder(new InputStreamReader(csvStream, StandardCharsets.UTF_8))
                .build()) {
            allRows = reader.readAll();
        } catch (IOException | CsvException e) {
            log.error("Failed to read CSV file: {}", e.getMessage());
            result.addError("Failed to read CSV file: " + e.getMessage());
            return result;
        }

        if (allRows.isEmpty()) {
            result.addError("CSV file is empty");
            return result;
        }

        // Validate headers
        String[] headers = allRows.get(0);
        if (!validateHeaders(headers, result)) {
            return result;
        }

        // Parse data rows
        List<CsvRow> dataRows = new ArrayList<>();
        for (int i = 1; i < allRows.size(); i++) {
            String[] row = allRows.get(i);
            int lineNumber = i + 1; // 1-based line numbers

            // Skip empty rows
            if (isEmptyRow(row)) {
                continue;
            }

            CsvRow csvRow = parseRow(row, lineNumber, result);
            if (csvRow != null) {
                dataRows.add(csvRow);
            }
        }

        // Check for duplicate acronyms within the file
        checkDuplicateAcronyms(dataRows, result);

        // If there are validation errors, return early
        if (result.hasValidationErrors()) {
            log.warn("CSV validation failed with {} errors", result.getValidationErrors().size());
            return result;
        }

        // Build a map of acronyms to UUIDs for parent resolution
        // Include both existing DB records and new records being imported
        Map<String, UUID> acronymToId = buildAcronymToIdMap(dataRows);

        // Process each row
        for (CsvRow csvRow : dataRows) {
            try {
                ImportAction action = processRow(csvRow, acronymToId);
                switch (action) {
                    case ADDED:
                        result.setAdded(result.getAdded() + 1);
                        break;
                    case UPDATED:
                        result.setUpdated(result.getUpdated() + 1);
                        break;
                    case SKIPPED:
                        result.setSkipped(result.getSkipped() + 1);
                        break;
                }
            } catch (Exception e) {
                log.error("Failed to process row {}: {}", csvRow.lineNumber, e.getMessage());
                result.addError(String.format("Line %d: Failed to process - %s",
                        csvRow.lineNumber, e.getMessage()));
            }
        }

        result.setSuccess(result.getErrors() == 0);
        log.info("CSV import completed: {}", result);

        return result;
    }

    /**
     * Validate CSV headers match expected schema.
     */
    private boolean validateHeaders(String[] headers, CsvImportResult result) {
        if (headers.length < 3) {
            result.addValidationError(1, "headers", String.join(",", headers),
                    "CSV must have at least 3 columns: officialName, branch, orgType");
            return false;
        }

        // Check required headers are present
        Set<String> headerSet = Arrays.stream(headers)
                .map(String::trim)
                .map(String::toLowerCase)
                .collect(Collectors.toSet());

        String[] requiredHeaders = {"officialname", "branch", "orgtype"};
        for (String required : requiredHeaders) {
            if (!headerSet.contains(required)) {
                result.addValidationError(1, "headers", String.join(",", headers),
                        "Missing required header: " + required);
                return false;
            }
        }

        return true;
    }

    /**
     * Parse a single CSV row into a CsvRow object with validation.
     */
    private CsvRow parseRow(String[] row, int lineNumber, CsvImportResult result) {
        CsvRow csvRow = new CsvRow();
        csvRow.lineNumber = lineNumber;

        // Ensure we have enough columns
        String[] paddedRow = Arrays.copyOf(row, EXPECTED_HEADERS.length);

        // officialName (required)
        csvRow.officialName = getStringValue(paddedRow, 0);
        if (csvRow.officialName == null || csvRow.officialName.isEmpty()) {
            result.addValidationError(lineNumber, "officialName", "",
                    "Official name is required");
            return null;
        }

        // acronym (optional)
        csvRow.acronym = getStringValue(paddedRow, 1);

        // branch (required)
        String branchValue = getStringValue(paddedRow, 2);
        if (branchValue == null || branchValue.isEmpty()) {
            result.addValidationError(lineNumber, "branch", "",
                    "Branch is required");
            return null;
        }
        try {
            csvRow.branch = GovernmentBranch.fromValue(branchValue.toLowerCase());
        } catch (IllegalArgumentException e) {
            result.addValidationError(lineNumber, "branch", branchValue,
                    "Invalid branch value. Must be one of: executive, legislative, judicial");
            return null;
        }

        // orgType (required)
        String orgTypeValue = getStringValue(paddedRow, 3);
        if (orgTypeValue == null || orgTypeValue.isEmpty()) {
            result.addValidationError(lineNumber, "orgType", "",
                    "Organization type is required");
            return null;
        }
        try {
            csvRow.orgType = OrganizationType.fromValue(orgTypeValue.toLowerCase());
        } catch (IllegalArgumentException e) {
            result.addValidationError(lineNumber, "orgType", orgTypeValue,
                    "Invalid orgType value. Must be one of: branch, department, independent_agency, bureau, office, commission, board");
            return null;
        }

        // orgLevel (optional)
        String orgLevelValue = getStringValue(paddedRow, 4);
        if (orgLevelValue != null && !orgLevelValue.isEmpty()) {
            try {
                csvRow.orgLevel = Integer.parseInt(orgLevelValue);
                if (csvRow.orgLevel < 1 || csvRow.orgLevel > 10) {
                    result.addValidationError(lineNumber, "orgLevel", orgLevelValue,
                            "Organization level must be between 1 and 10");
                    return null;
                }
            } catch (NumberFormatException e) {
                result.addValidationError(lineNumber, "orgLevel", orgLevelValue,
                        "Invalid number format for orgLevel");
                return null;
            }
        }

        // parentId (optional - can be UUID or acronym)
        csvRow.parentIdString = getStringValue(paddedRow, 5);

        // establishedDate (optional - yyyy-MM-dd)
        String establishedDateValue = getStringValue(paddedRow, 6);
        if (establishedDateValue != null && !establishedDateValue.isEmpty()) {
            try {
                csvRow.establishedDate = LocalDate.parse(establishedDateValue);
            } catch (DateTimeParseException e) {
                result.addValidationError(lineNumber, "establishedDate", establishedDateValue,
                        "Invalid date format. Expected: yyyy-MM-dd");
                return null;
            }
        }

        // dissolvedDate (optional - yyyy-MM-dd)
        String dissolvedDateValue = getStringValue(paddedRow, 7);
        if (dissolvedDateValue != null && !dissolvedDateValue.isEmpty()) {
            try {
                csvRow.dissolvedDate = LocalDate.parse(dissolvedDateValue);
            } catch (DateTimeParseException e) {
                result.addValidationError(lineNumber, "dissolvedDate", dissolvedDateValue,
                        "Invalid date format. Expected: yyyy-MM-dd");
                return null;
            }
        }

        // websiteUrl (optional)
        String websiteUrlValue = getStringValue(paddedRow, 8);
        if (websiteUrlValue != null && !websiteUrlValue.isEmpty()) {
            try {
                new URL(websiteUrlValue);
                csvRow.websiteUrl = websiteUrlValue;
            } catch (MalformedURLException e) {
                result.addValidationError(lineNumber, "websiteUrl", websiteUrlValue,
                        "Invalid URL format");
                return null;
            }
        }

        // jurisdictionAreas (optional - semicolon-separated)
        String jurisdictionAreasValue = getStringValue(paddedRow, 9);
        if (jurisdictionAreasValue != null && !jurisdictionAreasValue.isEmpty()) {
            csvRow.jurisdictionAreas = Arrays.stream(jurisdictionAreasValue.split(";"))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toList());
        }

        return csvRow;
    }

    /**
     * Get trimmed string value from row, handling nulls.
     */
    private String getStringValue(String[] row, int index) {
        if (index >= row.length || row[index] == null) {
            return null;
        }
        String value = row[index].trim();
        // Remove surrounding quotes if present
        if (value.startsWith("\"") && value.endsWith("\"") && value.length() > 1) {
            value = value.substring(1, value.length() - 1);
        }
        return value.isEmpty() ? null : value;
    }

    /**
     * Check if a row is empty (all cells null or empty).
     */
    private boolean isEmptyRow(String[] row) {
        for (String cell : row) {
            if (cell != null && !cell.trim().isEmpty()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Check for duplicate acronyms within the import file.
     */
    private void checkDuplicateAcronyms(List<CsvRow> rows, CsvImportResult result) {
        Map<String, List<Integer>> acronymLines = new HashMap<>();

        for (CsvRow row : rows) {
            if (row.acronym != null && !row.acronym.isEmpty()) {
                String key = row.acronym.toLowerCase();
                acronymLines.computeIfAbsent(key, k -> new ArrayList<>()).add(row.lineNumber);
            }
        }

        for (Map.Entry<String, List<Integer>> entry : acronymLines.entrySet()) {
            if (entry.getValue().size() > 1) {
                String lines = entry.getValue().stream()
                        .map(String::valueOf)
                        .collect(Collectors.joining(", "));
                result.addValidationError(entry.getValue().get(0), "acronym", entry.getKey(),
                        String.format("Duplicate acronym '%s' found on lines: %s", entry.getKey(), lines));
            }
        }
    }

    /**
     * Build a map of acronyms to UUIDs for parent resolution.
     * Includes existing DB records and new records being imported.
     */
    private Map<String, UUID> buildAcronymToIdMap(List<CsvRow> rows) {
        Map<String, UUID> map = new HashMap<>();

        // Get all existing organizations with acronyms
        List<GovernmentOrganization> existing = repository.findAll();
        for (GovernmentOrganization org : existing) {
            if (org.getAcronym() != null && !org.getAcronym().isEmpty()) {
                map.put(org.getAcronym().toLowerCase(), org.getId());
            }
        }

        return map;
    }

    /**
     * Process a single row: create or update organization.
     * Uses merge strategy: match by acronym first, then by name.
     */
    private ImportAction processRow(CsvRow csvRow, Map<String, UUID> acronymToId) {
        // Try to find existing organization
        Optional<GovernmentOrganization> existing = Optional.empty();

        // Match by acronym first (most reliable)
        if (csvRow.acronym != null && !csvRow.acronym.isEmpty()) {
            existing = repository.findByAcronymIgnoreCase(csvRow.acronym);
        }

        // If not found by acronym, try by official name
        if (existing.isEmpty()) {
            existing = repository.findByOfficialNameIgnoreCase(csvRow.officialName);
        }

        // Resolve parentId
        UUID parentId = resolveParentId(csvRow.parentIdString, acronymToId);

        if (existing.isPresent()) {
            // Update existing organization
            GovernmentOrganization org = existing.get();
            boolean updated = updateOrganization(org, csvRow, parentId);

            // Update acronymToId map for future parent resolution
            if (csvRow.acronym != null && !csvRow.acronym.isEmpty()) {
                acronymToId.put(csvRow.acronym.toLowerCase(), org.getId());
            }

            repository.save(org);
            return updated ? ImportAction.UPDATED : ImportAction.SKIPPED;
        } else {
            // Create new organization
            GovernmentOrganization org = createOrganization(csvRow, parentId);
            repository.save(org);

            // Update acronymToId map for future parent resolution
            if (csvRow.acronym != null && !csvRow.acronym.isEmpty()) {
                acronymToId.put(csvRow.acronym.toLowerCase(), org.getId());
            }

            return ImportAction.ADDED;
        }
    }

    /**
     * Resolve parentId which can be a UUID or an acronym.
     */
    private UUID resolveParentId(String parentIdString, Map<String, UUID> acronymToId) {
        if (parentIdString == null || parentIdString.isEmpty()) {
            return null;
        }

        // Check if it's a UUID
        if (UUID_PATTERN.matcher(parentIdString).matches()) {
            return UUID.fromString(parentIdString);
        }

        // Otherwise, treat as acronym and resolve
        return acronymToId.get(parentIdString.toLowerCase());
    }

    /**
     * Create a new organization from CSV row.
     */
    private GovernmentOrganization createOrganization(CsvRow csvRow, UUID parentId) {
        GovernmentOrganization org = new GovernmentOrganization();

        org.setOfficialName(csvRow.officialName);
        org.setAcronym(csvRow.acronym);
        org.setBranch(csvRow.branch);
        org.setOrgType(csvRow.orgType);
        org.setOrgLevel(csvRow.orgLevel != null ? csvRow.orgLevel : 1);
        org.setParentId(parentId);
        org.setEstablishedDate(csvRow.establishedDate);
        org.setDissolvedDate(csvRow.dissolvedDate);
        org.setWebsiteUrl(csvRow.websiteUrl);
        org.setJurisdictionAreas(csvRow.jurisdictionAreas);

        org.setCreatedBy(AUDIT_SOURCE);
        org.setUpdatedBy(AUDIT_SOURCE);

        log.debug("Creating new organization: {}", csvRow.officialName);

        return org;
    }

    /**
     * Update an existing organization with CSV data.
     * Preserves manually curated fields, only updates if new data is provided.
     */
    private boolean updateOrganization(GovernmentOrganization org, CsvRow csvRow, UUID parentId) {
        boolean updated = false;

        // Update acronym if we didn't have one
        if ((org.getAcronym() == null || org.getAcronym().isEmpty())
                && csvRow.acronym != null && !csvRow.acronym.isEmpty()) {
            org.setAcronym(csvRow.acronym);
            updated = true;
        }

        // Update branch and orgType (CSV is authoritative)
        if (org.getBranch() != csvRow.branch) {
            org.setBranch(csvRow.branch);
            updated = true;
        }

        if (org.getOrgType() != csvRow.orgType) {
            org.setOrgType(csvRow.orgType);
            updated = true;
        }

        // Update orgLevel if provided
        if (csvRow.orgLevel != null && !csvRow.orgLevel.equals(org.getOrgLevel())) {
            org.setOrgLevel(csvRow.orgLevel);
            updated = true;
        }

        // Update parentId if provided and different
        if (parentId != null && !parentId.equals(org.getParentId())) {
            org.setParentId(parentId);
            updated = true;
        }

        // Update dates only if currently null
        if (org.getEstablishedDate() == null && csvRow.establishedDate != null) {
            org.setEstablishedDate(csvRow.establishedDate);
            updated = true;
        }

        if (org.getDissolvedDate() == null && csvRow.dissolvedDate != null) {
            org.setDissolvedDate(csvRow.dissolvedDate);
            updated = true;
        }

        // Update website URL only if currently null
        if (org.getWebsiteUrl() == null && csvRow.websiteUrl != null) {
            org.setWebsiteUrl(csvRow.websiteUrl);
            updated = true;
        }

        // Update jurisdiction areas only if currently null or empty
        if ((org.getJurisdictionAreas() == null || org.getJurisdictionAreas().isEmpty())
                && csvRow.jurisdictionAreas != null && !csvRow.jurisdictionAreas.isEmpty()) {
            org.setJurisdictionAreas(csvRow.jurisdictionAreas);
            updated = true;
        }

        if (updated) {
            org.setUpdatedBy(AUDIT_SOURCE);
            log.debug("Updated organization: {}", csvRow.officialName);
        } else {
            log.debug("Skipped organization (no changes): {}", csvRow.officialName);
        }

        return updated;
    }

    /**
     * Internal class to hold parsed CSV row data.
     */
    private static class CsvRow {
        int lineNumber;
        String officialName;
        String acronym;
        GovernmentBranch branch;
        OrganizationType orgType;
        Integer orgLevel;
        String parentIdString;
        LocalDate establishedDate;
        LocalDate dissolvedDate;
        String websiteUrl;
        List<String> jurisdictionAreas;
    }

    private enum ImportAction {
        ADDED, UPDATED, SKIPPED
    }
}
