package org.newsanalyzer.service.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/**
 * DTO classes for parsing presidential seed data JSON.
 *
 * @author James (Dev Agent)
 * @since 2.0.0
 */
public class PresidencySeedData {

    /**
     * Root container for seed data.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SeedFile {
        private List<PresidencyEntry> presidencies;
    }

    /**
     * Single presidency entry in the seed file.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PresidencyEntry {
        private Integer number;
        private PersonEntry president;
        private String party;
        private LocalDate startDate;
        private LocalDate endDate;
        private Integer electionYear;
        private String endReason;
        private List<VicePresidentEntry> vicePresidents;

        /**
         * Unique key for identifying the person (for non-consecutive terms).
         * Format: "firstName_lastName" in lowercase.
         */
        public String getPresidentKey() {
            if (president == null) return null;
            return (president.getFirstName() + "_" + president.getLastName()).toLowerCase();
        }
    }

    /**
     * Person entry (president) in the seed data.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PersonEntry {
        private String firstName;
        private String lastName;
        private String middleName;
        private String suffix;
        private LocalDate birthDate;
        private LocalDate deathDate;
        private String birthPlace;
        private String imageUrl;
    }

    /**
     * Vice President entry in the seed data.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class VicePresidentEntry {
        private String firstName;
        private String lastName;
        private String middleName;
        private LocalDate startDate;
        private LocalDate endDate;

        /**
         * Unique key for identifying the VP person.
         */
        public String getPersonKey() {
            return (firstName + "_" + lastName).toLowerCase();
        }
    }
}
