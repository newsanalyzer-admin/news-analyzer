package org.newsanalyzer.service;

import lombok.extern.slf4j.Slf4j;
import org.newsanalyzer.dto.*;
import org.newsanalyzer.model.Person;
import org.newsanalyzer.repository.PersonRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Service for searching legislators from the unitedstates/congress-legislators repository.
 *
 * Provides search/filter functionality with caching to avoid repeated GitHub fetches.
 *
 * @author James (Dev Agent)
 * @since 2.0.0
 */
@Service
@Slf4j
public class LegislatorsSearchService {

    private static final String SOURCE_NAME = "Legislators Repo";
    private static final String SOURCE_URL = "https://github.com/unitedstates/congress-legislators";
    private static final long CACHE_TTL_MS = 15 * 60 * 1000; // 15 minutes

    private final LegislatorsRepoClient legislatorsRepoClient;
    private final PersonRepository personRepository;

    // In-memory cache
    private List<LegislatorYamlRecord> cachedLegislators = new ArrayList<>();
    private Instant cacheTimestamp = Instant.EPOCH;
    private final Object cacheLock = new Object();

    public LegislatorsSearchService(LegislatorsRepoClient legislatorsRepoClient,
                                     PersonRepository personRepository) {
        this.legislatorsRepoClient = legislatorsRepoClient;
        this.personRepository = personRepository;
    }

    /**
     * Search legislators with filtering and pagination.
     *
     * @param name      Name filter (partial match on full name)
     * @param bioguideId BioGuide ID filter (exact match)
     * @param state     State filter (exact match)
     * @param page      Page number (1-indexed)
     * @param pageSize  Results per page
     * @return Search response with results and pagination info
     */
    public LegislatorsSearchResponse<LegislatorSearchDTO> searchLegislators(
            String name,
            String bioguideId,
            String state,
            int page,
            int pageSize) {

        log.info("Searching legislators: name={}, bioguideId={}, state={}, page={}, pageSize={}",
                name, bioguideId, state, page, pageSize);

        // Get cached or fetch fresh data
        List<LegislatorYamlRecord> allLegislators = getCachedLegislators();
        boolean fromCache = !cacheTimestamp.equals(Instant.EPOCH);

        // Apply filters
        Stream<LegislatorYamlRecord> stream = allLegislators.stream();

        if (bioguideId != null && !bioguideId.isEmpty()) {
            String lowerBioguide = bioguideId.toLowerCase();
            stream = stream.filter(r -> r.getBioguideId() != null &&
                    r.getBioguideId().toLowerCase().equals(lowerBioguide));
        }

        if (name != null && !name.isEmpty()) {
            String lowerName = name.toLowerCase();
            stream = stream.filter(r -> {
                if (r.getName() == null) return false;
                String fullName = buildFullName(r.getName());
                return fullName.toLowerCase().contains(lowerName);
            });
        }

        if (state != null && !state.isEmpty()) {
            String upperState = state.toUpperCase();
            stream = stream.filter(r -> {
                LegislatorYamlRecord.LegislatorTerm term = getMostRecentTerm(r);
                return term != null && upperState.equals(term.getState());
            });
        }

        // Collect filtered results
        List<LegislatorYamlRecord> filtered = stream.collect(Collectors.toList());
        int total = filtered.size();

        // Apply pagination
        int startIndex = (page - 1) * pageSize;
        int endIndex = Math.min(startIndex + pageSize, total);

        List<LegislatorsSearchResult<LegislatorSearchDTO>> results = new ArrayList<>();

        if (startIndex < total) {
            List<LegislatorYamlRecord> pageResults = filtered.subList(startIndex, endIndex);

            for (LegislatorYamlRecord record : pageResults) {
                LegislatorSearchDTO dto = mapToSearchDTO(record);

                // Check for local match
                String localMatchId = null;
                if (dto.getBioguideId() != null) {
                    Optional<Person> existing = personRepository.findByBioguideId(dto.getBioguideId());
                    if (existing.isPresent()) {
                        localMatchId = existing.get().getId().toString();
                    }
                }

                results.add(LegislatorsSearchResult.<LegislatorSearchDTO>builder()
                        .data(dto)
                        .source(SOURCE_NAME)
                        .sourceUrl(SOURCE_URL)
                        .localMatchId(localMatchId)
                        .build());
            }
        }

        return LegislatorsSearchResponse.<LegislatorSearchDTO>builder()
                .results(results)
                .total(total)
                .page(page)
                .pageSize(pageSize)
                .cached(fromCache)
                .build();
    }

    /**
     * Get full details for a single legislator by bioguideId.
     *
     * @param bioguideId The BioGuide ID
     * @return Legislator details or empty if not found
     */
    public Optional<LegislatorDetailDTO> getLegislatorByBioguideId(String bioguideId) {
        log.info("Fetching legislator details: bioguideId={}", bioguideId);

        List<LegislatorYamlRecord> allLegislators = getCachedLegislators();

        return allLegislators.stream()
                .filter(r -> bioguideId.equals(r.getBioguideId()))
                .findFirst()
                .map(this::mapToDetailDTO);
    }

    /**
     * Check if a bioguideId exists in the local Person table.
     *
     * @param bioguideId The BioGuide ID to check
     * @return Optional containing the Person if found
     */
    public Optional<Person> findLocalPerson(String bioguideId) {
        return personRepository.findByBioguideId(bioguideId);
    }

    /**
     * Get cached legislators or fetch fresh data if cache expired.
     */
    private List<LegislatorYamlRecord> getCachedLegislators() {
        synchronized (cacheLock) {
            Instant now = Instant.now();
            if (cachedLegislators.isEmpty() ||
                    now.toEpochMilli() - cacheTimestamp.toEpochMilli() > CACHE_TTL_MS) {

                log.info("Cache expired or empty, fetching fresh legislator data");

                List<LegislatorYamlRecord> current = legislatorsRepoClient.fetchCurrentLegislators();
                List<LegislatorYamlRecord> historical = legislatorsRepoClient.fetchHistoricalLegislators();

                // Combine and deduplicate by bioguideId (prefer current over historical)
                Map<String, LegislatorYamlRecord> combined = new LinkedHashMap<>();

                // Add historical first
                for (LegislatorYamlRecord record : historical) {
                    if (record.getBioguideId() != null) {
                        combined.put(record.getBioguideId(), record);
                    }
                }

                // Overwrite with current (so current takes precedence)
                for (LegislatorYamlRecord record : current) {
                    if (record.getBioguideId() != null) {
                        combined.put(record.getBioguideId(), record);
                    }
                }

                cachedLegislators = new ArrayList<>(combined.values());
                cacheTimestamp = now;

                log.info("Cached {} legislators (current={}, historical={})",
                        cachedLegislators.size(), current.size(), historical.size());
            }

            return cachedLegislators;
        }
    }

    /**
     * Map YAML record to search DTO.
     */
    private LegislatorSearchDTO mapToSearchDTO(LegislatorYamlRecord record) {
        LegislatorYamlRecord.LegislatorTerm term = getMostRecentTerm(record);

        Map<String, String> socialMedia = record.buildSocialMediaMap();
        Map<String, Object> externalIds = record.buildExternalIdsMap();

        return LegislatorSearchDTO.builder()
                .bioguideId(record.getBioguideId())
                .name(buildFullName(record.getName()))
                .state(term != null ? term.getState() : null)
                .party(term != null ? term.getParty() : null)
                .chamber(term != null ? mapChamber(term.getType()) : null)
                .currentMember(isCurrentMember(record))
                .socialMedia(socialMedia)
                .externalIds(externalIds)
                .socialMediaCount(socialMedia.size())
                .externalIdCount(externalIds.size())
                .build();
    }

    /**
     * Map YAML record to detail DTO.
     */
    private LegislatorDetailDTO mapToDetailDTO(LegislatorYamlRecord record) {
        LegislatorYamlRecord.LegislatorName name = record.getName();
        LegislatorYamlRecord.LegislatorBio bio = record.getBio();
        LegislatorYamlRecord.LegislatorSocial social = record.getSocial();
        LegislatorYamlRecord.LegislatorId id = record.getId();

        LegislatorYamlRecord.LegislatorTerm mostRecentTerm = getMostRecentTerm(record);

        // Map terms
        List<LegislatorDetailDTO.TermInfo> terms = new ArrayList<>();
        if (record.getTerms() != null) {
            for (LegislatorYamlRecord.LegislatorTerm term : record.getTerms()) {
                terms.add(mapTermInfo(term));
            }
        }

        return LegislatorDetailDTO.builder()
                .bioguideId(record.getBioguideId())
                .name(mapNameInfo(name))
                .bio(mapBioInfo(bio))
                .currentTerm(mostRecentTerm != null ? mapTermInfo(mostRecentTerm) : null)
                .terms(terms)
                .socialMedia(mapSocialMediaInfo(social))
                .externalIds(mapExternalIdsInfo(id))
                .currentMember(isCurrentMember(record))
                .build();
    }

    private LegislatorDetailDTO.NameInfo mapNameInfo(LegislatorYamlRecord.LegislatorName name) {
        if (name == null) return null;
        return LegislatorDetailDTO.NameInfo.builder()
                .first(name.getFirst())
                .last(name.getLast())
                .middle(name.getMiddle())
                .suffix(name.getSuffix())
                .nickname(name.getNickname())
                .officialFull(name.getOfficialFull())
                .build();
    }

    private LegislatorDetailDTO.BioInfo mapBioInfo(LegislatorYamlRecord.LegislatorBio bio) {
        if (bio == null) return null;
        return LegislatorDetailDTO.BioInfo.builder()
                .birthday(bio.getBirthday())
                .gender(bio.getGender())
                .build();
    }

    private LegislatorDetailDTO.TermInfo mapTermInfo(LegislatorYamlRecord.LegislatorTerm term) {
        if (term == null) return null;
        return LegislatorDetailDTO.TermInfo.builder()
                .type(mapChamber(term.getType()))
                .start(term.getStart())
                .end(term.getEnd())
                .state(term.getState())
                .party(term.getParty())
                .district(term.getDistrict())
                .senateClass(term.getSenateClass())
                .stateRank(term.getStateRank())
                .url(term.getUrl())
                .phone(term.getPhone())
                .office(term.getOffice())
                .build();
    }

    private LegislatorDetailDTO.SocialMediaInfo mapSocialMediaInfo(LegislatorYamlRecord.LegislatorSocial social) {
        if (social == null) return null;
        return LegislatorDetailDTO.SocialMediaInfo.builder()
                .twitter(social.getTwitter())
                .twitterId(social.getTwitterId())
                .facebook(social.getFacebook())
                .facebookId(social.getFacebookId())
                .youtube(social.getYoutube())
                .youtubeId(social.getYoutubeId())
                .instagram(social.getInstagram())
                .instagramId(social.getInstagramId())
                .build();
    }

    private LegislatorDetailDTO.ExternalIdsInfo mapExternalIdsInfo(LegislatorYamlRecord.LegislatorId id) {
        if (id == null) return null;
        return LegislatorDetailDTO.ExternalIdsInfo.builder()
                .bioguide(id.getBioguide())
                .thomas(id.getThomas())
                .govtrack(id.getGovtrack())
                .opensecrets(id.getOpensecrets())
                .votesmart(id.getVotesmart())
                .fec(id.getFecIds())
                .wikipedia(id.getWikipedia())
                .ballotpedia(id.getBallotpedia())
                .icpsr(id.getIcpsr())
                .lis(id.getLis())
                .cspan(id.getCspan())
                .houseHistory(id.getHouseHistory())
                .build();
    }

    private String buildFullName(LegislatorYamlRecord.LegislatorName name) {
        if (name == null) return "Unknown";
        if (name.getOfficialFull() != null) return name.getOfficialFull();

        StringBuilder sb = new StringBuilder();
        if (name.getFirst() != null) sb.append(name.getFirst());
        if (name.getMiddle() != null) sb.append(" ").append(name.getMiddle());
        if (name.getLast() != null) sb.append(" ").append(name.getLast());
        if (name.getSuffix() != null) sb.append(" ").append(name.getSuffix());
        return sb.toString().trim();
    }

    private LegislatorYamlRecord.LegislatorTerm getMostRecentTerm(LegislatorYamlRecord record) {
        if (record.getTerms() == null || record.getTerms().isEmpty()) {
            return null;
        }
        // Terms are typically ordered by date; get the last one
        return record.getTerms().get(record.getTerms().size() - 1);
    }

    private boolean isCurrentMember(LegislatorYamlRecord record) {
        LegislatorYamlRecord.LegislatorTerm term = getMostRecentTerm(record);
        if (term == null || term.getEnd() == null) return false;
        // If end date is in the future, they're current
        try {
            java.time.LocalDate endDate = java.time.LocalDate.parse(term.getEnd());
            return endDate.isAfter(java.time.LocalDate.now());
        } catch (Exception e) {
            return false;
        }
    }

    private String mapChamber(String type) {
        if (type == null) return null;
        return switch (type.toLowerCase()) {
            case "sen" -> "Senate";
            case "rep" -> "House";
            default -> type;
        };
    }
}
