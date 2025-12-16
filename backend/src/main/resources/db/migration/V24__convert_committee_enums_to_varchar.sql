-- V24: Convert PostgreSQL enum types to VARCHAR for Hibernate compatibility
-- PostgreSQL custom enum types cause issues with Hibernate's @Enumerated(EnumType.STRING)

-- First, alter the columns to VARCHAR (requires casting through text)
ALTER TABLE committees
    ALTER COLUMN chamber TYPE VARCHAR(20) USING chamber::text,
    ALTER COLUMN committee_type TYPE VARCHAR(20) USING committee_type::text;

-- Drop the old enum types (they are no longer needed)
DROP TYPE IF EXISTS committee_chamber;
DROP TYPE IF EXISTS committee_type;
