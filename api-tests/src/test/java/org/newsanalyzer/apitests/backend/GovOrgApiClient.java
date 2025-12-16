package org.newsanalyzer.apitests.backend;

import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.newsanalyzer.apitests.config.Endpoints;

import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;

/**
 * API client helper for Government Organization operations.
 * Provides convenient methods for common Government Organization API calls.
 */
public class GovOrgApiClient {

    private final RequestSpecification spec;

    public GovOrgApiClient(RequestSpecification spec) {
        this.spec = spec;
    }

    // ==================== CRUD Operations ====================

    /**
     * List all government organizations (paginated).
     */
    public Response listAll() {
        return given()
                .spec(spec)
                .when()
                .get(Endpoints.Backend.GOV_ORGS);
    }

    /**
     * List all government organizations with pagination parameters.
     */
    public Response listAll(int page, int size) {
        return given()
                .spec(spec)
                .queryParam("page", page)
                .queryParam("size", size)
                .when()
                .get(Endpoints.Backend.GOV_ORGS);
    }

    /**
     * List active government organizations.
     */
    public Response listActive() {
        return given()
                .spec(spec)
                .when()
                .get(Endpoints.Backend.GOV_ORGS_ACTIVE);
    }

    /**
     * Get government organization by ID.
     */
    public Response getById(UUID id) {
        return given()
                .spec(spec)
                .pathParam("id", id.toString())
                .when()
                .get(Endpoints.Backend.GOV_ORG_BY_ID);
    }

    /**
     * Get government organization by ID (string version).
     */
    public Response getById(String id) {
        return given()
                .spec(spec)
                .pathParam("id", id)
                .when()
                .get(Endpoints.Backend.GOV_ORG_BY_ID);
    }

    /**
     * Create a new government organization.
     */
    public Response create(Map<String, Object> govOrgRequest) {
        return given()
                .spec(spec)
                .contentType(JSON)
                .body(govOrgRequest)
                .when()
                .post(Endpoints.Backend.GOV_ORGS);
    }

    /**
     * Update a government organization.
     */
    public Response update(UUID id, Map<String, Object> govOrgRequest) {
        return given()
                .spec(spec)
                .contentType(JSON)
                .pathParam("id", id.toString())
                .body(govOrgRequest)
                .when()
                .put(Endpoints.Backend.GOV_ORG_BY_ID);
    }

    /**
     * Update a government organization (string ID version).
     */
    public Response update(String id, Map<String, Object> govOrgRequest) {
        return given()
                .spec(spec)
                .contentType(JSON)
                .pathParam("id", id)
                .body(govOrgRequest)
                .when()
                .put(Endpoints.Backend.GOV_ORG_BY_ID);
    }

    /**
     * Delete (soft delete) a government organization.
     */
    public Response delete(UUID id) {
        return given()
                .spec(spec)
                .pathParam("id", id.toString())
                .when()
                .delete(Endpoints.Backend.GOV_ORG_BY_ID);
    }

    /**
     * Delete (soft delete) a government organization (string ID version).
     */
    public Response delete(String id) {
        return given()
                .spec(spec)
                .pathParam("id", id)
                .when()
                .delete(Endpoints.Backend.GOV_ORG_BY_ID);
    }

    // ==================== Search Operations ====================

    /**
     * Search organizations by name or acronym.
     */
    public Response search(String query) {
        return given()
                .spec(spec)
                .queryParam("query", query)
                .when()
                .get(Endpoints.Backend.GOV_ORGS_SEARCH);
    }

    /**
     * Fuzzy search organizations.
     */
    public Response fuzzySearch(String query) {
        return given()
                .spec(spec)
                .queryParam("query", query)
                .when()
                .get(Endpoints.Backend.GOV_ORGS_SEARCH_FUZZY);
    }

    /**
     * Full-text search organizations.
     */
    public Response fullTextSearch(String query) {
        return given()
                .spec(spec)
                .queryParam("query", query)
                .when()
                .get(Endpoints.Backend.GOV_ORGS_SEARCH_FULLTEXT);
    }

    /**
     * Find organization by exact name or acronym.
     */
    public Response findByNameOrAcronym(String nameOrAcronym) {
        return given()
                .spec(spec)
                .queryParam("nameOrAcronym", nameOrAcronym)
                .when()
                .get(Endpoints.Backend.GOV_ORGS_FIND);
    }

    // ==================== Filter Operations ====================

    /**
     * Get cabinet departments.
     */
    public Response getCabinetDepartments() {
        return given()
                .spec(spec)
                .when()
                .get(Endpoints.Backend.GOV_ORGS_CABINET);
    }

    /**
     * Get independent agencies.
     */
    public Response getIndependentAgencies() {
        return given()
                .spec(spec)
                .when()
                .get(Endpoints.Backend.GOV_ORGS_INDEPENDENT);
    }

    /**
     * Get organizations by type.
     */
    public Response getByType(String type) {
        return given()
                .spec(spec)
                .queryParam("type", type)
                .when()
                .get(Endpoints.Backend.GOV_ORGS_BY_TYPE);
    }

    /**
     * Get organizations by branch.
     */
    public Response getByBranch(String branch) {
        return given()
                .spec(spec)
                .queryParam("branch", branch)
                .when()
                .get(Endpoints.Backend.GOV_ORGS_BY_BRANCH);
    }

    /**
     * Get organizations by jurisdiction.
     */
    public Response getByJurisdiction(String jurisdiction) {
        return given()
                .spec(spec)
                .queryParam("jurisdiction", jurisdiction)
                .when()
                .get(Endpoints.Backend.GOV_ORGS_BY_JURISDICTION);
    }

    // ==================== Hierarchy Operations ====================

    /**
     * Get organization hierarchy.
     */
    public Response getHierarchy(UUID id) {
        return given()
                .spec(spec)
                .pathParam("id", id.toString())
                .when()
                .get(Endpoints.Backend.GOV_ORG_HIERARCHY);
    }

    /**
     * Get organization descendants.
     */
    public Response getDescendants(UUID id) {
        return given()
                .spec(spec)
                .pathParam("id", id.toString())
                .when()
                .get(Endpoints.Backend.GOV_ORG_DESCENDANTS);
    }

    /**
     * Get organization ancestors.
     */
    public Response getAncestors(UUID id) {
        return given()
                .spec(spec)
                .pathParam("id", id.toString())
                .when()
                .get(Endpoints.Backend.GOV_ORG_ANCESTORS);
    }

    /**
     * Get top-level organizations.
     */
    public Response getTopLevel() {
        return given()
                .spec(spec)
                .when()
                .get(Endpoints.Backend.GOV_ORGS_TOP_LEVEL);
    }

    // ==================== Validation Operations ====================

    /**
     * Validate entity against government organizations.
     */
    public Response validateEntity(String entityText, String entityType) {
        Map<String, Object> request = Map.of(
                "entityText", entityText,
                "entityType", entityType
        );

        return given()
                .spec(spec)
                .contentType(JSON)
                .body(request)
                .when()
                .post(Endpoints.Backend.GOV_ORGS_VALIDATE_ENTITY);
    }

    // ==================== Statistics Operations ====================

    /**
     * Get organization statistics.
     */
    public Response getStatistics() {
        return given()
                .spec(spec)
                .when()
                .get(Endpoints.Backend.GOV_ORGS_STATISTICS);
    }

    // ==================== Utility Methods ====================

    /**
     * Create a government organization and return its ID.
     */
    public String createAndGetId(Map<String, Object> govOrgRequest) {
        return create(govOrgRequest)
                .then()
                .statusCode(201)
                .extract()
                .path("id");
    }
}
