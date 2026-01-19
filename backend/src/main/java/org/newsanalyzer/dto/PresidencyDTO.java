package org.newsanalyzer.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.newsanalyzer.model.Individual;
import org.newsanalyzer.model.Presidency;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Data Transfer Object for Presidency API responses.
 * Includes president details and computed fields.
 *
 * Part of ARCH-1.6: Updated to use Individual instead of Person.
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
    private UUID individualId;
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
        private UUID individualId;
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
     * Create PresidencyDTO from Presidency entity and Individual.
     *
     * @param presidency the presidency entity
     * @param individual the president's individual record
     * @return the DTO
     */
    public static PresidencyDTO from(Presidency presidency, Individual individual) {
        return from(presidency, individual, null, null);
    }

    /**
     * Create PresidencyDTO from Presidency entity, Individual, EO count, and VP list.
     *
     * @param presidency the presidency entity
     * @param individual the president's individual record
     * @param eoCount executive order count (can be null)
     * @param vicePresidents list of VPs (can be null)
     * @return the DTO
     */
    public static PresidencyDTO from(Presidency presidency, Individual individual,
                                      Integer eoCount, List<VicePresidentDTO> vicePresidents) {
        if (presidency == null) return null;

        String fullName = buildFullName(individual);
        boolean living = individual != null && individual.getDeathDate() == null;

        return PresidencyDTO.builder()
                .id(presidency.getId())
                .number(presidency.getNumber())
                .ordinalLabel(presidency.getOrdinalLabel())
                .individualId(presidency.getIndividualId())
                .presidentFullName(fullName)
                .presidentFirstName(individual != null ? individual.getFirstName() : null)
                .presidentLastName(individual != null ? individual.getLastName() : null)
                .imageUrl(individual != null ? individual.getImageUrl() : null)
                .birthDate(individual != null ? individual.getBirthDate() : null)
                .deathDate(individual != null ? individual.getDeathDate() : null)
                .birthPlace(individual != null ? individual.getBirthPlace() : null)
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
     * Build full name from Individual entity.
     */
    private static String buildFullName(Individual individual) {
        if (individual == null) return null;

        StringBuilder sb = new StringBuilder();
        sb.append(individual.getFirstName());

        if (individual.getMiddleName() != null && !individual.getMiddleName().isEmpty()) {
            sb.append(" ").append(individual.getMiddleName());
        }

        sb.append(" ").append(individual.getLastName());

        if (individual.getSuffix() != null && !individual.getSuffix().isEmpty()) {
            sb.append(" ").append(individual.getSuffix());
        }

        return sb.toString();
    }
}
