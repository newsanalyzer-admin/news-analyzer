package org.newsanalyzer.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Data Transfer Object for Presidency administration data.
 * Contains Vice Presidents and Chiefs of Staff for a presidency.
 *
 * @author James (Dev Agent)
 * @since 2.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PresidencyAdministrationDTO {

    private UUID presidencyId;
    private Integer presidencyNumber;
    private String presidencyLabel;         // "47th Presidency"

    private List<OfficeholderDTO> vicePresidents;
    private List<OfficeholderDTO> chiefsOfStaff;
    private List<CabinetMemberDTO> cabinetMembers;

    // =====================================================================
    // Nested DTOs
    // =====================================================================

    /**
     * DTO for an officeholder (VP, CoS, etc.)
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OfficeholderDTO {
        private UUID holdingId;
        private UUID personId;
        private String fullName;
        private String firstName;
        private String lastName;
        private String positionTitle;

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        private LocalDate startDate;

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        private LocalDate endDate;

        private String termLabel;           // "1789-1797" or "2021-present"
        private String imageUrl;
    }

    /**
     * DTO for Cabinet member information.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CabinetMemberDTO {
        private UUID holdingId;
        private UUID personId;
        private String fullName;
        private String positionTitle;
        private String departmentName;
        private UUID departmentId;

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        private LocalDate startDate;

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        private LocalDate endDate;
    }
}
