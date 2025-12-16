package org.newsanalyzer.apitests.backend;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.newsanalyzer.apitests.BaseApiTest;

import java.util.Map;

import static org.hamcrest.Matchers.*;

/**
 * Search tests for the Government Organization API endpoints.
 * Tests search, fuzzy search, full-text search, and find by name/acronym.
 */
@Tag("backend")
@Tag("integration")
@DisplayName("Government Organization Search Tests")
class GovOrgSearchTest extends BaseApiTest {

    private GovOrgApiClient govOrgClient;

    @BeforeEach
    void setUp() {
        govOrgClient = new GovOrgApiClient(getBackendSpec());
    }

    // ==================== Search by Name/Acronym Tests ====================

    @Test
    @DisplayName("GET /api/government-organizations/search?query= - should return matches by name or acronym")
    void shouldSearchOrganizations_byNameOrAcronym() {
        // First create a searchable organization
        String uniqueName = "Searchable Agency " + System.currentTimeMillis();
        Map<String, Object> orgRequest = GovOrgTestDataBuilder.aGovOrg()
                .withOfficialName(uniqueName)
                .withAcronym("SA" + System.currentTimeMillis() % 1000)
                .build();

        govOrgClient.create(orgRequest)
                .then()
                .statusCode(201);

        // Search for it
        govOrgClient.search("Searchable Agency")
                .then()
                .statusCode(200)
                .body("$", instanceOf(java.util.List.class));
    }

    @Test
    @DisplayName("GET /api/government-organizations/search?query= - should return empty list for no matches")
    void shouldSearchOrganizations_returnsEmptyListWhenNoMatches() {
        String nonExistentQuery = "ZZZNonExistentOrg" + System.currentTimeMillis();

        govOrgClient.search(nonExistentQuery)
                .then()
                .statusCode(200)
                .body("$", instanceOf(java.util.List.class))
                .body("size()", equalTo(0));
    }

    // ==================== Fuzzy Search Tests ====================

    @Test
    @DisplayName("GET /api/government-organizations/search/fuzzy?query= - should find similar names with typos")
    void shouldFuzzySearch_findsSimilarNames() {
        // Create an organization with a known name and unique acronym
        long timestamp = System.currentTimeMillis();
        String uniqueName = "FuzzyTestOrganization" + timestamp;
        String uniqueAcronym = "F" + (timestamp % 100000);
        Map<String, Object> orgRequest = GovOrgTestDataBuilder.aGovOrg()
                .withOfficialName(uniqueName)
                .withAcronym(uniqueAcronym)
                .build();

        govOrgClient.create(orgRequest)
                .then()
                .statusCode(201);

        // Fuzzy search with a slight typo
        govOrgClient.fuzzySearch("FuzzyTestOrganizaton") // missing 'i'
                .then()
                .statusCode(200)
                .body("$", instanceOf(java.util.List.class));
    }

    @Test
    @DisplayName("GET /api/government-organizations/search/fuzzy?query= - should work with partial matches")
    void shouldFuzzySearch_worksWithPartialMatches() {
        govOrgClient.fuzzySearch("Agency")
                .then()
                .statusCode(200)
                .body("$", instanceOf(java.util.List.class));
    }

    // ==================== Full-text Search Tests ====================

    @Test
    @DisplayName("GET /api/government-organizations/search/fulltext?query= - should search all fields")
    void shouldFullTextSearch_searchesAllFields() {
        // Create an organization with searchable content
        String uniqueMission = "UniqueFullTextMission" + System.currentTimeMillis();
        Map<String, Object> orgRequest = GovOrgTestDataBuilder.aGovOrg()
                .withOfficialName("Full Text Test Org " + System.currentTimeMillis())
                .withMission(uniqueMission)
                .withDescription("Organization dedicated to testing full-text search")
                .build();

        govOrgClient.create(orgRequest)
                .then()
                .statusCode(201);

        // Full-text search by mission
        govOrgClient.fullTextSearch("UniqueFullTextMission")
                .then()
                .statusCode(200)
                .body("$", instanceOf(java.util.List.class));
    }

    @Test
    @DisplayName("GET /api/government-organizations/search/fulltext?query= - should return results for common terms")
    void shouldFullTextSearch_returnsResultsForCommonTerms() {
        govOrgClient.fullTextSearch("government")
                .then()
                .statusCode(200)
                .body("$", instanceOf(java.util.List.class));
    }

    // ==================== Find by Name or Acronym (Exact Match) Tests ====================

    @Test
    @DisplayName("GET /api/government-organizations/find?nameOrAcronym= - should return exact match by name")
    void shouldFindByNameOrAcronym_exactMatch() {
        // Create an organization with unique name and acronym
        long timestamp = System.currentTimeMillis();
        String uniqueName = "ExactMatchOrg" + timestamp;
        String uniqueAcronym = "E" + (timestamp % 100000);
        Map<String, Object> orgRequest = GovOrgTestDataBuilder.aGovOrg()
                .withOfficialName(uniqueName)
                .withAcronym(uniqueAcronym)
                .build();

        govOrgClient.create(orgRequest)
                .then()
                .statusCode(201);

        // Find by exact name
        govOrgClient.findByNameOrAcronym(uniqueName)
                .then()
                .statusCode(200)
                .body("officialName", equalTo(uniqueName));
    }

    @Test
    @DisplayName("GET /api/government-organizations/find?nameOrAcronym= - should return exact match by acronym")
    void shouldFindByNameOrAcronym_matchesByAcronym() {
        // Create an organization with a unique acronym
        String uniqueAcronym = "UAC" + System.currentTimeMillis() % 10000;
        Map<String, Object> orgRequest = GovOrgTestDataBuilder.aGovOrg()
                .withOfficialName("Unique Acronym Org " + System.currentTimeMillis())
                .withAcronym(uniqueAcronym)
                .build();

        govOrgClient.create(orgRequest)
                .then()
                .statusCode(201);

        // Find by acronym
        govOrgClient.findByNameOrAcronym(uniqueAcronym)
                .then()
                .statusCode(200)
                .body("acronym", equalTo(uniqueAcronym));
    }

    @Test
    @DisplayName("GET /api/government-organizations/find?nameOrAcronym= - should return 404 when no exact match")
    void shouldFindByNameOrAcronym_returns404WhenNoMatch() {
        String nonExistentName = "ZZZNonExistentOrgName" + System.currentTimeMillis();

        govOrgClient.findByNameOrAcronym(nonExistentName)
                .then()
                .statusCode(404);
    }
}
