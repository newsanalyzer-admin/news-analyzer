package org.newsanalyzer.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.newsanalyzer.dto.RegulationDTO;
import org.newsanalyzer.model.*;
import org.newsanalyzer.repository.GovernmentOrganizationRepository;
import org.newsanalyzer.repository.RegulationAgencyRepository;
import org.newsanalyzer.repository.RegulationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for RegulationLookupService.
 *
 * @author James (Dev Agent)
 * @since 2.0.0
 */
@ExtendWith(MockitoExtension.class)
class RegulationLookupServiceTest {

    @Mock
    private RegulationRepository regulationRepository;

    @Mock
    private RegulationAgencyRepository regulationAgencyRepository;

    @Mock
    private GovernmentOrganizationRepository governmentOrganizationRepository;

    private RegulationLookupService service;

    // Test data
    private UUID regulationId;
    private UUID orgId;
    private Regulation testRegulation;
    private GovernmentOrganization testOrg;
    private RegulationAgency testAgency;

    @BeforeEach
    void setUp() {
        service = new RegulationLookupService(
                regulationRepository,
                regulationAgencyRepository,
                governmentOrganizationRepository
        );

        regulationId = UUID.randomUUID();
        orgId = UUID.randomUUID();

        testRegulation = Regulation.builder()
                .id(regulationId)
                .documentNumber("2024-12345")
                .title("Test Regulation Title")
                .documentAbstract("Test abstract content")
                .documentType(DocumentType.RULE)
                .publicationDate(LocalDate.of(2024, 3, 15))
                .effectiveOn(LocalDate.of(2024, 5, 15))
                .cfrReferences(List.of(new CfrReference(40, 60, null)))
                .docketIds(List.of("EPA-2024-0001"))
                .sourceUrl("https://federalregister.gov/d/2024-12345")
                .build();

        testOrg = GovernmentOrganization.builder()
                .id(orgId)
                .officialName("Environmental Protection Agency")
                .acronym("EPA")
                .build();

        testAgency = RegulationAgency.builder()
                .regulationId(regulationId)
                .organizationId(orgId)
                .agencyNameRaw("Environmental Protection Agency")
                .primaryAgency(true)
                .build();
    }

    // =====================================================================
    // List Regulations Tests
    // =====================================================================

    @Test
    void listRegulations_returnsPagedResults() {
        // Given
        Page<Regulation> regulationPage = new PageImpl<>(List.of(testRegulation));
        when(regulationRepository.findAll(any(PageRequest.class))).thenReturn(regulationPage);
        when(regulationAgencyRepository.findByRegulationIdIn(any())).thenReturn(List.of(testAgency));
        when(governmentOrganizationRepository.findAllById(any())).thenReturn(List.of(testOrg));

        // When
        Page<RegulationDTO> result = service.listRegulations(0, 20);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getDocumentNumber()).isEqualTo("2024-12345");
    }

    @Test
    void listRegulations_emptyPage_returnsEmptyPage() {
        // Given
        Page<Regulation> emptyPage = Page.empty();
        when(regulationRepository.findAll(any(PageRequest.class))).thenReturn(emptyPage);

        // When
        Page<RegulationDTO> result = service.listRegulations(0, 20);

        // Then
        assertThat(result).isEmpty();
    }

    // =====================================================================
    // Find by Document Number Tests
    // =====================================================================

    @Test
    void findByDocumentNumber_found_returnsDTO() {
        // Given
        when(regulationRepository.findByDocumentNumber("2024-12345")).thenReturn(Optional.of(testRegulation));
        when(regulationAgencyRepository.findByRegulationId(regulationId)).thenReturn(List.of(testAgency));
        when(governmentOrganizationRepository.findAllById(any())).thenReturn(List.of(testOrg));

        // When
        Optional<RegulationDTO> result = service.findByDocumentNumber("2024-12345");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getDocumentNumber()).isEqualTo("2024-12345");
        assertThat(result.get().getTitle()).isEqualTo("Test Regulation Title");
        assertThat(result.get().getDocumentType()).isEqualTo("RULE");
    }

    @Test
    void findByDocumentNumber_notFound_returnsEmpty() {
        // Given
        when(regulationRepository.findByDocumentNumber("invalid")).thenReturn(Optional.empty());

        // When
        Optional<RegulationDTO> result = service.findByDocumentNumber("invalid");

        // Then
        assertThat(result).isEmpty();
    }

    // =====================================================================
    // Search Tests
    // =====================================================================

    @Test
    void searchRegulations_validQuery_returnsResults() {
        // Given
        Page<Regulation> regulationPage = new PageImpl<>(List.of(testRegulation));
        when(regulationRepository.searchByTitleOrAbstract(eq("emissions"), any(PageRequest.class)))
                .thenReturn(regulationPage);
        when(regulationAgencyRepository.findByRegulationIdIn(any())).thenReturn(List.of(testAgency));
        when(governmentOrganizationRepository.findAllById(any())).thenReturn(List.of(testOrg));

        // When
        Page<RegulationDTO> result = service.searchRegulations("emissions", 0, 20);

        // Then
        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void searchRegulations_blankQuery_returnsEmpty() {
        // When
        Page<RegulationDTO> result = service.searchRegulations("  ", 0, 20);

        // Then
        assertThat(result).isEmpty();
        verifyNoInteractions(regulationRepository);
    }

    @Test
    void searchRegulations_nullQuery_returnsEmpty() {
        // When
        Page<RegulationDTO> result = service.searchRegulations(null, 0, 20);

        // Then
        assertThat(result).isEmpty();
    }

    // =====================================================================
    // Agency Filter Tests
    // =====================================================================

    @Test
    void findByAgency_validOrgId_returnsResults() {
        // Given
        Page<Regulation> regulationPage = new PageImpl<>(List.of(testRegulation));
        when(regulationRepository.findByAgencyId(eq(orgId), any(PageRequest.class))).thenReturn(regulationPage);
        when(regulationAgencyRepository.findByRegulationIdIn(any())).thenReturn(List.of(testAgency));
        when(governmentOrganizationRepository.findAllById(any())).thenReturn(List.of(testOrg));

        // When
        Page<RegulationDTO> result = service.findByAgency(orgId, 0, 20);

        // Then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getAgencies()).hasSize(1);
        assertThat(result.getContent().get(0).getAgencies().get(0).getName())
                .isEqualTo("Environmental Protection Agency");
    }

    // =====================================================================
    // Document Type Filter Tests
    // =====================================================================

    @Test
    void findByDocumentType_validType_returnsResults() {
        // Given
        Page<Regulation> regulationPage = new PageImpl<>(List.of(testRegulation));
        when(regulationRepository.findByDocumentType(eq(DocumentType.RULE), any(PageRequest.class)))
                .thenReturn(regulationPage);
        when(regulationAgencyRepository.findByRegulationIdIn(any())).thenReturn(List.of(testAgency));
        when(governmentOrganizationRepository.findAllById(any())).thenReturn(List.of(testOrg));

        // When
        Page<RegulationDTO> result = service.findByDocumentType(DocumentType.RULE, 0, 20);

        // Then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getDocumentType()).isEqualTo("RULE");
    }

    // =====================================================================
    // Date Range Filter Tests
    // =====================================================================

    @Test
    void findByDateRange_validRange_returnsResults() {
        // Given
        LocalDate start = LocalDate.of(2024, 1, 1);
        LocalDate end = LocalDate.of(2024, 12, 31);
        Page<Regulation> regulationPage = new PageImpl<>(List.of(testRegulation));
        when(regulationRepository.findByPublicationDateBetween(eq(start), eq(end), any(PageRequest.class)))
                .thenReturn(regulationPage);
        when(regulationAgencyRepository.findByRegulationIdIn(any())).thenReturn(List.of(testAgency));
        when(governmentOrganizationRepository.findAllById(any())).thenReturn(List.of(testOrg));

        // When
        Page<RegulationDTO> result = service.findByDateRange(start, end, 0, 20);

        // Then
        assertThat(result.getContent()).hasSize(1);
    }

    // =====================================================================
    // Effective Date Tests
    // =====================================================================

    @Test
    void findRulesEffectiveOn_validDate_returnsRules() {
        // Given
        LocalDate date = LocalDate.of(2024, 6, 1);
        when(regulationRepository.findRulesEffectiveOnOrBefore(date)).thenReturn(List.of(testRegulation));
        when(regulationAgencyRepository.findByRegulationIdIn(any())).thenReturn(List.of(testAgency));
        when(governmentOrganizationRepository.findAllById(any())).thenReturn(List.of(testOrg));

        // When
        List<RegulationDTO> result = service.findRulesEffectiveOn(date);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getEffectiveOn()).isEqualTo(LocalDate.of(2024, 5, 15));
    }

    // =====================================================================
    // CFR Reference Tests
    // =====================================================================

    @Test
    void findByCfrReference_validCitation_returnsResults() {
        // Given
        when(regulationRepository.findByCfrReference("{\"title\": 40, \"part\": 60}"))
                .thenReturn(List.of(testRegulation));
        when(regulationAgencyRepository.findByRegulationIdIn(any())).thenReturn(List.of(testAgency));
        when(governmentOrganizationRepository.findAllById(any())).thenReturn(List.of(testOrg));

        // When
        List<RegulationDTO> result = service.findByCfrReference(40, 60);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCfrReferences()).hasSize(1);
        assertThat(result.get(0).getCfrReferences().get(0).getTitle()).isEqualTo(40);
    }

    // =====================================================================
    // DTO Mapping Tests
    // =====================================================================

    @Test
    void toDTO_includesAgencyInfo() {
        // Given
        when(regulationRepository.findByDocumentNumber("2024-12345")).thenReturn(Optional.of(testRegulation));
        when(regulationAgencyRepository.findByRegulationId(regulationId)).thenReturn(List.of(testAgency));
        when(governmentOrganizationRepository.findAllById(any())).thenReturn(List.of(testOrg));

        // When
        Optional<RegulationDTO> result = service.findByDocumentNumber("2024-12345");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getAgencies()).hasSize(1);
        RegulationDTO.AgencyDTO agency = result.get().getAgencies().get(0);
        assertThat(agency.getId()).isEqualTo(orgId);
        assertThat(agency.getName()).isEqualTo("Environmental Protection Agency");
        assertThat(agency.getAcronym()).isEqualTo("EPA");
        assertThat(agency.isPrimary()).isTrue();
    }

    @Test
    void toDTO_includesCfrReferences() {
        // Given
        when(regulationRepository.findByDocumentNumber("2024-12345")).thenReturn(Optional.of(testRegulation));
        when(regulationAgencyRepository.findByRegulationId(regulationId)).thenReturn(List.of());
        when(governmentOrganizationRepository.findAllById(any())).thenReturn(List.of());

        // When
        Optional<RegulationDTO> result = service.findByDocumentNumber("2024-12345");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getCfrReferences()).hasSize(1);
        RegulationDTO.CfrReferenceDTO cfr = result.get().getCfrReferences().get(0);
        assertThat(cfr.getTitle()).isEqualTo(40);
        assertThat(cfr.getPart()).isEqualTo(60);
        assertThat(cfr.getFullCitation()).isEqualTo("40 CFR 60");
    }

    @Test
    void toDTO_handlesNullCfrReferences() {
        // Given
        testRegulation.setCfrReferences(null);
        when(regulationRepository.findByDocumentNumber("2024-12345")).thenReturn(Optional.of(testRegulation));
        when(regulationAgencyRepository.findByRegulationId(regulationId)).thenReturn(List.of());
        when(governmentOrganizationRepository.findAllById(any())).thenReturn(List.of());

        // When
        Optional<RegulationDTO> result = service.findByDocumentNumber("2024-12345");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getCfrReferences()).isNull();
    }

    @Test
    void batchFetch_avoidsNPlusOne() {
        // Given - multiple regulations
        UUID regId2 = UUID.randomUUID();
        Regulation reg2 = Regulation.builder()
                .id(regId2)
                .documentNumber("2024-12346")
                .title("Second Regulation")
                .documentType(DocumentType.NOTICE)
                .publicationDate(LocalDate.of(2024, 3, 16))
                .build();

        RegulationAgency agency2 = RegulationAgency.builder()
                .regulationId(regId2)
                .organizationId(orgId)
                .agencyNameRaw("EPA")
                .primaryAgency(true)
                .build();

        Page<Regulation> regulationPage = new PageImpl<>(List.of(testRegulation, reg2));
        when(regulationRepository.findAll(any(PageRequest.class))).thenReturn(regulationPage);
        when(regulationAgencyRepository.findByRegulationIdIn(any())).thenReturn(List.of(testAgency, agency2));
        when(governmentOrganizationRepository.findAllById(any())).thenReturn(List.of(testOrg));

        // When
        Page<RegulationDTO> result = service.listRegulations(0, 20);

        // Then
        assertThat(result.getContent()).hasSize(2);
        // Verify batch fetch was used (only 1 call to findByRegulationIdIn, not N calls to findByRegulationId)
        verify(regulationAgencyRepository, times(1)).findByRegulationIdIn(any());
        verify(regulationAgencyRepository, never()).findByRegulationId(any());
    }
}
