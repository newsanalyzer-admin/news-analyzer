package org.newsanalyzer.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.newsanalyzer.dto.LinkageStatistics;
import org.newsanalyzer.service.AgencyLinkageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Set;

/**
 * Admin controller for agency linkage operations.
 *
 * Provides endpoints for:
 * - Viewing unmatched agencies
 * - Viewing linkage statistics
 * - Managing caches
 *
 * @author James (Dev Agent)
 * @since 2.0.0
 */
@RestController
@RequestMapping("/api/admin/regulations")
@Tag(name = "Admin - Agency Linkage", description = "Administrative endpoints for agency-regulation linkage")
@RequiredArgsConstructor
public class AgencyLinkageController {

    private static final Logger log = LoggerFactory.getLogger(AgencyLinkageController.class);

    private final AgencyLinkageService agencyLinkageService;

    /**
     * Get list of unmatched agency names.
     */
    @GetMapping("/unmatched-agencies")
    @Operation(
            summary = "Get unmatched agencies",
            description = "Returns a sorted list of agency names from the Federal Register that could not be matched " +
                         "to any GovernmentOrganization record. Use this to identify agencies that need manual mapping."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Unmatched agencies retrieved successfully")
    })
    public ResponseEntity<Set<String>> getUnmatchedAgencies() {
        Set<String> unmatched = agencyLinkageService.getUnmatchedAgencies();
        log.info("Returning {} unmatched agencies", unmatched.size());
        return ResponseEntity.ok(unmatched);
    }

    /**
     * Clear the unmatched agencies list.
     */
    @DeleteMapping("/unmatched-agencies")
    @Operation(
            summary = "Clear unmatched agencies",
            description = "Clears the list of unmatched agency names. Useful after adding manual mappings " +
                         "or GovernmentOrganization records to reset tracking."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Unmatched agencies list cleared")
    })
    public ResponseEntity<Void> clearUnmatchedAgencies() {
        agencyLinkageService.clearUnmatchedAgencies();
        log.info("Unmatched agencies list cleared via API");
        return ResponseEntity.noContent().build();
    }

    /**
     * Get linkage statistics.
     */
    @GetMapping("/linkage-stats")
    @Operation(
            summary = "Get linkage statistics",
            description = "Returns statistics about regulation-agency linkage including total regulations, " +
                         "linked regulations, linkage rate, and unmatched agency count."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Statistics retrieved successfully")
    })
    public ResponseEntity<LinkageStatistics> getLinkageStatistics() {
        LinkageStatistics stats = agencyLinkageService.getStatistics();
        log.info("Returning linkage stats: {}% linkage rate ({} linked of {} total)",
                String.format("%.2f", stats.getLinkageRate()),
                stats.getLinkedRegulations(),
                stats.getTotalRegulations());
        return ResponseEntity.ok(stats);
    }

    /**
     * Refresh agency linkage caches.
     */
    @PostMapping("/refresh-caches")
    @Operation(
            summary = "Refresh linkage caches",
            description = "Forces a refresh of the agency name/acronym/ID caches from the database. " +
                         "Use this after adding or updating GovernmentOrganization records."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Caches refreshed successfully")
    })
    public ResponseEntity<Map<String, Integer>> refreshCaches() {
        log.info("Cache refresh triggered via API");
        agencyLinkageService.refreshCaches();
        Map<String, Integer> cacheSizes = agencyLinkageService.getCacheSizes();
        log.info("Caches refreshed: {}", cacheSizes);
        return ResponseEntity.ok(cacheSizes);
    }

    /**
     * Get cache sizes.
     */
    @GetMapping("/cache-stats")
    @Operation(
            summary = "Get cache statistics",
            description = "Returns the current sizes of the agency linkage caches"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cache stats retrieved successfully")
    })
    public ResponseEntity<Map<String, Integer>> getCacheStats() {
        return ResponseEntity.ok(agencyLinkageService.getCacheSizes());
    }
}
