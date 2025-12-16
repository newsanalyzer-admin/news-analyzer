package org.newsanalyzer.scheduler;

import org.newsanalyzer.config.LegislatorsConfig;
import org.newsanalyzer.service.LegislatorsEnrichmentService;
import org.newsanalyzer.service.LegislatorsEnrichmentService.SyncResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Scheduled job for weekly enrichment sync from unitedstates/congress-legislators.
 *
 * Runs every Sunday at 4 AM UTC by default.
 * Stores the last sync commit hash to avoid redundant processing.
 *
 * @author James (Dev Agent)
 * @since 2.0.0
 */
@Component
public class EnrichmentScheduler {

    private static final Logger log = LoggerFactory.getLogger(EnrichmentScheduler.class);

    private final LegislatorsEnrichmentService enrichmentService;
    private final LegislatorsConfig config;

    // Track the last successful commit hash (in-memory; could be persisted in DB)
    private final AtomicReference<String> lastSyncCommit = new AtomicReference<>(null);
    private final AtomicReference<LocalDateTime> lastSyncTime = new AtomicReference<>(null);

    public EnrichmentScheduler(LegislatorsEnrichmentService enrichmentService,
                               LegislatorsConfig config) {
        this.enrichmentService = enrichmentService;
        this.config = config;
    }

    /**
     * Run weekly enrichment sync.
     * Schedule: Sunday 4 AM UTC (0 0 4 * * SUN)
     */
    @Scheduled(cron = "${legislators.sync.schedule:0 0 4 * * SUN}")
    public void runWeeklySync() {
        log.info("Starting scheduled enrichment sync");

        try {
            SyncResult result = enrichmentService.runFullSync(lastSyncCommit.get());

            if (result.success()) {
                lastSyncCommit.set(result.commitSha());
                lastSyncTime.set(LocalDateTime.now());
                log.info("Scheduled sync completed: {}", result.message());
            } else {
                log.error("Scheduled sync failed: {}", result.message());
            }

        } catch (Exception e) {
            log.error("Error during scheduled sync: {}", e.getMessage(), e);
        }
    }

    /**
     * Manual trigger for enrichment sync (called by admin endpoint).
     *
     * @param forceSync If true, ignores commit hash check and forces sync
     * @return SyncResult with details
     */
    public SyncResult triggerManualSync(boolean forceSync) {
        log.info("Manual sync triggered (forceSync={})", forceSync);

        String lastCommit = forceSync ? null : lastSyncCommit.get();
        SyncResult result = enrichmentService.runFullSync(lastCommit);

        if (result.success()) {
            lastSyncCommit.set(result.commitSha());
            lastSyncTime.set(LocalDateTime.now());
        }

        return result;
    }

    /**
     * Get last sync status information.
     */
    public SyncStatus getStatus() {
        return new SyncStatus(
                lastSyncCommit.get(),
                lastSyncTime.get(),
                config.getSync().getSchedule()
        );
    }

    /**
     * Status record for sync information.
     */
    public record SyncStatus(
            String lastCommitSha,
            LocalDateTime lastSyncTime,
            String schedule
    ) {}
}
