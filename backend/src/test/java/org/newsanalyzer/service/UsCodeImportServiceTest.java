package org.newsanalyzer.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.newsanalyzer.dto.ParsedStatuteSection;
import org.newsanalyzer.dto.UsCodeImportResult;
import org.newsanalyzer.model.Statute;
import org.newsanalyzer.repository.StatuteRepository;

import javax.xml.stream.XMLStreamException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

/**
 * Unit tests for UsCodeImportService.
 *
 * @author James (Dev Agent)
 * @since 2.0.0
 */
@ExtendWith(MockitoExtension.class)
class UsCodeImportServiceTest {

    @Mock
    private UsCodeDownloadService downloadService;

    @Mock
    private UslmXmlParser xmlParser;

    @Mock
    private StatuteRepository statuteRepository;

    @InjectMocks
    private UsCodeImportService importService;

    @Captor
    private ArgumentCaptor<Statute> statuteCaptor;

    private static final String RELEASE_POINT = "119-22";

    @BeforeEach
    void setUp() {
        lenient().when(downloadService.getDefaultReleasePoint()).thenReturn(RELEASE_POINT);
    }

    // =====================================================================
    // Import Title Tests
    // =====================================================================

    @Test
    void importTitle_success_returnsSuccessResult() throws Exception {
        // Given
        int titleNumber = 5;
        InputStream mockStream = new ByteArrayInputStream("xml".getBytes());
        when(downloadService.downloadTitle(titleNumber, RELEASE_POINT)).thenReturn(mockStream);

        // Simulate parser producing one section
        doAnswer(invocation -> {
            Consumer<ParsedStatuteSection> consumer = invocation.getArgument(1);
            consumer.accept(createTestSection("/us/usc/t5/s101"));
            return null;
        }).when(xmlParser).parseStream(any(), any());

        when(statuteRepository.findByUscIdentifier("/us/usc/t5/s101")).thenReturn(Optional.empty());
        when(statuteRepository.saveAndFlush(any())).thenAnswer(inv -> inv.getArgument(0));

        // When
        UsCodeImportResult result = importService.importTitle(titleNumber, null);

        // Then
        assertTrue(result.isSuccess());
        assertEquals(titleNumber, result.getTitleNumber());
        assertEquals(RELEASE_POINT, result.getReleasePoint());
        assertEquals(1, result.getSectionsInserted());
        assertEquals(0, result.getSectionsUpdated());
        assertEquals(0, result.getSectionsFailed());
        assertNotNull(result.getStartedAt());
        assertNotNull(result.getCompletedAt());
    }

    @Test
    void importTitle_downloadFails_returnsErrorResult() throws Exception {
        // Given
        when(downloadService.downloadTitle(5, RELEASE_POINT))
                .thenThrow(new IOException("Network error"));

        // When
        UsCodeImportResult result = importService.importTitle(5, null);

        // Then
        assertFalse(result.isSuccess());
        assertTrue(result.getErrorMessage().contains("Download failed"));
    }

    @Test
    void importTitle_parseFails_returnsErrorResult() throws Exception {
        // Given
        InputStream mockStream = new ByteArrayInputStream("xml".getBytes());
        when(downloadService.downloadTitle(5, RELEASE_POINT)).thenReturn(mockStream);
        doThrow(new XMLStreamException("Parse error"))
                .when(xmlParser).parseStream(any(), any());

        // When
        UsCodeImportResult result = importService.importTitle(5, null);

        // Then
        assertFalse(result.isSuccess());
        assertTrue(result.getErrorMessage().contains("Parse failed"));
    }

    // =====================================================================
    // Upsert Logic Tests
    // =====================================================================

    @Test
    void importTitle_existingRecord_updatesIt() throws Exception {
        // Given
        int titleNumber = 5;
        InputStream mockStream = new ByteArrayInputStream("xml".getBytes());
        when(downloadService.downloadTitle(titleNumber, RELEASE_POINT)).thenReturn(mockStream);

        Statute existingStatute = Statute.builder()
                .id(UUID.randomUUID())
                .uscIdentifier("/us/usc/t5/s101")
                .titleNumber(5)
                .sectionNumber("101")
                .heading("Old heading")
                .build();

        doAnswer(invocation -> {
            Consumer<ParsedStatuteSection> consumer = invocation.getArgument(1);
            ParsedStatuteSection section = createTestSection("/us/usc/t5/s101");
            section.setHeading("New heading");
            consumer.accept(section);
            return null;
        }).when(xmlParser).parseStream(any(), any());

        when(statuteRepository.findByUscIdentifier("/us/usc/t5/s101"))
                .thenReturn(Optional.of(existingStatute));
        when(statuteRepository.saveAndFlush(any())).thenAnswer(inv -> inv.getArgument(0));

        // When
        UsCodeImportResult result = importService.importTitle(titleNumber, null);

        // Then
        assertTrue(result.isSuccess());
        assertEquals(0, result.getSectionsInserted());
        assertEquals(1, result.getSectionsUpdated());

        verify(statuteRepository).saveAndFlush(statuteCaptor.capture());
        Statute saved = statuteCaptor.getValue();
        assertEquals("New heading", saved.getHeading());
    }

    @Test
    void importTitle_newRecord_insertsIt() throws Exception {
        // Given
        int titleNumber = 5;
        InputStream mockStream = new ByteArrayInputStream("xml".getBytes());
        when(downloadService.downloadTitle(titleNumber, RELEASE_POINT)).thenReturn(mockStream);

        doAnswer(invocation -> {
            Consumer<ParsedStatuteSection> consumer = invocation.getArgument(1);
            consumer.accept(createTestSection("/us/usc/t5/s999"));
            return null;
        }).when(xmlParser).parseStream(any(), any());

        when(statuteRepository.findByUscIdentifier("/us/usc/t5/s999"))
                .thenReturn(Optional.empty());
        when(statuteRepository.saveAndFlush(any())).thenAnswer(inv -> inv.getArgument(0));

        // When
        UsCodeImportResult result = importService.importTitle(titleNumber, null);

        // Then
        assertTrue(result.isSuccess());
        assertEquals(1, result.getSectionsInserted());
        assertEquals(0, result.getSectionsUpdated());

        verify(statuteRepository).saveAndFlush(statuteCaptor.capture());
        Statute saved = statuteCaptor.getValue();
        assertEquals("/us/usc/t5/s999", saved.getUscIdentifier());
        assertEquals("USCODE", saved.getImportSource());
    }

    // =====================================================================
    // Multiple Sections Tests
    // =====================================================================

    @Test
    void importTitle_multipleSections_processesBatch() throws Exception {
        // Given
        int titleNumber = 5;
        InputStream mockStream = new ByteArrayInputStream("xml".getBytes());
        when(downloadService.downloadTitle(titleNumber, RELEASE_POINT)).thenReturn(mockStream);

        doAnswer(invocation -> {
            Consumer<ParsedStatuteSection> consumer = invocation.getArgument(1);
            consumer.accept(createTestSection("/us/usc/t5/s101"));
            consumer.accept(createTestSection("/us/usc/t5/s102"));
            consumer.accept(createTestSection("/us/usc/t5/s103"));
            return null;
        }).when(xmlParser).parseStream(any(), any());

        when(statuteRepository.findByUscIdentifier(any())).thenReturn(Optional.empty());
        when(statuteRepository.saveAndFlush(any())).thenAnswer(inv -> inv.getArgument(0));

        // When
        UsCodeImportResult result = importService.importTitle(titleNumber, null);

        // Then
        assertTrue(result.isSuccess());
        assertEquals(3, result.getSectionsInserted());
        assertEquals(3, result.getTotalProcessed());
        verify(statuteRepository, times(3)).saveAndFlush(any(Statute.class));
    }

    // =====================================================================
    // Statistics Tests
    // =====================================================================

    @Test
    void getTotalStatuteCount_delegatesToRepository() {
        // Given
        when(statuteRepository.countAll()).thenReturn(12345L);

        // When
        long count = importService.getTotalStatuteCount();

        // Then
        assertEquals(12345L, count);
    }

    @Test
    void getUsCodeCount_delegatesToRepository() {
        // Given
        when(statuteRepository.countByImportSource("USCODE")).thenReturn(9999L);

        // When
        long count = importService.getUsCodeCount();

        // Then
        assertEquals(9999L, count);
    }

    // =====================================================================
    // Custom Release Point Tests
    // =====================================================================

    @Test
    void importTitle_customReleasePoint_usesProvided() throws Exception {
        // Given
        String customRelease = "118-100";
        InputStream mockStream = new ByteArrayInputStream("xml".getBytes());
        when(downloadService.downloadTitle(5, customRelease)).thenReturn(mockStream);

        doAnswer(invocation -> {
            Consumer<ParsedStatuteSection> consumer = invocation.getArgument(1);
            consumer.accept(createTestSection("/us/usc/t5/s101"));
            return null;
        }).when(xmlParser).parseStream(any(), any());

        when(statuteRepository.findByUscIdentifier(any())).thenReturn(Optional.empty());
        when(statuteRepository.saveAndFlush(any())).thenAnswer(inv -> inv.getArgument(0));

        // When
        UsCodeImportResult result = importService.importTitle(5, customRelease);

        // Then
        assertEquals(customRelease, result.getReleasePoint());
        verify(downloadService).downloadTitle(5, customRelease);
    }

    // =====================================================================
    // Helper Methods
    // =====================================================================

    private ParsedStatuteSection createTestSection(String identifier) {
        return ParsedStatuteSection.builder()
                .uscIdentifier(identifier)
                .titleNumber(5)
                .titleName("GOVERNMENT ORGANIZATION")
                .sectionNumber(identifier.substring(identifier.lastIndexOf('s') + 1))
                .heading("Test section")
                .contentText("Sample content text")
                .contentXml("<section>...</section>")
                .sourceCredit("(Pub. L. 89-554)")
                .hasParseErrors(false)
                .build();
    }
}
