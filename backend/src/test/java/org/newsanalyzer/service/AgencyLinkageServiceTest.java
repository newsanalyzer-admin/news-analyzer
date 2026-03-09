package org.newsanalyzer.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.newsanalyzer.dto.FederalRegisterAgency;
import org.newsanalyzer.dto.LinkageStatistics;
import org.newsanalyzer.model.GovernmentOrganization;
import org.newsanalyzer.model.Regulation;
import org.newsanalyzer.model.RegulationAgency;
import org.newsanalyzer.repository.GovernmentOrganizationRepository;
import org.newsanalyzer.repository.RegulationAgencyRepository;
import org.newsanalyzer.repository.RegulationRepository;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AgencyLinkageService.
 *
 * @author James (Dev Agent)
 * @since 2.0.0
 */
@ExtendWith(MockitoExtension.class)
class AgencyLinkageServiceTest {

    @Mock
    private GovernmentOrganizationRepository govOrgRepository;

    @Mock
    private RegulationAgencyRepository regulationAgencyRepository;

    @Mock
    private RegulationRepository regulationRepository;

    private AgencyLinkageService agencyLinkageService;

    private UUID epaId;
    private UUID dodId;
    private UUID fdaId;

    @BeforeEach
    void setUp() {
        agencyLinkageService = new AgencyLinkageService(
                govOrgRepository,
                regulationAgencyRepository,
                regulationRepository
        );

        // Set configuration properties
        ReflectionTestUtils.setField(agencyLinkageService, "fuzzyThreshold", 0.85);
        ReflectionTestUtils.setField(agencyLinkageService, "fuzzyEnabled", true);

        // Initialize test IDs
        epaId = UUID.randomUUID();
        dodId = UUID.randomUUID();
        fdaId = UUID.randomUUID();

        // Set up mock organizations
        List<GovernmentOrganization> orgs = List.of(
                createOrg(epaId, "Environmental Protection Agency", "EPA", 145),
                createOrg(dodId, "Department of Defense", "DOD", null),
                createOrg(fdaId, "Food and Drug Administration", "FDA", 81)
        );

        when(govOrgRepository.findAll()).thenReturn(orgs);

        // Initialize caches
        agencyLinkageService.refreshCaches();
    }

    private GovernmentOrganization createOrg(UUID id, String name, String acronym, Integer frId) {
        return GovernmentOrganization.builder()
                .id(id)
                .officialName(name)
                .acronym(acronym)
                .federalRegisterAgencyId(frId)
                .build();
    }

    // =========================================================================
    // Exact Name Matching Tests
    // =========================================================================

    @Test
    void testExactNameMatch() {
        FederalRegisterAgency agency = new FederalRegisterAgency();
        agency.setName("Environmental Protection Agency");

        Optional<UUID> result = agencyLinkageService.findGovernmentOrganization(agency);

        assertTrue(result.isPresent());
        assertEquals(epaId, result.get());
    }

    @Test
    void testCaseInsensitiveNameMatch() {
        FederalRegisterAgency agency = new FederalRegisterAgency();
        agency.setName("ENVIRONMENTAL PROTECTION AGENCY");

        Optional<UUID> result = agencyLinkageService.findGovernmentOrganization(agency);

        assertTrue(result.isPresent());
        assertEquals(epaId, result.get());
    }

    @Test
    void testNameMatchWithExtraWhitespace() {
        FederalRegisterAgency agency = new FederalRegisterAgency();
        agency.setName("  Environmental   Protection   Agency  ");

        Optional<UUID> result = agencyLinkageService.findGovernmentOrganization(agency);

        assertTrue(result.isPresent());
        assertEquals(epaId, result.get());
    }

    // =========================================================================
    // Acronym Matching Tests
    // =========================================================================

    @Test
    void testAcronymMatch() {
        FederalRegisterAgency agency = new FederalRegisterAgency();
        agency.setName("Unknown Agency Name");
        agency.setShortName("EPA");

        Optional<UUID> result = agencyLinkageService.findGovernmentOrganization(agency);

        assertTrue(result.isPresent());
        assertEquals(epaId, result.get());
    }

    @Test
    void testAcronymMatchCaseInsensitive() {
        FederalRegisterAgency agency = new FederalRegisterAgency();
        agency.setName("Unknown Agency Name");
        agency.setShortName("epa");

        Optional<UUID> result = agencyLinkageService.findGovernmentOrganization(agency);

        assertTrue(result.isPresent());
        assertEquals(epaId, result.get());
    }

    // =========================================================================
    // Federal Register ID Matching Tests
    // =========================================================================

    @Test
    void testFederalRegisterIdMatch() {
        FederalRegisterAgency agency = new FederalRegisterAgency();
        agency.setId(145);
        agency.setName("Some Unknown Name");

        Optional<UUID> result = agencyLinkageService.findGovernmentOrganization(agency);

        assertTrue(result.isPresent());
        assertEquals(epaId, result.get());
    }

    @Test
    void testFederalRegisterIdTakesPrecedence() {
        // FDA has FR ID 81, but we set the name to EPA - FR ID should win
        FederalRegisterAgency agency = new FederalRegisterAgency();
        agency.setId(81);
        agency.setName("Environmental Protection Agency");

        Optional<UUID> result = agencyLinkageService.findGovernmentOrganization(agency);

        assertTrue(result.isPresent());
        assertEquals(fdaId, result.get()); // Should match FDA by FR ID
    }

    // =========================================================================
    // Manual Mapping Tests
    // =========================================================================

    @Test
    void testManualMappingMatch() {
        // "department of health and human services" maps to "HHS" in KNOWN_NAME_TO_ACRONYM
        // Add HHS to the mock data
        UUID hhsId = UUID.randomUUID();
        List<GovernmentOrganization> orgs = List.of(
                createOrg(epaId, "Environmental Protection Agency", "EPA", 145),
                createOrg(hhsId, "Department of Health and Human Services", "HHS", null)
        );
        when(govOrgRepository.findAll()).thenReturn(orgs);
        agencyLinkageService.refreshCaches();

        FederalRegisterAgency agency = new FederalRegisterAgency();
        agency.setName("Department of Health and Human Services");

        Optional<UUID> result = agencyLinkageService.findGovernmentOrganization(agency);

        assertTrue(result.isPresent());
        assertEquals(hhsId, result.get());
    }

    // =========================================================================
    // Fuzzy Matching Tests
    // =========================================================================

    @Test
    void testFuzzyMatchWithTypo() {
        FederalRegisterAgency agency = new FederalRegisterAgency();
        agency.setName("Enviromental Protection Agency"); // Typo: "Enviromental"

        Optional<UUID> result = agencyLinkageService.findGovernmentOrganization(agency);

        assertTrue(result.isPresent());
        assertEquals(epaId, result.get());
    }

    @Test
    void testFuzzyMatchDisabled() {
        ReflectionTestUtils.setField(agencyLinkageService, "fuzzyEnabled", false);

        FederalRegisterAgency agency = new FederalRegisterAgency();
        agency.setName("Enviromental Protection Agency"); // Typo

        Optional<UUID> result = agencyLinkageService.findGovernmentOrganization(agency);

        assertFalse(result.isPresent());
    }

    @Test
    void testFuzzyMatchBelowThreshold() {
        FederalRegisterAgency agency = new FederalRegisterAgency();
        agency.setName("Something Completely Different");

        Optional<UUID> result = agencyLinkageService.findGovernmentOrganization(agency);

        assertFalse(result.isPresent());
    }

    // =========================================================================
    // Unmatched Handling Tests
    // =========================================================================

    @Test
    void testUnmatchedAgencyTracking() {
        agencyLinkageService.clearUnmatchedAgencies();

        // The unmatched tracking happens in linkRegulationToAgencies
        Regulation regulation = Regulation.builder()
                .id(UUID.randomUUID())
                .documentNumber("2024-12345")
                .build();

        FederalRegisterAgency agency = new FederalRegisterAgency();
        agency.setName("Unknown Agency XYZ");

        // No stubbing needed - unmatched agency won't call existsByRegulationIdAndOrganizationId
        agencyLinkageService.linkRegulationToAgencies(regulation, List.of(agency));

        Set<String> unmatched = agencyLinkageService.getUnmatchedAgencies();
        assertTrue(unmatched.contains("Unknown Agency XYZ"));
    }

    @Test
    void testClearUnmatchedAgencies() {
        // Add an unmatched agency
        Regulation regulation = Regulation.builder()
                .id(UUID.randomUUID())
                .documentNumber("2024-12345")
                .build();

        FederalRegisterAgency agency = new FederalRegisterAgency();
        agency.setName("Unknown Agency ABC");

        agencyLinkageService.linkRegulationToAgencies(regulation, List.of(agency));

        assertFalse(agencyLinkageService.getUnmatchedAgencies().isEmpty());

        agencyLinkageService.clearUnmatchedAgencies();

        assertTrue(agencyLinkageService.getUnmatchedAgencies().isEmpty());
    }

    // =========================================================================
    // Link Regulation to Agencies Tests
    // =========================================================================

    @Test
    void testLinkRegulationToAgenciesSingleAgency() {
        Regulation regulation = Regulation.builder()
                .id(UUID.randomUUID())
                .documentNumber("2024-12345")
                .build();

        FederalRegisterAgency agency = new FederalRegisterAgency();
        agency.setId(145);
        agency.setName("Environmental Protection Agency");

        when(regulationAgencyRepository.existsByRegulationIdAndOrganizationId(any(), any())).thenReturn(false);
        when(regulationAgencyRepository.save(any(RegulationAgency.class))).thenAnswer(i -> i.getArgument(0));

        int linked = agencyLinkageService.linkRegulationToAgencies(regulation, List.of(agency));

        assertEquals(1, linked);
        verify(regulationAgencyRepository).save(argThat(ra ->
                ra.getRegulationId().equals(regulation.getId()) &&
                ra.getOrganizationId().equals(epaId) &&
                ra.isPrimaryAgency()
        ));
    }

    @Test
    void testLinkRegulationToAgenciesMultipleAgencies() {
        Regulation regulation = Regulation.builder()
                .id(UUID.randomUUID())
                .documentNumber("2024-12345")
                .build();

        FederalRegisterAgency agency1 = new FederalRegisterAgency();
        agency1.setId(145);
        agency1.setName("Environmental Protection Agency");

        FederalRegisterAgency agency2 = new FederalRegisterAgency();
        agency2.setId(81);
        agency2.setName("Food and Drug Administration");

        when(regulationAgencyRepository.existsByRegulationIdAndOrganizationId(any(), any())).thenReturn(false);
        when(regulationAgencyRepository.save(any(RegulationAgency.class))).thenAnswer(i -> i.getArgument(0));

        int linked = agencyLinkageService.linkRegulationToAgencies(regulation, List.of(agency1, agency2));

        assertEquals(2, linked);
        verify(regulationAgencyRepository, times(2)).save(any(RegulationAgency.class));
    }

    @Test
    void testLinkRegulationPrimaryAgencyFirst() {
        Regulation regulation = Regulation.builder()
                .id(UUID.randomUUID())
                .documentNumber("2024-12345")
                .build();

        FederalRegisterAgency agency1 = new FederalRegisterAgency();
        agency1.setId(145);
        agency1.setName("EPA");

        FederalRegisterAgency agency2 = new FederalRegisterAgency();
        agency2.setId(81);
        agency2.setName("FDA");

        when(regulationAgencyRepository.existsByRegulationIdAndOrganizationId(any(), any())).thenReturn(false);

        List<RegulationAgency> savedAgencies = new ArrayList<>();
        when(regulationAgencyRepository.save(any(RegulationAgency.class))).thenAnswer(i -> {
            RegulationAgency ra = i.getArgument(0);
            savedAgencies.add(ra);
            return ra;
        });

        agencyLinkageService.linkRegulationToAgencies(regulation, List.of(agency1, agency2));

        assertEquals(2, savedAgencies.size());
        assertTrue(savedAgencies.get(0).isPrimaryAgency()); // First is primary
        assertFalse(savedAgencies.get(1).isPrimaryAgency()); // Second is not
    }

    @Test
    void testLinkRegulationDeletesExistingLinks() {
        Regulation regulation = Regulation.builder()
                .id(UUID.randomUUID())
                .documentNumber("2024-12345")
                .build();

        FederalRegisterAgency agency = new FederalRegisterAgency();
        agency.setId(145);
        agency.setName("EPA");

        when(regulationAgencyRepository.existsByRegulationIdAndOrganizationId(any(), any())).thenReturn(false);
        when(regulationAgencyRepository.save(any(RegulationAgency.class))).thenAnswer(i -> i.getArgument(0));

        agencyLinkageService.linkRegulationToAgencies(regulation, List.of(agency));

        verify(regulationAgencyRepository).deleteByRegulationId(regulation.getId());
    }

    @Test
    void testLinkRegulationEmptyAgencies() {
        Regulation regulation = Regulation.builder()
                .id(UUID.randomUUID())
                .documentNumber("2024-12345")
                .build();

        int linked = agencyLinkageService.linkRegulationToAgencies(regulation, Collections.emptyList());

        assertEquals(0, linked);
        verify(regulationAgencyRepository, never()).save(any());
    }

    @Test
    void testLinkRegulationNullAgencies() {
        Regulation regulation = Regulation.builder()
                .id(UUID.randomUUID())
                .documentNumber("2024-12345")
                .build();

        int linked = agencyLinkageService.linkRegulationToAgencies(regulation, null);

        assertEquals(0, linked);
        verify(regulationAgencyRepository, never()).save(any());
    }

    // =========================================================================
    // Statistics Tests
    // =========================================================================

    @Test
    void testGetStatistics() {
        when(regulationRepository.count()).thenReturn(100L);
        when(regulationAgencyRepository.countDistinctRegulations()).thenReturn(95L);

        LinkageStatistics stats = agencyLinkageService.getStatistics();

        assertEquals(100, stats.getTotalRegulations());
        assertEquals(95, stats.getLinkedRegulations());
        assertEquals(95.0, stats.getLinkageRate(), 0.01);
        assertTrue(stats.meetsTarget(95.0));
    }

    @Test
    void testGetStatisticsEmptyDatabase() {
        when(regulationRepository.count()).thenReturn(0L);
        when(regulationAgencyRepository.countDistinctRegulations()).thenReturn(0L);

        LinkageStatistics stats = agencyLinkageService.getStatistics();

        assertEquals(0, stats.getTotalRegulations());
        assertEquals(0, stats.getLinkedRegulations());
        assertEquals(0.0, stats.getLinkageRate(), 0.01);
    }

    // =========================================================================
    // Cache Tests
    // =========================================================================

    @Test
    void testRefreshCaches() {
        Map<String, Integer> sizes = agencyLinkageService.getCacheSizes();

        assertEquals(3, sizes.get("nameCache")); // 3 orgs
        assertEquals(3, sizes.get("acronymCache")); // 3 acronyms
        assertEquals(2, sizes.get("federalRegisterIdCache")); // 2 with FR IDs (EPA, FDA)
    }

    @Test
    void testCacheIncludesFormerNames() {
        UUID orgId = UUID.randomUUID();
        GovernmentOrganization org = GovernmentOrganization.builder()
                .id(orgId)
                .officialName("Current Name")
                .acronym("CN")
                .formerNames(List.of("Old Name", "Previous Name"))
                .build();

        when(govOrgRepository.findAll()).thenReturn(List.of(org));
        agencyLinkageService.refreshCaches();

        FederalRegisterAgency agency = new FederalRegisterAgency();
        agency.setName("Old Name");

        Optional<UUID> result = agencyLinkageService.findGovernmentOrganization(agency);

        assertTrue(result.isPresent());
        assertEquals(orgId, result.get());
    }

    // =========================================================================
    // Edge Case Tests
    // =========================================================================

    @Test
    void testNullAgency() {
        Optional<UUID> result = agencyLinkageService.findGovernmentOrganization(null);
        assertFalse(result.isPresent());
    }

    @Test
    void testAgencyWithNullName() {
        FederalRegisterAgency agency = new FederalRegisterAgency();
        agency.setName(null);

        Optional<UUID> result = agencyLinkageService.findGovernmentOrganization(agency);
        assertFalse(result.isPresent());
    }

    @Test
    void testAgencyWithEmptyName() {
        FederalRegisterAgency agency = new FederalRegisterAgency();
        agency.setName("");

        Optional<UUID> result = agencyLinkageService.findGovernmentOrganization(agency);
        assertFalse(result.isPresent());
    }
}
