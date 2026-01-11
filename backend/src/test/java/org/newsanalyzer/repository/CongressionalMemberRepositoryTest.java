package org.newsanalyzer.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.newsanalyzer.TestcontainersConfiguration;
import org.newsanalyzer.model.CongressionalMember;
import org.newsanalyzer.model.DataSource;
import org.newsanalyzer.model.Individual;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for CongressionalMemberRepository.
 * Uses PostgreSQL Testcontainer for full PostgreSQL feature support.
 *
 * Part of ARCH-1.2: Create CongressionalMember Entity
 */
@DataJpaTest
@ActiveProfiles("tc")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestcontainersConfiguration.class)
class CongressionalMemberRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private CongressionalMemberRepository congressionalMemberRepository;

    @Autowired
    private IndividualRepository individualRepository;

    private Individual individual1;
    private Individual individual2;
    private Individual individual3;
    private CongressionalMember senator;
    private CongressionalMember representative;

    @BeforeEach
    void setUp() {
        congressionalMemberRepository.deleteAll();
        individualRepository.deleteAll();
        entityManager.flush();

        // Create Individual records first (required for FK)
        individual1 = Individual.builder()
                .firstName("Nancy")
                .lastName("Pelosi")
                .birthDate(LocalDate.of(1940, 3, 26))
                .birthPlace("Baltimore, Maryland")
                .gender("Female")
                .party("Democratic")
                .primaryDataSource(DataSource.CONGRESS_GOV)
                .build();
        individual1 = individualRepository.save(individual1);

        individual2 = Individual.builder()
                .firstName("Chuck")
                .lastName("Schumer")
                .birthDate(LocalDate.of(1950, 11, 23))
                .birthPlace("Brooklyn, New York")
                .gender("Male")
                .party("Democratic")
                .primaryDataSource(DataSource.CONGRESS_GOV)
                .build();
        individual2 = individualRepository.save(individual2);

        individual3 = Individual.builder()
                .firstName("Kevin")
                .lastName("McCarthy")
                .birthDate(LocalDate.of(1965, 1, 26))
                .birthPlace("Bakersfield, California")
                .gender("Male")
                .party("Republican")
                .primaryDataSource(DataSource.CONGRESS_GOV)
                .build();
        individual3 = individualRepository.save(individual3);

        entityManager.flush();

        // Create CongressionalMember records
        representative = CongressionalMember.builder()
                .individualId(individual1.getId())
                .bioguideId("P000197")
                .chamber(CongressionalMember.Chamber.HOUSE)
                .state("CA")
                .party("Democratic")
                .dataSource(DataSource.CONGRESS_GOV)
                .build();

        senator = CongressionalMember.builder()
                .individualId(individual2.getId())
                .bioguideId("S000148")
                .chamber(CongressionalMember.Chamber.SENATE)
                .state("NY")
                .party("Democratic")
                .dataSource(DataSource.CONGRESS_GOV)
                .build();
    }

    // =====================================================================
    // Basic CRUD Tests
    // =====================================================================

    @Test
    void testSaveCongressionalMember() {
        CongressionalMember saved = congressionalMemberRepository.save(representative);

        assertNotNull(saved.getId());
        assertEquals("P000197", saved.getBioguideId());
        assertEquals(CongressionalMember.Chamber.HOUSE, saved.getChamber());
        assertEquals("CA", saved.getState());
        assertNotNull(saved.getCreatedAt());
        assertNotNull(saved.getUpdatedAt());
    }

    @Test
    void testFindById() {
        CongressionalMember saved = congressionalMemberRepository.save(representative);
        entityManager.flush();
        entityManager.clear();

        Optional<CongressionalMember> found = congressionalMemberRepository.findById(saved.getId());

        assertTrue(found.isPresent());
        assertEquals(saved.getId(), found.get().getId());
        assertEquals("P000197", found.get().getBioguideId());
    }

    @Test
    void testUpdateCongressionalMember() {
        CongressionalMember saved = congressionalMemberRepository.save(representative);
        entityManager.flush();

        saved.setParty("Independent");
        CongressionalMember updated = congressionalMemberRepository.save(saved);
        entityManager.flush();
        entityManager.clear();

        Optional<CongressionalMember> found = congressionalMemberRepository.findById(updated.getId());
        assertTrue(found.isPresent());
        assertEquals("Independent", found.get().getParty());
    }

    @Test
    void testDeleteCongressionalMember() {
        CongressionalMember saved = congressionalMemberRepository.save(representative);
        entityManager.flush();

        congressionalMemberRepository.deleteById(saved.getId());
        entityManager.flush();

        Optional<CongressionalMember> found = congressionalMemberRepository.findById(saved.getId());
        assertFalse(found.isPresent());
    }

    // =====================================================================
    // Identifier Lookup Tests
    // =====================================================================

    @Test
    void testFindByBioguideId() {
        congressionalMemberRepository.save(representative);
        congressionalMemberRepository.save(senator);
        entityManager.flush();

        Optional<CongressionalMember> found = congressionalMemberRepository.findByBioguideId("P000197");

        assertTrue(found.isPresent());
        assertEquals("CA", found.get().getState());
        assertEquals(CongressionalMember.Chamber.HOUSE, found.get().getChamber());
    }

    @Test
    void testFindByBioguideId_notFound() {
        congressionalMemberRepository.save(representative);
        entityManager.flush();

        Optional<CongressionalMember> found = congressionalMemberRepository.findByBioguideId("X000000");

        assertFalse(found.isPresent());
    }

    @Test
    void testFindByIndividualId() {
        congressionalMemberRepository.save(representative);
        entityManager.flush();

        Optional<CongressionalMember> found = congressionalMemberRepository.findByIndividualId(individual1.getId());

        assertTrue(found.isPresent());
        assertEquals("P000197", found.get().getBioguideId());
    }

    @Test
    void testExistsByBioguideId() {
        congressionalMemberRepository.save(representative);
        entityManager.flush();

        assertTrue(congressionalMemberRepository.existsByBioguideId("P000197"));
        assertFalse(congressionalMemberRepository.existsByBioguideId("X000000"));
    }

    @Test
    void testExistsByIndividualId() {
        congressionalMemberRepository.save(representative);
        entityManager.flush();

        assertTrue(congressionalMemberRepository.existsByIndividualId(individual1.getId()));
        assertFalse(congressionalMemberRepository.existsByIndividualId(UUID.randomUUID()));
    }

    // =====================================================================
    // Congressional Attribute Query Tests
    // =====================================================================

    @Test
    void testFindByState() {
        congressionalMemberRepository.save(representative);
        congressionalMemberRepository.save(senator);
        entityManager.flush();

        List<CongressionalMember> caMembers = congressionalMemberRepository.findByState("CA");
        List<CongressionalMember> nyMembers = congressionalMemberRepository.findByState("NY");

        assertEquals(1, caMembers.size());
        assertEquals("P000197", caMembers.get(0).getBioguideId());

        assertEquals(1, nyMembers.size());
        assertEquals("S000148", nyMembers.get(0).getBioguideId());
    }

    @Test
    void testFindByChamber() {
        congressionalMemberRepository.save(representative);
        congressionalMemberRepository.save(senator);
        entityManager.flush();

        List<CongressionalMember> houseMembers = congressionalMemberRepository
                .findByChamber(CongressionalMember.Chamber.HOUSE);
        List<CongressionalMember> senateMembers = congressionalMemberRepository
                .findByChamber(CongressionalMember.Chamber.SENATE);

        assertEquals(1, houseMembers.size());
        assertEquals("P000197", houseMembers.get(0).getBioguideId());

        assertEquals(1, senateMembers.size());
        assertEquals("S000148", senateMembers.get(0).getBioguideId());
    }

    @Test
    void testFindByParty() {
        congressionalMemberRepository.save(representative);
        congressionalMemberRepository.save(senator);

        CongressionalMember republican = CongressionalMember.builder()
                .individualId(individual3.getId())
                .bioguideId("M001165")
                .chamber(CongressionalMember.Chamber.HOUSE)
                .state("CA")
                .party("Republican")
                .dataSource(DataSource.CONGRESS_GOV)
                .build();
        congressionalMemberRepository.save(republican);
        entityManager.flush();

        List<CongressionalMember> democrats = congressionalMemberRepository.findByParty("Democratic");
        List<CongressionalMember> republicans = congressionalMemberRepository.findByParty("Republican");

        assertEquals(2, democrats.size());
        assertEquals(1, republicans.size());
        assertEquals("M001165", republicans.get(0).getBioguideId());
    }

    @Test
    void testFindByStateAndChamber() {
        congressionalMemberRepository.save(representative);

        CongressionalMember caSenator = CongressionalMember.builder()
                .individualId(individual3.getId())
                .bioguideId("F000062")
                .chamber(CongressionalMember.Chamber.SENATE)
                .state("CA")
                .party("Democratic")
                .dataSource(DataSource.CONGRESS_GOV)
                .build();
        congressionalMemberRepository.save(caSenator);
        entityManager.flush();

        List<CongressionalMember> caHouse = congressionalMemberRepository
                .findByStateAndChamber("CA", CongressionalMember.Chamber.HOUSE);
        List<CongressionalMember> caSenate = congressionalMemberRepository
                .findByStateAndChamber("CA", CongressionalMember.Chamber.SENATE);

        assertEquals(1, caHouse.size());
        assertEquals("P000197", caHouse.get(0).getBioguideId());

        assertEquals(1, caSenate.size());
        assertEquals("F000062", caSenate.get(0).getBioguideId());
    }

    // =====================================================================
    // Paginated Query Tests
    // =====================================================================

    @Test
    void testFindByChamber_paginated() {
        congressionalMemberRepository.save(representative);

        CongressionalMember rep2 = CongressionalMember.builder()
                .individualId(individual3.getId())
                .bioguideId("M001165")
                .chamber(CongressionalMember.Chamber.HOUSE)
                .state("CA")
                .party("Republican")
                .dataSource(DataSource.CONGRESS_GOV)
                .build();
        congressionalMemberRepository.save(rep2);
        entityManager.flush();

        Page<CongressionalMember> page = congressionalMemberRepository
                .findByChamber(CongressionalMember.Chamber.HOUSE, PageRequest.of(0, 1));

        assertEquals(2, page.getTotalElements());
        assertEquals(1, page.getContent().size());
        assertEquals(2, page.getTotalPages());
    }

    @Test
    void testFindByState_paginated() {
        congressionalMemberRepository.save(representative);

        CongressionalMember rep2 = CongressionalMember.builder()
                .individualId(individual3.getId())
                .bioguideId("M001165")
                .chamber(CongressionalMember.Chamber.HOUSE)
                .state("CA")
                .party("Republican")
                .dataSource(DataSource.CONGRESS_GOV)
                .build();
        congressionalMemberRepository.save(rep2);
        entityManager.flush();

        Page<CongressionalMember> page = congressionalMemberRepository
                .findByState("CA", PageRequest.of(0, 10));

        assertEquals(2, page.getTotalElements());
        assertEquals(2, page.getContent().size());
    }

    // =====================================================================
    // Eager Loading Query Tests
    // =====================================================================

    @Test
    void testFindByBioguideIdWithIndividual() {
        congressionalMemberRepository.save(representative);
        entityManager.flush();
        entityManager.clear();

        Optional<CongressionalMember> found = congressionalMemberRepository
                .findByBioguideIdWithIndividual("P000197");

        assertTrue(found.isPresent());
        assertNotNull(found.get().getIndividual());
        assertEquals("Nancy", found.get().getIndividual().getFirstName());
        assertEquals("Pelosi", found.get().getIndividual().getLastName());
    }

    @Test
    void testFindByChamberWithIndividual() {
        congressionalMemberRepository.save(representative);
        congressionalMemberRepository.save(senator);
        entityManager.flush();
        entityManager.clear();

        List<CongressionalMember> houseMembers = congressionalMemberRepository
                .findByChamberWithIndividual(CongressionalMember.Chamber.HOUSE);

        assertEquals(1, houseMembers.size());
        assertNotNull(houseMembers.get(0).getIndividual());
        assertEquals("Nancy", houseMembers.get(0).getIndividual().getFirstName());
    }

    @Test
    void testFindByStateWithIndividual() {
        congressionalMemberRepository.save(representative);
        entityManager.flush();
        entityManager.clear();

        List<CongressionalMember> caMembers = congressionalMemberRepository
                .findByStateWithIndividual("CA");

        assertEquals(1, caMembers.size());
        assertNotNull(caMembers.get(0).getIndividual());
        assertEquals("Pelosi", caMembers.get(0).getIndividual().getLastName());
    }

    // =====================================================================
    // Count Query Tests
    // =====================================================================

    @Test
    void testCountByChamber() {
        congressionalMemberRepository.save(representative);
        congressionalMemberRepository.save(senator);
        entityManager.flush();

        assertEquals(1, congressionalMemberRepository.countByChamber(CongressionalMember.Chamber.HOUSE));
        assertEquals(1, congressionalMemberRepository.countByChamber(CongressionalMember.Chamber.SENATE));
    }

    @Test
    void testCountByParty() {
        congressionalMemberRepository.save(representative);
        congressionalMemberRepository.save(senator);
        entityManager.flush();

        assertEquals(2, congressionalMemberRepository.countByParty("Democratic"));
        assertEquals(0, congressionalMemberRepository.countByParty("Republican"));
    }

    @Test
    void testCountByState() {
        congressionalMemberRepository.save(representative);
        congressionalMemberRepository.save(senator);
        entityManager.flush();

        assertEquals(1, congressionalMemberRepository.countByState("CA"));
        assertEquals(1, congressionalMemberRepository.countByState("NY"));
        assertEquals(0, congressionalMemberRepository.countByState("TX"));
    }

    // =====================================================================
    // Individual Relationship Tests
    // =====================================================================

    @Test
    void testIndividualRelationship_lazyLoading() {
        congressionalMemberRepository.save(representative);
        entityManager.flush();
        entityManager.clear();

        Optional<CongressionalMember> found = congressionalMemberRepository.findByBioguideId("P000197");

        assertTrue(found.isPresent());
        assertEquals(individual1.getId(), found.get().getIndividualId());
        // Individual is lazy loaded, so we need to access it within a transaction
    }

    @Test
    void testGetFullName_throughIndividual() {
        congressionalMemberRepository.save(representative);
        entityManager.flush();
        entityManager.clear();

        Optional<CongressionalMember> found = congressionalMemberRepository
                .findByBioguideIdWithIndividual("P000197");

        assertTrue(found.isPresent());
        assertEquals("Nancy Pelosi", found.get().getFullName());
    }

    // =====================================================================
    // Helper Method Tests
    // =====================================================================

    @Test
    void testIsSenator() {
        CongressionalMember saved = congressionalMemberRepository.save(senator);

        assertTrue(saved.isSenator());
        assertFalse(saved.isRepresentative());
    }

    @Test
    void testIsRepresentative() {
        CongressionalMember saved = congressionalMemberRepository.save(representative);

        assertTrue(saved.isRepresentative());
        assertFalse(saved.isSenator());
    }

    // =====================================================================
    // Constraint Tests
    // =====================================================================

    @Test
    void testBioguideIdUnique() {
        congressionalMemberRepository.save(representative);
        entityManager.flush();

        // Verify we can check for existing bioguide IDs
        assertTrue(congressionalMemberRepository.existsByBioguideId("P000197"));
        assertFalse(congressionalMemberRepository.existsByBioguideId("NEW001"));
    }

    @Test
    void testIndividualIdUnique() {
        congressionalMemberRepository.save(representative);
        entityManager.flush();

        // Verify we can check for existing individual IDs
        assertTrue(congressionalMemberRepository.existsByIndividualId(individual1.getId()));
        assertFalse(congressionalMemberRepository.existsByIndividualId(UUID.randomUUID()));
    }
}
