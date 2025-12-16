package org.newsanalyzer.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.newsanalyzer.model.Entity;
import org.newsanalyzer.model.EntityType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for EntityRepository.
 * Uses H2 in-memory database for testing.
 */
@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@Disabled("Requires PostgreSQL - Entity uses JSONB columns not supported by H2")
class EntityRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private EntityRepository entityRepository;

    private Entity testPerson;
    private Entity testOrganization;

    @BeforeEach
    void setUp() {
        // Clear any existing data
        entityRepository.deleteAll();

        // Create test person
        testPerson = new Entity();
        testPerson.setEntityType(EntityType.PERSON);
        testPerson.setName("Elizabeth Warren");
        testPerson.setSchemaOrgType("Person");

        Map<String, Object> personProps = new HashMap<>();
        personProps.put("jobTitle", "United States Senator");
        personProps.put("politicalParty", "Democratic Party");
        testPerson.setProperties(personProps);

        Map<String, Object> personSchema = new HashMap<>();
        personSchema.put("@type", "Person");
        personSchema.put("name", "Elizabeth Warren");
        testPerson.setSchemaOrgData(personSchema);

        testPerson.setConfidenceScore(0.95f);
        testPerson.setVerified(false);
        testPerson.setSource("test");

        // Create test organization
        testOrganization = new Entity();
        testOrganization.setEntityType(EntityType.GOVERNMENT_ORG);
        testOrganization.setName("Environmental Protection Agency");
        testOrganization.setSchemaOrgType("GovernmentOrganization");

        Map<String, Object> orgProps = new HashMap<>();
        orgProps.put("url", "https://www.epa.gov");
        orgProps.put("description", "Federal agency protecting environment");
        testOrganization.setProperties(orgProps);

        Map<String, Object> orgSchema = new HashMap<>();
        orgSchema.put("@type", "GovernmentOrganization");
        orgSchema.put("name", "Environmental Protection Agency");
        testOrganization.setSchemaOrgData(orgSchema);

        testOrganization.setConfidenceScore(1.0f);
        testOrganization.setVerified(true);
        testOrganization.setSource("manual");
    }

    @Test
    void testSaveEntity() {
        Entity saved = entityRepository.save(testPerson);

        assertNotNull(saved.getId());
        assertEquals("Elizabeth Warren", saved.getName());
        assertEquals(EntityType.PERSON, saved.getEntityType());
        assertNotNull(saved.getCreatedAt());
        assertNotNull(saved.getUpdatedAt());
    }

    @Test
    void testFindById() {
        Entity saved = entityRepository.save(testPerson);
        entityManager.flush();

        Optional<Entity> found = entityRepository.findById(saved.getId());

        assertTrue(found.isPresent());
        assertEquals(saved.getId(), found.get().getId());
        assertEquals("Elizabeth Warren", found.get().getName());
    }

    @Test
    void testFindByEntityType() {
        entityRepository.save(testPerson);
        entityRepository.save(testOrganization);
        entityManager.flush();

        List<Entity> persons = entityRepository.findByEntityType(EntityType.PERSON);
        List<Entity> govOrgs = entityRepository.findByEntityType(EntityType.GOVERNMENT_ORG);

        assertEquals(1, persons.size());
        assertEquals("Elizabeth Warren", persons.get(0).getName());

        assertEquals(1, govOrgs.size());
        assertEquals("Environmental Protection Agency", govOrgs.get(0).getName());
    }

    @Test
    void testFindBySchemaOrgType() {
        entityRepository.save(testPerson);
        entityRepository.save(testOrganization);
        entityManager.flush();

        List<Entity> persons = entityRepository.findBySchemaOrgType("Person");
        List<Entity> govOrgs = entityRepository.findBySchemaOrgType("GovernmentOrganization");

        assertEquals(1, persons.size());
        assertEquals("Elizabeth Warren", persons.get(0).getName());

        assertEquals(1, govOrgs.size());
        assertEquals("Environmental Protection Agency", govOrgs.get(0).getName());
    }

    @Test
    void testFindByName() {
        entityRepository.save(testPerson);
        entityManager.flush();

        Optional<Entity> found = entityRepository.findByName("Elizabeth Warren");

        assertTrue(found.isPresent());
        assertEquals("Elizabeth Warren", found.get().getName());
    }

    @Test
    void testFindByNameContainingIgnoreCase() {
        entityRepository.save(testPerson);
        entityRepository.save(testOrganization);
        entityManager.flush();

        List<Entity> results = entityRepository.findByNameContainingIgnoreCase("warren");

        assertEquals(1, results.size());
        assertEquals("Elizabeth Warren", results.get(0).getName());

        List<Entity> results2 = entityRepository.findByNameContainingIgnoreCase("AGENCY");
        assertEquals(1, results2.size());
        assertEquals("Environmental Protection Agency", results2.get(0).getName());
    }

    @Test
    void testFindByVerifiedTrue() {
        entityRepository.save(testPerson);      // verified = false
        entityRepository.save(testOrganization); // verified = true
        entityManager.flush();

        List<Entity> verified = entityRepository.findByVerifiedTrue();

        assertEquals(1, verified.size());
        assertEquals("Environmental Protection Agency", verified.get(0).getName());
        assertTrue(verified.get(0).getVerified());
    }

    @Test
    void testFindByEntityTypeAndVerified() {
        testPerson.setVerified(true);
        entityRepository.save(testPerson);
        entityRepository.save(testOrganization);

        Entity unverifiedPerson = new Entity();
        unverifiedPerson.setEntityType(EntityType.PERSON);
        unverifiedPerson.setName("Joe Biden");
        unverifiedPerson.setVerified(false);
        entityRepository.save(unverifiedPerson);

        entityManager.flush();

        List<Entity> verifiedPersons = entityRepository.findByEntityTypeAndVerified(EntityType.PERSON, true);
        List<Entity> unverifiedPersons = entityRepository.findByEntityTypeAndVerified(EntityType.PERSON, false);

        assertEquals(1, verifiedPersons.size());
        assertEquals("Elizabeth Warren", verifiedPersons.get(0).getName());

        assertEquals(1, unverifiedPersons.size());
        assertEquals("Joe Biden", unverifiedPersons.get(0).getName());
    }

    @Test
    void testFindByConfidenceScoreGreaterThanEqual() {
        testPerson.setConfidenceScore(0.95f);
        testOrganization.setConfidenceScore(0.85f);

        entityRepository.save(testPerson);
        entityRepository.save(testOrganization);
        entityManager.flush();

        List<Entity> highConfidence = entityRepository.findByConfidenceScoreGreaterThanEqual(0.90f);

        assertEquals(1, highConfidence.size());
        assertEquals("Elizabeth Warren", highConfidence.get(0).getName());
        assertTrue(highConfidence.get(0).getConfidenceScore() >= 0.90f);
    }

    @Test
    void testFindBySource() {
        testPerson.setSource("article:123");
        testOrganization.setSource("manual");

        entityRepository.save(testPerson);
        entityRepository.save(testOrganization);
        entityManager.flush();

        List<Entity> fromArticle = entityRepository.findBySource("article:123");
        List<Entity> fromManual = entityRepository.findBySource("manual");

        assertEquals(1, fromArticle.size());
        assertEquals("Elizabeth Warren", fromArticle.get(0).getName());

        assertEquals(1, fromManual.size());
        assertEquals("Environmental Protection Agency", fromManual.get(0).getName());
    }

    @Test
    void testCountByEntityType() {
        entityRepository.save(testPerson);
        entityRepository.save(testOrganization);

        Entity anotherPerson = new Entity();
        anotherPerson.setEntityType(EntityType.PERSON);
        anotherPerson.setName("Bernie Sanders");
        entityRepository.save(anotherPerson);

        entityManager.flush();

        long personCount = entityRepository.countByEntityType(EntityType.PERSON);
        long govOrgCount = entityRepository.countByEntityType(EntityType.GOVERNMENT_ORG);
        long locationCount = entityRepository.countByEntityType(EntityType.LOCATION);

        assertEquals(2, personCount);
        assertEquals(1, govOrgCount);
        assertEquals(0, locationCount);
    }

    @Test
    void testFindRecentEntities() {
        entityRepository.save(testPerson);
        entityRepository.save(testOrganization);
        entityManager.flush();

        LocalDateTime fromDate = LocalDateTime.now().minusDays(1);
        List<Entity> recent = entityRepository.findRecentEntities(fromDate);

        assertEquals(2, recent.size());
    }

    @Test
    void testFindByPropertyJobTitle() {
        entityRepository.save(testPerson);
        entityManager.flush();

        List<Entity> results = entityRepository.findByPropertyJobTitle("United States Senator");

        assertEquals(1, results.size());
        assertEquals("Elizabeth Warren", results.get(0).getName());
    }

    @Test
    void testUpdateEntity() {
        Entity saved = entityRepository.save(testPerson);
        entityManager.flush();

        saved.setName("Elizabeth Warren Updated");
        saved.setVerified(true);

        Entity updated = entityRepository.save(saved);
        entityManager.flush();

        Optional<Entity> found = entityRepository.findById(updated.getId());
        assertTrue(found.isPresent());
        assertEquals("Elizabeth Warren Updated", found.get().getName());
        assertTrue(found.get().getVerified());
    }

    @Test
    void testDeleteEntity() {
        Entity saved = entityRepository.save(testPerson);
        entityManager.flush();

        entityRepository.deleteById(saved.getId());
        entityManager.flush();

        Optional<Entity> found = entityRepository.findById(saved.getId());
        assertFalse(found.isPresent());
    }

    @Test
    void testPropertiesArePersisted() {
        Entity saved = entityRepository.save(testPerson);
        entityManager.flush();
        entityManager.clear();

        Optional<Entity> found = entityRepository.findById(saved.getId());
        assertTrue(found.isPresent());

        Map<String, Object> properties = found.get().getProperties();
        assertNotNull(properties);
        assertEquals("United States Senator", properties.get("jobTitle"));
        assertEquals("Democratic Party", properties.get("politicalParty"));
    }

    @Test
    void testSchemaOrgDataIsPersisted() {
        Entity saved = entityRepository.save(testPerson);
        entityManager.flush();
        entityManager.clear();

        Optional<Entity> found = entityRepository.findById(saved.getId());
        assertTrue(found.isPresent());

        Map<String, Object> schemaOrgData = found.get().getSchemaOrgData();
        assertNotNull(schemaOrgData);
        assertEquals("Person", schemaOrgData.get("@type"));
        assertEquals("Elizabeth Warren", schemaOrgData.get("name"));
    }
}
