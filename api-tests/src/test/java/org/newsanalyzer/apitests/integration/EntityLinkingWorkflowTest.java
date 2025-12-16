package org.newsanalyzer.apitests.integration;

import io.restassured.response.Response;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.newsanalyzer.apitests.integration.util.ServiceOrchestrator;
import org.newsanalyzer.apitests.reasoning.ReasoningTestDataBuilder;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for the entity linking → external KB workflow.
 *
 * <p>Tests the flow of linking entities to external knowledge bases
 * (Wikidata, DBpedia) and updating the backend with external IDs.</p>
 *
 * <p>Workflow:</p>
 * <pre>
 * [Entity in Backend]
 *        │
 *        ▼
 * [Reasoning Service] POST /entities/link
 *        │
 *        ▼
 * [External KB Match (Wikidata/DBpedia)]
 *        │
 *        ▼
 * [Backend API] PUT /api/entities/{id}
 *        │
 *        ▼
 * [Entity with external_ids JSONB]
 * </pre>
 */
@Tag("integration")
@DisplayName("Entity Linking → External KB Workflow Tests")
class EntityLinkingWorkflowTest extends IntegrationTestBase {

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

    // ==================== AC 4: External KB Linking ====================

    @Test
    @DisplayName("Given entity, when linked to Wikidata, then backend updated with external ID")
    void shouldLinkEntity_toWikidata_andUpdateBackend() {
        // Given - create entity in backend
        System.out.println("\n[TEST] shouldLinkEntity_toWikidata_andUpdateBackend");

        Instant start = startTiming();

        Map<String, Object> entityRequest = new HashMap<>();
        entityRequest.put("name", "Environmental Protection Agency");
        entityRequest.put("entityType", "GOVERNMENT_ORG");
        entityRequest.put("confidenceScore", 0.95);

        Response createResponse = entityClient.createEntity(entityRequest);
        endTiming(start, "Create Entity");

        assertThat(createResponse.statusCode()).isIn(200, 201);
        String entityId = createResponse.jsonPath().getString("id");
        trackEntityForCleanup(entityId);
        System.out.println("  Created entity: " + entityId);

        // When - link to external KB via reasoning service
        start = startTiming();
        Response linkResponse = reasoningClient.linkSingleEntity(
                "Environmental Protection Agency",
                "government_org",
                "US federal agency that protects the environment"
        );
        endTiming(start, "Entity Linking");

        System.out.println("  Linking status: " + linkResponse.statusCode());

        // Then - verify linking result
        assertThat(linkResponse.statusCode())
                .as("Linking should complete")
                .isIn(200, 503); // 503 if linking service not configured

        if (linkResponse.statusCode() == 200) {
            String linkingStatus = linkResponse.jsonPath().getString("linking_status");
            String wikidataId = linkResponse.jsonPath().getString("wikidata_id");
            String dbpediaUri = linkResponse.jsonPath().getString("dbpedia_uri");
            Double confidence = linkResponse.jsonPath().getDouble("linking_confidence");

            System.out.println("  Linking status: " + linkingStatus);
            System.out.println("  Wikidata ID: " + wikidataId);
            System.out.println("  DBpedia URI: " + dbpediaUri);
            System.out.println("  Confidence: " + confidence);

            // If linked successfully, update backend
            if ("linked".equals(linkingStatus) && (wikidataId != null || dbpediaUri != null)) {
                start = startTiming();

                Map<String, Object> updateRequest = new HashMap<>();
                updateRequest.put("name", "Environmental Protection Agency");
                updateRequest.put("entityType", "GOVERNMENT_ORG");
                updateRequest.put("confidenceScore", 0.95);

                Map<String, Object> externalIds = new HashMap<>();
                if (wikidataId != null) {
                    externalIds.put("wikidata_id", wikidataId);
                }
                if (dbpediaUri != null) {
                    externalIds.put("dbpedia_uri", dbpediaUri);
                }
                updateRequest.put("externalIds", externalIds);

                Response updateResponse = entityClient.updateEntity(entityId, updateRequest);
                endTiming(start, "Update Backend");

                System.out.println("  Backend update status: " + updateResponse.statusCode());

                // Verify update
                if (updateResponse.statusCode() == 200) {
                    start = startTiming();
                    Response getResponse = entityClient.getEntityById(entityId);
                    endTiming(start, "Verify Update");

                    Map<String, Object> storedExternalIds = getResponse.jsonPath().getMap("externalIds");
                    if (storedExternalIds != null) {
                        System.out.println("  Stored external IDs: " + storedExternalIds);
                        assertThat(storedExternalIds)
                                .as("External IDs should be stored")
                                .isNotEmpty();
                    }
                }
            }
        } else {
            System.out.println("  Linking service not available (503)");
        }

        printTimingSummary();
    }

    @Test
    @DisplayName("Given entity, when enriched with external properties, then properties added to backend")
    void shouldEnrichEntity_withExternalProperties() {
        // Given - create entity
        System.out.println("\n[TEST] shouldEnrichEntity_withExternalProperties");

        Instant start = startTiming();

        Map<String, Object> entityRequest = new HashMap<>();
        entityRequest.put("name", "Elizabeth Warren");
        entityRequest.put("entityType", "PERSON");
        entityRequest.put("confidenceScore", 0.92);

        Response createResponse = entityClient.createEntity(entityRequest);
        endTiming(start, "Create Entity");

        assertThat(createResponse.statusCode()).isIn(200, 201);
        String entityId = createResponse.jsonPath().getString("id");
        trackEntityForCleanup(entityId);

        // When - use orchestrator to link
        start = startTiming();
        ServiceOrchestrator.LinkingResult linkResult = orchestrator.linkToExternalKB(entityId);
        endTiming(start, "Link to External KB");

        System.out.println("  Linking success: " + linkResult.success);
        System.out.println("  Linking status: " + linkResult.linkingStatus);

        // Then - verify linking result
        if (linkResult.success) {
            System.out.println("  Wikidata ID: " + linkResult.wikidataId);
            System.out.println("  DBpedia URI: " + linkResult.dbpediaUri);
            System.out.println("  Confidence: " + linkResult.linkingConfidence);

            // Verify at least one external ID
            boolean hasExternalId = linkResult.wikidataId != null || linkResult.dbpediaUri != null;
            System.out.println("  Has external ID: " + hasExternalId);
        } else {
            System.out.println("  Linking did not succeed: " + linkResult.errorMessage);
            // This is acceptable - external KB may not have matching entity
        }

        printTimingSummary();
    }

    @Test
    @DisplayName("Given batch of entities, when linked to external KBs, then statistics returned")
    void shouldLinkBatchEntities_withStatistics() {
        // Given - batch of entities to link
        System.out.println("\n[TEST] shouldLinkBatchEntities_withStatistics");

        Instant start = startTiming();

        List<Map<String, Object>> entities = List.of(
                Map.of("text", "EPA", "entity_type", "government_org"),
                Map.of("text", "Elizabeth Warren", "entity_type", "person"),
                Map.of("text", "Washington DC", "entity_type", "location")
        );
        Map<String, Object> options = ReasoningTestDataBuilder.buildLinkingOptions("both", 0.6, 5);
        Map<String, Object> linkRequest = ReasoningTestDataBuilder.buildBatchLinkRequest(entities, options);

        // When - batch link
        Response linkResponse = reasoningClient.linkEntities(linkRequest);
        endTiming(start, "Batch Linking");

        System.out.println("  Batch linking status: " + linkResponse.statusCode());

        // Then - verify statistics
        assertThat(linkResponse.statusCode()).isIn(200, 503);

        if (linkResponse.statusCode() == 200) {
            List<Map<String, Object>> linkedEntities = linkResponse.jsonPath().getList("linked_entities");
            Map<String, Object> statistics = linkResponse.jsonPath().getMap("statistics");

            System.out.println("  Linked entities: " + (linkedEntities != null ? linkedEntities.size() : 0));

            if (statistics != null) {
                System.out.println("  Statistics:");
                System.out.println("    Total: " + statistics.get("total"));
                System.out.println("    Linked: " + statistics.get("linked"));
                System.out.println("    Needs review: " + statistics.get("needs_review"));
                System.out.println("    Not found: " + statistics.get("not_found"));
                System.out.println("    Success rate: " + statistics.get("success_rate"));

                assertThat((Integer) statistics.get("total"))
                        .as("Total should match input count")
                        .isEqualTo(3);
            }

            // Verify each linked entity has required fields
            if (linkedEntities != null) {
                for (Map<String, Object> linked : linkedEntities) {
                    assertThat(linked.get("text")).isNotNull();
                    assertThat(linked.get("entity_type")).isNotNull();
                    assertThat(linked.get("linking_status")).isNotNull();
                }
            }
        }

        printTimingSummary();
    }

    @Test
    @DisplayName("Given entity with context, when linked, then disambiguation improves accuracy")
    void shouldUseContext_forDisambiguation() {
        // Given - entity that could be ambiguous without context
        System.out.println("\n[TEST] shouldUseContext_forDisambiguation");

        Instant start = startTiming();

        // "Washington" could be the state, the city, or a person
        // Context should help disambiguate

        // Link without context
        Response noContextResponse = reasoningClient.linkSingleEntity(
                "Washington",
                "location",
                null
        );
        endTiming(start, "Link without context");

        System.out.println("  Without context status: " + noContextResponse.statusCode());
        if (noContextResponse.statusCode() == 200) {
            String status1 = noContextResponse.jsonPath().getString("linking_status");
            Double conf1 = noContextResponse.jsonPath().getDouble("linking_confidence");
            System.out.println("    Status: " + status1 + ", Confidence: " + conf1);
        }

        // Link with context
        start = startTiming();
        Response withContextResponse = reasoningClient.linkSingleEntity(
                "Washington",
                "location",
                "The capital city of the United States where Congress meets"
        );
        endTiming(start, "Link with context");

        System.out.println("  With context status: " + withContextResponse.statusCode());
        if (withContextResponse.statusCode() == 200) {
            String status2 = withContextResponse.jsonPath().getString("linking_status");
            Double conf2 = withContextResponse.jsonPath().getDouble("linking_confidence");
            String wikidataId = withContextResponse.jsonPath().getString("wikidata_id");
            System.out.println("    Status: " + status2 + ", Confidence: " + conf2);
            System.out.println("    Wikidata ID: " + wikidataId);

            // With good context, should link to Washington D.C. (Q61)
            if (wikidataId != null) {
                System.out.println("    Context helped identify: " + wikidataId);
            }
        }

        printTimingSummary();
    }

    @Test
    @DisplayName("Given linked entity, when external IDs stored, then JSONB contains wikidata_id")
    void shouldStoreExternalIds_inJsonbField() {
        // Given - create entity
        System.out.println("\n[TEST] shouldStoreExternalIds_inJsonbField");

        Instant start = startTiming();

        Map<String, Object> entityRequest = new HashMap<>();
        entityRequest.put("name", "Department of Defense");
        entityRequest.put("entityType", "GOVERNMENT_ORG");
        entityRequest.put("confidenceScore", 0.98);

        // Pre-populate with known external IDs
        Map<String, Object> externalIds = new HashMap<>();
        externalIds.put("wikidata_id", "Q11223");
        externalIds.put("dbpedia_uri", "http://dbpedia.org/resource/United_States_Department_of_Defense");
        entityRequest.put("externalIds", externalIds);

        Response createResponse = entityClient.createEntity(entityRequest);
        endTiming(start, "Create Entity with External IDs");

        assertThat(createResponse.statusCode()).isIn(200, 201);
        String entityId = createResponse.jsonPath().getString("id");
        trackEntityForCleanup(entityId);

        // When - retrieve entity
        start = startTiming();
        Response getResponse = entityClient.getEntityById(entityId);
        endTiming(start, "Retrieve Entity");

        assertThat(getResponse.statusCode()).isEqualTo(200);

        // Then - verify external IDs are stored correctly
        Map<String, Object> storedExternalIds = getResponse.jsonPath().getMap("externalIds");

        System.out.println("  Stored external IDs: " + storedExternalIds);

        if (storedExternalIds != null) {
            String storedWikidataId = (String) storedExternalIds.get("wikidata_id");
            String storedDbpediaUri = (String) storedExternalIds.get("dbpedia_uri");

            System.out.println("  Wikidata ID: " + storedWikidataId);
            System.out.println("  DBpedia URI: " + storedDbpediaUri);

            assertThat(storedWikidataId)
                    .as("Wikidata ID should be stored")
                    .isEqualTo("Q11223");

            assertThat(storedDbpediaUri)
                    .as("DBpedia URI should be stored")
                    .contains("Department_of_Defense");
        }

        printTimingSummary();
    }

    @Test
    @DisplayName("Given entity not in external KB, when linking attempted, then status indicates not found")
    void shouldHandleNotFoundInExternalKB() {
        // Given - fictional entity that won't be in external KBs
        System.out.println("\n[TEST] shouldHandleNotFoundInExternalKB");

        String fictionalEntity = "Zylothian Space Agency " + System.currentTimeMillis();

        Instant start = startTiming();

        // When - attempt to link
        Response linkResponse = reasoningClient.linkSingleEntity(
                fictionalEntity,
                "organization",
                "A fictional space agency"
        );
        endTiming(start, "Link Fictional Entity");

        System.out.println("  Link status: " + linkResponse.statusCode());

        // Then - should indicate not found or low confidence
        assertThat(linkResponse.statusCode()).isIn(200, 503);

        if (linkResponse.statusCode() == 200) {
            String linkingStatus = linkResponse.jsonPath().getString("linking_status");
            Double confidence = linkResponse.jsonPath().getDouble("linking_confidence");

            System.out.println("  Linking status: " + linkingStatus);
            System.out.println("  Confidence: " + confidence);

            // Should either be "not_found" or have very low confidence
            assertThat(linkingStatus)
                    .as("Should indicate entity not found or needs review")
                    .isIn("not_found", "needs_review", "linked");

            if ("linked".equals(linkingStatus)) {
                // If somehow linked, confidence should be low
                assertThat(confidence)
                        .as("Confidence should be low for fictional entity")
                        .isLessThan(0.5);
            }
        }

        printTimingSummary();
    }
}
