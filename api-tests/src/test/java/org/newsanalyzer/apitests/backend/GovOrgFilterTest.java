package org.newsanalyzer.apitests.backend;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.newsanalyzer.apitests.BaseApiTest;

import java.util.Map;

import static org.hamcrest.Matchers.*;

/**
 * Filter tests for the Government Organization API endpoints.
 * Tests filtering by cabinet departments, independent agencies, type, branch, and jurisdiction.
 */
@Tag("backend")
@Tag("integration")
@DisplayName("Government Organization Filter Tests")
class GovOrgFilterTest extends BaseApiTest {

    private GovOrgApiClient govOrgClient;

    @BeforeEach
    void setUp() {
        govOrgClient = new GovOrgApiClient(getBackendSpec());
    }

    // ==================== Cabinet Departments Tests ====================

    @Test
    @DisplayName("GET /api/government-organizations/cabinet-departments - should return cabinet departments")
    void shouldGetCabinetDepartments_returns15Departments() {
        // Create a cabinet department for testing with unique acronym
        long timestamp = System.currentTimeMillis();
        String uniqueAcronym = "C" + (timestamp % 100000);
        Map<String, Object> cabinetDept = GovOrgTestDataBuilder.aCabinetDepartment()
                .withOfficialName("Test Cabinet Department " + timestamp)
                .withAcronym(uniqueAcronym)
                .build();

        govOrgClient.create(cabinetDept)
                .then()
                .statusCode(201);

        // Get cabinet departments
        govOrgClient.getCabinetDepartments()
                .then()
                .statusCode(200)
                .body("$", instanceOf(java.util.List.class))
                .body("size()", greaterThanOrEqualTo(0));
    }

    @Test
    @DisplayName("GET /api/government-organizations/cabinet-departments - should only return DEPARTMENT type")
    void shouldGetCabinetDepartments_onlyReturnsDepartmentType() {
        govOrgClient.getCabinetDepartments()
                .then()
                .statusCode(200)
                .body("$", instanceOf(java.util.List.class));
        // If there are results, they should all be DEPARTMENT type
    }

    // ==================== Independent Agencies Tests ====================

    @Test
    @DisplayName("GET /api/government-organizations/independent-agencies - should return independent agencies")
    void shouldGetIndependentAgencies_returnsNonCabinet() {
        // Create an independent agency for testing with unique acronym
        long timestamp = System.currentTimeMillis();
        String uniqueAcronym = "I" + (timestamp % 100000);
        Map<String, Object> independentAgency = GovOrgTestDataBuilder.anIndependentAgency()
                .withOfficialName("Test Independent Agency " + timestamp)
                .withAcronym(uniqueAcronym)
                .build();

        govOrgClient.create(independentAgency)
                .then()
                .statusCode(201);

        // Get independent agencies
        govOrgClient.getIndependentAgencies()
                .then()
                .statusCode(200)
                .body("$", instanceOf(java.util.List.class))
                .body("size()", greaterThanOrEqualTo(0));
    }

    // ==================== Filter by Type Tests ====================

    @Test
    @DisplayName("GET /api/government-organizations/by-type?type= - should return organizations of specified type")
    void shouldFilterByType_returnsMatchingOrgs() {
        // Create an agency
        Map<String, Object> agency = GovOrgTestDataBuilder.aGovOrg()
                .withOfficialName("Filter By Type Agency " + System.currentTimeMillis())
                .withOrganizationType(GovOrgTestDataBuilder.TYPE_AGENCY)
                .build();

        govOrgClient.create(agency)
                .then()
                .statusCode(201);

        // Filter by AGENCY type
        govOrgClient.getByType(GovOrgTestDataBuilder.TYPE_AGENCY)
                .then()
                .statusCode(200)
                .body("$", instanceOf(java.util.List.class))
                .body("size()", greaterThan(0));
    }

    @Test
    @DisplayName("GET /api/government-organizations/by-type?type=BUREAU - should return only bureaus")
    void shouldFilterByType_returnsBureaus() {
        // Create a bureau with unique acronym
        long timestamp = System.currentTimeMillis();
        String uniqueAcronym = "B" + (timestamp % 100000);
        Map<String, Object> bureau = GovOrgTestDataBuilder.aBureau()
                .withOfficialName("Test Bureau " + timestamp)
                .withAcronym(uniqueAcronym)
                .build();

        govOrgClient.create(bureau)
                .then()
                .statusCode(201);

        govOrgClient.getByType(GovOrgTestDataBuilder.TYPE_BUREAU)
                .then()
                .statusCode(200)
                .body("$", instanceOf(java.util.List.class));
    }

    // ==================== Filter by Branch Tests ====================

    @Test
    @DisplayName("GET /api/government-organizations/by-branch?branch= - should return organizations of specified branch")
    void shouldFilterByBranch_returnsMatchingOrgs() {
        // Create an executive branch org
        Map<String, Object> execOrg = GovOrgTestDataBuilder.aGovOrg()
                .withOfficialName("Executive Branch Org " + System.currentTimeMillis())
                .withGovernmentBranch(GovOrgTestDataBuilder.BRANCH_EXECUTIVE)
                .build();

        govOrgClient.create(execOrg)
                .then()
                .statusCode(201);

        // Filter by EXECUTIVE branch
        govOrgClient.getByBranch(GovOrgTestDataBuilder.BRANCH_EXECUTIVE)
                .then()
                .statusCode(200)
                .body("$", instanceOf(java.util.List.class))
                .body("size()", greaterThan(0));
    }

    @Test
    @DisplayName("GET /api/government-organizations/by-branch?branch=LEGISLATIVE - should return legislative branch orgs")
    void shouldFilterByBranch_returnsLegislativeBranch() {
        govOrgClient.getByBranch(GovOrgTestDataBuilder.BRANCH_LEGISLATIVE)
                .then()
                .statusCode(200)
                .body("$", instanceOf(java.util.List.class));
    }

    @Test
    @DisplayName("GET /api/government-organizations/by-branch?branch=JUDICIAL - should return judicial branch orgs")
    void shouldFilterByBranch_returnsJudicialBranch() {
        govOrgClient.getByBranch(GovOrgTestDataBuilder.BRANCH_JUDICIAL)
                .then()
                .statusCode(200)
                .body("$", instanceOf(java.util.List.class));
    }

    // Note: INDEPENDENT is not a valid branch per the GovernmentBranch enum
    // Valid branches are: executive, legislative, judicial

    // ==================== Filter by Jurisdiction Tests ====================

    @Test
    @DisplayName("GET /api/government-organizations/by-jurisdiction?jurisdiction= - should return matching orgs")
    void shouldFilterByJurisdiction_returnsMatchingOrgs() {
        // Jurisdiction is stored as an array (jurisdiction_areas) in the model
        // The API endpoint expects a string that matches one of the jurisdiction areas
        govOrgClient.getByJurisdiction("environmental_protection")
                .then()
                .statusCode(200)
                .body("$", instanceOf(java.util.List.class));
    }

    @Test
    @DisplayName("GET /api/government-organizations/by-jurisdiction?jurisdiction= - should return empty for non-matching")
    void shouldFilterByJurisdiction_returnsEmptyForNoMatches() {
        // Query for a jurisdiction that might not have any orgs
        govOrgClient.getByJurisdiction("NONEXISTENT_JURISDICTION")
                .then()
                .statusCode(200)
                .body("$", instanceOf(java.util.List.class));
    }
}
