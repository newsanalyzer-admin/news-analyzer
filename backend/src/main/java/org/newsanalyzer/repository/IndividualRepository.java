package org.newsanalyzer.repository;

import org.newsanalyzer.model.DataSource;
import org.newsanalyzer.model.Individual;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for Individual entity operations.
 *
 * Provides standard CRUD operations plus custom queries for:
 * - Name-based lookups (case-sensitive and case-insensitive)
 * - Birth date matching for deduplication
 * - Data source filtering
 *
 * Part of ARCH-1 refactor.
 *
 * @author Claude (Dev Agent)
 * @since 3.0.0
 */
@Repository
public interface IndividualRepository extends JpaRepository<Individual, UUID> {

    // =====================================================================
    // Name-based Lookups
    // =====================================================================

    /**
     * Find individual by exact first and last name (case-sensitive).
     */
    Optional<Individual> findByFirstNameAndLastName(String firstName, String lastName);

    /**
     * Find individuals by first and last name (case-insensitive).
     */
    List<Individual> findByFirstNameIgnoreCaseAndLastNameIgnoreCase(String firstName, String lastName);

    /**
     * Find individual by exact first name, last name, and birth date.
     * Primary method for deduplication.
     */
    Optional<Individual> findByFirstNameAndLastNameAndBirthDate(
            String firstName, String lastName, LocalDate birthDate);

    /**
     * Find individual by first name, last name, and birth date (case-insensitive names).
     * Used for robust deduplication matching.
     */
    Optional<Individual> findByFirstNameIgnoreCaseAndLastNameIgnoreCaseAndBirthDate(
            String firstName, String lastName, LocalDate birthDate);

    // =====================================================================
    // Name Search
    // =====================================================================

    /**
     * Find individuals by last name containing (case-insensitive).
     */
    List<Individual> findByLastNameContainingIgnoreCase(String lastName);

    /**
     * Search by full name pattern.
     */
    @Query("SELECT i FROM Individual i WHERE " +
           "LOWER(CONCAT(i.firstName, ' ', i.lastName)) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Individual> searchByName(@Param("name") String name);

    // =====================================================================
    // Data Source Filtering
    // =====================================================================

    /**
     * Find all individuals from a specific data source.
     */
    List<Individual> findByPrimaryDataSource(DataSource dataSource);

    /**
     * Count individuals by data source.
     */
    long countByPrimaryDataSource(DataSource dataSource);

    // =====================================================================
    // Living/Deceased Filtering
    // =====================================================================

    /**
     * Find all living individuals (death_date is null).
     */
    List<Individual> findByDeathDateIsNull();

    /**
     * Find all deceased individuals (death_date is not null).
     */
    List<Individual> findByDeathDateIsNotNull();

    // =====================================================================
    // External ID Queries
    // =====================================================================

    /**
     * Find individual by bioguide ID stored in external_ids JSONB.
     */
    @Query(value = "SELECT * FROM individuals WHERE external_ids->>'bioguideId' = :bioguideId",
           nativeQuery = true)
    Optional<Individual> findByBioguideId(@Param("bioguideId") String bioguideId);

    /**
     * Find individual by any external ID key-value pair.
     */
    @Query(value = "SELECT * FROM individuals WHERE external_ids->>:key = :value",
           nativeQuery = true)
    Optional<Individual> findByExternalId(@Param("key") String key, @Param("value") String value);

    // =====================================================================
    // Existence Checks
    // =====================================================================

    /**
     * Check if an individual exists with the given name and birth date.
     */
    boolean existsByFirstNameIgnoreCaseAndLastNameIgnoreCaseAndBirthDate(
            String firstName, String lastName, LocalDate birthDate);
}
