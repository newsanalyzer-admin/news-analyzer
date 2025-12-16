package org.newsanalyzer.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.newsanalyzer.dto.UsCodeImportResult;
import org.newsanalyzer.service.UsCodeImportService;
import org.newsanalyzer.service.UslmXmlParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.xml.stream.XMLStreamException;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * REST Controller for US Code XML file upload and import operations.
 *
 * Provides endpoints for:
 * - Uploading and importing US Code XML files from uscode.house.gov
 * - Checking import status
 * - Getting last import results
 *
 * Base Path: /api/admin/import/statutes
 *
 * Note: These endpoints should be secured in production.
 *
 * @author James (Dev Agent)
 * @since 2.0.0
 */
@RestController
@RequestMapping("/api/admin/import/statutes")
@Tag(name = "US Code Import", description = "US Code XML file upload and import operations")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"})
public class StatuteImportController {

    private static final Logger log = LoggerFactory.getLogger(StatuteImportController.class);

    /**
     * Maximum file size: 100MB (US Code title XML files can be large).
     */
    private static final long MAX_FILE_SIZE = 100 * 1024 * 1024;

    /**
     * Default release point for imported files.
     */
    private static final String DEFAULT_RELEASE_POINT = "file-upload";

    private final UsCodeImportService importService;
    private final UslmXmlParser xmlParser;

    // Track ongoing imports to prevent concurrent runs
    private volatile boolean importInProgress = false;
    private UsCodeImportResult lastResult = null;

    public StatuteImportController(UsCodeImportService importService, UslmXmlParser xmlParser) {
        this.importService = importService;
        this.xmlParser = xmlParser;
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload and import US Code XML file",
               description = "Uploads and imports a US Code XML file downloaded from uscode.house.gov. " +
                       "This operation parses the USLM XML and creates/updates statute records. " +
                       "Import one title at a time for verification.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Import completed successfully",
                content = @Content(schema = @Schema(implementation = UsCodeImportResult.class))),
        @ApiResponse(responseCode = "400", description = "Invalid XML format or file"),
        @ApiResponse(responseCode = "409", description = "Import already in progress"),
        @ApiResponse(responseCode = "413", description = "File too large (max 100MB)"),
        @ApiResponse(responseCode = "500", description = "Import failed")
    })
    public ResponseEntity<UsCodeImportResult> uploadAndImport(
            @RequestParam("file") MultipartFile file) {

        // Check if import is already running
        if (importInProgress) {
            log.warn("US Code import already in progress");
            return ResponseEntity.status(409).build();
        }

        // Validate file is present
        if (file == null || file.isEmpty()) {
            log.warn("Empty or null file received");
            return ResponseEntity.badRequest().build();
        }

        // Check file size
        if (file.getSize() > MAX_FILE_SIZE) {
            log.warn("File size {} exceeds maximum {} bytes", file.getSize(), MAX_FILE_SIZE);
            return ResponseEntity.status(413).build();
        }

        // Validate file extension
        String filename = file.getOriginalFilename();
        if (filename == null || !filename.toLowerCase().endsWith(".xml")) {
            log.warn("Invalid file type: {}. Expected .xml file", filename);
            return ResponseEntity.badRequest().build();
        }

        // Validate XML content (check for XML header)
        try {
            if (!isValidXmlContent(file.getInputStream())) {
                log.warn("File does not appear to be valid XML: {}", filename);
                return ResponseEntity.badRequest().build();
            }
        } catch (IOException e) {
            log.error("Failed to read file for validation: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }

        log.info("US Code import triggered via file upload, file: {}, size: {} bytes",
                filename, file.getSize());

        importInProgress = true;
        UsCodeImportResult result = UsCodeImportResult.forFullImport(DEFAULT_RELEASE_POINT);

        try {
            importService.importFromStream(file.getInputStream(), DEFAULT_RELEASE_POINT, result);
            result.markSuccess();
            lastResult = result;

            log.info("US Code import completed: {} inserted, {} updated, {} failed",
                    result.getSectionsInserted(), result.getSectionsUpdated(), result.getSectionsFailed());

            return ResponseEntity.ok(result);

        } catch (XMLStreamException e) {
            log.error("Failed to parse XML file: {}", e.getMessage());
            result.markFailed("XML parse error: " + e.getMessage());
            lastResult = result;
            return ResponseEntity.badRequest().body(result);

        } catch (IOException e) {
            log.error("Failed to read uploaded file: {}", e.getMessage());
            result.markFailed("File read error: " + e.getMessage());
            lastResult = result;
            return ResponseEntity.internalServerError().body(result);

        } catch (Exception e) {
            log.error("Unexpected error during import: {}", e.getMessage(), e);
            result.markFailed("Unexpected error: " + e.getMessage());
            lastResult = result;
            return ResponseEntity.internalServerError().body(result);

        } finally {
            importInProgress = false;
        }
    }

    @GetMapping("/status")
    @Operation(summary = "Get US Code import status",
               description = "Check if a US Code import is currently running and get summary of last import")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Status returned")
    })
    public ResponseEntity<Map<String, Object>> getStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("inProgress", importInProgress);

        if (lastResult != null) {
            Map<String, Object> lastImport = new HashMap<>();
            lastImport.put("startedAt", lastResult.getStartedAt());
            lastImport.put("completedAt", lastResult.getCompletedAt());
            lastImport.put("titleNumber", lastResult.getTitleNumber());
            lastImport.put("releasePoint", lastResult.getReleasePoint());
            lastImport.put("sectionsInserted", lastResult.getSectionsInserted());
            lastImport.put("sectionsUpdated", lastResult.getSectionsUpdated());
            lastImport.put("sectionsFailed", lastResult.getSectionsFailed());
            lastImport.put("totalProcessed", lastResult.getTotalProcessed());
            lastImport.put("success", lastResult.isSuccess());
            lastImport.put("duration", lastResult.getDurationFormatted());
            status.put("lastImport", lastImport);
        }

        return ResponseEntity.ok(status);
    }

    @GetMapping("/last-result")
    @Operation(summary = "Get last US Code import result",
               description = "Returns the full result from the last US Code import including error details")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Last result returned",
                content = @Content(schema = @Schema(implementation = UsCodeImportResult.class))),
        @ApiResponse(responseCode = "404", description = "No previous import found")
    })
    public ResponseEntity<UsCodeImportResult> getLastResult() {
        if (lastResult == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(lastResult);
    }

    /**
     * Validate that the file content starts with XML declaration or root element.
     */
    private boolean isValidXmlContent(InputStream inputStream) throws IOException {
        BufferedInputStream bis = new BufferedInputStream(inputStream);
        bis.mark(1024);

        byte[] header = new byte[256];
        int bytesRead = bis.read(header);
        bis.reset();

        if (bytesRead < 5) {
            return false;
        }

        String headerStr = new String(header, 0, bytesRead).trim().toLowerCase();
        return headerStr.startsWith("<?xml") || headerStr.startsWith("<uslm");
    }
}
