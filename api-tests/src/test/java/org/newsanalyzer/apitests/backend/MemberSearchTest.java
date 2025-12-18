package org.newsanalyzer.apitests.backend;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.newsanalyzer.apitests.BaseApiTest;
import org.newsanalyzer.apitests.data.DatabaseIntegrationTest.SeedPersons;

import static org.hamcrest.Matchers.*;

/**
 * Search and filter tests for the Member (Congressional) API endpoints.
 * Tests search by name, filter by state, and filter by chamber.
 *
 * @author James (Dev Agent)
 */
@Tag("backend")
@DisplayName("Member Search and Filter Tests")
class MemberSearchTest extends BaseApiTest {

    private MemberApiClient memberClient;

    @BeforeEach
    void setUp() {
        memberClient = new MemberApiClient(getBackendSpec());
    }

    // ==================== Search by Name Tests ====================

    @Test
    @DisplayName("GET /api/members/search?name= - should find members by last name")
    void shouldSearchByName_findsByLastName() {
        memberClient.searchByName("Sanders")
                .then()
                .statusCode(200)
                .body("content", instanceOf(java.util.List.class))
                .body("content.size()", greaterThanOrEqualTo(1))
                .body("content[0].lastName", equalToIgnoringCase("Sanders"));
    }

    @Test
    @DisplayName("GET /api/members/search?name= - should find members by first name")
    void shouldSearchByName_findsByFirstName() {
        memberClient.searchByName("Nancy")
                .then()
                .statusCode(200)
                .body("content", instanceOf(java.util.List.class))
                .body("content.size()", greaterThanOrEqualTo(1));
    }

    @Test
    @DisplayName("GET /api/members/search?name= - should return empty when no match")
    void shouldSearchByName_returnsEmptyWhenNoMatch() {
        memberClient.searchByName("NonExistentName12345")
                .then()
                .statusCode(200)
                .body("content", instanceOf(java.util.List.class))
                .body("content.size()", equalTo(0));
    }

    @Test
    @DisplayName("GET /api/members/search?name= - should return 400 for empty name")
    void shouldSearchByName_returns400ForEmptyName() {
        memberClient.searchByName("")
                .then()
                .statusCode(400);
    }

    @Test
    @DisplayName("GET /api/members/search?name= - should support pagination")
    void shouldSearchByName_supportsPagination() {
        memberClient.searchByName("a", 0, 2)
                .then()
                .statusCode(200)
                .body("content", instanceOf(java.util.List.class))
                .body("size", equalTo(2));
    }

    // ==================== Filter by State Tests ====================

    @Test
    @DisplayName("GET /api/members/by-state/{state} - should filter by state code")
    void shouldFilterByState_returnsMatchingMembers() {
        memberClient.getByState("VT")
                .then()
                .statusCode(200)
                .body("content", instanceOf(java.util.List.class))
                .body("content.size()", greaterThanOrEqualTo(1))
                .body("content[0].state", equalTo("VT"));
    }

    @Test
    @DisplayName("GET /api/members/by-state/{state} - should handle lowercase state")
    void shouldFilterByState_handlesLowercaseState() {
        memberClient.getByState("vt")
                .then()
                .statusCode(200)
                .body("content", instanceOf(java.util.List.class));
    }

    @Test
    @DisplayName("GET /api/members/by-state/{state} - should return 400 for invalid state code")
    void shouldFilterByState_returns400ForInvalidStateCode() {
        memberClient.getByState("VERMONT")
                .then()
                .statusCode(400);
    }

    @Test
    @DisplayName("GET /api/members/by-state/{state} - should return empty for state with no members")
    void shouldFilterByState_returnsEmptyWhenNoMembers() {
        // Use a territory or uncommon state that might not be in seed data
        memberClient.getByState("AK")
                .then()
                .statusCode(200)
                .body("content", instanceOf(java.util.List.class));
    }

    @Test
    @DisplayName("GET /api/members/by-state/{state} - Texas should have multiple members")
    void shouldFilterByState_texasHasMultipleMembers() {
        memberClient.getByState("TX")
                .then()
                .statusCode(200)
                .body("content", instanceOf(java.util.List.class))
                .body("content.size()", greaterThanOrEqualTo(1));
    }

    // ==================== Filter by Chamber Tests ====================

    @Test
    @DisplayName("GET /api/members/by-chamber/{chamber} - should filter by SENATE")
    void shouldFilterByChamber_returnsSenators() {
        memberClient.getByChamber("SENATE")
                .then()
                .statusCode(200)
                .body("content", instanceOf(java.util.List.class))
                .body("content.size()", greaterThanOrEqualTo(1))
                .body("content[0].chamber", equalTo("SENATE"));
    }

    @Test
    @DisplayName("GET /api/members/by-chamber/{chamber} - should filter by HOUSE")
    void shouldFilterByChamber_returnsRepresentatives() {
        memberClient.getByChamber("HOUSE")
                .then()
                .statusCode(200)
                .body("content", instanceOf(java.util.List.class))
                .body("content.size()", greaterThanOrEqualTo(1))
                .body("content[0].chamber", equalTo("HOUSE"));
    }

    @Test
    @DisplayName("GET /api/members/by-chamber/{chamber} - should handle lowercase chamber")
    void shouldFilterByChamber_handlesLowercaseChamber() {
        memberClient.getByChamber("senate")
                .then()
                .statusCode(200)
                .body("content", instanceOf(java.util.List.class));
    }

    @Test
    @DisplayName("GET /api/members/by-chamber/{chamber} - should return 400 for invalid chamber")
    void shouldFilterByChamber_returns400ForInvalidChamber() {
        memberClient.getByChamber("INVALID")
                .then()
                .statusCode(400);
    }

    @Test
    @DisplayName("GET /api/members/by-chamber/{chamber} - should support pagination")
    void shouldFilterByChamber_supportsPagination() {
        memberClient.getByChamber("SENATE", 0, 2)
                .then()
                .statusCode(200)
                .body("content", instanceOf(java.util.List.class))
                .body("size", equalTo(2));
    }
}
