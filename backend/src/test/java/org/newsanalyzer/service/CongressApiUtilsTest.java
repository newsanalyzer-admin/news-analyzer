package org.newsanalyzer.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.MissingNode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for CongressApiUtils.
 */
class CongressApiUtilsTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    @DisplayName("normalizeTermsArray - handles direct array format")
    void normalizeTermsArray_directArray_returnsArray() throws Exception {
        JsonNode terms = mapper.readTree("""
            [
              {"chamber": "Senate", "startYear": 2023},
              {"chamber": "House", "startYear": 2019}
            ]
            """);

        JsonNode result = CongressApiUtils.normalizeTermsArray(terms);

        assertThat(result.isArray()).isTrue();
        assertThat(result.size()).isEqualTo(2);
        assertThat(result.get(0).path("chamber").asText()).isEqualTo("Senate");
    }

    @Test
    @DisplayName("normalizeTermsArray - handles terms.item object format")
    void normalizeTermsArray_itemFormat_returnsItemArray() throws Exception {
        JsonNode terms = mapper.readTree("""
            {
              "item": [
                {"chamber": "Senate", "startYear": 2023}
              ]
            }
            """);

        JsonNode result = CongressApiUtils.normalizeTermsArray(terms);

        assertThat(result.isArray()).isTrue();
        assertThat(result.size()).isEqualTo(1);
        assertThat(result.get(0).path("chamber").asText()).isEqualTo("Senate");
    }

    @Test
    @DisplayName("normalizeTermsArray - handles null input")
    void normalizeTermsArray_null_returnsEmptyArray() {
        JsonNode result = CongressApiUtils.normalizeTermsArray(null);

        assertThat(result.isArray()).isTrue();
        assertThat(result.size()).isEqualTo(0);
    }

    @Test
    @DisplayName("normalizeTermsArray - handles missing node")
    void normalizeTermsArray_missingNode_returnsEmptyArray() {
        JsonNode result = CongressApiUtils.normalizeTermsArray(MissingNode.getInstance());

        assertThat(result.isArray()).isTrue();
        assertThat(result.size()).isEqualTo(0);
    }

    @Test
    @DisplayName("normalizeTermsArray - handles empty object")
    void normalizeTermsArray_emptyObject_returnsEmptyArray() throws Exception {
        JsonNode terms = mapper.readTree("{}");

        JsonNode result = CongressApiUtils.normalizeTermsArray(terms);

        assertThat(result.isArray()).isTrue();
        assertThat(result.size()).isEqualTo(0);
    }

    @Test
    @DisplayName("normalizeTermsArray - handles null JSON node")
    void normalizeTermsArray_nullNode_returnsEmptyArray() throws Exception {
        JsonNode terms = mapper.readTree("null");

        JsonNode result = CongressApiUtils.normalizeTermsArray(terms);

        assertThat(result.isArray()).isTrue();
        assertThat(result.size()).isEqualTo(0);
    }

    // =====================================================================
    // sanitizeUrl Tests (STAB-1.8 AC4)
    // =====================================================================

    @Test
    @DisplayName("sanitizeUrl - redacts api_key parameter")
    void sanitizeUrl_redactsApiKey() {
        String url = "https://api.congress.gov/v3/member?limit=250&api_key=SECRET123&offset=0";

        String result = CongressApiUtils.sanitizeUrl(url);

        assertThat(result).contains("api_key=[REDACTED]");
        assertThat(result).doesNotContain("SECRET123");
        assertThat(result).contains("limit=250");
        assertThat(result).contains("offset=0");
    }

    @Test
    @DisplayName("sanitizeUrl - handles api_key as first parameter")
    void sanitizeUrl_apiKeyFirst() {
        String url = "https://api.congress.gov/v3/member?api_key=SECRET123&limit=250";

        String result = CongressApiUtils.sanitizeUrl(url);

        assertThat(result).contains("api_key=[REDACTED]");
        assertThat(result).doesNotContain("SECRET123");
    }

    @Test
    @DisplayName("sanitizeUrl - handles URL without api_key")
    void sanitizeUrl_noApiKey() {
        String url = "https://api.example.com/data?limit=10";

        String result = CongressApiUtils.sanitizeUrl(url);

        assertThat(result).isEqualTo(url);
    }

    @Test
    @DisplayName("sanitizeUrl - handles null URL")
    void sanitizeUrl_null_returnsNull() {
        assertThat(CongressApiUtils.sanitizeUrl(null)).isEqualTo("null");
    }
}
