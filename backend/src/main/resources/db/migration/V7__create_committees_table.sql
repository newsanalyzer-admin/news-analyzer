-- V7: Create committees table
-- Stores Congressional committee data from Congress.gov API
-- Part of FB-1.2: Committee Data Integration

-- Committee type enum
CREATE TYPE committee_type AS ENUM ('STANDING', 'SELECT', 'SPECIAL', 'JOINT', 'SUBCOMMITTEE', 'OTHER');

-- Committee chamber enum (extends Person.Chamber with JOINT)
CREATE TYPE committee_chamber AS ENUM ('SENATE', 'HOUSE', 'JOINT');

-- Committees table
CREATE TABLE committees (
    committee_code VARCHAR(20) PRIMARY KEY,  -- systemCode from Congress.gov API (e.g., 'hsju00')
    name VARCHAR(255) NOT NULL,
    chamber committee_chamber NOT NULL,
    committee_type committee_type NOT NULL,
    parent_committee_code VARCHAR(20),       -- For subcommittees
    thomas_id VARCHAR(20),                   -- Legacy Thomas ID for cross-referencing
    url VARCHAR(500),                        -- Committee website URL

    -- Data source tracking
    congress_last_sync TIMESTAMP,
    data_source VARCHAR(50) DEFAULT 'CONGRESS_GOV',

    -- Audit fields
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by VARCHAR(100) DEFAULT 'system',
    updated_by VARCHAR(100) DEFAULT 'system',

    -- Self-referencing FK for subcommittees
    CONSTRAINT fk_parent_committee
        FOREIGN KEY (parent_committee_code)
        REFERENCES committees(committee_code)
        ON DELETE SET NULL
);

-- Indexes
CREATE INDEX idx_committees_chamber ON committees(chamber);
CREATE INDEX idx_committees_type ON committees(committee_type);
CREATE INDEX idx_committees_parent ON committees(parent_committee_code);
CREATE INDEX idx_committees_name ON committees(name);

-- Full-text search index on committee name
CREATE INDEX idx_committees_name_fulltext ON committees USING gin(to_tsvector('english', name));

-- Comments
COMMENT ON TABLE committees IS 'Congressional committees from Congress.gov API';
COMMENT ON COLUMN committees.committee_code IS 'System code from Congress.gov API (e.g., hsju00)';
COMMENT ON COLUMN committees.parent_committee_code IS 'Parent committee code for subcommittees';
COMMENT ON COLUMN committees.thomas_id IS 'Legacy Thomas ID for historical cross-referencing';
