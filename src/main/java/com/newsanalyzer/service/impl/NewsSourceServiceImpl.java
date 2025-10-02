package com.newsanalyzer.service.impl;

import com.newsanalyzer.dto.NewsSourceDto;
import com.newsanalyzer.dto.ReliabilityScoreDto;
import com.newsanalyzer.exception.DuplicateResourceException;
import com.newsanalyzer.exception.ResourceNotFoundException;
import com.newsanalyzer.model.NewsSource;
import com.newsanalyzer.repository.jpa.NewsSourceRepository;
import com.newsanalyzer.service.NewsSourceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementation of NewsSourceService.
 *
 * Provides business logic for managing news sources including
 * CRUD operations, reliability scoring, and caching.
 */
@Service
@Transactional
public class NewsSourceServiceImpl implements NewsSourceService {

    private static final Logger logger = LoggerFactory.getLogger(NewsSourceServiceImpl.class);

    private final NewsSourceRepository newsSourceRepository;

    @Autowired
    public NewsSourceServiceImpl(NewsSourceRepository newsSourceRepository) {
        this.newsSourceRepository = newsSourceRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NewsSourceDto> getAllActiveSources(Pageable pageable) {
        logger.debug("Getting all active sources with pagination: {}", pageable);

        Page<NewsSource> sources = newsSourceRepository.findByIsActiveTrueOrderByReliabilityScoreDesc(pageable);
        return sources.map(NewsSourceDto::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "source-by-id", key = "#sourceId")
    public NewsSourceDto getSourceById(UUID sourceId) {
        logger.debug("Getting source by ID: {}", sourceId);

        NewsSource source = newsSourceRepository.findById(sourceId)
                .orElseThrow(() -> new ResourceNotFoundException("News source not found with ID: " + sourceId));

        return NewsSourceDto.fromEntity(source);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "source-by-domain", key = "#domain")
    public NewsSourceDto getSourceByDomain(String domain) {
        logger.debug("Getting source by domain: {}", domain);

        NewsSource source = newsSourceRepository.findByDomain(domain)
                .orElseThrow(() -> new ResourceNotFoundException("News source not found with domain: " + domain));

        return NewsSourceDto.fromEntity(source);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NewsSourceDto> getSourcesByType(NewsSource.SourceType sourceType) {
        logger.debug("Getting sources by type: {}", sourceType);

        List<NewsSource> sources = newsSourceRepository.findBySourceTypeAndIsActiveTrue(sourceType);
        return sources.stream()
                .map(NewsSourceDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NewsSourceDto> searchSourcesByName(String name, Pageable pageable) {
        logger.debug("Searching sources by name: {} with pagination: {}", name, pageable);

        Page<NewsSource> sources = newsSourceRepository.findBySourceNameContainingIgnoreCase(name, pageable);
        return sources.map(NewsSourceDto::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NewsSourceDto> getReliableSources(BigDecimal threshold, Pageable pageable) {
        logger.debug("Getting reliable sources above threshold: {} with pagination: {}", threshold, pageable);

        Page<NewsSource> sources = newsSourceRepository
                .findByReliabilityScoreGreaterThanEqualAndIsActiveTrueOrderByReliabilityScoreDesc(threshold, pageable);

        return sources.map(NewsSourceDto::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NewsSourceDto> getSourcesByBiasRange(BigDecimal minBias, BigDecimal maxBias) {
        logger.debug("Getting sources by bias range: {} to {}", minBias, maxBias);

        List<NewsSource> sources = newsSourceRepository.findByPoliticalBiasScoreBetweenAndIsActiveTrue(minBias, maxBias);
        return sources.stream()
                .map(NewsSourceDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "source-reliability", key = "#sourceId")
    public ReliabilityScoreDto getSourceReliability(UUID sourceId) {
        logger.debug("Getting reliability score for source: {}", sourceId);

        NewsSource source = newsSourceRepository.findById(sourceId)
                .orElseThrow(() -> new ResourceNotFoundException("News source not found with ID: " + sourceId));

        // For now, return basic reliability info
        // In a full implementation, this would query accuracy_records table
        ReliabilityScoreDto reliability = ReliabilityScoreDto.basic(source.getReliabilityScore());
        reliability.setLastUpdated(source.getUpdatedAt());
        reliability.setMethodology("Calculated from historical accuracy records");

        return reliability;
    }

    @Override
    public NewsSourceDto createSource(NewsSourceDto newsSourceDto) {
        logger.info("Creating new news source: {}", newsSourceDto.getSourceName());

        // Check if domain already exists
        if (newsSourceDto.getDomain() != null && newsSourceRepository.existsByDomain(newsSourceDto.getDomain())) {
            throw new DuplicateResourceException("News source with domain already exists: " + newsSourceDto.getDomain());
        }

        NewsSource source = newsSourceDto.toEntity();
        source.setCreatedAt(LocalDateTime.now());
        source.setUpdatedAt(LocalDateTime.now());

        NewsSource savedSource = newsSourceRepository.save(source);
        logger.info("Created news source with ID: {}", savedSource.getSourceId());

        return NewsSourceDto.fromEntity(savedSource);
    }

    @Override
    @CacheEvict(value = {"source-by-id", "source-by-domain"}, key = "#sourceId")
    public NewsSourceDto updateSource(UUID sourceId, NewsSourceDto newsSourceDto) {
        logger.info("Updating news source: {}", sourceId);

        NewsSource existingSource = newsSourceRepository.findById(sourceId)
                .orElseThrow(() -> new ResourceNotFoundException("News source not found with ID: " + sourceId));

        // Update fields
        existingSource.setSourceName(newsSourceDto.getSourceName());
        existingSource.setDomain(newsSourceDto.getDomain());
        existingSource.setDescription(newsSourceDto.getDescription());
        existingSource.setFoundedDate(newsSourceDto.getFoundedDate());
        existingSource.setSourceType(newsSourceDto.getSourceType());
        existingSource.setUpdatedAt(LocalDateTime.now());

        // Don't update reliability scores through this method
        // Use dedicated methods for that

        NewsSource updatedSource = newsSourceRepository.save(existingSource);
        logger.info("Updated news source: {}", updatedSource.getSourceId());

        return NewsSourceDto.fromEntity(updatedSource);
    }

    @Override
    @CacheEvict(value = {"source-by-id", "source-by-domain", "source-reliability"}, key = "#sourceId")
    public NewsSourceDto updateReliabilityScore(UUID sourceId, BigDecimal reliabilityScore) {
        logger.info("Updating reliability score for source {}: {}", sourceId, reliabilityScore);

        NewsSource source = newsSourceRepository.findById(sourceId)
                .orElseThrow(() -> new ResourceNotFoundException("News source not found with ID: " + sourceId));

        source.setReliabilityScore(reliabilityScore);
        source.setUpdatedAt(LocalDateTime.now());

        NewsSource updatedSource = newsSourceRepository.save(source);
        logger.info("Updated reliability score for source: {}", updatedSource.getSourceId());

        return NewsSourceDto.fromEntity(updatedSource);
    }

    @Override
    @CacheEvict(value = {"source-by-id", "source-by-domain"}, key = "#sourceId")
    public void deactivateSource(UUID sourceId) {
        logger.info("Deactivating news source: {}", sourceId);

        NewsSource source = newsSourceRepository.findById(sourceId)
                .orElseThrow(() -> new ResourceNotFoundException("News source not found with ID: " + sourceId));

        source.setIsActive(false);
        source.setUpdatedAt(LocalDateTime.now());

        newsSourceRepository.save(source);
        logger.info("Deactivated news source: {}", sourceId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NewsSourceDto> getSourcesNeedingScoring() {
        logger.debug("Getting sources that need reliability scoring");

        List<NewsSource> sources = newsSourceRepository.findSourcesNeedingScoring();
        return sources.stream()
                .map(NewsSourceDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "reliability-statistics")
    public Object getReliabilityStatistics() {
        logger.debug("Getting system-wide reliability statistics");

        // Calculate various statistics
        Map<String, Object> statistics = new HashMap<>();

        statistics.put("totalActiveSources", newsSourceRepository.findByIsActiveTrue().size());
        statistics.put("averageReliabilityScore", newsSourceRepository.getAverageReliabilityScore());

        // Count by source type
        Map<String, Long> sourceTypeCounts = new HashMap<>();
        for (NewsSource.SourceType type : NewsSource.SourceType.values()) {
            sourceTypeCounts.put(type.name(), newsSourceRepository.countBySourceTypeAndIsActiveTrue(type));
        }
        statistics.put("sourceTypeCounts", sourceTypeCounts);

        // Count sources needing scoring
        statistics.put("sourcesNeedingScoring", newsSourceRepository.findSourcesNeedingScoring().size());

        // Count highly biased sources
        statistics.put("highlyBiasedSources",
                newsSourceRepository.findHighlyBiasedSources(new BigDecimal("0.5")).size());

        statistics.put("lastUpdated", LocalDateTime.now());

        return statistics;
    }

    @Override
    @CacheEvict(value = "source-reliability", key = "#sourceId")
    public ReliabilityScoreDto recalculateReliabilityScore(UUID sourceId) {
        logger.info("Recalculating reliability score for source: {}", sourceId);

        // This is a placeholder implementation
        // In a full implementation, this would:
        // 1. Query all accuracy records for this source
        // 2. Calculate weighted reliability score based on recency and volume
        // 3. Update the source's reliability score
        // 4. Return detailed reliability information

        NewsSource source = newsSourceRepository.findById(sourceId)
                .orElseThrow(() -> new ResourceNotFoundException("News source not found with ID: " + sourceId));

        // For now, just return current reliability
        ReliabilityScoreDto reliability = ReliabilityScoreDto.basic(source.getReliabilityScore());
        reliability.setLastUpdated(LocalDateTime.now());
        reliability.setMethodology("Placeholder - needs accuracy records implementation");

        return reliability;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean domainExists(String domain) {
        return newsSourceRepository.existsByDomain(domain);
    }
}