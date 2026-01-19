package org.newsanalyzer.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.newsanalyzer.model.CongressionalMember;
import org.newsanalyzer.model.CongressionalMember.Chamber;
import org.newsanalyzer.model.DataSource;
import org.newsanalyzer.model.Individual;
import org.newsanalyzer.repository.CongressionalMemberRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for CongressionalMemberService.
 *
 * Tests CRUD operations, identifier lookups, chamber/state queries,
 * and the two-entity pattern (findOrCreate).
 *
 * Part of ARCH-1.6: Update Services Layer
 *
 * @author Claude (Dev Agent)
 * @since 3.0.0
 */
@ExtendWith(MockitoExtension.class)
class CongressionalMemberServiceTest {

    @Mock
    private CongressionalMemberRepository congressionalMemberRepository;

    @Mock
    private IndividualService individualService;

    @InjectMocks
    private CongressionalMemberService congressionalMemberService;

    private CongressionalMember testMember;
    private Individual testIndividual;
    private UUID testMemberId;
    private UUID testIndividualId;
    private LocalDate testBirthDate;

    @BeforeEach
    void setUp() {
        testMemberId = UUID.randomUUID();
        testIndividualId = UUID.randomUUID();
        testBirthDate = LocalDate.of(1946, 6, 22);

        testIndividual = Individual.builder()
                .id(testIndividualId)
                .firstName("Elizabeth")
                .lastName("Warren")
                .birthDate(testBirthDate)
                .build();

        testMember = CongressionalMember.builder()
                .id(testMemberId)
                .individualId(testIndividualId)
                .bioguideId("W000817")
                .chamber(Chamber.SENATE)
                .state("MA")
                .party("Democratic")
                .dataSource(DataSource.CONGRESS_GOV)
                .build();
    }

    // =====================================================================
    // CRUD Operations
    // =====================================================================

    @Test
    void findById_existingMember_returnsMember() {
        when(congressionalMemberRepository.findById(testMemberId)).thenReturn(Optional.of(testMember));

        Optional<CongressionalMember> result = congressionalMemberService.findById(testMemberId);

        assertTrue(result.isPresent());
        assertEquals("W000817", result.get().getBioguideId());
        verify(congressionalMemberRepository).findById(testMemberId);
    }

    @Test
    void findById_nonExistingMember_returnsEmpty() {
        when(congressionalMemberRepository.findById(testMemberId)).thenReturn(Optional.empty());

        Optional<CongressionalMember> result = congressionalMemberService.findById(testMemberId);

        assertFalse(result.isPresent());
    }

    @Test
    void save_member_returnsSavedMember() {
        when(congressionalMemberRepository.save(testMember)).thenReturn(testMember);

        CongressionalMember result = congressionalMemberService.save(testMember);

        assertNotNull(result);
        assertEquals(testMemberId, result.getId());
        verify(congressionalMemberRepository).save(testMember);
    }

    @Test
    void deleteById_existingMember_deletesSuccessfully() {
        doNothing().when(congressionalMemberRepository).deleteById(testMemberId);

        congressionalMemberService.deleteById(testMemberId);

        verify(congressionalMemberRepository).deleteById(testMemberId);
    }

    @Test
    void findAll_returnsAllMembers() {
        List<CongressionalMember> members = Arrays.asList(testMember);
        when(congressionalMemberRepository.findAll()).thenReturn(members);

        List<CongressionalMember> result = congressionalMemberService.findAll();

        assertEquals(1, result.size());
        assertEquals("W000817", result.get(0).getBioguideId());
    }

    @Test
    void findAll_paginated_returnsPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<CongressionalMember> page = new PageImpl<>(Arrays.asList(testMember));
        when(congressionalMemberRepository.findAll(pageable)).thenReturn(page);

        Page<CongressionalMember> result = congressionalMemberService.findAll(pageable);

        assertEquals(1, result.getContent().size());
    }

    // =====================================================================
    // Identifier Lookups
    // =====================================================================

    @Test
    void findByBioguideId_existingBioguide_returnsMember() {
        when(congressionalMemberRepository.findByBioguideId("W000817"))
                .thenReturn(Optional.of(testMember));

        Optional<CongressionalMember> result = congressionalMemberService.findByBioguideId("W000817");

        assertTrue(result.isPresent());
        assertEquals(Chamber.SENATE, result.get().getChamber());
    }

    @Test
    void findByBioguideId_nonExistingBioguide_returnsEmpty() {
        when(congressionalMemberRepository.findByBioguideId("X999999"))
                .thenReturn(Optional.empty());

        Optional<CongressionalMember> result = congressionalMemberService.findByBioguideId("X999999");

        assertFalse(result.isPresent());
    }

    @Test
    void findByIndividualId_existingIndividual_returnsMember() {
        when(congressionalMemberRepository.findByIndividualId(testIndividualId))
                .thenReturn(Optional.of(testMember));

        Optional<CongressionalMember> result = congressionalMemberService.findByIndividualId(testIndividualId);

        assertTrue(result.isPresent());
        assertEquals(testIndividualId, result.get().getIndividualId());
    }

    @Test
    void existsByBioguideId_existingBioguide_returnsTrue() {
        when(congressionalMemberRepository.existsByBioguideId("W000817")).thenReturn(true);

        boolean result = congressionalMemberService.existsByBioguideId("W000817");

        assertTrue(result);
    }

    @Test
    void existsByBioguideId_nonExistingBioguide_returnsFalse() {
        when(congressionalMemberRepository.existsByBioguideId("X999999")).thenReturn(false);

        boolean result = congressionalMemberService.existsByBioguideId("X999999");

        assertFalse(result);
    }

    // =====================================================================
    // Eager Loading Queries
    // =====================================================================

    @Test
    void findByBioguideIdWithIndividual_returnsMemberWithIndividual() {
        when(congressionalMemberRepository.findByBioguideIdWithIndividual("W000817"))
                .thenReturn(Optional.of(testMember));

        Optional<CongressionalMember> result =
                congressionalMemberService.findByBioguideIdWithIndividual("W000817");

        assertTrue(result.isPresent());
        verify(congressionalMemberRepository).findByBioguideIdWithIndividual("W000817");
    }

    @Test
    void findByChamberWithIndividual_returnsMembers() {
        List<CongressionalMember> members = Arrays.asList(testMember);
        when(congressionalMemberRepository.findByChamberWithIndividual(Chamber.SENATE))
                .thenReturn(members);

        List<CongressionalMember> result =
                congressionalMemberService.findByChamberWithIndividual(Chamber.SENATE);

        assertEquals(1, result.size());
    }

    @Test
    void findByStateWithIndividual_returnsMembers() {
        List<CongressionalMember> members = Arrays.asList(testMember);
        when(congressionalMemberRepository.findByStateWithIndividual("MA"))
                .thenReturn(members);

        List<CongressionalMember> result =
                congressionalMemberService.findByStateWithIndividual("MA");

        assertEquals(1, result.size());
    }

    // =====================================================================
    // Chamber/State/Party Queries
    // =====================================================================

    @Test
    void findByChamber_senate_returnsSenatMembers() {
        List<CongressionalMember> senators = Arrays.asList(testMember);
        when(congressionalMemberRepository.findByChamber(Chamber.SENATE)).thenReturn(senators);

        List<CongressionalMember> result = congressionalMemberService.findByChamber(Chamber.SENATE);

        assertEquals(1, result.size());
        assertEquals(Chamber.SENATE, result.get(0).getChamber());
    }

    @Test
    void findByChamber_paginated_returnsPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<CongressionalMember> page = new PageImpl<>(Arrays.asList(testMember));
        when(congressionalMemberRepository.findByChamber(Chamber.SENATE, pageable)).thenReturn(page);

        Page<CongressionalMember> result =
                congressionalMemberService.findByChamber(Chamber.SENATE, pageable);

        assertEquals(1, result.getContent().size());
    }

    @Test
    void findByState_returnsStateMembers() {
        List<CongressionalMember> maMembers = Arrays.asList(testMember);
        when(congressionalMemberRepository.findByState("MA")).thenReturn(maMembers);

        List<CongressionalMember> result = congressionalMemberService.findByState("MA");

        assertEquals(1, result.size());
        assertEquals("MA", result.get(0).getState());
    }

    @Test
    void findByParty_returnsPartyMembers() {
        List<CongressionalMember> dems = Arrays.asList(testMember);
        when(congressionalMemberRepository.findByParty("Democratic")).thenReturn(dems);

        List<CongressionalMember> result = congressionalMemberService.findByParty("Democratic");

        assertEquals(1, result.size());
        assertEquals("Democratic", result.get(0).getParty());
    }

    @Test
    void findByStateAndChamber_returnsFilteredMembers() {
        List<CongressionalMember> maSenatrors = Arrays.asList(testMember);
        when(congressionalMemberRepository.findByStateAndChamber("MA", Chamber.SENATE))
                .thenReturn(maSenatrors);

        List<CongressionalMember> result =
                congressionalMemberService.findByStateAndChamber("MA", Chamber.SENATE);

        assertEquals(1, result.size());
    }

    // =====================================================================
    // Statistics
    // =====================================================================

    @Test
    void countByChamber_returnsCorrectCount() {
        when(congressionalMemberRepository.countByChamber(Chamber.SENATE)).thenReturn(100L);

        long result = congressionalMemberService.countByChamber(Chamber.SENATE);

        assertEquals(100L, result);
    }

    @Test
    void countByParty_returnsCorrectCount() {
        when(congressionalMemberRepository.countByParty("Democratic")).thenReturn(50L);

        long result = congressionalMemberService.countByParty("Democratic");

        assertEquals(50L, result);
    }

    @Test
    void countByState_returnsCorrectCount() {
        when(congressionalMemberRepository.countByState("MA")).thenReturn(11L);

        long result = congressionalMemberService.countByState("MA");

        assertEquals(11L, result);
    }

    @Test
    void count_returnsTotal() {
        when(congressionalMemberRepository.count()).thenReturn(535L);

        long result = congressionalMemberService.count();

        assertEquals(535L, result);
    }

    // =====================================================================
    // Two-Entity Pattern (findOrCreate)
    // =====================================================================

    @Test
    void findOrCreate_existingMember_returnsMemberAndUpdatesFields() {
        CongressionalMember existingMember = CongressionalMember.builder()
                .id(testMemberId)
                .individualId(testIndividualId)
                .bioguideId("W000817")
                .chamber(Chamber.SENATE)
                .state("MA")
                .party("Democratic")
                .build();

        when(congressionalMemberRepository.findByBioguideId("W000817"))
                .thenReturn(Optional.of(existingMember));

        CongressionalMember result = congressionalMemberService.findOrCreate(
                "W000817",
                "Elizabeth", "Warren", testBirthDate,
                Chamber.SENATE, "MA", "Democratic"
        );

        assertEquals(testMemberId, result.getId());
        verify(individualService, never()).findOrCreate(any(), any(), any(), any());
    }

    @Test
    void findOrCreate_existingMember_updatesChangedFields() {
        CongressionalMember existingMember = CongressionalMember.builder()
                .id(testMemberId)
                .individualId(testIndividualId)
                .bioguideId("W000817")
                .chamber(Chamber.SENATE)
                .state("MA")
                .party("Democratic")
                .build();

        when(congressionalMemberRepository.findByBioguideId("W000817"))
                .thenReturn(Optional.of(existingMember));
        when(congressionalMemberRepository.save(any(CongressionalMember.class)))
                .thenReturn(existingMember);

        // Change party to Republican
        CongressionalMember result = congressionalMemberService.findOrCreate(
                "W000817",
                "Elizabeth", "Warren", testBirthDate,
                Chamber.SENATE, "MA", "Republican"
        );

        verify(congressionalMemberRepository).save(argThat(m -> "Republican".equals(m.getParty())));
    }

    @Test
    void findOrCreate_newMember_createsIndividualAndMember() {
        when(congressionalMemberRepository.findByBioguideId("S000148"))
                .thenReturn(Optional.empty());
        when(individualService.findOrCreate("Charles", "Schumer", testBirthDate, DataSource.CONGRESS_GOV))
                .thenReturn(testIndividual);

        CongressionalMember newMember = CongressionalMember.builder()
                .id(UUID.randomUUID())
                .individualId(testIndividualId)
                .bioguideId("S000148")
                .chamber(Chamber.SENATE)
                .state("NY")
                .party("Democratic")
                .build();
        when(congressionalMemberRepository.save(any(CongressionalMember.class)))
                .thenReturn(newMember);

        CongressionalMember result = congressionalMemberService.findOrCreate(
                "S000148",
                "Charles", "Schumer", testBirthDate,
                Chamber.SENATE, "NY", "Democratic"
        );

        assertNotNull(result);
        assertEquals("S000148", result.getBioguideId());
        verify(individualService).findOrCreate("Charles", "Schumer", testBirthDate, DataSource.CONGRESS_GOV);
        verify(congressionalMemberRepository).save(any(CongressionalMember.class));
    }

    @Test
    void findOrCreate_nullBioguideId_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            congressionalMemberService.findOrCreate(
                    null,
                    "Elizabeth", "Warren", testBirthDate,
                    Chamber.SENATE, "MA", "Democratic"
            );
        });
    }

    @Test
    void findOrCreate_blankBioguideId_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            congressionalMemberService.findOrCreate(
                    "   ",
                    "Elizabeth", "Warren", testBirthDate,
                    Chamber.SENATE, "MA", "Democratic"
            );
        });
    }

    // =====================================================================
    // getIndividual
    // =====================================================================

    @Test
    void getIndividual_existingMember_returnsIndividual() {
        when(individualService.findById(testIndividualId)).thenReturn(Optional.of(testIndividual));

        Optional<Individual> result = congressionalMemberService.getIndividual(testMember);

        assertTrue(result.isPresent());
        assertEquals("Elizabeth", result.get().getFirstName());
    }

    @Test
    void getIndividual_nullMember_returnsEmpty() {
        Optional<Individual> result = congressionalMemberService.getIndividual(null);

        assertFalse(result.isPresent());
        verify(individualService, never()).findById(any());
    }

    @Test
    void getIndividual_memberWithNullIndividualId_returnsEmpty() {
        CongressionalMember memberWithoutIndividual = CongressionalMember.builder()
                .id(testMemberId)
                .bioguideId("X000000")
                .build();

        Optional<Individual> result = congressionalMemberService.getIndividual(memberWithoutIndividual);

        assertFalse(result.isPresent());
        verify(individualService, never()).findById(any());
    }
}
