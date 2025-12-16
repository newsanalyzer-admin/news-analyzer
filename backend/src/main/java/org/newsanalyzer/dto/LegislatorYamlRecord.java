package org.newsanalyzer.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * DTO representing a legislator record from the unitedstates/congress-legislators YAML file.
 *
 * @author James (Dev Agent)
 * @since 2.0.0
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class LegislatorYamlRecord {

    private LegislatorId id;
    private LegislatorName name;
    private LegislatorBio bio;
    private List<LegislatorTerm> terms;
    private LegislatorSocial social;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class LegislatorId {
        private String bioguide;
        private String thomas;
        private Integer govtrack;
        private String opensecrets;
        private Integer votesmart;
        private Object fec; // Can be String or List<String>
        private String wikipedia;
        private String ballotpedia;
        private String icpsr;
        private String lis;
        @JsonProperty("cspan")
        private Integer cspan;
        @JsonProperty("house_history")
        private Integer houseHistory;
        @JsonProperty("bioguide_previous")
        private List<String> bioguidesPrevious;

        /**
         * Get FEC IDs as a list (handles both single value and array in YAML)
         */
        public List<String> getFecIds() {
            if (fec == null) {
                return List.of();
            }
            if (fec instanceof List) {
                @SuppressWarnings("unchecked")
                List<String> fecList = (List<String>) fec;
                return fecList;
            }
            return List.of(fec.toString());
        }
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class LegislatorName {
        private String first;
        private String last;
        private String middle;
        private String suffix;
        private String nickname;
        @JsonProperty("official_full")
        private String officialFull;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class LegislatorBio {
        private String birthday;
        private String gender;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class LegislatorTerm {
        private String type; // "sen" or "rep"
        private String start;
        private String end;
        private String state;
        private String party;
        @JsonProperty("class")
        private Integer senateClass;
        private Integer district;
        private String url;
        private String address;
        private String phone;
        private String fax;
        @JsonProperty("contact_form")
        private String contactForm;
        private String office;
        @JsonProperty("state_rank")
        private String stateRank;
        @JsonProperty("rss_url")
        private String rssUrl;
        @JsonProperty("caucus")
        private String caucus;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class LegislatorSocial {
        private String twitter;
        @JsonProperty("twitter_id")
        private String twitterId;
        private String facebook;
        @JsonProperty("facebook_id")
        private String facebookId;
        private String youtube;
        @JsonProperty("youtube_id")
        private String youtubeId;
        private String instagram;
        @JsonProperty("instagram_id")
        private String instagramId;
    }

    /**
     * Build external IDs map for storing in Person.externalIds JSONB
     */
    public Map<String, Object> buildExternalIdsMap() {
        if (id == null) {
            return Map.of();
        }
        var map = new java.util.HashMap<String, Object>();
        if (id.getGovtrack() != null) map.put("govtrack", id.getGovtrack());
        if (id.getOpensecrets() != null) map.put("opensecrets", id.getOpensecrets());
        if (id.getVotesmart() != null) map.put("votesmart", id.getVotesmart());
        if (!id.getFecIds().isEmpty()) map.put("fec", id.getFecIds());
        if (id.getThomas() != null) map.put("thomas", id.getThomas());
        if (id.getWikipedia() != null) map.put("wikipedia", id.getWikipedia());
        if (id.getBallotpedia() != null) map.put("ballotpedia", id.getBallotpedia());
        if (id.getIcpsr() != null) map.put("icpsr", id.getIcpsr());
        if (id.getLis() != null) map.put("lis", id.getLis());
        if (id.getCspan() != null) map.put("cspan", id.getCspan());
        if (id.getHouseHistory() != null) map.put("house_history", id.getHouseHistory());
        return map;
    }

    /**
     * Build social media map for storing in Person.socialMedia JSONB
     */
    public Map<String, String> buildSocialMediaMap() {
        if (social == null) {
            return Map.of();
        }
        var map = new java.util.HashMap<String, String>();
        if (social.getTwitter() != null) map.put("twitter", social.getTwitter());
        if (social.getFacebook() != null) map.put("facebook", social.getFacebook());
        if (social.getYoutube() != null) map.put("youtube", social.getYoutube());
        if (social.getInstagram() != null) map.put("instagram", social.getInstagram());
        return map;
    }

    /**
     * Get the BioGuide ID
     */
    public String getBioguideId() {
        return id != null ? id.getBioguide() : null;
    }
}
