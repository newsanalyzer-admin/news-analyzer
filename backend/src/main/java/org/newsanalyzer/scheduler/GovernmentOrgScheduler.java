package org.newsanalyzer.scheduler;

import org.newsanalyzer.service.GovernmentOrgSyncService;
import org.newsanalyzer.service.GovernmentOrgSyncService.SyncResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Scheduled job for weekly sync from Federal Register API.
 *
 * Runs every Sunday at 5 AM UTC by default (one hour after legislators sync).
 * Disabled by default via gov-org.sync.enabled=false.
 *
 * @author James (Dev Agent)
 * @since 2.0.0
 */
@Component
@ConditionalOnProperty(name = "gov-org.sync.enabled", havingValue = "true")
public class GovernmentOrgScheduler {

    private static final Logger log = LoggerFactory.getLogger(GovernmentOrgScheduler.class);

    private final GovernmentOrgSyncService syncService;

    // Track last sync time (in-memory; sync status is also available via service)
    private final AtomicReference<LocalDateTime> lastScheduledSyncTime = new AtomicReference<>(null);
    private final AtomicReference<SyncResult> lastSyncResult = new AtomicReference<>(null);

    public GovernmentOrgScheduler(GovernmentOrgSyncService syncService) {
        this.syncService = syncService;
    }

    /**
     * Run weekly Federal Register sync.
     * Schedule: Sunday 5 AM UTC (0 0 5 * * SUN)
     */
    @Scheduled(cron = "${gov-org.sync.schedule:0 0 5 * * SUN}")
    public void runWeeklySync() {
        log.info("Starting scheduled Federal Register sync");

        try {
            SyncResult result = syncService.syncFromFederalRegister();
            lastScheduledSyncTime.set(LocalDateTime.now());
            lastSyncResult.set(result);

            if (result.getErrors() == 0) {
                log.info("Scheduled Federal Register sync completed successfully: {}", result);
            } else {
                log.warn("Scheduled Federal Register sync completed with {} errors: {}",
                        result.getErrors(), result);
            }

        } catch (Exception e) {
            log.error("Error during scheduled Federal Register sync: {}", e.getMessage(), e);
        }
    }

    /**
     * Get last scheduled sync time.
     */
    public LocalDateTime getLastScheduledSyncTime() {
        return lastScheduledSyncTime.get();
    }

    /**
     * Get last sync result.
     */
    public SyncResult getLastSyncResult() {
        return lastSyncResult.get();
    }
}
