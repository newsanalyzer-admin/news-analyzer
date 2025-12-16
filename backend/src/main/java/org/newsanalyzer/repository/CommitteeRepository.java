package org.newsanalyzer.repository;

import org.newsanalyzer.model.Committee;
import org.newsanalyzer.model.CommitteeChamber;
import org.newsanalyzer.model.CommitteeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Committee entity.
 *
 * Provides CRUD operations and custom queries for Congressional committee data.
 *
 * @author James (Dev Agent)
 * @since 2.0.0
 */
@Repository
public interface CommitteeRepository extends JpaRepository<Committee, String> {

    // =====================================================================
    // Basic Lookups
    // =====================================================================

    /**
     * Find committee by committee code
     */
    Optional<Committee> findByCommitteeCode(String committeeCode);

    /**
     * Check if committee exists by code
     */
    boolean existsByCommitteeCode(String committeeCode);

    /**
     * Find committee by Thomas ID
     */
    Optional<Committee> findByThomasId(String thomasId);

    // =====================================================================
    // By Chamber
    // =====================================================================

    /**
     * Find all committees by chamber
     */
    List<Committee> findByChamber(CommitteeChamber chamber);

    /**
     * Find all committees by chamber with pagination
     */
    Page<Committee> findByChamber(CommitteeChamber chamber, Pageable pageable);

    /**
     * Count committees by chamber
     */
    long countByChamber(CommitteeChamber chamber);

    // =====================================================================
    // By Type
    // =====================================================================

    /**
     * Find all committees by type
     */
    List<Committee> findByCommitteeType(CommitteeType committeeType);

    /**
     * Find all committees by type with pagination
     */
    Page<Committee> findByCommitteeType(CommitteeType committeeType, Pageable pageable);

    /**
     * Find committees by chamber and type
     */
    List<Committee> findByChamberAndCommitteeType(CommitteeChamber chamber, CommitteeType committeeType);

    // =====================================================================
    // Subcommittees
    // =====================================================================

    /**
     * Find all subcommittees of a parent committee
     */
    List<Committee> findByParentCommittee_CommitteeCode(String parentCommitteeCode);

    /**
     * Find all subcommittees of a parent committee with pagination
     */
    Page<Committee> findByParentCommittee_CommitteeCode(String parentCommitteeCode, Pageable pageable);

    /**
     * Find all top-level committees (no parent)
     */
    List<Committee> findByParentCommitteeIsNull();

    /**
     * Find all top-level committees by chamber
     */
    List<Committee> findByChamberAndParentCommitteeIsNull(CommitteeChamber chamber);

    /**
     * Count subcommittees for a parent
     */
    long countByParentCommittee_CommitteeCode(String parentCommitteeCode);

    // =====================================================================
    // Name Search
    // =====================================================================

    /**
     * Search by name containing (case-insensitive)
     */
    List<Committee> findByNameContainingIgnoreCase(String name);

    /**
     * Search by name containing with pagination
     */
    Page<Committee> findByNameContainingIgnoreCase(String name, Pageable pageable);

    /**
     * Full-text search using PostgreSQL ts_vector on name
     */
    @Query(value = "SELECT * FROM committees c " +
           "WHERE to_tsvector('english', c.name) @@ plainto_tsquery('english', :query)",
           nativeQuery = true)
    List<Committee> fullTextSearchByName(@Param("query") String query);

    // =====================================================================
    // Statistics
    // =====================================================================

    /**
     * Get type distribution
     */
    @Query("SELECT c.committeeType, COUNT(c) FROM Committee c GROUP BY c.committeeType ORDER BY COUNT(c) DESC")
    List<Object[]> getTypeDistribution();

    /**
     * Get chamber distribution
     */
    @Query("SELECT c.chamber, COUNT(c) FROM Committee c GROUP BY c.chamber ORDER BY c.chamber")
    List<Object[]> getChamberDistribution();

    /**
     * Count total subcommittees
     */
    @Query("SELECT COUNT(c) FROM Committee c WHERE c.parentCommittee IS NOT NULL")
    long countSubcommittees();

    /**
     * Count total parent committees
     */
    @Query("SELECT COUNT(c) FROM Committee c WHERE c.parentCommittee IS NULL")
    long countParentCommittees();
}
