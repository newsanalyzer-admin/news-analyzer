package com.newsanalyzer.controller;

import com.newsanalyzer.dto.NewsSourceDto;
import com.newsanalyzer.dto.ReliabilityScoreDto;
import com.newsanalyzer.model.NewsSource;
import com.newsanalyzer.service.NewsSourceService;
// import io.micrometer.core.annotation.Timed; // Comment out until Micrometer is properly configured
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * REST controller for managing news sources.
 *
 * Provides endpoints for CRUD operations on news sources,
 * reliability scoring, and source analysis.
 */
@RestController
@RequestMapping("/v1/sources")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:8080"})
public class NewsSourceController {

    private final NewsSourceService newsSourceService;

    @Autowired
    public NewsSourceController(NewsSourceService newsSourceService) {
        this.newsSourceService = newsSourceService;
    }

    /**
     * Get all news sources with pagination.
     *
     * @param pageable pagination parameters
     * @return page of news sources
     */
    @GetMapping
    // @Timed(name = "newsource.getAll", description = "Time taken to get all news sources")
    public ResponseEntity<Page<NewsSourceDto>> getAllSources(Pageable pageable) {
        Page<NewsSourceDto> sources = newsSourceService.getAllActiveSources(pageable);
        return ResponseEntity.ok(sources);
    }

    /**
     * Get a specific news source by ID.
     *
     * @param sourceId the source ID
     * @return the news source
     */
    @GetMapping("/{sourceId}")
    // @Timed(name = "newsource.getById", description = "Time taken to get news source by ID")
    public ResponseEntity<NewsSourceDto> getSourceById(@PathVariable UUID sourceId) {
        NewsSourceDto source = newsSourceService.getSourceById(sourceId);
        return ResponseEntity.ok(source);
    }

    /**
     * Get a news source by domain.
     *
     * @param domain the domain to search for
     * @return the news source
     */
    @GetMapping("/domain/{domain}")
    // @Timed(name = "newsource.getByDomain", description = "Time taken to get news source by domain")
    public ResponseEntity<NewsSourceDto> getSourceByDomain(@PathVariable String domain) {
        NewsSourceDto source = newsSourceService.getSourceByDomain(domain);
        return ResponseEntity.ok(source);
    }

    /**
     * Get news sources by type.
     *
     * @param sourceType the type of source
     * @return list of sources of the specified type
     */
    @GetMapping("/type/{sourceType}")
    public ResponseEntity<List<NewsSourceDto>> getSourcesByType(
            @PathVariable NewsSource.SourceType sourceType) {
        List<NewsSourceDto> sources = newsSourceService.getSourcesByType(sourceType);
        return ResponseEntity.ok(sources);
    }

    /**
     * Search news sources by name.
     *
     * @param name the search term
     * @param pageable pagination parameters
     * @return page of matching sources
     */
    @GetMapping("/search")
    public ResponseEntity<Page<NewsSourceDto>> searchSources(
            @RequestParam String name,
            Pageable pageable) {
        Page<NewsSourceDto> sources = newsSourceService.searchSourcesByName(name, pageable);
        return ResponseEntity.ok(sources);
    }

    /**
     * Get reliable news sources (above threshold).
     *
     * @param threshold minimum reliability score (default 0.7)
     * @param pageable pagination parameters
     * @return page of reliable sources
     */
    @GetMapping("/reliable")
    public ResponseEntity<Page<NewsSourceDto>> getReliableSources(
            @RequestParam(defaultValue = "0.7") BigDecimal threshold,
            Pageable pageable) {
        Page<NewsSourceDto> sources = newsSourceService.getReliableSources(threshold, pageable);
        return ResponseEntity.ok(sources);
    }

    /**
     * Get sources within a specific bias range.
     *
     * @param minBias minimum bias score
     * @param maxBias maximum bias score
     * @return list of sources within bias range
     */
    @GetMapping("/bias-range")
    public ResponseEntity<List<NewsSourceDto>> getSourcesByBiasRange(
            @RequestParam BigDecimal minBias,
            @RequestParam BigDecimal maxBias) {
        List<NewsSourceDto> sources = newsSourceService.getSourcesByBiasRange(minBias, maxBias);
        return ResponseEntity.ok(sources);
    }

    /**
     * Get the reliability score for a specific source.
     *
     * @param sourceId the source ID
     * @return reliability score information
     */
    @GetMapping("/{sourceId}/reliability")
    // @Timed(name = "newsource.getReliability", description = "Time taken to get source reliability")
    public ResponseEntity<ReliabilityScoreDto> getSourceReliability(@PathVariable UUID sourceId) {
        ReliabilityScoreDto reliability = newsSourceService.getSourceReliability(sourceId);
        return ResponseEntity.ok(reliability);
    }

    /**
     * Create a new news source.
     *
     * @param newsSourceDto the source data
     * @return created source
     */
    @PostMapping
    // @Timed(name = "newsource.create", description = "Time taken to create news source")
    public ResponseEntity<NewsSourceDto> createSource(@Valid @RequestBody NewsSourceDto newsSourceDto) {
        NewsSourceDto createdSource = newsSourceService.createSource(newsSourceDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdSource);
    }

    /**
     * Update an existing news source.
     *
     * @param sourceId the source ID
     * @param newsSourceDto updated source data
     * @return updated source
     */
    @PutMapping("/{sourceId}")
    // @Timed(name = "newsource.update", description = "Time taken to update news source")
    public ResponseEntity<NewsSourceDto> updateSource(
            @PathVariable UUID sourceId,
            @Valid @RequestBody NewsSourceDto newsSourceDto) {
        NewsSourceDto updatedSource = newsSourceService.updateSource(sourceId, newsSourceDto);
        return ResponseEntity.ok(updatedSource);
    }

    /**
     * Update reliability score for a source.
     *
     * @param sourceId the source ID
     * @param reliabilityScore new reliability score
     * @return updated source
     */
    @PatchMapping("/{sourceId}/reliability")
    public ResponseEntity<NewsSourceDto> updateReliabilityScore(
            @PathVariable UUID sourceId,
            @RequestParam BigDecimal reliabilityScore) {
        NewsSourceDto updatedSource = newsSourceService.updateReliabilityScore(sourceId, reliabilityScore);
        return ResponseEntity.ok(updatedSource);
    }

    /**
     * Deactivate a news source (soft delete).
     *
     * @param sourceId the source ID
     * @return no content response
     */
    @DeleteMapping("/{sourceId}")
    public ResponseEntity<Void> deactivateSource(@PathVariable UUID sourceId) {
        newsSourceService.deactivateSource(sourceId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get sources that need reliability scoring.
     *
     * @return list of sources needing scoring
     */
    @GetMapping("/needs-scoring")
    public ResponseEntity<List<NewsSourceDto>> getSourcesNeedingScoring() {
        List<NewsSourceDto> sources = newsSourceService.getSourcesNeedingScoring();
        return ResponseEntity.ok(sources);
    }

    /**
     * Get system-wide reliability statistics.
     *
     * @return reliability statistics
     */
    @GetMapping("/statistics/reliability")
    public ResponseEntity<Object> getReliabilityStatistics() {
        Object statistics = newsSourceService.getReliabilityStatistics();
        return ResponseEntity.ok(statistics);
    }

    /**
     * Health check endpoint.
     *
     * @return simple health status
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("News Source Service is healthy");
    }
}