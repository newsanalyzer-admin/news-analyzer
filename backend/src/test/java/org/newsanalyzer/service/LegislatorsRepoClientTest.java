package org.newsanalyzer.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.newsanalyzer.config.LegislatorsConfig;
import org.newsanalyzer.dto.LegislatorYamlRecord;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * Unit tests for LegislatorsRepoClient.
 *
 * @author James (Dev Agent)
 */
@ExtendWith(MockitoExtension.class)
class LegislatorsRepoClientTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private RestTemplateBuilder restTemplateBuilder;

    private LegislatorsConfig config;
    private ObjectMapper objectMapper;
    private LegislatorsRepoClient client;

    @BeforeEach
    void setUp() {
        config = new LegislatorsConfig();
        config.getGithub().setBaseUrl("https://raw.githubusercontent.com/test/repo/main");
        config.getGithub().setApiUrl("https://api.github.com/repos/test/repo");
        config.getGithub().setCurrentFile("legislators-current.yaml");
        config.getGithub().setTimeout(30000);

        objectMapper = new ObjectMapper();

        // Mock RestTemplateBuilder to return our mock RestTemplate
        when(restTemplateBuilder.setConnectTimeout(Duration.ofMillis(30000))).thenReturn(restTemplateBuilder);
        when(restTemplateBuilder.setReadTimeout(Duration.ofMillis(30000))).thenReturn(restTemplateBuilder);
        when(restTemplateBuilder.build()).thenReturn(restTemplate);

        client = new LegislatorsRepoClient(config, restTemplateBuilder, objectMapper);
    }

    @Test
    @DisplayName("Should parse YAML with all fields")
    void fetchCurrentLegislators_validYaml_parsesCorrectly() {
        // Given
        String yaml = """
            - id:
                bioguide: S000033
                govtrack: 400357
                opensecrets: N00000528
                votesmart: 27110
                fec:
                  - S4VT00033
                  - H8VT01016
                wikipedia: Bernie Sanders
              name:
                first: Bernard
                last: Sanders
              bio:
                birthday: "1941-09-08"
                gender: M
              social:
                twitter: SenSanders
                facebook: senatorsanders
            """;

        when(restTemplate.getForEntity(anyString(), eq(String.class)))
                .thenReturn(new ResponseEntity<>(yaml, HttpStatus.OK));

        // When
        List<LegislatorYamlRecord> records = client.fetchCurrentLegislators();

        // Then
        assertThat(records).hasSize(1);

        LegislatorYamlRecord record = records.get(0);
        assertThat(record.getBioguideId()).isEqualTo("S000033");
        assertThat(record.getId().getGovtrack()).isEqualTo(400357);
        assertThat(record.getId().getOpensecrets()).isEqualTo("N00000528");
        assertThat(record.getId().getVotesmart()).isEqualTo(27110);
        assertThat(record.getId().getFecIds()).containsExactly("S4VT00033", "H8VT01016");
        assertThat(record.getId().getWikipedia()).isEqualTo("Bernie Sanders");
        assertThat(record.getName().getFirst()).isEqualTo("Bernard");
        assertThat(record.getName().getLast()).isEqualTo("Sanders");
        assertThat(record.getBio().getBirthday()).isEqualTo("1941-09-08");
        assertThat(record.getBio().getGender()).isEqualTo("M");
        assertThat(record.getSocial().getTwitter()).isEqualTo("SenSanders");
        assertThat(record.getSocial().getFacebook()).isEqualTo("senatorsanders");
    }

    @Test
    @DisplayName("Should parse YAML with missing optional fields")
    void fetchCurrentLegislators_minimalYaml_parsesCorrectly() {
        // Given
        String yaml = """
            - id:
                bioguide: A000001
              name:
                first: John
                last: Doe
            """;

        when(restTemplate.getForEntity(anyString(), eq(String.class)))
                .thenReturn(new ResponseEntity<>(yaml, HttpStatus.OK));

        // When
        List<LegislatorYamlRecord> records = client.fetchCurrentLegislators();

        // Then
        assertThat(records).hasSize(1);
        assertThat(records.get(0).getBioguideId()).isEqualTo("A000001");
        assertThat(records.get(0).getSocial()).isNull();
        assertThat(records.get(0).getId().getFecIds()).isEmpty();
    }

    @Test
    @DisplayName("Should handle single FEC ID (not array)")
    void fetchCurrentLegislators_singleFecId_handlesCorrectly() {
        // Given
        String yaml = """
            - id:
                bioguide: A000001
                fec: S1TEST001
              name:
                first: John
                last: Doe
            """;

        when(restTemplate.getForEntity(anyString(), eq(String.class)))
                .thenReturn(new ResponseEntity<>(yaml, HttpStatus.OK));

        // When
        List<LegislatorYamlRecord> records = client.fetchCurrentLegislators();

        // Then
        assertThat(records).hasSize(1);
        assertThat(records.get(0).getId().getFecIds()).containsExactly("S1TEST001");
    }

    @Test
    @DisplayName("Should return empty list on HTTP error")
    void fetchCurrentLegislators_httpError_returnsEmptyList() {
        // Given
        when(restTemplate.getForEntity(anyString(), eq(String.class)))
                .thenReturn(new ResponseEntity<>(HttpStatus.NOT_FOUND));

        // When
        List<LegislatorYamlRecord> records = client.fetchCurrentLegislators();

        // Then
        assertThat(records).isEmpty();
    }

    @Test
    @DisplayName("Should fetch latest commit SHA")
    void fetchLatestCommitSha_validResponse_returnsSha() {
        // Given
        String json = """
            {
              "sha": "abc123def456",
              "commit": {"message": "Test commit"}
            }
            """;

        when(restTemplate.getForEntity(anyString(), eq(String.class)))
                .thenReturn(new ResponseEntity<>(json, HttpStatus.OK));

        // When
        Optional<String> sha = client.fetchLatestCommitSha();

        // Then
        assertThat(sha).isPresent();
        assertThat(sha.get()).isEqualTo("abc123def456");
    }

    @Test
    @DisplayName("Should build external IDs map correctly")
    void buildExternalIdsMap_withAllFields_buildsCorrectly() {
        // Given
        String yaml = """
            - id:
                bioguide: S000033
                govtrack: 400357
                opensecrets: N00000528
                votesmart: 27110
                fec:
                  - S4VT00033
                wikipedia: Bernie Sanders
              name:
                first: Bernard
                last: Sanders
            """;

        when(restTemplate.getForEntity(anyString(), eq(String.class)))
                .thenReturn(new ResponseEntity<>(yaml, HttpStatus.OK));

        // When
        List<LegislatorYamlRecord> records = client.fetchCurrentLegislators();
        var externalIds = records.get(0).buildExternalIdsMap();

        // Then
        assertThat(externalIds).containsEntry("govtrack", 400357);
        assertThat(externalIds).containsEntry("opensecrets", "N00000528");
        assertThat(externalIds).containsEntry("votesmart", 27110);
        assertThat(externalIds).containsEntry("wikipedia", "Bernie Sanders");
        assertThat(externalIds).containsKey("fec");
    }

    @Test
    @DisplayName("Should build social media map correctly")
    void buildSocialMediaMap_withAllFields_buildsCorrectly() {
        // Given
        String yaml = """
            - id:
                bioguide: S000033
              name:
                first: Bernard
                last: Sanders
              social:
                twitter: SenSanders
                facebook: senatorsanders
                youtube: senatorbernie
                instagram: sensanders
            """;

        when(restTemplate.getForEntity(anyString(), eq(String.class)))
                .thenReturn(new ResponseEntity<>(yaml, HttpStatus.OK));

        // When
        List<LegislatorYamlRecord> records = client.fetchCurrentLegislators();
        var socialMedia = records.get(0).buildSocialMediaMap();

        // Then
        assertThat(socialMedia).containsEntry("twitter", "SenSanders");
        assertThat(socialMedia).containsEntry("facebook", "senatorsanders");
        assertThat(socialMedia).containsEntry("youtube", "senatorbernie");
        assertThat(socialMedia).containsEntry("instagram", "sensanders");
    }
}
