package org.newsanalyzer.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Result of FJC CSV import operation.
 *
 * @author James (Dev Agent)
 * @since 2.0.0
 */
@Data
@Builder
public class FjcImportResult {

    private boolean success;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    private int totalRecords;
    private int personsCreated;
    private int personsUpdated;
    private int positionsCreated;
    private int holdingsCreated;
    private int holdingsUpdated;
    private int skipped;
    private int errors;

    @Builder.Default
    private List<String> errorMessages = new ArrayList<>();

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
     * Add an error message
     */
    public void addError(String message) {
        if (errorMessages == null) {
            errorMessages = new ArrayList<>();
        }
        errorMessages.add(message);
        errors++;
    }

    /**
     * Get summary message
     */
    public String getSummary() {
        return String.format(
            "FJC Import: %d records processed in %ds. " +
            "Persons: %d created, %d updated. " +
            "Positions: %d created. Holdings: %d created, %d updated. " +
            "Skipped: %d, Errors: %d",
            totalRecords, getDurationSeconds(),
            personsCreated, personsUpdated,
            positionsCreated,
            holdingsCreated, holdingsUpdated,
            skipped, errors
        );
    }
}
