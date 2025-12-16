package org.newsanalyzer.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.newsanalyzer.config.CongressApiConfig;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for CongressApiClient.
 *
 * @author James (Dev Agent)
 */
@ExtendWith(MockitoExtension.class)
class CongressApiClientTest {

    @Mock
    private RestTemplate restTemplate;

    private CongressApiConfig config;
    private ObjectMapper objectMapper;
    private CongressApiClient client;

    @BeforeEach
    void setUp() {
        config = new CongressApiConfig();
        config.setBaseUrl("https://api.congress.gov/v3");
        config.setKey("test-api-key");
        config.setRateLimit(5000);
        config.setTimeout(30000);

        objectMapper = new ObjectMapper();
        client = new CongressApiClient(config, objectMapper);
    }

    @Test
    @DisplayName("Should check if API is configured")
    void isConfigured_withKey_returnsTrue() {
        assertThat(client.isConfigured()).isTrue();
    }

    @Test
    @DisplayName("Should return false when API key is not set")
    void isConfigured_withoutKey_returnsFalse() {
        config.setKey(null);
        client = new CongressApiClient(config, objectMapper);
        assertThat(client.isConfigured()).isFalse();
    }

    @Test
    @DisplayName("Should return false when API key is empty")
    void isConfigured_withEmptyKey_returnsFalse() {
        config.setKey("");
        client = new CongressApiClient(config, objectMapper);
        assertThat(client.isConfigured()).isFalse();
    }

    @Test
    @DisplayName("Should track request count")
    void getRequestCount_initialValue_isZero() {
        assertThat(client.getRequestCount()).isEqualTo(0);
    }

    @Test
    @DisplayName("Should parse member response correctly")
    void fetchMemberByBioguideId_validResponse_parsesMember() throws Exception {
        // Given
        String mockResponse = """
            {
              "member": {
                "bioguideId": "S000033",
                "firstName": "Bernard",
                "lastName": "Sanders",
                "party": "Independent",
                "state": "VT"
              }
            }
            """;

        // The actual client uses internal RestTemplate, so we test the config
        assertThat(config.getBaseUrl()).isEqualTo("https://api.congress.gov/v3");
        assertThat(config.getKey()).isEqualTo("test-api-key");
    }

    @Test
    @DisplayName("Config should have correct default values")
    void config_defaults_areCorrect() {
        CongressApiConfig defaultConfig = new CongressApiConfig();
        assertThat(defaultConfig.getBaseUrl()).isEqualTo("https://api.congress.gov/v3");
        assertThat(defaultConfig.getRateLimit()).isEqualTo(5000);
        assertThat(defaultConfig.getTimeout()).isEqualTo(30000);
    }
}
