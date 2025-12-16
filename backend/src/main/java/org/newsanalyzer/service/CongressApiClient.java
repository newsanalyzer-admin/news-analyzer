package org.newsanalyzer.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.newsanalyzer.config.CongressApiConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Client for Congress.gov API.
 *
 * Handles authentication, rate limiting, and retry logic for API calls.
 *
 * @author James (Dev Agent)
 * @since 2.0.0
 * @see <a href="https://api.congress.gov">Congress.gov API Documentation</a>
 */
@Service
public class CongressApiClient {

    private static final Logger log = LoggerFactory.getLogger(CongressApiClient.class);

    private static final int MAX_LIMIT = 250;
    private static final int MAX_RETRIES = 3;
    private static final long INITIAL_RETRY_DELAY_MS = 1000;

    private final CongressApiConfig config;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    // Rate limiting state
    private final AtomicInteger requestCount = new AtomicInteger(0);
    private final AtomicLong windowStart = new AtomicLong(System.currentTimeMillis());

    public CongressApiClient(CongressApiConfig config, ObjectMapper objectMapper) {
        this.config = config;
        this.objectMapper = objectMapper;
        this.restTemplate = new RestTemplate();
    }

    /**
     * Fetch all current members of Congress.
     *
     * @return List of member data as JsonNode
     */
    public List<JsonNode> fetchAllCurrentMembers() {
        List<JsonNode> allMembers = new ArrayList<>();
        int offset = 0;
        boolean hasMore = true;

        log.info("Starting fetch of all current members from Congress.gov API");

        while (hasMore) {
            Optional<JsonNode> response = fetchMembers(MAX_LIMIT, offset, true);

            if (response.isPresent()) {
                JsonNode data = response.get();
                JsonNode members = data.path("members");

                if (members.isArray() && members.size() > 0) {
                    for (JsonNode member : members) {
                        allMembers.add(member);
                    }
                    offset += members.size();
                    log.debug("Fetched {} members, total so far: {}", members.size(), allMembers.size());

                    // Check if there are more pages
                    JsonNode pagination = data.path("pagination");
                    int total = pagination.path("count").asInt(0);
                    hasMore = offset < total;
                } else {
                    hasMore = false;
                }
            } else {
                log.error("Failed to fetch members at offset {}", offset);
                hasMore = false;
            }
        }

        log.info("Completed fetch of {} current members", allMembers.size());
        return allMembers;
    }

    /**
     * Fetch members with pagination.
     *
     * @param limit Maximum number of results per page (max 250)
     * @param offset Starting offset for pagination
     * @param currentOnly Whether to fetch only current members
     * @return Optional containing the API response
     */
    public Optional<JsonNode> fetchMembers(int limit, int offset, boolean currentOnly) {
        checkRateLimit();

        String url = UriComponentsBuilder.fromHttpUrl(config.getBaseUrl() + "/member")
                .queryParam("limit", Math.min(limit, MAX_LIMIT))
                .queryParam("offset", offset)
                .queryParam("currentMember", currentOnly)
                .queryParam("api_key", config.getKey())
                .build()
                .toUriString();

        return executeWithRetry(url);
    }

    /**
     * Fetch a specific member by BioGuide ID.
     *
     * @param bioguideId The BioGuide ID of the member
     * @return Optional containing the member data
     */
    public Optional<JsonNode> fetchMemberByBioguideId(String bioguideId) {
        checkRateLimit();

        String url = UriComponentsBuilder.fromHttpUrl(config.getBaseUrl() + "/member/" + bioguideId)
                .queryParam("api_key", config.getKey())
                .build()
                .toUriString();

        return executeWithRetry(url);
    }

    /**
     * Execute API call with retry logic and exponential backoff.
     */
    private Optional<JsonNode> executeWithRetry(String url) {
        int attempt = 0;
        long delayMs = INITIAL_RETRY_DELAY_MS;

        while (attempt < MAX_RETRIES) {
            try {
                requestCount.incrementAndGet();
                String response = restTemplate.getForObject(url, String.class);

                if (response != null) {
                    return Optional.of(objectMapper.readTree(response));
                }
            } catch (RestClientException e) {
                attempt++;
                log.warn("API request failed (attempt {}/{}): {}", attempt, MAX_RETRIES, e.getMessage());

                if (attempt < MAX_RETRIES) {
                    try {
                        Thread.sleep(delayMs);
                        delayMs *= 2; // Exponential backoff
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        return Optional.empty();
                    }
                }
            } catch (Exception e) {
                log.error("Failed to parse API response: {}", e.getMessage());
                return Optional.empty();
            }
        }

        log.error("Failed to fetch from Congress.gov API after {} attempts", MAX_RETRIES);
        return Optional.empty();
    }

    /**
     * Check and enforce rate limiting.
     *
     * Ensures we don't exceed 5,000 requests per hour.
     */
    private void checkRateLimit() {
        long currentTime = System.currentTimeMillis();
        long windowStartTime = windowStart.get();
        long elapsedMs = currentTime - windowStartTime;

        // Reset window if an hour has passed
        if (elapsedMs >= Duration.ofHours(1).toMillis()) {
            windowStart.set(currentTime);
            requestCount.set(0);
            return;
        }

        // Check if we've exceeded the rate limit
        if (requestCount.get() >= config.getRateLimit()) {
            long sleepMs = Duration.ofHours(1).toMillis() - elapsedMs + 1000;
            log.warn("Rate limit reached, sleeping for {} ms", sleepMs);
            try {
                Thread.sleep(sleepMs);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            windowStart.set(System.currentTimeMillis());
            requestCount.set(0);
        }

        // Add small delay to stay within ~1.4 requests/second average
        try {
            Thread.sleep(750);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Get current request count for monitoring.
     */
    public int getRequestCount() {
        return requestCount.get();
    }

    /**
     * Check if API is configured with a valid key.
     */
    public boolean isConfigured() {
        return config.isConfigured();
    }

    // =====================================================================
    // Committee API Methods
    // =====================================================================

    /**
     * Fetch all committees for a specific chamber.
     *
     * @param chamber The chamber: "house", "senate", or "joint"
     * @return List of committee data as JsonNode
     */
    public List<JsonNode> fetchAllCommitteesByChamber(String chamber) {
        List<JsonNode> allCommittees = new ArrayList<>();
        int offset = 0;
        boolean hasMore = true;

        log.info("Starting fetch of all {} committees from Congress.gov API", chamber);

        while (hasMore) {
            Optional<JsonNode> response = fetchCommittees(chamber, MAX_LIMIT, offset);

            if (response.isPresent()) {
                JsonNode data = response.get();
                JsonNode committees = data.path("committees");

                if (committees.isArray() && committees.size() > 0) {
                    for (JsonNode committee : committees) {
                        allCommittees.add(committee);
                    }
                    offset += committees.size();
                    log.debug("Fetched {} {} committees, total so far: {}",
                            committees.size(), chamber, allCommittees.size());

                    // Check if there are more pages
                    JsonNode pagination = data.path("pagination");
                    int total = pagination.path("count").asInt(0);
                    hasMore = offset < total;
                } else {
                    hasMore = false;
                }
            } else {
                log.error("Failed to fetch {} committees at offset {}", chamber, offset);
                hasMore = false;
            }
        }

        log.info("Completed fetch of {} {} committees", allCommittees.size(), chamber);
        return allCommittees;
    }

    /**
     * Fetch committees with pagination for a specific chamber.
     *
     * @param chamber The chamber: "house", "senate", or "joint"
     * @param limit Maximum number of results per page (max 250)
     * @param offset Starting offset for pagination
     * @return Optional containing the API response
     */
    public Optional<JsonNode> fetchCommittees(String chamber, int limit, int offset) {
        checkRateLimit();

        String url = UriComponentsBuilder.fromHttpUrl(config.getBaseUrl() + "/committee")
                .queryParam("chamber", chamber)
                .queryParam("limit", Math.min(limit, MAX_LIMIT))
                .queryParam("offset", offset)
                .queryParam("api_key", config.getKey())
                .build()
                .toUriString();

        return executeWithRetry(url);
    }

    /**
     * Fetch a specific committee by chamber and code.
     *
     * @param chamber The chamber: "house", "senate", or "joint"
     * @param committeeCode The committee system code (e.g., "hsju00")
     * @return Optional containing the committee data
     */
    public Optional<JsonNode> fetchCommitteeByCode(String chamber, String committeeCode) {
        checkRateLimit();

        String url = UriComponentsBuilder.fromHttpUrl(config.getBaseUrl() + "/committee/" + chamber + "/" + committeeCode)
                .queryParam("api_key", config.getKey())
                .build()
                .toUriString();

        return executeWithRetry(url);
    }

    /**
     * Fetch committee details including subcommittees.
     *
     * @param chamber The chamber: "house", "senate", or "joint"
     * @param committeeCode The committee system code
     * @return Optional containing detailed committee data with subcommittees
     */
    public Optional<JsonNode> fetchCommitteeDetails(String chamber, String committeeCode) {
        return fetchCommitteeByCode(chamber, committeeCode);
    }

    /**
     * Fetch all committees for all chambers (House, Senate, Joint).
     *
     * @return List of all committee data as JsonNode
     */
    public List<JsonNode> fetchAllCommittees() {
        List<JsonNode> allCommittees = new ArrayList<>();

        // Fetch from all three chambers
        allCommittees.addAll(fetchAllCommitteesByChamber("house"));
        allCommittees.addAll(fetchAllCommitteesByChamber("senate"));
        allCommittees.addAll(fetchAllCommitteesByChamber("joint"));

        log.info("Completed fetch of {} total committees across all chambers", allCommittees.size());
        return allCommittees;
    }
}
