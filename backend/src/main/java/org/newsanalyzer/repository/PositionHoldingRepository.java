package org.newsanalyzer.repository;

import org.newsanalyzer.model.DataSource;
import org.newsanalyzer.model.Person.Chamber;
import org.newsanalyzer.model.PositionHolding;
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
 * Repository for PositionHolding entities with custom date range queries.
 *
 * @author James (Dev Agent)
 * @since 2.0.0
 */
@Repository
public interface PositionHoldingRepository extends JpaRepository<PositionHolding, UUID> {

    // =====================================================================
    // Person-based queries
    // =====================================================================

    List<PositionHolding> findByPersonId(UUID personId);

    Page<PositionHolding> findByPersonId(UUID personId, Pageable pageable);

    List<PositionHolding> findByPersonIdOrderByStartDateDesc(UUID personId);

    // =====================================================================
    // Position-based queries
    // =====================================================================

    List<PositionHolding> findByPositionId(UUID positionId);

    Page<PositionHolding> findByPositionId(UUID positionId, Pageable pageable);

    List<PositionHolding> findByPositionIdOrderByStartDateDesc(UUID positionId);

    // =====================================================================
    // Congress-based queries
    // =====================================================================

    List<PositionHolding> findByCongress(Integer congress);

    Page<PositionHolding> findByCongress(Integer congress, Pageable pageable);

    // =====================================================================
    // Date range queries
    // =====================================================================

    /**
     * Find all holdings for a person that were active on a specific date
     */
    @Query("SELECT h FROM PositionHolding h WHERE h.personId = :personId " +
            "AND h.startDate <= :date AND (h.endDate IS NULL OR h.endDate >= :date)")
    List<PositionHolding> findByPersonIdAndActiveOnDate(
            @Param("personId") UUID personId,
            @Param("date") LocalDate date);

    /**
     * Find the current holder(s) of a position
     */
    @Query("SELECT h FROM PositionHolding h WHERE h.positionId = :positionId " +
            "AND (h.endDate IS NULL OR h.endDate >= CURRENT_DATE)")
    List<PositionHolding> findCurrentHoldersByPositionId(@Param("positionId") UUID positionId);

    /**
     * Find all members who were in office on a specific date
     */
    @Query("SELECT h FROM PositionHolding h " +
            "WHERE h.startDate <= :date AND (h.endDate IS NULL OR h.endDate >= :date)")
    List<PositionHolding> findAllActiveOnDate(@Param("date") LocalDate date);

    /**
     * Find all members who were in office on a specific date (paginated)
     */
    @Query("SELECT h FROM PositionHolding h " +
            "WHERE h.startDate <= :date AND (h.endDate IS NULL OR h.endDate >= :date)")
    Page<PositionHolding> findAllActiveOnDate(@Param("date") LocalDate date, Pageable pageable);

    /**
     * Find position holders on a specific date for a specific position
     */
    @Query("SELECT h FROM PositionHolding h WHERE h.positionId = :positionId " +
            "AND h.startDate <= :date AND (h.endDate IS NULL OR h.endDate >= :date)")
    List<PositionHolding> findByPositionIdAndActiveOnDate(
            @Param("positionId") UUID positionId,
            @Param("date") LocalDate date);

    /**
     * Find all current holdings (no end date)
     */
    @Query("SELECT h FROM PositionHolding h WHERE h.endDate IS NULL")
    List<PositionHolding> findAllCurrent();

    /**
     * Find all current holdings (paginated)
     */
    @Query("SELECT h FROM PositionHolding h WHERE h.endDate IS NULL")
    Page<PositionHolding> findAllCurrent(Pageable pageable);

    // =====================================================================
    // Existence checks
    // =====================================================================

    /**
     * Check if a holding exists for person, position, and congress
     */
    @Query("SELECT CASE WHEN COUNT(h) > 0 THEN true ELSE false END FROM PositionHolding h " +
            "WHERE h.personId = :personId AND h.positionId = :positionId AND h.congress = :congress")
    boolean existsByPersonPositionCongress(
            @Param("personId") UUID personId,
            @Param("positionId") UUID positionId,
            @Param("congress") Integer congress);

    /**
     * Find existing holding for upsert
     */
    Optional<PositionHolding> findByPersonIdAndPositionIdAndCongress(
            UUID personId, UUID positionId, Integer congress);

    // =====================================================================
    // Chamber-based queries (via position join)
    // =====================================================================

    /**
     * Find all members who were in a specific chamber on a specific date
     */
    @Query("SELECT h FROM PositionHolding h JOIN GovernmentPosition p ON h.positionId = p.id " +
            "WHERE p.chamber = :chamber " +
            "AND h.startDate <= :date AND (h.endDate IS NULL OR h.endDate >= :date)")
    List<PositionHolding> findByChamberAndActiveOnDate(
            @Param("chamber") Chamber chamber,
            @Param("date") LocalDate date);

    /**
     * Find all members who were in a specific chamber on a specific date (paginated)
     */
    @Query("SELECT h FROM PositionHolding h JOIN GovernmentPosition p ON h.positionId = p.id " +
            "WHERE p.chamber = :chamber " +
            "AND h.startDate <= :date AND (h.endDate IS NULL OR h.endDate >= :date)")
    Page<PositionHolding> findByChamberAndActiveOnDate(
            @Param("chamber") Chamber chamber,
            @Param("date") LocalDate date,
            Pageable pageable);

    // =====================================================================
    // Statistics queries
    // =====================================================================

    /**
     * Count holdings by congress
     */
    @Query("SELECT h.congress, COUNT(h) FROM PositionHolding h GROUP BY h.congress ORDER BY h.congress DESC")
    List<Object[]> countByCongress();

    /**
     * Count current holdings by chamber
     */
    @Query("SELECT p.chamber, COUNT(h) FROM PositionHolding h " +
            "JOIN GovernmentPosition p ON h.positionId = p.id " +
            "WHERE h.endDate IS NULL " +
            "GROUP BY p.chamber")
    List<Object[]> countCurrentByChamber();

    // =====================================================================
    // PLUM Import Queries (FB-2.1)
    // =====================================================================

    /**
     * Find current holding for a person in a position (no end date)
     * Used for PLUM upsert - executive positions don't have congress numbers
     */
    @Query("SELECT h FROM PositionHolding h WHERE h.personId = :personId " +
            "AND h.positionId = :positionId AND h.endDate IS NULL")
    Optional<PositionHolding> findCurrentByPersonIdAndPositionId(
            @Param("personId") UUID personId,
            @Param("positionId") UUID positionId);

    /**
     * Find all holdings by person and position
     */
    List<PositionHolding> findByPersonIdAndPositionId(UUID personId, UUID positionId);

    /**
     * Find holding by person, position, and start date (for FJC upsert)
     */
    Optional<PositionHolding> findByPersonIdAndPositionIdAndStartDate(
            UUID personId, UUID positionId, LocalDate startDate);

    /**
     * Find holdings by data source
     */
    List<PositionHolding> findByDataSource(DataSource dataSource);

    /**
     * Find holdings by data source (paginated)
     */
    Page<PositionHolding> findByDataSource(DataSource dataSource, Pageable pageable);

    /**
     * Count holdings by data source
     */
    long countByDataSource(DataSource dataSource);

    // =====================================================================
    // Appointee Lookup Queries (FB-2.4)
    // =====================================================================

    /**
     * Find all current executive branch holdings
     */
    @Query("SELECT h FROM PositionHolding h " +
           "JOIN GovernmentPosition p ON h.positionId = p.id " +
           "WHERE p.branch = 'EXECUTIVE' AND h.endDate IS NULL " +
           "ORDER BY p.title")
    List<PositionHolding> findAllCurrentExecutiveHoldings();

    /**
     * Find all current executive branch holdings (paginated)
     */
    @Query("SELECT h FROM PositionHolding h " +
           "JOIN GovernmentPosition p ON h.positionId = p.id " +
           "WHERE p.branch = 'EXECUTIVE' AND h.endDate IS NULL")
    Page<PositionHolding> findAllCurrentExecutiveHoldings(Pageable pageable);

    /**
     * Find current executive holdings by organization
     */
    @Query("SELECT h FROM PositionHolding h " +
           "JOIN GovernmentPosition p ON h.positionId = p.id " +
           "WHERE p.branch = 'EXECUTIVE' AND p.organizationId = :orgId AND h.endDate IS NULL")
    List<PositionHolding> findCurrentExecutiveHoldingsByOrganizationId(@Param("orgId") UUID orgId);

    /**
     * Find current executive holdings by appointment type
     */
    @Query("SELECT h FROM PositionHolding h " +
           "JOIN GovernmentPosition p ON h.positionId = p.id " +
           "WHERE p.branch = 'EXECUTIVE' AND p.appointmentType = :appointmentType AND h.endDate IS NULL")
    List<PositionHolding> findCurrentExecutiveHoldingsByAppointmentType(
            @Param("appointmentType") org.newsanalyzer.model.AppointmentType appointmentType);

    /**
     * Check if a position has a current holder
     */
    @Query("SELECT CASE WHEN COUNT(h) > 0 THEN true ELSE false END FROM PositionHolding h " +
           "WHERE h.positionId = :positionId AND h.endDate IS NULL")
    boolean hasCurrentHolder(@Param("positionId") UUID positionId);
}
