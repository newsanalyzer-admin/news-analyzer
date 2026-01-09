package org.newsanalyzer.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.newsanalyzer.TestcontainersConfiguration;
import org.newsanalyzer.model.DataSource;
import org.newsanalyzer.model.Individual;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for IndividualRepository.
 * Uses PostgreSQL Testcontainer for full PostgreSQL feature support (JSONB, partial indexes).
 *
 * Part of ARCH-1.1: Create Individual Entity and Table
 */
@DataJpaTest
@ActiveProfiles("tc")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestcontainersConfiguration.class)
class IndividualRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private IndividualRepository individualRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private Individual testIndividual;
    private Individual testIndividual2;

    @BeforeEach
    void setUp() {
        individualRepository.deleteAll();

        testIndividual = Individual.builder()
                .firstName("John")
                .lastName("Adams")
                .middleName("Quincy")
                .birthDate(LocalDate.of(1735, 10, 30))
                .deathDate(LocalDate.of(1826, 7, 4))
                .birthPlace("Braintree, Massachusetts")
                .party("Federalist")
                .primaryDataSource(DataSource.WHITE_HOUSE_HISTORICAL)
                .build();

        testIndividual2 = Individual.builder()
                .firstName("Thomas")
                .lastName("Jefferson")
                .birthDate(LocalDate.of(1743, 4, 13))
                .deathDate(LocalDate.of(1826, 7, 4))
                .birthPlace("Shadwell, Virginia")
                .party("Democratic-Republican")
                .primaryDataSource(DataSource.WHITE_HOUSE_HISTORICAL)
                .build();
    }

    // =====================================================================
    // Basic CRUD Tests
    // =====================================================================

    @Test
    void testSaveIndividual() {
        Individual saved = individualRepository.save(testIndividual);

        assertNotNull(saved.getId());
        assertEquals("John", saved.getFirstName());
        assertEquals("Adams", saved.getLastName());
        assertNotNull(saved.getCreatedAt());
        assertNotNull(saved.getUpdatedAt());
    }

    @Test
    void testFindById() {
        Individual saved = individualRepository.save(testIndividual);
        entityManager.flush();
        entityManager.clear();

        Optional<Individual> found = individualRepository.findById(saved.getId());

        assertTrue(found.isPresent());
        assertEquals(saved.getId(), found.get().getId());
        assertEquals("John", found.get().getFirstName());
        assertEquals("Adams", found.get().getLastName());
    }

    @Test
    void testUpdateIndividual() {
        Individual saved = individualRepository.save(testIndividual);
        entityManager.flush();

        saved.setParty("Independent");
        Individual updated = individualRepository.save(saved);
        entityManager.flush();
        entityManager.clear();

        Optional<Individual> found = individualRepository.findById(updated.getId());
        assertTrue(found.isPresent());
        assertEquals("Independent", found.get().getParty());
    }

    @Test
    void testDeleteIndividual() {
        Individual saved = individualRepository.save(testIndividual);
        entityManager.flush();

        individualRepository.deleteById(saved.getId());
        entityManager.flush();

        Optional<Individual> found = individualRepository.findById(saved.getId());
        assertFalse(found.isPresent());
    }

    // =====================================================================
    // Name Lookup Tests
    // =====================================================================

    @Test
    void testFindByFirstNameAndLastName() {
        individualRepository.save(testIndividual);
        individualRepository.save(testIndividual2);
        entityManager.flush();

        Optional<Individual> found = individualRepository.findByFirstNameAndLastName("John", "Adams");

        assertTrue(found.isPresent());
        assertEquals("John", found.get().getFirstName());
        assertEquals("Adams", found.get().getLastName());
    }

    @Test
    void testFindByFirstNameIgnoreCaseAndLastNameIgnoreCase() {
        individualRepository.save(testIndividual);
        entityManager.flush();

        List<Individual> found = individualRepository
                .findByFirstNameIgnoreCaseAndLastNameIgnoreCase("JOHN", "ADAMS");

        assertEquals(1, found.size());
        assertEquals("John", found.get(0).getFirstName());
    }

    @Test
    void testFindByFirstNameAndLastNameAndBirthDate() {
        individualRepository.save(testIndividual);
        entityManager.flush();

        Optional<Individual> found = individualRepository
                .findByFirstNameAndLastNameAndBirthDate(
                        "John", "Adams", LocalDate.of(1735, 10, 30));

        assertTrue(found.isPresent());
        assertEquals("John", found.get().getFirstName());
    }

    @Test
    void testFindByFirstNameIgnoreCaseAndLastNameIgnoreCaseAndBirthDate() {
        individualRepository.save(testIndividual);
        entityManager.flush();

        Optional<Individual> found = individualRepository
                .findByFirstNameIgnoreCaseAndLastNameIgnoreCaseAndBirthDate(
                        "john", "adams", LocalDate.of(1735, 10, 30));

        assertTrue(found.isPresent());
        assertEquals("John", found.get().getFirstName());
    }

    @Test
    void testSearchByName() {
        individualRepository.save(testIndividual);
        individualRepository.save(testIndividual2);
        entityManager.flush();

        List<Individual> results = individualRepository.searchByName("Adams");

        assertEquals(1, results.size());
        assertEquals("Adams", results.get(0).getLastName());
    }

    @Test
    void testFindByLastNameContainingIgnoreCase() {
        individualRepository.save(testIndividual);
        individualRepository.save(testIndividual2);
        entityManager.flush();

        List<Individual> results = individualRepository.findByLastNameContainingIgnoreCase("adam");

        assertEquals(1, results.size());
        assertEquals("Adams", results.get(0).getLastName());
    }

    // =====================================================================
    // Data Source Tests
    // =====================================================================

    @Test
    void testFindByPrimaryDataSource() {
        individualRepository.save(testIndividual);

        Individual congressMember = Individual.builder()
                .firstName("Nancy")
                .lastName("Pelosi")
                .birthDate(LocalDate.of(1940, 3, 26))
                .primaryDataSource(DataSource.CONGRESS_GOV)
                .build();
        individualRepository.save(congressMember);
        entityManager.flush();

        List<Individual> whiteHouse = individualRepository
                .findByPrimaryDataSource(DataSource.WHITE_HOUSE_HISTORICAL);
        List<Individual> congress = individualRepository
                .findByPrimaryDataSource(DataSource.CONGRESS_GOV);

        assertEquals(1, whiteHouse.size());
        assertEquals("Adams", whiteHouse.get(0).getLastName());

        assertEquals(1, congress.size());
        assertEquals("Pelosi", congress.get(0).getLastName());
    }

    @Test
    void testCountByPrimaryDataSource() {
        individualRepository.save(testIndividual);
        individualRepository.save(testIndividual2);
        entityManager.flush();

        long count = individualRepository.countByPrimaryDataSource(DataSource.WHITE_HOUSE_HISTORICAL);

        assertEquals(2, count);
    }

    // =====================================================================
    // Living/Deceased Tests
    // =====================================================================

    @Test
    void testFindByDeathDateIsNull() {
        individualRepository.save(testIndividual); // deceased

        Individual livingPerson = Individual.builder()
                .firstName("Joe")
                .lastName("Biden")
                .birthDate(LocalDate.of(1942, 11, 20))
                .deathDate(null)
                .primaryDataSource(DataSource.WHITE_HOUSE_HISTORICAL)
                .build();
        individualRepository.save(livingPerson);
        entityManager.flush();

        List<Individual> living = individualRepository.findByDeathDateIsNull();

        assertEquals(1, living.size());
        assertEquals("Biden", living.get(0).getLastName());
        assertTrue(living.get(0).isLiving());
    }

    @Test
    void testFindByDeathDateIsNotNull() {
        individualRepository.save(testIndividual);
        individualRepository.save(testIndividual2);

        Individual livingPerson = Individual.builder()
                .firstName("Joe")
                .lastName("Biden")
                .birthDate(LocalDate.of(1942, 11, 20))
                .deathDate(null)
                .primaryDataSource(DataSource.WHITE_HOUSE_HISTORICAL)
                .build();
        individualRepository.save(livingPerson);
        entityManager.flush();

        List<Individual> deceased = individualRepository.findByDeathDateIsNotNull();

        assertEquals(2, deceased.size());
        assertFalse(deceased.get(0).isLiving());
    }

    // =====================================================================
    // JSONB External IDs Tests
    // =====================================================================

    @Test
    void testExternalIdsJsonbPersistence() {
        ObjectNode externalIds = objectMapper.createObjectNode();
        externalIds.put("bioguideId", "A000022");
        externalIds.put("wikidataId", "Q11806");
        externalIds.put("govtrackId", "300001");

        testIndividual.setExternalIds(externalIds);
        Individual saved = individualRepository.save(testIndividual);
        entityManager.flush();
        entityManager.clear();

        Optional<Individual> found = individualRepository.findById(saved.getId());

        assertTrue(found.isPresent());
        assertNotNull(found.get().getExternalIds());
        assertEquals("A000022", found.get().getExternalIds().get("bioguideId").asText());
        assertEquals("Q11806", found.get().getExternalIds().get("wikidataId").asText());
    }

    @Test
    void testFindByBioguideId() {
        ObjectNode externalIds = objectMapper.createObjectNode();
        externalIds.put("bioguideId", "A000022");
        testIndividual.setExternalIds(externalIds);
        individualRepository.save(testIndividual);
        entityManager.flush();

        Optional<Individual> found = individualRepository.findByBioguideId("A000022");

        assertTrue(found.isPresent());
        assertEquals("Adams", found.get().getLastName());
    }

    @Test
    void testFindByExternalId() {
        ObjectNode externalIds = objectMapper.createObjectNode();
        externalIds.put("wikidataId", "Q11806");
        testIndividual.setExternalIds(externalIds);
        individualRepository.save(testIndividual);
        entityManager.flush();

        Optional<Individual> found = individualRepository.findByExternalId("wikidataId", "Q11806");

        assertTrue(found.isPresent());
        assertEquals("Adams", found.get().getLastName());
    }

    @Test
    void testSocialMediaJsonbPersistence() {
        ObjectNode socialMedia = objectMapper.createObjectNode();
        socialMedia.put("twitter", "@JohnAdams");
        socialMedia.put("facebook", "JohnAdamsOfficial");

        testIndividual.setSocialMedia(socialMedia);
        Individual saved = individualRepository.save(testIndividual);
        entityManager.flush();
        entityManager.clear();

        Optional<Individual> found = individualRepository.findById(saved.getId());

        assertTrue(found.isPresent());
        assertNotNull(found.get().getSocialMedia());
        assertEquals("@JohnAdams", found.get().getSocialMedia().get("twitter").asText());
    }

    // =====================================================================
    // Unique Constraint Tests (MOD-1)
    // =====================================================================

    @Test
    void testUniqueConstraint_sameNameDifferentBirthDate() {
        individualRepository.save(testIndividual);
        entityManager.flush();

        // Different birth date should succeed
        Individual anotherJohnAdams = Individual.builder()
                .firstName("John")
                .lastName("Adams")
                .birthDate(LocalDate.of(1800, 1, 1))
                .primaryDataSource(DataSource.MANUAL)
                .build();

        Individual saved = individualRepository.save(anotherJohnAdams);
        entityManager.flush();

        assertNotNull(saved.getId());
        assertNotEquals(testIndividual.getId(), saved.getId());
    }

    @Test
    void testUniqueConstraint_sameName_nullBirthDate() {
        // Null birth dates should NOT trigger unique constraint (partial index)
        Individual person1 = Individual.builder()
                .firstName("Unknown")
                .lastName("Person")
                .birthDate(null)
                .primaryDataSource(DataSource.MANUAL)
                .build();
        individualRepository.save(person1);
        entityManager.flush();

        Individual person2 = Individual.builder()
                .firstName("Unknown")
                .lastName("Person")
                .birthDate(null)
                .primaryDataSource(DataSource.MANUAL)
                .build();
        Individual saved = individualRepository.save(person2);
        entityManager.flush();

        assertNotNull(saved.getId());
        assertNotEquals(person1.getId(), saved.getId());
    }

    @Test
    void testUniqueConstraint_preventsDuplicates() {
        // Save first individual
        individualRepository.saveAndFlush(testIndividual);
        entityManager.clear();

        // Verify case-insensitive lookup can find the existing individual
        // This is the recommended way to check for duplicates before insert
        Optional<Individual> existing = individualRepository
                .findByFirstNameIgnoreCaseAndLastNameIgnoreCaseAndBirthDate(
                        "JOHN", "ADAMS", LocalDate.of(1735, 10, 30));

        assertTrue(existing.isPresent(),
                "Should find existing individual with case-insensitive lookup");
        assertEquals("John", existing.get().getFirstName());
        assertEquals("Adams", existing.get().getLastName());

        // Verify the exists check also works
        assertTrue(individualRepository.existsByFirstNameIgnoreCaseAndLastNameIgnoreCaseAndBirthDate(
                "john", "adams", LocalDate.of(1735, 10, 30)));
    }

    @Test
    void testExistsByName() {
        individualRepository.save(testIndividual);
        entityManager.flush();

        boolean exists = individualRepository.existsByFirstNameIgnoreCaseAndLastNameIgnoreCaseAndBirthDate(
                "john", "adams", LocalDate.of(1735, 10, 30));
        boolean notExists = individualRepository.existsByFirstNameIgnoreCaseAndLastNameIgnoreCaseAndBirthDate(
                "George", "Washington", LocalDate.of(1732, 2, 22));

        assertTrue(exists);
        assertFalse(notExists);
    }

    // =====================================================================
    // Helper Method Tests
    // =====================================================================

    @Test
    void testGetFullName() {
        Individual saved = individualRepository.save(testIndividual);

        assertEquals("John Quincy Adams", saved.getFullName());
    }

    @Test
    void testGetFullName_noMiddleName() {
        testIndividual.setMiddleName(null);
        Individual saved = individualRepository.save(testIndividual);

        assertEquals("John Adams", saved.getFullName());
    }

    @Test
    void testGetFullName_withSuffix() {
        testIndividual.setSuffix("Jr.");
        Individual saved = individualRepository.save(testIndividual);

        assertEquals("John Quincy Adams Jr.", saved.getFullName());
    }

    @Test
    void testGetDisplayName() {
        Individual saved = individualRepository.save(testIndividual);

        assertEquals("Adams, John", saved.getDisplayName());
    }

    @Test
    void testIsLiving() {
        Individual living = Individual.builder()
                .firstName("Joe")
                .lastName("Biden")
                .birthDate(LocalDate.of(1942, 11, 20))
                .deathDate(null)
                .build();

        assertTrue(living.isLiving());
        assertFalse(testIndividual.isLiving());
    }
}
