package org.newsanalyzer.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.newsanalyzer.model.DataSource;
import org.newsanalyzer.model.Person;
import org.newsanalyzer.model.Person.Chamber;
import org.newsanalyzer.repository.PersonRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for MemberSyncService.
 *
 * @author James (Dev Agent)
 */
@ExtendWith(MockitoExtension.class)
class MemberSyncServiceTest {

    @Mock
    private CongressApiClient congressApiClient;

    @Mock
    private PersonRepository personRepository;

    private ObjectMapper objectMapper;
    private MemberSyncService syncService;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        syncService = new MemberSyncService(congressApiClient, personRepository, objectMapper);
    }

    @Test
    @DisplayName("Should return empty result when API not configured")
    void syncAllCurrentMembers_apiNotConfigured_returnsEmptyResult() {
        // Given
        when(congressApiClient.isConfigured()).thenReturn(false);

        // When
        MemberSyncService.SyncResult result = syncService.syncAllCurrentMembers();

        // Then
        assertThat(result.getTotal()).isEqualTo(0);
        assertThat(result.getAdded()).isEqualTo(0);
        assertThat(result.getUpdated()).isEqualTo(0);
        verify(congressApiClient, never()).fetchAllCurrentMembers();
    }

    @Test
    @DisplayName("Should sync new member correctly")
    void syncMember_newMember_createsRecord() throws Exception {
        // Given
        String memberJson = """
            {
              "bioguideId": "S000033",
              "firstName": "Bernard",
              "lastName": "Sanders",
              "party": "Independent",
              "state": "VT",
              "terms": [{"chamber": "Senate"}]
            }
            """;
        JsonNode memberData = objectMapper.readTree(memberJson);

        when(personRepository.findByBioguideId("S000033")).thenReturn(Optional.empty());
        when(personRepository.save(any(Person.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        boolean isNew = syncService.syncMember(memberData);

        // Then
        assertThat(isNew).isTrue();

        ArgumentCaptor<Person> personCaptor = ArgumentCaptor.forClass(Person.class);
        verify(personRepository).save(personCaptor.capture());

        Person savedPerson = personCaptor.getValue();
        assertThat(savedPerson.getBioguideId()).isEqualTo("S000033");
        assertThat(savedPerson.getFirstName()).isEqualTo("Bernard");
        assertThat(savedPerson.getLastName()).isEqualTo("Sanders");
        assertThat(savedPerson.getParty()).isEqualTo("Independent");
        assertThat(savedPerson.getState()).isEqualTo("VT");
        assertThat(savedPerson.getChamber()).isEqualTo(Chamber.SENATE);
        assertThat(savedPerson.getDataSource()).isEqualTo(DataSource.CONGRESS_GOV);
    }

    @Test
    @DisplayName("Should update existing member")
    void syncMember_existingMember_updatesRecord() throws Exception {
        // Given
        String memberJson = """
            {
              "bioguideId": "S000033",
              "firstName": "Bernard",
              "lastName": "Sanders",
              "party": "Independent",
              "state": "VT"
            }
            """;
        JsonNode memberData = objectMapper.readTree(memberJson);

        Person existingPerson = new Person();
        existingPerson.setBioguideId("S000033");
        existingPerson.setFirstName("Bernie");

        when(personRepository.findByBioguideId("S000033")).thenReturn(Optional.of(existingPerson));
        when(personRepository.save(any(Person.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        boolean isNew = syncService.syncMember(memberData);

        // Then
        assertThat(isNew).isFalse();
        verify(personRepository).save(existingPerson);
        assertThat(existingPerson.getFirstName()).isEqualTo("Bernard");
    }

    @Test
    @DisplayName("Should throw exception for missing bioguideId")
    void syncMember_missingBioguideId_throwsException() throws Exception {
        // Given
        String memberJson = """
            {
              "firstName": "John",
              "lastName": "Doe"
            }
            """;
        JsonNode memberData = objectMapper.readTree(memberJson);

        // When/Then
        assertThatThrownBy(() -> syncService.syncMember(memberData))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("bioguideId");
    }

    @Test
    @DisplayName("Should map HOUSE chamber correctly")
    void syncMember_houseMember_mapsChamberCorrectly() throws Exception {
        // Given
        String memberJson = """
            {
              "bioguideId": "P000197",
              "firstName": "Nancy",
              "lastName": "Pelosi",
              "terms": [{"chamber": "House of Representatives"}]
            }
            """;
        JsonNode memberData = objectMapper.readTree(memberJson);

        when(personRepository.findByBioguideId("P000197")).thenReturn(Optional.empty());
        when(personRepository.save(any(Person.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        syncService.syncMember(memberData);

        // Then
        ArgumentCaptor<Person> personCaptor = ArgumentCaptor.forClass(Person.class);
        verify(personRepository).save(personCaptor.capture());
        assertThat(personCaptor.getValue().getChamber()).isEqualTo(Chamber.HOUSE);
    }

    @Test
    @DisplayName("Should parse birth year correctly")
    void syncMember_withBirthYear_parsesBirthDate() throws Exception {
        // Given
        String memberJson = """
            {
              "bioguideId": "S000033",
              "firstName": "Bernard",
              "lastName": "Sanders",
              "birthYear": "1941"
            }
            """;
        JsonNode memberData = objectMapper.readTree(memberJson);

        when(personRepository.findByBioguideId("S000033")).thenReturn(Optional.empty());
        when(personRepository.save(any(Person.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        syncService.syncMember(memberData);

        // Then
        ArgumentCaptor<Person> personCaptor = ArgumentCaptor.forClass(Person.class);
        verify(personRepository).save(personCaptor.capture());
        assertThat(personCaptor.getValue().getBirthDate().getYear()).isEqualTo(1941);
    }

    @Test
    @DisplayName("Should extract image URL from depiction")
    void syncMember_withDepiction_extractsImageUrl() throws Exception {
        // Given
        String memberJson = """
            {
              "bioguideId": "S000033",
              "firstName": "Bernard",
              "lastName": "Sanders",
              "depiction": {
                "imageUrl": "https://example.com/sanders.jpg"
              }
            }
            """;
        JsonNode memberData = objectMapper.readTree(memberJson);

        when(personRepository.findByBioguideId("S000033")).thenReturn(Optional.empty());
        when(personRepository.save(any(Person.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        syncService.syncMember(memberData);

        // Then
        ArgumentCaptor<Person> personCaptor = ArgumentCaptor.forClass(Person.class);
        verify(personRepository).save(personCaptor.capture());
        assertThat(personCaptor.getValue().getImageUrl()).isEqualTo("https://example.com/sanders.jpg");
    }

    @Test
    @DisplayName("Should return member count")
    void getMemberCount_returnsTotalCount() {
        // Given
        when(personRepository.count()).thenReturn(535L);

        // When
        long count = syncService.getMemberCount();

        // Then
        assertThat(count).isEqualTo(535L);
    }
}
