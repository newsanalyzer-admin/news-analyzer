-- V16__person_nullable_bioguide.sql
-- FB-2.1: Allow Person records without bioguide_id for executive branch appointees

-- Remove NOT NULL constraint from bioguide_id
ALTER TABLE persons ALTER COLUMN bioguide_id DROP NOT NULL;

-- Drop the existing unique constraint (if exists) and recreate as partial index
-- This allows multiple NULL bioguide_ids while keeping unique for non-null values
-- NOTE: Must drop constraint FIRST - dropping the index alone fails because the constraint depends on it
ALTER TABLE persons DROP CONSTRAINT IF EXISTS persons_bioguide_id_key;
DROP INDEX IF EXISTS persons_bioguide_id_key;

CREATE UNIQUE INDEX uk_persons_bioguide_id
ON persons (bioguide_id)
WHERE bioguide_id IS NOT NULL;

COMMENT ON COLUMN persons.bioguide_id IS
    'BioGuide ID from Congress.gov - required for legislative branch, NULL for executive';
