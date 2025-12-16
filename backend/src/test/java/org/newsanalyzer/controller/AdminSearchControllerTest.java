package org.newsanalyzer.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.newsanalyzer.dto.*;
import org.newsanalyzer.service.CongressSearchService;
import org.newsanalyzer.service.FederalRegisterSearchService;
import org.newsanalyzer.service.LegislatorsSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.*;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for AdminSearchController.
 *
 * Tests Congress.gov search proxy endpoints with mocked service layer.
 *
 * @author Quinn (QA Agent)
 * @since 2.0.0
 */
@WebMvcTest(AdminSearchController.class)
@WithMockUser
class AdminSearchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CongressSearchService congressSearchService;

    @MockBean
    private FederalRegisterSearchService federalRegisterSearchService;

    @MockBean
    private LegislatorsSearchService legislatorsSearchService;

    private CongressMemberSearchDTO testMember;
    private CongressSearchResponse<CongressMemberSearchDTO> testResponse;

    @BeforeEach
    void setUp() {
        testMember = CongressMemberSearchDTO.builder()
                .bioguideId("S000033")
                .name("Sanders, Bernard")
                .firstName("Bernard")
                .lastName("Sanders")
                .state("VT")
                .party("I")
                .chamber("senate")
                .currentMember(true)
                .imageUrl("https://www.congress.gov/img/member/s000033.jpg")
                .url("https://www.congress.gov/member/bernard-sanders/S000033")
                .build();

        CongressSearchResult<CongressMemberSearchDTO> result = CongressSearchResult.<CongressMemberSearchDTO>builder()
                .data(testMember)
                .source("Congress.gov")
                .sourceUrl("https://www.congress.gov/member/bernard-sanders/S000033")
                .duplicateId(null)
                .build();

        testResponse = CongressSearchResponse.<CongressMemberSearchDTO>builder()
                .results(List.of(result))
                .total(1)
                .page(1)
                .pageSize(20)
                .rateLimitRemaining(4999)
                .rateLimitResetSeconds(3600)
                .build();
    }

    // =========================================================================
    // Search Endpoint Tests
    // =========================================================================

    @Test
    @DisplayName("GET /api/admin/search/congress/members - Should return search results")
    void searchCongressMembers_returnsResults() throws Exception {
        // Given
        when(congressSearchService.searchMembers(
                any(), any(), any(), any(), any(), eq(1), eq(20)))
                .thenReturn(testResponse);

        // When/Then
        mockMvc.perform(get("/api/admin/search/congress/members"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results", hasSize(1)))
                .andExpect(jsonPath("$.results[0].data.bioguideId", is("S000033")))
                .andExpect(jsonPath("$.results[0].data.name", is("Sanders, Bernard")))
                .andExpect(jsonPath("$.results[0].source", is("Congress.gov")))
                .andExpect(jsonPath("$.total", is(1)))
                .andExpect(jsonPath("$.page", is(1)))
                .andExpect(jsonPath("$.pageSize", is(20)));
    }

    @Test
    @DisplayName("GET /api/admin/search/congress/members - Should filter by name")
    void searchCongressMembers_withNameFilter_returnsFilteredResults() throws Exception {
        // Given
        when(congressSearchService.searchMembers(
                eq("Sanders"), any(), any(), any(), any(), eq(1), eq(20)))
                .thenReturn(testResponse);

        // When/Then
        mockMvc.perform(get("/api/admin/search/congress/members")
                        .param("name", "Sanders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results", hasSize(1)))
                .andExpect(jsonPath("$.results[0].data.lastName", is("Sanders")));
    }

    @Test
    @DisplayName("GET /api/admin/search/congress/members - Should filter by state")
    void searchCongressMembers_withStateFilter_returnsFilteredResults() throws Exception {
        // Given
        when(congressSearchService.searchMembers(
                any(), eq("VT"), any(), any(), any(), eq(1), eq(20)))
                .thenReturn(testResponse);

        // When/Then
        mockMvc.perform(get("/api/admin/search/congress/members")
                        .param("state", "VT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results", hasSize(1)))
                .andExpect(jsonPath("$.results[0].data.state", is("VT")));
    }

    @Test
    @DisplayName("GET /api/admin/search/congress/members - Should filter by party")
    void searchCongressMembers_withPartyFilter_returnsFilteredResults() throws Exception {
        // Given
        when(congressSearchService.searchMembers(
                any(), any(), eq("I"), any(), any(), eq(1), eq(20)))
                .thenReturn(testResponse);

        // When/Then
        mockMvc.perform(get("/api/admin/search/congress/members")
                        .param("party", "I"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results", hasSize(1)))
                .andExpect(jsonPath("$.results[0].data.party", is("I")));
    }

    @Test
    @DisplayName("GET /api/admin/search/congress/members - Should filter by chamber")
    void searchCongressMembers_withChamberFilter_returnsFilteredResults() throws Exception {
        // Given
        when(congressSearchService.searchMembers(
                any(), any(), any(), eq("senate"), any(), eq(1), eq(20)))
                .thenReturn(testResponse);

        // When/Then
        mockMvc.perform(get("/api/admin/search/congress/members")
                        .param("chamber", "senate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results", hasSize(1)))
                .andExpect(jsonPath("$.results[0].data.chamber", is("senate")));
    }

    @Test
    @DisplayName("GET /api/admin/search/congress/members - Should filter by congress")
    void searchCongressMembers_withCongressFilter_returnsFilteredResults() throws Exception {
        // Given
        when(congressSearchService.searchMembers(
                any(), any(), any(), any(), eq(118), eq(1), eq(20)))
                .thenReturn(testResponse);

        // When/Then
        mockMvc.perform(get("/api/admin/search/congress/members")
                        .param("congress", "118"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results", hasSize(1)));
    }

    @Test
    @DisplayName("GET /api/admin/search/congress/members - Should support pagination")
    void searchCongressMembers_withPagination_returnsCorrectPage() throws Exception {
        // Given
        CongressSearchResponse<CongressMemberSearchDTO> pageResponse = CongressSearchResponse.<CongressMemberSearchDTO>builder()
                .results(List.of())
                .total(100)
                .page(5)
                .pageSize(10)
                .build();
        when(congressSearchService.searchMembers(
                any(), any(), any(), any(), any(), eq(5), eq(10)))
                .thenReturn(pageResponse);

        // When/Then
        mockMvc.perform(get("/api/admin/search/congress/members")
                        .param("page", "5")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page", is(5)))
                .andExpect(jsonPath("$.pageSize", is(10)))
                .andExpect(jsonPath("$.total", is(100)));
    }

    @Test
    @DisplayName("GET /api/admin/search/congress/members - Should return rate limit headers")
    void searchCongressMembers_returnsRateLimitHeaders() throws Exception {
        // Given
        when(congressSearchService.searchMembers(
                any(), any(), any(), any(), any(), eq(1), eq(20)))
                .thenReturn(testResponse);

        // When/Then
        mockMvc.perform(get("/api/admin/search/congress/members"))
                .andExpect(status().isOk())
                .andExpect(header().string("X-RateLimit-Remaining", "4999"))
                .andExpect(header().string("X-RateLimit-Reset", "3600"));
    }

    @Test
    @DisplayName("GET /api/admin/search/congress/members - Should return empty results when no matches")
    void searchCongressMembers_noMatches_returnsEmptyResults() throws Exception {
        // Given
        CongressSearchResponse<CongressMemberSearchDTO> emptyResponse = CongressSearchResponse.<CongressMemberSearchDTO>builder()
                .results(new ArrayList<>())
                .total(0)
                .page(1)
                .pageSize(20)
                .build();
        when(congressSearchService.searchMembers(
                any(), any(), any(), any(), any(), eq(1), eq(20)))
                .thenReturn(emptyResponse);

        // When/Then
        mockMvc.perform(get("/api/admin/search/congress/members")
                        .param("name", "NonExistent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results", hasSize(0)))
                .andExpect(jsonPath("$.total", is(0)));
    }

    @Test
    @DisplayName("GET /api/admin/search/congress/members - Should include duplicateId when detected")
    void searchCongressMembers_withDuplicate_returnsDuplicateId() throws Exception {
        // Given
        String existingId = "550e8400-e29b-41d4-a716-446655440000";
        CongressSearchResult<CongressMemberSearchDTO> resultWithDuplicate = CongressSearchResult.<CongressMemberSearchDTO>builder()
                .data(testMember)
                .source("Congress.gov")
                .sourceUrl("https://www.congress.gov/member/bernard-sanders/S000033")
                .duplicateId(existingId)
                .build();

        CongressSearchResponse<CongressMemberSearchDTO> responseWithDuplicate = CongressSearchResponse.<CongressMemberSearchDTO>builder()
                .results(List.of(resultWithDuplicate))
                .total(1)
                .page(1)
                .pageSize(20)
                .build();

        when(congressSearchService.searchMembers(
                any(), any(), any(), any(), any(), eq(1), eq(20)))
                .thenReturn(responseWithDuplicate);

        // When/Then
        mockMvc.perform(get("/api/admin/search/congress/members"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results[0].duplicateId", is(existingId)));
    }

    // =========================================================================
    // Get Member Detail Endpoint Tests
    // =========================================================================

    @Test
    @DisplayName("GET /api/admin/search/congress/members/{bioguideId} - Should return member details")
    void getCongressMember_found_returnsMember() throws Exception {
        // Given
        when(congressSearchService.getMemberByBioguideId("S000033"))
                .thenReturn(Optional.of(testMember));

        // When/Then
        mockMvc.perform(get("/api/admin/search/congress/members/S000033"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bioguideId", is("S000033")))
                .andExpect(jsonPath("$.name", is("Sanders, Bernard")))
                .andExpect(jsonPath("$.firstName", is("Bernard")))
                .andExpect(jsonPath("$.lastName", is("Sanders")))
                .andExpect(jsonPath("$.state", is("VT")))
                .andExpect(jsonPath("$.party", is("I")))
                .andExpect(jsonPath("$.chamber", is("senate")))
                .andExpect(jsonPath("$.currentMember", is(true)));
    }

    @Test
    @DisplayName("GET /api/admin/search/congress/members/{bioguideId} - Should return 404 when not found")
    void getCongressMember_notFound_returns404() throws Exception {
        // Given
        when(congressSearchService.getMemberByBioguideId("INVALID"))
                .thenReturn(Optional.empty());

        // When/Then
        mockMvc.perform(get("/api/admin/search/congress/members/INVALID"))
                .andExpect(status().isNotFound());
    }

    // =========================================================================
    // Multiple Filters Tests
    // =========================================================================

    @Test
    @DisplayName("GET /api/admin/search/congress/members - Should combine multiple filters")
    void searchCongressMembers_withMultipleFilters_returnsFilteredResults() throws Exception {
        // Given
        when(congressSearchService.searchMembers(
                eq("Sanders"), eq("VT"), eq("I"), eq("senate"), eq(118), eq(1), eq(20)))
                .thenReturn(testResponse);

        // When/Then
        mockMvc.perform(get("/api/admin/search/congress/members")
                        .param("name", "Sanders")
                        .param("state", "VT")
                        .param("party", "I")
                        .param("chamber", "senate")
                        .param("congress", "118"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results", hasSize(1)));
    }

    // =========================================================================
    // Legislators Repo Search Tests (ADMIN-1.9)
    // =========================================================================

    @Nested
    @DisplayName("Legislators Repo Search Endpoint Tests")
    class LegislatorsSearchTests {

        private LegislatorSearchDTO testLegislator;
        private LegislatorsSearchResponse<LegislatorSearchDTO> testLegislatorsResponse;

        @BeforeEach
        void setUpLegislators() {
            testLegislator = LegislatorSearchDTO.builder()
                    .bioguideId("S000033")
                    .name("Bernard Sanders")
                    .state("VT")
                    .party("Independent")
                    .chamber("Senate")
                    .currentMember(true)
                    .socialMedia(Map.of("twitter", "SenSanders", "facebook", "senatorsanders"))
                    .externalIds(Map.of("govtrack", 400357, "opensecrets", "N00000528"))
                    .socialMediaCount(2)
                    .externalIdCount(2)
                    .build();

            LegislatorsSearchResult<LegislatorSearchDTO> result = LegislatorsSearchResult.<LegislatorSearchDTO>builder()
                    .data(testLegislator)
                    .source("Legislators Repo")
                    .sourceUrl("https://github.com/unitedstates/congress-legislators")
                    .localMatchId(null)
                    .build();

            testLegislatorsResponse = LegislatorsSearchResponse.<LegislatorSearchDTO>builder()
                    .results(List.of(result))
                    .total(1)
                    .page(1)
                    .pageSize(20)
                    .cached(false)
                    .build();
        }

        @Test
        @DisplayName("GET /api/admin/search/legislators - Should return search results")
        void searchLegislators_returnsResults() throws Exception {
            // Given
            when(legislatorsSearchService.searchLegislators(any(), any(), any(), eq(1), eq(20)))
                    .thenReturn(testLegislatorsResponse);

            // When/Then
            mockMvc.perform(get("/api/admin/search/legislators"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.results", hasSize(1)))
                    .andExpect(jsonPath("$.results[0].data.bioguideId", is("S000033")))
                    .andExpect(jsonPath("$.results[0].data.name", is("Bernard Sanders")))
                    .andExpect(jsonPath("$.results[0].source", is("Legislators Repo")))
                    .andExpect(jsonPath("$.total", is(1)))
                    .andExpect(jsonPath("$.page", is(1)))
                    .andExpect(jsonPath("$.pageSize", is(20)));
        }

        @Test
        @DisplayName("GET /api/admin/search/legislators - Should filter by name")
        void searchLegislators_withNameFilter_returnsFilteredResults() throws Exception {
            // Given
            when(legislatorsSearchService.searchLegislators(eq("Sanders"), any(), any(), eq(1), eq(20)))
                    .thenReturn(testLegislatorsResponse);

            // When/Then
            mockMvc.perform(get("/api/admin/search/legislators")
                            .param("name", "Sanders"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.results", hasSize(1)))
                    .andExpect(jsonPath("$.results[0].data.name", containsString("Sanders")));
        }

        @Test
        @DisplayName("GET /api/admin/search/legislators - Should filter by bioguideId")
        void searchLegislators_withBioguideIdFilter_returnsExactMatch() throws Exception {
            // Given
            when(legislatorsSearchService.searchLegislators(any(), eq("S000033"), any(), eq(1), eq(20)))
                    .thenReturn(testLegislatorsResponse);

            // When/Then
            mockMvc.perform(get("/api/admin/search/legislators")
                            .param("bioguideId", "S000033"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.results", hasSize(1)))
                    .andExpect(jsonPath("$.results[0].data.bioguideId", is("S000033")));
        }

        @Test
        @DisplayName("GET /api/admin/search/legislators - Should filter by state")
        void searchLegislators_withStateFilter_returnsFilteredResults() throws Exception {
            // Given
            when(legislatorsSearchService.searchLegislators(any(), any(), eq("VT"), eq(1), eq(20)))
                    .thenReturn(testLegislatorsResponse);

            // When/Then
            mockMvc.perform(get("/api/admin/search/legislators")
                            .param("state", "VT"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.results", hasSize(1)))
                    .andExpect(jsonPath("$.results[0].data.state", is("VT")));
        }

        @Test
        @DisplayName("GET /api/admin/search/legislators - Should support pagination")
        void searchLegislators_withPagination_returnsCorrectPage() throws Exception {
            // Given
            LegislatorsSearchResponse<LegislatorSearchDTO> pageResponse = LegislatorsSearchResponse.<LegislatorSearchDTO>builder()
                    .results(List.of())
                    .total(100)
                    .page(5)
                    .pageSize(10)
                    .cached(true)
                    .build();
            when(legislatorsSearchService.searchLegislators(any(), any(), any(), eq(5), eq(10)))
                    .thenReturn(pageResponse);

            // When/Then
            mockMvc.perform(get("/api/admin/search/legislators")
                            .param("page", "5")
                            .param("pageSize", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.page", is(5)))
                    .andExpect(jsonPath("$.pageSize", is(10)))
                    .andExpect(jsonPath("$.total", is(100)));
        }

        @Test
        @DisplayName("GET /api/admin/search/legislators - Should return empty results when no matches")
        void searchLegislators_noMatches_returnsEmptyResults() throws Exception {
            // Given
            LegislatorsSearchResponse<LegislatorSearchDTO> emptyResponse = LegislatorsSearchResponse.<LegislatorSearchDTO>builder()
                    .results(new ArrayList<>())
                    .total(0)
                    .page(1)
                    .pageSize(20)
                    .cached(false)
                    .build();
            when(legislatorsSearchService.searchLegislators(any(), any(), any(), eq(1), eq(20)))
                    .thenReturn(emptyResponse);

            // When/Then
            mockMvc.perform(get("/api/admin/search/legislators")
                            .param("name", "NonExistent"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.results", hasSize(0)))
                    .andExpect(jsonPath("$.total", is(0)));
        }

        @Test
        @DisplayName("GET /api/admin/search/legislators - Should include localMatchId when detected")
        void searchLegislators_withLocalMatch_returnsLocalMatchId() throws Exception {
            // Given
            String existingId = "550e8400-e29b-41d4-a716-446655440000";
            LegislatorsSearchResult<LegislatorSearchDTO> resultWithMatch = LegislatorsSearchResult.<LegislatorSearchDTO>builder()
                    .data(testLegislator)
                    .source("Legislators Repo")
                    .sourceUrl("https://github.com/unitedstates/congress-legislators")
                    .localMatchId(existingId)
                    .build();

            LegislatorsSearchResponse<LegislatorSearchDTO> responseWithMatch = LegislatorsSearchResponse.<LegislatorSearchDTO>builder()
                    .results(List.of(resultWithMatch))
                    .total(1)
                    .page(1)
                    .pageSize(20)
                    .cached(false)
                    .build();

            when(legislatorsSearchService.searchLegislators(any(), any(), any(), eq(1), eq(20)))
                    .thenReturn(responseWithMatch);

            // When/Then
            mockMvc.perform(get("/api/admin/search/legislators"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.results[0].localMatchId", is(existingId)));
        }

        @Test
        @DisplayName("GET /api/admin/search/legislators - Should indicate cached status")
        void searchLegislators_cached_returnsCachedTrue() throws Exception {
            // Given
            LegislatorsSearchResponse<LegislatorSearchDTO> cachedResponse = LegislatorsSearchResponse.<LegislatorSearchDTO>builder()
                    .results(List.of())
                    .total(0)
                    .page(1)
                    .pageSize(20)
                    .cached(true)
                    .build();
            when(legislatorsSearchService.searchLegislators(any(), any(), any(), eq(1), eq(20)))
                    .thenReturn(cachedResponse);

            // When/Then
            mockMvc.perform(get("/api/admin/search/legislators"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.cached", is(true)));
        }

        @Test
        @DisplayName("GET /api/admin/search/legislators - Should include social media and external ID counts")
        void searchLegislators_returnsSocialMediaAndExternalIdCounts() throws Exception {
            // Given
            when(legislatorsSearchService.searchLegislators(any(), any(), any(), eq(1), eq(20)))
                    .thenReturn(testLegislatorsResponse);

            // When/Then
            mockMvc.perform(get("/api/admin/search/legislators"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.results[0].data.socialMediaCount", is(2)))
                    .andExpect(jsonPath("$.results[0].data.externalIdCount", is(2)))
                    .andExpect(jsonPath("$.results[0].data.socialMedia.twitter", is("SenSanders")));
        }

        @Test
        @DisplayName("GET /api/admin/search/legislators/{bioguideId} - Should return legislator details")
        void getLegislator_found_returnsLegislator() throws Exception {
            // Given
            LegislatorDetailDTO detailDTO = LegislatorDetailDTO.builder()
                    .bioguideId("S000033")
                    .name(LegislatorDetailDTO.NameInfo.builder()
                            .first("Bernard")
                            .last("Sanders")
                            .build())
                    .currentMember(true)
                    .socialMedia(LegislatorDetailDTO.SocialMediaInfo.builder()
                            .twitter("SenSanders")
                            .facebook("senatorsanders")
                            .build())
                    .externalIds(LegislatorDetailDTO.ExternalIdsInfo.builder()
                            .govtrack(400357)
                            .opensecrets("N00000528")
                            .build())
                    .build();

            when(legislatorsSearchService.getLegislatorByBioguideId("S000033"))
                    .thenReturn(Optional.of(detailDTO));

            // When/Then
            mockMvc.perform(get("/api/admin/search/legislators/S000033"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.bioguideId", is("S000033")))
                    .andExpect(jsonPath("$.name.first", is("Bernard")))
                    .andExpect(jsonPath("$.name.last", is("Sanders")))
                    .andExpect(jsonPath("$.currentMember", is(true)))
                    .andExpect(jsonPath("$.socialMedia.twitter", is("SenSanders")))
                    .andExpect(jsonPath("$.externalIds.govtrack", is(400357)));
        }

        @Test
        @DisplayName("GET /api/admin/search/legislators/{bioguideId} - Should return 404 when not found")
        void getLegislator_notFound_returns404() throws Exception {
            // Given
            when(legislatorsSearchService.getLegislatorByBioguideId("INVALID"))
                    .thenReturn(Optional.empty());

            // When/Then
            mockMvc.perform(get("/api/admin/search/legislators/INVALID"))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("GET /api/admin/search/legislators - Should combine multiple filters")
        void searchLegislators_withMultipleFilters_returnsFilteredResults() throws Exception {
            // Given
            when(legislatorsSearchService.searchLegislators(eq("Sanders"), eq("S000033"), eq("VT"), eq(1), eq(20)))
                    .thenReturn(testLegislatorsResponse);

            // When/Then
            mockMvc.perform(get("/api/admin/search/legislators")
                            .param("name", "Sanders")
                            .param("bioguideId", "S000033")
                            .param("state", "VT"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.results", hasSize(1)));
        }
    }
}
