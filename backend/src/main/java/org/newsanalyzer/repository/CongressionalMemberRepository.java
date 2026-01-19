package org.newsanalyzer.repository;

import org.newsanalyzer.model.CongressionalMember;
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
 * Repository for CongressionalMember entities.
 * Provides CRUD operations and custom queries for congressional member data.
 *
 * Part of ARCH-1.2: Create CongressionalMember Entity
 *
 * @author Claude (Dev Agent)
 * @since 3.0.0
 */
@Repository
public interface CongressionalMemberRepository extends JpaRepository<CongressionalMember, UUID> {

    // =====================================================================
    // Lookup by identifiers
    // =====================================================================

    /**
     * Find congressional member by BioGuide ID.
     * @param bioguideId the Congress.gov BioGuide identifier
     * @return the member if found
     */
    Optional<CongressionalMember> findByBioguideId(String bioguideId);

    /**
     * Find congressional member by linked Individual ID.
     * @param individualId the UUID of the linked Individual
     * @return the member if found
     */
    Optional<CongressionalMember> findByIndividualId(UUID individualId);

    /**
     * Check if a congressional member exists with the given BioGuide ID.
     * @param bioguideId the Congress.gov BioGuide identifier
     * @return true if exists
     */
    boolean existsByBioguideId(String bioguideId);

    /**
     * Check if a congressional member exists for the given Individual.
     * @param individualId the UUID of the Individual
     * @return true if exists
     */
    boolean existsByIndividualId(UUID individualId);

    // =====================================================================
    // Queries by congressional attributes
    // =====================================================================

    /**
     * Find all members by state.
     * @param state the 2-letter state code
     * @return list of members representing that state
     */
    List<CongressionalMember> findByState(String state);

    /**
     * Find all members by chamber.
     * @param chamber SENATE or HOUSE
     * @return list of members in that chamber
     */
    List<CongressionalMember> findByChamber(CongressionalMember.Chamber chamber);

    /**
     * Find all members by party.
     * Note: Party is now on Individual, not CongressionalMember.
     * This query joins to Individual to filter by party.
     * @param party the party name (e.g., "Democratic", "Republican")
     * @return list of members in that party
     */
    @Query("SELECT cm FROM CongressionalMember cm " +
           "LEFT JOIN cm.individual i " +
           "WHERE i.party = :party")
    List<CongressionalMember> findByParty(@Param("party") String party);

    /**
     * Find all members by party with pagination.
     * @param party the party name
     * @param pageable pagination parameters
     * @return page of members
     */
    @Query("SELECT cm FROM CongressionalMember cm " +
           "LEFT JOIN cm.individual i " +
           "WHERE i.party = :party")
    Page<CongressionalMember> findByParty(@Param("party") String party, Pageable pageable);

    /**
     * Find all members by state and chamber.
     * @param state the 2-letter state code
     * @param chamber SENATE or HOUSE
     * @return list of members matching both criteria
     */
    List<CongressionalMember> findByStateAndChamber(String state, CongressionalMember.Chamber chamber);

    // =====================================================================
    // Paginated queries
    // =====================================================================

    /**
     * Find all members by chamber with pagination.
     * @param chamber SENATE or HOUSE
     * @param pageable pagination parameters
     * @return page of members
     */
    Page<CongressionalMember> findByChamber(CongressionalMember.Chamber chamber, Pageable pageable);

    /**
     * Find all members by state with pagination.
     * @param state the 2-letter state code
     * @param pageable pagination parameters
     * @return page of members
     */
    Page<CongressionalMember> findByState(String state, Pageable pageable);

    // =====================================================================
    // Eager-loading queries (with Individual)
    // =====================================================================

    /**
     * Find member by BioGuide ID with Individual eagerly loaded.
     * @param bioguideId the Congress.gov BioGuide identifier
     * @return the member with individual data if found
     */
    @Query("SELECT cm FROM CongressionalMember cm " +
           "LEFT JOIN FETCH cm.individual " +
           "WHERE cm.bioguideId = :bioguideId")
    Optional<CongressionalMember> findByBioguideIdWithIndividual(@Param("bioguideId") String bioguideId);

    /**
     * Find all members by chamber with Individual eagerly loaded.
     * @param chamber SENATE or HOUSE
     * @return list of members with individual data
     */
    @Query("SELECT cm FROM CongressionalMember cm " +
           "LEFT JOIN FETCH cm.individual " +
           "WHERE cm.chamber = :chamber")
    List<CongressionalMember> findByChamberWithIndividual(@Param("chamber") CongressionalMember.Chamber chamber);

    /**
     * Find all members by state with Individual eagerly loaded.
     * @param state the 2-letter state code
     * @return list of members with individual data
     */
    @Query("SELECT cm FROM CongressionalMember cm " +
           "LEFT JOIN FETCH cm.individual " +
           "WHERE cm.state = :state")
    List<CongressionalMember> findByStateWithIndividual(@Param("state") String state);

    /**
     * Find all members with Individual eagerly loaded.
     * @return list of all members with individual data
     */
    @Query("SELECT cm FROM CongressionalMember cm " +
           "LEFT JOIN FETCH cm.individual")
    List<CongressionalMember> findAllWithIndividual();

    /**
     * Find member by ID with Individual eagerly loaded.
     * @param id the member UUID
     * @return the member with individual data if found
     */
    @Query("SELECT cm FROM CongressionalMember cm " +
           "LEFT JOIN FETCH cm.individual " +
           "WHERE cm.id = :id")
    Optional<CongressionalMember> findByIdWithIndividual(@Param("id") UUID id);

    // =====================================================================
    // Count queries
    // =====================================================================

    /**
     * Count members by chamber.
     * @param chamber SENATE or HOUSE
     * @return count of members
     */
    long countByChamber(CongressionalMember.Chamber chamber);

    /**
     * Count members by party.
     * @param party the party name
     * @return count of members
     */
    long countByParty(String party);

    /**
     * Count members by state.
     * @param state the 2-letter state code
     * @return count of members
     */
    long countByState(String state);

    // =====================================================================
    // Statistics queries
    // =====================================================================

    /**
     * Get party distribution (count by party).
     * @return list of Object[] where [0]=party, [1]=count
     */
    @Query("SELECT i.party, COUNT(cm) FROM CongressionalMember cm " +
           "LEFT JOIN cm.individual i " +
           "GROUP BY i.party ORDER BY COUNT(cm) DESC")
    List<Object[]> getPartyDistribution();

    /**
     * Get state distribution (count by state).
     * @return list of Object[] where [0]=state, [1]=count
     */
    @Query("SELECT cm.state, COUNT(cm) FROM CongressionalMember cm " +
           "GROUP BY cm.state ORDER BY COUNT(cm) DESC")
    List<Object[]> getStateDistribution();

    // =====================================================================
    // External ID lookups
    // =====================================================================

    /**
     * Find member by FEC ID.
     */
    @Query("SELECT cm FROM CongressionalMember cm " +
           "LEFT JOIN cm.individual i " +
           "WHERE jsonb_extract_path_text(i.externalIds, 'fec') = :fecId")
    Optional<CongressionalMember> findByFecId(@Param("fecId") String fecId);

    /**
     * Find member by GovTrack ID.
     */
    @Query("SELECT cm FROM CongressionalMember cm " +
           "LEFT JOIN cm.individual i " +
           "WHERE CAST(jsonb_extract_path_text(i.externalIds, 'govtrack') AS integer) = :govtrackId")
    Optional<CongressionalMember> findByGovtrackId(@Param("govtrackId") Integer govtrackId);

    /**
     * Find member by OpenSecrets ID.
     */
    @Query("SELECT cm FROM CongressionalMember cm " +
           "LEFT JOIN cm.individual i " +
           "WHERE jsonb_extract_path_text(i.externalIds, 'opensecrets') = :opensecretsId")
    Optional<CongressionalMember> findByOpensecretsId(@Param("opensecretsId") String opensecretsId);

    /**
     * Find member by VoteSmart ID.
     */
    @Query("SELECT cm FROM CongressionalMember cm " +
           "LEFT JOIN cm.individual i " +
           "WHERE CAST(jsonb_extract_path_text(i.externalIds, 'votesmart') AS integer) = :votesmartId")
    Optional<CongressionalMember> findByVotesmartId(@Param("votesmartId") Integer votesmartId);
}
