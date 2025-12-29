package org.newsanalyzer;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * Testcontainers configuration for PostgreSQL integration tests.
 *
 * This configuration provides a real PostgreSQL container for tests that require
 * PostgreSQL-specific features like JSONB columns and array types.
 *
 * Usage:
 * <pre>
 * {@code
 * @DataJpaTest
 * @ActiveProfiles("tc")
 * @AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
 * @Import(TestcontainersConfiguration.class)
 * class MyRepositoryTest {
 *     // ...
 * }
 * }
 * </pre>
 *
 * Requirements:
 * - Docker must be running
 * - Use @ActiveProfiles("tc") to load the testcontainers profile (application-tc.yml)
 *
 * The container is shared across all tests using this configuration via static initialization.
 */
@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersConfiguration {

    static final PostgreSQLContainer<?> postgres;

    static {
        postgres = new PostgreSQLContainer<>(DockerImageName.parse("postgres:15-alpine"))
                .withDatabaseName("testdb")
                .withUsername("test")
                .withPassword("test");
        postgres.start();
    }

    @Bean
    @ServiceConnection
    PostgreSQLContainer<?> postgresContainer() {
        return postgres;
    }
}
