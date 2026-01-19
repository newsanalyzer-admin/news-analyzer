package org.newsanalyzer.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.newsanalyzer.model.CongressionalMember;
import org.newsanalyzer.repository.CongressionalMemberRepository;
import org.newsanalyzer.repository.IndividualRepository;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Unit tests for MemberService external ID lookup functionality.
 *
 * Part of ARCH-1.7: Updated to use CongressionalMemberRepository instead of PersonRepository.
 *
 * @author James (Dev Agent)
 * @since 3.0.0
 */
@ExtendWith(MockitoExtension.class)
class MemberServiceExternalIdTest {

    @Mock
    private CongressionalMemberRepository congressionalMemberRepository;

    @Mock
    private IndividualRepository individualRepository;

    private MemberService memberService;

    @BeforeEach
    void setUp() {
        memberService = new MemberService(congressionalMemberRepository, individualRepository);
    }

    @Test
    @DisplayName("Should find member by FEC ID")
    void findByExternalId_fecType_callsCorrectRepositoryMethod() {
        // Given
        CongressionalMember member = new CongressionalMember();
        member.setId(UUID.randomUUID());
        member.setBioguideId("S000033");
        when(congressionalMemberRepository.findByFecId("S4VT00033")).thenReturn(Optional.of(member));

        // When
        Optional<CongressionalMember> result = memberService.findByExternalId("fec", "S4VT00033");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getBioguideId()).isEqualTo("S000033");
        verify(congressionalMemberRepository).findByFecId("S4VT00033");
    }

    @Test
    @DisplayName("Should find member by GovTrack ID")
    void findByExternalId_govtrackType_callsCorrectRepositoryMethod() {
        // Given
        CongressionalMember member = new CongressionalMember();
        member.setId(UUID.randomUUID());
        member.setBioguideId("S000033");
        when(congressionalMemberRepository.findByGovtrackId(400357)).thenReturn(Optional.of(member));

        // When
        Optional<CongressionalMember> result = memberService.findByExternalId("govtrack", "400357");

        // Then
        assertThat(result).isPresent();
        verify(congressionalMemberRepository).findByGovtrackId(400357);
    }

    @Test
    @DisplayName("Should find member by OpenSecrets ID")
    void findByExternalId_opensecretsType_callsCorrectRepositoryMethod() {
        // Given
        CongressionalMember member = new CongressionalMember();
        member.setId(UUID.randomUUID());
        member.setBioguideId("S000033");
        when(congressionalMemberRepository.findByOpensecretsId("N00000528")).thenReturn(Optional.of(member));

        // When
        Optional<CongressionalMember> result = memberService.findByExternalId("opensecrets", "N00000528");

        // Then
        assertThat(result).isPresent();
        verify(congressionalMemberRepository).findByOpensecretsId("N00000528");
    }

    @Test
    @DisplayName("Should find member by VoteSmart ID")
    void findByExternalId_votesmartType_callsCorrectRepositoryMethod() {
        // Given
        CongressionalMember member = new CongressionalMember();
        member.setId(UUID.randomUUID());
        member.setBioguideId("S000033");
        when(congressionalMemberRepository.findByVotesmartId(27110)).thenReturn(Optional.of(member));

        // When
        Optional<CongressionalMember> result = memberService.findByExternalId("votesmart", "27110");

        // Then
        assertThat(result).isPresent();
        verify(congressionalMemberRepository).findByVotesmartId(27110);
    }

    @Test
    @DisplayName("Should handle case-insensitive type")
    void findByExternalId_upperCaseType_handlesCorrectly() {
        // Given
        CongressionalMember member = new CongressionalMember();
        member.setId(UUID.randomUUID());
        when(congressionalMemberRepository.findByFecId("S4VT00033")).thenReturn(Optional.of(member));

        // When
        Optional<CongressionalMember> result = memberService.findByExternalId("FEC", "S4VT00033");

        // Then
        assertThat(result).isPresent();
        verify(congressionalMemberRepository).findByFecId("S4VT00033");
    }

    @Test
    @DisplayName("Should return empty for invalid GovTrack ID (non-integer)")
    void findByExternalId_invalidGovtrackId_returnsEmpty() {
        // When
        Optional<CongressionalMember> result = memberService.findByExternalId("govtrack", "not-a-number");

        // Then
        assertThat(result).isEmpty();
        verify(congressionalMemberRepository, never()).findByGovtrackId(any());
    }

    @Test
    @DisplayName("Should return empty for invalid VoteSmart ID (non-integer)")
    void findByExternalId_invalidVotesmartId_returnsEmpty() {
        // When
        Optional<CongressionalMember> result = memberService.findByExternalId("votesmart", "not-a-number");

        // Then
        assertThat(result).isEmpty();
        verify(congressionalMemberRepository, never()).findByVotesmartId(any());
    }

    @Test
    @DisplayName("Should return empty for unsupported type")
    void findByExternalId_unsupportedType_returnsEmpty() {
        // When
        Optional<CongressionalMember> result = memberService.findByExternalId("unknown", "12345");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should return empty for null type")
    void findByExternalId_nullType_returnsEmpty() {
        // When
        Optional<CongressionalMember> result = memberService.findByExternalId(null, "12345");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should return empty for null ID")
    void findByExternalId_nullId_returnsEmpty() {
        // When
        Optional<CongressionalMember> result = memberService.findByExternalId("fec", null);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should return empty when member not found")
    void findByExternalId_memberNotFound_returnsEmpty() {
        // Given
        when(congressionalMemberRepository.findByFecId("UNKNOWN")).thenReturn(Optional.empty());

        // When
        Optional<CongressionalMember> result = memberService.findByExternalId("fec", "UNKNOWN");

        // Then
        assertThat(result).isEmpty();
    }
}
