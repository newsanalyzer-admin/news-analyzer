package org.newsanalyzer.apitests.data;

import org.newsanalyzer.apitests.util.DatabaseConnectionManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.stream.Collectors;

/**
 * Test data seeder for database integration tests.
 * Provides methods to seed entities and government organizations from SQL scripts.
 */
public class TestDataSeeder {

    private static final String SEED_PATH = "seed/";
    private static final String ENTITIES_SQL = SEED_PATH + "entities.sql";
    private static final String GOV_ORGS_SQL = SEED_PATH + "government_organizations.sql";
    private static final String PERSONS_SQL = SEED_PATH + "persons.sql";

    private final DatabaseConnectionManager connectionManager;

    public TestDataSeeder() {
        this.connectionManager = DatabaseConnectionManager.getInstance();
    }

    public TestDataSeeder(DatabaseConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    /**
     * Seed entities table from SQL script.
     */
    public void seedEntities() throws SQLException {
        executeSqlScript(ENTITIES_SQL);
        System.out.println("Seeded entities table");
    }

    /**
     * Seed entities using provided connection (for transaction control).
     */
    public void seedEntities(Connection connection) throws SQLException {
        executeSqlScript(connection, ENTITIES_SQL);
        System.out.println("Seeded entities table");
    }

    /**
     * Seed government organizations table from SQL script.
     */
    public void seedGovernmentOrganizations() throws SQLException {
        executeSqlScript(GOV_ORGS_SQL);
        System.out.println("Seeded government_organizations table");
    }

    /**
     * Seed government organizations using provided connection.
     */
    public void seedGovernmentOrganizations(Connection connection) throws SQLException {
        executeSqlScript(connection, GOV_ORGS_SQL);
        System.out.println("Seeded government_organizations table");
    }

    /**
     * Seed persons (Congressional members) table from SQL script.
     */
    public void seedPersons() throws SQLException {
        executeSqlScript(PERSONS_SQL);
        System.out.println("Seeded persons table");
    }

    /**
     * Seed persons using provided connection.
     */
    public void seedPersons(Connection connection) throws SQLException {
        executeSqlScript(connection, PERSONS_SQL);
        System.out.println("Seeded persons table");
    }

    /**
     * Seed all test data (government orgs first due to FK constraints).
     */
    public void seedFullTestDataset() throws SQLException {
        connectionManager.executeInTransaction(connection -> {
            // Order matters: gov orgs first, then entities (FK reference), then persons
            seedGovernmentOrganizations(connection);
            seedEntities(connection);
            seedPersons(connection);
        });
        System.out.println("Seeded full test dataset");
    }

    /**
     * Seed all test data using provided connection.
     */
    public void seedFullTestDataset(Connection connection) throws SQLException {
        // Order matters: gov orgs first, then entities (FK reference), then persons
        seedGovernmentOrganizations(connection);
        seedEntities(connection);
        seedPersons(connection);
        System.out.println("Seeded full test dataset");
    }

    /**
     * Execute a SQL script from resources.
     */
    private void executeSqlScript(String resourcePath) throws SQLException {
        connectionManager.executeInTransaction(connection -> {
            executeSqlScript(connection, resourcePath);
        });
    }

    /**
     * Execute a SQL script using provided connection.
     */
    private void executeSqlScript(Connection connection, String resourcePath) throws SQLException {
        String sql = loadSqlScript(resourcePath);
        if (sql == null || sql.trim().isEmpty()) {
            System.out.println("Warning: Empty or missing SQL script: " + resourcePath);
            return;
        }

        // Remove SQL comments before processing
        sql = removeComments(sql);

        try (Statement statement = connection.createStatement()) {
            // Split by semicolon and execute each statement
            String[] statements = sql.split(";");
            for (String stmt : statements) {
                String trimmed = stmt.trim();
                if (!trimmed.isEmpty()) {
                    statement.executeUpdate(trimmed);
                }
            }
        }
    }

    /**
     * Remove SQL comments (lines starting with --) from the script.
     */
    private String removeComments(String sql) {
        StringBuilder result = new StringBuilder();
        for (String line : sql.split("\n")) {
            String trimmedLine = line.trim();
            if (!trimmedLine.startsWith("--")) {
                result.append(line).append("\n");
            }
        }
        return result.toString();
    }

    /**
     * Load SQL script content from classpath.
     */
    private String loadSqlScript(String resourcePath) {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
            if (is == null) {
                System.err.println("SQL script not found: " + resourcePath);
                return null;
            }
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(is, StandardCharsets.UTF_8))) {
                return reader.lines().collect(Collectors.joining("\n"));
            }
        } catch (IOException e) {
            System.err.println("Error loading SQL script: " + resourcePath);
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Execute custom SQL statements.
     */
    public void executeSql(String sql) throws SQLException {
        connectionManager.executeInTransaction(connection -> {
            try (Statement statement = connection.createStatement()) {
                statement.execute(sql);
            }
        });
    }

    /**
     * Execute custom SQL using provided connection.
     */
    public void executeSql(Connection connection, String sql) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute(sql);
        }
    }

    /**
     * Static factory method.
     */
    public static TestDataSeeder create() {
        return new TestDataSeeder();
    }

    /**
     * Static convenience method to seed full dataset.
     */
    public static void seedAll() throws SQLException {
        create().seedFullTestDataset();
    }
}
