package org.newsanalyzer.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.newsanalyzer.exception.ResourceNotFoundException;
import org.newsanalyzer.model.CommitteeMembership;
import org.newsanalyzer.model.Person;
import org.newsanalyzer.model.Person.Chamber;
import org.newsanalyzer.model.PositionHolding;
import org.newsanalyzer.service.CommitteeService;
import org.newsanalyzer.service.MemberService;
import org.newsanalyzer.service.MemberSyncService;
import org.newsanalyzer.service.LegislatorsEnrichmentService;
import org.newsanalyzer.service.TermSyncService;
import org.newsanalyzer.scheduler.EnrichmentScheduler;
import org.newsanalyzer.repository.PositionHoldingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * REST Controller for Congressional Member lookup.
 *
 * Provides endpoints for:
 * - Listing all members (paginated)
 * - Looking up by BioGuide ID
 * - Searching by name
 * - Filtering by state or chamber
 * - Sync trigger (admin)
 *
 * Base Path: /api/members
 *
 * @author James (Dev Agent)
 * @since 2.0.0
 */
@RestController
@RequestMapping("/api/members")
@Tag(name = "Members", description = "Congressional member lookup")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"})
public class MemberController {

    private static final Logger log = LoggerFactory.getLogger(MemberController.class);

    private final MemberService memberService;
    private final MemberSyncService memberSyncService;
    private final CommitteeService committeeService;
    private final EnrichmentScheduler enrichmentScheduler;
    private final TermSyncService termSyncService;
    private final PositionHoldingRepository positionHoldingRepository;

    public MemberController(MemberService memberService,
                           MemberSyncService memberSyncService,
                           CommitteeService committeeService,
                           EnrichmentScheduler enrichmentScheduler,
                           TermSyncService termSyncService,
                           PositionHoldingRepository positionHoldingRepository) {
        this.memberService = memberService;
        this.memberSyncService = memberSyncService;
        this.committeeService = committeeService;
        this.enrichmentScheduler = enrichmentScheduler;
        this.termSyncService = termSyncService;
        this.positionHoldingRepository = positionHoldingRepository;
    }

    // =====================================================================
    // List Endpoints
    // =====================================================================

    @GetMapping
    @Operation(summary = "List all members",
               description = "Get paginated list of all Congressional members")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "List of members returned")
    })
    public ResponseEntity<Page<Person>> listAll(Pageable pageable) {
        Page<Person> members = memberService.findAll(pageable);
        return ResponseEntity.ok(members);
    }

    // =====================================================================
    // Lookup Endpoints
    // =====================================================================

    @GetMapping("/{bioguideId}")
    @Operation(summary = "Get member by BioGuide ID",
               description = "Retrieve a specific member by their Congress.gov BioGuide ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Member found"),
        @ApiResponse(responseCode = "404", description = "Member not found")
    })
    public ResponseEntity<Person> getByBioguideId(
            @Parameter(description = "BioGuide ID (e.g., S000033 for Bernie Sanders)")
            @PathVariable String bioguideId) {
        return memberService.findByBioguideId(bioguideId)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found: " + bioguideId));
    }

    // =====================================================================
    // Search Endpoints
    // =====================================================================

    @GetMapping("/search")
    @Operation(summary = "Search members by name",
               description = "Search for members by first or last name (case-insensitive)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Search results returned")
    })
    public ResponseEntity<Page<Person>> searchByName(
            @Parameter(description = "Name to search for")
            @RequestParam String name,
            Pageable pageable) {
        if (name == null || name.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        Page<Person> members = memberService.searchByName(name.trim(), pageable);
        return ResponseEntity.ok(members);
    }

    // =====================================================================
    // Filter Endpoints
    // =====================================================================

    @GetMapping("/by-state/{state}")
    @Operation(summary = "List members by state",
               description = "Get all members representing a specific state")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Members from state returned"),
        @ApiResponse(responseCode = "400", description = "Invalid state code")
    })
    public ResponseEntity<Page<Person>> getByState(
            @Parameter(description = "2-letter state code (e.g., CA, TX, NY)")
            @PathVariable String state,
            Pageable pageable) {
        if (state == null || state.length() != 2) {
            return ResponseEntity.badRequest().build();
        }
        Page<Person> members = memberService.findByState(state.toUpperCase(), pageable);
        return ResponseEntity.ok(members);
    }

    @GetMapping("/by-chamber/{chamber}")
    @Operation(summary = "List members by chamber",
               description = "Get all members in a specific chamber (SENATE or HOUSE)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Members from chamber returned"),
        @ApiResponse(responseCode = "400", description = "Invalid chamber")
    })
    public ResponseEntity<Page<Person>> getByChamber(
            @Parameter(description = "Chamber: SENATE or HOUSE")
            @PathVariable String chamber,
            Pageable pageable) {
        try {
            Chamber chamberEnum = Chamber.valueOf(chamber.toUpperCase());
            Page<Person> members = memberService.findByChamber(chamberEnum, pageable);
            return ResponseEntity.ok(members);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid chamber: {}", chamber);
            return ResponseEntity.badRequest().build();
        }
    }

    // =====================================================================
    // Statistics Endpoints
    // =====================================================================

    @GetMapping("/count")
    @Operation(summary = "Get total member count",
               description = "Returns the total number of members in the database")
    public ResponseEntity<Long> count() {
        return ResponseEntity.ok(memberService.count());
    }

    @GetMapping("/stats/party")
    @Operation(summary = "Get party distribution",
               description = "Returns count of members by party")
    public ResponseEntity<List<Object[]>> getPartyDistribution() {
        return ResponseEntity.ok(memberService.getPartyDistribution());
    }

    @GetMapping("/stats/state")
    @Operation(summary = "Get state distribution",
               description = "Returns count of members by state")
    public ResponseEntity<List<Object[]>> getStateDistribution() {
        return ResponseEntity.ok(memberService.getStateDistribution());
    }

    // =====================================================================
    // Cross-Reference Lookup Endpoints
    // =====================================================================

    @GetMapping("/by-external-id/{type}/{id}")
    @Operation(summary = "Find member by external ID",
               description = "Look up a member by an external ID type (fec, govtrack, opensecrets, votesmart)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Member found"),
        @ApiResponse(responseCode = "400", description = "Invalid ID type"),
        @ApiResponse(responseCode = "404", description = "Member not found")
    })
    public ResponseEntity<Person> getByExternalId(
            @Parameter(description = "External ID type: fec, govtrack, opensecrets, votesmart")
            @PathVariable String type,
            @Parameter(description = "External ID value")
            @PathVariable String id) {
        log.debug("Looking up member by external ID: type={}, id={}", type, id);

        return memberService.findByExternalId(type, id)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("Member not found with %s ID: %s", type, id)));
    }

    // =====================================================================
    // Term History Endpoints
    // =====================================================================

    @GetMapping("/{bioguideId}/terms")
    @Operation(summary = "Get member's term history",
               description = "List all Congressional terms for a member")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Term list returned"),
        @ApiResponse(responseCode = "404", description = "Member not found")
    })
    public ResponseEntity<List<PositionHolding>> getMemberTerms(
            @Parameter(description = "BioGuide ID of the member")
            @PathVariable String bioguideId) {

        // Verify member exists and get their ID
        Person member = memberService.getByBioguideId(bioguideId);

        List<PositionHolding> terms = positionHoldingRepository
                .findByPersonIdOrderByStartDateDesc(member.getId());
        return ResponseEntity.ok(terms);
    }

    @GetMapping("/on-date/{date}")
    @Operation(summary = "List members in office on date",
               description = "Get all members who were serving in Congress on a specific date")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Members on date returned"),
        @ApiResponse(responseCode = "400", description = "Invalid date format (use YYYY-MM-DD)")
    })
    public ResponseEntity<Page<PositionHolding>> getMembersOnDate(
            @Parameter(description = "Date in YYYY-MM-DD format")
            @PathVariable String date,
            Pageable pageable) {
        try {
            LocalDate queryDate = LocalDate.parse(date);
            Page<PositionHolding> holdings = positionHoldingRepository
                    .findAllActiveOnDate(queryDate, pageable);
            return ResponseEntity.ok(holdings);
        } catch (DateTimeParseException e) {
            log.warn("Invalid date format: {}", date);
            return ResponseEntity.badRequest().build();
        }
    }

    // =====================================================================
    // Committee Endpoints
    // =====================================================================

    @GetMapping("/{bioguideId}/committees")
    @Operation(summary = "Get member's committee assignments",
               description = "List all committees a member serves on")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Committee list returned"),
        @ApiResponse(responseCode = "404", description = "Member not found")
    })
    public ResponseEntity<Page<CommitteeMembership>> getMemberCommittees(
            @Parameter(description = "BioGuide ID of the member")
            @PathVariable String bioguideId,
            @Parameter(description = "Congress session number (e.g., 118)")
            @RequestParam(required = false) Integer congress,
            Pageable pageable) {

        // Verify member exists
        memberService.getByBioguideId(bioguideId);

        Page<CommitteeMembership> committees = committeeService.findCommitteesForMember(bioguideId, pageable);
        return ResponseEntity.ok(committees);
    }

    // =====================================================================
    // Admin Endpoints
    // =====================================================================

    @PostMapping("/sync")
    @Operation(summary = "Trigger member sync",
               description = "Trigger a full sync of all current members from Congress.gov API (admin only)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Sync completed"),
        @ApiResponse(responseCode = "500", description = "Sync failed")
    })
    public ResponseEntity<MemberSyncService.SyncResult> triggerSync() {
        log.info("Manual sync triggered via API");
        MemberSyncService.SyncResult result = memberSyncService.syncAllCurrentMembers();
        return ResponseEntity.ok(result);
    }

    @PostMapping("/enrichment-sync")
    @Operation(summary = "Trigger enrichment sync",
               description = "Trigger sync from unitedstates/congress-legislators GitHub repo (admin only)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Enrichment sync completed"),
        @ApiResponse(responseCode = "500", description = "Enrichment sync failed")
    })
    public ResponseEntity<LegislatorsEnrichmentService.SyncResult> triggerEnrichmentSync(
            @Parameter(description = "Force sync even if commit unchanged")
            @RequestParam(defaultValue = "false") boolean force) {
        log.info("Manual enrichment sync triggered via API (force={})", force);
        LegislatorsEnrichmentService.SyncResult result = enrichmentScheduler.triggerManualSync(force);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/enrichment-status")
    @Operation(summary = "Get enrichment sync status",
               description = "Get the status of the enrichment sync scheduler")
    public ResponseEntity<EnrichmentScheduler.SyncStatus> getEnrichmentStatus() {
        return ResponseEntity.ok(enrichmentScheduler.getStatus());
    }

    @PostMapping("/sync-terms")
    @Operation(summary = "Trigger term history sync",
               description = "Sync term history for all members from Congress.gov API (admin only)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Term sync completed"),
        @ApiResponse(responseCode = "500", description = "Term sync failed")
    })
    public ResponseEntity<TermSyncService.SyncResult> triggerTermSync() {
        log.info("Manual term sync triggered via API");
        TermSyncService.SyncResult result = termSyncService.syncAllCurrentMemberTerms();
        return ResponseEntity.ok(result);
    }
}
