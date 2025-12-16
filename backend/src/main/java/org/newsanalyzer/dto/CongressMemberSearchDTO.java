package org.newsanalyzer.dto;

import lombok.Builder;
import lombok.Data;

/**
 * DTO for Congress.gov member search results.
 *
 * Contains summary information for display in search results list.
 *
 * @author James (Dev Agent)
 * @since 2.0.0
 */
@Data
@Builder
public class CongressMemberSearchDTO {

    private String bioguideId;
    private String name;
    private String firstName;
    private String lastName;
    private String state;
    private String party;
    private String chamber;
    private String district;
    private boolean currentMember;
    private String imageUrl;
    private String url;
}
