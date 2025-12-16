package org.newsanalyzer.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.newsanalyzer.dto.*;
import org.newsanalyzer.service.CongressSearchService;
import org.newsanalyzer.service.FederalRegisterSearchService;
import org.newsanalyzer.service.LegislatorsSearchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * REST Controller for admin search operations against external APIs.
 *
 * Provides search endpoints that proxy to external data sources like Congress.gov,
 * with duplicate detection and rate limit tracking.
 *
 * Base Path: /api/admin/search
 *
 * @author James (Dev Agent)
 * @since 2.0.0
 */
@RestController
@RequestMapping("/api/admin/search")
@Tag(name = "Admin Search", description = "Search external APIs for data import")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"})
public class AdminSearchController {

    private static final Logger log = LoggerFactory.getLogger(AdminSearchController.class);

    private final CongressSearchService congressSearchService;
    private final FederalRegisterSearchService federalRegisterSearchService;
    private final LegislatorsSearchService legislatorsSearchService;

    public AdminSearchController(CongressSearchService congressSearchService,
                                  FederalRegisterSearchService federalRegisterSearchService,
                                  LegislatorsSearchService legislatorsSearchService) {
        this.congressSearchService = congressSearchService;
        this.federalRegisterSearchService = federalRegisterSearchService;
        this.legislatorsSearchService = legislatorsSearchService;
    }

    // =====================================================================
    // Congress.gov Member Search
    // =====================================================================

    @GetMapping("/congress/members")
    @Operation(summary = "Search Congress.gov members",
               description = "Search for Congressional members from Congress.gov API. " +
                       "Results include duplicate detection against local database.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Search results returned"),
        @ApiResponse(responseCode = "429", description = "Rate limit exceeded"),
        @ApiResponse(responseCode = "500", description = "Search failed")
    })
    public ResponseEntity<CongressSearchResponse<CongressMemberSearchDTO>> searchCongressMembers(
            @Parameter(description = "Name filter (partial match)")
            @RequestParam(required = false) String name,

            @Parameter(description = "State filter (2-letter code, e.g., CA, NY)")
            @RequestParam(required = false) String state,

            @Parameter(description = "Party filter (D, R, I, L)")
            @RequestParam(required = false) String party,

            @Parameter(description = "Chamber filter (house, senate)")
            @RequestParam(required = false) String chamber,

            @Parameter(description = "Congress number (e.g., 118)")
            @RequestParam(required = false) Integer congress,

            @Parameter(description = "Page number (1-indexed)")
            @RequestParam(defaultValue = "1") int page,

            @Parameter(description = "Results per page")
            @RequestParam(defaultValue = "20") int pageSize
    ) {
        log.info("Congress member search: name={}, state={}, party={}, chamber={}, congress={}, page={}, pageSize={}",
                name, state, party, chamber, congress, page, pageSize);

        CongressSearchResponse<CongressMemberSearchDTO> response = congressSearchService.searchMembers(
                name, state, party, chamber, congress, page, pageSize);

        // Add rate limit info to response headers
        HttpHeaders headers = new HttpHeaders();
        if (response.getRateLimitRemaining() != null) {
            headers.add("X-RateLimit-Remaining", response.getRateLimitRemaining().toString());
        }
        if (response.getRateLimitResetSeconds() != null) {
            headers.add("X-RateLimit-Reset", response.getRateLimitResetSeconds().toString());
        }

        return ResponseEntity.ok()
                .headers(headers)
                .body(response);
    }

    @GetMapping("/congress/members/{bioguideId}")
    @Operation(summary = "Get Congress.gov member details",
               description = "Get full details for a specific member from Congress.gov by BioGuide ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Member details returned"),
        @ApiResponse(responseCode = "404", description = "Member not found"),
        @ApiResponse(responseCode = "500", description = "Fetch failed")
    })
    public ResponseEntity<CongressMemberSearchDTO> getCongressMember(
            @Parameter(description = "BioGuide ID of the member")
            @PathVariable String bioguideId
    ) {
        log.info("Fetching Congress member details: bioguideId={}", bioguideId);

        Optional<CongressMemberSearchDTO> member = congressSearchService.getMemberByBioguideId(bioguideId);

        return member.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // =====================================================================
    // Federal Register Document Search
    // =====================================================================

    @GetMapping("/federal-register/documents")
    @Operation(summary = "Search Federal Register documents",
               description = "Search for documents from the Federal Register API. " +
                       "Results include duplicate detection against local Regulation table.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Search results returned"),
        @ApiResponse(responseCode = "500", description = "Search failed")
    })
    public ResponseEntity<FederalRegisterSearchResponse<FederalRegisterSearchDTO>> searchFederalRegisterDocuments(
            @Parameter(description = "Keyword filter (searches title)")
            @RequestParam(required = false) String keyword,

            @Parameter(description = "Federal Register agency ID filter")
            @RequestParam(required = false) Integer agencyId,

            @Parameter(description = "Document type filter (Rule, Proposed Rule, Notice, Presidential Document)")
            @RequestParam(required = false) String documentType,

            @Parameter(description = "Publication date from (inclusive, ISO format: YYYY-MM-DD)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,

            @Parameter(description = "Publication date to (inclusive, ISO format: YYYY-MM-DD)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,

            @Parameter(description = "Page number (1-indexed)")
            @RequestParam(defaultValue = "1") int page,

            @Parameter(description = "Results per page")
            @RequestParam(defaultValue = "20") int pageSize
    ) {
        log.info("Federal Register search: keyword={}, agencyId={}, documentType={}, dateFrom={}, dateTo={}, page={}, pageSize={}",
                keyword, agencyId, documentType, dateFrom, dateTo, page, pageSize);

        FederalRegisterSearchResponse<FederalRegisterSearchDTO> response =
                federalRegisterSearchService.searchDocuments(keyword, agencyId, documentType, dateFrom, dateTo, page, pageSize);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/federal-register/documents/{documentNumber}")
    @Operation(summary = "Get Federal Register document details",
               description = "Get full details for a specific document from Federal Register by document number")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Document details returned"),
        @ApiResponse(responseCode = "404", description = "Document not found"),
        @ApiResponse(responseCode = "500", description = "Fetch failed")
    })
    public ResponseEntity<FederalRegisterDetailDTO> getFederalRegisterDocument(
            @Parameter(description = "Federal Register document number (e.g., 2024-12345)")
            @PathVariable String documentNumber
    ) {
        log.info("Fetching Federal Register document: documentNumber={}", documentNumber);

        Optional<FederalRegisterDetailDTO> doc = federalRegisterSearchService.getDocumentByNumber(documentNumber);

        return doc.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/federal-register/agencies")
    @Operation(summary = "Get Federal Register agencies",
               description = "Get list of all agencies from Federal Register API for filtering")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Agencies list returned"),
        @ApiResponse(responseCode = "500", description = "Fetch failed")
    })
    public ResponseEntity<List<FederalRegisterAgency>> getFederalRegisterAgencies() {
        log.info("Fetching Federal Register agencies");

        List<FederalRegisterAgency> agencies = federalRegisterSearchService.getAllAgencies();

        return ResponseEntity.ok(agencies);
    }

    // =====================================================================
    // Legislators Repo Search (unitedstates/congress-legislators)
    // =====================================================================

    @GetMapping("/legislators")
    @Operation(summary = "Search Legislators Repo",
               description = "Search legislators from the unitedstates/congress-legislators GitHub repository. " +
                       "Results include local match detection against Person table.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Search results returned"),
        @ApiResponse(responseCode = "500", description = "Search failed")
    })
    public ResponseEntity<LegislatorsSearchResponse<LegislatorSearchDTO>> searchLegislators(
            @Parameter(description = "Name filter (partial match)")
            @RequestParam(required = false) String name,

            @Parameter(description = "BioGuide ID filter (exact match)")
            @RequestParam(required = false) String bioguideId,

            @Parameter(description = "State filter (2-letter code, e.g., CA, NY)")
            @RequestParam(required = false) String state,

            @Parameter(description = "Page number (1-indexed)")
            @RequestParam(defaultValue = "1") int page,

            @Parameter(description = "Results per page")
            @RequestParam(defaultValue = "20") int pageSize
    ) {
        log.info("Legislators search: name={}, bioguideId={}, state={}, page={}, pageSize={}",
                name, bioguideId, state, page, pageSize);

        LegislatorsSearchResponse<LegislatorSearchDTO> response =
                legislatorsSearchService.searchLegislators(name, bioguideId, state, page, pageSize);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/legislators/{bioguideId}")
    @Operation(summary = "Get legislator details from Legislators Repo",
               description = "Get full details for a specific legislator from the GitHub repository")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Legislator details returned"),
        @ApiResponse(responseCode = "404", description = "Legislator not found"),
        @ApiResponse(responseCode = "500", description = "Fetch failed")
    })
    public ResponseEntity<LegislatorDetailDTO> getLegislator(
            @Parameter(description = "BioGuide ID of the legislator")
            @PathVariable String bioguideId
    ) {
        log.info("Fetching legislator details: bioguideId={}", bioguideId);

        Optional<LegislatorDetailDTO> legislator = legislatorsSearchService.getLegislatorByBioguideId(bioguideId);

        return legislator.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
