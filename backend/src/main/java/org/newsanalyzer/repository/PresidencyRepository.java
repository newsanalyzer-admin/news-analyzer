package org.newsanalyzer.repository;

import org.newsanalyzer.model.DataSource;
import org.newsanalyzer.model.Presidency;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for Presidency entity.
 *
 * Provides CRUD operations and custom queries for presidential term data.
 *
 * @author James (Dev Agent)
 * @since 2.0.0
 */
@Repository
public interface PresidencyRepository extends JpaRepository<Presidency, UUID> {

    // =====================================================================
    // Basic Lookups
    // =====================================================================

    /**
     * Find presidency by number (1-47).
     */
    Optional<Presidency> findByNumber(Integer number);

    /**
     * Check if presidency exists by number.
     */
    boolean existsByNumber(Integer number);

    // =====================================================================
    // Current Presidency
    // =====================================================================

    /**
     * Find the current presidency (no end date, highest number).
     */
    Optional<Presidency> findFirstByEndDateIsNullOrderByNumberDesc();

    /**
     * Find all presidencies without end date (should be only one in normal state).
     */
    List<Presidency> findByEndDateIsNull();

    // =====================================================================
    // By Individual (for non-consecutive terms)
    // =====================================================================

    /**
     * Find all presidencies for a given individual (handles non-consecutive terms).
     * E.g., Cleveland (22nd & 24th) or Trump (45th & 47th).
     */
    List<Presidency> findByIndividualId(UUID individualId);

    /**
     * Find all presidencies for a given individual ordered by number.
     */
    List<Presidency> findByIndividualIdOrderByNumberAsc(UUID individualId);

    /**
     * Count presidencies for a given individual.
     */
    long countByIndividualId(UUID individualId);

    // =====================================================================
    // Ordered Lists
    // =====================================================================

    /**
     * Find all presidencies ordered by number descending (most recent first).
     */
    List<Presidency> findAllByOrderByNumberDesc();

    /**
     * Find all presidencies ordered by number ascending (chronological).
     */
    List<Presidency> findAllByOrderByNumberAsc();

    /**
     * Find all presidencies with pagination, ordered by number descending.
     */
    Page<Presidency> findAllByOrderByNumberDesc(Pageable pageable);

    // =====================================================================
    // By Party
    // =====================================================================

    /**
     * Find all presidencies by political party.
     */
    List<Presidency> findByParty(String party);

    /**
     * Find all presidencies by party (case-insensitive).
     */
    List<Presidency> findByPartyIgnoreCase(String party);

    /**
     * Count presidencies by party.
     */
    long countByParty(String party);

    /**
     * Get party distribution.
     */
    @Query("SELECT p.party, COUNT(p) FROM Presidency p GROUP BY p.party ORDER BY COUNT(p) DESC")
    List<Object[]> getPartyDistribution();

    // =====================================================================
    // By Data Source
    // =====================================================================

    /**
     * Find all presidencies by data source.
     */
    List<Presidency> findByDataSource(DataSource dataSource);

    /**
     * Count presidencies by data source.
     */
    long countByDataSource(DataSource dataSource);

    // =====================================================================
    // Range Queries
    // =====================================================================

    /**
     * Find presidencies within a number range (inclusive).
     */
    @Query("SELECT p FROM Presidency p WHERE p.number >= :start AND p.number <= :end ORDER BY p.number ASC")
    List<Presidency> findByNumberRange(@Param("start") Integer start, @Param("end") Integer end);

    /**
     * Find presidencies in the 20th century (26-43).
     */
    @Query("SELECT p FROM Presidency p WHERE p.number >= 26 AND p.number <= 43 ORDER BY p.number ASC")
    List<Presidency> find20thCenturyPresidencies();

    /**
     * Find presidencies in the 21st century (43+).
     */
    @Query("SELECT p FROM Presidency p WHERE p.number >= 43 ORDER BY p.number ASC")
    List<Presidency> find21stCenturyPresidencies();

    // =====================================================================
    // Statistics
    // =====================================================================

    /**
     * Get the highest presidency number.
     */
    @Query("SELECT MAX(p.number) FROM Presidency p")
    Integer findMaxNumber();

    /**
     * Count total presidencies.
     */
    @Query("SELECT COUNT(p) FROM Presidency p")
    long countTotal();
}
