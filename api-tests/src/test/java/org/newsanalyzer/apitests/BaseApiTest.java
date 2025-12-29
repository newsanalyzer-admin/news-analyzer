package org.newsanalyzer.apitests;

import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.newsanalyzer.apitests.config.RestAssuredConfiguration;
import org.newsanalyzer.apitests.config.TestConfig;
import org.newsanalyzer.apitests.data.TestDataSeeder;

import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Base class for all API tests.
 * Provides common setup, configuration, and utility methods.
 *
 * <p>Usage: Extend this class in your test classes to get automatic
 * REST Assured configuration and access to pre-configured request specs.</p>
 *
 * <p>Features:</p>
 * <ul>
 *   <li>Automatic REST Assured configuration</li>
 *   <li>Automatic database seeding with test data (runs once per test run)</li>
 *   <li>Pre-configured request specifications for backend and reasoning services</li>
 * </ul>
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

    /**
     * Flag to ensure database seeding only happens once across all test classes.
     */
    private static final AtomicBoolean databaseSeeded = new AtomicBoolean(false);

    /**
     * Flag to track if seeding failed (to avoid repeated attempts).
     */
    private static final AtomicBoolean seedingFailed = new AtomicBoolean(false);

    @BeforeAll
    void setupRestAssured() {
        RestAssuredConfiguration.initialize();
        logTestConfiguration();
        seedDatabaseIfNeeded();
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
     * Seeds the database with test data if not already seeded.
     * This runs once per JVM/test run, not per test class.
     */
    private void seedDatabaseIfNeeded() {
        // Skip if already seeded or if previous seeding attempt failed
        if (databaseSeeded.get() || seedingFailed.get()) {
            return;
        }

        // Use atomic compareAndSet to ensure only one thread seeds
        if (databaseSeeded.compareAndSet(false, true)) {
            System.out.println("=".repeat(60));
            System.out.println("Seeding Database with Test Data");
            System.out.println("=".repeat(60));

            try {
                TestDataSeeder seeder = TestDataSeeder.create();
                seeder.seedFullTestDataset();
                System.out.println("Database seeding completed successfully");
                System.out.println("=".repeat(60));
            } catch (SQLException e) {
                seedingFailed.set(true);
                System.err.println("WARNING: Database seeding failed: " + e.getMessage());
                System.err.println("Tests requiring seed data may fail.");
                System.err.println("Ensure database is accessible and migrations have been applied.");
                System.err.println("=".repeat(60));
                // Don't throw - allow tests to continue (some may not need seed data)
            } catch (Exception e) {
                seedingFailed.set(true);
                System.err.println("WARNING: Unexpected error during database seeding: " + e.getMessage());
                System.err.println("=".repeat(60));
            }
        }
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
