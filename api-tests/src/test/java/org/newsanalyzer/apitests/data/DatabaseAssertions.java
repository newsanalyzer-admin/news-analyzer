package org.newsanalyzer.apitests.data;

import org.newsanalyzer.apitests.util.DatabaseConnectionManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Database assertion utilities for integration tests.
 * Provides fluent assertions to verify database state.
 *
 * <p>Usage:</p>
 * <pre>
 * {@code
 * DatabaseAssertions.assertEntityExists(entityId);
 * DatabaseAssertions.assertEntityCount(5);
 * DatabaseAssertions.assertEntityHasName(entityId, "Expected Name");
 * DatabaseAssertions.assertGovOrgExists(govOrgId);
 * }
 * </pre>
 */
public class DatabaseAssertions {

    private static final DatabaseConnectionManager connectionManager = DatabaseConnectionManager.getInstance();

    private DatabaseAssertions() {
        // Utility class
    }

    // ========== Entity Assertions ==========

    /**
     * Assert that an entity with the given ID exists in the database.
     */
    public static void assertEntityExists(String id) {
        assertTrue(entityExists(id), "Entity should exist with ID: " + id);
    }

    /**
     * Assert that an entity with the given ID exists in the database.
     */
    public static void assertEntityExists(UUID id) {
        assertEntityExists(id.toString());
    }

    /**
     * Assert that an entity with the given ID does NOT exist in the database.
     */
    public static void assertEntityNotExists(String id) {
        assertFalse(entityExists(id), "Entity should NOT exist with ID: " + id);
    }

    /**
     * Assert that an entity with the given ID does NOT exist in the database.
     */
    public static void assertEntityNotExists(UUID id) {
        assertEntityNotExists(id.toString());
    }

    /**
     * Assert that an entity exists with the given name.
     */
    public static void assertEntityExistsByName(String name) {
        assertTrue(entityExistsByName(name), "Entity should exist with name: " + name);
    }

    /**
     * Assert that the total count of entities matches expected.
     */
    public static void assertEntityCount(int expectedCount) {
        int actualCount = countEntities();
        assertEquals(expectedCount, actualCount,
                "Expected " + expectedCount + " entities but found " + actualCount);
    }

    /**
     * Assert that the count of entities with given type matches expected.
     */
    public static void assertEntityCountByType(String entityType, int expectedCount) {
        int actualCount = countEntitiesByType(entityType);
        assertEquals(expectedCount, actualCount,
                "Expected " + expectedCount + " entities of type " + entityType + " but found " + actualCount);
    }

    /**
     * Assert that an entity has the expected name.
     */
    public static void assertEntityHasName(String id, String expectedName) {
        String actualName = getEntityName(id);
        assertEquals(expectedName, actualName,
                "Entity " + id + " should have name '" + expectedName + "' but was '" + actualName + "'");
    }

    /**
     * Assert that an entity has the expected type.
     */
    public static void assertEntityHasType(String id, String expectedType) {
        String actualType = getEntityType(id);
        assertEquals(expectedType, actualType,
                "Entity " + id + " should have type '" + expectedType + "' but was '" + actualType + "'");
    }

    /**
     * Assert that an entity is verified.
     */
    public static void assertEntityIsVerified(String id) {
        assertTrue(isEntityVerified(id), "Entity " + id + " should be verified");
    }

    /**
     * Assert that an entity is NOT verified.
     */
    public static void assertEntityIsNotVerified(String id) {
        assertFalse(isEntityVerified(id), "Entity " + id + " should NOT be verified");
    }

    /**
     * Assert that an entity is linked to a government organization.
     */
    public static void assertEntityLinkedToGovOrg(String entityId, String govOrgId) {
        String actualGovOrgId = getEntityGovOrgId(entityId);
        assertEquals(govOrgId, actualGovOrgId,
                "Entity " + entityId + " should be linked to gov org " + govOrgId);
    }

    // ========== Government Organization Assertions ==========

    /**
     * Assert that a government organization with the given ID exists.
     */
    public static void assertGovOrgExists(String id) {
        assertTrue(govOrgExists(id), "Government organization should exist with ID: " + id);
    }

    /**
     * Assert that a government organization with the given ID exists.
     */
    public static void assertGovOrgExists(UUID id) {
        assertGovOrgExists(id.toString());
    }

    /**
     * Assert that a government organization with the given ID does NOT exist.
     */
    public static void assertGovOrgNotExists(String id) {
        assertFalse(govOrgExists(id), "Government organization should NOT exist with ID: " + id);
    }

    /**
     * Assert that the total count of government organizations matches expected.
     */
    public static void assertGovOrgCount(int expectedCount) {
        int actualCount = countGovOrgs();
        assertEquals(expectedCount, actualCount,
                "Expected " + expectedCount + " government organizations but found " + actualCount);
    }

    /**
     * Assert that a government organization has the expected official name.
     */
    public static void assertGovOrgHasName(String id, String expectedName) {
        String actualName = getGovOrgName(id);
        assertEquals(expectedName, actualName,
                "Gov org " + id + " should have name '" + expectedName + "' but was '" + actualName + "'");
    }

    /**
     * Assert that a government organization has the expected acronym.
     */
    public static void assertGovOrgHasAcronym(String id, String expectedAcronym) {
        String actualAcronym = getGovOrgAcronym(id);
        assertEquals(expectedAcronym, actualAcronym,
                "Gov org " + id + " should have acronym '" + expectedAcronym + "' but was '" + actualAcronym + "'");
    }

    /**
     * Assert that a government organization has the expected type.
     */
    public static void assertGovOrgHasType(String id, String expectedType) {
        String actualType = getGovOrgType(id);
        assertEquals(expectedType, actualType,
                "Gov org " + id + " should have type '" + expectedType + "' but was '" + actualType + "'");
    }

    /**
     * Assert that a government organization has the expected parent.
     */
    public static void assertGovOrgHasParent(String id, String expectedParentId) {
        String actualParentId = getGovOrgParentId(id);
        assertEquals(expectedParentId, actualParentId,
                "Gov org " + id + " should have parent " + expectedParentId);
    }

    /**
     * Assert that a government organization has no parent (is a root org).
     */
    public static void assertGovOrgIsRoot(String id) {
        String parentId = getGovOrgParentId(id);
        assertNull(parentId, "Gov org " + id + " should be a root organization (no parent)");
    }

    // ========== Table State Assertions ==========

    /**
     * Assert that the entities table is empty.
     */
    public static void assertEntitiesTableEmpty() {
        assertEntityCount(0);
    }

    /**
     * Assert that the government organizations table is empty.
     */
    public static void assertGovOrgsTableEmpty() {
        assertGovOrgCount(0);
    }

    /**
     * Assert that both tables are empty.
     */
    public static void assertAllTablesEmpty() {
        assertEntitiesTableEmpty();
        assertGovOrgsTableEmpty();
    }

    // ========== Query Helper Methods ==========

    private static boolean entityExists(String id) {
        return queryForBoolean("SELECT EXISTS(SELECT 1 FROM entities WHERE id = ?)", id);
    }

    private static boolean entityExistsByName(String name) {
        return queryForBoolean("SELECT EXISTS(SELECT 1 FROM entities WHERE name = ?)", name);
    }

    private static int countEntities() {
        return queryForInt("SELECT COUNT(*) FROM entities");
    }

    private static int countEntitiesByType(String entityType) {
        return queryForInt("SELECT COUNT(*) FROM entities WHERE entity_type = ?", entityType);
    }

    private static String getEntityName(String id) {
        return queryForString("SELECT name FROM entities WHERE id = ?", id);
    }

    private static String getEntityType(String id) {
        return queryForString("SELECT entity_type FROM entities WHERE id = ?", id);
    }

    private static boolean isEntityVerified(String id) {
        return queryForBoolean("SELECT verified FROM entities WHERE id = ?", id);
    }

    private static String getEntityGovOrgId(String id) {
        return queryForString("SELECT government_org_id FROM entities WHERE id = ?", id);
    }

    private static boolean govOrgExists(String id) {
        return queryForBoolean("SELECT EXISTS(SELECT 1 FROM government_organizations WHERE id = ?)", id);
    }

    private static int countGovOrgs() {
        return queryForInt("SELECT COUNT(*) FROM government_organizations");
    }

    private static String getGovOrgName(String id) {
        return queryForString("SELECT official_name FROM government_organizations WHERE id = ?", id);
    }

    private static String getGovOrgAcronym(String id) {
        return queryForString("SELECT acronym FROM government_organizations WHERE id = ?", id);
    }

    private static String getGovOrgType(String id) {
        return queryForString("SELECT organization_type FROM government_organizations WHERE id = ?", id);
    }

    private static String getGovOrgParentId(String id) {
        return queryForString("SELECT parent_id FROM government_organizations WHERE id = ?", id);
    }

    // ========== Low-level Query Methods ==========

    private static boolean queryForBoolean(String sql, Object... params) {
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            setParameters(stmt, params);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getBoolean(1);
                }
                return false;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database query failed: " + sql, e);
        }
    }

    private static int queryForInt(String sql, Object... params) {
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            setParameters(stmt, params);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
                return 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database query failed: " + sql, e);
        }
    }

    private static String queryForString(String sql, Object... params) {
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            setParameters(stmt, params);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString(1);
                }
                return null;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database query failed: " + sql, e);
        }
    }

    private static void setParameters(PreparedStatement stmt, Object... params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            Object param = params[i];
            if (param instanceof String) {
                stmt.setString(i + 1, (String) param);
            } else if (param instanceof UUID) {
                stmt.setObject(i + 1, param);
            } else if (param instanceof Integer) {
                stmt.setInt(i + 1, (Integer) param);
            } else if (param instanceof Boolean) {
                stmt.setBoolean(i + 1, (Boolean) param);
            } else {
                stmt.setObject(i + 1, param);
            }
        }
    }
}
