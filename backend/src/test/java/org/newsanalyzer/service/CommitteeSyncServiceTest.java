package org.newsanalyzer.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.newsanalyzer.model.Committee;
import org.newsanalyzer.model.CommitteeChamber;
import org.newsanalyzer.model.CommitteeType;
import org.newsanalyzer.repository.CommitteeRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for CommitteeSyncService.
 *
 * @author James (Dev Agent)
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CommitteeSyncServiceTest {

    @Mock
    private CongressApiClient congressApiClient;

    @Mock
    private CommitteeRepository committeeRepository;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private CommitteeSyncService committeeSyncService;

    private ObjectMapper realMapper;
    private JsonNode sampleCommitteeData;

    @BeforeEach
    void setUp() {
        realMapper = new ObjectMapper();

        // Create sample committee data
        ObjectNode node = realMapper.createObjectNode();
        node.put("systemCode", "hsju00");
        node.put("name", "Committee on the Judiciary");
        node.put("committeeTypeCode", "Standing");
        sampleCommitteeData = node;
    }

    // =========================================================================
    // syncAllCommittees Tests
    // =========================================================================

    @Test
    @DisplayName("syncAllCommittees - Should return empty result when API not configured")
    void syncAllCommittees_apiNotConfigured_returnsEmptyResult() {
        // Given
        when(congressApiClient.isConfigured()).thenReturn(false);

        // When
        CommitteeSyncService.SyncResult result = committeeSyncService.syncAllCommittees();

        // Then
        assertThat(result.getTotal()).isEqualTo(0);
        assertThat(result.getAdded()).isEqualTo(0);
        assertThat(result.getUpdated()).isEqualTo(0);
        verify(congressApiClient, never()).fetchAllCommitteesByChamber(any());
    }

    @Test
    @DisplayName("syncAllCommittees - Should sync committees from all chambers")
    void syncAllCommittees_configured_syncAllChambers() {
        // Given
        when(congressApiClient.isConfigured()).thenReturn(true);
        when(congressApiClient.fetchAllCommitteesByChamber("house")).thenReturn(List.of(sampleCommitteeData));
        when(congressApiClient.fetchAllCommitteesByChamber("senate")).thenReturn(List.of());
        when(congressApiClient.fetchAllCommitteesByChamber("joint")).thenReturn(List.of());
        when(committeeRepository.findByCommitteeCode(any())).thenReturn(Optional.empty());
        when(committeeRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        CommitteeSyncService.SyncResult result = committeeSyncService.syncAllCommittees();

        // Then
        assertThat(result.getTotal()).isEqualTo(1);
        assertThat(result.getAdded()).isEqualTo(1);
        verify(congressApiClient).fetchAllCommitteesByChamber("house");
        verify(congressApiClient).fetchAllCommitteesByChamber("senate");
        verify(congressApiClient).fetchAllCommitteesByChamber("joint");
    }

    // =========================================================================
    // syncCommitteesByChamber Tests
    // =========================================================================

    @Test
    @DisplayName("syncCommitteesByChamber - Should sync committees and return stats")
    void syncCommitteesByChamber_success_returnsStats() {
        // Given
        when(congressApiClient.isConfigured()).thenReturn(true);
        when(congressApiClient.fetchAllCommitteesByChamber("house")).thenReturn(List.of(sampleCommitteeData));
        when(committeeRepository.findByCommitteeCode("hsju00")).thenReturn(Optional.empty());
        when(committeeRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        CommitteeSyncService.SyncResult result = committeeSyncService.syncCommitteesByChamber("house", CommitteeChamber.HOUSE);

        // Then
        assertThat(result.getAdded()).isEqualTo(1);
        assertThat(result.getUpdated()).isEqualTo(0);
    }

    @Test
    @DisplayName("syncCommitteesByChamber - Should update existing committees")
    void syncCommitteesByChamber_existingCommittee_updatesRecord() {
        // Given
        when(congressApiClient.isConfigured()).thenReturn(true);
        when(congressApiClient.fetchAllCommitteesByChamber("house")).thenReturn(List.of(sampleCommitteeData));

        Committee existingCommittee = new Committee();
        existingCommittee.setCommitteeCode("hsju00");
        existingCommittee.setName("Old Name");
        existingCommittee.setChamber(CommitteeChamber.HOUSE);
        existingCommittee.setCommitteeType(CommitteeType.STANDING);

        when(committeeRepository.findByCommitteeCode("hsju00")).thenReturn(Optional.of(existingCommittee));
        when(committeeRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        CommitteeSyncService.SyncResult result = committeeSyncService.syncCommitteesByChamber("house", CommitteeChamber.HOUSE);

        // Then
        assertThat(result.getAdded()).isEqualTo(0);
        assertThat(result.getUpdated()).isEqualTo(1);

        ArgumentCaptor<Committee> captor = ArgumentCaptor.forClass(Committee.class);
        verify(committeeRepository).save(captor.capture());
        assertThat(captor.getValue().getName()).isEqualTo("Committee on the Judiciary");
    }

    @Test
    @DisplayName("syncCommitteesByChamber - Should handle subcommittees")
    void syncCommitteesByChamber_withSubcommittee_linksToParent() {
        // Given
        when(congressApiClient.isConfigured()).thenReturn(true);

        // Parent committee
        ObjectNode parentNode = realMapper.createObjectNode();
        parentNode.put("systemCode", "hsju00");
        parentNode.put("name", "Committee on the Judiciary");
        parentNode.put("committeeTypeCode", "Standing");

        // Subcommittee
        ObjectNode subNode = realMapper.createObjectNode();
        subNode.put("systemCode", "hsju01");
        subNode.put("name", "Subcommittee on Immigration");
        subNode.put("committeeTypeCode", "Subcommittee");
        ObjectNode parentRef = realMapper.createObjectNode();
        parentRef.put("systemCode", "hsju00");
        subNode.set("parent", parentRef);

        when(congressApiClient.fetchAllCommitteesByChamber("house")).thenReturn(List.of(parentNode, subNode));

        Committee parentCommittee = new Committee();
        parentCommittee.setCommitteeCode("hsju00");
        parentCommittee.setName("Committee on the Judiciary");
        parentCommittee.setChamber(CommitteeChamber.HOUSE);
        parentCommittee.setCommitteeType(CommitteeType.STANDING);

        when(committeeRepository.findByCommitteeCode("hsju00")).thenReturn(Optional.of(parentCommittee));
        when(committeeRepository.findByCommitteeCode("hsju01")).thenReturn(Optional.empty());
        when(committeeRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        CommitteeSyncService.SyncResult result = committeeSyncService.syncCommitteesByChamber("house", CommitteeChamber.HOUSE);

        // Then
        assertThat(result.getUpdated()).isEqualTo(1); // Parent updated
        assertThat(result.getSubcommitteesAdded()).isEqualTo(1); // Subcommittee added
    }

    // =========================================================================
    // syncCommittee Tests
    // =========================================================================

    @Test
    @DisplayName("syncCommittee - Should throw exception when missing systemCode")
    void syncCommittee_missingSystemCode_throwsException() {
        // Given
        ObjectNode node = realMapper.createObjectNode();
        node.put("name", "Test Committee");

        // When/Then
        assertThatThrownBy(() -> committeeSyncService.syncCommittee(node, CommitteeChamber.HOUSE, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("systemCode");
    }

    @Test
    @DisplayName("syncCommittee - Should return true for new committee")
    void syncCommittee_newCommittee_returnsTrue() {
        // Given
        when(committeeRepository.findByCommitteeCode("hsju00")).thenReturn(Optional.empty());
        when(committeeRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        boolean isNew = committeeSyncService.syncCommittee(sampleCommitteeData, CommitteeChamber.HOUSE, null);

        // Then
        assertThat(isNew).isTrue();
        verify(committeeRepository).save(any(Committee.class));
    }

    @Test
    @DisplayName("syncCommittee - Should return false for existing committee")
    void syncCommittee_existingCommittee_returnsFalse() {
        // Given
        Committee existingCommittee = new Committee();
        existingCommittee.setCommitteeCode("hsju00");
        when(committeeRepository.findByCommitteeCode("hsju00")).thenReturn(Optional.of(existingCommittee));
        when(committeeRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        boolean isNew = committeeSyncService.syncCommittee(sampleCommitteeData, CommitteeChamber.HOUSE, null);

        // Then
        assertThat(isNew).isFalse();
    }

    @Test
    @DisplayName("syncCommittee - Should map committee type correctly")
    void syncCommittee_mapsCommitteeType_correctly() {
        // Given
        when(committeeRepository.findByCommitteeCode("hsju00")).thenReturn(Optional.empty());
        when(committeeRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        committeeSyncService.syncCommittee(sampleCommitteeData, CommitteeChamber.HOUSE, null);

        // Then
        ArgumentCaptor<Committee> captor = ArgumentCaptor.forClass(Committee.class);
        verify(committeeRepository).save(captor.capture());
        assertThat(captor.getValue().getCommitteeType()).isEqualTo(CommitteeType.STANDING);
    }

    // =========================================================================
    // syncCommitteeByCode Tests
    // =========================================================================

    @Test
    @DisplayName("syncCommitteeByCode - Should return empty when API not configured")
    void syncCommitteeByCode_apiNotConfigured_returnsEmpty() {
        // Given
        when(congressApiClient.isConfigured()).thenReturn(false);

        // When
        Optional<Committee> result = committeeSyncService.syncCommitteeByCode("house", "hsju00");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("syncCommitteeByCode - Should sync and return committee")
    void syncCommitteeByCode_success_returnsCommittee() {
        // Given
        when(congressApiClient.isConfigured()).thenReturn(true);

        ObjectNode response = realMapper.createObjectNode();
        response.set("committee", sampleCommitteeData);
        when(congressApiClient.fetchCommitteeByCode("house", "hsju00")).thenReturn(Optional.of(response));

        Committee savedCommittee = new Committee();
        savedCommittee.setCommitteeCode("hsju00");
        savedCommittee.setName("Committee on the Judiciary");
        when(committeeRepository.findByCommitteeCode("hsju00"))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(savedCommittee));
        when(committeeRepository.save(any())).thenReturn(savedCommittee);

        // When
        Optional<Committee> result = committeeSyncService.syncCommitteeByCode("house", "hsju00");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getCommitteeCode()).isEqualTo("hsju00");
    }

    // =========================================================================
    // getCommitteeCount and getSubcommitteeCount Tests
    // =========================================================================

    @Test
    @DisplayName("getCommitteeCount - Should return repository count")
    void getCommitteeCount_returnsCount() {
        // Given
        when(committeeRepository.count()).thenReturn(250L);

        // When
        long count = committeeSyncService.getCommitteeCount();

        // Then
        assertThat(count).isEqualTo(250L);
    }

    @Test
    @DisplayName("getSubcommitteeCount - Should return subcommittee count")
    void getSubcommitteeCount_returnsCount() {
        // Given
        when(committeeRepository.countSubcommittees()).thenReturn(200L);

        // When
        long count = committeeSyncService.getSubcommitteeCount();

        // Then
        assertThat(count).isEqualTo(200L);
    }
}
