package org.newsanalyzer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * DTO for legislator search results from the Legislators Repo.
 *
 * @author James (Dev Agent)
 * @since 2.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LegislatorSearchDTO {

    /** BioGuide ID - unique identifier */
    private String bioguideId;

    /** Full name of the legislator */
    private String name;

    /** State code (e.g., CA, NY) */
    private String state;

    /** Party affiliation */
    private String party;

    /** Chamber (Senate or House) */
    private String chamber;

    /** Whether currently serving */
    private boolean currentMember;

    /** Social media handles */
    private Map<String, String> socialMedia;

    /** External IDs from various sources */
    private Map<String, Object> externalIds;

    /** Number of external IDs available */
    private int externalIdCount;

    /** Number of social media accounts available */
    private int socialMediaCount;
}
