package org.newsanalyzer.service;

import org.newsanalyzer.model.Entity;
import org.newsanalyzer.model.EntityType;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Maps internal EntityType to Schema.org types and generates JSON-LD.
 *
 * This service handles the conversion between:
 * - Internal entity classification (EntityType enum)
 * - Schema.org standardized vocabulary (JSON-LD)
 */
@Service
public class SchemaOrgMapper {

    /**
     * Map EntityType to Schema.org type string
     */
    public String getSchemaOrgType(EntityType entityType) {
        return switch (entityType) {
            case PERSON -> "Person";
            case GOVERNMENT_ORG -> "GovernmentOrganization";
            case ORGANIZATION -> "Organization";
            case LOCATION -> "Place";
            case EVENT -> "Event";
            case CONCEPT -> "Thing";
        };
    }

    /**
     * Generate Schema.org JSON-LD from Entity
     */
    public Map<String, Object> generateJsonLd(Entity entity) {
        Map<String, Object> jsonLd = new HashMap<>();

        // Required JSON-LD fields
        jsonLd.put("@context", "https://schema.org");
        jsonLd.put("@type", entity.getSchemaOrgType());
        jsonLd.put("@id", "https://newsanalyzer.org/entities/" + entity.getId());
        jsonLd.put("name", entity.getName());

        // Add type-specific properties
        switch (entity.getEntityType()) {
            case PERSON -> addPersonProperties(jsonLd, entity);
            case GOVERNMENT_ORG -> addGovernmentOrgProperties(jsonLd, entity);
            case ORGANIZATION -> addOrganizationProperties(jsonLd, entity);
            case LOCATION -> addLocationProperties(jsonLd, entity);
            case EVENT -> addEventProperties(jsonLd, entity);
            case CONCEPT -> addConceptProperties(jsonLd, entity);
        }

        return jsonLd;
    }

    /**
     * Add Person-specific Schema.org properties
     */
    private void addPersonProperties(Map<String, Object> jsonLd, Entity entity) {
        Map<String, Object> props = entity.getProperties();
        if (props == null) return;

        if (props.containsKey("jobTitle")) {
            jsonLd.put("jobTitle", props.get("jobTitle"));
        }
        if (props.containsKey("affiliation")) {
            jsonLd.put("affiliation", props.get("affiliation"));
        }
        if (props.containsKey("url")) {
            jsonLd.put("url", props.get("url"));
        }
        if (props.containsKey("email")) {
            jsonLd.put("email", props.get("email"));
        }
        if (props.containsKey("description")) {
            jsonLd.put("description", props.get("description"));
        }

        // worksFor relationship
        if (props.containsKey("worksFor")) {
            Map<String, Object> worksFor = new HashMap<>();
            worksFor.put("@type", "GovernmentOrganization");
            worksFor.put("name", props.get("worksFor"));
            jsonLd.put("worksFor", worksFor);
        }

        // memberOf relationship (political party)
        if (props.containsKey("politicalParty")) {
            Map<String, Object> memberOf = new HashMap<>();
            memberOf.put("@type", "PoliticalParty");
            memberOf.put("name", props.get("politicalParty"));
            jsonLd.put("memberOf", memberOf);
        }
    }

    /**
     * Add GovernmentOrganization-specific Schema.org properties
     */
    private void addGovernmentOrgProperties(Map<String, Object> jsonLd, Entity entity) {
        Map<String, Object> props = entity.getProperties();
        if (props == null) return;

        if (props.containsKey("url")) {
            jsonLd.put("url", props.get("url"));
        }
        if (props.containsKey("description")) {
            jsonLd.put("description", props.get("description"));
        }
        if (props.containsKey("telephone")) {
            jsonLd.put("telephone", props.get("telephone"));
        }
        if (props.containsKey("email")) {
            jsonLd.put("email", props.get("email"));
        }

        // Parent organization relationship
        if (props.containsKey("parentOrganization")) {
            Map<String, Object> parent = new HashMap<>();
            parent.put("@type", "GovernmentOrganization");
            parent.put("name", props.get("parentOrganization"));
            jsonLd.put("parentOrganization", parent);
        }

        // Address
        if (props.containsKey("address")) {
            jsonLd.put("address", props.get("address"));
        }
    }

    /**
     * Add Organization-specific Schema.org properties
     */
    private void addOrganizationProperties(Map<String, Object> jsonLd, Entity entity) {
        Map<String, Object> props = entity.getProperties();
        if (props == null) return;

        if (props.containsKey("url")) {
            jsonLd.put("url", props.get("url"));
        }
        if (props.containsKey("description")) {
            jsonLd.put("description", props.get("description"));
        }
        if (props.containsKey("foundingDate")) {
            jsonLd.put("foundingDate", props.get("foundingDate"));
        }
        if (props.containsKey("numberOfEmployees")) {
            jsonLd.put("numberOfEmployees", props.get("numberOfEmployees"));
        }
    }

    /**
     * Add Place-specific Schema.org properties
     */
    private void addLocationProperties(Map<String, Object> jsonLd, Entity entity) {
        Map<String, Object> props = entity.getProperties();
        if (props == null) return;

        if (props.containsKey("description")) {
            jsonLd.put("description", props.get("description"));
        }

        // Geo coordinates
        if (props.containsKey("latitude") && props.containsKey("longitude")) {
            Map<String, Object> geo = new HashMap<>();
            geo.put("@type", "GeoCoordinates");
            geo.put("latitude", props.get("latitude"));
            geo.put("longitude", props.get("longitude"));
            jsonLd.put("geo", geo);
        }

        // Address
        if (props.containsKey("address")) {
            jsonLd.put("address", props.get("address"));
        }
    }

    /**
     * Add Event-specific Schema.org properties
     */
    private void addEventProperties(Map<String, Object> jsonLd, Entity entity) {
        Map<String, Object> props = entity.getProperties();
        if (props == null) return;

        if (props.containsKey("description")) {
            jsonLd.put("description", props.get("description"));
        }
        if (props.containsKey("startDate")) {
            jsonLd.put("startDate", props.get("startDate"));
        }
        if (props.containsKey("endDate")) {
            jsonLd.put("endDate", props.get("endDate"));
        }
        if (props.containsKey("location")) {
            jsonLd.put("location", props.get("location"));
        }
        if (props.containsKey("organizer")) {
            jsonLd.put("organizer", props.get("organizer"));
        }
    }

    /**
     * Add Concept/Thing-specific Schema.org properties
     */
    private void addConceptProperties(Map<String, Object> jsonLd, Entity entity) {
        Map<String, Object> props = entity.getProperties();
        if (props == null) return;

        if (props.containsKey("description")) {
            jsonLd.put("description", props.get("description"));
        }
        if (props.containsKey("url")) {
            jsonLd.put("url", props.get("url"));
        }
    }

    /**
     * Enrich existing Schema.org data with additional fields
     */
    public Map<String, Object> enrichSchemaOrgData(Map<String, Object> existingData, Entity entity) {
        if (existingData == null) {
            return generateJsonLd(entity);
        }

        // Ensure required fields are present
        existingData.putIfAbsent("@context", "https://schema.org");
        existingData.putIfAbsent("@type", entity.getSchemaOrgType());
        existingData.putIfAbsent("@id", "https://newsanalyzer.org/entities/" + entity.getId());
        existingData.putIfAbsent("name", entity.getName());

        return existingData;
    }
}
