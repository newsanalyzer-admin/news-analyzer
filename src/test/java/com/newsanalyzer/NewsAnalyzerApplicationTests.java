package com.newsanalyzer;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Basic integration test for the News Analyzer application.
 *
 * This test verifies that the Spring application context loads correctly
 * with all configurations and components properly wired.
 */
@SpringBootTest
@ActiveProfiles("test")
class NewsAnalyzerApplicationTests {

    /**
     * Test that the Spring application context loads successfully.
     * This is a smoke test to ensure basic application configuration is correct.
     */
    @Test
    void contextLoads() {
        // This test will fail if there are any configuration errors
        // or missing dependencies in the application context
    }
}