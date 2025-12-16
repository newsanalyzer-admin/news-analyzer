package org.newsanalyzer.apitests.backend;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

/**
 * Mock backend server using WireMock for unit-style API tests.
 * Provides stubbed responses for backend API endpoints.
 */
public class MockBackendServer {

    private static final int DEFAULT_PORT = 8089;
    private final WireMockServer wireMockServer;

    public MockBackendServer() {
        this(DEFAULT_PORT);
    }

    public MockBackendServer(int port) {
        wireMockServer = new WireMockServer(WireMockConfiguration.options()
                .port(port)
                .usingFilesUnderClasspath("wiremock"));
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

    // ==================== Entity Stubs ====================

    /**
     * Stub GET /api/entities to return a list of entities.
     */
    public void stubGetAllEntities() {
        stubFor(get(urlEqualTo("/api/entities"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                            [
                                {
                                    "id": "11111111-1111-1111-1111-111111111111",
                                    "name": "Mock Entity 1",
                                    "entityType": "ORGANIZATION",
                                    "schemaOrgType": "Organization",
                                    "verified": false
                                },
                                {
                                    "id": "22222222-2222-2222-2222-222222222222",
                                    "name": "Mock Entity 2",
                                    "entityType": "PERSON",
                                    "schemaOrgType": "Person",
                                    "verified": true
                                }
                            ]
                            """)));
    }

    /**
     * Stub GET /api/entities/{id} to return a single entity.
     */
    public void stubGetEntityById(String id) {
        stubFor(get(urlPathEqualTo("/api/entities/" + id))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(String.format("""
                            {
                                "id": "%s",
                                "name": "Mock Entity",
                                "entityType": "ORGANIZATION",
                                "schemaOrgType": "Organization",
                                "verified": false
                            }
                            """, id))));
    }

    /**
     * Stub GET /api/entities/{id} to return 404.
     */
    public void stubEntityNotFound(String id) {
        stubFor(get(urlPathEqualTo("/api/entities/" + id))
                .willReturn(aResponse()
                        .withStatus(404)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                            {
                                "error": "Entity not found",
                                "message": "Entity with specified ID does not exist"
                            }
                            """)));
    }

    /**
     * Stub POST /api/entities to create an entity.
     */
    public void stubCreateEntity() {
        stubFor(post(urlEqualTo("/api/entities"))
                .willReturn(aResponse()
                        .withStatus(201)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                            {
                                "id": "33333333-3333-3333-3333-333333333333",
                                "name": "Created Entity",
                                "entityType": "ORGANIZATION",
                                "schemaOrgType": "Organization",
                                "verified": false
                            }
                            """)));
    }

    /**
     * Stub POST /api/entities to return 400 for invalid request.
     */
    public void stubCreateEntityBadRequest() {
        stubFor(post(urlEqualTo("/api/entities"))
                .willReturn(aResponse()
                        .withStatus(400)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                            {
                                "error": "Validation error",
                                "message": "Name is required"
                            }
                            """)));
    }

    // ==================== Government Organization Stubs ====================

    /**
     * Stub GET /api/government-organizations to return paginated list.
     */
    public void stubGetAllGovOrgs() {
        stubFor(get(urlPathEqualTo("/api/government-organizations"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                            {
                                "content": [
                                    {
                                        "id": "44444444-4444-4444-4444-444444444444",
                                        "officialName": "Mock Department",
                                        "acronym": "MD",
                                        "organizationType": "DEPARTMENT",
                                        "governmentBranch": "EXECUTIVE"
                                    }
                                ],
                                "pageable": {
                                    "pageNumber": 0,
                                    "pageSize": 20
                                },
                                "totalElements": 1,
                                "totalPages": 1
                            }
                            """)));
    }

    /**
     * Stub GET /api/government-organizations/{id} to return a single org.
     */
    public void stubGetGovOrgById(String id) {
        stubFor(get(urlPathEqualTo("/api/government-organizations/" + id))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(String.format("""
                            {
                                "id": "%s",
                                "officialName": "Mock Agency",
                                "acronym": "MA",
                                "organizationType": "AGENCY",
                                "governmentBranch": "INDEPENDENT"
                            }
                            """, id))));
    }

    /**
     * Stub GET /api/government-organizations/statistics.
     */
    public void stubGetGovOrgStatistics() {
        stubFor(get(urlEqualTo("/api/government-organizations/statistics"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                            {
                                "totalCount": 100,
                                "byType": {
                                    "DEPARTMENT": 15,
                                    "AGENCY": 50,
                                    "BUREAU": 35
                                },
                                "byBranch": {
                                    "EXECUTIVE": 80,
                                    "LEGISLATIVE": 10,
                                    "JUDICIAL": 5,
                                    "INDEPENDENT": 5
                                }
                            }
                            """)));
    }

    // ==================== Health Check Stubs ====================

    /**
     * Stub GET /actuator/health to return healthy status.
     */
    public void stubHealthCheck() {
        stubFor(get(urlEqualTo("/actuator/health"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                            {
                                "status": "UP"
                            }
                            """)));
    }

    /**
     * Stub GET /actuator/health to return unhealthy status.
     */
    public void stubHealthCheckDown() {
        stubFor(get(urlEqualTo("/actuator/health"))
                .willReturn(aResponse()
                        .withStatus(503)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                            {
                                "status": "DOWN"
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
                                "error": "Internal Server Error",
                                "message": "An unexpected error occurred"
                            }
                            """)));
    }

    /**
     * Stub any endpoint to simulate timeout (delay).
     */
    public void stubTimeout(String path, int delayMillis) {
        stubFor(get(urlPathEqualTo(path))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withFixedDelay(delayMillis)));
    }
}
