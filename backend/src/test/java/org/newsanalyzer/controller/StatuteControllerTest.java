package org.newsanalyzer.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.newsanalyzer.model.Statute;
import org.newsanalyzer.repository.StatuteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
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
 * Unit tests for StatuteController using MockMvc with WebMvcTest.
 *
 * @author James (Dev Agent)
 * @since 2.0.0
 */
@WebMvcTest(StatuteController.class)
@AutoConfigureMockMvc(addFilters = false)
class StatuteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StatuteRepository statuteRepository;

    private Statute testStatute;
    private UUID statuteId;

    @BeforeEach
    void setUp() {
        statuteId = UUID.randomUUID();

        testStatute = Statute.builder()
                .id(statuteId)
                .uscIdentifier("/us/usc/t5/s101")
                .titleNumber(5)
                .titleName("GOVERNMENT ORGANIZATION AND EMPLOYEES")
                .chapterNumber("1")
                .chapterName("ORGANIZATION")
                .sectionNumber("101")
                .heading("Executive departments")
                .contentText("The Executive departments are: (1) The Department of State.")
                .contentXml("<section>...</section>")
                .sourceCredit("(Pub. L. 89–554, Sept. 6, 1966, 80 Stat. 378.)")
                .sourceUrl("https://uscode.house.gov/view.xhtml?req=granuleid:USC-prelim-title5-section101")
                .releasePoint("119-22")
                .importSource("USCODE")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    // =====================================================================
    // List Statutes Tests
    // =====================================================================

    @Test
    void listStatutes_returnsPage() throws Exception {
        // Given
        List<Statute> content = new ArrayList<>();
        content.add(testStatute);
        Page<Statute> page = new PageImpl<>(content, PageRequest.of(0, 20), 1);
        when(statuteRepository.findAll(any(Pageable.class))).thenReturn(page);

        // When/Then
        mockMvc.perform(get("/api/statutes")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].uscIdentifier", is("/us/usc/t5/s101")))
                .andExpect(jsonPath("$.content[0].citation", is("5 U.S.C. § 101")));
    }

    @Test
    void listStatutes_withTitleFilter_filtersResults() throws Exception {
        // Given
        List<Statute> content = new ArrayList<>();
        content.add(testStatute);
        Page<Statute> page = new PageImpl<>(content, PageRequest.of(0, 20), 1);
        when(statuteRepository.findByTitleNumber(eq(5), any(Pageable.class))).thenReturn(page);

        // When/Then
        mockMvc.perform(get("/api/statutes")
                        .param("titleNumber", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].titleNumber", is(5)));
    }

    // =====================================================================
    // Get by ID Tests
    // =====================================================================

    @Test
    void getById_found_returnsDTO() throws Exception {
        // Given
        when(statuteRepository.findById(statuteId)).thenReturn(Optional.of(testStatute));

        // When/Then
        mockMvc.perform(get("/api/statutes/" + statuteId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(statuteId.toString())))
                .andExpect(jsonPath("$.uscIdentifier", is("/us/usc/t5/s101")))
                .andExpect(jsonPath("$.heading", is("Executive departments")))
                .andExpect(jsonPath("$.contentXml", notNullValue())); // Full detail includes XML
    }

    @Test
    void getById_notFound_returns404() throws Exception {
        // Given
        UUID unknownId = UUID.randomUUID();
        when(statuteRepository.findById(unknownId)).thenReturn(Optional.empty());

        // When/Then
        mockMvc.perform(get("/api/statutes/" + unknownId))
                .andExpect(status().isNotFound());
    }

    // =====================================================================
    // Get by Citation Tests
    // =====================================================================

    @Test
    void getByCitation_found_returnsDTO() throws Exception {
        // Given
        when(statuteRepository.findByUscIdentifier("/us/usc/t5/s101"))
                .thenReturn(Optional.of(testStatute));

        // When/Then - URL-encoded slashes
        mockMvc.perform(get("/api/statutes/by-citation/%2Fus%2Fusc%2Ft5%2Fs101"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uscIdentifier", is("/us/usc/t5/s101")));
    }

    @Test
    void getByCitation_notFound_returns404() throws Exception {
        // Given
        when(statuteRepository.findByUscIdentifier("/us/usc/t99/s999"))
                .thenReturn(Optional.empty());

        // When/Then
        mockMvc.perform(get("/api/statutes/by-citation/%2Fus%2Fusc%2Ft99%2Fs999"))
                .andExpect(status().isNotFound());
    }

    // =====================================================================
    // Get by Title and Section Tests
    // =====================================================================

    @Test
    void getByTitleAndSection_found_returnsDTO() throws Exception {
        // Given
        when(statuteRepository.findByTitleNumberAndSectionNumber(5, "101"))
                .thenReturn(Optional.of(testStatute));

        // When/Then
        mockMvc.perform(get("/api/statutes/title/5/section/101"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titleNumber", is(5)))
                .andExpect(jsonPath("$.sectionNumber", is("101")));
    }

    @Test
    void getByTitleAndSection_notFound_returns404() throws Exception {
        // Given
        when(statuteRepository.findByTitleNumberAndSectionNumber(5, "9999"))
                .thenReturn(Optional.empty());

        // When/Then
        mockMvc.perform(get("/api/statutes/title/5/section/9999"))
                .andExpect(status().isNotFound());
    }

    // =====================================================================
    // List by Title Tests
    // =====================================================================

    @Test
    void getByTitle_returnsPage() throws Exception {
        // Given
        List<Statute> content = new ArrayList<>();
        content.add(testStatute);
        Page<Statute> page = new PageImpl<>(content, PageRequest.of(0, 20), 1);
        when(statuteRepository.findByTitleNumber(eq(5), any(Pageable.class))).thenReturn(page);

        // When/Then
        mockMvc.perform(get("/api/statutes/title/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].titleNumber", is(5)));
    }

    @Test
    void getByTitle_withChapterFilter_filtersResults() throws Exception {
        // Given
        List<Statute> content = new ArrayList<>();
        content.add(testStatute);
        Page<Statute> page = new PageImpl<>(content, PageRequest.of(0, 20), 1);
        when(statuteRepository.findByTitleNumberAndChapterNumber(eq(5), eq("1"), any(Pageable.class)))
                .thenReturn(page);

        // When/Then
        mockMvc.perform(get("/api/statutes/title/5")
                        .param("chapterNumber", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].chapterNumber", is("1")));
    }

    // =====================================================================
    // Search Tests
    // =====================================================================

    @Test
    void searchStatutes_validQuery_returnsResults() throws Exception {
        // Given
        List<Statute> content = new ArrayList<>();
        content.add(testStatute);
        Page<Statute> page = new PageImpl<>(content, PageRequest.of(0, 20), 1);
        when(statuteRepository.searchByContentText(eq("executive departments"), any(Pageable.class)))
                .thenReturn(page);

        // When/Then
        mockMvc.perform(get("/api/statutes/search")
                        .param("q", "executive departments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)));
    }

    @Test
    void searchStatutes_withTitleFilter_filtersResults() throws Exception {
        // Given
        List<Statute> content = new ArrayList<>();
        content.add(testStatute);
        Page<Statute> page = new PageImpl<>(content, PageRequest.of(0, 20), 1);
        when(statuteRepository.searchByContentTextAndTitle(eq("departments"), eq(5), any(Pageable.class)))
                .thenReturn(page);

        // When/Then
        mockMvc.perform(get("/api/statutes/search")
                        .param("q", "departments")
                        .param("titleNumber", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)));
    }

    // =====================================================================
    // Title Index Tests
    // =====================================================================

    @Test
    void listTitles_returnsTitleInfo() throws Exception {
        // Given
        List<Object[]> titleData = new ArrayList<>();
        titleData.add(new Object[]{5, "GOVERNMENT ORGANIZATION AND EMPLOYEES"});
        when(statuteRepository.findDistinctTitles()).thenReturn(titleData);
        when(statuteRepository.countByTitleNumber(5)).thenReturn(500L);

        // When/Then
        mockMvc.perform(get("/api/statutes/titles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].titleNumber", is(5)))
                .andExpect(jsonPath("$[0].titleName", is("GOVERNMENT ORGANIZATION AND EMPLOYEES")))
                .andExpect(jsonPath("$[0].sectionCount", is(500)));
    }

    // =====================================================================
    // Statistics Tests
    // =====================================================================

    @Test
    void getStats_returnsStatistics() throws Exception {
        // Given
        when(statuteRepository.countAll()).thenReturn(50000L);
        List<Object[]> byTitle = new ArrayList<>();
        byTitle.add(new Object[]{5, 500L});
        byTitle.add(new Object[]{26, 3000L});
        when(statuteRepository.countByTitle()).thenReturn(byTitle);
        when(statuteRepository.findDistinctReleasePoints()).thenReturn(List.of("119-22", "119-21"));

        // When/Then
        mockMvc.perform(get("/api/statutes/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalSections", is(50000)))
                .andExpect(jsonPath("$.titlesLoaded", is(2)))
                .andExpect(jsonPath("$.latestReleasePoint", is("119-22")));
    }

    // =====================================================================
    // Response Format Tests
    // =====================================================================

    @Test
    void response_includesAllFields() throws Exception {
        // Given
        when(statuteRepository.findById(statuteId)).thenReturn(Optional.of(testStatute));

        // When/Then
        mockMvc.perform(get("/api/statutes/" + statuteId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(statuteId.toString())))
                .andExpect(jsonPath("$.uscIdentifier", is("/us/usc/t5/s101")))
                .andExpect(jsonPath("$.citation", is("5 U.S.C. § 101")))
                .andExpect(jsonPath("$.titleNumber", is(5)))
                .andExpect(jsonPath("$.titleName", is("GOVERNMENT ORGANIZATION AND EMPLOYEES")))
                .andExpect(jsonPath("$.chapterNumber", is("1")))
                .andExpect(jsonPath("$.chapterName", is("ORGANIZATION")))
                .andExpect(jsonPath("$.sectionNumber", is("101")))
                .andExpect(jsonPath("$.heading", is("Executive departments")))
                .andExpect(jsonPath("$.contentText", containsString("Executive departments")))
                .andExpect(jsonPath("$.sourceCredit", is("(Pub. L. 89–554, Sept. 6, 1966, 80 Stat. 378.)")))
                .andExpect(jsonPath("$.sourceUrl", containsString("uscode.house.gov")))
                .andExpect(jsonPath("$.releasePoint", is("119-22")))
                .andExpect(jsonPath("$.importSource", is("USCODE")));
    }

    @Test
    void listResponse_excludesXmlForPerformance() throws Exception {
        // Given
        List<Statute> content = new ArrayList<>();
        content.add(testStatute);
        Page<Statute> page = new PageImpl<>(content, PageRequest.of(0, 20), 1);
        when(statuteRepository.findAll(any(Pageable.class))).thenReturn(page);

        // When/Then
        mockMvc.perform(get("/api/statutes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].contentXml").doesNotExist());
    }

    // =====================================================================
    // Pagination Tests
    // =====================================================================

    @Test
    void paginatedResponse_includesPageMetadata() throws Exception {
        // Given
        List<Statute> content = new ArrayList<>();
        content.add(testStatute);
        Page<Statute> page = new PageImpl<>(
                content,
                PageRequest.of(0, 20),
                156 // total elements
        );
        when(statuteRepository.searchByContentText(eq("test"), any(Pageable.class)))
                .thenReturn(page);

        // When/Then
        mockMvc.perform(get("/api/statutes/search")
                        .param("q", "test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.totalElements", is(156)))
                .andExpect(jsonPath("$.totalPages", is(8)))
                .andExpect(jsonPath("$.number", is(0)))
                .andExpect(jsonPath("$.size", is(20)));
    }

    // =====================================================================
    // Hierarchy Endpoint Tests (ADMIN-1.13)
    // =====================================================================

    @Test
    void getTitleHierarchy_found_returnsNestedStructure() throws Exception {
        // Given
        Statute section102 = Statute.builder()
                .id(UUID.randomUUID())
                .uscIdentifier("/us/usc/t5/s102")
                .titleNumber(5)
                .titleName("GOVERNMENT ORGANIZATION AND EMPLOYEES")
                .chapterNumber("1")
                .chapterName("ORGANIZATION")
                .sectionNumber("102")
                .heading("Executive departments and agencies")
                .contentText("The departments are listed...")
                .build();

        List<Statute> statutes = new ArrayList<>();
        statutes.add(testStatute); // Section 101
        statutes.add(section102);   // Section 102

        when(statuteRepository.findByTitleNumber(5)).thenReturn(statutes);

        // When/Then
        mockMvc.perform(get("/api/statutes/title/5/hierarchy"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titleNumber", is(5)))
                .andExpect(jsonPath("$.titleName", is("GOVERNMENT ORGANIZATION AND EMPLOYEES")))
                .andExpect(jsonPath("$.sectionCount", is(2)))
                .andExpect(jsonPath("$.chapters", hasSize(1)))
                .andExpect(jsonPath("$.chapters[0].chapterNumber", is("1")))
                .andExpect(jsonPath("$.chapters[0].chapterName", is("ORGANIZATION")))
                .andExpect(jsonPath("$.chapters[0].sectionCount", is(2)))
                .andExpect(jsonPath("$.chapters[0].sections", hasSize(2)));
    }

    @Test
    void getTitleHierarchy_notFound_returns404() throws Exception {
        // Given - Title 54 exists but has no statutes
        when(statuteRepository.findByTitleNumber(54)).thenReturn(new ArrayList<>());

        // When/Then
        mockMvc.perform(get("/api/statutes/title/54/hierarchy"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getTitleHierarchy_multipleChapters_groupsCorrectly() throws Exception {
        // Given
        Statute chapter1Section = testStatute; // Chapter 1

        Statute chapter2Section = Statute.builder()
                .id(UUID.randomUUID())
                .uscIdentifier("/us/usc/t5/s301")
                .titleNumber(5)
                .titleName("GOVERNMENT ORGANIZATION AND EMPLOYEES")
                .chapterNumber("3")
                .chapterName("GENERAL PROVISIONS")
                .sectionNumber("301")
                .heading("General principles")
                .contentText("General provisions text...")
                .build();

        List<Statute> statutes = new ArrayList<>();
        statutes.add(chapter1Section);
        statutes.add(chapter2Section);

        when(statuteRepository.findByTitleNumber(5)).thenReturn(statutes);

        // When/Then
        mockMvc.perform(get("/api/statutes/title/5/hierarchy"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.chapters", hasSize(2)))
                .andExpect(jsonPath("$.chapters[0].chapterNumber", is("1")))
                .andExpect(jsonPath("$.chapters[1].chapterNumber", is("3")));
    }

    @Test
    void getTitleHierarchy_sectionsIncludePreview() throws Exception {
        // Given
        when(statuteRepository.findByTitleNumber(5)).thenReturn(List.of(testStatute));

        // When/Then
        mockMvc.perform(get("/api/statutes/title/5/hierarchy"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.chapters[0].sections[0].sectionNumber", is("101")))
                .andExpect(jsonPath("$.chapters[0].sections[0].heading", is("Executive departments")))
                .andExpect(jsonPath("$.chapters[0].sections[0].contentPreview", containsString("Executive departments")))
                .andExpect(jsonPath("$.chapters[0].sections[0].uscIdentifier", is("/us/usc/t5/s101")));
    }

    @Test
    void getTitleHierarchy_longContentTruncated() throws Exception {
        // Given - Statute with very long content
        String longContent = "A".repeat(500);
        Statute longStatute = Statute.builder()
                .id(UUID.randomUUID())
                .uscIdentifier("/us/usc/t5/s101")
                .titleNumber(5)
                .titleName("GOVERNMENT ORGANIZATION AND EMPLOYEES")
                .chapterNumber("1")
                .chapterName("ORGANIZATION")
                .sectionNumber("101")
                .heading("Executive departments")
                .contentText(longContent)
                .build();

        when(statuteRepository.findByTitleNumber(5)).thenReturn(List.of(longStatute));

        // When/Then - Content should be truncated to 200 chars + "..."
        mockMvc.perform(get("/api/statutes/title/5/hierarchy"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.chapters[0].sections[0].contentPreview", endsWith("...")));
    }

    @Test
    void getTitleHierarchy_sectionsOrderedBySectionNumber() throws Exception {
        // Given - Sections in random order
        Statute section103 = Statute.builder()
                .id(UUID.randomUUID())
                .uscIdentifier("/us/usc/t5/s103")
                .titleNumber(5)
                .titleName("GOVERNMENT ORGANIZATION AND EMPLOYEES")
                .chapterNumber("1")
                .chapterName("ORGANIZATION")
                .sectionNumber("103")
                .heading("Third section")
                .contentText("Content")
                .build();

        Statute section102 = Statute.builder()
                .id(UUID.randomUUID())
                .uscIdentifier("/us/usc/t5/s102")
                .titleNumber(5)
                .titleName("GOVERNMENT ORGANIZATION AND EMPLOYEES")
                .chapterNumber("1")
                .chapterName("ORGANIZATION")
                .sectionNumber("102")
                .heading("Second section")
                .contentText("Content")
                .build();

        // Add in wrong order: 103, 101, 102
        List<Statute> statutes = new ArrayList<>();
        statutes.add(section103);
        statutes.add(testStatute); // 101
        statutes.add(section102);

        when(statuteRepository.findByTitleNumber(5)).thenReturn(statutes);

        // When/Then - Should be sorted: 101, 102, 103
        mockMvc.perform(get("/api/statutes/title/5/hierarchy"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.chapters[0].sections[0].sectionNumber", is("101")))
                .andExpect(jsonPath("$.chapters[0].sections[1].sectionNumber", is("102")))
                .andExpect(jsonPath("$.chapters[0].sections[2].sectionNumber", is("103")));
    }
}
