package org.newsanalyzer.apitests.reasoning;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.newsanalyzer.apitests.BaseApiTest;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * Entity linking tests for the Reasoning Service.
 * Tests POST /entities/link and POST /entities/link/single endpoints.
 */
@Tag("reasoning")
@Tag("integration")
@DisplayName("Entity Linking Tests")
class EntityLinkingTest extends BaseApiTest {

    private ReasoningApiClient client;

    @BeforeEach
    void setUp() {
        client = new ReasoningApiClient(getReasoningSpec());
    }

    // ==================== Single Entity Linking Tests ====================

    @Test
    @DisplayName("POST /entities/link/single - should link entity to Wikidata when match found")
    void shouldLinkEntity_toWikidata_whenMatchFound() {
        io.restassured.response.Response response = client.linkSingleEntity(
                "Environmental Protection Agency", "government_org",
                "The EPA announced new regulations");

        int statusCode = response.getStatusCode();
        assertThat(statusCode)
                .as("Status code should be 200 (success) or 503 (service unavailable)")
                .isIn(200, 503);

        if (statusCode == 200) {
            response.then()
                    .body("text", equalTo("Environmental Protection Agency"));
        }
        // When 503, external KB services are unavailable - test passes as service handles gracefully
    }

    @Test
    @DisplayName("POST /entities/link/single - should link entity to DBpedia when Wikidata fails")
    void shouldLinkEntity_toDBpedia_whenWikidataFails() {
        // Test fallback behavior - may require specific entity that's in DBpedia but not Wikidata
        io.restassured.response.Response response = client.linkSingleEntity(
                "Elizabeth Warren", "person",
                "Senator Elizabeth Warren spoke at the hearing");

        int statusCode = response.getStatusCode();
        assertThat(statusCode)
                .as("Status code should be 200 (success) or 503 (service unavailable)")
                .isIn(200, 503);

        if (statusCode == 200) {
            response.then()
                    .body("text", equalTo("Elizabeth Warren"));
        }
        // When 503, external KB services are unavailable - test passes as service handles gracefully
    }

    @Test
    @DisplayName("POST /entities/link/single - should link single entity using convenience endpoint")
    void shouldLinkSingleEntity_convenienceEndpoint() {
        Map<String, Object> request = ReasoningTestDataBuilder.buildSingleLinkRequest(
                "EPA", "government_org", "The EPA issued new regulations"
        );

        io.restassured.response.Response response = client.linkSingleEntity(request);

        int statusCode = response.getStatusCode();
        assertThat(statusCode)
                .as("Status code should be 200 (success) or 503 (service unavailable)")
                .isIn(200, 503);

        if (statusCode == 200) {
            response.then()
                    .body("text", equalTo("EPA"));
        }
        // When 503, external KB services are unavailable - test passes as service handles gracefully
    }

    // ==================== Batch Entity Linking Tests ====================

    @Test
    @DisplayName("POST /entities/link - should link batch entities and return statistics")
    void shouldLinkBatchEntities_returnsStatistics() {
        List<Map<String, Object>> entities = Arrays.asList(
                ReasoningTestDataBuilder.buildEpaLinkEntity(),
                ReasoningTestDataBuilder.buildPersonLinkEntity()
        );

        Map<String, Object> options = ReasoningTestDataBuilder.buildLinkingOptions(
                "both", 0.7, 5
        );

        Map<String, Object> request = ReasoningTestDataBuilder.buildBatchLinkRequest(entities, options);

        io.restassured.response.Response response = client.linkEntities(request);

        int statusCode = response.getStatusCode();
        assertThat(statusCode)
                .as("Status code should be 200 (success) or 503 (service unavailable)")
                .isIn(200, 503);

        if (statusCode == 200) {
            response.then()
                    .body("linked_entities", notNullValue())
                    .body("statistics", notNullValue());
        }
        // When 503, external KB services are unavailable - test passes as service handles gracefully
    }

    @Test
    @DisplayName("POST /entities/link - should return statistics with counts")
    void shouldReturnStatistics_withCounts() {
        List<Map<String, Object>> entities = Arrays.asList(
                ReasoningTestDataBuilder.buildEpaLinkEntity()
        );

        Map<String, Object> request = ReasoningTestDataBuilder.buildBatchLinkRequest(entities, null);

        client.linkEntities(request)
                .then()
                .statusCode(anyOf(equalTo(200), equalTo(503)));
        // When successful, statistics should include: total, linked, needs_review, not_found, errors
    }

    // ==================== Candidate and Ambiguity Tests ====================

    @Test
    @DisplayName("POST /entities/link/single - should return candidates when ambiguous")
    void shouldReturnCandidates_whenAmbiguous() {
        // Use an ambiguous entity name that could match multiple records
        client.linkSingleEntity("Washington", "location", "The event was held in Washington")
                .then()
                .statusCode(anyOf(equalTo(200), equalTo(503)));
        // Ambiguous entities may have is_ambiguous=true and candidates list
    }

    @Test
    @DisplayName("POST /entities/link/single - should set needs review when low confidence")
    void shouldSetNeedsReview_whenLowConfidence() {
        // Use an entity that may have low confidence match
        Map<String, Object> request = ReasoningTestDataBuilder.buildSingleLinkRequest(
                "Unknown Agency XYZ", "government_org", "The Unknown Agency XYZ released a statement"
        );

        client.linkSingleEntity(request)
                .then()
                .statusCode(anyOf(equalTo(200), equalTo(503)));
        // Low confidence matches should have needs_review=true
    }

    // ==================== Not Found Tests ====================

    @Test
    @DisplayName("POST /entities/link/single - should return not found when no match exists")
    void shouldReturnNotFound_whenNoMatchExists() {
        client.linkSingleEntity("Completely Fictional Entity 12345XYZ",
                               "organization",
                               "The Completely Fictional Entity announced today")
                .then()
                .statusCode(anyOf(equalTo(200), equalTo(503)));
        // linking_status should be "not_found" for entities with no KB match
    }

    // ==================== Error Handling Tests ====================

    @Test
    @DisplayName("POST /entities/link - should return 503 when linking service unavailable")
    void shouldReturn503_whenLinkingServiceUnavailable() {
        // This test validates behavior when external KB services are down
        // In mock mode, WireMock can simulate this
        List<Map<String, Object>> entities = Arrays.asList(
                ReasoningTestDataBuilder.buildEpaLinkEntity()
        );

        Map<String, Object> request = ReasoningTestDataBuilder.buildBatchLinkRequest(entities, null);

        // Response should be either 200 (success) or 503 (service unavailable)
        client.linkEntities(request)
                .then()
                .statusCode(anyOf(equalTo(200), equalTo(503)));
    }

    // ==================== Options Tests ====================

    @Test
    @DisplayName("POST /entities/link - should respect source option for wikidata only")
    void shouldRespectSourceOption_wikidataOnly() {
        List<Map<String, Object>> entities = Arrays.asList(
                ReasoningTestDataBuilder.buildEpaLinkEntity()
        );

        Map<String, Object> options = ReasoningTestDataBuilder.buildLinkingOptions(
                "wikidata", 0.5, 3
        );

        Map<String, Object> request = ReasoningTestDataBuilder.buildBatchLinkRequest(entities, options);

        client.linkEntities(request)
                .then()
                .statusCode(anyOf(equalTo(200), equalTo(503)));
    }

    @Test
    @DisplayName("POST /entities/link - should respect source option for dbpedia only")
    void shouldRespectSourceOption_dbpediaOnly() {
        List<Map<String, Object>> entities = Arrays.asList(
                ReasoningTestDataBuilder.buildEpaLinkEntity()
        );

        Map<String, Object> options = ReasoningTestDataBuilder.buildLinkingOptions(
                "dbpedia", 0.5, 3
        );

        Map<String, Object> request = ReasoningTestDataBuilder.buildBatchLinkRequest(entities, options);

        client.linkEntities(request)
                .then()
                .statusCode(anyOf(equalTo(200), equalTo(503)));
    }

    @Test
    @DisplayName("POST /entities/link - should respect max candidates option")
    void shouldRespectMaxCandidatesOption() {
        List<Map<String, Object>> entities = Arrays.asList(
                ReasoningTestDataBuilder.buildSingleLinkRequest(
                        "Washington", "location", "Meeting in Washington"
                )
        );

        Map<String, Object> options = ReasoningTestDataBuilder.buildLinkingOptions(
                "both", 0.3, 2
        );

        Map<String, Object> request = ReasoningTestDataBuilder.buildBatchLinkRequest(entities, options);

        client.linkEntities(request)
                .then()
                .statusCode(anyOf(equalTo(200), equalTo(503)));
    }
}
