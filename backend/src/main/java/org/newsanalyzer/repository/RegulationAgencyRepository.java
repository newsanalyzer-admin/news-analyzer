package org.newsanalyzer.repository;

import org.newsanalyzer.model.RegulationAgency;
import org.newsanalyzer.model.RegulationAgencyId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository for RegulationAgency join table.
 *
 * @author James (Dev Agent)
 * @since 2.0.0
 */
@Repository
public interface RegulationAgencyRepository extends JpaRepository<RegulationAgency, RegulationAgencyId> {

    // =====================================================================
    // Regulation-based Queries
    // =====================================================================

    /**
     * Find all agency links for a specific regulation.
     */
    List<RegulationAgency> findByRegulationId(UUID regulationId);

    /**
     * Find primary agency for a regulation.
     */
    @Query("SELECT ra FROM RegulationAgency ra WHERE ra.regulationId = :regulationId AND ra.primaryAgency = true")
    List<RegulationAgency> findPrimaryAgencyByRegulationId(@Param("regulationId") UUID regulationId);

    /**
     * Delete all agency links for a regulation.
     */
    void deleteByRegulationId(UUID regulationId);

    // =====================================================================
    // Organization-based Queries
    // =====================================================================

    /**
     * Find all regulation links for a specific organization.
     */
    List<RegulationAgency> findByOrganizationId(UUID organizationId);

    /**
     * Get regulation IDs for a specific organization (for filtering).
     */
    @Query("SELECT ra.regulationId FROM RegulationAgency ra WHERE ra.organizationId = :organizationId")
    List<UUID> findRegulationIdsByOrganizationId(@Param("organizationId") UUID organizationId);

    /**
     * Count regulations for an organization.
     */
    @Query("SELECT COUNT(ra) FROM RegulationAgency ra WHERE ra.organizationId = :organizationId")
    long countByOrganizationId(@Param("organizationId") UUID organizationId);

    // =====================================================================
    // Statistics Queries
    // =====================================================================

    /**
     * Count distinct regulations that have at least one agency link.
     */
    @Query("SELECT COUNT(DISTINCT ra.regulationId) FROM RegulationAgency ra")
    long countDistinctRegulations();

    /**
     * Count total regulation-agency links.
     */
    @Query("SELECT COUNT(ra) FROM RegulationAgency ra")
    long countLinkedRegulations();

    /**
     * Count regulations per agency.
     */
    @Query("SELECT ra.organizationId, COUNT(ra) FROM RegulationAgency ra GROUP BY ra.organizationId ORDER BY COUNT(ra) DESC")
    List<Object[]> countRegulationsPerAgency();

    // =====================================================================
    // Existence Checks
    // =====================================================================

    /**
     * Check if a regulation-agency link exists.
     */
    boolean existsByRegulationIdAndOrganizationId(UUID regulationId, UUID organizationId);

    // =====================================================================
    // Batch Queries (Performance Optimization)
    // =====================================================================

    /**
     * Find all agency links for multiple regulations (batch fetch to avoid N+1).
     *
     * @param regulationIds list of regulation IDs to fetch agencies for
     * @return list of all regulation-agency links for the given regulations
     */
    @Query("SELECT ra FROM RegulationAgency ra WHERE ra.regulationId IN :regulationIds")
    List<RegulationAgency> findByRegulationIdIn(@Param("regulationIds") List<UUID> regulationIds);
}
