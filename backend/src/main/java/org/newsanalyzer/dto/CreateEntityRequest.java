package org.newsanalyzer.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.newsanalyzer.model.EntityType;

import java.util.HashMap;
import java.util.Map;

/**
 * Request DTO for creating a new entity.
 *
 * Used for POST /api/entities
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateEntityRequest {

    @NotNull(message = "Entity type is required")
    private EntityType entityType;

    @NotBlank(message = "Name is required")
    private String name;

    /**
     * Optional flexible properties
     */
    private Map<String, Object> properties = new HashMap<>();

    /**
     * Optional Schema.org type (will be auto-generated if not provided)
     */
    private String schemaOrgType;

    /**
     * Optional Schema.org data (will be auto-generated if not provided)
     */
    private Map<String, Object> schemaOrgData = new HashMap<>();

    /**
     * Optional source identifier
     */
    private String source;

    /**
     * Optional confidence score (defaults to 1.0)
     */
    private Float confidenceScore;

    /**
     * Optional verification status (defaults to false)
     */
    private Boolean verified;
}
