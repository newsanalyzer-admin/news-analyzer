package org.newsanalyzer.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.newsanalyzer.dto.DocumentQueryParams;
import org.newsanalyzer.dto.FederalRegisterDocument;
import org.newsanalyzer.model.*;
import org.newsanalyzer.repository.ExecutiveOrderRepository;
import org.newsanalyzer.repository.PersonRepository;
import org.newsanalyzer.repository.PresidencyRepository;

import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ExecutiveOrderSyncService.
 *
 * @author James (Dev Agent)
 * @since 2.0.0
 */
@ExtendWith(MockitoExtension.class)
class ExecutiveOrderSyncServiceTest {

    @Mock
    private FederalRegisterClient federalRegisterClient;

    @Mock
    private ExecutiveOrderRepository executiveOrderRepository;

    @Mock
    private PresidencyRepository presidencyRepository;

    @Mock
    private PersonRepository personRepository;

    private ExecutiveOrderSyncService syncService;

    @BeforeEach
    void setUp() {
        syncService = new ExecutiveOrderSyncService(
                federalRegisterClient,
                executiveOrderRepository,
                presidencyRepository,
                personRepository
        );
    }

    // =========================================================================
    // SyncResult Tests
    // =========================================================================

    @Nested
    @DisplayName("SyncResult Statistics")
    class SyncResultTests {

        @Test
        @DisplayName("Should calculate total executive orders correctly")
        void syncResult_totalExecutiveOrders_addsAddedAndUpdated() {
            ExecutiveOrderSyncService.SyncResult result = new ExecutiveOrderSyncService.SyncResult();

            assertThat(result.getTotalExecutiveOrders()).isEqualTo(0);
            assertThat(result.getExecutiveOrdersAdded()).isEqualTo(0);
            assertThat(result.getExecutiveOrdersUpdated()).isEqualTo(0);
            assertThat(result.getExecutiveOrdersSkipped()).isEqualTo(0);
            assertThat(result.getPresidenciesProcessed()).isEqualTo(0);
            assertThat(result.getErrors()).isEqualTo(0);
            assertThat(result.getErrorMessages()).isEmpty();
        }

        @Test
        @DisplayName("Should format toString correctly")
        void syncResult_toString_formatsCorrectly() {
            ExecutiveOrderSyncService.SyncResult result = new ExecutiveOrderSyncService.SyncResult();

            String str = result.toString();

            assertThat(str).contains("SyncResult");
            assertThat(str).contains("eos=");
            assertThat(str).contains("added=");
            assertThat(str).contains("updated=");
            assertThat(str).contains("skipped=");
            assertThat(str).contains("presidencies=");
            assertThat(str).contains("errors=");
        }
    }

    // =========================================================================
    // API Availability Tests
    // =========================================================================

    @Nested
    @DisplayName("API Availability")
    class ApiAvailabilityTests {

        @Test
        @DisplayName("Should return error when API is unavailable")
        void syncAllExecutiveOrders_apiUnavailable_returnsError() {
            when(federalRegisterClient.isApiAvailable()).thenReturn(false);

            ExecutiveOrderSyncService.SyncResult result = syncService.syncAllExecutiveOrders();

            assertThat(result.getErrors()).isEqualTo(1);
            assertThat(result.getErrorMessages()).anyMatch(msg ->
                    msg.contains("Federal Register API is not available"));
            assertThat(result.getPresidenciesProcessed()).isEqualTo(0);
            verify(presidencyRepository, never()).findAllByOrderByNumberAsc();
        }
    }

    // =========================================================================
    // Sync All Executive Orders Tests
    // =========================================================================

    @Nested
    @DisplayName("Sync All Executive Orders")
    class SyncAllExecutiveOrdersTests {

        @Test
        @DisplayName("Should process all presidencies")
        void syncAllExecutiveOrders_processesAllPresidencies() {
            when(federalRegisterClient.isApiAvailable()).thenReturn(true);

            // Setup 3 test presidencies
            UUID personId = UUID.randomUUID();
            Person president = Person.builder()
                    .id(personId)
                    .firstName("Donald")
                    .lastName("Trump")
                    .build();

            UUID presidencyId = UUID.randomUUID();
            Presidency presidency = Presidency.builder()
                    .id(presidencyId)
                    .number(45)
                    .personId(personId)
                    .build();

            when(presidencyRepository.findAllByOrderByNumberAsc())
                    .thenReturn(List.of(presidency));
            when(personRepository.findById(personId))
                    .thenReturn(Optional.of(president));
            when(federalRegisterClient.fetchAllDocuments(any(DocumentQueryParams.class), anyInt()))
                    .thenReturn(Collections.emptyList());

            ExecutiveOrderSyncService.SyncResult result = syncService.syncAllExecutiveOrders();

            assertThat(result.getErrors()).isEqualTo(0);
            assertThat(result.getPresidenciesProcessed()).isEqualTo(1);
            verify(federalRegisterClient).fetchAllDocuments(any(DocumentQueryParams.class), anyInt());
        }

        @Test
        @DisplayName("Should skip pre-FDR presidents")
        void syncAllExecutiveOrders_skipsPreFdrPresidents() {
            when(federalRegisterClient.isApiAvailable()).thenReturn(true);

            // Pre-FDR president (before 1933)
            UUID personId = UUID.randomUUID();
            Person president = Person.builder()
                    .id(personId)
                    .firstName("Abraham")
                    .lastName("Lincoln")
                    .build();

            UUID presidencyId = UUID.randomUUID();
            Presidency presidency = Presidency.builder()
                    .id(presidencyId)
                    .number(16)
                    .personId(personId)
                    .build();

            when(presidencyRepository.findAllByOrderByNumberAsc())
                    .thenReturn(List.of(presidency));
            when(personRepository.findById(personId))
                    .thenReturn(Optional.of(president));

            ExecutiveOrderSyncService.SyncResult result = syncService.syncAllExecutiveOrders();

            // Presidency is processed but no API call made (no mapping for Lincoln)
            assertThat(result.getPresidenciesProcessed()).isEqualTo(1);
            verify(federalRegisterClient, never()).fetchAllDocuments(any(), anyInt());
        }

        @Test
        @DisplayName("Should create new EOs from Federal Register")
        void syncAllExecutiveOrders_createsNewEOs() {
            when(federalRegisterClient.isApiAvailable()).thenReturn(true);

            UUID personId = UUID.randomUUID();
            Person president = Person.builder()
                    .id(personId)
                    .firstName("Joe")
                    .lastName("Biden")
                    .build();

            UUID presidencyId = UUID.randomUUID();
            Presidency presidency = Presidency.builder()
                    .id(presidencyId)
                    .number(46)
                    .personId(personId)
                    .build();

            FederalRegisterDocument eoDoc = new FederalRegisterDocument();
            eoDoc.setDocumentNumber("2021-12345");
            eoDoc.setExecutiveOrderNumber(14001);
            eoDoc.setTitle("Test Executive Order");
            eoDoc.setSigningDate(LocalDate.of(2021, 1, 20));
            eoDoc.setDocumentAbstract("Test abstract");
            eoDoc.setCitation("86 FR 7001");
            eoDoc.setHtmlUrl("https://federalregister.gov/d/2021-12345");

            when(presidencyRepository.findAllByOrderByNumberAsc())
                    .thenReturn(List.of(presidency));
            when(personRepository.findById(personId))
                    .thenReturn(Optional.of(president));
            when(federalRegisterClient.fetchAllDocuments(any(DocumentQueryParams.class), anyInt()))
                    .thenReturn(List.of(eoDoc));
            when(executiveOrderRepository.findByEoNumber(14001))
                    .thenReturn(Optional.empty());
            when(executiveOrderRepository.save(any(ExecutiveOrder.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            ExecutiveOrderSyncService.SyncResult result = syncService.syncAllExecutiveOrders();

            assertThat(result.getExecutiveOrdersAdded()).isEqualTo(1);
            assertThat(result.getExecutiveOrdersUpdated()).isEqualTo(0);

            ArgumentCaptor<ExecutiveOrder> eoCaptor = ArgumentCaptor.forClass(ExecutiveOrder.class);
            verify(executiveOrderRepository).save(eoCaptor.capture());

            ExecutiveOrder savedEo = eoCaptor.getValue();
            assertThat(savedEo.getEoNumber()).isEqualTo(14001);
            assertThat(savedEo.getTitle()).isEqualTo("Test Executive Order");
            assertThat(savedEo.getPresidencyId()).isEqualTo(presidencyId);
            assertThat(savedEo.getStatus()).isEqualTo(ExecutiveOrderStatus.ACTIVE);
            assertThat(savedEo.getDataSource()).isEqualTo(DataSource.FEDERAL_REGISTER);
        }

        @Test
        @DisplayName("Should update existing EOs")
        void syncAllExecutiveOrders_updatesExistingEOs() {
            when(federalRegisterClient.isApiAvailable()).thenReturn(true);

            UUID personId = UUID.randomUUID();
            Person president = Person.builder()
                    .id(personId)
                    .firstName("Barack")
                    .lastName("Obama")
                    .build();

            UUID presidencyId = UUID.randomUUID();
            Presidency presidency = Presidency.builder()
                    .id(presidencyId)
                    .number(44)
                    .personId(personId)
                    .build();

            FederalRegisterDocument eoDoc = new FederalRegisterDocument();
            eoDoc.setDocumentNumber("2009-12345");
            eoDoc.setExecutiveOrderNumber(13489);
            eoDoc.setTitle("Updated Title");
            eoDoc.setSigningDate(LocalDate.of(2009, 1, 22));

            ExecutiveOrder existingEo = ExecutiveOrder.builder()
                    .id(UUID.randomUUID())
                    .presidencyId(presidencyId)
                    .eoNumber(13489)
                    .title("Original Title")
                    .build();

            when(presidencyRepository.findAllByOrderByNumberAsc())
                    .thenReturn(List.of(presidency));
            when(personRepository.findById(personId))
                    .thenReturn(Optional.of(president));
            when(federalRegisterClient.fetchAllDocuments(any(DocumentQueryParams.class), anyInt()))
                    .thenReturn(List.of(eoDoc));
            when(executiveOrderRepository.findByEoNumber(13489))
                    .thenReturn(Optional.of(existingEo));
            when(executiveOrderRepository.save(any(ExecutiveOrder.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            ExecutiveOrderSyncService.SyncResult result = syncService.syncAllExecutiveOrders();

            assertThat(result.getExecutiveOrdersAdded()).isEqualTo(0);
            assertThat(result.getExecutiveOrdersUpdated()).isEqualTo(1);

            verify(executiveOrderRepository).save(existingEo);
            assertThat(existingEo.getTitle()).isEqualTo("Updated Title");
        }

        @Test
        @DisplayName("Should skip documents without EO number")
        void syncAllExecutiveOrders_skipsDocumentsWithoutEoNumber() {
            when(federalRegisterClient.isApiAvailable()).thenReturn(true);

            UUID personId = UUID.randomUUID();
            Person president = Person.builder()
                    .id(personId)
                    .firstName("Donald")
                    .lastName("Trump")
                    .build();

            UUID presidencyId = UUID.randomUUID();
            Presidency presidency = Presidency.builder()
                    .id(presidencyId)
                    .number(45)
                    .personId(personId)
                    .build();

            FederalRegisterDocument docWithoutEoNumber = new FederalRegisterDocument();
            docWithoutEoNumber.setDocumentNumber("2017-00001");
            docWithoutEoNumber.setTitle("Some Document");
            // No executive order number set

            when(presidencyRepository.findAllByOrderByNumberAsc())
                    .thenReturn(List.of(presidency));
            when(personRepository.findById(personId))
                    .thenReturn(Optional.of(president));
            when(federalRegisterClient.fetchAllDocuments(any(DocumentQueryParams.class), anyInt()))
                    .thenReturn(List.of(docWithoutEoNumber));

            ExecutiveOrderSyncService.SyncResult result = syncService.syncAllExecutiveOrders();

            assertThat(result.getExecutiveOrdersSkipped()).isEqualTo(1);
            assertThat(result.getExecutiveOrdersAdded()).isEqualTo(0);
            verify(executiveOrderRepository, never()).save(any(ExecutiveOrder.class));
        }
    }

    // =========================================================================
    // Sync Executive Orders for Specific Presidency Tests
    // =========================================================================

    @Nested
    @DisplayName("Sync Executive Orders for Specific Presidency")
    class SyncExecutiveOrdersForPresidencyTests {

        @Test
        @DisplayName("Should return error for non-existent presidency")
        void syncForPresidency_presidentNotFound_returnsError() {
            when(presidencyRepository.findByNumber(99))
                    .thenReturn(Optional.empty());

            ExecutiveOrderSyncService.SyncResult result =
                    syncService.syncExecutiveOrdersForPresidency(99);

            assertThat(result.getErrors()).isEqualTo(1);
            assertThat(result.getErrorMessages()).anyMatch(msg ->
                    msg.contains("Presidency #99 not found"));
        }

        @Test
        @DisplayName("Should sync EOs for valid presidency")
        void syncForPresidency_validPresidency_syncsEOs() {
            UUID personId = UUID.randomUUID();
            Person president = Person.builder()
                    .id(personId)
                    .firstName("Donald")
                    .lastName("Trump")
                    .build();

            UUID presidencyId = UUID.randomUUID();
            Presidency presidency = Presidency.builder()
                    .id(presidencyId)
                    .number(45)
                    .personId(personId)
                    .build();

            FederalRegisterDocument eoDoc = new FederalRegisterDocument();
            eoDoc.setDocumentNumber("2017-12345");
            eoDoc.setExecutiveOrderNumber(13765);
            eoDoc.setTitle("Test EO");
            eoDoc.setSigningDate(LocalDate.of(2017, 1, 27));

            when(presidencyRepository.findByNumber(45))
                    .thenReturn(Optional.of(presidency));
            when(personRepository.findById(personId))
                    .thenReturn(Optional.of(president));
            when(federalRegisterClient.fetchAllDocuments(any(DocumentQueryParams.class), anyInt()))
                    .thenReturn(List.of(eoDoc));
            when(executiveOrderRepository.findByEoNumber(13765))
                    .thenReturn(Optional.empty());
            when(executiveOrderRepository.save(any(ExecutiveOrder.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            ExecutiveOrderSyncService.SyncResult result =
                    syncService.syncExecutiveOrdersForPresidency(45);

            assertThat(result.getErrors()).isEqualTo(0);
            assertThat(result.getPresidenciesProcessed()).isEqualTo(1);
            assertThat(result.getExecutiveOrdersAdded()).isEqualTo(1);
        }

        @Test
        @DisplayName("Should use correct Federal Register president identifier")
        void syncForPresidency_usesCorrectFederalRegisterName() {
            UUID personId = UUID.randomUUID();
            Person president = Person.builder()
                    .id(personId)
                    .firstName("Joe")
                    .lastName("Biden")
                    .build();

            UUID presidencyId = UUID.randomUUID();
            Presidency presidency = Presidency.builder()
                    .id(presidencyId)
                    .number(46)
                    .personId(personId)
                    .build();

            when(presidencyRepository.findByNumber(46))
                    .thenReturn(Optional.of(presidency));
            when(personRepository.findById(personId))
                    .thenReturn(Optional.of(president));
            when(federalRegisterClient.fetchAllDocuments(any(DocumentQueryParams.class), anyInt()))
                    .thenReturn(Collections.emptyList());

            syncService.syncExecutiveOrdersForPresidency(46);

            ArgumentCaptor<DocumentQueryParams> paramsCaptor =
                    ArgumentCaptor.forClass(DocumentQueryParams.class);
            verify(federalRegisterClient).fetchAllDocuments(paramsCaptor.capture(), anyInt());

            DocumentQueryParams params = paramsCaptor.getValue();
            assertThat(params.getPresident()).isEqualTo("joe-biden");
            assertThat(params.getPresidentialDocumentType()).isEqualTo("executive_order");
        }
    }

    // =========================================================================
    // Executive Order Count Tests
    // =========================================================================

    @Nested
    @DisplayName("Executive Order Counts")
    class ExecutiveOrderCountTests {

        @Test
        @DisplayName("Should return EO counts per presidency")
        void getExecutiveOrderCounts_returnsCountsPerPresidency() {
            UUID presidency1 = UUID.randomUUID();
            UUID presidency2 = UUID.randomUUID();

            List<Object[]> mockResults = Arrays.asList(
                    new Object[]{presidency1, 100L},
                    new Object[]{presidency2, 250L}
            );

            when(executiveOrderRepository.getCountPerPresidency())
                    .thenReturn(mockResults);

            Map<UUID, Long> counts = syncService.getExecutiveOrderCounts();

            assertThat(counts).hasSize(2);
            assertThat(counts.get(presidency1)).isEqualTo(100L);
            assertThat(counts.get(presidency2)).isEqualTo(250L);
        }

        @Test
        @DisplayName("Should return empty map when no EOs exist")
        void getExecutiveOrderCounts_noEOs_returnsEmptyMap() {
            when(executiveOrderRepository.getCountPerPresidency())
                    .thenReturn(Collections.emptyList());

            Map<UUID, Long> counts = syncService.getExecutiveOrderCounts();

            assertThat(counts).isEmpty();
        }
    }

    // =========================================================================
    // Error Handling Tests
    // =========================================================================

    @Nested
    @DisplayName("Error Handling")
    class ErrorHandlingTests {

        @Test
        @DisplayName("Should handle individual presidency errors gracefully")
        void syncAllExecutiveOrders_handlesIndividualErrors() {
            when(federalRegisterClient.isApiAvailable()).thenReturn(true);

            UUID personId1 = UUID.randomUUID();
            UUID personId2 = UUID.randomUUID();

            Presidency presidency1 = Presidency.builder()
                    .id(UUID.randomUUID())
                    .number(45)
                    .personId(personId1)
                    .build();

            Presidency presidency2 = Presidency.builder()
                    .id(UUID.randomUUID())
                    .number(46)
                    .personId(personId2)
                    .build();

            Person president2 = Person.builder()
                    .id(personId2)
                    .firstName("Joe")
                    .lastName("Biden")
                    .build();

            when(presidencyRepository.findAllByOrderByNumberAsc())
                    .thenReturn(Arrays.asList(presidency1, presidency2));
            // First presidency throws error
            when(personRepository.findById(personId1))
                    .thenThrow(new RuntimeException("Database error"));
            // Second presidency succeeds
            when(personRepository.findById(personId2))
                    .thenReturn(Optional.of(president2));
            when(federalRegisterClient.fetchAllDocuments(any(DocumentQueryParams.class), anyInt()))
                    .thenReturn(Collections.emptyList());

            ExecutiveOrderSyncService.SyncResult result = syncService.syncAllExecutiveOrders();

            assertThat(result.getErrors()).isEqualTo(1);
            assertThat(result.getPresidenciesProcessed()).isEqualTo(1);
            assertThat(result.getErrorMessages()).anyMatch(msg ->
                    msg.contains("Presidency #45"));
        }

        @Test
        @DisplayName("Should handle EO processing errors gracefully")
        void syncAllExecutiveOrders_handlesEoProcessingErrors() {
            when(federalRegisterClient.isApiAvailable()).thenReturn(true);

            UUID personId = UUID.randomUUID();
            Person president = Person.builder()
                    .id(personId)
                    .firstName("Donald")
                    .lastName("Trump")
                    .build();

            UUID presidencyId = UUID.randomUUID();
            Presidency presidency = Presidency.builder()
                    .id(presidencyId)
                    .number(45)
                    .personId(personId)
                    .build();

            FederalRegisterDocument eoDoc1 = new FederalRegisterDocument();
            eoDoc1.setDocumentNumber("2017-00001");
            eoDoc1.setExecutiveOrderNumber(13765);

            FederalRegisterDocument eoDoc2 = new FederalRegisterDocument();
            eoDoc2.setDocumentNumber("2017-00002");
            eoDoc2.setExecutiveOrderNumber(13766);

            when(presidencyRepository.findAllByOrderByNumberAsc())
                    .thenReturn(List.of(presidency));
            when(personRepository.findById(personId))
                    .thenReturn(Optional.of(president));
            when(federalRegisterClient.fetchAllDocuments(any(DocumentQueryParams.class), anyInt()))
                    .thenReturn(Arrays.asList(eoDoc1, eoDoc2));
            // First EO throws error
            when(executiveOrderRepository.findByEoNumber(13765))
                    .thenThrow(new RuntimeException("Database error"));
            // Second EO succeeds
            when(executiveOrderRepository.findByEoNumber(13766))
                    .thenReturn(Optional.empty());
            when(executiveOrderRepository.save(any(ExecutiveOrder.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            ExecutiveOrderSyncService.SyncResult result = syncService.syncAllExecutiveOrders();

            assertThat(result.getErrors()).isEqualTo(1);
            assertThat(result.getExecutiveOrdersAdded()).isEqualTo(1);
            assertThat(result.getErrorMessages()).anyMatch(msg ->
                    msg.contains("EO #13765"));
        }

        @Test
        @DisplayName("Should skip presidency when person not found")
        void syncAllExecutiveOrders_personNotFound_skipsPresidency() {
            when(federalRegisterClient.isApiAvailable()).thenReturn(true);

            UUID personId = UUID.randomUUID();
            Presidency presidency = Presidency.builder()
                    .id(UUID.randomUUID())
                    .number(45)
                    .personId(personId)
                    .build();

            when(presidencyRepository.findAllByOrderByNumberAsc())
                    .thenReturn(List.of(presidency));
            when(personRepository.findById(personId))
                    .thenReturn(Optional.empty());

            ExecutiveOrderSyncService.SyncResult result = syncService.syncAllExecutiveOrders();

            // Presidency processed but no API call made
            assertThat(result.getPresidenciesProcessed()).isEqualTo(1);
            verify(federalRegisterClient, never()).fetchAllDocuments(any(), anyInt());
        }
    }

    // =========================================================================
    // Data Transformation Tests
    // =========================================================================

    @Nested
    @DisplayName("Data Transformation")
    class DataTransformationTests {

        @Test
        @DisplayName("Should truncate long titles")
        void createExecutiveOrder_truncatesLongTitles() {
            when(federalRegisterClient.isApiAvailable()).thenReturn(true);

            UUID personId = UUID.randomUUID();
            Person president = Person.builder()
                    .id(personId)
                    .firstName("Barack")
                    .lastName("Obama")
                    .build();

            UUID presidencyId = UUID.randomUUID();
            Presidency presidency = Presidency.builder()
                    .id(presidencyId)
                    .number(44)
                    .personId(personId)
                    .build();

            // Create a title longer than 500 chars
            String longTitle = "A".repeat(600);

            FederalRegisterDocument eoDoc = new FederalRegisterDocument();
            eoDoc.setDocumentNumber("2009-12345");
            eoDoc.setExecutiveOrderNumber(13489);
            eoDoc.setTitle(longTitle);
            eoDoc.setSigningDate(LocalDate.of(2009, 1, 22));

            when(presidencyRepository.findAllByOrderByNumberAsc())
                    .thenReturn(List.of(presidency));
            when(personRepository.findById(personId))
                    .thenReturn(Optional.of(president));
            when(federalRegisterClient.fetchAllDocuments(any(DocumentQueryParams.class), anyInt()))
                    .thenReturn(List.of(eoDoc));
            when(executiveOrderRepository.findByEoNumber(13489))
                    .thenReturn(Optional.empty());
            when(executiveOrderRepository.save(any(ExecutiveOrder.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            syncService.syncAllExecutiveOrders();

            ArgumentCaptor<ExecutiveOrder> eoCaptor = ArgumentCaptor.forClass(ExecutiveOrder.class);
            verify(executiveOrderRepository).save(eoCaptor.capture());

            ExecutiveOrder savedEo = eoCaptor.getValue();
            assertThat(savedEo.getTitle()).hasSize(500);
            assertThat(savedEo.getTitle()).endsWith("...");
        }

        @Test
        @DisplayName("Should use publication date when signing date is null")
        void createExecutiveOrder_usesPublicationDateWhenSigningDateNull() {
            when(federalRegisterClient.isApiAvailable()).thenReturn(true);

            UUID personId = UUID.randomUUID();
            Person president = Person.builder()
                    .id(personId)
                    .firstName("Joe")
                    .lastName("Biden")
                    .build();

            UUID presidencyId = UUID.randomUUID();
            Presidency presidency = Presidency.builder()
                    .id(presidencyId)
                    .number(46)
                    .personId(personId)
                    .build();

            LocalDate pubDate = LocalDate.of(2021, 1, 22);

            FederalRegisterDocument eoDoc = new FederalRegisterDocument();
            eoDoc.setDocumentNumber("2021-12345");
            eoDoc.setExecutiveOrderNumber(14001);
            eoDoc.setTitle("Test EO");
            eoDoc.setSigningDate(null);
            eoDoc.setPublicationDate(pubDate);

            when(presidencyRepository.findAllByOrderByNumberAsc())
                    .thenReturn(List.of(presidency));
            when(personRepository.findById(personId))
                    .thenReturn(Optional.of(president));
            when(federalRegisterClient.fetchAllDocuments(any(DocumentQueryParams.class), anyInt()))
                    .thenReturn(List.of(eoDoc));
            when(executiveOrderRepository.findByEoNumber(14001))
                    .thenReturn(Optional.empty());
            when(executiveOrderRepository.save(any(ExecutiveOrder.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            syncService.syncAllExecutiveOrders();

            ArgumentCaptor<ExecutiveOrder> eoCaptor = ArgumentCaptor.forClass(ExecutiveOrder.class);
            verify(executiveOrderRepository).save(eoCaptor.capture());

            ExecutiveOrder savedEo = eoCaptor.getValue();
            assertThat(savedEo.getSigningDate()).isEqualTo(pubDate);
        }
    }
}
