package com.newsanalyzer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main application class for the News Analyzer platform.
 *
 * This application provides fact-checking, bias detection, and logical fallacy
 * identification for news articles, blogs, and social media posts.
 *
 * Features:
 * - Fact validation against authoritative sources
 * - Source reliability scoring
 * - Bias detection and analysis
 * - Personal knowledge workbenches
 * - Real-time analysis APIs
 */
@SpringBootApplication
@EnableCaching
@EnableAsync
@EnableScheduling
@EnableJpaRepositories(basePackages = "com.newsanalyzer.repository.jpa")
@EnableMongoRepositories(basePackages = "com.newsanalyzer.repository.mongo")
@EnableElasticsearchRepositories(basePackages = "com.newsanalyzer.repository.elasticsearch")
public class NewsAnalyzerApplication {

    public static void main(String[] args) {
        SpringApplication.run(NewsAnalyzerApplication.class, args);
    }
}