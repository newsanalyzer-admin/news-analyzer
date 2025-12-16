package org.newsanalyzer.apitests.config;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.config.HttpClientConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.filter.log.LogDetail;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;

/**
 * Configures REST Assured with base settings for API testing.
 * Provides pre-configured request specifications for backend and reasoning services.
 */
public class RestAssuredConfiguration {

    private static RequestSpecification backendSpec;
    private static RequestSpecification reasoningSpec;

    /**
     * Initializes REST Assured with global settings.
     * Call this in @BeforeAll of test classes.
     */
    public static void initialize() {
        // Enable logging on validation failure
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails(LogDetail.ALL);

        // Configure timeouts
        int timeoutMillis = TestConfig.getTimeoutSeconds() * 1000;
        RestAssured.config = RestAssuredConfig.config()
                .httpClient(HttpClientConfig.httpClientConfig()
                        .setParam("http.connection.timeout", timeoutMillis)
                        .setParam("http.socket.timeout", timeoutMillis));

        // Build request specifications
        buildRequestSpecs();
    }

    private static void buildRequestSpecs() {
        backendSpec = new RequestSpecBuilder()
                .setBaseUri(TestConfig.getBackendBaseUrl())
                .setContentType(ContentType.JSON)
                .setAccept(ContentType.JSON)
                .build();

        reasoningSpec = new RequestSpecBuilder()
                .setBaseUri(TestConfig.getReasoningBaseUrl())
                .setContentType(ContentType.JSON)
                .setAccept(ContentType.JSON)
                .build();
    }

    /**
     * Returns the request specification for backend API calls.
     */
    public static RequestSpecification getBackendSpec() {
        if (backendSpec == null) {
            initialize();
        }
        return backendSpec;
    }

    /**
     * Returns the request specification for reasoning service API calls.
     */
    public static RequestSpecification getReasoningSpec() {
        if (reasoningSpec == null) {
            initialize();
        }
        return reasoningSpec;
    }

    /**
     * Resets all configurations. Useful for tests that need fresh state.
     */
    public static void reset() {
        RestAssured.reset();
        backendSpec = null;
        reasoningSpec = null;
    }
}
