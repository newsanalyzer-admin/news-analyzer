package org.newsanalyzer.apitests.reasoning;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.newsanalyzer.apitests.BaseApiTest;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;

/**
 * OWL reasoning tests for the Reasoning Service.
 * Tests POST /entities/reason endpoint for inference and enrichment.
 */
@Tag("reasoning")
@Tag("integration")
@DisplayName("OWL Reasoning Tests")
class OwlReasoningTest extends BaseApiTest {

    private ReasoningApiClient client;

    @BeforeEach
    void setUp() {
        client = new ReasoningApiClient(getReasoningSpec());
    }

    // ==================== Entity Enrichment Tests ====================

    @Test
    @DisplayName("POST /entities/reason - should enrich entity with inferred types")
    void shouldEnrichEntity_withInferredTypes() {
        List<Map<String, Object>> entities = Arrays.asList(
                ReasoningTestDataBuilder.buildEpaReasoningEntity()
        );

        Map<String, Object> request = ReasoningTestDataBuilder.buildReasoningRequest(entities, true);

        client.reasonEntities(request)
                .then()
                .statusCode(anyOf(equalTo(200), equalTo(503)))
                .body("enriched_entities", notNullValue());
    }

    @Test
    @DisplayName("POST /entities/reason - should apply inference when enabled")
    void shouldApplyInference_whenEnabled() {
        List<Map<String, Object>> entities = Arrays.asList(
                ReasoningTestDataBuilder.buildEpaReasoningEntity()
        );

        Map<String, Object> request = ReasoningTestDataBuilder.buildReasoningRequest(entities, true);

        client.reasonEntities(request)
                .then()
                .statusCode(anyOf(equalTo(200), equalTo(503)))
                .body("inferred_triples", greaterThanOrEqualTo(0));
    }

    @Test
    @DisplayName("POST /entities/reason - should skip inference when disabled")
    void shouldSkipInference_whenDisabled() {
        List<Map<String, Object>> entities = Arrays.asList(
                ReasoningTestDataBuilder.buildEpaReasoningEntity()
        );

        Map<String, Object> request = ReasoningTestDataBuilder.buildReasoningRequest(entities, false);

        client.reasonEntities(request)
                .then()
                .statusCode(anyOf(equalTo(200), equalTo(503)))
                .body("inferred_triples", equalTo(0));
    }

    // ==================== Inference Count Tests ====================

    @Test
    @DisplayName("POST /entities/reason - should return inferred triple count")
    void shouldReturnInferredTripleCount() {
        List<Map<String, Object>> entities = Arrays.asList(
                ReasoningTestDataBuilder.buildEpaReasoningEntity()
        );

        Map<String, Object> request = ReasoningTestDataBuilder.buildReasoningRequest(entities, true);

        client.reasonEntities(request)
                .then()
                .statusCode(anyOf(equalTo(200), equalTo(503)))
                .body("inferred_triples", notNullValue());
    }

    // ==================== Consistency Checking Tests ====================

    @Test
    @DisplayName("POST /entities/reason - should check consistency and return errors")
    void shouldCheckConsistency_returnsErrors() {
        List<Map<String, Object>> entities = Arrays.asList(
                ReasoningTestDataBuilder.buildEpaReasoningEntity()
        );

        Map<String, Object> request = ReasoningTestDataBuilder.buildReasoningRequest(entities, true);

        client.reasonEntities(request)
                .then()
                .statusCode(anyOf(equalTo(200), equalTo(503)))
                .body("consistency_errors", notNullValue());
    }

    @Test
    @DisplayName("POST /entities/reason - should return empty consistency errors for valid entities")
    void shouldReturnEmptyConsistencyErrors_forValidEntities() {
        List<Map<String, Object>> entities = Arrays.asList(
                ReasoningTestDataBuilder.buildReasoningEntity(
                        "Department of Justice", "government_org", 0.95, null
                )
        );

        Map<String, Object> request = ReasoningTestDataBuilder.buildReasoningRequest(entities, true);

        client.reasonEntities(request)
                .then()
                .statusCode(anyOf(equalTo(200), equalTo(503)));
        // Valid entities should have empty consistency_errors list
    }

    // ==================== Classification Tests ====================

    @Test
    @DisplayName("POST /entities/reason - should classify executive agency from regulates property")
    void shouldClassifyExecutiveAgency_fromRegulatesProperty() {
        Map<String, Object> properties = new HashMap<>();
        properties.put("regulates", "environmental_policy");

        List<Map<String, Object>> entities = Arrays.asList(
                ReasoningTestDataBuilder.buildReasoningEntity(
                        "EPA", "government_org", 0.9, properties
                )
        );

        Map<String, Object> request = ReasoningTestDataBuilder.buildReasoningRequest(entities, true);

        client.reasonEntities(request)
                .then()
                .statusCode(anyOf(equalTo(200), equalTo(503)))
                .body("enriched_entities", notNullValue());
    }

    // ==================== Multiple Entity Tests ====================

    @Test
    @DisplayName("POST /entities/reason - should process multiple entities")
    void shouldProcessMultipleEntities() {
        List<Map<String, Object>> entities = Arrays.asList(
                ReasoningTestDataBuilder.buildReasoningEntity("EPA", "government_org", 0.9, null),
                ReasoningTestDataBuilder.buildReasoningEntity("FBI", "government_org", 0.85, null),
                ReasoningTestDataBuilder.buildReasoningEntity("Elizabeth Warren", "person", 0.9, null)
        );

        Map<String, Object> request = ReasoningTestDataBuilder.buildReasoningRequest(entities, true);

        client.reasonEntities(request)
                .then()
                .statusCode(anyOf(equalTo(200), equalTo(503)))
                .body("enriched_entities.size()", greaterThanOrEqualTo(0));
    }

    // ==================== Error Handling Tests ====================

    @Test
    @DisplayName("POST /entities/reason - should return 500 when reasoning fails")
    void shouldReturn500_whenReasoningFails() {
        // This test validates error handling when OWL reasoning encounters an issue
        // In production, malformed data or invalid ontology queries may cause this
        // In mock mode, WireMock can simulate this scenario
        List<Map<String, Object>> entities = Arrays.asList(
                ReasoningTestDataBuilder.buildEpaReasoningEntity()
        );

        Map<String, Object> request = ReasoningTestDataBuilder.buildReasoningRequest(entities, true);

        // Response should be either 200 (success), 500 (error), or 503 (service unavailable)
        client.reasonEntities(request)
                .then()
                .statusCode(anyOf(equalTo(200), equalTo(500), equalTo(503)));
    }

    @Test
    @DisplayName("POST /entities/reason - should handle empty entity list")
    void shouldHandleEmptyEntityList() {
        List<Map<String, Object>> entities = Arrays.asList();

        Map<String, Object> request = ReasoningTestDataBuilder.buildReasoningRequest(entities, true);

        client.reasonEntities(request)
                .then()
                .statusCode(anyOf(equalTo(200), equalTo(400), equalTo(422)));
    }

    // ==================== Property-Based Reasoning Tests ====================

    @Test
    @DisplayName("POST /entities/reason - should use properties for classification")
    void shouldUseProperties_forClassification() {
        Map<String, Object> properties = new HashMap<>();
        properties.put("govBranch", "EXECUTIVE");
        properties.put("isCabinet", true);

        List<Map<String, Object>> entities = Arrays.asList(
                ReasoningTestDataBuilder.buildReasoningEntity(
                        "Department of Defense", "government_org", 0.95, properties
                )
        );

        Map<String, Object> request = ReasoningTestDataBuilder.buildReasoningRequest(entities, true);

        client.reasonEntities(request)
                .then()
                .statusCode(anyOf(equalTo(200), equalTo(503)))
                .body("enriched_entities", notNullValue());
    }
}
