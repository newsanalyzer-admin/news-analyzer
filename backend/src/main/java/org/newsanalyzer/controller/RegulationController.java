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
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.newsanalyzer.dto.RegulationDTO;
import org.newsanalyzer.model.DocumentType;
import org.newsanalyzer.service.RegulationLookupService;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * REST controller for federal regulation lookup endpoints.
 * Provides query APIs for fact-checking and API consumers.
 *
 * @author James (Dev Agent)
 * @since 2.0.0
 */
@RestController
@RequestMapping("/api/regulations")
@Tag(name = "Regulations", description = "Federal Register regulation lookup and search endpoints")
@RequiredArgsConstructor
@Slf4j
@Validated
public class RegulationController {

    private final RegulationLookupService regulationLookupService;

    // =====================================================================
    // List Regulations
    // =====================================================================

    @GetMapping
    @Operation(
            summary = "List regulations",
            description = "Returns a paginated list of federal regulations, sorted by publication date (most recent first)"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully retrieved regulations"),
            @ApiResponse(responseCode = "400", description = "Invalid pagination parameters",
                    content = @Content(schema = @Schema(hidden = true)))
    })
    public Page<RegulationDTO> listRegulations(
            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(defaultValue = "0") @Min(0) int page,

            @Parameter(description = "Page size (1-100)", example = "20")
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {

        log.info("GET /api/regulations - page={}, size={}", page, size);
        return regulationLookupService.listRegulations(page, size);
    }

    // =====================================================================
    // Get by Document Number
    // =====================================================================

    @GetMapping("/{documentNumber}")
    @Operation(
            summary = "Get regulation by document number",
            description = "Returns a single regulation by its Federal Register document number"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Regulation found"),
            @ApiResponse(responseCode = "404", description = "Regulation not found",
                    content = @Content(schema = @Schema(hidden = true)))
    })
    public ResponseEntity<RegulationDTO> getByDocumentNumber(
            @Parameter(description = "Federal Register document number", example = "2024-12345")
            @PathVariable String documentNumber) {

        log.info("GET /api/regulations/{}", documentNumber);
        return regulationLookupService.findByDocumentNumber(documentNumber)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // =====================================================================
    // Full-Text Search
    // =====================================================================

    @GetMapping("/search")
    @Operation(
            summary = "Search regulations",
            description = "Full-text search across regulation titles and abstracts using PostgreSQL text search"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Search results"),
            @ApiResponse(responseCode = "400", description = "Invalid query or pagination parameters",
                    content = @Content(schema = @Schema(hidden = true)))
    })
    public Page<RegulationDTO> searchRegulations(
            @Parameter(description = "Search query", example = "emissions standards", required = true)
            @RequestParam @NotBlank String q,

            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(defaultValue = "0") @Min(0) int page,

            @Parameter(description = "Page size (1-100)", example = "20")
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {

        log.info("GET /api/regulations/search - q='{}', page={}, size={}", q, page, size);
        return regulationLookupService.searchRegulations(q, page, size);
    }

    // =====================================================================
    // Filter by Agency
    // =====================================================================

    @GetMapping("/by-agency/{orgId}")
    @Operation(
            summary = "Get regulations by agency",
            description = "Returns regulations issued by a specific government agency"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Regulations for the agency"),
            @ApiResponse(responseCode = "400", description = "Invalid organization ID",
                    content = @Content(schema = @Schema(hidden = true)))
    })
    public Page<RegulationDTO> getByAgency(
            @Parameter(description = "Government Organization ID", example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID orgId,

            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(defaultValue = "0") @Min(0) int page,

            @Parameter(description = "Page size (1-100)", example = "20")
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {

        log.info("GET /api/regulations/by-agency/{} - page={}, size={}", orgId, page, size);
        return regulationLookupService.findByAgency(orgId, page, size);
    }

    // =====================================================================
    // Filter by Document Type
    // =====================================================================

    @GetMapping("/by-type/{type}")
    @Operation(
            summary = "Get regulations by document type",
            description = "Filter regulations by type: RULE, PROPOSED_RULE, NOTICE, PRESIDENTIAL_DOCUMENT, or OTHER"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Regulations of the specified type"),
            @ApiResponse(responseCode = "400", description = "Invalid document type",
                    content = @Content(schema = @Schema(hidden = true)))
    })
    public Page<RegulationDTO> getByType(
            @Parameter(description = "Document type", example = "RULE")
            @PathVariable DocumentType type,

            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(defaultValue = "0") @Min(0) int page,

            @Parameter(description = "Page size (1-100)", example = "20")
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {

        log.info("GET /api/regulations/by-type/{} - page={}, size={}", type, page, size);
        return regulationLookupService.findByDocumentType(type, page, size);
    }

    // =====================================================================
    // Filter by Date Range
    // =====================================================================

    @GetMapping("/by-date-range")
    @Operation(
            summary = "Get regulations by publication date range",
            description = "Filter regulations published between two dates (inclusive)"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Regulations in the date range"),
            @ApiResponse(responseCode = "400", description = "Invalid date format or range",
                    content = @Content(schema = @Schema(hidden = true)))
    })
    public Page<RegulationDTO> getByDateRange(
            @Parameter(description = "Start date (inclusive, ISO format)", example = "2024-01-01")
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,

            @Parameter(description = "End date (inclusive, ISO format)", example = "2024-12-31")
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,

            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(defaultValue = "0") @Min(0) int page,

            @Parameter(description = "Page size (1-100)", example = "20")
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {

        log.info("GET /api/regulations/by-date-range - start={}, end={}, page={}, size={}", start, end, page, size);
        return regulationLookupService.findByDateRange(start, end, page, size);
    }

    // =====================================================================
    // Effective Date Query
    // =====================================================================

    @GetMapping("/effective-on/{date}")
    @Operation(
            summary = "Get rules effective on date",
            description = "Returns final rules (not proposed rules or notices) that are effective on or before the specified date"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Rules effective on the date"),
            @ApiResponse(responseCode = "400", description = "Invalid date format",
                    content = @Content(schema = @Schema(hidden = true)))
    })
    public List<RegulationDTO> getEffectiveOn(
            @Parameter(description = "Effective date (ISO format)", example = "2024-06-01")
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        log.info("GET /api/regulations/effective-on/{}", date);
        return regulationLookupService.findRulesEffectiveOn(date);
    }

    // =====================================================================
    // CFR Reference Lookup
    // =====================================================================

    @GetMapping("/cfr/{title}/{part}")
    @Operation(
            summary = "Get regulations by CFR citation",
            description = "Returns regulations that reference a specific CFR title and part"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Regulations referencing the CFR citation"),
            @ApiResponse(responseCode = "400", description = "Invalid CFR title or part",
                    content = @Content(schema = @Schema(hidden = true)))
    })
    public List<RegulationDTO> getByCfrReference(
            @Parameter(description = "CFR title number (e.g., 40 for environment)", example = "40")
            @PathVariable @Min(1) Integer title,

            @Parameter(description = "CFR part number", example = "60")
            @PathVariable @Min(1) Integer part) {

        log.info("GET /api/regulations/cfr/{}/{}", title, part);
        return regulationLookupService.findByCfrReference(title, part);
    }
}
