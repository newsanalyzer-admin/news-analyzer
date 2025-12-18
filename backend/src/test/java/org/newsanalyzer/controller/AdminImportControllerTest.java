package org.newsanalyzer.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.newsanalyzer.dto.CongressMemberImportRequest;
import org.newsanalyzer.model.Person;
import org.newsanalyzer.model.Person.Chamber;
import org.newsanalyzer.repository.PersonRepository;
import org.newsanalyzer.repository.RegulationRepository;
import org.newsanalyzer.service.FederalRegisterImportService;
import org.newsanalyzer.service.LegislatorEnrichmentImportService;
import org.newsanalyzer.service.LegislatorsSearchService;
import org.newsanalyzer.service.MemberSyncService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for AdminImportController.
 *
 * Tests Congress.gov member import endpoints with mocked service layer.
 *
 * @author Quinn (QA Agent)
 * @since 2.0.0
 */
@WebMvcTest(AdminImportController.class)
@WithMockUser
class AdminImportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MemberSyncService memberSyncService;

    @MockBean
    private PersonRepository personRepository;

    @MockBean
    private FederalRegisterImportService federalRegisterImportService;

    @MockBean
    private RegulationRepository regulationRepository;

    @MockBean
    private LegislatorEnrichmentImportService legislatorEnrichmentImportService;

    @MockBean
    private LegislatorsSearchService legislatorsSearchService;

    private Person testPerson;
    private UUID testPersonId;

    @BeforeEach
    void setUp() {
        testPersonId = UUID.randomUUID();
        testPerson = new Person();
        testPerson.setId(testPersonId);
        testPerson.setBioguideId("S000033");
        testPerson.setFirstName("Bernard");
        testPerson.setLastName("Sanders");
        testPerson.setParty("Independent");
        testPerson.setState("VT");
        testPerson.setChamber(Chamber.SENATE);
        testPerson.setBirthDate(LocalDate.of(1941, 9, 8));
    }

    // =========================================================================
    // Import Endpoint Tests
    // =========================================================================

    @Test
    @DisplayName("POST /api/admin/import/congress/member - Should create new member")
    void importCongressMember_newMember_createsRecord() throws Exception {
        // Given
        CongressMemberImportRequest request = new CongressMemberImportRequest();
        request.setBioguideId("S000033");
        request.setForceOverwrite(true);

        when(personRepository.findByBioguideId("S000033")).thenReturn(Optional.empty());
        when(memberSyncService.syncMemberByBioguideId("S000033")).thenReturn(Optional.of(testPerson));

        // When/Then
        mockMvc.perform(post("/api/admin/import/congress/member")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(testPersonId.toString())))
                .andExpect(jsonPath("$.bioguideId", is("S000033")))
                .andExpect(jsonPath("$.name", is("Bernard Sanders")))
                .andExpect(jsonPath("$.created", is(true)))
                .andExpect(jsonPath("$.updated", is(false)));
    }

    @Test
    @DisplayName("POST /api/admin/import/congress/member - Should update existing member with forceOverwrite")
    void importCongressMember_existingMember_updatesRecord() throws Exception {
        // Given
        CongressMemberImportRequest request = new CongressMemberImportRequest();
        request.setBioguideId("S000033");
        request.setForceOverwrite(true);

        when(personRepository.findByBioguideId("S000033")).thenReturn(Optional.of(testPerson));
        when(memberSyncService.syncMemberByBioguideId("S000033")).thenReturn(Optional.of(testPerson));

        // When/Then
        mockMvc.perform(post("/api/admin/import/congress/member")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(testPersonId.toString())))
                .andExpect(jsonPath("$.created", is(false)))
                .andExpect(jsonPath("$.updated", is(true)));
    }

    @Test
    @DisplayName("POST /api/admin/import/congress/member - Should reject without forceOverwrite when exists")
    void importCongressMember_existingMemberWithoutForce_returnsError() throws Exception {
        // Given
        CongressMemberImportRequest request = new CongressMemberImportRequest();
        request.setBioguideId("S000033");
        request.setForceOverwrite(false);

        when(personRepository.findByBioguideId("S000033")).thenReturn(Optional.of(testPerson));

        // When/Then
        mockMvc.perform(post("/api/admin/import/congress/member")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(testPersonId.toString())))
                .andExpect(jsonPath("$.created", is(false)))
                .andExpect(jsonPath("$.updated", is(false)))
                .andExpect(jsonPath("$.error", containsString("already exists")));

        // Verify sync was not called
        verify(memberSyncService, never()).syncMemberByBioguideId(any());
    }

    @Test
    @DisplayName("POST /api/admin/import/congress/member - Should return 400 for null bioguideId")
    void importCongressMember_nullBioguideId_returns400() throws Exception {
        // Given
        CongressMemberImportRequest request = new CongressMemberImportRequest();
        request.setBioguideId(null);

        // When/Then
        mockMvc.perform(post("/api/admin/import/congress/member")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("bioguideId is required")));
    }

    @Test
    @DisplayName("POST /api/admin/import/congress/member - Should return 400 for empty bioguideId")
    void importCongressMember_emptyBioguideId_returns400() throws Exception {
        // Given
        CongressMemberImportRequest request = new CongressMemberImportRequest();
        request.setBioguideId("");

        // When/Then
        mockMvc.perform(post("/api/admin/import/congress/member")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("bioguideId is required")));
    }

    @Test
    @DisplayName("POST /api/admin/import/congress/member - Should return 400 for whitespace bioguideId")
    void importCongressMember_whitespaceBioguideId_returns400() throws Exception {
        // Given
        CongressMemberImportRequest request = new CongressMemberImportRequest();
        request.setBioguideId("   ");

        // When/Then
        mockMvc.perform(post("/api/admin/import/congress/member")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("bioguideId is required")));
    }

    @Test
    @DisplayName("POST /api/admin/import/congress/member - Should return 404 when member not found on Congress.gov")
    void importCongressMember_notFoundOnCongressGov_returns404() throws Exception {
        // Given
        CongressMemberImportRequest request = new CongressMemberImportRequest();
        request.setBioguideId("INVALID123");
        request.setForceOverwrite(true);

        when(personRepository.findByBioguideId("INVALID123")).thenReturn(Optional.empty());
        when(memberSyncService.syncMemberByBioguideId("INVALID123")).thenReturn(Optional.empty());

        // When/Then
        mockMvc.perform(post("/api/admin/import/congress/member")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.bioguideId", is("INVALID123")))
                .andExpect(jsonPath("$.error", containsString("not found on Congress.gov")));
    }

    // =========================================================================
    // Check Exists Endpoint Tests
    // =========================================================================

    @Test
    @DisplayName("GET /api/admin/import/congress/member/{bioguideId}/exists - Should return true when exists")
    void checkMemberExists_exists_returnsTrue() throws Exception {
        // Given
        when(personRepository.findByBioguideId("S000033")).thenReturn(Optional.of(testPerson));

        // When/Then
        mockMvc.perform(get("/api/admin/import/congress/member/S000033/exists"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.exists", is(true)))
                .andExpect(jsonPath("$.id", is(testPersonId.toString())))
                .andExpect(jsonPath("$.name", is("Bernard Sanders")));
    }

    @Test
    @DisplayName("GET /api/admin/import/congress/member/{bioguideId}/exists - Should return false when not exists")
    void checkMemberExists_notExists_returnsFalse() throws Exception {
        // Given
        when(personRepository.findByBioguideId("NONEXISTENT")).thenReturn(Optional.empty());

        // When/Then
        mockMvc.perform(get("/api/admin/import/congress/member/NONEXISTENT/exists"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.exists", is(false)))
                .andExpect(jsonPath("$.id", nullValue()))
                .andExpect(jsonPath("$.name", nullValue()));
    }

    // =========================================================================
    // Default Values Tests
    // =========================================================================

    @Test
    @DisplayName("POST /api/admin/import/congress/member - forceOverwrite defaults to false")
    void importCongressMember_defaultForceOverwrite_isFalse() throws Exception {
        // Given - request without setting forceOverwrite
        String requestJson = "{\"bioguideId\": \"S000033\"}";

        when(personRepository.findByBioguideId("S000033")).thenReturn(Optional.of(testPerson));

        // When/Then - should behave as if forceOverwrite = false
        mockMvc.perform(post("/api/admin/import/congress/member")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.error", containsString("already exists")));

        // Verify sync was not called because forceOverwrite defaults to false
        verify(memberSyncService, never()).syncMemberByBioguideId(any());
    }
}
