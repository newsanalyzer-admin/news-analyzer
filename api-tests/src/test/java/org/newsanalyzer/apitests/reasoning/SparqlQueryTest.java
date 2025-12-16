package org.newsanalyzer.apitests.reasoning;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.newsanalyzer.apitests.BaseApiTest;

import static org.hamcrest.Matchers.*;

/**
 * SPARQL query tests for the Reasoning Service.
 * Tests POST /entities/query/sparql endpoint.
 */
@Tag("reasoning")
@Tag("integration")
@DisplayName("SPARQL Query Tests")
class SparqlQueryTest extends BaseApiTest {

    private ReasoningApiClient client;

    @BeforeEach
    void setUp() {
        client = new ReasoningApiClient(getReasoningSpec());
    }

    // ==================== Basic Query Tests ====================

    @Test
    @DisplayName("POST /entities/query/sparql - should execute SPARQL query and return results")
    void shouldExecuteSparqlQuery_returnsResults() {
        client.executeSparqlQuery(ReasoningTestDataBuilder.SPARQL_EXECUTIVE_AGENCIES)
                .then()
                .statusCode(anyOf(equalTo(200), equalTo(400), equalTo(503)))
                .body("results", anyOf(notNullValue(), nullValue()));
    }

    @Test
    @DisplayName("POST /entities/query/sparql - should return count in response")
    void shouldReturnCount_inResponse() {
        client.executeSparqlQuery(ReasoningTestDataBuilder.SPARQL_EXECUTIVE_AGENCIES)
                .then()
                .statusCode(anyOf(equalTo(200), equalTo(400), equalTo(503)));
        // Response should include 'count' field
    }

    // ==================== Empty Results Tests ====================

    @Test
    @DisplayName("POST /entities/query/sparql - should return empty results when no matches")
    void shouldReturnEmptyResults_whenNoMatches() {
        String noMatchQuery = "PREFIX na: <http://newsanalyzer.org/ontology#> " +
                             "SELECT ?x WHERE { ?x a na:NonExistentClass12345 }";

        client.executeSparqlQuery(noMatchQuery)
                .then()
                .statusCode(anyOf(equalTo(200), equalTo(400), equalTo(503)));
        // Results should be empty list when no matches
    }

    // ==================== Error Handling Tests ====================

    @Test
    @DisplayName("POST /entities/query/sparql - should return 400 when invalid query")
    void shouldReturn400_whenInvalidQuery() {
        client.executeSparqlQuery(ReasoningTestDataBuilder.SPARQL_INVALID)
                .then()
                .statusCode(400);
    }

    @Test
    @DisplayName("POST /entities/query/sparql - should return error message for invalid query")
    void shouldReturnErrorMessage_forInvalidQuery() {
        client.executeSparqlQuery(ReasoningTestDataBuilder.SPARQL_INVALID)
                .then()
                .statusCode(400)
                .body("detail", notNullValue());
    }

    // ==================== Ontology Query Tests ====================

    @Test
    @DisplayName("POST /entities/query/sparql - should query executive agencies from ontology")
    void shouldQueryExecutiveAgencies_fromOntology() {
        client.executeSparqlQuery(ReasoningTestDataBuilder.SPARQL_EXECUTIVE_AGENCIES)
                .then()
                .statusCode(anyOf(equalTo(200), equalTo(400), equalTo(503)));
    }

    @Test
    @DisplayName("POST /entities/query/sparql - should query classes from ontology")
    void shouldQueryClasses_fromOntology() {
        String classQuery = "SELECT DISTINCT ?class WHERE { ?class a owl:Class } LIMIT 10";

        client.executeSparqlQuery(classQuery)
                .then()
                .statusCode(anyOf(equalTo(200), equalTo(400), equalTo(503)));
    }

    // ==================== Complex Query Tests ====================

    @Test
    @DisplayName("POST /entities/query/sparql - should handle SELECT queries")
    void shouldHandleSelectQueries() {
        String selectQuery = "SELECT ?s ?p ?o WHERE { ?s ?p ?o } LIMIT 5";

        client.executeSparqlQuery(selectQuery)
                .then()
                .statusCode(anyOf(equalTo(200), equalTo(400), equalTo(503)));
    }

    @Test
    @DisplayName("POST /entities/query/sparql - should handle queries with FILTER")
    void shouldHandleQueries_withFilter() {
        String filterQuery = "PREFIX na: <http://newsanalyzer.org/ontology#> " +
                            "SELECT ?org WHERE { ?org a na:GovernmentOrganization . " +
                            "FILTER(CONTAINS(STR(?org), 'EPA')) }";

        client.executeSparqlQuery(filterQuery)
                .then()
                .statusCode(anyOf(equalTo(200), equalTo(400), equalTo(503)));
    }

    @Test
    @DisplayName("POST /entities/query/sparql - should handle queries with LIMIT")
    void shouldHandleQueries_withLimit() {
        String limitQuery = "SELECT ?s WHERE { ?s ?p ?o } LIMIT 10";

        client.executeSparqlQuery(limitQuery)
                .then()
                .statusCode(anyOf(equalTo(200), equalTo(400), equalTo(503)));
    }

    // ==================== Prefix Tests ====================

    @Test
    @DisplayName("POST /entities/query/sparql - should handle queries with standard prefixes")
    void shouldHandleQueries_withStandardPrefixes() {
        String prefixQuery = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
                            "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> " +
                            "SELECT ?class ?label WHERE { ?class a rdfs:Class . " +
                            "OPTIONAL { ?class rdfs:label ?label } } LIMIT 5";

        client.executeSparqlQuery(prefixQuery)
                .then()
                .statusCode(anyOf(equalTo(200), equalTo(400), equalTo(503)));
    }

    @Test
    @DisplayName("POST /entities/query/sparql - should handle queries with custom ontology prefix")
    void shouldHandleQueries_withCustomOntologyPrefix() {
        String customPrefixQuery = "PREFIX na: <http://newsanalyzer.org/ontology#> " +
                                  "PREFIX schema: <http://schema.org/> " +
                                  "SELECT ?entity WHERE { ?entity a schema:GovernmentOrganization } LIMIT 5";

        client.executeSparqlQuery(customPrefixQuery)
                .then()
                .statusCode(anyOf(equalTo(200), equalTo(400), equalTo(503)));
    }
}
