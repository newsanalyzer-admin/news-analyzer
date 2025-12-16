package org.newsanalyzer.dto;

import com.opencsv.bean.CsvBindByName;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for parsing OPM PLUM CSV records.
 *
 * Maps to the 14-column format from:
 * https://www.opm.gov/about-us/open-government/plum-reporting/plum-archive/
 *
 * @author James (Dev Agent)
 * @since 2.0.0
 */
@Data
@NoArgsConstructor
public class PlumCsvRecord {

    @CsvBindByName(column = "AgencyName")
    private String agencyName;

    @CsvBindByName(column = "OrganizationName")
    private String organizationName;

    @CsvBindByName(column = "PositionTitle")
    private String positionTitle;

    @CsvBindByName(column = "PositionStatus")
    private String positionStatus; // "Filled" or "Vacant"

    @CsvBindByName(column = "AppointmentTypeDescription")
    private String appointmentTypeDescription; // PAS, PA, NA, CA, XS descriptions

    @CsvBindByName(column = "ExpirationDate")
    private String expirationDate; // M/d/yyyy H:mm format

    @CsvBindByName(column = "LevelGradePay")
    private String levelGradePay;

    @CsvBindByName(column = "Location")
    private String location;

    @CsvBindByName(column = "IncumbentFirstName")
    private String incumbentFirstName;

    @CsvBindByName(column = "IncumbentLastName")
    private String incumbentLastName;

    @CsvBindByName(column = "PaymentPlanDescription")
    private String paymentPlanDescription; // Pay plan (EX, ES, GS, etc.)

    @CsvBindByName(column = "Tenure")
    private String tenure;

    @CsvBindByName(column = "IncumbentBeginDate")
    private String incumbentBeginDate; // M/d/yyyy H:mm format

    @CsvBindByName(column = "IncumbentVacateDate")
    private String incumbentVacateDate; // M/d/yyyy H:mm format

    /**
     * Check if position is currently filled
     */
    public boolean isFilled() {
        return "Filled".equalsIgnoreCase(positionStatus);
    }

    /**
     * Check if position is vacant
     */
    public boolean isVacant() {
        return "Vacant".equalsIgnoreCase(positionStatus);
    }

    /**
     * Check if incumbent information is available
     */
    public boolean hasIncumbent() {
        return incumbentFirstName != null && !incumbentFirstName.isBlank()
                && incumbentLastName != null && !incumbentLastName.isBlank();
    }

    /**
     * Get display name for incumbent
     */
    public String getIncumbentDisplayName() {
        if (!hasIncumbent()) {
            return null;
        }
        return incumbentFirstName.trim() + " " + incumbentLastName.trim();
    }

    /**
     * Extract pay plan code from PaymentPlanDescription
     * Typically the first word/code (e.g., "EX" from "EX - Executive Schedule")
     */
    public String getPayPlanCode() {
        if (paymentPlanDescription == null || paymentPlanDescription.isBlank()) {
            return null;
        }
        String desc = paymentPlanDescription.trim();
        int dashIndex = desc.indexOf('-');
        if (dashIndex > 0) {
            return desc.substring(0, dashIndex).trim();
        }
        int spaceIndex = desc.indexOf(' ');
        if (spaceIndex > 0) {
            return desc.substring(0, spaceIndex).trim();
        }
        return desc;
    }

    /**
     * Extract pay grade from LevelGradePay
     */
    public String getPayGrade() {
        if (levelGradePay == null || levelGradePay.isBlank()) {
            return null;
        }
        return levelGradePay.trim();
    }

    /**
     * Parse tenure as integer
     */
    public Integer getTenureCode() {
        if (tenure == null || tenure.isBlank()) {
            return null;
        }
        try {
            return Integer.parseInt(tenure.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
