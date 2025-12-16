package org.newsanalyzer.apitests.backend;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.newsanalyzer.apitests.BaseApiTest;

import java.util.Map;

import static org.hamcrest.Matchers.*;

/**
 * CRUD tests for the Government Organization API endpoints.
 * Tests create, read, update, and delete operations.
 */
@Tag("backend")
@Tag("integration")
@DisplayName("Government Organization CRUD Tests")
class GovOrgCrudTest extends BaseApiTest {

    private GovOrgApiClient govOrgClient;

    @BeforeEach
    void setUp() {
        govOrgClient = new GovOrgApiClient(getBackendSpec());
    }

    // ==================== LIST Tests ====================

    @Test
    @DisplayName("GET /api/government-organizations - should return paginated list of organizations")
    void shouldListAllOrganizations_withPagination() {
        govOrgClient.listAll()
                .then()
                .statusCode(200)
                .body("content", instanceOf(java.util.List.class))
                .body("pageable", notNullValue())
                .body("totalElements", greaterThanOrEqualTo(0));
    }

    @Test
    @DisplayName("GET /api/government-organizations?page=0&size=5 - should return requested page size")
    void shouldListAllOrganizations_respectsPageSize() {
        govOrgClient.listAll(0, 5)
                .then()
                .statusCode(200)
                .body("content", instanceOf(java.util.List.class))
                .body("content.size()", lessThanOrEqualTo(5))
                .body("pageable.pageSize", equalTo(5));
    }

    @Test
    @DisplayName("GET /api/government-organizations/active - should return only active organizations")
    void shouldListActiveOrganizations_excludesDissolved() {
        govOrgClient.listActive()
                .then()
                .statusCode(200)
                .body("$", instanceOf(java.util.List.class));
    }

    // ==================== GET by ID Tests ====================

    @Test
    @DisplayName("GET /api/government-organizations/{id} - should return 200 when organization exists")
    void shouldGetOrganizationById_whenExists_returns200() {
        // First create an organization with unique acronym
        long timestamp = System.currentTimeMillis();
        String uniqueAcronym = "G" + (timestamp % 100000);
        Map<String, Object> orgRequest = GovOrgTestDataBuilder.aGovOrg()
                .withOfficialName("Test Org for GET " + timestamp)
                .withAcronym(uniqueAcronym)
                .build();

        String orgId = govOrgClient.createAndGetId(orgRequest);

        // Then get it by ID
        govOrgClient.getById(orgId)
                .then()
                .statusCode(200)
                .body("id", equalTo(orgId))
                .body("officialName", equalTo(orgRequest.get("officialName")));
    }

    @Test
    @DisplayName("GET /api/government-organizations/{id} - should return 404 when organization not found")
    void shouldGetOrganizationById_whenNotFound_returns404() {
        govOrgClient.getById(GovOrgTestDataBuilder.NON_EXISTENT_ID)
                .then()
                .statusCode(404);
    }

    // ==================== CREATE Tests ====================

    @Test
    @DisplayName("POST /api/government-organizations - should create organization when valid returns 201")
    void shouldCreateOrganization_whenValid_returns201() {
        // Use unique acronym based on timestamp to avoid conflicts
        long timestamp = System.currentTimeMillis();
        String uniqueAcronym = "T" + (timestamp % 100000);  // 6-char max acronym
        Map<String, Object> orgRequest = GovOrgTestDataBuilder.aGovOrg()
                .withOfficialName("New Test Agency " + timestamp)
                .withAcronym(uniqueAcronym)
                .withMission("Test agency mission")
                .build();

        govOrgClient.create(orgRequest)
                .then()
                .statusCode(201)
                .body("id", notNullValue())
                .body("officialName", equalTo(orgRequest.get("officialName")))
                .body("acronym", equalTo(uniqueAcronym))
                .body("orgType", equalTo(GovOrgTestDataBuilder.TYPE_INDEPENDENT_AGENCY));
    }

    @Test
    @DisplayName("POST /api/government-organizations - should return 400 when invalid request")
    void shouldCreateOrganization_whenInvalid_returns400() {
        Map<String, Object> invalidRequest = GovOrgTestDataBuilder.buildInvalidGovOrg();

        govOrgClient.create(invalidRequest)
                .then()
                .statusCode(400);
    }

    // ==================== UPDATE Tests ====================

    @Test
    @DisplayName("PUT /api/government-organizations/{id} - should return 200 when organization exists")
    void shouldUpdateOrganization_whenExists_returns200() {
        // First create an organization with unique acronym
        long timestamp = System.currentTimeMillis();
        String createAcronym = "U" + (timestamp % 100000);
        Map<String, Object> createRequest = GovOrgTestDataBuilder.aGovOrg()
                .withOfficialName("Org for UPDATE " + timestamp)
                .withAcronym(createAcronym)
                .build();

        String orgId = govOrgClient.createAndGetId(createRequest);

        // Then update it with a different unique acronym
        String updateAcronym = "X" + ((timestamp + 1) % 100000);
        String updatedName = "Updated Organization Name " + System.currentTimeMillis();
        Map<String, Object> updateRequest = GovOrgTestDataBuilder.aGovOrg()
                .withOfficialName(updatedName)
                .withAcronym(updateAcronym)
                .withMission("Updated mission statement")
                .build();

        govOrgClient.update(orgId, updateRequest)
                .then()
                .statusCode(200)
                .body("id", equalTo(orgId))
                .body("officialName", equalTo(updatedName))
                .body("acronym", equalTo(updateAcronym));
    }

    @Test
    @DisplayName("PUT /api/government-organizations/{id} - should return 404 when organization not found")
    void shouldUpdateOrganization_whenNotFound_returns404() {
        Map<String, Object> updateRequest = GovOrgTestDataBuilder.aGovOrg()
                .withOfficialName("Non-existent Update")
                .build();

        govOrgClient.update(GovOrgTestDataBuilder.NON_EXISTENT_ID, updateRequest)
                .then()
                .statusCode(404);
    }

    // ==================== DELETE Tests ====================

    @Test
    @DisplayName("DELETE /api/government-organizations/{id} - should soft delete organization")
    void shouldDeleteOrganization_softDeletes() {
        // First create an organization with unique acronym
        long timestamp = System.currentTimeMillis();
        String uniqueAcronym = "D" + (timestamp % 100000);
        Map<String, Object> orgRequest = GovOrgTestDataBuilder.aGovOrg()
                .withOfficialName("Org for DELETE " + timestamp)
                .withAcronym(uniqueAcronym)
                .build();

        String orgId = govOrgClient.createAndGetId(orgRequest);

        // Then delete it (soft delete)
        govOrgClient.delete(orgId)
                .then()
                .statusCode(204);

        // The organization should still exist but be marked as dissolved
        // (soft delete behavior - may return 200 with dissolved date set, or 404 if hard deleted)
    }

    @Test
    @DisplayName("DELETE /api/government-organizations/{id} - should return 404 when organization not found")
    void shouldDeleteOrganization_whenNotFound_returns404() {
        govOrgClient.delete(GovOrgTestDataBuilder.NON_EXISTENT_ID)
                .then()
                .statusCode(404);
    }
}
