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
import org.newsanalyzer.model.CongressionalMember;
import org.newsanalyzer.model.Individual;
import org.newsanalyzer.repository.CongressionalMemberRepository;
import org.newsanalyzer.repository.IndividualRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for LegislatorsEnrichmentService.
 *
 * Updated for ARCH-1.9: Uses CongressionalMember + Individual instead of Person.
 *
 * @author James (Dev Agent)
 */
@ExtendWith(MockitoExtension.class)
class LegislatorsEnrichmentServiceTest {

    @Mock
    private CongressionalMemberRepository congressionalMemberRepository;

    @Mock
    private IndividualRepository individualRepository;

    @Mock
    private LegislatorsRepoClient repoClient;

    private ObjectMapper objectMapper;
    private LegislatorsEnrichmentService enrichmentService;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        enrichmentService = new LegislatorsEnrichmentService(
                congressionalMemberRepository, individualRepository, repoClient, objectMapper);
    }

    private CongressionalMember createTestMemberWithIndividual(String bioguideId) {
        Individual individual = new Individual();
        individual.setId(UUID.randomUUID());
        individual.setFirstName("Bernard");
        individual.setLastName("Sanders");

        CongressionalMember member = new CongressionalMember();
        member.setId(UUID.randomUUID());
        member.setBioguideId(bioguideId);
        member.setIndividualId(individual.getId());
        member.setIndividual(individual);

        return member;
    }

    @Test
    @DisplayName("Should enrich individual with external IDs")
    void enrichCurrentLegislators_matchingMember_enrichesExternalIds() {
        // Given
        LegislatorYamlRecord record = createTestRecord("S000033");
        when(repoClient.fetchCurrentLegislators()).thenReturn(List.of(record));

        CongressionalMember member = createTestMemberWithIndividual("S000033");
        when(congressionalMemberRepository.findByBioguideIdWithIndividual("S000033"))
                .thenReturn(Optional.of(member));
        when(individualRepository.save(any(Individual.class))).thenAnswer(inv -> inv.getArgument(0));
        when(congressionalMemberRepository.save(any(CongressionalMember.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        var result = enrichmentService.enrichCurrentLegislators("commit123");

        // Then
        assertThat(result.matched()).isEqualTo(1);
        assertThat(result.notFound()).isEqualTo(0);

        // Verify enrichment tracking on CongressionalMember
        ArgumentCaptor<CongressionalMember> memberCaptor = ArgumentCaptor.forClass(CongressionalMember.class);
        verify(congressionalMemberRepository).save(memberCaptor.capture());
        assertThat(memberCaptor.getValue().getEnrichmentSource()).isEqualTo("LEGISLATORS_REPO");
        assertThat(memberCaptor.getValue().getEnrichmentVersion()).isEqualTo("commit123");

        // Verify external IDs on Individual
        ArgumentCaptor<Individual> individualCaptor = ArgumentCaptor.forClass(Individual.class);
        verify(individualRepository).save(individualCaptor.capture());
        JsonNode externalIds = individualCaptor.getValue().getExternalIds();
        assertThat(externalIds).isNotNull();
        assertThat(externalIds.get("govtrack").asInt()).isEqualTo(400357);
        assertThat(externalIds.get("opensecrets").asText()).isEqualTo("N00000528");
    }

    @Test
    @DisplayName("Should enrich individual with social media")
    void enrichCurrentLegislators_matchingMember_enrichesSocialMedia() {
        // Given
        LegislatorYamlRecord record = createTestRecord("S000033");
        when(repoClient.fetchCurrentLegislators()).thenReturn(List.of(record));

        CongressionalMember member = createTestMemberWithIndividual("S000033");
        when(congressionalMemberRepository.findByBioguideIdWithIndividual("S000033"))
                .thenReturn(Optional.of(member));
        when(individualRepository.save(any(Individual.class))).thenAnswer(inv -> inv.getArgument(0));
        when(congressionalMemberRepository.save(any(CongressionalMember.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        enrichmentService.enrichCurrentLegislators("commit123");

        // Then
        ArgumentCaptor<Individual> captor = ArgumentCaptor.forClass(Individual.class);
        verify(individualRepository).save(captor.capture());

        JsonNode socialMedia = captor.getValue().getSocialMedia();
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

        CongressionalMember member = createTestMemberWithIndividual("S000033");
        Individual individual = member.getIndividual();
        // Set existing external ID on Individual
        individual.setExternalIds(objectMapper.readTree("{\"govtrack\": 999999}"));

        when(congressionalMemberRepository.findByBioguideIdWithIndividual("S000033"))
                .thenReturn(Optional.of(member));
        when(individualRepository.save(any(Individual.class))).thenAnswer(inv -> inv.getArgument(0));
        when(congressionalMemberRepository.save(any(CongressionalMember.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        enrichmentService.enrichCurrentLegislators("commit123");

        // Then
        ArgumentCaptor<Individual> captor = ArgumentCaptor.forClass(Individual.class);
        verify(individualRepository).save(captor.capture());

        Individual savedIndividual = captor.getValue();
        // Existing govtrack should NOT be overwritten
        assertThat(savedIndividual.getExternalIds().get("govtrack").asInt()).isEqualTo(999999);
        // But new fields should be added
        assertThat(savedIndividual.getExternalIds().get("opensecrets").asText()).isEqualTo("N00000528");
    }

    @Test
    @DisplayName("Should track not found members")
    void enrichCurrentLegislators_memberNotFound_incrementsNotFound() {
        // Given
        LegislatorYamlRecord record = createTestRecord("UNKNOWN");
        when(repoClient.fetchCurrentLegislators()).thenReturn(List.of(record));
        when(congressionalMemberRepository.findByBioguideIdWithIndividual("UNKNOWN")).thenReturn(Optional.empty());

        // When
        var result = enrichmentService.enrichCurrentLegislators("commit123");

        // Then
        assertThat(result.matched()).isEqualTo(0);
        assertThat(result.notFound()).isEqualTo(1);
        verify(individualRepository, never()).save(any());
        verify(congressionalMemberRepository, never()).save(any());
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

        CongressionalMember member = createTestMemberWithIndividual("S000033");
        Individual individual = member.getIndividual();

        when(congressionalMemberRepository.findByBioguideIdWithIndividual("S000033"))
                .thenReturn(Optional.of(member));
        when(individualRepository.save(any(Individual.class))).thenAnswer(inv -> inv.getArgument(0));
        when(congressionalMemberRepository.save(any(CongressionalMember.class))).thenAnswer(inv -> inv.getArgument(0));

        // When - run enrichment twice
        enrichmentService.enrichCurrentLegislators("commit1");

        // After first enrichment, individual has external IDs
        individual.setExternalIds(objectMapper.readTree("{\"govtrack\": 400357, \"opensecrets\": \"N00000528\"}"));
        individual.setSocialMedia(objectMapper.readTree("{\"twitter\": \"SenSanders\", \"facebook\": \"senatorsanders\"}"));

        enrichmentService.enrichCurrentLegislators("commit2");

        // Then - should have been saved twice but with same data (not duplicated)
        verify(individualRepository, times(2)).save(any(Individual.class));

        ArgumentCaptor<Individual> captor = ArgumentCaptor.forClass(Individual.class);
        verify(individualRepository, times(2)).save(captor.capture());

        List<Individual> savedIndividuals = captor.getAllValues();
        // Second save should have same external IDs (not duplicated)
        Individual secondSave = savedIndividuals.get(1);
        assertThat(secondSave.getExternalIds().get("govtrack").asInt()).isEqualTo(400357);
    }

    private LegislatorYamlRecord createTestRecord(String bioguideId) {
        LegislatorYamlRecord record = new LegislatorYamlRecord();

        LegislatorYamlRecord.LegislatorId id = new LegislatorYamlRecord.LegislatorId();
        id.setBioguide(bioguideId);
        id.setGovtrack(400357L);
        id.setOpensecrets("N00000528");
        id.setVotesmart(27110L);
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
