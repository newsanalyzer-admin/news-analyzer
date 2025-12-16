package org.newsanalyzer.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.newsanalyzer.dto.GovmanImportResult;
import org.newsanalyzer.service.GovmanXmlImportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * REST Controller for Government Manual XML import operations.
 *
 * Provides endpoints for:
 * - Uploading and importing GOVMAN XML files
 * - Checking import status
 * - Getting last import results
 *
 * Base Path: /api/admin/import/govman
 *
 * Note: These endpoints should be secured in production.
 *
 * @author James (Dev Agent)
 * @since 2.0.0
 */
@RestController
@RequestMapping("/api/admin/import/govman")
@Tag(name = "GOVMAN Import", description = "Government Manual XML import operations")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"})
public class GovmanImportController {

    private static final Logger log = LoggerFactory.getLogger(GovmanImportController.class);

    private static final long MAX_FILE_SIZE = 15 * 1024 * 1024; // 15MB

    private final GovmanXmlImportService importService;

    // Track ongoing imports to prevent concurrent runs
    private volatile boolean importInProgress = false;
    private GovmanImportResult lastResult = null;

    public GovmanImportController(GovmanXmlImportService importService) {
        this.importService = importService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Import GOVMAN XML file",
               description = "Uploads and imports a Government Manual XML file. " +
                       "This operation parses the XML and creates/updates government organization records.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Import completed successfully",
                content = @Content(schema = @Schema(implementation = GovmanImportResult.class))),
        @ApiResponse(responseCode = "400", description = "Invalid XML format or file"),
        @ApiResponse(responseCode = "409", description = "Import already in progress"),
        @ApiResponse(responseCode = "413", description = "File too large"),
        @ApiResponse(responseCode = "500", description = "Import failed")
    })
    public ResponseEntity<GovmanImportResult> importGovman(
            @RequestParam("file") MultipartFile file) {

        // Check if import is already running
        if (importInProgress) {
            log.warn("GOVMAN import already in progress");
            return ResponseEntity.status(409).build();
        }

        // Validate file
        if (file == null || file.isEmpty()) {
            log.warn("Empty or null file received");
            return ResponseEntity.badRequest().build();
        }

        // Check file size
        if (file.getSize() > MAX_FILE_SIZE) {
            log.warn("File size {} exceeds maximum {}", file.getSize(), MAX_FILE_SIZE);
            return ResponseEntity.status(413).build();
        }

        // Validate file type
        String filename = file.getOriginalFilename();
        if (filename == null || !filename.toLowerCase().endsWith(".xml")) {
            log.warn("Invalid file type: {}", filename);
            return ResponseEntity.badRequest().build();
        }

        log.info("GOVMAN import triggered via API, file: {}, size: {} bytes",
                filename, file.getSize());

        importInProgress = true;

        try {
            GovmanImportResult result = importService.importFromStream(file.getInputStream());
            lastResult = result;

            if (result.getErrors() > 0) {
                log.warn("GOVMAN import completed with {} errors", result.getErrors());
            } else {
                log.info("GOVMAN import completed successfully");
            }

            return ResponseEntity.ok(result);

        } catch (IOException e) {
            log.error("Failed to read uploaded file", e);
            GovmanImportResult errorResult = GovmanImportResult.builder()
                    .errors(1)
                    .build();
            errorResult.addError("Failed to read file: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResult);

        } finally {
            importInProgress = false;
        }
    }

    @GetMapping("/status")
    @Operation(summary = "Get GOVMAN import status",
               description = "Check if a GOVMAN import is currently running and get summary of last import")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Status returned")
    })
    public ResponseEntity<Map<String, Object>> getStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("inProgress", importInProgress);

        if (lastResult != null) {
            Map<String, Object> lastImport = new HashMap<>();
            lastImport.put("startTime", lastResult.getStartTime());
            lastImport.put("endTime", lastResult.getEndTime());
            lastImport.put("total", lastResult.getTotal());
            lastImport.put("imported", lastResult.getImported());
            lastImport.put("updated", lastResult.getUpdated());
            lastImport.put("skipped", lastResult.getSkipped());
            lastImport.put("errors", lastResult.getErrors());
            lastImport.put("durationSeconds", lastResult.getDurationSeconds());
            lastImport.put("successRate", lastResult.getSuccessRate());
            status.put("lastImport", lastImport);
        }

        return ResponseEntity.ok(status);
    }

    @GetMapping("/last-result")
    @Operation(summary = "Get last GOVMAN import result",
               description = "Returns the full result from the last GOVMAN import including error details")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Last result returned",
                content = @Content(schema = @Schema(implementation = GovmanImportResult.class))),
        @ApiResponse(responseCode = "404", description = "No previous import found")
    })
    public ResponseEntity<GovmanImportResult> getLastResult() {
        if (lastResult == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(lastResult);
    }
}
