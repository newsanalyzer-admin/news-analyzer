-- =====================================================================
-- Migration: V3__create_government_organizations.sql
-- Description: Create schema for US Government organizational chart data
-- Author: Winston (Architect)
-- Date: 2025-11-21
-- Dependencies: V2__* (existing entity tables)
-- Data Source: GovInfo API (https://api.govinfo.gov)
-- =====================================================================

-- =====================================================================
-- TABLE: government_organizations
-- Purpose: Store official US Government organizational structure
-- Source: US Government Manual via GovInfo API
-- =====================================================================

CREATE TABLE government_organizations (
    -- Primary Key
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    -- Official Information
    official_name VARCHAR(500) NOT NULL,
    acronym VARCHAR(50),
    former_names TEXT[], -- Array of historical names

    -- Classification
    org_type VARCHAR(100) NOT NULL, -- 'department', 'independent_agency', 'bureau', 'office', 'commission', 'board'
    branch VARCHAR(50) NOT NULL CHECK (branch IN ('executive', 'legislative', 'judicial')),

    -- Hierarchy
    parent_id UUID REFERENCES government_organizations(id) ON DELETE SET NULL,
    org_level INTEGER DEFAULT 1, -- 1=top-level, 2=sub-org, 3=bureau, etc.

    -- Historical Information
    established_date DATE,
    dissolved_date DATE, -- NULL if currently active
    authorizing_legislation TEXT, -- Public Law or statute

    -- Descriptive Information
    mission_statement TEXT,
    description TEXT,

    -- Contact Information
    website_url VARCHAR(500),
    contact_info JSONB, -- {email, phone, address, etc.}

    -- Jurisdiction and Responsibilities
    jurisdiction_areas TEXT[], -- ['environmental_protection', 'public_health', etc.]
    primary_functions TEXT[],

    -- Metadata
    metadata JSONB, -- Raw GovInfo API data
    schema_org_data JSONB, -- Schema.org JSON-LD representation

    -- Data Source Tracking
    govinfo_package_id VARCHAR(200), -- GovInfo package identifier
    govinfo_year INTEGER, -- Year of Government Manual
    govinfo_last_sync TIMESTAMP,
    data_quality_score DECIMAL(3,2), -- 0.00-1.00 confidence in data accuracy

    -- Audit Fields
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by VARCHAR(100) DEFAULT 'system',
    updated_by VARCHAR(100) DEFAULT 'system',

    -- Constraints
    CONSTRAINT valid_org_level CHECK (org_level >= 1 AND org_level <= 10),
    CONSTRAINT valid_quality_score CHECK (data_quality_score >= 0 AND data_quality_score <= 1),
    CONSTRAINT unique_official_name UNIQUE (official_name),
    CONSTRAINT active_org_check CHECK (dissolved_date IS NULL OR dissolved_date > established_date)
);

-- Indexes for performance
CREATE INDEX idx_gov_org_acronym ON government_organizations(acronym) WHERE acronym IS NOT NULL;
CREATE INDEX idx_gov_org_parent ON government_organizations(parent_id) WHERE parent_id IS NOT NULL;
CREATE INDEX idx_gov_org_type ON government_organizations(org_type);
CREATE INDEX idx_gov_org_branch ON government_organizations(branch);
CREATE INDEX idx_gov_org_active ON government_organizations(dissolved_date) WHERE dissolved_date IS NULL;

-- Partial unique index for acronym per branch (only when acronym is not null)
CREATE UNIQUE INDEX idx_gov_org_unique_acronym_branch
    ON government_organizations(acronym, branch)
    WHERE acronym IS NOT NULL;
CREATE INDEX idx_gov_org_level ON government_organizations(org_level);
CREATE INDEX idx_gov_org_name_trgm ON government_organizations USING gin(official_name gin_trgm_ops);
CREATE INDEX idx_gov_org_schema_org ON government_organizations USING gin(schema_org_data jsonb_path_ops);
CREATE INDEX idx_gov_org_jurisdiction ON government_organizations USING gin(jurisdiction_areas);

-- Full-text search index
CREATE INDEX idx_gov_org_fulltext ON government_organizations USING gin(
    to_tsvector('english',
        coalesce(official_name, '') || ' ' ||
        coalesce(acronym, '') || ' ' ||
        coalesce(mission_statement, '') || ' ' ||
        coalesce(description, '')
    )
);

-- Comments
COMMENT ON TABLE government_organizations IS 'Official US Government organizational structure from US Government Manual';
COMMENT ON COLUMN government_organizations.org_type IS 'Type of organization: department, independent_agency, bureau, office, commission, board';
COMMENT ON COLUMN government_organizations.branch IS 'Branch of government: executive, legislative, judicial';
COMMENT ON COLUMN government_organizations.org_level IS 'Hierarchical level: 1=cabinet/top-level, 2=sub-agency, 3=bureau, etc.';
COMMENT ON COLUMN government_organizations.govinfo_package_id IS 'GovInfo API package identifier for traceability';

-- =====================================================================
-- TABLE: government_organization_aliases
-- Purpose: Track alternate names, acronyms, and historical names
-- =====================================================================

CREATE TABLE government_organization_aliases (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id UUID NOT NULL REFERENCES government_organizations(id) ON DELETE CASCADE,

    -- Alias Information
    alias_name VARCHAR(500) NOT NULL,
    alias_type VARCHAR(50) NOT NULL, -- 'acronym', 'former_name', 'colloquial', 'abbreviation', 'popular_name'

    -- Validity Period
    valid_from DATE,
    valid_to DATE, -- NULL if currently valid

    -- Context
    usage_context TEXT, -- When/why this alias is used
    is_official BOOLEAN DEFAULT false, -- Official vs colloquial

    -- Metadata
    source VARCHAR(200), -- Where this alias was found
    notes TEXT,

    -- Audit
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),

    -- Constraints
    CONSTRAINT unique_org_alias UNIQUE (organization_id, alias_name, alias_type),
    CONSTRAINT valid_alias_period CHECK (valid_to IS NULL OR valid_to >= valid_from)
);

CREATE INDEX idx_gov_alias_org ON government_organization_aliases(organization_id);
CREATE INDEX idx_gov_alias_name ON government_organization_aliases(alias_name);
CREATE INDEX idx_gov_alias_type ON government_organization_aliases(alias_type);
CREATE INDEX idx_gov_alias_active ON government_organization_aliases(valid_to) WHERE valid_to IS NULL;

COMMENT ON TABLE government_organization_aliases IS 'Alternate names, acronyms, and historical names for government organizations';

-- =====================================================================
-- TABLE: government_organization_relationships
-- Purpose: Track non-hierarchical relationships between organizations
-- =====================================================================

CREATE TABLE government_organization_relationships (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    -- Related Organizations
    source_org_id UUID NOT NULL REFERENCES government_organizations(id) ON DELETE CASCADE,
    target_org_id UUID NOT NULL REFERENCES government_organizations(id) ON DELETE CASCADE,

    -- Relationship Information
    relationship_type VARCHAR(100) NOT NULL, -- 'reports_to', 'coordinates_with', 'regulates', 'funds', 'advises', 'oversees', 'collaborates_with'
    relationship_strength VARCHAR(20) DEFAULT 'normal', -- 'strong', 'normal', 'weak'
    is_bidirectional BOOLEAN DEFAULT false,

    -- Description
    description TEXT,
    formal_authority TEXT, -- Legal or regulatory basis

    -- Validity Period
    valid_from DATE,
    valid_to DATE, -- NULL if currently valid

    -- Metadata
    source_document VARCHAR(500), -- Legal citation or reference
    notes TEXT,

    -- Audit
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),

    -- Constraints
    CONSTRAINT no_self_relationship CHECK (source_org_id != target_org_id),
    CONSTRAINT valid_relationship_period CHECK (valid_to IS NULL OR valid_to >= valid_from),
    CONSTRAINT unique_org_relationship UNIQUE (source_org_id, target_org_id, relationship_type, valid_from)
);

CREATE INDEX idx_gov_rel_source ON government_organization_relationships(source_org_id);
CREATE INDEX idx_gov_rel_target ON government_organization_relationships(target_org_id);
CREATE INDEX idx_gov_rel_type ON government_organization_relationships(relationship_type);
CREATE INDEX idx_gov_rel_active ON government_organization_relationships(valid_to) WHERE valid_to IS NULL;
CREATE INDEX idx_gov_rel_bidirectional ON government_organization_relationships(is_bidirectional) WHERE is_bidirectional = true;

COMMENT ON TABLE government_organization_relationships IS 'Non-hierarchical relationships between government organizations (coordination, regulation, etc.)';

-- =====================================================================
-- TABLE: government_organization_jurisdictions
-- Purpose: Track areas of responsibility and authority
-- =====================================================================

CREATE TABLE government_organization_jurisdictions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id UUID NOT NULL REFERENCES government_organizations(id) ON DELETE CASCADE,

    -- Jurisdiction Information
    jurisdiction_area VARCHAR(200) NOT NULL, -- 'environmental_protection', 'national_defense', 'public_health', etc.
    jurisdiction_category VARCHAR(100), -- 'regulatory', 'programmatic', 'advisory', 'oversight'

    -- Description
    description TEXT,
    scope TEXT, -- Geographic or topical scope

    -- Authority
    legal_authority TEXT, -- Statutory or regulatory basis
    authority_level VARCHAR(50), -- 'primary', 'shared', 'advisory'

    -- Priority
    priority INTEGER DEFAULT 1, -- 1=highest priority

    -- Geographic Scope
    geographic_scope VARCHAR(50), -- 'national', 'regional', 'state', 'local', 'international'
    specific_regions TEXT[], -- Specific states, regions, or areas

    -- Validity
    valid_from DATE,
    valid_to DATE,

    -- Audit
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),

    -- Constraints
    CONSTRAINT unique_org_jurisdiction UNIQUE (organization_id, jurisdiction_area),
    CONSTRAINT valid_priority CHECK (priority >= 1),
    CONSTRAINT valid_jurisdiction_period CHECK (valid_to IS NULL OR valid_to >= valid_from)
);

CREATE INDEX idx_gov_juris_org ON government_organization_jurisdictions(organization_id);
CREATE INDEX idx_gov_juris_area ON government_organization_jurisdictions(jurisdiction_area);
CREATE INDEX idx_gov_juris_category ON government_organization_jurisdictions(jurisdiction_category);
CREATE INDEX idx_gov_juris_priority ON government_organization_jurisdictions(priority);
CREATE INDEX idx_gov_juris_active ON government_organization_jurisdictions(valid_to) WHERE valid_to IS NULL;

COMMENT ON TABLE government_organization_jurisdictions IS 'Areas of responsibility, authority, and jurisdiction for government organizations';

-- =====================================================================
-- TABLE: government_organization_history
-- Purpose: Track organizational changes over time
-- =====================================================================

CREATE TABLE government_organization_history (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id UUID NOT NULL REFERENCES government_organizations(id) ON DELETE CASCADE,

    -- Change Information
    change_type VARCHAR(100) NOT NULL, -- 'created', 'renamed', 'merged', 'split', 'dissolved', 'reorganized', 'transferred', 'mission_changed'
    change_date DATE NOT NULL,
    effective_date DATE, -- When change took effect (may differ from announcement)

    -- Description
    change_description TEXT NOT NULL,
    rationale TEXT, -- Why the change was made

    -- Related Organizations
    related_organizations JSONB, -- IDs and roles of other orgs involved (mergers, splits, transfers)

    -- Before/After State
    state_before JSONB, -- Snapshot of relevant fields before change
    state_after JSONB, -- Snapshot after change

    -- Legal Basis
    authorizing_document VARCHAR(500), -- Executive order, legislation, etc.
    source_document VARCHAR(500), -- Where this information came from

    -- Impact
    impact_level VARCHAR(20), -- 'major', 'moderate', 'minor'
    affected_functions TEXT[],

    -- Metadata
    notes TEXT,

    -- Audit
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by VARCHAR(100) DEFAULT 'system',

    -- Constraints
    CONSTRAINT valid_effective_date CHECK (effective_date IS NULL OR effective_date >= change_date)
);

CREATE INDEX idx_gov_history_org ON government_organization_history(organization_id);
CREATE INDEX idx_gov_history_type ON government_organization_history(change_type);
CREATE INDEX idx_gov_history_date ON government_organization_history(change_date DESC);
CREATE INDEX idx_gov_history_effective ON government_organization_history(effective_date DESC);
CREATE INDEX idx_gov_history_impact ON government_organization_history(impact_level);

COMMENT ON TABLE government_organization_history IS 'Historical record of organizational changes (mergers, splits, reorganizations, etc.)';

-- =====================================================================
-- TABLE: government_organization_sync_log
-- Purpose: Track data synchronization from GovInfo API
-- =====================================================================

CREATE TABLE government_organization_sync_log (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    -- Sync Information
    sync_start_time TIMESTAMP NOT NULL DEFAULT NOW(),
    sync_end_time TIMESTAMP,
    sync_status VARCHAR(50) NOT NULL, -- 'in_progress', 'completed', 'failed', 'partial'

    -- Scope
    govinfo_year INTEGER NOT NULL,
    govinfo_collection VARCHAR(100) DEFAULT 'GOVMAN',

    -- Results
    organizations_added INTEGER DEFAULT 0,
    organizations_updated INTEGER DEFAULT 0,
    organizations_removed INTEGER DEFAULT 0,
    organizations_unchanged INTEGER DEFAULT 0,

    -- Changes Detected
    new_agencies TEXT[],
    renamed_agencies TEXT[],
    dissolved_agencies TEXT[],

    -- Performance
    api_calls_made INTEGER DEFAULT 0,
    processing_time_seconds INTEGER,

    -- Errors
    error_count INTEGER DEFAULT 0,
    error_messages TEXT[],
    warnings TEXT[],

    -- Data Quality
    validation_passed INTEGER DEFAULT 0,
    validation_failed INTEGER DEFAULT 0,
    data_quality_average DECIMAL(3,2),

    -- Metadata
    triggered_by VARCHAR(100), -- 'scheduled_job', 'manual', 'api_webhook'
    performed_by VARCHAR(100) DEFAULT 'system',
    notes TEXT,

    -- Audit
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_gov_sync_status ON government_organization_sync_log(sync_status);
CREATE INDEX idx_gov_sync_year ON government_organization_sync_log(govinfo_year DESC);
CREATE INDEX idx_gov_sync_start ON government_organization_sync_log(sync_start_time DESC);
CREATE INDEX idx_gov_sync_triggered ON government_organization_sync_log(triggered_by);

COMMENT ON TABLE government_organization_sync_log IS 'Audit log for GovInfo API synchronization jobs';

-- =====================================================================
-- VIEWS: Convenience views for common queries
-- =====================================================================

-- Active organizations only
CREATE VIEW vw_active_government_organizations AS
SELECT
    id,
    official_name,
    acronym,
    org_type,
    branch,
    parent_id,
    org_level,
    established_date,
    website_url,
    jurisdiction_areas,
    schema_org_data,
    created_at,
    updated_at
FROM government_organizations
WHERE dissolved_date IS NULL;

COMMENT ON VIEW vw_active_government_organizations IS 'Currently active government organizations only';

-- Cabinet departments
CREATE VIEW vw_cabinet_departments AS
SELECT
    id,
    official_name,
    acronym,
    established_date,
    website_url,
    mission_statement,
    schema_org_data
FROM government_organizations
WHERE org_type = 'department'
  AND branch = 'executive'
  AND org_level = 1
  AND dissolved_date IS NULL
ORDER BY official_name;

COMMENT ON VIEW vw_cabinet_departments IS 'The 15 Cabinet-level executive departments';

-- Independent agencies
CREATE VIEW vw_independent_agencies AS
SELECT
    id,
    official_name,
    acronym,
    established_date,
    website_url,
    mission_statement,
    jurisdiction_areas,
    schema_org_data
FROM government_organizations
WHERE org_type = 'independent_agency'
  AND branch = 'executive'
  AND parent_id IS NULL
  AND dissolved_date IS NULL
ORDER BY official_name;

COMMENT ON VIEW vw_independent_agencies IS 'Independent executive agencies (EPA, NASA, etc.)';

-- Organizational hierarchy (recursive CTE)
CREATE VIEW vw_organization_hierarchy AS
WITH RECURSIVE org_tree AS (
    -- Base case: top-level organizations
    SELECT
        id,
        official_name,
        acronym,
        org_type,
        branch,
        parent_id,
        org_level,
        ARRAY[official_name] AS hierarchy_path,
        official_name AS hierarchy_string,
        0 AS depth
    FROM government_organizations
    WHERE parent_id IS NULL
      AND dissolved_date IS NULL

    UNION ALL

    -- Recursive case: children
    SELECT
        o.id,
        o.official_name,
        o.acronym,
        o.org_type,
        o.branch,
        o.parent_id,
        o.org_level,
        ot.hierarchy_path || o.official_name,
        ot.hierarchy_string || ' > ' || o.official_name,
        ot.depth + 1
    FROM government_organizations o
    INNER JOIN org_tree ot ON o.parent_id = ot.id
    WHERE o.dissolved_date IS NULL
)
SELECT * FROM org_tree;

COMMENT ON VIEW vw_organization_hierarchy IS 'Recursive hierarchy of government organizations showing full path from top level';

-- =====================================================================
-- FUNCTIONS: Helper functions for common operations
-- =====================================================================

-- Function: Get all child organizations (recursive)
CREATE OR REPLACE FUNCTION get_child_organizations(parent_org_id UUID)
RETURNS TABLE (
    id UUID,
    official_name VARCHAR,
    org_level INTEGER,
    depth INTEGER
) AS $$
WITH RECURSIVE children AS (
    SELECT
        id,
        official_name,
        org_level,
        0 AS depth
    FROM government_organizations
    WHERE parent_id = parent_org_id
      AND dissolved_date IS NULL

    UNION ALL

    SELECT
        o.id,
        o.official_name,
        o.org_level,
        c.depth + 1
    FROM government_organizations o
    INNER JOIN children c ON o.parent_id = c.id
    WHERE o.dissolved_date IS NULL
)
SELECT * FROM children
ORDER BY depth, official_name;
$$ LANGUAGE SQL STABLE;

COMMENT ON FUNCTION get_child_organizations IS 'Get all descendant organizations recursively';

-- Function: Get organizational ancestry (parents)
CREATE OR REPLACE FUNCTION get_organization_ancestry(org_id UUID)
RETURNS TABLE (
    id UUID,
    official_name VARCHAR,
    org_level INTEGER,
    depth INTEGER
) AS $$
WITH RECURSIVE ancestors AS (
    SELECT
        id,
        official_name,
        parent_id,
        org_level,
        0 AS depth
    FROM government_organizations
    WHERE id = org_id

    UNION ALL

    SELECT
        o.id,
        o.official_name,
        o.parent_id,
        o.org_level,
        a.depth + 1
    FROM government_organizations o
    INNER JOIN ancestors a ON o.id = a.parent_id
)
SELECT
    id,
    official_name,
    org_level,
    depth
FROM ancestors
ORDER BY depth DESC;
$$ LANGUAGE SQL STABLE;

COMMENT ON FUNCTION get_organization_ancestry IS 'Get all ancestor organizations (parents) up to top level';

-- Function: Search organizations by name (fuzzy)
CREATE OR REPLACE FUNCTION search_government_organizations(search_text TEXT)
RETURNS TABLE (
    id UUID,
    official_name VARCHAR,
    acronym VARCHAR,
    similarity_score REAL
) AS $$
SELECT
    id,
    official_name,
    acronym,
    similarity(official_name, search_text) AS similarity_score
FROM government_organizations
WHERE
    dissolved_date IS NULL
    AND (
        official_name ILIKE '%' || search_text || '%'
        OR acronym ILIKE '%' || search_text || '%'
        OR similarity(official_name, search_text) > 0.3
    )
ORDER BY similarity_score DESC, official_name
LIMIT 20;
$$ LANGUAGE SQL STABLE;

COMMENT ON FUNCTION search_government_organizations IS 'Fuzzy search for government organizations by name';

-- =====================================================================
-- TRIGGERS: Automatic timestamp updates
-- =====================================================================

-- Trigger function: Update updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Apply trigger to all tables
CREATE TRIGGER update_government_organizations_updated_at
    BEFORE UPDATE ON government_organizations
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_government_organization_aliases_updated_at
    BEFORE UPDATE ON government_organization_aliases
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_government_organization_relationships_updated_at
    BEFORE UPDATE ON government_organization_relationships
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_government_organization_jurisdictions_updated_at
    BEFORE UPDATE ON government_organization_jurisdictions
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- =====================================================================
-- INITIAL DATA: Seed with well-known organizations
-- =====================================================================

-- Insert Executive Branch (placeholder for hierarchy)
INSERT INTO government_organizations (
    official_name,
    acronym,
    org_type,
    branch,
    org_level,
    established_date,
    mission_statement,
    data_quality_score,
    created_by
) VALUES
(
    'Executive Branch',
    NULL,
    'branch',
    'executive',
    0,
    '1789-04-30',
    'The Executive Branch carries out and enforces laws under the leadership of the President.',
    1.0,
    'seed_data'
);

-- Get Executive Branch ID for parent reference
DO $$
DECLARE
    exec_branch_id UUID;
BEGIN
    SELECT id INTO exec_branch_id
    FROM government_organizations
    WHERE official_name = 'Executive Branch';

    -- Insert Cabinet Departments (15 departments)
    INSERT INTO government_organizations (
        official_name, acronym, org_type, branch, parent_id, org_level,
        established_date, website_url, jurisdiction_areas,
        data_quality_score, created_by
    ) VALUES
    -- Original 4 departments
    ('Department of State', 'DOS', 'department', 'executive', exec_branch_id, 1,
     '1789-07-27', 'https://www.state.gov',
     ARRAY['foreign_affairs', 'diplomacy', 'international_relations'], 1.0, 'seed_data'),

    ('Department of the Treasury', 'Treasury', 'department', 'executive', exec_branch_id, 1,
     '1789-09-02', 'https://home.treasury.gov',
     ARRAY['fiscal_policy', 'taxation', 'currency'], 1.0, 'seed_data'),

    ('Department of Defense', 'DOD', 'department', 'executive', exec_branch_id, 1,
     '1949-08-10', 'https://www.defense.gov',
     ARRAY['national_defense', 'military_operations'], 1.0, 'seed_data'),

    ('Department of Justice', 'DOJ', 'department', 'executive', exec_branch_id, 1,
     '1870-06-22', 'https://www.justice.gov',
     ARRAY['law_enforcement', 'legal_affairs', 'civil_rights'], 1.0, 'seed_data'),

    -- Interior (1849)
    ('Department of the Interior', 'DOI', 'department', 'executive', exec_branch_id, 1,
     '1849-03-03', 'https://www.doi.gov',
     ARRAY['public_lands', 'natural_resources', 'indigenous_affairs'], 1.0, 'seed_data'),

    -- Agriculture (1889)
    ('Department of Agriculture', 'USDA', 'department', 'executive', exec_branch_id, 1,
     '1889-02-09', 'https://www.usda.gov',
     ARRAY['agriculture', 'food_safety', 'rural_development'], 1.0, 'seed_data'),

    -- Commerce (1903)
    ('Department of Commerce', 'DOC', 'department', 'executive', exec_branch_id, 1,
     '1903-02-14', 'https://www.commerce.gov',
     ARRAY['economic_development', 'trade', 'business'], 1.0, 'seed_data'),

    -- Labor (1913)
    ('Department of Labor', 'DOL', 'department', 'executive', exec_branch_id, 1,
     '1913-03-04', 'https://www.dol.gov',
     ARRAY['labor_standards', 'worker_protection', 'employment'], 1.0, 'seed_data'),

    -- Health and Human Services (1979)
    ('Department of Health and Human Services', 'HHS', 'department', 'executive', exec_branch_id, 1,
     '1979-05-04', 'https://www.hhs.gov',
     ARRAY['public_health', 'social_services', 'medicare_medicaid'], 1.0, 'seed_data'),

    -- Housing and Urban Development (1965)
    ('Department of Housing and Urban Development', 'HUD', 'department', 'executive', exec_branch_id, 1,
     '1965-09-09', 'https://www.hud.gov',
     ARRAY['housing', 'urban_development', 'fair_housing'], 1.0, 'seed_data'),

    -- Transportation (1966)
    ('Department of Transportation', 'DOT', 'department', 'executive', exec_branch_id, 1,
     '1966-10-15', 'https://www.transportation.gov',
     ARRAY['transportation_infrastructure', 'highway_safety', 'aviation'], 1.0, 'seed_data'),

    -- Energy (1977)
    ('Department of Energy', 'DOE', 'department', 'executive', exec_branch_id, 1,
     '1977-08-04', 'https://www.energy.gov',
     ARRAY['energy_policy', 'nuclear_security', 'renewable_energy'], 1.0, 'seed_data'),

    -- Education (1979)
    ('Department of Education', 'ED', 'department', 'executive', exec_branch_id, 1,
     '1979-05-04', 'https://www.ed.gov',
     ARRAY['education_policy', 'student_aid', 'educational_research'], 1.0, 'seed_data'),

    -- Veterans Affairs (1989)
    ('Department of Veterans Affairs', 'VA', 'department', 'executive', exec_branch_id, 1,
     '1989-03-15', 'https://www.va.gov',
     ARRAY['veterans_benefits', 'healthcare', 'military_pensions'], 1.0, 'seed_data'),

    -- Homeland Security (2002)
    ('Department of Homeland Security', 'DHS', 'department', 'executive', exec_branch_id, 1,
     '2002-11-25', 'https://www.dhs.gov',
     ARRAY['national_security', 'border_security', 'cybersecurity', 'disaster_response'], 1.0, 'seed_data');

    -- Insert Major Independent Agencies
    INSERT INTO government_organizations (
        official_name, acronym, org_type, branch, parent_id, org_level,
        established_date, website_url, jurisdiction_areas,
        data_quality_score, created_by
    ) VALUES
    ('Environmental Protection Agency', 'EPA', 'independent_agency', 'executive', exec_branch_id, 1,
     '1970-12-02', 'https://www.epa.gov',
     ARRAY['environmental_protection', 'pollution_control', 'clean_air', 'clean_water'], 1.0, 'seed_data'),

    ('National Aeronautics and Space Administration', 'NASA', 'independent_agency', 'executive', exec_branch_id, 1,
     '1958-07-29', 'https://www.nasa.gov',
     ARRAY['space_exploration', 'aeronautics', 'scientific_research'], 1.0, 'seed_data'),

    ('Central Intelligence Agency', 'CIA', 'independent_agency', 'executive', exec_branch_id, 1,
     '1947-09-18', 'https://www.cia.gov',
     ARRAY['foreign_intelligence', 'national_security'], 1.0, 'seed_data'),

    ('Federal Bureau of Investigation', 'FBI', 'independent_agency', 'executive', exec_branch_id, 1,
     '1908-07-26', 'https://www.fbi.gov',
     ARRAY['law_enforcement', 'counterterrorism', 'cybercrime'], 1.0, 'seed_data'),

    ('Social Security Administration', 'SSA', 'independent_agency', 'executive', exec_branch_id, 1,
     '1935-08-14', 'https://www.ssa.gov',
     ARRAY['social_security', 'retirement_benefits', 'disability_insurance'], 1.0, 'seed_data');
END $$;

-- =====================================================================
-- GRANTS: Set appropriate permissions
-- =====================================================================

-- Grant read access to all users
GRANT SELECT ON ALL TABLES IN SCHEMA public TO PUBLIC;
GRANT SELECT ON ALL SEQUENCES IN SCHEMA public TO PUBLIC;

-- Grant write access to application role (adjust as needed)
-- GRANT INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO newsanalyzer_app;

-- =====================================================================
-- COMPLETION LOG
-- =====================================================================

-- Log migration completion
DO $$
BEGIN
    RAISE NOTICE 'Migration V3__create_government_organizations.sql completed successfully';
    RAISE NOTICE 'Created 6 tables: government_organizations, aliases, relationships, jurisdictions, history, sync_log';
    RAISE NOTICE 'Created 4 views: active orgs, cabinet departments, independent agencies, hierarchy';
    RAISE NOTICE 'Created 3 functions: get_child_organizations, get_organization_ancestry, search_government_organizations';
    RAISE NOTICE 'Seeded 20 initial organizations: 15 Cabinet departments + 5 major independent agencies';
END $$;
