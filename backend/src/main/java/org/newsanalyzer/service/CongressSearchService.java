package org.newsanalyzer.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.newsanalyzer.dto.CongressMemberSearchDTO;
import org.newsanalyzer.dto.CongressSearchResponse;
import org.newsanalyzer.dto.CongressSearchResult;
import org.newsanalyzer.model.Person;
import org.newsanalyzer.repository.PersonRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Service for searching Congress.gov members with duplicate detection.
 *
 * Wraps CongressApiClient and adds:
 * - Response transformation to DTOs
 * - Duplicate detection against local Person table
 * - Rate limit tracking
 *
 * @author James (Dev Agent)
 * @since 2.0.0
 */
@Service
public class CongressSearchService {

    private static final Logger log = LoggerFactory.getLogger(CongressSearchService.class);
    private static final String SOURCE_NAME = "Congress.gov";
    private static final String CONGRESS_GOV_BASE_URL = "https://www.congress.gov/member/";

    private final CongressApiClient congressApiClient;
    private final PersonRepository personRepository;

    public CongressSearchService(CongressApiClient congressApiClient, PersonRepository personRepository) {
        this.congressApiClient = congressApiClient;
        this.personRepository = personRepository;
    }

    /**
     * Search members from Congress.gov API with filtering and pagination.
     *
     * @param name       Name filter (partial match)
     * @param state      State filter (2-letter code)
     * @param party      Party filter (D, R, I, etc.)
     * @param chamber    Chamber filter (house, senate)
     * @param congress   Congress number (e.g., 118)
     * @param page       Page number (1-indexed)
     * @param pageSize   Results per page
     * @return Search response with results and pagination info
     */
    public CongressSearchResponse<CongressMemberSearchDTO> searchMembers(
            String name,
            String state,
            String party,
            String chamber,
            Integer congress,
            int page,
            int pageSize) {

        log.info("Searching Congress.gov members: name={}, state={}, party={}, chamber={}, congress={}, page={}, pageSize={}",
                name, state, party, chamber, congress, page, pageSize);

        // Calculate offset for pagination
        int offset = (page - 1) * pageSize;

        // Fetch from Congress.gov API
        // Note: Congress.gov API filtering is limited, we fetch and filter locally
        Optional<JsonNode> response = congressApiClient.fetchMembers(pageSize, offset, true);

        if (response.isEmpty()) {
            log.warn("No response from Congress.gov API");
            return CongressSearchResponse.<CongressMemberSearchDTO>builder()
                    .results(new ArrayList<>())
                    .total(0)
                    .page(page)
                    .pageSize(pageSize)
                    .build();
        }

        JsonNode data = response.get();
        JsonNode membersNode = data.path("members");
        JsonNode pagination = data.path("pagination");

        List<CongressSearchResult<CongressMemberSearchDTO>> results = new ArrayList<>();

        if (membersNode.isArray()) {
            for (JsonNode memberNode : membersNode) {
                CongressMemberSearchDTO dto = mapToSearchDTO(memberNode);

                // Apply local filters
                if (!matchesFilters(dto, name, state, party, chamber)) {
                    continue;
                }

                // Check for duplicate in local database
                String duplicateId = null;
                if (dto.getBioguideId() != null) {
                    Optional<Person> existingPerson = personRepository.findByBioguideId(dto.getBioguideId());
                    if (existingPerson.isPresent()) {
                        duplicateId = existingPerson.get().getId().toString();
                    }
                }

                results.add(CongressSearchResult.<CongressMemberSearchDTO>builder()
                        .data(dto)
                        .source(SOURCE_NAME)
                        .sourceUrl(buildSourceUrl(dto.getBioguideId(), dto.getName()))
                        .duplicateId(duplicateId)
                        .build());
            }
        }

        int total = pagination.path("count").asInt(results.size());

        return CongressSearchResponse.<CongressMemberSearchDTO>builder()
                .results(results)
                .total(total)
                .page(page)
                .pageSize(pageSize)
                .rateLimitRemaining(getRateLimitRemaining())
                .build();
    }

    /**
     * Get member details by bioguide ID.
     *
     * @param bioguideId The BioGuide ID
     * @return Member details or empty if not found
     */
    public Optional<CongressMemberSearchDTO> getMemberByBioguideId(String bioguideId) {
        Optional<JsonNode> response = congressApiClient.fetchMemberByBioguideId(bioguideId);

        if (response.isEmpty()) {
            return Optional.empty();
        }

        JsonNode data = response.get();
        JsonNode memberNode = data.path("member");

        if (memberNode.isMissingNode()) {
            return Optional.empty();
        }

        return Optional.of(mapToSearchDTO(memberNode));
    }

    /**
     * Map Congress.gov API response to DTO.
     */
    private CongressMemberSearchDTO mapToSearchDTO(JsonNode node) {
        // Handle both list format and detail format
        String bioguideId = getTextValue(node, "bioguideId");
        String name = getTextValue(node, "name");
        String state = getTextValue(node, "state");
        String party = getTextValue(node, "partyName");
        String district = getTextValue(node, "district");

        // Parse name into first/last if full name provided
        String firstName = "";
        String lastName = "";
        if (name != null && name.contains(",")) {
            String[] parts = name.split(",", 2);
            lastName = parts[0].trim();
            firstName = parts.length > 1 ? parts[1].trim() : "";
        } else if (name != null && name.contains(" ")) {
            String[] parts = name.split(" ", 2);
            firstName = parts[0].trim();
            lastName = parts.length > 1 ? parts[1].trim() : "";
        }

        // Determine chamber from terms or direct field
        String chamber = determineChamber(node);

        // Current member status
        boolean currentMember = true; // Default true since we fetch current members
        JsonNode currentMemberNode = node.path("currentMember");
        if (!currentMemberNode.isMissingNode()) {
            currentMember = currentMemberNode.asBoolean(true);
        }

        // Image URL - check depiction object
        String imageUrl = null;
        JsonNode depiction = node.path("depiction");
        if (!depiction.isMissingNode()) {
            imageUrl = getTextValue(depiction, "imageUrl");
        }

        // Congress.gov URL
        String url = getTextValue(node, "url");

        return CongressMemberSearchDTO.builder()
                .bioguideId(bioguideId)
                .name(name)
                .firstName(firstName)
                .lastName(lastName)
                .state(state)
                .party(mapPartyName(party))
                .chamber(chamber)
                .district(district)
                .currentMember(currentMember)
                .imageUrl(imageUrl)
                .url(url)
                .build();
    }

    /**
     * Determine chamber from member data.
     */
    private String determineChamber(JsonNode node) {
        // Check terms array for most recent chamber
        JsonNode terms = node.path("terms");
        if (terms.isArray() && terms.size() > 0) {
            // Get most recent term (last in array)
            JsonNode lastTerm = terms.get(terms.size() - 1);
            JsonNode chamber = lastTerm.path("chamber");
            if (!chamber.isMissingNode()) {
                String chamberValue = chamber.asText().toLowerCase();
                if (chamberValue.contains("senate")) {
                    return "senate";
                } else if (chamberValue.contains("house")) {
                    return "house";
                }
            }
        }

        // Check direct chamber field
        JsonNode chamberNode = node.path("chamber");
        if (!chamberNode.isMissingNode()) {
            return chamberNode.asText().toLowerCase();
        }

        return null;
    }

    /**
     * Map full party name to abbreviation.
     */
    private String mapPartyName(String partyName) {
        if (partyName == null) {
            return null;
        }
        switch (partyName.toLowerCase()) {
            case "democratic":
            case "democrat":
                return "D";
            case "republican":
                return "R";
            case "independent":
                return "I";
            case "libertarian":
                return "L";
            default:
                return partyName;
        }
    }

    /**
     * Check if member matches all provided filters.
     */
    private boolean matchesFilters(CongressMemberSearchDTO dto, String name, String state, String party, String chamber) {
        // Name filter (partial match, case-insensitive)
        if (name != null && !name.isEmpty()) {
            String lowerName = name.toLowerCase();
            String fullName = (dto.getName() != null ? dto.getName() : "").toLowerCase();
            if (!fullName.contains(lowerName)) {
                return false;
            }
        }

        // State filter
        if (state != null && !state.isEmpty()) {
            if (dto.getState() == null || !dto.getState().equalsIgnoreCase(state)) {
                return false;
            }
        }

        // Party filter
        if (party != null && !party.isEmpty()) {
            if (dto.getParty() == null || !dto.getParty().equalsIgnoreCase(party)) {
                return false;
            }
        }

        // Chamber filter
        if (chamber != null && !chamber.isEmpty()) {
            if (dto.getChamber() == null || !dto.getChamber().equalsIgnoreCase(chamber)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Build Congress.gov URL for a member.
     */
    private String buildSourceUrl(String bioguideId, String name) {
        if (bioguideId == null) {
            return null;
        }
        // Congress.gov format: /member/firstname-lastname/bioguideId
        String slug = name != null ? name.toLowerCase().replaceAll("[^a-z0-9]+", "-") : "";
        return CONGRESS_GOV_BASE_URL + slug + "/" + bioguideId;
    }

    /**
     * Get rate limit remaining (estimated based on request count).
     */
    private Integer getRateLimitRemaining() {
        // CongressApiClient tracks request count internally
        int requestCount = congressApiClient.getRequestCount();
        int rateLimit = 5000; // Congress.gov hourly limit
        return Math.max(0, rateLimit - requestCount);
    }

    /**
     * Helper to safely get text value from JsonNode.
     */
    private String getTextValue(JsonNode node, String field) {
        JsonNode value = node.path(field);
        return value.isMissingNode() || value.isNull() ? null : value.asText();
    }
}
