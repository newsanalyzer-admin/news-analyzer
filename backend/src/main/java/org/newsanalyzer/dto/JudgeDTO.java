package org.newsanalyzer.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;
import org.newsanalyzer.model.*;

import java.time.LocalDate;
import java.util.UUID;

/**
 * DTO for federal judge information.
 *
 * Combines data from Person, GovernmentPosition, PositionHolding, and GovernmentOrganization
 * to provide a complete view of a federal judge.
 *
 * @author James (Dev Agent)
 * @since 2.0.0
 */
@Data
@Builder
public class JudgeDTO {

    private UUID id;
    private String fjcNid; // FJC Node ID (unique identifier from FJC)
    private String firstName;
    private String middleName;
    private String lastName;
    private String suffix;
    private String fullName;
    private String gender;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate deathDate;

    // Court Information
    private String courtName;
    private String courtType; // Supreme Court, U.S. Court of Appeals, U.S. District Court
    private String circuit;
    private UUID courtOrganizationId;

    // Appointment Information
    private String appointingPresident;
    private String partyOfAppointingPresident;
    private String abaRating;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate nominationDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate confirmationDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate commissionDate;

    private Integer ayesCount;
    private Integer naysCount;

    // Service Information
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate seniorStatusDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate terminationDate;

    private String terminationReason;
    private String judicialStatus; // ACTIVE, SENIOR, DECEASED, RESIGNED, etc.

    // Professional Background
    private String professionalCareer;

    private boolean current;

    /**
     * Create JudgeDTO from entity objects.
     *
     * @param person The person (judge)
     * @param position The government position (judgeship)
     * @param holding The position holding record
     * @param court The court organization
     * @return JudgeDTO
     */
    public static JudgeDTO from(Person person, GovernmentPosition position,
                                PositionHolding holding, GovernmentOrganization court) {
        JudgeDTOBuilder builder = JudgeDTO.builder();

        // Person fields
        if (person != null) {
            builder.id(person.getId())
                   .firstName(person.getFirstName())
                   .middleName(person.getMiddleName())
                   .lastName(person.getLastName())
                   .suffix(person.getSuffix())
                   .fullName(person.getFullName())
                   .gender(person.getGender())
                   .birthDate(person.getBirthDate());
        }

        // Position/Court fields
        if (position != null) {
            builder.courtOrganizationId(position.getOrganizationId());
        }

        // Court organization
        if (court != null) {
            builder.courtName(court.getOfficialName());
        }

        // Holding fields
        if (holding != null) {
            builder.commissionDate(holding.getStartDate())
                   .terminationDate(holding.getEndDate())
                   .current(holding.isCurrent());
        } else {
            builder.current(false);
        }

        return builder.build();
    }

    /**
     * Determine judicial status based on available data.
     */
    public static String determineStatus(LocalDate seniorStatusDate, LocalDate terminationDate,
                                         String terminationReason, LocalDate deathDate) {
        if (deathDate != null) {
            return "DECEASED";
        }
        if (terminationDate != null) {
            if (terminationReason != null) {
                return terminationReason.toUpperCase();
            }
            return "TERMINATED";
        }
        if (seniorStatusDate != null && seniorStatusDate.isBefore(LocalDate.now())) {
            return "SENIOR";
        }
        return "ACTIVE";
    }
}
