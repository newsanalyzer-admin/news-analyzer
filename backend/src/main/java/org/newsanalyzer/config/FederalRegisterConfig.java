package org.newsanalyzer.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import lombok.Data;

/**
 * Configuration properties for Federal Register API.
 *
 * @author James (Dev Agent)
 * @since 2.0.0
 */
@Configuration
@ConfigurationProperties(prefix = "federal-register")
@Data
public class FederalRegisterConfig {

    /**
     * Base URL for Federal Register API
     */
    private String baseUrl = "https://www.federalregister.gov/api/v1";

    /**
     * Request timeout in milliseconds
     */
    private int timeout = 30000;

    /**
     * Number of retry attempts for failed requests
     */
    private int retryAttempts = 3;

    /**
     * Rate limit delay between requests in milliseconds (good API citizenship)
     */
    private int rateLimitMs = 100;

    /**
     * Sync configuration for regulation documents
     */
    private SyncConfig sync = new SyncConfig();

    /**
     * Sync configuration properties.
     */
    @Data
    public static class SyncConfig {
        /**
         * Enable/disable automatic sync
         */
        private boolean enabled = false;

        /**
         * Cron expression for sync schedule (default: 3:00 AM UTC daily)
         */
        private String cron = "0 0 3 * * *";

        /**
         * Number of documents per page when fetching
         */
        private int pageSize = 100;

        /**
         * Maximum number of pages to fetch per sync (safety limit)
         */
        private int maxPages = 100;

        /**
         * Number of days to backfill on initial sync
         */
        private int initialBackfillDays = 365;
    }
}
