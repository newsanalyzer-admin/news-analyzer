-- V34: Create individuals table
-- ARCH-1.1: Individual Table Refactor
-- This table stores universal biographical data for any person in the system.
-- Role-specific data (Congressional, Judicial, etc.) is stored in separate tables.

CREATE TABLE individuals (
    id UUID PRIMARY KEY,

    -- Personal Information
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    middle_name VARCHAR(100),
    suffix VARCHAR(20),

    -- Biographical Information
    birth_date DATE,
    death_date DATE,
    birth_place VARCHAR(200),
    gender VARCHAR(10),
    image_url VARCHAR(500),

    -- Political Affiliation (MOD-2)
    party VARCHAR(50),

    -- External IDs for Cross-Referencing (JSONB)
    external_ids JSONB,

    -- Social Media (JSONB)
    social_media JSONB,

    -- Data Source Tracking (MOD-3)
    primary_data_source VARCHAR(50),

    -- Audit Fields
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Index for name lookups
CREATE INDEX idx_individuals_name ON individuals(first_name, last_name);

-- Index for birth date lookups
CREATE INDEX idx_individuals_birth_date ON individuals(birth_date);

-- Composite unique constraint for deduplication (MOD-1)
-- Uses case-insensitive matching on first_name and last_name
-- Only applies when birth_date is NOT NULL (partial index)
CREATE UNIQUE INDEX idx_individuals_unique_person
ON individuals(LOWER(first_name), LOWER(last_name), birth_date)
WHERE birth_date IS NOT NULL;

-- Index for data source lookups
CREATE INDEX idx_individuals_data_source ON individuals(primary_data_source);

COMMENT ON TABLE individuals IS 'Master table for biographical data of all individuals in the system';
COMMENT ON COLUMN individuals.external_ids IS 'JSONB map of external system IDs (e.g., bioguideId, fjcId, wikidataId)';
COMMENT ON COLUMN individuals.social_media IS 'JSONB map of social media handles';
COMMENT ON COLUMN individuals.primary_data_source IS 'The primary/authoritative data source for this individual';
