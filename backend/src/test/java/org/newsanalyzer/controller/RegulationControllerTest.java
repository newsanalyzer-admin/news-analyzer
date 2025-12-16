package org.newsanalyzer.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.newsanalyzer.dto.RegulationDTO;
import org.newsanalyzer.model.DocumentType;
import org.newsanalyzer.service.RegulationLookupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for RegulationController using MockMvc with WebMvcTest.
 *
 * @author James (Dev Agent)
 * @since 2.0.0
 */
@WebMvcTest(RegulationController.class)
@AutoConfigureMockMvc(addFilters = false)
class RegulationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RegulationLookupService regulationLookupService;

    private RegulationDTO testDTO;
    private UUID regulationId;
    private UUID orgId;

    @BeforeEach
    void setUp() {
        regulationId = UUID.randomUUID();
        orgId = UUID.randomUUID();

        // Use ArrayList for Jackson serialization compatibility
        ArrayList<RegulationDTO.CfrReferenceDTO> cfrRefs = new ArrayList<>();
        cfrRefs.add(RegulationDTO.CfrReferenceDTO.builder()
                .title(40)
                .part(50)
                .fullCitation("40 CFR 50")
                .build());

        ArrayList<RegulationDTO.AgencyDTO> agencies = new ArrayList<>();
        agencies.add(RegulationDTO.AgencyDTO.builder()
                .id(orgId)
                .name("Environmental Protection Agency")
                .acronym("EPA")
                .primary(true)
                .build());

        testDTO = RegulationDTO.builder()
                .id(regulationId)
                .documentNumber("2024-12345")
                .title("Air Quality Standards for Fine Particulate Matter")
                .documentAbstract("The EPA is revising the primary annual PM2.5 standard...")
                .documentType("RULE")
                .documentTypeDescription("Final Rule")
                .publicationDate(LocalDate.of(2024, 3, 15))
                .effectiveOn(LocalDate.of(2024, 5, 15))
                .regulationIdNumber("2060-AU09")
                .cfrReferences(cfrRefs)
                .agencies(agencies)
                .sourceUrl("https://www.federalregister.gov/d/2024-12345")
                .build();
    }

    // =====================================================================
    // List Regulations Tests
    // =====================================================================

    @Test
    void listRegulations_returnsPage() throws Exception {
        // Given
        List<RegulationDTO> content = new ArrayList<>();
        content.add(testDTO);
        Page<RegulationDTO> page = new PageImpl<>(content, PageRequest.of(0, 20), 1);
        when(regulationLookupService.listRegulations(0, 20)).thenReturn(page);

        // When/Then
        mockMvc.perform(get("/api/regulations")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].documentNumber", is("2024-12345")))
                .andExpect(jsonPath("$.content[0].title", is("Air Quality Standards for Fine Particulate Matter")));
    }

    @Test
    void listRegulations_defaultPagination() throws Exception {
        // Given
        List<RegulationDTO> content = new ArrayList<>();
        content.add(testDTO);
        Page<RegulationDTO> page = new PageImpl<>(content, PageRequest.of(0, 20), 1);
        when(regulationLookupService.listRegulations(0, 20)).thenReturn(page);

        // When/Then
        mockMvc.perform(get("/api/regulations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)));
    }

    // =====================================================================
    // Get by Document Number Tests
    // =====================================================================

    @Test
    void getByDocumentNumber_found_returnsDTO() throws Exception {
        // Given
        when(regulationLookupService.findByDocumentNumber("2024-12345"))
                .thenReturn(Optional.of(testDTO));

        // When/Then
        mockMvc.perform(get("/api/regulations/2024-12345"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.documentNumber", is("2024-12345")))
                .andExpect(jsonPath("$.title", is("Air Quality Standards for Fine Particulate Matter")))
                .andExpect(jsonPath("$.documentType", is("RULE")))
                .andExpect(jsonPath("$.documentTypeDescription", is("Final Rule")));
    }

    @Test
    void getByDocumentNumber_notFound_returns404() throws Exception {
        // Given
        when(regulationLookupService.findByDocumentNumber("invalid"))
                .thenReturn(Optional.empty());

        // When/Then
        mockMvc.perform(get("/api/regulations/invalid"))
                .andExpect(status().isNotFound());
    }

    // =====================================================================
    // Search Tests
    // =====================================================================

    @Test
    void searchRegulations_validQuery_returnsResults() throws Exception {
        // Given
        List<RegulationDTO> content = new ArrayList<>();
        content.add(testDTO);
        Page<RegulationDTO> page = new PageImpl<>(content, PageRequest.of(0, 20), 1);
        when(regulationLookupService.searchRegulations(eq("emissions"), eq(0), eq(20)))
                .thenReturn(page);

        // When/Then
        mockMvc.perform(get("/api/regulations/search")
                        .param("q", "emissions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].documentNumber", is("2024-12345")));
    }

    @Test
    void searchRegulations_withPagination_usesParams() throws Exception {
        // Given
        List<RegulationDTO> content = new ArrayList<>();
        content.add(testDTO);
        Page<RegulationDTO> page = new PageImpl<>(content, PageRequest.of(2, 50), 1);
        when(regulationLookupService.searchRegulations("air quality", 2, 50)).thenReturn(page);

        // When/Then
        mockMvc.perform(get("/api/regulations/search")
                        .param("q", "air quality")
                        .param("page", "2")
                        .param("size", "50"))
                .andExpect(status().isOk());
    }

    // =====================================================================
    // Agency Filter Tests
    // =====================================================================

    @Test
    void getByAgency_validOrgId_returnsResults() throws Exception {
        // Given
        List<RegulationDTO> content = new ArrayList<>();
        content.add(testDTO);
        Page<RegulationDTO> page = new PageImpl<>(content, PageRequest.of(0, 20), 1);
        when(regulationLookupService.findByAgency(eq(orgId), eq(0), eq(20))).thenReturn(page);

        // When/Then
        mockMvc.perform(get("/api/regulations/by-agency/" + orgId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].agencies[0].name", is("Environmental Protection Agency")));
    }

    // =====================================================================
    // Document Type Tests
    // =====================================================================

    @Test
    void getByType_validType_returnsResults() throws Exception {
        // Given
        List<RegulationDTO> content = new ArrayList<>();
        content.add(testDTO);
        Page<RegulationDTO> page = new PageImpl<>(content, PageRequest.of(0, 20), 1);
        when(regulationLookupService.findByDocumentType(eq(DocumentType.RULE), eq(0), eq(20)))
                .thenReturn(page);

        // When/Then
        mockMvc.perform(get("/api/regulations/by-type/RULE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].documentType", is("RULE")));
    }

    // =====================================================================
    // Date Range Tests
    // =====================================================================

    @Test
    void getByDateRange_validRange_returnsResults() throws Exception {
        // Given
        List<RegulationDTO> content = new ArrayList<>();
        content.add(testDTO);
        Page<RegulationDTO> page = new PageImpl<>(content, PageRequest.of(0, 20), 1);
        LocalDate start = LocalDate.of(2024, 1, 1);
        LocalDate end = LocalDate.of(2024, 12, 31);
        when(regulationLookupService.findByDateRange(eq(start), eq(end), eq(0), eq(20)))
                .thenReturn(page);

        // When/Then
        mockMvc.perform(get("/api/regulations/by-date-range")
                        .param("start", "2024-01-01")
                        .param("end", "2024-12-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)));
    }

    // =====================================================================
    // Effective Date Tests
    // =====================================================================

    @Test
    void getEffectiveOn_validDate_returnsRules() throws Exception {
        // Given
        LocalDate date = LocalDate.of(2024, 6, 1);
        when(regulationLookupService.findRulesEffectiveOn(date)).thenReturn(List.of(testDTO));

        // When/Then
        mockMvc.perform(get("/api/regulations/effective-on/2024-06-01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].effectiveOn", is("2024-05-15")));
    }

    // =====================================================================
    // CFR Reference Tests
    // =====================================================================

    @Test
    void getByCfrReference_validCitation_returnsResults() throws Exception {
        // Given
        when(regulationLookupService.findByCfrReference(40, 50)).thenReturn(List.of(testDTO));

        // When/Then
        mockMvc.perform(get("/api/regulations/cfr/40/50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].cfrReferences[0].title", is(40)))
                .andExpect(jsonPath("$[0].cfrReferences[0].fullCitation", is("40 CFR 50")));
    }

    // =====================================================================
    // Response Format Tests
    // =====================================================================

    @Test
    void response_includesAllFields() throws Exception {
        // Given
        when(regulationLookupService.findByDocumentNumber("2024-12345"))
                .thenReturn(Optional.of(testDTO));

        // When/Then
        mockMvc.perform(get("/api/regulations/2024-12345")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(regulationId.toString())))
                .andExpect(jsonPath("$.documentNumber", is("2024-12345")))
                .andExpect(jsonPath("$.title", is("Air Quality Standards for Fine Particulate Matter")))
                .andExpect(jsonPath("$.documentAbstract", containsString("EPA")))
                .andExpect(jsonPath("$.documentType", is("RULE")))
                .andExpect(jsonPath("$.documentTypeDescription", is("Final Rule")))
                .andExpect(jsonPath("$.publicationDate", is("2024-03-15")))
                .andExpect(jsonPath("$.effectiveOn", is("2024-05-15")))
                .andExpect(jsonPath("$.regulationIdNumber", is("2060-AU09")))
                .andExpect(jsonPath("$.cfrReferences", hasSize(1)))
                .andExpect(jsonPath("$.cfrReferences[0].title", is(40)))
                .andExpect(jsonPath("$.cfrReferences[0].part", is(50)))
                .andExpect(jsonPath("$.cfrReferences[0].fullCitation", is("40 CFR 50")))
                .andExpect(jsonPath("$.agencies", hasSize(1)))
                .andExpect(jsonPath("$.agencies[0].id", is(orgId.toString())))
                .andExpect(jsonPath("$.agencies[0].name", is("Environmental Protection Agency")))
                .andExpect(jsonPath("$.agencies[0].acronym", is("EPA")))
                .andExpect(jsonPath("$.agencies[0].primary", is(true)))
                .andExpect(jsonPath("$.sourceUrl", is("https://www.federalregister.gov/d/2024-12345")));
    }

    // =====================================================================
    // Pagination Response Tests
    // =====================================================================

    @Test
    void paginatedResponse_includesPageMetadata() throws Exception {
        // Given
        List<RegulationDTO> content = new ArrayList<>();
        content.add(testDTO);
        Page<RegulationDTO> page = new PageImpl<>(
                content,
                PageRequest.of(0, 20),
                156 // total elements
        );
        when(regulationLookupService.searchRegulations("test", 0, 20)).thenReturn(page);

        // When/Then
        mockMvc.perform(get("/api/regulations/search")
                        .param("q", "test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.totalElements", is(156)))
                .andExpect(jsonPath("$.totalPages", is(8)))
                .andExpect(jsonPath("$.number", is(0)))
                .andExpect(jsonPath("$.size", is(20)));
    }
}
