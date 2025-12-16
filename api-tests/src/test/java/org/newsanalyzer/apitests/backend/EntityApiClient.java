package org.newsanalyzer.apitests.backend;

import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.newsanalyzer.apitests.config.Endpoints;

import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;

/**
 * API client helper for Entity operations.
 * Provides convenient methods for common Entity API calls.
 */
public class EntityApiClient {

    private final RequestSpecification spec;

    public EntityApiClient(RequestSpecification spec) {
        this.spec = spec;
    }

    // ==================== CREATE Operations ====================

    /**
     * Create a new entity.
     */
    public Response createEntity(Map<String, Object> entityRequest) {
        return given()
                .spec(spec)
                .contentType(JSON)
                .body(entityRequest)
                .when()
                .post(Endpoints.Backend.ENTITIES);
    }

    /**
     * Create and validate a new entity (with gov org linking).
     */
    public Response createAndValidateEntity(Map<String, Object> entityRequest) {
        return given()
                .spec(spec)
                .contentType(JSON)
                .body(entityRequest)
                .when()
                .post(Endpoints.Backend.ENTITIES_VALIDATE);
    }

    // ==================== READ Operations ====================

    /**
     * Get all entities.
     */
    public Response getAllEntities() {
        return given()
                .spec(spec)
                .when()
                .get(Endpoints.Backend.ENTITIES);
    }

    /**
     * Get entity by ID.
     */
    public Response getEntityById(UUID id) {
        return given()
                .spec(spec)
                .pathParam("id", id.toString())
                .when()
                .get(Endpoints.Backend.ENTITY_BY_ID);
    }

    /**
     * Get entity by ID (string version).
     */
    public Response getEntityById(String id) {
        return given()
                .spec(spec)
                .pathParam("id", id)
                .when()
                .get(Endpoints.Backend.ENTITY_BY_ID);
    }

    /**
     * Get entities by type.
     */
    public Response getEntitiesByType(String type) {
        return given()
                .spec(spec)
                .pathParam("type", type)
                .when()
                .get(Endpoints.Backend.ENTITIES_BY_TYPE);
    }

    /**
     * Get entities by Schema.org type.
     */
    public Response getEntitiesBySchemaOrgType(String schemaOrgType) {
        return given()
                .spec(spec)
                .pathParam("schemaOrgType", schemaOrgType)
                .when()
                .get(Endpoints.Backend.ENTITIES_BY_SCHEMA_ORG_TYPE);
    }

    /**
     * Search entities by name.
     */
    public Response searchEntities(String query) {
        return given()
                .spec(spec)
                .queryParam("q", query)
                .when()
                .get(Endpoints.Backend.ENTITIES_SEARCH);
    }

    /**
     * Full-text search entities.
     */
    public Response fullTextSearch(String query, int limit) {
        return given()
                .spec(spec)
                .queryParam("q", query)
                .queryParam("limit", limit)
                .when()
                .get(Endpoints.Backend.ENTITIES_SEARCH_FULLTEXT);
    }

    /**
     * Full-text search with default limit.
     */
    public Response fullTextSearch(String query) {
        return fullTextSearch(query, 20);
    }

    /**
     * Get recent entities.
     */
    public Response getRecentEntities(int days) {
        return given()
                .spec(spec)
                .queryParam("days", days)
                .when()
                .get(Endpoints.Backend.ENTITIES_RECENT);
    }

    /**
     * Get recent entities with default 7 days.
     */
    public Response getRecentEntities() {
        return getRecentEntities(7);
    }

    // ==================== UPDATE Operations ====================

    /**
     * Update an entity.
     */
    public Response updateEntity(UUID id, Map<String, Object> entityRequest) {
        return given()
                .spec(spec)
                .contentType(JSON)
                .pathParam("id", id.toString())
                .body(entityRequest)
                .when()
                .put(Endpoints.Backend.ENTITY_BY_ID);
    }

    /**
     * Update an entity (string ID version).
     */
    public Response updateEntity(String id, Map<String, Object> entityRequest) {
        return given()
                .spec(spec)
                .contentType(JSON)
                .pathParam("id", id)
                .body(entityRequest)
                .when()
                .put(Endpoints.Backend.ENTITY_BY_ID);
    }

    // ==================== DELETE Operations ====================

    /**
     * Delete an entity.
     */
    public Response deleteEntity(UUID id) {
        return given()
                .spec(spec)
                .pathParam("id", id.toString())
                .when()
                .delete(Endpoints.Backend.ENTITY_BY_ID);
    }

    /**
     * Delete an entity (string ID version).
     */
    public Response deleteEntity(String id) {
        return given()
                .spec(spec)
                .pathParam("id", id)
                .when()
                .delete(Endpoints.Backend.ENTITY_BY_ID);
    }

    // ==================== VALIDATION Operations ====================

    /**
     * Validate an existing entity.
     */
    public Response validateEntity(UUID id) {
        return given()
                .spec(spec)
                .pathParam("id", id.toString())
                .when()
                .post(Endpoints.Backend.ENTITY_VALIDATE);
    }

    /**
     * Verify an entity.
     */
    public Response verifyEntity(UUID id) {
        return given()
                .spec(spec)
                .pathParam("id", id.toString())
                .when()
                .post(Endpoints.Backend.ENTITY_VERIFY);
    }

    // ==================== Utility Methods ====================

    /**
     * Create an entity and return its ID from the response.
     */
    public String createEntityAndGetId(Map<String, Object> entityRequest) {
        Response response = createEntity(entityRequest);
        return response.jsonPath().getString("id");
    }

    /**
     * Create an entity, extract and return the full response object.
     */
    public Response createEntityExpecting(Map<String, Object> entityRequest, int expectedStatus) {
        return createEntity(entityRequest)
                .then()
                .statusCode(expectedStatus)
                .extract()
                .response();
    }
}
