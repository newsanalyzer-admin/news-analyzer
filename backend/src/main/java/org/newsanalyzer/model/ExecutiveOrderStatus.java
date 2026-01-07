package org.newsanalyzer.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Enum representing the status of an Executive Order.
 *
 * @author James (Dev Agent)
 * @since 2.0.0
 */
public enum ExecutiveOrderStatus {
    ACTIVE("active"),
    REVOKED("revoked"),
    SUPERSEDED("superseded");

    private final String value;

    ExecutiveOrderStatus(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static ExecutiveOrderStatus fromValue(String value) {
        for (ExecutiveOrderStatus status : values()) {
            if (status.value.equalsIgnoreCase(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown executive order status: " + value);
    }
}
