package org.newsanalyzer.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for LinkageStatistics DTO.
 *
 * @author James (Dev Agent)
 * @since 2.0.0
 */
class LinkageStatisticsTest {

    @Test
    void testLinkageRateCalculation() {
        LinkageStatistics stats = LinkageStatistics.builder()
                .totalRegulations(100)
                .linkedRegulations(95)
                .build();

        assertEquals(95.0, stats.getLinkageRate(), 0.01);
    }

    @Test
    void testLinkageRateZeroTotal() {
        LinkageStatistics stats = LinkageStatistics.builder()
                .totalRegulations(0)
                .linkedRegulations(0)
                .build();

        assertEquals(0.0, stats.getLinkageRate(), 0.01);
    }

    @Test
    void testLinkageRatePartial() {
        LinkageStatistics stats = LinkageStatistics.builder()
                .totalRegulations(200)
                .linkedRegulations(150)
                .build();

        assertEquals(75.0, stats.getLinkageRate(), 0.01);
    }

    @Test
    void testMeetsTargetTrue() {
        LinkageStatistics stats = LinkageStatistics.builder()
                .totalRegulations(100)
                .linkedRegulations(95)
                .build();

        assertTrue(stats.meetsTarget(95.0));
        assertTrue(stats.meetsTarget(90.0));
    }

    @Test
    void testMeetsTargetFalse() {
        LinkageStatistics stats = LinkageStatistics.builder()
                .totalRegulations(100)
                .linkedRegulations(90)
                .build();

        assertFalse(stats.meetsTarget(95.0));
    }

    @Test
    void testMeetsTargetExactly() {
        LinkageStatistics stats = LinkageStatistics.builder()
                .totalRegulations(100)
                .linkedRegulations(95)
                .build();

        assertTrue(stats.meetsTarget(95.0));
    }

    @Test
    void testBuilderDefaults() {
        LinkageStatistics stats = LinkageStatistics.builder().build();

        assertEquals(0, stats.getTotalRegulations());
        assertEquals(0, stats.getLinkedRegulations());
        assertEquals(0, stats.getUnmatchedAgencyNames());
        assertEquals(0, stats.getAgenciesLinkedInOperation());
    }

    @Test
    void testFullBuilder() {
        LinkageStatistics stats = LinkageStatistics.builder()
                .totalRegulations(500)
                .linkedRegulations(475)
                .unmatchedAgencyNames(10)
                .agenciesLinkedInOperation(50)
                .build();

        assertEquals(500, stats.getTotalRegulations());
        assertEquals(475, stats.getLinkedRegulations());
        assertEquals(10, stats.getUnmatchedAgencyNames());
        assertEquals(50, stats.getAgenciesLinkedInOperation());
        assertEquals(95.0, stats.getLinkageRate(), 0.01);
    }
}
