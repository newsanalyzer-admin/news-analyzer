package org.newsanalyzer.apitests.reasoning;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

/**
 * Mock reasoning server using WireMock for unit-style API tests.
 * Provides stubbed responses for FastAPI reasoning service endpoints.
 * Response structures match the Pydantic models defined in the reasoning service.
 */
public class MockReasoningServer {

    private static final int DEFAULT_PORT = 8090;
    private final WireMockServer wireMockServer;

    public MockReasoningServer() {
        this(DEFAULT_PORT);
    }

    public MockReasoningServer(int port) {
        wireMockServer = new WireMockServer(WireMockConfiguration.options()
                .port(port)
                .usingFilesUnderClasspath("wiremock/reasoning"));
    }

    /**
     * Start the mock server.
     */
    public void start() {
        wireMockServer.start();
        WireMock.configureFor("localhost", wireMockServer.port());
    }

    /**
     * Stop the mock server.
     */
    public void stop() {
        wireMockServer.stop();
    }

    /**
     * Reset all stubs and request logs.
     */
    public void reset() {
        wireMockServer.resetAll();
    }

    /**
     * Get the base URL of the mock server.
     */
    public String getBaseUrl() {
        return wireMockServer.baseUrl();
    }

    /**
     * Get the port the server is running on.
     */
    public int getPort() {
        return wireMockServer.port();
    }

    // ==================== Health Check Stubs ====================

    /**
     * Stub GET / (root) to return service info.
     */
    public void stubRootEndpoint() {
        stubFor(get(urlEqualTo("/"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                            {
                                "service": "reasoning-service",
                                "version": "1.0.0",
                                "status": "running"
                            }
                            """)));
    }

    /**
     * Stub GET /health to return healthy status.
     */
    public void stubHealthCheck() {
        stubFor(get(urlEqualTo("/health"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                            {
                                "status": "healthy",
                                "ontology_loaded": true,
                                "triple_count": 1500,
                                "timestamp": "2024-01-15T10:30:00Z"
                            }
                            """)));
    }

    /**
     * Stub GET /health to return unhealthy status.
     */
    public void stubHealthCheckDown() {
        stubFor(get(urlEqualTo("/health"))
                .willReturn(aResponse()
                        .withStatus(503)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                            {
                                "status": "unhealthy",
                                "ontology_loaded": false,
                                "triple_count": 0,
                                "error": "Ontology failed to load"
                            }
                            """)));
    }

    // ==================== Entity Extraction Stubs ====================

    /**
     * Stub POST /entities/extract to return extracted entities.
     */
    public void stubEntityExtraction() {
        stubFor(post(urlEqualTo("/entities/extract"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                            {
                                "entities": [
                                    {
                                        "text": "Environmental Protection Agency",
                                        "type": "government_org",
                                        "confidence": 0.95,
                                        "start_offset": 4,
                                        "end_offset": 35,
                                        "schema_org_type": "GovernmentOrganization"
                                    },
                                    {
                                        "text": "Washington D.C.",
                                        "type": "location",
                                        "confidence": 0.92,
                                        "start_offset": 50,
                                        "end_offset": 65,
                                        "schema_org_type": "Place"
                                    }
                                ],
                                "text_length": 150,
                                "processing_time_ms": 45
                            }
                            """)));
    }

    /**
     * Stub POST /entities/extract to return empty results.
     */
    public void stubEntityExtractionEmpty() {
        stubFor(post(urlEqualTo("/entities/extract"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                            {
                                "entities": [],
                                "text_length": 20,
                                "processing_time_ms": 10
                            }
                            """)));
    }

    /**
     * Stub POST /entities/extract to return 400 for invalid request.
     */
    public void stubEntityExtractionBadRequest() {
        stubFor(post(urlEqualTo("/entities/extract"))
                .willReturn(aResponse()
                        .withStatus(400)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                            {
                                "detail": "Text field is required and cannot be empty"
                            }
                            """)));
    }

    // ==================== Entity Linking Stubs ====================

    /**
     * Stub POST /entities/link to return linked entities with statistics.
     */
    public void stubEntityLinking() {
        stubFor(post(urlEqualTo("/entities/link"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                            {
                                "linked_entities": [
                                    {
                                        "text": "EPA",
                                        "type": "government_org",
                                        "wikidata_id": "Q460190",
                                        "wikidata_label": "Environmental Protection Agency",
                                        "wikidata_description": "US government agency for environmental protection",
                                        "dbpedia_uri": "http://dbpedia.org/resource/Environmental_Protection_Agency",
                                        "linking_confidence": 0.95,
                                        "linking_status": "linked",
                                        "is_ambiguous": false,
                                        "needs_review": false
                                    }
                                ],
                                "statistics": {
                                    "total": 1,
                                    "linked": 1,
                                    "needs_review": 0,
                                    "not_found": 0,
                                    "errors": 0
                                }
                            }
                            """)));
    }

    /**
     * Stub POST /entities/link/single to return single linked entity.
     */
    public void stubSingleEntityLinking() {
        stubFor(post(urlEqualTo("/entities/link/single"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                            {
                                "text": "Environmental Protection Agency",
                                "type": "government_org",
                                "wikidata_id": "Q460190",
                                "wikidata_label": "Environmental Protection Agency",
                                "wikidata_description": "US government agency for environmental protection",
                                "dbpedia_uri": "http://dbpedia.org/resource/Environmental_Protection_Agency",
                                "linking_confidence": 0.95,
                                "linking_status": "linked",
                                "is_ambiguous": false,
                                "needs_review": false
                            }
                            """)));
    }

    /**
     * Stub POST /entities/link/single for ambiguous entity.
     */
    public void stubAmbiguousEntityLinking() {
        stubFor(post(urlEqualTo("/entities/link/single"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                            {
                                "text": "Washington",
                                "type": "location",
                                "linking_status": "needs_review",
                                "is_ambiguous": true,
                                "needs_review": true,
                                "candidates": [
                                    {
                                        "wikidata_id": "Q61",
                                        "label": "Washington, D.C.",
                                        "description": "Capital of the United States",
                                        "score": 0.75
                                    },
                                    {
                                        "wikidata_id": "Q1223",
                                        "label": "Washington (state)",
                                        "description": "State in the Pacific Northwest",
                                        "score": 0.70
                                    }
                                ]
                            }
                            """)));
    }

    /**
     * Stub POST /entities/link/single for not found entity.
     */
    public void stubEntityLinkingNotFound() {
        stubFor(post(urlEqualTo("/entities/link/single"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                            {
                                "text": "Unknown Entity XYZ",
                                "type": "organization",
                                "linking_status": "not_found",
                                "is_ambiguous": false,
                                "needs_review": false
                            }
                            """)));
    }

    /**
     * Stub POST /entities/link to return 503 for service unavailable.
     */
    public void stubEntityLinkingUnavailable() {
        stubFor(post(urlEqualTo("/entities/link"))
                .willReturn(aResponse()
                        .withStatus(503)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                            {
                                "detail": "Knowledge base service temporarily unavailable"
                            }
                            """)));
    }

    // ==================== OWL Reasoning Stubs ====================

    /**
     * Stub POST /entities/reason to return enriched entities.
     */
    public void stubOwlReasoning() {
        stubFor(post(urlEqualTo("/entities/reason"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                            {
                                "enriched_entities": [
                                    {
                                        "text": "EPA",
                                        "type": "government_org",
                                        "inferred_types": ["ExecutiveAgency", "RegulatoryAgency", "GovernmentOrganization"],
                                        "properties": {
                                            "govBranch": "EXECUTIVE",
                                            "regulates": "environmental_policy"
                                        }
                                    }
                                ],
                                "inferred_triples": 5,
                                "consistency_errors": []
                            }
                            """)));
    }

    /**
     * Stub POST /entities/reason with inference disabled.
     */
    public void stubOwlReasoningNoInference() {
        stubFor(post(urlEqualTo("/entities/reason"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                            {
                                "enriched_entities": [
                                    {
                                        "text": "EPA",
                                        "type": "government_org",
                                        "inferred_types": [],
                                        "properties": {}
                                    }
                                ],
                                "inferred_triples": 0,
                                "consistency_errors": []
                            }
                            """)));
    }

    /**
     * Stub POST /entities/reason with consistency errors.
     */
    public void stubOwlReasoningWithErrors() {
        stubFor(post(urlEqualTo("/entities/reason"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                            {
                                "enriched_entities": [],
                                "inferred_triples": 0,
                                "consistency_errors": [
                                    "Entity type conflict: cannot be both Person and Organization",
                                    "Missing required property: name"
                                ]
                            }
                            """)));
    }

    // ==================== SPARQL Query Stubs ====================

    /**
     * Stub POST /entities/query/sparql to return query results.
     */
    public void stubSparqlQuery() {
        stubFor(post(urlEqualTo("/entities/query/sparql"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                            {
                                "results": [
                                    {"org": "http://newsanalyzer.org/ontology#EPA", "label": "Environmental Protection Agency"},
                                    {"org": "http://newsanalyzer.org/ontology#FBI", "label": "Federal Bureau of Investigation"}
                                ],
                                "count": 2,
                                "query_time_ms": 25
                            }
                            """)));
    }

    /**
     * Stub POST /entities/query/sparql to return empty results.
     */
    public void stubSparqlQueryEmpty() {
        stubFor(post(urlEqualTo("/entities/query/sparql"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                            {
                                "results": [],
                                "count": 0,
                                "query_time_ms": 5
                            }
                            """)));
    }

    /**
     * Stub POST /entities/query/sparql to return 400 for invalid query.
     */
    public void stubSparqlQueryInvalid() {
        stubFor(post(urlEqualTo("/entities/query/sparql"))
                .willReturn(aResponse()
                        .withStatus(400)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                            {
                                "detail": "Invalid SPARQL query: Syntax error at line 1"
                            }
                            """)));
    }

    // ==================== Ontology Stats Stubs ====================

    /**
     * Stub GET /entities/ontology/stats to return statistics.
     */
    public void stubOntologyStats() {
        stubFor(get(urlEqualTo("/entities/ontology/stats"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                            {
                                "total_triples": 1500,
                                "classes": 45,
                                "properties": 120,
                                "individuals": 350,
                                "namespaces": ["http://newsanalyzer.org/ontology#", "http://schema.org/"]
                            }
                            """)));
    }

    // ==================== Government Organization Stubs ====================

    /**
     * Stub GET /government-orgs/health to return health status.
     */
    public void stubGovOrgsHealth() {
        stubFor(get(urlEqualTo("/government-orgs/health"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                            {
                                "status": "healthy",
                                "api_key_configured": true,
                                "cache_size": 150,
                                "last_sync": "2024-01-15T08:00:00Z"
                            }
                            """)));
    }

    /**
     * Stub GET /government-orgs/test-api-connection to return connection status.
     */
    public void stubGovOrgsApiConnection() {
        stubFor(get(urlEqualTo("/government-orgs/test-api-connection"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                            {
                                "status": "success",
                                "api_accessible": true,
                                "response_time_ms": 150,
                                "timestamp": "2024-01-15T10:30:00Z"
                            }
                            """)));
    }

    /**
     * Stub GET /government-orgs/test-api-connection when API is unavailable.
     */
    public void stubGovOrgsApiConnectionFailed() {
        stubFor(get(urlEqualTo("/government-orgs/test-api-connection"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                            {
                                "status": "error",
                                "api_accessible": false,
                                "error": "Connection timeout",
                                "timestamp": "2024-01-15T10:30:00Z"
                            }
                            """)));
    }

    /**
     * Stub POST /government-orgs/ingest to return ingestion result.
     */
    public void stubGovOrgsIngestion() {
        stubFor(post(urlEqualTo("/government-orgs/ingest"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                            {
                                "status": "success",
                                "year": 2024,
                                "total_organizations": 450,
                                "new_organizations": 5,
                                "updated_organizations": 12,
                                "processing_time_ms": 5000
                            }
                            """)));
    }

    /**
     * Stub POST /government-orgs/ingest for invalid year.
     */
    public void stubGovOrgsIngestionInvalidYear() {
        stubFor(post(urlEqualTo("/government-orgs/ingest"))
                .willReturn(aResponse()
                        .withStatus(400)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                            {
                                "detail": "Year must be between 1990 and current year"
                            }
                            """)));
    }

    /**
     * Stub POST /government-orgs/ingest when GovInfo API is unavailable.
     */
    public void stubGovOrgsIngestionUnavailable() {
        stubFor(post(urlEqualTo("/government-orgs/ingest"))
                .willReturn(aResponse()
                        .withStatus(503)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                            {
                                "detail": "GovInfo API is temporarily unavailable"
                            }
                            """)));
    }

    /**
     * Stub POST /government-orgs/process-package to return processing result.
     */
    public void stubGovOrgsProcessPackage() {
        stubFor(post(urlEqualTo("/government-orgs/process-package"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                            {
                                "status": "success",
                                "package_id": "GOVMAN-2024-001234",
                                "organizations_extracted": 15,
                                "processing_time_ms": 2500
                            }
                            """)));
    }

    /**
     * Stub GET /government-orgs/fetch-packages to return package list.
     */
    public void stubGovOrgsFetchPackages() {
        stubFor(get(urlPathEqualTo("/government-orgs/fetch-packages"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                            {
                                "year": 2024,
                                "packages": [
                                    {"package_id": "GOVMAN-2024-001234", "title": "Government Manual 2024"},
                                    {"package_id": "GOVMAN-2024-001235", "title": "Government Manual 2024 Supplement"}
                                ],
                                "count": 2,
                                "offset": 0,
                                "page_size": 10
                            }
                            """)));
    }

    /**
     * Stub POST /government-orgs/enrich-entity to return enrichment result.
     */
    public void stubGovOrgsEnrichEntity() {
        stubFor(post(urlEqualTo("/government-orgs/enrich-entity"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                            {
                                "entity_text": "EPA",
                                "entity_type": "government_org",
                                "confidence": 0.95,
                                "is_government_org": true,
                                "matched_organization": {
                                    "official_name": "Environmental Protection Agency",
                                    "acronym": "EPA",
                                    "organization_type": "AGENCY",
                                    "government_branch": "EXECUTIVE",
                                    "parent_organization": "None"
                                }
                            }
                            """)));
    }

    /**
     * Stub POST /government-orgs/enrich-entity for non-government entity.
     */
    public void stubGovOrgsEnrichNonGovEntity() {
        stubFor(post(urlEqualTo("/government-orgs/enrich-entity"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                            {
                                "entity_text": "Apple Inc.",
                                "entity_type": "organization",
                                "confidence": 0.85,
                                "is_government_org": false,
                                "matched_organization": null
                            }
                            """)));
    }

    // ==================== Error Stubs ====================

    /**
     * Stub any endpoint to return 500 Internal Server Error.
     */
    public void stubInternalServerError(String path) {
        stubFor(any(urlPathEqualTo(path))
                .willReturn(aResponse()
                        .withStatus(500)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                            {
                                "detail": "Internal server error"
                            }
                            """)));
    }

    /**
     * Stub any endpoint to return 503 Service Unavailable.
     */
    public void stubServiceUnavailable(String path) {
        stubFor(any(urlPathEqualTo(path))
                .willReturn(aResponse()
                        .withStatus(503)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                            {
                                "detail": "Service temporarily unavailable"
                            }
                            """)));
    }

    /**
     * Stub any endpoint to simulate timeout (delay).
     */
    public void stubTimeout(String path, int delayMillis) {
        stubFor(any(urlPathEqualTo(path))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withFixedDelay(delayMillis)));
    }

    // ==================== Setup All Stubs ====================

    /**
     * Configure all standard stubs for a typical test scenario.
     * Call this method in @BeforeEach for quick setup.
     */
    public void setupAllStubs() {
        stubRootEndpoint();
        stubHealthCheck();
        stubEntityExtraction();
        stubEntityLinking();
        stubSingleEntityLinking();
        stubOwlReasoning();
        stubSparqlQuery();
        stubOntologyStats();
        stubGovOrgsHealth();
        stubGovOrgsApiConnection();
        stubGovOrgsIngestion();
        stubGovOrgsProcessPackage();
        stubGovOrgsFetchPackages();
        stubGovOrgsEnrichEntity();
    }
}
