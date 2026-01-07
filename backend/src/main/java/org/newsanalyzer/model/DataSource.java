package org.newsanalyzer.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Data source for position and term information.
 *
 * @author James (Dev Agent)
 * @since 2.0.0
 */
public enum DataSource {
    CONGRESS_GOV("congress_gov"),
    GOVINFO("govinfo"),
    LEGISLATORS_REPO("legislators_repo"),
    PLUM_CSV("plum_csv"),
    FJC("fjc"),
    MANUAL("manual"),
    USA_GOV("usa_gov"),
    FEDERAL_REGISTER("federal_register"),
    WHITE_HOUSE_HISTORICAL("white_house_historical");

    private final String value;

    DataSource(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static DataSource fromValue(String value) {
        for (DataSource source : values()) {
            if (source.value.equalsIgnoreCase(value)) {
                return source;
            }
        }
        throw new IllegalArgumentException("Unknown data source: " + value);
    }
}
