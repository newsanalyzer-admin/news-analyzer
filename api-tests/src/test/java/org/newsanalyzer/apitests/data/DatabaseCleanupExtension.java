package org.newsanalyzer.apitests.data;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.newsanalyzer.apitests.util.DatabaseConnectionManager;

import java.sql.SQLException;

/**
 * JUnit 5 extension for database cleanup between tests.
 * Provides automatic cleanup and reseeding before each test method,
 * and final cleanup after all tests complete.
 *
 * <p>Usage:</p>
 * <pre>
 * {@code
 * @ExtendWith(DatabaseCleanupExtension.class)
 * public class MyDatabaseTest {
 *     @Test
 *     void testSomething() {
 *         // Database is clean and seeded before this runs
 *     }
 * }
 * }
 * </pre>
 *
 * <p>Or with custom configuration:</p>
 * <pre>
 * {@code
 * @RegisterExtension
 * static DatabaseCleanupExtension cleanup = DatabaseCleanupExtension.builder()
 *     .cleanOnly(true)  // Don't reseed
 *     .verbose(true)
 *     .build();
 * }
 * </pre>
 */
public class DatabaseCleanupExtension implements BeforeEachCallback, AfterAllCallback {

    private final boolean reseedAfterClean;
    private final boolean verbose;
    private final boolean cleanBeforeEach;
    private final boolean cleanAfterAll;

    private final TestDataCleaner cleaner;
    private final TestDataSeeder seeder;

    /**
     * Default constructor for @ExtendWith usage.
     * Cleans and reseeds before each test.
     */
    public DatabaseCleanupExtension() {
        this(true, false, true, true);
    }

    /**
     * Full constructor for custom configuration.
     */
    public DatabaseCleanupExtension(boolean reseedAfterClean, boolean verbose,
                                     boolean cleanBeforeEach, boolean cleanAfterAll) {
        this.reseedAfterClean = reseedAfterClean;
        this.verbose = verbose;
        this.cleanBeforeEach = cleanBeforeEach;
        this.cleanAfterAll = cleanAfterAll;

        DatabaseConnectionManager connectionManager = DatabaseConnectionManager.getInstance();
        this.cleaner = new TestDataCleaner(connectionManager);
        this.seeder = new TestDataSeeder(connectionManager);
    }

    @Override
    public void beforeEach(ExtensionContext context) throws Exception {
        if (!cleanBeforeEach) {
            return;
        }

        String testName = context.getDisplayName();

        if (verbose) {
            System.out.println("[DatabaseCleanup] Before test: " + testName);
        }

        try {
            if (reseedAfterClean) {
                cleaner.cleanAndReseed();
                if (verbose) {
                    System.out.println("[DatabaseCleanup] Cleaned and reseeded database");
                }
            } else {
                cleaner.cleanAllTables();
                if (verbose) {
                    System.out.println("[DatabaseCleanup] Cleaned database (no reseed)");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to clean database before test: " + testName, e);
        }
    }

    @Override
    public void afterAll(ExtensionContext context) throws Exception {
        if (!cleanAfterAll) {
            return;
        }

        String className = context.getTestClass()
                .map(Class::getSimpleName)
                .orElse("Unknown");

        if (verbose) {
            System.out.println("[DatabaseCleanup] After all tests in: " + className);
        }

        try {
            cleaner.cleanAllTables();
            if (verbose) {
                System.out.println("[DatabaseCleanup] Final cleanup complete");
            }
        } catch (SQLException e) {
            // Log but don't fail - cleanup after all tests is best-effort
            System.err.println("[DatabaseCleanup] Warning: Failed final cleanup for " + className);
            e.printStackTrace();
        }
    }

    // ========== Factory Methods ==========

    /**
     * Create extension that only cleans (no reseed).
     */
    public static DatabaseCleanupExtension cleanOnly() {
        return new DatabaseCleanupExtension(false, false, true, true);
    }

    /**
     * Create extension with verbose logging.
     */
    public static DatabaseCleanupExtension verbose() {
        return new DatabaseCleanupExtension(true, true, true, true);
    }

    /**
     * Create extension that cleans only, with verbose logging.
     */
    public static DatabaseCleanupExtension cleanOnlyVerbose() {
        return new DatabaseCleanupExtension(false, true, true, true);
    }

    /**
     * Create extension that doesn't clean before each test.
     * Useful when tests manage their own data.
     */
    public static DatabaseCleanupExtension afterAllOnly() {
        return new DatabaseCleanupExtension(false, false, false, true);
    }

    /**
     * Create a builder for custom configuration.
     */
    public static Builder builder() {
        return new Builder();
    }

    // ========== Builder ==========

    public static class Builder {
        private boolean reseedAfterClean = true;
        private boolean verbose = false;
        private boolean cleanBeforeEach = true;
        private boolean cleanAfterAll = true;

        /**
         * Only clean tables, don't reseed with test data.
         */
        public Builder cleanOnly(boolean cleanOnly) {
            this.reseedAfterClean = !cleanOnly;
            return this;
        }

        /**
         * Reseed database after cleaning.
         */
        public Builder reseed(boolean reseed) {
            this.reseedAfterClean = reseed;
            return this;
        }

        /**
         * Enable verbose logging.
         */
        public Builder verbose(boolean verbose) {
            this.verbose = verbose;
            return this;
        }

        /**
         * Clean database before each test method.
         */
        public Builder cleanBeforeEach(boolean clean) {
            this.cleanBeforeEach = clean;
            return this;
        }

        /**
         * Clean database after all tests complete.
         */
        public Builder cleanAfterAll(boolean clean) {
            this.cleanAfterAll = clean;
            return this;
        }

        public DatabaseCleanupExtension build() {
            return new DatabaseCleanupExtension(reseedAfterClean, verbose, cleanBeforeEach, cleanAfterAll);
        }
    }
}
