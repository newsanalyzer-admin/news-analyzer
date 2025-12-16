package org.newsanalyzer.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.newsanalyzer.model.*;
import org.newsanalyzer.model.Person.Chamber;
import org.newsanalyzer.repository.GovernmentPositionRepository;
import org.newsanalyzer.repository.PersonRepository;
import org.newsanalyzer.repository.PositionHoldingRepository;

import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for TermSyncService.
 *
 * @author James (Dev Agent)
 */
@ExtendWith(MockitoExtension.class)
class TermSyncServiceTest {

    @Mock
    private CongressApiClient congressApiClient;

    @Mock
    private PersonRepository personRepository;

    @Mock
    private GovernmentPositionRepository positionRepository;

    @Mock
    private PositionHoldingRepository holdingRepository;

    @Mock
    private PositionInitializationService positionInitService;

    @InjectMocks
    private TermSyncService termSyncService;

    private ObjectMapper objectMapper;
    private Person testPerson;
    private GovernmentPosition testPosition;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();

        testPerson = new Person();
        testPerson.setId(UUID.randomUUID());
        testPerson.setBioguideId("S000033");
        testPerson.setFirstName("Bernard");
        testPerson.setLastName("Sanders");
        testPerson.setState("VT");
        testPerson.setChamber(Chamber.SENATE);

        testPosition = new GovernmentPosition();
        testPosition.setId(UUID.randomUUID());
        testPosition.setTitle("Senator");
        testPosition.setChamber(Chamber.SENATE);
        testPosition.setState("VT");
        testPosition.setSenateClass(1);
        testPosition.setPositionType(PositionType.ELECTED);
    }

    @Test
    @DisplayName("syncTermsForMember - Should create new term for valid member")
    void syncTermsForMember_validMember_createsTerms() throws Exception {
        // Given
        String apiResponse = """
            {
              "member": {
                "bioguideId": "S000033",
                "terms": {
                  "item": [
                    {
                      "chamber": "Senate",
                      "congress": 118,
                      "startYear": 2023,
                      "endYear": 2025,
                      "stateCode": "VT"
                    }
                  ]
                }
              }
            }
            """;
        JsonNode responseNode = objectMapper.readTree(apiResponse);

        when(personRepository.findByBioguideId("S000033")).thenReturn(Optional.of(testPerson));
        when(congressApiClient.fetchMemberByBioguideId("S000033")).thenReturn(Optional.of(responseNode));
        when(positionRepository.findByChamberAndState(Chamber.SENATE, "VT"))
                .thenReturn(List.of(testPosition));
        when(holdingRepository.findByPersonIdAndPositionIdAndCongress(any(), any(), eq(118)))
                .thenReturn(Optional.empty());
        when(holdingRepository.save(any(PositionHolding.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        TermSyncService.TermResult result = termSyncService.syncTermsForMember("S000033");

        // Then
        assertThat(result.getAdded()).isEqualTo(1);
        assertThat(result.getUpdated()).isEqualTo(0);
        verify(holdingRepository, times(1)).save(any(PositionHolding.class));
    }

    @Test
    @DisplayName("syncTermsForMember - Should skip terms before 1990")
    void syncTermsForMember_oldTerms_skipped() throws Exception {
        // Given
        String apiResponse = """
            {
              "member": {
                "bioguideId": "S000033",
                "terms": {
                  "item": [
                    {
                      "chamber": "House of Representatives",
                      "congress": 102,
                      "startYear": 1991,
                      "endYear": 1993,
                      "stateCode": "VT",
                      "district": 0
                    },
                    {
                      "chamber": "House of Representatives",
                      "congress": 100,
                      "startYear": 1987,
                      "endYear": 1989,
                      "stateCode": "VT",
                      "district": 0
                    }
                  ]
                }
              }
            }
            """;
        JsonNode responseNode = objectMapper.readTree(apiResponse);

        GovernmentPosition housePosition = new GovernmentPosition();
        housePosition.setId(UUID.randomUUID());
        housePosition.setChamber(Chamber.HOUSE);
        housePosition.setState("VT");
        housePosition.setDistrict(0);

        when(personRepository.findByBioguideId("S000033")).thenReturn(Optional.of(testPerson));
        when(congressApiClient.fetchMemberByBioguideId("S000033")).thenReturn(Optional.of(responseNode));
        when(positionRepository.findByChamberAndStateAndDistrict(Chamber.HOUSE, "VT", 0))
                .thenReturn(Optional.of(housePosition));
        when(holdingRepository.findByPersonIdAndPositionIdAndCongress(any(), any(), eq(102)))
                .thenReturn(Optional.empty());
        when(holdingRepository.save(any(PositionHolding.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        TermSyncService.TermResult result = termSyncService.syncTermsForMember("S000033");

        // Then - only 1991 term should be processed, 1987 term should be skipped
        assertThat(result.getAdded()).isEqualTo(1);
        verify(holdingRepository, times(1)).save(any(PositionHolding.class));
    }

    @Test
    @DisplayName("syncTermsForMember - Should update existing term when end date changes")
    void syncTermsForMember_existingTerm_updates() throws Exception {
        // Given
        String apiResponse = """
            {
              "member": {
                "bioguideId": "S000033",
                "terms": {
                  "item": [
                    {
                      "chamber": "Senate",
                      "congress": 118,
                      "startYear": 2023,
                      "endYear": 2025,
                      "stateCode": "VT"
                    }
                  ]
                }
              }
            }
            """;
        JsonNode responseNode = objectMapper.readTree(apiResponse);

        PositionHolding existingHolding = PositionHolding.builder()
                .id(UUID.randomUUID())
                .personId(testPerson.getId())
                .positionId(testPosition.getId())
                .startDate(LocalDate.of(2023, 1, 3))
                .endDate(null)  // Was current, now has end date
                .congress(118)
                .dataSource(DataSource.CONGRESS_GOV)
                .build();

        when(personRepository.findByBioguideId("S000033")).thenReturn(Optional.of(testPerson));
        when(congressApiClient.fetchMemberByBioguideId("S000033")).thenReturn(Optional.of(responseNode));
        when(positionRepository.findByChamberAndState(Chamber.SENATE, "VT"))
                .thenReturn(List.of(testPosition));
        when(holdingRepository.findByPersonIdAndPositionIdAndCongress(any(), any(), eq(118)))
                .thenReturn(Optional.of(existingHolding));
        when(holdingRepository.save(any(PositionHolding.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        TermSyncService.TermResult result = termSyncService.syncTermsForMember("S000033");

        // Then
        assertThat(result.getAdded()).isEqualTo(0);
        assertThat(result.getUpdated()).isEqualTo(1);
        verify(holdingRepository, times(1)).save(any(PositionHolding.class));
    }

    @Test
    @DisplayName("syncTermsForMember - Should return empty when person not found")
    void syncTermsForMember_personNotFound_returnsEmpty() {
        // Given
        when(personRepository.findByBioguideId("INVALID")).thenReturn(Optional.empty());

        // When
        TermSyncService.TermResult result = termSyncService.syncTermsForMember("INVALID");

        // Then
        assertThat(result.getAdded()).isEqualTo(0);
        assertThat(result.getUpdated()).isEqualTo(0);
        verify(congressApiClient, never()).fetchMemberByBioguideId(anyString());
    }

    @Test
    @DisplayName("syncTermsForMember - Should return empty when API returns no data")
    void syncTermsForMember_apiEmpty_returnsEmpty() {
        // Given
        when(personRepository.findByBioguideId("S000033")).thenReturn(Optional.of(testPerson));
        when(congressApiClient.fetchMemberByBioguideId("S000033")).thenReturn(Optional.empty());

        // When
        TermSyncService.TermResult result = termSyncService.syncTermsForMember("S000033");

        // Then
        assertThat(result.getAdded()).isEqualTo(0);
        assertThat(result.getUpdated()).isEqualTo(0);
    }

    @Test
    @DisplayName("syncAllCurrentMemberTerms - Should process all members")
    void syncAllCurrentMemberTerms_processesAllMembers() throws Exception {
        // Given
        Person member2 = new Person();
        member2.setId(UUID.randomUUID());
        member2.setBioguideId("P000197");
        member2.setFirstName("Nancy");
        member2.setLastName("Pelosi");
        member2.setState("CA");
        member2.setChamber(Chamber.HOUSE);

        when(personRepository.findAll()).thenReturn(List.of(testPerson, member2));
        when(personRepository.findByBioguideId(anyString())).thenReturn(Optional.empty());

        // When
        TermSyncService.SyncResult result = termSyncService.syncAllCurrentMemberTerms();

        // Then
        assertThat(result.getMembersProcessed()).isEqualTo(2);
        verify(positionInitService, times(1)).initializeAllPositions();
    }

    @Test
    @DisplayName("syncTermsForMember - Should handle current term with null end date")
    void syncTermsForMember_currentTerm_handlesNullEndDate() throws Exception {
        // Given
        String apiResponse = """
            {
              "member": {
                "bioguideId": "S000033",
                "terms": {
                  "item": [
                    {
                      "chamber": "Senate",
                      "congress": 119,
                      "startYear": 2025,
                      "endYear": null,
                      "stateCode": "VT"
                    }
                  ]
                }
              }
            }
            """;
        JsonNode responseNode = objectMapper.readTree(apiResponse);

        when(personRepository.findByBioguideId("S000033")).thenReturn(Optional.of(testPerson));
        when(congressApiClient.fetchMemberByBioguideId("S000033")).thenReturn(Optional.of(responseNode));
        when(positionRepository.findByChamberAndState(Chamber.SENATE, "VT"))
                .thenReturn(List.of(testPosition));
        when(holdingRepository.findByPersonIdAndPositionIdAndCongress(any(), any(), eq(119)))
                .thenReturn(Optional.empty());
        when(holdingRepository.save(any(PositionHolding.class)))
                .thenAnswer(invocation -> {
                    PositionHolding holding = invocation.getArgument(0);
                    assertThat(holding.getEndDate()).isNull();
                    assertThat(holding.isCurrent()).isTrue();
                    return holding;
                });

        // When
        TermSyncService.TermResult result = termSyncService.syncTermsForMember("S000033");

        // Then
        assertThat(result.getAdded()).isEqualTo(1);
        verify(holdingRepository, times(1)).save(any(PositionHolding.class));
    }
}
