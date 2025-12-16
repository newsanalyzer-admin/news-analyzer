package org.newsanalyzer.repository;

import org.newsanalyzer.model.Statute;
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
 * Repository for Statute entities with custom query methods.
 * Supports lookup by USC identifier, title filtering, and full-text search.
 *
 * @author James (Dev Agent)
 * @since 2.0.0
 */
@Repository
public interface StatuteRepository extends JpaRepository<Statute, UUID> {

    // =====================================================================
    // Basic Lookup Queries
    // =====================================================================

    /**
     * Find statute by USC identifier (e.g., "/us/usc/t5/s101").
     */
    Optional<Statute> findByUscIdentifier(String uscIdentifier);

    /**
     * Check if a statute exists by USC identifier.
     */
    boolean existsByUscIdentifier(String uscIdentifier);

    // =====================================================================
    // Title Queries
    // =====================================================================

    /**
     * Find all statutes in a specific title (paginated).
     */
    Page<Statute> findByTitleNumber(Integer titleNumber, Pageable pageable);

    /**
     * Find all statutes in a specific title (non-paginated).
     */
    List<Statute> findByTitleNumber(Integer titleNumber);

    /**
     * Find statutes by title and chapter.
     */
    Page<Statute> findByTitleNumberAndChapterNumber(Integer titleNumber, String chapterNumber, Pageable pageable);

    /**
     * Count statutes by title number.
     */
    long countByTitleNumber(Integer titleNumber);

    // =====================================================================
    // Section Queries
    // =====================================================================

    /**
     * Find statute by title number and section number.
     */
    Optional<Statute> findByTitleNumberAndSectionNumber(Integer titleNumber, String sectionNumber);

    /**
     * Find statutes by section number pattern (across all titles).
     */
    List<Statute> findBySectionNumberContaining(String sectionPattern);

    // =====================================================================
    // Full-Text Search Queries
    // =====================================================================

    /**
     * Full-text search on content_text using PostgreSQL tsvector.
     */
    @Query(value = "SELECT * FROM statutes WHERE to_tsvector('english', content_text) @@ plainto_tsquery('english', :query)",
           countQuery = "SELECT COUNT(*) FROM statutes WHERE to_tsvector('english', content_text) @@ plainto_tsquery('english', :query)",
           nativeQuery = true)
    Page<Statute> searchByContentText(@Param("query") String query, Pageable pageable);

    /**
     * Full-text search with title filter.
     */
    @Query(value = "SELECT * FROM statutes WHERE title_number = :titleNumber " +
                   "AND to_tsvector('english', content_text) @@ plainto_tsquery('english', :query)",
           countQuery = "SELECT COUNT(*) FROM statutes WHERE title_number = :titleNumber " +
                       "AND to_tsvector('english', content_text) @@ plainto_tsquery('english', :query)",
           nativeQuery = true)
    Page<Statute> searchByContentTextAndTitle(@Param("query") String query,
                                               @Param("titleNumber") Integer titleNumber,
                                               Pageable pageable);

    /**
     * Search statutes by heading pattern (case-insensitive).
     */
    Page<Statute> findByHeadingContainingIgnoreCase(String headingPattern, Pageable pageable);

    // =====================================================================
    // Statistics Queries
    // =====================================================================

    /**
     * Count all statutes.
     */
    @Query("SELECT COUNT(s) FROM Statute s")
    long countAll();

    /**
     * Count statutes by title.
     */
    @Query(value = "SELECT title_number, COUNT(*) as count " +
                   "FROM statutes GROUP BY title_number ORDER BY title_number",
           nativeQuery = true)
    List<Object[]> countByTitle();

    /**
     * Get distinct title numbers with their names.
     */
    @Query("SELECT DISTINCT s.titleNumber, s.titleName FROM Statute s ORDER BY s.titleNumber")
    List<Object[]> findDistinctTitles();

    /**
     * Get the most recently updated statute.
     */
    Optional<Statute> findTopByOrderByUpdatedAtDesc();

    // =====================================================================
    // Release Point Queries
    // =====================================================================

    /**
     * Find statutes by release point.
     */
    List<Statute> findByReleasePoint(String releasePoint);

    /**
     * Get distinct release points.
     */
    @Query("SELECT DISTINCT s.releasePoint FROM Statute s WHERE s.releasePoint IS NOT NULL ORDER BY s.releasePoint DESC")
    List<String> findDistinctReleasePoints();

    // =====================================================================
    // Import Source Queries
    // =====================================================================

    /**
     * Find statutes by import source.
     */
    Page<Statute> findByImportSource(String importSource, Pageable pageable);

    /**
     * Count statutes by import source.
     */
    long countByImportSource(String importSource);
}
