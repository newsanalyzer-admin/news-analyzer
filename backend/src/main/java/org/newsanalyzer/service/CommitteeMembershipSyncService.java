package org.newsanalyzer.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.newsanalyzer.model.Committee;
import org.newsanalyzer.model.CommitteeMembership;
import org.newsanalyzer.model.MembershipRole;
import org.newsanalyzer.model.Person;
import org.newsanalyzer.repository.CommitteeMembershipRepository;
import org.newsanalyzer.repository.CommitteeRepository;
import org.newsanalyzer.repository.PersonRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service for synchronizing committee membership data from Congress.gov API.
 *
 * Handles syncing the relationship between members and committees,
 * including roles like Chair, Ranking Member, etc.
 *
 * @author James (Dev Agent)
 * @since 2.0.0
 */
@Service
@Transactional
public class CommitteeMembershipSyncService {

    private static final Logger log = LoggerFactory.getLogger(CommitteeMembershipSyncService.class);

    private final CongressApiClient congressApiClient;
    private final CommitteeMembershipRepository membershipRepository;
    private final CommitteeRepository committeeRepository;
    private final PersonRepository personRepository;
    private final ObjectMapper objectMapper;

    // Default to current Congress (can be configured)
    private static final int CURRENT_CONGRESS = 118;

    public CommitteeMembershipSyncService(CongressApiClient congressApiClient,
                                          CommitteeMembershipRepository membershipRepository,
                                          CommitteeRepository committeeRepository,
                                          PersonRepository personRepository,
                                          ObjectMapper objectMapper) {
        this.congressApiClient = congressApiClient;
        this.membershipRepository = membershipRepository;
        this.committeeRepository = committeeRepository;
        this.personRepository = personRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * Sync result statistics
     */
    public static class SyncResult {
        private int added;
        private int updated;
        private int skipped;
        private int errors;
        private int total;

        public int getAdded() { return added; }
        public int getUpdated() { return updated; }
        public int getSkipped() { return skipped; }
        public int getErrors() { return errors; }
        public int getTotal() { return total; }

        @Override
        public String toString() {
            return String.format("SyncResult{added=%d, updated=%d, skipped=%d, errors=%d, total=%d}",
                    added, updated, skipped, errors, total);
        }
    }

    /**
     * Sync memberships for all committees in the current Congress.
     *
     * @return SyncResult with statistics
     */
    public SyncResult syncAllMemberships() {
        return syncAllMemberships(CURRENT_CONGRESS);
    }

    /**
     * Sync memberships for all committees in a specific Congress.
     *
     * @param congress Congress session number (e.g., 118)
     * @return SyncResult with statistics
     */
    public SyncResult syncAllMemberships(int congress) {
        SyncResult result = new SyncResult();

        if (!congressApiClient.isConfigured()) {
            log.error("Congress.gov API key not configured. Set CONGRESS_API_KEY environment variable.");
            return result;
        }

        log.info("Starting sync of committee memberships for Congress {}", congress);

        // Get all committees
        List<Committee> committees = committeeRepository.findAll();
        result.total = committees.size();

        for (Committee committee : committees) {
            try {
                SyncResult committeeResult = syncMembershipsForCommittee(
                        committee.getCommitteeCode(),
                        getChamberApiValue(committee),
                        congress
                );
                result.added += committeeResult.added;
                result.updated += committeeResult.updated;
                result.skipped += committeeResult.skipped;
                result.errors += committeeResult.errors;

            } catch (Exception e) {
                result.errors++;
                log.error("Failed to sync memberships for committee {}: {}",
                        committee.getCommitteeCode(), e.getMessage());
            }
        }

        log.info("Membership sync completed: {}", result);
        return result;
    }

    /**
     * Sync memberships for a specific committee.
     *
     * @param committeeCode Committee system code
     * @param chamber Chamber API value ("house", "senate", "joint")
     * @param congress Congress session number
     * @return SyncResult with statistics
     */
    public SyncResult syncMembershipsForCommittee(String committeeCode, String chamber, int congress) {
        SyncResult result = new SyncResult();

        Optional<JsonNode> response = congressApiClient.fetchCommitteeByCode(chamber, committeeCode);

        if (response.isEmpty()) {
            log.warn("Failed to fetch committee details for {}", committeeCode);
            result.errors++;
            return result;
        }

        JsonNode committeeData = response.get().path("committee");
        if (committeeData.isMissingNode()) {
            log.warn("No committee data found for {}", committeeCode);
            result.errors++;
            return result;
        }

        // Get the committee entity
        Optional<Committee> committee = committeeRepository.findByCommitteeCode(committeeCode);
        if (committee.isEmpty()) {
            log.warn("Committee {} not found in database", committeeCode);
            result.skipped++;
            return result;
        }

        // Sync members from the committee data
        JsonNode members = committeeData.path("members");
        if (members.isArray()) {
            for (JsonNode memberData : members) {
                try {
                    boolean synced = syncMembership(memberData, committee.get(), congress);
                    if (synced) {
                        result.added++;
                    } else {
                        result.updated++;
                    }
                } catch (Exception e) {
                    log.debug("Skipped member sync: {}", e.getMessage());
                    result.skipped++;
                }
            }
            result.total = members.size();
        }

        return result;
    }

    /**
     * Sync a single membership from API data.
     *
     * @param memberData JSON data for the member on this committee
     * @param committee Committee entity
     * @param congress Congress session number
     * @return true if new membership created, false if updated
     */
    private boolean syncMembership(JsonNode memberData, Committee committee, int congress) {
        String bioguideId = memberData.path("bioguideId").asText();

        if (bioguideId == null || bioguideId.isEmpty()) {
            throw new IllegalArgumentException("Member data missing bioguideId");
        }

        // Find the Person
        Optional<Person> person = personRepository.findByBioguideId(bioguideId);
        if (person.isEmpty()) {
            throw new IllegalArgumentException("Person not found: " + bioguideId);
        }

        // Check if membership already exists
        Optional<CommitteeMembership> existing = membershipRepository
                .findByPerson_IdAndCommittee_CommitteeCodeAndCongress(
                        person.get().getId(),
                        committee.getCommitteeCode(),
                        congress
                );

        CommitteeMembership membership = existing.orElse(new CommitteeMembership());
        boolean isNew = existing.isEmpty();

        // Map data
        membership.setPerson(person.get());
        membership.setCommittee(committee);
        membership.setCongress(congress);
        membership.setRole(mapRole(memberData.path("role").asText()));
        membership.setCongressLastSync(LocalDateTime.now());
        membership.setDataSource("CONGRESS_GOV");

        // Map dates if available
        String startDateStr = getTextOrNull(memberData, "startDate");
        if (startDateStr != null) {
            try {
                membership.setStartDate(LocalDate.parse(startDateStr));
            } catch (Exception e) {
                log.debug("Could not parse start date: {}", startDateStr);
            }
        }

        String endDateStr = getTextOrNull(memberData, "endDate");
        if (endDateStr != null) {
            try {
                membership.setEndDate(LocalDate.parse(endDateStr));
            } catch (Exception e) {
                log.debug("Could not parse end date: {}", endDateStr);
            }
        }

        membershipRepository.save(membership);

        if (isNew) {
            log.debug("Added membership: {} on {} ({})",
                    bioguideId, committee.getName(), membership.getRole());
        } else {
            log.debug("Updated membership: {} on {} ({})",
                    bioguideId, committee.getName(), membership.getRole());
        }

        return isNew;
    }

    /**
     * Map role string to MembershipRole enum.
     */
    private MembershipRole mapRole(String roleStr) {
        if (roleStr == null || roleStr.isEmpty()) {
            return MembershipRole.MEMBER;
        }

        String normalized = roleStr.toUpperCase();
        if (normalized.contains("CHAIR") && !normalized.contains("VICE")) {
            return MembershipRole.CHAIR;
        } else if (normalized.contains("VICE") && normalized.contains("CHAIR")) {
            return MembershipRole.VICE_CHAIR;
        } else if (normalized.contains("RANKING")) {
            return MembershipRole.RANKING_MEMBER;
        } else if (normalized.contains("EX OFFICIO") || normalized.contains("EX-OFFICIO")) {
            return MembershipRole.EX_OFFICIO;
        }
        return MembershipRole.MEMBER;
    }

    /**
     * Get chamber API value from Committee entity.
     */
    private String getChamberApiValue(Committee committee) {
        switch (committee.getChamber()) {
            case SENATE:
                return "senate";
            case HOUSE:
                return "house";
            case JOINT:
                return "joint";
            default:
                return "house";
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
     * Get count of synced memberships.
     */
    public long getMembershipCount() {
        return membershipRepository.count();
    }

    /**
     * Get count of memberships for a specific congress.
     */
    public long getMembershipCountByCongress(int congress) {
        return membershipRepository.countByCongress(congress);
    }
}
