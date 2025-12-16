package org.newsanalyzer.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.newsanalyzer.dto.LegislatorYamlRecord;
import org.newsanalyzer.model.Person;
import org.newsanalyzer.repository.PersonRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service for enriching Person records with data from the unitedstates/congress-legislators repository.
 *
 * Merges external IDs and social media handles without overwriting primary fields
 * (name, party, state, etc.) which come from Congress.gov.
 *
 * @author James (Dev Agent)
 * @since 2.0.0
 */
@Service
public class LegislatorsEnrichmentService {

    private static final Logger log = LoggerFactory.getLogger(LegislatorsEnrichmentService.class);
    private static final String ENRICHMENT_SOURCE = "LEGISLATORS_REPO";

    private final PersonRepository personRepository;
    private final LegislatorsRepoClient legislatorsRepoClient;
    private final ObjectMapper objectMapper;

    public LegislatorsEnrichmentService(PersonRepository personRepository,
                                        LegislatorsRepoClient legislatorsRepoClient,
                                        ObjectMapper objectMapper) {
        this.personRepository = personRepository;
        this.legislatorsRepoClient = legislatorsRepoClient;
        this.objectMapper = objectMapper;
    }

    /**
     * Enrich all persons with data from the current legislators file.
     *
     * @param commitSha The git commit SHA to record as enrichment version
     * @return EnrichmentResult with statistics
     */
    @Transactional
    public EnrichmentResult enrichCurrentLegislators(String commitSha) {
        log.info("Starting enrichment from current legislators (commit: {})", commitSha);

        List<LegislatorYamlRecord> records = legislatorsRepoClient.fetchCurrentLegislators();
        return processRecords(records, commitSha);
    }

    /**
     * Enrich all persons with data from the historical legislators file.
     *
     * @param commitSha The git commit SHA to record as enrichment version
     * @return EnrichmentResult with statistics
     */
    @Transactional
    public EnrichmentResult enrichHistoricalLegislators(String commitSha) {
        log.info("Starting enrichment from historical legislators (commit: {})", commitSha);

        List<LegislatorYamlRecord> records = legislatorsRepoClient.fetchHistoricalLegislators();
        return processRecords(records, commitSha);
    }

    /**
     * Process a list of legislator records and enrich matching Person entities.
     */
    private EnrichmentResult processRecords(List<LegislatorYamlRecord> records, String commitSha) {
        int matched = 0;
        int notFound = 0;
        int errors = 0;

        for (LegislatorYamlRecord record : records) {
            String bioguideId = record.getBioguideId();
            if (bioguideId == null || bioguideId.isBlank()) {
                log.warn("Skipping record with no bioguide ID");
                errors++;
                continue;
            }

            try {
                Optional<Person> personOpt = personRepository.findByBioguideId(bioguideId);
                if (personOpt.isPresent()) {
                    Person person = personOpt.get();
                    enrichPerson(person, record, commitSha);
                    personRepository.save(person);
                    matched++;
                } else {
                    notFound++;
                    log.debug("No matching Person for bioguideId: {}", bioguideId);
                }
            } catch (Exception e) {
                errors++;
                log.error("Error enriching person {}: {}", bioguideId, e.getMessage());
            }
        }

        EnrichmentResult result = new EnrichmentResult(records.size(), matched, notFound, errors);
        log.info("Enrichment complete: {}", result);
        return result;
    }

    /**
     * Enrich a single Person with data from a legislator record.
     * Does NOT overwrite primary fields (name, party, state, etc.).
     */
    private void enrichPerson(Person person, LegislatorYamlRecord record, String commitSha) {
        // Merge external IDs
        Map<String, Object> newExternalIds = record.buildExternalIdsMap();
        if (!newExternalIds.isEmpty()) {
            JsonNode mergedExternalIds = mergeJsonNodes(person.getExternalIds(), newExternalIds);
            person.setExternalIds(mergedExternalIds);
        }

        // Merge social media
        Map<String, String> newSocialMedia = record.buildSocialMediaMap();
        if (!newSocialMedia.isEmpty()) {
            JsonNode mergedSocialMedia = mergeJsonNodes(person.getSocialMedia(), newSocialMedia);
            person.setSocialMedia(mergedSocialMedia);
        }

        // Set enrichment tracking
        person.setEnrichmentSource(ENRICHMENT_SOURCE);
        person.setEnrichmentVersion(commitSha);
    }

    /**
     * Merge new values into existing JSON, preserving existing values.
     * New values only added if key doesn't exist or existing value is null.
     */
    private JsonNode mergeJsonNodes(JsonNode existing, Map<String, ?> newValues) {
        ObjectNode result;

        if (existing == null || existing.isNull() || existing.isEmpty()) {
            result = objectMapper.createObjectNode();
        } else {
            result = existing.deepCopy().isObject()
                    ? (ObjectNode) existing.deepCopy()
                    : objectMapper.createObjectNode();
        }

        for (Map.Entry<String, ?> entry : newValues.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            // Only add if key doesn't exist or is null
            if (!result.has(key) || result.get(key).isNull()) {
                if (value instanceof List) {
                    result.set(key, objectMapper.valueToTree(value));
                } else if (value instanceof Integer) {
                    result.put(key, (Integer) value);
                } else if (value instanceof String) {
                    result.put(key, (String) value);
                } else if (value != null) {
                    result.set(key, objectMapper.valueToTree(value));
                }
            }
        }

        return result;
    }

    /**
     * Run a full sync: fetch commit SHA, check if changed, then enrich.
     *
     * @param lastKnownCommitSha The last known commit SHA (null to force sync)
     * @return SyncResult with details
     */
    @Transactional
    public SyncResult runFullSync(String lastKnownCommitSha) {
        log.info("Starting full sync (last known commit: {})", lastKnownCommitSha);

        // Get latest commit SHA
        Optional<String> latestCommitOpt = legislatorsRepoClient.fetchLatestCommitSha();
        if (latestCommitOpt.isEmpty()) {
            log.error("Failed to fetch latest commit SHA");
            return new SyncResult(false, null, null, "Failed to fetch commit SHA");
        }

        String latestCommit = latestCommitOpt.get();

        // Check if commit changed
        if (latestCommit.equals(lastKnownCommitSha)) {
            log.info("No changes detected (commit {} unchanged)", latestCommit);
            return new SyncResult(true, latestCommit, null, "No changes - commit unchanged");
        }

        // Run enrichment
        EnrichmentResult enrichmentResult = enrichCurrentLegislators(latestCommit);

        return new SyncResult(true, latestCommit, enrichmentResult, "Sync completed successfully");
    }

    /**
     * Result of an enrichment operation.
     */
    public record EnrichmentResult(int total, int matched, int notFound, int errors) {
        @Override
        public String toString() {
            return String.format("total=%d, matched=%d, notFound=%d, errors=%d",
                    total, matched, notFound, errors);
        }
    }

    /**
     * Result of a sync operation.
     */
    public record SyncResult(boolean success, String commitSha, EnrichmentResult enrichment, String message) {
    }
}
