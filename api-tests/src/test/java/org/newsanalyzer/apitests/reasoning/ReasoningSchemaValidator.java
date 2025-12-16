package org.newsanalyzer.apitests.reasoning;

import io.restassured.module.jsv.JsonSchemaValidator;
import io.restassured.response.Response;

import static org.hamcrest.Matchers.*;

/**
 * Schema validation utilities for Reasoning Service API responses.
 * Provides methods to validate response structures against expected Pydantic models.
 */
public class ReasoningSchemaValidator {

    // ==================== Health Response Validation ====================

    /**
     * Validate health check response structure.
     * Expected: { status: string, ontology_loaded: boolean, triple_count: integer, timestamp?: string }
     */
    public static void validateHealthResponse(Response response) {
        response.then()
                .body("status", anyOf(equalTo("healthy"), equalTo("unhealthy")))
                .body("ontology_loaded", instanceOf(Boolean.class))
                .body("triple_count", instanceOf(Number.class));
    }

    /**
     * Validate root endpoint response structure.
     * Expected: { service: string, version: string, status: string }
     */
    public static void validateRootResponse(Response response) {
        response.then()
                .body("service", notNullValue())
                .body("version", notNullValue())
                .body("status", notNullValue());
    }

    // ==================== Entity Extraction Response Validation ====================

    /**
     * Validate entity extraction response structure.
     * Expected: { entities: array, text_length: integer, processing_time_ms: integer }
     */
    public static void validateExtractionResponse(Response response) {
        response.then()
                .body("entities", notNullValue())
                .body("text_length", instanceOf(Number.class))
                .body("processing_time_ms", instanceOf(Number.class));
    }

    /**
     * Validate extracted entity structure.
     * Expected: { text: string, type: string, confidence: number, start_offset: int, end_offset: int, schema_org_type?: string }
     */
    public static void validateExtractedEntity(Response response, int index) {
        String prefix = "entities[" + index + "].";
        response.then()
                .body(prefix + "text", notNullValue())
                .body(prefix + "type", notNullValue())
                .body(prefix + "confidence", instanceOf(Number.class))
                .body(prefix + "start_offset", instanceOf(Number.class))
                .body(prefix + "end_offset", instanceOf(Number.class));
    }

    /**
     * Validate all extracted entities have required fields.
     */
    public static void validateAllExtractedEntities(Response response) {
        response.then()
                .body("entities.text", everyItem(notNullValue()))
                .body("entities.type", everyItem(notNullValue()))
                .body("entities.confidence", everyItem(instanceOf(Number.class)));
    }

    // ==================== Entity Linking Response Validation ====================

    /**
     * Validate batch entity linking response structure.
     * Expected: { linked_entities: array, statistics: object }
     */
    public static void validateBatchLinkingResponse(Response response) {
        response.then()
                .body("linked_entities", notNullValue())
                .body("statistics", notNullValue());
    }

    /**
     * Validate linking statistics structure.
     * Expected: { total: int, linked: int, needs_review: int, not_found: int, errors: int }
     */
    public static void validateLinkingStatistics(Response response) {
        response.then()
                .body("statistics.total", instanceOf(Number.class))
                .body("statistics.linked", instanceOf(Number.class))
                .body("statistics.needs_review", instanceOf(Number.class))
                .body("statistics.not_found", instanceOf(Number.class))
                .body("statistics.errors", instanceOf(Number.class));
    }

    /**
     * Validate single linked entity response structure.
     * Expected: { text: string, type: string, linking_status: string, is_ambiguous: boolean, needs_review: boolean, ... }
     */
    public static void validateSingleLinkedEntity(Response response) {
        response.then()
                .body("text", notNullValue())
                .body("type", notNullValue())
                .body("linking_status", anyOf(
                        equalTo("linked"),
                        equalTo("needs_review"),
                        equalTo("not_found"),
                        equalTo("error")
                ))
                .body("is_ambiguous", instanceOf(Boolean.class))
                .body("needs_review", instanceOf(Boolean.class));
    }

    /**
     * Validate linked entity with Wikidata fields.
     */
    public static void validateWikidataLinkedEntity(Response response) {
        response.then()
                .body("wikidata_id", matchesPattern("Q\\d+"))
                .body("wikidata_label", notNullValue());
    }

    // ==================== OWL Reasoning Response Validation ====================

    /**
     * Validate OWL reasoning response structure.
     * Expected: { enriched_entities: array, inferred_triples: int, consistency_errors: array }
     */
    public static void validateReasoningResponse(Response response) {
        response.then()
                .body("enriched_entities", notNullValue())
                .body("inferred_triples", instanceOf(Number.class))
                .body("consistency_errors", notNullValue());
    }

    /**
     * Validate enriched entity structure.
     * Expected: { text: string, type: string, inferred_types: array, properties: object }
     */
    public static void validateEnrichedEntity(Response response, int index) {
        String prefix = "enriched_entities[" + index + "].";
        response.then()
                .body(prefix + "text", notNullValue())
                .body(prefix + "type", notNullValue())
                .body(prefix + "inferred_types", notNullValue())
                .body(prefix + "properties", notNullValue());
    }

    // ==================== SPARQL Query Response Validation ====================

    /**
     * Validate SPARQL query response structure.
     * Expected: { results: array, count: int, query_time_ms?: int }
     */
    public static void validateSparqlResponse(Response response) {
        response.then()
                .body("results", notNullValue())
                .body("count", instanceOf(Number.class));
    }

    // ==================== Ontology Stats Response Validation ====================

    /**
     * Validate ontology statistics response structure.
     * Expected: { total_triples: int, classes: int, properties: int, individuals: int, namespaces?: array }
     */
    public static void validateOntologyStatsResponse(Response response) {
        response.then()
                .body("total_triples", instanceOf(Number.class))
                .body("classes", instanceOf(Number.class))
                .body("properties", instanceOf(Number.class))
                .body("individuals", instanceOf(Number.class));
    }

    // ==================== Government Organization Response Validation ====================

    /**
     * Validate gov orgs health response structure.
     * Expected: { status: string, api_key_configured: boolean, ... }
     */
    public static void validateGovOrgsHealthResponse(Response response) {
        response.then()
                .body("status", equalTo("healthy"))
                .body("api_key_configured", instanceOf(Boolean.class));
    }

    /**
     * Validate API connection test response structure.
     * Expected: { status: string, api_accessible: boolean, timestamp: string }
     */
    public static void validateApiConnectionResponse(Response response) {
        response.then()
                .body("status", anyOf(equalTo("success"), equalTo("error")))
                .body("api_accessible", instanceOf(Boolean.class))
                .body("timestamp", notNullValue());
    }

    /**
     * Validate ingestion response structure.
     * Expected: { status: string, year: int, total_organizations: int, ... }
     */
    public static void validateIngestionResponse(Response response) {
        response.then()
                .body("status", notNullValue())
                .body("year", instanceOf(Number.class))
                .body("total_organizations", instanceOf(Number.class));
    }

    /**
     * Validate fetch packages response structure.
     * Expected: { year: int, packages: array, count: int, offset: int, page_size: int }
     */
    public static void validateFetchPackagesResponse(Response response) {
        response.then()
                .body("year", instanceOf(Number.class))
                .body("packages", notNullValue())
                .body("count", instanceOf(Number.class))
                .body("offset", instanceOf(Number.class))
                .body("page_size", instanceOf(Number.class));
    }

    /**
     * Validate entity enrichment response structure.
     * Expected: { entity_text: string, entity_type: string, confidence: number, is_government_org: boolean, ... }
     */
    public static void validateEnrichmentResponse(Response response) {
        response.then()
                .body("entity_text", notNullValue())
                .body("entity_type", notNullValue())
                .body("confidence", instanceOf(Number.class))
                .body("is_government_org", instanceOf(Boolean.class));
    }

    /**
     * Validate matched organization structure within enrichment response.
     */
    public static void validateMatchedOrganization(Response response) {
        response.then()
                .body("matched_organization.official_name", notNullValue())
                .body("matched_organization.organization_type", notNullValue())
                .body("matched_organization.government_branch", notNullValue());
    }

    // ==================== Error Response Validation ====================

    /**
     * Validate FastAPI error response structure.
     * Expected: { detail: string } or { detail: array }
     */
    public static void validateErrorResponse(Response response) {
        response.then()
                .body("detail", notNullValue());
    }

    /**
     * Validate validation error response (422).
     * Expected: { detail: [{ loc: array, msg: string, type: string }] }
     */
    public static void validateValidationErrorResponse(Response response) {
        response.then()
                .statusCode(422)
                .body("detail", notNullValue())
                .body("detail[0].msg", notNullValue());
    }

    // ==================== Schema.org JSON-LD Validation ====================

    /**
     * Validate Schema.org JSON-LD structure in entity.
     * Expected: { @context: string, @type: string, ... }
     */
    public static void validateSchemaOrgJsonLd(Response response, String path) {
        response.then()
                .body(path + ".@context", containsString("schema.org"))
                .body(path + ".@type", notNullValue());
    }

    /**
     * Validate Schema.org type is a valid type.
     */
    public static void validateSchemaOrgType(Response response, String path) {
        response.then()
                .body(path, anyOf(
                        equalTo("Person"),
                        equalTo("Organization"),
                        equalTo("GovernmentOrganization"),
                        equalTo("Place"),
                        equalTo("Event"),
                        equalTo("Thing")
                ));
    }

    // ==================== Utility Methods ====================

    /**
     * Validate response has expected content type.
     */
    public static void validateJsonContentType(Response response) {
        response.then()
                .contentType(containsString("application/json"));
    }

    /**
     * Validate response time is within acceptable limit.
     */
    public static void validateResponseTime(Response response, long maxMillis) {
        response.then()
                .time(lessThan(maxMillis));
    }

    /**
     * Validate array is not empty.
     */
    public static void validateNonEmptyArray(Response response, String path) {
        response.then()
                .body(path + ".size()", greaterThan(0));
    }

    /**
     * Validate array has expected size.
     */
    public static void validateArraySize(Response response, String path, int expectedSize) {
        response.then()
                .body(path + ".size()", equalTo(expectedSize));
    }
}
