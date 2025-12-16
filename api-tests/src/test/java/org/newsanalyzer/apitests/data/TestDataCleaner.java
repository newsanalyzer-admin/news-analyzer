package org.newsanalyzer.apitests.data;

import org.newsanalyzer.apitests.util.DatabaseConnectionManager;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Test data cleaner for database integration tests.
 * Provides methods to clean tables and reset sequences for test isolation.
 */
public class TestDataCleaner {

    private static final String CLEANUP_SQL = "seed/cleanup.sql";

    private final DatabaseConnectionManager connectionManager;

    public TestDataCleaner() {
        this.connectionManager = DatabaseConnectionManager.getInstance();
    }

    public TestDataCleaner(DatabaseConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    /**
     * Clean all test data tables (truncate with CASCADE).
     * Order: entities first (FK to gov_orgs), then government_organizations.
     */
    public void cleanAllTables() throws SQLException {
        connectionManager.executeInTransaction(connection -> {
            cleanAllTables(connection);
        });
        System.out.println("Cleaned all test data tables");
    }

    /**
     * Clean all tables using provided connection.
     */
    public void cleanAllTables(Connection connection) throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            // Disable FK checks for faster cleanup
            stmt.execute("SET session_replication_role = 'replica'");

            // Truncate in order
            stmt.execute("TRUNCATE TABLE entities CASCADE");
            stmt.execute("TRUNCATE TABLE government_organizations CASCADE");

            // Re-enable FK checks
            stmt.execute("SET session_replication_role = 'origin'");
        }
    }

    /**
     * Clean only the entities table.
     */
    public void cleanEntities() throws SQLException {
        connectionManager.executeInTransaction(connection -> {
            cleanEntities(connection);
        });
        System.out.println("Cleaned entities table");
    }

    /**
     * Clean entities using provided connection.
     */
    public void cleanEntities(Connection connection) throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("TRUNCATE TABLE entities CASCADE");
        }
    }

    /**
     * Clean only the government_organizations table.
     * Note: This will also cascade delete entities that reference gov_orgs.
     */
    public void cleanGovernmentOrganizations() throws SQLException {
        connectionManager.executeInTransaction(connection -> {
            cleanGovernmentOrganizations(connection);
        });
        System.out.println("Cleaned government_organizations table");
    }

    /**
     * Clean government organizations using provided connection.
     */
    public void cleanGovernmentOrganizations(Connection connection) throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            // Must clean entities first due to FK
            stmt.execute("TRUNCATE TABLE entities CASCADE");
            stmt.execute("TRUNCATE TABLE government_organizations CASCADE");
        }
    }

    /**
     * Reset ID sequences to starting values.
     * Useful when tests depend on specific ID values.
     */
    public void resetSequences() throws SQLException {
        connectionManager.executeInTransaction(connection -> {
            resetSequences(connection);
        });
        System.out.println("Reset sequences");
    }

    /**
     * Reset sequences using provided connection.
     */
    public void resetSequences(Connection connection) throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            // Reset any sequences if they exist
            // Note: UUID primary keys don't use sequences, but include for completeness
            stmt.execute("SELECT setval(seq.relname::regclass, 1, false) FROM pg_class seq " +
                    "WHERE seq.relkind = 'S' AND seq.relnamespace = current_schema()::regnamespace");
        }
    }

    /**
     * Delete entities by type (soft delete alternative).
     */
    public int deleteEntitiesByType(String entityType) throws SQLException {
        return connectionManager.executeInTransaction(connection -> {
            try (Statement stmt = connection.createStatement()) {
                return stmt.executeUpdate(
                        "DELETE FROM entities WHERE entity_type = '" + entityType + "'"
                );
            }
        });
    }

    /**
     * Delete a specific entity by ID.
     */
    public boolean deleteEntity(String id) throws SQLException {
        return connectionManager.executeInTransaction(connection -> {
            try (Statement stmt = connection.createStatement()) {
                int affected = stmt.executeUpdate(
                        "DELETE FROM entities WHERE id = '" + id + "'"
                );
                return affected > 0;
            }
        });
    }

    /**
     * Delete a specific government organization by ID.
     * Note: Will fail if entities reference this gov_org (unless CASCADE).
     */
    public boolean deleteGovOrg(String id) throws SQLException {
        return connectionManager.executeInTransaction(connection -> {
            try (Statement stmt = connection.createStatement()) {
                // First remove entity references
                stmt.executeUpdate(
                        "UPDATE entities SET government_org_id = NULL WHERE government_org_id = '" + id + "'"
                );
                int affected = stmt.executeUpdate(
                        "DELETE FROM government_organizations WHERE id = '" + id + "'"
                );
                return affected > 0;
            }
        });
    }

    /**
     * Clean and reseed - convenience method for test setup.
     */
    public void cleanAndReseed() throws SQLException {
        connectionManager.executeInTransaction(connection -> {
            cleanAllTables(connection);
            new TestDataSeeder(connectionManager).seedFullTestDataset(connection);
        });
        System.out.println("Cleaned and reseeded test data");
    }

    /**
     * Static factory method.
     */
    public static TestDataCleaner create() {
        return new TestDataCleaner();
    }

    /**
     * Static convenience method to clean all tables.
     */
    public static void cleanAll() throws SQLException {
        create().cleanAllTables();
    }

    /**
     * Static convenience method to clean and reseed.
     */
    public static void resetTestData() throws SQLException {
        create().cleanAndReseed();
    }
}
