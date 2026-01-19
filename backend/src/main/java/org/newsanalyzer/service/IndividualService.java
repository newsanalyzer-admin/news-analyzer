package org.newsanalyzer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.newsanalyzer.model.DataSource;
import org.newsanalyzer.model.Individual;
import org.newsanalyzer.repository.IndividualRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service for Individual entity operations.
 *
 * Provides CRUD operations and deduplication logic for managing
 * individual (person) records across all data sources.
 *
 * Part of ARCH-1.6: Update Services Layer
 *
 * @author Claude (Dev Agent)
 * @since 3.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class IndividualService {

    private final IndividualRepository individualRepository;

    // =====================================================================
    // CRUD Operations
    // =====================================================================

    /**
     * Find individual by ID.
     */
    public Optional<Individual> findById(UUID id) {
        return individualRepository.findById(id);
    }

    /**
     * Save or update an individual.
     */
    @Transactional
    public Individual save(Individual individual) {
        return individualRepository.save(individual);
    }

    /**
     * Delete an individual by ID.
     */
    @Transactional
    public void deleteById(UUID id) {
        individualRepository.deleteById(id);
    }

    /**
     * Find all individuals.
     */
    public List<Individual> findAll() {
        return individualRepository.findAll();
    }

    // =====================================================================
    // Lookup Methods
    // =====================================================================

    /**
     * Find individual by exact first and last name.
     */
    public Optional<Individual> findByFirstNameAndLastName(String firstName, String lastName) {
        return individualRepository.findByFirstNameAndLastName(firstName, lastName);
    }

    /**
     * Find individuals by name (case-insensitive).
     */
    public List<Individual> findByNameIgnoreCase(String firstName, String lastName) {
        return individualRepository.findByFirstNameIgnoreCaseAndLastNameIgnoreCase(firstName, lastName);
    }

    /**
     * Find individual by name and birth date (case-insensitive names).
     */
    public Optional<Individual> findByNameAndBirthDate(String firstName, String lastName, LocalDate birthDate) {
        return individualRepository.findByFirstNameIgnoreCaseAndLastNameIgnoreCaseAndBirthDate(
                firstName, lastName, birthDate);
    }

    /**
     * Find individual by bioguide ID (from external_ids JSONB).
     */
    public Optional<Individual> findByBioguideId(String bioguideId) {
        return individualRepository.findByBioguideId(bioguideId);
    }

    /**
     * Find individuals by data source.
     */
    public List<Individual> findByDataSource(DataSource dataSource) {
        return individualRepository.findByPrimaryDataSource(dataSource);
    }

    /**
     * Search individuals by name pattern.
     */
    public List<Individual> searchByName(String name) {
        return individualRepository.searchByName(name);
    }

    // =====================================================================
    // Deduplication - Find or Create
    // =====================================================================

    /**
     * Find existing individual or create new one.
     *
     * Deduplication logic:
     * 1. If birthDate provided: exact match on (firstName, lastName, birthDate)
     * 2. If no birthDate: name-only match if exactly one result
     * 3. Otherwise: create new individual
     *
     * @param firstName first name (required)
     * @param lastName last name (required)
     * @param birthDate birth date (optional, improves deduplication)
     * @param dataSource data source for new records
     * @return existing or newly created Individual
     */
    @Transactional
    public Individual findOrCreate(String firstName, String lastName,
                                   LocalDate birthDate, DataSource dataSource) {
        if (firstName == null || lastName == null) {
            throw new IllegalArgumentException("First name and last name are required");
        }

        // Try exact match with birth date first
        if (birthDate != null) {
            Optional<Individual> existing = individualRepository
                    .findByFirstNameIgnoreCaseAndLastNameIgnoreCaseAndBirthDate(
                            firstName, lastName, birthDate);
            if (existing.isPresent()) {
                log.debug("Found existing individual by name+birthDate: {} {} ({})",
                        firstName, lastName, birthDate);
                return existing.get();
            }
        }

        // If no birth date, try name-only match
        if (birthDate == null) {
            List<Individual> matches = individualRepository
                    .findByFirstNameIgnoreCaseAndLastNameIgnoreCase(firstName, lastName);
            if (matches.size() == 1) {
                log.debug("Found single individual by name only: {} {}", firstName, lastName);
                return matches.get(0);
            }
            if (matches.size() > 1) {
                log.warn("Multiple individuals found for {} {} without birthDate - creating new",
                        firstName, lastName);
            }
        }

        // Create new individual
        log.info("Creating new individual: {} {} (source: {})", firstName, lastName, dataSource);
        Individual individual = Individual.builder()
                .firstName(firstName)
                .lastName(lastName)
                .birthDate(birthDate)
                .primaryDataSource(dataSource)
                .build();

        return individualRepository.save(individual);
    }

    /**
     * Find existing individual or create with full details.
     *
     * @param firstName first name
     * @param lastName last name
     * @param middleName middle name (optional)
     * @param suffix suffix (optional)
     * @param birthDate birth date (optional)
     * @param birthPlace birth place (optional)
     * @param gender gender (optional)
     * @param party party affiliation (optional)
     * @param dataSource data source
     * @return existing or newly created Individual
     */
    @Transactional
    public Individual findOrCreate(String firstName, String lastName,
                                   String middleName, String suffix,
                                   LocalDate birthDate, String birthPlace,
                                   String gender, String party,
                                   DataSource dataSource) {
        // First check if exists
        if (birthDate != null) {
            Optional<Individual> existing = individualRepository
                    .findByFirstNameIgnoreCaseAndLastNameIgnoreCaseAndBirthDate(
                            firstName, lastName, birthDate);
            if (existing.isPresent()) {
                // Update with additional details if provided
                Individual individual = existing.get();
                boolean updated = false;
                if (middleName != null && individual.getMiddleName() == null) {
                    individual.setMiddleName(middleName);
                    updated = true;
                }
                if (suffix != null && individual.getSuffix() == null) {
                    individual.setSuffix(suffix);
                    updated = true;
                }
                if (birthPlace != null && individual.getBirthPlace() == null) {
                    individual.setBirthPlace(birthPlace);
                    updated = true;
                }
                if (gender != null && individual.getGender() == null) {
                    individual.setGender(gender);
                    updated = true;
                }
                if (party != null && individual.getParty() == null) {
                    individual.setParty(party);
                    updated = true;
                }
                if (updated) {
                    return individualRepository.save(individual);
                }
                return individual;
            }
        }

        // Try name-only match if no birth date
        if (birthDate == null) {
            List<Individual> matches = individualRepository
                    .findByFirstNameIgnoreCaseAndLastNameIgnoreCase(firstName, lastName);
            if (matches.size() == 1) {
                return matches.get(0);
            }
        }

        // Create new
        Individual individual = Individual.builder()
                .firstName(firstName)
                .lastName(lastName)
                .middleName(middleName)
                .suffix(suffix)
                .birthDate(birthDate)
                .birthPlace(birthPlace)
                .gender(gender)
                .party(party)
                .primaryDataSource(dataSource)
                .build();

        return individualRepository.save(individual);
    }

    // =====================================================================
    // Existence Checks
    // =====================================================================

    /**
     * Check if individual exists with given name and birth date.
     */
    public boolean exists(String firstName, String lastName, LocalDate birthDate) {
        return individualRepository.existsByFirstNameIgnoreCaseAndLastNameIgnoreCaseAndBirthDate(
                firstName, lastName, birthDate);
    }

    /**
     * Count individuals by data source.
     */
    public long countByDataSource(DataSource dataSource) {
        return individualRepository.countByPrimaryDataSource(dataSource);
    }

    /**
     * Count all individuals.
     */
    public long count() {
        return individualRepository.count();
    }
}
