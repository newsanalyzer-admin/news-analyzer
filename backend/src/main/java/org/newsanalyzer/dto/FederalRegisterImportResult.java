package org.newsanalyzer.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * Result DTO for Federal Register document import.
 *
 * @author James (Dev Agent)
 * @since 2.0.0
 */
@Data
@Builder
public class FederalRegisterImportResult {

    /** ID of the created/updated Regulation record */
    private String id;

    /** Document number that was imported */
    private String documentNumber;

    /** Document title */
    private String title;

    /** Whether a new record was created */
    private boolean created;

    /** Whether an existing record was updated */
    private boolean updated;

    /** Number of agencies linked */
    private int linkedAgencies;

    /** Names of agencies that were successfully linked */
    private List<String> linkedAgencyNames;

    /** Names of agencies that could not be matched */
    private List<String> unmatchedAgencyNames;

    /** Error message if import failed */
    private String error;
}
