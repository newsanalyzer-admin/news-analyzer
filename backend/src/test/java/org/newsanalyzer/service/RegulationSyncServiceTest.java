package org.newsanalyzer.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.newsanalyzer.config.FederalRegisterConfig;
import org.newsanalyzer.dto.DocumentQueryParams;
import org.newsanalyzer.dto.FederalRegisterAgency;
import org.newsanalyzer.dto.FederalRegisterDocument;
import org.newsanalyzer.dto.SyncStatistics;
import org.newsanalyzer.model.DocumentType;
import org.newsanalyzer.model.Regulation;
import org.newsanalyzer.repository.RegulationRepository;
import java.util.UUID;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

/**
 * Unit tests for RegulationSyncService.
 *
 * @author James (Dev Agent)
 * @since 2.0.0
 */
@ExtendWith(MockitoExtension.class)
class RegulationSyncServiceTest {

    @Mock
    private FederalRegisterClient federalRegisterClient;

    @Mock
    private RegulationRepository regulationRepository;

    @Mock
    private AgencyLinkageService agencyLinkageService;

    private FederalRegisterConfig config;

    private RegulationSyncService syncService;

    @BeforeEach
    void setUp() {
        config = new FederalRegisterConfig();
        config.setSync(new FederalRegisterConfig.SyncConfig());
        config.getSync().setPageSize(100);
        config.getSync().setMaxPages(10);
        config.getSync().setInitialBackfillDays(30);

        syncService = new RegulationSyncService(
                federalRegisterClient,
                regulationRepository,
                config,
                agencyLinkageService
        );
    }

    @Test
    void testSyncCreatesNewRegulations() {
        // Given
        FederalRegisterDocument doc = createTestDocument("2024-12345", "Test Rule", "Rule");
        List<FederalRegisterDocument> documents = Collections.singletonList(doc);

        when(regulationRepository.findTopByOrderByPublicationDateDesc()).thenReturn(Optional.empty());
        when(federalRegisterClient.fetchAllDocuments(any(DocumentQueryParams.class), anyInt()))
                .thenReturn(documents);
        when(regulationRepository.findByDocumentNumber("2024-12345")).thenReturn(Optional.empty());
        when(regulationRepository.save(any(Regulation.class))).thenAnswer(i -> i.getArgument(0));

        // When
        SyncStatistics stats = syncService.syncRegulations();

        // Then
        assertEquals(SyncStatistics.SyncStatus.COMPLETED, stats.getStatus());
        assertEquals(1, stats.getFetched());
        assertEquals(1, stats.getCreated());
        assertEquals(0, stats.getUpdated());
        assertEquals(0, stats.getErrors());

        verify(regulationRepository, times(1)).save(any(Regulation.class));
    }

    @Test
    void testSyncUpdatesExistingRegulations() {
        // Given
        FederalRegisterDocument doc = createTestDocument("2024-12345", "Updated Title", "Rule");
        List<FederalRegisterDocument> documents = Collections.singletonList(doc);

        Regulation existingRegulation = Regulation.builder()
                .documentNumber("2024-12345")
                .title("Original Title")
                .documentType(DocumentType.RULE)
                .publicationDate(LocalDate.of(2024, 3, 15))
                .build();

        when(regulationRepository.findTopByOrderByPublicationDateDesc()).thenReturn(Optional.empty());
        when(federalRegisterClient.fetchAllDocuments(any(DocumentQueryParams.class), anyInt()))
                .thenReturn(documents);
        when(regulationRepository.findByDocumentNumber("2024-12345"))
                .thenReturn(Optional.of(existingRegulation));
        when(regulationRepository.save(any(Regulation.class))).thenAnswer(i -> i.getArgument(0));

        // When
        SyncStatistics stats = syncService.syncRegulations();

        // Then
        assertEquals(SyncStatistics.SyncStatus.COMPLETED, stats.getStatus());
        assertEquals(1, stats.getFetched());
        assertEquals(0, stats.getCreated());
        assertEquals(1, stats.getUpdated());
        assertEquals(0, stats.getErrors());

        verify(regulationRepository, times(1)).save(any(Regulation.class));
    }

    @Test
    void testSyncSkipsUnchangedRegulations() {
        // Given
        FederalRegisterDocument doc = createTestDocument("2024-12345", "Same Title", "Rule");
        List<FederalRegisterDocument> documents = Collections.singletonList(doc);

        Regulation existingRegulation = Regulation.builder()
                .documentNumber("2024-12345")
                .title("Same Title")
                .documentAbstract(null)
                .effectiveOn(null)
                .documentType(DocumentType.RULE)
                .publicationDate(LocalDate.of(2024, 3, 15))
                .build();

        when(regulationRepository.findTopByOrderByPublicationDateDesc()).thenReturn(Optional.empty());
        when(federalRegisterClient.fetchAllDocuments(any(DocumentQueryParams.class), anyInt()))
                .thenReturn(documents);
        when(regulationRepository.findByDocumentNumber("2024-12345"))
                .thenReturn(Optional.of(existingRegulation));

        // When
        SyncStatistics stats = syncService.syncRegulations();

        // Then
        assertEquals(SyncStatistics.SyncStatus.COMPLETED, stats.getStatus());
        assertEquals(1, stats.getFetched());
        assertEquals(0, stats.getCreated());
        assertEquals(0, stats.getUpdated());
        assertEquals(1, stats.getSkipped());

        verify(regulationRepository, never()).save(any(Regulation.class));
    }

    @Test
    void testSyncHandlesEmptyResponse() {
        // Given
        when(regulationRepository.findTopByOrderByPublicationDateDesc()).thenReturn(Optional.empty());
        when(federalRegisterClient.fetchAllDocuments(any(DocumentQueryParams.class), anyInt()))
                .thenReturn(Collections.emptyList());

        // When
        SyncStatistics stats = syncService.syncRegulations();

        // Then
        assertEquals(SyncStatistics.SyncStatus.COMPLETED, stats.getStatus());
        assertEquals(0, stats.getFetched());
        assertEquals(0, stats.getCreated());
        assertEquals(0, stats.getUpdated());
    }

    @Test
    void testSyncHandlesDocumentWithNullNumber() {
        // Given
        FederalRegisterDocument doc = createTestDocument(null, "Test Rule", "Rule");
        List<FederalRegisterDocument> documents = Collections.singletonList(doc);

        when(regulationRepository.findTopByOrderByPublicationDateDesc()).thenReturn(Optional.empty());
        when(federalRegisterClient.fetchAllDocuments(any(DocumentQueryParams.class), anyInt()))
                .thenReturn(documents);

        // When
        SyncStatistics stats = syncService.syncRegulations();

        // Then
        assertEquals(SyncStatistics.SyncStatus.COMPLETED, stats.getStatus());
        assertEquals(1, stats.getFetched());
        assertEquals(1, stats.getSkipped());
        assertEquals(0, stats.getErrors());

        verify(regulationRepository, never()).save(any(Regulation.class));
    }

    @Test
    void testSyncStartsFromLastKnownDate() {
        // Given
        Regulation lastRegulation = Regulation.builder()
                .documentNumber("2024-00001")
                .title("Last Known")
                .documentType(DocumentType.RULE)
                .publicationDate(LocalDate.of(2024, 6, 15))
                .build();

        when(regulationRepository.findTopByOrderByPublicationDateDesc())
                .thenReturn(Optional.of(lastRegulation));
        when(federalRegisterClient.fetchAllDocuments(any(DocumentQueryParams.class), anyInt()))
                .thenReturn(Collections.emptyList());

        // When
        syncService.syncRegulations();

        // Then - verify the query starts from the last known date
        verify(federalRegisterClient).fetchAllDocuments(
                argThat(params -> params.getPublicationDateGte().equals(LocalDate.of(2024, 6, 15))),
                anyInt()
        );
    }

    @Test
    void testSyncMultipleDocuments() {
        // Given
        List<FederalRegisterDocument> documents = Arrays.asList(
                createTestDocument("2024-00001", "Rule 1", "Rule"),
                createTestDocument("2024-00002", "Rule 2", "Proposed Rule"),
                createTestDocument("2024-00003", "Notice 1", "Notice")
        );

        when(regulationRepository.findTopByOrderByPublicationDateDesc()).thenReturn(Optional.empty());
        when(federalRegisterClient.fetchAllDocuments(any(DocumentQueryParams.class), anyInt()))
                .thenReturn(documents);
        when(regulationRepository.findByDocumentNumber(anyString())).thenReturn(Optional.empty());
        when(regulationRepository.save(any(Regulation.class))).thenAnswer(i -> i.getArgument(0));

        // When
        SyncStatistics stats = syncService.syncRegulations();

        // Then
        assertEquals(SyncStatistics.SyncStatus.COMPLETED, stats.getStatus());
        assertEquals(3, stats.getFetched());
        assertEquals(3, stats.getCreated());

        verify(regulationRepository, times(3)).save(any(Regulation.class));
    }

    @Test
    void testGetSyncStatusWhenNoSyncPerformed() {
        SyncStatistics stats = syncService.getSyncStatus();
        assertNull(stats);
    }

    @Test
    void testIsSyncRunningReturnsFalseWhenNoSync() {
        assertFalse(syncService.isSyncRunning());
    }

    private FederalRegisterDocument createTestDocument(String docNumber, String title, String type) {
        FederalRegisterDocument doc = new FederalRegisterDocument();
        doc.setDocumentNumber(docNumber);
        doc.setTitle(title);
        doc.setType(type);
        doc.setPublicationDate(LocalDate.of(2024, 3, 15));
        return doc;
    }
}
