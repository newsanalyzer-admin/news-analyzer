package org.newsanalyzer.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.newsanalyzer.config.FederalRegisterConfig;
import org.newsanalyzer.dto.DocumentQueryParams;
import org.newsanalyzer.dto.FederalRegisterDocument;
import org.newsanalyzer.dto.FederalRegisterDocumentPage;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for FederalRegisterClient document fetching methods.
 * Tests parsing and mapping of API responses.
 *
 * @author James (Dev Agent)
 * @since 2.0.0
 */
@ExtendWith(MockitoExtension.class)
class FederalRegisterClientDocumentTest {

    private ObjectMapper objectMapper;
    private FederalRegisterConfig config;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        config = new FederalRegisterConfig();
        config.setBaseUrl("https://www.federalregister.gov/api/v1");
        config.setRetryAttempts(1);
        config.setRateLimitMs(0);
    }

    @Test
    void testParseDocumentPageResponse() throws Exception {
        // Given - sample API response
        String jsonResponse = """
            {
                "count": 2,
                "total_pages": 1,
                "next_page_url": null,
                "results": [
                    {
                        "document_number": "2024-12345",
                        "title": "Air Quality Standards",
                        "abstract": "The EPA is revising standards...",
                        "type": "Rule",
                        "publication_date": "2024-03-15",
                        "effective_on": "2024-05-15",
                        "html_url": "https://federalregister.gov/d/2024-12345",
                        "pdf_url": "https://federalregister.gov/pdf/2024-12345.pdf",
                        "agencies": [
                            {"id": 145, "name": "Environmental Protection Agency", "short_name": "EPA"}
                        ],
                        "cfr_references": [
                            {"title": 40, "part": 60}
                        ],
                        "docket_ids": ["EPA-HQ-OAR-2023-0001"]
                    },
                    {
                        "document_number": "2024-12346",
                        "title": "Proposed Emissions Rule",
                        "type": "Proposed Rule",
                        "publication_date": "2024-03-16"
                    }
                ]
            }
            """;

        // When
        FederalRegisterDocumentPage page = objectMapper.readValue(jsonResponse, FederalRegisterDocumentPage.class);

        // Then
        assertEquals(2, page.getCount());
        assertEquals(1, page.getTotalPages());
        assertFalse(page.hasNextPage());
        assertNotNull(page.getResults());
        assertEquals(2, page.getResults().size());

        FederalRegisterDocument firstDoc = page.getResults().get(0);
        assertEquals("2024-12345", firstDoc.getDocumentNumber());
        assertEquals("Air Quality Standards", firstDoc.getTitle());
        assertEquals("The EPA is revising standards...", firstDoc.getDocumentAbstract());
        assertEquals("Rule", firstDoc.getType());
        assertEquals(LocalDate.of(2024, 3, 15), firstDoc.getPublicationDate());
        assertEquals(LocalDate.of(2024, 5, 15), firstDoc.getEffectiveOn());
        assertNotNull(firstDoc.getAgencies());
        assertEquals(1, firstDoc.getAgencies().size());
        assertEquals("Environmental Protection Agency", firstDoc.getAgencies().get(0).getName());
        assertNotNull(firstDoc.getCfrReferences());
        assertEquals(1, firstDoc.getCfrReferences().size());
        assertEquals(40, firstDoc.getCfrReferences().get(0).getTitle());
        assertEquals(60, firstDoc.getCfrReferences().get(0).getPart());
    }

    @Test
    void testParseSingleDocumentResponse() throws Exception {
        // Given
        String jsonResponse = """
            {
                "document_number": "2024-99999",
                "title": "Presidential Proclamation",
                "type": "Presidential Document",
                "publication_date": "2024-07-04",
                "signing_date": "2024-07-03",
                "regulation_id_number": "1234-AB56",
                "html_url": "https://federalregister.gov/d/2024-99999"
            }
            """;

        // When
        FederalRegisterDocument doc = objectMapper.readValue(jsonResponse, FederalRegisterDocument.class);

        // Then
        assertEquals("2024-99999", doc.getDocumentNumber());
        assertEquals("Presidential Proclamation", doc.getTitle());
        assertEquals("Presidential Document", doc.getType());
        assertEquals(LocalDate.of(2024, 7, 4), doc.getPublicationDate());
        assertEquals(LocalDate.of(2024, 7, 3), doc.getSigningDate());
        assertEquals("1234-AB56", doc.getRegulationIdNumber());
    }

    @Test
    void testParseResponseWithMissingFields() throws Exception {
        // Given - minimal response
        String jsonResponse = """
            {
                "document_number": "2024-00001",
                "title": "Minimal Document",
                "type": "Notice",
                "publication_date": "2024-01-01"
            }
            """;

        // When
        FederalRegisterDocument doc = objectMapper.readValue(jsonResponse, FederalRegisterDocument.class);

        // Then
        assertEquals("2024-00001", doc.getDocumentNumber());
        assertEquals("Minimal Document", doc.getTitle());
        assertNull(doc.getDocumentAbstract());
        assertNull(doc.getEffectiveOn());
        assertNull(doc.getSigningDate());
        assertNull(doc.getAgencies());
        assertNull(doc.getCfrReferences());
        assertNull(doc.getDocketIds());
    }

    @Test
    void testParseEmptyPage() throws Exception {
        // Given
        String jsonResponse = """
            {
                "count": 0,
                "total_pages": 0,
                "results": []
            }
            """;

        // When
        FederalRegisterDocumentPage page = objectMapper.readValue(jsonResponse, FederalRegisterDocumentPage.class);

        // Then
        assertEquals(0, page.getCount());
        assertTrue(page.isEmpty());
    }

    @Test
    void testDocumentQueryParamsBuildsCorrectUrl() {
        // Given
        DocumentQueryParams params = DocumentQueryParams.builder()
                .publicationDateGte(LocalDate.of(2024, 1, 1))
                .documentTypes(Arrays.asList("Rule", "Notice"))
                .perPage(50)
                .page(2)
                .build();

        // When
        String url = params.buildUrl("https://api.test.com/v1");

        // Then
        assertTrue(url.contains("conditions[publication_date][gte]=2024-01-01"));
        assertTrue(url.contains("conditions[type][]=Rule"));
        assertTrue(url.contains("conditions[type][]=Notice"));
        assertTrue(url.contains("per_page=50"));
        assertTrue(url.contains("page=2"));
    }
}
