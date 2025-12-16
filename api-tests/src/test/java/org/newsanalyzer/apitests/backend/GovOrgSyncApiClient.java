package org.newsanalyzer.apitests.backend;

import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.newsanalyzer.apitests.backend.dto.CsvImportResultDto;
import org.newsanalyzer.apitests.backend.dto.SyncResultDto;
import org.newsanalyzer.apitests.backend.dto.SyncStatusDto;
import org.newsanalyzer.apitests.config.Endpoints;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;

/**
 * API client helper for Government Organization sync and import operations.
 * Provides convenient methods for sync endpoints:
 * - POST /api/government-organizations/sync/federal-register
 * - GET /api/government-organizations/sync/status
 * - POST /api/government-organizations/import/csv
 */
public class GovOrgSyncApiClient {

    private final RequestSpecification spec;
    private final String baseUri;

    public GovOrgSyncApiClient(RequestSpecification spec) {
        this.spec = spec;
        this.baseUri = null; // Will use spec for JSON requests
    }

    public GovOrgSyncApiClient(RequestSpecification spec, String baseUri) {
        this.spec = spec;
        this.baseUri = baseUri;
    }

    // ==================== Sync Operations ====================

    /**
     * Trigger sync from Federal Register API.
     * Returns SyncResultDto on success (200).
     */
    public SyncResultDto triggerFederalRegisterSync() {
        return given()
                .spec(spec)
                .contentType(JSON)
                .when()
                .post(Endpoints.Backend.GOV_ORGS_SYNC_FEDERAL_REGISTER)
                .then()
                .statusCode(200)
                .extract()
                .as(SyncResultDto.class);
    }

    /**
     * Trigger sync from Federal Register API and return raw response.
     * Use this when expecting non-200 status codes.
     */
    public Response triggerFederalRegisterSyncRaw() {
        return given()
                .spec(spec)
                .contentType(JSON)
                .when()
                .post(Endpoints.Backend.GOV_ORGS_SYNC_FEDERAL_REGISTER);
    }

    /**
     * Get sync status.
     * Returns SyncStatusDto with organization counts and API availability.
     */
    public SyncStatusDto getSyncStatus() {
        return given()
                .spec(spec)
                .when()
                .get(Endpoints.Backend.GOV_ORGS_SYNC_STATUS)
                .then()
                .statusCode(200)
                .extract()
                .as(SyncStatusDto.class);
    }

    /**
     * Get sync status as raw response.
     */
    public Response getSyncStatusRaw() {
        return given()
                .spec(spec)
                .when()
                .get(Endpoints.Backend.GOV_ORGS_SYNC_STATUS);
    }

    // ==================== CSV Import Operations ====================

    /**
     * Import organizations from CSV file.
     * Returns CsvImportResultDto on success (200).
     * Note: Uses baseUri directly to avoid Content-Type conflict with multipart uploads.
     */
    public CsvImportResultDto importCsv(File csvFile) {
        return given()
                .baseUri(getBaseUri())
                .multiPart("file", csvFile, "text/csv")
                .when()
                .post(Endpoints.Backend.GOV_ORGS_IMPORT_CSV)
                .then()
                .statusCode(200)
                .extract()
                .as(CsvImportResultDto.class);
    }

    /**
     * Import organizations from CSV file and return raw response.
     * Use this when expecting validation errors (400) or other status codes.
     * Note: Uses baseUri directly to avoid Content-Type conflict with multipart uploads.
     */
    public Response importCsvRaw(File csvFile) {
        return given()
                .baseUri(getBaseUri())
                .multiPart("file", csvFile, "text/csv")
                .when()
                .post(Endpoints.Backend.GOV_ORGS_IMPORT_CSV);
    }

    /**
     * Import organizations from CSV content string.
     * Creates a temporary file and uploads it.
     * Returns CsvImportResultDto on success (200).
     */
    public CsvImportResultDto importCsv(String csvContent) {
        File tempFile = createTempCsvFile(csvContent);
        try {
            return importCsv(tempFile);
        } finally {
            tempFile.delete();
        }
    }

    /**
     * Import organizations from CSV content string and return raw response.
     * Use this when expecting validation errors (400) or other status codes.
     */
    public Response importCsvRaw(String csvContent) {
        File tempFile = createTempCsvFile(csvContent);
        try {
            return importCsvRaw(tempFile);
        } finally {
            tempFile.delete();
        }
    }

    /**
     * Import organizations expecting validation errors (400 response).
     * Returns CsvImportResultDto extracted from 400 response.
     * Note: Uses baseUri directly to avoid Content-Type conflict with multipart uploads.
     */
    public CsvImportResultDto importCsvExpectError(File csvFile) {
        return given()
                .baseUri(getBaseUri())
                .multiPart("file", csvFile, "text/csv")
                .when()
                .post(Endpoints.Backend.GOV_ORGS_IMPORT_CSV)
                .then()
                .statusCode(400)
                .extract()
                .as(CsvImportResultDto.class);
    }

    /**
     * Import organizations from CSV content expecting validation errors.
     */
    public CsvImportResultDto importCsvExpectError(String csvContent) {
        File tempFile = createTempCsvFile(csvContent);
        try {
            return importCsvExpectError(tempFile);
        } finally {
            tempFile.delete();
        }
    }

    // ==================== Utility Methods ====================

    /**
     * Create a temporary CSV file from content string.
     */
    private File createTempCsvFile(String csvContent) {
        try {
            Path tempFile = Files.createTempFile("test-import-", ".csv");
            Files.write(tempFile, csvContent.getBytes(StandardCharsets.UTF_8));
            return tempFile.toFile();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create temp CSV file", e);
        }
    }

    /**
     * Get the base URI for multipart requests.
     * Multipart requests cannot use the spec directly because it has Content-Type: JSON set.
     */
    private String getBaseUri() {
        if (baseUri != null) {
            return baseUri;
        }
        // Default fallback - must be provided via constructor for multipart to work
        throw new IllegalStateException(
                "baseUri must be provided for multipart requests. " +
                "Use GovOrgSyncApiClient(spec, baseUri) constructor.");
    }
}
