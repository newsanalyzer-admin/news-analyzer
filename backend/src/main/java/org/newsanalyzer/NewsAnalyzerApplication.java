package org.newsanalyzer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * NewsAnalyzer v2 - Main Application
 *
 * Independent, open-source platform for news analysis, fact-checking, and bias detection.
 * Hosted on Hetzner Cloud (Germany) for transparency and independence.
 */
@SpringBootApplication
@EnableJpaAuditing
@EnableCaching
public class NewsAnalyzerApplication {

    public static void main(String[] args) {
        SpringApplication.run(NewsAnalyzerApplication.class, args);
    }
}
