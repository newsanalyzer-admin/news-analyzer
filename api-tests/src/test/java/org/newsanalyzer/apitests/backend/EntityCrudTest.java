package org.newsanalyzer.apitests.backend;

import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.newsanalyzer.apitests.BaseApiTest;

import java.util.Map;

import static org.hamcrest.Matchers.*;

/**
 * CRUD tests for the Entity API endpoints.
 * Tests create, read, update, and delete operations.
 */
@Tag("backend")
@Tag("integration")
@DisplayName("Entity CRUD Tests")
class EntityCrudTest extends BaseApiTest {

    private EntityApiClient entityClient;

    @BeforeEach
    void setUp() {
        entityClient = new EntityApiClient(getBackendSpec());
    }

    // ==================== CREATE Tests ====================

    @Test
    @DisplayName("POST /api/entities - should create entity when valid request returns 201")
    void shouldCreateEntity_whenValidRequest_returns201() {
        Map<String, Object> entityRequest = EntityTestDataBuilder.anEntity()
                .withName("Test Organization " + System.currentTimeMillis())
                .withEntityType(EntityTestDataBuilder.TYPE_ORGANIZATION)
                .withSchemaOrgType(EntityTestDataBuilder.SCHEMA_ORGANIZATION)
                .build();

        entityClient.createEntity(entityRequest)
                .then()
                .statusCode(201)
                .body("id", notNullValue())
                .body("name", equalTo(entityRequest.get("name")))
                .body("entityType", equalTo(EntityTestDataBuilder.TYPE_ORGANIZATION));
    }

    @Test
    @DisplayName("POST /api/entities - should return 400 when invalid request (missing name)")
    void shouldCreateEntity_whenInvalidRequest_returns400() {
        Map<String, Object> invalidRequest = EntityTestDataBuilder.buildInvalidEntity();

        entityClient.createEntity(invalidRequest)
                .then()
                .statusCode(400);
    }

    // ==================== READ Tests ====================

    @Test
    @DisplayName("GET /api/entities - should return 200 with list of entities")
    void shouldGetAllEntities_returns200WithList() {
        entityClient.getAllEntities()
                .then()
                .statusCode(200)
                .body("$", instanceOf(java.util.List.class));
    }

    @Test
    @DisplayName("GET /api/entities/{id} - should return 200 when entity exists")
    void shouldGetEntityById_whenExists_returns200() {
        // First create an entity
        Map<String, Object> entityRequest = EntityTestDataBuilder.anEntity()
                .withName("Entity for GET test " + System.currentTimeMillis())
                .build();

        String entityId = entityClient.createEntity(entityRequest)
                .then()
                .statusCode(201)
                .extract()
                .path("id");

        // Then get it by ID
        entityClient.getEntityById(entityId)
                .then()
                .statusCode(200)
                .body("id", equalTo(entityId))
                .body("name", equalTo(entityRequest.get("name")));
    }

    @Test
    @DisplayName("GET /api/entities/{id} - should return 404 when entity not found")
    void shouldGetEntityById_whenNotFound_returns404() {
        entityClient.getEntityById(EntityTestDataBuilder.NON_EXISTENT_ID)
                .then()
                .statusCode(404);
    }

    // ==================== UPDATE Tests ====================

    @Test
    @DisplayName("PUT /api/entities/{id} - should return 200 when entity exists")
    void shouldUpdateEntity_whenExists_returns200() {
        // First create an entity
        Map<String, Object> createRequest = EntityTestDataBuilder.anEntity()
                .withName("Entity for UPDATE test " + System.currentTimeMillis())
                .build();

        String entityId = entityClient.createEntity(createRequest)
                .then()
                .statusCode(201)
                .extract()
                .path("id");

        // Then update it
        String updatedName = "Updated Entity Name " + System.currentTimeMillis();
        Map<String, Object> updateRequest = EntityTestDataBuilder.anEntity()
                .withName(updatedName)
                .withEntityType(EntityTestDataBuilder.TYPE_ORGANIZATION)
                .build();

        entityClient.updateEntity(entityId, updateRequest)
                .then()
                .statusCode(200)
                .body("id", equalTo(entityId))
                .body("name", equalTo(updatedName));
    }

    @Test
    @DisplayName("PUT /api/entities/{id} - should return 404 when entity not found")
    void shouldUpdateEntity_whenNotFound_returns404() {
        Map<String, Object> updateRequest = EntityTestDataBuilder.anEntity()
                .withName("Non-existent Entity Update")
                .build();

        entityClient.updateEntity(EntityTestDataBuilder.NON_EXISTENT_ID, updateRequest)
                .then()
                .statusCode(404);
    }

    // ==================== DELETE Tests ====================

    @Test
    @DisplayName("DELETE /api/entities/{id} - should return 204 when entity exists")
    void shouldDeleteEntity_whenExists_returns204() {
        // First create an entity
        Map<String, Object> entityRequest = EntityTestDataBuilder.anEntity()
                .withName("Entity for DELETE test " + System.currentTimeMillis())
                .build();

        String entityId = entityClient.createEntity(entityRequest)
                .then()
                .statusCode(201)
                .extract()
                .path("id");

        // Then delete it
        entityClient.deleteEntity(entityId)
                .then()
                .statusCode(204);

        // Verify it's gone
        entityClient.getEntityById(entityId)
                .then()
                .statusCode(404);
    }

    @Test
    @DisplayName("DELETE /api/entities/{id} - should return 404 when entity not found")
    void shouldDeleteEntity_whenNotFound_returns404() {
        entityClient.deleteEntity(EntityTestDataBuilder.NON_EXISTENT_ID)
                .then()
                .statusCode(404);
    }
}
