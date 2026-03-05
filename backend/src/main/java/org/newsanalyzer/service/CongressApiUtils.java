package org.newsanalyzer.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;

import java.util.regex.Pattern;

/**
 * Utility methods for Congress.gov API response handling.
 *
 * @since 2.0.0
 */
public final class CongressApiUtils {

    private static final JsonNode EMPTY_ARRAY = JsonNodeFactory.instance.arrayNode();
    private static final Pattern API_KEY_PATTERN = Pattern.compile("([?&])api_key=[^&]*");

    private CongressApiUtils() {
        // Utility class
    }

    /**
     * Sanitize a URL by removing the api_key query parameter.
     * Prevents API keys from appearing in log files.
     *
     * @param url The URL that may contain an api_key parameter
     * @return The URL with api_key replaced by [REDACTED]
     */
    public static String sanitizeUrl(String url) {
        if (url == null) {
            return "null";
        }
        return API_KEY_PATTERN.matcher(url).replaceAll("$1api_key=[REDACTED]");
    }

    /**
     * Normalize the terms node from a Congress.gov API response.
     *
     * The API returns terms in two formats depending on the endpoint:
     * <ul>
     *   <li>Direct array: {@code "terms": [ {...}, {...} ]}</li>
     *   <li>Object with item array: {@code "terms": { "item": [ {...}, {...} ] }}</li>
     * </ul>
     *
     * This method normalizes both formats to a JsonNode array.
     *
     * @param termsNode The "terms" node from the API response
     * @return A JsonNode array of term objects, or an empty array if no terms found
     */
    public static JsonNode normalizeTermsArray(JsonNode termsNode) {
        if (termsNode == null || termsNode.isMissingNode() || termsNode.isNull()) {
            return EMPTY_ARRAY;
        }
        if (termsNode.isArray()) {
            return termsNode;
        }
        if (termsNode.has("item")) {
            JsonNode items = termsNode.path("item");
            if (items.isArray()) {
                return items;
            }
        }
        return EMPTY_ARRAY;
    }
}
