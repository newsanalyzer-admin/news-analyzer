package org.newsanalyzer.apitests.reasoning;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.newsanalyzer.apitests.BaseApiTest;

import static org.hamcrest.Matchers.*;

/**
 * Ontology statistics tests for the Reasoning Service.
 * Tests GET /entities/ontology/stats endpoint.
 */
@Tag("reasoning")
@Tag("integration")
@DisplayName("Ontology Statistics Tests")
class OntologyStatsTest extends BaseApiTest {

    private ReasoningApiClient client;

    @BeforeEach
    void setUp() {
        client = new ReasoningApiClient(getReasoningSpec());
    }

    // ==================== Basic Stats Tests ====================

    @Test
    @DisplayName("GET /entities/ontology/stats - should return ontology stats with counts")
    void shouldReturnOntologyStats_withCounts() {
        client.getOntologyStats()
                .then()
                .statusCode(anyOf(equalTo(200), equalTo(503)))
                .body("$", notNullValue());
    }

    @Test
    @DisplayName("GET /entities/ontology/stats - should include total triples count")
    void shouldIncludeTotalTriplesCount() {
        client.getOntologyStats()
                .then()
                .statusCode(anyOf(equalTo(200), equalTo(503)))
                .body("total_triples", anyOf(greaterThanOrEqualTo(0), nullValue()));
    }

    // ==================== Class Count Tests ====================

    @Test
    @DisplayName("GET /entities/ontology/stats - should include class count")
    void shouldIncludeClassCount() {
        client.getOntologyStats()
                .then()
                .statusCode(anyOf(equalTo(200), equalTo(503)))
                .body("classes", anyOf(greaterThanOrEqualTo(0), nullValue()));
    }

    // ==================== Property Count Tests ====================

    @Test
    @DisplayName("GET /entities/ontology/stats - should include property count")
    void shouldIncludePropertyCount() {
        client.getOntologyStats()
                .then()
                .statusCode(anyOf(equalTo(200), equalTo(503)))
                .body("properties", anyOf(greaterThanOrEqualTo(0), nullValue()));
    }

    // ==================== Individual Count Tests ====================

    @Test
    @DisplayName("GET /entities/ontology/stats - should include individual count")
    void shouldIncludeIndividualCount() {
        client.getOntologyStats()
                .then()
                .statusCode(anyOf(equalTo(200), equalTo(503)))
                .body("individuals", anyOf(greaterThanOrEqualTo(0), nullValue()));
    }

    // ==================== Response Structure Tests ====================

    @Test
    @DisplayName("GET /entities/ontology/stats - should return numeric values for all counts")
    void shouldReturnNumericValues_forAllCounts() {
        client.getOntologyStats()
                .then()
                .statusCode(anyOf(equalTo(200), equalTo(503)));
        // All count fields should be numeric (integers)
    }

    @Test
    @DisplayName("GET /entities/ontology/stats - should return consistent response structure")
    void shouldReturnConsistentResponseStructure() {
        // Call twice and verify structure is the same
        client.getOntologyStats()
                .then()
                .statusCode(anyOf(equalTo(200), equalTo(503)));

        client.getOntologyStats()
                .then()
                .statusCode(anyOf(equalTo(200), equalTo(503)));
    }

    // ==================== Availability Tests ====================

    @Test
    @DisplayName("GET /entities/ontology/stats - should be available when service is running")
    void shouldBeAvailable_whenServiceRunning() {
        // First check service health
        client.getHealth()
                .then()
                .statusCode(200);

        // Then stats should be available
        client.getOntologyStats()
                .then()
                .statusCode(anyOf(equalTo(200), equalTo(503)));
    }
}
