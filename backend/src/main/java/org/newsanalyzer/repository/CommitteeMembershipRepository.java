package org.newsanalyzer.repository;

import org.newsanalyzer.model.CommitteeMembership;
import org.newsanalyzer.model.MembershipRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for CommitteeMembership entity.
 *
 * Provides queries for the person-committee join table.
 *
 * @author James (Dev Agent)
 * @since 2.0.0
 */
@Repository
public interface CommitteeMembershipRepository extends JpaRepository<CommitteeMembership, UUID> {

    // =====================================================================
    // Basic Lookups
    // =====================================================================

    /**
     * Find membership by person, committee, and congress
     */
    Optional<CommitteeMembership> findByPerson_IdAndCommittee_CommitteeCodeAndCongress(
            UUID personId, String committeeCode, Integer congress);

    /**
     * Check if membership exists
     */
    boolean existsByPerson_IdAndCommittee_CommitteeCodeAndCongress(
            UUID personId, String committeeCode, Integer congress);

    // =====================================================================
    // By Person
    // =====================================================================

    /**
     * Find all memberships for a person
     */
    List<CommitteeMembership> findByPerson_Id(UUID personId);

    /**
     * Find all memberships for a person by BioGuide ID
     */
    List<CommitteeMembership> findByPerson_BioguideId(String bioguideId);

    /**
     * Find all memberships for a person by BioGuide ID with pagination
     */
    Page<CommitteeMembership> findByPerson_BioguideId(String bioguideId, Pageable pageable);

    /**
     * Find memberships for a person in a specific congress
     */
    List<CommitteeMembership> findByPerson_BioguideIdAndCongress(String bioguideId, Integer congress);

    /**
     * Count committees a person serves on in a congress
     */
    long countByPerson_BioguideIdAndCongress(String bioguideId, Integer congress);

    // =====================================================================
    // By Committee
    // =====================================================================

    /**
     * Find all memberships for a committee
     */
    List<CommitteeMembership> findByCommittee_CommitteeCode(String committeeCode);

    /**
     * Find all memberships for a committee with pagination
     */
    Page<CommitteeMembership> findByCommittee_CommitteeCode(String committeeCode, Pageable pageable);

    /**
     * Find memberships for a committee in a specific congress
     */
    List<CommitteeMembership> findByCommittee_CommitteeCodeAndCongress(String committeeCode, Integer congress);

    /**
     * Find memberships for a committee in a specific congress with pagination
     */
    Page<CommitteeMembership> findByCommittee_CommitteeCodeAndCongress(
            String committeeCode, Integer congress, Pageable pageable);

    /**
     * Count members on a committee in a congress
     */
    long countByCommittee_CommitteeCodeAndCongress(String committeeCode, Integer congress);

    // =====================================================================
    // By Role
    // =====================================================================

    /**
     * Find committee chairs
     */
    List<CommitteeMembership> findByRole(MembershipRole role);

    /**
     * Find committee chairs for a specific committee
     */
    Optional<CommitteeMembership> findByCommittee_CommitteeCodeAndRoleAndCongress(
            String committeeCode, MembershipRole role, Integer congress);

    /**
     * Find all leadership roles for a person
     */
    @Query("SELECT m FROM CommitteeMembership m WHERE m.person.bioguideId = :bioguideId " +
           "AND m.role IN (org.newsanalyzer.model.MembershipRole.CHAIR, " +
           "org.newsanalyzer.model.MembershipRole.VICE_CHAIR, " +
           "org.newsanalyzer.model.MembershipRole.RANKING_MEMBER)")
    List<CommitteeMembership> findLeadershipRolesByBioguideId(@Param("bioguideId") String bioguideId);

    // =====================================================================
    // By Congress
    // =====================================================================

    /**
     * Find all memberships for a congress
     */
    List<CommitteeMembership> findByCongress(Integer congress);

    /**
     * Find all memberships for a congress with pagination
     */
    Page<CommitteeMembership> findByCongress(Integer congress, Pageable pageable);

    /**
     * Count total memberships for a congress
     */
    long countByCongress(Integer congress);

    // =====================================================================
    // Statistics
    // =====================================================================

    /**
     * Get role distribution for a committee in a congress
     */
    @Query("SELECT m.role, COUNT(m) FROM CommitteeMembership m " +
           "WHERE m.committee.committeeCode = :committeeCode AND m.congress = :congress " +
           "GROUP BY m.role ORDER BY m.role")
    List<Object[]> getRoleDistributionForCommittee(
            @Param("committeeCode") String committeeCode,
            @Param("congress") Integer congress);

    /**
     * Get committees a person chairs in a congress
     */
    @Query("SELECT m FROM CommitteeMembership m WHERE m.person.bioguideId = :bioguideId " +
           "AND m.congress = :congress AND m.role = org.newsanalyzer.model.MembershipRole.CHAIR")
    List<CommitteeMembership> findChairPositionsByBioguideIdAndCongress(
            @Param("bioguideId") String bioguideId,
            @Param("congress") Integer congress);

    /**
     * Get distinct congresses in the system
     */
    @Query("SELECT DISTINCT m.congress FROM CommitteeMembership m ORDER BY m.congress DESC")
    List<Integer> findDistinctCongresses();
}
