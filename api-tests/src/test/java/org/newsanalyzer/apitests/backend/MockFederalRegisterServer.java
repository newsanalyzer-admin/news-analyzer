package org.newsanalyzer.apitests.backend;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

/**
 * Mock Federal Register API server using WireMock.
 *
 * Used for testing sync functionality with deterministic responses.
 * The backend must be configured to point to this mock server's URL
 * via the federal-register.base-url property.
 *
 * Usage:
 * 1. Start mock server: mockServer.start()
 * 2. Configure backend to use: mockServer.getBaseUrl() as federal-register.base-url
 * 3. Run tests
 * 4. Stop server: mockServer.stop()
 */
public class MockFederalRegisterServer {

    private static final int DEFAULT_PORT = 8091;
    private final WireMockServer wireMockServer;

    public MockFederalRegisterServer() {
        this(DEFAULT_PORT);
    }

    public MockFederalRegisterServer(int port) {
        wireMockServer = new WireMockServer(WireMockConfiguration.options()
                .port(port)
                .usingFilesUnderClasspath("wiremock/federal-register"));
    }

    /**
     * Create with dynamic port allocation.
     */
    public static MockFederalRegisterServer withDynamicPort() {
        WireMockServer server = new WireMockServer(WireMockConfiguration.options()
                .dynamicPort()
                .usingFilesUnderClasspath("wiremock/federal-register"));
        return new MockFederalRegisterServer(server);
    }

    private MockFederalRegisterServer(WireMockServer server) {
        this.wireMockServer = server;
    }

    /**
     * Start the mock server.
     */
    public void start() {
        wireMockServer.start();
        WireMock.configureFor("localhost", wireMockServer.port());
        stubDefaultAgencies();
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
        stubDefaultAgencies();
    }

    /**
     * Get the base URL of the mock server.
     * Use this as the federal-register.base-url in backend configuration.
     */
    public String getBaseUrl() {
        return wireMockServer.baseUrl();
    }

    /**
     * Get the API path (matches Federal Register API structure).
     */
    public String getApiPath() {
        return wireMockServer.baseUrl();
    }

    /**
     * Get the port the server is running on.
     */
    public int getPort() {
        return wireMockServer.port();
    }

    // ==================== Default Stubs ====================

    /**
     * Set up default agencies stub with sample data.
     */
    public void stubDefaultAgencies() {
        stubFor(get(urlEqualTo("/agencies"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(getSampleAgenciesJson())));
    }

    // ==================== Custom Stubs ====================

    /**
     * Stub agencies endpoint with custom agency list.
     */
    public void stubAgencies(String jsonBody) {
        wireMockServer.removeStub(get(urlEqualTo("/agencies")));
        stubFor(get(urlEqualTo("/agencies"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(jsonBody)));
    }

    /**
     * Stub agencies endpoint with JSON file from classpath.
     */
    public void stubAgenciesFromFile(String filename) {
        wireMockServer.removeStub(get(urlEqualTo("/agencies")));
        stubFor(get(urlEqualTo("/agencies"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile(filename)));
    }

    /**
     * Stub API unavailable (500 error).
     */
    public void stubApiUnavailable() {
        wireMockServer.removeStub(get(urlEqualTo("/agencies")));
        stubFor(get(urlEqualTo("/agencies"))
                .willReturn(aResponse()
                        .withStatus(500)
                        .withBody("Internal Server Error")));
    }

    /**
     * Stub API timeout.
     */
    public void stubApiTimeout(int delayMs) {
        wireMockServer.removeStub(get(urlEqualTo("/agencies")));
        stubFor(get(urlEqualTo("/agencies"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(getSampleAgenciesJson())
                        .withFixedDelay(delayMs)));
    }

    /**
     * Stub API returning empty agencies list.
     */
    public void stubEmptyAgencies() {
        wireMockServer.removeStub(get(urlEqualTo("/agencies")));
        stubFor(get(urlEqualTo("/agencies"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("[]")));
    }

    /**
     * Stub API returning 404 Not Found.
     */
    public void stubApiNotFound() {
        wireMockServer.removeStub(get(urlEqualTo("/agencies")));
        stubFor(get(urlEqualTo("/agencies"))
                .willReturn(aResponse()
                        .withStatus(404)
                        .withBody("Not Found")));
    }

    // ==================== Sample Data ====================

    /**
     * Get sample agencies JSON for tests.
     * Includes 5 representative agencies.
     */
    private String getSampleAgenciesJson() {
        return """
            [
                {
                    "id": 1,
                    "name": "Department of Agriculture",
                    "short_name": "USDA",
                    "description": "The Department of Agriculture provides leadership on food, agriculture, natural resources, rural development, nutrition, and related issues.",
                    "url": "https://www.usda.gov",
                    "parent_id": null,
                    "logo": null,
                    "recent_articles_url": "https://www.federalregister.gov/api/v1/agencies/agriculture-department/recent_articles.json",
                    "slug": "agriculture-department"
                },
                {
                    "id": 2,
                    "name": "Environmental Protection Agency",
                    "short_name": "EPA",
                    "description": "The Environmental Protection Agency protects human health and the environment.",
                    "url": "https://www.epa.gov",
                    "parent_id": null,
                    "logo": null,
                    "recent_articles_url": "https://www.federalregister.gov/api/v1/agencies/environmental-protection-agency/recent_articles.json",
                    "slug": "environmental-protection-agency"
                },
                {
                    "id": 3,
                    "name": "Department of Defense",
                    "short_name": "DOD",
                    "description": "The Department of Defense provides the military forces needed to deter war and ensure our nation's security.",
                    "url": "https://www.defense.gov",
                    "parent_id": null,
                    "logo": null,
                    "recent_articles_url": "https://www.federalregister.gov/api/v1/agencies/defense-department/recent_articles.json",
                    "slug": "defense-department"
                },
                {
                    "id": 4,
                    "name": "Department of State",
                    "short_name": "State",
                    "description": "The Department of State leads U.S. foreign policy.",
                    "url": "https://www.state.gov",
                    "parent_id": null,
                    "logo": null,
                    "recent_articles_url": "https://www.federalregister.gov/api/v1/agencies/state-department/recent_articles.json",
                    "slug": "state-department"
                },
                {
                    "id": 5,
                    "name": "National Aeronautics and Space Administration",
                    "short_name": "NASA",
                    "description": "NASA explores the unknown in air and space.",
                    "url": "https://www.nasa.gov",
                    "parent_id": null,
                    "logo": null,
                    "recent_articles_url": "https://www.federalregister.gov/api/v1/agencies/national-aeronautics-and-space-administration/recent_articles.json",
                    "slug": "national-aeronautics-and-space-administration"
                }
            ]
            """;
    }
}
