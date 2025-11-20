package org.newsanalyzer.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.newsanalyzer.model.EntityType;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Data Transfer Object for Entity API requests and responses.
 *
 * This DTO is used for:
 * - API responses (GET /api/entities)
 * - API requests (POST /api/entities, PUT /api/entities/{id})
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EntityDTO {

    private UUID id;

    /**
     * Internal entity type (PERSON, GOVERNMENT_ORG, etc.)
     */
    private EntityType entityType;

    /**
     * Entity name
     */
    private String name;

    /**
     * Flexible properties (JSONB)
     */
    private Map<String, Object> properties = new HashMap<>();

    /**
     * Schema.org type (e.g., "Person", "GovernmentOrganization")
     */
    private String schemaOrgType;

    /**
     * Full Schema.org JSON-LD representation
     */
    private Map<String, Object> schemaOrgData = new HashMap<>();

    /**
     * Source identifier
     */
    private String source;

    /**
     * Confidence score (0.0 to 1.0)
     */
    private Float confidenceScore;

    /**
     * Verification status
     */
    private Boolean verified;

    /**
     * Creation timestamp
     */
    private LocalDateTime createdAt;

    /**
     * Last update timestamp
     */
    private LocalDateTime updatedAt;
}
