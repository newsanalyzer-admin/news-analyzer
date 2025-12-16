package org.newsanalyzer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Statistics from a regulation sync operation.
 *
 * @author James (Dev Agent)
 * @since 2.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SyncStatistics {

    /**
     * Total documents fetched from API
     */
    private int fetched;

    /**
     * New documents created in database
     */
    private int created;

    /**
     * Existing documents updated
     */
    private int updated;

    /**
     * Documents skipped (already up to date)
     */
    private int skipped;

    /**
     * Documents that failed to process
     */
    private int errors;

    /**
     * Number of agency links created during sync
     */
    private int linkedAgencies;

    /**
     * Number of unmatched agency names encountered
     */
    private int unmatchedAgencies;

    /**
     * Start time of the sync
     */
    private LocalDateTime startTime;

    /**
     * End time of the sync
     */
    private LocalDateTime endTime;

    /**
     * Status of the sync (RUNNING, COMPLETED, FAILED)
     */
    private SyncStatus status;

    /**
     * Error message if sync failed
     */
    private String errorMessage;

    /**
     * Calculate duration in seconds
     */
    public long getDurationSeconds() {
        if (startTime == null || endTime == null) {
            return 0;
        }
        return java.time.Duration.between(startTime, endTime).getSeconds();
    }

    /**
     * Sync status enum
     */
    public enum SyncStatus {
        RUNNING,
        COMPLETED,
        FAILED
    }
}
