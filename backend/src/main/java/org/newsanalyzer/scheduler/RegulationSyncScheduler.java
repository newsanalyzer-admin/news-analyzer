package org.newsanalyzer.scheduler;

import lombok.RequiredArgsConstructor;
import org.newsanalyzer.config.FederalRegisterConfig;
import org.newsanalyzer.dto.SyncStatistics;
import org.newsanalyzer.service.RegulationSyncService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduler for automatic regulation synchronization.
 *
 * Runs daily at 3:00 AM UTC by default. Can be enabled/disabled
 * via configuration property federal-register.sync.enabled.
 *
 * @author James (Dev Agent)
 * @since 2.0.0
 */
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "federal-register.sync.enabled", havingValue = "true")
public class RegulationSyncScheduler {

    private static final Logger log = LoggerFactory.getLogger(RegulationSyncScheduler.class);

    private final RegulationSyncService regulationSyncService;
    private final FederalRegisterConfig config;

    /**
     * Scheduled sync job.
     * Runs according to the configured cron expression.
     */
    @Scheduled(cron = "${federal-register.sync.cron:0 0 3 * * *}")
    public void scheduledSync() {
        log.info("Starting scheduled regulation sync (cron: {})", config.getSync().getCron());

        try {
            SyncStatistics stats = regulationSyncService.syncRegulations();

            if (stats.getStatus() == SyncStatistics.SyncStatus.COMPLETED) {
                log.info("Scheduled regulation sync completed successfully: " +
                         "fetched={}, created={}, updated={}, errors={}, duration={}s",
                        stats.getFetched(), stats.getCreated(), stats.getUpdated(),
                        stats.getErrors(), stats.getDurationSeconds());
            } else {
                log.error("Scheduled regulation sync failed: {}", stats.getErrorMessage());
            }
        } catch (Exception e) {
            log.error("Scheduled regulation sync threw unexpected exception", e);
        }
    }
}
