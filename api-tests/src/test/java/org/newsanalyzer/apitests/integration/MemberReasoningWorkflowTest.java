package org.newsanalyzer.apitests.integration;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.newsanalyzer.apitests.backend.MemberApiClient;
import org.newsanalyzer.apitests.data.DatabaseIntegrationTest.SeedPersons;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * Integration tests for Member API in cross-service workflows.
 * Tests scenarios where Congressional member data is used alongside
 * entity extraction and reasoning services.
 *
 * @author James (Dev Agent)
 */
@Tag("integration")
@DisplayName("Member Reasoning Workflow Integration Tests")
class MemberReasoningWorkflowTest extends IntegrationTestBase {

    private MemberApiClient memberClient;

    // Sample text mentioning Congress members
    private static final String CONGRESS_ARTICLE = """
            Senator Bernie Sanders (I-VT) proposed new legislation on healthcare reform today.
            The bill has gained support from Senator Elizabeth Warren (D-MA) and Representative
            Alexandria Ocasio-Cortez (D-NY). Senate Minority Leader Mitch McConnell (R-KY)
            has indicated opposition to the measure.
            """;

    private static final String CONGRESS_MEMBER_TEXT = "Senator Bernie Sanders from Vermont introduced the bill.";

    @BeforeAll
    void setupMemberClient() {
        memberClient = new MemberApiClient(getBackendSpec());
    }

    // ==================== Member Lookup Workflow Tests ====================

    @Test
    @DisplayName("Should lookup member after entity extraction identifies a person")
    void shouldLookupMemberAfterEntityExtraction() {
        Instant start = startTiming();

        // Step 1: Extract entities from text mentioning a senator
        var extractionResponse = reasoningClient.extractEntities(CONGRESS_MEMBER_TEXT);
        endTiming(start, "Entity Extraction");

        extractionResponse
                .then()
                .statusCode(200);

        // Step 2: Lookup the member by known BioGuide ID (simulating post-extraction lookup)
        start = startTiming();
        var memberResponse = memberClient.getByBioguideId(SeedPersons.SANDERS_BIOGUIDE);
        endTiming(start, "Member Lookup");

        memberResponse
                .then()
                .statusCode(200)
                .body("bioguideId", equalTo(SeedPersons.SANDERS_BIOGUIDE))
                .body("firstName", equalTo("Bernard"))
                .body("lastName", equalTo("Sanders"))
                .body("state", equalTo("VT"))
                .body("chamber", equalTo("SENATE"));

        printTimingSummary();
    }

    @Test
    @DisplayName("Should verify member data consistency across services")
    void shouldVerifyMemberDataConsistency() {
        Instant start = startTiming();

        // Step 1: Get all seed members
        var senatorsResponse = memberClient.getByChamber("SENATE");
        endTiming(start, "Get Senators");

        senatorsResponse
                .then()
                .statusCode(200)
                .body("content.size()", greaterThanOrEqualTo(SeedPersons.SENATE_COUNT));

        start = startTiming();
        var houseMembersResponse = memberClient.getByChamber("HOUSE");
        endTiming(start, "Get House Members");

        houseMembersResponse
                .then()
                .statusCode(200)
                .body("content.size()", greaterThanOrEqualTo(SeedPersons.HOUSE_COUNT));

        // Step 2: Verify total count matches expectations
        start = startTiming();
        var countResponse = memberClient.getCount();
        endTiming(start, "Get Total Count");

        String countString = countResponse.body().asString();
        int totalCount = Integer.parseInt(countString);
        assertThat(totalCount).isGreaterThanOrEqualTo(SeedPersons.TOTAL_PERSON_COUNT);

        printTimingSummary();
    }

    @Test
    @DisplayName("Should correlate extracted person entities with member database")
    void shouldCorrelateExtractedEntitiesWithMembers() {
        Instant start = startTiming();

        // Step 1: Extract entities from article mentioning multiple members
        var extractionResponse = reasoningClient.extractEntities(CONGRESS_ARTICLE);
        endTiming(start, "Extract Entities from Article");

        extractionResponse
                .then()
                .statusCode(200);

        // Step 2: Search for each known member mentioned
        String[] membersToFind = {"Sanders", "Warren", "Ocasio-Cortez", "McConnell"};

        for (String name : membersToFind) {
            start = startTiming();
            var searchResponse = memberClient.searchByName(name);
            endTiming(start, "Search: " + name);

            searchResponse
                    .then()
                    .statusCode(200)
                    .body("content.size()", greaterThanOrEqualTo(1));
        }

        printTimingSummary();
    }

    // ==================== Member Statistics Workflow Tests ====================

    @Test
    @DisplayName("Should aggregate member statistics for fact-checking context")
    void shouldAggregateMemberStatistics() {
        Instant start = startTiming();

        // Step 1: Get party distribution
        var partyStatsResponse = memberClient.getPartyDistribution();
        endTiming(start, "Get Party Distribution");

        partyStatsResponse
                .then()
                .statusCode(200)
                .body("$", instanceOf(java.util.List.class));

        // Step 2: Get state distribution
        start = startTiming();
        var stateStatsResponse = memberClient.getStateDistribution();
        endTiming(start, "Get State Distribution");

        stateStatsResponse
                .then()
                .statusCode(200)
                .body("$", instanceOf(java.util.List.class));

        // Step 3: Get total count
        start = startTiming();
        var countResponse = memberClient.getCount();
        endTiming(start, "Get Total Count");

        countResponse
                .then()
                .statusCode(200);

        // Verify all operations completed within acceptable time
        assertTotalTimeUnder(VALIDATION_THRESHOLD_MS * 3, "Member Statistics Aggregation");

        printTimingSummary();
    }

    // ==================== Multi-filter Workflow Tests ====================

    @Test
    @DisplayName("Should filter members by multiple criteria for targeted lookup")
    void shouldFilterByMultipleCriteria() {
        Instant start = startTiming();

        // Step 1: Filter by state (TX - should have Ted Cruz and Al Green in seed)
        var texasMembersResponse = memberClient.getByState("TX");
        endTiming(start, "Filter by State (TX)");

        texasMembersResponse
                .then()
                .statusCode(200)
                .body("content.size()", greaterThanOrEqualTo(1));

        // Step 2: Filter by chamber (SENATE)
        start = startTiming();
        var senatorsResponse = memberClient.getByChamber("SENATE");
        endTiming(start, "Filter by Chamber (SENATE)");

        senatorsResponse
                .then()
                .statusCode(200)
                .body("content.size()", greaterThanOrEqualTo(1));

        // Step 3: Search by party-related name pattern
        start = startTiming();
        var searchResponse = memberClient.searchByName("Cruz");
        endTiming(start, "Search by Name (Cruz)");

        searchResponse
                .then()
                .statusCode(200)
                .body("content.size()", greaterThanOrEqualTo(1))
                .body("content[0].party", equalTo("Republican"));

        printTimingSummary();
    }

    // ==================== Performance Tests ====================

    @Test
    @DisplayName("Should complete member lookup under 500ms")
    void shouldCompleteMemberLookupUnderThreshold() {
        Instant start = startTiming();

        memberClient.getByBioguideId(SeedPersons.SANDERS_BIOGUIDE)
                .then()
                .statusCode(200);

        long duration = endTiming(start, "Single Member Lookup");

        assertThat(duration)
                .as("Member lookup should complete under 500ms")
                .isLessThan(STORAGE_THRESHOLD_MS);
    }

    @Test
    @DisplayName("Should complete paginated list under 500ms")
    void shouldCompletePaginatedListUnderThreshold() {
        Instant start = startTiming();

        memberClient.listAll(0, 20)
                .then()
                .statusCode(200);

        long duration = endTiming(start, "Paginated List (20 items)");

        assertThat(duration)
                .as("Paginated list should complete under 500ms")
                .isLessThan(STORAGE_THRESHOLD_MS);
    }
}
