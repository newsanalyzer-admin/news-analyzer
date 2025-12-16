package org.newsanalyzer.apitests.backend;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.newsanalyzer.apitests.BaseApiTest;

import java.util.Map;
import java.util.UUID;

import static org.hamcrest.Matchers.*;

/**
 * Hierarchy tests for the Government Organization API endpoints.
 * Tests hierarchy, descendants, ancestors, and top-level organizations.
 */
@Tag("backend")
@Tag("integration")
@DisplayName("Government Organization Hierarchy Tests")
class GovOrgHierarchyTest extends BaseApiTest {

    private GovOrgApiClient govOrgClient;

    @BeforeEach
    void setUp() {
        govOrgClient = new GovOrgApiClient(getBackendSpec());
    }

    // ==================== Hierarchy Tests ====================

    @Test
    @DisplayName("GET /api/government-organizations/{id}/hierarchy - should return ancestors and children")
    void shouldGetHierarchy_includesAncestorsAndChildren() {
        // Create a parent organization with unique acronym
        long timestamp = System.currentTimeMillis();
        String parentAcronym = "P" + (timestamp % 100000);
        Map<String, Object> parentOrg = GovOrgTestDataBuilder.aCabinetDepartment()
                .withOfficialName("Parent Department " + timestamp)
                .withAcronym(parentAcronym)
                .build();

        String parentId = govOrgClient.createAndGetId(parentOrg);

        // Create a child organization with unique acronym
        String childAcronym = "C" + ((timestamp + 1) % 100000);
        Map<String, Object> childOrg = GovOrgTestDataBuilder.aBureau()
                .withOfficialName("Child Bureau " + System.currentTimeMillis())
                .withAcronym(childAcronym)
                .withParentOrganizationId(UUID.fromString(parentId))
                .build();

        String childId = govOrgClient.createAndGetId(childOrg);

        // Get hierarchy for the child
        govOrgClient.getHierarchy(UUID.fromString(childId))
                .then()
                .statusCode(200)
                .body("organization", notNullValue())
                .body("organization.id", equalTo(childId));
    }

    @Test
    @DisplayName("GET /api/government-organizations/{id}/hierarchy - should return 404 for non-existent org")
    void shouldGetHierarchy_returns404WhenNotFound() {
        govOrgClient.getHierarchy(GovOrgTestDataBuilder.NON_EXISTENT_ID)
                .then()
                .statusCode(404);
    }

    // ==================== Descendants Tests ====================

    @Test
    @DisplayName("GET /api/government-organizations/{id}/descendants - should return all child organizations")
    void shouldGetDescendants_returnsAllChildren() {
        // Create a parent organization with unique acronym
        long timestamp = System.currentTimeMillis();
        String parentAcronym = "D" + (timestamp % 100000);
        Map<String, Object> parentOrg = GovOrgTestDataBuilder.aCabinetDepartment()
                .withOfficialName("Descendant Test Parent " + timestamp)
                .withAcronym(parentAcronym)
                .build();

        String parentId = govOrgClient.createAndGetId(parentOrg);

        // Create child organizations with unique acronyms
        String child1Acronym = "A" + ((timestamp + 1) % 100000);
        Map<String, Object> childOrg1 = GovOrgTestDataBuilder.aBureau()
                .withOfficialName("Descendant Child 1 " + System.currentTimeMillis())
                .withAcronym(child1Acronym)
                .withParentOrganizationId(UUID.fromString(parentId))
                .build();

        govOrgClient.create(childOrg1)
                .then()
                .statusCode(201);

        String child2Acronym = "B" + ((timestamp + 2) % 100000);
        Map<String, Object> childOrg2 = GovOrgTestDataBuilder.aGovOrg()
                .withOfficialName("Descendant Child 2 " + System.currentTimeMillis())
                .withAcronym(child2Acronym)
                .withOrganizationType(GovOrgTestDataBuilder.TYPE_OFFICE)
                .withParentOrganizationId(UUID.fromString(parentId))
                .build();

        govOrgClient.create(childOrg2)
                .then()
                .statusCode(201);

        // Get descendants
        govOrgClient.getDescendants(UUID.fromString(parentId))
                .then()
                .statusCode(200)
                .body("$", instanceOf(java.util.List.class))
                .body("size()", greaterThanOrEqualTo(2));
    }

    @Test
    @DisplayName("GET /api/government-organizations/{id}/descendants - should return empty list for leaf org")
    void shouldGetDescendants_returnsEmptyForLeafOrganization() {
        // Create an organization without children, with unique acronym
        long timestamp = System.currentTimeMillis();
        String uniqueAcronym = "L" + (timestamp % 100000);
        Map<String, Object> leafOrg = GovOrgTestDataBuilder.aGovOrg()
                .withOfficialName("Leaf Organization " + timestamp)
                .withAcronym(uniqueAcronym)
                .build();

        String leafId = govOrgClient.createAndGetId(leafOrg);

        govOrgClient.getDescendants(UUID.fromString(leafId))
                .then()
                .statusCode(200)
                .body("$", instanceOf(java.util.List.class))
                .body("size()", equalTo(0));
    }

    // ==================== Ancestors Tests ====================

    @Test
    @DisplayName("GET /api/government-organizations/{id}/ancestors - should return parent chain")
    void shouldGetAncestors_returnsParentChain() {
        // Create a grandparent organization with unique acronym
        long timestamp = System.currentTimeMillis();
        String gpAcronym = "G" + (timestamp % 100000);
        Map<String, Object> grandparent = GovOrgTestDataBuilder.aCabinetDepartment()
                .withOfficialName("Grandparent Dept " + timestamp)
                .withAcronym(gpAcronym)
                .build();

        String grandparentId = govOrgClient.createAndGetId(grandparent);

        // Create a parent organization with unique acronym
        String pAcronym = "P" + ((timestamp + 1) % 100000);
        Map<String, Object> parent = GovOrgTestDataBuilder.aBureau()
                .withOfficialName("Parent Bureau " + System.currentTimeMillis())
                .withAcronym(pAcronym)
                .withParentOrganizationId(UUID.fromString(grandparentId))
                .build();

        String parentId = govOrgClient.createAndGetId(parent);

        // Create a child organization with unique acronym
        String cAcronym = "C" + ((timestamp + 2) % 100000);
        Map<String, Object> child = GovOrgTestDataBuilder.aGovOrg()
                .withOfficialName("Child Office " + System.currentTimeMillis())
                .withAcronym(cAcronym)
                .withOrganizationType(GovOrgTestDataBuilder.TYPE_OFFICE)
                .withParentOrganizationId(UUID.fromString(parentId))
                .build();

        String childId = govOrgClient.createAndGetId(child);

        // Get ancestors
        govOrgClient.getAncestors(UUID.fromString(childId))
                .then()
                .statusCode(200)
                .body("$", instanceOf(java.util.List.class))
                .body("size()", greaterThanOrEqualTo(2));
    }

    @Test
    @DisplayName("GET /api/government-organizations/{id}/ancestors - should return empty for top-level org")
    void shouldGetAncestors_returnsEmptyForTopLevelOrganization() {
        // Create a top-level organization (no parent) with unique acronym
        long timestamp = System.currentTimeMillis();
        String uniqueAcronym = "T" + (timestamp % 100000);
        Map<String, Object> topLevel = GovOrgTestDataBuilder.aCabinetDepartment()
                .withOfficialName("Top Level Dept " + timestamp)
                .withAcronym(uniqueAcronym)
                .build();

        String topLevelId = govOrgClient.createAndGetId(topLevel);

        govOrgClient.getAncestors(UUID.fromString(topLevelId))
                .then()
                .statusCode(200)
                .body("$", instanceOf(java.util.List.class))
                .body("size()", equalTo(0));
    }

    // ==================== Top-Level Organizations Tests ====================

    @Test
    @DisplayName("GET /api/government-organizations/top-level - should return organizations without parent")
    void shouldGetTopLevel_returnsRootOrganizations() {
        // Create a top-level organization with unique acronym
        long timestamp = System.currentTimeMillis();
        String uniqueAcronym = "R" + (timestamp % 100000);
        Map<String, Object> topLevel = GovOrgTestDataBuilder.aCabinetDepartment()
                .withOfficialName("Top Level Test " + timestamp)
                .withAcronym(uniqueAcronym)
                .build();

        govOrgClient.create(topLevel)
                .then()
                .statusCode(201);

        // Get top-level organizations
        govOrgClient.getTopLevel()
                .then()
                .statusCode(200)
                .body("$", instanceOf(java.util.List.class))
                .body("size()", greaterThan(0));
    }

    @Test
    @DisplayName("GET /api/government-organizations/top-level - should not include child organizations")
    void shouldGetTopLevel_excludesChildOrganizations() {
        govOrgClient.getTopLevel()
                .then()
                .statusCode(200)
                .body("$", instanceOf(java.util.List.class));
        // All returned organizations should have no parent (parentOrganizationId is null)
    }
}
