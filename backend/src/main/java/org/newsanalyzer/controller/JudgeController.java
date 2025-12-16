package org.newsanalyzer.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.newsanalyzer.dto.FjcImportResult;
import org.newsanalyzer.dto.JudgeDTO;
import org.newsanalyzer.service.FjcCsvImportService;
import org.newsanalyzer.service.JudgeService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * REST controller for federal judge data.
 *
 * Provides endpoints to query and import federal judge information.
 *
 * @author James (Dev Agent)
 * @since 2.0.0
 */
@RestController
@RequestMapping("/api/judges")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class JudgeController {

    private final JudgeService judgeService;
    private final FjcCsvImportService fjcImportService;

    // =========================================================================
    // Query Endpoints
    // =========================================================================

    /**
     * Get all judges with optional filters.
     *
     * @param page Page number (0-indexed)
     * @param size Page size
     * @param courtLevel Filter by court level: SUPREME, APPEALS, DISTRICT
     * @param circuit Filter by circuit: 1-11, DC, FEDERAL
     * @param status Filter by status: ACTIVE, SENIOR, ALL
     * @param search Search by name
     * @return Page of judges
     */
    @GetMapping
    public ResponseEntity<Page<JudgeDTO>> listJudges(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String courtLevel,
            @RequestParam(required = false) String circuit,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "lastName") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<JudgeDTO> judges = judgeService.findJudges(courtLevel, circuit, status, search, pageable);
        return ResponseEntity.ok(judges);
    }

    /**
     * Get a specific judge by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<JudgeDTO> getJudge(@PathVariable UUID id) {
        return judgeService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Search judges by name.
     */
    @GetMapping("/search")
    public ResponseEntity<List<JudgeDTO>> searchJudges(@RequestParam String q) {
        List<JudgeDTO> results = judgeService.searchByName(q);
        return ResponseEntity.ok(results);
    }

    /**
     * Get judge statistics.
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        Map<String, Object> stats = judgeService.getStatistics();
        return ResponseEntity.ok(stats);
    }

    // =========================================================================
    // Import Endpoints (Admin)
    // =========================================================================

    /**
     * Import judges from FJC CSV URL.
     *
     * @param offset Number of records to skip
     * @param limit Maximum records to import
     * @return Import result
     */
    @PostMapping("/import/fjc")
    public ResponseEntity<FjcImportResult> importFromFjc(
            @RequestParam(required = false) Integer offset,
            @RequestParam(required = false) Integer limit) {

        log.info("Starting FJC import (offset={}, limit={})", offset, limit);
        FjcImportResult result = fjcImportService.importFromUrl(offset, limit);
        log.info("FJC import completed: {}", result.getSummary());

        return result.isSuccess()
                ? ResponseEntity.ok(result)
                : ResponseEntity.badRequest().body(result);
    }

    /**
     * Import judges from uploaded CSV file.
     *
     * @param file CSV file to import
     * @return Import result
     */
    @PostMapping("/import/csv")
    public ResponseEntity<FjcImportResult> importFromCsv(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(
                    FjcImportResult.builder()
                            .success(false)
                            .errorMessages(List.of("No file provided"))
                            .build()
            );
        }

        log.info("Starting FJC CSV import from uploaded file: {}", file.getOriginalFilename());

        try {
            FjcImportResult result = fjcImportService.importFromStream(file.getInputStream());
            log.info("FJC CSV import completed: {}", result.getSummary());

            return result.isSuccess()
                    ? ResponseEntity.ok(result)
                    : ResponseEntity.badRequest().body(result);
        } catch (Exception e) {
            log.error("Failed to import FJC CSV: {}", e.getMessage());
            return ResponseEntity.badRequest().body(
                    FjcImportResult.builder()
                            .success(false)
                            .errorMessages(List.of("Import failed: " + e.getMessage()))
                            .build()
            );
        }
    }
}
