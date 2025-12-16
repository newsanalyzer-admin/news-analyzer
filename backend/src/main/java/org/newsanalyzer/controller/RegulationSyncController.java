package org.newsanalyzer.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.newsanalyzer.dto.SyncStatistics;
import org.newsanalyzer.service.RegulationSyncService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Admin controller for regulation sync operations.
 *
 * @author James (Dev Agent)
 * @since 2.0.0
 */
@RestController
@RequestMapping("/api/admin/sync/regulations")
@Tag(name = "Admin - Regulation Sync", description = "Administrative endpoints for regulation synchronization")
@RequiredArgsConstructor
public class RegulationSyncController {

    private static final Logger log = LoggerFactory.getLogger(RegulationSyncController.class);

    private final RegulationSyncService regulationSyncService;

    /**
     * Trigger a manual sync of regulations from the Federal Register API.
     */
    @PostMapping
    @Operation(
            summary = "Trigger regulation sync",
            description = "Manually triggers a sync of regulations from the Federal Register API. " +
                         "Returns immediately if a sync is already running."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Sync completed successfully"),
            @ApiResponse(responseCode = "409", description = "Sync already in progress"),
            @ApiResponse(responseCode = "500", description = "Sync failed")
    })
    public ResponseEntity<SyncStatistics> triggerSync() {
        log.info("Manual regulation sync triggered via API");

        if (regulationSyncService.isSyncRunning()) {
            log.warn("Sync already in progress, rejecting new request");
            SyncStatistics currentStats = regulationSyncService.getSyncStatus();
            return ResponseEntity.status(409).body(currentStats);
        }

        SyncStatistics stats = regulationSyncService.syncRegulations();

        if (stats.getStatus() == SyncStatistics.SyncStatus.COMPLETED) {
            return ResponseEntity.ok(stats);
        } else {
            return ResponseEntity.internalServerError().body(stats);
        }
    }

    /**
     * Get the status of the current or last sync operation.
     */
    @GetMapping("/status")
    @Operation(
            summary = "Get sync status",
            description = "Returns the status of the current or last regulation sync operation"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Status retrieved successfully"),
            @ApiResponse(responseCode = "204", description = "No sync has been performed yet")
    })
    public ResponseEntity<SyncStatistics> getSyncStatus() {
        SyncStatistics stats = regulationSyncService.getSyncStatus();

        if (stats == null) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(stats);
    }
}
