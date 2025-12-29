package org.newsanalyzer.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.newsanalyzer.TestcontainersConfiguration;
import org.newsanalyzer.model.Statute;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Repository tests for StatuteRepository.
 * Uses PostgreSQL Testcontainer for full PostgreSQL feature support.
 *
 * @author James (Dev Agent)
 * @since 2.0.0
 */
@DataJpaTest
@ActiveProfiles("tc")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestcontainersConfiguration.class)
class StatuteRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private StatuteRepository statuteRepository;

    private Statute testStatute1;
    private Statute testStatute2;
    private Statute testStatute3;

    @BeforeEach
    void setUp() {
        // Create test statutes
        testStatute1 = Statute.builder()
                .uscIdentifier("/us/usc/t5/s101")
                .titleNumber(5)
                .titleName("GOVERNMENT ORGANIZATION AND EMPLOYEES")
                .chapterNumber("1")
                .chapterName("ORGANIZATION")
                .sectionNumber("101")
                .heading("Executive departments")
                .contentText("The Executive departments are the Department of State and Treasury")
                .importSource("USCODE")
                .releasePoint("119-22")
                .build();

        testStatute2 = Statute.builder()
                .uscIdentifier("/us/usc/t5/s102")
                .titleNumber(5)
                .titleName("GOVERNMENT ORGANIZATION AND EMPLOYEES")
                .chapterNumber("1")
                .chapterName("ORGANIZATION")
                .sectionNumber("102")
                .heading("Military departments")
                .contentText("The Military departments are the Department of the Army and Navy")
                .importSource("USCODE")
                .releasePoint("119-22")
                .build();

        testStatute3 = Statute.builder()
                .uscIdentifier("/us/usc/t42/s1983")
                .titleNumber(42)
                .titleName("THE PUBLIC HEALTH AND WELFARE")
                .chapterNumber("21")
                .chapterName("CIVIL RIGHTS")
                .sectionNumber("1983")
                .heading("Civil action for deprivation of rights")
                .contentText("Every person who under color of any statute deprives any citizen")
                .importSource("USCODE")
                .releasePoint("119-22")
                .build();

        entityManager.persist(testStatute1);
        entityManager.persist(testStatute2);
        entityManager.persist(testStatute3);
        entityManager.flush();
    }

    // =====================================================================
    // Find by USC Identifier Tests
    // =====================================================================

    @Test
    void findByUscIdentifier_exists_returnsStatute() {
        // When
        Optional<Statute> result = statuteRepository.findByUscIdentifier("/us/usc/t5/s101");

        // Then
        assertTrue(result.isPresent());
        assertEquals("Executive departments", result.get().getHeading());
    }

    @Test
    void findByUscIdentifier_notExists_returnsEmpty() {
        // When
        Optional<Statute> result = statuteRepository.findByUscIdentifier("/us/usc/t99/s999");

        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    void existsByUscIdentifier_exists_returnsTrue() {
        // When/Then
        assertTrue(statuteRepository.existsByUscIdentifier("/us/usc/t5/s101"));
    }

    @Test
    void existsByUscIdentifier_notExists_returnsFalse() {
        // When/Then
        assertFalse(statuteRepository.existsByUscIdentifier("/us/usc/t99/s999"));
    }

    // =====================================================================
    // Find by Title Tests
    // =====================================================================

    @Test
    void findByTitleNumber_returnsMatchingStatutes() {
        // When
        Page<Statute> result = statuteRepository.findByTitleNumber(5, PageRequest.of(0, 20));

        // Then
        assertEquals(2, result.getTotalElements());
    }

    @Test
    void findByTitleNumber_list_returnsAll() {
        // When
        List<Statute> result = statuteRepository.findByTitleNumber(5);

        // Then
        assertEquals(2, result.size());
    }

    @Test
    void countByTitleNumber_returnsCount() {
        // When
        long count = statuteRepository.countByTitleNumber(5);

        // Then
        assertEquals(2, count);
    }

    // =====================================================================
    // Find by Title and Chapter Tests
    // =====================================================================

    @Test
    void findByTitleNumberAndChapterNumber_returnsMatching() {
        // When
        Page<Statute> result = statuteRepository.findByTitleNumberAndChapterNumber(
                5, "1", PageRequest.of(0, 20));

        // Then
        assertEquals(2, result.getTotalElements());
    }

    // =====================================================================
    // Find by Title and Section Tests
    // =====================================================================

    @Test
    void findByTitleNumberAndSectionNumber_found_returnsStatute() {
        // When
        Optional<Statute> result = statuteRepository.findByTitleNumberAndSectionNumber(5, "101");

        // Then
        assertTrue(result.isPresent());
        assertEquals("Executive departments", result.get().getHeading());
    }

    @Test
    void findByTitleNumberAndSectionNumber_notFound_returnsEmpty() {
        // When
        Optional<Statute> result = statuteRepository.findByTitleNumberAndSectionNumber(5, "9999");

        // Then
        assertTrue(result.isEmpty());
    }

    // =====================================================================
    // Statistics Tests
    // =====================================================================

    @Test
    void countAll_returnsTotal() {
        // When
        long count = statuteRepository.countAll();

        // Then
        assertEquals(3, count);
    }

    @Test
    void findDistinctTitles_returnsUniqueTitles() {
        // When
        List<Object[]> titles = statuteRepository.findDistinctTitles();

        // Then
        assertEquals(2, titles.size()); // Title 5 and Title 42
    }

    @Test
    void countByTitle_returnsGroupedCounts() {
        // When
        List<Object[]> counts = statuteRepository.countByTitle();

        // Then
        assertEquals(2, counts.size());
    }

    // =====================================================================
    // Release Point Tests
    // =====================================================================

    @Test
    void findByReleasePoint_returnsMatching() {
        // When
        List<Statute> result = statuteRepository.findByReleasePoint("119-22");

        // Then
        assertEquals(3, result.size());
    }

    @Test
    void findDistinctReleasePoints_returnsUnique() {
        // When
        List<String> releasePoints = statuteRepository.findDistinctReleasePoints();

        // Then
        assertEquals(1, releasePoints.size());
        assertEquals("119-22", releasePoints.get(0));
    }

    // =====================================================================
    // Import Source Tests
    // =====================================================================

    @Test
    void findByImportSource_returnsMatching() {
        // When
        Page<Statute> result = statuteRepository.findByImportSource("USCODE", PageRequest.of(0, 20));

        // Then
        assertEquals(3, result.getTotalElements());
    }

    @Test
    void countByImportSource_returnsCount() {
        // When
        long count = statuteRepository.countByImportSource("USCODE");

        // Then
        assertEquals(3, count);
    }

    // =====================================================================
    // Section Pattern Search Tests
    // =====================================================================

    @Test
    void findBySectionNumberContaining_findsMatches() {
        // When
        List<Statute> result = statuteRepository.findBySectionNumberContaining("10");

        // Then
        assertEquals(2, result.size()); // 101 and 102
    }
}
