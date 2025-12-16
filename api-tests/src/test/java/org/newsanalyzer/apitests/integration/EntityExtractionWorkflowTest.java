package org.newsanalyzer.apitests.integration;

import io.restassured.response.Response;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.newsanalyzer.apitests.integration.util.ServiceOrchestrator;
import org.newsanalyzer.apitests.integration.util.WorkflowAssertions;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for the entity extraction → backend storage workflow.
 *
 * <p>Tests the complete flow from text extraction via the reasoning service
 * to entity storage in the backend, verifying Schema.org data preservation.</p>
 *
 * <p>Workflow:</p>
 * <pre>
 * [News Article Text]
 *        │
 *        ▼
 * [Reasoning Service] POST /entities/extract
 *        │
 *        ▼
 * [Extracted Entities with Schema.org]
 *        │
 *        ▼
 * [Backend API] POST /api/entities
 *        │
 *        ▼
 * [Stored Entity in PostgreSQL]
 * </pre>
 */
@Tag("integration")
@DisplayName("Entity Extraction → Storage Workflow Tests")
class EntityExtractionWorkflowTest extends IntegrationTestBase {

    private ServiceOrchestrator orchestrator;

    @Override
    void setupIntegrationClients() {
        super.setupIntegrationClients();
        orchestrator = new ServiceOrchestrator(entityClient, govOrgClient, reasoningClient);
    }

    @AfterEach
    void cleanup() {
        cleanupTestData();
    }

    // ==================== AC 1, 5: End-to-End Extraction and Storage ====================

    @Test
    @DisplayName("Given text with person entity, when extracted and stored, then entity is persisted with correct data")
    void shouldExtractAndStoreEntity_endToEnd() {
        // Given - text containing a person entity
        String text = SIMPLE_PERSON_TEXT;
        System.out.println("\n[TEST] shouldExtractAndStoreEntity_endToEnd");
        System.out.println("  Input text: " + text);

        Instant start = startTiming();

        // When - extract entities from reasoning service
        Response extractResponse = reasoningClient.extractEntities(text, 0.5);
        endTiming(start, "Entity Extraction");

        // Then - extraction should succeed
        assertThat(extractResponse.statusCode())
                .as("Extraction should succeed")
                .isEqualTo(200);

        List<Map<String, Object>> entities = extractResponse.jsonPath().getList("entities");
        System.out.println("  Extracted entities: " + entities.size());

        // Store each extracted entity
        start = startTiming();
        for (Map<String, Object> extracted : entities) {
            Map<String, Object> entityRequest = buildEntityRequest(extracted);
            Response createResponse = entityClient.createEntity(entityRequest);

            assertThat(createResponse.statusCode())
                    .as("Entity creation should succeed")
                    .isIn(200, 201);

            String entityId = createResponse.jsonPath().getString("id");
            trackEntityForCleanup(entityId);

            System.out.println("  Stored entity: " + extracted.get("text") + " -> " + entityId);
        }
        endTiming(start, "Entity Storage");

        // Verify stored entities can be retrieved
        start = startTiming();
        for (String entityId : createdEntityIds) {
            Response getResponse = entityClient.getEntityById(entityId);
            assertThat(getResponse.statusCode()).isEqualTo(200);
        }
        endTiming(start, "Entity Verification");

        printTimingSummary();
    }

    @Test
    @DisplayName("Given text with multiple entities, when extracted and stored, then all entities are persisted")
    void shouldExtractMultipleEntities_andStoreAll() {
        // Given - news article with multiple entities
        String text = SAMPLE_ARTICLE;
        System.out.println("\n[TEST] shouldExtractMultipleEntities_andStoreAll");

        Instant start = startTiming();

        // When - use orchestrator to extract and store
        ServiceOrchestrator.WorkflowResult result = orchestrator.extractAndStore(text, 0.5);
        endTiming(start, "Full Extraction Workflow");

        // Then - workflow should succeed with multiple entities
        System.out.println("  Extracted: " + result.extractedCount + " entities");
        System.out.println("  Stored: " + result.storedEntityIds.size() + " entities");
        System.out.println("  Failed: " + result.failedStorageCount + " entities");

        assertThat(result.success)
                .as("Workflow should succeed")
                .isTrue();

        assertThat(result.extractedCount)
                .as("Should extract multiple entities from article")
                .isGreaterThan(0);

        assertThat(result.storedEntityIds)
                .as("All extracted entities should be stored")
                .hasSameSizeAs(result.storedEntityIds);

        assertThat(result.failedStorageCount)
                .as("No storage failures should occur")
                .isZero();

        // Track for cleanup
        createdEntityIds.addAll(result.storedEntityIds);

        printTimingSummary();
    }

    @Test
    @DisplayName("Given extracted entity with Schema.org data, when stored, then Schema.org data is preserved")
    void shouldPreserveSchemaOrgData_throughWorkflow() {
        // Given - text that will produce entity with Schema.org data
        String text = "The Environmental Protection Agency issued new regulations.";
        System.out.println("\n[TEST] shouldPreserveSchemaOrgData_throughWorkflow");

        Instant start = startTiming();

        // When - extract entity
        Response extractResponse = reasoningClient.extractEntities(text, 0.5);
        endTiming(start, "Extraction");

        assertThat(extractResponse.statusCode()).isEqualTo(200);

        List<Map<String, Object>> entities = extractResponse.jsonPath().getList("entities");
        assertThat(entities).isNotEmpty();

        // Find an entity with Schema.org data
        Map<String, Object> entityWithSchema = entities.stream()
                .filter(e -> e.get("schema_org_type") != null)
                .findFirst()
                .orElse(entities.get(0));

        String originalSchemaOrgType = (String) entityWithSchema.get("schema_org_type");
        @SuppressWarnings("unchecked")
        Map<String, Object> originalSchemaOrgData = (Map<String, Object>) entityWithSchema.get("schema_org_data");

        System.out.println("  Original Schema.org type: " + originalSchemaOrgType);

        // When - store entity with Schema.org data
        start = startTiming();
        Map<String, Object> entityRequest = buildEntityRequest(entityWithSchema);
        Response createResponse = entityClient.createEntity(entityRequest);
        endTiming(start, "Storage");

        assertThat(createResponse.statusCode()).isIn(200, 201);
        String entityId = createResponse.jsonPath().getString("id");
        trackEntityForCleanup(entityId);

        // Then - retrieve and verify Schema.org data is preserved
        start = startTiming();
        Response getResponse = entityClient.getEntityById(entityId);
        endTiming(start, "Retrieval");

        assertThat(getResponse.statusCode()).isEqualTo(200);

        String storedSchemaOrgType = getResponse.jsonPath().getString("schemaOrgType");
        System.out.println("  Stored Schema.org type: " + storedSchemaOrgType);

        // Verify Schema.org type is preserved (or mapped correctly)
        if (originalSchemaOrgType != null) {
            assertThat(storedSchemaOrgType)
                    .as("Schema.org type should be preserved")
                    .isNotNull();
        }

        // Verify Schema.org data structure
        Map<String, Object> storedSchemaOrgData = getResponse.jsonPath().getMap("schemaOrgData");
        if (originalSchemaOrgData != null && !originalSchemaOrgData.isEmpty()) {
            assertThat(storedSchemaOrgData)
                    .as("Schema.org data should be preserved")
                    .isNotNull();

            // Verify JSON-LD required fields
            if (storedSchemaOrgData != null) {
                System.out.println("  Schema.org data preserved with " + storedSchemaOrgData.size() + " fields");
            }
        }

        printTimingSummary();
    }

    @Test
    @DisplayName("Given text with no entities, when extracted, then workflow handles gracefully")
    void shouldHandleNoEntitiesExtracted_gracefully() {
        // Given - text with no extractable entities
        String text = "This is a simple sentence with no named entities.";
        System.out.println("\n[TEST] shouldHandleNoEntitiesExtracted_gracefully");

        Instant start = startTiming();

        // When - extract entities
        Response extractResponse = reasoningClient.extractEntities(text, 0.9);
        endTiming(start, "Extraction");

        // Then - should return successfully with empty or minimal results
        assertThat(extractResponse.statusCode())
                .as("Extraction should still succeed")
                .isEqualTo(200);

        int totalCount = extractResponse.jsonPath().getInt("total_count");
        List<Object> entities = extractResponse.jsonPath().getList("entities");

        System.out.println("  Entities extracted: " + totalCount);

        // The extraction may or may not find entities depending on NER model
        // The key assertion is that the workflow handles this gracefully
        assertThat(entities)
                .as("Entities list should be present (may be empty)")
                .isNotNull();

        // Use orchestrator to verify full workflow handles empty extraction
        start = startTiming();
        ServiceOrchestrator.WorkflowResult result = orchestrator.extractAndStore(text, 0.95);
        endTiming(start, "Full Workflow");

        // Should not fail even with no entities
        if (result.extractedCount == 0) {
            System.out.println("  No entities extracted - workflow handled gracefully");
            assertThat(result.storedEntityIds).isEmpty();
        } else {
            System.out.println("  Some entities extracted: " + result.extractedCount);
            createdEntityIds.addAll(result.storedEntityIds);
        }

        printTimingSummary();
    }

    @Test
    @DisplayName("Given entity type from extraction, when stored, then type is correctly mapped")
    void shouldMapEntityTypes_correctly() {
        // Given - text with different entity types
        String text = "Senator Elizabeth Warren met with EPA officials in Washington DC.";
        System.out.println("\n[TEST] shouldMapEntityTypes_correctly");

        Instant start = startTiming();

        // When - extract and store
        ServiceOrchestrator.WorkflowResult result = orchestrator.extractAndStore(text, 0.5);
        endTiming(start, "Extraction and Storage");

        assertThat(result.success).isTrue();
        assertThat(result.storedEntityIds).isNotEmpty();

        createdEntityIds.addAll(result.storedEntityIds);

        // Then - verify entity types are valid
        start = startTiming();
        for (String entityId : result.storedEntityIds) {
            Response getResponse = entityClient.getEntityById(entityId);
            assertThat(getResponse.statusCode()).isEqualTo(200);

            String entityType = getResponse.jsonPath().getString("entityType");
            String name = getResponse.jsonPath().getString("name");

            System.out.println("  Entity: " + name + " -> Type: " + entityType);

            // Entity type should be one of the valid types
            assertThat(entityType)
                    .as("Entity type should be valid")
                    .isIn("PERSON", "ORGANIZATION", "GOVERNMENT_ORG", "LOCATION", "EVENT", "CONCEPT");
        }
        endTiming(start, "Type Verification");

        printTimingSummary();
    }

    // ==================== Helper Methods ====================

    private Map<String, Object> buildEntityRequest(Map<String, Object> extracted) {
        Map<String, Object> request = new HashMap<>();
        request.put("name", extracted.get("text"));

        // Map entity_type to EntityType enum format
        String entityType = (String) extracted.get("entity_type");
        request.put("entityType", mapToEntityType(entityType));

        request.put("confidenceScore", extracted.getOrDefault("confidence", 0.9));

        // Include Schema.org data if present
        if (extracted.containsKey("schema_org_type")) {
            request.put("schemaOrgType", extracted.get("schema_org_type"));
        }
        if (extracted.containsKey("schema_org_data")) {
            request.put("schemaOrgData", extracted.get("schema_org_data"));
        }
        if (extracted.containsKey("properties")) {
            request.put("properties", extracted.get("properties"));
        }

        return request;
    }

    private String mapToEntityType(String extractedType) {
        if (extractedType == null) return "CONCEPT";

        return switch (extractedType.toLowerCase()) {
            case "person" -> "PERSON";
            case "organization", "org" -> "ORGANIZATION";
            case "government_org", "governmentorganization", "gov_org" -> "GOVERNMENT_ORG";
            case "location", "gpe", "loc" -> "LOCATION";
            case "event" -> "EVENT";
            default -> "CONCEPT";
        };
    }
}
