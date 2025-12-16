package org.newsanalyzer.apitests;

import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.newsanalyzer.apitests.config.RestAssuredConfiguration;
import org.newsanalyzer.apitests.config.TestConfig;

/**
 * Base class for all API tests.
 * Provides common setup, configuration, and utility methods.
 *
 * <p>Usage: Extend this class in your test classes to get automatic
 * REST Assured configuration and access to pre-configured request specs.</p>
 *
 * <p>Example:</p>
 * <pre>
 * class MyApiTest extends BaseApiTest {
 *     {@literal @}Test
 *     void shouldGetEntity() {
 *         given()
 *             .spec(getBackendSpec())
 *         .when()
 *             .get("/api/entities")
 *         .then()
 *             .statusCode(200);
 *     }
 * }
 * </pre>
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class BaseApiTest {

    @BeforeAll
    void setupRestAssured() {
        RestAssuredConfiguration.initialize();
        logTestConfiguration();
    }

    private void logTestConfiguration() {
        System.out.println("=".repeat(60));
        System.out.println("API Test Configuration");
        System.out.println("=".repeat(60));
        System.out.println("Profile: " + TestConfig.getActiveProfile());
        System.out.println("Backend URL: " + TestConfig.getBackendBaseUrl());
        System.out.println("Reasoning URL: " + TestConfig.getReasoningBaseUrl());
        System.out.println("Timeout: " + TestConfig.getTimeoutSeconds() + " seconds");
        System.out.println("=".repeat(60));
    }

    /**
     * Returns the pre-configured request specification for backend API calls.
     */
    protected RequestSpecification getBackendSpec() {
        return RestAssuredConfiguration.getBackendSpec();
    }

    /**
     * Returns the pre-configured request specification for reasoning service API calls.
     */
    protected RequestSpecification getReasoningSpec() {
        return RestAssuredConfiguration.getReasoningSpec();
    }

    /**
     * Returns the backend base URL from configuration.
     */
    protected String getBackendBaseUrl() {
        return TestConfig.getBackendBaseUrl();
    }

    /**
     * Returns the reasoning service base URL from configuration.
     */
    protected String getReasoningBaseUrl() {
        return TestConfig.getReasoningBaseUrl();
    }
}
