package org.newsanalyzer.apitests.util;

import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * JUnit 5 extension that runs Flyway migrations before all tests in a class.
 *
 * Usage:
 * <pre>
 * {@code
 * @ExtendWith(FlywayMigrationExtension.class)
 * class MyDatabaseTest {
 *     // migrations run once before all tests
 * }
 * }
 * </pre>
 *
 * Or with @RegisterExtension for more control:
 * <pre>
 * {@code
 * class MyDatabaseTest {
 *     @RegisterExtension
 *     static FlywayMigrationExtension migrations = new FlywayMigrationExtension(true);
 * }
 * }
 * </pre>
 */
public class FlywayMigrationExtension implements BeforeAllCallback {

    private static final ExtensionContext.Namespace NAMESPACE =
            ExtensionContext.Namespace.create(FlywayMigrationExtension.class);
    private static final String MIGRATIONS_RUN_KEY = "migrationsRun";

    private final boolean cleanFirst;
    private final boolean verbose;

    /**
     * Create extension with default settings (no clean, non-verbose).
     */
    public FlywayMigrationExtension() {
        this(false, false);
    }

    /**
     * Create extension with clean option.
     * @param cleanFirst if true, clean database before migrations
     */
    public FlywayMigrationExtension(boolean cleanFirst) {
        this(cleanFirst, false);
    }

    /**
     * Create extension with full options.
     * @param cleanFirst if true, clean database before migrations
     * @param verbose if true, print migration status
     */
    public FlywayMigrationExtension(boolean cleanFirst, boolean verbose) {
        this.cleanFirst = cleanFirst;
        this.verbose = verbose;
    }

    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
        // Use root context store to ensure migrations run only once per test run
        ExtensionContext.Store store = context.getRoot().getStore(NAMESPACE);

        // Check if migrations have already been run
        Boolean alreadyRun = store.get(MIGRATIONS_RUN_KEY, Boolean.class);
        if (Boolean.TRUE.equals(alreadyRun)) {
            if (verbose) {
                System.out.println("Migrations already run in this test execution");
            }
            return;
        }

        // Run migrations
        try {
            FlywayMigrationRunner runner = FlywayMigrationRunner.create();

            if (verbose) {
                System.out.println("\n=== Running Flyway Migrations ===");
                System.out.println("Clean first: " + cleanFirst);
            }

            if (cleanFirst) {
                runner.cleanAndMigrate();
            } else {
                runner.migrate();
            }

            if (verbose) {
                runner.printMigrationStatus();
            }

            // Mark migrations as run
            store.put(MIGRATIONS_RUN_KEY, Boolean.TRUE);

        } catch (Exception e) {
            System.err.println("Failed to run Flyway migrations: " + e.getMessage());
            throw new RuntimeException("Migration failed", e);
        }
    }

    /**
     * Factory method for clean + migrate extension.
     */
    public static FlywayMigrationExtension withClean() {
        return new FlywayMigrationExtension(true, false);
    }

    /**
     * Factory method for verbose extension.
     */
    public static FlywayMigrationExtension verbose() {
        return new FlywayMigrationExtension(false, true);
    }

    /**
     * Factory method for clean + verbose extension.
     */
    public static FlywayMigrationExtension withCleanVerbose() {
        return new FlywayMigrationExtension(true, true);
    }
}
