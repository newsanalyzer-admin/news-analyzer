package org.newsanalyzer.service;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.newsanalyzer.model.*;
import org.newsanalyzer.model.Person.Chamber;
import org.newsanalyzer.repository.GovernmentPositionRepository;
import org.newsanalyzer.repository.PersonRepository;
import org.newsanalyzer.repository.PositionHoldingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

/**
 * Service for syncing Congressional term history from Congress.gov API.
 *
 * Parses term data from member detail responses and creates PositionHolding records.
 * Filters to 1990s-present per scope requirements.
 *
 * @author James (Dev Agent)
 * @since 2.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TermSyncService {

    private static final int MIN_YEAR = 1990;  // Historical scope limit

    private final CongressApiClient congressApiClient;
    private final PersonRepository personRepository;
    private final GovernmentPositionRepository positionRepository;
    private final PositionHoldingRepository holdingRepository;
    private final PositionInitializationService positionInitService;

    /**
     * Sync terms for all current members in the database.
     *
     * @return SyncResult with statistics
     */
    @Transactional
    public SyncResult syncAllCurrentMemberTerms() {
        log.info("Starting term sync for all current members...");

        // Ensure positions are initialized
        positionInitService.initializeAllPositions();

        List<Person> members = personRepository.findAll();
        int processed = 0;
        int termsAdded = 0;
        int termsUpdated = 0;
        int errors = 0;

        for (Person member : members) {
            try {
                TermResult result = syncTermsForMember(member.getBioguideId());
                termsAdded += result.getAdded();
                termsUpdated += result.getUpdated();
                processed++;

                if (processed % 50 == 0) {
                    log.info("Processed {}/{} members, {} terms added, {} updated",
                            processed, members.size(), termsAdded, termsUpdated);
                }
            } catch (Exception e) {
                log.error("Failed to sync terms for member {}: {}", member.getBioguideId(), e.getMessage());
                errors++;
            }
        }

        log.info("Term sync complete: {} members processed, {} terms added, {} updated, {} errors",
                processed, termsAdded, termsUpdated, errors);

        return new SyncResult(processed, termsAdded, termsUpdated, errors);
    }

    /**
     * Sync terms for a specific member by BioGuide ID.
     *
     * @param bioguideId The BioGuide ID of the member
     * @return TermResult with counts
     */
    @Transactional
    public TermResult syncTermsForMember(String bioguideId) {
        Optional<Person> personOpt = personRepository.findByBioguideId(bioguideId);
        if (personOpt.isEmpty()) {
            log.warn("Person not found for bioguideId: {}", bioguideId);
            return new TermResult(0, 0);
        }

        Person person = personOpt.get();

        // Fetch member detail from Congress.gov API
        Optional<JsonNode> memberData = congressApiClient.fetchMemberByBioguideId(bioguideId);
        if (memberData.isEmpty()) {
            log.warn("Could not fetch member data from API for: {}", bioguideId);
            return new TermResult(0, 0);
        }

        JsonNode member = memberData.get().path("member");
        JsonNode termsNode = member.path("terms");

        // Handle both array and object with "item" array
        JsonNode termsArray;
        if (termsNode.has("item")) {
            termsArray = termsNode.path("item");
        } else if (termsNode.isArray()) {
            termsArray = termsNode;
        } else {
            log.debug("No terms found for member: {}", bioguideId);
            return new TermResult(0, 0);
        }

        int added = 0;
        int updated = 0;

        for (JsonNode termNode : termsArray) {
            int startYear = termNode.path("startYear").asInt(0);

            // Skip terms before our historical scope
            if (startYear < MIN_YEAR) {
                continue;
            }

            try {
                TermResult result = processTermNode(person, termNode);
                added += result.getAdded();
                updated += result.getUpdated();
            } catch (Exception e) {
                log.error("Failed to process term for {}: {}", bioguideId, e.getMessage());
            }
        }

        log.debug("Synced {} terms for {} ({} added, {} updated)",
                added + updated, bioguideId, added, updated);

        return new TermResult(added, updated);
    }

    /**
     * Process a single term node from the API response.
     */
    private TermResult processTermNode(Person person, JsonNode termNode) {
        String chamberStr = termNode.path("chamber").asText("");
        String stateCode = termNode.path("stateCode").asText(termNode.path("state").asText(""));
        Integer congress = termNode.has("congress") ? termNode.path("congress").asInt() : null;
        int startYear = termNode.path("startYear").asInt();
        Integer endYear = termNode.has("endYear") && !termNode.path("endYear").isNull()
                ? termNode.path("endYear").asInt() : null;

        // Determine chamber
        Chamber chamber;
        if ("Senate".equalsIgnoreCase(chamberStr)) {
            chamber = Chamber.SENATE;
        } else if ("House of Representatives".equalsIgnoreCase(chamberStr) || "House".equalsIgnoreCase(chamberStr)) {
            chamber = Chamber.HOUSE;
        } else {
            log.warn("Unknown chamber: {} for term", chamberStr);
            return new TermResult(0, 0);
        }

        // Find or determine position
        GovernmentPosition position = findPosition(chamber, stateCode, termNode);
        if (position == null) {
            log.warn("Could not find position for {} {} term", stateCode, chamber);
            return new TermResult(0, 0);
        }

        // Calculate dates from years
        LocalDate startDate = LocalDate.of(startYear, 1, 3);  // Congress starts Jan 3
        LocalDate endDate = endYear != null ? LocalDate.of(endYear, 1, 3) : null;

        // Check if holding already exists
        Optional<PositionHolding> existingOpt = holdingRepository
                .findByPersonIdAndPositionIdAndCongress(person.getId(), position.getId(), congress);

        if (existingOpt.isPresent()) {
            // Update existing holding if end date changed
            PositionHolding existing = existingOpt.get();
            if (!Objects.equals(existing.getEndDate(), endDate)) {
                existing.setEndDate(endDate);
                holdingRepository.save(existing);
                return new TermResult(0, 1);
            }
            return new TermResult(0, 0);  // No change
        }

        // Create new holding
        PositionHolding holding = PositionHolding.builder()
                .personId(person.getId())
                .positionId(position.getId())
                .startDate(startDate)
                .endDate(endDate)
                .congress(congress)
                .dataSource(DataSource.CONGRESS_GOV)
                .sourceReference("member/" + person.getBioguideId())
                .build();

        holdingRepository.save(holding);
        return new TermResult(1, 0);
    }

    /**
     * Find the position for a given chamber, state, and term data.
     */
    private GovernmentPosition findPosition(Chamber chamber, String state, JsonNode termNode) {
        if (chamber == Chamber.SENATE) {
            // For Senate, we need to determine the class
            // Congress.gov doesn't always provide class directly, so we use the existing position
            List<GovernmentPosition> senatePositions = positionRepository
                    .findByChamberAndState(Chamber.SENATE, state);

            if (senatePositions.isEmpty()) {
                return null;
            }

            // Return first available - in practice, we'd need more logic to determine class
            // For now, just return the first one since we're tracking by congress number
            return senatePositions.get(0);

        } else {
            // For House, use district
            int district = termNode.path("district").asInt(0);  // 0 for at-large

            return positionRepository
                    .findByChamberAndStateAndDistrict(Chamber.HOUSE, state, district)
                    .orElse(null);
        }
    }

    // =====================================================================
    // Result classes
    // =====================================================================

    public static class SyncResult {
        private final int membersProcessed;
        private final int termsAdded;
        private final int termsUpdated;
        private final int errors;

        public SyncResult(int membersProcessed, int termsAdded, int termsUpdated, int errors) {
            this.membersProcessed = membersProcessed;
            this.termsAdded = termsAdded;
            this.termsUpdated = termsUpdated;
            this.errors = errors;
        }

        public int getMembersProcessed() { return membersProcessed; }
        public int getTermsAdded() { return termsAdded; }
        public int getTermsUpdated() { return termsUpdated; }
        public int getErrors() { return errors; }
        public int getTotalTerms() { return termsAdded + termsUpdated; }
    }

    public static class TermResult {
        private final int added;
        private final int updated;

        public TermResult(int added, int updated) {
            this.added = added;
            this.updated = updated;
        }

        public int getAdded() { return added; }
        public int getUpdated() { return updated; }
    }
}
