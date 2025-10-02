package com.newsanalyzer.service;

import com.newsanalyzer.dto.NewsSourceDto;
import com.newsanalyzer.dto.ReliabilityScoreDto;
import com.newsanalyzer.model.NewsSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Service interface for managing news sources.
 *
 * Defines the contract for news source operations including
 * CRUD operations, reliability scoring, and analysis.
 */
public interface NewsSourceService {

    /**
     * Get all active news sources with pagination.
     *
     * @param pageable pagination parameters
     * @return page of news source DTOs
     */
    Page<NewsSourceDto> getAllActiveSources(Pageable pageable);

    /**
     * Get a news source by ID.
     *
     * @param sourceId the source ID
     * @return news source DTO
     * @throws ResourceNotFoundException if source not found
     */
    NewsSourceDto getSourceById(UUID sourceId);

    /**
     * Get a news source by domain.
     *
     * @param domain the domain
     * @return news source DTO
     * @throws ResourceNotFoundException if source not found
     */
    NewsSourceDto getSourceByDomain(String domain);

    /**
     * Get news sources by type.
     *
     * @param sourceType the source type
     * @return list of news source DTOs
     */
    List<NewsSourceDto> getSourcesByType(NewsSource.SourceType sourceType);

    /**
     * Search news sources by name.
     *
     * @param name search term
     * @param pageable pagination parameters
     * @return page of matching sources
     */
    Page<NewsSourceDto> searchSourcesByName(String name, Pageable pageable);

    /**
     * Get reliable news sources above threshold.
     *
     * @param threshold minimum reliability score
     * @param pageable pagination parameters
     * @return page of reliable sources
     */
    Page<NewsSourceDto> getReliableSources(BigDecimal threshold, Pageable pageable);

    /**
     * Get sources within bias range.
     *
     * @param minBias minimum bias score
     * @param maxBias maximum bias score
     * @return list of sources within range
     */
    List<NewsSourceDto> getSourcesByBiasRange(BigDecimal minBias, BigDecimal maxBias);

    /**
     * Get reliability score for a source.
     *
     * @param sourceId the source ID
     * @return reliability score information
     */
    ReliabilityScoreDto getSourceReliability(UUID sourceId);

    /**
     * Create a new news source.
     *
     * @param newsSourceDto source data
     * @return created source DTO
     * @throws DuplicateResourceException if domain already exists
     */
    NewsSourceDto createSource(NewsSourceDto newsSourceDto);

    /**
     * Update an existing news source.
     *
     * @param sourceId the source ID
     * @param newsSourceDto updated source data
     * @return updated source DTO
     * @throws ResourceNotFoundException if source not found
     */
    NewsSourceDto updateSource(UUID sourceId, NewsSourceDto newsSourceDto);

    /**
     * Update reliability score for a source.
     *
     * @param sourceId the source ID
     * @param reliabilityScore new reliability score
     * @return updated source DTO
     */
    NewsSourceDto updateReliabilityScore(UUID sourceId, BigDecimal reliabilityScore);

    /**
     * Deactivate a news source (soft delete).
     *
     * @param sourceId the source ID
     */
    void deactivateSource(UUID sourceId);

    /**
     * Get sources that need reliability scoring.
     *
     * @return list of sources needing scoring
     */
    List<NewsSourceDto> getSourcesNeedingScoring();

    /**
     * Get system-wide reliability statistics.
     *
     * @return reliability statistics
     */
    Object getReliabilityStatistics();

    /**
     * Calculate and update reliability score for a source.
     *
     * @param sourceId the source ID
     * @return updated reliability score
     */
    ReliabilityScoreDto recalculateReliabilityScore(UUID sourceId);

    /**
     * Check if a domain already exists.
     *
     * @param domain the domain to check
     * @return true if domain exists
     */
    boolean domainExists(String domain);
}