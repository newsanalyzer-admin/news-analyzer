package org.newsanalyzer.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Represents the three branches of the US federal government.
 * Used to distinguish between Congressional, Executive, and Judicial positions.
 *
 * @author James (Dev Agent)
 * @since 2.0.0
 */
public enum Branch {
    LEGISLATIVE("legislative", "Legislative Branch - Congress"),
    EXECUTIVE("executive", "Executive Branch - President and Agencies"),
    JUDICIAL("judicial", "Judicial Branch - Federal Courts");

    private final String value;
    private final String description;

    Branch(String value, String description) {
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
    public static Branch fromValue(String value) {
        for (Branch branch : values()) {
            if (branch.value.equalsIgnoreCase(value)) {
                return branch;
            }
        }
        throw new IllegalArgumentException("Unknown branch: " + value);
    }
}
