package org.newsanalyzer.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.newsanalyzer.model.DataSource;
import org.newsanalyzer.model.Individual;
import org.newsanalyzer.repository.IndividualRepository;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for IndividualService.
 *
 * Tests CRUD operations, name lookups, and deduplication logic.
 *
 * Part of ARCH-1.6: Update Services Layer
 *
 * @author Claude (Dev Agent)
 * @since 3.0.0
 */
@ExtendWith(MockitoExtension.class)
class IndividualServiceTest {

    @Mock
    private IndividualRepository individualRepository;

    @InjectMocks
    private IndividualService individualService;

    private Individual testIndividual;
    private UUID testId;
    private LocalDate testBirthDate;

    @BeforeEach
    void setUp() {
        testId = UUID.randomUUID();
        testBirthDate = LocalDate.of(1946, 6, 22);

        testIndividual = Individual.builder()
                .id(testId)
                .firstName("Elizabeth")
                .lastName("Warren")
                .birthDate(testBirthDate)
                .birthPlace("Oklahoma City, Oklahoma")
                .party("Democratic")
                .primaryDataSource(DataSource.CONGRESS_GOV)
                .build();
    }

    // =====================================================================
    // CRUD Operations
    // =====================================================================

    @Test
    void findById_existingIndividual_returnsIndividual() {
        when(individualRepository.findById(testId)).thenReturn(Optional.of(testIndividual));

        Optional<Individual> result = individualService.findById(testId);

        assertTrue(result.isPresent());
        assertEquals("Elizabeth", result.get().getFirstName());
        assertEquals("Warren", result.get().getLastName());
        verify(individualRepository).findById(testId);
    }

    @Test
    void findById_nonExistingIndividual_returnsEmpty() {
        when(individualRepository.findById(testId)).thenReturn(Optional.empty());

        Optional<Individual> result = individualService.findById(testId);

        assertFalse(result.isPresent());
        verify(individualRepository).findById(testId);
    }

    @Test
    void save_individual_returnsSavedIndividual() {
        when(individualRepository.save(testIndividual)).thenReturn(testIndividual);

        Individual result = individualService.save(testIndividual);

        assertNotNull(result);
        assertEquals(testId, result.getId());
        verify(individualRepository).save(testIndividual);
    }

    @Test
    void deleteById_existingIndividual_deletesSuccessfully() {
        doNothing().when(individualRepository).deleteById(testId);

        individualService.deleteById(testId);

        verify(individualRepository).deleteById(testId);
    }

    @Test
    void findAll_returnsAllIndividuals() {
        List<Individual> individuals = Arrays.asList(testIndividual);
        when(individualRepository.findAll()).thenReturn(individuals);

        List<Individual> result = individualService.findAll();

        assertEquals(1, result.size());
        assertEquals("Elizabeth", result.get(0).getFirstName());
        verify(individualRepository).findAll();
    }

    // =====================================================================
    // Lookup Methods
    // =====================================================================

    @Test
    void findByFirstNameAndLastName_existingIndividual_returnsIndividual() {
        when(individualRepository.findByFirstNameAndLastName("Elizabeth", "Warren"))
                .thenReturn(Optional.of(testIndividual));

        Optional<Individual> result = individualService.findByFirstNameAndLastName("Elizabeth", "Warren");

        assertTrue(result.isPresent());
        assertEquals("Elizabeth", result.get().getFirstName());
        verify(individualRepository).findByFirstNameAndLastName("Elizabeth", "Warren");
    }

    @Test
    void findByNameIgnoreCase_multipleMatches_returnsAll() {
        List<Individual> matches = Arrays.asList(testIndividual);
        when(individualRepository.findByFirstNameIgnoreCaseAndLastNameIgnoreCase("elizabeth", "warren"))
                .thenReturn(matches);

        List<Individual> result = individualService.findByNameIgnoreCase("elizabeth", "warren");

        assertEquals(1, result.size());
        verify(individualRepository).findByFirstNameIgnoreCaseAndLastNameIgnoreCase("elizabeth", "warren");
    }

    @Test
    void findByNameAndBirthDate_exactMatch_returnsIndividual() {
        when(individualRepository.findByFirstNameIgnoreCaseAndLastNameIgnoreCaseAndBirthDate(
                "Elizabeth", "Warren", testBirthDate))
                .thenReturn(Optional.of(testIndividual));

        Optional<Individual> result = individualService.findByNameAndBirthDate(
                "Elizabeth", "Warren", testBirthDate);

        assertTrue(result.isPresent());
        assertEquals(testBirthDate, result.get().getBirthDate());
        verify(individualRepository).findByFirstNameIgnoreCaseAndLastNameIgnoreCaseAndBirthDate(
                "Elizabeth", "Warren", testBirthDate);
    }

    @Test
    void findByBioguideId_existingBioguide_returnsIndividual() {
        when(individualRepository.findByBioguideId("W000817")).thenReturn(Optional.of(testIndividual));

        Optional<Individual> result = individualService.findByBioguideId("W000817");

        assertTrue(result.isPresent());
        verify(individualRepository).findByBioguideId("W000817");
    }

    @Test
    void findByDataSource_returnsMatchingIndividuals() {
        List<Individual> individuals = Arrays.asList(testIndividual);
        when(individualRepository.findByPrimaryDataSource(DataSource.CONGRESS_GOV)).thenReturn(individuals);

        List<Individual> result = individualService.findByDataSource(DataSource.CONGRESS_GOV);

        assertEquals(1, result.size());
        verify(individualRepository).findByPrimaryDataSource(DataSource.CONGRESS_GOV);
    }

    @Test
    void searchByName_partialMatch_returnsResults() {
        List<Individual> matches = Arrays.asList(testIndividual);
        when(individualRepository.searchByName("warren")).thenReturn(matches);

        List<Individual> result = individualService.searchByName("warren");

        assertEquals(1, result.size());
        verify(individualRepository).searchByName("warren");
    }

    // =====================================================================
    // Deduplication - findOrCreate
    // =====================================================================

    @Test
    void findOrCreate_existingIndividualWithBirthDate_returnsExisting() {
        when(individualRepository.findByFirstNameIgnoreCaseAndLastNameIgnoreCaseAndBirthDate(
                "Elizabeth", "Warren", testBirthDate))
                .thenReturn(Optional.of(testIndividual));

        Individual result = individualService.findOrCreate(
                "Elizabeth", "Warren", testBirthDate, DataSource.CONGRESS_GOV);

        assertEquals(testId, result.getId());
        verify(individualRepository).findByFirstNameIgnoreCaseAndLastNameIgnoreCaseAndBirthDate(
                "Elizabeth", "Warren", testBirthDate);
        verify(individualRepository, never()).save(any());
    }

    @Test
    void findOrCreate_newIndividualWithBirthDate_createsNew() {
        when(individualRepository.findByFirstNameIgnoreCaseAndLastNameIgnoreCaseAndBirthDate(
                "John", "Doe", testBirthDate))
                .thenReturn(Optional.empty());

        Individual newIndividual = Individual.builder()
                .firstName("John")
                .lastName("Doe")
                .birthDate(testBirthDate)
                .primaryDataSource(DataSource.CONGRESS_GOV)
                .build();
        when(individualRepository.save(any(Individual.class))).thenReturn(newIndividual);

        Individual result = individualService.findOrCreate(
                "John", "Doe", testBirthDate, DataSource.CONGRESS_GOV);

        assertEquals("John", result.getFirstName());
        assertEquals("Doe", result.getLastName());
        verify(individualRepository).save(any(Individual.class));
    }

    @Test
    void findOrCreate_nullBirthDate_singleNameMatch_returnsExisting() {
        List<Individual> matches = Arrays.asList(testIndividual);
        when(individualRepository.findByFirstNameIgnoreCaseAndLastNameIgnoreCase("Elizabeth", "Warren"))
                .thenReturn(matches);

        Individual result = individualService.findOrCreate(
                "Elizabeth", "Warren", null, DataSource.CONGRESS_GOV);

        assertEquals(testId, result.getId());
        verify(individualRepository).findByFirstNameIgnoreCaseAndLastNameIgnoreCase("Elizabeth", "Warren");
        verify(individualRepository, never()).save(any());
    }

    @Test
    void findOrCreate_nullBirthDate_multipleNameMatches_createsNew() {
        Individual otherIndividual = Individual.builder()
                .id(UUID.randomUUID())
                .firstName("Elizabeth")
                .lastName("Warren")
                .birthDate(LocalDate.of(1980, 1, 1))
                .build();

        List<Individual> matches = Arrays.asList(testIndividual, otherIndividual);
        when(individualRepository.findByFirstNameIgnoreCaseAndLastNameIgnoreCase("Elizabeth", "Warren"))
                .thenReturn(matches);

        Individual newIndividual = Individual.builder()
                .firstName("Elizabeth")
                .lastName("Warren")
                .primaryDataSource(DataSource.CONGRESS_GOV)
                .build();
        when(individualRepository.save(any(Individual.class))).thenReturn(newIndividual);

        Individual result = individualService.findOrCreate(
                "Elizabeth", "Warren", null, DataSource.CONGRESS_GOV);

        verify(individualRepository).save(any(Individual.class));
    }

    @Test
    void findOrCreate_nullFirstName_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            individualService.findOrCreate(null, "Warren", testBirthDate, DataSource.CONGRESS_GOV);
        });
    }

    @Test
    void findOrCreate_nullLastName_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            individualService.findOrCreate("Elizabeth", null, testBirthDate, DataSource.CONGRESS_GOV);
        });
    }

    @Test
    void findOrCreate_withFullDetails_existingIndividual_updatesFields() {
        Individual existingIndividual = Individual.builder()
                .id(testId)
                .firstName("Elizabeth")
                .lastName("Warren")
                .birthDate(testBirthDate)
                .build();

        when(individualRepository.findByFirstNameIgnoreCaseAndLastNameIgnoreCaseAndBirthDate(
                "Elizabeth", "Warren", testBirthDate))
                .thenReturn(Optional.of(existingIndividual));
        when(individualRepository.save(any(Individual.class))).thenReturn(existingIndividual);

        Individual result = individualService.findOrCreate(
                "Elizabeth", "Warren",
                "Ann", // middleName
                null, // suffix
                testBirthDate,
                "Oklahoma City, Oklahoma", // birthPlace
                "Female", // gender
                "Democratic", // party
                DataSource.CONGRESS_GOV);

        assertEquals("Ann", existingIndividual.getMiddleName());
        assertEquals("Oklahoma City, Oklahoma", existingIndividual.getBirthPlace());
        verify(individualRepository).save(existingIndividual);
    }

    @Test
    void findOrCreate_withFullDetails_newIndividual_createsWithAllFields() {
        when(individualRepository.findByFirstNameIgnoreCaseAndLastNameIgnoreCaseAndBirthDate(
                "John", "Smith", testBirthDate))
                .thenReturn(Optional.empty());
        when(individualRepository.findByFirstNameIgnoreCaseAndLastNameIgnoreCase("John", "Smith"))
                .thenReturn(Collections.emptyList());

        Individual newIndividual = Individual.builder()
                .firstName("John")
                .lastName("Smith")
                .middleName("Q")
                .birthDate(testBirthDate)
                .birthPlace("New York")
                .gender("Male")
                .party("Republican")
                .primaryDataSource(DataSource.CONGRESS_GOV)
                .build();
        when(individualRepository.save(any(Individual.class))).thenReturn(newIndividual);

        Individual result = individualService.findOrCreate(
                "John", "Smith", "Q", null, testBirthDate,
                "New York", "Male", "Republican", DataSource.CONGRESS_GOV);

        verify(individualRepository).save(argThat(ind ->
                "John".equals(ind.getFirstName()) &&
                "Smith".equals(ind.getLastName()) &&
                "Q".equals(ind.getMiddleName())
        ));
    }

    // =====================================================================
    // Existence Checks
    // =====================================================================

    @Test
    void exists_existingIndividual_returnsTrue() {
        when(individualRepository.existsByFirstNameIgnoreCaseAndLastNameIgnoreCaseAndBirthDate(
                "Elizabeth", "Warren", testBirthDate))
                .thenReturn(true);

        boolean result = individualService.exists("Elizabeth", "Warren", testBirthDate);

        assertTrue(result);
    }

    @Test
    void exists_nonExistingIndividual_returnsFalse() {
        when(individualRepository.existsByFirstNameIgnoreCaseAndLastNameIgnoreCaseAndBirthDate(
                "Unknown", "Person", testBirthDate))
                .thenReturn(false);

        boolean result = individualService.exists("Unknown", "Person", testBirthDate);

        assertFalse(result);
    }

    @Test
    void countByDataSource_returnsCorrectCount() {
        when(individualRepository.countByPrimaryDataSource(DataSource.CONGRESS_GOV)).thenReturn(42L);

        long result = individualService.countByDataSource(DataSource.CONGRESS_GOV);

        assertEquals(42L, result);
    }

    @Test
    void count_returnsTotal() {
        when(individualRepository.count()).thenReturn(100L);

        long result = individualService.count();

        assertEquals(100L, result);
    }
}
