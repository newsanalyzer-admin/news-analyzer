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
 * - President Individual records
 * - Presidency records (linked to Individual)
 * - Vice President Individual records
 * - VP PositionHolding records (linked to Presidency)
 *
 * Handles non-consecutive terms (Cleveland 22/24, Trump 45/47) by
 * reusing the same Individual record for multiple Presidency entries.
 *
 * Part of ARCH-1.6: Updated to use Individual instead of Person.
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

    private final IndividualService individualService;
    private final PresidencyRepository presidencyRepository;
    private final GovernmentPositionRepository positionRepository;
    private final PositionHoldingRepository positionHoldingRepository;
    private final ObjectMapper objectMapper;

    // Cache for individual lookups during sync
    private final Map<String, Individual> individualCache = new HashMap<>();

    public PresidentialSyncService(IndividualService individualService,
                                   PresidencyRepository presidencyRepository,
                                   GovernmentPositionRepository positionRepository,
                                   PositionHoldingRepository positionHoldingRepository) {
        this.individualService = individualService;
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
        private int individualsAdded;
        private int individualsUpdated;
        private int vpHoldingsAdded;
        private int errors;
        private List<String> errorMessages = new ArrayList<>();

        public int getPresidenciesAdded() { return presidenciesAdded; }
        public int getPresidenciesUpdated() { return presidenciesUpdated; }
        public int getIndividualsAdded() { return individualsAdded; }
        public int getIndividualsUpdated() { return individualsUpdated; }
        public int getVpHoldingsAdded() { return vpHoldingsAdded; }
        public int getErrors() { return errors; }
        public List<String> getErrorMessages() { return errorMessages; }

        public int getTotalPresidencies() { return presidenciesAdded + presidenciesUpdated; }

        @Override
        public String toString() {
            return String.format("SyncResult{presidencies=%d (added=%d, updated=%d), " +
                            "individuals=%d (added=%d, updated=%d), vpHoldings=%d, errors=%d}",
                    getTotalPresidencies(), presidenciesAdded, presidenciesUpdated,
                    individualsAdded + individualsUpdated, individualsAdded, individualsUpdated,
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

            // Clear individual cache
            individualCache.clear();

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

        // 1. Create/update president Individual
        Individual president = syncIndividual(entry.getPresident(), entry.getPresidentKey(), result);

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
     * Sync an individual record (president or VP).
     *
     * Uses IndividualService.findOrCreate for deduplication, then updates
     * additional fields from the seed entry.
     */
    private Individual syncIndividual(PersonEntry entry, String personKey, SyncResult result) {
        // Check cache first (for non-consecutive terms)
        if (individualCache.containsKey(personKey)) {
            log.debug("Using cached individual: {}", personKey);
            return individualCache.get(personKey);
        }

        // Use IndividualService for findOrCreate with deduplication
        // First check if we need to determine if this is an update or create
        var existingOpt = individualService.findByNameAndBirthDate(
                entry.getFirstName(), entry.getLastName(), entry.getBirthDate());

        Individual individual;
        if (existingOpt.isPresent()) {
            individual = existingOpt.get();
            boolean updated = updateIndividual(individual, entry);
            if (updated) {
                individual = individualService.save(individual);
                result.individualsUpdated++;
                log.debug("Updated individual: {} {}", entry.getFirstName(), entry.getLastName());
            }
        } else {
            // Use findOrCreate with full details
            individual = individualService.findOrCreate(
                    entry.getFirstName(),
                    entry.getLastName(),
                    entry.getMiddleName(),
                    entry.getSuffix(),
                    entry.getBirthDate(),
                    entry.getBirthPlace(),
                    null, // gender
                    null, // party
                    DataSource.WHITE_HOUSE_HISTORICAL
            );

            // Update additional fields not handled by findOrCreate
            boolean updated = false;
            if (entry.getDeathDate() != null && individual.getDeathDate() == null) {
                individual.setDeathDate(entry.getDeathDate());
                updated = true;
            }
            if (entry.getImageUrl() != null && individual.getImageUrl() == null) {
                individual.setImageUrl(entry.getImageUrl());
                updated = true;
            }
            if (updated) {
                individual = individualService.save(individual);
            }
            result.individualsAdded++;
            log.debug("Created individual: {} {}", entry.getFirstName(), entry.getLastName());
        }

        // Cache for later lookups
        individualCache.put(personKey, individual);
        return individual;
    }

    /**
     * Update existing individual with seed data (only if fields are empty).
     *
     * @return true if any field was updated
     */
    private boolean updateIndividual(Individual individual, PersonEntry entry) {
        boolean updated = false;
        // Only update fields if currently null (don't overwrite existing data)
        if (individual.getBirthDate() == null && entry.getBirthDate() != null) {
            individual.setBirthDate(entry.getBirthDate());
            updated = true;
        }
        if (individual.getDeathDate() == null && entry.getDeathDate() != null) {
            individual.setDeathDate(entry.getDeathDate());
            updated = true;
        }
        if (individual.getBirthPlace() == null && entry.getBirthPlace() != null) {
            individual.setBirthPlace(entry.getBirthPlace());
            updated = true;
        }
        if (individual.getImageUrl() == null && entry.getImageUrl() != null) {
            individual.setImageUrl(entry.getImageUrl());
            updated = true;
        }
        if (individual.getMiddleName() == null && entry.getMiddleName() != null) {
            individual.setMiddleName(entry.getMiddleName());
            updated = true;
        }
        return updated;
    }

    /**
     * Sync a Presidency record.
     */
    private Presidency syncPresidencyRecord(PresidencyEntry entry, UUID individualId, SyncResult result) {
        Optional<Presidency> existing = presidencyRepository.findByNumber(entry.getNumber());

        Presidency presidency;
        if (existing.isPresent()) {
            presidency = existing.get();
            updatePresidency(presidency, entry, individualId);
            presidency = presidencyRepository.save(presidency);
            result.presidenciesUpdated++;
            log.debug("Updated presidency #{}", entry.getNumber());
        } else {
            presidency = createPresidency(entry, individualId);
            presidency = presidencyRepository.save(presidency);
            result.presidenciesAdded++;
            log.debug("Created presidency #{}", entry.getNumber());
        }

        return presidency;
    }

    /**
     * Create a new Presidency from seed entry.
     */
    private Presidency createPresidency(PresidencyEntry entry, UUID individualId) {
        PresidencyEndReason endReason = null;
        if (entry.getEndReason() != null) {
            try {
                endReason = PresidencyEndReason.valueOf(entry.getEndReason());
            } catch (IllegalArgumentException e) {
                log.warn("Unknown end reason: {}", entry.getEndReason());
            }
        }

        return Presidency.builder()
                .individualId(individualId)
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
    private void updatePresidency(Presidency presidency, PresidencyEntry entry, UUID individualId) {
        presidency.setIndividualId(individualId);
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
        // Create/update VP individual
        PersonEntry vpPersonEntry = new PersonEntry();
        vpPersonEntry.setFirstName(entry.getFirstName());
        vpPersonEntry.setLastName(entry.getLastName());
        vpPersonEntry.setMiddleName(entry.getMiddleName());

        Individual vpIndividual = syncIndividual(vpPersonEntry, entry.getPersonKey(), result);

        // Check if holding already exists for this individual, position, and date range
        // We use start date as unique identifier within a position
        boolean exists = positionHoldingRepository
                .findByIndividualIdAndPositionIdAndStartDate(vpIndividual.getId(), vpPositionId, entry.getStartDate())
                .isPresent();

        if (!exists) {
            PositionHolding holding = PositionHolding.builder()
                    .individualId(vpIndividual.getId())
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
