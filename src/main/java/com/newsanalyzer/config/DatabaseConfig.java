package com.newsanalyzer.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Database configuration for the News Analyzer application.
 *
 * Enables JPA auditing for automatic timestamp management
 * and transaction management across the application.
 */
@Configuration
@EnableJpaAuditing
@EnableTransactionManagement
public class DatabaseConfig {

    // JPA Auditing is enabled to automatically populate
    // @CreatedDate and @LastModifiedDate fields in entities

    // Transaction management is enabled for declarative transactions
    // using @Transactional annotations
}