package org.newsanalyzer.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Regulation model and related classes.
 *
 * @author James (Dev Agent)
 * @since 2.0.0
 */
class RegulationTest {

    private Regulation regulation;

    @BeforeEach
    void setUp() {
        regulation = new Regulation();
    }

    // =====================================================================
    // Regulation Entity Tests
    // =====================================================================

    @Test
    void testRegulationCreation() {
        regulation.setDocumentNumber("2024-12345");
        regulation.setTitle("Air Quality Standards for Fine Particulate Matter");
        regulation.setDocumentType(DocumentType.RULE);
        regulation.setPublicationDate(LocalDate.of(2024, 3, 15));

        assertEquals("2024-12345", regulation.getDocumentNumber());
        assertEquals("Air Quality Standards for Fine Particulate Matter", regulation.getTitle());
        assertEquals(DocumentType.RULE, regulation.getDocumentType());
        assertEquals(LocalDate.of(2024, 3, 15), regulation.getPublicationDate());
    }

    @Test
    void testRegulationBuilder() {
        Regulation reg = Regulation.builder()
                .documentNumber("2024-67890")
                .title("Test Regulation")
                .documentAbstract("This is a test abstract")
                .documentType(DocumentType.PROPOSED_RULE)
                .publicationDate(LocalDate.of(2024, 6, 1))
                .effectiveOn(LocalDate.of(2024, 9, 1))
                .regulationIdNumber("1234-AB56")
                .sourceUrl("https://federalregister.gov/d/2024-67890")
                .build();

        assertEquals("2024-67890", reg.getDocumentNumber());
        assertEquals("Test Regulation", reg.getTitle());
        assertEquals("This is a test abstract", reg.getDocumentAbstract());
        assertEquals(DocumentType.PROPOSED_RULE, reg.getDocumentType());
        assertEquals(LocalDate.of(2024, 6, 1), reg.getPublicationDate());
        assertEquals(LocalDate.of(2024, 9, 1), reg.getEffectiveOn());
        assertEquals("1234-AB56", reg.getRegulationIdNumber());
        assertEquals("https://federalregister.gov/d/2024-67890", reg.getSourceUrl());
    }

    @Test
    void testRegulationWithCfrReferences() {
        CfrReference cfr1 = new CfrReference(40, 60, null);
        CfrReference cfr2 = new CfrReference(40, 61, "5");

        regulation.setCfrReferences(Arrays.asList(cfr1, cfr2));

        assertNotNull(regulation.getCfrReferences());
        assertEquals(2, regulation.getCfrReferences().size());
        assertEquals(40, regulation.getCfrReferences().get(0).getTitle());
        assertEquals(60, regulation.getCfrReferences().get(0).getPart());
    }

    @Test
    void testRegulationWithDocketIds() {
        List<String> docketIds = Arrays.asList("EPA-HQ-OAR-2023-0001", "EPA-HQ-OAR-2023-0002");
        regulation.setDocketIds(docketIds);

        assertNotNull(regulation.getDocketIds());
        assertEquals(2, regulation.getDocketIds().size());
        assertEquals("EPA-HQ-OAR-2023-0001", regulation.getDocketIds().get(0));
    }

    @Test
    void testRegulationNullableFields() {
        regulation.setDocumentNumber("2024-00001");
        regulation.setTitle("Test");
        regulation.setDocumentType(DocumentType.NOTICE);
        regulation.setPublicationDate(LocalDate.now());

        // These fields should be nullable
        assertNull(regulation.getEffectiveOn());
        assertNull(regulation.getSigningDate());
        assertNull(regulation.getDocumentAbstract());
        assertNull(regulation.getRegulationIdNumber());
        assertNull(regulation.getCfrReferences());
        assertNull(regulation.getDocketIds());
    }

    @Test
    void testRegulationUrls() {
        regulation.setSourceUrl("https://federalregister.gov/d/2024-12345");
        regulation.setPdfUrl("https://federalregister.gov/pdf/2024-12345.pdf");
        regulation.setHtmlUrl("https://federalregister.gov/html/2024-12345.html");

        assertEquals("https://federalregister.gov/d/2024-12345", regulation.getSourceUrl());
        assertEquals("https://federalregister.gov/pdf/2024-12345.pdf", regulation.getPdfUrl());
        assertEquals("https://federalregister.gov/html/2024-12345.html", regulation.getHtmlUrl());
    }

    // =====================================================================
    // DocumentType Enum Tests
    // =====================================================================

    @Test
    void testDocumentTypeValues() {
        assertEquals(5, DocumentType.values().length);

        assertEquals("Final Rule", DocumentType.RULE.getDisplayName());
        assertEquals("Proposed Rule", DocumentType.PROPOSED_RULE.getDisplayName());
        assertEquals("Notice", DocumentType.NOTICE.getDisplayName());
        assertEquals("Presidential Document", DocumentType.PRESIDENTIAL_DOCUMENT.getDisplayName());
        assertEquals("Other", DocumentType.OTHER.getDisplayName());
    }

    @Test
    void testDocumentTypeDescriptions() {
        assertNotNull(DocumentType.RULE.getDescription());
        assertNotNull(DocumentType.PROPOSED_RULE.getDescription());
        assertNotNull(DocumentType.NOTICE.getDescription());
        assertNotNull(DocumentType.PRESIDENTIAL_DOCUMENT.getDescription());
        assertNotNull(DocumentType.OTHER.getDescription());
    }

    @Test
    void testDocumentTypeFromFederalRegisterType() {
        assertEquals(DocumentType.RULE, DocumentType.fromFederalRegisterType("Rule"));
        assertEquals(DocumentType.RULE, DocumentType.fromFederalRegisterType("RULE"));
        assertEquals(DocumentType.RULE, DocumentType.fromFederalRegisterType("rule"));

        assertEquals(DocumentType.PROPOSED_RULE, DocumentType.fromFederalRegisterType("Proposed Rule"));
        assertEquals(DocumentType.PROPOSED_RULE, DocumentType.fromFederalRegisterType("proposed rule"));

        assertEquals(DocumentType.NOTICE, DocumentType.fromFederalRegisterType("Notice"));
        assertEquals(DocumentType.PRESIDENTIAL_DOCUMENT, DocumentType.fromFederalRegisterType("Presidential Document"));

        assertEquals(DocumentType.OTHER, DocumentType.fromFederalRegisterType("Unknown"));
        assertEquals(DocumentType.OTHER, DocumentType.fromFederalRegisterType(null));
        assertEquals(DocumentType.OTHER, DocumentType.fromFederalRegisterType(""));
    }

    // =====================================================================
    // CfrReference Tests
    // =====================================================================

    @Test
    void testCfrReferenceCreation() {
        CfrReference cfr = new CfrReference(40, 60, "5");

        assertEquals(40, cfr.getTitle());
        assertEquals(60, cfr.getPart());
        assertEquals("5", cfr.getSection());
    }

    @Test
    void testCfrReferenceFullCitation() {
        CfrReference cfr1 = new CfrReference(40, 60, null);
        assertEquals("40 CFR 60", cfr1.getFullCitation());

        CfrReference cfr2 = new CfrReference(40, 60, "5");
        assertEquals("40 CFR 60.5", cfr2.getFullCitation());

        CfrReference cfr3 = new CfrReference(40, 60, "");
        assertEquals("40 CFR 60", cfr3.getFullCitation());

        CfrReference cfr4 = new CfrReference(40, 60, "   ");
        assertEquals("40 CFR 60", cfr4.getFullCitation());
    }

    @Test
    void testCfrReferenceWithNullPart() {
        CfrReference cfr = new CfrReference(40, null, null);
        assertEquals("40 CFR ", cfr.getFullCitation());
    }

    @Test
    void testCfrReferenceEquality() {
        CfrReference cfr1 = new CfrReference(40, 60, "5");
        CfrReference cfr2 = new CfrReference(40, 60, "5");
        CfrReference cfr3 = new CfrReference(40, 61, "5");

        assertEquals(cfr1, cfr2);
        assertNotEquals(cfr1, cfr3);
    }

    // =====================================================================
    // RegulationAgency Tests
    // =====================================================================

    @Test
    void testRegulationAgencyCreation() {
        java.util.UUID regulationId = java.util.UUID.randomUUID();
        java.util.UUID orgId = java.util.UUID.randomUUID();

        RegulationAgency ra = RegulationAgency.builder()
                .regulationId(regulationId)
                .organizationId(orgId)
                .agencyNameRaw("Environmental Protection Agency")
                .primaryAgency(true)
                .build();

        assertEquals(regulationId, ra.getRegulationId());
        assertEquals(orgId, ra.getOrganizationId());
        assertEquals("Environmental Protection Agency", ra.getAgencyNameRaw());
        assertTrue(ra.isPrimaryAgency());
    }

    @Test
    void testRegulationAgencyIdCompositeKey() {
        java.util.UUID regulationId = java.util.UUID.randomUUID();
        java.util.UUID orgId = java.util.UUID.randomUUID();

        RegulationAgencyId id1 = new RegulationAgencyId(regulationId, orgId);
        RegulationAgencyId id2 = new RegulationAgencyId(regulationId, orgId);
        RegulationAgencyId id3 = new RegulationAgencyId(java.util.UUID.randomUUID(), orgId);

        assertEquals(id1, id2);
        assertNotEquals(id1, id3);
        assertEquals(id1.hashCode(), id2.hashCode());
    }
}
