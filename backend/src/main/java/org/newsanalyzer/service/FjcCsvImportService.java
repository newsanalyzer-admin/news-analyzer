package org.newsanalyzer.service;

import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import lombok.extern.slf4j.Slf4j;
import org.newsanalyzer.dto.FjcImportResult;
import org.newsanalyzer.dto.FjcJudgeCsvRecord;
import org.newsanalyzer.model.*;
import org.newsanalyzer.repository.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.client.RestTemplate;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for importing federal judge data from FJC's Biographical Directory CSV.
 *
 * Downloads and parses the FJC CSV file, creating/updating Person, GovernmentPosition,
 * and PositionHolding records for federal judges.
 *
 * @author James (Dev Agent)
 * @since 2.0.0
 * @see <a href="https://www.fjc.gov/history/judges/biographical-directory-article-iii-federal-judges-export">FJC Export</a>
 */
@Service
@Slf4j
public class FjcCsvImportService {

    private final PersonRepository personRepository;
    private final GovernmentPositionRepository positionRepository;
    private final PositionHoldingRepository holdingRepository;
    private final GovernmentOrganizationRepository orgRepository;
    private final RestTemplate restTemplate;
    private final TransactionTemplate transactionTemplate;

    @Value("${fjc.csv.url:https://www.fjc.gov/sites/default/files/history/judges.csv}")
    private String fjcCsvUrl;

    @Value("${fjc.import.batch-size:100}")
    private int batchSize;

    // Date format from FJC CSV (MM/DD/YYYY)
    private static final DateTimeFormatter FJC_DATE_FORMAT = DateTimeFormatter.ofPattern("M/d/yyyy");

    // Cache for court organization lookups
    private final Map<String, UUID> courtCache = new ConcurrentHashMap<>();

    public FjcCsvImportService(PersonRepository personRepository,
                               GovernmentPositionRepository positionRepository,
                               PositionHoldingRepository holdingRepository,
                               GovernmentOrganizationRepository orgRepository,
                               RestTemplate restTemplate,
                               PlatformTransactionManager transactionManager) {
        this.personRepository = personRepository;
        this.positionRepository = positionRepository;
        this.holdingRepository = holdingRepository;
        this.orgRepository = orgRepository;
        this.restTemplate = restTemplate;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    /**
     * Import judges from the FJC CSV URL.
     *
     * @return Import result with statistics
     */
    public FjcImportResult importFromUrl() {
        return importFromUrl(null, null);
    }

    /**
     * Import judges from the FJC CSV URL with optional offset/limit.
     *
     * @param offset Number of records to skip
     * @param limit Maximum records to process
     * @return Import result with statistics
     */
    public FjcImportResult importFromUrl(Integer offset, Integer limit) {
        log.info("Starting FJC CSV import from: {} (offset={}, limit={})", fjcCsvUrl, offset, limit);

        FjcImportResult.FjcImportResultBuilder result = FjcImportResult.builder()
                .startTime(LocalDateTime.now())
                .success(false)
                .personsCreated(0)
                .personsUpdated(0)
                .positionsCreated(0)
                .holdingsCreated(0)
                .holdingsUpdated(0)
                .skipped(0)
                .errors(0)
                .errorMessages(new ArrayList<>());

        try {
            // Download CSV
            log.info("Downloading FJC CSV from: {}", fjcCsvUrl);
            String csvContent = restTemplate.getForObject(fjcCsvUrl, String.class);

            if (csvContent == null || csvContent.isEmpty()) {
                result.errorMessages(List.of("Failed to download CSV or empty content"));
                return result.endTime(LocalDateTime.now()).build();
            }

            // Build court cache
            buildCourtCache();

            // Parse CSV
            List<FjcJudgeCsvRecord> records = parseCsv(csvContent);
            int totalRecords = records.size();
            result.totalRecords(totalRecords);
            log.info("Parsed {} records from FJC CSV", totalRecords);

            // Apply offset/limit
            int startIdx = offset != null ? offset : 0;
            int endIdx = limit != null ? Math.min(startIdx + limit, totalRecords) : totalRecords;
            records = records.subList(startIdx, endIdx);
            log.info("Processing records {} to {} (out of {})", startIdx, endIdx, totalRecords);

            // Process records
            int processed = 0;
            int personsCreated = 0;
            int personsUpdated = 0;
            int positionsCreated = 0;
            int holdingsCreated = 0;
            int holdingsUpdated = 0;
            int skipped = 0;
            int errors = 0;
            List<String> errorMessages = new ArrayList<>();

            for (FjcJudgeCsvRecord record : records) {
                try {
                    ImportStats stats = processSingleRecord(record);
                    personsCreated += stats.personsCreated;
                    personsUpdated += stats.personsUpdated;
                    positionsCreated += stats.positionsCreated;
                    holdingsCreated += stats.holdingsCreated;
                    holdingsUpdated += stats.holdingsUpdated;
                    if (stats.skipped) skipped++;
                } catch (Exception e) {
                    errors++;
                    String msg = String.format("Error processing judge NID=%s (%s): %s",
                            record.getNid(), record.getFullName(), e.getMessage());
                    errorMessages.add(msg);
                    log.warn(msg);
                }
                processed++;

                if (processed % 500 == 0) {
                    log.info("Processed {}/{} records...", processed, records.size());
                }
            }

            result.success(errors == 0)
                  .personsCreated(personsCreated)
                  .personsUpdated(personsUpdated)
                  .positionsCreated(positionsCreated)
                  .holdingsCreated(holdingsCreated)
                  .holdingsUpdated(holdingsUpdated)
                  .skipped(skipped)
                  .errors(errors)
                  .errorMessages(errorMessages);

        } catch (Exception e) {
            log.error("FJC import failed: {}", e.getMessage(), e);
            result.errorMessages(List.of("Import failed: " + e.getMessage()));
        }

        FjcImportResult finalResult = result.endTime(LocalDateTime.now()).build();
        log.info("FJC import completed: {}", finalResult.getSummary());
        return finalResult;
    }

    /**
     * Import from an input stream (for file uploads).
     */
    public FjcImportResult importFromStream(InputStream inputStream) {
        log.info("Starting FJC CSV import from input stream");

        FjcImportResult.FjcImportResultBuilder result = FjcImportResult.builder()
                .startTime(LocalDateTime.now())
                .success(false)
                .errorMessages(new ArrayList<>());

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            String csvContent = sb.toString();

            buildCourtCache();
            List<FjcJudgeCsvRecord> records = parseCsv(csvContent);
            result.totalRecords(records.size());

            int personsCreated = 0, personsUpdated = 0, positionsCreated = 0;
            int holdingsCreated = 0, holdingsUpdated = 0, skipped = 0, errors = 0;
            List<String> errorMessages = new ArrayList<>();

            for (FjcJudgeCsvRecord record : records) {
                try {
                    ImportStats stats = processSingleRecord(record);
                    personsCreated += stats.personsCreated;
                    personsUpdated += stats.personsUpdated;
                    positionsCreated += stats.positionsCreated;
                    holdingsCreated += stats.holdingsCreated;
                    holdingsUpdated += stats.holdingsUpdated;
                    if (stats.skipped) skipped++;
                } catch (Exception e) {
                    errors++;
                    errorMessages.add("Error: " + record.getNid() + " - " + e.getMessage());
                }
            }

            result.success(errors == 0)
                  .personsCreated(personsCreated)
                  .personsUpdated(personsUpdated)
                  .positionsCreated(positionsCreated)
                  .holdingsCreated(holdingsCreated)
                  .holdingsUpdated(holdingsUpdated)
                  .skipped(skipped)
                  .errors(errors)
                  .errorMessages(errorMessages);

        } catch (Exception e) {
            log.error("FJC import from stream failed: {}", e.getMessage(), e);
            result.errorMessages(List.of("Import failed: " + e.getMessage()));
        }

        return result.endTime(LocalDateTime.now()).build();
    }

    private List<FjcJudgeCsvRecord> parseCsv(String csvContent) {
        try (Reader reader = new StringReader(csvContent)) {
            CsvToBean<FjcJudgeCsvRecord> csvToBean = new CsvToBeanBuilder<FjcJudgeCsvRecord>(reader)
                    .withType(FjcJudgeCsvRecord.class)
                    .withIgnoreLeadingWhiteSpace(true)
                    .withIgnoreEmptyLine(true)
                    .build();
            return csvToBean.parse();
        } catch (Exception e) {
            log.error("Failed to parse FJC CSV: {}", e.getMessage());
            throw new RuntimeException("CSV parsing failed: " + e.getMessage(), e);
        }
    }

    private void buildCourtCache() {
        courtCache.clear();
        List<GovernmentOrganization> courts = orgRepository.findByBranch(
                GovernmentOrganization.GovernmentBranch.JUDICIAL);
        for (GovernmentOrganization court : courts) {
            // Cache by official name and acronym
            courtCache.put(court.getOfficialName().toLowerCase(), court.getId());
            if (court.getAcronym() != null && !court.getAcronym().isEmpty()) {
                courtCache.put(court.getAcronym().toLowerCase(), court.getId());
            }
        }
        log.info("Built court cache with {} entries", courtCache.size());
    }

    private ImportStats processSingleRecord(FjcJudgeCsvRecord record) {
        return transactionTemplate.execute(status -> {
            ImportStats stats = new ImportStats();

            // Skip if no name
            if (record.getLastName() == null || record.getLastName().isBlank()) {
                stats.skipped = true;
                return stats;
            }

            // Skip if no court info
            if (record.getCourtName1() == null || record.getCourtName1().isBlank()) {
                stats.skipped = true;
                return stats;
            }

            // Find or create Person
            Person person = findOrCreatePerson(record, stats);

            // Find court organization
            UUID courtId = findCourtId(record.getCourtName1());

            // Find or create position (judgeship)
            GovernmentPosition position = findOrCreatePosition(record, courtId, stats);

            // Find or create position holding
            findOrCreateHolding(person, position, record, stats);

            return stats;
        });
    }

    private Person findOrCreatePerson(FjcJudgeCsvRecord record, ImportStats stats) {
        // Try to find by FJC NID in external_ids
        String fjcNid = record.getNid();
        Optional<Person> existing = Optional.empty();

        if (fjcNid != null && !fjcNid.isBlank()) {
            // Search by name since we don't have FJC NID indexed
            existing = personRepository.findByFirstNameAndLastName(
                    record.getFirstName().trim(),
                    record.getLastName().trim());
        }

        if (existing.isEmpty()) {
            // Try by name match
            existing = personRepository.findByFirstNameAndLastName(
                    record.getFirstName() != null ? record.getFirstName().trim() : "",
                    record.getLastName().trim());
        }

        Person person;
        if (existing.isPresent()) {
            person = existing.get();
            // Update if needed
            updatePersonFromRecord(person, record);
            person = personRepository.save(person);
            stats.personsUpdated++;
        } else {
            person = createPersonFromRecord(record);
            person = personRepository.save(person);
            stats.personsCreated++;
        }

        return person;
    }

    private Person createPersonFromRecord(FjcJudgeCsvRecord record) {
        Person person = new Person();
        person.setFirstName(record.getFirstName() != null ? record.getFirstName().trim() : "");
        person.setLastName(record.getLastName().trim());
        person.setMiddleName(record.getMiddleName());
        person.setSuffix(record.getSuffix());
        person.setGender(record.getGender());
        person.setBirthDate(parseDate(record.getBirthDateString()));
        person.setDataSource(DataSource.FJC);
        person.setEnrichmentSource("fjc");
        return person;
    }

    private void updatePersonFromRecord(Person person, FjcJudgeCsvRecord record) {
        // Only update fields that are empty
        if (person.getMiddleName() == null && record.getMiddleName() != null) {
            person.setMiddleName(record.getMiddleName());
        }
        if (person.getSuffix() == null && record.getSuffix() != null) {
            person.setSuffix(record.getSuffix());
        }
        if (person.getGender() == null && record.getGender() != null) {
            person.setGender(record.getGender());
        }
        if (person.getBirthDate() == null && record.getBirthDateString() != null) {
            person.setBirthDate(parseDate(record.getBirthDateString()));
        }
    }

    private UUID findCourtId(String courtName) {
        if (courtName == null || courtName.isBlank()) {
            return null;
        }

        String key = courtName.toLowerCase().trim();
        UUID courtId = courtCache.get(key);

        if (courtId == null) {
            // Try fuzzy match - find courts containing the district/circuit name
            for (Map.Entry<String, UUID> entry : courtCache.entrySet()) {
                if (entry.getKey().contains(key) || key.contains(entry.getKey())) {
                    return entry.getValue();
                }
            }
        }

        return courtId;
    }

    private GovernmentPosition findOrCreatePosition(FjcJudgeCsvRecord record, UUID courtId, ImportStats stats) {
        String title = record.getAppointmentTitle1() != null ? record.getAppointmentTitle1().trim() : "Judge";
        String courtName = record.getCourtName1();

        // Try to find existing position
        Optional<GovernmentPosition> existing = positionRepository.findByTitleAndOrganizationId(title, courtId);

        if (existing.isPresent()) {
            return existing.get();
        }

        // Create new position
        GovernmentPosition position = new GovernmentPosition();
        position.setTitle(title);
        position.setBranch(Branch.JUDICIAL);
        position.setPositionType(PositionType.APPOINTED);
        position.setOrganizationId(courtId);
        position.setDescription("Federal judgeship on " + courtName);
        position = positionRepository.save(position);
        stats.positionsCreated++;

        return position;
    }

    private void findOrCreateHolding(Person person, GovernmentPosition position,
                                      FjcJudgeCsvRecord record, ImportStats stats) {
        LocalDate commissionDate = parseDate(record.getCommissionDate1());
        LocalDate terminationDate = parseDate(record.getTerminationDate1());

        if (commissionDate == null) {
            // Try confirmation date as fallback
            commissionDate = parseDate(record.getConfirmationDate1());
        }

        if (commissionDate == null) {
            // Can't create holding without a start date
            return;
        }

        // Check for existing holding
        Optional<PositionHolding> existing = holdingRepository.findByPersonIdAndPositionIdAndStartDate(
                person.getId(), position.getId(), commissionDate);

        if (existing.isPresent()) {
            PositionHolding holding = existing.get();
            // Update end date if changed
            if (terminationDate != null && holding.getEndDate() == null) {
                holding.setEndDate(terminationDate);
                holdingRepository.save(holding);
                stats.holdingsUpdated++;
            }
            return;
        }

        // Create new holding
        PositionHolding holding = new PositionHolding();
        holding.setPersonId(person.getId());
        holding.setPositionId(position.getId());
        holding.setStartDate(commissionDate);
        holding.setEndDate(terminationDate);
        holding.setDataSource(DataSource.FJC);
        holding.setSourceReference("FJC NID: " + record.getNid());
        holdingRepository.save(holding);
        stats.holdingsCreated++;
    }

    private LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(dateStr.trim(), FJC_DATE_FORMAT);
        } catch (DateTimeParseException e) {
            log.trace("Could not parse date: {}", dateStr);
            return null;
        }
    }

    /**
     * Internal class for tracking import statistics per record.
     */
    private static class ImportStats {
        int personsCreated = 0;
        int personsUpdated = 0;
        int positionsCreated = 0;
        int holdingsCreated = 0;
        int holdingsUpdated = 0;
        boolean skipped = false;
    }
}
