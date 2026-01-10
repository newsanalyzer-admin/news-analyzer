-- V36_rollback__undo_migrate_persons_to_individuals.sql
-- ARCH-1.3: Rollback script for data migration
-- WARNING: This script is NOT a Flyway migration - run manually if needed

-- =====================================================================
-- ROLLBACK INSTRUCTIONS
-- =====================================================================
-- This script undoes V36 migration. Run manually if you need to rollback:
--
-- 1. Connect to database
-- 2. Run this script
-- 3. Delete V36 from flyway_schema_history:
--    DELETE FROM flyway_schema_history WHERE version = '36';
--
-- =====================================================================

-- Step 1: Drop the index on individual_id
DROP INDEX IF EXISTS idx_persons_individual_id;

-- Step 2: Remove the individual_id column from persons
ALTER TABLE persons DROP COLUMN IF EXISTS individual_id;

-- Step 3: Clear the individuals table (preserve structure for re-migration)
TRUNCATE TABLE individuals CASCADE;

-- Step 4: Verify rollback
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'persons' AND column_name = 'individual_id'
    ) THEN
        RAISE EXCEPTION 'Rollback failed: individual_id column still exists in persons';
    END IF;

    IF (SELECT COUNT(*) FROM individuals) > 0 THEN
        RAISE EXCEPTION 'Rollback failed: individuals table is not empty';
    END IF;

    RAISE NOTICE 'Rollback successful: V36 changes have been reverted';
END $$;

-- =====================================================================
-- After running this script, also run:
-- DELETE FROM flyway_schema_history WHERE version = '36';
-- =====================================================================
