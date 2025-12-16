package org.newsanalyzer.apitests.reasoning;

import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.newsanalyzer.apitests.config.Endpoints;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;

/**
 * API client helper for Reasoning Service operations.
 * Provides convenient methods for common Reasoning Service API calls.
 */
public class ReasoningApiClient {

    private final RequestSpecification spec;

    public ReasoningApiClient(RequestSpecification spec) {
        this.spec = spec;
    }

    // ==================== Health Endpoints ====================

    /**
     * Get root health check.
     */
    public Response getRootHealth() {
        return given()
                .spec(spec)
                .when()
                .get(Endpoints.Reasoning.ROOT);
    }

    /**
     * Get detailed health status.
     */
    public Response getHealth() {
        return given()
                .spec(spec)
                .when()
                .get(Endpoints.Reasoning.HEALTH);
    }

    /**
     * Get government orgs service health.
     */
    public Response getGovOrgsHealth() {
        return given()
                .spec(spec)
                .when()
                .get(Endpoints.Reasoning.GOV_ORGS_HEALTH);
    }

    // ==================== Entity Extraction Endpoints ====================

    /**
     * Extract entities from text.
     */
    public Response extractEntities(Map<String, Object> request) {
        return given()
                .spec(spec)
                .contentType(JSON)
                .body(request)
                .when()
                .post(Endpoints.Reasoning.ENTITIES_EXTRACT);
    }

    /**
     * Extract entities with text and confidence threshold.
     */
    public Response extractEntities(String text, double confidenceThreshold) {
        return extractEntities(ReasoningTestDataBuilder.buildExtractionRequest(text, confidenceThreshold));
    }

    /**
     * Extract entities with default confidence.
     */
    public Response extractEntities(String text) {
        return extractEntities(ReasoningTestDataBuilder.buildExtractionRequest(text));
    }

    // ==================== Entity Linking Endpoints ====================

    /**
     * Link batch of entities.
     */
    public Response linkEntities(Map<String, Object> request) {
        return given()
                .spec(spec)
                .contentType(JSON)
                .body(request)
                .when()
                .post(Endpoints.Reasoning.ENTITIES_LINK);
    }

    /**
     * Link a single entity.
     */
    public Response linkSingleEntity(Map<String, Object> request) {
        return given()
                .spec(spec)
                .contentType(JSON)
                .body(request)
                .when()
                .post(Endpoints.Reasoning.ENTITIES_LINK_SINGLE);
    }

    /**
     * Link a single entity with text, type, and context.
     */
    public Response linkSingleEntity(String text, String entityType, String context) {
        return linkSingleEntity(ReasoningTestDataBuilder.buildSingleLinkRequest(text, entityType, context));
    }

    // ==================== OWL Reasoning Endpoints ====================

    /**
     * Apply OWL reasoning to entities.
     */
    public Response reasonEntities(Map<String, Object> request) {
        return given()
                .spec(spec)
                .contentType(JSON)
                .body(request)
                .when()
                .post(Endpoints.Reasoning.ENTITIES_REASON);
    }

    // ==================== Ontology Statistics Endpoints ====================

    /**
     * Get ontology statistics.
     */
    public Response getOntologyStats() {
        return given()
                .spec(spec)
                .when()
                .get(Endpoints.Reasoning.ONTOLOGY_STATS);
    }

    // ==================== SPARQL Query Endpoints ====================

    /**
     * Execute SPARQL query.
     */
    public Response executeSparqlQuery(Map<String, Object> request) {
        return given()
                .spec(spec)
                .contentType(JSON)
                .body(request)
                .when()
                .post(Endpoints.Reasoning.SPARQL_QUERY);
    }

    /**
     * Execute SPARQL query with query string.
     */
    public Response executeSparqlQuery(String query) {
        return executeSparqlQuery(ReasoningTestDataBuilder.buildSparqlRequest(query));
    }

    // ==================== Government Organization Endpoints ====================

    /**
     * Trigger government manual ingestion.
     */
    public Response triggerIngestion(Map<String, Object> request) {
        return given()
                .spec(spec)
                .contentType(JSON)
                .body(request)
                .when()
                .post(Endpoints.Reasoning.GOV_ORGS_INGEST);
    }

    /**
     * Trigger ingestion for a specific year.
     */
    public Response triggerIngestion(int year) {
        return triggerIngestion(ReasoningTestDataBuilder.buildIngestionRequest(year));
    }

    /**
     * Process a single GovInfo package.
     */
    public Response processPackage(Map<String, Object> request) {
        return given()
                .spec(spec)
                .contentType(JSON)
                .body(request)
                .when()
                .post(Endpoints.Reasoning.GOV_ORGS_PROCESS_PACKAGE);
    }

    /**
     * Process a package by ID.
     */
    public Response processPackage(String packageId) {
        return processPackage(ReasoningTestDataBuilder.buildPackageProcessRequest(packageId));
    }

    /**
     * Fetch available packages from GovInfo.
     */
    public Response fetchPackages(int year, int pageSize, int offset) {
        return given()
                .spec(spec)
                .queryParam("year", year)
                .queryParam("page_size", pageSize)
                .queryParam("offset", offset)
                .when()
                .get(Endpoints.Reasoning.GOV_ORGS_FETCH_PACKAGES);
    }

    /**
     * Fetch packages with defaults.
     */
    public Response fetchPackages(int year) {
        return fetchPackages(year, 100, 0);
    }

    /**
     * Enrich entity with government org data.
     */
    public Response enrichEntity(Map<String, Object> request) {
        return given()
                .spec(spec)
                .contentType(JSON)
                .body(request)
                .when()
                .post(Endpoints.Reasoning.GOV_ORGS_ENRICH_ENTITY);
    }

    /**
     * Enrich entity with text, type, and confidence.
     */
    public Response enrichEntity(String entityText, String entityType, double confidence) {
        return enrichEntity(ReasoningTestDataBuilder.buildEnrichmentRequest(entityText, entityType, confidence));
    }

    /**
     * Test GovInfo API connection.
     */
    public Response testApiConnection() {
        return given()
                .spec(spec)
                .when()
                .get(Endpoints.Reasoning.GOV_ORGS_TEST_API_CONNECTION);
    }
}
