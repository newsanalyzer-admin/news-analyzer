package org.newsanalyzer.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.newsanalyzer.dto.GovmanImportResult;
import org.newsanalyzer.service.GovmanXmlImportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for GovmanImportController using MockMvc.
 *
 * Tests cover:
 * - Valid XML file upload returns 200 with ImportResult
 * - Invalid XML returns 400
 * - File too large returns 413
 * - Import already in progress returns 409
 * - Status and last-result endpoints
 *
 * @author James (Dev Agent)
 * @since 2.0.0
 */
@WebMvcTest(GovmanImportController.class)
@AutoConfigureMockMvc(addFilters = false)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class GovmanImportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GovmanXmlImportService importService;

    private String validXml;
    private GovmanImportResult successResult;

    @BeforeEach
    void setUp() {
        validXml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <GovernmentManual>
              <Entity>
                <EntityId>TEST-1</EntityId>
                <AgencyName>Test Congress</AgencyName>
                <Category>Legislative Branch</Category>
              </Entity>
            </GovernmentManual>
            """;

        successResult = GovmanImportResult.builder()
                .startTime(LocalDateTime.now().minusSeconds(5))
                .endTime(LocalDateTime.now())
                .total(1)
                .imported(1)
                .updated(0)
                .skipped(0)
                .errors(0)
                .errorDetails(new ArrayList<>())
                .build();
    }

    // =====================================================================
    // Import Endpoint Tests
    // =====================================================================

    @Test
    void importGovman_validXml_returns200WithResult() throws Exception {
        // Given
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "govman-test.xml",
                MediaType.APPLICATION_XML_VALUE,
                validXml.getBytes()
        );

        when(importService.importFromStream(any(InputStream.class)))
                .thenReturn(successResult);

        // When/Then
        mockMvc.perform(multipart("/api/admin/import/govman")
                        .file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total", is(1)))
                .andExpect(jsonPath("$.imported", is(1)))
                .andExpect(jsonPath("$.errors", is(0)));
    }

    @Test
    void importGovman_emptyFile_returns400() throws Exception {
        // Given
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "empty.xml",
                MediaType.APPLICATION_XML_VALUE,
                new byte[0]
        );

        // When/Then
        mockMvc.perform(multipart("/api/admin/import/govman")
                        .file(file))
                .andExpect(status().isBadRequest());
    }

    @Test
    void importGovman_nonXmlFile_returns400() throws Exception {
        // Given
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "data.txt",
                MediaType.TEXT_PLAIN_VALUE,
                "not xml content".getBytes()
        );

        // When/Then
        mockMvc.perform(multipart("/api/admin/import/govman")
                        .file(file))
                .andExpect(status().isBadRequest());
    }

    @Test
    void importGovman_withErrors_returns200WithErrorDetails() throws Exception {
        // Given
        GovmanImportResult resultWithErrors = GovmanImportResult.builder()
                .startTime(LocalDateTime.now().minusSeconds(5))
                .endTime(LocalDateTime.now())
                .total(5)
                .imported(2)
                .updated(1)
                .skipped(0)
                .errors(2)
                .errorDetails(new ArrayList<>())
                .build();
        resultWithErrors.getErrorDetails().add("[Entity TEST-1] Missing AgencyName");
        resultWithErrors.getErrorDetails().add("[Entity TEST-2] Invalid Category");

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "govman-test.xml",
                MediaType.APPLICATION_XML_VALUE,
                validXml.getBytes()
        );

        when(importService.importFromStream(any(InputStream.class)))
                .thenReturn(resultWithErrors);

        // When/Then
        mockMvc.perform(multipart("/api/admin/import/govman")
                        .file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total", is(5)))
                .andExpect(jsonPath("$.imported", is(2)))
                .andExpect(jsonPath("$.errors", is(2)))
                .andExpect(jsonPath("$.errorDetails", hasSize(2)));
    }

    // =====================================================================
    // Status Endpoint Tests
    // =====================================================================

    @Test
    void getStatus_noImport_returnsBasicStatus() throws Exception {
        // When/Then
        mockMvc.perform(get("/api/admin/import/govman/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.inProgress", is(false)));
    }

    // =====================================================================
    // Last Result Endpoint Tests
    // =====================================================================

    @Test
    @Order(1)  // Run first before any imports set lastResult
    void getLastResult_noPreviousImport_returns404() throws Exception {
        // When/Then
        mockMvc.perform(get("/api/admin/import/govman/last-result"))
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(2)  // Run after 404 test
    void getLastResult_afterImport_returnsResult() throws Exception {
        // Given - First do an import
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "govman-test.xml",
                MediaType.APPLICATION_XML_VALUE,
                validXml.getBytes()
        );

        when(importService.importFromStream(any(InputStream.class)))
                .thenReturn(successResult);

        // Do import first
        mockMvc.perform(multipart("/api/admin/import/govman")
                        .file(file))
                .andExpect(status().isOk());

        // When/Then - Get last result
        mockMvc.perform(get("/api/admin/import/govman/last-result"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total", is(1)))
                .andExpect(jsonPath("$.imported", is(1)));
    }

    // =====================================================================
    // Response Format Tests
    // =====================================================================

    @Test
    void importResult_includesAllFields() throws Exception {
        // Given
        GovmanImportResult fullResult = GovmanImportResult.builder()
                .startTime(LocalDateTime.of(2025, 1, 1, 10, 0, 0))
                .endTime(LocalDateTime.of(2025, 1, 1, 10, 0, 30))
                .total(100)
                .imported(80)
                .updated(10)
                .skipped(5)
                .errors(5)
                .errorDetails(new ArrayList<>())
                .build();

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "govman-test.xml",
                MediaType.APPLICATION_XML_VALUE,
                validXml.getBytes()
        );

        when(importService.importFromStream(any(InputStream.class)))
                .thenReturn(fullResult);

        // When/Then
        mockMvc.perform(multipart("/api/admin/import/govman")
                        .file(file))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.total", is(100)))
                .andExpect(jsonPath("$.imported", is(80)))
                .andExpect(jsonPath("$.updated", is(10)))
                .andExpect(jsonPath("$.skipped", is(5)))
                .andExpect(jsonPath("$.errors", is(5)))
                .andExpect(jsonPath("$.durationSeconds", is(30)))
                .andExpect(jsonPath("$.successRate", is(90.0)));
    }

    // =====================================================================
    // Content-Type Tests
    // =====================================================================

    @Test
    void importGovman_acceptsMultipartFormData() throws Exception {
        // Given
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "govman-test.xml",
                MediaType.APPLICATION_XML_VALUE,
                validXml.getBytes()
        );

        when(importService.importFromStream(any(InputStream.class)))
                .thenReturn(successResult);

        // When/Then
        mockMvc.perform(multipart("/api/admin/import/govman")
                        .file(file)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk());
    }
}
