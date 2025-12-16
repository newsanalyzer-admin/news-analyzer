package org.newsanalyzer.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.newsanalyzer.dto.FederalRegisterAgency;
import org.newsanalyzer.dto.LinkageStatistics;
import org.newsanalyzer.model.GovernmentOrganization;
import org.newsanalyzer.model.Regulation;
import org.newsanalyzer.model.RegulationAgency;
import org.newsanalyzer.repository.GovernmentOrganizationRepository;
import org.newsanalyzer.repository.RegulationAgencyRepository;
import org.newsanalyzer.repository.RegulationRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for linking regulations to government organizations.
 *
 * Provides multi-level matching:
 * 1. Federal Register agency ID (highest confidence)
 * 2. Exact name match (case-insensitive)
 * 3. Acronym match
 * 4. Manual mapping lookup
 * 5. Fuzzy matching using Levenshtein distance
 *
 * @author James (Dev Agent)
 * @since 2.0.0
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AgencyLinkageService {

    private final GovernmentOrganizationRepository govOrgRepository;
    private final RegulationAgencyRepository regulationAgencyRepository;
    private final RegulationRepository regulationRepository;

    /**
     * Similarity threshold for fuzzy matching (0.0 - 1.0).
     * Default is 0.85 (85% similarity required).
     */
    @Value("${federal-register.linkage.fuzzy-threshold:0.85}")
    private double fuzzyThreshold;

    /**
     * Whether fuzzy matching is enabled.
     */
    @Value("${federal-register.linkage.fuzzy-enabled:true}")
    private boolean fuzzyEnabled;

    // =====================================================================
    // Caches
    // =====================================================================

    /** Cache: normalized official name -> organization ID */
    private final Map<String, UUID> nameToIdCache = new ConcurrentHashMap<>();

    /** Cache: uppercase acronym -> organization ID */
    private final Map<String, UUID> acronymToIdCache = new ConcurrentHashMap<>();

    /** Cache: Federal Register agency ID -> organization ID */
    private final Map<Integer, UUID> federalRegisterIdCache = new ConcurrentHashMap<>();

    /** Track unmatched agency names for reporting */
    private final Set<String> unmatchedAgencies = ConcurrentHashMap.newKeySet();

    /** Levenshtein distance calculator for fuzzy matching */
    private final LevenshteinDistance levenshteinDistance = new LevenshteinDistance();

    // =====================================================================
    // Manual Mappings (Common Variations)
    // =====================================================================

    /**
     * Known name variations that map to standard acronyms.
     * Used when Federal Register name differs from GovernmentOrganization name.
     */
    private static final Map<String, String> KNOWN_NAME_TO_ACRONYM = Map.ofEntries(
            Map.entry("environmental protection agency", "EPA"),
            Map.entry("department of health and human services", "HHS"),
            Map.entry("department of transportation", "DOT"),
            Map.entry("securities and exchange commission", "SEC"),
            Map.entry("federal communications commission", "FCC"),
            Map.entry("food and drug administration", "FDA"),
            Map.entry("centers for medicare & medicaid services", "CMS"),
            Map.entry("centers for medicare and medicaid services", "CMS"),
            Map.entry("internal revenue service", "IRS"),
            Map.entry("federal aviation administration", "FAA"),
            Map.entry("occupational safety and health administration", "OSHA"),
            Map.entry("national aeronautics and space administration", "NASA"),
            Map.entry("department of defense", "DOD"),
            Map.entry("department of agriculture", "USDA"),
            Map.entry("department of commerce", "DOC"),
            Map.entry("department of education", "ED"),
            Map.entry("department of energy", "DOE"),
            Map.entry("department of homeland security", "DHS"),
            Map.entry("department of housing and urban development", "HUD"),
            Map.entry("department of the interior", "DOI"),
            Map.entry("department of justice", "DOJ"),
            Map.entry("department of labor", "DOL"),
            Map.entry("department of state", "DOS"),
            Map.entry("department of the treasury", "TREASURY"),
            Map.entry("department of veterans affairs", "VA"),
            Map.entry("federal reserve system", "FED"),
            Map.entry("federal trade commission", "FTC"),
            Map.entry("nuclear regulatory commission", "NRC"),
            Map.entry("consumer financial protection bureau", "CFPB"),
            Map.entry("small business administration", "SBA"),
            Map.entry("social security administration", "SSA")
    );

    // =====================================================================
    // Initialization
    // =====================================================================

    /**
     * Initialize caches on startup.
     */
    @PostConstruct
    public void init() {
        refreshCaches();
    }

    // =====================================================================
    // Core Linkage Methods
    // =====================================================================

    /**
     * Link a regulation to its agencies.
     *
     * @param regulation The regulation to link
     * @param frAgencies Agencies from Federal Register API
     * @return Number of agencies successfully linked
     */
    @Transactional
    public int linkRegulationToAgencies(Regulation regulation, List<FederalRegisterAgency> frAgencies) {
        if (frAgencies == null || frAgencies.isEmpty()) {
            return 0;
        }

        int linked = 0;
        boolean first = true;

        // Delete existing links for this regulation (for re-sync scenarios)
        regulationAgencyRepository.deleteByRegulationId(regulation.getId());

        for (FederalRegisterAgency frAgency : frAgencies) {
            Optional<UUID> orgId = findGovernmentOrganization(frAgency);

            if (orgId.isPresent()) {
                // Check if link already exists
                if (!regulationAgencyRepository.existsByRegulationIdAndOrganizationId(
                        regulation.getId(), orgId.get())) {

                    RegulationAgency link = RegulationAgency.builder()
                            .regulationId(regulation.getId())
                            .organizationId(orgId.get())
                            .agencyNameRaw(frAgency.getName())
                            .primaryAgency(first)
                            .build();

                    regulationAgencyRepository.save(link);
                    linked++;
                    first = false;

                    log.debug("Linked regulation {} to organization {} (agency: {})",
                            regulation.getDocumentNumber(), orgId.get(), frAgency.getName());
                }
            } else {
                // Track unmatched for reporting
                if (frAgency.getName() != null) {
                    unmatchedAgencies.add(frAgency.getName());
                    log.warn("Unmatched agency for regulation {}: '{}' (FR ID: {})",
                            regulation.getDocumentNumber(), frAgency.getName(), frAgency.getId());
                }
            }
        }

        return linked;
    }

    /**
     * Find matching GovernmentOrganization for a Federal Register agency.
     * Uses multi-level matching strategy.
     *
     * @param frAgency The Federal Register agency to match
     * @return Optional containing the organization ID if found
     */
    public Optional<UUID> findGovernmentOrganization(FederalRegisterAgency frAgency) {
        if (frAgency == null) {
            return Optional.empty();
        }

        // 1. Try Federal Register ID first (highest confidence)
        if (frAgency.getId() != null) {
            UUID idMatch = federalRegisterIdCache.get(frAgency.getId());
            if (idMatch != null) {
                log.trace("Matched by Federal Register ID: {} -> {}", frAgency.getId(), idMatch);
                return Optional.of(idMatch);
            }
        }

        // 2. Try exact name match (case-insensitive)
        String normalizedName = normalizeName(frAgency.getName());
        if (!normalizedName.isEmpty()) {
            UUID nameMatch = nameToIdCache.get(normalizedName);
            if (nameMatch != null) {
                log.trace("Matched by exact name: '{}' -> {}", frAgency.getName(), nameMatch);
                return Optional.of(nameMatch);
            }
        }

        // 3. Try acronym match (short_name from Federal Register)
        String shortName = frAgency.getShortName();
        if (shortName != null && !shortName.isEmpty()) {
            UUID acronymMatch = acronymToIdCache.get(shortName.toUpperCase());
            if (acronymMatch != null) {
                log.trace("Matched by acronym: '{}' -> {}", shortName, acronymMatch);
                return Optional.of(acronymMatch);
            }
        }

        // 4. Try manual mapping lookup
        String mappedAcronym = KNOWN_NAME_TO_ACRONYM.get(normalizedName);
        if (mappedAcronym != null) {
            UUID mappedMatch = acronymToIdCache.get(mappedAcronym.toUpperCase());
            if (mappedMatch != null) {
                log.trace("Matched by manual mapping: '{}' -> {} -> {}",
                        frAgency.getName(), mappedAcronym, mappedMatch);
                return Optional.of(mappedMatch);
            }
        }

        // 5. Try fuzzy matching (if enabled)
        if (fuzzyEnabled && !normalizedName.isEmpty()) {
            Optional<UUID> fuzzyMatch = findByFuzzyMatch(normalizedName);
            if (fuzzyMatch.isPresent()) {
                return fuzzyMatch;
            }
        }

        return Optional.empty();
    }

    // =====================================================================
    // Fuzzy Matching
    // =====================================================================

    /**
     * Find organization by fuzzy string matching using Levenshtein distance.
     *
     * @param normalizedName The normalized agency name to match
     * @return Optional containing the organization ID if a good match is found
     */
    private Optional<UUID> findByFuzzyMatch(String normalizedName) {
        UUID bestMatch = null;
        double bestSimilarity = 0.0;
        String bestMatchName = null;

        for (Map.Entry<String, UUID> entry : nameToIdCache.entrySet()) {
            double similarity = calculateSimilarity(normalizedName, entry.getKey());

            if (similarity > bestSimilarity && similarity >= fuzzyThreshold) {
                bestSimilarity = similarity;
                bestMatch = entry.getValue();
                bestMatchName = entry.getKey();
            }
        }

        if (bestMatch != null) {
            log.info("Fuzzy match: '{}' -> '{}' (similarity: {:.2f})",
                    normalizedName, bestMatchName, bestSimilarity);
            return Optional.of(bestMatch);
        }

        return Optional.empty();
    }

    /**
     * Calculate similarity between two strings using Levenshtein distance.
     *
     * @param s1 First string
     * @param s2 Second string
     * @return Similarity score between 0.0 and 1.0
     */
    private double calculateSimilarity(String s1, String s2) {
        if (s1 == null || s2 == null) {
            return 0.0;
        }
        if (s1.equals(s2)) {
            return 1.0;
        }

        int distance = levenshteinDistance.apply(s1, s2);
        int maxLength = Math.max(s1.length(), s2.length());

        if (maxLength == 0) {
            return 1.0;
        }

        return 1.0 - ((double) distance / maxLength);
    }

    // =====================================================================
    // Cache Management
    // =====================================================================

    /**
     * Refresh all caches from the database.
     * Should be called on startup and when GovernmentOrganization data changes.
     */
    public void refreshCaches() {
        log.info("Refreshing agency linkage caches...");

        nameToIdCache.clear();
        acronymToIdCache.clear();
        federalRegisterIdCache.clear();

        List<GovernmentOrganization> orgs = govOrgRepository.findAll();

        for (GovernmentOrganization org : orgs) {
            // Cache by normalized official name
            if (org.getOfficialName() != null) {
                nameToIdCache.put(normalizeName(org.getOfficialName()), org.getId());
            }

            // Cache by uppercase acronym
            if (org.getAcronym() != null && !org.getAcronym().isEmpty()) {
                acronymToIdCache.put(org.getAcronym().toUpperCase(), org.getId());
            }

            // Cache by Federal Register agency ID
            if (org.getFederalRegisterAgencyId() != null) {
                federalRegisterIdCache.put(org.getFederalRegisterAgencyId(), org.getId());
            }

            // Also cache former names if available
            if (org.getFormerNames() != null) {
                for (String formerName : org.getFormerNames()) {
                    if (formerName != null && !formerName.isEmpty()) {
                        nameToIdCache.put(normalizeName(formerName), org.getId());
                    }
                }
            }
        }

        log.info("Agency linkage caches refreshed: {} names, {} acronyms, {} FR IDs",
                nameToIdCache.size(), acronymToIdCache.size(), federalRegisterIdCache.size());
    }

    // =====================================================================
    // Unmatched Tracking
    // =====================================================================

    /**
     * Get list of unmatched agency names for review.
     *
     * @return Unmodifiable set of unmatched agency names
     */
    public Set<String> getUnmatchedAgencies() {
        return Collections.unmodifiableSet(new TreeSet<>(unmatchedAgencies));
    }

    /**
     * Clear the unmatched agencies list.
     */
    public void clearUnmatchedAgencies() {
        unmatchedAgencies.clear();
        log.info("Unmatched agencies list cleared");
    }

    /**
     * Get count of unmatched agencies.
     *
     * @return Number of distinct unmatched agency names
     */
    public int getUnmatchedCount() {
        return unmatchedAgencies.size();
    }

    // =====================================================================
    // Statistics
    // =====================================================================

    /**
     * Get comprehensive linkage statistics.
     *
     * @return LinkageStatistics object with current stats
     */
    public LinkageStatistics getStatistics() {
        long totalRegulations = regulationRepository.count();
        long linkedRegulations = regulationAgencyRepository.countDistinctRegulations();
        long unmatchedCount = unmatchedAgencies.size();

        return LinkageStatistics.builder()
                .totalRegulations(totalRegulations)
                .linkedRegulations(linkedRegulations)
                .unmatchedAgencyNames(unmatchedCount)
                .build();
    }

    // =====================================================================
    // Utility Methods
    // =====================================================================

    /**
     * Normalize agency name for matching.
     * Converts to lowercase, removes extra whitespace.
     *
     * @param name The name to normalize
     * @return Normalized name
     */
    private String normalizeName(String name) {
        if (name == null) {
            return "";
        }
        return name.toLowerCase()
                .replaceAll("\\s+", " ")
                .trim();
    }

    /**
     * Get cache sizes for monitoring.
     *
     * @return Map with cache names and sizes
     */
    public Map<String, Integer> getCacheSizes() {
        return Map.of(
                "nameCache", nameToIdCache.size(),
                "acronymCache", acronymToIdCache.size(),
                "federalRegisterIdCache", federalRegisterIdCache.size()
        );
    }
}
