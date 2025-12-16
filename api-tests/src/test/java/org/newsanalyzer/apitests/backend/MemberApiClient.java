package org.newsanalyzer.apitests.backend;

import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.newsanalyzer.apitests.config.Endpoints;

import static io.restassured.RestAssured.given;

/**
 * API client helper for Member (Congressional) operations.
 * Provides convenient methods for common Member API calls.
 *
 * @author James (Dev Agent)
 */
public class MemberApiClient {

    private final RequestSpecification spec;

    public MemberApiClient(RequestSpecification spec) {
        this.spec = spec;
    }

    // ==================== List Operations ====================

    /**
     * List all members (paginated).
     */
    public Response listAll() {
        return given()
                .spec(spec)
                .when()
                .get(Endpoints.Backend.MEMBERS);
    }

    /**
     * List all members with pagination parameters.
     */
    public Response listAll(int page, int size) {
        return given()
                .spec(spec)
                .queryParam("page", page)
                .queryParam("size", size)
                .when()
                .get(Endpoints.Backend.MEMBERS);
    }

    /**
     * List all members with pagination and sorting.
     */
    public Response listAll(int page, int size, String sort) {
        return given()
                .spec(spec)
                .queryParam("page", page)
                .queryParam("size", size)
                .queryParam("sort", sort)
                .when()
                .get(Endpoints.Backend.MEMBERS);
    }

    // ==================== Lookup Operations ====================

    /**
     * Get member by BioGuide ID.
     */
    public Response getByBioguideId(String bioguideId) {
        return given()
                .spec(spec)
                .pathParam("bioguideId", bioguideId)
                .when()
                .get(Endpoints.Backend.MEMBER_BY_BIOGUIDE_ID);
    }

    // ==================== Search Operations ====================

    /**
     * Search members by name.
     */
    public Response searchByName(String name) {
        return given()
                .spec(spec)
                .queryParam("name", name)
                .when()
                .get(Endpoints.Backend.MEMBERS_SEARCH);
    }

    /**
     * Search members by name with pagination.
     */
    public Response searchByName(String name, int page, int size) {
        return given()
                .spec(spec)
                .queryParam("name", name)
                .queryParam("page", page)
                .queryParam("size", size)
                .when()
                .get(Endpoints.Backend.MEMBERS_SEARCH);
    }

    // ==================== Filter Operations ====================

    /**
     * Get members by state (2-letter code).
     */
    public Response getByState(String state) {
        return given()
                .spec(spec)
                .pathParam("state", state)
                .when()
                .get(Endpoints.Backend.MEMBERS_BY_STATE);
    }

    /**
     * Get members by state with pagination.
     */
    public Response getByState(String state, int page, int size) {
        return given()
                .spec(spec)
                .pathParam("state", state)
                .queryParam("page", page)
                .queryParam("size", size)
                .when()
                .get(Endpoints.Backend.MEMBERS_BY_STATE);
    }

    /**
     * Get members by chamber (SENATE or HOUSE).
     */
    public Response getByChamber(String chamber) {
        return given()
                .spec(spec)
                .pathParam("chamber", chamber)
                .when()
                .get(Endpoints.Backend.MEMBERS_BY_CHAMBER);
    }

    /**
     * Get members by chamber with pagination.
     */
    public Response getByChamber(String chamber, int page, int size) {
        return given()
                .spec(spec)
                .pathParam("chamber", chamber)
                .queryParam("page", page)
                .queryParam("size", size)
                .when()
                .get(Endpoints.Backend.MEMBERS_BY_CHAMBER);
    }

    // ==================== Statistics Operations ====================

    /**
     * Get total member count.
     */
    public Response getCount() {
        return given()
                .spec(spec)
                .when()
                .get(Endpoints.Backend.MEMBERS_COUNT);
    }

    /**
     * Get party distribution statistics.
     */
    public Response getPartyDistribution() {
        return given()
                .spec(spec)
                .when()
                .get(Endpoints.Backend.MEMBERS_STATS_PARTY);
    }

    /**
     * Get state distribution statistics.
     */
    public Response getStateDistribution() {
        return given()
                .spec(spec)
                .when()
                .get(Endpoints.Backend.MEMBERS_STATS_STATE);
    }

    // ==================== Admin Operations ====================

    /**
     * Trigger member sync from Congress.gov API.
     */
    public Response triggerSync() {
        return given()
                .spec(spec)
                .when()
                .post(Endpoints.Backend.MEMBERS_SYNC);
    }
}
