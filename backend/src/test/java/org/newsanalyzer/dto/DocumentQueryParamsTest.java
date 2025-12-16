package org.newsanalyzer.dto;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for DocumentQueryParams.
 *
 * @author James (Dev Agent)
 * @since 2.0.0
 */
class DocumentQueryParamsTest {

    @Test
    void testDefaultValues() {
        DocumentQueryParams params = DocumentQueryParams.builder().build();

        assertEquals(100, params.getPerPage());
        assertEquals(1, params.getPage());
        assertNull(params.getPublicationDateGte());
        assertNull(params.getPublicationDateLte());
        assertNull(params.getDocumentTypes());
        assertNull(params.getAgencyIds());
    }

    @Test
    void testBuildUrlWithMinimalParams() {
        DocumentQueryParams params = DocumentQueryParams.builder().build();
        String url = params.buildUrl("https://api.test.com/v1");

        assertTrue(url.startsWith("https://api.test.com/v1/documents?"));
        assertTrue(url.contains("per_page=100"));
        assertTrue(url.contains("page=1"));
        assertTrue(url.contains("fields[]=document_number"));
    }

    @Test
    void testBuildUrlWithDateRange() {
        DocumentQueryParams params = DocumentQueryParams.builder()
                .publicationDateGte(LocalDate.of(2024, 1, 1))
                .publicationDateLte(LocalDate.of(2024, 12, 31))
                .build();

        String url = params.buildUrl("https://api.test.com/v1");

        assertTrue(url.contains("conditions[publication_date][gte]=2024-01-01"));
        assertTrue(url.contains("conditions[publication_date][lte]=2024-12-31"));
    }

    @Test
    void testBuildUrlWithDocumentTypes() {
        DocumentQueryParams params = DocumentQueryParams.builder()
                .documentTypes(Arrays.asList("Rule", "Proposed Rule"))
                .build();

        String url = params.buildUrl("https://api.test.com/v1");

        assertTrue(url.contains("conditions[type][]=Rule"));
        assertTrue(url.contains("conditions[type][]=Proposed Rule"));
    }

    @Test
    void testBuildUrlWithAgencyIds() {
        DocumentQueryParams params = DocumentQueryParams.builder()
                .agencyIds(Arrays.asList(145, 267))
                .build();

        String url = params.buildUrl("https://api.test.com/v1");

        assertTrue(url.contains("conditions[agencies][]=145"));
        assertTrue(url.contains("conditions[agencies][]=267"));
    }

    @Test
    void testBuildUrlWithPagination() {
        DocumentQueryParams params = DocumentQueryParams.builder()
                .perPage(50)
                .page(3)
                .build();

        String url = params.buildUrl("https://api.test.com/v1");

        assertTrue(url.contains("per_page=50"));
        assertTrue(url.contains("page=3"));
    }

    @Test
    void testBuildUrlWithAllParams() {
        DocumentQueryParams params = DocumentQueryParams.builder()
                .publicationDateGte(LocalDate.of(2024, 6, 1))
                .documentTypes(Arrays.asList("Rule"))
                .agencyIds(Arrays.asList(145))
                .perPage(25)
                .page(2)
                .build();

        String url = params.buildUrl("https://api.test.com/v1");

        assertTrue(url.contains("conditions[publication_date][gte]=2024-06-01"));
        assertTrue(url.contains("conditions[type][]=Rule"));
        assertTrue(url.contains("conditions[agencies][]=145"));
        assertTrue(url.contains("per_page=25"));
        assertTrue(url.contains("page=2"));
    }
}
