package org.newsanalyzer.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.newsanalyzer.dto.CongressMemberSearchDTO;
import org.newsanalyzer.dto.CongressSearchResponse;
import org.newsanalyzer.dto.CongressSearchResult;
import org.newsanalyzer.model.Person;
import org.newsanalyzer.repository.PersonRepository;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for CongressSearchService.
 *
 * Tests search logic, filtering, DTO mapping, and duplicate detection.
 *
 * @author Quinn (QA Agent)
 * @since 2.0.0
 */
@ExtendWith(MockitoExtension.class)
class CongressSearchServiceTest {

    @Mock
    private CongressApiClient congressApiClient;

    @Mock
    private PersonRepository personRepository;

    private ObjectMapper objectMapper;
    private CongressSearchService searchService;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        searchService = new CongressSearchService(congressApiClient, personRepository);
    }

    // =========================================================================
    // Search Members Tests
    // =========================================================================

    @Test
    @DisplayName("Should return search results from Congress.gov API")
    void searchMembers_validRequest_returnsResults() throws Exception {
        // Given
        String apiResponse = """
            {
              "members": [
                {
                  "bioguideId": "S000033",
                  "name": "Sanders, Bernard",
                  "state": "VT",
                  "partyName": "Independent",
                  "currentMember": true,
                  "terms": [{"chamber": "Senate"}],
                  "depiction": {"imageUrl": "https://example.com/image.jpg"}
                }
              ],
              "pagination": {"count": 1}
            }
            """;
        JsonNode response = objectMapper.readTree(apiResponse);
        when(congressApiClient.fetchMembers(eq(20), eq(0), eq(true))).thenReturn(Optional.of(response));
        when(congressApiClient.getRequestCount()).thenReturn(1);
        when(personRepository.findByBioguideId("S000033")).thenReturn(Optional.empty());

        // When
        CongressSearchResponse<CongressMemberSearchDTO> result = searchService.searchMembers(
                null, null, null, null, null, 1, 20);

        // Then
        assertThat(result.getResults()).hasSize(1);
        assertThat(result.getTotal()).isEqualTo(1);
        assertThat(result.getPage()).isEqualTo(1);
        assertThat(result.getPageSize()).isEqualTo(20);

        CongressSearchResult<CongressMemberSearchDTO> firstResult = result.getResults().get(0);
        assertThat(firstResult.getSource()).isEqualTo("Congress.gov");
        assertThat(firstResult.getDuplicateId()).isNull();

        CongressMemberSearchDTO member = firstResult.getData();
        assertThat(member.getBioguideId()).isEqualTo("S000033");
        assertThat(member.getName()).isEqualTo("Sanders, Bernard");
        assertThat(member.getState()).isEqualTo("VT");
        assertThat(member.getParty()).isEqualTo("I"); // Mapped from "Independent"
        assertThat(member.getChamber()).isEqualTo("senate");
        assertThat(member.isCurrentMember()).isTrue();
    }

    @Test
    @DisplayName("Should return empty results when API returns no data")
    void searchMembers_noApiResponse_returnsEmptyResults() {
        // Given
        when(congressApiClient.fetchMembers(eq(20), eq(0), eq(true))).thenReturn(Optional.empty());

        // When
        CongressSearchResponse<CongressMemberSearchDTO> result = searchService.searchMembers(
                null, null, null, null, null, 1, 20);

        // Then
        assertThat(result.getResults()).isEmpty();
        assertThat(result.getTotal()).isEqualTo(0);
    }

    @Test
    @DisplayName("Should filter results by name")
    void searchMembers_nameFilter_filtersCorrectly() throws Exception {
        // Given
        String apiResponse = """
            {
              "members": [
                {"bioguideId": "S000033", "name": "Sanders, Bernard", "state": "VT", "partyName": "Independent"},
                {"bioguideId": "P000197", "name": "Pelosi, Nancy", "state": "CA", "partyName": "Democratic"}
              ],
              "pagination": {"count": 2}
            }
            """;
        JsonNode response = objectMapper.readTree(apiResponse);
        when(congressApiClient.fetchMembers(anyInt(), anyInt(), anyBoolean())).thenReturn(Optional.of(response));
        when(congressApiClient.getRequestCount()).thenReturn(1);

        // When
        CongressSearchResponse<CongressMemberSearchDTO> result = searchService.searchMembers(
                "Sanders", null, null, null, null, 1, 20);

        // Then
        assertThat(result.getResults()).hasSize(1);
        assertThat(result.getResults().get(0).getData().getName()).contains("Sanders");
    }

    @Test
    @DisplayName("Should filter results by state")
    void searchMembers_stateFilter_filtersCorrectly() throws Exception {
        // Given
        String apiResponse = """
            {
              "members": [
                {"bioguideId": "S000033", "name": "Sanders, Bernard", "state": "VT", "partyName": "Independent"},
                {"bioguideId": "P000197", "name": "Pelosi, Nancy", "state": "CA", "partyName": "Democratic"}
              ],
              "pagination": {"count": 2}
            }
            """;
        JsonNode response = objectMapper.readTree(apiResponse);
        when(congressApiClient.fetchMembers(anyInt(), anyInt(), anyBoolean())).thenReturn(Optional.of(response));
        when(congressApiClient.getRequestCount()).thenReturn(1);

        // When
        CongressSearchResponse<CongressMemberSearchDTO> result = searchService.searchMembers(
                null, "VT", null, null, null, 1, 20);

        // Then
        assertThat(result.getResults()).hasSize(1);
        assertThat(result.getResults().get(0).getData().getState()).isEqualTo("VT");
    }

    @Test
    @DisplayName("Should filter results by party")
    void searchMembers_partyFilter_filtersCorrectly() throws Exception {
        // Given
        String apiResponse = """
            {
              "members": [
                {"bioguideId": "S000033", "name": "Sanders, Bernard", "state": "VT", "partyName": "Independent"},
                {"bioguideId": "P000197", "name": "Pelosi, Nancy", "state": "CA", "partyName": "Democratic"}
              ],
              "pagination": {"count": 2}
            }
            """;
        JsonNode response = objectMapper.readTree(apiResponse);
        when(congressApiClient.fetchMembers(anyInt(), anyInt(), anyBoolean())).thenReturn(Optional.of(response));
        when(congressApiClient.getRequestCount()).thenReturn(1);

        // When
        CongressSearchResponse<CongressMemberSearchDTO> result = searchService.searchMembers(
                null, null, "D", null, null, 1, 20);

        // Then
        assertThat(result.getResults()).hasSize(1);
        assertThat(result.getResults().get(0).getData().getParty()).isEqualTo("D");
    }

    @Test
    @DisplayName("Should filter results by chamber")
    void searchMembers_chamberFilter_filtersCorrectly() throws Exception {
        // Given
        String apiResponse = """
            {
              "members": [
                {"bioguideId": "S000033", "name": "Sanders, Bernard", "state": "VT", "partyName": "Independent", "terms": [{"chamber": "Senate"}]},
                {"bioguideId": "P000197", "name": "Pelosi, Nancy", "state": "CA", "partyName": "Democratic", "terms": [{"chamber": "House of Representatives"}]}
              ],
              "pagination": {"count": 2}
            }
            """;
        JsonNode response = objectMapper.readTree(apiResponse);
        when(congressApiClient.fetchMembers(anyInt(), anyInt(), anyBoolean())).thenReturn(Optional.of(response));
        when(congressApiClient.getRequestCount()).thenReturn(1);

        // When
        CongressSearchResponse<CongressMemberSearchDTO> result = searchService.searchMembers(
                null, null, null, "senate", null, 1, 20);

        // Then
        assertThat(result.getResults()).hasSize(1);
        assertThat(result.getResults().get(0).getData().getChamber()).isEqualTo("senate");
    }

    @Test
    @DisplayName("Should detect duplicates and return duplicateId")
    void searchMembers_duplicateExists_returnsDuplicateId() throws Exception {
        // Given
        UUID existingPersonId = UUID.randomUUID();
        Person existingPerson = new Person();
        existingPerson.setId(existingPersonId);
        existingPerson.setBioguideId("S000033");

        String apiResponse = """
            {
              "members": [{"bioguideId": "S000033", "name": "Sanders, Bernard", "state": "VT", "partyName": "Independent"}],
              "pagination": {"count": 1}
            }
            """;
        JsonNode response = objectMapper.readTree(apiResponse);
        when(congressApiClient.fetchMembers(anyInt(), anyInt(), anyBoolean())).thenReturn(Optional.of(response));
        when(congressApiClient.getRequestCount()).thenReturn(1);
        when(personRepository.findByBioguideId("S000033")).thenReturn(Optional.of(existingPerson));

        // When
        CongressSearchResponse<CongressMemberSearchDTO> result = searchService.searchMembers(
                null, null, null, null, null, 1, 20);

        // Then
        assertThat(result.getResults()).hasSize(1);
        assertThat(result.getResults().get(0).getDuplicateId()).isEqualTo(existingPersonId.toString());
    }

    @Test
    @DisplayName("Should calculate rate limit remaining")
    void searchMembers_calculatesRateLimit() throws Exception {
        // Given
        String apiResponse = """
            {"members": [], "pagination": {"count": 0}}
            """;
        JsonNode response = objectMapper.readTree(apiResponse);
        when(congressApiClient.fetchMembers(anyInt(), anyInt(), anyBoolean())).thenReturn(Optional.of(response));
        when(congressApiClient.getRequestCount()).thenReturn(100);

        // When
        CongressSearchResponse<CongressMemberSearchDTO> result = searchService.searchMembers(
                null, null, null, null, null, 1, 20);

        // Then
        assertThat(result.getRateLimitRemaining()).isEqualTo(4900); // 5000 - 100
    }

    @Test
    @DisplayName("Should handle pagination offset correctly")
    void searchMembers_pagination_calculatesOffsetCorrectly() throws Exception {
        // Given
        String apiResponse = """
            {"members": [], "pagination": {"count": 100}}
            """;
        JsonNode response = objectMapper.readTree(apiResponse);
        when(congressApiClient.fetchMembers(eq(10), eq(40), eq(true))).thenReturn(Optional.of(response));
        when(congressApiClient.getRequestCount()).thenReturn(1);

        // When - page 5 with pageSize 10 should have offset 40
        CongressSearchResponse<CongressMemberSearchDTO> result = searchService.searchMembers(
                null, null, null, null, null, 5, 10);

        // Then
        verify(congressApiClient).fetchMembers(eq(10), eq(40), eq(true));
        assertThat(result.getPage()).isEqualTo(5);
        assertThat(result.getPageSize()).isEqualTo(10);
    }

    // =========================================================================
    // Get Member By BioguideId Tests
    // =========================================================================

    @Test
    @DisplayName("Should return member details by bioguideId")
    void getMemberByBioguideId_found_returnsMember() throws Exception {
        // Given
        String apiResponse = """
            {
              "member": {
                "bioguideId": "S000033",
                "name": "Sanders, Bernard",
                "state": "VT",
                "partyName": "Independent",
                "currentMember": true,
                "terms": [{"chamber": "Senate"}]
              }
            }
            """;
        JsonNode response = objectMapper.readTree(apiResponse);
        when(congressApiClient.fetchMemberByBioguideId("S000033")).thenReturn(Optional.of(response));

        // When
        Optional<CongressMemberSearchDTO> result = searchService.getMemberByBioguideId("S000033");

        // Then
        assertThat(result).isPresent();
        CongressMemberSearchDTO member = result.get();
        assertThat(member.getBioguideId()).isEqualTo("S000033");
        assertThat(member.getName()).isEqualTo("Sanders, Bernard");
        assertThat(member.getParty()).isEqualTo("I");
    }

    @Test
    @DisplayName("Should return empty when member not found")
    void getMemberByBioguideId_notFound_returnsEmpty() {
        // Given
        when(congressApiClient.fetchMemberByBioguideId("INVALID")).thenReturn(Optional.empty());

        // When
        Optional<CongressMemberSearchDTO> result = searchService.getMemberByBioguideId("INVALID");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should return empty when member node is missing in response")
    void getMemberByBioguideId_missingMemberNode_returnsEmpty() throws Exception {
        // Given
        String apiResponse = """
            {"request": {}}
            """;
        JsonNode response = objectMapper.readTree(apiResponse);
        when(congressApiClient.fetchMemberByBioguideId("S000033")).thenReturn(Optional.of(response));

        // When
        Optional<CongressMemberSearchDTO> result = searchService.getMemberByBioguideId("S000033");

        // Then
        assertThat(result).isEmpty();
    }

    // =========================================================================
    // Party Mapping Tests
    // =========================================================================

    @Test
    @DisplayName("Should map Democratic to D")
    void searchMembers_democraticParty_mappedToD() throws Exception {
        // Given
        String apiResponse = """
            {"members": [{"bioguideId": "P000197", "name": "Pelosi, Nancy", "state": "CA", "partyName": "Democratic"}], "pagination": {"count": 1}}
            """;
        JsonNode response = objectMapper.readTree(apiResponse);
        when(congressApiClient.fetchMembers(anyInt(), anyInt(), anyBoolean())).thenReturn(Optional.of(response));
        when(congressApiClient.getRequestCount()).thenReturn(1);

        // When
        CongressSearchResponse<CongressMemberSearchDTO> result = searchService.searchMembers(
                null, null, null, null, null, 1, 20);

        // Then
        assertThat(result.getResults().get(0).getData().getParty()).isEqualTo("D");
    }

    @Test
    @DisplayName("Should map Republican to R")
    void searchMembers_republicanParty_mappedToR() throws Exception {
        // Given
        String apiResponse = """
            {"members": [{"bioguideId": "M000355", "name": "McConnell, Mitch", "state": "KY", "partyName": "Republican"}], "pagination": {"count": 1}}
            """;
        JsonNode response = objectMapper.readTree(apiResponse);
        when(congressApiClient.fetchMembers(anyInt(), anyInt(), anyBoolean())).thenReturn(Optional.of(response));
        when(congressApiClient.getRequestCount()).thenReturn(1);

        // When
        CongressSearchResponse<CongressMemberSearchDTO> result = searchService.searchMembers(
                null, null, null, null, null, 1, 20);

        // Then
        assertThat(result.getResults().get(0).getData().getParty()).isEqualTo("R");
    }

    @Test
    @DisplayName("Should map Independent to I")
    void searchMembers_independentParty_mappedToI() throws Exception {
        // Given
        String apiResponse = """
            {"members": [{"bioguideId": "S000033", "name": "Sanders, Bernard", "state": "VT", "partyName": "Independent"}], "pagination": {"count": 1}}
            """;
        JsonNode response = objectMapper.readTree(apiResponse);
        when(congressApiClient.fetchMembers(anyInt(), anyInt(), anyBoolean())).thenReturn(Optional.of(response));
        when(congressApiClient.getRequestCount()).thenReturn(1);

        // When
        CongressSearchResponse<CongressMemberSearchDTO> result = searchService.searchMembers(
                null, null, null, null, null, 1, 20);

        // Then
        assertThat(result.getResults().get(0).getData().getParty()).isEqualTo("I");
    }

    // =========================================================================
    // Name Parsing Tests
    // =========================================================================

    @Test
    @DisplayName("Should parse comma-separated name correctly")
    void searchMembers_commaSeparatedName_parsedCorrectly() throws Exception {
        // Given
        String apiResponse = """
            {"members": [{"bioguideId": "S000033", "name": "Sanders, Bernard", "state": "VT"}], "pagination": {"count": 1}}
            """;
        JsonNode response = objectMapper.readTree(apiResponse);
        when(congressApiClient.fetchMembers(anyInt(), anyInt(), anyBoolean())).thenReturn(Optional.of(response));
        when(congressApiClient.getRequestCount()).thenReturn(1);

        // When
        CongressSearchResponse<CongressMemberSearchDTO> result = searchService.searchMembers(
                null, null, null, null, null, 1, 20);

        // Then
        CongressMemberSearchDTO member = result.getResults().get(0).getData();
        assertThat(member.getLastName()).isEqualTo("Sanders");
        assertThat(member.getFirstName()).isEqualTo("Bernard");
    }

    @Test
    @DisplayName("Should build source URL correctly")
    void searchMembers_buildSourceUrl_formatsCorrectly() throws Exception {
        // Given
        String apiResponse = """
            {"members": [{"bioguideId": "S000033", "name": "Sanders, Bernard", "state": "VT"}], "pagination": {"count": 1}}
            """;
        JsonNode response = objectMapper.readTree(apiResponse);
        when(congressApiClient.fetchMembers(anyInt(), anyInt(), anyBoolean())).thenReturn(Optional.of(response));
        when(congressApiClient.getRequestCount()).thenReturn(1);

        // When
        CongressSearchResponse<CongressMemberSearchDTO> result = searchService.searchMembers(
                null, null, null, null, null, 1, 20);

        // Then
        String sourceUrl = result.getResults().get(0).getSourceUrl();
        assertThat(sourceUrl).contains("congress.gov/member");
        assertThat(sourceUrl).contains("S000033");
    }
}
