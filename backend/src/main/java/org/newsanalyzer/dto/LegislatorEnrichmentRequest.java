package org.newsanalyzer.dto;

import lombok.Data;

/**
 * Request DTO for enriching a Person record from Legislators Repo.
 *
 * @author James (Dev Agent)
 * @since 2.0.0
 */
@Data
public class LegislatorEnrichmentRequest {

    /** BioGuide ID of the legislator to enrich from */
    private String bioguideId;
}
