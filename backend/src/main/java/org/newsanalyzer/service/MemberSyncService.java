package org.newsanalyzer.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.newsanalyzer.model.DataSource;
import org.newsanalyzer.model.Person;
import org.newsanalyzer.model.Person.Chamber;
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
 * Service for synchronizing Congressional member data from Congress.gov API.
 *
 * Handles full sync and incremental updates of member data.
 *
 * @author James (Dev Agent)
 * @since 2.0.0
 */
@Service
@Transactional
public class MemberSyncService {

    private static final Logger log = LoggerFactory.getLogger(MemberSyncService.class);

    private final CongressApiClient congressApiClient;
    private final PersonRepository personRepository;
    private final ObjectMapper objectMapper;

    public MemberSyncService(CongressApiClient congressApiClient,
                             PersonRepository personRepository,
                             ObjectMapper objectMapper) {
        this.congressApiClient = congressApiClient;
        this.personRepository = personRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * Sync result statistics
     */
    public static class SyncResult {
        private int added;
        private int updated;
        private int errors;
        private int total;

        public int getAdded() { return added; }
        public int getUpdated() { return updated; }
        public int getErrors() { return errors; }
        public int getTotal() { return total; }

        public void setAdded(int added) { this.added = added; }
        public void setUpdated(int updated) { this.updated = updated; }
        public void setErrors(int errors) { this.errors = errors; }
        public void setTotal(int total) { this.total = total; }

        @Override
        public String toString() {
            return String.format("SyncResult{added=%d, updated=%d, errors=%d, total=%d}",
                    added, updated, errors, total);
        }
    }

    /**
     * Perform full sync of all current members.
     *
     * @return SyncResult with statistics
     */
    public SyncResult syncAllCurrentMembers() {
        SyncResult result = new SyncResult();

        if (!congressApiClient.isConfigured()) {
            log.error("Congress.gov API key not configured. Set CONGRESS_API_KEY environment variable.");
            return result;
        }

        log.info("Starting full sync of current Congress members");
        List<JsonNode> members = congressApiClient.fetchAllCurrentMembers();
        result.total = members.size();

        for (JsonNode memberData : members) {
            try {
                boolean isNew = syncMember(memberData);
                if (isNew) {
                    result.added++;
                } else {
                    result.updated++;
                }
            } catch (Exception e) {
                result.errors++;
                log.error("Failed to sync member: {}", e.getMessage());
            }
        }

        log.info("Sync completed: {}", result);
        return result;
    }

    /**
     * Sync a single member from API data.
     *
     * @param memberData JSON data from Congress.gov API
     * @return true if new record was created, false if updated
     */
    public boolean syncMember(JsonNode memberData) {
        String bioguideId = memberData.path("bioguideId").asText();

        if (bioguideId == null || bioguideId.isEmpty()) {
            throw new IllegalArgumentException("Member data missing bioguideId");
        }

        Optional<Person> existingPerson = personRepository.findByBioguideId(bioguideId);
        Person person = existingPerson.orElse(new Person());
        boolean isNew = existingPerson.isEmpty();

        // Map API response to Person entity
        mapMemberDataToPerson(memberData, person);
        person.setCongressLastSync(LocalDateTime.now());

        personRepository.save(person);

        if (isNew) {
            log.debug("Added new member: {} {}", person.getFirstName(), person.getLastName());
        } else {
            log.debug("Updated member: {} {}", person.getFirstName(), person.getLastName());
        }

        return isNew;
    }

    /**
     * Sync a specific member by BioGuide ID.
     *
     * @param bioguideId BioGuide ID to sync
     * @return Optional containing the synced Person, or empty if fetch failed
     */
    public Optional<Person> syncMemberByBioguideId(String bioguideId) {
        if (!congressApiClient.isConfigured()) {
            log.error("Congress.gov API key not configured");
            return Optional.empty();
        }

        Optional<JsonNode> response = congressApiClient.fetchMemberByBioguideId(bioguideId);

        if (response.isPresent()) {
            JsonNode memberData = response.get().path("member");
            if (!memberData.isMissingNode()) {
                syncMember(memberData);
                return personRepository.findByBioguideId(bioguideId);
            }
        }

        return Optional.empty();
    }

    /**
     * Map Congress.gov API response to Person entity.
     *
     * API returns: name (full), partyName, state, terms[].chamber, depiction.imageUrl
     * Not: firstName, lastName, party (separate fields)
     */
    private void mapMemberDataToPerson(JsonNode data, Person person) {
        person.setBioguideId(data.path("bioguideId").asText());

        // Parse full name into first/last name
        // Format is typically "LastName, FirstName MiddleName" or just "LastName, FirstName"
        String fullName = getTextOrNull(data, "name");
        if (fullName != null) {
            parseAndSetName(fullName, person);
        }

        // API uses "partyName" not "party"
        person.setParty(getTextOrNull(data, "partyName"));

        // State - API returns full state name, need to map to 2-letter code
        String stateStr = getTextOrNull(data, "state");
        if (stateStr != null) {
            person.setState(mapStateToCode(stateStr));
        }

        // Map chamber from terms.item array
        JsonNode terms = data.path("terms");
        if (!terms.isMissingNode()) {
            JsonNode termItems = terms.path("item");
            if (termItems.isArray() && termItems.size() > 0) {
                // Get most recent term (last in array)
                JsonNode latestTerm = termItems.get(termItems.size() - 1);
                String chamberStr = getTextOrNull(latestTerm, "chamber");
                if (chamberStr != null) {
                    person.setChamber(mapChamber(chamberStr));
                }
            }
        }

        // Map birth date from birthYear
        String birthYear = getTextOrNull(data, "birthYear");
        if (birthYear != null && !birthYear.isEmpty()) {
            try {
                int year = Integer.parseInt(birthYear);
                person.setBirthDate(LocalDate.of(year, 1, 1));
            } catch (NumberFormatException e) {
                log.warn("Invalid birth year: {}", birthYear);
            }
        }

        // Map image URL from depiction
        JsonNode depiction = data.path("depiction");
        if (!depiction.isMissingNode()) {
            String imageUrl = getTextOrNull(depiction, "imageUrl");
            person.setImageUrl(imageUrl);
        }

        // Map gender if available
        person.setGender(getTextOrNull(data, "gender"));

        // Set data source
        person.setDataSource(DataSource.CONGRESS_GOV);
    }

    /**
     * Parse full name in "LastName, FirstName MiddleName" format.
     */
    private void parseAndSetName(String fullName, Person person) {
        if (fullName == null || fullName.isEmpty()) {
            return;
        }

        // Format: "LastName, FirstName MiddleName" or "LastName, FirstName"
        int commaIndex = fullName.indexOf(',');
        if (commaIndex > 0) {
            String lastName = fullName.substring(0, commaIndex).trim();
            String rest = fullName.substring(commaIndex + 1).trim();

            person.setLastName(lastName);

            // Split remaining into first name and optional middle name
            String[] parts = rest.split("\\s+", 2);
            if (parts.length > 0) {
                person.setFirstName(parts[0]);
            }
            if (parts.length > 1) {
                person.setMiddleName(parts[1]);
            }
        } else {
            // No comma - try splitting by space (FirstName LastName format)
            String[] parts = fullName.split("\\s+");
            if (parts.length >= 2) {
                person.setFirstName(parts[0]);
                person.setLastName(parts[parts.length - 1]);
                if (parts.length > 2) {
                    // Middle parts
                    StringBuilder middle = new StringBuilder();
                    for (int i = 1; i < parts.length - 1; i++) {
                        if (middle.length() > 0) middle.append(" ");
                        middle.append(parts[i]);
                    }
                    person.setMiddleName(middle.toString());
                }
            } else if (parts.length == 1) {
                person.setLastName(parts[0]);
                person.setFirstName("Unknown");
            }
        }
    }

    /**
     * Map state name to 2-letter code.
     */
    private String mapStateToCode(String state) {
        if (state == null || state.length() == 2) {
            return state; // Already a code or null
        }

        // Common state mappings
        return switch (state.toUpperCase()) {
            case "ALABAMA" -> "AL";
            case "ALASKA" -> "AK";
            case "ARIZONA" -> "AZ";
            case "ARKANSAS" -> "AR";
            case "CALIFORNIA" -> "CA";
            case "COLORADO" -> "CO";
            case "CONNECTICUT" -> "CT";
            case "DELAWARE" -> "DE";
            case "FLORIDA" -> "FL";
            case "GEORGIA" -> "GA";
            case "HAWAII" -> "HI";
            case "IDAHO" -> "ID";
            case "ILLINOIS" -> "IL";
            case "INDIANA" -> "IN";
            case "IOWA" -> "IA";
            case "KANSAS" -> "KS";
            case "KENTUCKY" -> "KY";
            case "LOUISIANA" -> "LA";
            case "MAINE" -> "ME";
            case "MARYLAND" -> "MD";
            case "MASSACHUSETTS" -> "MA";
            case "MICHIGAN" -> "MI";
            case "MINNESOTA" -> "MN";
            case "MISSISSIPPI" -> "MS";
            case "MISSOURI" -> "MO";
            case "MONTANA" -> "MT";
            case "NEBRASKA" -> "NE";
            case "NEVADA" -> "NV";
            case "NEW HAMPSHIRE" -> "NH";
            case "NEW JERSEY" -> "NJ";
            case "NEW MEXICO" -> "NM";
            case "NEW YORK" -> "NY";
            case "NORTH CAROLINA" -> "NC";
            case "NORTH DAKOTA" -> "ND";
            case "OHIO" -> "OH";
            case "OKLAHOMA" -> "OK";
            case "OREGON" -> "OR";
            case "PENNSYLVANIA" -> "PA";
            case "RHODE ISLAND" -> "RI";
            case "SOUTH CAROLINA" -> "SC";
            case "SOUTH DAKOTA" -> "SD";
            case "TENNESSEE" -> "TN";
            case "TEXAS" -> "TX";
            case "UTAH" -> "UT";
            case "VERMONT" -> "VT";
            case "VIRGINIA" -> "VA";
            case "WASHINGTON" -> "WA";
            case "WEST VIRGINIA" -> "WV";
            case "WISCONSIN" -> "WI";
            case "WYOMING" -> "WY";
            case "DISTRICT OF COLUMBIA" -> "DC";
            case "PUERTO RICO" -> "PR";
            case "GUAM" -> "GU";
            case "VIRGIN ISLANDS" -> "VI";
            case "AMERICAN SAMOA" -> "AS";
            case "NORTHERN MARIANA ISLANDS" -> "MP";
            default -> state.length() <= 2 ? state : state.substring(0, 2).toUpperCase();
        };
    }

    /**
     * Map chamber string to Chamber enum.
     */
    private Chamber mapChamber(String chamberStr) {
        if (chamberStr == null) return null;

        String normalized = chamberStr.toUpperCase();
        if (normalized.contains("SENATE")) {
            return Chamber.SENATE;
        } else if (normalized.contains("HOUSE") || normalized.contains("REPRESENTATIVE")) {
            return Chamber.HOUSE;
        }
        return null;
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
     * Get count of synced members.
     */
    public long getMemberCount() {
        return personRepository.count();
    }
}
