package org.newsanalyzer.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * DTO representing an agency from the Federal Register API.
 *
 * Maps to the JSON response from: https://www.federalregister.gov/api/v1/agencies
 *
 * @author James (Dev Agent)
 * @since 2.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class FederalRegisterAgency {

    /**
     * Federal Register internal ID for the agency.
     */
    private Integer id;

    /**
     * Official name of the agency (e.g., "Department of Agriculture").
     */
    private String name;

    /**
     * Short name/acronym (e.g., "USDA").
     */
    @JsonProperty("short_name")
    private String shortName;

    /**
     * URL to the agency's page on Federal Register.
     */
    private String url;

    /**
     * Parent agency's Federal Register ID (null if top-level agency).
     */
    @JsonProperty("parent_id")
    private Integer parentId;

    /**
     * Description of the agency's mission/purpose.
     */
    private String description;

    /**
     * Slug identifier used in URLs.
     */
    private String slug;

    /**
     * Agency logo data. May be null, or a nested object with thumb_url, small_url, medium_url fields.
     * Typed as Object to handle both null and nested JSON object responses from the API.
     */
    private Object logo;
}
