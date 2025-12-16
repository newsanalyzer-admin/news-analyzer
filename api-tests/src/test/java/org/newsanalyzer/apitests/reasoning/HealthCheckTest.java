package org.newsanalyzer.apitests.reasoning;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.newsanalyzer.apitests.BaseApiTest;

import static org.hamcrest.Matchers.*;

/**
 * Health check tests for the Python FastAPI reasoning service.
 * Tests root, health, and government-orgs health endpoints.
 */
@Tag("reasoning")
@Tag("integration")
@DisplayName("Reasoning Service Health Check Tests")
class HealthCheckTest extends BaseApiTest {

    private ReasoningApiClient client;

    @BeforeEach
    void setUp() {
        client = new ReasoningApiClient(getReasoningSpec());
    }

    // ==================== Root Endpoint Tests ====================

    @Test
    @DisplayName("GET / - should return healthy status when root endpoint called")
    void shouldReturnHealthy_whenRootEndpoint() {
        client.getRootHealth()
                .then()
                .statusCode(200)
                .body("status", equalTo("healthy"))
                .body("service", notNullValue())
                .body("version", notNullValue());
    }

    @Test
    @DisplayName("GET / - should return service info in root response")
    void shouldReturnServiceInfo_whenRootEndpoint() {
        client.getRootHealth()
                .then()
                .statusCode(200)
                .body("service", containsString("NewsAnalyzer"));
    }

    // ==================== Health Endpoint Tests ====================

    @Test
    @DisplayName("GET /health - should return detailed health when health endpoint called")
    void shouldReturnDetailedHealth_whenHealthEndpoint() {
        client.getHealth()
                .then()
                .statusCode(200)
                .body("status", equalTo("healthy"))
                .body("services", notNullValue());
    }

    @Test
    @DisplayName("GET /health - should include spaCy status in health response")
    void shouldIncludeSpacyStatus_inHealthResponse() {
        client.getHealth()
                .then()
                .statusCode(200)
                .body("services.spacy", notNullValue());
    }

    @Test
    @DisplayName("GET /health - should include prolog status in health response")
    void shouldIncludePrologStatus_inHealthResponse() {
        client.getHealth()
                .then()
                .statusCode(200)
                .body("services.prolog", notNullValue());
    }

    // ==================== Government Orgs Health Endpoint Tests ====================

    @Test
    @DisplayName("GET /government-orgs/health - should return gov org service health")
    void shouldReturnGovOrgHealth_whenGovOrgHealthEndpoint() {
        client.getGovOrgsHealth()
                .then()
                .statusCode(200)
                .body("service", equalTo("Government Organization Ingestion"))
                .body("status", equalTo("healthy"));
    }

    @Test
    @DisplayName("GET /government-orgs/health - should include API key configuration status")
    void shouldIncludeApiKeyStatus_inGovOrgHealthResponse() {
        client.getGovOrgsHealth()
                .then()
                .statusCode(200)
                .body("api_key_configured", notNullValue());
    }

    @Test
    @DisplayName("GET /government-orgs/health - should include features list")
    void shouldIncludeFeatures_inGovOrgHealthResponse() {
        client.getGovOrgsHealth()
                .then()
                .statusCode(200)
                .body("features", notNullValue())
                .body("features.ingestion", equalTo(true))
                .body("features.parsing", equalTo(true))
                .body("features.schema_org_transformation", equalTo(true));
    }

    @Test
    @DisplayName("GET /government-orgs/health - should include timestamp")
    void shouldIncludeTimestamp_inGovOrgHealthResponse() {
        client.getGovOrgsHealth()
                .then()
                .statusCode(200)
                .body("timestamp", notNullValue());
    }
}
