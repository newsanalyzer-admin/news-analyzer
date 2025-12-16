package org.newsanalyzer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.newsanalyzer.exception.ResourceNotFoundException;
import org.newsanalyzer.model.GovernmentOrganization;
import org.newsanalyzer.model.GovernmentOrganization.GovernmentBranch;
import org.newsanalyzer.model.GovernmentOrganization.OrganizationType;
import org.newsanalyzer.repository.GovernmentOrganizationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for managing government organizations.
 *
 * Provides business logic for:
 * - CRUD operations
 * - Search and filtering
 * - Hierarchy navigation
 * - Entity validation
 * - Data quality checks
 *
 * @author Winston (Architect Agent)
 * @since 2.0.0
 */
@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GovernmentOrganizationService {

    private final GovernmentOrganizationRepository repository;

    // =====================================================================
    // CRUD Operations
    // =====================================================================

    /**
     * Get organization by ID
     */
    public Optional<GovernmentOrganization> findById(UUID id) {
        return repository.findById(id);
    }

    /**
     * Get all organizations with pagination
     */
    public Page<GovernmentOrganization> findAll(Pageable pageable) {
        return repository.findAll(pageable);
    }

    /**
     * Get all active organizations
     */
    public List<GovernmentOrganization> findAllActive() {
        return repository.findAllActive();
    }

    /**
     * Get all active organizations with pagination
     */
    public Page<GovernmentOrganization> findAllActive(Pageable pageable) {
        return repository.findAllActive(pageable);
    }

    /**
     * Create new organization
     */
    @Transactional
    public GovernmentOrganization create(GovernmentOrganization organization) {
        validateOrganization(organization);
        log.info("Creating government organization: {}", organization.getOfficialName());
        return repository.save(organization);
    }

    /**
     * Update existing organization
     */
    @Transactional
    public GovernmentOrganization update(UUID id, GovernmentOrganization organization) {
        GovernmentOrganization existing = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("GovernmentOrganization", id));

        validateOrganization(organization);

        // Update fields
        existing.setOfficialName(organization.getOfficialName());
        existing.setAcronym(organization.getAcronym());
        existing.setOrgType(organization.getOrgType());
        existing.setBranch(organization.getBranch());
        existing.setParentId(organization.getParentId());
        existing.setOrgLevel(organization.getOrgLevel());
        existing.setMissionStatement(organization.getMissionStatement());
        existing.setDescription(organization.getDescription());
        existing.setWebsiteUrl(organization.getWebsiteUrl());
        existing.setJurisdictionAreas(organization.getJurisdictionAreas());
        existing.setSchemaOrgData(organization.getSchemaOrgData());
        existing.setUpdatedBy(organization.getUpdatedBy());

        log.info("Updated government organization: {}", existing.getOfficialName());
        return repository.save(existing);
    }

    /**
     * Delete organization (soft delete by setting dissolved date)
     */
    @Transactional
    public void delete(UUID id) {
        GovernmentOrganization organization = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("GovernmentOrganization", id));

        organization.setDissolvedDate(LocalDate.now());
        repository.save(organization);
        log.info("Soft deleted government organization: {}", organization.getOfficialName());
    }

    // =====================================================================
    // Search Operations
    // =====================================================================

    /**
     * Find organization by name or acronym
     */
    public Optional<GovernmentOrganization> findByNameOrAcronym(String nameOrAcronym) {
        // Try exact name match first
        Optional<GovernmentOrganization> byName = repository.findByOfficialName(nameOrAcronym);
        if (byName.isPresent()) {
            return byName;
        }

        // Try acronym match
        return repository.findByAcronymIgnoreCase(nameOrAcronym);
    }

    /**
     * Search organizations (LIKE query)
     */
    public List<GovernmentOrganization> search(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return Collections.emptyList();
        }
        return repository.searchByName(searchTerm.trim());
    }

    /**
     * Fuzzy search using trigram similarity
     */
    public List<GovernmentOrganization> fuzzySearch(String searchText) {
        if (searchText == null || searchText.trim().isEmpty()) {
            return Collections.emptyList();
        }
        return repository.fuzzySearch(searchText.trim());
    }

    /**
     * Full-text search
     */
    public List<GovernmentOrganization> fullTextSearch(String query) {
        if (query == null || query.trim().isEmpty()) {
            return Collections.emptyList();
        }
        return repository.fullTextSearch(query.trim());
    }

    // =====================================================================
    // Filtering Operations
    // =====================================================================

    /**
     * Get Cabinet departments
     */
    public List<GovernmentOrganization> getCabinetDepartments() {
        return repository.findCabinetDepartments();
    }

    /**
     * Get independent agencies
     */
    public List<GovernmentOrganization> getIndependentAgencies() {
        return repository.findIndependentAgencies();
    }

    /**
     * Get organizations by type
     */
    public List<GovernmentOrganization> findByType(OrganizationType type) {
        return repository.findByOrgType(type);
    }

    /**
     * Get organizations by branch
     */
    public List<GovernmentOrganization> findByBranch(GovernmentBranch branch) {
        return repository.findByBranch(branch);
    }

    /**
     * Get organizations by jurisdiction area
     */
    public List<GovernmentOrganization> findByJurisdiction(String jurisdictionArea) {
        return repository.findByJurisdictionArea(jurisdictionArea);
    }

    // =====================================================================
    // Hierarchy Operations
    // =====================================================================

    /**
     * Get organization hierarchy (parents and children)
     */
    public OrganizationHierarchy getHierarchy(UUID organizationId) {
        GovernmentOrganization org = repository.findById(organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("GovernmentOrganization", organizationId));

        List<GovernmentOrganization> ancestors = repository.findAllAncestors(organizationId);
        List<GovernmentOrganization> children = repository.findActiveChildrenByParentId(organizationId);

        return OrganizationHierarchy.builder()
                .organization(org)
                .ancestors(ancestors)
                .children(children)
                .build();
    }

    /**
     * Get all descendants of an organization
     */
    public List<GovernmentOrganization> getDescendants(UUID parentId) {
        return repository.findAllDescendants(parentId);
    }

    /**
     * Get all ancestors of an organization
     */
    public List<GovernmentOrganization> getAncestors(UUID organizationId) {
        return repository.findAllAncestors(organizationId);
    }

    /**
     * Get top-level organizations
     */
    public List<GovernmentOrganization> getTopLevelOrganizations() {
        return repository.findTopLevelOrganizations();
    }

    // =====================================================================
    // Validation Operations
    // =====================================================================

    /**
     * Validate entity text against government organizations
     */
    public EntityValidationResult validateEntity(String entityText, String entityType) {
        if (!"government_org".equalsIgnoreCase(entityType)) {
            return EntityValidationResult.notApplicable();
        }

        // Try exact match
        Optional<GovernmentOrganization> exactMatch = findByNameOrAcronym(entityText);
        if (exactMatch.isPresent()) {
            return EntityValidationResult.valid(exactMatch.get(), 1.0, "exact");
        }

        // Try fuzzy search
        List<GovernmentOrganization> fuzzyMatches = fuzzySearch(entityText);
        if (!fuzzyMatches.isEmpty()) {
            GovernmentOrganization bestMatch = fuzzyMatches.get(0);
            double confidence = 0.85; // Fuzzy match confidence

            List<String> suggestions = fuzzyMatches.stream()
                    .limit(3)
                    .map(GovernmentOrganization::getOfficialName)
                    .collect(Collectors.toList());

            return EntityValidationResult.valid(bestMatch, confidence, "fuzzy", suggestions);
        }

        // No match found
        List<String> suggestions = search(entityText).stream()
                .limit(3)
                .map(GovernmentOrganization::getOfficialName)
                .collect(Collectors.toList());

        return EntityValidationResult.invalid(suggestions);
    }

    /**
     * Validate organization data
     */
    private void validateOrganization(GovernmentOrganization org) {
        if (org.getOfficialName() == null || org.getOfficialName().trim().isEmpty()) {
            throw new IllegalArgumentException("Official name is required");
        }

        if (org.getOrgType() == null) {
            throw new IllegalArgumentException("Organization type is required");
        }

        if (org.getBranch() == null) {
            throw new IllegalArgumentException("Government branch is required");
        }

        // Check for duplicate acronym in same branch
        if (org.getAcronym() != null && !org.getAcronym().isEmpty()) {
            if (org.getId() != null) {
                boolean duplicate = repository.existsByAcronymExcludingId(org.getAcronym(), org.getId());
                if (duplicate) {
                    throw new IllegalArgumentException(
                            "Acronym already exists in this branch: " + org.getAcronym());
                }
            } else {
                boolean duplicate = repository.existsByAcronym(org.getAcronym());
                if (duplicate) {
                    throw new IllegalArgumentException(
                            "Acronym already exists: " + org.getAcronym());
                }
            }
        }

        // Validate date logic
        if (org.getDissolvedDate() != null && org.getEstablishedDate() != null) {
            if (org.getDissolvedDate().isBefore(org.getEstablishedDate())) {
                throw new IllegalArgumentException(
                        "Dissolved date cannot be before established date");
            }
        }
    }

    // =====================================================================
    // Statistics Operations
    // =====================================================================

    /**
     * Get organization statistics
     */
    public OrganizationStatistics getStatistics() {
        long totalActive = repository.countActive();
        List<Object[]> byType = repository.countByType();
        List<Object[]> byBranch = repository.countByBranch();

        Map<String, Long> typeStats = byType.stream()
                .collect(Collectors.toMap(
                        arr -> ((OrganizationType) arr[0]).getValue(),
                        arr -> (Long) arr[1]
                ));

        Map<String, Long> branchStats = byBranch.stream()
                .collect(Collectors.toMap(
                        arr -> ((GovernmentBranch) arr[0]).getValue(),
                        arr -> (Long) arr[1]
                ));

        return OrganizationStatistics.builder()
                .totalActive(totalActive)
                .byType(typeStats)
                .byBranch(branchStats)
                .build();
    }

    // =====================================================================
    // DTOs
    // =====================================================================

    /**
     * Organization hierarchy DTO
     */
    @lombok.Data
    @lombok.Builder
    public static class OrganizationHierarchy {
        private GovernmentOrganization organization;
        private List<GovernmentOrganization> ancestors;
        private List<GovernmentOrganization> children;
    }

    /**
     * Entity validation result
     */
    @lombok.Data
    @lombok.Builder
    public static class EntityValidationResult {
        private boolean valid;
        private boolean applicable; // Whether this validator applies to entity type
        private double confidence;
        private String matchType; // "exact", "acronym", "fuzzy", "none"
        private GovernmentOrganization matchedOrganization;
        private List<String> suggestions;

        public static EntityValidationResult notApplicable() {
            return EntityValidationResult.builder()
                    .applicable(false)
                    .valid(false)
                    .confidence(0.0)
                    .matchType("not_applicable")
                    .suggestions(Collections.emptyList())
                    .build();
        }

        public static EntityValidationResult valid(
                GovernmentOrganization org,
                double confidence,
                String matchType) {
            return EntityValidationResult.builder()
                    .applicable(true)
                    .valid(true)
                    .confidence(confidence)
                    .matchType(matchType)
                    .matchedOrganization(org)
                    .suggestions(Collections.emptyList())
                    .build();
        }

        public static EntityValidationResult valid(
                GovernmentOrganization org,
                double confidence,
                String matchType,
                List<String> suggestions) {
            return EntityValidationResult.builder()
                    .applicable(true)
                    .valid(true)
                    .confidence(confidence)
                    .matchType(matchType)
                    .matchedOrganization(org)
                    .suggestions(suggestions)
                    .build();
        }

        public static EntityValidationResult invalid(List<String> suggestions) {
            return EntityValidationResult.builder()
                    .applicable(true)
                    .valid(false)
                    .confidence(0.0)
                    .matchType("none")
                    .suggestions(suggestions)
                    .build();
        }
    }

    /**
     * Organization statistics DTO
     */
    @lombok.Data
    @lombok.Builder
    public static class OrganizationStatistics {
        private long totalActive;
        private Map<String, Long> byType;
        private Map<String, Long> byBranch;
    }
}
