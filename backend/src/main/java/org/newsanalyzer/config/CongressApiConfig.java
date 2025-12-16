package org.newsanalyzer.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import lombok.Data;

/**
 * Configuration properties for Congress.gov API.
 *
 * @author James (Dev Agent)
 * @since 2.0.0
 */
@Configuration
@ConfigurationProperties(prefix = "congress.api")
@Data
public class CongressApiConfig {

    /**
     * Base URL for Congress.gov API
     */
    private String baseUrl = "https://api.congress.gov/v3";

    /**
     * API key from api.data.gov
     */
    private String key;

    /**
     * Rate limit (requests per hour)
     */
    private int rateLimit = 5000;

    /**
     * Request timeout in milliseconds
     */
    private int timeout = 30000;

    /**
     * Check if API key is configured
     */
    public boolean isConfigured() {
        return key != null && !key.isEmpty();
    }
}
