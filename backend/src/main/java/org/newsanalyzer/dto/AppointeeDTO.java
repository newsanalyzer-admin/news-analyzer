package org.newsanalyzer.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;
import org.newsanalyzer.model.*;

import java.time.LocalDate;
import java.util.UUID;

/**
 * DTO for executive branch appointee information.
 *
 * Combines data from Individual, GovernmentPosition, PositionHolding, and GovernmentOrganization
 * to provide a complete view of an appointee.
 *
 * Part of ARCH-1.6: Updated to use Individual instead of Person.
 *
 * @author James (Dev Agent)
 * @since 2.0.0
 */
@Data
@Builder
public class AppointeeDTO {

    private UUID id;
    private String firstName;
    private String lastName;
    private String fullName;

    private String positionTitle;
    private UUID positionId;
    private String appointmentType;
    private String appointmentTypeDescription;
    private String payPlan;
    private String payGrade;
    private String location;

    private String agencyName;
    private String organizationName;
    private UUID organizationId;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate expirationDate;

    private Integer tenure;
    private boolean current;
    private String status; // "Filled" or "Vacant"

    /**
     * Create AppointeeDTO from entity objects.
     *
     * @param individual The individual holding the position
     * @param position The government position
     * @param holding The position holding record (may be null for vacant positions)
     * @param org The organization (may be null)
     * @return AppointeeDTO
     */
    public static AppointeeDTO from(Individual individual, GovernmentPosition position,
                                    PositionHolding holding, GovernmentOrganization org) {
        AppointeeDTOBuilder builder = AppointeeDTO.builder();

        // Individual fields
        if (individual != null) {
            builder.id(individual.getId())
                   .firstName(individual.getFirstName())
                   .lastName(individual.getLastName())
                   .fullName(individual.getFullName());
        }

        // Position fields
        if (position != null) {
            builder.positionId(position.getId())
                   .positionTitle(position.getTitle())
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

        // Organization fields
        if (org != null) {
            builder.agencyName(org.getOfficialName())
                   .organizationName(org.getOfficialName());
        }

        // Holding fields
        if (holding != null) {
            builder.startDate(holding.getStartDate())
                   .endDate(holding.getEndDate())
                   .tenure(holding.getTenure())
                   .current(holding.isCurrent())
                   .status("Filled");
        } else {
            builder.current(false)
                   .status("Vacant");
        }

        return builder.build();
    }

    /**
     * Create AppointeeDTO for a vacant position.
     */
    public static AppointeeDTO forVacantPosition(GovernmentPosition position, GovernmentOrganization org) {
        return from(null, position, null, org);
    }
}
