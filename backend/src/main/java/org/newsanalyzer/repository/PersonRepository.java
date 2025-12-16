package org.newsanalyzer.repository;

import org.newsanalyzer.model.DataSource;
import org.newsanalyzer.model.Person;
import org.newsanalyzer.model.Person.Chamber;
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
 * Repository for Person entity.
 *
 * Provides CRUD operations and custom queries for Congressional member data.
 *
 * @author James (Dev Agent)
 * @since 2.0.0
 */
@Repository
public interface PersonRepository extends JpaRepository<Person, UUID> {

    // =====================================================================
    // Basic Lookups
    // =====================================================================

    /**
     * Find person by BioGuide ID
     */
    Optional<Person> findByBioguideId(String bioguideId);

    /**
     * Check if person exists by BioGuide ID
     */
    boolean existsByBioguideId(String bioguideId);

    // =====================================================================
    // By State and Chamber
    // =====================================================================

    /**
     * Find all members by state
     */
    List<Person> findByState(String state);

    /**
     * Find all members by state with pagination
     */
    Page<Person> findByState(String state, Pageable pageable);

    /**
     * Find all members by chamber
     */
    List<Person> findByChamber(Chamber chamber);

    /**
     * Find all members by chamber with pagination
     */
    Page<Person> findByChamber(Chamber chamber, Pageable pageable);

    /**
     * Find members by state and chamber
     */
    List<Person> findByStateAndChamber(String state, Chamber chamber);

    // =====================================================================
    // By Party
    // =====================================================================

    /**
     * Find all members by party
     */
    List<Person> findByParty(String party);

    /**
     * Find members by party with pagination
     */
    Page<Person> findByParty(String party, Pageable pageable);

    // =====================================================================
    // Name Search
    // =====================================================================

    /**
     * Search by last name (case-insensitive)
     */
    List<Person> findByLastNameIgnoreCase(String lastName);

    /**
     * Search by last name containing (case-insensitive)
     */
    List<Person> findByLastNameContainingIgnoreCase(String lastName);

    /**
     * Search by first name or last name containing (case-insensitive)
     */
    @Query("SELECT p FROM Person p " +
           "WHERE LOWER(p.firstName) LIKE LOWER(CONCAT('%', :name, '%')) " +
           "OR LOWER(p.lastName) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Person> searchByName(@Param("name") String name);

    /**
     * Search by first name or last name containing with pagination
     */
    @Query("SELECT p FROM Person p " +
           "WHERE LOWER(p.firstName) LIKE LOWER(CONCAT('%', :name, '%')) " +
           "OR LOWER(p.lastName) LIKE LOWER(CONCAT('%', :name, '%'))")
    Page<Person> searchByName(@Param("name") String name, Pageable pageable);

    /**
     * Full-text search using PostgreSQL ts_vector
     */
    @Query(value = "SELECT * FROM persons p " +
           "WHERE to_tsvector('english', coalesce(p.first_name, '') || ' ' || coalesce(p.last_name, '')) " +
           "@@ plainto_tsquery('english', :query)",
           nativeQuery = true)
    List<Person> fullTextSearchByName(@Param("query") String query);

    // =====================================================================
    // Cross-Reference Queries
    // =====================================================================

    /**
     * Find by external ID (JSONB containment query)
     * Example: findByExternalIdContains("{\"govtrack\": 400357}")
     */
    @Query(value = "SELECT * FROM persons p WHERE p.external_ids @> CAST(:json AS jsonb)",
           nativeQuery = true)
    Optional<Person> findByExternalIdContains(@Param("json") String json);

    /**
     * Find by FEC ID (checks array field)
     * Uses jsonb_exists function instead of ? operator to avoid Spring Data JPA parameter conflict
     */
    @Query(value = "SELECT * FROM persons p WHERE jsonb_exists(p.external_ids -> 'fec', :fecId)",
           nativeQuery = true)
    Optional<Person> findByFecId(@Param("fecId") String fecId);

    /**
     * Find by GovTrack ID
     */
    @Query(value = "SELECT * FROM persons p WHERE (p.external_ids ->> 'govtrack')::integer = :govtrackId",
           nativeQuery = true)
    Optional<Person> findByGovtrackId(@Param("govtrackId") Integer govtrackId);

    /**
     * Find by OpenSecrets ID
     */
    @Query(value = "SELECT * FROM persons p WHERE p.external_ids ->> 'opensecrets' = :opensecretsId",
           nativeQuery = true)
    Optional<Person> findByOpensecretsId(@Param("opensecretsId") String opensecretsId);

    /**
     * Find by VoteSmart ID
     */
    @Query(value = "SELECT * FROM persons p WHERE (p.external_ids ->> 'votesmart')::integer = :votesmartId",
           nativeQuery = true)
    Optional<Person> findByVotesmartId(@Param("votesmartId") Integer votesmartId);

    // =====================================================================
    // Statistics
    // =====================================================================

    /**
     * Count members by chamber
     */
    long countByChamber(Chamber chamber);

    /**
     * Count members by state
     */
    long countByState(String state);

    /**
     * Count members by party
     */
    long countByParty(String party);

    /**
     * Get party distribution
     */
    @Query("SELECT p.party, COUNT(p) FROM Person p GROUP BY p.party ORDER BY COUNT(p) DESC")
    List<Object[]> getPartyDistribution();

    /**
     * Get state distribution
     */
    @Query("SELECT p.state, COUNT(p) FROM Person p GROUP BY p.state ORDER BY p.state")
    List<Object[]> getStateDistribution();

    // =====================================================================
    // PLUM Import Queries (FB-2.1)
    // =====================================================================

    /**
     * Find person by exact first and last name (case-insensitive)
     */
    @Query("SELECT p FROM Person p WHERE LOWER(p.firstName) = LOWER(:firstName) " +
           "AND LOWER(p.lastName) = LOWER(:lastName)")
    List<Person> findByFirstNameAndLastNameIgnoreCase(
            @Param("firstName") String firstName,
            @Param("lastName") String lastName);

    /**
     * Find person by exact first and last name (returns Optional for single match)
     */
    Optional<Person> findByFirstNameAndLastName(String firstName, String lastName);

    /**
     * Find person by first name, last name, and data source
     */
    @Query("SELECT p FROM Person p WHERE LOWER(p.firstName) = LOWER(:firstName) " +
           "AND LOWER(p.lastName) = LOWER(:lastName) AND p.dataSource = :dataSource")
    Optional<Person> findByFirstNameAndLastNameAndDataSource(
            @Param("firstName") String firstName,
            @Param("lastName") String lastName,
            @Param("dataSource") DataSource dataSource);

    /**
     * Find all persons by data source
     */
    List<Person> findByDataSource(DataSource dataSource);

    /**
     * Find all persons by data source (paginated)
     */
    Page<Person> findByDataSource(DataSource dataSource, Pageable pageable);

    /**
     * Count persons by data source
     */
    long countByDataSource(DataSource dataSource);
}
