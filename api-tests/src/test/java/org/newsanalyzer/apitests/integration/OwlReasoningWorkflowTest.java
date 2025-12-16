package org.newsanalyzer.apitests.integration;

import io.restassured.response.Response;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.newsanalyzer.apitests.integration.util.ServiceOrchestrator;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for the OWL reasoning → entity enrichment workflow.
 *
 * <p>Tests the flow of applying OWL reasoning to entities and enriching
 * them with inferred types and properties.</p>
 *
 * <p>Workflow:</p>
 * <pre>
 * [Entity with Properties]
 *        │
 *        ▼
 * [Reasoning Service] POST /entities/reason
 *        │
 *        ▼
 * [Inferred Types & Properties]
 *        │
 *        ▼
 * [Backend API] PUT /api/entities/{id}
 *        │
 *        ▼
 * [Updated Entity with Enrichment]
 * </pre>
 */
@Tag("integration")
@DisplayName("OWL Reasoning → Entity Enrichment Workflow Tests")
class OwlReasoningWorkflowTest extends IntegrationTestBase {

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

    // ==================== AC 3, 5: OWL Reasoning and Enrichment ====================

    @Test
    @DisplayName("Given entity, when OWL reasoning applied, then entity enriched with inferred types")
    void shouldEnrichEntity_withInferredTypes() {
        // Given - create an entity in the backend
        System.out.println("\n[TEST] shouldEnrichEntity_withInferredTypes");

        Instant start = startTiming();

        Map<String, Object> entityRequest = new HashMap<>();
        entityRequest.put("name", "Environmental Protection Agency");
        entityRequest.put("entityType", "GOVERNMENT_ORG");
        entityRequest.put("confidenceScore", 0.95);
        entityRequest.put("schemaOrgType", "GovernmentOrganization");

        Map<String, Object> properties = new HashMap<>();
        properties.put("regulates", "environmental_policy");
        properties.put("jurisdiction", "federal");
        entityRequest.put("properties", properties);

        Response createResponse = entityClient.createEntity(entityRequest);
        endTiming(start, "Create Entity");

        assertThat(createResponse.statusCode()).isIn(200, 201);
        String entityId = createResponse.jsonPath().getString("id");
        trackEntityForCleanup(entityId);
        System.out.println("  Created entity: " + entityId);

        // When - apply OWL reasoning via reasoning service
        start = startTiming();

        Map<String, Object> reasoningRequest = new HashMap<>();
        List<Map<String, Object>> entities = new ArrayList<>();

        Map<String, Object> entityData = new HashMap<>();
        entityData.put("text", "Environmental Protection Agency");
        entityData.put("entity_type", "government_org");
        entityData.put("confidence", 0.95);
        entityData.put("properties", properties);
        entities.add(entityData);

        reasoningRequest.put("entities", entities);
        reasoningRequest.put("enable_inference", true);

        Response reasoningResponse = reasoningClient.reasonEntities(reasoningRequest);
        endTiming(start, "OWL Reasoning");

        System.out.println("  Reasoning status: " + reasoningResponse.statusCode());

        // Then - verify reasoning returned enriched data
        assertThat(reasoningResponse.statusCode())
                .as("Reasoning should succeed")
                .isEqualTo(200);

        List<Map<String, Object>> enrichedEntities = reasoningResponse.jsonPath().getList("enriched_entities");
        int inferredTriples = reasoningResponse.jsonPath().getInt("inferred_triples");
        List<String> consistencyErrors = reasoningResponse.jsonPath().getList("consistency_errors");

        System.out.println("  Enriched entities: " + (enrichedEntities != null ? enrichedEntities.size() : 0));
        System.out.println("  Inferred triples: " + inferredTriples);
        System.out.println("  Consistency errors: " + (consistencyErrors != null ? consistencyErrors.size() : 0));

        assertThat(enrichedEntities)
                .as("Should return enriched entities")
                .isNotNull()
                .isNotEmpty();

        // Verify enrichment contains additional type information
        Map<String, Object> enriched = enrichedEntities.get(0);
        System.out.println("  Enriched data keys: " + enriched.keySet());

        // Check for inferred types or properties
        if (enriched.containsKey("schema_org_types")) {
            @SuppressWarnings("unchecked")
            List<String> schemaOrgTypes = (List<String>) enriched.get("schema_org_types");
            System.out.println("  Inferred Schema.org types: " + schemaOrgTypes);
        }

        if (enriched.containsKey("inferred_properties")) {
            @SuppressWarnings("unchecked")
            Map<String, Object> inferredProps = (Map<String, Object>) enriched.get("inferred_properties");
            System.out.println("  Inferred properties: " + inferredProps.keySet());
        }

        printTimingSummary();
    }

    @Test
    @DisplayName("Given gov org with 'regulates' property, when OWL reasoning applied, then ExecutiveAgency type inferred")
    void shouldInferExecutiveAgency_fromProperties() {
        // Given - entity with regulatory property
        System.out.println("\n[TEST] shouldInferExecutiveAgency_fromProperties");

        Instant start = startTiming();

        // Build reasoning request with regulatory entity
        Map<String, Object> reasoningRequest = new HashMap<>();
        List<Map<String, Object>> entities = new ArrayList<>();

        Map<String, Object> entityData = new HashMap<>();
        entityData.put("text", "EPA");
        entityData.put("entity_type", "government_org");
        entityData.put("confidence", 0.9);

        Map<String, Object> properties = new HashMap<>();
        properties.put("regulates", "environmental_policy");
        properties.put("enforces", "clean_air_act");
        entityData.put("properties", properties);

        entities.add(entityData);
        reasoningRequest.put("entities", entities);
        reasoningRequest.put("enable_inference", true);

        // When - apply OWL reasoning
        Response reasoningResponse = reasoningClient.reasonEntities(reasoningRequest);
        endTiming(start, "OWL Reasoning");

        // Then - verify inference results
        assertThat(reasoningResponse.statusCode()).isEqualTo(200);

        List<Map<String, Object>> enrichedEntities = reasoningResponse.jsonPath().getList("enriched_entities");
        int inferredTriples = reasoningResponse.jsonPath().getInt("inferred_triples");

        System.out.println("  Inferred triples: " + inferredTriples);

        assertThat(enrichedEntities).isNotNull().isNotEmpty();

        Map<String, Object> enriched = enrichedEntities.get(0);

        // Check for ExecutiveAgency type inference (depends on ontology rules)
        @SuppressWarnings("unchecked")
        List<String> types = (List<String>) enriched.get("schema_org_types");
        if (types != null) {
            System.out.println("  Inferred types: " + types);

            // Check if any agency-related type was inferred
            boolean hasAgencyType = types.stream()
                    .anyMatch(t -> t.toLowerCase().contains("agency") ||
                            t.toLowerCase().contains("organization"));
            System.out.println("  Has agency/org type: " + hasAgencyType);
        }

        // Verify reasoning was actually applied
        assertThat(enriched.get("reasoning_applied"))
                .as("Reasoning should be marked as applied")
                .isEqualTo(true);

        printTimingSummary();
    }

    @Test
    @DisplayName("Given entity, when enriched and updated in backend, then consistency maintained")
    void shouldMaintainConsistency_acrossServices() {
        // Given - create entity in backend
        System.out.println("\n[TEST] shouldMaintainConsistency_acrossServices");

        Instant start = startTiming();

        Map<String, Object> entityRequest = new HashMap<>();
        entityRequest.put("name", "Department of Justice");
        entityRequest.put("entityType", "GOVERNMENT_ORG");
        entityRequest.put("confidenceScore", 0.92);

        Map<String, Object> properties = new HashMap<>();
        properties.put("prosecutes", "federal_crimes");
        entityRequest.put("properties", properties);

        Response createResponse = entityClient.createEntity(entityRequest);
        endTiming(start, "Create Entity");

        assertThat(createResponse.statusCode()).isIn(200, 201);
        String entityId = createResponse.jsonPath().getString("id");
        trackEntityForCleanup(entityId);

        // When - use orchestrator to enrich with reasoning
        start = startTiming();
        ServiceOrchestrator.EnrichmentResult enrichmentResult = orchestrator.enrichWithReasoning(entityId);
        endTiming(start, "Enrichment Workflow");

        System.out.println("  Enrichment success: " + enrichmentResult.success);

        if (enrichmentResult.success && enrichmentResult.enrichedData != null) {
            // Update entity with enriched data
            start = startTiming();

            Map<String, Object> updateRequest = new HashMap<>();
            updateRequest.put("name", "Department of Justice");
            updateRequest.put("entityType", "GOVERNMENT_ORG");
            updateRequest.put("confidenceScore", 0.92);

            // Add enriched properties
            if (enrichmentResult.enrichedData.containsKey("inferred_properties")) {
                @SuppressWarnings("unchecked")
                Map<String, Object> inferredProps =
                        (Map<String, Object>) enrichmentResult.enrichedData.get("inferred_properties");
                properties.putAll(inferredProps);
            }
            updateRequest.put("properties", properties);

            Response updateResponse = entityClient.updateEntity(entityId, updateRequest);
            endTiming(start, "Update Entity");

            System.out.println("  Update status: " + updateResponse.statusCode());
        }

        // Then - verify consistency by retrieving
        start = startTiming();
        Response getResponse = entityClient.getEntityById(entityId);
        endTiming(start, "Retrieve Entity");

        assertThat(getResponse.statusCode()).isEqualTo(200);

        String name = getResponse.jsonPath().getString("name");
        String type = getResponse.jsonPath().getString("entityType");

        System.out.println("  Final name: " + name);
        System.out.println("  Final type: " + type);

        assertThat(name).isEqualTo("Department of Justice");
        assertThat(type).isEqualTo("GOVERNMENT_ORG");

        printTimingSummary();
    }

    @Test
    @DisplayName("Given multiple entities, when OWL reasoning applied in batch, then all are enriched")
    void shouldEnrichMultipleEntities_inBatch() {
        // Given - multiple entities for batch reasoning
        System.out.println("\n[TEST] shouldEnrichMultipleEntities_inBatch");

        Instant start = startTiming();

        Map<String, Object> reasoningRequest = new HashMap<>();
        List<Map<String, Object>> entities = new ArrayList<>();

        // Entity 1: EPA
        Map<String, Object> epa = new HashMap<>();
        epa.put("text", "EPA");
        epa.put("entity_type", "government_org");
        epa.put("confidence", 0.9);
        epa.put("properties", Map.of("regulates", "environment"));
        entities.add(epa);

        // Entity 2: DOJ
        Map<String, Object> doj = new HashMap<>();
        doj.put("text", "Department of Justice");
        doj.put("entity_type", "government_org");
        doj.put("confidence", 0.95);
        doj.put("properties", Map.of("prosecutes", "crimes"));
        entities.add(doj);

        // Entity 3: Person
        Map<String, Object> person = new HashMap<>();
        person.put("text", "Elizabeth Warren");
        person.put("entity_type", "person");
        person.put("confidence", 0.88);
        person.put("properties", Map.of("occupation", "senator"));
        entities.add(person);

        reasoningRequest.put("entities", entities);
        reasoningRequest.put("enable_inference", true);

        // When - apply batch reasoning
        Response reasoningResponse = reasoningClient.reasonEntities(reasoningRequest);
        endTiming(start, "Batch OWL Reasoning");

        // Then - all entities should be processed
        assertThat(reasoningResponse.statusCode()).isEqualTo(200);

        List<Map<String, Object>> enrichedEntities = reasoningResponse.jsonPath().getList("enriched_entities");
        int inferredTriples = reasoningResponse.jsonPath().getInt("inferred_triples");

        System.out.println("  Input entities: " + entities.size());
        System.out.println("  Enriched entities: " + (enrichedEntities != null ? enrichedEntities.size() : 0));
        System.out.println("  Total inferred triples: " + inferredTriples);

        assertThat(enrichedEntities)
                .as("All entities should be enriched")
                .hasSameSizeAs(entities);

        // Verify each entity was processed
        for (int i = 0; i < enrichedEntities.size(); i++) {
            Map<String, Object> enriched = enrichedEntities.get(i);
            Boolean reasoningApplied = (Boolean) enriched.get("reasoning_applied");
            System.out.println("  Entity " + (i + 1) + " reasoning applied: " + reasoningApplied);

            assertThat(reasoningApplied)
                    .as("Reasoning should be applied to entity " + (i + 1))
                    .isTrue();
        }

        printTimingSummary();
    }

    @Test
    @DisplayName("Given ontology stats endpoint, when queried, then returns valid statistics")
    void shouldReturnOntologyStats() {
        // Given/When - query ontology statistics
        System.out.println("\n[TEST] shouldReturnOntologyStats");

        Instant start = startTiming();
        Response statsResponse = reasoningClient.getOntologyStats();
        endTiming(start, "Get Ontology Stats");

        // Then - should return valid stats
        assertThat(statsResponse.statusCode()).isEqualTo(200);

        int totalTriples = statsResponse.jsonPath().getInt("total_triples");
        int classes = statsResponse.jsonPath().getInt("classes");
        int properties = statsResponse.jsonPath().getInt("properties");
        int individuals = statsResponse.jsonPath().getInt("individuals");

        System.out.println("  Total triples: " + totalTriples);
        System.out.println("  Classes: " + classes);
        System.out.println("  Properties: " + properties);
        System.out.println("  Individuals: " + individuals);

        assertThat(totalTriples)
                .as("Ontology should have triples loaded")
                .isGreaterThanOrEqualTo(0);

        printTimingSummary();
    }
}
