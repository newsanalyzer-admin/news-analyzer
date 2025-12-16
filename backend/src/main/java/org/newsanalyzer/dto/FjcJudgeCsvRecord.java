package org.newsanalyzer.dto;

import com.opencsv.bean.CsvBindByName;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for parsing FJC Biographical Directory CSV records.
 *
 * Maps to the flat file format from:
 * https://www.fjc.gov/sites/default/files/history/judges.csv
 *
 * Note: The FJC CSV has 288 columns. This DTO captures the essential fields.
 * Columns support up to 6 judicial appointments per judge; we capture the first (primary).
 *
 * @author James (Dev Agent)
 * @since 2.0.0
 * @see <a href="https://www.fjc.gov/history/judges/biographical-directory-article-iii-federal-judges-export">FJC Export</a>
 */
@Data
@NoArgsConstructor
public class FjcJudgeCsvRecord {

    // =====================================================================
    // Identification
    // =====================================================================

    @CsvBindByName(column = "nid")
    private String nid; // FJC Node ID - unique identifier

    @CsvBindByName(column = "jid")
    private String jid; // Judge ID

    // =====================================================================
    // Personal Information
    // =====================================================================

    @CsvBindByName(column = "Last Name")
    private String lastName;

    @CsvBindByName(column = "First Name")
    private String firstName;

    @CsvBindByName(column = "Middle Name")
    private String middleName;

    @CsvBindByName(column = "Suffix")
    private String suffix;

    @CsvBindByName(column = "Birth Month")
    private String birthMonth;

    @CsvBindByName(column = "Birth Day")
    private String birthDay;

    @CsvBindByName(column = "Birth Year")
    private String birthYear;

    @CsvBindByName(column = "Birth City")
    private String birthCity;

    @CsvBindByName(column = "Birth State")
    private String birthState;

    @CsvBindByName(column = "Death Month")
    private String deathMonth;

    @CsvBindByName(column = "Death Day")
    private String deathDay;

    @CsvBindByName(column = "Death Year")
    private String deathYear;

    @CsvBindByName(column = "Gender")
    private String gender;

    @CsvBindByName(column = "Race or Ethnicity")
    private String raceOrEthnicity;

    // =====================================================================
    // Primary Appointment (Position 1 of up to 6)
    // =====================================================================

    @CsvBindByName(column = "Court Type (1)")
    private String courtType1;

    @CsvBindByName(column = "Court Name (1)")
    private String courtName1;

    @CsvBindByName(column = "Appointment Title (1)")
    private String appointmentTitle1;

    @CsvBindByName(column = "Appointing President (1)")
    private String appointingPresident1;

    @CsvBindByName(column = "Party of Appointing President (1)")
    private String partyOfAppointingPresident1;

    @CsvBindByName(column = "ABA Rating (1)")
    private String abaRating1;

    @CsvBindByName(column = "Nomination Date (1)")
    private String nominationDate1;

    @CsvBindByName(column = "Confirmation Date (1)")
    private String confirmationDate1;

    @CsvBindByName(column = "Commission Date (1)")
    private String commissionDate1;

    @CsvBindByName(column = "Ayes/Nays (1)")
    private String ayesNays1;

    @CsvBindByName(column = "Senior Status Date (1)")
    private String seniorStatusDate1;

    @CsvBindByName(column = "Termination (1)")
    private String termination1;

    @CsvBindByName(column = "Termination Date (1)")
    private String terminationDate1;

    // =====================================================================
    // Professional Background
    // =====================================================================

    @CsvBindByName(column = "Professional Career")
    private String professionalCareer;

    // =====================================================================
    // Helper Methods
    // =====================================================================

    /**
     * Get full name
     */
    public String getFullName() {
        StringBuilder sb = new StringBuilder();
        if (firstName != null && !firstName.isBlank()) {
            sb.append(firstName.trim());
        }
        if (middleName != null && !middleName.isBlank()) {
            sb.append(" ").append(middleName.trim());
        }
        if (lastName != null && !lastName.isBlank()) {
            sb.append(" ").append(lastName.trim());
        }
        if (suffix != null && !suffix.isBlank()) {
            sb.append(" ").append(suffix.trim());
        }
        return sb.toString().trim();
    }

    /**
     * Get birth date as string (MM/DD/YYYY format from FJC)
     */
    public String getBirthDateString() {
        if (birthMonth == null || birthDay == null || birthYear == null) {
            return null;
        }
        if (birthMonth.isBlank() || birthYear.isBlank()) {
            return null;
        }
        return birthMonth + "/" + birthDay + "/" + birthYear;
    }

    /**
     * Get death date as string (MM/DD/YYYY format from FJC)
     */
    public String getDeathDateString() {
        if (deathMonth == null || deathDay == null || deathYear == null) {
            return null;
        }
        if (deathMonth.isBlank() || deathYear.isBlank()) {
            return null;
        }
        return deathMonth + "/" + deathDay + "/" + deathYear;
    }

    /**
     * Parse ayes count from "Ayes/Nays" field (format: "XX-YY" or "Voice Vote")
     */
    public Integer getAyesCount() {
        return parseVoteCount(ayesNays1, 0);
    }

    /**
     * Parse nays count from "Ayes/Nays" field (format: "XX-YY" or "Voice Vote")
     */
    public Integer getNaysCount() {
        return parseVoteCount(ayesNays1, 1);
    }

    private Integer parseVoteCount(String ayesNays, int index) {
        if (ayesNays == null || ayesNays.isBlank()) {
            return null;
        }
        String[] parts = ayesNays.split("-");
        if (parts.length != 2) {
            return null;
        }
        try {
            return Integer.parseInt(parts[index].trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Determine current judicial status
     */
    public String getJudicialStatus() {
        if (deathYear != null && !deathYear.isBlank()) {
            return "DECEASED";
        }
        if (termination1 != null && !termination1.isBlank()) {
            String term = termination1.trim().toUpperCase();
            if (term.contains("RESIGN")) return "RESIGNED";
            if (term.contains("RETIRE")) return "RETIRED";
            if (term.contains("DEATH") || term.contains("DIED")) return "DECEASED";
            if (term.contains("IMPEACH")) return "REMOVED";
            return "TERMINATED";
        }
        if (seniorStatusDate1 != null && !seniorStatusDate1.isBlank()) {
            return "SENIOR";
        }
        return "ACTIVE";
    }

    /**
     * Check if this is an Article III judge
     */
    public boolean isArticleIIIJudge() {
        return courtType1 != null && (
            courtType1.contains("Supreme") ||
            courtType1.contains("Court of Appeals") ||
            courtType1.contains("District Court") ||
            courtType1.contains("Court of International Trade") ||
            courtType1.contains("Court of Federal Claims")
        );
    }

    /**
     * Check if judge is currently active or senior
     */
    public boolean isCurrentlyServing() {
        String status = getJudicialStatus();
        return "ACTIVE".equals(status) || "SENIOR".equals(status);
    }
}
