package org.newsanalyzer.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * Result DTO for legislator enrichment operation.
 *
 * @author James (Dev Agent)
 * @since 2.0.0
 */
@Data
@Builder
public class LegislatorEnrichmentResult {

    /** Whether a local Person was found and enriched */
    private boolean matched;

    /** Person ID if matched */
    private String personId;

    /** Person name if matched */
    private String personName;

    /** BioGuide ID used for matching */
    private String bioguideId;

    /** Fields that were added (did not exist before) */
    private List<String> fieldsAdded;

    /** Fields that were updated (had different values) */
    private List<String> fieldsUpdated;

    /** Total number of changes made */
    private int totalChanges;

    /** Error message if enrichment failed */
    private String error;
}
