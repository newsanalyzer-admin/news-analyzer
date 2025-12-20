package org.newsanalyzer.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.newsanalyzer.dto.JudgeDTO;
import org.newsanalyzer.model.*;
import org.newsanalyzer.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for JudgeService.
 *
 * Tests cover:
 * - Find current judges with pagination
 * - Find judges with filters
 * - Find judge by ID
 * - Search judges by name
 * - Get statistics
 *
 * @author James (Dev Agent)
 * @since 2.0.0
 * @story UI-1.11
 */
@ExtendWith(MockitoExtension.class)
class JudgeServiceTest {

    @Mock
    private PersonRepository personRepository;

    @Mock
    private GovernmentPositionRepository positionRepository;

    @Mock
    private PositionHoldingRepository holdingRepository;

    @Mock
    private GovernmentOrganizationRepository orgRepository;

    @InjectMocks
    private JudgeService judgeService;

    private Person samplePerson;
    private GovernmentPosition samplePosition;
    private PositionHolding sampleHolding;
    private GovernmentOrganization sampleCourt;

    @BeforeEach
    void setUp() {
        // Create sample person (judge)
        samplePerson = new Person();
        samplePerson.setId(UUID.randomUUID());
        samplePerson.setFirstName("John");
        samplePerson.setLastName("Roberts");
        // fullName is computed from firstName + lastName
        samplePerson.setDataSource(DataSource.FJC);

        // Create sample court
        sampleCourt = new GovernmentOrganization();
        sampleCourt.setId(UUID.randomUUID());
        sampleCourt.setOfficialName("Supreme Court of the United States");
        sampleCourt.setBranch(GovernmentOrganization.GovernmentBranch.JUDICIAL);

        // Create sample position
        samplePosition = new GovernmentPosition();
        samplePosition.setId(UUID.randomUUID());
        samplePosition.setTitle("Chief Justice");
        samplePosition.setOrganizationId(sampleCourt.getId());

        // Create sample holding
        sampleHolding = new PositionHolding();
        sampleHolding.setId(UUID.randomUUID());
        sampleHolding.setPersonId(samplePerson.getId());
        sampleHolding.setPositionId(samplePosition.getId());
        sampleHolding.setStartDate(LocalDate.of(2005, 9, 29));
        sampleHolding.setEndDate(null); // Current
        sampleHolding.setDataSource(DataSource.FJC);
    }

    @Nested
    @DisplayName("findCurrentJudges")
    class FindCurrentJudgesTests {

        @Test
        @DisplayName("Should return page of current judges")
        void findCurrentJudges_returnsPageOfJudges() {
            // Given
            Pageable pageable = PageRequest.of(0, 20);
            Page<PositionHolding> holdingPage = new PageImpl<>(List.of(sampleHolding), pageable, 1);
            when(holdingRepository.findByDataSource(eq(DataSource.FJC), any(Pageable.class)))
                    .thenReturn(holdingPage);
            when(personRepository.findById(samplePerson.getId())).thenReturn(Optional.of(samplePerson));
            when(positionRepository.findById(samplePosition.getId())).thenReturn(Optional.of(samplePosition));
            when(orgRepository.findById(sampleCourt.getId())).thenReturn(Optional.of(sampleCourt));

            // When
            Page<JudgeDTO> result = judgeService.findCurrentJudges(pageable);

            // Then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getLastName()).isEqualTo("Roberts");
            assertThat(result.getContent().get(0).isCurrent()).isTrue();
        }

        @Test
        @DisplayName("Should return empty page when no judges found")
        void findCurrentJudges_noJudges_returnsEmptyPage() {
            // Given
            Pageable pageable = PageRequest.of(0, 20);
            Page<PositionHolding> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);
            when(holdingRepository.findByDataSource(eq(DataSource.FJC), any(Pageable.class)))
                    .thenReturn(emptyPage);

            // When
            Page<JudgeDTO> result = judgeService.findCurrentJudges(pageable);

            // Then
            assertThat(result.getContent()).isEmpty();
        }
    }

    @Nested
    @DisplayName("findJudges with filters")
    class FindJudgesWithFiltersTests {

        @Test
        @DisplayName("Should filter by court level")
        void findJudges_filterByCourtLevel_filtersCorrectly() {
            // Given
            Pageable pageable = PageRequest.of(0, 20);
            // findJudges now uses unpaged query and applies sorting/pagination in-memory
            when(holdingRepository.findByDataSource(DataSource.FJC))
                    .thenReturn(List.of(sampleHolding));
            when(personRepository.findById(samplePerson.getId())).thenReturn(Optional.of(samplePerson));
            when(positionRepository.findById(samplePosition.getId())).thenReturn(Optional.of(samplePosition));
            when(orgRepository.findById(sampleCourt.getId())).thenReturn(Optional.of(sampleCourt));

            // When - filter by "supreme"
            Page<JudgeDTO> result = judgeService.findJudges("supreme", null, null, null, pageable);

            // Then
            assertThat(result.getContent()).hasSize(1);
        }

        @Test
        @DisplayName("Should filter out non-matching court levels")
        void findJudges_filterByCourtLevel_excludesNonMatching() {
            // Given
            Pageable pageable = PageRequest.of(0, 20);
            when(holdingRepository.findByDataSource(DataSource.FJC))
                    .thenReturn(List.of(sampleHolding));
            when(personRepository.findById(samplePerson.getId())).thenReturn(Optional.of(samplePerson));
            when(positionRepository.findById(samplePosition.getId())).thenReturn(Optional.of(samplePosition));
            when(orgRepository.findById(sampleCourt.getId())).thenReturn(Optional.of(sampleCourt));

            // When - filter by "district" (Supreme Court should not match)
            Page<JudgeDTO> result = judgeService.findJudges("district", null, null, null, pageable);

            // Then
            assertThat(result.getContent()).isEmpty();
        }

        @Test
        @DisplayName("Should filter by search term")
        void findJudges_filterBySearch_findsMatches() {
            // Given
            Pageable pageable = PageRequest.of(0, 20);
            when(holdingRepository.findByDataSource(DataSource.FJC))
                    .thenReturn(List.of(sampleHolding));
            when(personRepository.findById(samplePerson.getId())).thenReturn(Optional.of(samplePerson));
            when(positionRepository.findById(samplePosition.getId())).thenReturn(Optional.of(samplePosition));
            when(orgRepository.findById(sampleCourt.getId())).thenReturn(Optional.of(sampleCourt));

            // When - search for "Roberts"
            Page<JudgeDTO> result = judgeService.findJudges(null, null, null, "Roberts", pageable);

            // Then
            assertThat(result.getContent()).hasSize(1);
        }
    }

    @Nested
    @DisplayName("findById")
    class FindByIdTests {

        @Test
        @DisplayName("Should return judge when found")
        void findById_existingId_returnsJudge() {
            // Given
            UUID judgeId = samplePerson.getId();
            when(personRepository.findById(judgeId)).thenReturn(Optional.of(samplePerson));
            when(holdingRepository.findByPersonIdOrderByStartDateDesc(judgeId))
                    .thenReturn(List.of(sampleHolding));
            when(positionRepository.findById(samplePosition.getId())).thenReturn(Optional.of(samplePosition));
            when(orgRepository.findById(sampleCourt.getId())).thenReturn(Optional.of(sampleCourt));

            // When
            Optional<JudgeDTO> result = judgeService.findById(judgeId);

            // Then
            assertThat(result).isPresent();
            assertThat(result.get().getLastName()).isEqualTo("Roberts");
        }

        @Test
        @DisplayName("Should return empty when not found")
        void findById_nonExistingId_returnsEmpty() {
            // Given
            UUID nonExistingId = UUID.randomUUID();
            when(personRepository.findById(nonExistingId)).thenReturn(Optional.empty());

            // When
            Optional<JudgeDTO> result = judgeService.findById(nonExistingId);

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should return empty when person is not a judge (no FJC data source)")
        void findById_personNotJudge_returnsEmpty() {
            // Given
            Person nonJudge = new Person();
            nonJudge.setId(UUID.randomUUID());
            nonJudge.setDataSource(DataSource.MANUAL); // Not FJC

            when(personRepository.findById(nonJudge.getId())).thenReturn(Optional.of(nonJudge));

            // When
            Optional<JudgeDTO> result = judgeService.findById(nonJudge.getId());

            // Then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("searchByName")
    class SearchByNameTests {

        @Test
        @DisplayName("Should find judges matching name")
        void searchByName_matchingName_returnsResults() {
            // Given
            when(personRepository.searchByName("Roberts")).thenReturn(List.of(samplePerson));
            when(holdingRepository.findByPersonIdOrderByStartDateDesc(samplePerson.getId()))
                    .thenReturn(List.of(sampleHolding));
            when(positionRepository.findById(samplePosition.getId())).thenReturn(Optional.of(samplePosition));
            when(orgRepository.findById(sampleCourt.getId())).thenReturn(Optional.of(sampleCourt));

            // When
            List<JudgeDTO> results = judgeService.searchByName("Roberts");

            // Then
            assertThat(results).hasSize(1);
            assertThat(results.get(0).getLastName()).isEqualTo("Roberts");
        }

        @Test
        @DisplayName("Should return empty list for no matches")
        void searchByName_noMatches_returnsEmptyList() {
            // Given
            when(personRepository.searchByName("NonExistent")).thenReturn(Collections.emptyList());

            // When
            List<JudgeDTO> results = judgeService.searchByName("NonExistent");

            // Then
            assertThat(results).isEmpty();
        }

        @Test
        @DisplayName("Should perform case-insensitive search")
        void searchByName_caseInsensitive_findsMatches() {
            // Given
            when(personRepository.searchByName("ROBERTS")).thenReturn(List.of(samplePerson));
            when(holdingRepository.findByPersonIdOrderByStartDateDesc(samplePerson.getId()))
                    .thenReturn(List.of(sampleHolding));
            when(positionRepository.findById(samplePosition.getId())).thenReturn(Optional.of(samplePosition));
            when(orgRepository.findById(sampleCourt.getId())).thenReturn(Optional.of(sampleCourt));

            // When
            List<JudgeDTO> results = judgeService.searchByName("ROBERTS");

            // Then
            assertThat(results).hasSize(1);
        }
    }

    @Nested
    @DisplayName("getStatistics")
    class GetStatisticsTests {

        @Test
        @DisplayName("Should return statistics")
        void getStatistics_returnsStats() {
            // Given
            when(personRepository.countByDataSource(DataSource.FJC)).thenReturn(1200L);
            when(holdingRepository.countByDataSource(DataSource.FJC)).thenReturn(1500L);
            // getStatistics() counts current judges manually by filtering holdings with endDate == null
            PositionHolding currentHolding = new PositionHolding();
            currentHolding.setEndDate(null);
            PositionHolding formerHolding = new PositionHolding();
            formerHolding.setEndDate(LocalDate.of(2020, 1, 1));
            when(holdingRepository.findByDataSource(DataSource.FJC))
                    .thenReturn(List.of(currentHolding, formerHolding));

            // When
            Map<String, Object> stats = judgeService.getStatistics();

            // Then
            assertThat(stats).containsEntry("totalJudges", 1200L);
            assertThat(stats).containsEntry("totalAppointments", 1500L);
            assertThat(stats).containsEntry("currentJudges", 1L); // Only one holding with null endDate
        }
    }
}
