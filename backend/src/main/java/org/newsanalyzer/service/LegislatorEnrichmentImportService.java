package org.newsanalyzer.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.newsanalyzer.dto.*;
import org.newsanalyzer.model.Person;
import org.newsanalyzer.repository.PersonRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Service for enriching Person records from the Legislators Repo.
 *
 * Provides preview and import functionality with detailed change tracking.
 *
 * @author James (Dev Agent)
 * @since 2.0.0
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class LegislatorEnrichmentImportService {

    private static final String ENRICHMENT_SOURCE = "LEGISLATORS_REPO";

    private final LegislatorsSearchService legislatorsSearchService;
    private final LegislatorsRepoClient legislatorsRepoClient;
    private final PersonRepository personRepository;
    private final ObjectMapper objectMapper;

    /**
     * Preview what changes would be made by enriching a Person.
     *
     * @param bioguideId The BioGuide ID to look up
     * @return Preview of changes
     */
    public EnrichmentPreview previewEnrichment(String bioguideId) {
        log.info("Previewing enrichment for bioguideId={}", bioguideId);

        // Find the legislator in the repo
        Optional<LegislatorDetailDTO> legislatorOpt = legislatorsSearchService.getLegislatorByBioguideId(bioguideId);

        if (legislatorOpt.isEmpty()) {
            return EnrichmentPreview.builder()
                    .bioguideId(bioguideId)
                    .localMatch(false)
                    .fieldsToAdd(List.of())
                    .fieldsToUpdate(List.of())
                    .totalChanges(0)
                    .build();
        }

        LegislatorDetailDTO legislator = legislatorOpt.get();

        // Find local Person
        Optional<Person> personOpt = personRepository.findByBioguideId(bioguideId);

        if (personOpt.isEmpty()) {
            // No local match - can't enrich
            Map<String, Object> newExternalIds = buildExternalIdsMap(legislator.getExternalIds());
            Map<String, String> newSocialMedia = buildSocialMediaMap(legislator.getSocialMedia());

            return EnrichmentPreview.builder()
                    .bioguideId(bioguideId)
                    .localMatch(false)
                    .newData(EnrichmentPreview.EnrichmentData.builder()
                            .externalIds(newExternalIds)
                            .socialMedia(newSocialMedia)
                            .build())
                    .fieldsToAdd(List.of())
                    .fieldsToUpdate(List.of())
                    .totalChanges(0)
                    .build();
        }

        Person person = personOpt.get();

        // Calculate diff
        Map<String, Object> newExternalIds = buildExternalIdsMap(legislator.getExternalIds());
        Map<String, String> newSocialMedia = buildSocialMediaMap(legislator.getSocialMedia());

        Map<String, Object> currentExternalIds = jsonNodeToMap(person.getExternalIds());
        Map<String, Object> currentSocialMedia = jsonNodeToMap(person.getSocialMedia());

        List<String> fieldsToAdd = new ArrayList<>();
        List<String> fieldsToUpdate = new ArrayList<>();

        // Check external IDs
        for (Map.Entry<String, Object> entry : newExternalIds.entrySet()) {
            String key = "externalIds." + entry.getKey();
            if (!currentExternalIds.containsKey(entry.getKey()) || currentExternalIds.get(entry.getKey()) == null) {
                fieldsToAdd.add(key);
            } else if (!Objects.equals(currentExternalIds.get(entry.getKey()), entry.getValue())) {
                fieldsToUpdate.add(key);
            }
        }

        // Check social media
        for (Map.Entry<String, String> entry : newSocialMedia.entrySet()) {
            String key = "socialMedia." + entry.getKey();
            if (!currentSocialMedia.containsKey(entry.getKey()) || currentSocialMedia.get(entry.getKey()) == null) {
                fieldsToAdd.add(key);
            } else if (!Objects.equals(currentSocialMedia.get(entry.getKey()), entry.getValue())) {
                fieldsToUpdate.add(key);
            }
        }

        return EnrichmentPreview.builder()
                .bioguideId(bioguideId)
                .localMatch(true)
                .currentPerson(EnrichmentPreview.PersonSnapshot.builder()
                        .id(person.getId().toString())
                        .name(person.getFirstName() + " " + person.getLastName())
                        .externalIds(currentExternalIds)
                        .socialMedia(currentSocialMedia)
                        .enrichmentSource(person.getEnrichmentSource())
                        .enrichmentVersion(person.getEnrichmentVersion())
                        .build())
                .newData(EnrichmentPreview.EnrichmentData.builder()
                        .externalIds(newExternalIds)
                        .socialMedia(newSocialMedia)
                        .build())
                .fieldsToAdd(fieldsToAdd)
                .fieldsToUpdate(fieldsToUpdate)
                .totalChanges(fieldsToAdd.size() + fieldsToUpdate.size())
                .build();
    }

    /**
     * Enrich a Person record from Legislators Repo data.
     *
     * @param bioguideId The BioGuide ID to enrich
     * @return Enrichment result
     */
    @Transactional
    public LegislatorEnrichmentResult enrichPerson(String bioguideId) {
        log.info("Enriching person with bioguideId={}", bioguideId);

        // Find local Person
        Optional<Person> personOpt = personRepository.findByBioguideId(bioguideId);

        if (personOpt.isEmpty()) {
            return LegislatorEnrichmentResult.builder()
                    .matched(false)
                    .bioguideId(bioguideId)
                    .error("No local Person found with bioguideId: " + bioguideId)
                    .fieldsAdded(List.of())
                    .fieldsUpdated(List.of())
                    .totalChanges(0)
                    .build();
        }

        // Find the legislator in the repo
        Optional<LegislatorDetailDTO> legislatorOpt = legislatorsSearchService.getLegislatorByBioguideId(bioguideId);

        if (legislatorOpt.isEmpty()) {
            return LegislatorEnrichmentResult.builder()
                    .matched(false)
                    .bioguideId(bioguideId)
                    .error("Legislator not found in Legislators Repo: " + bioguideId)
                    .fieldsAdded(List.of())
                    .fieldsUpdated(List.of())
                    .totalChanges(0)
                    .build();
        }

        Person person = personOpt.get();
        LegislatorDetailDTO legislator = legislatorOpt.get();

        // Get current values
        Map<String, Object> currentExternalIds = jsonNodeToMap(person.getExternalIds());
        Map<String, Object> currentSocialMedia = jsonNodeToMap(person.getSocialMedia());

        // Get new values
        Map<String, Object> newExternalIds = buildExternalIdsMap(legislator.getExternalIds());
        Map<String, String> newSocialMedia = buildSocialMediaMap(legislator.getSocialMedia());

        List<String> fieldsAdded = new ArrayList<>();
        List<String> fieldsUpdated = new ArrayList<>();

        // Merge external IDs (only add, don't overwrite)
        ObjectNode mergedExternalIds = mergeJsonNodes(person.getExternalIds(), newExternalIds, "externalIds", fieldsAdded);
        person.setExternalIds(mergedExternalIds);

        // Merge social media (only add, don't overwrite)
        ObjectNode mergedSocialMedia = mergeJsonNodes(person.getSocialMedia(), newSocialMedia, "socialMedia", fieldsAdded);
        person.setSocialMedia(mergedSocialMedia);

        // Get latest commit SHA for version tracking
        String commitSha = legislatorsRepoClient.fetchLatestCommitSha().orElse("unknown");

        // Set enrichment tracking
        person.setEnrichmentSource(ENRICHMENT_SOURCE);
        person.setEnrichmentVersion(commitSha);

        personRepository.save(person);

        log.info("Enriched person {}: added={}, updated={}",
                bioguideId, fieldsAdded.size(), fieldsUpdated.size());

        return LegislatorEnrichmentResult.builder()
                .matched(true)
                .personId(person.getId().toString())
                .personName(person.getFirstName() + " " + person.getLastName())
                .bioguideId(bioguideId)
                .fieldsAdded(fieldsAdded)
                .fieldsUpdated(fieldsUpdated)
                .totalChanges(fieldsAdded.size() + fieldsUpdated.size())
                .build();
    }

    /**
     * Convert JsonNode to Map for comparison.
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> jsonNodeToMap(JsonNode node) {
        if (node == null || node.isNull() || node.isEmpty()) {
            return new HashMap<>();
        }
        return objectMapper.convertValue(node, Map.class);
    }

    /**
     * Merge new values into existing JSON, only adding keys that don't exist.
     */
    private ObjectNode mergeJsonNodes(JsonNode existing, Map<String, ?> newValues, String prefix, List<String> fieldsAdded) {
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
                fieldsAdded.add(prefix + "." + key);
            }
        }

        return result;
    }

    /**
     * Build external IDs map from detail DTO.
     */
    private Map<String, Object> buildExternalIdsMap(LegislatorDetailDTO.ExternalIdsInfo ids) {
        if (ids == null) return Map.of();

        Map<String, Object> map = new HashMap<>();
        if (ids.getGovtrack() != null) map.put("govtrack", ids.getGovtrack());
        if (ids.getOpensecrets() != null) map.put("opensecrets", ids.getOpensecrets());
        if (ids.getVotesmart() != null) map.put("votesmart", ids.getVotesmart());
        if (ids.getFec() != null && !ids.getFec().isEmpty()) map.put("fec", ids.getFec());
        if (ids.getThomas() != null) map.put("thomas", ids.getThomas());
        if (ids.getWikipedia() != null) map.put("wikipedia", ids.getWikipedia());
        if (ids.getBallotpedia() != null) map.put("ballotpedia", ids.getBallotpedia());
        if (ids.getIcpsr() != null) map.put("icpsr", ids.getIcpsr());
        if (ids.getLis() != null) map.put("lis", ids.getLis());
        if (ids.getCspan() != null) map.put("cspan", ids.getCspan());
        if (ids.getHouseHistory() != null) map.put("house_history", ids.getHouseHistory());
        return map;
    }

    /**
     * Build social media map from detail DTO.
     */
    private Map<String, String> buildSocialMediaMap(LegislatorDetailDTO.SocialMediaInfo social) {
        if (social == null) return Map.of();

        Map<String, String> map = new HashMap<>();
        if (social.getTwitter() != null) map.put("twitter", social.getTwitter());
        if (social.getFacebook() != null) map.put("facebook", social.getFacebook());
        if (social.getYoutube() != null) map.put("youtube", social.getYoutube());
        if (social.getInstagram() != null) map.put("instagram", social.getInstagram());
        return map;
    }
}
