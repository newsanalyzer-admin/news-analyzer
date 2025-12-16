package org.newsanalyzer.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.newsanalyzer.dto.SyncStatistics;
import org.newsanalyzer.service.RegulationSyncService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for RegulationSyncController.
 *
 * @author James (Dev Agent)
 * @since 2.0.0
 */
@ExtendWith(MockitoExtension.class)
class RegulationSyncControllerTest {

    @Mock
    private RegulationSyncService regulationSyncService;

    @InjectMocks
    private RegulationSyncController controller;

    @Test
    void testTriggerSyncSuccess() {
        // Given
        SyncStatistics stats = SyncStatistics.builder()
                .fetched(10)
                .created(5)
                .updated(3)
                .errors(0)
                .status(SyncStatistics.SyncStatus.COMPLETED)
                .startTime(LocalDateTime.now())
                .endTime(LocalDateTime.now())
                .build();

        when(regulationSyncService.isSyncRunning()).thenReturn(false);
        when(regulationSyncService.syncRegulations()).thenReturn(stats);

        // When
        ResponseEntity<SyncStatistics> response = controller.triggerSync();

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(10, response.getBody().getFetched());
        assertEquals(5, response.getBody().getCreated());
        verify(regulationSyncService).syncRegulations();
    }

    @Test
    void testTriggerSyncWhenAlreadyRunning() {
        // Given
        SyncStatistics runningStats = SyncStatistics.builder()
                .status(SyncStatistics.SyncStatus.RUNNING)
                .startTime(LocalDateTime.now())
                .build();

        when(regulationSyncService.isSyncRunning()).thenReturn(true);
        when(regulationSyncService.getSyncStatus()).thenReturn(runningStats);

        // When
        ResponseEntity<SyncStatistics> response = controller.triggerSync();

        // Then
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(SyncStatistics.SyncStatus.RUNNING, response.getBody().getStatus());
        verify(regulationSyncService, never()).syncRegulations();
    }

    @Test
    void testTriggerSyncFailed() {
        // Given
        SyncStatistics failedStats = SyncStatistics.builder()
                .status(SyncStatistics.SyncStatus.FAILED)
                .errorMessage("API unavailable")
                .startTime(LocalDateTime.now())
                .endTime(LocalDateTime.now())
                .build();

        when(regulationSyncService.isSyncRunning()).thenReturn(false);
        when(regulationSyncService.syncRegulations()).thenReturn(failedStats);

        // When
        ResponseEntity<SyncStatistics> response = controller.triggerSync();

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(SyncStatistics.SyncStatus.FAILED, response.getBody().getStatus());
    }

    @Test
    void testGetSyncStatusWithStats() {
        // Given
        SyncStatistics stats = SyncStatistics.builder()
                .fetched(100)
                .status(SyncStatistics.SyncStatus.COMPLETED)
                .build();

        when(regulationSyncService.getSyncStatus()).thenReturn(stats);

        // When
        ResponseEntity<SyncStatistics> response = controller.getSyncStatus();

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(100, response.getBody().getFetched());
    }

    @Test
    void testGetSyncStatusNoSync() {
        // Given
        when(regulationSyncService.getSyncStatus()).thenReturn(null);

        // When
        ResponseEntity<SyncStatistics> response = controller.getSyncStatus();

        // Then
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());
    }
}
