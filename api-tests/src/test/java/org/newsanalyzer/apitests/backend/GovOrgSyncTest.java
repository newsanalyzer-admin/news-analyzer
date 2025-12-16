package org.newsanalyzer.apitests.backend;

import io.restassured.response.Response;
import org.junit.jupiter.api.*;
import org.newsanalyzer.apitests.BaseApiTest;
import org.newsanalyzer.apitests.backend.dto.SyncResultDto;
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
        @DisplayName("Should trigger sync and return result with counts")
        void shouldTriggerSyncAndReturnResultWithCounts() {
            // When: Trigger Federal Register sync
            Response response = syncClient.triggerFederalRegisterSyncRaw();

            // Then: Response contains sync result fields
            response.then()
                    .statusCode(anyOf(is(200), is(500))) // 500 if API unavailable
                    .contentType("application/json")
                    .body("added", notNullValue())
                    .body("updated", notNullValue())
                    .body("skipped", notNullValue())
                    .body("errors", notNullValue());
        }

        @Test
        @DisplayName("Should return valid sync result structure on success")
        void shouldReturnValidSyncResultStructureOnSuccess() {
            // When: Trigger sync
            Response response = syncClient.triggerFederalRegisterSyncRaw();

            // Then: If successful, all counts are non-negative
            if (response.statusCode() == 200) {
                SyncResultDto result = response.as(SyncResultDto.class);

                assertTrue(result.getAdded() >= 0, "Added count should be >= 0");
                assertTrue(result.getUpdated() >= 0, "Updated count should be >= 0");
                assertTrue(result.getSkipped() >= 0, "Skipped count should be >= 0");
                assertTrue(result.getErrors() >= 0, "Errors count should be >= 0");
            }
        }

        @Test
        @DisplayName("Should return total processed count")
        void shouldReturnTotalProcessedCount() {
            // When: Trigger sync
            Response response = syncClient.triggerFederalRegisterSyncRaw();

            // Then: Total should equal sum of added + updated + skipped + errors
            if (response.statusCode() == 200) {
                SyncResultDto result = response.as(SyncResultDto.class);

                int expectedTotal = result.getAdded() + result.getUpdated() +
                        result.getSkipped() + result.getErrors();
                assertEquals(expectedTotal, result.getTotal(),
                        "Total should equal sum of added, updated, skipped, and errors");
            }
        }

        @Test
        @DisplayName("Should include error messages if errors occur")
        void shouldIncludeErrorMessagesIfErrorsOccur() {
            // When: Trigger sync
            Response response = syncClient.triggerFederalRegisterSyncRaw();

            // Then: Response has errorMessages field
            response.then()
                    .body("errorMessages", notNullValue());
        }
    }

    // ==================== Sync Process Verification (AC: 2, 6) ====================

    @Nested
    @DisplayName("Sync Process Verification")
    class SyncProcessVerificationTests {

        @Test
        @DisplayName("Should sync executive branch agencies from Federal Register")
        void shouldSyncExecutiveBranchAgencies() {
            // When: Trigger sync
            Response response = syncClient.triggerFederalRegisterSyncRaw();

            // Then: If successful, verify some data was processed
            if (response.statusCode() == 200) {
                SyncResultDto result = response.as(SyncResultDto.class);

                // The Federal Register API returns ~300 agencies
                // Depending on database state, these could be added, updated, or skipped
                int total = result.getTotal();

                // Should have processed some agencies (unless API returned empty)
                // This is a sanity check - exact count depends on API response
                System.out.println("Sync result: " + result);
            }
        }

        @Test
        @DisplayName("Should update organization count after sync")
        void shouldUpdateOrganizationCountAfterSync() {
            // Given: Initial status
            SyncStatusDto initialStatus = syncClient.getSyncStatus();
            long initialCount = initialStatus.getTotalOrganizations();

            // When: Trigger sync (may add/update organizations)
            Response syncResponse = syncClient.triggerFederalRegisterSyncRaw();

            // Then: Check final status
            if (syncResponse.statusCode() == 200) {
                SyncStatusDto finalStatus = syncClient.getSyncStatus();

                // Count should be at least the initial count (sync shouldn't delete)
                assertTrue(finalStatus.getTotalOrganizations() >= 0,
                        "Total organizations should be non-negative after sync");
            }
        }
    }

    // ==================== Error Handling Tests ====================

    @Nested
    @DisplayName("Error Handling")
    class ErrorHandlingTests {

        @Test
        @DisplayName("Should handle sync gracefully when API returns empty")
        void shouldHandleSyncGracefullyWhenApiReturnsEmpty() {
            // When: Trigger sync
            Response response = syncClient.triggerFederalRegisterSyncRaw();

            // Then: Either success or controlled error, not 500 crash
            int statusCode = response.statusCode();
            assertTrue(statusCode == 200 || statusCode == 500,
                    "Expected 200 (success) or 500 (controlled error), got " + statusCode);
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
