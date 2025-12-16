package org.newsanalyzer.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import lombok.Data;

/**
 * Configuration properties for the unitedstates/congress-legislators GitHub repository sync.
 *
 * @author James (Dev Agent)
 * @since 2.0.0
 */
@Configuration
@ConfigurationProperties(prefix = "legislators")
@Data
public class LegislatorsConfig {

    private GitHub github = new GitHub();
    private Sync sync = new Sync();

    @Data
    public static class GitHub {
        private String baseUrl = "https://raw.githubusercontent.com/unitedstates/congress-legislators/main";
        private String apiUrl = "https://api.github.com/repos/unitedstates/congress-legislators";
        private String currentFile = "legislators-current.yaml";
        private String historicalFile = "legislators-historical.yaml";
        private int timeout = 30000;
    }

    @Data
    public static class Sync {
        private String schedule = "0 0 4 * * SUN";
        private String historicalCutoff = "1990-01-01";
    }
}
