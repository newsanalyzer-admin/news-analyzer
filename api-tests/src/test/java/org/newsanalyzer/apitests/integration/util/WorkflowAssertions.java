package org.newsanalyzer.apitests.integration.util;

import io.restassured.response.Response;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Custom assertions for cross-service integration test verification.
 *
 * <p>Provides specialized assertions for verifying entity consistency,
 * Schema.org completeness, and external ID presence across services.</p>
 */
public final class WorkflowAssertions {

    private WorkflowAssertions() {
        // Utility class - prevent instantiation
    }

    // ==================== Entity Consistency Assertions ====================

    /**
     * Assert that an entity response from API matches expected values.
     *
     * @param response The API response containing entity data
     * @param expectedName Expected entity name
     * @param expectedType Expected entity type
     */
    public static void assertEntityMatches(Response response, String expectedName, String expectedType) {
        assertThat(response.statusCode())
                .as("Response should be successful")
                .isIn(200, 201);

        String actualName = response.jsonPath().getString("name");
        String actualType = response.jsonPath().getString("entityType");

        assertThat(actualName)
                .as("Entity name should match")
                .isEqualTo(expectedName);

        assertThat(actualType)
                .as("Entity type should match")
                .isEqualTo(expectedType);
    }

    /**
     * Assert that extracted entity data is consistent after storage.
     *
     * @param extractedEntity The entity from extraction response
     * @param storedEntity The entity retrieved from backend
     */
    public static void assertEntityConsistent(
            Map<String, Object> extractedEntity,
            Map<String, Object> storedEntity) {

        String extractedText = (String) extractedEntity.get("text");
        String storedName = (String) storedEntity.get("name");

        assertThat(storedName)
                .as("Stored entity name should match extracted text")
                .isEqualTo(extractedText);

        // Verify type consistency (with mapping)
        String extractedType = (String) extractedEntity.get("entity_type");
        String storedType = (String) storedEntity.get("entityType");

        assertThat(storedType)
                .as("Entity type should be consistent")
                .isNotNull();

        // Verify confidence is preserved
        Number extractedConfidence = (Number) extractedEntity.get("confidence");
        Number storedConfidence = (Number) storedEntity.get("confidenceScore");

        if (extractedConfidence != null && storedConfidence != null) {
            assertThat(storedConfidence.doubleValue())
                    .as("Confidence score should be preserved")
                    .isCloseTo(extractedConfidence.doubleValue(), org.assertj.core.data.Offset.offset(0.01));
        }
    }

    // ==================== Schema.org Assertions ====================

    /**
     * Assert that Schema.org data is complete in entity response.
     *
     * @param response The API response containing entity data
     */
    public static void assertSchemaOrgComplete(Response response) {
        assertThat(response.statusCode())
                .as("Response should be successful")
                .isIn(200, 201);

        String schemaOrgType = response.jsonPath().getString("schemaOrgType");
        assertThat(schemaOrgType)
                .as("Schema.org type should be present")
                .isNotNull()
                .isNotEmpty();

        Map<String, Object> schemaOrgData = response.jsonPath().getMap("schemaOrgData");
        assertThat(schemaOrgData)
                .as("Schema.org data should be present")
                .isNotNull();

        // Verify JSON-LD structure
        assertThat(schemaOrgData.get("@context"))
                .as("@context should be present in Schema.org data")
                .isNotNull();

        assertThat(schemaOrgData.get("@type"))
                .as("@type should be present in Schema.org data")
                .isNotNull();
    }

    /**
     * Assert Schema.org data from a map.
     *
     * @param entity Entity map with schemaOrgData
     */
    public static void assertSchemaOrgComplete(Map<String, Object> entity) {
        assertThat(entity.get("schemaOrgType"))
                .as("Schema.org type should be present")
                .isNotNull();

        @SuppressWarnings("unchecked")
        Map<String, Object> schemaOrgData = (Map<String, Object>) entity.get("schemaOrgData");

        if (schemaOrgData != null) {
            assertThat(schemaOrgData.get("@context"))
                    .as("@context should be present")
                    .isNotNull();
            assertThat(schemaOrgData.get("@type"))
                    .as("@type should be present")
                    .isNotNull();
        }
    }

    /**
     * Assert that Schema.org type matches expected value.
     *
     * @param response The API response
     * @param expectedType Expected Schema.org type (e.g., "Person", "GovernmentOrganization")
     */
    public static void assertSchemaOrgType(Response response, String expectedType) {
        String schemaOrgType = response.jsonPath().getString("schemaOrgType");
        assertThat(schemaOrgType)
                .as("Schema.org type should match expected")
                .isEqualTo(expectedType);
    }

    // ==================== External ID Assertions ====================

    /**
     * Assert that external IDs are present in entity response.
     *
     * @param response The API response containing entity data
     */
    public static void assertExternalIdsPresent(Response response) {
        Map<String, Object> externalIds = response.jsonPath().getMap("externalIds");

        assertThat(externalIds)
                .as("External IDs should be present")
                .isNotNull()
                .isNotEmpty();
    }

    /**
     * Assert that Wikidata ID is present.
     *
     * @param response The API response
     */
    public static void assertWikidataIdPresent(Response response) {
        Map<String, Object> externalIds = response.jsonPath().getMap("externalIds");

        assertThat(externalIds)
                .as("External IDs should be present")
                .isNotNull();

        Object wikidataId = externalIds.get("wikidata_id");
        assertThat(wikidataId)
                .as("Wikidata ID should be present in external IDs")
                .isNotNull();

        assertThat(wikidataId.toString())
                .as("Wikidata ID should start with Q")
                .startsWith("Q");
    }

    /**
     * Assert external IDs from linking result.
     *
     * @param linkingResult The linking result from orchestrator
     */
    public static void assertLinkingSuccessful(ServiceOrchestrator.LinkingResult linkingResult) {
        assertThat(linkingResult.success)
                .as("Linking should be successful")
                .isTrue();

        assertThat(linkingResult.linkingStatus)
                .as("Linking status should be 'linked'")
                .isEqualTo("linked");

        assertThat(linkingResult.linkingConfidence)
                .as("Linking confidence should be above threshold")
                .isGreaterThan(0.5);

        // At least one external ID should be present
        boolean hasExternalId = linkingResult.wikidataId != null || linkingResult.dbpediaUri != null;
        assertThat(hasExternalId)
                .as("At least one external ID should be present")
                .isTrue();
    }

    // ==================== Workflow Result Assertions ====================

    /**
     * Assert extraction workflow was successful.
     *
     * @param result The workflow result
     * @param minExpectedEntities Minimum number of expected entities
     */
    public static void assertExtractionSuccessful(
            ServiceOrchestrator.WorkflowResult result,
            int minExpectedEntities) {

        assertThat(result.success)
                .as("Extraction workflow should succeed")
                .isTrue();

        assertThat(result.extractedCount)
                .as("Should extract at least %d entities", minExpectedEntities)
                .isGreaterThanOrEqualTo(minExpectedEntities);

        assertThat(result.storedEntityIds)
                .as("All extracted entities should be stored")
                .hasSizeGreaterThanOrEqualTo(minExpectedEntities);

        assertThat(result.failedStorageCount)
                .as("No storage failures should occur")
                .isZero();
    }

    /**
     * Assert validation workflow was successful with gov org link.
     *
     * @param result The validation result
     */
    public static void assertValidationWithLink(ServiceOrchestrator.ValidationResult result) {
        assertThat(result.success)
                .as("Validation should succeed")
                .isTrue();

        assertThat(result.linkedGovOrgId)
                .as("Should be linked to a government organization")
                .isNotNull()
                .isNotEmpty();
    }

    /**
     * Assert validation workflow completed without finding a gov org match.
     *
     * @param result The validation result
     */
    public static void assertValidationWithoutLink(ServiceOrchestrator.ValidationResult result) {
        assertThat(result.success)
                .as("Validation should succeed even without match")
                .isTrue();

        // linkedGovOrgId may be null when no match found
    }

    /**
     * Assert enrichment workflow was successful.
     *
     * @param result The enrichment result
     */
    public static void assertEnrichmentSuccessful(ServiceOrchestrator.EnrichmentResult result) {
        assertThat(result.success)
                .as("Enrichment should succeed")
                .isTrue();

        assertThat(result.enrichedData)
                .as("Enriched data should be present")
                .isNotNull()
                .isNotEmpty();
    }

    /**
     * Assert full pipeline was successful.
     *
     * @param result The full pipeline result
     * @param minExpectedEntities Minimum expected entities processed
     */
    public static void assertFullPipelineSuccessful(
            ServiceOrchestrator.FullPipelineResult result,
            int minExpectedEntities) {

        assertThat(result.success)
                .as("Full pipeline should succeed")
                .isTrue();

        assertThat(result.extractionResult.storedEntityIds)
                .as("Should process at least %d entities", minExpectedEntities)
                .hasSizeGreaterThanOrEqualTo(minExpectedEntities);

        assertThat(result.entityResults)
                .as("Should have results for all entities")
                .hasSizeGreaterThanOrEqualTo(minExpectedEntities);
    }

    // ==================== Response Assertions ====================

    /**
     * Assert response indicates no entities were found/extracted.
     *
     * @param response The extraction response
     */
    public static void assertNoEntitiesExtracted(Response response) {
        assertThat(response.statusCode())
                .as("Extraction should still succeed")
                .isEqualTo(200);

        int totalCount = response.jsonPath().getInt("total_count");
        assertThat(totalCount)
                .as("No entities should be extracted")
                .isZero();

        List<Object> entities = response.jsonPath().getList("entities");
        assertThat(entities)
                .as("Entities list should be empty")
                .isEmpty();
    }

    /**
     * Assert response indicates a graceful failure.
     *
     * @param response The failed response
     * @param expectedStatus Expected HTTP status code
     */
    public static void assertGracefulFailure(Response response, int expectedStatus) {
        assertThat(response.statusCode())
                .as("Response should indicate failure")
                .isEqualTo(expectedStatus);

        // Should have error details
        String detail = response.jsonPath().getString("detail");
        if (detail == null) {
            detail = response.jsonPath().getString("message");
        }

        assertThat(detail)
                .as("Error message should be present")
                .isNotNull();
    }

    // ==================== Timing Assertions ====================

    /**
     * Assert response time is within threshold.
     *
     * @param response The API response
     * @param maxTimeMs Maximum acceptable time in milliseconds
     */
    public static void assertResponseTimeUnder(Response response, long maxTimeMs) {
        long responseTime = response.getTime();
        assertThat(responseTime)
                .as("Response time should be under %d ms", maxTimeMs)
                .isLessThan(maxTimeMs);
    }
}
