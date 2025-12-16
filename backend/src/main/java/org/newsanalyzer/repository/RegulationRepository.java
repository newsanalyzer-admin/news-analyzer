package org.newsanalyzer.repository;

import org.newsanalyzer.model.DocumentType;
import org.newsanalyzer.model.Regulation;
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
 * Repository for Regulation entities with custom query methods.
 *
 * @author James (Dev Agent)
 * @since 2.0.0
 */
@Repository
public interface RegulationRepository extends JpaRepository<Regulation, UUID> {

    // =====================================================================
    // Basic Lookup Queries
    // =====================================================================

    /**
     * Find regulation by Federal Register document number.
     */
    Optional<Regulation> findByDocumentNumber(String documentNumber);

    /**
     * Check if a regulation exists by document number.
     */
    boolean existsByDocumentNumber(String documentNumber);

    // =====================================================================
    // Date Range Queries
    // =====================================================================

    /**
     * Find regulations published between two dates (paginated).
     */
    Page<Regulation> findByPublicationDateBetween(LocalDate start, LocalDate end, Pageable pageable);

    /**
     * Find regulations effective between two dates (paginated).
     */
    Page<Regulation> findByEffectiveOnBetween(LocalDate start, LocalDate end, Pageable pageable);

    /**
     * Find final rules effective on or before a specific date.
     */
    @Query("SELECT r FROM Regulation r WHERE r.effectiveOn <= :date AND r.documentType = 'RULE' ORDER BY r.effectiveOn DESC")
    List<Regulation> findRulesEffectiveOnOrBefore(@Param("date") LocalDate date);

    // =====================================================================
    // Type Queries
    // =====================================================================

    /**
     * Find regulations by document type (paginated).
     */
    Page<Regulation> findByDocumentType(DocumentType type, Pageable pageable);

    /**
     * Count regulations by document type.
     */
    long countByDocumentType(DocumentType type);

    // =====================================================================
    // Full-Text Search Queries
    // =====================================================================

    /**
     * Full-text search on title and abstract using PostgreSQL tsvector.
     */
    @Query(value = "SELECT * FROM regulations WHERE search_vector @@ plainto_tsquery('english', :query)",
           countQuery = "SELECT COUNT(*) FROM regulations WHERE search_vector @@ plainto_tsquery('english', :query)",
           nativeQuery = true)
    Page<Regulation> searchByTitleOrAbstract(@Param("query") String query, Pageable pageable);

    // =====================================================================
    // CFR Reference Queries
    // =====================================================================

    /**
     * Find regulations by CFR reference using JSONB contains operator.
     *
     * @param cfrJson JSON string like '[{"title": 40, "part": 60}]'
     */
    @Query(value = "SELECT * FROM regulations WHERE cfr_references @> :cfrJson::jsonb",
           nativeQuery = true)
    List<Regulation> findByCfrReference(@Param("cfrJson") String cfrJson);

    // =====================================================================
    // ID-based Queries (for agency filtering)
    // =====================================================================

    /**
     * Find regulations by list of IDs (for agency filtering).
     */
    Page<Regulation> findByIdIn(List<UUID> ids, Pageable pageable);

    /**
     * Find regulations by list of IDs (non-paginated).
     */
    List<Regulation> findByIdIn(List<UUID> ids);

    // =====================================================================
    // Statistics Queries
    // =====================================================================

    /**
     * Get the most recently published regulation.
     */
    Optional<Regulation> findTopByOrderByPublicationDateDesc();

    /**
     * Count all regulations.
     */
    @Query("SELECT COUNT(r) FROM Regulation r")
    long countAll();

    /**
     * Count regulations by year.
     */
    @Query(value = "SELECT EXTRACT(YEAR FROM publication_date) as year, COUNT(*) as count " +
                   "FROM regulations GROUP BY EXTRACT(YEAR FROM publication_date) ORDER BY year DESC",
           nativeQuery = true)
    List<Object[]> countByYear();

    // =====================================================================
    // Agency Filtering Queries (Performance Optimization)
    // =====================================================================

    /**
     * Find regulations by agency ID using direct JOIN (more efficient than ID list).
     *
     * @param orgId the government organization ID
     * @param pageable pagination parameters
     * @return page of regulations issued by the specified agency
     */
    @Query("SELECT r FROM Regulation r JOIN RegulationAgency ra ON r.id = ra.regulationId " +
           "WHERE ra.organizationId = :orgId ORDER BY r.publicationDate DESC")
    Page<Regulation> findByAgencyId(@Param("orgId") UUID orgId, Pageable pageable);
}
