package org.newsanalyzer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.newsanalyzer.model.GovernmentOrganization;
import org.newsanalyzer.model.GovernmentPosition;
import org.newsanalyzer.model.Person.Chamber;
import org.newsanalyzer.model.PositionType;
import org.newsanalyzer.repository.GovernmentOrganizationRepository;
import org.newsanalyzer.repository.GovernmentPositionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Service for initializing Congressional positions (Senate and House seats).
 *
 * Creates all 100 Senate seats (2 per state, each with a class) and
 * all 435 House seats (varying by state based on apportionment).
 *
 * @author James (Dev Agent)
 * @since 2.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PositionInitializationService {

    private final GovernmentPositionRepository positionRepository;
    private final GovernmentOrganizationRepository governmentOrgRepository;

    // US States and their 2-letter codes
    private static final List<String> STATES = Arrays.asList(
            "AL", "AK", "AZ", "AR", "CA", "CO", "CT", "DE", "FL", "GA",
            "HI", "ID", "IL", "IN", "IA", "KS", "KY", "LA", "ME", "MD",
            "MA", "MI", "MN", "MS", "MO", "MT", "NE", "NV", "NH", "NJ",
            "NM", "NY", "NC", "ND", "OH", "OK", "OR", "PA", "RI", "SC",
            "SD", "TN", "TX", "UT", "VT", "VA", "WA", "WV", "WI", "WY"
    );

    // House districts per state (2020 apportionment, 118th Congress)
    private static final Map<String, Integer> HOUSE_DISTRICTS = new LinkedHashMap<>();
    static {
        HOUSE_DISTRICTS.put("AL", 7);
        HOUSE_DISTRICTS.put("AK", 1);
        HOUSE_DISTRICTS.put("AZ", 9);
        HOUSE_DISTRICTS.put("AR", 4);
        HOUSE_DISTRICTS.put("CA", 52);
        HOUSE_DISTRICTS.put("CO", 8);
        HOUSE_DISTRICTS.put("CT", 5);
        HOUSE_DISTRICTS.put("DE", 1);
        HOUSE_DISTRICTS.put("FL", 28);
        HOUSE_DISTRICTS.put("GA", 14);
        HOUSE_DISTRICTS.put("HI", 2);
        HOUSE_DISTRICTS.put("ID", 2);
        HOUSE_DISTRICTS.put("IL", 17);
        HOUSE_DISTRICTS.put("IN", 9);
        HOUSE_DISTRICTS.put("IA", 4);
        HOUSE_DISTRICTS.put("KS", 4);
        HOUSE_DISTRICTS.put("KY", 6);
        HOUSE_DISTRICTS.put("LA", 6);
        HOUSE_DISTRICTS.put("ME", 2);
        HOUSE_DISTRICTS.put("MD", 8);
        HOUSE_DISTRICTS.put("MA", 9);
        HOUSE_DISTRICTS.put("MI", 13);
        HOUSE_DISTRICTS.put("MN", 8);
        HOUSE_DISTRICTS.put("MS", 4);
        HOUSE_DISTRICTS.put("MO", 8);
        HOUSE_DISTRICTS.put("MT", 2);
        HOUSE_DISTRICTS.put("NE", 3);
        HOUSE_DISTRICTS.put("NV", 4);
        HOUSE_DISTRICTS.put("NH", 2);
        HOUSE_DISTRICTS.put("NJ", 12);
        HOUSE_DISTRICTS.put("NM", 3);
        HOUSE_DISTRICTS.put("NY", 26);
        HOUSE_DISTRICTS.put("NC", 14);
        HOUSE_DISTRICTS.put("ND", 1);
        HOUSE_DISTRICTS.put("OH", 15);
        HOUSE_DISTRICTS.put("OK", 5);
        HOUSE_DISTRICTS.put("OR", 6);
        HOUSE_DISTRICTS.put("PA", 17);
        HOUSE_DISTRICTS.put("RI", 2);
        HOUSE_DISTRICTS.put("SC", 7);
        HOUSE_DISTRICTS.put("SD", 1);
        HOUSE_DISTRICTS.put("TN", 9);
        HOUSE_DISTRICTS.put("TX", 38);
        HOUSE_DISTRICTS.put("UT", 4);
        HOUSE_DISTRICTS.put("VT", 1);
        HOUSE_DISTRICTS.put("VA", 11);
        HOUSE_DISTRICTS.put("WA", 10);
        HOUSE_DISTRICTS.put("WV", 2);
        HOUSE_DISTRICTS.put("WI", 8);
        HOUSE_DISTRICTS.put("WY", 1);
    }

    // Senate class assignments by state (Class 1, 2, or 3)
    // Based on historical assignments
    private static final Map<String, int[]> SENATE_CLASSES = new LinkedHashMap<>();
    static {
        // Each state has two senators in different classes
        SENATE_CLASSES.put("AL", new int[]{2, 3});
        SENATE_CLASSES.put("AK", new int[]{2, 3});
        SENATE_CLASSES.put("AZ", new int[]{1, 3});
        SENATE_CLASSES.put("AR", new int[]{2, 3});
        SENATE_CLASSES.put("CA", new int[]{1, 3});
        SENATE_CLASSES.put("CO", new int[]{2, 3});
        SENATE_CLASSES.put("CT", new int[]{1, 3});
        SENATE_CLASSES.put("DE", new int[]{1, 2});
        SENATE_CLASSES.put("FL", new int[]{1, 3});
        SENATE_CLASSES.put("GA", new int[]{2, 3});
        SENATE_CLASSES.put("HI", new int[]{1, 3});
        SENATE_CLASSES.put("ID", new int[]{2, 3});
        SENATE_CLASSES.put("IL", new int[]{2, 3});
        SENATE_CLASSES.put("IN", new int[]{1, 3});
        SENATE_CLASSES.put("IA", new int[]{2, 3});
        SENATE_CLASSES.put("KS", new int[]{2, 3});
        SENATE_CLASSES.put("KY", new int[]{2, 3});
        SENATE_CLASSES.put("LA", new int[]{2, 3});
        SENATE_CLASSES.put("ME", new int[]{1, 2});
        SENATE_CLASSES.put("MD", new int[]{1, 3});
        SENATE_CLASSES.put("MA", new int[]{1, 2});
        SENATE_CLASSES.put("MI", new int[]{1, 2});
        SENATE_CLASSES.put("MN", new int[]{1, 2});
        SENATE_CLASSES.put("MS", new int[]{1, 2});
        SENATE_CLASSES.put("MO", new int[]{1, 3});
        SENATE_CLASSES.put("MT", new int[]{1, 2});
        SENATE_CLASSES.put("NE", new int[]{1, 2});
        SENATE_CLASSES.put("NV", new int[]{1, 3});
        SENATE_CLASSES.put("NH", new int[]{2, 3});
        SENATE_CLASSES.put("NJ", new int[]{1, 2});
        SENATE_CLASSES.put("NM", new int[]{1, 2});
        SENATE_CLASSES.put("NY", new int[]{1, 3});
        SENATE_CLASSES.put("NC", new int[]{2, 3});
        SENATE_CLASSES.put("ND", new int[]{1, 3});
        SENATE_CLASSES.put("OH", new int[]{1, 3});
        SENATE_CLASSES.put("OK", new int[]{2, 3});
        SENATE_CLASSES.put("OR", new int[]{2, 3});
        SENATE_CLASSES.put("PA", new int[]{1, 3});
        SENATE_CLASSES.put("RI", new int[]{1, 2});
        SENATE_CLASSES.put("SC", new int[]{2, 3});
        SENATE_CLASSES.put("SD", new int[]{2, 3});
        SENATE_CLASSES.put("TN", new int[]{1, 2});
        SENATE_CLASSES.put("TX", new int[]{1, 2});
        SENATE_CLASSES.put("UT", new int[]{1, 3});
        SENATE_CLASSES.put("VT", new int[]{1, 3});
        SENATE_CLASSES.put("VA", new int[]{1, 2});
        SENATE_CLASSES.put("WA", new int[]{1, 3});
        SENATE_CLASSES.put("WV", new int[]{1, 2});
        SENATE_CLASSES.put("WI", new int[]{1, 3});
        SENATE_CLASSES.put("WY", new int[]{1, 2});
    }

    /**
     * Initialize all Congressional positions if not already present.
     *
     * @return InitResult with counts of created positions
     */
    @Transactional
    public InitResult initializeAllPositions() {
        log.info("Starting Congressional position initialization...");

        // Find Congress organization for linking
        UUID congressOrgId = findCongressOrganizationId();

        int senateCreated = initializeSenatePositions(congressOrgId);
        int houseCreated = initializeHousePositions(congressOrgId);

        log.info("Position initialization complete: {} Senate, {} House positions created",
                senateCreated, houseCreated);

        return new InitResult(senateCreated, houseCreated);
    }

    /**
     * Initialize all 100 Senate positions (2 per state with different classes).
     */
    @Transactional
    public int initializeSenatePositions(UUID congressOrgId) {
        int created = 0;

        for (String state : STATES) {
            int[] classes = SENATE_CLASSES.get(state);
            for (int senateClass : classes) {
                if (!positionRepository.findByChamberAndStateAndSenateClass(
                        Chamber.SENATE, state, senateClass).isPresent()) {

                    GovernmentPosition position = GovernmentPosition.builder()
                            .title("Senator")
                            .chamber(Chamber.SENATE)
                            .state(state)
                            .senateClass(senateClass)
                            .positionType(PositionType.ELECTED)
                            .organizationId(congressOrgId)
                            .description(String.format("U.S. Senator from %s (Class %d)", state, senateClass))
                            .build();

                    positionRepository.save(position);
                    created++;
                    log.debug("Created Senate position: {} Class {}", state, senateClass);
                }
            }
        }

        log.info("Senate positions: {} created, {} total expected",
                created, STATES.size() * 2);
        return created;
    }

    /**
     * Initialize all 435 House positions (varying districts per state).
     */
    @Transactional
    public int initializeHousePositions(UUID congressOrgId) {
        int created = 0;

        for (Map.Entry<String, Integer> entry : HOUSE_DISTRICTS.entrySet()) {
            String state = entry.getKey();
            int numDistricts = entry.getValue();

            for (int district = 1; district <= numDistricts; district++) {
                // For at-large states (1 district), use district 0
                int districtNum = numDistricts == 1 ? 0 : district;

                if (!positionRepository.findByChamberAndStateAndDistrict(
                        Chamber.HOUSE, state, districtNum).isPresent()) {

                    String description = numDistricts == 1
                            ? String.format("U.S. Representative from %s (At-Large)", state)
                            : String.format("U.S. Representative from %s District %d", state, district);

                    GovernmentPosition position = GovernmentPosition.builder()
                            .title("Representative")
                            .chamber(Chamber.HOUSE)
                            .state(state)
                            .district(districtNum)
                            .positionType(PositionType.ELECTED)
                            .organizationId(congressOrgId)
                            .description(description)
                            .build();

                    positionRepository.save(position);
                    created++;
                    log.debug("Created House position: {} District {}", state, districtNum);
                }
            }
        }

        int totalExpected = HOUSE_DISTRICTS.values().stream().mapToInt(Integer::intValue).sum();
        log.info("House positions: {} created, {} total expected", created, totalExpected);
        return created;
    }

    /**
     * Find the Congress organization ID for linking positions.
     */
    private UUID findCongressOrganizationId() {
        // Try to find by name patterns
        return governmentOrgRepository.findByOfficialNameContainingIgnoreCase("Congress")
                .stream()
                .filter(org -> org.getBranch() == GovernmentOrganization.GovernmentBranch.LEGISLATIVE)
                .findFirst()
                .map(GovernmentOrganization::getId)
                .orElseGet(() -> {
                    log.warn("Congress organization not found in database, positions will not be linked");
                    return null;
                });
    }

    /**
     * Get statistics about initialized positions.
     */
    public PositionStats getPositionStats() {
        long senateCount = positionRepository.countByChamber(Chamber.SENATE);
        long houseCount = positionRepository.countByChamber(Chamber.HOUSE);
        return new PositionStats(senateCount, houseCount);
    }

    // =====================================================================
    // Result classes
    // =====================================================================

    public static class InitResult {
        private final int senateCreated;
        private final int houseCreated;

        public InitResult(int senateCreated, int houseCreated) {
            this.senateCreated = senateCreated;
            this.houseCreated = houseCreated;
        }

        public int getSenateCreated() { return senateCreated; }
        public int getHouseCreated() { return houseCreated; }
        public int getTotalCreated() { return senateCreated + houseCreated; }
    }

    public static class PositionStats {
        private final long senateCount;
        private final long houseCount;

        public PositionStats(long senateCount, long houseCount) {
            this.senateCount = senateCount;
            this.houseCount = houseCount;
        }

        public long getSenateCount() { return senateCount; }
        public long getHouseCount() { return houseCount; }
        public long getTotalCount() { return senateCount + houseCount; }
    }
}
