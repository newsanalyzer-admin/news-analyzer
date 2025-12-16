-- V6__create_persons_table.sql
-- Create persons table for Congressional member data
-- Part of FB-1: Factbase Expansion with Congressional Data

CREATE TABLE persons (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    -- Congress.gov identifiers
    bioguide_id VARCHAR(20) UNIQUE NOT NULL,

    -- Personal information
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    middle_name VARCHAR(100),
    suffix VARCHAR(20),

    -- Political information
    party VARCHAR(50),
    state VARCHAR(2),
    chamber VARCHAR(20),

    -- Biographical information
    birth_date DATE,
    gender VARCHAR(10),
    image_url VARCHAR(500),

    -- External IDs for cross-referencing (FEC, GovTrack, OpenSecrets, etc.)
    external_ids JSONB DEFAULT '{}'::jsonb,

    -- Data source tracking
    congress_last_sync TIMESTAMP,
    data_source VARCHAR(50) DEFAULT 'CONGRESS_GOV',

    -- Audit fields
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by VARCHAR(100) DEFAULT 'system',
    updated_by VARCHAR(100) DEFAULT 'system'
);

-- Create indexes for fast lookups
CREATE UNIQUE INDEX idx_persons_bioguide_id ON persons(bioguide_id);
CREATE INDEX idx_persons_last_name ON persons(last_name);
CREATE INDEX idx_persons_state ON persons(state);
CREATE INDEX idx_persons_chamber ON persons(chamber);
CREATE INDEX idx_persons_party ON persons(party);

-- Full-text search index for name search
CREATE INDEX idx_persons_name_search ON persons USING gin(
    to_tsvector('english', coalesce(first_name, '') || ' ' || coalesce(last_name, ''))
);

-- GIN index for external_ids JSONB queries
CREATE INDEX idx_persons_external_ids ON persons USING gin(external_ids);

-- Comments
COMMENT ON TABLE persons IS 'Congressional members and other persons from authoritative sources';
COMMENT ON COLUMN persons.bioguide_id IS 'Unique identifier from Congress.gov BioGuide';
COMMENT ON COLUMN persons.external_ids IS 'Cross-reference IDs: {fec: [...], govtrack: 123, opensecrets: "...", etc.}';
COMMENT ON COLUMN persons.chamber IS 'SENATE or HOUSE';
