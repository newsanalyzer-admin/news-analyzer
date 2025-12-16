package org.newsanalyzer.apitests.data;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.newsanalyzer.apitests.util.DatabaseConnectionManager;
import org.newsanalyzer.apitests.util.FlywayMigrationExtension;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Base class for database integration tests.
 * Provides automatic database setup, migrations, and cleanup.
 *
 * <p>Usage:</p>
 * <pre>
 * {@code
 * public class MyDatabaseTest extends DatabaseIntegrationTest {
 *
 *     @Test
 *     void testEntityCreation() throws SQLException {
 *         // Database is ready with migrations applied
 *         // Test data is cleaned and reseeded before each test
 *
 *         UUID id = EntityTestDataBuilder.aPerson()
 *             .withName("Test Person")
 *             .buildAndPersist();
 *
 *         DatabaseAssertions.assertEntityExists(id);
 *     }
 * }
 * }
 * </pre>
 *
 * <p>Features:</p>
 * <ul>
 *   <li>Flyway migrations run once before all tests</li>
 *   <li>Database cleaned and reseeded before each test method</li>
 *   <li>Connection pool properly initialized</li>
 *   <li>Transaction support via getConnection()</li>
 * </ul>
 */
public abstract class DatabaseIntegrationTest {

    /**
     * Flyway migration extension - runs migrations once per test run.
     * Set verbose(true) for debugging migration issues.
     */
    @RegisterExtension
    static FlywayMigrationExtension flyway = new FlywayMigrationExtension();

    /**
     * Database cleanup extension - cleans and reseeds before each test.
     */
    @RegisterExtension
    DatabaseCleanupExtension cleanup = new DatabaseCleanupExtension();

    /**
     * Connection manager singleton for direct database access.
     */
    protected static DatabaseConnectionManager connectionManager;

    /**
     * Test data seeder for manual seeding control.
     */
    protected static TestDataSeeder seeder;

    /**
     * Test data cleaner for manual cleanup control.
     */
    protected static TestDataCleaner cleaner;

    @BeforeAll
    static void initializeDatabaseComponents() {
        connectionManager = DatabaseConnectionManager.getInstance();
        seeder = new TestDataSeeder(connectionManager);
        cleaner = new TestDataCleaner(connectionManager);
    }

    /**
     * Get a database connection from the pool.
     * Caller is responsible for closing the connection.
     */
    protected Connection getConnection() throws SQLException {
        return connectionManager.getConnection();
    }

    /**
     * Execute code within a transaction.
     * Automatically commits on success, rolls back on failure.
     */
    protected <T> T inTransaction(DatabaseConnectionManager.TransactionCallback<T> callback) throws SQLException {
        return connectionManager.executeInTransaction(callback);
    }

    /**
     * Execute code within a transaction (void return).
     */
    protected void inTransaction(DatabaseConnectionManager.TransactionVoidCallback callback) throws SQLException {
        connectionManager.executeInTransaction(callback);
    }

    /**
     * Clean all test data tables.
     */
    protected void cleanDatabase() throws SQLException {
        cleaner.cleanAllTables();
    }

    /**
     * Reseed the database with standard test data.
     */
    protected void reseedDatabase() throws SQLException {
        seeder.seedFullTestDataset();
    }

    /**
     * Clean and reseed in a single transaction.
     */
    protected void resetDatabase() throws SQLException {
        cleaner.cleanAndReseed();
    }

    // ==================== Test Data Constants ====================

    /**
     * Known entity IDs from seed data (entities.sql).
     */
    public static class SeedEntities {
        // PERSON entities
        public static final String ELIZABETH_WARREN_ID = "11111111-1111-1111-1111-111111111111";
        public static final String MERRICK_GARLAND_ID = "22222222-1111-1111-1111-111111111111";
        public static final String JANET_YELLEN_ID = "33333333-1111-1111-1111-111111111111";
        public static final String LLOYD_AUSTIN_ID = "44444444-1111-1111-1111-111111111111";
        public static final String ANTONY_BLINKEN_ID = "55555555-1111-1111-1111-111111111111";

        // GOVERNMENT_ORG entities
        public static final String EPA_ENTITY_ID = "11111111-2222-2222-2222-222222222222";
        public static final String FBI_ENTITY_ID = "22222222-2222-2222-2222-222222222222";
        public static final String NASA_ENTITY_ID = "33333333-2222-2222-2222-222222222222";
        public static final String DOJ_ENTITY_ID = "44444444-2222-2222-2222-222222222222";
        public static final String CIA_ENTITY_ID = "55555555-2222-2222-2222-222222222222";

        // ORGANIZATION entities
        public static final String GOOGLE_ID = "11111111-3333-3333-3333-333333333333";
        public static final String ACLU_ID = "22222222-3333-3333-3333-333333333333";
        public static final String EXXON_ID = "33333333-3333-3333-3333-333333333333";

        // LOCATION entities
        public static final String WASHINGTON_DC_ID = "11111111-4444-4444-4444-444444444444";
        public static final String CALIFORNIA_ID = "22222222-4444-4444-4444-444444444444";
        public static final String NYC_ID = "33333333-4444-4444-4444-444444444444";

        // Counts by type
        public static final int PERSON_COUNT = 5;
        public static final int GOVERNMENT_ORG_COUNT = 5;
        public static final int ORGANIZATION_COUNT = 3;
        public static final int LOCATION_COUNT = 3;
        public static final int TOTAL_ENTITY_COUNT = 16;
    }

    /**
     * Known government organization IDs from seed data (government_organizations.sql).
     */
    public static class SeedGovOrgs {
        // Cabinet Departments
        public static final String DOJ_ID = "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa";
        public static final String DOD_ID = "bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb";
        public static final String STATE_ID = "cccccccc-cccc-cccc-cccc-cccccccccccc";

        // Independent Agencies
        public static final String EPA_ID = "dddddddd-dddd-dddd-dddd-dddddddddddd";
        public static final String NASA_ID = "eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee";
        public static final String CIA_ID = "ffffffff-ffff-ffff-ffff-ffffffffffff";

        // Bureaus (with parent relationships)
        public static final String FBI_ID = "11111111-aaaa-aaaa-aaaa-aaaaaaaaaaaa";
        public static final String ATF_ID = "22222222-aaaa-aaaa-aaaa-aaaaaaaaaaaa";

        // Counts
        public static final int DEPARTMENT_COUNT = 3;
        public static final int AGENCY_COUNT = 3;
        public static final int BUREAU_COUNT = 2;
        public static final int TOTAL_GOV_ORG_COUNT = 8;
    }

    /**
     * Known person (Congressional member) IDs and BioGuide IDs from seed data (persons.sql).
     */
    public static class SeedPersons {
        // Senate members
        public static final String SANDERS_ID = "aaaaaaaa-1111-1111-1111-111111111111";
        public static final String SANDERS_BIOGUIDE = "S000033";
        public static final String MCCONNELL_ID = "bbbbbbbb-1111-1111-1111-111111111111";
        public static final String MCCONNELL_BIOGUIDE = "M000355";
        public static final String WARREN_ID = "cccccccc-1111-1111-1111-111111111111";
        public static final String WARREN_BIOGUIDE = "W000817";
        public static final String CRUZ_ID = "dddddddd-1111-1111-1111-111111111111";
        public static final String CRUZ_BIOGUIDE = "C001098";

        // House members
        public static final String PELOSI_ID = "eeeeeeee-1111-1111-1111-111111111111";
        public static final String PELOSI_BIOGUIDE = "P000197";
        public static final String AOC_ID = "ffffffff-1111-1111-1111-111111111111";
        public static final String AOC_BIOGUIDE = "O000172";
        public static final String JORDAN_ID = "11111111-2222-1111-1111-111111111111";
        public static final String JORDAN_BIOGUIDE = "J000289";
        public static final String GREEN_ID = "22222222-2222-1111-1111-111111111111";
        public static final String GREEN_BIOGUIDE = "G000553";

        // Counts
        public static final int SENATE_COUNT = 4;
        public static final int HOUSE_COUNT = 4;
        public static final int TOTAL_PERSON_COUNT = 8;

        // Party counts
        public static final int DEMOCRATIC_COUNT = 4;
        public static final int REPUBLICAN_COUNT = 3;
        public static final int INDEPENDENT_COUNT = 1;

        // States represented
        public static final String[] STATES = {"VT", "KY", "MA", "TX", "CA", "NY", "OH"};
    }
}
