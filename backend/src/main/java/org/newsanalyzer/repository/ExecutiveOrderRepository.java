package org.newsanalyzer.repository;

import org.newsanalyzer.model.DataSource;
import org.newsanalyzer.model.ExecutiveOrder;
import org.newsanalyzer.model.ExecutiveOrderStatus;
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
 * Repository for ExecutiveOrder entity.
 *
 * Provides CRUD operations and custom queries for Executive Order data.
 *
 * @author James (Dev Agent)
 * @since 2.0.0
 */
@Repository
public interface ExecutiveOrderRepository extends JpaRepository<ExecutiveOrder, UUID> {

    // =====================================================================
    // Basic Lookups
    // =====================================================================

    /**
     * Find Executive Order by EO number.
     */
    Optional<ExecutiveOrder> findByEoNumber(Integer eoNumber);

    /**
     * Check if Executive Order exists by number.
     */
    boolean existsByEoNumber(Integer eoNumber);

    // =====================================================================
    // By Presidency
    // =====================================================================

    /**
     * Find all Executive Orders for a presidency.
     */
    List<ExecutiveOrder> findByPresidencyId(UUID presidencyId);

    /**
     * Find all Executive Orders for a presidency (paginated).
     */
    Page<ExecutiveOrder> findByPresidencyId(UUID presidencyId, Pageable pageable);

    /**
     * Find all Executive Orders for a presidency, ordered by signing date descending.
     */
    List<ExecutiveOrder> findByPresidencyIdOrderBySigningDateDesc(UUID presidencyId);

    /**
     * Find all Executive Orders for a presidency, ordered by EO number ascending.
     */
    List<ExecutiveOrder> findByPresidencyIdOrderByEoNumberAsc(UUID presidencyId);

    /**
     * Count Executive Orders for a presidency.
     */
    long countByPresidencyId(UUID presidencyId);

    // =====================================================================
    // By Status
    // =====================================================================

    /**
     * Find all Executive Orders by status.
     */
    List<ExecutiveOrder> findByStatus(ExecutiveOrderStatus status);

    /**
     * Find all Executive Orders by status (paginated).
     */
    Page<ExecutiveOrder> findByStatus(ExecutiveOrderStatus status, Pageable pageable);

    /**
     * Count Executive Orders by status.
     */
    long countByStatus(ExecutiveOrderStatus status);

    /**
     * Find active Executive Orders.
     */
    @Query("SELECT eo FROM ExecutiveOrder eo WHERE eo.status = 'ACTIVE' ORDER BY eo.eoNumber DESC")
    List<ExecutiveOrder> findActiveOrders();

    /**
     * Find revoked Executive Orders.
     */
    @Query("SELECT eo FROM ExecutiveOrder eo WHERE eo.status = 'REVOKED' ORDER BY eo.eoNumber DESC")
    List<ExecutiveOrder> findRevokedOrders();

    // =====================================================================
    // By Presidency and Status
    // =====================================================================

    /**
     * Find Executive Orders for a presidency by status.
     */
    List<ExecutiveOrder> findByPresidencyIdAndStatus(UUID presidencyId, ExecutiveOrderStatus status);

    /**
     * Count active Executive Orders for a presidency.
     */
    @Query("SELECT COUNT(eo) FROM ExecutiveOrder eo WHERE eo.presidencyId = :presidencyId AND eo.status = 'ACTIVE'")
    long countActiveByPresidencyId(@Param("presidencyId") UUID presidencyId);

    // =====================================================================
    // Title Search
    // =====================================================================

    /**
     * Search by title containing (case-insensitive).
     */
    List<ExecutiveOrder> findByTitleContainingIgnoreCase(String title);

    /**
     * Search by title containing (case-insensitive, paginated).
     */
    Page<ExecutiveOrder> findByTitleContainingIgnoreCase(String title, Pageable pageable);

    /**
     * Full-text search in title and summary using PostgreSQL ts_vector.
     */
    @Query(value = "SELECT * FROM executive_orders eo " +
           "WHERE to_tsvector('english', coalesce(eo.title, '') || ' ' || coalesce(eo.summary, '')) " +
           "@@ plainto_tsquery('english', :query)",
           nativeQuery = true)
    List<ExecutiveOrder> fullTextSearch(@Param("query") String query);

    // =====================================================================
    // Date Range Queries
    // =====================================================================

    /**
     * Find Executive Orders signed between dates.
     */
    List<ExecutiveOrder> findBySigningDateBetween(LocalDate start, LocalDate end);

    /**
     * Find Executive Orders signed after date.
     */
    List<ExecutiveOrder> findBySigningDateAfter(LocalDate date);

    /**
     * Find Executive Orders signed in a specific year.
     */
    @Query("SELECT eo FROM ExecutiveOrder eo WHERE YEAR(eo.signingDate) = :year ORDER BY eo.signingDate ASC")
    List<ExecutiveOrder> findBySigningYear(@Param("year") int year);

    // =====================================================================
    // Revocation Queries
    // =====================================================================

    /**
     * Find Executive Orders revoked by a specific EO.
     */
    List<ExecutiveOrder> findByRevokedByEo(Integer eoNumber);

    /**
     * Find Executive Orders that revoked other orders.
     */
    @Query("SELECT eo FROM ExecutiveOrder eo WHERE eo.revokedByEo IS NOT NULL")
    List<ExecutiveOrder> findOrdersThatRevoked();

    // =====================================================================
    // By Data Source
    // =====================================================================

    /**
     * Find all Executive Orders by data source.
     */
    List<ExecutiveOrder> findByDataSource(DataSource dataSource);

    /**
     * Count Executive Orders by data source.
     */
    long countByDataSource(DataSource dataSource);

    // =====================================================================
    // Statistics
    // =====================================================================

    /**
     * Get the highest EO number.
     */
    @Query("SELECT MAX(eo.eoNumber) FROM ExecutiveOrder eo")
    Integer findMaxEoNumber();

    /**
     * Get the lowest EO number.
     */
    @Query("SELECT MIN(eo.eoNumber) FROM ExecutiveOrder eo")
    Integer findMinEoNumber();

    /**
     * Count total Executive Orders.
     */
    @Query("SELECT COUNT(eo) FROM ExecutiveOrder eo")
    long countTotal();

    /**
     * Get status distribution.
     */
    @Query("SELECT eo.status, COUNT(eo) FROM ExecutiveOrder eo GROUP BY eo.status")
    List<Object[]> getStatusDistribution();

    /**
     * Get EO count per presidency.
     */
    @Query("SELECT eo.presidencyId, COUNT(eo) FROM ExecutiveOrder eo GROUP BY eo.presidencyId")
    List<Object[]> getCountPerPresidency();
}
