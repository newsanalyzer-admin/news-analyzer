package org.newsanalyzer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * NewsAnalyzer v2 - Main Application
 *
 * Independent, open-source platform for news analysis, fact-checking, and bias detection.
 * Hosted on Hetzner Cloud (Germany) for transparency and independence.
 */
@SpringBootApplication
@EnableCaching
@EnableScheduling
@ConfigurationPropertiesScan
public class NewsAnalyzerApplication {

    public static void main(String[] args) {
        SpringApplication.run(NewsAnalyzerApplication.class, args);
    }
}
