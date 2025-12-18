package org.newsanalyzer.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.newsanalyzer.dto.FjcImportResult;
import org.newsanalyzer.dto.JudgeDTO;
import org.newsanalyzer.service.FjcCsvImportService;
import org.newsanalyzer.service.JudgeService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;

import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for JudgeController.
 *
 * Tests cover:
 * - List judges with pagination
 * - Get judge by ID
 * - Search judges by name
 * - Get judge statistics
 * - Import from FJC URL
 * - Import from CSV upload
 *
 * @author James (Dev Agent)
 * @since 2.0.0
 * @story UI-1.11
 */
@ExtendWith(MockitoExtension.class)
class JudgeControllerTest {

    @Mock
    private JudgeService judgeService;

    @Mock
    private FjcCsvImportService fjcImportService;

    @InjectMocks
    private JudgeController controller;

    private JudgeDTO sampleJudge;

    @BeforeEach
    void setUp() {
        sampleJudge = JudgeDTO.builder()
                .id(UUID.randomUUID())
                .firstName("John")
                .lastName("Roberts")
                .fullName("John Roberts")
                .courtName("Supreme Court of the United States")
                .courtType("Supreme Court")
                .judicialStatus("ACTIVE")
                .commissionDate(LocalDate.of(2005, 9, 29))
                .current(true)
                .build();
    }

    @Nested
    @DisplayName("GET /api/judges - List Judges")
    class ListJudgesTests {

        @Test
        @DisplayName("Should return page of judges with default pagination")
        void listJudges_defaultPagination_returnsPage() {
            // Given
            Page<JudgeDTO> page = new PageImpl<>(List.of(sampleJudge));
            when(judgeService.findJudges(any(), any(), any(), any(), any(Pageable.class)))
                    .thenReturn(page);

            // When
            ResponseEntity<Page<JudgeDTO>> response = controller.listJudges(
                    0, 20, null, null, null, null, "lastName", "asc");

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getContent()).hasSize(1);
            assertThat(response.getBody().getContent().get(0).getFullName()).isEqualTo("John Roberts");
        }

        @Test
        @DisplayName("Should filter judges by court level")
        void listJudges_withCourtLevelFilter_filtersCorrectly() {
            // Given
            Page<JudgeDTO> page = new PageImpl<>(List.of(sampleJudge));
            when(judgeService.findJudges(eq("SUPREME"), isNull(), isNull(), isNull(), any(Pageable.class)))
                    .thenReturn(page);

            // When
            ResponseEntity<Page<JudgeDTO>> response = controller.listJudges(
                    0, 20, "SUPREME", null, null, null, "lastName", "asc");

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            verify(judgeService).findJudges(eq("SUPREME"), isNull(), isNull(), isNull(), any(Pageable.class));
        }

        @Test
        @DisplayName("Should filter judges by circuit")
        void listJudges_withCircuitFilter_filtersCorrectly() {
            // Given
            Page<JudgeDTO> page = new PageImpl<>(Collections.emptyList());
            when(judgeService.findJudges(isNull(), eq("9"), isNull(), isNull(), any(Pageable.class)))
                    .thenReturn(page);

            // When
            ResponseEntity<Page<JudgeDTO>> response = controller.listJudges(
                    0, 20, null, "9", null, null, "lastName", "asc");

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            verify(judgeService).findJudges(isNull(), eq("9"), isNull(), isNull(), any(Pageable.class));
        }

        @Test
        @DisplayName("Should filter judges by status")
        void listJudges_withStatusFilter_filtersCorrectly() {
            // Given
            Page<JudgeDTO> page = new PageImpl<>(List.of(sampleJudge));
            when(judgeService.findJudges(isNull(), isNull(), eq("ACTIVE"), isNull(), any(Pageable.class)))
                    .thenReturn(page);

            // When
            ResponseEntity<Page<JudgeDTO>> response = controller.listJudges(
                    0, 20, null, null, "ACTIVE", null, "lastName", "asc");

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            verify(judgeService).findJudges(isNull(), isNull(), eq("ACTIVE"), isNull(), any(Pageable.class));
        }
    }

    @Nested
    @DisplayName("GET /api/judges/{id} - Get Judge by ID")
    class GetJudgeByIdTests {

        @Test
        @DisplayName("Should return judge when found")
        void getJudge_existingId_returnsJudge() {
            // Given
            UUID judgeId = sampleJudge.getId();
            when(judgeService.findById(judgeId)).thenReturn(Optional.of(sampleJudge));

            // When
            ResponseEntity<JudgeDTO> response = controller.getJudge(judgeId);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getFullName()).isEqualTo("John Roberts");
        }

        @Test
        @DisplayName("Should return 404 when judge not found")
        void getJudge_nonExistingId_returns404() {
            // Given
            UUID judgeId = UUID.randomUUID();
            when(judgeService.findById(judgeId)).thenReturn(Optional.empty());

            // When
            ResponseEntity<JudgeDTO> response = controller.getJudge(judgeId);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("GET /api/judges/search - Search Judges")
    class SearchJudgesTests {

        @Test
        @DisplayName("Should return matching judges")
        void searchJudges_withQuery_returnsMatches() {
            // Given
            when(judgeService.searchByName("Roberts")).thenReturn(List.of(sampleJudge));

            // When
            ResponseEntity<List<JudgeDTO>> response = controller.searchJudges("Roberts");

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).hasSize(1);
            assertThat(response.getBody().get(0).getLastName()).isEqualTo("Roberts");
        }

        @Test
        @DisplayName("Should return empty list for no matches")
        void searchJudges_noMatches_returnsEmptyList() {
            // Given
            when(judgeService.searchByName("NonExistent")).thenReturn(Collections.emptyList());

            // When
            ResponseEntity<List<JudgeDTO>> response = controller.searchJudges("NonExistent");

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isEmpty();
        }
    }

    @Nested
    @DisplayName("GET /api/judges/stats - Get Statistics")
    class GetStatisticsTests {

        @Test
        @DisplayName("Should return judge statistics")
        void getStats_returnsStatistics() {
            // Given
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalJudges", 1200);
            stats.put("activeJudges", 870);
            stats.put("seniorJudges", 330);
            when(judgeService.getStatistics()).thenReturn(stats);

            // When
            ResponseEntity<Map<String, Object>> response = controller.getStatistics();

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).containsEntry("totalJudges", 1200);
            assertThat(response.getBody()).containsEntry("activeJudges", 870);
        }
    }

    @Nested
    @DisplayName("POST /api/judges/import/fjc - Import from FJC")
    class ImportFromFjcTests {

        @Test
        @DisplayName("Should successfully import from FJC URL")
        void importFromFjc_success_returnsResult() {
            // Given
            FjcImportResult result = FjcImportResult.builder()
                    .success(true)
                    .totalRecords(1000)
                    .personsCreated(950)
                    .personsUpdated(50)
                    .build();
            // Controller always calls importFromUrl(offset, limit), even with nulls
            when(fjcImportService.importFromUrl(null, null)).thenReturn(result);

            // When
            ResponseEntity<FjcImportResult> response = controller.importFromFjc(null, null);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody().isSuccess()).isTrue();
            assertThat(response.getBody().getTotalRecords()).isEqualTo(1000);
        }

        @Test
        @DisplayName("Should support offset and limit parameters")
        void importFromFjc_withOffsetAndLimit_usesParameters() {
            // Given
            FjcImportResult result = FjcImportResult.builder()
                    .success(true)
                    .totalRecords(100)
                    .build();
            when(fjcImportService.importFromUrl(100, 50)).thenReturn(result);

            // When
            ResponseEntity<FjcImportResult> response = controller.importFromFjc(100, 50);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            verify(fjcImportService).importFromUrl(100, 50);
        }
    }

    @Nested
    @DisplayName("POST /api/judges/import/csv - Import from CSV Upload")
    class ImportFromCsvTests {

        @Test
        @DisplayName("Should successfully import from uploaded CSV")
        void importFromCsv_validFile_returnsResult() throws Exception {
            // Given
            MockMultipartFile file = new MockMultipartFile(
                    "file",
                    "judges.csv",
                    "text/csv",
                    "nid,firstName,lastName\n1,John,Roberts".getBytes()
            );

            FjcImportResult result = FjcImportResult.builder()
                    .success(true)
                    .totalRecords(1)
                    .personsCreated(1)
                    .build();
            when(fjcImportService.importFromStream(any())).thenReturn(result);

            // When
            ResponseEntity<FjcImportResult> response = controller.importFromCsv(file);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody().isSuccess()).isTrue();
            verify(fjcImportService).importFromStream(any());
        }

        @Test
        @DisplayName("Should return 400 for empty file")
        void importFromCsv_emptyFile_returns400() throws Exception {
            // Given
            MockMultipartFile file = new MockMultipartFile(
                    "file",
                    "empty.csv",
                    "text/csv",
                    new byte[0]
            );

            // When
            ResponseEntity<FjcImportResult> response = controller.importFromCsv(file);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            verifyNoInteractions(fjcImportService);
        }
    }
}
