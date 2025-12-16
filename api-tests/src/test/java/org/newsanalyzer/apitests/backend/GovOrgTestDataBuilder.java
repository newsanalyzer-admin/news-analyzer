package org.newsanalyzer.apitests.backend;

import org.newsanalyzer.apitests.util.DatabaseConnectionManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Builder for creating Government Organization test data.
 * Uses builder pattern to construct organization request payloads for testing.
 * Supports both API payload generation and direct database persistence.
 */
public class GovOrgTestDataBuilder {

    // Test data constants
    public static final UUID TEST_GOV_ORG_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    public static final UUID NON_EXISTENT_ID = UUID.fromString("99999999-9999-9999-9999-999999999999");

    // Organization types matching backend OrganizationType enum (lowercase values)
    public static final String TYPE_BRANCH = "branch";
    public static final String TYPE_DEPARTMENT = "department";
    public static final String TYPE_INDEPENDENT_AGENCY = "independent_agency";
    public static final String TYPE_BUREAU = "bureau";
    public static final String TYPE_OFFICE = "office";
    public static final String TYPE_COMMISSION = "commission";
    public static final String TYPE_BOARD = "board";
    // Legacy alias for backward compatibility
    public static final String TYPE_AGENCY = "independent_agency";

    // Government branches matching backend GovernmentBranch enum (lowercase values)
    public static final String BRANCH_EXECUTIVE = "executive";
    public static final String BRANCH_LEGISLATIVE = "legislative";
    public static final String BRANCH_JUDICIAL = "judicial";

    // Jurisdictions
    public static final String JURISDICTION_FEDERAL = "FEDERAL";
    public static final String JURISDICTION_STATE = "STATE";
    public static final String JURISDICTION_LOCAL = "LOCAL";

    private UUID id;
    private String officialName;
    private String acronym;
    private String orgType;
    private String branch;
    private Integer orgLevel;
    private String missionStatement;
    private String description;
    private String websiteUrl;
    private UUID parentId;

    public GovOrgTestDataBuilder() {
        // Default values - only required fields
        this.officialName = "Test Organization";
        this.orgType = TYPE_INDEPENDENT_AGENCY;
        this.branch = BRANCH_EXECUTIVE;
        // Note: orgLevel is optional, don't set by default
    }

    public static GovOrgTestDataBuilder aGovOrg() {
        return new GovOrgTestDataBuilder();
    }

    public static GovOrgTestDataBuilder aCabinetDepartment() {
        return new GovOrgTestDataBuilder()
                .withOrgType(TYPE_DEPARTMENT)
                .withBranch(BRANCH_EXECUTIVE)
                .withOrgLevel(1);
    }

    public static GovOrgTestDataBuilder anIndependentAgency() {
        return new GovOrgTestDataBuilder()
                .withOrgType(TYPE_INDEPENDENT_AGENCY)
                .withBranch(BRANCH_EXECUTIVE);
    }

    public static GovOrgTestDataBuilder aBureau() {
        return new GovOrgTestDataBuilder()
                .withOrgType(TYPE_BUREAU)
                .withBranch(BRANCH_EXECUTIVE)
                .withOrgLevel(2);
    }

    public GovOrgTestDataBuilder withOfficialName(String officialName) {
        this.officialName = officialName;
        return this;
    }

    public GovOrgTestDataBuilder withAcronym(String acronym) {
        this.acronym = acronym;
        return this;
    }

    public GovOrgTestDataBuilder withOrgType(String orgType) {
        this.orgType = orgType;
        return this;
    }

    // Alias for backward compatibility
    public GovOrgTestDataBuilder withOrganizationType(String organizationType) {
        this.orgType = organizationType;
        return this;
    }

    public GovOrgTestDataBuilder withBranch(String branch) {
        this.branch = branch;
        return this;
    }

    // Alias for backward compatibility
    public GovOrgTestDataBuilder withGovernmentBranch(String governmentBranch) {
        this.branch = governmentBranch;
        return this;
    }

    public GovOrgTestDataBuilder withOrgLevel(Integer orgLevel) {
        this.orgLevel = orgLevel;
        return this;
    }

    public GovOrgTestDataBuilder withMission(String mission) {
        this.missionStatement = mission;
        return this;
    }

    public GovOrgTestDataBuilder withDescription(String description) {
        this.description = description;
        return this;
    }

    public GovOrgTestDataBuilder withWebsiteUrl(String websiteUrl) {
        this.websiteUrl = websiteUrl;
        return this;
    }

    public GovOrgTestDataBuilder withParentId(UUID parentId) {
        this.parentId = parentId;
        return this;
    }

    // Alias for backward compatibility
    public GovOrgTestDataBuilder withParentOrganizationId(UUID parentOrganizationId) {
        this.parentId = parentOrganizationId;
        return this;
    }

    public GovOrgTestDataBuilder withId(UUID id) {
        this.id = id;
        return this;
    }

    /**
     * Builds the government organization request as a Map suitable for JSON serialization.
     */
    public Map<String, Object> build() {
        Map<String, Object> govOrg = new HashMap<>();
        govOrg.put("officialName", officialName);
        govOrg.put("orgType", orgType);
        govOrg.put("branch", branch);

        if (acronym != null) {
            govOrg.put("acronym", acronym);
        }
        if (orgLevel != null) {
            govOrg.put("orgLevel", orgLevel);
        }
        if (missionStatement != null) {
            govOrg.put("missionStatement", missionStatement);
        }
        if (description != null) {
            govOrg.put("description", description);
        }
        if (websiteUrl != null) {
            govOrg.put("websiteUrl", websiteUrl);
        }
        if (parentId != null) {
            govOrg.put("parentId", parentId.toString());
        }

        return govOrg;
    }

    /**
     * Builds an invalid government organization request (missing required fields).
     */
    public static Map<String, Object> buildInvalidGovOrg() {
        Map<String, Object> govOrg = new HashMap<>();
        // Missing required 'officialName' field
        govOrg.put("orgType", TYPE_INDEPENDENT_AGENCY);
        return govOrg;
    }

    // ==================== Sample Organizations ====================

    public static Map<String, Object> buildEpa() {
        return anIndependentAgency()
                .withOfficialName("Environmental Protection Agency")
                .withAcronym("EPA")
                .withMission("Protect human health and the environment")
                .withWebsiteUrl("https://www.epa.gov")
                .build();
    }

    public static Map<String, Object> buildNasa() {
        return anIndependentAgency()
                .withOfficialName("National Aeronautics and Space Administration")
                .withAcronym("NASA")
                .withMission("Drive advances in science, technology, aeronautics, and space exploration")
                .withWebsiteUrl("https://www.nasa.gov")
                .build();
    }

    public static Map<String, Object> buildDoj() {
        return aCabinetDepartment()
                .withOfficialName("Department of Justice")
                .withAcronym("DOJ")
                .withMission("Ensure fair and impartial administration of justice")
                .withWebsiteUrl("https://www.justice.gov")
                .build();
    }

    public static Map<String, Object> buildDod() {
        return aCabinetDepartment()
                .withOfficialName("Department of Defense")
                .withAcronym("DOD")
                .withMission("Provide the military forces needed to deter war and ensure national security")
                .withWebsiteUrl("https://www.defense.gov")
                .build();
    }

    public static Map<String, Object> buildFbi() {
        return aBureau()
                .withOfficialName("Federal Bureau of Investigation")
                .withAcronym("FBI")
                .withMission("Protect the American people and uphold the Constitution")
                .withWebsiteUrl("https://www.fbi.gov")
                .build();
    }

    public static Map<String, Object> buildLegislativeBranch() {
        return aGovOrg()
                .withOfficialName("United States Congress")
                .withOrgType(TYPE_BRANCH)  // Use TYPE_BRANCH for top-level branches
                .withBranch(BRANCH_LEGISLATIVE)
                .withMission("Legislative branch of the federal government")
                .build();
    }

    public static Map<String, Object> buildJudicialBranch() {
        return aGovOrg()
                .withOfficialName("Supreme Court of the United States")
                .withAcronym("SCOTUS")
                .withOrganizationType(TYPE_OFFICE)
                .withGovernmentBranch(BRANCH_JUDICIAL)
                .withMission("Final arbiter of the law")
                .build();
    }

    // ==================== Database Persistence Methods ====================

    /**
     * Persist the government organization directly to the database.
     * Returns the generated UUID.
     */
    public UUID persistToDatabase() throws SQLException {
        UUID govOrgId = this.id != null ? this.id : UUID.randomUUID();
        DatabaseConnectionManager connectionManager = DatabaseConnectionManager.getInstance();

        connectionManager.executeInTransaction(connection -> {
            persistToDatabase(connection, govOrgId);
        });

        return govOrgId;
    }

    /**
     * Persist the government organization to the database using an existing connection.
     * Useful for transaction control.
     */
    public void persistToDatabase(Connection connection, UUID govOrgId) throws SQLException {
        String sql = "INSERT INTO government_organizations " +
                "(id, official_name, acronym, org_type, branch, parent_id, org_level, website_url, mission_statement, created_at, updated_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            Timestamp now = Timestamp.from(Instant.now());

            stmt.setObject(1, govOrgId);
            stmt.setString(2, officialName);
            stmt.setString(3, acronym);
            stmt.setString(4, orgType);
            stmt.setString(5, branch);
            stmt.setObject(6, parentId);
            stmt.setObject(7, orgLevel);
            stmt.setString(8, websiteUrl);
            stmt.setString(9, missionStatement);
            stmt.setTimestamp(10, now);
            stmt.setTimestamp(11, now);

            stmt.executeUpdate();
        }
    }

    /**
     * Build and persist in one step.
     * Returns the organization ID.
     */
    public UUID buildAndPersist() throws SQLException {
        return persistToDatabase();
    }

    /**
     * Build and persist with a specific ID.
     */
    public UUID buildAndPersist(UUID id) throws SQLException {
        this.id = id;
        return persistToDatabase();
    }

    // ==================== Getters for Verification ====================

    public UUID getId() {
        return id;
    }

    public String getOfficialName() {
        return officialName;
    }

    public String getAcronym() {
        return acronym;
    }

    public String getOrgType() {
        return orgType;
    }

    // Alias for backward compatibility
    public String getOrganizationType() {
        return orgType;
    }

    public String getBranch() {
        return branch;
    }

    // Alias for backward compatibility
    public String getGovernmentBranch() {
        return branch;
    }

    public Integer getOrgLevel() {
        return orgLevel;
    }

    public UUID getParentId() {
        return parentId;
    }

    // Alias for backward compatibility
    public UUID getParentOrganizationId() {
        return parentId;
    }
}
