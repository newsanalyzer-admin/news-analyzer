package org.newsanalyzer.config;

import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.type.format.jackson.JacksonJsonFormatMapper;
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * JPA Configuration for NewsAnalyzer.
 *
 * Configures:
 * - JSONB type support via Hypersistence Utils
 * - JPA auditing (@CreatedDate, @LastModifiedDate)
 * - Transaction management
 * - Repository scanning
 */
@Configuration
@EnableJpaRepositories(basePackages = "org.newsanalyzer.repository")
@EnableJpaAuditing
@EnableTransactionManagement
public class JpaConfig {

    /**
     * Register custom Hibernate types (JSONB support)
     */
    @Bean
    public HibernatePropertiesCustomizer hibernatePropertiesCustomizer() {
        return hibernateProperties -> {
            // Register JsonBinaryType for JSONB columns
            hibernateProperties.put(
                AvailableSettings.JSON_FORMAT_MAPPER,
                JacksonJsonFormatMapper.class.getName()
            );
        };
    }
}
