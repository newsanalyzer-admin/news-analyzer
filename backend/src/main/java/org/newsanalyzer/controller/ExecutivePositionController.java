package org.newsanalyzer.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.newsanalyzer.dto.ExecutivePositionDTO;
import org.newsanalyzer.service.AppointeeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Executive Branch Positions.
 *
 * Provides endpoints for querying executive branch positions,
 * including vacant positions.
 *
 * Base Path: /api/positions/executive
 *
 * @author James (Dev Agent)
 * @since 2.0.0
 */
@RestController
@RequestMapping("/api/positions/executive")
@Tag(name = "Executive Positions", description = "Executive branch position lookup")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"})
@RequiredArgsConstructor
public class ExecutivePositionController {

    private static final Logger log = LoggerFactory.getLogger(ExecutivePositionController.class);

    private final AppointeeService appointeeService;

    // =====================================================================
    // List Endpoints
    // =====================================================================

    @GetMapping
    @Operation(summary = "List all executive positions",
               description = "Returns paginated list of all executive branch positions")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "List of positions returned")
    })
    public ResponseEntity<Page<ExecutivePositionDTO>> getAllPositions(
            @Parameter(description = "Page number (0-indexed)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, Math.min(size, 100));
        Page<ExecutivePositionDTO> positions = appointeeService.getAllExecutivePositions(pageable);
        return ResponseEntity.ok(positions);
    }

    // =====================================================================
    // Special Endpoints
    // =====================================================================

    @GetMapping("/vacant")
    @Operation(summary = "List vacant executive positions",
               description = "Returns all executive branch positions that are currently vacant")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "List of vacant positions returned")
    })
    public ResponseEntity<List<ExecutivePositionDTO>> getVacantPositions() {
        List<ExecutivePositionDTO> vacant = appointeeService.getVacantPositions();
        return ResponseEntity.ok(vacant);
    }

    // =====================================================================
    // Statistics Endpoints
    // =====================================================================

    @GetMapping("/count")
    @Operation(summary = "Get executive position count",
               description = "Returns the total number of executive positions")
    public ResponseEntity<Long> getCount() {
        Page<ExecutivePositionDTO> page = appointeeService.getAllExecutivePositions(PageRequest.of(0, 1));
        return ResponseEntity.ok(page.getTotalElements());
    }

    @GetMapping("/vacant/count")
    @Operation(summary = "Get vacant position count",
               description = "Returns the total number of vacant executive positions")
    public ResponseEntity<Integer> getVacantCount() {
        List<ExecutivePositionDTO> vacant = appointeeService.getVacantPositions();
        return ResponseEntity.ok(vacant.size());
    }
}
