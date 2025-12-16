package org.newsanalyzer.service;

import lombok.RequiredArgsConstructor;
import org.newsanalyzer.config.FederalRegisterConfig;
import org.newsanalyzer.dto.DocumentQueryParams;
import org.newsanalyzer.dto.FederalRegisterAgency;
import org.newsanalyzer.dto.FederalRegisterDocument;
import org.newsanalyzer.dto.SyncStatistics;
import org.newsanalyzer.model.CfrReference;
import org.newsanalyzer.model.DocumentType;
import org.newsanalyzer.model.Regulation;
import org.newsanalyzer.repository.RegulationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * Service for synchronizing regulations from the Federal Register API.
 *
 * @author James (Dev Agent)
 * @since 2.0.0
 */
@Service
@RequiredArgsConstructor
public class RegulationSyncService {

    private static final Logger log = LoggerFactory.getLogger(RegulationSyncService.class);

    private final FederalRegisterClient federalRegisterClient;
    private final RegulationRepository regulationRepository;
    private final FederalRegisterConfig config;
    private final AgencyLinkageService agencyLinkageService;

    // Track sync status for status endpoint
    private final AtomicReference<SyncStatistics> lastSyncStats = new AtomicReference<>();
    private final AtomicReference<SyncStatistics> currentSyncStats = new AtomicReference<>();

    // Document types to sync
    private static final List<String> SYNC_DOCUMENT_TYPES = Arrays.asList(
            "Rule", "Proposed Rule", "Notice", "Presidential Document"
    );

    /**
     * Perform a full sync of regulations.
     * Fetches documents since the last sync date or initial backfill period.
     *
     * @return Sync statistics
     */
    @Transactional
    public SyncStatistics syncRegulations() {
        log.info("Starting regulation sync");

        SyncStatistics stats = SyncStatistics.builder()
                .startTime(LocalDateTime.now())
                .status(SyncStatistics.SyncStatus.RUNNING)
                .build();
        currentSyncStats.set(stats);

        try {
            // Refresh agency linkage caches before sync
            agencyLinkageService.refreshCaches();
            // Clear previous unmatched agencies tracking
            agencyLinkageService.clearUnmatchedAgencies();

            // Determine start date for sync
            LocalDate syncFromDate = determineSyncStartDate();
            log.info("Syncing regulations from date: {}", syncFromDate);

            // Build query parameters
            DocumentQueryParams params = DocumentQueryParams.builder()
                    .publicationDateGte(syncFromDate)
                    .documentTypes(SYNC_DOCUMENT_TYPES)
                    .perPage(config.getSync().getPageSize())
                    .build();

            // Fetch all documents
            List<FederalRegisterDocument> documents = federalRegisterClient.fetchAllDocuments(
                    params, config.getSync().getMaxPages());

            stats.setFetched(documents.size());
            log.info("Fetched {} documents from Federal Register API", documents.size());

            // Process each document
            int created = 0;
            int updated = 0;
            int skipped = 0;
            int errors = 0;
            int linkedAgencies = 0;

            for (FederalRegisterDocument doc : documents) {
                try {
                    ProcessResultWithLinkage result = processDocument(doc);
                    switch (result.result) {
                        case CREATED -> created++;
                        case UPDATED -> updated++;
                        case SKIPPED -> skipped++;
                    }
                    linkedAgencies += result.linkedAgencies;
                } catch (Exception e) {
                    log.error("Error processing document {}: {}", doc.getDocumentNumber(), e.getMessage());
                    errors++;
                }
            }

            stats.setCreated(created);
            stats.setUpdated(updated);
            stats.setSkipped(skipped);
            stats.setErrors(errors);
            stats.setLinkedAgencies(linkedAgencies);
            stats.setUnmatchedAgencies(agencyLinkageService.getUnmatchedCount());
            stats.setEndTime(LocalDateTime.now());
            stats.setStatus(SyncStatistics.SyncStatus.COMPLETED);

            log.info("Regulation sync completed: fetched={}, created={}, updated={}, skipped={}, errors={}, " +
                    "linkedAgencies={}, unmatchedAgencies={}, duration={}s",
                    stats.getFetched(), stats.getCreated(), stats.getUpdated(),
                    stats.getSkipped(), stats.getErrors(), stats.getLinkedAgencies(),
                    stats.getUnmatchedAgencies(), stats.getDurationSeconds());

        } catch (Exception e) {
            log.error("Regulation sync failed: {}", e.getMessage(), e);
            stats.setEndTime(LocalDateTime.now());
            stats.setStatus(SyncStatistics.SyncStatus.FAILED);
            stats.setErrorMessage(e.getMessage());
        }

        lastSyncStats.set(stats);
        currentSyncStats.set(null);
        return stats;
    }

    /**
     * Determine the start date for sync based on last known regulation.
     */
    private LocalDate determineSyncStartDate() {
        Optional<Regulation> lastRegulation = regulationRepository.findTopByOrderByPublicationDateDesc();

        if (lastRegulation.isPresent()) {
            // Sync from day after last known publication
            return lastRegulation.get().getPublicationDate();
        } else {
            // Initial sync: backfill configured number of days
            return LocalDate.now().minusDays(config.getSync().getInitialBackfillDays());
        }
    }

    /**
     * Process a single document from the API.
     */
    private ProcessResultWithLinkage processDocument(FederalRegisterDocument doc) {
        if (doc.getDocumentNumber() == null || doc.getDocumentNumber().isBlank()) {
            log.warn("Skipping document with null/empty document number");
            return new ProcessResultWithLinkage(ProcessResult.SKIPPED, 0);
        }

        Optional<Regulation> existing = regulationRepository.findByDocumentNumber(doc.getDocumentNumber());
        int linkedCount = 0;

        if (existing.isPresent()) {
            // Update existing regulation if needed
            Regulation regulation = existing.get();
            if (hasChanges(regulation, doc)) {
                updateRegulation(regulation, doc);
                regulationRepository.save(regulation);
                // Link agencies for updated regulation
                linkedCount = linkAgencies(regulation, doc);
                log.debug("Updated regulation: {}", doc.getDocumentNumber());
                return new ProcessResultWithLinkage(ProcessResult.UPDATED, linkedCount);
            } else {
                log.trace("Skipped unchanged regulation: {}", doc.getDocumentNumber());
                return new ProcessResultWithLinkage(ProcessResult.SKIPPED, 0);
            }
        } else {
            // Create new regulation
            Regulation regulation = createRegulation(doc);
            regulationRepository.save(regulation);
            // Link agencies for new regulation
            linkedCount = linkAgencies(regulation, doc);
            log.debug("Created regulation: {}", doc.getDocumentNumber());
            return new ProcessResultWithLinkage(ProcessResult.CREATED, linkedCount);
        }
    }

    /**
     * Link agencies to a regulation.
     */
    private int linkAgencies(Regulation regulation, FederalRegisterDocument doc) {
        List<FederalRegisterAgency> agencies = doc.getAgencies();
        if (agencies == null || agencies.isEmpty()) {
            return 0;
        }
        return agencyLinkageService.linkRegulationToAgencies(regulation, agencies);
    }

    /**
     * Check if the API document has changes compared to stored regulation.
     */
    private boolean hasChanges(Regulation existing, FederalRegisterDocument doc) {
        // Compare key fields that might change
        if (!nullSafeEquals(existing.getTitle(), doc.getTitle())) return true;
        if (!nullSafeEquals(existing.getDocumentAbstract(), doc.getDocumentAbstract())) return true;
        if (!nullSafeEquals(existing.getEffectiveOn(), doc.getEffectiveOn())) return true;
        return false;
    }

    private boolean nullSafeEquals(Object a, Object b) {
        if (a == null && b == null) return true;
        if (a == null || b == null) return false;
        return a.equals(b);
    }

    /**
     * Create a new Regulation entity from API document.
     */
    private Regulation createRegulation(FederalRegisterDocument doc) {
        return Regulation.builder()
                .documentNumber(doc.getDocumentNumber())
                .title(truncate(doc.getTitle(), 1000))
                .documentAbstract(doc.getDocumentAbstract())
                .documentType(DocumentType.fromFederalRegisterType(doc.getType()))
                .publicationDate(doc.getPublicationDate())
                .effectiveOn(doc.getEffectiveOn())
                .signingDate(doc.getSigningDate())
                .regulationIdNumber(doc.getRegulationIdNumber())
                .cfrReferences(convertCfrReferences(doc.getCfrReferences()))
                .docketIds(doc.getDocketIds())
                .sourceUrl(buildSourceUrl(doc.getDocumentNumber()))
                .pdfUrl(doc.getPdfUrl())
                .htmlUrl(doc.getHtmlUrl())
                .build();
    }

    /**
     * Update an existing Regulation entity from API document.
     */
    private void updateRegulation(Regulation regulation, FederalRegisterDocument doc) {
        regulation.setTitle(truncate(doc.getTitle(), 1000));
        regulation.setDocumentAbstract(doc.getDocumentAbstract());
        regulation.setDocumentType(DocumentType.fromFederalRegisterType(doc.getType()));
        regulation.setEffectiveOn(doc.getEffectiveOn());
        regulation.setSigningDate(doc.getSigningDate());
        regulation.setRegulationIdNumber(doc.getRegulationIdNumber());
        regulation.setCfrReferences(convertCfrReferences(doc.getCfrReferences()));
        regulation.setDocketIds(doc.getDocketIds());
        regulation.setPdfUrl(doc.getPdfUrl());
        regulation.setHtmlUrl(doc.getHtmlUrl());
    }

    /**
     * Convert API CFR references to model CFR references.
     */
    private List<CfrReference> convertCfrReferences(List<FederalRegisterDocument.FederalRegisterCfrReference> apiRefs) {
        if (apiRefs == null || apiRefs.isEmpty()) {
            return null;
        }
        return apiRefs.stream()
                .map(ref -> new CfrReference(ref.getTitle(), ref.getPart(), ref.getSection()))
                .collect(Collectors.toList());
    }

    /**
     * Build the source URL for a document.
     */
    private String buildSourceUrl(String documentNumber) {
        return "https://www.federalregister.gov/d/" + documentNumber;
    }

    /**
     * Truncate a string to max length.
     */
    private String truncate(String str, int maxLength) {
        if (str == null) return null;
        if (str.length() <= maxLength) return str;
        return str.substring(0, maxLength - 3) + "...";
    }

    /**
     * Get the status of the current or last sync.
     */
    public SyncStatistics getSyncStatus() {
        SyncStatistics current = currentSyncStats.get();
        if (current != null) {
            return current;
        }
        return lastSyncStats.get();
    }

    /**
     * Check if a sync is currently running.
     */
    public boolean isSyncRunning() {
        return currentSyncStats.get() != null;
    }

    private enum ProcessResult {
        CREATED, UPDATED, SKIPPED
    }

    /**
     * Wrapper for process result with linkage count.
     */
    private record ProcessResultWithLinkage(ProcessResult result, int linkedAgencies) {}
}
