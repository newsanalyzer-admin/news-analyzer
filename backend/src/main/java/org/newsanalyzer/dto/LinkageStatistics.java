package org.newsanalyzer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Statistics for agency-regulation linkage operations.
 *
 * @author James (Dev Agent)
 * @since 2.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LinkageStatistics {

    /**
     * Total number of regulations in the database.
     */
    private long totalRegulations;

    /**
     * Number of regulations successfully linked to at least one agency.
     */
    private long linkedRegulations;

    /**
     * Number of distinct unmatched agency names encountered.
     */
    private long unmatchedAgencyNames;

    /**
     * Number of agencies linked during this operation (for sync stats).
     */
    @Builder.Default
    private int agenciesLinkedInOperation = 0;

    /**
     * Calculate the linkage rate as a percentage.
     *
     * @return Percentage of regulations linked to agencies (0.0 - 100.0)
     */
    public double getLinkageRate() {
        if (totalRegulations == 0) {
            return 0.0;
        }
        return (double) linkedRegulations / totalRegulations * 100;
    }

    /**
     * Check if linkage rate meets the target threshold.
     *
     * @param targetRate Target percentage (e.g., 95.0 for 95%)
     * @return true if linkage rate is at or above target
     */
    public boolean meetsTarget(double targetRate) {
        return getLinkageRate() >= targetRate;
    }
}
