package org.newsanalyzer.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import lombok.Data;

import java.time.Duration;

/**
 * Configuration for PLUM CSV import.
 *
 * @author James (Dev Agent)
 * @since 2.0.0
 */
@Configuration
@ConfigurationProperties(prefix = "plum")
@Data
public class PlumConfig {

    /**
     * CSV file download settings
     */
    private Csv csv = new Csv();

    /**
     * Import settings
     */
    private Import importSettings = new Import();

    @Data
    public static class Csv {
        /**
         * URL to download PLUM CSV from
         */
        private String url = "https://www.opm.gov/about-us/open-government/plum-reporting/plum-archive/plum-archive-biden-administration.csv";
    }

    @Data
    public static class Import {
        /**
         * Batch size for processing records
         */
        private int batchSize = 100;
    }

    /**
     * RestTemplate configured for PLUM CSV download.
     *
     * Configured with longer timeouts since the CSV file can be large.
     */
    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder
                .setConnectTimeout(Duration.ofSeconds(30))
                .setReadTimeout(Duration.ofMinutes(5))
                .build();
    }
}
