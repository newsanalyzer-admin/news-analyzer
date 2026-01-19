package org.newsanalyzer.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.newsanalyzer.dto.*;
import org.newsanalyzer.model.CongressionalMember;
import org.newsanalyzer.model.Individual;
import org.newsanalyzer.model.Person;
import org.newsanalyzer.model.Regulation;
import org.newsanalyzer.repository.CongressionalMemberRepository;
import org.newsanalyzer.repository.PersonRepository;
import org.newsanalyzer.repository.RegulationRepository;
import org.newsanalyzer.service.FederalRegisterImportService;
import org.newsanalyzer.service.LegislatorEnrichmentImportService;
import org.newsanalyzer.service.MemberSyncService;
import org.newsanalyzer.service.LegislatorsSearchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

/**
 * REST Controller for admin import operations from external APIs.
 *
 * Provides endpoints to import data from external sources like Congress.gov
 * into the local database.
 *
 * Base Path: /api/admin/import
 *
 * @author James (Dev Agent)
 * @since 2.0.0
 */
@RestController
@RequestMapping("/api/admin/import")
@Tag(name = "Admin Import", description = "Import data from external APIs")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"})
public class AdminImportController {

    private static final Logger log = LoggerFactory.getLogger(AdminImportController.class);

    private final MemberSyncService memberSyncService;
    private final CongressionalMemberRepository congressionalMemberRepository;
    private final PersonRepository personRepository;
    private final FederalRegisterImportService federalRegisterImportService;
    private final RegulationRepository regulationRepository;
    private final LegislatorEnrichmentImportService legislatorEnrichmentImportService;
    private final LegislatorsSearchService legislatorsSearchService;

    public AdminImportController(MemberSyncService memberSyncService,
                                  CongressionalMemberRepository congressionalMemberRepository,
                                  PersonRepository personRepository,
                                  FederalRegisterImportService federalRegisterImportService,
                                  RegulationRepository regulationRepository,
                                  LegislatorEnrichmentImportService legislatorEnrichmentImportService,
                                  LegislatorsSearchService legislatorsSearchService) {
        this.memberSyncService = memberSyncService;
        this.congressionalMemberRepository = congressionalMemberRepository;
        this.personRepository = personRepository;
        this.federalRegisterImportService = federalRegisterImportService;
        this.regulationRepository = regulationRepository;
        this.legislatorEnrichmentImportService = legislatorEnrichmentImportService;
        this.legislatorsSearchService = legislatorsSearchService;
    }

    // =====================================================================
    // Congress.gov Member Import
    // =====================================================================

    @PostMapping("/congress/member")
    @Operation(summary = "Import member from Congress.gov",
               description = "Import a specific member from Congress.gov by BioGuide ID. " +
                       "Creates a new record or updates existing if bioguideId already exists.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Import successful"),
        @ApiResponse(responseCode = "400", description = "Invalid request"),
        @ApiResponse(responseCode = "404", description = "Member not found on Congress.gov"),
        @ApiResponse(responseCode = "500", description = "Import failed")
    })
    public ResponseEntity<CongressImportResult> importCongressMember(
            @RequestBody CongressMemberImportRequest request
    ) {
        String bioguideId = request.getBioguideId();

        if (bioguideId == null || bioguideId.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(
                    CongressImportResult.builder()
                            .error("bioguideId is required")
                            .build()
            );
        }

        log.info("Importing Congress member: bioguideId={}, forceOverwrite={}",
                bioguideId, request.isForceOverwrite());

        // Check if already exists
        Optional<CongressionalMember> existing = congressionalMemberRepository.findByBioguideIdWithIndividual(bioguideId);
        boolean wasNew = existing.isEmpty();

        if (existing.isPresent() && !request.isForceOverwrite()) {
            // Return existing record info
            CongressionalMember member = existing.get();
            Individual individual = member.getIndividual();
            String name = individual != null
                    ? individual.getFirstName() + " " + individual.getLastName()
                    : "Unknown";
            return ResponseEntity.ok(
                    CongressImportResult.builder()
                            .id(member.getId().toString())
                            .bioguideId(member.getBioguideId())
                            .name(name)
                            .created(false)
                            .updated(false)
                            .error("Record already exists. Set forceOverwrite=true to update.")
                            .build()
            );
        }

        // Sync from Congress.gov
        Optional<CongressionalMember> syncedMember = memberSyncService.syncMemberByBioguideId(bioguideId);

        if (syncedMember.isEmpty()) {
            return ResponseEntity.status(404).body(
                    CongressImportResult.builder()
                            .bioguideId(bioguideId)
                            .error("Member not found on Congress.gov")
                            .build()
            );
        }

        CongressionalMember member = syncedMember.get();
        Individual individual = member.getIndividual();
        String name = individual != null
                ? individual.getFirstName() + " " + individual.getLastName()
                : "Unknown";
        return ResponseEntity.ok(
                CongressImportResult.builder()
                        .id(member.getId().toString())
                        .bioguideId(member.getBioguideId())
                        .name(name)
                        .created(wasNew)
                        .updated(!wasNew)
                        .build()
        );
    }

    @GetMapping("/congress/member/{bioguideId}/exists")
    @Operation(summary = "Check if member exists locally",
               description = "Check if a member with the given BioGuide ID already exists in the local database")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Check completed")
    })
    public ResponseEntity<ExistsResponse> checkMemberExists(
            @Parameter(description = "BioGuide ID to check")
            @PathVariable String bioguideId
    ) {
        Optional<CongressionalMember> existing = congressionalMemberRepository.findByBioguideIdWithIndividual(bioguideId);

        if (existing.isPresent()) {
            CongressionalMember member = existing.get();
            Individual individual = member.getIndividual();
            String name = individual != null
                    ? individual.getFirstName() + " " + individual.getLastName()
                    : "Unknown";
            return ResponseEntity.ok(new ExistsResponse(
                    true,
                    member.getId().toString(),
                    name
            ));
        }

        return ResponseEntity.ok(new ExistsResponse(false, null, null));
    }

    /**
     * Simple DTO for exists check response
     */
    public record ExistsResponse(boolean exists, String id, String name) {}

    // =====================================================================
    // Federal Register Document Import
    // =====================================================================

    @PostMapping("/federal-register/document")
    @Operation(summary = "Import document from Federal Register",
               description = "Import a specific document from Federal Register by document number. " +
                       "Creates a new Regulation record or updates existing if document number already exists. " +
                       "Automatically links agencies to GovernmentOrganization records.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Import successful"),
        @ApiResponse(responseCode = "400", description = "Invalid request"),
        @ApiResponse(responseCode = "404", description = "Document not found on Federal Register"),
        @ApiResponse(responseCode = "500", description = "Import failed")
    })
    public ResponseEntity<FederalRegisterImportResult> importFederalRegisterDocument(
            @RequestBody FederalRegisterImportRequest request
    ) {
        String documentNumber = request.getDocumentNumber();

        if (documentNumber == null || documentNumber.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(
                    FederalRegisterImportResult.builder()
                            .error("documentNumber is required")
                            .build()
            );
        }

        log.info("Importing Federal Register document: documentNumber={}, forceOverwrite={}",
                documentNumber, request.isForceOverwrite());

        FederalRegisterImportResult result = federalRegisterImportService.importDocument(
                documentNumber.trim(), request.isForceOverwrite());

        // Return 404 if document not found on Federal Register
        if (result.getError() != null && result.getError().contains("not found on Federal Register")) {
            return ResponseEntity.status(404).body(result);
        }

        return ResponseEntity.ok(result);
    }

    @GetMapping("/federal-register/document/{documentNumber}/exists")
    @Operation(summary = "Check if regulation exists locally",
               description = "Check if a regulation with the given document number already exists in the local database")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Check completed")
    })
    public ResponseEntity<RegulationExistsResponse> checkRegulationExists(
            @Parameter(description = "Federal Register document number to check")
            @PathVariable String documentNumber
    ) {
        Optional<Regulation> existing = regulationRepository.findByDocumentNumber(documentNumber);

        if (existing.isPresent()) {
            Regulation regulation = existing.get();
            return ResponseEntity.ok(new RegulationExistsResponse(
                    true,
                    regulation.getId().toString(),
                    regulation.getTitle()
            ));
        }

        return ResponseEntity.ok(new RegulationExistsResponse(false, null, null));
    }

    /**
     * Response DTO for regulation exists check
     */
    public record RegulationExistsResponse(boolean exists, String id, String title) {}

    // =====================================================================
    // Legislators Repo Enrichment
    // =====================================================================

    @GetMapping("/legislators/{bioguideId}/preview")
    @Operation(summary = "Preview legislator enrichment",
               description = "Preview what fields would be added/updated when enriching a Person " +
                       "record from the Legislators Repo. Shows current vs new values for comparison.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Preview generated"),
        @ApiResponse(responseCode = "404", description = "Legislator not found in Legislators Repo")
    })
    public ResponseEntity<EnrichmentPreview> previewLegislatorEnrichment(
            @Parameter(description = "BioGuide ID to preview enrichment for")
            @PathVariable String bioguideId
    ) {
        log.info("Previewing legislator enrichment: bioguideId={}", bioguideId);

        EnrichmentPreview preview = legislatorEnrichmentImportService.previewEnrichment(bioguideId);

        // Return 404 if not found in Legislators Repo
        if (!preview.isLocalMatch() && preview.getNewData() == null) {
            return ResponseEntity.status(404).body(preview);
        }

        return ResponseEntity.ok(preview);
    }

    @PostMapping("/legislators/enrich")
    @Operation(summary = "Enrich Person from Legislators Repo",
               description = "Enrich an existing Person record with data from the Legislators Repo. " +
                       "Adds external IDs and social media links. Only adds fields that don't already exist " +
                       "(does not overwrite existing data). Requires a local Person match via bioguideId.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Enrichment completed"),
        @ApiResponse(responseCode = "400", description = "Invalid request"),
        @ApiResponse(responseCode = "404", description = "No local Person match or legislator not found")
    })
    public ResponseEntity<LegislatorEnrichmentResult> enrichFromLegislatorsRepo(
            @RequestBody LegislatorEnrichmentRequest request
    ) {
        String bioguideId = request.getBioguideId();

        if (bioguideId == null || bioguideId.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(
                    LegislatorEnrichmentResult.builder()
                            .error("bioguideId is required")
                            .fieldsAdded(java.util.List.of())
                            .fieldsUpdated(java.util.List.of())
                            .totalChanges(0)
                            .build()
            );
        }

        log.info("Enriching Person from Legislators Repo: bioguideId={}", bioguideId);

        LegislatorEnrichmentResult result = legislatorEnrichmentImportService.enrichPerson(bioguideId.trim());

        // Return 404 if no match found
        if (!result.isMatched()) {
            return ResponseEntity.status(404).body(result);
        }

        return ResponseEntity.ok(result);
    }

    @GetMapping("/legislators/{bioguideId}/exists")
    @Operation(summary = "Check if legislator exists in Legislators Repo",
               description = "Check if a legislator with the given BioGuide ID exists in the Legislators Repo " +
                       "and whether there's a local Person match for enrichment")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Check completed")
    })
    public ResponseEntity<LegislatorExistsResponse> checkLegislatorExists(
            @Parameter(description = "BioGuide ID to check")
            @PathVariable String bioguideId
    ) {
        // Check if exists in Legislators Repo
        boolean existsInRepo = legislatorsSearchService.getLegislatorByBioguideId(bioguideId).isPresent();

        // Check if local Person match exists
        Optional<Person> localPerson = personRepository.findByBioguideId(bioguideId);

        return ResponseEntity.ok(new LegislatorExistsResponse(
                existsInRepo,
                localPerson.isPresent(),
                localPerson.map(p -> p.getId().toString()).orElse(null),
                localPerson.map(p -> p.getFirstName() + " " + p.getLastName()).orElse(null)
        ));
    }

    /**
     * Response DTO for legislator exists check
     */
    public record LegislatorExistsResponse(
            boolean existsInRepo,
            boolean localMatch,
            String localPersonId,
            String localPersonName
    ) {}
}
