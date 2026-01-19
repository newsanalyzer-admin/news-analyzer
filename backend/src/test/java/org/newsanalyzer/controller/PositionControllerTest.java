package org.newsanalyzer.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.newsanalyzer.exception.ResourceNotFoundException;
import org.newsanalyzer.model.GovernmentPosition;
import org.newsanalyzer.model.Person.Chamber;
import org.newsanalyzer.model.PositionHolding;
import org.newsanalyzer.model.PositionType;
import org.newsanalyzer.model.DataSource;
import org.newsanalyzer.repository.GovernmentPositionRepository;
import org.newsanalyzer.repository.PositionHoldingRepository;
import org.newsanalyzer.service.PositionInitializationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for PositionController.
 *
 * @author James (Dev Agent)
 */
@WebMvcTest(PositionController.class)
@WithMockUser
class PositionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GovernmentPositionRepository positionRepository;

    @MockBean
    private PositionHoldingRepository holdingRepository;

    @MockBean
    private PositionInitializationService positionInitService;

    private GovernmentPosition testPosition;
    private PositionHolding testHolding;

    @BeforeEach
    void setUp() {
        testPosition = new GovernmentPosition();
        testPosition.setId(UUID.randomUUID());
        testPosition.setTitle("Senator");
        testPosition.setChamber(Chamber.SENATE);
        testPosition.setState("VT");
        testPosition.setSenateClass(1);
        testPosition.setPositionType(PositionType.ELECTED);
        testPosition.setDescription("U.S. Senator from VT (Class 1)");

        testHolding = PositionHolding.builder()
                .id(UUID.randomUUID())
                .individualId(UUID.randomUUID())
                .positionId(testPosition.getId())
                .startDate(LocalDate.of(2023, 1, 3))
                .endDate(null)
                .congress(118)
                .dataSource(DataSource.CONGRESS_GOV)
                .build();
    }

    // =====================================================================
    // List Endpoints Tests
    // =====================================================================

    @Test
    @DisplayName("GET /api/positions - Should return paginated list of positions")
    void listAll_returnsPagedPositions() throws Exception {
        // Given
        Page<GovernmentPosition> page = new PageImpl<>(List.of(testPosition));
        when(positionRepository.findAll(any(Pageable.class))).thenReturn(page);

        // When/Then
        mockMvc.perform(get("/api/positions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].title", is("Senator")))
                .andExpect(jsonPath("$.content[0].state", is("VT")))
                .andExpect(jsonPath("$.content[0].chamber", is("SENATE")));
    }

    @Test
    @DisplayName("GET /api/positions/senate - Should return all Senate positions")
    void listSenatePositions_returnsSenatePositions() throws Exception {
        // Given
        when(positionRepository.findAllSenatePositions()).thenReturn(List.of(testPosition));

        // When/Then
        mockMvc.perform(get("/api/positions/senate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].chamber", is("SENATE")));
    }

    @Test
    @DisplayName("GET /api/positions/house - Should return all House positions")
    void listHousePositions_returnsHousePositions() throws Exception {
        // Given
        GovernmentPosition housePosition = new GovernmentPosition();
        housePosition.setId(UUID.randomUUID());
        housePosition.setTitle("Representative");
        housePosition.setChamber(Chamber.HOUSE);
        housePosition.setState("CA");
        housePosition.setDistrict(12);
        housePosition.setPositionType(PositionType.ELECTED);

        when(positionRepository.findAllHousePositions()).thenReturn(List.of(housePosition));

        // When/Then
        mockMvc.perform(get("/api/positions/house"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].chamber", is("HOUSE")))
                .andExpect(jsonPath("$[0].district", is(12)));
    }

    // =====================================================================
    // Lookup Endpoints Tests
    // =====================================================================

    @Test
    @DisplayName("GET /api/positions/{id} - Should return position when found")
    void getById_found_returnsPosition() throws Exception {
        // Given
        when(positionRepository.findById(testPosition.getId())).thenReturn(Optional.of(testPosition));

        // When/Then
        mockMvc.perform(get("/api/positions/" + testPosition.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is("Senator")))
                .andExpect(jsonPath("$.state", is("VT")));
    }

    @Test
    @DisplayName("GET /api/positions/{id} - Should return 404 when not found")
    void getById_notFound_returns404() throws Exception {
        // Given
        UUID unknownId = UUID.randomUUID();
        when(positionRepository.findById(unknownId)).thenReturn(Optional.empty());

        // When/Then
        mockMvc.perform(get("/api/positions/" + unknownId))
                .andExpect(status().isNotFound());
    }

    // =====================================================================
    // Filter Endpoints Tests
    // =====================================================================

    @Test
    @DisplayName("GET /api/positions/by-chamber/{chamber} - Should filter by SENATE")
    void getByChamber_senate_returnsSenatePositions() throws Exception {
        // Given
        Page<GovernmentPosition> page = new PageImpl<>(List.of(testPosition));
        when(positionRepository.findByChamber(eq(Chamber.SENATE), any(Pageable.class))).thenReturn(page);

        // When/Then
        mockMvc.perform(get("/api/positions/by-chamber/SENATE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].chamber", is("SENATE")));
    }

    @Test
    @DisplayName("GET /api/positions/by-chamber/{chamber} - Should return 400 for invalid chamber")
    void getByChamber_invalid_returns400() throws Exception {
        mockMvc.perform(get("/api/positions/by-chamber/INVALID"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/positions/by-state/{state} - Should filter by state")
    void getByState_validState_returnsPositions() throws Exception {
        // Given
        Page<GovernmentPosition> page = new PageImpl<>(List.of(testPosition));
        when(positionRepository.findByState(eq("VT"), any(Pageable.class))).thenReturn(page);

        // When/Then
        mockMvc.perform(get("/api/positions/by-state/VT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].state", is("VT")));
    }

    @Test
    @DisplayName("GET /api/positions/by-state/{state} - Should return 400 for invalid state")
    void getByState_invalidState_returns400() throws Exception {
        mockMvc.perform(get("/api/positions/by-state/VERMONT"))
                .andExpect(status().isBadRequest());
    }

    // =====================================================================
    // History Endpoints Tests
    // =====================================================================

    @Test
    @DisplayName("GET /api/positions/{id}/history - Should return position history")
    void getPositionHistory_found_returnsHistory() throws Exception {
        // Given
        when(positionRepository.existsById(testPosition.getId())).thenReturn(true);
        when(holdingRepository.findByPositionIdOrderByStartDateDesc(testPosition.getId()))
                .thenReturn(List.of(testHolding));

        // When/Then
        mockMvc.perform(get("/api/positions/" + testPosition.getId() + "/history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].congress", is(118)));
    }

    @Test
    @DisplayName("GET /api/positions/{id}/history - Should return 404 for unknown position")
    void getPositionHistory_notFound_returns404() throws Exception {
        // Given
        UUID unknownId = UUID.randomUUID();
        when(positionRepository.existsById(unknownId)).thenReturn(false);

        // When/Then
        mockMvc.perform(get("/api/positions/" + unknownId + "/history"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/positions/{id}/holder - Should return current holder")
    void getCurrentHolder_found_returnsHolder() throws Exception {
        // Given
        when(positionRepository.existsById(testPosition.getId())).thenReturn(true);
        when(holdingRepository.findCurrentHoldersByPositionId(testPosition.getId()))
                .thenReturn(List.of(testHolding));

        // When/Then
        mockMvc.perform(get("/api/positions/" + testPosition.getId() + "/holder"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    @DisplayName("GET /api/positions/{id}/holder-on/{date} - Should return holder on specific date")
    void getHolderOnDate_validDate_returnsHolder() throws Exception {
        // Given
        when(positionRepository.existsById(testPosition.getId())).thenReturn(true);
        when(holdingRepository.findByPositionIdAndActiveOnDate(
                eq(testPosition.getId()), eq(LocalDate.of(2024, 1, 15))))
                .thenReturn(List.of(testHolding));

        // When/Then
        mockMvc.perform(get("/api/positions/" + testPosition.getId() + "/holder-on/2024-01-15"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    @DisplayName("GET /api/positions/{id}/holder-on/{date} - Should return 400 for invalid date")
    void getHolderOnDate_invalidDate_returns400() throws Exception {
        // Given
        when(positionRepository.existsById(testPosition.getId())).thenReturn(true);

        // When/Then
        mockMvc.perform(get("/api/positions/" + testPosition.getId() + "/holder-on/not-a-date"))
                .andExpect(status().isBadRequest());
    }

    // =====================================================================
    // Statistics Endpoints Tests
    // =====================================================================

    @Test
    @DisplayName("GET /api/positions/count - Should return total count")
    void count_returnsTotal() throws Exception {
        // Given
        when(positionRepository.count()).thenReturn(535L);

        // When/Then
        mockMvc.perform(get("/api/positions/count"))
                .andExpect(status().isOk())
                .andExpect(content().string("535"));
    }

    @Test
    @DisplayName("GET /api/positions/stats - Should return position stats")
    void getStats_returnsStats() throws Exception {
        // Given
        PositionInitializationService.PositionStats stats =
                new PositionInitializationService.PositionStats(100, 435);
        when(positionInitService.getPositionStats()).thenReturn(stats);

        // When/Then
        mockMvc.perform(get("/api/positions/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.senateCount", is(100)))
                .andExpect(jsonPath("$.houseCount", is(435)))
                .andExpect(jsonPath("$.totalCount", is(535)));
    }

    // =====================================================================
    // Admin Endpoints Tests
    // =====================================================================

    @Test
    @DisplayName("POST /api/positions/initialize - Should trigger position initialization")
    void initializePositions_returnsResult() throws Exception {
        // Given
        PositionInitializationService.InitResult result =
                new PositionInitializationService.InitResult(100, 435);
        when(positionInitService.initializeAllPositions()).thenReturn(result);

        // When/Then
        mockMvc.perform(post("/api/positions/initialize")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.senateCreated", is(100)))
                .andExpect(jsonPath("$.houseCreated", is(435)))
                .andExpect(jsonPath("$.totalCreated", is(535)));
    }

    // =====================================================================
    // Response Time Tests
    // =====================================================================

    @Test
    @DisplayName("GET /api/positions - Should respond within 500ms")
    void listAll_respondsWithin500ms() throws Exception {
        // Given
        Page<GovernmentPosition> page = new PageImpl<>(List.of(testPosition));
        when(positionRepository.findAll(any(Pageable.class))).thenReturn(page);

        // When
        long startTime = System.currentTimeMillis();
        mockMvc.perform(get("/api/positions"))
                .andExpect(status().isOk());
        long endTime = System.currentTimeMillis();

        // Then
        long responseTime = endTime - startTime;
        org.assertj.core.api.Assertions.assertThat(responseTime).isLessThan(500);
    }
}
