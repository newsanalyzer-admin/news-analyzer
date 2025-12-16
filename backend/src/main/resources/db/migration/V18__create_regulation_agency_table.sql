-- V18__create_regulation_agency_table.sql
-- Many-to-many relationship between regulations and government organizations

CREATE TABLE regulation_agencies (
    regulation_id UUID NOT NULL,
    organization_id UUID NOT NULL,
    agency_name_raw VARCHAR(255),
    is_primary_agency BOOLEAN DEFAULT FALSE,
    PRIMARY KEY (regulation_id, organization_id),
    FOREIGN KEY (regulation_id) REFERENCES regulations(id) ON DELETE CASCADE,
    FOREIGN KEY (organization_id) REFERENCES government_organizations(id) ON DELETE CASCADE
);

-- Indexes for lookup queries
CREATE INDEX idx_regulation_agency_regulation ON regulation_agencies(regulation_id);
CREATE INDEX idx_regulation_agency_org ON regulation_agencies(organization_id);

-- Table and column comments
COMMENT ON TABLE regulation_agencies IS 'Many-to-many relationship between regulations and agencies';
COMMENT ON COLUMN regulation_agencies.agency_name_raw IS 'Original agency name from Federal Register API';
COMMENT ON COLUMN regulation_agencies.is_primary_agency IS 'True if this is the primary issuing agency';
