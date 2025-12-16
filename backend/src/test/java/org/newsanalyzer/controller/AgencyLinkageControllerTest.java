package org.newsanalyzer.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.newsanalyzer.dto.LinkageStatistics;
import org.newsanalyzer.service.AgencyLinkageService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AgencyLinkageController.
 *
 * @author James (Dev Agent)
 * @since 2.0.0
 */
@ExtendWith(MockitoExtension.class)
class AgencyLinkageControllerTest {

    @Mock
    private AgencyLinkageService agencyLinkageService;

    @InjectMocks
    private AgencyLinkageController controller;

    @Test
    void testGetUnmatchedAgencies() {
        Set<String> unmatched = new TreeSet<>(Set.of("Unknown Agency 1", "Unknown Agency 2"));
        when(agencyLinkageService.getUnmatchedAgencies()).thenReturn(unmatched);

        ResponseEntity<Set<String>> response = controller.getUnmatchedAgencies();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        assertTrue(response.getBody().contains("Unknown Agency 1"));
    }

    @Test
    void testGetUnmatchedAgenciesEmpty() {
        when(agencyLinkageService.getUnmatchedAgencies()).thenReturn(Set.of());

        ResponseEntity<Set<String>> response = controller.getUnmatchedAgencies();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());
    }

    @Test
    void testClearUnmatchedAgencies() {
        ResponseEntity<Void> response = controller.clearUnmatchedAgencies();

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());
        verify(agencyLinkageService).clearUnmatchedAgencies();
    }

    @Test
    void testGetLinkageStatistics() {
        LinkageStatistics stats = LinkageStatistics.builder()
                .totalRegulations(100)
                .linkedRegulations(95)
                .unmatchedAgencyNames(3)
                .build();

        when(agencyLinkageService.getStatistics()).thenReturn(stats);

        ResponseEntity<LinkageStatistics> response = controller.getLinkageStatistics();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(100, response.getBody().getTotalRegulations());
        assertEquals(95, response.getBody().getLinkedRegulations());
        assertEquals(95.0, response.getBody().getLinkageRate(), 0.01);
    }

    @Test
    void testRefreshCaches() {
        Map<String, Integer> cacheSizes = Map.of(
                "nameCache", 150,
                "acronymCache", 100,
                "federalRegisterIdCache", 50
        );

        when(agencyLinkageService.getCacheSizes()).thenReturn(cacheSizes);

        ResponseEntity<Map<String, Integer>> response = controller.refreshCaches();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(150, response.getBody().get("nameCache"));
        verify(agencyLinkageService).refreshCaches();
    }

    @Test
    void testGetCacheStats() {
        Map<String, Integer> cacheSizes = Map.of(
                "nameCache", 150,
                "acronymCache", 100,
                "federalRegisterIdCache", 50
        );

        when(agencyLinkageService.getCacheSizes()).thenReturn(cacheSizes);

        ResponseEntity<Map<String, Integer>> response = controller.getCacheStats();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(3, response.getBody().size());
    }
}
