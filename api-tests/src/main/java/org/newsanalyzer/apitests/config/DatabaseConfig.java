package org.newsanalyzer.apitests.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Database configuration for API integration tests.
 * Loads database connection settings from properties files based on test profile.
 * Supports externalized credentials via environment variables.
 */
public class DatabaseConfig {

    private static final String PROPERTIES_FILE_PATTERN = "application-%s.properties";
    private static final String DEFAULT_PROFILE = "local";

    private final String url;
    private final String username;
    private final String password;
    private final String driverClassName;
    private final int minimumIdle;
    private final int maximumPoolSize;
    private final long connectionTimeout;
    private final long idleTimeout;
    private final long maxLifetime;

    private static DatabaseConfig instance;

    private DatabaseConfig(String profile) {
        Properties props = loadProperties(profile);

        // Database URL - required
        this.url = resolveProperty(props, "db.url", "jdbc:postgresql://localhost:5432/newsanalyzer_test");

        // Credentials - support environment variable fallback
        this.username = resolveProperty(props, "db.username",
                System.getenv().getOrDefault("DB_USERNAME", "postgres"));
        this.password = resolveProperty(props, "db.password",
                System.getenv().getOrDefault("DB_PASSWORD", "postgres"));

        // Driver class
        this.driverClassName = resolveProperty(props, "db.driver", "org.postgresql.Driver");

        // Connection pool settings - optimized for tests
        this.minimumIdle = Integer.parseInt(resolveProperty(props, "db.pool.minIdle", "2"));
        this.maximumPoolSize = Integer.parseInt(resolveProperty(props, "db.pool.maxSize", "5"));
        this.connectionTimeout = Long.parseLong(resolveProperty(props, "db.pool.connectionTimeout", "30000"));
        this.idleTimeout = Long.parseLong(resolveProperty(props, "db.pool.idleTimeout", "600000"));
        this.maxLifetime = Long.parseLong(resolveProperty(props, "db.pool.maxLifetime", "1800000"));
    }

    /**
     * Get singleton instance with default profile from system property.
     */
    public static synchronized DatabaseConfig getInstance() {
        if (instance == null) {
            String profile = System.getProperty("test.profile", DEFAULT_PROFILE);
            instance = new DatabaseConfig(profile);
        }
        return instance;
    }

    /**
     * Get singleton instance with specified profile.
     */
    public static synchronized DatabaseConfig getInstance(String profile) {
        if (instance == null || !instance.url.contains(profile)) {
            instance = new DatabaseConfig(profile);
        }
        return instance;
    }

    /**
     * Reset the singleton instance (useful for tests).
     */
    public static synchronized void reset() {
        instance = null;
    }

    private Properties loadProperties(String profile) {
        Properties props = new Properties();
        String fileName = String.format(PROPERTIES_FILE_PATTERN, profile);

        try (InputStream is = getClass().getClassLoader().getResourceAsStream(fileName)) {
            if (is != null) {
                props.load(is);
            } else {
                System.out.println("Warning: Properties file not found: " + fileName +
                        ", using defaults");
            }
        } catch (IOException e) {
            System.err.println("Error loading properties file: " + fileName);
            e.printStackTrace();
        }

        return props;
    }

    private String resolveProperty(Properties props, String key, String defaultValue) {
        String value = props.getProperty(key);
        if (value == null) {
            return defaultValue;
        }

        // Support ${ENV_VAR:default} syntax
        if (value.startsWith("${") && value.contains("}")) {
            int endIndex = value.indexOf("}");
            String envPart = value.substring(2, endIndex);
            String[] parts = envPart.split(":", 2);
            String envVar = parts[0];
            String envDefault = parts.length > 1 ? parts[1] : defaultValue;

            String envValue = System.getenv(envVar);
            return envValue != null ? envValue : envDefault;
        }

        return value;
    }

    // Getters
    public String getUrl() {
        return url;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getDriverClassName() {
        return driverClassName;
    }

    public int getMinimumIdle() {
        return minimumIdle;
    }

    public int getMaximumPoolSize() {
        return maximumPoolSize;
    }

    public long getConnectionTimeout() {
        return connectionTimeout;
    }

    public long getIdleTimeout() {
        return idleTimeout;
    }

    public long getMaxLifetime() {
        return maxLifetime;
    }

    @Override
    public String toString() {
        return "DatabaseConfig{" +
                "url='" + url + '\'' +
                ", username='" + username + '\'' +
                ", driverClassName='" + driverClassName + '\'' +
                ", minimumIdle=" + minimumIdle +
                ", maximumPoolSize=" + maximumPoolSize +
                '}';
    }
}
