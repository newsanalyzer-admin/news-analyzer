package org.newsanalyzer.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Type of government position.
 *
 * @author James (Dev Agent)
 * @since 2.0.0
 */
public enum PositionType {
    ELECTED("elected"),
    APPOINTED("appointed"),
    CAREER("career");

    private final String value;

    PositionType(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static PositionType fromValue(String value) {
        for (PositionType type : values()) {
            if (type.value.equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown position type: " + value);
    }
}
