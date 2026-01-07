package org.newsanalyzer.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.newsanalyzer.dto.ExecutiveOrderDTO;
import org.newsanalyzer.dto.PresidencyAdministrationDTO;
import org.newsanalyzer.dto.PresidencyDTO;
import org.newsanalyzer.service.PresidencyService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST controller for presidency data endpoints.
 * Provides public APIs for presidential data access.
 *
 * Base Path: /api/presidencies
 *
 * @author James (Dev Agent)
 * @since 2.0.0
 */
@RestController
@RequestMapping("/api/presidencies")
@Tag(name = "Presidencies", description = "U.S. Presidency data and history endpoints")
@RequiredArgsConstructor
@Slf4j
@Validated
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"})
public class PresidencyController {

    private final PresidencyService presidencyService;

    // =====================================================================
    // List Presidencies
    // =====================================================================

    @GetMapping
    @Operation(
            summary = "List all presidencies",
            description = "Returns a paginated list of U.S. presidencies, ordered by number (most recent first)"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully retrieved presidencies"),
            @ApiResponse(responseCode = "400", description = "Invalid pagination parameters",
                    content = @Content(schema = @Schema(hidden = true)))
    })
    public Page<PresidencyDTO> listPresidencies(
            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(defaultValue = "0") @Min(0) int page,

            @Parameter(description = "Page size (1-100)", example = "20")
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {

        log.info("GET /api/presidencies - page={}, size={}", page, size);
        return presidencyService.listPresidencies(page, size);
    }

    // =====================================================================
    // Current Presidency
    // =====================================================================

    @GetMapping("/current")
    @Operation(
            summary = "Get current presidency",
            description = "Returns the current (most recent) U.S. presidency with full details"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Current presidency found"),
            @ApiResponse(responseCode = "404", description = "No current presidency found",
                    content = @Content(schema = @Schema(hidden = true)))
    })
    public ResponseEntity<PresidencyDTO> getCurrentPresidency() {
        log.info("GET /api/presidencies/current");
        return presidencyService.getCurrentPresidency()
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // =====================================================================
    // Get by ID
    // =====================================================================

    @GetMapping("/{id}")
    @Operation(
            summary = "Get presidency by ID",
            description = "Returns a single presidency by its UUID with full details including VP list"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Presidency found"),
            @ApiResponse(responseCode = "404", description = "Presidency not found",
                    content = @Content(schema = @Schema(hidden = true)))
    })
    public ResponseEntity<PresidencyDTO> getPresidencyById(
            @Parameter(description = "Presidency UUID", example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID id) {

        log.info("GET /api/presidencies/{}", id);
        return presidencyService.getPresidencyById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // =====================================================================
    // Get by Number
    // =====================================================================

    @GetMapping("/number/{number}")
    @Operation(
            summary = "Get presidency by number",
            description = "Returns a presidency by its historical number (1-47) with full details"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Presidency found"),
            @ApiResponse(responseCode = "400", description = "Invalid presidency number",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", description = "Presidency not found",
                    content = @Content(schema = @Schema(hidden = true)))
    })
    public ResponseEntity<PresidencyDTO> getPresidencyByNumber(
            @Parameter(description = "Presidency number (1-47)", example = "47")
            @PathVariable @Min(1) @Max(99) Integer number) {

        log.info("GET /api/presidencies/number/{}", number);
        return presidencyService.getPresidencyByNumber(number)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // =====================================================================
    // Executive Orders
    // =====================================================================

    @GetMapping("/{id}/executive-orders")
    @Operation(
            summary = "Get executive orders for a presidency",
            description = "Returns a paginated list of Executive Orders signed during the specified presidency"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Executive orders retrieved"),
            @ApiResponse(responseCode = "400", description = "Invalid parameters",
                    content = @Content(schema = @Schema(hidden = true)))
    })
    public Page<ExecutiveOrderDTO> getExecutiveOrders(
            @Parameter(description = "Presidency UUID", example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID id,

            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(defaultValue = "0") @Min(0) int page,

            @Parameter(description = "Page size (1-100)", example = "20")
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {

        log.info("GET /api/presidencies/{}/executive-orders - page={}, size={}", id, page, size);
        return presidencyService.getExecutiveOrders(id, page, size);
    }

    // =====================================================================
    // Administration (VP, CoS, Cabinet)
    // =====================================================================

    @GetMapping("/{id}/administration")
    @Operation(
            summary = "Get administration for a presidency",
            description = "Returns Vice Presidents, Chiefs of Staff, and Cabinet members for the specified presidency"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Administration data retrieved"),
            @ApiResponse(responseCode = "404", description = "Presidency not found",
                    content = @Content(schema = @Schema(hidden = true)))
    })
    public ResponseEntity<PresidencyAdministrationDTO> getAdministration(
            @Parameter(description = "Presidency UUID", example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID id) {

        log.info("GET /api/presidencies/{}/administration", id);
        return presidencyService.getAdministration(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
