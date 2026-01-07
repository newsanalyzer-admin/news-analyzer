package org.newsanalyzer.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.newsanalyzer.model.Person;
import org.newsanalyzer.model.Presidency;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Data Transfer Object for Presidency API responses.
 * Includes president details and computed fields.
 *
 * @author James (Dev Agent)
 * @since 2.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PresidencyDTO {

    private UUID id;
    private Integer number;
    private String ordinalLabel;           // "1st", "22nd", "47th"

    // President details
    private UUID personId;
    private String presidentFullName;
    private String presidentFirstName;
    private String presidentLastName;
    private String imageUrl;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate birthDate;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate deathDate;

    private String birthPlace;
    private boolean isLiving;

    // Term information
    private String party;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    private String termLabel;              // "1789-1797" or "2021-present"
    private Long termDays;                 // computed duration in days
    private Integer electionYear;
    private String endReason;
    private boolean isCurrent;

    // Related data counts
    private Integer executiveOrderCount;
    private List<VicePresidentDTO> vicePresidents;

    // Predecessor/successor info
    private UUID predecessorId;
    private UUID successorId;

    // =====================================================================
    // Nested DTOs
    // =====================================================================

    /**
     * DTO for Vice President information.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VicePresidentDTO {
        private UUID personId;
        private String fullName;
        private String firstName;
        private String lastName;

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        private LocalDate startDate;

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        private LocalDate endDate;

        private String termLabel;
    }

    // =====================================================================
    // Factory Methods
    // =====================================================================

    /**
     * Create PresidencyDTO from Presidency entity and Person.
     *
     * @param presidency the presidency entity
     * @param person the president's person record
     * @return the DTO
     */
    public static PresidencyDTO from(Presidency presidency, Person person) {
        return from(presidency, person, null, null);
    }

    /**
     * Create PresidencyDTO from Presidency entity, Person, EO count, and VP list.
     *
     * @param presidency the presidency entity
     * @param person the president's person record
     * @param eoCount executive order count (can be null)
     * @param vicePresidents list of VPs (can be null)
     * @return the DTO
     */
    public static PresidencyDTO from(Presidency presidency, Person person,
                                      Integer eoCount, List<VicePresidentDTO> vicePresidents) {
        if (presidency == null) return null;

        String fullName = buildFullName(person);
        boolean living = person != null && person.getDeathDate() == null;

        return PresidencyDTO.builder()
                .id(presidency.getId())
                .number(presidency.getNumber())
                .ordinalLabel(presidency.getOrdinalLabel())
                .personId(presidency.getPersonId())
                .presidentFullName(fullName)
                .presidentFirstName(person != null ? person.getFirstName() : null)
                .presidentLastName(person != null ? person.getLastName() : null)
                .imageUrl(person != null ? person.getImageUrl() : null)
                .birthDate(person != null ? person.getBirthDate() : null)
                .deathDate(person != null ? person.getDeathDate() : null)
                .birthPlace(person != null ? person.getBirthPlace() : null)
                .isLiving(living)
                .party(presidency.getParty())
                .startDate(presidency.getStartDate())
                .endDate(presidency.getEndDate())
                .termLabel(presidency.getTermLabel())
                .termDays(presidency.getTermDays())
                .electionYear(presidency.getElectionYear())
                .endReason(presidency.getEndReason() != null ? presidency.getEndReason().name() : null)
                .isCurrent(presidency.isCurrent())
                .executiveOrderCount(eoCount)
                .vicePresidents(vicePresidents)
                .predecessorId(presidency.getPredecessorId())
                .successorId(presidency.getSuccessorId())
                .build();
    }

    /**
     * Build full name from Person entity.
     */
    private static String buildFullName(Person person) {
        if (person == null) return null;

        StringBuilder sb = new StringBuilder();
        sb.append(person.getFirstName());

        if (person.getMiddleName() != null && !person.getMiddleName().isEmpty()) {
            sb.append(" ").append(person.getMiddleName());
        }

        sb.append(" ").append(person.getLastName());

        if (person.getSuffix() != null && !person.getSuffix().isEmpty()) {
            sb.append(" ").append(person.getSuffix());
        }

        return sb.toString();
    }
}
