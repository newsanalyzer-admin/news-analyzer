package org.newsanalyzer.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI/Swagger configuration for API documentation.
 *
 * Access at: http://localhost:8080/swagger-ui.html
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI newsAnalyzerOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("NewsAnalyzer API")
                .description("""
                    NewsAnalyzer v2 REST API

                    Independent, open-source platform for news analysis, fact-checking, and bias detection.

                    Features:
                    - Entity extraction with Schema.org support
                    - Logical fallacy detection
                    - Cognitive bias identification
                    - Fact verification
                    - Source reliability tracking

                    All entities are stored with dual-layer design:
                    - Internal classification (entity_type) for database optimization
                    - Schema.org vocabulary (schema_org_type, schema_org_data) for semantic web standards
                    """)
                .version("2.0.0")
                .contact(new Contact()
                    .name("NewsAnalyzer Team")
                    .email("admin@newsanalyzer.org")
                    .url("https://newsanalyzer.org"))
                .license(new License()
                    .name("MIT License")
                    .url("https://opensource.org/licenses/MIT")))
            .servers(List.of(
                new Server()
                    .url("http://localhost:8080")
                    .description("Development server"),
                new Server()
                    .url("https://api.newsanalyzer.org")
                    .description("Production server (Hetzner Cloud, Germany)")
            ));
    }
}
