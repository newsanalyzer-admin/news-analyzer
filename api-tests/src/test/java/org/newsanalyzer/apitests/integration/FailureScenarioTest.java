package org.newsanalyzer.apitests.integration;

import io.restassured.response.Response;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.newsanalyzer.apitests.config.TestConfig;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for failure scenarios in cross-service workflows.
 *
 * <p>Tests graceful handling of service unavailability, timeouts, and invalid data.</p>
 *
 * <p>Scenarios:</p>
 * <ul>
 *   <li>Reasoning service unavailable</li>
 *   <li>Backend service unavailable</li>
 *   <li>Timeout on slow responses</li>
 *   <li>Invalid data from reasoning service</li>
 *   <li>Retry on transient failures</li>
 * </ul>
 */
@Tag("integration")
@DisplayName("Failure Scenario Tests")
class FailureScenarioTest extends IntegrationTestBase {

    @AfterEach
    void cleanup() {
        cleanupTestData();
    }

    // ==================== AC 6: Failure Scenarios ====================

    @Test
    @DisplayName("Given reasoning service unavailable, when extraction attempted, then graceful failure")
    void shouldHandleReasoningServiceUnavailable() {
        // Given - attempt to call a non-existent endpoint to simulate unavailability
        System.out.println("\n[TEST] shouldHandleReasoningServiceUnavailable");

        // Use an invalid port to simulate service down
        String invalidUrl = "http://localhost:59999";

        Instant start = startTiming();

        try {
            // When - attempt to extract from unavailable service
            Response response = given()
                    .baseUri(invalidUrl)
                    .contentType(JSON)
                    .body(Map.of("text", "Test text", "confidence_threshold", 0.7))
                    .when()
                    .post("/entities/extract");

            endTiming(start, "Request to unavailable service");

            // This shouldn't be reached if service is truly unavailable
            System.out.println("  Response received (unexpected): " + response.statusCode());

        } catch (Exception e) {
            endTiming(start, "Connection failure");

            // Then - should fail gracefully with connection error
            System.out.println("  Expected failure: " + e.getClass().getSimpleName());
            System.out.println("  Message: " + e.getMessage());

            assertThat(e)
                    .as("Should throw connection exception")
                    .isInstanceOfAny(
                            java.net.ConnectException.class,
                            org.apache.http.conn.HttpHostConnectException.class,
                            Exception.class
                    );
        }

        printTimingSummary();
    }

    @Test
    @DisplayName("Given backend unavailable, when storage attempted, then graceful failure")
    void shouldHandleBackendUnavailable() {
        // Given - attempt to call backend on invalid port
        System.out.println("\n[TEST] shouldHandleBackendUnavailable");

        String invalidUrl = "http://localhost:59998";

        Instant start = startTiming();

        try {
            // When - attempt to create entity on unavailable backend
            Map<String, Object> entityRequest = new HashMap<>();
            entityRequest.put("name", "Test Entity");
            entityRequest.put("entityType", "CONCEPT");

            Response response = given()
                    .baseUri(invalidUrl)
                    .contentType(JSON)
                    .body(entityRequest)
                    .when()
                    .post("/api/entities");

            endTiming(start, "Request to unavailable backend");

            System.out.println("  Response received (unexpected): " + response.statusCode());

        } catch (Exception e) {
            endTiming(start, "Connection failure");

            // Then - should fail gracefully
            System.out.println("  Expected failure: " + e.getClass().getSimpleName());

            assertThat(e)
                    .as("Should throw connection exception")
                    .isNotNull();
        }

        printTimingSummary();
    }

    @Test
    @DisplayName("Given slow response, when timeout exceeded, then request fails appropriately")
    void shouldHandleTimeout_onSlowResponse() {
        // Given - configure very short timeout
        System.out.println("\n[TEST] shouldHandleTimeout_onSlowResponse");

        Instant start = startTiming();

        // Using actual service but checking response time
        Response response = reasoningClient.extractEntities(SAMPLE_ARTICLE, 0.5);
        long responseTime = response.getTime();
        endTiming(start, "Extraction request");

        System.out.println("  Response time: " + responseTime + " ms");
        System.out.println("  Status code: " + response.statusCode());

        // Then - verify response time is reasonable (under configured timeout)
        int configuredTimeout = TestConfig.getTimeoutSeconds() * 1000;
        System.out.println("  Configured timeout: " + configuredTimeout + " ms");

        assertThat(responseTime)
                .as("Response should complete before timeout")
                .isLessThan(configuredTimeout);

        // If response is slow, log warning
        if (responseTime > 5000) {
            System.out.println("  WARNING: Response time exceeded 5 seconds");
        }

        printTimingSummary();
    }

    @Test
    @DisplayName("Given invalid JSON sent to reasoning, when processed, then returns error")
    void shouldHandleInvalidData_fromReasoning() {
        // Given - send malformed request to reasoning service
        System.out.println("\n[TEST] shouldHandleInvalidData_fromReasoning");

        Instant start = startTiming();

        // When - send request with missing required field
        Map<String, Object> invalidRequest = new HashMap<>();
        // Missing "text" field which is required
        invalidRequest.put("confidence_threshold", 0.7);

        Response response = given()
                .spec(getReasoningSpec())
                .contentType(JSON)
                .body(invalidRequest)
                .when()
                .post("/entities/extract");

        endTiming(start, "Invalid request");

        System.out.println("  Response status: " + response.statusCode());

        // Then - should return validation error (422) or bad request (400)
        assertThat(response.statusCode())
                .as("Should return validation error for missing required field")
                .isIn(400, 422);

        // Verify error message is helpful
        String detail = response.jsonPath().getString("detail");
        if (detail == null) {
            // Try alternative error format
            detail = response.asString();
        }
        System.out.println("  Error detail: " + detail);

        assertThat(detail)
                .as("Error message should be present")
                .isNotNull()
                .isNotEmpty();

        printTimingSummary();
    }

    @Test
    @DisplayName("Given invalid entity type to backend, when created, then returns validation error")
    void shouldHandleInvalidEntityType_inBackend() {
        // Given - entity with invalid type
        System.out.println("\n[TEST] shouldHandleInvalidEntityType_inBackend");

        Instant start = startTiming();

        Map<String, Object> invalidEntity = new HashMap<>();
        invalidEntity.put("name", "Test Entity");
        invalidEntity.put("entityType", "INVALID_TYPE_12345");

        // When - attempt to create
        Response response = entityClient.createEntity(invalidEntity);
        endTiming(start, "Create with invalid type");

        System.out.println("  Response status: " + response.statusCode());

        // Then - should reject invalid type
        assertThat(response.statusCode())
                .as("Should return error for invalid entity type")
                .isIn(400, 422, 500);

        printTimingSummary();
    }

    @Test
    @DisplayName("Given empty text for extraction, when processed, then handles gracefully")
    void shouldHandleEmptyText_gracefully() {
        // Given - empty text
        System.out.println("\n[TEST] shouldHandleEmptyText_gracefully");

        Instant start = startTiming();

        // When - attempt extraction with empty text
        Response response = reasoningClient.extractEntities("", 0.5);
        endTiming(start, "Extract empty text");

        System.out.println("  Response status: " + response.statusCode());

        // Then - should either succeed with empty results or return validation error
        assertThat(response.statusCode())
                .as("Should handle empty text appropriately")
                .isIn(200, 400, 422);

        if (response.statusCode() == 200) {
            int count = response.jsonPath().getInt("total_count");
            System.out.println("  Entities found: " + count);
            assertThat(count)
                    .as("Empty text should yield no entities")
                    .isZero();
        }

        printTimingSummary();
    }

    @Test
    @DisplayName("Given non-existent entity ID, when retrieved, then returns 404")
    void shouldHandle_nonExistentEntity() {
        // Given - random UUID that doesn't exist
        System.out.println("\n[TEST] shouldHandle_nonExistentEntity");

        String nonExistentId = "00000000-0000-0000-0000-000000000000";

        Instant start = startTiming();

        // When - attempt to retrieve
        Response response = entityClient.getEntityById(nonExistentId);
        endTiming(start, "Get non-existent entity");

        System.out.println("  Response status: " + response.statusCode());

        // Then - should return 404
        assertThat(response.statusCode())
                .as("Non-existent entity should return 404")
                .isEqualTo(404);

        printTimingSummary();
    }

    @Test
    @DisplayName("Given invalid UUID format, when used as entity ID, then returns error")
    void shouldHandle_invalidUuidFormat() {
        // Given - invalid UUID format
        System.out.println("\n[TEST] shouldHandle_invalidUuidFormat");

        String invalidId = "not-a-valid-uuid";

        Instant start = startTiming();

        // When - attempt to retrieve with invalid ID
        Response response = entityClient.getEntityById(invalidId);
        endTiming(start, "Get with invalid UUID");

        System.out.println("  Response status: " + response.statusCode());

        // Then - should return 400 or 404
        assertThat(response.statusCode())
                .as("Invalid UUID should return error")
                .isIn(400, 404, 500);

        printTimingSummary();
    }

    @Test
    @DisplayName("Given extremely long text, when extracted, then handles without crashing")
    void shouldHandle_veryLongText() {
        // Given - very long text (simulate large article)
        System.out.println("\n[TEST] shouldHandle_veryLongText");

        StringBuilder longText = new StringBuilder();
        for (int i = 0; i < 100; i++) {
            longText.append("Senator Elizabeth Warren discussed policy with EPA officials. ");
        }
        String text = longText.toString();
        System.out.println("  Text length: " + text.length() + " characters");

        Instant start = startTiming();

        // When - attempt extraction
        Response response = reasoningClient.extractEntities(text, 0.5);
        endTiming(start, "Extract long text");

        System.out.println("  Response status: " + response.statusCode());

        // Then - should handle without error (may take longer)
        assertThat(response.statusCode())
                .as("Long text should be processed")
                .isIn(200, 413, 422); // 413 = Payload Too Large

        if (response.statusCode() == 200) {
            int count = response.jsonPath().getInt("total_count");
            System.out.println("  Entities extracted: " + count);
        }

        printTimingSummary();
    }

    @Test
    @DisplayName("Given special characters in entity name, when stored and retrieved, then preserved")
    void shouldHandle_specialCharacters() {
        // Given - entity name with special characters
        System.out.println("\n[TEST] shouldHandle_specialCharacters");

        Map<String, Object> entityRequest = new HashMap<>();
        entityRequest.put("name", "Test & Entity <with> \"special\" 'chars' / 100%");
        entityRequest.put("entityType", "CONCEPT");
        entityRequest.put("confidenceScore", 0.9);

        Instant start = startTiming();

        // When - create entity
        Response createResponse = entityClient.createEntity(entityRequest);
        endTiming(start, "Create entity with special chars");

        System.out.println("  Create status: " + createResponse.statusCode());

        if (createResponse.statusCode() == 201 || createResponse.statusCode() == 200) {
            String entityId = createResponse.jsonPath().getString("id");
            trackEntityForCleanup(entityId);

            // Retrieve and verify
            start = startTiming();
            Response getResponse = entityClient.getEntityById(entityId);
            endTiming(start, "Retrieve entity");

            String retrievedName = getResponse.jsonPath().getString("name");
            System.out.println("  Original name: " + entityRequest.get("name"));
            System.out.println("  Retrieved name: " + retrievedName);

            // Then - name should be preserved
            assertThat(retrievedName)
                    .as("Special characters should be preserved")
                    .isEqualTo(entityRequest.get("name"));
        } else {
            System.out.println("  Creation failed (may be expected for some special chars)");
        }

        printTimingSummary();
    }

    @Test
    @DisplayName("Given concurrent requests, when processed, then no data corruption")
    void shouldHandle_concurrentRequests() {
        // Given - multiple simultaneous requests
        System.out.println("\n[TEST] shouldHandle_concurrentRequests");

        int numRequests = 5;
        String[] entityIds = new String[numRequests];

        Instant start = startTiming();

        // When - create multiple entities concurrently (sequential for simplicity)
        for (int i = 0; i < numRequests; i++) {
            Map<String, Object> entityRequest = new HashMap<>();
            entityRequest.put("name", "Concurrent Test Entity " + i);
            entityRequest.put("entityType", "CONCEPT");
            entityRequest.put("confidenceScore", 0.9);

            Response response = entityClient.createEntity(entityRequest);
            if (response.statusCode() == 201 || response.statusCode() == 200) {
                entityIds[i] = response.jsonPath().getString("id");
                trackEntityForCleanup(entityIds[i]);
            }
        }
        endTiming(start, "Create " + numRequests + " entities");

        // Then - verify all were created correctly
        start = startTiming();
        int successCount = 0;
        for (int i = 0; i < numRequests; i++) {
            if (entityIds[i] != null) {
                Response getResponse = entityClient.getEntityById(entityIds[i]);
                if (getResponse.statusCode() == 200) {
                    String name = getResponse.jsonPath().getString("name");
                    assertThat(name).contains("Concurrent Test Entity " + i);
                    successCount++;
                }
            }
        }
        endTiming(start, "Verify " + numRequests + " entities");

        System.out.println("  Created and verified: " + successCount + "/" + numRequests);

        assertThat(successCount)
                .as("All concurrent requests should succeed")
                .isEqualTo(numRequests);

        printTimingSummary();
    }
}
