package org.newsanalyzer.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.newsanalyzer.config.FederalRegisterConfig;
import org.newsanalyzer.dto.FederalRegisterAgency;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.web.client.RestTemplate;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

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
        client = new FederalRegisterClient(config, objectMapper, new RestTemplateBuilder());
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

        FederalRegisterClient customClient = new FederalRegisterClient(customConfig, objectMapper, new RestTemplateBuilder());

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
    @DisplayName("FederalRegisterAgency handles logo as nested object")
    void federalRegisterAgency_logoAsObject() throws Exception {
        String json = """
            {
              "id": 10,
              "name": "Consumer Financial Protection Bureau",
              "short_name": "CFPB",
              "slug": "consumer-financial-protection-bureau",
              "logo": {
                "thumb_url": "https://example.com/thumb.png",
                "small_url": "https://example.com/small.png",
                "medium_url": "https://example.com/medium.png"
              }
            }
            """;

        FederalRegisterAgency agency = objectMapper.readValue(json, FederalRegisterAgency.class);

        assertThat(agency.getId()).isEqualTo(10);
        assertThat(agency.getName()).isEqualTo("Consumer Financial Protection Bureau");
        assertThat(agency.getLogo()).isNotNull();
    }

    @Test
    @DisplayName("FederalRegisterAgency handles logo as null")
    void federalRegisterAgency_logoAsNull() throws Exception {
        String json = """
            {
              "id": 1,
              "name": "Department of Agriculture",
              "logo": null
            }
            """;

        FederalRegisterAgency agency = objectMapper.readValue(json, FederalRegisterAgency.class);

        assertThat(agency.getLogo()).isNull();
    }

    @Test
    @DisplayName("FederalRegisterAgency ignores unknown fields from live API")
    void federalRegisterAgency_ignoresUnknownFields() throws Exception {
        String json = """
            {
              "id": 1,
              "name": "Department of Agriculture",
              "short_name": "USDA",
              "url": "https://www.usda.gov",
              "parent_id": null,
              "slug": "agriculture-department",
              "logo": null,
              "agency_url": "",
              "child_ids": [],
              "child_slugs": [],
              "recent_articles_url": "https://www.federalregister.gov/api/v1/agencies/agriculture-department/recent_articles.json",
              "json_url": "https://www.federalregister.gov/api/v1/agencies/1.json"
            }
            """;

        FederalRegisterAgency agency = objectMapper.readValue(json, FederalRegisterAgency.class);

        assertThat(agency.getId()).isEqualTo(1);
        assertThat(agency.getName()).isEqualTo("Department of Agriculture");
        assertThat(agency.getShortName()).isEqualTo("USDA");
    }

    @Test
    @DisplayName("FederalRegisterAgency array with mixed logo types deserializes correctly")
    void federalRegisterAgency_arrayWithMixedLogos() throws Exception {
        String json = """
            [
              {
                "id": 1,
                "name": "Agency Without Logo",
                "short_name": "AWL",
                "logo": null,
                "recent_articles_url": "https://example.com/recent"
              },
              {
                "id": 2,
                "name": "Agency With Logo",
                "short_name": "AWL2",
                "logo": {"thumb_url": "https://example.com/thumb.png", "small_url": "https://example.com/small.png", "medium_url": "https://example.com/medium.png"},
                "child_ids": [3, 4],
                "child_slugs": ["sub-agency-a", "sub-agency-b"]
              }
            ]
            """;

        List<FederalRegisterAgency> agencies = objectMapper.readValue(json,
                objectMapper.getTypeFactory().constructCollectionType(List.class, FederalRegisterAgency.class));

        assertThat(agencies).hasSize(2);
        assertThat(agencies.get(0).getLogo()).isNull();
        assertThat(agencies.get(1).getLogo()).isNotNull();
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

    // =====================================================================
    // Error Differentiation Tests (STAB-1.7 AC2, AC5)
    // =====================================================================

    @Test
    @DisplayName("fetchAllAgencies returns empty list on invalid JSON (parse error)")
    void fetchAllAgencies_invalidJson_returnsEmptyList() throws Exception {
        // Given - a client with a mocked RestTemplate returning invalid JSON
        FederalRegisterClient mockedClient = createClientWithMockedRestTemplate("not valid json {{{");

        // When
        List<FederalRegisterAgency> result = mockedClient.fetchAllAgencies();

        // Then - returns empty list (doesn't throw)
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("fetchDocument throws FederalRegisterParseException on invalid JSON")
    void fetchDocument_invalidJson_throwsParseException() throws Exception {
        // Given
        FederalRegisterClient mockedClient = createClientWithMockedRestTemplate("not valid json {{{");

        // When / Then
        assertThatThrownBy(() -> mockedClient.fetchDocument("2024-12345"))
                .isInstanceOf(FederalRegisterParseException.class)
                .hasMessageContaining("Failed to parse document 2024-12345");
    }

    @Test
    @DisplayName("fetchDocument returns empty Optional when API returns no response")
    void fetchDocument_noResponse_returnsEmpty() throws Exception {
        // Given - RestTemplate returns null (simulating 404/no response after retries)
        FederalRegisterClient mockedClient = createClientWithMockedRestTemplate(null);

        // When
        Optional<FederalRegisterAgency> result = mockedClient.fetchAgency("nonexistent-agency");

        // Then
        assertThat(result).isEmpty();
    }

    // =====================================================================
    // HTTP Status-Aware Retry Tests (STAB-1.8 AC1, AC3, AC5)
    // =====================================================================

    @Test
    @DisplayName("404 error is not retried — returns empty immediately")
    void fetchAgency_404_notRetried() throws Exception {
        // Given
        FederalRegisterClient mockedClient = createClientWithThrowingRestTemplate(
                HttpClientErrorException.create(HttpStatus.NOT_FOUND, "Not Found", null, null, null));

        // When
        Optional<FederalRegisterAgency> result = mockedClient.fetchAgency("nonexistent");

        // Then
        assertThat(result).isEmpty();
        // Verify only 1 call was made (no retries)
        RestTemplate mockRt = getMockedRestTemplate(mockedClient);
        verify(mockRt, times(1)).getForObject(anyString(), eq(String.class));
    }

    @Test
    @DisplayName("500 error is retried")
    void fetchAgency_500_isRetried() throws Exception {
        // Given - always throws 500
        FederalRegisterClient mockedClient = createClientWithThrowingRestTemplate(
                HttpServerErrorException.create(HttpStatus.INTERNAL_SERVER_ERROR, "Server Error", null, null, null));

        // When
        Optional<FederalRegisterAgency> result = mockedClient.fetchAgency("some-agency");

        // Then
        assertThat(result).isEmpty();
        // Verify retries happened (retryAttempts = 2)
        RestTemplate mockRt = getMockedRestTemplate(mockedClient);
        verify(mockRt, times(2)).getForObject(anyString(), eq(String.class));
    }

    // =====================================================================
    // Helper: create client with mocked RestTemplate
    // =====================================================================

    private FederalRegisterClient createClientWithMockedRestTemplate(String responseBody) throws Exception {
        FederalRegisterConfig testConfig = createTestConfig(1);

        FederalRegisterClient testClient = new FederalRegisterClient(
                testConfig, objectMapper, new RestTemplateBuilder());

        RestTemplate mockRestTemplate = mock(RestTemplate.class);
        when(mockRestTemplate.getForObject(anyString(), eq(String.class)))
                .thenReturn(responseBody);

        setRestTemplate(testClient, mockRestTemplate);
        return testClient;
    }

    private FederalRegisterClient createClientWithThrowingRestTemplate(Exception exception) throws Exception {
        FederalRegisterConfig testConfig = createTestConfig(2); // 2 retries to verify retry behavior

        FederalRegisterClient testClient = new FederalRegisterClient(
                testConfig, objectMapper, new RestTemplateBuilder());

        RestTemplate mockRestTemplate = mock(RestTemplate.class);
        when(mockRestTemplate.getForObject(anyString(), eq(String.class)))
                .thenThrow(exception);

        setRestTemplate(testClient, mockRestTemplate);
        return testClient;
    }

    private FederalRegisterConfig createTestConfig(int retryAttempts) {
        FederalRegisterConfig testConfig = new FederalRegisterConfig();
        testConfig.setBaseUrl("https://test.api.example.com/api/v1");
        testConfig.setTimeout(1000);
        testConfig.setRetryAttempts(retryAttempts);
        testConfig.setRateLimitMs(0);
        return testConfig;
    }

    private void setRestTemplate(FederalRegisterClient client, RestTemplate restTemplate) throws Exception {
        Field restTemplateField = FederalRegisterClient.class.getDeclaredField("restTemplate");
        restTemplateField.setAccessible(true);
        restTemplateField.set(client, restTemplate);
    }

    private RestTemplate getMockedRestTemplate(FederalRegisterClient client) throws Exception {
        Field restTemplateField = FederalRegisterClient.class.getDeclaredField("restTemplate");
        restTemplateField.setAccessible(true);
        return (RestTemplate) restTemplateField.get(client);
    }
}
