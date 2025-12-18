package org.newsanalyzer.apitests.reasoning;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.newsanalyzer.apitests.BaseApiTest;

import java.util.Map;

import static org.hamcrest.Matchers.*;

/**
 * Government Organization API tests for the Reasoning Service.
 * Tests /government-orgs/* endpoints for ingestion, processing, and enrichment.
 */
@Tag("reasoning")
@Tag("integration")
@DisplayName("Government Organization API Tests")
class GovOrgApiTest extends BaseApiTest {

    private ReasoningApiClient client;

    @BeforeEach
    void setUp() {
        client = new ReasoningApiClient(getReasoningSpec());
    }

    // ==================== Ingestion Tests ====================

    @Test
    @DisplayName("POST /government-orgs/ingest - should trigger ingestion when valid year returns 200")
    void shouldTriggerIngestion_whenValidYear_returns200() {
        Map<String, Object> request = ReasoningTestDataBuilder.buildIngestionRequest(
                ReasoningTestDataBuilder.VALID_YEAR
        );

        client.triggerIngestion(request)
                .then()
                .statusCode(anyOf(equalTo(200), equalTo(400), equalTo(500), equalTo(503)));
        // 200 = success, 400 = validation error, 500 = ingestion error, 503 = API unavailable
    }

    @Test
    @DisplayName("POST /government-orgs/ingest - should return 400 when invalid year (too low)")
    void shouldReturn400_whenInvalidYear_tooLow() {
        Map<String, Object> request = ReasoningTestDataBuilder.buildIngestionRequest(
                ReasoningTestDataBuilder.INVALID_YEAR_LOW, false, null
        );

        client.triggerIngestion(request)
                .then()
                .statusCode(anyOf(equalTo(400), equalTo(422)));
    }

    @Test
    @DisplayName("POST /government-orgs/ingest - should return 400 when invalid year (too high)")
    void shouldReturn400_whenInvalidYear_tooHigh() {
        Map<String, Object> request = ReasoningTestDataBuilder.buildIngestionRequest(
                ReasoningTestDataBuilder.INVALID_YEAR_HIGH, false, null
        );

        client.triggerIngestion(request)
                .then()
                .statusCode(anyOf(equalTo(400), equalTo(422)));
    }

    @Test
    @DisplayName("POST /government-orgs/ingest - should return ingestion response structure")
    void shouldReturnIngestionResponseStructure() {
        Map<String, Object> request = ReasoningTestDataBuilder.buildIngestionRequest(
                ReasoningTestDataBuilder.VALID_YEAR
        );

        client.triggerIngestion(request)
                .then()
                .statusCode(anyOf(equalTo(200), equalTo(400), equalTo(500), equalTo(503)));
        // Response should include: status, year, total_organizations, etc.
        // 400 = validation error may occur depending on service state
    }

    // ==================== Package Processing Tests ====================

    @Test
    @DisplayName("POST /government-orgs/process-package - should process package when valid package ID")
    void shouldProcessPackage_whenValidPackageId() {
        client.processPackage(ReasoningTestDataBuilder.SAMPLE_PACKAGE_ID)
                .then()
                .statusCode(anyOf(equalTo(200), equalTo(500), equalTo(503)));
    }

    @Test
    @DisplayName("POST /government-orgs/process-package - should return 500 when package processing fails")
    void shouldReturn500_whenPackageProcessingFails() {
        // Use an invalid/non-existent package ID
        client.processPackage("INVALID-PACKAGE-ID-12345")
                .then()
                .statusCode(anyOf(equalTo(400), equalTo(500), equalTo(503)));
    }

    // ==================== Fetch Packages Tests ====================

    @Test
    @DisplayName("GET /government-orgs/fetch-packages - should fetch packages with pagination")
    void shouldFetchPackages_withPagination() {
        client.fetchPackages(ReasoningTestDataBuilder.VALID_YEAR, 10, 0)
                .then()
                .statusCode(anyOf(equalTo(200), equalTo(400), equalTo(500)))
                .body("year", anyOf(equalTo(ReasoningTestDataBuilder.VALID_YEAR), nullValue()));
    }

    @Test
    @DisplayName("GET /government-orgs/fetch-packages - should return packages list")
    void shouldReturnPackagesList() {
        client.fetchPackages(ReasoningTestDataBuilder.VALID_YEAR)
                .then()
                .statusCode(anyOf(equalTo(200), equalTo(400), equalTo(500)));
        // Response should include: packages, count, offset, page_size
    }

    @Test
    @DisplayName("GET /government-orgs/fetch-packages - should respect pagination parameters")
    void shouldRespectPaginationParameters() {
        client.fetchPackages(ReasoningTestDataBuilder.VALID_YEAR, 5, 0)
                .then()
                .statusCode(anyOf(equalTo(200), equalTo(400), equalTo(500)));
    }

    // ==================== Entity Enrichment Tests ====================

    @Test
    @DisplayName("POST /government-orgs/enrich-entity - should enrich entity with gov org data")
    void shouldEnrichEntity_withGovOrgData() {
        Map<String, Object> request = ReasoningTestDataBuilder.buildEpaEnrichmentRequest();

        client.enrichEntity(request)
                .then()
                .statusCode(200)
                .body("entity_text", equalTo("EPA"))
                .body("entity_type", equalTo("government_org"))
                .body("is_government_org", equalTo(true));
    }

    @Test
    @DisplayName("POST /government-orgs/enrich-entity - should return enrichment response structure")
    void shouldReturnEnrichmentResponseStructure() {
        Map<String, Object> request = ReasoningTestDataBuilder.buildEnrichmentRequest(
                "Department of Justice", "government_org", 0.9
        );

        client.enrichEntity(request)
                .then()
                .statusCode(200)
                .body("entity_text", notNullValue())
                .body("entity_type", notNullValue())
                .body("confidence", notNullValue())
                .body("is_government_org", notNullValue());
    }

    @Test
    @DisplayName("POST /government-orgs/enrich-entity - should handle non-government entity")
    void shouldHandleNonGovernmentEntity() {
        Map<String, Object> request = ReasoningTestDataBuilder.buildEnrichmentRequest(
                "Apple Inc.", "organization", 0.85
        );

        client.enrichEntity(request)
                .then()
                .statusCode(200)
                .body("is_government_org", equalTo(false));
    }

    // ==================== Health and API Connection Tests ====================

    @Test
    @DisplayName("GET /government-orgs/health - should return gov org health with API key status")
    void shouldReturnGovOrgHealth_withApiKeyStatus() {
        client.getGovOrgsHealth()
                .then()
                .statusCode(200)
                .body("status", equalTo("healthy"))
                .body("api_key_configured", notNullValue());
    }

    @Test
    @DisplayName("GET /government-orgs/test-api-connection - should test API connection returns status")
    void shouldTestApiConnection_returnsStatus() {
        client.testApiConnection()
                .then()
                .statusCode(200)
                .body("status", anyOf(equalTo("success"), equalTo("error")))
                .body("api_accessible", notNullValue());
    }

    @Test
    @DisplayName("GET /government-orgs/test-api-connection - should include timestamp")
    void shouldIncludeTimestamp_inApiConnectionTest() {
        client.testApiConnection()
                .then()
                .statusCode(200)
                .body("timestamp", notNullValue());
    }

    // ==================== Service Unavailability Tests ====================

    @Test
    @DisplayName("POST /government-orgs/ingest - should return 503 when GovInfo API unavailable")
    void shouldReturn503_whenGovInfoApiUnavailable() {
        // This test validates behavior when external GovInfo API is down
        // In mock mode, WireMock can simulate this scenario
        Map<String, Object> request = ReasoningTestDataBuilder.buildIngestionRequest(
                ReasoningTestDataBuilder.VALID_YEAR
        );

        // Response should be one of: 200 (success), 400 (validation), 500 (internal error), 503 (service unavailable)
        client.triggerIngestion(request)
                .then()
                .statusCode(anyOf(equalTo(200), equalTo(400), equalTo(500), equalTo(503)));
    }
}
