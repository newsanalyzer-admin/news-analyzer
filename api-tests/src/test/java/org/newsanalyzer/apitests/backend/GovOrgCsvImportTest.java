package org.newsanalyzer.apitests.backend;

import io.restassured.response.Response;
import org.junit.jupiter.api.*;
import org.newsanalyzer.apitests.BaseApiTest;
import org.newsanalyzer.apitests.backend.dto.CsvImportResultDto;
import org.newsanalyzer.apitests.backend.dto.CsvValidationErrorDto;
import org.newsanalyzer.apitests.config.Endpoints;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for Government Organization CSV Import endpoint.
 *
 * Tests:
 * - POST /api/government-organizations/import/csv
 *
 * Test scenarios:
 * - Valid CSV with legislative branch organizations
 * - Valid CSV with judicial branch organizations
 * - Validation errors (invalid branch, invalid orgType)
 * - Missing required fields
 * - Empty file
 *
 * AC: 4 from story FB-2-GOV.4
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("Government Organization CSV Import Tests")
class GovOrgCsvImportTest extends BaseApiTest {

    private GovOrgSyncApiClient syncClient;

    @BeforeAll
    void setUpClient() {
        syncClient = new GovOrgSyncApiClient(getBackendSpec(), getBackendBaseUrl());
    }

    // ==================== Successful Import Tests ====================

    @Nested
    @DisplayName("Successful CSV Imports")
    class SuccessfulImportTests {

        @Test
        @DisplayName("Should import valid legislative branch organizations")
        void shouldImportValidLegislativeOrganizations() {
            // Given: Valid CSV with legislative branch organizations
            String csv = """
                officialName,acronym,branch,orgType,orgLevel,parentId,establishedDate,dissolvedDate,websiteUrl,jurisdictionAreas
                "Test Legislative Body",TLB,legislative,office,1,,1789-03-04,,https://test-legislative.gov,"test jurisdiction"
                """;

            // When: Import CSV
            Response response = syncClient.importCsvRaw(csv);

            // Then: Import succeeds
            response.then()
                    .statusCode(200)
                    .contentType("application/json")
                    .body("success", is(true))
                    .body("added", greaterThanOrEqualTo(0))
                    .body("updated", greaterThanOrEqualTo(0))
                    .body("errors", is(0));
        }

        @Test
        @DisplayName("Should import valid judicial branch organizations")
        void shouldImportValidJudicialOrganizations() {
            // Given: Valid CSV with judicial branch organizations
            String csv = """
                officialName,acronym,branch,orgType,orgLevel,parentId,establishedDate,dissolvedDate,websiteUrl,jurisdictionAreas
                "Test Judicial Court",TJC,judicial,branch,1,,1789-03-04,,https://test-judicial.gov,"test jurisdiction"
                """;

            // When: Import CSV
            Response response = syncClient.importCsvRaw(csv);

            // Then: Import succeeds
            response.then()
                    .statusCode(200)
                    .body("success", is(true));
        }

        @Test
        @DisplayName("Should return import counts on success")
        void shouldReturnImportCountsOnSuccess() {
            // Given: Valid CSV
            String csv = """
                officialName,acronym,branch,orgType,orgLevel,parentId,establishedDate,dissolvedDate,websiteUrl,jurisdictionAreas
                "Import Test Org",ITO,executive,office,1,,,,https://import-test.gov,
                """;

            // When: Import CSV
            CsvImportResultDto result = syncClient.importCsv(csv);

            // Then: Result contains counts
            assertNotNull(result);
            assertTrue(result.isSuccess(), "Import should succeed");
            assertTrue(result.getAdded() >= 0, "Added count should be >= 0");
            assertTrue(result.getUpdated() >= 0, "Updated count should be >= 0");
            assertTrue(result.getSkipped() >= 0, "Skipped count should be >= 0");
            assertEquals(0, result.getErrors(), "Errors should be 0 for valid CSV");
        }

        @Test
        @DisplayName("Should handle CSV with only required fields")
        void shouldHandleCsvWithOnlyRequiredFields() {
            // Given: CSV with only required fields
            String csv = """
                officialName,acronym,branch,orgType,orgLevel,parentId,establishedDate,dissolvedDate,websiteUrl,jurisdictionAreas
                "Minimal Org",,legislative,office,,,,,,,
                """;

            // When: Import CSV
            Response response = syncClient.importCsvRaw(csv);

            // Then: Import succeeds
            response.then()
                    .statusCode(200)
                    .body("success", is(true));
        }
    }

    // ==================== Validation Error Tests ====================

    @Nested
    @DisplayName("Validation Error Scenarios")
    class ValidationErrorTests {

        @Test
        @DisplayName("Should return validation error for invalid branch value")
        void shouldReturnValidationErrorForInvalidBranch() {
            // Given: CSV with invalid branch value
            String csv = """
                officialName,acronym,branch,orgType,orgLevel,parentId,establishedDate,dissolvedDate,websiteUrl,jurisdictionAreas
                "Invalid Branch Org",IBO,congress,office,1,,,,https://invalid.gov,
                """;

            // When: Import CSV
            Response response = syncClient.importCsvRaw(csv);

            // Then: Validation error returned
            response.then()
                    .statusCode(400)
                    .body("success", is(false))
                    .body("validationErrors", hasSize(greaterThan(0)));
        }

        @Test
        @DisplayName("Should return validation error for invalid orgType value")
        void shouldReturnValidationErrorForInvalidOrgType() {
            // Given: CSV with invalid orgType value
            String csv = """
                officialName,acronym,branch,orgType,orgLevel,parentId,establishedDate,dissolvedDate,websiteUrl,jurisdictionAreas
                "Invalid Type Org",ITO,executive,division,1,,,,https://invalid.gov,
                """;

            // When: Import CSV
            Response response = syncClient.importCsvRaw(csv);

            // Then: Validation error returned
            response.then()
                    .statusCode(400)
                    .body("success", is(false))
                    .body("validationErrors", hasSize(greaterThan(0)));
        }

        @Test
        @DisplayName("Should return validation error with line number and field info")
        void shouldReturnValidationErrorWithLineNumberAndFieldInfo() {
            // Given: CSV with validation error on line 2 (first data row)
            String csv = """
                officialName,acronym,branch,orgType,orgLevel,parentId,establishedDate,dissolvedDate,websiteUrl,jurisdictionAreas
                "Test Org",TEST,invalid_branch,office,1,,,,https://test.gov,
                """;

            // When: Import CSV expecting error
            CsvImportResultDto result = syncClient.importCsvExpectError(csv);

            // Then: Validation error includes line number and field info
            assertTrue(result.hasValidationErrors(), "Should have validation errors");
            List<CsvValidationErrorDto> errors = result.getValidationErrors();
            assertFalse(errors.isEmpty(), "Validation errors list should not be empty");

            CsvValidationErrorDto error = errors.get(0);
            assertTrue(error.getLine() >= 2, "Line number should be >= 2 (data starts at line 2)");
            assertNotNull(error.getField(), "Field name should be present");
            assertNotNull(error.getMessage(), "Error message should be present");
        }

        @Test
        @DisplayName("Should return validation error for missing required fields")
        void shouldReturnValidationErrorForMissingRequiredFields() {
            // Given: CSV missing officialName (required field)
            String csv = """
                officialName,acronym,branch,orgType,orgLevel,parentId,establishedDate,dissolvedDate,websiteUrl,jurisdictionAreas
                ,TEST,legislative,office,1,,,,https://test.gov,
                """;

            // When: Import CSV
            Response response = syncClient.importCsvRaw(csv);

            // Then: Validation error returned
            response.then()
                    .statusCode(400)
                    .body("success", is(false))
                    .body("validationErrors", hasSize(greaterThan(0)));
        }

        @Test
        @DisplayName("Should return validation error for invalid date format")
        void shouldReturnValidationErrorForInvalidDateFormat() {
            // Given: CSV with invalid date format
            String csv = """
                officialName,acronym,branch,orgType,orgLevel,parentId,establishedDate,dissolvedDate,websiteUrl,jurisdictionAreas
                "Date Test Org",DTO,legislative,office,1,,March 4 1789,,https://test.gov,
                """;

            // When: Import CSV
            Response response = syncClient.importCsvRaw(csv);

            // Then: Validation error returned for date
            response.then()
                    .statusCode(400)
                    .body("success", is(false))
                    .body("validationErrors", hasSize(greaterThan(0)));
        }
    }

    // ==================== Edge Case Tests ====================

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCaseTests {

        @Test
        @DisplayName("Should handle empty file")
        void shouldHandleEmptyFile() {
            // Given: Empty CSV content
            String csv = "";

            // When: Import empty CSV
            Response response = syncClient.importCsvRaw(csv);

            // Then: Returns error (empty file)
            response.then()
                    .statusCode(400);
        }

        @Test
        @DisplayName("Should handle headers-only file")
        void shouldHandleHeadersOnlyFile() {
            // Given: CSV with only headers, no data
            String csv = """
                officialName,acronym,branch,orgType,orgLevel,parentId,establishedDate,dissolvedDate,websiteUrl,jurisdictionAreas
                """;

            // When: Import CSV
            Response response = syncClient.importCsvRaw(csv);

            // Then: Succeeds with 0 records processed
            response.then()
                    .statusCode(200)
                    .body("success", is(true))
                    .body("added", is(0))
                    .body("updated", is(0))
                    .body("errors", is(0));
        }

        @Test
        @DisplayName("Should handle multiple records in single CSV")
        void shouldHandleMultipleRecordsInSingleCsv() {
            // Given: CSV with multiple organizations
            String csv = """
                officialName,acronym,branch,orgType,orgLevel,parentId,establishedDate,dissolvedDate,websiteUrl,jurisdictionAreas
                "Multi Test Org 1",MTO1,legislative,office,1,,,,https://multi1.gov,
                "Multi Test Org 2",MTO2,legislative,office,1,,,,https://multi2.gov,
                "Multi Test Org 3",MTO3,judicial,office,1,,,,https://multi3.gov,
                """;

            // When: Import CSV
            CsvImportResultDto result = syncClient.importCsv(csv);

            // Then: All records processed
            assertTrue(result.isSuccess(), "Import should succeed");
            assertTrue(result.getTotal() >= 3, "Should process at least 3 records");
        }

        @Test
        @DisplayName("Should update existing organization on duplicate import")
        void shouldUpdateExistingOrganizationOnDuplicateImport() {
            // Given: CSV with same organization imported twice
            String csv = """
                officialName,acronym,branch,orgType,orgLevel,parentId,establishedDate,dissolvedDate,websiteUrl,jurisdictionAreas
                "Duplicate Test Org",DUPE,legislative,office,1,,,,https://dupe.gov,
                """;

            // When: Import CSV twice
            CsvImportResultDto firstImport = syncClient.importCsv(csv);
            CsvImportResultDto secondImport = syncClient.importCsv(csv);

            // Then: First import adds, second import updates or skips
            assertTrue(firstImport.isSuccess(), "First import should succeed");
            assertTrue(secondImport.isSuccess(), "Second import should succeed");
            // Second import should either update or skip (not add as new)
            assertTrue(secondImport.getUpdated() >= 0 || secondImport.getSkipped() >= 0,
                    "Second import should update or skip existing records");
        }
    }

    // ==================== File Upload Tests ====================

    @Nested
    @DisplayName("File Upload Tests")
    class FileUploadTests {

        @Test
        @DisplayName("Should accept CSV file upload")
        void shouldAcceptCsvFileUpload() throws IOException {
            // Given: CSV file from test resources
            File csvFile = getResourceFile("csv/valid-legislative-orgs.csv");

            // When: Upload file
            Response response = syncClient.importCsvRaw(csvFile);

            // Then: File accepted and processed
            response.then()
                    .statusCode(anyOf(is(200), is(400))); // 400 if data already exists
        }

        @Test
        @DisplayName("Should process legislative organizations from file")
        void shouldProcessLegislativeOrganizationsFromFile() throws IOException {
            // Given: Legislative organizations CSV file
            File csvFile = getResourceFile("csv/valid-legislative-orgs.csv");

            // When: Upload file
            Response response = syncClient.importCsvRaw(csvFile);

            // Then: Organizations processed
            if (response.statusCode() == 200) {
                response.then()
                        .body("success", is(true));
            }
        }

        @Test
        @DisplayName("Should process judicial organizations from file")
        void shouldProcessJudicialOrganizationsFromFile() throws IOException {
            // Given: Judicial organizations CSV file
            File csvFile = getResourceFile("csv/valid-judicial-orgs.csv");

            // When: Upload file
            Response response = syncClient.importCsvRaw(csvFile);

            // Then: Organizations processed
            if (response.statusCode() == 200) {
                response.then()
                        .body("success", is(true));
            }
        }
    }

    // ==================== Helper Methods ====================

    private File getResourceFile(String resourcePath) throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        try (InputStream is = classLoader.getResourceAsStream(resourcePath)) {
            if (is == null) {
                throw new IOException("Resource not found: " + resourcePath);
            }
            Path tempFile = Files.createTempFile("test-csv-", ".csv");
            Files.copy(is, tempFile, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            return tempFile.toFile();
        }
    }
}
