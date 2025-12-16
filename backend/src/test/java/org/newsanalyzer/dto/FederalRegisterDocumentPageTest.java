package org.newsanalyzer.dto;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for FederalRegisterDocumentPage.
 *
 * @author James (Dev Agent)
 * @since 2.0.0
 */
class FederalRegisterDocumentPageTest {

    @Test
    void testEmptyPage() {
        FederalRegisterDocumentPage page = new FederalRegisterDocumentPage();

        assertTrue(page.isEmpty());
        assertFalse(page.hasNextPage());
    }

    @Test
    void testPageWithResults() {
        FederalRegisterDocumentPage page = new FederalRegisterDocumentPage();
        page.setResults(Arrays.asList(new FederalRegisterDocument(), new FederalRegisterDocument()));
        page.setCount(100);
        page.setTotalPages(5);

        assertFalse(page.isEmpty());
        assertEquals(2, page.getResults().size());
        assertEquals(100, page.getCount());
        assertEquals(5, page.getTotalPages());
    }

    @Test
    void testHasNextPage() {
        FederalRegisterDocumentPage page = new FederalRegisterDocumentPage();
        page.setNextPageUrl("https://api.federalregister.gov/v1/documents?page=2");

        assertTrue(page.hasNextPage());
    }

    @Test
    void testNoNextPage() {
        FederalRegisterDocumentPage page = new FederalRegisterDocumentPage();
        page.setNextPageUrl(null);

        assertFalse(page.hasNextPage());
    }

    @Test
    void testEmptyNextPageUrl() {
        FederalRegisterDocumentPage page = new FederalRegisterDocumentPage();
        page.setNextPageUrl("");

        assertFalse(page.hasNextPage());
    }

    @Test
    void testIsEmptyWithNullResults() {
        FederalRegisterDocumentPage page = new FederalRegisterDocumentPage();
        page.setResults(null);

        assertTrue(page.isEmpty());
    }

    @Test
    void testIsEmptyWithEmptyResults() {
        FederalRegisterDocumentPage page = new FederalRegisterDocumentPage();
        page.setResults(Collections.emptyList());

        assertTrue(page.isEmpty());
    }
}
