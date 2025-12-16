package org.newsanalyzer.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.newsanalyzer.dto.AppointeeDTO;
import org.newsanalyzer.exception.ResourceNotFoundException;
import org.newsanalyzer.model.AppointmentType;
import org.newsanalyzer.service.AppointeeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST Controller for Executive Branch Appointees.
 *
 * Provides endpoints for querying political appointees, Cabinet members,
 * and executive branch personnel.
 *
 * Base Path: /api/appointees
 *
 * @author James (Dev Agent)
 * @since 2.0.0
 */
@RestController
@RequestMapping("/api/appointees")
@Tag(name = "Appointees", description = "Executive branch appointee lookup")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"})
@RequiredArgsConstructor
public class AppointeeController {

    private static final Logger log = LoggerFactory.getLogger(AppointeeController.class);

    private final AppointeeService appointeeService;

    // =====================================================================
    // List Endpoints
    // =====================================================================

    @GetMapping
    @Operation(summary = "List all appointees",
               description = "Returns paginated list of all current executive branch appointees")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "List of appointees returned")
    })
    public ResponseEntity<Page<AppointeeDTO>> getAllAppointees(
            @Parameter(description = "Page number (0-indexed)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, Math.min(size, 100));
        Page<AppointeeDTO> appointees = appointeeService.getAllAppointees(pageable);
        return ResponseEntity.ok(appointees);
    }

    // =====================================================================
    // Lookup Endpoints
    // =====================================================================

    @GetMapping("/{id}")
    @Operation(summary = "Get appointee by ID",
               description = "Retrieve a specific appointee by their person UUID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Appointee found"),
        @ApiResponse(responseCode = "404", description = "Appointee not found")
    })
    public ResponseEntity<AppointeeDTO> getAppointeeById(
            @Parameter(description = "Person UUID")
            @PathVariable UUID id) {

        return appointeeService.getAppointeeById(id)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException("Appointee not found: " + id));
    }

    // =====================================================================
    // Search Endpoints
    // =====================================================================

    @GetMapping("/search")
    @Operation(summary = "Search appointees",
               description = "Search appointees by name or position title")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Search results returned"),
        @ApiResponse(responseCode = "400", description = "Query too short (minimum 2 characters)")
    })
    public ResponseEntity<List<AppointeeDTO>> searchAppointees(
            @Parameter(description = "Search query (name or title)")
            @RequestParam String q,
            @Parameter(description = "Maximum results to return")
            @RequestParam(defaultValue = "20") int limit) {

        if (q == null || q.trim().length() < 2) {
            return ResponseEntity.badRequest().build();
        }

        List<AppointeeDTO> results = appointeeService.searchAppointees(q.trim(), Math.min(limit, 100));
        return ResponseEntity.ok(results);
    }

    // =====================================================================
    // Filter Endpoints
    // =====================================================================

    @GetMapping("/by-agency/{orgId}")
    @Operation(summary = "Get appointees by agency",
               description = "Get all current appointees in a specific agency/organization")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Appointees for agency returned")
    })
    public ResponseEntity<List<AppointeeDTO>> getByAgency(
            @Parameter(description = "Organization UUID")
            @PathVariable UUID orgId) {

        List<AppointeeDTO> appointees = appointeeService.getAppointeesByAgency(orgId);
        return ResponseEntity.ok(appointees);
    }

    @GetMapping("/by-type/{type}")
    @Operation(summary = "Get appointees by appointment type",
               description = "Get all current appointees of a specific appointment type (PAS, PA, NA, CA, XS)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Appointees of type returned"),
        @ApiResponse(responseCode = "400", description = "Invalid appointment type")
    })
    public ResponseEntity<List<AppointeeDTO>> getByType(
            @Parameter(description = "Appointment type: PAS, PA, NA, CA, or XS")
            @PathVariable String type) {

        try {
            AppointmentType appointmentType = AppointmentType.valueOf(type.toUpperCase());
            List<AppointeeDTO> appointees = appointeeService.getAppointeesByType(appointmentType);
            return ResponseEntity.ok(appointees);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid appointment type requested: {}", type);
            return ResponseEntity.badRequest().build();
        }
    }

    // =====================================================================
    // Special Endpoints
    // =====================================================================

    @GetMapping("/cabinet")
    @Operation(summary = "Get Cabinet members",
               description = "Get all current Cabinet-level appointees (department secretaries and key positions)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Cabinet members returned")
    })
    public ResponseEntity<List<AppointeeDTO>> getCabinetMembers() {
        List<AppointeeDTO> cabinet = appointeeService.getCabinetMembers();
        return ResponseEntity.ok(cabinet);
    }

    // =====================================================================
    // Statistics Endpoints
    // =====================================================================

    @GetMapping("/count")
    @Operation(summary = "Get appointee count",
               description = "Returns the total number of current executive appointees")
    public ResponseEntity<Long> getCount() {
        Page<AppointeeDTO> page = appointeeService.getAllAppointees(PageRequest.of(0, 1));
        return ResponseEntity.ok(page.getTotalElements());
    }
}
