package org.newsanalyzer.apitests.backend;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.newsanalyzer.apitests.util.DatabaseConnectionManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Builder for creating Entity test data.
 * Uses builder pattern to construct entity request payloads for testing.
 * Supports both API payload generation and direct database persistence.
 */
public class EntityTestDataBuilder {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    // Test data constants
    public static final UUID TEST_ENTITY_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    public static final UUID NON_EXISTENT_ID = UUID.fromString("99999999-9999-9999-9999-999999999999");

    // Entity types matching backend EntityType enum
    public static final String TYPE_PERSON = "PERSON";
    public static final String TYPE_GOVERNMENT_ORG = "GOVERNMENT_ORG";
    public static final String TYPE_ORGANIZATION = "ORGANIZATION";
    public static final String TYPE_LOCATION = "LOCATION";
    public static final String TYPE_EVENT = "EVENT";
    public static final String TYPE_CONCEPT = "CONCEPT";

    // Schema.org types
    public static final String SCHEMA_PERSON = "Person";
    public static final String SCHEMA_GOV_ORG = "GovernmentOrganization";
    public static final String SCHEMA_ORGANIZATION = "Organization";
    public static final String SCHEMA_PLACE = "Place";

    private UUID id;
    private String name;
    private String entityType;
    private String schemaOrgType;
    private String description;
    private String source;
    private Double confidenceScore;
    private Map<String, Object> properties;
    private Boolean verified;
    private UUID governmentOrgId;

    public EntityTestDataBuilder() {
        // Default values
        this.name = "Test Entity";
        this.entityType = TYPE_ORGANIZATION;
        this.schemaOrgType = SCHEMA_ORGANIZATION;
        this.properties = new HashMap<>();
    }

    public static EntityTestDataBuilder anEntity() {
        return new EntityTestDataBuilder();
    }

    public static EntityTestDataBuilder aPerson() {
        return new EntityTestDataBuilder()
                .withName("John Doe")
                .withEntityType(TYPE_PERSON)
                .withSchemaOrgType(SCHEMA_PERSON);
    }

    public static EntityTestDataBuilder aGovernmentOrganization() {
        return new EntityTestDataBuilder()
                .withName("Environmental Protection Agency")
                .withEntityType(TYPE_GOVERNMENT_ORG)
                .withSchemaOrgType(SCHEMA_GOV_ORG);
    }

    public static EntityTestDataBuilder anOrganization() {
        return new EntityTestDataBuilder()
                .withName("Acme Corporation")
                .withEntityType(TYPE_ORGANIZATION)
                .withSchemaOrgType(SCHEMA_ORGANIZATION);
    }

    public static EntityTestDataBuilder aLocation() {
        return new EntityTestDataBuilder()
                .withName("Washington D.C.")
                .withEntityType(TYPE_LOCATION)
                .withSchemaOrgType(SCHEMA_PLACE);
    }

    public EntityTestDataBuilder withName(String name) {
        this.name = name;
        return this;
    }

    public EntityTestDataBuilder withEntityType(String entityType) {
        this.entityType = entityType;
        return this;
    }

    public EntityTestDataBuilder withSchemaOrgType(String schemaOrgType) {
        this.schemaOrgType = schemaOrgType;
        return this;
    }

    public EntityTestDataBuilder withDescription(String description) {
        this.description = description;
        return this;
    }

    public EntityTestDataBuilder withSource(String source) {
        this.source = source;
        return this;
    }

    public EntityTestDataBuilder withConfidenceScore(Double confidenceScore) {
        this.confidenceScore = confidenceScore;
        return this;
    }

    public EntityTestDataBuilder withProperty(String key, Object value) {
        this.properties.put(key, value);
        return this;
    }

    public EntityTestDataBuilder withProperties(Map<String, Object> properties) {
        this.properties.putAll(properties);
        return this;
    }

    public EntityTestDataBuilder withId(UUID id) {
        this.id = id;
        return this;
    }

    public EntityTestDataBuilder withVerified(Boolean verified) {
        this.verified = verified;
        return this;
    }

    public EntityTestDataBuilder withGovernmentOrgId(UUID governmentOrgId) {
        this.governmentOrgId = governmentOrgId;
        return this;
    }

    /**
     * Builds the entity request as a Map suitable for JSON serialization.
     */
    public Map<String, Object> build() {
        Map<String, Object> entity = new HashMap<>();
        entity.put("name", name);
        entity.put("entityType", entityType);

        if (schemaOrgType != null) {
            entity.put("schemaOrgType", schemaOrgType);
        }
        if (description != null) {
            entity.put("description", description);
        }
        if (source != null) {
            entity.put("source", source);
        }
        if (confidenceScore != null) {
            entity.put("confidenceScore", confidenceScore);
        }
        if (!properties.isEmpty()) {
            entity.put("properties", properties);
        }

        return entity;
    }

    /**
     * Builds an invalid entity request (missing required fields).
     */
    public static Map<String, Object> buildInvalidEntity() {
        Map<String, Object> entity = new HashMap<>();
        // Missing required 'name' field
        entity.put("entityType", TYPE_ORGANIZATION);
        return entity;
    }

    /**
     * Builds an entity with empty name (validation should fail).
     */
    public static Map<String, Object> buildEntityWithEmptyName() {
        Map<String, Object> entity = new HashMap<>();
        entity.put("name", "");
        entity.put("entityType", TYPE_ORGANIZATION);
        return entity;
    }

    /**
     * Builds an entity with null name (validation should fail).
     */
    public static Map<String, Object> buildEntityWithNullName() {
        Map<String, Object> entity = new HashMap<>();
        entity.put("name", null);
        entity.put("entityType", TYPE_ORGANIZATION);
        return entity;
    }

    // Sample entities for search testing
    public static Map<String, Object> buildEpaEntity() {
        return aGovernmentOrganization()
                .withName("Environmental Protection Agency")
                .withDescription("The EPA protects human health and the environment")
                .build();
    }

    public static Map<String, Object> buildNasaEntity() {
        return aGovernmentOrganization()
                .withName("National Aeronautics and Space Administration")
                .withDescription("NASA is responsible for the civilian space program")
                .build();
    }

    public static Map<String, Object> buildDojEntity() {
        return aGovernmentOrganization()
                .withName("Department of Justice")
                .withDescription("The DOJ enforces federal laws")
                .build();
    }

    // ==================== Database Persistence Methods ====================

    /**
     * Persist the entity directly to the database.
     * Returns the generated UUID.
     */
    public UUID persistToDatabase() throws SQLException {
        UUID entityId = this.id != null ? this.id : UUID.randomUUID();
        DatabaseConnectionManager connectionManager = DatabaseConnectionManager.getInstance();

        connectionManager.executeInTransaction(connection -> {
            persistToDatabase(connection, entityId);
        });

        return entityId;
    }

    /**
     * Persist the entity to the database using an existing connection.
     * Useful for transaction control.
     */
    public void persistToDatabase(Connection connection, UUID entityId) throws SQLException {
        String sql = "INSERT INTO entities (id, name, entity_type, schema_org_type, properties, verified, government_org_id, created_at, updated_at) " +
                     "VALUES (?, ?, ?, ?, ?::jsonb, ?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            Timestamp now = Timestamp.from(Instant.now());

            stmt.setObject(1, entityId);
            stmt.setString(2, name);
            stmt.setString(3, entityType);
            stmt.setString(4, schemaOrgType);

            // Convert properties map to JSON string
            String propertiesJson = properties.isEmpty() ? null : toJson(properties);
            stmt.setString(5, propertiesJson);

            stmt.setBoolean(6, verified != null ? verified : false);
            stmt.setObject(7, governmentOrgId);
            stmt.setTimestamp(8, now);
            stmt.setTimestamp(9, now);

            stmt.executeUpdate();
        }
    }

    /**
     * Build and persist in one step.
     * Returns the entity ID.
     */
    public UUID buildAndPersist() throws SQLException {
        return persistToDatabase();
    }

    /**
     * Build and persist with a specific ID.
     */
    public UUID buildAndPersist(UUID id) throws SQLException {
        this.id = id;
        return persistToDatabase();
    }

    /**
     * Convert map to JSON string.
     */
    private String toJson(Map<String, Object> map) {
        try {
            return objectMapper.writeValueAsString(map);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize properties to JSON", e);
        }
    }

    // ==================== Getters for Verification ====================

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEntityType() {
        return entityType;
    }

    public String getSchemaOrgType() {
        return schemaOrgType;
    }

    public Boolean getVerified() {
        return verified;
    }

    public UUID getGovernmentOrgId() {
        return governmentOrgId;
    }
}
