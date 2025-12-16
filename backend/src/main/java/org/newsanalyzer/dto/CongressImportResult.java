package org.newsanalyzer.dto;

import lombok.Builder;
import lombok.Data;

/**
 * Result DTO for Congress member import operations.
 *
 * @author James (Dev Agent)
 * @since 2.0.0
 */
@Data
@Builder
public class CongressImportResult {

    /** ID of the imported/updated record */
    private String id;

    /** True if a new record was created */
    private boolean created;

    /** True if an existing record was updated */
    private boolean updated;

    /** Error message if import failed */
    private String error;

    /** BioGuide ID of the imported member */
    private String bioguideId;

    /** Name of the imported member */
    private String name;
}
