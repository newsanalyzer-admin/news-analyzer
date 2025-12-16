package org.newsanalyzer.service;

import org.newsanalyzer.exception.ResourceNotFoundException;
import org.newsanalyzer.model.Committee;
import org.newsanalyzer.model.CommitteeChamber;
import org.newsanalyzer.model.CommitteeMembership;
import org.newsanalyzer.model.CommitteeType;
import org.newsanalyzer.repository.CommitteeMembershipRepository;
import org.newsanalyzer.repository.CommitteeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service for committee lookup operations.
 *
 * Provides business logic for the CommitteeController.
 *
 * @author James (Dev Agent)
 * @since 2.0.0
 */
@Service
@Transactional(readOnly = true)
public class CommitteeService {

    private static final Logger log = LoggerFactory.getLogger(CommitteeService.class);

    private final CommitteeRepository committeeRepository;
    private final CommitteeMembershipRepository membershipRepository;

    public CommitteeService(CommitteeRepository committeeRepository,
                           CommitteeMembershipRepository membershipRepository) {
        this.committeeRepository = committeeRepository;
        this.membershipRepository = membershipRepository;
    }

    // =====================================================================
    // Committee Lookups
    // =====================================================================

    /**
     * Find all committees with pagination.
     */
    public Page<Committee> findAll(Pageable pageable) {
        return committeeRepository.findAll(pageable);
    }

    /**
     * Find committee by code.
     */
    public Optional<Committee> findByCode(String committeeCode) {
        return committeeRepository.findByCommitteeCode(committeeCode);
    }

    /**
     * Find committee by code or throw exception.
     */
    public Committee getByCode(String committeeCode) {
        return committeeRepository.findByCommitteeCode(committeeCode)
                .orElseThrow(() -> new ResourceNotFoundException("Committee not found: " + committeeCode));
    }

    /**
     * Search committees by name.
     */
    public Page<Committee> searchByName(String name, Pageable pageable) {
        return committeeRepository.findByNameContainingIgnoreCase(name, pageable);
    }

    /**
     * Search committees by name (returns list).
     */
    public List<Committee> searchByName(String name) {
        return committeeRepository.findByNameContainingIgnoreCase(name);
    }

    // =====================================================================
    // Filter by Chamber
    // =====================================================================

    /**
     * Find committees by chamber.
     */
    public Page<Committee> findByChamber(CommitteeChamber chamber, Pageable pageable) {
        return committeeRepository.findByChamber(chamber, pageable);
    }

    /**
     * Find committees by chamber (returns list).
     */
    public List<Committee> findByChamber(CommitteeChamber chamber) {
        return committeeRepository.findByChamber(chamber);
    }

    // =====================================================================
    // Filter by Type
    // =====================================================================

    /**
     * Find committees by type.
     */
    public Page<Committee> findByType(CommitteeType type, Pageable pageable) {
        return committeeRepository.findByCommitteeType(type, pageable);
    }

    /**
     * Find committees by type (returns list).
     */
    public List<Committee> findByType(CommitteeType type) {
        return committeeRepository.findByCommitteeType(type);
    }

    // =====================================================================
    // Subcommittees
    // =====================================================================

    /**
     * Find subcommittees for a parent committee.
     */
    public Page<Committee> findSubcommittees(String parentCommitteeCode, Pageable pageable) {
        return committeeRepository.findByParentCommittee_CommitteeCode(parentCommitteeCode, pageable);
    }

    /**
     * Find subcommittees for a parent committee (returns list).
     */
    public List<Committee> findSubcommittees(String parentCommitteeCode) {
        return committeeRepository.findByParentCommittee_CommitteeCode(parentCommitteeCode);
    }

    /**
     * Find all top-level committees (no parent).
     */
    public List<Committee> findParentCommittees() {
        return committeeRepository.findByParentCommitteeIsNull();
    }

    /**
     * Find top-level committees by chamber.
     */
    public List<Committee> findParentCommitteesByChamber(CommitteeChamber chamber) {
        return committeeRepository.findByChamberAndParentCommitteeIsNull(chamber);
    }

    // =====================================================================
    // Membership Lookups
    // =====================================================================

    /**
     * Find members of a committee.
     */
    public Page<CommitteeMembership> findMembers(String committeeCode, Pageable pageable) {
        return membershipRepository.findByCommittee_CommitteeCode(committeeCode, pageable);
    }

    /**
     * Find members of a committee (returns list).
     */
    public List<CommitteeMembership> findMembers(String committeeCode) {
        return membershipRepository.findByCommittee_CommitteeCode(committeeCode);
    }

    /**
     * Find members of a committee in a specific congress.
     */
    public Page<CommitteeMembership> findMembersByCongress(String committeeCode, int congress, Pageable pageable) {
        return membershipRepository.findByCommittee_CommitteeCodeAndCongress(committeeCode, congress, pageable);
    }

    /**
     * Find members of a committee in a specific congress (returns list).
     */
    public List<CommitteeMembership> findMembersByCongress(String committeeCode, int congress) {
        return membershipRepository.findByCommittee_CommitteeCodeAndCongress(committeeCode, congress);
    }

    /**
     * Find committees a member serves on.
     */
    public List<CommitteeMembership> findCommitteesForMember(String bioguideId) {
        return membershipRepository.findByPerson_BioguideId(bioguideId);
    }

    /**
     * Find committees a member serves on with pagination.
     */
    public Page<CommitteeMembership> findCommitteesForMember(String bioguideId, Pageable pageable) {
        return membershipRepository.findByPerson_BioguideId(bioguideId, pageable);
    }

    /**
     * Find committees a member serves on in a specific congress.
     */
    public List<CommitteeMembership> findCommitteesForMemberByCongress(String bioguideId, int congress) {
        return membershipRepository.findByPerson_BioguideIdAndCongress(bioguideId, congress);
    }

    // =====================================================================
    // Statistics
    // =====================================================================

    /**
     * Get total committee count.
     */
    public long count() {
        return committeeRepository.count();
    }

    /**
     * Get count by chamber.
     */
    public long countByChamber(CommitteeChamber chamber) {
        return committeeRepository.countByChamber(chamber);
    }

    /**
     * Get count of subcommittees.
     */
    public long countSubcommittees() {
        return committeeRepository.countSubcommittees();
    }

    /**
     * Get count of parent committees.
     */
    public long countParentCommittees() {
        return committeeRepository.countParentCommittees();
    }

    /**
     * Get type distribution.
     */
    public List<Object[]> getTypeDistribution() {
        return committeeRepository.getTypeDistribution();
    }

    /**
     * Get chamber distribution.
     */
    public List<Object[]> getChamberDistribution() {
        return committeeRepository.getChamberDistribution();
    }

    /**
     * Get member count for a committee in a congress.
     */
    public long countMembers(String committeeCode, int congress) {
        return membershipRepository.countByCommittee_CommitteeCodeAndCongress(committeeCode, congress);
    }
}
