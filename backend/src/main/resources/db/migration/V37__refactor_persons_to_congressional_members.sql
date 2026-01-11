-- V37__refactor_persons_to_congressional_members.sql
-- ARCH-1.2: Refactor persons table to congressional_members
-- This migration drops biographical columns (now in individuals) and renames the table.
--
-- PREREQUISITE: V36 must have run to populate individual_id for all persons.
--
-- NOTE: Existing FK constraints from committee_memberships, position_holdings, and
-- presidencies will automatically update to reference congressional_members(id).
-- These will be properly migrated to reference individuals in ARCH-1.4/ARCH-1.5.

-- =====================================================================
-- Step 1: Verify prerequisites
-- =====================================================================
DO $$
DECLARE
    unlinked_count INTEGER;
BEGIN
    -- Ensure individual_id column exists
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'persons' AND column_name = 'individual_id'
    ) THEN
        RAISE EXCEPTION 'Prerequisites not met: individual_id column does not exist in persons table. Run V36 first.';
    END IF;

    -- Ensure all persons have individual_id populated
    SELECT COUNT(*) INTO unlinked_count FROM persons WHERE individual_id IS NULL;
    IF unlinked_count > 0 THEN
        RAISE EXCEPTION 'Prerequisites not met: % persons have NULL individual_id. Ensure V36 migration completed successfully.', unlinked_count;
    END IF;

    RAISE NOTICE 'Prerequisites verified: all persons have individual_id populated.';
END $$;

-- =====================================================================
-- Step 2: Drop indexes on columns that will be removed
-- =====================================================================
DROP INDEX IF EXISTS idx_persons_last_name;
DROP INDEX IF EXISTS idx_persons_name_search;
DROP INDEX IF EXISTS idx_persons_external_ids;
DROP INDEX IF EXISTS idx_persons_social_media;

-- =====================================================================
-- Step 3: Drop biographical columns (now stored in individuals table)
-- =====================================================================
-- These columns are now stored in the linked Individual record:
--   first_name, last_name, middle_name, suffix
--   birth_date, death_date, birth_place
--   gender, image_url
--   external_ids, social_media

ALTER TABLE persons DROP COLUMN IF EXISTS first_name;
ALTER TABLE persons DROP COLUMN IF EXISTS last_name;
ALTER TABLE persons DROP COLUMN IF EXISTS middle_name;
ALTER TABLE persons DROP COLUMN IF EXISTS suffix;
ALTER TABLE persons DROP COLUMN IF EXISTS birth_date;
ALTER TABLE persons DROP COLUMN IF EXISTS death_date;
ALTER TABLE persons DROP COLUMN IF EXISTS birth_place;
ALTER TABLE persons DROP COLUMN IF EXISTS gender;
ALTER TABLE persons DROP COLUMN IF EXISTS image_url;
ALTER TABLE persons DROP COLUMN IF EXISTS external_ids;
ALTER TABLE persons DROP COLUMN IF EXISTS social_media;

-- =====================================================================
-- Step 4: Rename table persons -> congressional_members
-- =====================================================================
-- PostgreSQL will automatically update FK constraints that reference this table
ALTER TABLE persons RENAME TO congressional_members;

-- =====================================================================
-- Step 5: Rename indexes to match new table name
-- =====================================================================
ALTER INDEX IF EXISTS idx_persons_bioguide_id RENAME TO idx_congressional_members_bioguide_id;
ALTER INDEX IF EXISTS idx_persons_state RENAME TO idx_congressional_members_state;
ALTER INDEX IF EXISTS idx_persons_chamber RENAME TO idx_congressional_members_chamber;
ALTER INDEX IF EXISTS idx_persons_party RENAME TO idx_congressional_members_party;
ALTER INDEX IF EXISTS idx_persons_individual_id RENAME TO idx_congressional_members_individual_id;

-- =====================================================================
-- Step 6: Add FK constraint on individual_id
-- =====================================================================
ALTER TABLE congressional_members
ADD CONSTRAINT fk_congressional_member_individual
FOREIGN KEY (individual_id) REFERENCES individuals(id);

-- =====================================================================
-- Step 7: Add unique constraint on individual_id (one person = one congressional record)
-- =====================================================================
ALTER TABLE congressional_members
ADD CONSTRAINT uk_congressional_member_individual UNIQUE (individual_id);

-- =====================================================================
-- Step 8: Update table and column comments
-- =====================================================================
COMMENT ON TABLE congressional_members IS 'Congressional members (Senators and Representatives) - role-specific data linked to individuals table';
COMMENT ON COLUMN congressional_members.id IS 'Primary key (preserved from original persons table)';
COMMENT ON COLUMN congressional_members.individual_id IS 'FK to individuals table - links to biographical data';
COMMENT ON COLUMN congressional_members.bioguide_id IS 'Unique identifier from Congress.gov BioGuide';
COMMENT ON COLUMN congressional_members.party IS 'Current party affiliation in Congress';
COMMENT ON COLUMN congressional_members.state IS 'State represented (2-letter code)';
COMMENT ON COLUMN congressional_members.chamber IS 'SENATE or HOUSE';
COMMENT ON COLUMN congressional_members.congress_last_sync IS 'Last sync from Congress.gov API';
COMMENT ON COLUMN congressional_members.data_source IS 'Primary data source (CONGRESS_GOV, etc.)';
COMMENT ON COLUMN congressional_members.enrichment_source IS 'Source of enrichment data';
COMMENT ON COLUMN congressional_members.enrichment_version IS 'Version/commit SHA of enrichment data';

-- =====================================================================
-- Verification queries (for manual checking, not executed)
-- =====================================================================
-- SELECT COUNT(*) FROM congressional_members; -- Should match pre-migration persons count
-- SELECT * FROM congressional_members WHERE individual_id IS NULL; -- Should return 0 rows
-- SELECT cm.id, i.first_name, i.last_name, cm.bioguide_id, cm.state, cm.chamber
-- FROM congressional_members cm
-- JOIN individuals i ON cm.individual_id = i.id
-- LIMIT 10;

DO $$ BEGIN
    RAISE NOTICE 'Migration V37 complete: persons table refactored to congressional_members';
END $$;
