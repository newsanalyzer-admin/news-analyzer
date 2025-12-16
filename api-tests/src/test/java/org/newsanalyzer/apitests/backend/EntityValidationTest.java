package org.newsanalyzer.apitests.backend;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.newsanalyzer.apitests.BaseApiTest;

import java.util.Map;
import java.util.UUID;

import static org.hamcrest.Matchers.*;

/**
 * Validation tests for the Entity API endpoints.
 * Tests entity validation and verification operations.
 */
@Tag("backend")
@Tag("integration")
@DisplayName("Entity Validation Tests")
class EntityValidationTest extends BaseApiTest {

    private EntityApiClient entityClient;

    @BeforeEach
    void setUp() {
        entityClient = new EntityApiClient(getBackendSpec());
    }

    // ==================== Create and Validate Tests ====================

    @Test
    @DisplayName("POST /api/entities/validate - should create and validate entity, linking to gov org")
    void shouldCreateAndValidateEntity_linksToGovOrg() {
        // Create a government organization entity that should be validated
        Map<String, Object> entityRequest = EntityTestDataBuilder.aGovernmentOrganization()
                .withName("Environmental Protection Agency")
                .withDescription("The EPA is an independent agency of the United States government")
                .build();

        entityClient.createAndValidateEntity(entityRequest)
                .then()
                .statusCode(201)
                .body("id", notNullValue())
                .body("name", equalTo("Environmental Protection Agency"))
                .body("entityType", equalTo(EntityTestDataBuilder.TYPE_GOVERNMENT_ORG));
    }

    @Test
    @DisplayName("POST /api/entities/validate - should create entity even if no gov org match found")
    void shouldCreateAndValidateEntity_createsEvenWithoutMatch() {
        // Create an entity that won't match any government organization
        String uniqueName = "NonMatchingOrganization" + System.currentTimeMillis();
        Map<String, Object> entityRequest = EntityTestDataBuilder.anOrganization()
                .withName(uniqueName)
                .build();

        entityClient.createAndValidateEntity(entityRequest)
                .then()
                .statusCode(201)
                .body("id", notNullValue())
                .body("name", equalTo(uniqueName));
    }

    // ==================== Validate Existing Entity Tests ====================

    @Test
    @DisplayName("POST /api/entities/{id}/validate - should validate existing entity and update link")
    void shouldValidateExistingEntity_updatesLink() {
        // First create an entity without validation
        Map<String, Object> entityRequest = EntityTestDataBuilder.aGovernmentOrganization()
                .withName("Department of Justice " + System.currentTimeMillis())
                .build();

        String entityId = entityClient.createEntity(entityRequest)
                .then()
                .statusCode(201)
                .extract()
                .path("id");

        // Then validate it
        entityClient.validateEntity(UUID.fromString(entityId))
                .then()
                .statusCode(200)
                .body("id", equalTo(entityId));
    }

    @Test
    @DisplayName("POST /api/entities/{id}/validate - should return 404 for non-existent entity")
    void shouldValidateExistingEntity_returns404WhenNotFound() {
        entityClient.validateEntity(EntityTestDataBuilder.NON_EXISTENT_ID)
                .then()
                .statusCode(404);
    }

    // ==================== Verify Entity Tests ====================

    @Test
    @DisplayName("POST /api/entities/{id}/verify - should verify entity and set verified flag")
    void shouldVerifyEntity_setsVerifiedFlag() {
        // First create an entity
        Map<String, Object> entityRequest = EntityTestDataBuilder.anEntity()
                .withName("Entity for verification " + System.currentTimeMillis())
                .build();

        String entityId = entityClient.createEntity(entityRequest)
                .then()
                .statusCode(201)
                .extract()
                .path("id");

        // Then verify it
        entityClient.verifyEntity(UUID.fromString(entityId))
                .then()
                .statusCode(200)
                .body("id", equalTo(entityId))
                .body("verified", equalTo(true));
    }

    @Test
    @DisplayName("POST /api/entities/{id}/verify - should return 404 for non-existent entity")
    void shouldVerifyEntity_returns404WhenNotFound() {
        entityClient.verifyEntity(EntityTestDataBuilder.NON_EXISTENT_ID)
                .then()
                .statusCode(404);
    }

    @Test
    @DisplayName("POST /api/entities/{id}/verify - should be idempotent (verify already verified)")
    void shouldVerifyEntity_isIdempotent() {
        // Create and verify an entity
        Map<String, Object> entityRequest = EntityTestDataBuilder.anEntity()
                .withName("Entity for idempotent verify " + System.currentTimeMillis())
                .build();

        String entityId = entityClient.createEntity(entityRequest)
                .then()
                .statusCode(201)
                .extract()
                .path("id");

        // Verify it twice
        entityClient.verifyEntity(UUID.fromString(entityId))
                .then()
                .statusCode(200)
                .body("verified", equalTo(true));

        entityClient.verifyEntity(UUID.fromString(entityId))
                .then()
                .statusCode(200)
                .body("verified", equalTo(true));
    }
}
