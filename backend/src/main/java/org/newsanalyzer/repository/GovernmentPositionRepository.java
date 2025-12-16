package org.newsanalyzer.repository;

import org.newsanalyzer.model.AppointmentType;
import org.newsanalyzer.model.Branch;
import org.newsanalyzer.model.GovernmentPosition;
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
 * Repository for GovernmentPosition entities.
 *
 * @author James (Dev Agent)
 * @since 2.0.0
 */
@Repository
public interface GovernmentPositionRepository extends JpaRepository<GovernmentPosition, UUID> {

    // =====================================================================
    // Find by attributes
    // =====================================================================

    List<GovernmentPosition> findByChamber(Chamber chamber);

    Page<GovernmentPosition> findByChamber(Chamber chamber, Pageable pageable);

    List<GovernmentPosition> findByState(String state);

    Page<GovernmentPosition> findByState(String state, Pageable pageable);

    List<GovernmentPosition> findByChamberAndState(Chamber chamber, String state);

    // =====================================================================
    // Find specific seats
    // =====================================================================

    /**
     * Find a Senate seat by state and class
     */
    Optional<GovernmentPosition> findByChamberAndStateAndSenateClass(
            Chamber chamber, String state, Integer senateClass);

    /**
     * Find a House seat by state and district
     */
    Optional<GovernmentPosition> findByChamberAndStateAndDistrict(
            Chamber chamber, String state, Integer district);

    // =====================================================================
    // Counting queries
    // =====================================================================

    long countByChamber(Chamber chamber);

    long countByState(String state);

    // =====================================================================
    // Organization-based queries
    // =====================================================================

    List<GovernmentPosition> findByOrganizationId(UUID organizationId);

    // =====================================================================
    // Custom queries
    // =====================================================================

    /**
     * Find all Senate positions
     */
    @Query("SELECT p FROM GovernmentPosition p WHERE p.chamber = 'SENATE' ORDER BY p.state, p.senateClass")
    List<GovernmentPosition> findAllSenatePositions();

    /**
     * Find all House positions
     */
    @Query("SELECT p FROM GovernmentPosition p WHERE p.chamber = 'HOUSE' ORDER BY p.state, p.district")
    List<GovernmentPosition> findAllHousePositions();

    /**
     * Check if a position exists by chamber, state, and district/class
     */
    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM GovernmentPosition p " +
            "WHERE p.chamber = :chamber AND p.state = :state " +
            "AND ((:district IS NULL AND p.senateClass = :senateClass) " +
            "     OR (:senateClass IS NULL AND p.district = :district))")
    boolean existsBySeat(@Param("chamber") Chamber chamber,
                         @Param("state") String state,
                         @Param("district") Integer district,
                         @Param("senateClass") Integer senateClass);

    // =====================================================================
    // Branch-based queries (FB-2.2)
    // =====================================================================

    /**
     * Find positions by branch
     */
    List<GovernmentPosition> findByBranch(Branch branch);

    Page<GovernmentPosition> findByBranch(Branch branch, Pageable pageable);

    long countByBranch(Branch branch);

    // =====================================================================
    // Executive branch queries (FB-2.2)
    // =====================================================================

    /**
     * Find positions by appointment type
     */
    List<GovernmentPosition> findByAppointmentType(AppointmentType appointmentType);

    Page<GovernmentPosition> findByAppointmentType(AppointmentType appointmentType, Pageable pageable);

    /**
     * Find positions by pay plan
     */
    List<GovernmentPosition> findByPayPlan(String payPlan);

    /**
     * Find executive positions by organization
     */
    @Query("SELECT p FROM GovernmentPosition p WHERE p.branch = :branch AND p.organizationId = :orgId")
    List<GovernmentPosition> findByBranchAndOrganizationId(
            @Param("branch") Branch branch,
            @Param("orgId") UUID organizationId);

    /**
     * Find all executive positions
     */
    @Query("SELECT p FROM GovernmentPosition p WHERE p.branch = 'EXECUTIVE' ORDER BY p.title")
    List<GovernmentPosition> findAllExecutivePositions();

    /**
     * Find all executive positions (paginated)
     */
    @Query("SELECT p FROM GovernmentPosition p WHERE p.branch = 'EXECUTIVE'")
    Page<GovernmentPosition> findAllExecutivePositions(Pageable pageable);

    /**
     * Find executive position by title and organization (for upsert logic)
     */
    Optional<GovernmentPosition> findByBranchAndTitleAndOrganizationId(
            Branch branch, String title, UUID organizationId);

    /**
     * Find position by title and organization (for judicial position lookup)
     */
    Optional<GovernmentPosition> findByTitleAndOrganizationId(String title, UUID organizationId);

    // =====================================================================
    // Appointee Lookup queries (FB-2.4)
    // =====================================================================

    /**
     * Search executive positions by title (case-insensitive)
     */
    @Query("SELECT p FROM GovernmentPosition p WHERE p.branch = 'EXECUTIVE' " +
           "AND LOWER(p.title) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<GovernmentPosition> searchExecutivePositionsByTitle(@Param("query") String query);

    /**
     * Find Cabinet-level positions by title keywords
     */
    @Query("SELECT p FROM GovernmentPosition p WHERE p.branch = 'EXECUTIVE' " +
           "AND (LOWER(p.title) LIKE '%secretary%' " +
           "OR LOWER(p.title) LIKE '%attorney general%' " +
           "OR LOWER(p.title) LIKE '%administrator%' " +
           "OR LOWER(p.title) LIKE '%director%') " +
           "AND p.appointmentType = 'PAS' " +
           "ORDER BY p.title")
    List<GovernmentPosition> findCabinetLevelPositions();

    /**
     * Find executive positions by appointment type and organization
     */
    @Query("SELECT p FROM GovernmentPosition p WHERE p.branch = 'EXECUTIVE' " +
           "AND p.appointmentType = :appointmentType AND p.organizationId = :orgId")
    List<GovernmentPosition> findByAppointmentTypeAndOrganizationId(
            @Param("appointmentType") AppointmentType appointmentType,
            @Param("orgId") UUID organizationId);
}
