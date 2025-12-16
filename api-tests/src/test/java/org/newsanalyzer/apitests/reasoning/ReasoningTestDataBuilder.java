package org.newsanalyzer.apitests.reasoning;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Builder for creating Reasoning Service test data.
 * Provides sample texts, request payloads, and test constants for reasoning API tests.
 */
public class ReasoningTestDataBuilder {

    // ==================== Test Text Constants ====================

    public static final String POLITICAL_TEXT =
            "Senator Elizabeth Warren criticized the EPA's new environmental regulations " +
            "during a Senate hearing on climate change policy.";

    public static final String GOVERNMENT_ORG_TEXT =
            "The Department of Justice announced an investigation into the matter, " +
            "with coordination from the FBI and the Securities and Exchange Commission.";

    public static final String MIXED_ENTITIES_TEXT =
            "President Biden met with German Chancellor Scholz in Washington D.C. " +
            "to discuss NATO defense spending and the ongoing conflict in Ukraine.";

    public static final String PERSON_ONLY_TEXT =
            "Elizabeth Warren and Bernie Sanders introduced new legislation.";

    public static final String LOCATION_TEXT =
            "The conference was held in Washington D.C., with delegates from New York and California.";

    public static final String NO_ENTITIES_TEXT =
            "The quick brown fox jumps over the lazy dog.";

    public static final String EMPTY_TEXT = "";

    // ==================== Entity Extraction Builders ====================

    /**
     * Build an entity extraction request.
     */
    public static Map<String, Object> buildExtractionRequest(String text, double confidenceThreshold) {
        Map<String, Object> request = new HashMap<>();
        request.put("text", text);
        request.put("confidence_threshold", confidenceThreshold);
        return request;
    }

    /**
     * Build an entity extraction request with default confidence.
     */
    public static Map<String, Object> buildExtractionRequest(String text) {
        return buildExtractionRequest(text, 0.7);
    }

    // ==================== Entity Linking Builders ====================

    /**
     * Build a single entity link request.
     */
    public static Map<String, Object> buildSingleLinkRequest(String text, String entityType, String context) {
        Map<String, Object> request = new HashMap<>();
        request.put("text", text);
        request.put("entity_type", entityType);
        if (context != null) {
            request.put("context", context);
        }
        return request;
    }

    /**
     * Build a batch entity link request.
     */
    public static Map<String, Object> buildBatchLinkRequest(List<Map<String, Object>> entities, Map<String, Object> options) {
        Map<String, Object> request = new HashMap<>();
        request.put("entities", entities);
        if (options != null) {
            request.put("options", options);
        }
        return request;
    }

    /**
     * Build linking options.
     */
    public static Map<String, Object> buildLinkingOptions(String sources, double minConfidence, int maxCandidates) {
        Map<String, Object> options = new HashMap<>();
        options.put("sources", sources);
        options.put("min_confidence", minConfidence);
        options.put("max_candidates", maxCandidates);
        return options;
    }

    /**
     * Build a sample EPA entity for linking.
     */
    public static Map<String, Object> buildEpaLinkEntity() {
        return buildSingleLinkRequest(
                "Environmental Protection Agency",
                "government_org",
                "The EPA announced new environmental regulations"
        );
    }

    /**
     * Build a sample person entity for linking.
     */
    public static Map<String, Object> buildPersonLinkEntity() {
        return buildSingleLinkRequest(
                "Elizabeth Warren",
                "person",
                "Senator Elizabeth Warren spoke at the hearing"
        );
    }

    // ==================== OWL Reasoning Builders ====================

    /**
     * Build an entity for reasoning.
     */
    public static Map<String, Object> buildReasoningEntity(String text, String entityType, double confidence, Map<String, Object> properties) {
        Map<String, Object> entity = new HashMap<>();
        entity.put("text", text);
        entity.put("entity_type", entityType);
        entity.put("confidence", confidence);
        if (properties != null) {
            entity.put("properties", properties);
        } else {
            entity.put("properties", new HashMap<>());
        }
        return entity;
    }

    /**
     * Build a reasoning request.
     */
    public static Map<String, Object> buildReasoningRequest(List<Map<String, Object>> entities, boolean enableInference) {
        Map<String, Object> request = new HashMap<>();
        request.put("entities", entities);
        request.put("enable_inference", enableInference);
        return request;
    }

    /**
     * Build a sample EPA reasoning entity.
     */
    public static Map<String, Object> buildEpaReasoningEntity() {
        Map<String, Object> properties = new HashMap<>();
        properties.put("regulates", "environmental_policy");
        return buildReasoningEntity("EPA", "government_org", 0.9, properties);
    }

    // ==================== SPARQL Query Builders ====================

    /**
     * Build a SPARQL query request.
     */
    public static Map<String, Object> buildSparqlRequest(String query) {
        Map<String, Object> request = new HashMap<>();
        request.put("query", query);
        return request;
    }

    /**
     * Sample SPARQL query to find executive agencies.
     */
    public static final String SPARQL_EXECUTIVE_AGENCIES =
            "PREFIX na: <http://newsanalyzer.org/ontology#> " +
            "SELECT ?org WHERE { ?org a na:ExecutiveAgency }";

    /**
     * Sample SPARQL query to count all classes.
     */
    public static final String SPARQL_COUNT_CLASSES =
            "SELECT (COUNT(DISTINCT ?class) AS ?count) WHERE { ?class a owl:Class }";

    /**
     * Invalid SPARQL query for error testing.
     */
    public static final String SPARQL_INVALID =
            "SELEC * FROM WHERE";

    // ==================== Government Organization Builders ====================

    /**
     * Build an ingestion request.
     */
    public static Map<String, Object> buildIngestionRequest(int year, boolean saveToFile, String outputDir) {
        Map<String, Object> request = new HashMap<>();
        request.put("year", year);
        request.put("save_to_file", saveToFile);
        if (outputDir != null) {
            request.put("output_dir", outputDir);
        }
        return request;
    }

    /**
     * Build an ingestion request with defaults.
     */
    public static Map<String, Object> buildIngestionRequest(int year) {
        return buildIngestionRequest(year, false, null);
    }

    /**
     * Build a package process request.
     */
    public static Map<String, Object> buildPackageProcessRequest(String packageId) {
        Map<String, Object> request = new HashMap<>();
        request.put("package_id", packageId);
        return request;
    }

    /**
     * Build an organization enrichment request.
     */
    public static Map<String, Object> buildEnrichmentRequest(String entityText, String entityType, double confidence) {
        Map<String, Object> request = new HashMap<>();
        request.put("entity_text", entityText);
        request.put("entity_type", entityType);
        request.put("confidence", confidence);
        request.put("properties", new HashMap<>());
        return request;
    }

    /**
     * Build a sample EPA enrichment request.
     */
    public static Map<String, Object> buildEpaEnrichmentRequest() {
        return buildEnrichmentRequest("EPA", "government_org", 0.95);
    }

    // ==================== Test Constants ====================

    public static final int VALID_YEAR = 2024;
    public static final int INVALID_YEAR_LOW = 1990;
    public static final int INVALID_YEAR_HIGH = 2050;
    public static final String SAMPLE_PACKAGE_ID = "GOVMAN-2024-12-01";
    public static final double DEFAULT_CONFIDENCE = 0.7;
    public static final double HIGH_CONFIDENCE = 0.9;
    public static final double LOW_CONFIDENCE = 0.3;
    public static final double INVALID_CONFIDENCE_NEGATIVE = -0.1;
    public static final double INVALID_CONFIDENCE_OVER = 1.5;
}
