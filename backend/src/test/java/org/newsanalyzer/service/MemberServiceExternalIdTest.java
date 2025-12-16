package org.newsanalyzer.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.newsanalyzer.model.Person;
import org.newsanalyzer.repository.PersonRepository;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Unit tests for MemberService external ID lookup functionality.
 *
 * @author James (Dev Agent)
 */
@ExtendWith(MockitoExtension.class)
class MemberServiceExternalIdTest {

    @Mock
    private PersonRepository personRepository;

    private MemberService memberService;

    @BeforeEach
    void setUp() {
        memberService = new MemberService(personRepository);
    }

    @Test
    @DisplayName("Should find member by FEC ID")
    void findByExternalId_fecType_callsCorrectRepositoryMethod() {
        // Given
        Person person = new Person();
        person.setBioguideId("S000033");
        when(personRepository.findByFecId("S4VT00033")).thenReturn(Optional.of(person));

        // When
        Optional<Person> result = memberService.findByExternalId("fec", "S4VT00033");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getBioguideId()).isEqualTo("S000033");
        verify(personRepository).findByFecId("S4VT00033");
    }

    @Test
    @DisplayName("Should find member by GovTrack ID")
    void findByExternalId_govtrackType_callsCorrectRepositoryMethod() {
        // Given
        Person person = new Person();
        person.setBioguideId("S000033");
        when(personRepository.findByGovtrackId(400357)).thenReturn(Optional.of(person));

        // When
        Optional<Person> result = memberService.findByExternalId("govtrack", "400357");

        // Then
        assertThat(result).isPresent();
        verify(personRepository).findByGovtrackId(400357);
    }

    @Test
    @DisplayName("Should find member by OpenSecrets ID")
    void findByExternalId_opensecretsType_callsCorrectRepositoryMethod() {
        // Given
        Person person = new Person();
        person.setBioguideId("S000033");
        when(personRepository.findByOpensecretsId("N00000528")).thenReturn(Optional.of(person));

        // When
        Optional<Person> result = memberService.findByExternalId("opensecrets", "N00000528");

        // Then
        assertThat(result).isPresent();
        verify(personRepository).findByOpensecretsId("N00000528");
    }

    @Test
    @DisplayName("Should find member by VoteSmart ID")
    void findByExternalId_votesmartType_callsCorrectRepositoryMethod() {
        // Given
        Person person = new Person();
        person.setBioguideId("S000033");
        when(personRepository.findByVotesmartId(27110)).thenReturn(Optional.of(person));

        // When
        Optional<Person> result = memberService.findByExternalId("votesmart", "27110");

        // Then
        assertThat(result).isPresent();
        verify(personRepository).findByVotesmartId(27110);
    }

    @Test
    @DisplayName("Should handle case-insensitive type")
    void findByExternalId_upperCaseType_handlesCorrectly() {
        // Given
        Person person = new Person();
        when(personRepository.findByFecId("S4VT00033")).thenReturn(Optional.of(person));

        // When
        Optional<Person> result = memberService.findByExternalId("FEC", "S4VT00033");

        // Then
        assertThat(result).isPresent();
        verify(personRepository).findByFecId("S4VT00033");
    }

    @Test
    @DisplayName("Should return empty for invalid GovTrack ID (non-integer)")
    void findByExternalId_invalidGovtrackId_returnsEmpty() {
        // When
        Optional<Person> result = memberService.findByExternalId("govtrack", "not-a-number");

        // Then
        assertThat(result).isEmpty();
        verify(personRepository, never()).findByGovtrackId(any());
    }

    @Test
    @DisplayName("Should return empty for invalid VoteSmart ID (non-integer)")
    void findByExternalId_invalidVotesmartId_returnsEmpty() {
        // When
        Optional<Person> result = memberService.findByExternalId("votesmart", "not-a-number");

        // Then
        assertThat(result).isEmpty();
        verify(personRepository, never()).findByVotesmartId(any());
    }

    @Test
    @DisplayName("Should return empty for unsupported type")
    void findByExternalId_unsupportedType_returnsEmpty() {
        // When
        Optional<Person> result = memberService.findByExternalId("unknown", "12345");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should return empty for null type")
    void findByExternalId_nullType_returnsEmpty() {
        // When
        Optional<Person> result = memberService.findByExternalId(null, "12345");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should return empty for null ID")
    void findByExternalId_nullId_returnsEmpty() {
        // When
        Optional<Person> result = memberService.findByExternalId("fec", null);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should return empty when member not found")
    void findByExternalId_memberNotFound_returnsEmpty() {
        // Given
        when(personRepository.findByFecId("UNKNOWN")).thenReturn(Optional.empty());

        // When
        Optional<Person> result = memberService.findByExternalId("fec", "UNKNOWN");

        // Then
        assertThat(result).isEmpty();
    }
}
