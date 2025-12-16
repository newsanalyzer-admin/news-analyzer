package org.newsanalyzer.apitests.config;

/**
 * Constants for API endpoints across all services.
 * Centralizes endpoint definitions to avoid hardcoding paths in tests.
 */
public final class Endpoints {

    private Endpoints() {
        // Utility class - prevent instantiation
    }

    // ==================== Backend API Endpoints ====================

    public static final class Backend {
        private Backend() {}

        // Health
        public static final String HEALTH = "/actuator/health";

        // Entities
        public static final String ENTITIES = "/api/entities";
        public static final String ENTITY_BY_ID = "/api/entities/{id}";
        public static final String ENTITIES_VALIDATE = "/api/entities/validate";
        public static final String ENTITY_VALIDATE = "/api/entities/{id}/validate";
        public static final String ENTITY_VERIFY = "/api/entities/{id}/verify";
        public static final String ENTITIES_BY_TYPE = "/api/entities/type/{type}";
        public static final String ENTITIES_BY_SCHEMA_ORG_TYPE = "/api/entities/schema-org-type/{schemaOrgType}";
        public static final String ENTITIES_SEARCH = "/api/entities/search";
        public static final String ENTITIES_SEARCH_FULLTEXT = "/api/entities/search/fulltext";
        public static final String ENTITIES_RECENT = "/api/entities/recent";

        // Government Organizations
        public static final String GOV_ORGS = "/api/government-organizations";
        public static final String GOV_ORG_BY_ID = "/api/government-organizations/{id}";
        public static final String GOV_ORGS_ACTIVE = "/api/government-organizations/active";
        public static final String GOV_ORGS_SEARCH = "/api/government-organizations/search";
        public static final String GOV_ORGS_SEARCH_FUZZY = "/api/government-organizations/search/fuzzy";
        public static final String GOV_ORGS_SEARCH_FULLTEXT = "/api/government-organizations/search/fulltext";
        public static final String GOV_ORGS_FIND = "/api/government-organizations/find";
        public static final String GOV_ORGS_CABINET = "/api/government-organizations/cabinet-departments";
        public static final String GOV_ORGS_INDEPENDENT = "/api/government-organizations/independent-agencies";
        public static final String GOV_ORGS_BY_TYPE = "/api/government-organizations/by-type";
        public static final String GOV_ORGS_BY_BRANCH = "/api/government-organizations/by-branch";
        public static final String GOV_ORGS_BY_JURISDICTION = "/api/government-organizations/by-jurisdiction";
        public static final String GOV_ORG_HIERARCHY = "/api/government-organizations/{id}/hierarchy";
        public static final String GOV_ORG_DESCENDANTS = "/api/government-organizations/{id}/descendants";
        public static final String GOV_ORG_ANCESTORS = "/api/government-organizations/{id}/ancestors";
        public static final String GOV_ORGS_TOP_LEVEL = "/api/government-organizations/top-level";
        public static final String GOV_ORGS_VALIDATE_ENTITY = "/api/government-organizations/validate-entity";
        public static final String GOV_ORGS_STATISTICS = "/api/government-organizations/statistics";

        // Government Organization Sync
        public static final String GOV_ORGS_SYNC_FEDERAL_REGISTER = "/api/government-organizations/sync/federal-register";
        public static final String GOV_ORGS_SYNC_STATUS = "/api/government-organizations/sync/status";
        public static final String GOV_ORGS_IMPORT_CSV = "/api/government-organizations/import/csv";

        // Members (Congressional)
        public static final String MEMBERS = "/api/members";
        public static final String MEMBER_BY_BIOGUIDE_ID = "/api/members/{bioguideId}";
        public static final String MEMBERS_SEARCH = "/api/members/search";
        public static final String MEMBERS_BY_STATE = "/api/members/by-state/{state}";
        public static final String MEMBERS_BY_CHAMBER = "/api/members/by-chamber/{chamber}";
        public static final String MEMBERS_COUNT = "/api/members/count";
        public static final String MEMBERS_STATS_PARTY = "/api/members/stats/party";
        public static final String MEMBERS_STATS_STATE = "/api/members/stats/state";
        public static final String MEMBERS_SYNC = "/api/members/sync";
    }

    // ==================== Reasoning Service Endpoints ====================

    public static final class Reasoning {
        private Reasoning() {}

        // Health
        public static final String ROOT = "/";
        public static final String HEALTH = "/health";

        // Entities
        public static final String ENTITIES_EXTRACT = "/entities/extract";
        public static final String ENTITIES_REASON = "/entities/reason";
        public static final String ENTITIES_LINK = "/entities/link";
        public static final String ENTITIES_LINK_SINGLE = "/entities/link/single";
        public static final String ONTOLOGY_STATS = "/entities/ontology/stats";
        public static final String SPARQL_QUERY = "/entities/query/sparql";

        // Government Organizations
        public static final String GOV_ORGS_INGEST = "/government-orgs/ingest";
        public static final String GOV_ORGS_PROCESS_PACKAGE = "/government-orgs/process-package";
        public static final String GOV_ORGS_FETCH_PACKAGES = "/government-orgs/fetch-packages";
        public static final String GOV_ORGS_ENRICH_ENTITY = "/government-orgs/enrich-entity";
        public static final String GOV_ORGS_HEALTH = "/government-orgs/health";
        public static final String GOV_ORGS_TEST_API_CONNECTION = "/government-orgs/test-api-connection";
    }
}
