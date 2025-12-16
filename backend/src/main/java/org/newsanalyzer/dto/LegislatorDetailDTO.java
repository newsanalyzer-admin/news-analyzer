package org.newsanalyzer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * DTO for full legislator details from the Legislators Repo.
 *
 * @author James (Dev Agent)
 * @since 2.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LegislatorDetailDTO {

    /** BioGuide ID - unique identifier */
    private String bioguideId;

    /** Name components */
    private NameInfo name;

    /** Bio information */
    private BioInfo bio;

    /** Current/most recent term info */
    private TermInfo currentTerm;

    /** All terms served */
    private List<TermInfo> terms;

    /** Social media handles with IDs */
    private SocialMediaInfo socialMedia;

    /** All external IDs */
    private ExternalIdsInfo externalIds;

    /** Whether currently serving */
    private boolean currentMember;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NameInfo {
        private String first;
        private String last;
        private String middle;
        private String suffix;
        private String nickname;
        private String officialFull;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BioInfo {
        private String birthday;
        private String gender;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TermInfo {
        private String type;
        private String start;
        private String end;
        private String state;
        private String party;
        private Integer district;
        private Integer senateClass;
        private String stateRank;
        private String url;
        private String phone;
        private String office;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SocialMediaInfo {
        private String twitter;
        private String twitterId;
        private String facebook;
        private String facebookId;
        private String youtube;
        private String youtubeId;
        private String instagram;
        private String instagramId;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExternalIdsInfo {
        private String bioguide;
        private String thomas;
        private Integer govtrack;
        private String opensecrets;
        private Integer votesmart;
        private List<String> fec;
        private String wikipedia;
        private String ballotpedia;
        private String icpsr;
        private String lis;
        private Integer cspan;
        private Integer houseHistory;
    }
}
