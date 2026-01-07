package org.newsanalyzer.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.newsanalyzer.dto.ExecutiveOrderDTO;
import org.newsanalyzer.dto.PresidencyAdministrationDTO;
import org.newsanalyzer.dto.PresidencyDTO;
import org.newsanalyzer.service.PresidencyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for PresidencyController using MockMvc with WebMvcTest.
 *
 * @author James (Dev Agent)
 * @since 2.0.0
 */
@WebMvcTest(PresidencyController.class)
@AutoConfigureMockMvc(addFilters = false)
class PresidencyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PresidencyService presidencyService;

    private PresidencyDTO currentPresidencyDTO;
    private PresidencyDTO historicalPresidencyDTO;
    private UUID currentPresidencyId;
    private UUID historicalPresidencyId;

    @BeforeEach
    void setUp() {
        currentPresidencyId = UUID.randomUUID();
        historicalPresidencyId = UUID.randomUUID();

        // Current presidency (47th - Biden to Trump transition as example)
        ArrayList<PresidencyDTO.VicePresidentDTO> currentVPs = new ArrayList<>();
        currentVPs.add(PresidencyDTO.VicePresidentDTO.builder()
                .personId(UUID.randomUUID())
                .fullName("JD Vance")
                .firstName("JD")
                .lastName("Vance")
                .startDate(LocalDate.of(2025, 1, 20))
                .termLabel("2025-present")
                .build());

        currentPresidencyDTO = PresidencyDTO.builder()
                .id(currentPresidencyId)
                .number(47)
                .ordinalLabel("47th")
                .personId(UUID.randomUUID())
                .presidentFullName("Donald John Trump")
                .presidentFirstName("Donald")
                .presidentLastName("Trump")
                .party("Republican")
                .startDate(LocalDate.of(2025, 1, 20))
                .endDate(null)
                .termLabel("2025-present")
                .isCurrent(true)
                .isLiving(true)
                .executiveOrderCount(0)
                .vicePresidents(currentVPs)
                .build();

        // Historical presidency (1st - Washington)
        ArrayList<PresidencyDTO.VicePresidentDTO> historicalVPs = new ArrayList<>();
        historicalVPs.add(PresidencyDTO.VicePresidentDTO.builder()
                .personId(UUID.randomUUID())
                .fullName("John Adams")
                .firstName("John")
                .lastName("Adams")
                .startDate(LocalDate.of(1789, 4, 21))
                .endDate(LocalDate.of(1797, 3, 4))
                .termLabel("1789-1797")
                .build());

        historicalPresidencyDTO = PresidencyDTO.builder()
                .id(historicalPresidencyId)
                .number(1)
                .ordinalLabel("1st")
                .personId(UUID.randomUUID())
                .presidentFullName("George Washington")
                .presidentFirstName("George")
                .presidentLastName("Washington")
                .party("Independent")
                .startDate(LocalDate.of(1789, 4, 30))
                .endDate(LocalDate.of(1797, 3, 4))
                .termLabel("1789-1797")
                .termDays(2865L)
                .isCurrent(false)
                .isLiving(false)
                .birthDate(LocalDate.of(1732, 2, 22))
                .deathDate(LocalDate.of(1799, 12, 14))
                .birthPlace("Westmoreland County, Virginia")
                .endReason("TERM_END")
                .executiveOrderCount(8)
                .vicePresidents(historicalVPs)
                .build();
    }

    // =====================================================================
    // List Presidencies Tests
    // =====================================================================

    @Nested
    @DisplayName("GET /api/presidencies")
    class ListPresidenciesTests {

        @Test
        @DisplayName("Should return paginated list of presidencies")
        void listPresidencies_returnsPage() throws Exception {
            // Given
            List<PresidencyDTO> content = new ArrayList<>();
            content.add(currentPresidencyDTO);
            content.add(historicalPresidencyDTO);
            Page<PresidencyDTO> page = new PageImpl<>(content, PageRequest.of(0, 20), 47);
            when(presidencyService.listPresidencies(0, 20)).thenReturn(page);

            // When/Then
            mockMvc.perform(get("/api/presidencies")
                            .param("page", "0")
                            .param("size", "20"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(2)))
                    .andExpect(jsonPath("$.content[0].number", is(47)))
                    .andExpect(jsonPath("$.content[0].presidentFullName", is("Donald John Trump")))
                    .andExpect(jsonPath("$.content[1].number", is(1)))
                    .andExpect(jsonPath("$.totalElements", is(47)));
        }

        @Test
        @DisplayName("Should use default pagination when not specified")
        void listPresidencies_defaultPagination() throws Exception {
            // Given
            List<PresidencyDTO> content = new ArrayList<>();
            content.add(currentPresidencyDTO);
            Page<PresidencyDTO> page = new PageImpl<>(content, PageRequest.of(0, 20), 1);
            when(presidencyService.listPresidencies(0, 20)).thenReturn(page);

            // When/Then
            mockMvc.perform(get("/api/presidencies"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(1)));
        }
    }

    // =====================================================================
    // Current Presidency Tests
    // =====================================================================

    @Nested
    @DisplayName("GET /api/presidencies/current")
    class CurrentPresidencyTests {

        @Test
        @DisplayName("Should return current presidency")
        void getCurrentPresidency_returnsCurrentPresident() throws Exception {
            // Given
            when(presidencyService.getCurrentPresidency()).thenReturn(Optional.of(currentPresidencyDTO));

            // When/Then
            mockMvc.perform(get("/api/presidencies/current"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.number", is(47)))
                    .andExpect(jsonPath("$.ordinalLabel", is("47th")))
                    .andExpect(jsonPath("$.current", is(true)))
                    .andExpect(jsonPath("$.presidentFullName", is("Donald John Trump")))
                    .andExpect(jsonPath("$.party", is("Republican")));
        }

        @Test
        @DisplayName("Should return 404 when no current presidency")
        void getCurrentPresidency_notFound() throws Exception {
            // Given
            when(presidencyService.getCurrentPresidency()).thenReturn(Optional.empty());

            // When/Then
            mockMvc.perform(get("/api/presidencies/current"))
                    .andExpect(status().isNotFound());
        }
    }

    // =====================================================================
    // Get by ID Tests
    // =====================================================================

    @Nested
    @DisplayName("GET /api/presidencies/{id}")
    class GetByIdTests {

        @Test
        @DisplayName("Should return presidency by ID")
        void getPresidencyById_returnsPresidency() throws Exception {
            // Given
            when(presidencyService.getPresidencyById(historicalPresidencyId))
                    .thenReturn(Optional.of(historicalPresidencyDTO));

            // When/Then
            mockMvc.perform(get("/api/presidencies/{id}", historicalPresidencyId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", is(historicalPresidencyId.toString())))
                    .andExpect(jsonPath("$.number", is(1)))
                    .andExpect(jsonPath("$.presidentFullName", is("George Washington")))
                    .andExpect(jsonPath("$.party", is("Independent")))
                    .andExpect(jsonPath("$.termLabel", is("1789-1797")))
                    .andExpect(jsonPath("$.endReason", is("TERM_END")))
                    .andExpect(jsonPath("$.vicePresidents", hasSize(1)))
                    .andExpect(jsonPath("$.vicePresidents[0].fullName", is("John Adams")));
        }

        @Test
        @DisplayName("Should return 404 for unknown ID")
        void getPresidencyById_notFound() throws Exception {
            // Given
            UUID unknownId = UUID.randomUUID();
            when(presidencyService.getPresidencyById(unknownId)).thenReturn(Optional.empty());

            // When/Then
            mockMvc.perform(get("/api/presidencies/{id}", unknownId))
                    .andExpect(status().isNotFound());
        }
    }

    // =====================================================================
    // Get by Number Tests
    // =====================================================================

    @Nested
    @DisplayName("GET /api/presidencies/number/{number}")
    class GetByNumberTests {

        @Test
        @DisplayName("Should return presidency by number")
        void getPresidencyByNumber_returnsPresidency() throws Exception {
            // Given
            when(presidencyService.getPresidencyByNumber(1))
                    .thenReturn(Optional.of(historicalPresidencyDTO));

            // When/Then
            mockMvc.perform(get("/api/presidencies/number/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.number", is(1)))
                    .andExpect(jsonPath("$.ordinalLabel", is("1st")))
                    .andExpect(jsonPath("$.presidentFullName", is("George Washington")));
        }

        @Test
        @DisplayName("Should return 404 for unknown presidency number")
        void getPresidencyByNumber_notFound() throws Exception {
            // Given
            when(presidencyService.getPresidencyByNumber(99)).thenReturn(Optional.empty());

            // When/Then
            mockMvc.perform(get("/api/presidencies/number/99"))
                    .andExpect(status().isNotFound());
        }
    }

    // =====================================================================
    // Executive Orders Tests
    // =====================================================================

    @Nested
    @DisplayName("GET /api/presidencies/{id}/executive-orders")
    class ExecutiveOrdersTests {

        @Test
        @DisplayName("Should return paginated executive orders")
        void getExecutiveOrders_returnsList() throws Exception {
            // Given
            List<ExecutiveOrderDTO> content = new ArrayList<>();
            content.add(ExecutiveOrderDTO.builder()
                    .id(UUID.randomUUID())
                    .presidencyId(historicalPresidencyId)
                    .eoNumber(1)
                    .title("Providing for Lighthouse Operations")
                    .signingDate(LocalDate.of(1789, 8, 7))
                    .status("ACTIVE")
                    .build());
            Page<ExecutiveOrderDTO> page = new PageImpl<>(content, PageRequest.of(0, 20), 1);
            when(presidencyService.getExecutiveOrders(eq(historicalPresidencyId), eq(0), eq(20)))
                    .thenReturn(page);

            // When/Then
            mockMvc.perform(get("/api/presidencies/{id}/executive-orders", historicalPresidencyId)
                            .param("page", "0")
                            .param("size", "20"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(1)))
                    .andExpect(jsonPath("$.content[0].eoNumber", is(1)))
                    .andExpect(jsonPath("$.content[0].title", is("Providing for Lighthouse Operations")));
        }

        @Test
        @DisplayName("Should return empty page when no executive orders")
        void getExecutiveOrders_emptyList() throws Exception {
            // Given
            Page<ExecutiveOrderDTO> page = new PageImpl<>(new ArrayList<>(), PageRequest.of(0, 20), 0);
            when(presidencyService.getExecutiveOrders(any(UUID.class), eq(0), eq(20)))
                    .thenReturn(page);

            // When/Then
            mockMvc.perform(get("/api/presidencies/{id}/executive-orders", currentPresidencyId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(0)))
                    .andExpect(jsonPath("$.totalElements", is(0)));
        }
    }

    // =====================================================================
    // Administration Tests
    // =====================================================================

    @Nested
    @DisplayName("GET /api/presidencies/{id}/administration")
    class AdministrationTests {

        @Test
        @DisplayName("Should return administration data with VPs")
        void getAdministration_returnsVPsAndCoS() throws Exception {
            // Given
            List<PresidencyAdministrationDTO.OfficeholderDTO> vps = new ArrayList<>();
            vps.add(PresidencyAdministrationDTO.OfficeholderDTO.builder()
                    .holdingId(UUID.randomUUID())
                    .personId(UUID.randomUUID())
                    .fullName("John Adams")
                    .firstName("John")
                    .lastName("Adams")
                    .positionTitle("Vice President of the United States")
                    .startDate(LocalDate.of(1789, 4, 21))
                    .endDate(LocalDate.of(1797, 3, 4))
                    .termLabel("1789-1797")
                    .build());

            PresidencyAdministrationDTO adminDTO = PresidencyAdministrationDTO.builder()
                    .presidencyId(historicalPresidencyId)
                    .presidencyNumber(1)
                    .presidencyLabel("1st Presidency")
                    .vicePresidents(vps)
                    .chiefsOfStaff(new ArrayList<>())
                    .cabinetMembers(new ArrayList<>())
                    .build();

            when(presidencyService.getAdministration(historicalPresidencyId))
                    .thenReturn(Optional.of(adminDTO));

            // When/Then
            mockMvc.perform(get("/api/presidencies/{id}/administration", historicalPresidencyId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.presidencyNumber", is(1)))
                    .andExpect(jsonPath("$.presidencyLabel", is("1st Presidency")))
                    .andExpect(jsonPath("$.vicePresidents", hasSize(1)))
                    .andExpect(jsonPath("$.vicePresidents[0].fullName", is("John Adams")))
                    .andExpect(jsonPath("$.vicePresidents[0].positionTitle", is("Vice President of the United States")));
        }

        @Test
        @DisplayName("Should return 404 when presidency not found")
        void getAdministration_notFound() throws Exception {
            // Given
            UUID unknownId = UUID.randomUUID();
            when(presidencyService.getAdministration(unknownId)).thenReturn(Optional.empty());

            // When/Then
            mockMvc.perform(get("/api/presidencies/{id}/administration", unknownId))
                    .andExpect(status().isNotFound());
        }
    }
}
