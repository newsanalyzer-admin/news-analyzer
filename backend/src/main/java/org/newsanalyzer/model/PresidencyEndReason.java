package org.newsanalyzer.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Enum representing how a presidency ended.
 *
 * @author James (Dev Agent)
 * @since 2.0.0
 */
public enum PresidencyEndReason {
    TERM_END("term_end"),
    DEATH("death"),
    RESIGNATION("resignation"),
    SUCCESSION("succession");

    private final String value;

    PresidencyEndReason(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static PresidencyEndReason fromValue(String value) {
        for (PresidencyEndReason reason : values()) {
            if (reason.value.equalsIgnoreCase(value)) {
                return reason;
            }
        }
        throw new IllegalArgumentException("Unknown presidency end reason: " + value);
    }
}
