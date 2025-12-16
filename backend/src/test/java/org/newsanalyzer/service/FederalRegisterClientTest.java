package org.newsanalyzer.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.newsanalyzer.config.FederalRegisterConfig;
import org.newsanalyzer.dto.FederalRegisterAgency;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for FederalRegisterClient.
 *
 * @author James (Dev Agent)
 */
@ExtendWith(MockitoExtension.class)
class FederalRegisterClientTest {

    private FederalRegisterConfig config;
    private ObjectMapper objectMapper;
    private FederalRegisterClient client;

    @BeforeEach
    void setUp() {
        config = new FederalRegisterConfig();
        config.setBaseUrl("https://www.federalregister.gov/api/v1");
        config.setTimeout(30000);
        config.setRetryAttempts(3);
        config.setRateLimitMs(100);

        objectMapper = new ObjectMapper();
        client = new FederalRegisterClient(config, objectMapper);
    }

    @Test
    @DisplayName("Config should have correct default values")
    void config_defaults_areCorrect() {
        FederalRegisterConfig defaultConfig = new FederalRegisterConfig();
        assertThat(defaultConfig.getBaseUrl()).isEqualTo("https://www.federalregister.gov/api/v1");
        assertThat(defaultConfig.getTimeout()).isEqualTo(30000);
        assertThat(defaultConfig.getRetryAttempts()).isEqualTo(3);
        assertThat(defaultConfig.getRateLimitMs()).isEqualTo(100);
    }

    @Test
    @DisplayName("Should return configured base URL")
    void getBaseUrl_returnsConfiguredUrl() {
        assertThat(client.getBaseUrl()).isEqualTo("https://www.federalregister.gov/api/v1");
    }

    @Test
    @DisplayName("Config can be customized")
    void config_canBeCustomized() {
        FederalRegisterConfig customConfig = new FederalRegisterConfig();
        customConfig.setBaseUrl("https://custom.api.example.com");
        customConfig.setTimeout(60000);
        customConfig.setRetryAttempts(5);
        customConfig.setRateLimitMs(200);

        FederalRegisterClient customClient = new FederalRegisterClient(customConfig, objectMapper);

        assertThat(customClient.getBaseUrl()).isEqualTo("https://custom.api.example.com");
    }

    @Test
    @DisplayName("FederalRegisterAgency DTO maps JSON fields correctly")
    void federalRegisterAgency_jsonMapping() {
        FederalRegisterAgency agency = new FederalRegisterAgency();
        agency.setId(1);
        agency.setName("Department of Agriculture");
        agency.setShortName("USDA");
        agency.setUrl("https://www.federalregister.gov/agencies/agriculture-department");
        agency.setParentId(null);
        agency.setDescription("The Department of Agriculture provides leadership on food...");
        agency.setSlug("agriculture-department");

        assertThat(agency.getId()).isEqualTo(1);
        assertThat(agency.getName()).isEqualTo("Department of Agriculture");
        assertThat(agency.getShortName()).isEqualTo("USDA");
        assertThat(agency.getUrl()).isEqualTo("https://www.federalregister.gov/agencies/agriculture-department");
        assertThat(agency.getParentId()).isNull();
        assertThat(agency.getDescription()).contains("Department of Agriculture");
        assertThat(agency.getSlug()).isEqualTo("agriculture-department");
    }

    @Test
    @DisplayName("FederalRegisterAgency DTO handles child agency with parentId")
    void federalRegisterAgency_childAgency() {
        FederalRegisterAgency agency = new FederalRegisterAgency();
        agency.setId(2);
        agency.setName("Agricultural Marketing Service");
        agency.setShortName("AMS");
        agency.setParentId(1); // Parent is USDA

        assertThat(agency.getId()).isEqualTo(2);
        assertThat(agency.getParentId()).isEqualTo(1);
        assertThat(agency.getShortName()).isEqualTo("AMS");
    }

    @Test
    @DisplayName("FederalRegisterAgency can be deserialized from JSON")
    void federalRegisterAgency_deserializeJson() throws Exception {
        String json = """
            {
              "id": 1,
              "name": "Department of Agriculture",
              "short_name": "USDA",
              "url": "https://www.federalregister.gov/agencies/agriculture-department",
              "parent_id": null,
              "description": "The Department of Agriculture...",
              "slug": "agriculture-department"
            }
            """;

        FederalRegisterAgency agency = objectMapper.readValue(json, FederalRegisterAgency.class);

        assertThat(agency.getId()).isEqualTo(1);
        assertThat(agency.getName()).isEqualTo("Department of Agriculture");
        assertThat(agency.getShortName()).isEqualTo("USDA");
        assertThat(agency.getUrl()).isEqualTo("https://www.federalregister.gov/agencies/agriculture-department");
        assertThat(agency.getParentId()).isNull();
        assertThat(agency.getSlug()).isEqualTo("agriculture-department");
    }

    @Test
    @DisplayName("FederalRegisterAgency handles null fields gracefully")
    void federalRegisterAgency_nullFields() throws Exception {
        String json = """
            {
              "id": 99,
              "name": "Test Agency"
            }
            """;

        FederalRegisterAgency agency = objectMapper.readValue(json, FederalRegisterAgency.class);

        assertThat(agency.getId()).isEqualTo(99);
        assertThat(agency.getName()).isEqualTo("Test Agency");
        assertThat(agency.getShortName()).isNull();
        assertThat(agency.getParentId()).isNull();
        assertThat(agency.getDescription()).isNull();
    }

    @Test
    @DisplayName("FederalRegisterAgency deserializes array correctly")
    void federalRegisterAgency_deserializeArray() throws Exception {
        String json = """
            [
              {
                "id": 1,
                "name": "Department of Agriculture",
                "short_name": "USDA"
              },
              {
                "id": 2,
                "name": "Agricultural Marketing Service",
                "short_name": "AMS",
                "parent_id": 1
              }
            ]
            """;

        List<FederalRegisterAgency> agencies = objectMapper.readValue(json,
                objectMapper.getTypeFactory().constructCollectionType(List.class, FederalRegisterAgency.class));

        assertThat(agencies).hasSize(2);
        assertThat(agencies.get(0).getName()).isEqualTo("Department of Agriculture");
        assertThat(agencies.get(1).getName()).isEqualTo("Agricultural Marketing Service");
        assertThat(agencies.get(1).getParentId()).isEqualTo(1);
    }
}
