-- V38__add_individual_id_to_presidency_holding.sql
-- ARCH-1.4 Phase A: Add individual_id columns to presidencies and position_holdings
--
-- After V37, the persons table has been renamed to congressional_members.
-- The congressional_members table has individual_id already populated (from V36).
-- This migration adds individual_id to presidencies and position_holdings,
-- then populates it from the congressional_members mapping.

-- =====================================================================
-- Step 1: Add individual_id column to presidencies (nullable initially)
-- =====================================================================
ALTER TABLE presidencies ADD COLUMN IF NOT EXISTS individual_id UUID;

-- =====================================================================
-- Step 2: Add individual_id column to position_holdings (nullable initially)
-- =====================================================================
ALTER TABLE position_holdings ADD COLUMN IF NOT EXISTS individual_id UUID;

-- =====================================================================
-- Step 3: Populate presidencies.individual_id from congressional_members
-- =====================================================================
-- The person_id in presidencies references the old persons table (now congressional_members)
-- which has individual_id already populated from V36

UPDATE presidencies p
SET individual_id = cm.individual_id
FROM congressional_members cm
WHERE p.person_id = cm.id
  AND p.individual_id IS NULL;

-- =====================================================================
-- Step 4: Populate position_holdings.individual_id from congressional_members
-- =====================================================================
UPDATE position_holdings ph
SET individual_id = cm.individual_id
FROM congressional_members cm
WHERE ph.person_id = cm.id
  AND ph.individual_id IS NULL;

-- =====================================================================
-- Step 5: Handle any presidencies that don't match congressional_members
-- =====================================================================
-- Presidents may not have been in Congress (e.g., Eisenhower, Trump 1st term)
-- For these, we need to look up the individual directly by matching person data
-- that was migrated to individuals table

-- First, log any unmatched presidencies
DO $$
DECLARE
    unmatched_count INTEGER;
    unmatched_record RECORD;
BEGIN
    SELECT COUNT(*) INTO unmatched_count
    FROM presidencies WHERE individual_id IS NULL;

    IF unmatched_count > 0 THEN
        RAISE WARNING 'Found % presidencies without individual_id mapping via congressional_members', unmatched_count;

        -- Log details for debugging
        FOR unmatched_record IN
            SELECT p.id, p.number, p.party, p.start_date
            FROM presidencies p
            WHERE p.individual_id IS NULL
            LIMIT 5
        LOOP
            RAISE WARNING 'Unmatched presidency #%: party=%, start=%',
                unmatched_record.number,
                unmatched_record.party,
                unmatched_record.start_date;
        END LOOP;
    END IF;
END $$;

-- =====================================================================
-- Step 6: Handle any position_holdings that don't match congressional_members
-- =====================================================================
DO $$
DECLARE
    unmatched_count INTEGER;
BEGIN
    SELECT COUNT(*) INTO unmatched_count
    FROM position_holdings WHERE individual_id IS NULL;

    IF unmatched_count > 0 THEN
        RAISE WARNING 'Found % position_holdings without individual_id mapping', unmatched_count;
    END IF;
END $$;

-- =====================================================================
-- Step 7: Add indexes on new columns (before making NOT NULL)
-- =====================================================================
CREATE INDEX IF NOT EXISTS idx_presidency_individual ON presidencies(individual_id);
CREATE INDEX IF NOT EXISTS idx_holding_individual ON position_holdings(individual_id);

-- =====================================================================
-- Verification queries (for manual checking)
-- =====================================================================
-- SELECT COUNT(*) as total, COUNT(individual_id) as mapped FROM presidencies;
-- SELECT COUNT(*) as total, COUNT(individual_id) as mapped FROM position_holdings;
-- SELECT p.number, p.party, p.person_id, p.individual_id FROM presidencies p ORDER BY p.number;

DO $$ BEGIN
    RAISE NOTICE 'V38 Phase A complete: individual_id columns added and populated where possible';
END $$;
