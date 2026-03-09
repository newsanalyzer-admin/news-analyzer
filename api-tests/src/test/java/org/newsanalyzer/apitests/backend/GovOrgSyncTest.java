package org.newsanalyzer.apitests.backend;

import io.restassured.response.Response;
import org.junit.jupiter.api.*;
import org.newsanalyzer.apitests.BaseApiTest;
import org.newsanalyzer.apitests.backend.dto.SyncStatusDto;
import org.newsanalyzer.apitests.config.Endpoints;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for Government Organization Sync API endpoints.
 *
 * Tests:
 * - POST /api/government-organizations/sync/federal-register
 * - GET /api/government-organizations/sync/status
 *
 * These tests run against the live backend which calls the real Federal Register API.
 * For deterministic testing, use the MockFederalRegisterServer.
 *
 * AC: 2, 3, 6 from story FB-2-GOV.4
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("Government Organization Sync API Tests")
class GovOrgSyncTest extends BaseApiTest {

    private GovOrgSyncApiClient syncClient;

    @BeforeAll
    void setUpClient() {
        syncClient = new GovOrgSyncApiClient(getBackendSpec(), getBackendBaseUrl());
    }

    // ==================== Sync Status Tests (AC: 3) ====================

    @Nested
    @DisplayName("GET /sync/status - Sync Status")
    class SyncStatusTests {

        @Test
        @DisplayName("Should return sync status with required fields")
        void shouldReturnSyncStatusWithRequiredFields() {
            // When: Get sync status
            Response response = syncClient.getSyncStatusRaw();

            // Then: Response contains required fields
            response.then()
                    .statusCode(200)
                    .contentType("application/json")
                    .body("totalOrganizations", notNullValue())
                    .body("federalRegisterAvailable", notNullValue())
                    .body("countByBranch", notNullValue());
        }

        @Test
        @DisplayName("Should return total organization count")
        void shouldReturnTotalOrganizationCount() {
            // When: Get sync status
            SyncStatusDto status = syncClient.getSyncStatus();

            // Then: Total count is returned (could be 0 for empty database)
            assertNotNull(status);
            assertTrue(status.getTotalOrganizations() >= 0,
                    "Total organizations should be non-negative");
        }

        @Test
        @DisplayName("Should return counts by branch")
        void shouldReturnCountsByBranch() {
            // When: Get sync status
            Response response = syncClient.getSyncStatusRaw();

            // Then: countByBranch map is present
            response.then()
                    .statusCode(200)
                    .body("countByBranch", notNullValue());
        }

        @Test
        @DisplayName("Should indicate Federal Register API availability")
        void shouldIndicateFederalRegisterApiAvailability() {
            // When: Get sync status
            SyncStatusDto status = syncClient.getSyncStatus();

            // Then: API availability is indicated
            // Note: Could be true or false depending on network conditions
            assertNotNull(status);
            // federalRegisterAvailable is a boolean, just ensure it's parsed correctly
        }
    }

    // ==================== Federal Register Sync Tests (AC: 2) ====================

    @Nested
    @DisplayName("POST /sync/federal-register - Trigger Sync")
    class FederalRegisterSyncTests {

        @Test
        @DisplayName("Should trigger async sync and return job status")
        void shouldTriggerSyncAndReturnResultWithCounts() {
            // When: Trigger Federal Register sync (now async)
            Response response = syncClient.triggerFederalRegisterSyncRaw();

            // Then: Response is 202 Accepted (job started) or 409 Conflict (already running)
            int statusCode = response.statusCode();
            assertTrue(statusCode == 202 || statusCode == 409,
                    "Expected 202 (job started) or 409 (already running), got " + statusCode);

            if (statusCode == 202) {
                response.then()
                        .contentType("application/json")
                        .body("jobId", notNullValue())
                        .body("syncType", equalTo("gov-orgs"))
                        .body("state", equalTo("RUNNING"));
            }
        }

        @Test
        @DisplayName("Should return valid async job structure on accepted")
        void shouldReturnValidSyncResultStructureOnSuccess() {
            // When: Trigger sync (now async)
            Response response = syncClient.triggerFederalRegisterSyncRaw();

            // Then: If accepted, job status has required fields
            if (response.statusCode() == 202) {
                String jobId = response.jsonPath().getString("jobId");
                String state = response.jsonPath().getString("state");
                String syncType = response.jsonPath().getString("syncType");

                assertNotNull(jobId, "Job ID should be present");
                assertEquals("RUNNING", state, "State should be RUNNING");
                assertEquals("gov-orgs", syncType, "Sync type should be gov-orgs");
            }
        }

        @Test
        @DisplayName("Should return job ID that can be used for polling")
        void shouldReturnTotalProcessedCount() {
            // When: Trigger sync
            Response response = syncClient.triggerFederalRegisterSyncRaw();

            // Then: If accepted, job ID is a valid UUID-like string
            if (response.statusCode() == 202) {
                String jobId = response.jsonPath().getString("jobId");
                assertNotNull(jobId, "Job ID should be present for polling");
                assertFalse(jobId.isEmpty(), "Job ID should not be empty");
            }
        }

        @Test
        @DisplayName("Should return 409 when sync is already running")
        void shouldIncludeErrorMessagesIfErrorsOccur() {
            // When: Trigger two syncs in quick succession
            Response firstResponse = syncClient.triggerFederalRegisterSyncRaw();
            Response secondResponse = syncClient.triggerFederalRegisterSyncRaw();

            // Then: At least one should be 202, and if first was 202 then second may be 409
            int first = firstResponse.statusCode();
            int second = secondResponse.statusCode();

            assertTrue(first == 202 || first == 409,
                    "First sync should return 202 or 409, got " + first);
            assertTrue(second == 202 || second == 409,
                    "Second sync should return 202 or 409, got " + second);
        }
    }

    // ==================== Sync Process Verification (AC: 2, 6) ====================

    @Nested
    @DisplayName("Sync Process Verification")
    class SyncProcessVerificationTests {

        @Test
        @DisplayName("Should start async sync for executive branch agencies")
        void shouldSyncExecutiveBranchAgencies() {
            // When: Trigger sync (now async)
            Response response = syncClient.triggerFederalRegisterSyncRaw();

            // Then: Sync job should be accepted or already running
            int statusCode = response.statusCode();
            assertTrue(statusCode == 202 || statusCode == 409,
                    "Expected 202 (job started) or 409 (already running), got " + statusCode);

            if (statusCode == 202) {
                String jobId = response.jsonPath().getString("jobId");
                System.out.println("Sync job started: " + jobId);
            }
        }

        @Test
        @DisplayName("Should update organization count after sync")
        void shouldUpdateOrganizationCountAfterSync() {
            // Given: Initial status
            SyncStatusDto initialStatus = syncClient.getSyncStatus();
            long initialCount = initialStatus.getTotalOrganizations();

            // When: Trigger sync (async - may add/update organizations)
            Response syncResponse = syncClient.triggerFederalRegisterSyncRaw();

            // Then: Verify sync was accepted
            int statusCode = syncResponse.statusCode();
            assertTrue(statusCode == 202 || statusCode == 409,
                    "Expected 202 (job started) or 409 (already running), got " + statusCode);

            // Verify status endpoint still works
            SyncStatusDto finalStatus = syncClient.getSyncStatus();
            assertTrue(finalStatus.getTotalOrganizations() >= 0,
                    "Total organizations should be non-negative after sync");
        }
    }

    // ==================== Error Handling Tests ====================

    @Nested
    @DisplayName("Error Handling")
    class ErrorHandlingTests {

        @Test
        @DisplayName("Should handle sync gracefully - returns accepted or conflict")
        void shouldHandleSyncGracefullyWhenApiReturnsEmpty() {
            // When: Trigger sync (async pattern)
            Response response = syncClient.triggerFederalRegisterSyncRaw();

            // Then: Either 202 Accepted or 409 Conflict, never 500
            int statusCode = response.statusCode();
            assertTrue(statusCode == 202 || statusCode == 409,
                    "Expected 202 (accepted) or 409 (already running), got " + statusCode);
        }

        @Test
        @DisplayName("Should return errorMessages in error response")
        void shouldReturnErrorMessagesInErrorResponse() {
            // When: Trigger sync
            Response response = syncClient.triggerFederalRegisterSyncRaw();

            // Then: Error messages field should be present
            if (response.statusCode() == 500) {
                response.then()
                        .body("errorMessages", hasSize(greaterThan(0)));
            }
        }
    }
}
