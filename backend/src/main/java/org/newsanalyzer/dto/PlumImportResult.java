package org.newsanalyzer.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Result DTO for PLUM CSV import operations.
 *
 * Tracks statistics and errors from the import process.
 *
 * @author James (Dev Agent)
 * @since 2.0.0
 */
@Data
@Builder
public class PlumImportResult {

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    private int totalRecords;
    private int personsCreated;
    private int personsUpdated;
    private int positionsCreated;
    private int positionsUpdated;
    private int holdingsCreated;
    private int holdingsUpdated;
    private int vacantPositions;
    private int skipped;
    private int errors;

    private int unmatchedAgencies;

    @Builder.Default
    private List<ImportError> errorDetails = new ArrayList<>();

    @Builder.Default
    private List<String> unmatchedAgencyNames = new ArrayList<>();

    /**
     * Add an error to the result
     */
    public void addError(int lineNumber, String message, String record) {
        if (errorDetails == null) {
            errorDetails = new ArrayList<>();
        }
        errorDetails.add(ImportError.builder()
                .lineNumber(lineNumber)
                .message(message)
                .record(record)
                .build());
        errors++;
    }

    /**
     * Add an unmatched agency
     */
    public void addUnmatchedAgency(String agencyName) {
        if (unmatchedAgencyNames == null) {
            unmatchedAgencyNames = new ArrayList<>();
        }
        if (!unmatchedAgencyNames.contains(agencyName)) {
            unmatchedAgencyNames.add(agencyName);
            unmatchedAgencies++;
        }
    }

    /**
     * Get duration in seconds
     */
    public Long getDurationSeconds() {
        if (startTime == null || endTime == null) {
            return null;
        }
        return java.time.Duration.between(startTime, endTime).getSeconds();
    }

    /**
     * Get success rate as percentage
     */
    public Double getSuccessRate() {
        if (totalRecords == 0) {
            return 0.0;
        }
        int successful = totalRecords - errors - skipped;
        return (successful * 100.0) / totalRecords;
    }

    /**
     * Build summary message
     */
    public String getSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("PLUM Import Complete: %d records processed in %d seconds%n",
                totalRecords, getDurationSeconds() != null ? getDurationSeconds() : 0));
        sb.append(String.format("  Persons:   %d created, %d updated%n", personsCreated, personsUpdated));
        sb.append(String.format("  Positions: %d created, %d updated%n", positionsCreated, positionsUpdated));
        sb.append(String.format("  Holdings:  %d created, %d updated%n", holdingsCreated, holdingsUpdated));
        sb.append(String.format("  Vacant:    %d%n", vacantPositions));
        sb.append(String.format("  Skipped:   %d%n", skipped));
        sb.append(String.format("  Errors:    %d%n", errors));
        if (unmatchedAgencies > 0) {
            sb.append(String.format("  Unmatched agencies: %d%n", unmatchedAgencies));
        }
        return sb.toString();
    }

    /**
     * Individual error detail
     */
    @Data
    @Builder
    public static class ImportError {
        private int lineNumber;
        private String message;
        private String record;
    }
}
