package org.newsanalyzer.service;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.newsanalyzer.dto.GovmanImportResult;
import org.newsanalyzer.dto.govman.GovmanDocument;
import org.newsanalyzer.dto.govman.GovmanEntity;
import org.newsanalyzer.model.GovernmentOrganization;
import org.newsanalyzer.model.GovernmentOrganization.GovernmentBranch;
import org.newsanalyzer.model.GovernmentOrganization.OrganizationType;
import org.newsanalyzer.repository.GovernmentOrganizationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Service for importing Government Manual XML files.
 *
 * Parses GOVMAN XML using JAXB and imports government organizations
 * with proper parent-child relationships and branch mappings.
 *
 * @author James (Dev Agent)
 * @since 2.0.0
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class GovmanXmlImportService {

    private static final String IMPORT_SOURCE = "GOVMAN";

    private final GovernmentOrganizationRepository repository;

    private JAXBContext jaxbContext;

    /**
     * Parse XML from an InputStream.
     *
     * @param xmlStream The XML input stream
     * @return List of parsed GovmanEntity objects
     * @throws JAXBException if XML parsing fails
     */
    public List<GovmanEntity> parseXml(InputStream xmlStream) throws JAXBException {
        if (jaxbContext == null) {
            jaxbContext = JAXBContext.newInstance(GovmanDocument.class);
        }

        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        GovmanDocument document = (GovmanDocument) unmarshaller.unmarshal(xmlStream);

        return document.getEntities();
    }

    /**
     * Import entities from an XML input stream.
     *
     * @param xmlStream The XML input stream
     * @return Import result with statistics
     */
    @Transactional
    public GovmanImportResult importFromStream(InputStream xmlStream) {
        log.info("Starting GOVMAN XML import");

        GovmanImportResult result = GovmanImportResult.builder()
                .startTime(LocalDateTime.now())
                .errorDetails(new ArrayList<>())
                .build();

        try {
            List<GovmanEntity> entities = parseXml(xmlStream);
            result.setTotal(entities.size());
            log.info("Parsed {} entities from GOVMAN XML", entities.size());

            // Build parent-child relationship map
            Map<String, GovmanEntity> entityMap = buildEntityMap(entities);

            // Import entities
            importEntities(entities, entityMap, result);

            result.setEndTime(LocalDateTime.now());
            log.info(result.getSummary());

        } catch (JAXBException e) {
            log.error("Failed to parse GOVMAN XML", e);
            result.addError("XML parsing failed: " + e.getMessage());
            result.setEndTime(LocalDateTime.now());
        }

        return result;
    }

    /**
     * Build a map of entity ID to entity for parent lookup.
     */
    Map<String, GovmanEntity> buildEntityMap(List<GovmanEntity> entities) {
        Map<String, GovmanEntity> map = new HashMap<>();
        for (GovmanEntity entity : entities) {
            if (entity.getEntityId() != null) {
                map.put(entity.getEntityId(), entity);
            }
        }
        return map;
    }

    /**
     * Import entities into the database.
     */
    private void importEntities(List<GovmanEntity> entities,
                                Map<String, GovmanEntity> entityMap,
                                GovmanImportResult result) {
        // Track imported entities by their GOVMAN ID for parent resolution
        Map<String, UUID> importedIdMap = new HashMap<>();

        // Pre-load existing organizations by govinfoPackageId for duplicate detection
        Map<String, GovernmentOrganization> existingByPackageId = new HashMap<>();
        repository.findByImportSource(IMPORT_SOURCE).forEach(org -> {
            if (org.getGovinfoPackageId() != null) {
                existingByPackageId.put(org.getGovinfoPackageId(), org);
            }
        });

        // First pass: import all entities without parent references
        for (GovmanEntity entity : entities) {
            if (!validateEntity(entity, result)) {
                continue;
            }

            try {
                String externalId = "GOVMAN:" + entity.getEntityId();
                GovernmentOrganization org = importOrUpdateEntity(entity, externalId, existingByPackageId, result);
                if (org != null) {
                    importedIdMap.put(entity.getEntityId(), org.getId());
                }
            } catch (Exception e) {
                log.warn("Error importing entity {}: {}", entity.getEntityId(), e.getMessage());
                result.addError(entity.getEntityId(), e.getMessage());
            }
        }

        // Second pass: resolve parent references
        resolveParentReferences(entities, importedIdMap, entityMap, result);
    }

    /**
     * Validate a single entity.
     */
    boolean validateEntity(GovmanEntity entity, GovmanImportResult result) {
        if (entity.getEntityId() == null || entity.getEntityId().isBlank()) {
            result.addError("null", "Missing EntityId");
            return false;
        }

        if (entity.getAgencyName() == null || entity.getAgencyName().isBlank()) {
            result.addError(entity.getEntityId(), "Missing AgencyName");
            return false;
        }

        return true;
    }

    /**
     * Import or update a single entity.
     */
    private GovernmentOrganization importOrUpdateEntity(GovmanEntity entity,
                                                        String externalId,
                                                        Map<String, GovernmentOrganization> existingByPackageId,
                                                        GovmanImportResult result) {
        // Check for duplicate by external ID
        GovernmentOrganization existing = existingByPackageId.get(externalId);

        if (existing == null) {
            // Try to find by name (case-insensitive)
            existing = repository.findByOfficialNameIgnoreCase(entity.getAgencyName().trim()).orElse(null);
        }

        if (existing != null) {
            // Update existing record if it's from GOVMAN
            if (IMPORT_SOURCE.equals(existing.getImportSource())) {
                updateOrganization(existing, entity);
                repository.save(existing);
                result.incrementUpdated();
                log.debug("Updated existing organization: {}", entity.getAgencyName());
            } else {
                // Skip if it exists from a different source (don't overwrite manual entries)
                result.incrementSkipped();
                log.debug("Skipped duplicate (different source): {}", entity.getAgencyName());
            }
            return existing;
        }

        // Create new organization
        GovernmentOrganization org = createOrganization(entity, externalId);
        org = repository.save(org);
        result.incrementImported();
        log.debug("Imported new organization: {}", entity.getAgencyName());

        return org;
    }

    /**
     * Create a new GovernmentOrganization from a GovmanEntity.
     */
    private GovernmentOrganization createOrganization(GovmanEntity entity, String externalId) {
        GovernmentBranch branch = mapCategoryToBranch(entity.getCategory());
        OrganizationType orgType = mapEntityTypeToOrgType(entity.getEntityType());

        return GovernmentOrganization.builder()
                .officialName(entity.getAgencyName().trim())
                .branch(branch)
                .orgType(orgType)
                .missionStatement(entity.getMissionStatementText())
                .websiteUrl(entity.getWebAddress())
                .govinfoPackageId(externalId)
                .importSource(IMPORT_SOURCE)
                .createdBy("GOVMAN_IMPORT")
                .updatedBy("GOVMAN_IMPORT")
                .build();
    }

    /**
     * Update an existing organization with data from GovmanEntity.
     */
    private void updateOrganization(GovernmentOrganization org, GovmanEntity entity) {
        // Only update fields that may have changed
        String missionText = entity.getMissionStatementText();
        if (missionText != null) {
            org.setMissionStatement(missionText);
        }
        if (entity.getWebAddress() != null && !entity.getWebAddress().isBlank()) {
            org.setWebsiteUrl(entity.getWebAddress());
        }
        org.setUpdatedBy("GOVMAN_IMPORT");
    }

    /**
     * Resolve parent references after all entities are imported.
     */
    private void resolveParentReferences(List<GovmanEntity> entities,
                                         Map<String, UUID> importedIdMap,
                                         Map<String, GovmanEntity> entityMap,
                                         GovmanImportResult result) {
        for (GovmanEntity entity : entities) {
            if (!entity.hasParent()) {
                continue;
            }

            UUID childId = importedIdMap.get(entity.getEntityId());
            UUID parentId = importedIdMap.get(entity.getParentId());

            if (childId == null) {
                // Child wasn't imported (validation failed or duplicate)
                continue;
            }

            UUID resolvedParentId = parentId;
            if (resolvedParentId == null) {
                // Try to find parent by name in existing orgs
                GovmanEntity parentEntity = entityMap.get(entity.getParentId());
                if (parentEntity != null) {
                    Optional<GovernmentOrganization> existingParent =
                            repository.findByOfficialNameIgnoreCase(parentEntity.getAgencyName().trim());
                    if (existingParent.isPresent()) {
                        resolvedParentId = existingParent.get().getId();
                    }
                }
            }

            if (resolvedParentId != null) {
                // Update child with parent reference
                final UUID finalParentId = resolvedParentId;
                final String agencyName = entity.getAgencyName();
                repository.findById(childId).ifPresent(child -> {
                    child.setParentId(finalParentId);
                    repository.save(child);
                    log.debug("Set parent for {}: parentId={}", agencyName, finalParentId);
                });
            } else {
                log.debug("Could not resolve parent {} for entity {}",
                        entity.getParentId(), entity.getAgencyName());
            }
        }
    }

    /**
     * Map GOVMAN Category to GovernmentBranch.
     *
     * @param category The category from XML (e.g., "Legislative Branch")
     * @return The corresponding GovernmentBranch
     */
    GovernmentBranch mapCategoryToBranch(String category) {
        if (category == null) {
            log.debug("Null category, defaulting to EXECUTIVE");
            return GovernmentBranch.EXECUTIVE;
        }

        String normalized = category.toLowerCase().trim();

        if (normalized.contains("legislative")) {
            return GovernmentBranch.LEGISLATIVE;
        }
        if (normalized.contains("judicial")) {
            return GovernmentBranch.JUDICIAL;
        }
        if (normalized.contains("executive")) {
            return GovernmentBranch.EXECUTIVE;
        }

        log.debug("Unknown category '{}', defaulting to EXECUTIVE", category);
        return GovernmentBranch.EXECUTIVE;
    }

    /**
     * Map GOVMAN EntityType to OrganizationType.
     *
     * @param entityType The entity type from XML
     * @return The corresponding OrganizationType
     */
    OrganizationType mapEntityTypeToOrgType(String entityType) {
        if (entityType == null) {
            return OrganizationType.OFFICE;
        }

        String normalized = entityType.toLowerCase().trim();

        return switch (normalized) {
            case "branch" -> OrganizationType.BRANCH;
            case "department" -> OrganizationType.DEPARTMENT;
            case "agency" -> OrganizationType.INDEPENDENT_AGENCY;
            case "bureau" -> OrganizationType.BUREAU;
            case "commission" -> OrganizationType.COMMISSION;
            case "board" -> OrganizationType.BOARD;
            default -> OrganizationType.OFFICE;
        };
    }

    /**
     * Detect if an entity is a duplicate.
     *
     * @param entity The entity to check
     * @return Optional containing existing organization if duplicate found
     */
    public Optional<GovernmentOrganization> detectDuplicate(GovmanEntity entity) {
        String externalId = "GOVMAN:" + entity.getEntityId();

        // First try exact external_id match
        Optional<GovernmentOrganization> byExternalId = repository.findByGovinfoExternalId(externalId);
        if (byExternalId.isPresent()) {
            return byExternalId;
        }

        // Fall back to name match
        if (entity.getAgencyName() != null) {
            return repository.findByOfficialNameIgnoreCase(entity.getAgencyName().trim());
        }

        return Optional.empty();
    }
}
