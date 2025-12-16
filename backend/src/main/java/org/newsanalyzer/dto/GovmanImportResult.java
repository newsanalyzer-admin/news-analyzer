package org.newsanalyzer.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Result DTO for GOVMAN XML import operations.
 *
 * Tracks statistics and errors from the import process.
 *
 * @author James (Dev Agent)
 * @since 2.0.0
 */
@Data
@Builder
public class GovmanImportResult {

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    private int total;
    private int imported;
    private int updated;
    private int skipped;
    private int errors;

    @Builder.Default
    private List<String> errorDetails = new ArrayList<>();

    /**
     * Add an error message to the result.
     */
    public void addError(String message) {
        if (errorDetails == null) {
            errorDetails = new ArrayList<>();
        }
        errorDetails.add(message);
        errors++;
    }

    /**
     * Add an error with entity context.
     */
    public void addError(String entityId, String message) {
        if (errorDetails == null) {
            errorDetails = new ArrayList<>();
        }
        errorDetails.add(String.format("[Entity %s] %s", entityId, message));
        errors++;
    }

    /**
     * Get duration in seconds.
     */
    public Long getDurationSeconds() {
        if (startTime == null || endTime == null) {
            return null;
        }
        return Duration.between(startTime, endTime).getSeconds();
    }

    /**
     * Get duration in milliseconds.
     */
    public Long getDurationMillis() {
        if (startTime == null || endTime == null) {
            return null;
        }
        return Duration.between(startTime, endTime).toMillis();
    }

    /**
     * Get success rate as percentage.
     */
    public Double getSuccessRate() {
        if (total == 0) {
            return 0.0;
        }
        int successful = imported + updated;
        return (successful * 100.0) / total;
    }

    /**
     * Build summary message.
     */
    public String getSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("GOVMAN Import Complete: %d entities processed in %d seconds%n",
                total, getDurationSeconds() != null ? getDurationSeconds() : 0));
        sb.append(String.format("  Imported: %d%n", imported));
        sb.append(String.format("  Updated:  %d%n", updated));
        sb.append(String.format("  Skipped:  %d%n", skipped));
        sb.append(String.format("  Errors:   %d%n", errors));
        sb.append(String.format("  Success rate: %.1f%%%n", getSuccessRate()));
        return sb.toString();
    }

    /**
     * Increment the imported count.
     */
    public void incrementImported() {
        this.imported++;
    }

    /**
     * Increment the updated count.
     */
    public void incrementUpdated() {
        this.updated++;
    }

    /**
     * Increment the skipped count.
     */
    public void incrementSkipped() {
        this.skipped++;
    }
}
