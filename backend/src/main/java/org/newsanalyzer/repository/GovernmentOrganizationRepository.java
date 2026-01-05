package org.newsanalyzer.repository;

import org.newsanalyzer.model.GovernmentOrganization;
import org.newsanalyzer.model.GovernmentOrganization.GovernmentBranch;
import org.newsanalyzer.model.GovernmentOrganization.OrganizationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for GovernmentOrganization entity.
 *
 * Provides CRUD operations and custom queries for government organizational data.
 * Includes fuzzy search, hierarchy queries, and jurisdiction filtering.
 *
 * @author Winston (Architect Agent)
 * @since 2.0.0
 */
@Repository
public interface GovernmentOrganizationRepository extends JpaRepository<GovernmentOrganization, UUID> {

    // =====================================================================
    // Basic Lookups
    // =====================================================================

    /**
     * Find organization by exact official name
     */
    Optional<GovernmentOrganization> findByOfficialName(String officialName);

    /**
     * Find organization by acronym
     */
    Optional<GovernmentOrganization> findByAcronym(String acronym);

    /**
     * Find organization by acronym (case-insensitive)
     */
    @Query("SELECT o FROM GovernmentOrganization o WHERE LOWER(o.acronym) = LOWER(:acronym)")
    Optional<GovernmentOrganization> findByAcronymIgnoreCase(@Param("acronym") String acronym);

    /**
     * Find organization by official name (case-insensitive)
     */
    @Query("SELECT o FROM GovernmentOrganization o WHERE LOWER(o.officialName) = LOWER(:officialName)")
    Optional<GovernmentOrganization> findByOfficialNameIgnoreCase(@Param("officialName") String officialName);

    /**
     * Find organization by GovInfo package ID
     */
    Optional<GovernmentOrganization> findByGovinfoPackageId(String packageId);

    /**
     * Find organization by Federal Register agency ID
     */
    Optional<GovernmentOrganization> findByFederalRegisterAgencyId(Integer federalRegisterAgencyId);

    // =====================================================================
    // Active/Inactive Organizations
    // =====================================================================

    /**
     * Find all active organizations (not dissolved)
     */
    @Query("SELECT o FROM GovernmentOrganization o WHERE o.dissolvedDate IS NULL")
    List<GovernmentOrganization> findAllActive();

    /**
     * Find all active organizations with pagination
     */
    @Query("SELECT o FROM GovernmentOrganization o WHERE o.dissolvedDate IS NULL")
    Page<GovernmentOrganization> findAllActive(Pageable pageable);

    /**
     * Find dissolved organizations
     */
    @Query("SELECT o FROM GovernmentOrganization o WHERE o.dissolvedDate IS NOT NULL")
    List<GovernmentOrganization> findAllDissolved();

    /**
     * Find organizations dissolved after a specific date
     */
    @Query("SELECT o FROM GovernmentOrganization o WHERE o.dissolvedDate > :date")
    List<GovernmentOrganization> findDissolvedAfter(@Param("date") LocalDate date);

    // =====================================================================
    // By Type and Branch
    // =====================================================================

    /**
     * Find organizations by type
     */
    List<GovernmentOrganization> findByOrgType(OrganizationType orgType);

    /**
     * Find organizations by branch
     */
    List<GovernmentOrganization> findByBranch(GovernmentBranch branch);

    /**
     * Find organizations by type and branch
     */
    List<GovernmentOrganization> findByOrgTypeAndBranch(
            OrganizationType orgType,
            GovernmentBranch branch
    );

    /**
     * Find organizations by type, branch, and level
     */
    List<GovernmentOrganization> findByOrgTypeAndBranchAndOrgLevel(
            OrganizationType orgType,
            GovernmentBranch branch,
            Integer orgLevel
    );

    /**
     * Find Cabinet departments (15 departments)
     */
    @Query("SELECT o FROM GovernmentOrganization o " +
           "WHERE o.orgType = org.newsanalyzer.model.GovernmentOrganization$OrganizationType.DEPARTMENT " +
           "AND o.branch = org.newsanalyzer.model.GovernmentOrganization$GovernmentBranch.EXECUTIVE " +
           "AND o.orgLevel = 1 " +
           "AND o.dissolvedDate IS NULL " +
           "ORDER BY o.officialName")
    List<GovernmentOrganization> findCabinetDepartments();

    /**
     * Find independent agencies
     */
    @Query("SELECT o FROM GovernmentOrganization o " +
           "WHERE o.orgType = org.newsanalyzer.model.GovernmentOrganization$OrganizationType.INDEPENDENT_AGENCY " +
           "AND o.branch = org.newsanalyzer.model.GovernmentOrganization$GovernmentBranch.EXECUTIVE " +
           "AND o.parentId IS NULL " +
           "AND o.dissolvedDate IS NULL " +
           "ORDER BY o.officialName")
    List<GovernmentOrganization> findIndependentAgencies();

    /**
     * Find government corporations (UI-6.0)
     */
    @Query("SELECT o FROM GovernmentOrganization o " +
           "WHERE o.orgType = org.newsanalyzer.model.GovernmentOrganization$OrganizationType.GOVERNMENT_CORPORATION " +
           "AND o.branch = org.newsanalyzer.model.GovernmentOrganization$GovernmentBranch.EXECUTIVE " +
           "AND o.dissolvedDate IS NULL " +
           "ORDER BY o.officialName")
    List<GovernmentOrganization> findGovernmentCorporations();

    // =====================================================================
    // Hierarchy Queries
    // =====================================================================

    /**
     * Find all children of a parent organization
     */
    List<GovernmentOrganization> findByParentId(UUID parentId);

    /**
     * Find all active children of a parent organization
     */
    @Query("SELECT o FROM GovernmentOrganization o " +
           "WHERE o.parentId = :parentId " +
           "AND o.dissolvedDate IS NULL")
    List<GovernmentOrganization> findActiveChildrenByParentId(@Param("parentId") UUID parentId);

    /**
     * Find top-level organizations (no parent)
     */
    @Query("SELECT o FROM GovernmentOrganization o " +
           "WHERE o.parentId IS NULL " +
           "AND o.dissolvedDate IS NULL " +
           "ORDER BY o.orgLevel, o.officialName")
    List<GovernmentOrganization> findTopLevelOrganizations();

    /**
     * Find organizations by hierarchical level
     */
    List<GovernmentOrganization> findByOrgLevel(Integer orgLevel);

    /**
     * Get all descendants recursively using SQL function
     */
    @Query(value = "SELECT o.* FROM government_organizations o " +
                   "JOIN get_child_organizations(:parentId) c ON o.id = c.id",
           nativeQuery = true)
    List<GovernmentOrganization> findAllDescendants(@Param("parentId") UUID parentId);

    /**
     * Get all ancestors recursively using SQL function
     * Note: Excludes the organization itself (depth > 0 filters out the starting org)
     */
    @Query(value = "SELECT o.* FROM government_organizations o " +
                   "JOIN get_organization_ancestry(:orgId) a ON o.id = a.id " +
                   "WHERE a.depth > 0",
           nativeQuery = true)
    List<GovernmentOrganization> findAllAncestors(@Param("orgId") UUID orgId);

    // =====================================================================
    // Search and Filtering
    // =====================================================================

    /**
     * Search organizations by name (LIKE query)
     */
    @Query("SELECT o FROM GovernmentOrganization o " +
           "WHERE LOWER(o.officialName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "OR LOWER(o.acronym) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<GovernmentOrganization> searchByName(@Param("searchTerm") String searchTerm);

    /**
     * Find organizations by official name containing text (case-insensitive)
     */
    List<GovernmentOrganization> findByOfficialNameContainingIgnoreCase(String name);

    /**
     * Fuzzy search using trigram similarity (PostgreSQL function)
     */
    @Query(value = "SELECT go.* FROM government_organizations go " +
                   "JOIN search_government_organizations(:searchText) s ON go.id = s.id " +
                   "ORDER BY s.similarity_score DESC",
           nativeQuery = true)
    List<GovernmentOrganization> fuzzySearch(@Param("searchText") String searchText);

    /**
     * Full-text search
     */
    @Query(value = "SELECT o.* FROM government_organizations o " +
                   "WHERE to_tsvector('english', " +
                   "    coalesce(o.official_name, '') || ' ' || " +
                   "    coalesce(o.acronym, '') || ' ' || " +
                   "    coalesce(o.mission_statement, '') || ' ' || " +
                   "    coalesce(o.description, '')" +
                   ") @@ plainto_tsquery('english', :query) " +
                   "AND o.dissolved_date IS NULL",
           nativeQuery = true)
    List<GovernmentOrganization> fullTextSearch(@Param("query") String query);

    // =====================================================================
    // Jurisdiction Queries
    // =====================================================================

    /**
     * Find organizations by jurisdiction area (array contains)
     */
    @Query(value = "SELECT * FROM government_organizations " +
                   "WHERE :jurisdictionArea = ANY(jurisdiction_areas) " +
                   "AND dissolved_date IS NULL",
           nativeQuery = true)
    List<GovernmentOrganization> findByJurisdictionArea(@Param("jurisdictionArea") String jurisdictionArea);

    /**
     * Find organizations with any of the specified jurisdictions
     */
    @Query(value = "SELECT * FROM government_organizations " +
                   "WHERE jurisdiction_areas && CAST(:jurisdictions AS text[]) " +
                   "AND dissolved_date IS NULL",
           nativeQuery = true)
    List<GovernmentOrganization> findByAnyJurisdiction(@Param("jurisdictions") String[] jurisdictions);

    /**
     * Find organizations by website domain
     */
    @Query("SELECT o FROM GovernmentOrganization o " +
           "WHERE o.websiteUrl LIKE CONCAT('%', :domain, '%')")
    List<GovernmentOrganization> findByWebsiteDomain(@Param("domain") String domain);

    // =====================================================================
    // Temporal Queries
    // =====================================================================

    /**
     * Find organizations established in a specific year
     */
    @Query("SELECT o FROM GovernmentOrganization o " +
           "WHERE YEAR(o.establishedDate) = :year")
    List<GovernmentOrganization> findEstablishedInYear(@Param("year") int year);

    /**
     * Find organizations established between dates
     */
    @Query("SELECT o FROM GovernmentOrganization o " +
           "WHERE o.establishedDate BETWEEN :startDate AND :endDate")
    List<GovernmentOrganization> findEstablishedBetween(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    /**
     * Find recently created organizations (last N days)
     */
    @Query("SELECT o FROM GovernmentOrganization o " +
           "WHERE o.establishedDate >= :sinceDate " +
           "AND o.dissolvedDate IS NULL " +
           "ORDER BY o.establishedDate DESC")
    List<GovernmentOrganization> findRecentlyEstablished(@Param("sinceDate") LocalDate sinceDate);

    // =====================================================================
    // Data Quality
    // =====================================================================

    /**
     * Find organizations with low data quality score
     */
    @Query("SELECT o FROM GovernmentOrganization o " +
           "WHERE o.dataQualityScore < :threshold " +
           "ORDER BY o.dataQualityScore ASC")
    List<GovernmentOrganization> findLowQualityData(@Param("threshold") double threshold);

    /**
     * Find organizations missing critical data
     */
    @Query("SELECT o FROM GovernmentOrganization o " +
           "WHERE o.websiteUrl IS NULL " +
           "OR o.missionStatement IS NULL " +
           "OR o.establishedDate IS NULL")
    List<GovernmentOrganization> findMissingCriticalData();

    /**
     * Find organizations not synced recently
     */
    @Query("SELECT o FROM GovernmentOrganization o " +
           "WHERE o.govinfoLastSync IS NULL " +
           "OR o.govinfoLastSync < :thresholdDate")
    List<GovernmentOrganization> findNeedingSync(@Param("thresholdDate") LocalDate thresholdDate);

    // =====================================================================
    // Statistics
    // =====================================================================

    /**
     * Count organizations by type
     */
    @Query("SELECT o.orgType, COUNT(o) FROM GovernmentOrganization o " +
           "WHERE o.dissolvedDate IS NULL " +
           "GROUP BY o.orgType")
    List<Object[]> countByType();

    /**
     * Count organizations by branch
     */
    @Query("SELECT o.branch, COUNT(o) FROM GovernmentOrganization o " +
           "WHERE o.dissolvedDate IS NULL " +
           "GROUP BY o.branch")
    List<Object[]> countByBranch();

    /**
     * Count total active organizations
     */
    @Query("SELECT COUNT(o) FROM GovernmentOrganization o WHERE o.dissolvedDate IS NULL")
    long countActive();

    /**
     * Count organizations by GovInfo year
     */
    @Query("SELECT o.govinfoYear, COUNT(o) FROM GovernmentOrganization o " +
           "GROUP BY o.govinfoYear " +
           "ORDER BY o.govinfoYear DESC")
    List<Object[]> countByGovinfoYear();

    // =====================================================================
    // Validation
    // =====================================================================

    /**
     * Check if organization name exists
     */
    boolean existsByOfficialName(String officialName);

    /**
     * Check if acronym exists
     */
    boolean existsByAcronym(String acronym);

    /**
     * Check if acronym exists for a different organization
     */
    @Query("SELECT CASE WHEN COUNT(o) > 0 THEN true ELSE false END " +
           "FROM GovernmentOrganization o " +
           "WHERE o.acronym = :acronym AND o.id != :id")
    boolean existsByAcronymExcludingId(@Param("acronym") String acronym, @Param("id") UUID id);

    /**
     * Find duplicate acronyms within the same branch
     */
    @Query(value = "SELECT branch, acronym, COUNT(*) as count " +
                   "FROM government_organizations " +
                   "WHERE acronym IS NOT NULL " +
                   "GROUP BY branch, acronym " +
                   "HAVING COUNT(*) > 1",
           nativeQuery = true)
    List<Object[]> findDuplicateAcronyms();

    // =====================================================================
    // Import Source Queries
    // =====================================================================

    /**
     * Find organizations by import source
     */
    List<GovernmentOrganization> findByImportSource(String importSource);

    /**
     * Find organization by GovInfo package ID (used as external ID for GOVMAN imports)
     */
    @Query("SELECT o FROM GovernmentOrganization o WHERE o.govinfoPackageId = :packageId")
    Optional<GovernmentOrganization> findByGovinfoExternalId(@Param("packageId") String packageId);
}
