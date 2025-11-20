package org.newsanalyzer.model;

/**
 * Internal entity classification types.
 *
 * Maps to Schema.org types via SchemaOrgMapper:
 * - PERSON → Person
 * - GOVERNMENT_ORG → GovernmentOrganization
 * - ORGANIZATION → Organization
 * - LOCATION → Place
 * - EVENT → Event
 * - CONCEPT → Thing or CreativeWork
 */
public enum EntityType {
    /**
     * Individual person (politicians, officials, public figures)
     * Schema.org: Person
     */
    PERSON,

    /**
     * Government organization (Congress, Senate, agencies)
     * Schema.org: GovernmentOrganization
     */
    GOVERNMENT_ORG,

    /**
     * Non-government organization (companies, NGOs, political parties)
     * Schema.org: Organization
     */
    ORGANIZATION,

    /**
     * Geographic location (countries, states, cities)
     * Schema.org: Place
     */
    LOCATION,

    /**
     * Event (elections, hearings, protests)
     * Schema.org: Event
     */
    EVENT,

    /**
     * Abstract concept (policies, laws, ideologies)
     * Schema.org: Thing or CreativeWork
     */
    CONCEPT
}
