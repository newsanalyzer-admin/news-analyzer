package org.newsanalyzer.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;
import org.newsanalyzer.model.GovernmentOrganization;
import org.newsanalyzer.model.GovernmentPosition;

import java.time.LocalDate;
import java.util.UUID;

/**
 * DTO for executive branch position information.
 *
 * @author James (Dev Agent)
 * @since 2.0.0
 */
@Data
@Builder
public class ExecutivePositionDTO {

    private UUID id;
    private String title;
    private String appointmentType;
    private String appointmentTypeDescription;
    private String payPlan;
    private String payGrade;
    private String location;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate expirationDate;

    private String agencyName;
    private UUID organizationId;

    private String status; // "Filled" or "Vacant"
    private String currentHolder; // Name of current holder if filled

    /**
     * Create ExecutivePositionDTO from entity objects.
     *
     * @param position The government position
     * @param org The organization (may be null)
     * @param currentHolderName Name of current holder (null if vacant)
     * @return ExecutivePositionDTO
     */
    public static ExecutivePositionDTO from(GovernmentPosition position,
                                            GovernmentOrganization org,
                                            String currentHolderName) {
        ExecutivePositionDTOBuilder builder = ExecutivePositionDTO.builder();

        if (position != null) {
            builder.id(position.getId())
                   .title(position.getTitle())
                   .payPlan(position.getPayPlan())
                   .payGrade(position.getPayGrade())
                   .location(position.getLocation())
                   .expirationDate(position.getExpirationDate())
                   .organizationId(position.getOrganizationId());

            if (position.getAppointmentType() != null) {
                builder.appointmentType(position.getAppointmentType().name())
                       .appointmentTypeDescription(position.getAppointmentType().getDescription());
            }
        }

        if (org != null) {
            builder.agencyName(org.getOfficialName());
        }

        if (currentHolderName != null && !currentHolderName.isBlank()) {
            builder.status("Filled")
                   .currentHolder(currentHolderName);
        } else {
            builder.status("Vacant")
                   .currentHolder(null);
        }

        return builder.build();
    }

    /**
     * Create ExecutivePositionDTO for a vacant position.
     */
    public static ExecutivePositionDTO forVacant(GovernmentPosition position, GovernmentOrganization org) {
        return from(position, org, null);
    }
}
