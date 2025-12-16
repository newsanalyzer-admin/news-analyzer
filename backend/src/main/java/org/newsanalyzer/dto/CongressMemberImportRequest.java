package org.newsanalyzer.dto;

import lombok.Data;

/**
 * Request DTO for importing a Congress.gov member.
 *
 * @author James (Dev Agent)
 * @since 2.0.0
 */
@Data
public class CongressMemberImportRequest {

    /** BioGuide ID of the member to import */
    private String bioguideId;

    /** If true, overwrite existing record even if it exists */
    private boolean forceOverwrite;

    /** Optional: specific fields to update (if null, update all) */
    private String[] fieldsToUpdate;
}
