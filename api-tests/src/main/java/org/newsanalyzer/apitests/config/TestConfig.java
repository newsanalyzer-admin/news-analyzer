package org.newsanalyzer.apitests.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Loads environment-specific configuration properties for API tests.
 * Properties are loaded based on the active test profile (local, ci, staging).
 */
public class TestConfig {

    private static final Properties properties = new Properties();
    private static final String PROFILE_PROPERTY = "test.profile";
    private static final String DEFAULT_PROFILE = "local";

    static {
        loadProperties();
    }

    private static void loadProperties() {
        String profile = System.getProperty(PROFILE_PROPERTY, DEFAULT_PROFILE);
        String propertiesFile = "application-" + profile + ".properties";

        try (InputStream input = TestConfig.class.getClassLoader().getResourceAsStream(propertiesFile)) {
            if (input != null) {
                properties.load(input);
            } else {
                System.err.println("Warning: Could not find " + propertiesFile + ", using defaults");
                setDefaults();
            }
        } catch (IOException e) {
            System.err.println("Error loading properties: " + e.getMessage());
            setDefaults();
        }
    }

    private static void setDefaults() {
        properties.setProperty("backend.baseUrl", "http://localhost:8080");
        properties.setProperty("reasoning.baseUrl", "http://localhost:8000");
        properties.setProperty("test.timeout.seconds", "30");
    }

    public static String getBackendBaseUrl() {
        return properties.getProperty("backend.baseUrl", "http://localhost:8080");
    }

    public static String getReasoningBaseUrl() {
        return properties.getProperty("reasoning.baseUrl", "http://localhost:8000");
    }

    public static int getTimeoutSeconds() {
        return Integer.parseInt(properties.getProperty("test.timeout.seconds", "30"));
    }

    public static String getProperty(String key) {
        return properties.getProperty(key);
    }

    public static String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    public static String getActiveProfile() {
        return System.getProperty(PROFILE_PROPERTY, DEFAULT_PROFILE);
    }
}
