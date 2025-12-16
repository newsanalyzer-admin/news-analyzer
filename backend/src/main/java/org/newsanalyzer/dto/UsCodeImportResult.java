package org.newsanalyzer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Result statistics from a US Code import operation.
 *
 * @author James (Dev Agent)
 * @since 2.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsCodeImportResult {

    /**
     * Title number that was imported (null for full import).
     */
    private Integer titleNumber;

    /**
     * Release point used for import.
     */
    private String releasePoint;

    /**
     * Number of sections newly inserted.
     */
    @Builder.Default
    private int sectionsInserted = 0;

    /**
     * Number of sections updated (existing records).
     */
    @Builder.Default
    private int sectionsUpdated = 0;

    /**
     * Number of sections that failed to import.
     */
    @Builder.Default
    private int sectionsFailed = 0;

    /**
     * Total sections processed.
     */
    @Builder.Default
    private int totalProcessed = 0;

    /**
     * When the import started.
     */
    private LocalDateTime startedAt;

    /**
     * When the import completed.
     */
    private LocalDateTime completedAt;

    /**
     * Whether the import completed successfully.
     */
    @Builder.Default
    private boolean success = false;

    /**
     * Error message if import failed.
     */
    private String errorMessage;

    /**
     * List of individual error messages (capped for memory).
     */
    @Builder.Default
    private List<String> errors = new ArrayList<>();

    /**
     * Maximum errors to collect before truncating.
     */
    private static final int MAX_ERRORS = 100;

    /**
     * Add an error message.
     */
    public void addError(String error) {
        if (errors.size() < MAX_ERRORS) {
            errors.add(error);
        }
        sectionsFailed++;
    }

    /**
     * Get duration of import.
     */
    public Duration getDuration() {
        if (startedAt != null && completedAt != null) {
            return Duration.between(startedAt, completedAt);
        }
        return Duration.ZERO;
    }

    /**
     * Get duration in human-readable format.
     */
    public String getDurationFormatted() {
        Duration duration = getDuration();
        long minutes = duration.toMinutes();
        long seconds = duration.minusMinutes(minutes).getSeconds();
        return String.format("%d min %d sec", minutes, seconds);
    }

    /**
     * Mark import as completed successfully.
     */
    public void markSuccess() {
        this.completedAt = LocalDateTime.now();
        this.success = true;
    }

    /**
     * Mark import as failed.
     */
    public void markFailed(String errorMessage) {
        this.completedAt = LocalDateTime.now();
        this.success = false;
        this.errorMessage = errorMessage;
    }

    /**
     * Create a new result for a title import.
     */
    public static UsCodeImportResult forTitle(int titleNumber, String releasePoint) {
        return UsCodeImportResult.builder()
                .titleNumber(titleNumber)
                .releasePoint(releasePoint)
                .startedAt(LocalDateTime.now())
                .build();
    }

    /**
     * Create a new result for a full import.
     */
    public static UsCodeImportResult forFullImport(String releasePoint) {
        return UsCodeImportResult.builder()
                .releasePoint(releasePoint)
                .startedAt(LocalDateTime.now())
                .build();
    }
}
