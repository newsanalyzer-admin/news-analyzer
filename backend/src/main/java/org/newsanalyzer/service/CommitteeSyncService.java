package org.newsanalyzer.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.newsanalyzer.model.Committee;
import org.newsanalyzer.model.CommitteeChamber;
import org.newsanalyzer.model.CommitteeType;
import org.newsanalyzer.repository.CommitteeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service for synchronizing Congressional committee data from Congress.gov API.
 *
 * Handles full sync and incremental updates of committee data including subcommittees.
 *
 * @author James (Dev Agent)
 * @since 2.0.0
 */
@Service
public class CommitteeSyncService {

    private static final Logger log = LoggerFactory.getLogger(CommitteeSyncService.class);

    private final CongressApiClient congressApiClient;
    private final CommitteeRepository committeeRepository;
    private final ObjectMapper objectMapper;

    @PersistenceContext
    private EntityManager entityManager;

    public CommitteeSyncService(CongressApiClient congressApiClient,
                                CommitteeRepository committeeRepository,
                                ObjectMapper objectMapper) {
        this.congressApiClient = congressApiClient;
        this.committeeRepository = committeeRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * Sync result statistics
     */
    public static class SyncResult {
        private int added;
        private int updated;
        private int subcommitteesAdded;
        private int subcommitteesUpdated;
        private int errors;
        private int total;

        public int getAdded() { return added; }
        public int getUpdated() { return updated; }
        public int getSubcommitteesAdded() { return subcommitteesAdded; }
        public int getSubcommitteesUpdated() { return subcommitteesUpdated; }
        public int getErrors() { return errors; }
        public int getTotal() { return total; }

        @Override
        public String toString() {
            return String.format("SyncResult{added=%d, updated=%d, subcommitteesAdded=%d, subcommitteesUpdated=%d, errors=%d, total=%d}",
                    added, updated, subcommitteesAdded, subcommitteesUpdated, errors, total);
        }
    }

    /**
     * Perform full sync of all committees from all chambers.
     *
     * @return SyncResult with statistics
     */
    public SyncResult syncAllCommittees() {
        SyncResult result = new SyncResult();

        if (!congressApiClient.isConfigured()) {
            log.error("Congress.gov API key not configured. Set CONGRESS_API_KEY environment variable.");
            return result;
        }

        log.info("Starting full sync of Congressional committees");

        // Sync House committees
        SyncResult houseResult = syncCommitteesByChamber("house", CommitteeChamber.HOUSE);
        result.added += houseResult.added;
        result.updated += houseResult.updated;
        result.subcommitteesAdded += houseResult.subcommitteesAdded;
        result.subcommitteesUpdated += houseResult.subcommitteesUpdated;
        result.errors += houseResult.errors;
        result.total += houseResult.total;

        // Sync Senate committees
        SyncResult senateResult = syncCommitteesByChamber("senate", CommitteeChamber.SENATE);
        result.added += senateResult.added;
        result.updated += senateResult.updated;
        result.subcommitteesAdded += senateResult.subcommitteesAdded;
        result.subcommitteesUpdated += senateResult.subcommitteesUpdated;
        result.errors += senateResult.errors;
        result.total += senateResult.total;

        // Sync Joint committees
        SyncResult jointResult = syncCommitteesByChamber("joint", CommitteeChamber.JOINT);
        result.added += jointResult.added;
        result.updated += jointResult.updated;
        result.subcommitteesAdded += jointResult.subcommitteesAdded;
        result.subcommitteesUpdated += jointResult.subcommitteesUpdated;
        result.errors += jointResult.errors;
        result.total += jointResult.total;

        log.info("Committee sync completed: {}", result);
        return result;
    }

    /**
     * Sync committees for a specific chamber.
     *
     * @param chamberApiValue API value for chamber ("house", "senate", "joint")
     * @param chamber CommitteeChamber enum value
     * @return SyncResult with statistics
     */
    public SyncResult syncCommitteesByChamber(String chamberApiValue, CommitteeChamber chamber) {
        SyncResult result = new SyncResult();

        log.info("Starting sync of {} committees", chamberApiValue);
        List<JsonNode> committees = congressApiClient.fetchAllCommitteesByChamber(chamberApiValue);
        result.total = committees.size();

        // First pass: sync parent committees
        Map<String, Committee> parentCommittees = new HashMap<>();
        List<JsonNode> subcommitteesToSync = new ArrayList<>();

        for (JsonNode committeeData : committees) {
            try {
                String systemCode = committeeData.path("systemCode").asText();
                String type = committeeData.path("committeeTypeCode").asText();

                // If it's a subcommittee, save for second pass
                if ("subcommittee".equalsIgnoreCase(type) ||
                    committeeData.path("parent").isObject()) {
                    subcommitteesToSync.add(committeeData);
                    continue;
                }

                boolean isNew = syncCommittee(committeeData, chamber, null);
                if (isNew) {
                    result.added++;
                } else {
                    result.updated++;
                }

                // Cache parent committee for subcommittee linking
                committeeRepository.findByCommitteeCode(systemCode)
                        .ifPresent(c -> parentCommittees.put(systemCode, c));

            } catch (Exception e) {
                result.errors++;
                log.error("Failed to sync committee: {}", e.getMessage());
                // Clear persistence context to recover from failed transaction
                entityManager.clear();
            }
        }

        // Second pass: sync subcommittees with parent references
        for (JsonNode subcommitteeData : subcommitteesToSync) {
            try {
                // Find parent committee code
                String parentCode = null;
                JsonNode parent = subcommitteeData.path("parent");
                if (parent.isObject()) {
                    parentCode = parent.path("systemCode").asText();
                }

                Committee parentCommittee = parentCode != null ?
                        parentCommittees.getOrDefault(parentCode,
                                committeeRepository.findByCommitteeCode(parentCode).orElse(null)) :
                        null;

                boolean isNew = syncCommittee(subcommitteeData, chamber, parentCommittee);
                if (isNew) {
                    result.subcommitteesAdded++;
                } else {
                    result.subcommitteesUpdated++;
                }

            } catch (Exception e) {
                result.errors++;
                log.error("Failed to sync subcommittee: {}", e.getMessage());
                // Clear persistence context to recover from failed transaction
                entityManager.clear();
            }
        }

        log.info("{} committee sync completed: {} added, {} updated, {} subcommittees added, {} subcommittees updated, {} errors",
                chamberApiValue, result.added, result.updated,
                result.subcommitteesAdded, result.subcommitteesUpdated, result.errors);

        return result;
    }

    /**
     * Sync a single committee from API data.
     * Each committee is saved in its own transaction to isolate failures.
     *
     * @param committeeData JSON data from Congress.gov API
     * @param chamber Chamber enum value
     * @param parentCommittee Parent committee if this is a subcommittee
     * @return true if new record was created, false if updated
     */
    @Transactional
    public boolean syncCommittee(JsonNode committeeData, CommitteeChamber chamber, Committee parentCommittee) {
        String committeeCode = committeeData.path("systemCode").asText();

        if (committeeCode == null || committeeCode.isEmpty()) {
            throw new IllegalArgumentException("Committee data missing systemCode");
        }

        Optional<Committee> existingCommittee = committeeRepository.findByCommitteeCode(committeeCode);
        Committee committee = existingCommittee.orElse(new Committee());
        boolean isNew = existingCommittee.isEmpty();

        // Map API response to Committee entity
        mapCommitteeDataToEntity(committeeData, committee, chamber, parentCommittee);
        committee.setCongressLastSync(LocalDateTime.now());

        committeeRepository.save(committee);

        if (isNew) {
            log.debug("Added new committee: {} ({})", committee.getName(), committee.getCommitteeCode());
        } else {
            log.debug("Updated committee: {} ({})", committee.getName(), committee.getCommitteeCode());
        }

        return isNew;
    }

    /**
     * Sync a specific committee by code.
     *
     * @param chamber Chamber API value ("house", "senate", "joint")
     * @param committeeCode Committee system code
     * @return Optional containing the synced Committee, or empty if fetch failed
     */
    public Optional<Committee> syncCommitteeByCode(String chamber, String committeeCode) {
        if (!congressApiClient.isConfigured()) {
            log.error("Congress.gov API key not configured");
            return Optional.empty();
        }

        Optional<JsonNode> response = congressApiClient.fetchCommitteeByCode(chamber, committeeCode);

        if (response.isPresent()) {
            JsonNode committeeData = response.get().path("committee");
            if (!committeeData.isMissingNode()) {
                CommitteeChamber chamberEnum = mapChamber(chamber);
                syncCommittee(committeeData, chamberEnum, null);
                return committeeRepository.findByCommitteeCode(committeeCode);
            }
        }

        return Optional.empty();
    }

    /**
     * Map Congress.gov API response to Committee entity.
     */
    private void mapCommitteeDataToEntity(JsonNode data, Committee committee,
                                          CommitteeChamber chamber, Committee parentCommittee) {
        committee.setCommitteeCode(data.path("systemCode").asText());
        committee.setName(getTextOrNull(data, "name"));
        committee.setChamber(chamber);
        committee.setCommitteeType(mapCommitteeType(getTextOrNull(data, "committeeTypeCode")));
        committee.setParentCommittee(parentCommittee);

        // Map Thomas ID if available
        committee.setThomasId(getTextOrNull(data, "thomasId"));

        // Map URL if available
        committee.setUrl(getTextOrNull(data, "url"));

        // Set data source
        committee.setDataSource("CONGRESS_GOV");
    }

    /**
     * Map chamber string to CommitteeChamber enum.
     */
    private CommitteeChamber mapChamber(String chamberStr) {
        if (chamberStr == null) return null;

        String normalized = chamberStr.toLowerCase();
        switch (normalized) {
            case "senate":
                return CommitteeChamber.SENATE;
            case "house":
                return CommitteeChamber.HOUSE;
            case "joint":
                return CommitteeChamber.JOINT;
            default:
                return null;
        }
    }

    /**
     * Map committee type string to CommitteeType enum.
     */
    private CommitteeType mapCommitteeType(String typeStr) {
        if (typeStr == null) return CommitteeType.OTHER;

        String normalized = typeStr.toUpperCase();
        switch (normalized) {
            case "STANDING":
                return CommitteeType.STANDING;
            case "SELECT":
                return CommitteeType.SELECT;
            case "SPECIAL":
                return CommitteeType.SPECIAL;
            case "JOINT":
                return CommitteeType.JOINT;
            case "SUBCOMMITTEE":
                return CommitteeType.SUBCOMMITTEE;
            default:
                return CommitteeType.OTHER;
        }
    }

    /**
     * Get text value from JSON or null if missing.
     */
    private String getTextOrNull(JsonNode node, String field) {
        JsonNode value = node.path(field);
        if (value.isMissingNode() || value.isNull()) {
            return null;
        }
        String text = value.asText();
        return text.isEmpty() ? null : text;
    }

    /**
     * Get count of synced committees.
     */
    public long getCommitteeCount() {
        return committeeRepository.count();
    }

    /**
     * Get count of synced subcommittees.
     */
    public long getSubcommitteeCount() {
        return committeeRepository.countSubcommittees();
    }
}
