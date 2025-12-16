package org.newsanalyzer.apitests.integration;

import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.TestInstance;
import org.newsanalyzer.apitests.BaseApiTest;
import org.newsanalyzer.apitests.backend.EntityApiClient;
import org.newsanalyzer.apitests.backend.GovOrgApiClient;
import org.newsanalyzer.apitests.reasoning.ReasoningApiClient;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Base class for all cross-service integration tests.
 *
 * <p>Provides configuration for both backend and reasoning service clients,
 * timing utilities, and test data cleanup mechanisms.</p>
 *
 * <p>All integration test classes should extend this class and be annotated
 * with {@code @Tag("integration")} for filtering.</p>
 *
 * <p>Usage:</p>
 * <pre>
 * {@literal @}Tag("integration")
 * class MyIntegrationTest extends IntegrationTestBase {
 *     {@literal @}Test
 *     void shouldTestCrossServiceWorkflow() {
 *         // Given - use reasoningClient to extract entities
 *         // When - use entityClient to store entities
 *         // Then - verify data consistency
 *     }
 * }
 * </pre>
 */
@Tag("integration")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class IntegrationTestBase extends BaseApiTest {

    // API Clients for cross-service operations
    protected EntityApiClient entityClient;
    protected GovOrgApiClient govOrgClient;
    protected ReasoningApiClient reasoningClient;

    // Timing tracking
    private Instant testStartTime;
    private final List<TimingRecord> timingRecords = new ArrayList<>();

    // Test data tracking for cleanup
    protected final List<String> createdEntityIds = new ArrayList<>();
    protected final List<String> createdGovOrgIds = new ArrayList<>();

    // Performance thresholds (in milliseconds)
    protected static final long EXTRACTION_THRESHOLD_MS = 2000;
    protected static final long STORAGE_THRESHOLD_MS = 500;
    protected static final long VALIDATION_THRESHOLD_MS = 1000;
    protected static final long REASONING_THRESHOLD_MS = 3000;
    protected static final long FULL_PIPELINE_SINGLE_THRESHOLD_MS = 5000;
    protected static final long FULL_PIPELINE_BATCH_THRESHOLD_MS = 30000;

    @BeforeAll
    void setupIntegrationClients() {
        entityClient = new EntityApiClient(getBackendSpec());
        govOrgClient = new GovOrgApiClient(getBackendSpec());
        reasoningClient = new ReasoningApiClient(getReasoningSpec());

        logIntegrationTestConfiguration();
        verifyServicesHealthy();
    }

    @BeforeEach
    void resetTestState() {
        testStartTime = Instant.now();
        timingRecords.clear();
    }

    private void logIntegrationTestConfiguration() {
        System.out.println("-".repeat(60));
        System.out.println("Integration Test Configuration");
        System.out.println("-".repeat(60));
        System.out.println("Cross-service integration tests enabled");
        System.out.println("Backend URL: " + getBackendBaseUrl());
        System.out.println("Reasoning URL: " + getReasoningBaseUrl());
        System.out.println("-".repeat(60));
    }

    /**
     * Verify both services are healthy before running integration tests.
     */
    protected void verifyServicesHealthy() {
        // Check backend health
        int backendStatus = reasoningClient.getRootHealth().statusCode();
        if (backendStatus != 200) {
            System.err.println("WARNING: Reasoning service may not be healthy. Status: " + backendStatus);
        }

        // Check reasoning service health
        try {
            io.restassured.RestAssured.given()
                    .spec(getBackendSpec())
                    .when()
                    .get("/actuator/health")
                    .then()
                    .statusCode(200);
        } catch (Exception e) {
            System.err.println("WARNING: Backend service may not be healthy: " + e.getMessage());
        }
    }

    // ==================== Timing Utilities ====================

    /**
     * Record a timing measurement for a workflow step.
     */
    protected void recordTiming(String stepName, long durationMs) {
        timingRecords.add(new TimingRecord(stepName, durationMs));
        System.out.println(String.format("  [TIMING] %s: %d ms", stepName, durationMs));
    }

    /**
     * Start a timing measurement and return the start instant.
     */
    protected Instant startTiming() {
        return Instant.now();
    }

    /**
     * End a timing measurement and record it.
     */
    protected long endTiming(Instant start, String stepName) {
        long durationMs = Duration.between(start, Instant.now()).toMillis();
        recordTiming(stepName, durationMs);
        return durationMs;
    }

    /**
     * Get total elapsed time since test start.
     */
    protected long getTotalElapsedMs() {
        return Duration.between(testStartTime, Instant.now()).toMillis();
    }

    /**
     * Print timing summary for current test.
     */
    protected void printTimingSummary() {
        System.out.println("\n  [TIMING SUMMARY]");
        System.out.println("  " + "-".repeat(40));
        long total = 0;
        for (TimingRecord record : timingRecords) {
            System.out.println(String.format("  %-30s %6d ms", record.stepName, record.durationMs));
            total += record.durationMs;
        }
        System.out.println("  " + "-".repeat(40));
        System.out.println(String.format("  %-30s %6d ms", "TOTAL", total));
        System.out.println();
    }

    /**
     * Assert that total time is under a threshold.
     */
    protected void assertTotalTimeUnder(long thresholdMs, String workflowName) {
        long total = getTotalElapsedMs();
        if (total > thresholdMs) {
            printTimingSummary();
            throw new AssertionError(String.format(
                    "Workflow '%s' exceeded time limit. Expected < %d ms, actual: %d ms",
                    workflowName, thresholdMs, total
            ));
        }
    }

    // ==================== Test Data Cleanup ====================

    /**
     * Track an entity ID for cleanup.
     */
    protected void trackEntityForCleanup(String entityId) {
        if (entityId != null && !entityId.isEmpty()) {
            createdEntityIds.add(entityId);
        }
    }

    /**
     * Track a government org ID for cleanup.
     */
    protected void trackGovOrgForCleanup(String govOrgId) {
        if (govOrgId != null && !govOrgId.isEmpty()) {
            createdGovOrgIds.add(govOrgId);
        }
    }

    /**
     * Clean up all tracked test data.
     * Call this in @AfterEach or @AfterAll if needed.
     */
    protected void cleanupTestData() {
        // Clean up entities
        for (String entityId : createdEntityIds) {
            try {
                entityClient.deleteEntity(entityId);
            } catch (Exception e) {
                System.err.println("Failed to cleanup entity " + entityId + ": " + e.getMessage());
            }
        }
        createdEntityIds.clear();

        // Clean up government orgs
        for (String govOrgId : createdGovOrgIds) {
            try {
                govOrgClient.delete(govOrgId);
            } catch (Exception e) {
                System.err.println("Failed to cleanup gov org " + govOrgId + ": " + e.getMessage());
            }
        }
        createdGovOrgIds.clear();
    }

    // ==================== Helper Classes ====================

    /**
     * Record for storing timing information.
     */
    protected static class TimingRecord {
        final String stepName;
        final long durationMs;

        TimingRecord(String stepName, long durationMs) {
            this.stepName = stepName;
            this.durationMs = durationMs;
        }
    }

    // ==================== Test Data Builders ====================

    /**
     * Sample news article for full pipeline tests.
     */
    protected static final String SAMPLE_ARTICLE = """
            Senator Elizabeth Warren (D-MA) criticized the Environmental Protection Agency's
            decision to roll back emissions standards during a Senate hearing yesterday.
            The EPA, led by Administrator Michael Regan, defended the changes as necessary
            for economic recovery. The Department of Justice is reviewing the legality of
            the new regulations.
            """;

    /**
     * Simple text for single entity extraction.
     */
    protected static final String SIMPLE_PERSON_TEXT = "Senator Elizabeth Warren spoke at the hearing.";

    /**
     * Text with a government organization.
     */
    protected static final String GOV_ORG_TEXT = "The Environmental Protection Agency announced new regulations.";

    /**
     * Generate a unique test name to avoid collisions.
     */
    protected String generateUniqueName(String prefix) {
        return prefix + "_" + UUID.randomUUID().toString().substring(0, 8);
    }
}
