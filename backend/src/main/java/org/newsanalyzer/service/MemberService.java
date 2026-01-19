package org.newsanalyzer.service;

import org.newsanalyzer.exception.ResourceNotFoundException;
import org.newsanalyzer.model.CongressionalMember;
import org.newsanalyzer.model.CongressionalMember.Chamber;
import org.newsanalyzer.model.Individual;
import org.newsanalyzer.repository.CongressionalMemberRepository;
import org.newsanalyzer.repository.IndividualRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for member (CongressionalMember) lookup operations.
 *
 * Provides business logic for the MemberController.
 *
 * Part of ARCH-1.6: Updated to use CongressionalMember instead of Person.
 *
 * @author James (Dev Agent)
 * @since 2.0.0
 */
@Service
@Transactional(readOnly = true)
public class MemberService {

    private static final Logger log = LoggerFactory.getLogger(MemberService.class);

    private final CongressionalMemberRepository congressionalMemberRepository;
    private final IndividualRepository individualRepository;

    public MemberService(CongressionalMemberRepository congressionalMemberRepository,
                        IndividualRepository individualRepository) {
        this.congressionalMemberRepository = congressionalMemberRepository;
        this.individualRepository = individualRepository;
    }

    /**
     * Find all members with pagination.
     */
    public Page<CongressionalMember> findAll(Pageable pageable) {
        return congressionalMemberRepository.findAll(pageable);
    }

    /**
     * Find member by ID.
     */
    public Optional<CongressionalMember> findById(UUID id) {
        return congressionalMemberRepository.findById(id);
    }

    /**
     * Find member by BioGuide ID.
     */
    public Optional<CongressionalMember> findByBioguideId(String bioguideId) {
        return congressionalMemberRepository.findByBioguideId(bioguideId);
    }

    /**
     * Find member by BioGuide ID or throw exception.
     */
    public CongressionalMember getByBioguideId(String bioguideId) {
        return congressionalMemberRepository.findByBioguideId(bioguideId)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found with bioguideId: " + bioguideId));
    }

    /**
     * Get the Individual associated with a CongressionalMember.
     */
    public Optional<Individual> getIndividual(CongressionalMember member) {
        if (member == null || member.getIndividualId() == null) {
            return Optional.empty();
        }
        return individualRepository.findById(member.getIndividualId());
    }

    /**
     * Search members by name (searches linked Individual names).
     */
    public Page<CongressionalMember> searchByName(String name, Pageable pageable) {
        // Search individuals by name, then find corresponding congressional members
        List<Individual> matchingIndividuals = individualRepository.searchByName(name);
        List<UUID> individualIds = matchingIndividuals.stream()
                .map(Individual::getId)
                .collect(Collectors.toList());

        // Find congressional members with these individual IDs
        List<CongressionalMember> members = congressionalMemberRepository.findAll().stream()
                .filter(m -> individualIds.contains(m.getIndividualId()))
                .collect(Collectors.toList());

        // Apply pagination
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), members.size());
        List<CongressionalMember> pageContent = start < members.size()
                ? members.subList(start, end)
                : List.of();

        return new PageImpl<>(pageContent, pageable, members.size());
    }

    /**
     * Search members by name (returns list).
     */
    public List<CongressionalMember> searchByName(String name) {
        List<Individual> matchingIndividuals = individualRepository.searchByName(name);
        List<UUID> individualIds = matchingIndividuals.stream()
                .map(Individual::getId)
                .collect(Collectors.toList());

        return congressionalMemberRepository.findAll().stream()
                .filter(m -> individualIds.contains(m.getIndividualId()))
                .collect(Collectors.toList());
    }

    /**
     * Find members by state.
     */
    public Page<CongressionalMember> findByState(String state, Pageable pageable) {
        return congressionalMemberRepository.findByState(state.toUpperCase(), pageable);
    }

    /**
     * Find members by state (returns list).
     */
    public List<CongressionalMember> findByState(String state) {
        return congressionalMemberRepository.findByState(state.toUpperCase());
    }

    /**
     * Find members by chamber.
     */
    public Page<CongressionalMember> findByChamber(Chamber chamber, Pageable pageable) {
        return congressionalMemberRepository.findByChamber(chamber, pageable);
    }

    /**
     * Find members by chamber (returns list).
     */
    public List<CongressionalMember> findByChamber(Chamber chamber) {
        return congressionalMemberRepository.findByChamber(chamber);
    }

    /**
     * Find members by party.
     */
    public Page<CongressionalMember> findByParty(String party, Pageable pageable) {
        return congressionalMemberRepository.findByParty(party, pageable);
    }

    /**
     * Get total member count.
     */
    public long count() {
        return congressionalMemberRepository.count();
    }

    /**
     * Get count by chamber.
     */
    public long countByChamber(Chamber chamber) {
        return congressionalMemberRepository.countByChamber(chamber);
    }

    /**
     * Get party distribution.
     */
    public List<Object[]> getPartyDistribution() {
        return congressionalMemberRepository.getPartyDistribution();
    }

    /**
     * Get state distribution.
     */
    public List<Object[]> getStateDistribution() {
        return congressionalMemberRepository.getStateDistribution();
    }

    /**
     * Find member by external ID (FEC, GovTrack, OpenSecrets, VoteSmart).
     *
     * @param type The ID type: fec, govtrack, opensecrets, votesmart
     * @param id The ID value
     * @return Optional containing the member if found
     * @throws IllegalArgumentException if type is not supported
     */
    public Optional<CongressionalMember> findByExternalId(String type, String id) {
        if (type == null || id == null) {
            return Optional.empty();
        }

        String normalizedType = type.toLowerCase().trim();
        log.debug("Looking up member by external ID: type={}, id={}", normalizedType, id);

        return switch (normalizedType) {
            case "fec" -> congressionalMemberRepository.findByFecId(id);
            case "govtrack" -> {
                try {
                    yield congressionalMemberRepository.findByGovtrackId(Integer.parseInt(id));
                } catch (NumberFormatException e) {
                    log.warn("Invalid GovTrack ID (not an integer): {}", id);
                    yield Optional.empty();
                }
            }
            case "opensecrets" -> congressionalMemberRepository.findByOpensecretsId(id);
            case "votesmart" -> {
                try {
                    yield congressionalMemberRepository.findByVotesmartId(Integer.parseInt(id));
                } catch (NumberFormatException e) {
                    log.warn("Invalid VoteSmart ID (not an integer): {}", id);
                    yield Optional.empty();
                }
            }
            default -> {
                log.warn("Unsupported external ID type: {}", normalizedType);
                yield Optional.empty();
            }
        };
    }
}
