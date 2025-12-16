package org.newsanalyzer.service;

import jakarta.xml.bind.JAXBException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.newsanalyzer.dto.GovmanImportResult;
import org.newsanalyzer.dto.govman.GovmanEntity;
import org.newsanalyzer.model.GovernmentOrganization;
import org.newsanalyzer.model.GovernmentOrganization.GovernmentBranch;
import org.newsanalyzer.model.GovernmentOrganization.OrganizationType;
import org.newsanalyzer.repository.GovernmentOrganizationRepository;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for GovmanXmlImportService.
 *
 * Tests cover:
 * - XML parsing with valid documents
 * - Category to branch mapping
 * - Parent-child relationship building
 * - Validation (missing required fields)
 * - Duplicate detection logic
 * - Import result counting
 *
 * @author James (Dev Agent)
 * @since 2.0.0
 */
@ExtendWith(MockitoExtension.class)
class GovmanXmlImportServiceTest {

    @Mock
    private GovernmentOrganizationRepository repository;

    @InjectMocks
    private GovmanXmlImportService importService;

    private InputStream createXmlStream(String xmlContent) {
        return new ByteArrayInputStream(xmlContent.getBytes(StandardCharsets.UTF_8));
    }

    @Nested
    @DisplayName("XML Parsing Tests")
    class XmlParsingTests {

        @Test
        @DisplayName("Should parse valid XML document with entities")
        void testParseXml_validDocument_returnsEntities() throws JAXBException {
            // Given
            String xml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <GovernmentManual>
                  <Entity>
                    <EntityId>TEST-1</EntityId>
                    <ParentId></ParentId>
                    <SortOrder>1</SortOrder>
                    <EntityType>Branch</EntityType>
                    <Category>Legislative Branch</Category>
                    <AgencyName>Test Congress</AgencyName>
                    <MissionStatement>
                      <Para>Test mission statement.</Para>
                    </MissionStatement>
                    <WebAddress>https://test.gov</WebAddress>
                  </Entity>
                  <Entity>
                    <EntityId>TEST-2</EntityId>
                    <ParentId>TEST-1</ParentId>
                    <SortOrder>1</SortOrder>
                    <EntityType>Agency</EntityType>
                    <Category>Legislative Branch</Category>
                    <AgencyName>Test Senate</AgencyName>
                  </Entity>
                  <Entity>
                    <EntityId>TEST-3</EntityId>
                    <ParentId>TEST-1</ParentId>
                    <SortOrder>2</SortOrder>
                    <EntityType>Agency</EntityType>
                    <Category>Legislative Branch</Category>
                    <AgencyName>Test House</AgencyName>
                  </Entity>
                </GovernmentManual>
                """;

            // When
            List<GovmanEntity> entities = importService.parseXml(createXmlStream(xml));

            // Then
            assertThat(entities).hasSize(3);
            assertThat(entities.get(0).getEntityId()).isEqualTo("TEST-1");
            assertThat(entities.get(0).getAgencyName()).isEqualTo("Test Congress");
            assertThat(entities.get(0).getCategory()).isEqualTo("Legislative Branch");
            assertThat(entities.get(0).getMissionStatement()).isEqualTo("Test mission statement.");
            assertThat(entities.get(0).getWebAddress()).isEqualTo("https://test.gov");
            assertThat(entities.get(1).getParentId()).isEqualTo("TEST-1");
        }

        @Test
        @DisplayName("Should parse empty document and return empty list")
        void testParseXml_emptyDocument_returnsEmptyList() throws JAXBException {
            // Given
            String xml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <GovernmentManual>
                </GovernmentManual>
                """;

            // When
            List<GovmanEntity> entities = importService.parseXml(createXmlStream(xml));

            // Then
            assertThat(entities).isEmpty();
        }

        @Test
        @DisplayName("Should concatenate multiple mission statement paragraphs")
        void testParseXml_multipleParagraphs_concatenatesMissionStatement() throws JAXBException {
            // Given
            String xml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <GovernmentManual>
                  <Entity>
                    <EntityId>TEST-1</EntityId>
                    <AgencyName>Test Agency</AgencyName>
                    <MissionStatement>
                      <Para>First paragraph.</Para>
                      <Para>Second paragraph.</Para>
                      <Para>Third paragraph.</Para>
                    </MissionStatement>
                  </Entity>
                </GovernmentManual>
                """;

            // When
            List<GovmanEntity> entities = importService.parseXml(createXmlStream(xml));

            // Then
            assertThat(entities).hasSize(1);
            assertThat(entities.get(0).getMissionStatement())
                    .isEqualTo("First paragraph.\n\nSecond paragraph.\n\nThird paragraph.");
        }
    }

    @Nested
    @DisplayName("Category to Branch Mapping Tests")
    class CategoryMappingTests {

        @Test
        @DisplayName("Should map 'Legislative Branch' to LEGISLATIVE")
        void testMapCategoryToBranch_legislativeBranch_returnsLegislative() {
            // When
            GovernmentBranch branch = importService.mapCategoryToBranch("Legislative Branch");

            // Then
            assertThat(branch).isEqualTo(GovernmentBranch.LEGISLATIVE);
        }

        @Test
        @DisplayName("Should map 'Executive Branch' to EXECUTIVE")
        void testMapCategoryToBranch_executiveBranch_returnsExecutive() {
            // When
            GovernmentBranch branch = importService.mapCategoryToBranch("Executive Branch");

            // Then
            assertThat(branch).isEqualTo(GovernmentBranch.EXECUTIVE);
        }

        @Test
        @DisplayName("Should map 'Judicial Branch' to JUDICIAL")
        void testMapCategoryToBranch_judicialBranch_returnsJudicial() {
            // When
            GovernmentBranch branch = importService.mapCategoryToBranch("Judicial Branch");

            // Then
            assertThat(branch).isEqualTo(GovernmentBranch.JUDICIAL);
        }

        @Test
        @DisplayName("Should map unknown category to EXECUTIVE (default)")
        void testMapCategoryToBranch_unknownCategory_returnsExecutive() {
            // When
            GovernmentBranch branch = importService.mapCategoryToBranch("Unknown Category");

            // Then
            assertThat(branch).isEqualTo(GovernmentBranch.EXECUTIVE);
        }

        @Test
        @DisplayName("Should map null category to EXECUTIVE (default)")
        void testMapCategoryToBranch_nullCategory_returnsExecutive() {
            // When
            GovernmentBranch branch = importService.mapCategoryToBranch(null);

            // Then
            assertThat(branch).isEqualTo(GovernmentBranch.EXECUTIVE);
        }

        @Test
        @DisplayName("Should handle case-insensitive matching")
        void testMapCategoryToBranch_caseInsensitive() {
            // When/Then
            assertThat(importService.mapCategoryToBranch("LEGISLATIVE BRANCH"))
                    .isEqualTo(GovernmentBranch.LEGISLATIVE);
            assertThat(importService.mapCategoryToBranch("legislative branch"))
                    .isEqualTo(GovernmentBranch.LEGISLATIVE);
        }
    }

    @Nested
    @DisplayName("Entity Type Mapping Tests")
    class EntityTypeMappingTests {

        @Test
        @DisplayName("Should map entity types correctly")
        void testMapEntityTypeToOrgType() {
            assertThat(importService.mapEntityTypeToOrgType("Branch"))
                    .isEqualTo(OrganizationType.BRANCH);
            assertThat(importService.mapEntityTypeToOrgType("Department"))
                    .isEqualTo(OrganizationType.DEPARTMENT);
            assertThat(importService.mapEntityTypeToOrgType("Agency"))
                    .isEqualTo(OrganizationType.INDEPENDENT_AGENCY);
            assertThat(importService.mapEntityTypeToOrgType("Bureau"))
                    .isEqualTo(OrganizationType.BUREAU);
            assertThat(importService.mapEntityTypeToOrgType("Commission"))
                    .isEqualTo(OrganizationType.COMMISSION);
            assertThat(importService.mapEntityTypeToOrgType("Board"))
                    .isEqualTo(OrganizationType.BOARD);
            assertThat(importService.mapEntityTypeToOrgType("Unknown"))
                    .isEqualTo(OrganizationType.OFFICE);
            assertThat(importService.mapEntityTypeToOrgType(null))
                    .isEqualTo(OrganizationType.OFFICE);
        }
    }

    @Nested
    @DisplayName("Validation Tests")
    class ValidationTests {

        @Test
        @DisplayName("Should fail validation when AgencyName is missing")
        void testValidateEntity_missingAgencyName_returnsFalse() {
            // Given
            GovmanEntity entity = new GovmanEntity();
            entity.setEntityId("TEST-1");
            entity.setAgencyName(null);

            GovmanImportResult result = GovmanImportResult.builder().build();

            // When
            boolean isValid = importService.validateEntity(entity, result);

            // Then
            assertThat(isValid).isFalse();
            assertThat(result.getErrors()).isEqualTo(1);
        }

        @Test
        @DisplayName("Should fail validation when EntityId is missing")
        void testValidateEntity_missingEntityId_returnsFalse() {
            // Given
            GovmanEntity entity = new GovmanEntity();
            entity.setEntityId(null);
            entity.setAgencyName("Test Agency");

            GovmanImportResult result = GovmanImportResult.builder().build();

            // When
            boolean isValid = importService.validateEntity(entity, result);

            // Then
            assertThat(isValid).isFalse();
        }

        @Test
        @DisplayName("Should pass validation with valid required fields")
        void testValidateEntity_validFields_returnsTrue() {
            // Given
            GovmanEntity entity = new GovmanEntity();
            entity.setEntityId("TEST-1");
            entity.setAgencyName("Test Agency");

            GovmanImportResult result = GovmanImportResult.builder().build();

            // When
            boolean isValid = importService.validateEntity(entity, result);

            // Then
            assertThat(isValid).isTrue();
        }
    }

    @Nested
    @DisplayName("Parent-Child Relationship Tests")
    class ParentChildTests {

        @Test
        @DisplayName("Should build entity map from list")
        void testBuildParentChildRelationships_validParentIds_buildsTree() {
            // Given
            GovmanEntity parent = new GovmanEntity();
            parent.setEntityId("PARENT-1");
            parent.setAgencyName("Parent Agency");

            GovmanEntity child = new GovmanEntity();
            child.setEntityId("CHILD-1");
            child.setParentId("PARENT-1");
            child.setAgencyName("Child Agency");

            List<GovmanEntity> entities = List.of(parent, child);

            // When
            var entityMap = importService.buildEntityMap(entities);

            // Then
            assertThat(entityMap).hasSize(2);
            assertThat(entityMap.get("PARENT-1")).isEqualTo(parent);
            assertThat(entityMap.get("CHILD-1")).isEqualTo(child);
        }
    }

    @Nested
    @DisplayName("Duplicate Detection Tests")
    class DuplicateDetectionTests {

        @Test
        @DisplayName("Should detect duplicate by external ID")
        void testDetectDuplicate_existingExternalId_returnsExisting() {
            // Given
            GovmanEntity entity = new GovmanEntity();
            entity.setEntityId("TEST-1");
            entity.setAgencyName("Test Agency");

            GovernmentOrganization existing = new GovernmentOrganization();
            existing.setId(UUID.randomUUID());
            existing.setGovinfoPackageId("GOVMAN:TEST-1");

            when(repository.findByGovinfoExternalId("GOVMAN:TEST-1"))
                    .thenReturn(Optional.of(existing));

            // When
            Optional<GovernmentOrganization> duplicate = importService.detectDuplicate(entity);

            // Then
            assertThat(duplicate).isPresent();
            assertThat(duplicate.get().getId()).isEqualTo(existing.getId());
        }

        @Test
        @DisplayName("Should detect duplicate by name when external ID not found")
        void testDetectDuplicate_existingName_returnsExisting() {
            // Given
            GovmanEntity entity = new GovmanEntity();
            entity.setEntityId("TEST-1");
            entity.setAgencyName("Test Agency");

            GovernmentOrganization existing = new GovernmentOrganization();
            existing.setId(UUID.randomUUID());
            existing.setOfficialName("Test Agency");

            when(repository.findByGovinfoExternalId("GOVMAN:TEST-1"))
                    .thenReturn(Optional.empty());
            when(repository.findByOfficialNameIgnoreCase("Test Agency"))
                    .thenReturn(Optional.of(existing));

            // When
            Optional<GovernmentOrganization> duplicate = importService.detectDuplicate(entity);

            // Then
            assertThat(duplicate).isPresent();
            assertThat(duplicate.get().getOfficialName()).isEqualTo("Test Agency");
        }

        @Test
        @DisplayName("Should return empty when no duplicate found")
        void testDetectDuplicate_noDuplicate_returnsEmpty() {
            // Given
            GovmanEntity entity = new GovmanEntity();
            entity.setEntityId("TEST-1");
            entity.setAgencyName("New Agency");

            when(repository.findByGovinfoExternalId("GOVMAN:TEST-1"))
                    .thenReturn(Optional.empty());
            when(repository.findByOfficialNameIgnoreCase("New Agency"))
                    .thenReturn(Optional.empty());

            // When
            Optional<GovernmentOrganization> duplicate = importService.detectDuplicate(entity);

            // Then
            assertThat(duplicate).isEmpty();
        }
    }

    @Nested
    @DisplayName("Import Result Counting Tests")
    class ImportResultTests {

        @Test
        @DisplayName("Should count imported, updated, skipped, and errors correctly")
        void testImportEntities_mixedResults_returnsAccurateCounts() {
            // Given
            String xml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <GovernmentManual>
                  <Entity>
                    <EntityId>NEW-1</EntityId>
                    <AgencyName>New Agency</AgencyName>
                    <Category>Executive Branch</Category>
                  </Entity>
                  <Entity>
                    <EntityId>EXISTING-1</EntityId>
                    <AgencyName>Existing Agency</AgencyName>
                    <Category>Executive Branch</Category>
                  </Entity>
                  <Entity>
                    <EntityId></EntityId>
                    <AgencyName>Invalid - No ID</AgencyName>
                  </Entity>
                  <Entity>
                    <EntityId>INVALID-2</EntityId>
                    <AgencyName></AgencyName>
                  </Entity>
                  <Entity>
                    <EntityId>MANUAL-1</EntityId>
                    <AgencyName>Manual Entry</AgencyName>
                    <Category>Executive Branch</Category>
                  </Entity>
                </GovernmentManual>
                """;

            // Existing organization from GOVMAN source (should be updated)
            GovernmentOrganization existingGovman = new GovernmentOrganization();
            existingGovman.setId(UUID.randomUUID());
            existingGovman.setOfficialName("Existing Agency");
            existingGovman.setImportSource("GOVMAN");

            // Existing organization from MANUAL source (should be skipped)
            GovernmentOrganization existingManual = new GovernmentOrganization();
            existingManual.setId(UUID.randomUUID());
            existingManual.setOfficialName("Manual Entry");
            existingManual.setImportSource("MANUAL");

            when(repository.findByImportSource("GOVMAN"))
                    .thenReturn(Collections.singletonList(existingGovman));
            when(repository.findByOfficialNameIgnoreCase("New Agency"))
                    .thenReturn(Optional.empty());
            when(repository.findByOfficialNameIgnoreCase("Existing Agency"))
                    .thenReturn(Optional.of(existingGovman));
            when(repository.findByOfficialNameIgnoreCase("Manual Entry"))
                    .thenReturn(Optional.of(existingManual));
            when(repository.save(any(GovernmentOrganization.class)))
                    .thenAnswer(invocation -> {
                        GovernmentOrganization org = invocation.getArgument(0);
                        if (org.getId() == null) {
                            org.setId(UUID.randomUUID());
                        }
                        return org;
                    });
            // Note: findById not needed as parent resolution happens in second pass

            // When
            GovmanImportResult result = importService.importFromStream(createXmlStream(xml));

            // Then
            assertThat(result.getTotal()).isEqualTo(5);
            assertThat(result.getImported()).isEqualTo(1);  // NEW-1
            assertThat(result.getUpdated()).isEqualTo(1);   // EXISTING-1
            assertThat(result.getSkipped()).isEqualTo(1);   // MANUAL-1 (different source)
            assertThat(result.getErrors()).isEqualTo(2);    // 2 invalid entities
        }

        @Test
        @DisplayName("Should successfully import valid organization")
        void testImport_validEntity_createsOrganization() {
            // Given
            String xml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <GovernmentManual>
                  <Entity>
                    <EntityId>TEST-1</EntityId>
                    <EntityType>Department</EntityType>
                    <Category>Executive Branch</Category>
                    <AgencyName>Test Department</AgencyName>
                    <MissionStatement>
                      <Para>Test mission.</Para>
                    </MissionStatement>
                    <WebAddress>https://test.gov</WebAddress>
                  </Entity>
                </GovernmentManual>
                """;

            when(repository.findByImportSource("GOVMAN")).thenReturn(Collections.emptyList());
            when(repository.findByOfficialNameIgnoreCase("Test Department")).thenReturn(Optional.empty());
            when(repository.save(any(GovernmentOrganization.class)))
                    .thenAnswer(invocation -> {
                        GovernmentOrganization org = invocation.getArgument(0);
                        org.setId(UUID.randomUUID());
                        return org;
                    });

            // When
            GovmanImportResult result = importService.importFromStream(createXmlStream(xml));

            // Then
            assertThat(result.getImported()).isEqualTo(1);
            assertThat(result.getErrors()).isEqualTo(0);

            // Verify saved organization
            ArgumentCaptor<GovernmentOrganization> captor = ArgumentCaptor.forClass(GovernmentOrganization.class);
            verify(repository).save(captor.capture());

            GovernmentOrganization saved = captor.getValue();
            assertThat(saved.getOfficialName()).isEqualTo("Test Department");
            assertThat(saved.getBranch()).isEqualTo(GovernmentBranch.EXECUTIVE);
            assertThat(saved.getOrgType()).isEqualTo(OrganizationType.DEPARTMENT);
            assertThat(saved.getMissionStatement()).isEqualTo("Test mission.");
            assertThat(saved.getWebsiteUrl()).isEqualTo("https://test.gov");
            assertThat(saved.getImportSource()).isEqualTo("GOVMAN");
            assertThat(saved.getGovinfoPackageId()).isEqualTo("GOVMAN:TEST-1");
        }
    }
}
