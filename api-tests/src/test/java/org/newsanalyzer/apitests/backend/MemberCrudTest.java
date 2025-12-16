package org.newsanalyzer.apitests.backend;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.newsanalyzer.apitests.BaseApiTest;
import org.newsanalyzer.apitests.data.DatabaseIntegrationTest.SeedPersons;

import static org.hamcrest.Matchers.*;

/**
 * CRUD tests for the Member (Congressional) API endpoints.
 * Tests listing, lookup by BioGuide ID, and statistics.
 *
 * @author James (Dev Agent)
 */
@Tag("backend")
@DisplayName("Member CRUD Tests")
class MemberCrudTest extends BaseApiTest {

    private MemberApiClient memberClient;

    @BeforeEach
    void setUp() {
        memberClient = new MemberApiClient(getBackendSpec());
    }

    // ==================== List All Tests ====================

    @Test
    @DisplayName("GET /api/members - should return paginated list of members")
    void shouldListAllMembers_returnsPaginatedList() {
        memberClient.listAll()
                .then()
                .statusCode(200)
                .body("content", instanceOf(java.util.List.class))
                .body("pageable", notNullValue())
                .body("totalElements", greaterThanOrEqualTo(0));
    }

    @Test
    @DisplayName("GET /api/members - should support pagination parameters")
    void shouldListAllMembers_supportsPagination() {
        memberClient.listAll(0, 5)
                .then()
                .statusCode(200)
                .body("content", instanceOf(java.util.List.class))
                .body("size", equalTo(5))
                .body("number", equalTo(0));
    }

    @Test
    @DisplayName("GET /api/members - should support sorting")
    void shouldListAllMembers_supportsSorting() {
        memberClient.listAll(0, 10, "lastName,asc")
                .then()
                .statusCode(200)
                .body("content", instanceOf(java.util.List.class));
    }

    // ==================== Get by BioGuide ID Tests ====================

    @Test
    @DisplayName("GET /api/members/{bioguideId} - should return member when found")
    void shouldGetByBioguideId_returnsMemberWhenFound() {
        // Use known seed data BioGuide ID
        memberClient.getByBioguideId(SeedPersons.SANDERS_BIOGUIDE)
                .then()
                .statusCode(200)
                .body("bioguideId", equalTo(SeedPersons.SANDERS_BIOGUIDE))
                .body("firstName", equalTo("Bernard"))
                .body("lastName", equalTo("Sanders"))
                .body("party", equalTo("Independent"))
                .body("state", equalTo("VT"))
                .body("chamber", equalTo("SENATE"));
    }

    @Test
    @DisplayName("GET /api/members/{bioguideId} - should return House member correctly")
    void shouldGetByBioguideId_returnsHouseMember() {
        memberClient.getByBioguideId(SeedPersons.PELOSI_BIOGUIDE)
                .then()
                .statusCode(200)
                .body("bioguideId", equalTo(SeedPersons.PELOSI_BIOGUIDE))
                .body("firstName", equalTo("Nancy"))
                .body("lastName", equalTo("Pelosi"))
                .body("chamber", equalTo("HOUSE"));
    }

    @Test
    @DisplayName("GET /api/members/{bioguideId} - should return 404 when not found")
    void shouldGetByBioguideId_returns404WhenNotFound() {
        memberClient.getByBioguideId("INVALID_BIOGUIDE_ID")
                .then()
                .statusCode(404);
    }

    // ==================== Count Tests ====================

    @Test
    @DisplayName("GET /api/members/count - should return total member count")
    void shouldGetCount_returnsTotalCount() {
        memberClient.getCount()
                .then()
                .statusCode(200)
                .body(greaterThanOrEqualTo("0"));
    }

    // ==================== Statistics Tests ====================

    @Test
    @DisplayName("GET /api/members/stats/party - should return party distribution")
    void shouldGetPartyDistribution_returnsStats() {
        memberClient.getPartyDistribution()
                .then()
                .statusCode(200)
                .body("$", instanceOf(java.util.List.class));
    }

    @Test
    @DisplayName("GET /api/members/stats/state - should return state distribution")
    void shouldGetStateDistribution_returnsStats() {
        memberClient.getStateDistribution()
                .then()
                .statusCode(200)
                .body("$", instanceOf(java.util.List.class));
    }
}
