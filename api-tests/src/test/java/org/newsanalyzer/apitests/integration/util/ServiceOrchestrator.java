package org.newsanalyzer.apitests.integration.util;

import io.restassured.response.Response;
import org.newsanalyzer.apitests.backend.EntityApiClient;
import org.newsanalyzer.apitests.backend.GovOrgApiClient;
import org.newsanalyzer.apitests.reasoning.ReasoningApiClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Orchestrates cross-service workflows for integration tests.
 *
 * <p>Provides high-level methods that combine multiple service calls
 * into coherent workflows, simulating real-world usage patterns.</p>
 */
public class ServiceOrchestrator {

    private final EntityApiClient entityClient;
    private final GovOrgApiClient govOrgClient;
    private final ReasoningApiClient reasoningClient;

    public ServiceOrchestrator(
            EntityApiClient entityClient,
            GovOrgApiClient govOrgClient,
            ReasoningApiClient reasoningClient) {
        this.entityClient = entityClient;
        this.govOrgClient = govOrgClient;
        this.reasoningClient = reasoningClient;
    }

    // ==================== Extraction Workflow ====================

    /**
     * Extract entities from text and store them in the backend.
     *
     * @param text The text to extract entities from
     * @return WorkflowResult containing extracted and stored entity IDs
     */
    public WorkflowResult extractAndStore(String text) {
        return extractAndStore(text, 0.7);
    }

    /**
     * Extract entities from text with custom confidence threshold and store them.
     *
     * @param text The text to extract entities from
     * @param confidenceThreshold Minimum confidence for extraction
     * @return WorkflowResult containing extracted and stored entity IDs
     */
    public WorkflowResult extractAndStore(String text, double confidenceThreshold) {
        WorkflowResult result = new WorkflowResult();

        // Step 1: Extract entities from reasoning service
        Response extractResponse = reasoningClient.extractEntities(text, confidenceThreshold);
        result.extractionResponse = extractResponse;

        if (extractResponse.statusCode() != 200) {
            result.success = false;
            result.errorMessage = "Extraction failed with status: " + extractResponse.statusCode();
            return result;
        }

        List<Map<String, Object>> extractedEntities = extractResponse.jsonPath().getList("entities");
        result.extractedCount = extractedEntities != null ? extractedEntities.size() : 0;

        if (result.extractedCount == 0) {
            result.success = true;
            result.errorMessage = "No entities extracted";
            return result;
        }

        // Step 2: Store each entity in the backend
        for (Map<String, Object> extracted : extractedEntities) {
            Map<String, Object> entityRequest = buildEntityRequest(extracted);
            Response createResponse = entityClient.createEntity(entityRequest);

            if (createResponse.statusCode() == 201 || createResponse.statusCode() == 200) {
                String entityId = createResponse.jsonPath().getString("id");
                result.storedEntityIds.add(entityId);
            } else {
                result.failedStorageCount++;
            }
        }

        result.success = result.failedStorageCount == 0;
        return result;
    }

    // ==================== Validation Workflow ====================

    /**
     * Validate an entity against government organizations and link if matched.
     *
     * @param entityId The ID of the entity to validate
     * @return ValidationResult containing validation status and linked gov org
     */
    public ValidationResult validateAndLink(String entityId) {
        ValidationResult result = new ValidationResult();

        // Get entity first
        Response entityResponse = entityClient.getEntityById(entityId);
        if (entityResponse.statusCode() != 200) {
            result.success = false;
            result.errorMessage = "Entity not found: " + entityId;
            return result;
        }

        result.originalEntity = entityResponse.jsonPath().getMap("");

        // Validate the entity
        Response validateResponse = entityClient.validateEntity(java.util.UUID.fromString(entityId));
        result.validationResponse = validateResponse;

        if (validateResponse.statusCode() == 200) {
            result.success = true;
            result.linkedGovOrgId = validateResponse.jsonPath().getString("governmentOrganizationId");
            result.standardizedName = validateResponse.jsonPath().getString("standardizedName");
        } else {
            result.success = false;
            result.errorMessage = "Validation failed with status: " + validateResponse.statusCode();
        }

        return result;
    }

    // ==================== Reasoning Workflow ====================

    /**
     * Apply OWL reasoning to enrich an entity.
     *
     * @param entityId The ID of the entity to enrich
     * @return EnrichmentResult containing enriched data
     */
    public EnrichmentResult enrichWithReasoning(String entityId) {
        EnrichmentResult result = new EnrichmentResult();

        // Get entity from backend
        Response entityResponse = entityClient.getEntityById(entityId);
        if (entityResponse.statusCode() != 200) {
            result.success = false;
            result.errorMessage = "Entity not found: " + entityId;
            return result;
        }

        Map<String, Object> entity = entityResponse.jsonPath().getMap("");
        result.originalEntity = entity;

        // Build reasoning request
        Map<String, Object> reasoningRequest = new HashMap<>();
        List<Map<String, Object>> entities = new ArrayList<>();

        Map<String, Object> entityData = new HashMap<>();
        entityData.put("text", entity.get("name"));
        entityData.put("entity_type", entity.get("entityType"));
        entityData.put("confidence", entity.getOrDefault("confidenceScore", 0.9));
        entityData.put("properties", entity.getOrDefault("properties", Map.of()));
        entities.add(entityData);

        reasoningRequest.put("entities", entities);
        reasoningRequest.put("enable_inference", true);

        // Apply OWL reasoning
        Response reasoningResponse = reasoningClient.reasonEntities(reasoningRequest);
        result.reasoningResponse = reasoningResponse;

        if (reasoningResponse.statusCode() != 200) {
            result.success = false;
            result.errorMessage = "Reasoning failed with status: " + reasoningResponse.statusCode();
            return result;
        }

        List<Map<String, Object>> enrichedEntities = reasoningResponse.jsonPath().getList("enriched_entities");
        if (enrichedEntities != null && !enrichedEntities.isEmpty()) {
            result.enrichedData = enrichedEntities.get(0);
            result.inferredTriples = reasoningResponse.jsonPath().getInt("inferred_triples");
            result.success = true;
        }

        return result;
    }

    // ==================== Linking Workflow ====================

    /**
     * Link an entity to external knowledge bases.
     *
     * @param entityId The ID of the entity to link
     * @return LinkingResult containing linking status and external IDs
     */
    public LinkingResult linkToExternalKB(String entityId) {
        LinkingResult result = new LinkingResult();

        // Get entity from backend
        Response entityResponse = entityClient.getEntityById(entityId);
        if (entityResponse.statusCode() != 200) {
            result.success = false;
            result.errorMessage = "Entity not found: " + entityId;
            return result;
        }

        Map<String, Object> entity = entityResponse.jsonPath().getMap("");
        result.originalEntity = entity;

        String entityText = (String) entity.get("name");
        String entityType = (String) entity.get("entityType");

        // Link via reasoning service
        Response linkResponse = reasoningClient.linkSingleEntity(entityText, entityType.toLowerCase(), null);
        result.linkingResponse = linkResponse;

        if (linkResponse.statusCode() != 200) {
            result.success = false;
            result.errorMessage = "Linking failed with status: " + linkResponse.statusCode();
            return result;
        }

        result.wikidataId = linkResponse.jsonPath().getString("wikidata_id");
        result.dbpediaUri = linkResponse.jsonPath().getString("dbpedia_uri");
        result.linkingStatus = linkResponse.jsonPath().getString("linking_status");
        result.linkingConfidence = linkResponse.jsonPath().getDouble("linking_confidence");
        result.success = "linked".equals(result.linkingStatus);

        return result;
    }

    // ==================== Full Pipeline Workflow ====================

    /**
     * Execute full pipeline: extract → store → validate → link → reason.
     *
     * @param text The news article text
     * @return FullPipelineResult containing all workflow results
     */
    public FullPipelineResult processArticle(String text) {
        FullPipelineResult result = new FullPipelineResult();

        // Step 1: Extract and store
        WorkflowResult extractResult = extractAndStore(text);
        result.extractionResult = extractResult;

        if (!extractResult.success || extractResult.storedEntityIds.isEmpty()) {
            result.success = false;
            result.errorMessage = "Extraction or storage failed";
            return result;
        }

        // Process each stored entity
        for (String entityId : extractResult.storedEntityIds) {
            EntityPipelineResult entityResult = new EntityPipelineResult();
            entityResult.entityId = entityId;

            // Step 2: Validate and link to gov org (if applicable)
            ValidationResult validationResult = validateAndLink(entityId);
            entityResult.validationResult = validationResult;

            // Step 3: Link to external KB
            LinkingResult linkingResult = linkToExternalKB(entityId);
            entityResult.linkingResult = linkingResult;

            // Step 4: Apply OWL reasoning
            EnrichmentResult enrichmentResult = enrichWithReasoning(entityId);
            entityResult.enrichmentResult = enrichmentResult;

            result.entityResults.add(entityResult);
        }

        result.success = true;
        return result;
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

    // ==================== Result Classes ====================

    /**
     * Result of extract and store workflow.
     */
    public static class WorkflowResult {
        public boolean success;
        public String errorMessage;
        public Response extractionResponse;
        public int extractedCount;
        public List<String> storedEntityIds = new ArrayList<>();
        public int failedStorageCount;
    }

    /**
     * Result of validation workflow.
     */
    public static class ValidationResult {
        public boolean success;
        public String errorMessage;
        public Response validationResponse;
        public Map<String, Object> originalEntity;
        public String linkedGovOrgId;
        public String standardizedName;
    }

    /**
     * Result of OWL reasoning enrichment.
     */
    public static class EnrichmentResult {
        public boolean success;
        public String errorMessage;
        public Response reasoningResponse;
        public Map<String, Object> originalEntity;
        public Map<String, Object> enrichedData;
        public int inferredTriples;
    }

    /**
     * Result of external KB linking.
     */
    public static class LinkingResult {
        public boolean success;
        public String errorMessage;
        public Response linkingResponse;
        public Map<String, Object> originalEntity;
        public String wikidataId;
        public String dbpediaUri;
        public String linkingStatus;
        public double linkingConfidence;
    }

    /**
     * Result for a single entity in full pipeline.
     */
    public static class EntityPipelineResult {
        public String entityId;
        public ValidationResult validationResult;
        public LinkingResult linkingResult;
        public EnrichmentResult enrichmentResult;
    }

    /**
     * Result of full pipeline execution.
     */
    public static class FullPipelineResult {
        public boolean success;
        public String errorMessage;
        public WorkflowResult extractionResult;
        public List<EntityPipelineResult> entityResults = new ArrayList<>();
    }
}
