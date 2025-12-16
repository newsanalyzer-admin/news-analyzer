package org.newsanalyzer.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * DTO for detailed Congress.gov member information.
 *
 * Contains full details for preview modal display.
 *
 * @author James (Dev Agent)
 * @since 2.0.0
 */
@Data
@Builder
public class CongressMemberDetailDTO {

    private String bioguideId;
    private String name;
    private String firstName;
    private String lastName;
    private String middleName;
    private String suffix;
    private String state;
    private String party;
    private String chamber;
    private String district;
    private boolean currentMember;
    private String imageUrl;
    private String url;

    // Birth information
    private String birthDate;

    // Contact information
    private String officialWebsiteUrl;
    private String addressLine1;
    private String addressLine2;
    private String phone;

    // Terms served
    private List<TermInfo> terms;

    // External IDs
    private Integer govtrackId;
    private String opensecretsId;
    private Integer votesmartId;
    private String icpsrId;
    private String lisId;

    /**
     * Term information
     */
    @Data
    @Builder
    public static class TermInfo {
        private String chamber;
        private Integer congress;
        private String startYear;
        private String endYear;
        private String stateCode;
        private String district;
    }
}
