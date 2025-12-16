package org.newsanalyzer.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.newsanalyzer.dto.*;
import org.newsanalyzer.model.Person;
import org.newsanalyzer.repository.PersonRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Unit tests for LegislatorsSearchService.
 *
 * Tests search/filter logic, pagination, caching, and local match detection.
 *
 * @author James (Dev Agent)
 */
@ExtendWith(MockitoExtension.class)
class LegislatorsSearchServiceTest {

    @Mock
    private LegislatorsRepoClient legislatorsRepoClient;

    @Mock
    private PersonRepository personRepository;

    private LegislatorsSearchService searchService;

    @BeforeEach
    void setUp() {
        searchService = new LegislatorsSearchService(legislatorsRepoClient, personRepository);
    }

    // =================================================================
    // Search Filter Tests
    // =================================================================

    @Test
    @DisplayName("Should return all legislators when no filters applied")
    void searchLegislators_noFilters_returnsAll() {
        // Given
        List<LegislatorYamlRecord> legislators = List.of(
                createTestRecord("S000033", "Bernard", "Sanders", "VT", "Independent"),
                createTestRecord("W000817", "Elizabeth", "Warren", "MA", "Democrat")
        );
        when(legislatorsRepoClient.fetchCurrentLegislators()).thenReturn(legislators);
        when(legislatorsRepoClient.fetchHistoricalLegislators()).thenReturn(List.of());

        // When
        var response = searchService.searchLegislators(null, null, null, 1, 20);

        // Then
        assertThat(response.getTotal()).isEqualTo(2);
        assertThat(response.getResults()).hasSize(2);
    }

    @Test
    @DisplayName("Should filter by name (partial match, case-insensitive)")
    void searchLegislators_nameFilter_returnsMatching() {
        // Given
        List<LegislatorYamlRecord> legislators = List.of(
                createTestRecord("S000033", "Bernard", "Sanders", "VT", "Independent"),
                createTestRecord("W000817", "Elizabeth", "Warren", "MA", "Democrat"),
                createTestRecord("S001234", "Jane", "Smith", "NY", "Democrat")
        );
        when(legislatorsRepoClient.fetchCurrentLegislators()).thenReturn(legislators);
        when(legislatorsRepoClient.fetchHistoricalLegislators()).thenReturn(List.of());

        // When
        var response = searchService.searchLegislators("sand", null, null, 1, 20);

        // Then
        assertThat(response.getTotal()).isEqualTo(1);
        assertThat(response.getResults().get(0).getData().getName()).contains("Sanders");
    }

    @Test
    @DisplayName("Should filter by exact bioguideId (case-insensitive)")
    void searchLegislators_bioguideIdFilter_returnsExactMatch() {
        // Given
        List<LegislatorYamlRecord> legislators = List.of(
                createTestRecord("S000033", "Bernard", "Sanders", "VT", "Independent"),
                createTestRecord("W000817", "Elizabeth", "Warren", "MA", "Democrat")
        );
        when(legislatorsRepoClient.fetchCurrentLegislators()).thenReturn(legislators);
        when(legislatorsRepoClient.fetchHistoricalLegislators()).thenReturn(List.of());

        // When
        var response = searchService.searchLegislators(null, "s000033", null, 1, 20);

        // Then
        assertThat(response.getTotal()).isEqualTo(1);
        assertThat(response.getResults().get(0).getData().getBioguideId()).isEqualTo("S000033");
    }

    @Test
    @DisplayName("Should filter by state (exact match)")
    void searchLegislators_stateFilter_returnsMatching() {
        // Given
        List<LegislatorYamlRecord> legislators = List.of(
                createTestRecord("S000033", "Bernard", "Sanders", "VT", "Independent"),
                createTestRecord("W000817", "Elizabeth", "Warren", "MA", "Democrat"),
                createTestRecord("L000123", "Patrick", "Leahy", "VT", "Democrat")
        );
        when(legislatorsRepoClient.fetchCurrentLegislators()).thenReturn(legislators);
        when(legislatorsRepoClient.fetchHistoricalLegislators()).thenReturn(List.of());

        // When
        var response = searchService.searchLegislators(null, null, "VT", 1, 20);

        // Then
        assertThat(response.getTotal()).isEqualTo(2);
        assertThat(response.getResults())
                .allSatisfy(r -> assertThat(r.getData().getState()).isEqualTo("VT"));
    }

    @Test
    @DisplayName("Should combine multiple filters (AND logic)")
    void searchLegislators_multipleFilters_combinesWithAnd() {
        // Given
        List<LegislatorYamlRecord> legislators = List.of(
                createTestRecord("S000033", "Bernard", "Sanders", "VT", "Independent"),
                createTestRecord("W000817", "Elizabeth", "Warren", "MA", "Democrat"),
                createTestRecord("L000123", "Patrick", "Leahy", "VT", "Democrat")
        );
        when(legislatorsRepoClient.fetchCurrentLegislators()).thenReturn(legislators);
        when(legislatorsRepoClient.fetchHistoricalLegislators()).thenReturn(List.of());

        // When - filter by state VT AND name containing "sand"
        var response = searchService.searchLegislators("sand", null, "VT", 1, 20);

        // Then
        assertThat(response.getTotal()).isEqualTo(1);
        assertThat(response.getResults().get(0).getData().getBioguideId()).isEqualTo("S000033");
    }

    @Test
    @DisplayName("Should return empty results when no matches")
    void searchLegislators_noMatches_returnsEmpty() {
        // Given
        List<LegislatorYamlRecord> legislators = List.of(
                createTestRecord("S000033", "Bernard", "Sanders", "VT", "Independent")
        );
        when(legislatorsRepoClient.fetchCurrentLegislators()).thenReturn(legislators);
        when(legislatorsRepoClient.fetchHistoricalLegislators()).thenReturn(List.of());

        // When
        var response = searchService.searchLegislators("xyz-nonexistent", null, null, 1, 20);

        // Then
        assertThat(response.getTotal()).isEqualTo(0);
        assertThat(response.getResults()).isEmpty();
    }

    // =================================================================
    // Pagination Tests
    // =================================================================

    @Test
    @DisplayName("Should paginate results correctly")
    void searchLegislators_pagination_returnsCorrectPage() {
        // Given
        List<LegislatorYamlRecord> legislators = List.of(
                createTestRecord("A000001", "Person", "One", "AL", "Democrat"),
                createTestRecord("B000002", "Person", "Two", "AK", "Republican"),
                createTestRecord("C000003", "Person", "Three", "AZ", "Democrat"),
                createTestRecord("D000004", "Person", "Four", "AR", "Republican"),
                createTestRecord("E000005", "Person", "Five", "CA", "Democrat")
        );
        when(legislatorsRepoClient.fetchCurrentLegislators()).thenReturn(legislators);
        when(legislatorsRepoClient.fetchHistoricalLegislators()).thenReturn(List.of());

        // When - get page 2 with pageSize 2
        var response = searchService.searchLegislators(null, null, null, 2, 2);

        // Then
        assertThat(response.getTotal()).isEqualTo(5);
        assertThat(response.getPage()).isEqualTo(2);
        assertThat(response.getPageSize()).isEqualTo(2);
        assertThat(response.getResults()).hasSize(2);
        assertThat(response.getResults().get(0).getData().getBioguideId()).isEqualTo("C000003");
        assertThat(response.getResults().get(1).getData().getBioguideId()).isEqualTo("D000004");
    }

    @Test
    @DisplayName("Should return partial page when at end of results")
    void searchLegislators_lastPage_returnsPartialResults() {
        // Given
        List<LegislatorYamlRecord> legislators = List.of(
                createTestRecord("A000001", "Person", "One", "AL", "Democrat"),
                createTestRecord("B000002", "Person", "Two", "AK", "Republican"),
                createTestRecord("C000003", "Person", "Three", "AZ", "Democrat")
        );
        when(legislatorsRepoClient.fetchCurrentLegislators()).thenReturn(legislators);
        when(legislatorsRepoClient.fetchHistoricalLegislators()).thenReturn(List.of());

        // When - get page 2 with pageSize 2 (only 1 result left)
        var response = searchService.searchLegislators(null, null, null, 2, 2);

        // Then
        assertThat(response.getTotal()).isEqualTo(3);
        assertThat(response.getResults()).hasSize(1);
    }

    @Test
    @DisplayName("Should return empty when page exceeds results")
    void searchLegislators_pageExceedsResults_returnsEmpty() {
        // Given
        List<LegislatorYamlRecord> legislators = List.of(
                createTestRecord("A000001", "Person", "One", "AL", "Democrat")
        );
        when(legislatorsRepoClient.fetchCurrentLegislators()).thenReturn(legislators);
        when(legislatorsRepoClient.fetchHistoricalLegislators()).thenReturn(List.of());

        // When - request page 5 when only 1 result exists
        var response = searchService.searchLegislators(null, null, null, 5, 20);

        // Then
        assertThat(response.getTotal()).isEqualTo(1);
        assertThat(response.getResults()).isEmpty();
    }

    // =================================================================
    // Local Match Detection Tests
    // =================================================================

    @Test
    @DisplayName("Should detect local match when Person exists")
    void searchLegislators_personExists_setsLocalMatchId() {
        // Given
        UUID personId = UUID.randomUUID();
        Person existingPerson = new Person();
        existingPerson.setId(personId);
        existingPerson.setBioguideId("S000033");

        List<LegislatorYamlRecord> legislators = List.of(
                createTestRecord("S000033", "Bernard", "Sanders", "VT", "Independent")
        );
        when(legislatorsRepoClient.fetchCurrentLegislators()).thenReturn(legislators);
        when(legislatorsRepoClient.fetchHistoricalLegislators()).thenReturn(List.of());
        when(personRepository.findByBioguideId("S000033")).thenReturn(Optional.of(existingPerson));

        // When
        var response = searchService.searchLegislators(null, null, null, 1, 20);

        // Then
        assertThat(response.getResults().get(0).getLocalMatchId()).isEqualTo(personId.toString());
    }

    @Test
    @DisplayName("Should not set localMatchId when Person does not exist")
    void searchLegislators_personNotExists_localMatchIdNull() {
        // Given
        List<LegislatorYamlRecord> legislators = List.of(
                createTestRecord("S000033", "Bernard", "Sanders", "VT", "Independent")
        );
        when(legislatorsRepoClient.fetchCurrentLegislators()).thenReturn(legislators);
        when(legislatorsRepoClient.fetchHistoricalLegislators()).thenReturn(List.of());
        when(personRepository.findByBioguideId("S000033")).thenReturn(Optional.empty());

        // When
        var response = searchService.searchLegislators(null, null, null, 1, 20);

        // Then
        assertThat(response.getResults().get(0).getLocalMatchId()).isNull();
    }

    // =================================================================
    // Caching Tests
    // =================================================================

    @Test
    @DisplayName("Should cache legislators data")
    void searchLegislators_calledTwice_usesCache() {
        // Given
        List<LegislatorYamlRecord> legislators = List.of(
                createTestRecord("S000033", "Bernard", "Sanders", "VT", "Independent")
        );
        when(legislatorsRepoClient.fetchCurrentLegislators()).thenReturn(legislators);
        when(legislatorsRepoClient.fetchHistoricalLegislators()).thenReturn(List.of());

        // When - call twice
        searchService.searchLegislators(null, null, null, 1, 20);
        searchService.searchLegislators(null, null, null, 1, 20);

        // Then - should only fetch once (cached)
        verify(legislatorsRepoClient, times(1)).fetchCurrentLegislators();
        verify(legislatorsRepoClient, times(1)).fetchHistoricalLegislators();
    }

    @Test
    @DisplayName("Should indicate cached status in response")
    void searchLegislators_fromCache_returnsCachedTrue() {
        // Given
        List<LegislatorYamlRecord> legislators = List.of(
                createTestRecord("S000033", "Bernard", "Sanders", "VT", "Independent")
        );
        when(legislatorsRepoClient.fetchCurrentLegislators()).thenReturn(legislators);
        when(legislatorsRepoClient.fetchHistoricalLegislators()).thenReturn(List.of());

        // When - first call populates cache
        var response1 = searchService.searchLegislators(null, null, null, 1, 20);
        // Second call should use cache
        var response2 = searchService.searchLegislators(null, null, null, 1, 20);

        // Then
        assertThat(response2.isCached()).isTrue();
    }

    // =================================================================
    // Deduplication Tests
    // =================================================================

    @Test
    @DisplayName("Should prefer current legislators over historical")
    void searchLegislators_duplicateBioguide_prefersCurrent() {
        // Given - same person in both current and historical
        LegislatorYamlRecord currentRecord = createTestRecord("S000033", "Bernard", "Sanders", "VT", "Independent");
        LegislatorYamlRecord historicalRecord = createTestRecord("S000033", "Bernard", "Sanders", "VT", "Democrat");

        when(legislatorsRepoClient.fetchCurrentLegislators()).thenReturn(List.of(currentRecord));
        when(legislatorsRepoClient.fetchHistoricalLegislators()).thenReturn(List.of(historicalRecord));

        // When
        var response = searchService.searchLegislators(null, null, null, 1, 20);

        // Then - should have only 1 result, from current (Independent)
        assertThat(response.getTotal()).isEqualTo(1);
        assertThat(response.getResults().get(0).getData().getParty()).isEqualTo("Independent");
    }

    // =================================================================
    // Get Legislator By BioguideId Tests
    // =================================================================

    @Test
    @DisplayName("Should return legislator details by bioguideId")
    void getLegislatorByBioguideId_exists_returnsDetails() {
        // Given
        List<LegislatorYamlRecord> legislators = List.of(
                createTestRecord("S000033", "Bernard", "Sanders", "VT", "Independent")
        );
        when(legislatorsRepoClient.fetchCurrentLegislators()).thenReturn(legislators);
        when(legislatorsRepoClient.fetchHistoricalLegislators()).thenReturn(List.of());

        // When
        var result = searchService.getLegislatorByBioguideId("S000033");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getBioguideId()).isEqualTo("S000033");
        assertThat(result.get().getName().getFirst()).isEqualTo("Bernard");
        assertThat(result.get().getName().getLast()).isEqualTo("Sanders");
    }

    @Test
    @DisplayName("Should return empty when bioguideId not found")
    void getLegislatorByBioguideId_notExists_returnsEmpty() {
        // Given
        when(legislatorsRepoClient.fetchCurrentLegislators()).thenReturn(List.of());
        when(legislatorsRepoClient.fetchHistoricalLegislators()).thenReturn(List.of());

        // When
        var result = searchService.getLegislatorByBioguideId("NONEXISTENT");

        // Then
        assertThat(result).isEmpty();
    }

    // =================================================================
    // DTO Mapping Tests
    // =================================================================

    @Test
    @DisplayName("Should map social media counts correctly")
    void searchLegislators_withSocialMedia_countsCorrectly() {
        // Given
        LegislatorYamlRecord record = createTestRecord("S000033", "Bernard", "Sanders", "VT", "Independent");
        record.getSocial().setTwitter("SenSanders");
        record.getSocial().setFacebook("senatorsanders");
        record.getSocial().setYoutube("SenatorBernie");

        when(legislatorsRepoClient.fetchCurrentLegislators()).thenReturn(List.of(record));
        when(legislatorsRepoClient.fetchHistoricalLegislators()).thenReturn(List.of());

        // When
        var response = searchService.searchLegislators(null, null, null, 1, 20);

        // Then
        var dto = response.getResults().get(0).getData();
        assertThat(dto.getSocialMediaCount()).isEqualTo(3);
        assertThat(dto.getSocialMedia()).containsKeys("twitter", "facebook", "youtube");
    }

    @Test
    @DisplayName("Should map external IDs counts correctly")
    void searchLegislators_withExternalIds_countsCorrectly() {
        // Given
        LegislatorYamlRecord record = createTestRecord("S000033", "Bernard", "Sanders", "VT", "Independent");
        record.getId().setGovtrack(400357);
        record.getId().setOpensecrets("N00000528");
        record.getId().setVotesmart(27110);

        when(legislatorsRepoClient.fetchCurrentLegislators()).thenReturn(List.of(record));
        when(legislatorsRepoClient.fetchHistoricalLegislators()).thenReturn(List.of());

        // When
        var response = searchService.searchLegislators(null, null, null, 1, 20);

        // Then
        var dto = response.getResults().get(0).getData();
        assertThat(dto.getExternalIdCount()).isGreaterThanOrEqualTo(3);
        assertThat(dto.getExternalIds()).containsKeys("govtrack", "opensecrets", "votesmart");
    }

    @Test
    @DisplayName("Should map chamber correctly (sen -> Senate, rep -> House)")
    void searchLegislators_chamberMapping_mapsCorrectly() {
        // Given
        LegislatorYamlRecord senator = createTestRecord("S000033", "Bernard", "Sanders", "VT", "Independent");
        senator.getTerms().get(0).setType("sen");

        LegislatorYamlRecord representative = createTestRecord("P000197", "Nancy", "Pelosi", "CA", "Democrat");
        representative.getTerms().get(0).setType("rep");

        when(legislatorsRepoClient.fetchCurrentLegislators()).thenReturn(List.of(senator, representative));
        when(legislatorsRepoClient.fetchHistoricalLegislators()).thenReturn(List.of());

        // When
        var response = searchService.searchLegislators(null, null, null, 1, 20);

        // Then
        assertThat(response.getResults())
                .extracting(r -> r.getData().getChamber())
                .containsExactlyInAnyOrder("Senate", "House");
    }

    // =================================================================
    // Helper Methods
    // =================================================================

    private LegislatorYamlRecord createTestRecord(String bioguideId, String firstName, String lastName,
                                                   String state, String party) {
        LegislatorYamlRecord record = new LegislatorYamlRecord();

        LegislatorYamlRecord.LegislatorId id = new LegislatorYamlRecord.LegislatorId();
        id.setBioguide(bioguideId);
        record.setId(id);

        LegislatorYamlRecord.LegislatorName name = new LegislatorYamlRecord.LegislatorName();
        name.setFirst(firstName);
        name.setLast(lastName);
        record.setName(name);

        LegislatorYamlRecord.LegislatorTerm term = new LegislatorYamlRecord.LegislatorTerm();
        term.setState(state);
        term.setParty(party);
        term.setType("sen");
        term.setStart("2023-01-03");
        term.setEnd("2029-01-03");
        record.setTerms(List.of(term));

        LegislatorYamlRecord.LegislatorSocial social = new LegislatorYamlRecord.LegislatorSocial();
        record.setSocial(social);

        return record;
    }
}
