package org.newsanalyzer.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.newsanalyzer.exception.ResourceNotFoundException;
import org.newsanalyzer.model.GovernmentPosition;
import org.newsanalyzer.model.Person.Chamber;
import org.newsanalyzer.model.PositionHolding;
import org.newsanalyzer.repository.GovernmentPositionRepository;
import org.newsanalyzer.repository.PositionHoldingRepository;
import org.newsanalyzer.service.PositionInitializationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.UUID;

/**
 * REST Controller for Congressional Positions.
 *
 * Provides endpoints for:
 * - Listing all positions (Senate seats, House seats)
 * - Getting position by ID
 * - Filtering by chamber or state
 * - Position history (who held position over time)
 *
 * Base Path: /api/positions
 *
 * @author James (Dev Agent)
 * @since 2.0.0
 */
@RestController
@RequestMapping("/api/positions")
@Tag(name = "Positions", description = "Congressional positions (seats)")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"})
public class PositionController {

    private static final Logger log = LoggerFactory.getLogger(PositionController.class);

    private final GovernmentPositionRepository positionRepository;
    private final PositionHoldingRepository holdingRepository;
    private final PositionInitializationService positionInitService;

    public PositionController(GovernmentPositionRepository positionRepository,
                              PositionHoldingRepository holdingRepository,
                              PositionInitializationService positionInitService) {
        this.positionRepository = positionRepository;
        this.holdingRepository = holdingRepository;
        this.positionInitService = positionInitService;
    }

    // =====================================================================
    // List Endpoints
    // =====================================================================

    @GetMapping
    @Operation(summary = "List all positions",
               description = "Get paginated list of all Congressional positions (Senate and House seats)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "List of positions returned")
    })
    public ResponseEntity<Page<GovernmentPosition>> listAll(Pageable pageable) {
        Page<GovernmentPosition> positions = positionRepository.findAll(pageable);
        return ResponseEntity.ok(positions);
    }

    @GetMapping("/senate")
    @Operation(summary = "List Senate positions",
               description = "Get all 100 Senate seat positions")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "List of Senate positions returned")
    })
    public ResponseEntity<List<GovernmentPosition>> listSenatePositions() {
        List<GovernmentPosition> positions = positionRepository.findAllSenatePositions();
        return ResponseEntity.ok(positions);
    }

    @GetMapping("/house")
    @Operation(summary = "List House positions",
               description = "Get all 435 House seat positions")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "List of House positions returned")
    })
    public ResponseEntity<List<GovernmentPosition>> listHousePositions() {
        List<GovernmentPosition> positions = positionRepository.findAllHousePositions();
        return ResponseEntity.ok(positions);
    }

    // =====================================================================
    // Lookup Endpoints
    // =====================================================================

    @GetMapping("/{id}")
    @Operation(summary = "Get position by ID",
               description = "Retrieve a specific position by its UUID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Position found"),
        @ApiResponse(responseCode = "404", description = "Position not found")
    })
    public ResponseEntity<GovernmentPosition> getById(
            @Parameter(description = "Position UUID")
            @PathVariable UUID id) {
        return positionRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException("Position not found: " + id));
    }

    // =====================================================================
    // Filter Endpoints
    // =====================================================================

    @GetMapping("/by-chamber/{chamber}")
    @Operation(summary = "List positions by chamber",
               description = "Get all positions in a specific chamber (SENATE or HOUSE)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Positions from chamber returned"),
        @ApiResponse(responseCode = "400", description = "Invalid chamber")
    })
    public ResponseEntity<Page<GovernmentPosition>> getByChamber(
            @Parameter(description = "Chamber: SENATE or HOUSE")
            @PathVariable String chamber,
            Pageable pageable) {
        try {
            Chamber chamberEnum = Chamber.valueOf(chamber.toUpperCase());
            Page<GovernmentPosition> positions = positionRepository.findByChamber(chamberEnum, pageable);
            return ResponseEntity.ok(positions);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid chamber: {}", chamber);
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/by-state/{state}")
    @Operation(summary = "List positions by state",
               description = "Get all positions for a specific state")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Positions from state returned"),
        @ApiResponse(responseCode = "400", description = "Invalid state code")
    })
    public ResponseEntity<Page<GovernmentPosition>> getByState(
            @Parameter(description = "2-letter state code (e.g., CA, TX, NY)")
            @PathVariable String state,
            Pageable pageable) {
        if (state == null || state.length() != 2) {
            return ResponseEntity.badRequest().build();
        }
        Page<GovernmentPosition> positions = positionRepository.findByState(state.toUpperCase(), pageable);
        return ResponseEntity.ok(positions);
    }

    // =====================================================================
    // History Endpoints
    // =====================================================================

    @GetMapping("/{id}/history")
    @Operation(summary = "Get position holders history",
               description = "Get all people who have held this position over time")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Position history returned"),
        @ApiResponse(responseCode = "404", description = "Position not found")
    })
    public ResponseEntity<List<PositionHolding>> getPositionHistory(
            @Parameter(description = "Position UUID")
            @PathVariable UUID id) {
        // Verify position exists
        if (!positionRepository.existsById(id)) {
            throw new ResourceNotFoundException("Position not found: " + id);
        }

        List<PositionHolding> history = holdingRepository.findByPositionIdOrderByStartDateDesc(id);
        return ResponseEntity.ok(history);
    }

    @GetMapping("/{id}/holder")
    @Operation(summary = "Get current position holder",
               description = "Get the person currently holding this position")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Current holder returned"),
        @ApiResponse(responseCode = "404", description = "Position not found or vacant")
    })
    public ResponseEntity<List<PositionHolding>> getCurrentHolder(
            @Parameter(description = "Position UUID")
            @PathVariable UUID id) {
        // Verify position exists
        if (!positionRepository.existsById(id)) {
            throw new ResourceNotFoundException("Position not found: " + id);
        }

        List<PositionHolding> currentHolders = holdingRepository.findCurrentHoldersByPositionId(id);
        return ResponseEntity.ok(currentHolders);
    }

    @GetMapping("/{id}/holder-on/{date}")
    @Operation(summary = "Get position holder on date",
               description = "Get who held this position on a specific date")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Holder on date returned"),
        @ApiResponse(responseCode = "400", description = "Invalid date format (use YYYY-MM-DD)"),
        @ApiResponse(responseCode = "404", description = "Position not found")
    })
    public ResponseEntity<List<PositionHolding>> getHolderOnDate(
            @Parameter(description = "Position UUID")
            @PathVariable UUID id,
            @Parameter(description = "Date in YYYY-MM-DD format")
            @PathVariable String date) {
        // Verify position exists
        if (!positionRepository.existsById(id)) {
            throw new ResourceNotFoundException("Position not found: " + id);
        }

        try {
            LocalDate queryDate = LocalDate.parse(date);
            List<PositionHolding> holders = holdingRepository.findByPositionIdAndActiveOnDate(id, queryDate);
            return ResponseEntity.ok(holders);
        } catch (DateTimeParseException e) {
            log.warn("Invalid date format: {}", date);
            return ResponseEntity.badRequest().build();
        }
    }

    // =====================================================================
    // Statistics Endpoints
    // =====================================================================

    @GetMapping("/count")
    @Operation(summary = "Get total position count",
               description = "Returns the total number of positions")
    public ResponseEntity<Long> count() {
        return ResponseEntity.ok(positionRepository.count());
    }

    @GetMapping("/stats")
    @Operation(summary = "Get position statistics",
               description = "Returns count of positions by chamber")
    public ResponseEntity<PositionInitializationService.PositionStats> getStats() {
        return ResponseEntity.ok(positionInitService.getPositionStats());
    }

    // =====================================================================
    // Admin Endpoints
    // =====================================================================

    @PostMapping("/initialize")
    @Operation(summary = "Initialize all positions",
               description = "Create all Senate and House positions if not already present (admin only)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Initialization completed"),
        @ApiResponse(responseCode = "500", description = "Initialization failed")
    })
    public ResponseEntity<PositionInitializationService.InitResult> initializePositions() {
        log.info("Position initialization triggered via API");
        PositionInitializationService.InitResult result = positionInitService.initializeAllPositions();
        return ResponseEntity.ok(result);
    }
}
