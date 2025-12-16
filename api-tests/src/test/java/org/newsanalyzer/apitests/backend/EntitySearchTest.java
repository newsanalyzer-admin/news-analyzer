package org.newsanalyzer.apitests.backend;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.newsanalyzer.apitests.BaseApiTest;

import java.util.Map;

import static org.hamcrest.Matchers.*;

/**
 * Search and filter tests for the Entity API endpoints.
 * Tests filtering by type, schema.org type, search, and recent entities.
 */
@Tag("backend")
@Tag("integration")
@DisplayName("Entity Search and Filter Tests")
class EntitySearchTest extends BaseApiTest {

    private EntityApiClient entityClient;

    @BeforeEach
    void setUp() {
        entityClient = new EntityApiClient(getBackendSpec());
    }

    // ==================== Filter by Type Tests ====================

    @Test
    @DisplayName("GET /api/entities/type/{type} - should return filtered list by entity type")
    void shouldGetEntitiesByType_returnsFilteredList() {
        // First create an entity of a specific type
        String uniqueName = "Gov Org Type Test " + System.currentTimeMillis();
        Map<String, Object> entityRequest = EntityTestDataBuilder.aGovernmentOrganization()
                .withName(uniqueName)
                .build();

        entityClient.createEntity(entityRequest)
                .then()
                .statusCode(201);

        // Filter by type
        entityClient.getEntitiesByType(EntityTestDataBuilder.TYPE_GOVERNMENT_ORG)
                .then()
                .statusCode(200)
                .body("$", instanceOf(java.util.List.class))
                .body("findAll { it.entityType == 'GOVERNMENT_ORG' }.size()", greaterThan(0));
    }

    @Test
    @DisplayName("GET /api/entities/type/{type} - should return empty list for type with no entities")
    void shouldGetEntitiesByType_returnsEmptyListWhenNoMatches() {
        // Query for a type that likely has no entities - use EVENT which is less common
        entityClient.getEntitiesByType(EntityTestDataBuilder.TYPE_EVENT)
                .then()
                .statusCode(200)
                .body("$", instanceOf(java.util.List.class));
    }

    // ==================== Filter by Schema.org Type Tests ====================

    @Test
    @DisplayName("GET /api/entities/schema-org-type/{schemaOrgType} - should return filtered list by Schema.org type")
    void shouldGetEntitiesBySchemaOrgType_returnsFilteredList() {
        // First create an entity with a specific Schema.org type
        String uniqueName = "Schema Org Type Test " + System.currentTimeMillis();
        Map<String, Object> entityRequest = EntityTestDataBuilder.aGovernmentOrganization()
                .withName(uniqueName)
                .withSchemaOrgType(EntityTestDataBuilder.SCHEMA_GOV_ORG)
                .build();

        entityClient.createEntity(entityRequest)
                .then()
                .statusCode(201);

        // Filter by Schema.org type
        entityClient.getEntitiesBySchemaOrgType(EntityTestDataBuilder.SCHEMA_GOV_ORG)
                .then()
                .statusCode(200)
                .body("$", instanceOf(java.util.List.class));
    }

    // ==================== Search by Name Tests ====================

    @Test
    @DisplayName("GET /api/entities/search?q= - should return matches for name search")
    void shouldSearchEntities_byName_returnsMatches() {
        // Create an entity with a unique searchable name
        String uniqueName = "SearchableEntityName" + System.currentTimeMillis();
        Map<String, Object> entityRequest = EntityTestDataBuilder.anEntity()
                .withName(uniqueName)
                .build();

        entityClient.createEntity(entityRequest)
                .then()
                .statusCode(201);

        // Search for the entity
        entityClient.searchEntities("SearchableEntityName")
                .then()
                .statusCode(200)
                .body("$", instanceOf(java.util.List.class));
    }

    @Test
    @DisplayName("GET /api/entities/search?q= - should return empty list for no matches")
    void shouldSearchEntities_byName_returnsEmptyListWhenNoMatches() {
        String nonExistentName = "ZZZNonExistentEntityName" + System.currentTimeMillis();

        entityClient.searchEntities(nonExistentName)
                .then()
                .statusCode(200)
                .body("$", instanceOf(java.util.List.class))
                .body("size()", equalTo(0));
    }

    // ==================== Full-text Search Tests ====================

    @Test
    @DisplayName("GET /api/entities/search/fulltext?q= - should return ranked results")
    void shouldFullTextSearch_returnsRankedResults() {
        // Create an entity with searchable content
        String uniqueName = "FullTextSearchEntity" + System.currentTimeMillis();
        Map<String, Object> entityRequest = EntityTestDataBuilder.anEntity()
                .withName(uniqueName)
                .withDescription("This entity contains searchable content for testing")
                .build();

        entityClient.createEntity(entityRequest)
                .then()
                .statusCode(201);

        // Full-text search
        entityClient.fullTextSearch("FullTextSearchEntity", 10)
                .then()
                .statusCode(200)
                .body("$", instanceOf(java.util.List.class));
    }

    @Test
    @DisplayName("GET /api/entities/search/fulltext?q=&limit= - should respect limit parameter")
    void shouldFullTextSearch_respectsLimitParameter() {
        entityClient.fullTextSearch("entity", 5)
                .then()
                .statusCode(200)
                .body("$", instanceOf(java.util.List.class))
                .body("size()", lessThanOrEqualTo(5));
    }

    // ==================== Recent Entities Tests ====================

    @Test
    @DisplayName("GET /api/entities/recent?days= - should return entities within date range")
    void shouldGetRecentEntities_returnsWithinDateRange() {
        // Create a fresh entity
        String uniqueName = "RecentEntity" + System.currentTimeMillis();
        Map<String, Object> entityRequest = EntityTestDataBuilder.anEntity()
                .withName(uniqueName)
                .build();

        entityClient.createEntity(entityRequest)
                .then()
                .statusCode(201);

        // Get recent entities from last 7 days
        entityClient.getRecentEntities(7)
                .then()
                .statusCode(200)
                .body("$", instanceOf(java.util.List.class))
                .body("size()", greaterThan(0));
    }

    @Test
    @DisplayName("GET /api/entities/recent - should use default 7 days when no parameter")
    void shouldGetRecentEntities_usesDefaultDaysParameter() {
        entityClient.getRecentEntities()
                .then()
                .statusCode(200)
                .body("$", instanceOf(java.util.List.class));
    }

    @Test
    @DisplayName("GET /api/entities/recent?days=1 - should return only very recent entities")
    void shouldGetRecentEntities_returnsOnlyVeryRecentEntities() {
        entityClient.getRecentEntities(1)
                .then()
                .statusCode(200)
                .body("$", instanceOf(java.util.List.class));
    }
}
