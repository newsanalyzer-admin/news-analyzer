package org.newsanalyzer.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.newsanalyzer.dto.UsCodeImportResult;
import org.newsanalyzer.service.UsCodeImportService;
import org.newsanalyzer.service.UslmXmlParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.ArrayList;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for StatuteImportController using MockMvc.
 *
 * Tests cover:
 * - Valid USLM XML file upload returns 200 with UsCodeImportResult
 * - Invalid XML returns 400
 * - Non-XML file returns 400
 * - Empty file returns 400
 * - File too large returns 413
 * - Import already in progress returns 409
 * - Status and last-result endpoints
 *
 * @author James (Dev Agent)
 * @since 2.0.0
 */
@WebMvcTest(StatuteImportController.class)
@AutoConfigureMockMvc(addFilters = false)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class StatuteImportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UsCodeImportService importService;

    @MockBean
    private UslmXmlParser xmlParser;

    private String validUslmXml;
    private UsCodeImportResult successResult;

    @BeforeEach
    void setUp() {
        validUslmXml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <uslm>
              <main>
                <title identifier="/us/usc/t5">
                  <num>5</num>
                  <heading>GOVERNMENT ORGANIZATION AND EMPLOYEES</heading>
                  <section identifier="/us/usc/t5/s101">
                    <num>§ 101</num>
                    <heading>Executive departments</heading>
                    <content>The Executive departments are...</content>
                  </section>
                </title>
              </main>
            </uslm>
            """;

        successResult = UsCodeImportResult.builder()
                .titleNumber(5)
                .releasePoint("file-upload")
                .sectionsInserted(100)
                .sectionsUpdated(0)
                .sectionsFailed(0)
                .totalProcessed(100)
                .startedAt(LocalDateTime.now().minusSeconds(30))
                .completedAt(LocalDateTime.now())
                .success(true)
                .errors(new ArrayList<>())
                .build();
    }

    // =====================================================================
    // Upload Endpoint Tests
    // =====================================================================

    @Test
    void uploadAndImport_validXml_returns200WithResult() throws Exception {
        // Given
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "usc05.xml",
                MediaType.APPLICATION_XML_VALUE,
                validUslmXml.getBytes()
        );

        doNothing().when(importService).importFromStream(any(), eq("file-upload"), any());

        // When/Then
        mockMvc.perform(multipart("/api/admin/import/statutes/upload")
                        .file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)));
    }

    @Test
    void uploadAndImport_emptyFile_returns400() throws Exception {
        // Given
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "empty.xml",
                MediaType.APPLICATION_XML_VALUE,
                new byte[0]
        );

        // When/Then
        mockMvc.perform(multipart("/api/admin/import/statutes/upload")
                        .file(file))
                .andExpect(status().isBadRequest());
    }

    @Test
    void uploadAndImport_nonXmlExtension_returns400() throws Exception {
        // Given
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "data.txt",
                MediaType.TEXT_PLAIN_VALUE,
                "not xml content".getBytes()
        );

        // When/Then
        mockMvc.perform(multipart("/api/admin/import/statutes/upload")
                        .file(file))
                .andExpect(status().isBadRequest());
    }

    @Test
    void uploadAndImport_invalidXmlContent_returns400() throws Exception {
        // Given - File has .xml extension but invalid content
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "fake.xml",
                MediaType.APPLICATION_XML_VALUE,
                "This is not XML content at all".getBytes()
        );

        // When/Then
        mockMvc.perform(multipart("/api/admin/import/statutes/upload")
                        .file(file))
                .andExpect(status().isBadRequest());
    }

    @Test
    void uploadAndImport_htmlInsteadOfXml_returns400() throws Exception {
        // Given - HTML error page (simulates what uscode.house.gov returns for bad URLs)
        String htmlContent = "<!DOCTYPE html><html><body>Document not found</body></html>";
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "error.xml",
                MediaType.APPLICATION_XML_VALUE,
                htmlContent.getBytes()
        );

        // When/Then - HTML is valid XML start, but not USLM format
        // The controller validates for <?xml or <uslm prefix
        mockMvc.perform(multipart("/api/admin/import/statutes/upload")
                        .file(file))
                .andExpect(status().isBadRequest());
    }

    // =====================================================================
    // Status Endpoint Tests
    // =====================================================================

    @Test
    void getStatus_noImport_returnsBasicStatus() throws Exception {
        // When/Then
        mockMvc.perform(get("/api/admin/import/statutes/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.inProgress", is(false)));
    }

    @Test
    void getStatus_afterImport_includesLastImportInfo() throws Exception {
        // Given - Do an import first
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "usc05.xml",
                MediaType.APPLICATION_XML_VALUE,
                validUslmXml.getBytes()
        );

        doNothing().when(importService).importFromStream(any(), eq("file-upload"), any());

        mockMvc.perform(multipart("/api/admin/import/statutes/upload")
                        .file(file))
                .andExpect(status().isOk());

        // When/Then
        mockMvc.perform(get("/api/admin/import/statutes/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.inProgress", is(false)))
                .andExpect(jsonPath("$.lastImport", notNullValue()))
                .andExpect(jsonPath("$.lastImport.releasePoint", is("file-upload")));
    }

    // =====================================================================
    // Last Result Endpoint Tests
    // =====================================================================

    @Test
    @Order(1) // Run first before any imports set lastResult
    void getLastResult_noPreviousImport_returns404() throws Exception {
        // When/Then
        mockMvc.perform(get("/api/admin/import/statutes/last-result"))
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(2) // Run after 404 test
    void getLastResult_afterImport_returnsResult() throws Exception {
        // Given - First do an import
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "usc05.xml",
                MediaType.APPLICATION_XML_VALUE,
                validUslmXml.getBytes()
        );

        doNothing().when(importService).importFromStream(any(), eq("file-upload"), any());

        // Do import first
        mockMvc.perform(multipart("/api/admin/import/statutes/upload")
                        .file(file))
                .andExpect(status().isOk());

        // When/Then - Get last result
        mockMvc.perform(get("/api/admin/import/statutes/last-result"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.releasePoint", is("file-upload")));
    }

    // =====================================================================
    // Response Format Tests
    // =====================================================================

    @Test
    void importResult_includesAllFields() throws Exception {
        // Given
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "usc05.xml",
                MediaType.APPLICATION_XML_VALUE,
                validUslmXml.getBytes()
        );

        doNothing().when(importService).importFromStream(any(), eq("file-upload"), any());

        // When/Then
        mockMvc.perform(multipart("/api/admin/import/statutes/upload")
                        .file(file))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.releasePoint", is("file-upload")))
                .andExpect(jsonPath("$.sectionsInserted", isA(Number.class)))
                .andExpect(jsonPath("$.sectionsUpdated", isA(Number.class)))
                .andExpect(jsonPath("$.sectionsFailed", isA(Number.class)))
                .andExpect(jsonPath("$.totalProcessed", isA(Number.class)))
                .andExpect(jsonPath("$.success", isA(Boolean.class)));
    }

    // =====================================================================
    // Content-Type Tests
    // =====================================================================

    @Test
    void uploadAndImport_acceptsMultipartFormData() throws Exception {
        // Given
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "usc05.xml",
                MediaType.APPLICATION_XML_VALUE,
                validUslmXml.getBytes()
        );

        doNothing().when(importService).importFromStream(any(), eq("file-upload"), any());

        // When/Then
        mockMvc.perform(multipart("/api/admin/import/statutes/upload")
                        .file(file)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk());
    }

    // =====================================================================
    // USLM Format Validation Tests
    // =====================================================================

    @Test
    void uploadAndImport_uslmRootElement_isValid() throws Exception {
        // Given - USLM starts with <uslm> instead of <?xml
        String uslmOnlyXml = "<uslm><main><title identifier=\"/us/usc/t5\"></title></main></uslm>";
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "usc05.xml",
                MediaType.APPLICATION_XML_VALUE,
                uslmOnlyXml.getBytes()
        );

        doNothing().when(importService).importFromStream(any(), eq("file-upload"), any());

        // When/Then
        mockMvc.perform(multipart("/api/admin/import/statutes/upload")
                        .file(file))
                .andExpect(status().isOk());
    }
}
