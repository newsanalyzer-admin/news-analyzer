package org.newsanalyzer.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Type of executive branch appointment from OPM PLUM data.
 *
 * @author James (Dev Agent)
 * @since 2.0.0
 */
public enum AppointmentType {
    PAS("PAS", "Presidential Appointment with Senate Confirmation"),
    PA("PA", "Presidential Appointment without Senate Confirmation"),
    NA("NA", "Noncareer Appointment"),
    CA("CA", "Career Appointment"),
    XS("XS", "Schedule C - Expected to change with administration");

    private final String value;
    private final String description;

    AppointmentType(String value, String description) {
        this.value = value;
        this.description = description;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    public String getDescription() {
        return description;
    }

    @JsonCreator
    public static AppointmentType fromValue(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        for (AppointmentType type : values()) {
            if (type.value.equalsIgnoreCase(value.trim())) {
                return type;
            }
        }
        return null; // Unknown appointment types return null rather than throwing
    }

    /**
     * Parse appointment type from PLUM CSV value.
     * Handles full descriptions like "Presidential Appointment, Senate Confirmed" as well as codes.
     */
    public static AppointmentType fromCsvValue(String csvValue) {
        if (csvValue == null || csvValue.isBlank()) {
            return null;
        }

        String normalized = csvValue.trim().toUpperCase();

        // Try direct code match first
        for (AppointmentType type : values()) {
            if (type.value.equals(normalized)) {
                return type;
            }
        }

        // Try matching by description keywords
        if (normalized.contains("SENATE") && normalized.contains("CONFIRM")) {
            return PAS;
        }
        if (normalized.contains("PRESIDENTIAL") && !normalized.contains("SENATE")) {
            return PA;
        }
        if (normalized.contains("NONCAREER") || normalized.contains("NON-CAREER")) {
            return NA;
        }
        if (normalized.contains("CAREER") && !normalized.contains("NON")) {
            return CA;
        }
        if (normalized.contains("SCHEDULE C") || normalized.contains("SCHEDULE-C")) {
            return XS;
        }

        return null;
    }
}
