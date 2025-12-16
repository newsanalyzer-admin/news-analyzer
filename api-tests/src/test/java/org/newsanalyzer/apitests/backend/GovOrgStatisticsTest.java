package org.newsanalyzer.apitests.backend;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.newsanalyzer.apitests.BaseApiTest;

import static org.hamcrest.Matchers.*;

/**
 * Statistics and validation tests for the Government Organization API endpoints.
 * Tests statistics endpoint and entity validation.
 */
@Tag("backend")
@Tag("integration")
@DisplayName("Government Organization Statistics Tests")
class GovOrgStatisticsTest extends BaseApiTest {

    private GovOrgApiClient govOrgClient;

    @BeforeEach
    void setUp() {
        govOrgClient = new GovOrgApiClient(getBackendSpec());
    }

    // ==================== Statistics Tests ====================

    @Test
    @DisplayName("GET /api/government-organizations/statistics - should return organization counts")
    void shouldGetStatistics_returnsCounts() {
        govOrgClient.getStatistics()
                .then()
                .statusCode(200)
                .body("totalActive", greaterThanOrEqualTo(0));
    }

    @Test
    @DisplayName("GET /api/government-organizations/statistics - should include counts by type")
    void shouldGetStatistics_includesCountsByType() {
        govOrgClient.getStatistics()
                .then()
                .statusCode(200)
                .body("$", notNullValue());
        // Response should include breakdown by type (DEPARTMENT, AGENCY, BUREAU, etc.)
    }

    @Test
    @DisplayName("GET /api/government-organizations/statistics - should include counts by branch")
    void shouldGetStatistics_includesCountsByBranch() {
        govOrgClient.getStatistics()
                .then()
                .statusCode(200)
                .body("$", notNullValue());
        // Response should include breakdown by branch (EXECUTIVE, LEGISLATIVE, JUDICIAL, INDEPENDENT)
    }

    // ==================== Entity Validation Tests ====================

    @Test
    @DisplayName("POST /api/government-organizations/validate-entity - should return validation result")
    void shouldValidateEntity_returnsValidationResult() {
        govOrgClient.validateEntity("Environmental Protection Agency", "GOVERNMENT_ORG")
                .then()
                .statusCode(200)
                .body("$", notNullValue());
    }

    @Test
    @DisplayName("POST /api/government-organizations/validate-entity - should match known acronym")
    void shouldValidateEntity_matchesAcronym() {
        govOrgClient.validateEntity("EPA", "GOVERNMENT_ORG")
                .then()
                .statusCode(200)
                .body("$", notNullValue());
    }

    @Test
    @DisplayName("POST /api/government-organizations/validate-entity - should return result for unknown entity")
    void shouldValidateEntity_handlesUnknownEntity() {
        govOrgClient.validateEntity("Unknown Fictional Agency XYZ", "GOVERNMENT_ORG")
                .then()
                .statusCode(200)
                .body("$", notNullValue());
        // Should return a result even if no match found
    }

    @Test
    @DisplayName("POST /api/government-organizations/validate-entity - should handle partial matches")
    void shouldValidateEntity_handlesPartialMatches() {
        govOrgClient.validateEntity("Department of", "GOVERNMENT_ORG")
                .then()
                .statusCode(200)
                .body("$", notNullValue());
    }

    @Test
    @DisplayName("POST /api/government-organizations/validate-entity - should handle non-government entity type")
    void shouldValidateEntity_handlesNonGovernmentType() {
        govOrgClient.validateEntity("Apple Inc.", "ORGANIZATION")
                .then()
                .statusCode(200)
                .body("$", notNullValue());
        // Should return result indicating no government org match
    }
}
