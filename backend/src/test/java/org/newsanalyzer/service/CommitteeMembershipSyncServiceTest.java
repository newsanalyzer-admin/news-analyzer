package org.newsanalyzer.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.newsanalyzer.model.Committee;
import org.newsanalyzer.model.CommitteeChamber;
import org.newsanalyzer.model.CommitteeMembership;
import org.newsanalyzer.model.CommitteeType;
import org.newsanalyzer.model.MembershipRole;
import org.newsanalyzer.model.Person;
import org.newsanalyzer.repository.CommitteeMembershipRepository;
import org.newsanalyzer.repository.CommitteeRepository;
import org.newsanalyzer.repository.PersonRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for CommitteeMembershipSyncService.
 *
 * @author James (Dev Agent)
 */
@ExtendWith(MockitoExtension.class)
class CommitteeMembershipSyncServiceTest {

    @Mock
    private CongressApiClient congressApiClient;

    @Mock
    private CommitteeMembershipRepository membershipRepository;

    @Mock
    private CommitteeRepository committeeRepository;

    @Mock
    private PersonRepository personRepository;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private CommitteeMembershipSyncService syncService;

    private ObjectMapper realMapper;
    private Committee testCommittee;
    private Person testPerson;

    @BeforeEach
    void setUp() {
        realMapper = new ObjectMapper();

        testCommittee = new Committee();
        testCommittee.setCommitteeCode("hsju00");
        testCommittee.setName("Committee on the Judiciary");
        testCommittee.setChamber(CommitteeChamber.HOUSE);
        testCommittee.setCommitteeType(CommitteeType.STANDING);

        testPerson = new Person();
        testPerson.setId(UUID.randomUUID());
        testPerson.setBioguideId("S000033");
        testPerson.setFirstName("Bernard");
        testPerson.setLastName("Sanders");
    }

    // =========================================================================
    // syncAllMemberships Tests
    // =========================================================================

    @Nested
    @DisplayName("syncAllMemberships")
    class SyncAllMembershipsTests {

        @Test
        @DisplayName("Should return empty result when API not configured")
        void apiNotConfigured_returnsEmptyResult() {
            // Given
            when(congressApiClient.isConfigured()).thenReturn(false);

            // When
            CommitteeMembershipSyncService.SyncResult result = syncService.syncAllMemberships();

            // Then
            assertThat(result.getTotal()).isEqualTo(0);
            assertThat(result.getAdded()).isEqualTo(0);
            assertThat(result.getUpdated()).isEqualTo(0);
            verify(committeeRepository, never()).findAll();
        }

        @Test
        @DisplayName("Should use default congress (118) when no congress specified")
        void noCongressSpecified_usesDefault118() {
            // Given
            when(congressApiClient.isConfigured()).thenReturn(true);
            when(committeeRepository.findAll()).thenReturn(List.of());

            // When
            syncService.syncAllMemberships();

            // Then
            verify(committeeRepository).findAll();
        }

        @Test
        @DisplayName("Should sync memberships for all committees")
        void configured_syncsAllCommittees() {
            // Given
            when(congressApiClient.isConfigured()).thenReturn(true);
            when(committeeRepository.findAll()).thenReturn(List.of(testCommittee));

            ObjectNode response = createCommitteeResponse(List.of());
            when(congressApiClient.fetchCommitteeByCode("house", "hsju00"))
                    .thenReturn(Optional.of(response));
            when(committeeRepository.findByCommitteeCode("hsju00"))
                    .thenReturn(Optional.of(testCommittee));

            // When
            CommitteeMembershipSyncService.SyncResult result = syncService.syncAllMemberships(118);

            // Then
            assertThat(result.getTotal()).isEqualTo(1);
            verify(congressApiClient).fetchCommitteeByCode("house", "hsju00");
        }

        @Test
        @DisplayName("Should accumulate stats from all committees")
        void multipleCommittees_accumulatesStats() {
            // Given
            Committee senateCommittee = new Committee();
            senateCommittee.setCommitteeCode("ssju00");
            senateCommittee.setName("Senate Judiciary");
            senateCommittee.setChamber(CommitteeChamber.SENATE);

            when(congressApiClient.isConfigured()).thenReturn(true);
            when(committeeRepository.findAll()).thenReturn(List.of(testCommittee, senateCommittee));

            ObjectNode houseResponse = createCommitteeResponse(List.of(
                    createMemberNode("S000033", "Member")
            ));
            ObjectNode senateResponse = createCommitteeResponse(List.of(
                    createMemberNode("P000197", "Chair")
            ));

            when(congressApiClient.fetchCommitteeByCode("house", "hsju00"))
                    .thenReturn(Optional.of(houseResponse));
            when(congressApiClient.fetchCommitteeByCode("senate", "ssju00"))
                    .thenReturn(Optional.of(senateResponse));
            when(committeeRepository.findByCommitteeCode("hsju00"))
                    .thenReturn(Optional.of(testCommittee));
            when(committeeRepository.findByCommitteeCode("ssju00"))
                    .thenReturn(Optional.of(senateCommittee));

            Person person1 = createPerson("S000033");
            Person person2 = createPerson("P000197");
            when(personRepository.findByBioguideId("S000033")).thenReturn(Optional.of(person1));
            when(personRepository.findByBioguideId("P000197")).thenReturn(Optional.of(person2));
            when(membershipRepository.findByPerson_IdAndCommittee_CommitteeCodeAndCongress(any(), any(), any()))
                    .thenReturn(Optional.empty());
            when(membershipRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            // When
            CommitteeMembershipSyncService.SyncResult result = syncService.syncAllMemberships(118);

            // Then
            assertThat(result.getAdded()).isEqualTo(2);
        }

        @Test
        @DisplayName("Should handle committee sync failure gracefully")
        void committeeSyncFails_incrementsErrors() {
            // Given
            when(congressApiClient.isConfigured()).thenReturn(true);
            when(committeeRepository.findAll()).thenReturn(List.of(testCommittee));
            when(congressApiClient.fetchCommitteeByCode(any(), any()))
                    .thenThrow(new RuntimeException("API Error"));

            // When
            CommitteeMembershipSyncService.SyncResult result = syncService.syncAllMemberships(118);

            // Then
            assertThat(result.getErrors()).isEqualTo(1);
        }
    }

    // =========================================================================
    // syncMembershipsForCommittee Tests
    // =========================================================================

    @Nested
    @DisplayName("syncMembershipsForCommittee")
    class SyncMembershipsForCommitteeTests {

        @Test
        @DisplayName("Should return error when API response is empty")
        void apiResponseEmpty_returnsError() {
            // Given
            when(congressApiClient.fetchCommitteeByCode("house", "hsju00"))
                    .thenReturn(Optional.empty());

            // When
            CommitteeMembershipSyncService.SyncResult result =
                    syncService.syncMembershipsForCommittee("hsju00", "house", 118);

            // Then
            assertThat(result.getErrors()).isEqualTo(1);
            assertThat(result.getAdded()).isEqualTo(0);
        }

        @Test
        @DisplayName("Should return error when committee data is missing")
        void committeeDataMissing_returnsError() {
            // Given
            ObjectNode emptyResponse = realMapper.createObjectNode();
            when(congressApiClient.fetchCommitteeByCode("house", "hsju00"))
                    .thenReturn(Optional.of(emptyResponse));

            // When
            CommitteeMembershipSyncService.SyncResult result =
                    syncService.syncMembershipsForCommittee("hsju00", "house", 118);

            // Then
            assertThat(result.getErrors()).isEqualTo(1);
        }

        @Test
        @DisplayName("Should skip when committee not in database")
        void committeeNotInDatabase_skips() {
            // Given
            ObjectNode response = createCommitteeResponse(List.of());
            when(congressApiClient.fetchCommitteeByCode("house", "hsju00"))
                    .thenReturn(Optional.of(response));
            when(committeeRepository.findByCommitteeCode("hsju00"))
                    .thenReturn(Optional.empty());

            // When
            CommitteeMembershipSyncService.SyncResult result =
                    syncService.syncMembershipsForCommittee("hsju00", "house", 118);

            // Then
            assertThat(result.getSkipped()).isEqualTo(1);
        }

        @Test
        @DisplayName("Should add new membership")
        void newMembership_addsToDatabase() {
            // Given
            ObjectNode response = createCommitteeResponse(List.of(
                    createMemberNode("S000033", "Member")
            ));
            when(congressApiClient.fetchCommitteeByCode("house", "hsju00"))
                    .thenReturn(Optional.of(response));
            when(committeeRepository.findByCommitteeCode("hsju00"))
                    .thenReturn(Optional.of(testCommittee));
            when(personRepository.findByBioguideId("S000033"))
                    .thenReturn(Optional.of(testPerson));
            when(membershipRepository.findByPerson_IdAndCommittee_CommitteeCodeAndCongress(
                    testPerson.getId(), "hsju00", 118))
                    .thenReturn(Optional.empty());
            when(membershipRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            // When
            CommitteeMembershipSyncService.SyncResult result =
                    syncService.syncMembershipsForCommittee("hsju00", "house", 118);

            // Then
            assertThat(result.getAdded()).isEqualTo(1);
            assertThat(result.getTotal()).isEqualTo(1);

            ArgumentCaptor<CommitteeMembership> captor = ArgumentCaptor.forClass(CommitteeMembership.class);
            verify(membershipRepository).save(captor.capture());
            assertThat(captor.getValue().getPerson()).isEqualTo(testPerson);
            assertThat(captor.getValue().getCommittee()).isEqualTo(testCommittee);
            assertThat(captor.getValue().getCongress()).isEqualTo(118);
        }

        @Test
        @DisplayName("Should update existing membership")
        void existingMembership_updates() {
            // Given
            ObjectNode response = createCommitteeResponse(List.of(
                    createMemberNode("S000033", "Chair")
            ));
            when(congressApiClient.fetchCommitteeByCode("house", "hsju00"))
                    .thenReturn(Optional.of(response));
            when(committeeRepository.findByCommitteeCode("hsju00"))
                    .thenReturn(Optional.of(testCommittee));
            when(personRepository.findByBioguideId("S000033"))
                    .thenReturn(Optional.of(testPerson));

            CommitteeMembership existingMembership = new CommitteeMembership();
            existingMembership.setId(UUID.randomUUID());
            existingMembership.setRole(MembershipRole.MEMBER);
            when(membershipRepository.findByPerson_IdAndCommittee_CommitteeCodeAndCongress(
                    testPerson.getId(), "hsju00", 118))
                    .thenReturn(Optional.of(existingMembership));
            when(membershipRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            // When
            CommitteeMembershipSyncService.SyncResult result =
                    syncService.syncMembershipsForCommittee("hsju00", "house", 118);

            // Then
            assertThat(result.getUpdated()).isEqualTo(1);
            assertThat(result.getAdded()).isEqualTo(0);

            ArgumentCaptor<CommitteeMembership> captor = ArgumentCaptor.forClass(CommitteeMembership.class);
            verify(membershipRepository).save(captor.capture());
            assertThat(captor.getValue().getRole()).isEqualTo(MembershipRole.CHAIR);
        }

        @Test
        @DisplayName("Should skip member when person not found")
        void personNotFound_skipsMember() {
            // Given
            ObjectNode response = createCommitteeResponse(List.of(
                    createMemberNode("UNKNOWN", "Member")
            ));
            when(congressApiClient.fetchCommitteeByCode("house", "hsju00"))
                    .thenReturn(Optional.of(response));
            when(committeeRepository.findByCommitteeCode("hsju00"))
                    .thenReturn(Optional.of(testCommittee));
            when(personRepository.findByBioguideId("UNKNOWN"))
                    .thenReturn(Optional.empty());

            // When
            CommitteeMembershipSyncService.SyncResult result =
                    syncService.syncMembershipsForCommittee("hsju00", "house", 118);

            // Then
            assertThat(result.getSkipped()).isEqualTo(1);
            assertThat(result.getAdded()).isEqualTo(0);
        }

        @Test
        @DisplayName("Should skip member when bioguideId missing")
        void bioguideIdMissing_skipsMember() {
            // Given
            ObjectNode memberNode = realMapper.createObjectNode();
            memberNode.put("role", "Member");
            // No bioguideId

            ObjectNode response = createCommitteeResponse(List.of(memberNode));
            when(congressApiClient.fetchCommitteeByCode("house", "hsju00"))
                    .thenReturn(Optional.of(response));
            when(committeeRepository.findByCommitteeCode("hsju00"))
                    .thenReturn(Optional.of(testCommittee));

            // When
            CommitteeMembershipSyncService.SyncResult result =
                    syncService.syncMembershipsForCommittee("hsju00", "house", 118);

            // Then
            assertThat(result.getSkipped()).isEqualTo(1);
        }

        @Test
        @DisplayName("Should handle multiple members")
        void multipleMembers_syncsAll() {
            // Given
            ObjectNode response = createCommitteeResponse(List.of(
                    createMemberNode("S000033", "Chair"),
                    createMemberNode("P000197", "Ranking Member"),
                    createMemberNode("M000355", "Member")
            ));
            when(congressApiClient.fetchCommitteeByCode("house", "hsju00"))
                    .thenReturn(Optional.of(response));
            when(committeeRepository.findByCommitteeCode("hsju00"))
                    .thenReturn(Optional.of(testCommittee));

            Person person1 = createPerson("S000033");
            Person person2 = createPerson("P000197");
            Person person3 = createPerson("M000355");
            when(personRepository.findByBioguideId("S000033")).thenReturn(Optional.of(person1));
            when(personRepository.findByBioguideId("P000197")).thenReturn(Optional.of(person2));
            when(personRepository.findByBioguideId("M000355")).thenReturn(Optional.of(person3));
            when(membershipRepository.findByPerson_IdAndCommittee_CommitteeCodeAndCongress(any(), any(), any()))
                    .thenReturn(Optional.empty());
            when(membershipRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            // When
            CommitteeMembershipSyncService.SyncResult result =
                    syncService.syncMembershipsForCommittee("hsju00", "house", 118);

            // Then
            assertThat(result.getAdded()).isEqualTo(3);
            assertThat(result.getTotal()).isEqualTo(3);
            verify(membershipRepository, times(3)).save(any());
        }
    }

    // =========================================================================
    // Role Mapping Tests
    // =========================================================================

    @Nested
    @DisplayName("Role Mapping")
    class RoleMappingTests {

        @ParameterizedTest
        @CsvSource({
                "Chair, CHAIR",
                "CHAIR, CHAIR",
                "Committee Chair, CHAIR",
                "Vice Chair, VICE_CHAIR",
                "Vice-Chair, VICE_CHAIR",
                "VICE CHAIR, VICE_CHAIR",
                "Ranking Member, RANKING_MEMBER",
                "RANKING MEMBER, RANKING_MEMBER",
                "Ex Officio, EX_OFFICIO",
                "Ex-Officio, EX_OFFICIO",
                "EX OFFICIO, EX_OFFICIO",
                "Member, MEMBER",
                "MEMBER, MEMBER",
                "'', MEMBER"
        })
        @DisplayName("Should map role string to correct enum")
        void mapsRoleCorrectly(String roleStr, String expectedRole) {
            // Given
            ObjectNode response = createCommitteeResponse(List.of(
                    createMemberNode("S000033", roleStr)
            ));
            when(congressApiClient.fetchCommitteeByCode("house", "hsju00"))
                    .thenReturn(Optional.of(response));
            when(committeeRepository.findByCommitteeCode("hsju00"))
                    .thenReturn(Optional.of(testCommittee));
            when(personRepository.findByBioguideId("S000033"))
                    .thenReturn(Optional.of(testPerson));
            when(membershipRepository.findByPerson_IdAndCommittee_CommitteeCodeAndCongress(any(), any(), any()))
                    .thenReturn(Optional.empty());
            when(membershipRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            // When
            syncService.syncMembershipsForCommittee("hsju00", "house", 118);

            // Then
            ArgumentCaptor<CommitteeMembership> captor = ArgumentCaptor.forClass(CommitteeMembership.class);
            verify(membershipRepository).save(captor.capture());
            assertThat(captor.getValue().getRole()).isEqualTo(MembershipRole.valueOf(expectedRole));
        }
    }

    // =========================================================================
    // Chamber Mapping Tests
    // =========================================================================

    @Nested
    @DisplayName("Chamber API Value Mapping")
    class ChamberMappingTests {

        @Test
        @DisplayName("Should use 'house' for HOUSE chamber")
        void houseChambersUsesHouseApiValue() {
            // Given
            testCommittee.setChamber(CommitteeChamber.HOUSE);
            when(congressApiClient.isConfigured()).thenReturn(true);
            when(committeeRepository.findAll()).thenReturn(List.of(testCommittee));
            when(congressApiClient.fetchCommitteeByCode(eq("house"), any()))
                    .thenReturn(Optional.empty());

            // When
            syncService.syncAllMemberships(118);

            // Then
            verify(congressApiClient).fetchCommitteeByCode(eq("house"), any());
        }

        @Test
        @DisplayName("Should use 'senate' for SENATE chamber")
        void senateChamberUsesSenateApiValue() {
            // Given
            testCommittee.setChamber(CommitteeChamber.SENATE);
            when(congressApiClient.isConfigured()).thenReturn(true);
            when(committeeRepository.findAll()).thenReturn(List.of(testCommittee));
            when(congressApiClient.fetchCommitteeByCode(eq("senate"), any()))
                    .thenReturn(Optional.empty());

            // When
            syncService.syncAllMemberships(118);

            // Then
            verify(congressApiClient).fetchCommitteeByCode(eq("senate"), any());
        }

        @Test
        @DisplayName("Should use 'joint' for JOINT chamber")
        void jointChamberUsesJointApiValue() {
            // Given
            testCommittee.setChamber(CommitteeChamber.JOINT);
            when(congressApiClient.isConfigured()).thenReturn(true);
            when(committeeRepository.findAll()).thenReturn(List.of(testCommittee));
            when(congressApiClient.fetchCommitteeByCode(eq("joint"), any()))
                    .thenReturn(Optional.empty());

            // When
            syncService.syncAllMemberships(118);

            // Then
            verify(congressApiClient).fetchCommitteeByCode(eq("joint"), any());
        }
    }

    // =========================================================================
    // Count Methods Tests
    // =========================================================================

    @Nested
    @DisplayName("Count Methods")
    class CountMethodsTests {

        @Test
        @DisplayName("getMembershipCount - Should return repository count")
        void getMembershipCount_returnsCount() {
            // Given
            when(membershipRepository.count()).thenReturn(500L);

            // When
            long count = syncService.getMembershipCount();

            // Then
            assertThat(count).isEqualTo(500L);
        }

        @Test
        @DisplayName("getMembershipCountByCongress - Should return count for congress")
        void getMembershipCountByCongress_returnsCount() {
            // Given
            when(membershipRepository.countByCongress(118)).thenReturn(450L);

            // When
            long count = syncService.getMembershipCountByCongress(118);

            // Then
            assertThat(count).isEqualTo(450L);
            verify(membershipRepository).countByCongress(118);
        }
    }

    // =========================================================================
    // Date Parsing Tests
    // =========================================================================

    @Nested
    @DisplayName("Date Parsing")
    class DateParsingTests {

        @Test
        @DisplayName("Should parse valid start and end dates")
        void validDates_parseSuccessfully() {
            // Given
            ObjectNode memberNode = realMapper.createObjectNode();
            memberNode.put("bioguideId", "S000033");
            memberNode.put("role", "Member");
            memberNode.put("startDate", "2023-01-03");
            memberNode.put("endDate", "2025-01-03");

            ObjectNode response = createCommitteeResponse(List.of(memberNode));
            when(congressApiClient.fetchCommitteeByCode("house", "hsju00"))
                    .thenReturn(Optional.of(response));
            when(committeeRepository.findByCommitteeCode("hsju00"))
                    .thenReturn(Optional.of(testCommittee));
            when(personRepository.findByBioguideId("S000033"))
                    .thenReturn(Optional.of(testPerson));
            when(membershipRepository.findByPerson_IdAndCommittee_CommitteeCodeAndCongress(any(), any(), any()))
                    .thenReturn(Optional.empty());
            when(membershipRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            // When
            syncService.syncMembershipsForCommittee("hsju00", "house", 118);

            // Then
            ArgumentCaptor<CommitteeMembership> captor = ArgumentCaptor.forClass(CommitteeMembership.class);
            verify(membershipRepository).save(captor.capture());
            assertThat(captor.getValue().getStartDate()).isNotNull();
            assertThat(captor.getValue().getEndDate()).isNotNull();
        }

        @Test
        @DisplayName("Should handle invalid date formats gracefully")
        void invalidDates_handledGracefully() {
            // Given
            ObjectNode memberNode = realMapper.createObjectNode();
            memberNode.put("bioguideId", "S000033");
            memberNode.put("role", "Member");
            memberNode.put("startDate", "invalid-date");

            ObjectNode response = createCommitteeResponse(List.of(memberNode));
            when(congressApiClient.fetchCommitteeByCode("house", "hsju00"))
                    .thenReturn(Optional.of(response));
            when(committeeRepository.findByCommitteeCode("hsju00"))
                    .thenReturn(Optional.of(testCommittee));
            when(personRepository.findByBioguideId("S000033"))
                    .thenReturn(Optional.of(testPerson));
            when(membershipRepository.findByPerson_IdAndCommittee_CommitteeCodeAndCongress(any(), any(), any()))
                    .thenReturn(Optional.empty());
            when(membershipRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            // When
            CommitteeMembershipSyncService.SyncResult result =
                    syncService.syncMembershipsForCommittee("hsju00", "house", 118);

            // Then - should still add successfully, just without the date
            assertThat(result.getAdded()).isEqualTo(1);
        }
    }

    // =========================================================================
    // Helper Methods
    // =========================================================================

    private ObjectNode createCommitteeResponse(List<ObjectNode> members) {
        ObjectNode response = realMapper.createObjectNode();
        ObjectNode committee = realMapper.createObjectNode();
        committee.put("systemCode", "hsju00");
        committee.put("name", "Committee on the Judiciary");

        ArrayNode membersArray = realMapper.createArrayNode();
        for (ObjectNode member : members) {
            membersArray.add(member);
        }
        committee.set("members", membersArray);
        response.set("committee", committee);
        return response;
    }

    private ObjectNode createMemberNode(String bioguideId, String role) {
        ObjectNode node = realMapper.createObjectNode();
        node.put("bioguideId", bioguideId);
        node.put("role", role);
        return node;
    }

    private Person createPerson(String bioguideId) {
        Person person = new Person();
        person.setId(UUID.randomUUID());
        person.setBioguideId(bioguideId);
        return person;
    }
}
