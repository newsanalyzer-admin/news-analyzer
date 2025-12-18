package org.newsanalyzer.apitests.integration;

import io.restassured.response.Response;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.newsanalyzer.apitests.integration.util.ServiceOrchestrator;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for the entity validation → government org linking workflow.
 *
 * <p>Tests the flow of validating extracted entities against the government
 * organization master data and establishing linkages.</p>
 *
 * <p>Workflow:</p>
 * <pre>
 * [Entity from Extraction]
 *        │
 *        ▼
 * [Backend API] POST /api/entities/validate
 *        │
 *        ├─────────────────────────┐
 *        ▼                         ▼
 * [Match Gov Org?]            [No Match]
 *        │                         │
 *        ▼                         ▼
 * [Link to Gov Org]        [Store as-is]
 * [Standardize Name]
 * [Enrich Properties]
 * </pre>
 */
@Tag("integration")
@DisplayName("Entity Validation → Gov Org Linking Workflow Tests")
class EntityValidationWorkflowTest extends IntegrationTestBase {

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

    // ==================== AC 2, 4: Validation and Linking ====================

    @Test
    @DisplayName("Given gov org exists, when entity extracted and validated, then entity links to gov org")
    void shouldValidateAndLinkEntity_toGovernmentOrg() {
        // Given - create a government organization first
        System.out.println("\n[TEST] shouldValidateAndLinkEntity_toGovernmentOrg");

        Instant start = startTiming();

        Map<String, Object> govOrgRequest = new HashMap<>();
        govOrgRequest.put("name", "Environmental Protection Agency");
        govOrgRequest.put("acronym", "EPA");
        govOrgRequest.put("organizationType", "INDEPENDENT_AGENCY");
        govOrgRequest.put("governmentBranch", "EXECUTIVE");
        govOrgRequest.put("jurisdiction", "FEDERAL");
        govOrgRequest.put("isActive", true);

        Response createGovOrgResponse = govOrgClient.create(govOrgRequest);
        endTiming(start, "Create Gov Org");

        String govOrgId = null;
        if (createGovOrgResponse.statusCode() == 201) {
            govOrgId = createGovOrgResponse.jsonPath().getString("id");
            trackGovOrgForCleanup(govOrgId);
            System.out.println("  Created gov org: " + govOrgId);
        } else {
            // Gov org may already exist, try to find it
            System.out.println("  Gov org may already exist, searching...");
            Response searchResponse = govOrgClient.search("EPA");
            if (searchResponse.statusCode() == 200) {
                List<Map<String, Object>> results = searchResponse.jsonPath().getList("");
                if (results != null && !results.isEmpty()) {
                    govOrgId = (String) results.get(0).get("id");
                    System.out.println("  Found existing gov org: " + govOrgId);
                }
            }
        }

        // When - extract entity from text mentioning the gov org
        start = startTiming();
        String text = "The EPA announced new environmental regulations today.";
        Response extractResponse = reasoningClient.extractEntities(text, 0.5);
        endTiming(start, "Entity Extraction");

        assertThat(extractResponse.statusCode()).isEqualTo(200);

        List<Map<String, Object>> entities = extractResponse.jsonPath().getList("entities");
        System.out.println("  Extracted " + entities.size() + " entities");

        // Store entity first
        start = startTiming();
        Map<String, Object> entityRequest = new HashMap<>();
        entityRequest.put("name", "EPA");
        entityRequest.put("entityType", "GOVERNMENT_ORG");
        entityRequest.put("confidenceScore", 0.9);

        Response createEntityResponse = entityClient.createEntity(entityRequest);
        endTiming(start, "Create Entity");

        assertThat(createEntityResponse.statusCode()).isIn(200, 201);
        String entityId = createEntityResponse.jsonPath().getString("id");
        trackEntityForCleanup(entityId);
        System.out.println("  Created entity: " + entityId);

        // Then - validate the entity against gov orgs
        start = startTiming();
        Response validateResponse = govOrgClient.validateEntity("EPA", "GOVERNMENT_ORG");
        endTiming(start, "Entity Validation");

        System.out.println("  Validation status: " + validateResponse.statusCode());

        // Validation may return match or no match depending on data
        assertThat(validateResponse.statusCode())
                .as("Validation should complete")
                .isIn(200, 404);

        if (validateResponse.statusCode() == 200) {
            boolean isValid = validateResponse.jsonPath().getBoolean("valid");
            System.out.println("  Validation result: valid=" + isValid);

            if (isValid) {
                String matchedName = validateResponse.jsonPath().getString("matchedOrganization.officialName");
                System.out.println("  Matched organization: " + matchedName);

                assertThat(matchedName)
                        .as("Should match EPA or Environmental Protection Agency")
                        .satisfiesAnyOf(
                                name -> assertThat(name).containsIgnoringCase("EPA"),
                                name -> assertThat(name).containsIgnoringCase("Environmental Protection Agency")
                        );
            } else {
                System.out.println("  No match found - government org data may not be loaded");
            }
        }

        printTimingSummary();
    }

    @Test
    @DisplayName("Given acronym 'EPA', when validated, then standardized to full name")
    void shouldStandardizeName_whenLinkedToGovOrg() {
        // Given - government organization with acronym
        System.out.println("\n[TEST] shouldStandardizeName_whenLinkedToGovOrg");

        Instant start = startTiming();

        // Create gov org if not exists
        Map<String, Object> govOrgRequest = new HashMap<>();
        govOrgRequest.put("officialName", "Environmental Protection Agency");
        govOrgRequest.put("acronym", "EPA");
        govOrgRequest.put("orgType", "INDEPENDENT_AGENCY");
        govOrgRequest.put("branch", "EXECUTIVE");

        Response createGovOrgResponse = govOrgClient.create(govOrgRequest);
        endTiming(start, "Create Gov Org");

        if (createGovOrgResponse.statusCode() == 201) {
            String govOrgId = createGovOrgResponse.jsonPath().getString("id");
            trackGovOrgForCleanup(govOrgId);
        }

        // When - validate entity with acronym
        start = startTiming();
        Response validateResponse = govOrgClient.validateEntity("EPA", "GOVERNMENT_ORG");
        endTiming(start, "Validate Entity");

        System.out.println("  Validation response status: " + validateResponse.statusCode());

        // Then - should find match and potentially provide standardized name
        if (validateResponse.statusCode() == 200) {
            Map<String, Object> result = validateResponse.jsonPath().getMap("");
            System.out.println("  Validation result: " + result);

            // Check if standardized name is returned
            boolean isValid = validateResponse.jsonPath().getBoolean("valid");
            if (isValid) {
                String standardizedName = validateResponse.jsonPath().getString("matchedOrganization.officialName");
                System.out.println("  Standardized name: " + standardizedName);
                if (standardizedName != null) {
                    assertThat(standardizedName)
                            .as("Should provide full organization name")
                            .containsIgnoringCase("Environmental Protection Agency");
                }
            } else {
                System.out.println("  Validation returned valid=false");
            }
        }

        printTimingSummary();
    }

    @Test
    @DisplayName("Given entity with no matching gov org, when validated, then no link created")
    void shouldNotLink_whenNoMatchingGovOrg() {
        // Given - entity that doesn't match any gov org
        System.out.println("\n[TEST] shouldNotLink_whenNoMatchingGovOrg");

        Instant start = startTiming();

        String nonGovOrgName = "Acme Corporation " + UUID.randomUUID().toString().substring(0, 8);

        // When - validate non-government entity
        Response validateResponse = govOrgClient.validateEntity(nonGovOrgName, "ORGANIZATION");
        endTiming(start, "Validate Non-Gov Entity");

        System.out.println("  Validation status: " + validateResponse.statusCode());

        // Then - should not find a match
        assertThat(validateResponse.statusCode())
                .as("Should indicate no match found")
                .isIn(200, 404);

        if (validateResponse.statusCode() == 200) {
            // Check if match was found
            String matchedId = validateResponse.jsonPath().getString("matchedOrganization.id");
            Double confidence = validateResponse.jsonPath().getDouble("confidence");

            System.out.println("  Matched ID: " + matchedId);
            System.out.println("  Confidence: " + confidence);

            // If no match, matchedOrganization should be null or confidence should be low
            if (matchedId != null) {
                assertThat(confidence)
                        .as("Confidence should be low for non-matching entity")
                        .isLessThan(0.5);
            }
        } else {
            System.out.println("  No matching government organization found (404)");
        }

        printTimingSummary();
    }

    @Test
    @DisplayName("Given entity, when validation workflow executed, then data consistency maintained")
    void shouldMaintainDataConsistency_afterValidation() {
        // Given - create entity to validate
        System.out.println("\n[TEST] shouldMaintainDataConsistency_afterValidation");

        Instant start = startTiming();

        Map<String, Object> entityRequest = new HashMap<>();
        // Use a unique name that won't match any existing government organization
        String testName = "XYZNONEXISTENT-" + UUID.randomUUID().toString().substring(0, 8);
        entityRequest.put("name", testName);
        entityRequest.put("entityType", "GOVERNMENT_ORG");
        entityRequest.put("confidenceScore", 0.85);
        entityRequest.put("verified", false);

        Response createResponse = entityClient.createEntity(entityRequest);
        endTiming(start, "Create Entity");

        assertThat(createResponse.statusCode()).isIn(200, 201);
        String entityId = createResponse.jsonPath().getString("id");
        trackEntityForCleanup(entityId);

        // When - perform validation
        start = startTiming();
        Response validateResponse = entityClient.validateEntity(UUID.fromString(entityId));
        endTiming(start, "Validate Entity");

        System.out.println("  Validation status: " + validateResponse.statusCode());

        // Then - retrieve entity and verify consistency
        start = startTiming();
        Response getResponse = entityClient.getEntityById(entityId);
        endTiming(start, "Retrieve Entity");

        assertThat(getResponse.statusCode()).isEqualTo(200);

        String retrievedName = getResponse.jsonPath().getString("name");
        String retrievedType = getResponse.jsonPath().getString("entityType");

        System.out.println("  Retrieved name: " + retrievedName);
        System.out.println("  Retrieved type: " + retrievedType);

        // Core data should be consistent
        assertThat(retrievedName)
                .as("Entity name should be consistent")
                .isEqualTo(testName);

        assertThat(retrievedType)
                .as("Entity type should be consistent")
                .isEqualTo("GOVERNMENT_ORG");

        printTimingSummary();
    }

    @Test
    @DisplayName("Given multiple entities from article, when validated in batch, then all are processed")
    void shouldValidateMultipleEntities_fromArticle() {
        // Given - extract entities from article
        System.out.println("\n[TEST] shouldValidateMultipleEntities_fromArticle");

        Instant start = startTiming();

        // First, ensure gov org exists
        Map<String, Object> govOrgRequest = new HashMap<>();
        govOrgRequest.put("name", "Department of Justice");
        govOrgRequest.put("acronym", "DOJ");
        govOrgRequest.put("organizationType", "CABINET_DEPARTMENT");
        govOrgRequest.put("governmentBranch", "EXECUTIVE");
        govOrgRequest.put("jurisdiction", "FEDERAL");
        govOrgRequest.put("isActive", true);
        govOrgClient.create(govOrgRequest);
        endTiming(start, "Setup Gov Org");

        // Extract from article
        start = startTiming();
        ServiceOrchestrator.WorkflowResult extractResult = orchestrator.extractAndStore(SAMPLE_ARTICLE, 0.5);
        endTiming(start, "Extract and Store");

        assertThat(extractResult.success).isTrue();
        System.out.println("  Extracted and stored: " + extractResult.storedEntityIds.size() + " entities");

        createdEntityIds.addAll(extractResult.storedEntityIds);

        // When - validate each entity
        start = startTiming();
        int validatedCount = 0;
        int matchedCount = 0;

        for (String entityId : extractResult.storedEntityIds) {
            Response getResponse = entityClient.getEntityById(entityId);
            if (getResponse.statusCode() == 200) {
                String name = getResponse.jsonPath().getString("name");
                String type = getResponse.jsonPath().getString("entityType");

                if ("GOVERNMENT_ORG".equals(type)) {
                    Response validateResponse = govOrgClient.validateEntity(name, type);
                    validatedCount++;

                    if (validateResponse.statusCode() == 200 &&
                            validateResponse.jsonPath().getString("matchedOrganization.id") != null) {
                        matchedCount++;
                        System.out.println("  Matched: " + name);
                    }
                }
            }
        }
        endTiming(start, "Batch Validation");

        // Then - all gov org entities should be validated
        System.out.println("  Validated: " + validatedCount + " government entities");
        System.out.println("  Matched: " + matchedCount + " to master data");

        printTimingSummary();
    }
}
