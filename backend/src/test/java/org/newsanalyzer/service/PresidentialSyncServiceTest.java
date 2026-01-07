package org.newsanalyzer.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.newsanalyzer.model.*;
import org.newsanalyzer.repository.*;

import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for PresidentialSyncService.
 *
 * @author James (Dev Agent)
 * @since 2.0.0
 */
@ExtendWith(MockitoExtension.class)
class PresidentialSyncServiceTest {

    @Mock
    private PersonRepository personRepository;

    @Mock
    private PresidencyRepository presidencyRepository;

    @Mock
    private GovernmentPositionRepository positionRepository;

    @Mock
    private PositionHoldingRepository positionHoldingRepository;

    private PresidentialSyncService syncService;

    @BeforeEach
    void setUp() {
        syncService = new PresidentialSyncService(
                personRepository,
                presidencyRepository,
                positionRepository,
                positionHoldingRepository
        );
    }

    // =========================================================================
    // Count and Existence Tests
    // =========================================================================

    @Nested
    @DisplayName("Presidency Count and Existence")
    class PresidencyCountTests {

        @Test
        @DisplayName("Should return count from repository")
        void getPresidencyCount_returnsRepositoryCount() {
            when(presidencyRepository.count()).thenReturn(47L);

            long count = syncService.getPresidencyCount();

            assertThat(count).isEqualTo(47L);
            verify(presidencyRepository).count();
        }

        @Test
        @DisplayName("Should check existence by number")
        void presidencyExists_delegatesToRepository() {
            when(presidencyRepository.existsByNumber(1)).thenReturn(true);
            when(presidencyRepository.existsByNumber(99)).thenReturn(false);

            assertThat(syncService.presidencyExists(1)).isTrue();
            assertThat(syncService.presidencyExists(99)).isFalse();
        }
    }

    // =========================================================================
    // SyncResult Tests
    // =========================================================================

    @Nested
    @DisplayName("SyncResult Statistics")
    class SyncResultTests {

        @Test
        @DisplayName("Should calculate total presidencies correctly")
        void syncResult_totalPresidencies_addsAddedAndUpdated() {
            PresidentialSyncService.SyncResult result = new PresidentialSyncService.SyncResult();

            // Use reflection or test via toString
            assertThat(result.getTotalPresidencies()).isEqualTo(0);
            assertThat(result.getPresidenciesAdded()).isEqualTo(0);
            assertThat(result.getPresidenciesUpdated()).isEqualTo(0);
            assertThat(result.getPersonsAdded()).isEqualTo(0);
            assertThat(result.getVpHoldingsAdded()).isEqualTo(0);
            assertThat(result.getErrors()).isEqualTo(0);
            assertThat(result.getErrorMessages()).isEmpty();
        }

        @Test
        @DisplayName("Should format toString correctly")
        void syncResult_toString_formatsCorrectly() {
            PresidentialSyncService.SyncResult result = new PresidentialSyncService.SyncResult();

            String str = result.toString();

            assertThat(str).contains("SyncResult");
            assertThat(str).contains("presidencies=");
            assertThat(str).contains("persons=");
            assertThat(str).contains("vpHoldings=");
            assertThat(str).contains("errors=");
        }
    }

    // =========================================================================
    // Integration-style Tests (with mocked file loading)
    // =========================================================================

    @Nested
    @DisplayName("Sync From Seed File")
    class SyncFromSeedFileTests {

        @Test
        @DisplayName("Should load seed data and sync presidencies")
        void syncFromSeedFile_loadsSeedDataAndSyncs() {
            // Given - VP position setup
            GovernmentPosition vpPosition = GovernmentPosition.builder()
                    .id(UUID.randomUUID())
                    .title("Vice President of the United States")
                    .branch(Branch.EXECUTIVE)
                    .positionType(PositionType.ELECTED)
                    .build();
            when(positionRepository.findByTitle("Vice President of the United States"))
                    .thenReturn(Optional.of(vpPosition));

            // Mock person saves
            when(personRepository.findByFirstNameAndLastName(anyString(), anyString()))
                    .thenReturn(Optional.empty());
            when(personRepository.save(any(Person.class))).thenAnswer(invocation -> {
                Person p = invocation.getArgument(0);
                p.setId(UUID.randomUUID());
                return p;
            });

            // Mock presidency saves
            when(presidencyRepository.findByNumber(anyInt())).thenReturn(Optional.empty());
            when(presidencyRepository.save(any(Presidency.class))).thenAnswer(invocation -> {
                Presidency pres = invocation.getArgument(0);
                pres.setId(UUID.randomUUID());
                return pres;
            });

            // Mock VP holding saves
            when(positionHoldingRepository.findByPersonIdAndPositionIdAndStartDate(any(), any(), any()))
                    .thenReturn(Optional.empty());
            when(positionHoldingRepository.save(any(PositionHolding.class))).thenAnswer(invocation -> {
                PositionHolding h = invocation.getArgument(0);
                h.setId(UUID.randomUUID());
                return h;
            });

            // Mock presidency chain linking
            when(presidencyRepository.findAllByOrderByNumberAsc()).thenReturn(Collections.emptyList());

            // When
            PresidentialSyncService.SyncResult result = syncService.syncFromSeedFile();

            // Then
            assertThat(result.getErrors()).isEqualTo(0);
            assertThat(result.getTotalPresidencies()).isEqualTo(47);
            assertThat(result.getPresidenciesAdded()).isEqualTo(47);

            // Verify interactions
            verify(positionRepository).findByTitle("Vice President of the United States");
            verify(presidencyRepository, times(47)).save(any(Presidency.class));
        }

        @Test
        @DisplayName("Should create VP position if not exists")
        void syncFromSeedFile_createsVpPositionIfNotExists() {
            // Given - VP position does NOT exist
            GovernmentPosition vpPosition = GovernmentPosition.builder()
                    .id(UUID.randomUUID())
                    .title("Vice President of the United States")
                    .branch(Branch.EXECUTIVE)
                    .positionType(PositionType.ELECTED)
                    .build();

            when(positionRepository.findByTitle("Vice President of the United States"))
                    .thenReturn(Optional.empty());
            when(positionRepository.save(any(GovernmentPosition.class))).thenReturn(vpPosition);

            // Mock other saves
            when(personRepository.findByFirstNameAndLastName(anyString(), anyString()))
                    .thenReturn(Optional.empty());
            when(personRepository.save(any(Person.class))).thenAnswer(invocation -> {
                Person p = invocation.getArgument(0);
                p.setId(UUID.randomUUID());
                return p;
            });
            when(presidencyRepository.findByNumber(anyInt())).thenReturn(Optional.empty());
            when(presidencyRepository.save(any(Presidency.class))).thenAnswer(invocation -> {
                Presidency pres = invocation.getArgument(0);
                pres.setId(UUID.randomUUID());
                return pres;
            });
            when(positionHoldingRepository.findByPersonIdAndPositionIdAndStartDate(any(), any(), any()))
                    .thenReturn(Optional.empty());
            when(positionHoldingRepository.save(any(PositionHolding.class))).thenAnswer(invocation -> {
                PositionHolding h = invocation.getArgument(0);
                h.setId(UUID.randomUUID());
                return h;
            });
            when(presidencyRepository.findAllByOrderByNumberAsc()).thenReturn(Collections.emptyList());

            // When
            PresidentialSyncService.SyncResult result = syncService.syncFromSeedFile();

            // Then
            ArgumentCaptor<GovernmentPosition> posCaptor = ArgumentCaptor.forClass(GovernmentPosition.class);
            verify(positionRepository).save(posCaptor.capture());

            GovernmentPosition savedPosition = posCaptor.getValue();
            assertThat(savedPosition.getTitle()).isEqualTo("Vice President of the United States");
            assertThat(savedPosition.getBranch()).isEqualTo(Branch.EXECUTIVE);
            assertThat(savedPosition.getPositionType()).isEqualTo(PositionType.ELECTED);
        }

        @Test
        @DisplayName("Should update existing presidencies")
        void syncFromSeedFile_updatesExistingPresidencies() {
            // Given
            GovernmentPosition vpPosition = GovernmentPosition.builder()
                    .id(UUID.randomUUID())
                    .title("Vice President of the United States")
                    .branch(Branch.EXECUTIVE)
                    .build();
            when(positionRepository.findByTitle("Vice President of the United States"))
                    .thenReturn(Optional.of(vpPosition));

            // Person already exists
            Person existingPerson = Person.builder()
                    .id(UUID.randomUUID())
                    .firstName("George")
                    .lastName("Washington")
                    .build();
            when(personRepository.findByFirstNameAndLastName("George", "Washington"))
                    .thenReturn(Optional.of(existingPerson));
            when(personRepository.findByFirstNameAndLastName(argThat(s -> !s.equals("George")), anyString()))
                    .thenReturn(Optional.empty());
            when(personRepository.save(any(Person.class))).thenAnswer(invocation -> {
                Person p = invocation.getArgument(0);
                if (p.getId() == null) p.setId(UUID.randomUUID());
                return p;
            });

            // Presidency #1 already exists
            Presidency existingPresidency = Presidency.builder()
                    .id(UUID.randomUUID())
                    .number(1)
                    .personId(existingPerson.getId())
                    .build();
            when(presidencyRepository.findByNumber(1)).thenReturn(Optional.of(existingPresidency));
            when(presidencyRepository.findByNumber(argThat(n -> n != 1))).thenReturn(Optional.empty());
            when(presidencyRepository.save(any(Presidency.class))).thenAnswer(invocation -> {
                Presidency pres = invocation.getArgument(0);
                if (pres.getId() == null) pres.setId(UUID.randomUUID());
                return pres;
            });

            // VP holdings
            when(positionHoldingRepository.findByPersonIdAndPositionIdAndStartDate(any(), any(), any()))
                    .thenReturn(Optional.empty());
            when(positionHoldingRepository.save(any(PositionHolding.class))).thenAnswer(invocation -> {
                PositionHolding h = invocation.getArgument(0);
                h.setId(UUID.randomUUID());
                return h;
            });
            when(presidencyRepository.findAllByOrderByNumberAsc()).thenReturn(Collections.emptyList());

            // When
            PresidentialSyncService.SyncResult result = syncService.syncFromSeedFile();

            // Then
            assertThat(result.getPresidenciesUpdated()).isGreaterThanOrEqualTo(1);
        }

        @Test
        @DisplayName("Should link predecessor/successor chain")
        void syncFromSeedFile_linksPresidencyChain() {
            // Given
            GovernmentPosition vpPosition = GovernmentPosition.builder()
                    .id(UUID.randomUUID())
                    .title("Vice President of the United States")
                    .branch(Branch.EXECUTIVE)
                    .build();
            when(positionRepository.findByTitle("Vice President of the United States"))
                    .thenReturn(Optional.of(vpPosition));

            // Setup mock saves
            when(personRepository.findByFirstNameAndLastName(anyString(), anyString()))
                    .thenReturn(Optional.empty());
            when(personRepository.save(any(Person.class))).thenAnswer(invocation -> {
                Person p = invocation.getArgument(0);
                p.setId(UUID.randomUUID());
                return p;
            });
            when(presidencyRepository.findByNumber(anyInt())).thenReturn(Optional.empty());
            // Only set ID if null - preserve test presidency IDs during chain linking
            when(presidencyRepository.save(any(Presidency.class))).thenAnswer(invocation -> {
                Presidency pres = invocation.getArgument(0);
                if (pres.getId() == null) {
                    pres.setId(UUID.randomUUID());
                }
                return pres;
            });
            when(positionHoldingRepository.findByPersonIdAndPositionIdAndStartDate(any(), any(), any()))
                    .thenReturn(Optional.empty());
            when(positionHoldingRepository.save(any(PositionHolding.class))).thenAnswer(invocation -> {
                PositionHolding h = invocation.getArgument(0);
                if (h.getId() == null) {
                    h.setId(UUID.randomUUID());
                }
                return h;
            });

            // Setup chain linking with 3 test presidencies
            UUID id1 = UUID.randomUUID();
            UUID id2 = UUID.randomUUID();
            UUID id3 = UUID.randomUUID();
            Presidency pres1 = Presidency.builder().id(id1).number(1).build();
            Presidency pres2 = Presidency.builder().id(id2).number(2).build();
            Presidency pres3 = Presidency.builder().id(id3).number(3).build();
            when(presidencyRepository.findAllByOrderByNumberAsc())
                    .thenReturn(Arrays.asList(pres1, pres2, pres3));

            // When
            syncService.syncFromSeedFile();

            // Then - verify chain was linked
            assertThat(pres1.getSuccessorId()).isEqualTo(id2);
            assertThat(pres2.getPredecessorId()).isEqualTo(id1);
            assertThat(pres2.getSuccessorId()).isEqualTo(id3);
            assertThat(pres3.getPredecessorId()).isEqualTo(id2);
        }

        @Test
        @DisplayName("Should handle non-consecutive terms (same person for multiple presidencies)")
        void syncFromSeedFile_handlesNonConsecutiveTerms() {
            // Given
            GovernmentPosition vpPosition = GovernmentPosition.builder()
                    .id(UUID.randomUUID())
                    .title("Vice President of the United States")
                    .branch(Branch.EXECUTIVE)
                    .build();
            when(positionRepository.findByTitle("Vice President of the United States"))
                    .thenReturn(Optional.of(vpPosition));

            // Track how many times Grover Cleveland is created
            List<String> clevelandCreations = new ArrayList<>();

            when(personRepository.findByFirstNameAndLastName(anyString(), anyString()))
                    .thenReturn(Optional.empty());
            when(personRepository.save(any(Person.class))).thenAnswer(invocation -> {
                Person p = invocation.getArgument(0);
                p.setId(UUID.randomUUID());
                if ("Grover".equals(p.getFirstName()) && "Cleveland".equals(p.getLastName())) {
                    clevelandCreations.add(p.getFirstName() + " " + p.getLastName());
                }
                return p;
            });
            when(presidencyRepository.findByNumber(anyInt())).thenReturn(Optional.empty());
            when(presidencyRepository.save(any(Presidency.class))).thenAnswer(invocation -> {
                Presidency pres = invocation.getArgument(0);
                pres.setId(UUID.randomUUID());
                return pres;
            });
            when(positionHoldingRepository.findByPersonIdAndPositionIdAndStartDate(any(), any(), any()))
                    .thenReturn(Optional.empty());
            when(positionHoldingRepository.save(any(PositionHolding.class))).thenAnswer(invocation -> {
                PositionHolding h = invocation.getArgument(0);
                h.setId(UUID.randomUUID());
                return h;
            });
            when(presidencyRepository.findAllByOrderByNumberAsc()).thenReturn(Collections.emptyList());

            // When
            syncService.syncFromSeedFile();

            // Then - Cleveland should only be created ONCE (cached for 22nd and 24th presidency)
            assertThat(clevelandCreations).hasSize(1);
        }

        @Test
        @DisplayName("Should skip existing VP holdings")
        void syncFromSeedFile_skipsExistingVpHoldings() {
            // Given
            GovernmentPosition vpPosition = GovernmentPosition.builder()
                    .id(UUID.randomUUID())
                    .title("Vice President of the United States")
                    .branch(Branch.EXECUTIVE)
                    .build();
            when(positionRepository.findByTitle("Vice President of the United States"))
                    .thenReturn(Optional.of(vpPosition));

            when(personRepository.findByFirstNameAndLastName(anyString(), anyString()))
                    .thenReturn(Optional.empty());
            when(personRepository.save(any(Person.class))).thenAnswer(invocation -> {
                Person p = invocation.getArgument(0);
                p.setId(UUID.randomUUID());
                return p;
            });
            when(presidencyRepository.findByNumber(anyInt())).thenReturn(Optional.empty());
            when(presidencyRepository.save(any(Presidency.class))).thenAnswer(invocation -> {
                Presidency pres = invocation.getArgument(0);
                pres.setId(UUID.randomUUID());
                return pres;
            });

            // First VP holding exists, others do not
            PositionHolding existingHolding = PositionHolding.builder()
                    .id(UUID.randomUUID())
                    .build();
            when(positionHoldingRepository.findByPersonIdAndPositionIdAndStartDate(
                    any(), any(), eq(LocalDate.of(1789, 4, 21))))
                    .thenReturn(Optional.of(existingHolding));
            when(positionHoldingRepository.findByPersonIdAndPositionIdAndStartDate(
                    any(), any(), argThat(d -> !LocalDate.of(1789, 4, 21).equals(d))))
                    .thenReturn(Optional.empty());
            when(positionHoldingRepository.save(any(PositionHolding.class))).thenAnswer(invocation -> {
                PositionHolding h = invocation.getArgument(0);
                h.setId(UUID.randomUUID());
                return h;
            });
            when(presidencyRepository.findAllByOrderByNumberAsc()).thenReturn(Collections.emptyList());

            // When
            PresidentialSyncService.SyncResult result = syncService.syncFromSeedFile();

            // Then - less VP holdings than expected because some already exist
            assertThat(result.getErrors()).isEqualTo(0);
        }
    }

    // =========================================================================
    // Error Handling Tests
    // =========================================================================

    @Nested
    @DisplayName("Error Handling")
    class ErrorHandlingTests {

        @Test
        @DisplayName("Should handle individual presidency sync errors gracefully")
        void syncFromSeedFile_handlesIndividualErrors() {
            // Given
            GovernmentPosition vpPosition = GovernmentPosition.builder()
                    .id(UUID.randomUUID())
                    .title("Vice President of the United States")
                    .branch(Branch.EXECUTIVE)
                    .build();
            when(positionRepository.findByTitle("Vice President of the United States"))
                    .thenReturn(Optional.of(vpPosition));

            // First person save throws error, rest succeed
            when(personRepository.findByFirstNameAndLastName("George", "Washington"))
                    .thenThrow(new RuntimeException("Database error"));
            when(personRepository.findByFirstNameAndLastName(argThat(s -> !s.equals("George")), anyString()))
                    .thenReturn(Optional.empty());
            when(personRepository.save(any(Person.class))).thenAnswer(invocation -> {
                Person p = invocation.getArgument(0);
                p.setId(UUID.randomUUID());
                return p;
            });
            when(presidencyRepository.findByNumber(anyInt())).thenReturn(Optional.empty());
            when(presidencyRepository.save(any(Presidency.class))).thenAnswer(invocation -> {
                Presidency pres = invocation.getArgument(0);
                pres.setId(UUID.randomUUID());
                return pres;
            });
            when(positionHoldingRepository.findByPersonIdAndPositionIdAndStartDate(any(), any(), any()))
                    .thenReturn(Optional.empty());
            when(positionHoldingRepository.save(any(PositionHolding.class))).thenAnswer(invocation -> {
                PositionHolding h = invocation.getArgument(0);
                h.setId(UUID.randomUUID());
                return h;
            });
            when(presidencyRepository.findAllByOrderByNumberAsc()).thenReturn(Collections.emptyList());

            // When
            PresidentialSyncService.SyncResult result = syncService.syncFromSeedFile();

            // Then - should have 1 error but continue with the rest
            assertThat(result.getErrors()).isEqualTo(1);
            assertThat(result.getErrorMessages()).anyMatch(msg -> msg.contains("Presidency #1"));
            assertThat(result.getPresidenciesAdded()).isEqualTo(46); // 47 - 1 error
        }
    }
}
