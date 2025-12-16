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
import org.newsanalyzer.dto.LegislatorYamlRecord;
import org.newsanalyzer.model.Person;
import org.newsanalyzer.repository.PersonRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for LegislatorsEnrichmentService.
 *
 * @author James (Dev Agent)
 */
@ExtendWith(MockitoExtension.class)
class LegislatorsEnrichmentServiceTest {

    @Mock
    private PersonRepository personRepository;

    @Mock
    private LegislatorsRepoClient repoClient;

    private ObjectMapper objectMapper;
    private LegislatorsEnrichmentService enrichmentService;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        enrichmentService = new LegislatorsEnrichmentService(personRepository, repoClient, objectMapper);
    }

    @Test
    @DisplayName("Should enrich person with external IDs")
    void enrichCurrentLegislators_matchingPerson_enrichesExternalIds() {
        // Given
        LegislatorYamlRecord record = createTestRecord("S000033");
        when(repoClient.fetchCurrentLegislators()).thenReturn(List.of(record));

        Person person = new Person();
        person.setBioguideId("S000033");
        person.setFirstName("Bernard");
        person.setLastName("Sanders");
        when(personRepository.findByBioguideId("S000033")).thenReturn(Optional.of(person));
        when(personRepository.save(any(Person.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        var result = enrichmentService.enrichCurrentLegislators("commit123");

        // Then
        assertThat(result.matched()).isEqualTo(1);
        assertThat(result.notFound()).isEqualTo(0);

        ArgumentCaptor<Person> captor = ArgumentCaptor.forClass(Person.class);
        verify(personRepository).save(captor.capture());

        Person savedPerson = captor.getValue();
        assertThat(savedPerson.getEnrichmentSource()).isEqualTo("LEGISLATORS_REPO");
        assertThat(savedPerson.getEnrichmentVersion()).isEqualTo("commit123");

        // Check external IDs were merged
        JsonNode externalIds = savedPerson.getExternalIds();
        assertThat(externalIds).isNotNull();
        assertThat(externalIds.get("govtrack").asInt()).isEqualTo(400357);
        assertThat(externalIds.get("opensecrets").asText()).isEqualTo("N00000528");
    }

    @Test
    @DisplayName("Should enrich person with social media")
    void enrichCurrentLegislators_matchingPerson_enrichesSocialMedia() {
        // Given
        LegislatorYamlRecord record = createTestRecord("S000033");
        when(repoClient.fetchCurrentLegislators()).thenReturn(List.of(record));

        Person person = new Person();
        person.setBioguideId("S000033");
        when(personRepository.findByBioguideId("S000033")).thenReturn(Optional.of(person));
        when(personRepository.save(any(Person.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        enrichmentService.enrichCurrentLegislators("commit123");

        // Then
        ArgumentCaptor<Person> captor = ArgumentCaptor.forClass(Person.class);
        verify(personRepository).save(captor.capture());

        Person savedPerson = captor.getValue();
        JsonNode socialMedia = savedPerson.getSocialMedia();
        assertThat(socialMedia).isNotNull();
        assertThat(socialMedia.get("twitter").asText()).isEqualTo("SenSanders");
        assertThat(socialMedia.get("facebook").asText()).isEqualTo("senatorsanders");
    }

    @Test
    @DisplayName("Should not overwrite existing external IDs")
    void enrichCurrentLegislators_existingExternalIds_doesNotOverwrite() throws Exception {
        // Given
        LegislatorYamlRecord record = createTestRecord("S000033");
        when(repoClient.fetchCurrentLegislators()).thenReturn(List.of(record));

        Person person = new Person();
        person.setBioguideId("S000033");
        // Set existing external ID
        person.setExternalIds(objectMapper.readTree("{\"govtrack\": 999999}"));
        when(personRepository.findByBioguideId("S000033")).thenReturn(Optional.of(person));
        when(personRepository.save(any(Person.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        enrichmentService.enrichCurrentLegislators("commit123");

        // Then
        ArgumentCaptor<Person> captor = ArgumentCaptor.forClass(Person.class);
        verify(personRepository).save(captor.capture());

        Person savedPerson = captor.getValue();
        // Existing govtrack should NOT be overwritten
        assertThat(savedPerson.getExternalIds().get("govtrack").asInt()).isEqualTo(999999);
        // But new fields should be added
        assertThat(savedPerson.getExternalIds().get("opensecrets").asText()).isEqualTo("N00000528");
    }

    @Test
    @DisplayName("Should track not found persons")
    void enrichCurrentLegislators_personNotFound_incrementsNotFound() {
        // Given
        LegislatorYamlRecord record = createTestRecord("UNKNOWN");
        when(repoClient.fetchCurrentLegislators()).thenReturn(List.of(record));
        when(personRepository.findByBioguideId("UNKNOWN")).thenReturn(Optional.empty());

        // When
        var result = enrichmentService.enrichCurrentLegislators("commit123");

        // Then
        assertThat(result.matched()).isEqualTo(0);
        assertThat(result.notFound()).isEqualTo(1);
        verify(personRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should skip sync if commit unchanged")
    void runFullSync_commitUnchanged_skipsSyncAndReturnsSuccess() {
        // Given
        when(repoClient.fetchLatestCommitSha()).thenReturn(Optional.of("commit123"));

        // When
        var result = enrichmentService.runFullSync("commit123");

        // Then
        assertThat(result.success()).isTrue();
        assertThat(result.message()).contains("unchanged");
        verify(repoClient, never()).fetchCurrentLegislators();
    }

    @Test
    @DisplayName("Should run sync if commit changed")
    void runFullSync_commitChanged_runsSyncAndReturnsResult() {
        // Given
        when(repoClient.fetchLatestCommitSha()).thenReturn(Optional.of("newcommit"));
        when(repoClient.fetchCurrentLegislators()).thenReturn(List.of());

        // When
        var result = enrichmentService.runFullSync("oldcommit");

        // Then
        assertThat(result.success()).isTrue();
        assertThat(result.commitSha()).isEqualTo("newcommit");
        verify(repoClient).fetchCurrentLegislators();
    }

    @Test
    @DisplayName("Sync should be idempotent - running twice produces same result")
    void enrichCurrentLegislators_runTwice_isIdempotent() throws Exception {
        // Given
        LegislatorYamlRecord record = createTestRecord("S000033");
        when(repoClient.fetchCurrentLegislators()).thenReturn(List.of(record));

        Person person = new Person();
        person.setBioguideId("S000033");
        when(personRepository.findByBioguideId("S000033")).thenReturn(Optional.of(person));
        when(personRepository.save(any(Person.class))).thenAnswer(inv -> inv.getArgument(0));

        // When - run enrichment twice
        enrichmentService.enrichCurrentLegislators("commit1");

        // After first enrichment, person has external IDs
        person.setExternalIds(objectMapper.readTree("{\"govtrack\": 400357, \"opensecrets\": \"N00000528\"}"));
        person.setSocialMedia(objectMapper.readTree("{\"twitter\": \"SenSanders\", \"facebook\": \"senatorsanders\"}"));

        enrichmentService.enrichCurrentLegislators("commit2");

        // Then - should have been saved twice but with same data (not duplicated)
        verify(personRepository, times(2)).save(any(Person.class));

        ArgumentCaptor<Person> captor = ArgumentCaptor.forClass(Person.class);
        verify(personRepository, times(2)).save(captor.capture());

        List<Person> savedPersons = captor.getAllValues();
        // Second save should have same external IDs (not duplicated)
        Person secondSave = savedPersons.get(1);
        assertThat(secondSave.getExternalIds().get("govtrack").asInt()).isEqualTo(400357);
    }

    private LegislatorYamlRecord createTestRecord(String bioguideId) {
        LegislatorYamlRecord record = new LegislatorYamlRecord();

        LegislatorYamlRecord.LegislatorId id = new LegislatorYamlRecord.LegislatorId();
        id.setBioguide(bioguideId);
        id.setGovtrack(400357);
        id.setOpensecrets("N00000528");
        id.setVotesmart(27110);
        id.setFec(List.of("S4VT00033"));
        record.setId(id);

        LegislatorYamlRecord.LegislatorName name = new LegislatorYamlRecord.LegislatorName();
        name.setFirst("Bernard");
        name.setLast("Sanders");
        record.setName(name);

        LegislatorYamlRecord.LegislatorSocial social = new LegislatorYamlRecord.LegislatorSocial();
        social.setTwitter("SenSanders");
        social.setFacebook("senatorsanders");
        record.setSocial(social);

        return record;
    }
}
