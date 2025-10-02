package com.newsanalyzer.repository.jpa;

import com.newsanalyzer.model.NewsSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for NewsSource entities.
 *
 * Provides data access methods for news sources including
 * reliability scoring, bias analysis, and source management.
 */
@Repository
public interface NewsSourceRepository extends JpaRepository<NewsSource, UUID> {

    /**
     * Find a news source by its domain.
     *
     * @param domain the domain to search for
     * @return Optional containing the news source if found
     */
    Optional<NewsSource> findByDomain(String domain);

    /**
     * Find all active news sources.
     *
     * @return list of active news sources
     */
    List<NewsSource> findByIsActiveTrue();

    /**
     * Find active news sources by type.
     *
     * @param sourceType the type of source to filter by
     * @return list of active news sources of the specified type
     */
    List<NewsSource> findBySourceTypeAndIsActiveTrue(NewsSource.SourceType sourceType);

    /**
     * Find news sources with reliability score above threshold.
     *
     * @param threshold minimum reliability score
     * @param pageable pagination parameters
     * @return page of reliable news sources
     */
    Page<NewsSource> findByReliabilityScoreGreaterThanEqualAndIsActiveTrueOrderByReliabilityScoreDesc(
            BigDecimal threshold, Pageable pageable);

    /**
     * Find news sources by bias score range.
     *
     * @param minBias minimum bias score
     * @param maxBias maximum bias score
     * @return list of sources within bias range
     */
    List<NewsSource> findByPoliticalBiasScoreBetweenAndIsActiveTrue(
            BigDecimal minBias, BigDecimal maxBias);

    /**
     * Search news sources by name (case-insensitive).
     *
     * @param name partial name to search for
     * @param pageable pagination parameters
     * @return page of matching news sources
     */
    @Query("SELECT s FROM NewsSource s WHERE s.isActive = true AND " +
           "LOWER(s.sourceName) LIKE LOWER(CONCAT('%', :name, '%'))")
    Page<NewsSource> findBySourceNameContainingIgnoreCase(
            @Param("name") String name, Pageable pageable);

    /**
     * Get news sources ordered by reliability score.
     *
     * @param pageable pagination parameters
     * @return page of sources ordered by reliability (highest first)
     */
    Page<NewsSource> findByIsActiveTrueOrderByReliabilityScoreDesc(Pageable pageable);

    /**
     * Count active news sources by type.
     *
     * @param sourceType the type to count
     * @return number of active sources of the specified type
     */
    long countBySourceTypeAndIsActiveTrue(NewsSource.SourceType sourceType);

    /**
     * Find sources needing reliability score updates (no score or very low).
     *
     * @return list of sources that need scoring
     */
    @Query("SELECT s FROM NewsSource s WHERE s.isActive = true AND " +
           "(s.reliabilityScore IS NULL OR s.reliabilityScore < 0.1)")
    List<NewsSource> findSourcesNeedingScoring();

    /**
     * Get average reliability score for all active sources.
     *
     * @return average reliability score
     */
    @Query("SELECT AVG(s.reliabilityScore) FROM NewsSource s WHERE " +
           "s.isActive = true AND s.reliabilityScore IS NOT NULL")
    Double getAverageReliabilityScore();

    /**
     * Find sources with extreme bias (high positive or negative).
     *
     * @param threshold absolute bias threshold (e.g., 0.5)
     * @return list of highly biased sources
     */
    @Query("SELECT s FROM NewsSource s WHERE s.isActive = true AND " +
           "ABS(s.politicalBiasScore) >= :threshold")
    List<NewsSource> findHighlyBiasedSources(@Param("threshold") BigDecimal threshold);

    /**
     * Check if a domain already exists.
     *
     * @param domain the domain to check
     * @return true if domain exists
     */
    boolean existsByDomain(String domain);

    /**
     * Get sources for bias diversity analysis.
     *
     * @return sources grouped by bias range for diversity analysis
     */
    @Query("SELECT s FROM NewsSource s WHERE s.isActive = true AND " +
           "s.politicalBiasScore IS NOT NULL AND s.reliabilityScore >= 0.6 " +
           "ORDER BY s.politicalBiasScore")
    List<NewsSource> findForBiasDiversityAnalysis();
}