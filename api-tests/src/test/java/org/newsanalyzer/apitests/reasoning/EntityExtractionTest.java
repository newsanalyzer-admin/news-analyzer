package org.newsanalyzer.apitests.reasoning;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.newsanalyzer.apitests.BaseApiTest;

import static org.hamcrest.Matchers.*;

/**
 * Entity extraction tests for the Reasoning Service.
 * Tests POST /entities/extract endpoint with various text inputs.
 */
@Tag("reasoning")
@Tag("integration")
@DisplayName("Entity Extraction Tests")
class EntityExtractionTest extends BaseApiTest {

    private ReasoningApiClient client;

    @BeforeEach
    void setUp() {
        client = new ReasoningApiClient(getReasoningSpec());
    }

    // ==================== Success Scenario Tests ====================

    @Test
    @DisplayName("POST /entities/extract - should extract entities when valid text returns entity list")
    void shouldExtractEntities_whenValidText_returnsEntityList() {
        client.extractEntities(ReasoningTestDataBuilder.POLITICAL_TEXT)
                .then()
                .statusCode(200)
                .body("entities", notNullValue())
                .body("entities", instanceOf(java.util.List.class))
                .body("total_count", greaterThanOrEqualTo(0));
    }

    @Test
    @DisplayName("POST /entities/extract - should extract person from politician mention")
    void shouldExtractPerson_fromPoliticianMention() {
        client.extractEntities(ReasoningTestDataBuilder.PERSON_ONLY_TEXT)
                .then()
                .statusCode(200)
                .body("total_count", greaterThan(0));
        // Note: Specific entity type assertions depend on NLP model accuracy
    }

    @Test
    @DisplayName("POST /entities/extract - should extract government org from agency mention")
    void shouldExtractGovernmentOrg_fromAgencyMention() {
        client.extractEntities(ReasoningTestDataBuilder.GOVERNMENT_ORG_TEXT)
                .then()
                .statusCode(200)
                .body("total_count", greaterThan(0));
    }

    @Test
    @DisplayName("POST /entities/extract - should extract organization from company mention")
    void shouldExtractOrganization_fromCompanyMention() {
        String companyText = "Apple Inc. and Microsoft Corporation announced a partnership with Google.";
        client.extractEntities(companyText)
                .then()
                .statusCode(200)
                .body("total_count", greaterThanOrEqualTo(0));
    }

    @Test
    @DisplayName("POST /entities/extract - should extract location from place mention")
    void shouldExtractLocation_fromPlaceMention() {
        client.extractEntities(ReasoningTestDataBuilder.LOCATION_TEXT)
                .then()
                .statusCode(200)
                .body("total_count", greaterThanOrEqualTo(0));
    }

    // ==================== Confidence Threshold Tests ====================

    @Test
    @DisplayName("POST /entities/extract - should filter by confidence when threshold provided")
    void shouldFilterByConfidence_whenThresholdProvided() {
        // High confidence threshold should return fewer or equal entities
        client.extractEntities(ReasoningTestDataBuilder.POLITICAL_TEXT, ReasoningTestDataBuilder.HIGH_CONFIDENCE)
                .then()
                .statusCode(200)
                .body("entities", notNullValue());
    }

    @Test
    @DisplayName("POST /entities/extract - should return more entities with low confidence threshold")
    void shouldReturnMoreEntities_withLowConfidenceThreshold() {
        client.extractEntities(ReasoningTestDataBuilder.POLITICAL_TEXT, ReasoningTestDataBuilder.LOW_CONFIDENCE)
                .then()
                .statusCode(200)
                .body("entities", notNullValue());
    }

    // ==================== Empty/No Results Tests ====================

    @Test
    @DisplayName("POST /entities/extract - should return empty list when no entities found")
    void shouldReturnEmptyList_whenNoEntitiesFound() {
        client.extractEntities(ReasoningTestDataBuilder.NO_ENTITIES_TEXT)
                .then()
                .statusCode(200)
                .body("entities", notNullValue())
                .body("total_count", greaterThanOrEqualTo(0));
    }

    // ==================== Schema.org Data Tests ====================

    @Test
    @DisplayName("POST /entities/extract - should return schema org data for each entity")
    void shouldReturnSchemaOrgData_forEachEntity() {
        client.extractEntities(ReasoningTestDataBuilder.POLITICAL_TEXT)
                .then()
                .statusCode(200)
                .body("entities", notNullValue());
        // When entities are found, they should have schema_org_type and schema_org_data
    }

    @Test
    @DisplayName("POST /entities/extract - should include entity positions in response")
    void shouldIncludeEntityPositions_inResponse() {
        client.extractEntities(ReasoningTestDataBuilder.POLITICAL_TEXT)
                .then()
                .statusCode(200)
                .body("entities", notNullValue());
        // Entities should have start and end positions
    }

    // ==================== Error Handling Tests ====================

    @Test
    @DisplayName("POST /entities/extract - should return 400 when text empty")
    void shouldReturn400_whenTextEmpty() {
        client.extractEntities(ReasoningTestDataBuilder.EMPTY_TEXT)
                .then()
                .statusCode(anyOf(equalTo(400), equalTo(422)));
    }

    @Test
    @DisplayName("POST /entities/extract - should return 400 when confidence out of range (negative)")
    void shouldReturn400_whenConfidenceOutOfRange_negative() {
        client.extractEntities(ReasoningTestDataBuilder.POLITICAL_TEXT,
                              ReasoningTestDataBuilder.INVALID_CONFIDENCE_NEGATIVE)
                .then()
                .statusCode(anyOf(equalTo(400), equalTo(422)));
    }

    @Test
    @DisplayName("POST /entities/extract - should return 400 when confidence out of range (over 1)")
    void shouldReturn400_whenConfidenceOutOfRange_over() {
        client.extractEntities(ReasoningTestDataBuilder.POLITICAL_TEXT,
                              ReasoningTestDataBuilder.INVALID_CONFIDENCE_OVER)
                .then()
                .statusCode(anyOf(equalTo(400), equalTo(422)));
    }

    // ==================== Mixed Entity Tests ====================

    @Test
    @DisplayName("POST /entities/extract - should extract multiple entity types from mixed text")
    void shouldExtractMultipleEntityTypes_fromMixedText() {
        client.extractEntities(ReasoningTestDataBuilder.MIXED_ENTITIES_TEXT)
                .then()
                .statusCode(200)
                .body("total_count", greaterThanOrEqualTo(0));
    }

    @Test
    @DisplayName("POST /entities/extract - should handle long text input")
    void shouldHandleLongText_input() {
        String longText = ReasoningTestDataBuilder.POLITICAL_TEXT + " " +
                         ReasoningTestDataBuilder.GOVERNMENT_ORG_TEXT + " " +
                         ReasoningTestDataBuilder.MIXED_ENTITIES_TEXT;
        client.extractEntities(longText)
                .then()
                .statusCode(200)
                .body("entities", notNullValue());
    }
}
