package org.newsanalyzer.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.newsanalyzer.dto.PlumImportResult;
import org.newsanalyzer.dto.UsCodeImportResult;
import org.newsanalyzer.service.PlumCsvImportService;
import org.newsanalyzer.service.UsCodeImportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * REST Controller for administrative data synchronization operations.
 *
 * Provides endpoints for:
 * - Triggering PLUM CSV import from OPM
 * - Checking sync status
 * - Getting import statistics
 *
 * Base Path: /api/admin/sync
 *
 * Note: These endpoints should be secured in production. Currently open for development.
 *
 * @author James (Dev Agent)
 * @since 2.0.0
 */
@RestController
@RequestMapping("/api/admin/sync")
@Tag(name = "Admin Sync", description = "Administrative data synchronization operations")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"})
public class AdminSyncController {

    private static final Logger log = LoggerFactory.getLogger(AdminSyncController.class);

    private final PlumCsvImportService plumImportService;
    private final UsCodeImportService usCodeImportService;

    // Track ongoing imports to prevent concurrent runs
    private volatile boolean plumImportInProgress = false;
    private PlumImportResult lastPlumResult = null;

    private volatile boolean usCodeImportInProgress = false;
    private UsCodeImportResult lastUsCodeResult = null;

    public AdminSyncController(PlumCsvImportService plumImportService,
                               UsCodeImportService usCodeImportService) {
        this.plumImportService = plumImportService;
        this.usCodeImportService = usCodeImportService;
    }

    // =====================================================================
    // PLUM Import Endpoints
    // =====================================================================

    @PostMapping("/plum")
    @Operation(summary = "Import PLUM data from OPM",
               description = "Downloads and imports executive branch appointee data from the OPM PLUM CSV file. " +
                       "Use offset and limit parameters to process in chunks (e.g., offset=0&limit=1000, " +
                       "then offset=1000&limit=1000, etc.). Full dataset is ~21,000 records.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Import completed successfully"),
        @ApiResponse(responseCode = "409", description = "Import already in progress"),
        @ApiResponse(responseCode = "500", description = "Import failed")
    })
    public ResponseEntity<PlumImportResult> importPlumData(
            @RequestParam(required = false) Integer offset,
            @RequestParam(required = false) Integer limit) {
        if (plumImportInProgress) {
            log.warn("PLUM import already in progress");
            return ResponseEntity.status(409).build();
        }

        log.info("PLUM import triggered via API (offset={}, limit={})", offset, limit);
        plumImportInProgress = true;

        try {
            PlumImportResult result = plumImportService.importFromUrl(offset, limit);
            lastPlumResult = result;

            if (result.getErrors() > 0) {
                log.warn("PLUM import completed with {} errors", result.getErrors());
            } else {
                log.info("PLUM import completed successfully");
            }

            return ResponseEntity.ok(result);

        } finally {
            plumImportInProgress = false;
        }
    }

    @GetMapping("/plum/status")
    @Operation(summary = "Get PLUM import status",
               description = "Check if a PLUM import is currently running and get last import results")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Status returned")
    })
    public ResponseEntity<Map<String, Object>> getPlumStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("inProgress", plumImportInProgress);
        status.put("csvUrl", plumImportService.getPlumCsvUrl());

        if (lastPlumResult != null) {
            Map<String, Object> lastImport = new HashMap<>();
            lastImport.put("startTime", lastPlumResult.getStartTime());
            lastImport.put("endTime", lastPlumResult.getEndTime());
            lastImport.put("totalRecords", lastPlumResult.getTotalRecords());
            lastImport.put("personsCreated", lastPlumResult.getPersonsCreated());
            lastImport.put("personsUpdated", lastPlumResult.getPersonsUpdated());
            lastImport.put("positionsCreated", lastPlumResult.getPositionsCreated());
            lastImport.put("positionsUpdated", lastPlumResult.getPositionsUpdated());
            lastImport.put("holdingsCreated", lastPlumResult.getHoldingsCreated());
            lastImport.put("holdingsUpdated", lastPlumResult.getHoldingsUpdated());
            lastImport.put("errors", lastPlumResult.getErrors());
            lastImport.put("durationSeconds", lastPlumResult.getDurationSeconds());
            lastImport.put("successRate", lastPlumResult.getSuccessRate());
            status.put("lastImport", lastImport);
        }

        return ResponseEntity.ok(status);
    }

    @GetMapping("/plum/last-result")
    @Operation(summary = "Get last PLUM import result",
               description = "Returns the full result from the last PLUM import including error details")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Last result returned"),
        @ApiResponse(responseCode = "404", description = "No previous import found")
    })
    public ResponseEntity<PlumImportResult> getLastPlumResult() {
        if (lastPlumResult == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(lastPlumResult);
    }

    // =====================================================================
    // US Code Import Endpoints
    // =====================================================================

    @PostMapping("/statutes")
    @Operation(summary = "Import all US Code titles",
               description = "Downloads and imports all US Code titles from uscode.house.gov. " +
                       "This is a long-running operation that may take 30+ minutes for the full dataset.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Import completed"),
        @ApiResponse(responseCode = "409", description = "Import already in progress"),
        @ApiResponse(responseCode = "500", description = "Import failed")
    })
    public ResponseEntity<UsCodeImportResult> importAllStatutes(
            @RequestParam(required = false) String releasePoint) {

        if (usCodeImportInProgress) {
            log.warn("US Code import already in progress");
            return ResponseEntity.status(409).build();
        }

        log.info("Full US Code import triggered via API (releasePoint: {})", releasePoint);
        usCodeImportInProgress = true;

        try {
            UsCodeImportResult result = usCodeImportService.importAllTitles(releasePoint);
            lastUsCodeResult = result;

            if (!result.isSuccess()) {
                log.warn("US Code import completed with errors: {}", result.getErrorMessage());
            } else {
                log.info("US Code import completed successfully: {} sections imported",
                        result.getSectionsInserted() + result.getSectionsUpdated());
            }

            return ResponseEntity.ok(result);

        } finally {
            usCodeImportInProgress = false;
        }
    }

    @PostMapping("/statutes/{titleNumber}")
    @Operation(summary = "Import a specific US Code title",
               description = "Downloads and imports a single US Code title from uscode.house.gov.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Import completed"),
        @ApiResponse(responseCode = "409", description = "Import already in progress"),
        @ApiResponse(responseCode = "400", description = "Invalid title number"),
        @ApiResponse(responseCode = "500", description = "Import failed")
    })
    public ResponseEntity<UsCodeImportResult> importStatuteTitle(
            @PathVariable int titleNumber,
            @RequestParam(required = false) String releasePoint) {

        if (titleNumber < 1 || titleNumber > 54) {
            return ResponseEntity.badRequest().build();
        }

        if (usCodeImportInProgress) {
            log.warn("US Code import already in progress");
            return ResponseEntity.status(409).build();
        }

        log.info("US Code Title {} import triggered via API (releasePoint: {})", titleNumber, releasePoint);
        usCodeImportInProgress = true;

        try {
            UsCodeImportResult result = usCodeImportService.importTitle(titleNumber, releasePoint);
            lastUsCodeResult = result;

            if (!result.isSuccess()) {
                log.warn("US Code Title {} import failed: {}", titleNumber, result.getErrorMessage());
            } else {
                log.info("US Code Title {} import completed: {} inserted, {} updated",
                        titleNumber, result.getSectionsInserted(), result.getSectionsUpdated());
            }

            return ResponseEntity.ok(result);

        } finally {
            usCodeImportInProgress = false;
        }
    }

    @GetMapping("/statutes/status")
    @Operation(summary = "Get US Code import status",
               description = "Check if a US Code import is currently running and get last import results")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Status returned")
    })
    public ResponseEntity<Map<String, Object>> getStatutesStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("inProgress", usCodeImportInProgress);
        status.put("totalStatutes", usCodeImportService.getTotalStatuteCount());
        status.put("usCodeStatutes", usCodeImportService.getUsCodeCount());

        if (lastUsCodeResult != null) {
            Map<String, Object> lastImport = new HashMap<>();
            lastImport.put("titleNumber", lastUsCodeResult.getTitleNumber());
            lastImport.put("releasePoint", lastUsCodeResult.getReleasePoint());
            lastImport.put("startedAt", lastUsCodeResult.getStartedAt());
            lastImport.put("completedAt", lastUsCodeResult.getCompletedAt());
            lastImport.put("sectionsInserted", lastUsCodeResult.getSectionsInserted());
            lastImport.put("sectionsUpdated", lastUsCodeResult.getSectionsUpdated());
            lastImport.put("sectionsFailed", lastUsCodeResult.getSectionsFailed());
            lastImport.put("totalProcessed", lastUsCodeResult.getTotalProcessed());
            lastImport.put("success", lastUsCodeResult.isSuccess());
            lastImport.put("duration", lastUsCodeResult.getDurationFormatted());
            if (!lastUsCodeResult.isSuccess()) {
                lastImport.put("errorMessage", lastUsCodeResult.getErrorMessage());
            }
            status.put("lastImport", lastImport);
        }

        return ResponseEntity.ok(status);
    }

    @GetMapping("/statutes/last-result")
    @Operation(summary = "Get last US Code import result",
               description = "Returns the full result from the last US Code import including error details")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Last result returned"),
        @ApiResponse(responseCode = "404", description = "No previous import found")
    })
    public ResponseEntity<UsCodeImportResult> getLastStatutesResult() {
        if (lastUsCodeResult == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(lastUsCodeResult);
    }

    // =====================================================================
    // General Admin Endpoints
    // =====================================================================

    @GetMapping("/health")
    @Operation(summary = "Sync service health check",
               description = "Verify the sync service is operational")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Service is healthy")
    })
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("services", Map.of(
                "plumImport", Map.of(
                        "available", true,
                        "inProgress", plumImportInProgress,
                        "csvUrl", plumImportService.getPlumCsvUrl()
                ),
                "usCodeImport", Map.of(
                        "available", true,
                        "inProgress", usCodeImportInProgress,
                        "totalStatutes", usCodeImportService.getTotalStatuteCount()
                )
        ));
        return ResponseEntity.ok(health);
    }
}
