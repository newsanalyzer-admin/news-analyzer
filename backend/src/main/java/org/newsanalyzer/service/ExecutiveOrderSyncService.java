package org.newsanalyzer.service;

import org.newsanalyzer.dto.DocumentQueryParams;
import org.newsanalyzer.dto.FederalRegisterDocument;
import org.newsanalyzer.model.*;
import org.newsanalyzer.repository.ExecutiveOrderRepository;
import org.newsanalyzer.repository.IndividualRepository;
import org.newsanalyzer.repository.PresidencyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Service for synchronizing Executive Order data from the Federal Register API.
 *
 * Fetches Executive Orders for each presidency and stores metadata in the database.
 * Links EOs to their respective Presidency entities.
 *
 * Part of ARCH-1.6: Updated to use Individual instead of Person.
 *
 * @author James (Dev Agent)
 * @since 2.0.0
 */
@Service
@Transactional
public class ExecutiveOrderSyncService {

    private static final Logger log = LoggerFactory.getLogger(ExecutiveOrderSyncService.class);
    private static final String DOCUMENT_TYPE_PRESDOCU = "PRESDOCU";
    private static final String PRESIDENTIAL_DOC_TYPE_EO = "executive_order";
    private static final int MAX_PAGES_PER_PRESIDENT = 50;

    private final FederalRegisterClient federalRegisterClient;
    private final ExecutiveOrderRepository executiveOrderRepository;
    private final PresidencyRepository presidencyRepository;
    private final IndividualRepository individualRepository;

    // Map of president names to Federal Register API identifiers
    // Federal Register uses lowercase hyphenated names
    private static final Map<String, String> PRESIDENT_NAME_MAPPING = new LinkedHashMap<>();

    static {
        // Modern presidents (have EOs in Federal Register database)
        // Federal Register has EOs starting from FDR (1933)
        PRESIDENT_NAME_MAPPING.put("Franklin D. Roosevelt", "franklin-d-roosevelt");
        PRESIDENT_NAME_MAPPING.put("Harry S. Truman", "harry-s-truman");
        PRESIDENT_NAME_MAPPING.put("Dwight D. Eisenhower", "dwight-d-eisenhower");
        PRESIDENT_NAME_MAPPING.put("John F. Kennedy", "john-f-kennedy");
        PRESIDENT_NAME_MAPPING.put("Lyndon B. Johnson", "lyndon-b-johnson");
        PRESIDENT_NAME_MAPPING.put("Richard Nixon", "richard-nixon");
        PRESIDENT_NAME_MAPPING.put("Gerald R. Ford", "gerald-r-ford");
        PRESIDENT_NAME_MAPPING.put("Jimmy Carter", "jimmy-carter");
        PRESIDENT_NAME_MAPPING.put("Ronald Reagan", "ronald-reagan");
        PRESIDENT_NAME_MAPPING.put("George H.W. Bush", "george-hw-bush");
        PRESIDENT_NAME_MAPPING.put("William J. Clinton", "william-j-clinton");
        PRESIDENT_NAME_MAPPING.put("George W. Bush", "george-w-bush");
        PRESIDENT_NAME_MAPPING.put("Barack Obama", "barack-obama");
        PRESIDENT_NAME_MAPPING.put("Donald J. Trump", "donald-trump");
        PRESIDENT_NAME_MAPPING.put("Joseph R. Biden Jr.", "joe-biden");
    }

    public ExecutiveOrderSyncService(FederalRegisterClient federalRegisterClient,
                                     ExecutiveOrderRepository executiveOrderRepository,
                                     PresidencyRepository presidencyRepository,
                                     IndividualRepository individualRepository) {
        this.federalRegisterClient = federalRegisterClient;
        this.executiveOrderRepository = executiveOrderRepository;
        this.presidencyRepository = presidencyRepository;
        this.individualRepository = individualRepository;
    }

    /**
     * Sync result statistics.
     */
    public static class SyncResult {
        private int executiveOrdersAdded;
        private int executiveOrdersUpdated;
        private int executiveOrdersSkipped;
        private int presidenciesProcessed;
        private int errors;
        private List<String> errorMessages = new ArrayList<>();

        public int getExecutiveOrdersAdded() { return executiveOrdersAdded; }
        public int getExecutiveOrdersUpdated() { return executiveOrdersUpdated; }
        public int getExecutiveOrdersSkipped() { return executiveOrdersSkipped; }
        public int getPresidenciesProcessed() { return presidenciesProcessed; }
        public int getErrors() { return errors; }
        public List<String> getErrorMessages() { return errorMessages; }

        public int getTotalExecutiveOrders() {
            return executiveOrdersAdded + executiveOrdersUpdated;
        }

        @Override
        public String toString() {
            return String.format("SyncResult{eos=%d (added=%d, updated=%d, skipped=%d), " +
                            "presidencies=%d, errors=%d}",
                    getTotalExecutiveOrders(), executiveOrdersAdded, executiveOrdersUpdated,
                    executiveOrdersSkipped, presidenciesProcessed, errors);
        }
    }

    /**
     * Sync Executive Orders for all presidencies from Federal Register API.
     *
     * @return SyncResult with statistics
     */
    public SyncResult syncAllExecutiveOrders() {
        log.info("Starting Executive Order sync from Federal Register API");
        SyncResult result = new SyncResult();

        // Check if API is available
        if (!federalRegisterClient.isApiAvailable()) {
            log.error("Federal Register API is not available");
            result.errors++;
            result.errorMessages.add("Federal Register API is not available");
            return result;
        }

        // Get all presidencies
        List<Presidency> presidencies = presidencyRepository.findAllByOrderByNumberAsc();
        log.info("Found {} presidencies to process", presidencies.size());

        for (Presidency presidency : presidencies) {
            try {
                syncExecutiveOrdersForPresidency(presidency, result);
                result.presidenciesProcessed++;
            } catch (Exception e) {
                log.error("Error syncing EOs for presidency #{}: {}",
                        presidency.getNumber(), e.getMessage());
                result.errors++;
                result.errorMessages.add("Presidency #" + presidency.getNumber() + ": " + e.getMessage());
            }
        }

        log.info("Executive Order sync completed: {}", result);
        return result;
    }

    /**
     * Sync Executive Orders for a specific presidency.
     *
     * @param presidencyNumber The presidency number (1-47)
     * @return SyncResult with statistics
     */
    public SyncResult syncExecutiveOrdersForPresidency(int presidencyNumber) {
        log.info("Syncing Executive Orders for presidency #{}", presidencyNumber);
        SyncResult result = new SyncResult();

        Optional<Presidency> presidencyOpt = presidencyRepository.findByNumber(presidencyNumber);
        if (presidencyOpt.isEmpty()) {
            log.error("Presidency #{} not found", presidencyNumber);
            result.errors++;
            result.errorMessages.add("Presidency #" + presidencyNumber + " not found");
            return result;
        }

        try {
            syncExecutiveOrdersForPresidency(presidencyOpt.get(), result);
            result.presidenciesProcessed++;
        } catch (Exception e) {
            log.error("Error syncing EOs for presidency #{}: {}", presidencyNumber, e.getMessage());
            result.errors++;
            result.errorMessages.add(e.getMessage());
        }

        log.info("Executive Order sync for presidency #{} completed: {}", presidencyNumber, result);
        return result;
    }

    /**
     * Sync Executive Orders for a presidency.
     */
    private void syncExecutiveOrdersForPresidency(Presidency presidency, SyncResult result) {
        // Get president's name from linked Individual
        Optional<Individual> individualOpt = individualRepository.findById(presidency.getIndividualId());
        if (individualOpt.isEmpty()) {
            log.warn("Individual not found for presidency #{}, skipping", presidency.getNumber());
            return;
        }

        Individual president = individualOpt.get();
        String presidentFullName = president.getFirstName() + " " + president.getLastName();

        // Get Federal Register identifier for this president
        String federalRegisterName = getFederalRegisterName(presidentFullName, president);
        if (federalRegisterName == null) {
            log.debug("No Federal Register mapping for {}, skipping (pre-1933 president)",
                    presidentFullName);
            return;
        }

        log.info("Fetching EOs for {} (presidency #{}, FR name: {})",
                presidentFullName, presidency.getNumber(), federalRegisterName);

        // Build query for Executive Orders
        DocumentQueryParams params = DocumentQueryParams.builder()
                .documentTypes(List.of(DOCUMENT_TYPE_PRESDOCU))
                .presidentialDocumentType(PRESIDENTIAL_DOC_TYPE_EO)
                .president(federalRegisterName)
                .perPage(100)
                .build();

        // Fetch all EOs for this president
        List<FederalRegisterDocument> documents = federalRegisterClient.fetchAllDocuments(
                params, MAX_PAGES_PER_PRESIDENT);

        log.info("Fetched {} EO documents for {}", documents.size(), presidentFullName);

        // Process each EO
        for (FederalRegisterDocument doc : documents) {
            try {
                processExecutiveOrder(doc, presidency, result);
            } catch (Exception e) {
                log.error("Error processing EO #{}: {}", doc.getExecutiveOrderNumber(), e.getMessage());
                result.errors++;
                result.errorMessages.add("EO #" + doc.getExecutiveOrderNumber() + ": " + e.getMessage());
            }
        }
    }

    /**
     * Process a single Executive Order document.
     */
    private void processExecutiveOrder(FederalRegisterDocument doc, Presidency presidency, SyncResult result) {
        if (doc.getExecutiveOrderNumber() == null) {
            log.debug("Document {} has no EO number, skipping", doc.getDocumentNumber());
            result.executiveOrdersSkipped++;
            return;
        }

        // Check if EO already exists
        Optional<ExecutiveOrder> existingOpt = executiveOrderRepository.findByEoNumber(doc.getExecutiveOrderNumber());

        if (existingOpt.isPresent()) {
            // Update existing EO
            ExecutiveOrder existing = existingOpt.get();
            updateExecutiveOrder(existing, doc, presidency);
            executiveOrderRepository.save(existing);
            result.executiveOrdersUpdated++;
            log.debug("Updated EO #{}", doc.getExecutiveOrderNumber());
        } else {
            // Create new EO
            ExecutiveOrder newEo = createExecutiveOrder(doc, presidency);
            executiveOrderRepository.save(newEo);
            result.executiveOrdersAdded++;
            log.debug("Added EO #{}", doc.getExecutiveOrderNumber());
        }
    }

    /**
     * Create a new ExecutiveOrder from a Federal Register document.
     */
    private ExecutiveOrder createExecutiveOrder(FederalRegisterDocument doc, Presidency presidency) {
        return ExecutiveOrder.builder()
                .presidencyId(presidency.getId())
                .eoNumber(doc.getExecutiveOrderNumber())
                .title(truncateString(doc.getTitle(), 500))
                .signingDate(doc.getSigningDate() != null ? doc.getSigningDate() : doc.getPublicationDate())
                .summary(doc.getDocumentAbstract())
                .federalRegisterCitation(truncateString(doc.getCitation(), 100))
                .federalRegisterUrl(truncateString(doc.getHtmlUrl(), 500))
                .status(ExecutiveOrderStatus.ACTIVE)
                .dataSource(DataSource.FEDERAL_REGISTER)
                .sourceReference(doc.getDocumentNumber())
                .build();
    }

    /**
     * Update an existing ExecutiveOrder from a Federal Register document.
     */
    private void updateExecutiveOrder(ExecutiveOrder existing, FederalRegisterDocument doc, Presidency presidency) {
        // Update fields if they have new values
        if (doc.getTitle() != null) {
            existing.setTitle(truncateString(doc.getTitle(), 500));
        }
        if (doc.getSigningDate() != null) {
            existing.setSigningDate(doc.getSigningDate());
        } else if (doc.getPublicationDate() != null && existing.getSigningDate() == null) {
            existing.setSigningDate(doc.getPublicationDate());
        }
        if (doc.getDocumentAbstract() != null) {
            existing.setSummary(doc.getDocumentAbstract());
        }
        if (doc.getCitation() != null) {
            existing.setFederalRegisterCitation(truncateString(doc.getCitation(), 100));
        }
        if (doc.getHtmlUrl() != null) {
            existing.setFederalRegisterUrl(truncateString(doc.getHtmlUrl(), 500));
        }
        existing.setSourceReference(doc.getDocumentNumber());

        // Ensure correct presidency link
        if (!presidency.getId().equals(existing.getPresidencyId())) {
            log.warn("EO #{} presidency mismatch: was {}, updating to {}",
                    doc.getExecutiveOrderNumber(), existing.getPresidencyId(), presidency.getId());
            existing.setPresidencyId(presidency.getId());
        }
    }

    /**
     * Get Federal Register API identifier for a president.
     * Returns null for pre-FDR presidents (no EOs in database).
     */
    private String getFederalRegisterName(String fullName, Individual president) {
        // Try direct mapping
        for (Map.Entry<String, String> entry : PRESIDENT_NAME_MAPPING.entrySet()) {
            if (fullName.contains(entry.getKey()) ||
                    entry.getKey().contains(president.getLastName())) {
                return entry.getValue();
            }
        }

        // Try to construct from name parts
        String lastName = president.getLastName().toLowerCase();

        // Check if this is a known modern president by last name
        for (Map.Entry<String, String> entry : PRESIDENT_NAME_MAPPING.entrySet()) {
            if (entry.getKey().toLowerCase().contains(lastName)) {
                return entry.getValue();
            }
        }

        return null;
    }

    /**
     * Get count of Executive Orders per presidency.
     *
     * @return Map of presidency ID to EO count
     */
    public Map<UUID, Long> getExecutiveOrderCounts() {
        Map<UUID, Long> counts = new HashMap<>();
        List<Object[]> results = executiveOrderRepository.getCountPerPresidency();
        for (Object[] row : results) {
            UUID presidencyId = (UUID) row[0];
            Long count = (Long) row[1];
            counts.put(presidencyId, count);
        }
        return counts;
    }

    /**
     * Truncate a string to a maximum length.
     */
    private String truncateString(String value, int maxLength) {
        if (value == null) {
            return null;
        }
        if (value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength - 3) + "...";
    }
}
