package org.newsanalyzer.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.newsanalyzer.model.*;
import org.newsanalyzer.repository.*;
import org.newsanalyzer.service.dto.PresidencySeedData;
import org.newsanalyzer.service.dto.PresidencySeedData.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * Service for synchronizing presidential data from seed file.
 *
 * Imports all 47 presidencies with:
 * - President Person records
 * - Presidency records (linked to Person)
 * - Vice President Person records
 * - VP PositionHolding records (linked to Presidency)
 *
 * Handles non-consecutive terms (Cleveland 22/24, Trump 45/47) by
 * reusing the same Person record for multiple Presidency entries.
 *
 * @author James (Dev Agent)
 * @since 2.0.0
 */
@Service
@Transactional
public class PresidentialSyncService {

    private static final Logger log = LoggerFactory.getLogger(PresidentialSyncService.class);
    private static final String SEED_FILE_PATH = "data/presidencies-seed.json";
    private static final String VP_POSITION_TITLE = "Vice President of the United States";

    private final PersonRepository personRepository;
    private final PresidencyRepository presidencyRepository;
    private final GovernmentPositionRepository positionRepository;
    private final PositionHoldingRepository positionHoldingRepository;
    private final ObjectMapper objectMapper;

    // Cache for person lookups during sync
    private final Map<String, Person> personCache = new HashMap<>();

    public PresidentialSyncService(PersonRepository personRepository,
                                   PresidencyRepository presidencyRepository,
                                   GovernmentPositionRepository positionRepository,
                                   PositionHoldingRepository positionHoldingRepository) {
        this.personRepository = personRepository;
        this.presidencyRepository = presidencyRepository;
        this.positionRepository = positionRepository;
        this.positionHoldingRepository = positionHoldingRepository;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    /**
     * Sync result statistics.
     */
    public static class SyncResult {
        private int presidenciesAdded;
        private int presidenciesUpdated;
        private int personsAdded;
        private int personsUpdated;
        private int vpHoldingsAdded;
        private int errors;
        private List<String> errorMessages = new ArrayList<>();

        public int getPresidenciesAdded() { return presidenciesAdded; }
        public int getPresidenciesUpdated() { return presidenciesUpdated; }
        public int getPersonsAdded() { return personsAdded; }
        public int getPersonsUpdated() { return personsUpdated; }
        public int getVpHoldingsAdded() { return vpHoldingsAdded; }
        public int getErrors() { return errors; }
        public List<String> getErrorMessages() { return errorMessages; }

        public int getTotalPresidencies() { return presidenciesAdded + presidenciesUpdated; }

        @Override
        public String toString() {
            return String.format("SyncResult{presidencies=%d (added=%d, updated=%d), " +
                            "persons=%d (added=%d, updated=%d), vpHoldings=%d, errors=%d}",
                    getTotalPresidencies(), presidenciesAdded, presidenciesUpdated,
                    personsAdded + personsUpdated, personsAdded, personsUpdated,
                    vpHoldingsAdded, errors);
        }
    }

    /**
     * Perform full sync of presidential data from seed file.
     *
     * @return SyncResult with statistics
     */
    public SyncResult syncFromSeedFile() {
        SyncResult result = new SyncResult();
        log.info("Starting presidential data sync from seed file");

        try {
            // Load seed data
            SeedFile seedData = loadSeedData();
            if (seedData == null || seedData.getPresidencies() == null) {
                log.error("Failed to load seed data or no presidencies found");
                result.errors++;
                result.errorMessages.add("Failed to load seed data");
                return result;
            }

            log.info("Loaded {} presidencies from seed file", seedData.getPresidencies().size());

            // Clear person cache
            personCache.clear();

            // Ensure VP position exists
            GovernmentPosition vpPosition = ensureVpPositionExists();

            // Process each presidency
            for (PresidencyEntry entry : seedData.getPresidencies()) {
                try {
                    syncPresidency(entry, vpPosition, result);
                } catch (Exception e) {
                    log.error("Error syncing presidency #{}: {}", entry.getNumber(), e.getMessage(), e);
                    result.errors++;
                    result.errorMessages.add("Presidency #" + entry.getNumber() + ": " + e.getMessage());
                }
            }

            // Link predecessor/successor relationships
            linkPresidencyChain();

            log.info("Presidential sync completed: {}", result);

        } catch (Exception e) {
            log.error("Fatal error during presidential sync: {}", e.getMessage(), e);
            result.errors++;
            result.errorMessages.add("Fatal error: " + e.getMessage());
        }

        return result;
    }

    /**
     * Load seed data from classpath resource.
     */
    private SeedFile loadSeedData() throws IOException {
        ClassPathResource resource = new ClassPathResource(SEED_FILE_PATH);
        try (InputStream is = resource.getInputStream()) {
            return objectMapper.readValue(is, SeedFile.class);
        }
    }

    /**
     * Ensure the Vice President position exists in the database.
     */
    private GovernmentPosition ensureVpPositionExists() {
        return positionRepository.findByTitle(VP_POSITION_TITLE)
                .orElseGet(() -> {
                    log.info("Creating VP position: {}", VP_POSITION_TITLE);
                    GovernmentPosition vp = GovernmentPosition.builder()
                            .title(VP_POSITION_TITLE)
                            .branch(Branch.EXECUTIVE)
                            .positionType(PositionType.ELECTED)
                            .description("Vice President of the United States, elected with the President")
                            .build();
                    return positionRepository.save(vp);
                });
    }

    /**
     * Sync a single presidency entry.
     */
    private void syncPresidency(PresidencyEntry entry, GovernmentPosition vpPosition, SyncResult result) {
        log.debug("Syncing presidency #{}: {} {}",
                entry.getNumber(),
                entry.getPresident().getFirstName(),
                entry.getPresident().getLastName());

        // 1. Create/update president Person
        Person president = syncPerson(entry.getPresident(), entry.getPresidentKey(), result);

        // 2. Create/update Presidency
        Presidency presidency = syncPresidencyRecord(entry, president.getId(), result);

        // 3. Create VP holdings
        if (entry.getVicePresidents() != null) {
            for (VicePresidentEntry vpEntry : entry.getVicePresidents()) {
                syncVpHolding(vpEntry, presidency.getId(), vpPosition.getId(), result);
            }
        }
    }

    /**
     * Sync a person record (president or VP).
     */
    private Person syncPerson(PersonEntry entry, String personKey, SyncResult result) {
        // Check cache first (for non-consecutive terms)
        if (personCache.containsKey(personKey)) {
            log.debug("Using cached person: {}", personKey);
            return personCache.get(personKey);
        }

        // Look up by name
        Optional<Person> existing = personRepository.findByFirstNameAndLastName(
                entry.getFirstName(), entry.getLastName());

        Person person;
        if (existing.isPresent()) {
            person = existing.get();
            updatePerson(person, entry);
            person = personRepository.save(person);
            result.personsUpdated++;
            log.debug("Updated person: {} {}", entry.getFirstName(), entry.getLastName());
        } else {
            person = createPerson(entry);
            person = personRepository.save(person);
            result.personsAdded++;
            log.debug("Created person: {} {}", entry.getFirstName(), entry.getLastName());
        }

        // Cache for later lookups
        personCache.put(personKey, person);
        return person;
    }

    /**
     * Create a new Person from seed entry.
     */
    private Person createPerson(PersonEntry entry) {
        return Person.builder()
                .firstName(entry.getFirstName())
                .lastName(entry.getLastName())
                .middleName(entry.getMiddleName())
                .suffix(entry.getSuffix())
                .birthDate(entry.getBirthDate())
                .deathDate(entry.getDeathDate())
                .birthPlace(entry.getBirthPlace())
                .imageUrl(entry.getImageUrl())
                .dataSource(DataSource.WHITE_HOUSE_HISTORICAL)
                .build();
    }

    /**
     * Update existing person with seed data (only if fields are empty).
     */
    private void updatePerson(Person person, PersonEntry entry) {
        // Only update fields if currently null (don't overwrite existing data)
        if (person.getBirthDate() == null && entry.getBirthDate() != null) {
            person.setBirthDate(entry.getBirthDate());
        }
        if (person.getDeathDate() == null && entry.getDeathDate() != null) {
            person.setDeathDate(entry.getDeathDate());
        }
        if (person.getBirthPlace() == null && entry.getBirthPlace() != null) {
            person.setBirthPlace(entry.getBirthPlace());
        }
        if (person.getImageUrl() == null && entry.getImageUrl() != null) {
            person.setImageUrl(entry.getImageUrl());
        }
        if (person.getMiddleName() == null && entry.getMiddleName() != null) {
            person.setMiddleName(entry.getMiddleName());
        }
    }

    /**
     * Sync a Presidency record.
     */
    private Presidency syncPresidencyRecord(PresidencyEntry entry, UUID personId, SyncResult result) {
        Optional<Presidency> existing = presidencyRepository.findByNumber(entry.getNumber());

        Presidency presidency;
        if (existing.isPresent()) {
            presidency = existing.get();
            updatePresidency(presidency, entry, personId);
            presidency = presidencyRepository.save(presidency);
            result.presidenciesUpdated++;
            log.debug("Updated presidency #{}", entry.getNumber());
        } else {
            presidency = createPresidency(entry, personId);
            presidency = presidencyRepository.save(presidency);
            result.presidenciesAdded++;
            log.debug("Created presidency #{}", entry.getNumber());
        }

        return presidency;
    }

    /**
     * Create a new Presidency from seed entry.
     */
    private Presidency createPresidency(PresidencyEntry entry, UUID personId) {
        PresidencyEndReason endReason = null;
        if (entry.getEndReason() != null) {
            try {
                endReason = PresidencyEndReason.valueOf(entry.getEndReason());
            } catch (IllegalArgumentException e) {
                log.warn("Unknown end reason: {}", entry.getEndReason());
            }
        }

        return Presidency.builder()
                .personId(personId)
                .number(entry.getNumber())
                .party(entry.getParty())
                .startDate(entry.getStartDate())
                .endDate(entry.getEndDate())
                .electionYear(entry.getElectionYear())
                .endReason(endReason)
                .dataSource(DataSource.WHITE_HOUSE_HISTORICAL)
                .build();
    }

    /**
     * Update existing Presidency with seed data.
     */
    private void updatePresidency(Presidency presidency, PresidencyEntry entry, UUID personId) {
        presidency.setPersonId(personId);
        presidency.setParty(entry.getParty());
        presidency.setStartDate(entry.getStartDate());
        presidency.setEndDate(entry.getEndDate());
        presidency.setElectionYear(entry.getElectionYear());

        if (entry.getEndReason() != null) {
            try {
                presidency.setEndReason(PresidencyEndReason.valueOf(entry.getEndReason()));
            } catch (IllegalArgumentException e) {
                log.warn("Unknown end reason: {}", entry.getEndReason());
            }
        }
    }

    /**
     * Sync a Vice President PositionHolding record.
     */
    private void syncVpHolding(VicePresidentEntry entry, UUID presidencyId,
                               UUID vpPositionId, SyncResult result) {
        // Create/update VP person
        PersonEntry vpPersonEntry = new PersonEntry();
        vpPersonEntry.setFirstName(entry.getFirstName());
        vpPersonEntry.setLastName(entry.getLastName());
        vpPersonEntry.setMiddleName(entry.getMiddleName());

        Person vpPerson = syncPerson(vpPersonEntry, entry.getPersonKey(), result);

        // Check if holding already exists for this person, position, and date range
        // We use start date as unique identifier within a position
        boolean exists = positionHoldingRepository
                .findByPersonIdAndPositionIdAndStartDate(vpPerson.getId(), vpPositionId, entry.getStartDate())
                .isPresent();

        if (!exists) {
            PositionHolding holding = PositionHolding.builder()
                    .personId(vpPerson.getId())
                    .positionId(vpPositionId)
                    .presidencyId(presidencyId)
                    .startDate(entry.getStartDate())
                    .endDate(entry.getEndDate())
                    .dataSource(DataSource.WHITE_HOUSE_HISTORICAL)
                    .build();
            positionHoldingRepository.save(holding);
            result.vpHoldingsAdded++;
            log.debug("Created VP holding for {} {} ({} - {})",
                    entry.getFirstName(), entry.getLastName(),
                    entry.getStartDate(), entry.getEndDate());
        }
    }

    /**
     * Link predecessor/successor relationships between presidencies.
     */
    private void linkPresidencyChain() {
        log.debug("Linking presidency predecessor/successor chain");
        List<Presidency> all = presidencyRepository.findAllByOrderByNumberAsc();

        for (int i = 0; i < all.size(); i++) {
            Presidency current = all.get(i);
            boolean updated = false;

            // Set predecessor (if not first)
            if (i > 0 && current.getPredecessorId() == null) {
                current.setPredecessorId(all.get(i - 1).getId());
                updated = true;
            }

            // Set successor (if not last)
            if (i < all.size() - 1 && current.getSuccessorId() == null) {
                current.setSuccessorId(all.get(i + 1).getId());
                updated = true;
            }

            if (updated) {
                presidencyRepository.save(current);
            }
        }
    }

    /**
     * Get count of synced presidencies.
     */
    public long getPresidencyCount() {
        return presidencyRepository.count();
    }

    /**
     * Check if a specific presidency number exists.
     */
    public boolean presidencyExists(int number) {
        return presidencyRepository.existsByNumber(number);
    }
}
