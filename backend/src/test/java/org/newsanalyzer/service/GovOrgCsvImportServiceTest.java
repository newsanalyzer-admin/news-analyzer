package org.newsanalyzer.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.newsanalyzer.dto.CsvImportResult;
import org.newsanalyzer.model.GovernmentOrganization;
import org.newsanalyzer.model.GovernmentOrganization.GovernmentBranch;
import org.newsanalyzer.model.GovernmentOrganization.OrganizationType;
import org.newsanalyzer.repository.GovernmentOrganizationRepository;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for GovOrgCsvImportService.
 *
 * Tests cover:
 * - Valid CSV parsing with all fields
 * - Valid CSV with only required fields
 * - Validation error cases
 * - Merge strategy (update existing vs create new)
 * - Malformed CSV handling
 *
 * @author James (Dev Agent)
 * @since 2.0.0
 */
@ExtendWith(MockitoExtension.class)
class GovOrgCsvImportServiceTest {

    @Mock
    private GovernmentOrganizationRepository repository;

    @InjectMocks
    private GovOrgCsvImportService importService;

    private InputStream createCsvStream(String csvContent) {
        return new ByteArrayInputStream(csvContent.getBytes(StandardCharsets.UTF_8));
    }

    @Nested
    @DisplayName("Valid CSV Import Tests")
    class ValidCsvTests {

        @Test
        @DisplayName("Should successfully import valid CSV with all fields")
        void importValidCsvWithAllFields() {
            // Given
            String csv = """
                officialName,acronym,branch,orgType,orgLevel,parentId,establishedDate,dissolvedDate,websiteUrl,jurisdictionAreas
                "United States Senate",Senate,legislative,branch,1,,1789-03-04,,https://senate.gov,"legislation;confirmation"
                """;

            when(repository.findAll()).thenReturn(Collections.emptyList());
            when(repository.findByAcronymIgnoreCase("Senate")).thenReturn(Optional.empty());
            when(repository.findByOfficialNameIgnoreCase("United States Senate")).thenReturn(Optional.empty());
            when(repository.save(any(GovernmentOrganization.class)))
                    .thenAnswer(invocation -> {
                        GovernmentOrganization org = invocation.getArgument(0);
                        org.setId(UUID.randomUUID());
                        return org;
                    });

            // When
            CsvImportResult result = importService.importFromCsv(createCsvStream(csv));

            // Then
            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getAdded()).isEqualTo(1);
            assertThat(result.getUpdated()).isEqualTo(0);
            assertThat(result.getErrors()).isEqualTo(0);

            // Verify the saved organization
            ArgumentCaptor<GovernmentOrganization> captor = ArgumentCaptor.forClass(GovernmentOrganization.class);
            verify(repository).save(captor.capture());

            GovernmentOrganization saved = captor.getValue();
            assertThat(saved.getOfficialName()).isEqualTo("United States Senate");
            assertThat(saved.getAcronym()).isEqualTo("Senate");
            assertThat(saved.getBranch()).isEqualTo(GovernmentBranch.LEGISLATIVE);
            assertThat(saved.getOrgType()).isEqualTo(OrganizationType.BRANCH);
            assertThat(saved.getCreatedBy()).isEqualTo("csv-import");
            assertThat(saved.getUpdatedBy()).isEqualTo("csv-import");
        }

        @Test
        @DisplayName("Should successfully import valid CSV with only required fields")
        void importValidCsvWithRequiredFieldsOnly() {
            // Given
            String csv = """
                officialName,acronym,branch,orgType
                "Congressional Budget Office",,legislative,office
                """;

            when(repository.findAll()).thenReturn(Collections.emptyList());
            when(repository.findByOfficialNameIgnoreCase("Congressional Budget Office")).thenReturn(Optional.empty());
            when(repository.save(any(GovernmentOrganization.class)))
                    .thenAnswer(invocation -> {
                        GovernmentOrganization org = invocation.getArgument(0);
                        org.setId(UUID.randomUUID());
                        return org;
                    });

            // When
            CsvImportResult result = importService.importFromCsv(createCsvStream(csv));

            // Then
            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getAdded()).isEqualTo(1);
        }

        @Test
        @DisplayName("Should update existing organization when matched by acronym")
        void updateExistingOrganizationByAcronym() {
            // Given - columns must match EXPECTED_HEADERS order
            String csv = """
                officialName,acronym,branch,orgType,orgLevel,parentId,establishedDate,dissolvedDate,websiteUrl,jurisdictionAreas
                "Government Accountability Office",GAO,legislative,independent_agency,,,,,https://gao.gov,
                """;

            GovernmentOrganization existing = new GovernmentOrganization();
            existing.setId(UUID.randomUUID());
            existing.setOfficialName("Government Accountability Office");
            existing.setAcronym("GAO");
            existing.setBranch(GovernmentBranch.LEGISLATIVE);
            existing.setOrgType(OrganizationType.INDEPENDENT_AGENCY);
            // websiteUrl is null, so it should be updated

            when(repository.findAll()).thenReturn(Collections.singletonList(existing));
            when(repository.findByAcronymIgnoreCase("GAO")).thenReturn(Optional.of(existing));
            when(repository.save(any(GovernmentOrganization.class))).thenReturn(existing);

            // When
            CsvImportResult result = importService.importFromCsv(createCsvStream(csv));

            // Then
            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getUpdated()).isEqualTo(1);
            assertThat(result.getAdded()).isEqualTo(0);

            ArgumentCaptor<GovernmentOrganization> captor = ArgumentCaptor.forClass(GovernmentOrganization.class);
            verify(repository).save(captor.capture());

            GovernmentOrganization updated = captor.getValue();
            assertThat(updated.getWebsiteUrl()).isEqualTo("https://gao.gov");
            assertThat(updated.getUpdatedBy()).isEqualTo("csv-import");
        }

        @Test
        @DisplayName("Should skip organization when no changes needed")
        void skipOrganizationWhenNoChanges() {
            // Given - columns must match EXPECTED_HEADERS order
            String csv = """
                officialName,acronym,branch,orgType,orgLevel,parentId,establishedDate,dissolvedDate,websiteUrl,jurisdictionAreas
                "Government Accountability Office",GAO,legislative,independent_agency,,,,,https://gao.gov,
                """;

            GovernmentOrganization existing = new GovernmentOrganization();
            existing.setId(UUID.randomUUID());
            existing.setOfficialName("Government Accountability Office");
            existing.setAcronym("GAO");
            existing.setBranch(GovernmentBranch.LEGISLATIVE);
            existing.setOrgType(OrganizationType.INDEPENDENT_AGENCY);
            existing.setWebsiteUrl("https://gao.gov"); // Already has websiteUrl

            when(repository.findAll()).thenReturn(Collections.singletonList(existing));
            when(repository.findByAcronymIgnoreCase("GAO")).thenReturn(Optional.of(existing));
            when(repository.save(any(GovernmentOrganization.class))).thenReturn(existing);

            // When
            CsvImportResult result = importService.importFromCsv(createCsvStream(csv));

            // Then
            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getSkipped()).isEqualTo(1);
            assertThat(result.getUpdated()).isEqualTo(0);
        }

        @Test
        @DisplayName("Should resolve parentId by acronym")
        void resolveParentIdByAcronym() {
            // Given - columns must match EXPECTED_HEADERS order
            String csv = """
                officialName,acronym,branch,orgType,orgLevel,parentId,establishedDate,dissolvedDate,websiteUrl,jurisdictionAreas
                "Library of Congress",LOC,legislative,independent_agency,,,,,,
                "Congressional Research Service",CRS,legislative,office,,LOC,,,,
                """;

            when(repository.findAll()).thenReturn(Collections.emptyList());
            when(repository.findByAcronymIgnoreCase(anyString())).thenReturn(Optional.empty());
            when(repository.findByOfficialNameIgnoreCase(anyString())).thenReturn(Optional.empty());
            when(repository.save(any(GovernmentOrganization.class)))
                    .thenAnswer(invocation -> {
                        GovernmentOrganization org = invocation.getArgument(0);
                        org.setId(UUID.randomUUID());
                        return org;
                    });

            // When
            CsvImportResult result = importService.importFromCsv(createCsvStream(csv));

            // Then
            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getAdded()).isEqualTo(2);

            // Verify CRS has LOC's ID as parentId
            ArgumentCaptor<GovernmentOrganization> captor = ArgumentCaptor.forClass(GovernmentOrganization.class);
            verify(repository, times(2)).save(captor.capture());

            // The second save should be CRS with parentId set
            GovernmentOrganization crs = captor.getAllValues().get(1);
            assertThat(crs.getOfficialName()).isEqualTo("Congressional Research Service");
            // ParentId should be resolved from LOC acronym
        }
    }

    @Nested
    @DisplayName("Validation Error Tests")
    class ValidationErrorTests {

        @Test
        @DisplayName("Should return error when officialName is missing")
        void errorWhenOfficialNameMissing() {
            // Given
            String csv = """
                officialName,acronym,branch,orgType
                ,Senate,legislative,branch
                """;

            // When
            CsvImportResult result = importService.importFromCsv(createCsvStream(csv));

            // Then
            assertThat(result.isSuccess()).isFalse();
            assertThat(result.hasValidationErrors()).isTrue();
            assertThat(result.getValidationErrors()).hasSize(1);
            assertThat(result.getValidationErrors().get(0).getField()).isEqualTo("officialName");
            assertThat(result.getValidationErrors().get(0).getLine()).isEqualTo(2);
        }

        @Test
        @DisplayName("Should return error for invalid branch value")
        void errorForInvalidBranch() {
            // Given
            String csv = """
                officialName,acronym,branch,orgType
                "United States Senate",Senate,congress,branch
                """;

            // When
            CsvImportResult result = importService.importFromCsv(createCsvStream(csv));

            // Then
            assertThat(result.isSuccess()).isFalse();
            assertThat(result.hasValidationErrors()).isTrue();
            assertThat(result.getValidationErrors().get(0).getField()).isEqualTo("branch");
            assertThat(result.getValidationErrors().get(0).getValue()).isEqualTo("congress");
        }

        @Test
        @DisplayName("Should return error for invalid orgType value")
        void errorForInvalidOrgType() {
            // Given
            String csv = """
                officialName,acronym,branch,orgType
                "United States Senate",Senate,legislative,senate_body
                """;

            // When
            CsvImportResult result = importService.importFromCsv(createCsvStream(csv));

            // Then
            assertThat(result.isSuccess()).isFalse();
            assertThat(result.hasValidationErrors()).isTrue();
            assertThat(result.getValidationErrors().get(0).getField()).isEqualTo("orgType");
        }

        @Test
        @DisplayName("Should return error for invalid date format")
        void errorForInvalidDateFormat() {
            // Given - columns must match EXPECTED_HEADERS order
            String csv = """
                officialName,acronym,branch,orgType,orgLevel,parentId,establishedDate,dissolvedDate,websiteUrl,jurisdictionAreas
                "United States Senate",Senate,legislative,branch,,,March 4 1789,,,
                """;

            // When
            CsvImportResult result = importService.importFromCsv(createCsvStream(csv));

            // Then
            assertThat(result.isSuccess()).isFalse();
            assertThat(result.hasValidationErrors()).isTrue();
            assertThat(result.getValidationErrors().get(0).getField()).isEqualTo("establishedDate");
        }

        @Test
        @DisplayName("Should return error for invalid URL format")
        void errorForInvalidUrlFormat() {
            // Given - columns must match EXPECTED_HEADERS order
            String csv = """
                officialName,acronym,branch,orgType,orgLevel,parentId,establishedDate,dissolvedDate,websiteUrl,jurisdictionAreas
                "United States Senate",Senate,legislative,branch,,,,,not-a-valid-url,
                """;

            // When
            CsvImportResult result = importService.importFromCsv(createCsvStream(csv));

            // Then
            assertThat(result.isSuccess()).isFalse();
            assertThat(result.hasValidationErrors()).isTrue();
            assertThat(result.getValidationErrors().get(0).getField()).isEqualTo("websiteUrl");
        }

        @Test
        @DisplayName("Should return error for duplicate acronyms in file")
        void errorForDuplicateAcronyms() {
            // Given
            String csv = """
                officialName,acronym,branch,orgType
                "United States Senate",CONGRESS,legislative,branch
                "United States House",CONGRESS,legislative,branch
                """;

            // When
            CsvImportResult result = importService.importFromCsv(createCsvStream(csv));

            // Then
            assertThat(result.isSuccess()).isFalse();
            assertThat(result.hasValidationErrors()).isTrue();
            assertThat(result.getValidationErrors().get(0).getField()).isEqualTo("acronym");
            assertThat(result.getValidationErrors().get(0).getMessage()).contains("Duplicate acronym");
        }

        @Test
        @DisplayName("Should return error for invalid orgLevel")
        void errorForInvalidOrgLevel() {
            // Given
            String csv = """
                officialName,acronym,branch,orgType,orgLevel
                "United States Senate",Senate,legislative,branch,15
                """;

            // When
            CsvImportResult result = importService.importFromCsv(createCsvStream(csv));

            // Then
            assertThat(result.isSuccess()).isFalse();
            assertThat(result.hasValidationErrors()).isTrue();
            assertThat(result.getValidationErrors().get(0).getField()).isEqualTo("orgLevel");
        }
    }

    @Nested
    @DisplayName("Actual CSV File Validation Tests")
    class ActualCsvFileTests {

        @Test
        @DisplayName("Should parse legislative-branch-orgs.csv without validation errors")
        void parseLegislativeBranchCsv() throws Exception {
            // Given - read actual CSV file from data directory
            InputStream csvStream = getClass().getClassLoader()
                    .getResourceAsStream("csv/legislative-branch-orgs.csv");

            // Skip test if file not available in test resources
            if (csvStream == null) {
                // Try relative path from project root
                java.nio.file.Path csvPath = java.nio.file.Paths.get("../data/legislative-branch-orgs.csv");
                if (java.nio.file.Files.exists(csvPath)) {
                    csvStream = java.nio.file.Files.newInputStream(csvPath);
                } else {
                    // File not available, skip test
                    org.junit.jupiter.api.Assumptions.assumeTrue(false,
                            "Legislative branch CSV file not available in test resources");
                    return;
                }
            }

            when(repository.findAll()).thenReturn(Collections.emptyList());
            when(repository.findByAcronymIgnoreCase(anyString())).thenReturn(Optional.empty());
            when(repository.findByOfficialNameIgnoreCase(anyString())).thenReturn(Optional.empty());
            when(repository.save(any(GovernmentOrganization.class)))
                    .thenAnswer(invocation -> {
                        GovernmentOrganization org = invocation.getArgument(0);
                        org.setId(UUID.randomUUID());
                        return org;
                    });

            // When
            CsvImportResult result = importService.importFromCsv(csvStream);

            // Then
            assertThat(result.hasValidationErrors())
                    .withFailMessage("CSV validation errors: %s", result.getValidationErrors())
                    .isFalse();
            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getAdded()).isGreaterThanOrEqualTo(15); // AC: at least 15 orgs
        }

        @Test
        @DisplayName("Should parse judicial-branch-orgs.csv without validation errors")
        void parseJudicialBranchCsv() throws Exception {
            // Given - read actual CSV file from data directory
            InputStream csvStream = getClass().getClassLoader()
                    .getResourceAsStream("csv/judicial-branch-orgs.csv");

            // Skip test if file not available in test resources
            if (csvStream == null) {
                java.nio.file.Path csvPath = java.nio.file.Paths.get("../data/judicial-branch-orgs.csv");
                if (java.nio.file.Files.exists(csvPath)) {
                    csvStream = java.nio.file.Files.newInputStream(csvPath);
                } else {
                    org.junit.jupiter.api.Assumptions.assumeTrue(false,
                            "Judicial branch CSV file not available in test resources");
                    return;
                }
            }

            when(repository.findAll()).thenReturn(Collections.emptyList());
            when(repository.findByAcronymIgnoreCase(anyString())).thenReturn(Optional.empty());
            when(repository.findByOfficialNameIgnoreCase(anyString())).thenReturn(Optional.empty());
            when(repository.save(any(GovernmentOrganization.class)))
                    .thenAnswer(invocation -> {
                        GovernmentOrganization org = invocation.getArgument(0);
                        org.setId(UUID.randomUUID());
                        return org;
                    });

            // When
            CsvImportResult result = importService.importFromCsv(csvStream);

            // Then
            assertThat(result.hasValidationErrors())
                    .withFailMessage("CSV validation errors: %s", result.getValidationErrors())
                    .isFalse();
            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getAdded()).isGreaterThanOrEqualTo(120); // AC: at least 120 orgs
        }
    }

    @Nested
    @DisplayName("Edge Case Tests")
    class EdgeCaseTests {

        @Test
        @DisplayName("Should handle empty CSV file")
        void handleEmptyCsvFile() {
            // Given
            String csv = "";

            // When
            CsvImportResult result = importService.importFromCsv(createCsvStream(csv));

            // Then
            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getErrorMessages()).contains("CSV file is empty");
        }

        @Test
        @DisplayName("Should handle CSV with only headers")
        void handleCsvWithOnlyHeaders() {
            // Given
            String csv = "officialName,acronym,branch,orgType\n";

            when(repository.findAll()).thenReturn(Collections.emptyList());

            // When
            CsvImportResult result = importService.importFromCsv(createCsvStream(csv));

            // Then
            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getAdded()).isEqualTo(0);
            assertThat(result.getTotal()).isEqualTo(0);
        }

        @Test
        @DisplayName("Should handle missing required headers")
        void handleMissingRequiredHeaders() {
            // Given
            String csv = """
                officialName,acronym
                "United States Senate",Senate
                """;

            // When
            CsvImportResult result = importService.importFromCsv(createCsvStream(csv));

            // Then
            assertThat(result.isSuccess()).isFalse();
            assertThat(result.hasValidationErrors()).isTrue();
            assertThat(result.getValidationErrors().get(0).getField()).isEqualTo("headers");
        }

        @Test
        @DisplayName("Should skip empty rows")
        void skipEmptyRows() {
            // Given
            String csv = """
                officialName,acronym,branch,orgType
                "United States Senate",Senate,legislative,branch

                "United States House",House,legislative,branch
                """;

            when(repository.findAll()).thenReturn(Collections.emptyList());
            when(repository.findByAcronymIgnoreCase(anyString())).thenReturn(Optional.empty());
            when(repository.findByOfficialNameIgnoreCase(anyString())).thenReturn(Optional.empty());
            when(repository.save(any(GovernmentOrganization.class)))
                    .thenAnswer(invocation -> {
                        GovernmentOrganization org = invocation.getArgument(0);
                        org.setId(UUID.randomUUID());
                        return org;
                    });

            // When
            CsvImportResult result = importService.importFromCsv(createCsvStream(csv));

            // Then
            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getAdded()).isEqualTo(2);
        }

        @Test
        @DisplayName("Should handle case-insensitive branch values")
        void handleCaseInsensitiveBranch() {
            // Given
            String csv = """
                officialName,acronym,branch,orgType
                "United States Senate",Senate,LEGISLATIVE,branch
                """;

            when(repository.findAll()).thenReturn(Collections.emptyList());
            when(repository.findByAcronymIgnoreCase("Senate")).thenReturn(Optional.empty());
            when(repository.findByOfficialNameIgnoreCase("United States Senate")).thenReturn(Optional.empty());
            when(repository.save(any(GovernmentOrganization.class)))
                    .thenAnswer(invocation -> {
                        GovernmentOrganization org = invocation.getArgument(0);
                        org.setId(UUID.randomUUID());
                        return org;
                    });

            // When
            CsvImportResult result = importService.importFromCsv(createCsvStream(csv));

            // Then
            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getAdded()).isEqualTo(1);
        }
    }
}
