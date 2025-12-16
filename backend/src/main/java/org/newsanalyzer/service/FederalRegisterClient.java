package org.newsanalyzer.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.newsanalyzer.config.FederalRegisterConfig;
import org.newsanalyzer.dto.DocumentQueryParams;
import org.newsanalyzer.dto.FederalRegisterAgency;
import org.newsanalyzer.dto.FederalRegisterDocument;
import org.newsanalyzer.dto.FederalRegisterDocumentPage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Client for Federal Register API.
 *
 * Fetches government agency data from the public Federal Register API.
 * Implements retry logic, rate limiting, and error handling.
 *
 * @author James (Dev Agent)
 * @since 2.0.0
 * @see <a href="https://www.federalregister.gov/developers/documentation/api/v1">Federal Register API Documentation</a>
 */
@Service
public class FederalRegisterClient {

    private static final Logger log = LoggerFactory.getLogger(FederalRegisterClient.class);

    private final FederalRegisterConfig config;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    private long lastRequestTime = 0;

    public FederalRegisterClient(FederalRegisterConfig config, ObjectMapper objectMapper) {
        this.config = config;
        this.objectMapper = objectMapper;
        this.restTemplate = new RestTemplate();
    }

    /**
     * Fetch all agencies from the Federal Register API.
     *
     * @return List of all agencies, or empty list if the request fails
     */
    public List<FederalRegisterAgency> fetchAllAgencies() {
        log.info("Fetching all agencies from Federal Register API: {}/agencies", config.getBaseUrl());

        String url = config.getBaseUrl() + "/agencies";

        Optional<String> response = executeWithRetry(url);

        if (response.isPresent()) {
            try {
                List<FederalRegisterAgency> agencies = objectMapper.readValue(
                    response.get(),
                    new TypeReference<List<FederalRegisterAgency>>() {}
                );
                log.info("Successfully fetched {} agencies from Federal Register", agencies.size());
                return agencies;
            } catch (Exception e) {
                log.error("Failed to parse Federal Register API response: {}", e.getMessage());
                return Collections.emptyList();
            }
        }

        return Collections.emptyList();
    }

    /**
     * Fetch a specific agency by slug.
     *
     * @param slug The agency slug (e.g., "agriculture-department")
     * @return Optional containing the agency, or empty if not found
     */
    public Optional<FederalRegisterAgency> fetchAgency(String slug) {
        log.debug("Fetching agency with slug: {}", slug);

        String url = config.getBaseUrl() + "/agencies/" + slug;

        Optional<String> response = executeWithRetry(url);

        if (response.isPresent()) {
            try {
                FederalRegisterAgency agency = objectMapper.readValue(
                    response.get(),
                    FederalRegisterAgency.class
                );
                return Optional.of(agency);
            } catch (Exception e) {
                log.error("Failed to parse agency response for slug {}: {}", slug, e.getMessage());
                return Optional.empty();
            }
        }

        return Optional.empty();
    }

    // =========================================================================
    // Document Fetching Methods
    // =========================================================================

    /**
     * Fetch documents from the Federal Register API with filtering and pagination.
     *
     * @param params Query parameters for filtering documents
     * @return Page of documents, or empty page if request fails
     */
    public FederalRegisterDocumentPage fetchDocuments(DocumentQueryParams params) {
        String url = params.buildUrl(config.getBaseUrl());
        log.info("Fetching documents from Federal Register API: page={}, perPage={}",
                params.getPage(), params.getPerPage());
        log.debug("Request URL: {}", url);

        Optional<String> response = executeWithRetry(url);

        if (response.isPresent()) {
            try {
                FederalRegisterDocumentPage page = objectMapper.readValue(
                    response.get(),
                    FederalRegisterDocumentPage.class
                );
                log.info("Successfully fetched {} documents (page {}/{}, total: {})",
                        page.getResults() != null ? page.getResults().size() : 0,
                        params.getPage(),
                        page.getTotalPages(),
                        page.getCount());
                return page;
            } catch (Exception e) {
                log.error("Failed to parse Federal Register documents response: {}", e.getMessage());
                return new FederalRegisterDocumentPage();
            }
        }

        return new FederalRegisterDocumentPage();
    }

    /**
     * Fetch all documents matching the query, handling pagination automatically.
     *
     * @param params Query parameters for filtering documents
     * @param maxPages Maximum number of pages to fetch (to prevent runaway requests)
     * @return List of all documents matching the query
     */
    public List<FederalRegisterDocument> fetchAllDocuments(DocumentQueryParams params, int maxPages) {
        List<FederalRegisterDocument> allDocuments = new ArrayList<>();
        int currentPage = 1;

        DocumentQueryParams.DocumentQueryParamsBuilder paramsBuilder = DocumentQueryParams.builder()
                .publicationDateGte(params.getPublicationDateGte())
                .publicationDateLte(params.getPublicationDateLte())
                .documentTypes(params.getDocumentTypes())
                .agencyIds(params.getAgencyIds())
                .perPage(params.getPerPage());

        while (currentPage <= maxPages) {
            DocumentQueryParams pageParams = paramsBuilder.page(currentPage).build();
            FederalRegisterDocumentPage page = fetchDocuments(pageParams);

            if (page.isEmpty()) {
                break;
            }

            allDocuments.addAll(page.getResults());
            log.debug("Fetched page {}/{}, documents so far: {}",
                    currentPage, page.getTotalPages(), allDocuments.size());

            if (!page.hasNextPage() || currentPage >= page.getTotalPages()) {
                break;
            }

            currentPage++;
        }

        log.info("Fetched {} total documents across {} pages", allDocuments.size(), currentPage);
        return allDocuments;
    }

    /**
     * Fetch a single document by its document number.
     *
     * @param documentNumber The Federal Register document number (e.g., "2024-12345")
     * @return Optional containing the document, or empty if not found
     */
    public Optional<FederalRegisterDocument> fetchDocument(String documentNumber) {
        log.debug("Fetching document with number: {}", documentNumber);

        String url = config.getBaseUrl() + "/documents/" + documentNumber;

        Optional<String> response = executeWithRetry(url);

        if (response.isPresent()) {
            try {
                FederalRegisterDocument document = objectMapper.readValue(
                    response.get(),
                    FederalRegisterDocument.class
                );
                return Optional.of(document);
            } catch (Exception e) {
                log.error("Failed to parse document response for {}: {}", documentNumber, e.getMessage());
                return Optional.empty();
            }
        }

        return Optional.empty();
    }

    // =========================================================================
    // Utility Methods
    // =========================================================================

    /**
     * Check if the Federal Register API is available.
     *
     * @return true if the API responds successfully, false otherwise
     */
    public boolean isApiAvailable() {
        log.debug("Checking Federal Register API availability");

        String url = config.getBaseUrl() + "/agencies";

        try {
            applyRateLimit();
            restTemplate.headForHeaders(url);
            log.debug("Federal Register API is available");
            return true;
        } catch (RestClientException e) {
            log.warn("Federal Register API is unavailable: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Execute an API request with retry logic and exponential backoff.
     *
     * @param url The URL to request
     * @return Optional containing the response body, or empty if all retries failed
     */
    private Optional<String> executeWithRetry(String url) {
        int attempt = 0;
        long delayMs = 1000; // Initial retry delay

        while (attempt < config.getRetryAttempts()) {
            try {
                applyRateLimit();
                String response = restTemplate.getForObject(url, String.class);

                if (response != null) {
                    return Optional.of(response);
                }
            } catch (RestClientException e) {
                attempt++;
                log.warn("Federal Register API request failed (attempt {}/{}): {}",
                        attempt, config.getRetryAttempts(), e.getMessage());

                if (attempt < config.getRetryAttempts()) {
                    try {
                        Thread.sleep(delayMs);
                        delayMs *= 2; // Exponential backoff
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        log.warn("Retry interrupted");
                        return Optional.empty();
                    }
                }
            }
        }

        log.error("Failed to fetch from Federal Register API after {} attempts", config.getRetryAttempts());
        return Optional.empty();
    }

    /**
     * Apply rate limiting between requests.
     * Enforces minimum delay between requests to be a good API citizen.
     */
    private void applyRateLimit() {
        long currentTime = System.currentTimeMillis();
        long timeSinceLastRequest = currentTime - lastRequestTime;
        long requiredDelay = config.getRateLimitMs();

        if (timeSinceLastRequest < requiredDelay && lastRequestTime > 0) {
            long sleepTime = requiredDelay - timeSinceLastRequest;
            try {
                log.trace("Rate limiting: sleeping for {} ms", sleepTime);
                Thread.sleep(sleepTime);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        lastRequestTime = System.currentTimeMillis();
    }

    /**
     * Get the configured base URL.
     *
     * @return The Federal Register API base URL
     */
    public String getBaseUrl() {
        return config.getBaseUrl();
    }
}
