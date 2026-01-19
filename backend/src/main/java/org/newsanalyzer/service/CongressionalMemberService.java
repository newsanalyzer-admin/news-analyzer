package org.newsanalyzer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.newsanalyzer.model.CongressionalMember;
import org.newsanalyzer.model.DataSource;
import org.newsanalyzer.model.Individual;
import org.newsanalyzer.repository.CongressionalMemberRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service for CongressionalMember entity operations.
 *
 * Manages Congressional member records (Senators and Representatives)
 * which are linked to Individual records for biographical data.
 *
 * Part of ARCH-1.6: Update Services Layer
 *
 * @author Claude (Dev Agent)
 * @since 3.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CongressionalMemberService {

    private final CongressionalMemberRepository congressionalMemberRepository;
    private final IndividualService individualService;

    // =====================================================================
    // CRUD Operations
    // =====================================================================

    /**
     * Find congressional member by ID.
     */
    public Optional<CongressionalMember> findById(UUID id) {
        return congressionalMemberRepository.findById(id);
    }

    /**
     * Save or update a congressional member.
     */
    @Transactional
    public CongressionalMember save(CongressionalMember member) {
        return congressionalMemberRepository.save(member);
    }

    /**
     * Delete a congressional member by ID.
     */
    @Transactional
    public void deleteById(UUID id) {
        congressionalMemberRepository.deleteById(id);
    }

    /**
     * Find all congressional members.
     */
    public List<CongressionalMember> findAll() {
        return congressionalMemberRepository.findAll();
    }

    /**
     * Find all congressional members with pagination.
     */
    public Page<CongressionalMember> findAll(Pageable pageable) {
        return congressionalMemberRepository.findAll(pageable);
    }

    // =====================================================================
    // Identifier Lookups
    // =====================================================================

    /**
     * Find congressional member by BioGuide ID.
     */
    public Optional<CongressionalMember> findByBioguideId(String bioguideId) {
        return congressionalMemberRepository.findByBioguideId(bioguideId);
    }

    /**
     * Find congressional member by linked Individual ID.
     */
    public Optional<CongressionalMember> findByIndividualId(UUID individualId) {
        return congressionalMemberRepository.findByIndividualId(individualId);
    }

    /**
     * Check if member exists by BioGuide ID.
     */
    public boolean existsByBioguideId(String bioguideId) {
        return congressionalMemberRepository.existsByBioguideId(bioguideId);
    }

    // =====================================================================
    // Eager Loading Queries
    // =====================================================================

    /**
     * Find congressional member by BioGuide ID with Individual eagerly loaded.
     */
    public Optional<CongressionalMember> findByBioguideIdWithIndividual(String bioguideId) {
        return congressionalMemberRepository.findByBioguideIdWithIndividual(bioguideId);
    }

    /**
     * Find all members by chamber with Individual eagerly loaded.
     */
    public List<CongressionalMember> findByChamberWithIndividual(CongressionalMember.Chamber chamber) {
        return congressionalMemberRepository.findByChamberWithIndividual(chamber);
    }

    /**
     * Find all members by state with Individual eagerly loaded.
     */
    public List<CongressionalMember> findByStateWithIndividual(String state) {
        return congressionalMemberRepository.findByStateWithIndividual(state);
    }

    // =====================================================================
    // Chamber/State/Party Queries
    // =====================================================================

    /**
     * Find all members by chamber.
     */
    public List<CongressionalMember> findByChamber(CongressionalMember.Chamber chamber) {
        return congressionalMemberRepository.findByChamber(chamber);
    }

    /**
     * Find all members by chamber with pagination.
     */
    public Page<CongressionalMember> findByChamber(CongressionalMember.Chamber chamber, Pageable pageable) {
        return congressionalMemberRepository.findByChamber(chamber, pageable);
    }

    /**
     * Find all members by state.
     */
    public List<CongressionalMember> findByState(String state) {
        return congressionalMemberRepository.findByState(state);
    }

    /**
     * Find all members by party.
     */
    public List<CongressionalMember> findByParty(String party) {
        return congressionalMemberRepository.findByParty(party);
    }

    /**
     * Find all members by state and chamber.
     */
    public List<CongressionalMember> findByStateAndChamber(String state, CongressionalMember.Chamber chamber) {
        return congressionalMemberRepository.findByStateAndChamber(state, chamber);
    }

    // =====================================================================
    // Statistics
    // =====================================================================

    /**
     * Count members by chamber.
     */
    public long countByChamber(CongressionalMember.Chamber chamber) {
        return congressionalMemberRepository.countByChamber(chamber);
    }

    /**
     * Count members by party.
     */
    public long countByParty(String party) {
        return congressionalMemberRepository.countByParty(party);
    }

    /**
     * Count members by state.
     */
    public long countByState(String state) {
        return congressionalMemberRepository.countByState(state);
    }

    /**
     * Count all congressional members.
     */
    public long count() {
        return congressionalMemberRepository.count();
    }

    // =====================================================================
    // Find or Create (Two-Entity Pattern)
    // =====================================================================

    /**
     * Find existing congressional member by BioGuide ID or create new one.
     *
     * This method implements the two-entity pattern:
     * 1. First finds or creates the Individual record
     * 2. Then finds or creates the CongressionalMember record linked to it
     *
     * @param bioguideId BioGuide ID (required, unique identifier)
     * @param firstName first name for Individual
     * @param lastName last name for Individual
     * @param birthDate birth date for Individual deduplication
     * @param chamber Senate or House
     * @param state 2-letter state code
     * @param party party affiliation
     * @return existing or newly created CongressionalMember
     */
    @Transactional
    public CongressionalMember findOrCreate(String bioguideId,
                                            String firstName, String lastName,
                                            java.time.LocalDate birthDate,
                                            CongressionalMember.Chamber chamber,
                                            String state, String party) {
        if (bioguideId == null || bioguideId.isBlank()) {
            throw new IllegalArgumentException("BioGuide ID is required");
        }

        // Check if member already exists
        Optional<CongressionalMember> existing = congressionalMemberRepository.findByBioguideId(bioguideId);
        if (existing.isPresent()) {
            CongressionalMember member = existing.get();
            // Update mutable fields if changed
            boolean updated = false;
            if (chamber != null && member.getChamber() != chamber) {
                member.setChamber(chamber);
                updated = true;
            }
            if (state != null && !state.equals(member.getState())) {
                member.setState(state);
                updated = true;
            }
            if (party != null && !party.equals(member.getParty())) {
                member.setParty(party);
                updated = true;
            }
            if (updated) {
                log.debug("Updated congressional member: {}", bioguideId);
                return congressionalMemberRepository.save(member);
            }
            return member;
        }

        // Find or create Individual first
        Individual individual = individualService.findOrCreate(
                firstName, lastName, birthDate, DataSource.CONGRESS_GOV);

        // Create new CongressionalMember
        log.info("Creating new congressional member: {} ({} {})", bioguideId, firstName, lastName);
        CongressionalMember member = CongressionalMember.builder()
                .individualId(individual.getId())
                .bioguideId(bioguideId)
                .chamber(chamber)
                .state(state)
                .party(party)
                .dataSource(DataSource.CONGRESS_GOV)
                .build();

        return congressionalMemberRepository.save(member);
    }

    /**
     * Get the linked Individual for a congressional member.
     *
     * @param member the congressional member
     * @return the linked Individual
     */
    public Optional<Individual> getIndividual(CongressionalMember member) {
        if (member == null || member.getIndividualId() == null) {
            return Optional.empty();
        }
        return individualService.findById(member.getIndividualId());
    }
}
