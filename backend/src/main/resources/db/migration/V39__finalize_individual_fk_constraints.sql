-- V39__finalize_individual_fk_constraints.sql
-- ARCH-1.4 Phase B: Finalize individual_id constraints and drop person_id
--
-- Prerequisites: V38 must have populated individual_id for all rows.

-- =====================================================================
-- Step 1: Verify all individual_id values are populated
-- =====================================================================
DO $$
DECLARE
    pres_null_count INTEGER;
    hold_null_count INTEGER;
BEGIN
    SELECT COUNT(*) INTO pres_null_count FROM presidencies WHERE individual_id IS NULL;
    SELECT COUNT(*) INTO hold_null_count FROM position_holdings WHERE individual_id IS NULL;

    IF pres_null_count > 0 THEN
        RAISE EXCEPTION 'Cannot finalize: % presidencies have NULL individual_id', pres_null_count;
    END IF;

    IF hold_null_count > 0 THEN
        RAISE EXCEPTION 'Cannot finalize: % position_holdings have NULL individual_id', hold_null_count;
    END IF;

    RAISE NOTICE 'Verification passed: all individual_id values are populated';
END $$;

-- =====================================================================
-- Step 2: Make individual_id NOT NULL on presidencies
-- =====================================================================
ALTER TABLE presidencies ALTER COLUMN individual_id SET NOT NULL;

-- =====================================================================
-- Step 3: Make individual_id NOT NULL on position_holdings
-- =====================================================================
ALTER TABLE position_holdings ALTER COLUMN individual_id SET NOT NULL;

-- =====================================================================
-- Step 4: Add FK constraints to individuals table
-- =====================================================================
ALTER TABLE presidencies
ADD CONSTRAINT fk_presidency_individual
FOREIGN KEY (individual_id) REFERENCES individuals(id);

ALTER TABLE position_holdings
ADD CONSTRAINT fk_holding_individual
FOREIGN KEY (individual_id) REFERENCES individuals(id);

-- =====================================================================
-- Step 5: Drop old person_id columns and their FK constraints
-- =====================================================================
-- First drop FK constraints (named in original migrations)
ALTER TABLE presidencies DROP CONSTRAINT IF EXISTS fk_presidency_person;
ALTER TABLE position_holdings DROP CONSTRAINT IF EXISTS position_holdings_person_id_fkey;

-- Then drop the columns
ALTER TABLE presidencies DROP COLUMN IF EXISTS person_id;
ALTER TABLE position_holdings DROP COLUMN IF EXISTS person_id;

-- =====================================================================
-- Step 6: Drop old indexes and verify new indexes exist
-- =====================================================================
DROP INDEX IF EXISTS idx_presidency_person;
DROP INDEX IF EXISTS idx_holding_person;

-- New indexes were created in V38, verify they exist
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_indexes WHERE indexname = 'idx_presidency_individual'
    ) THEN
        CREATE INDEX idx_presidency_individual ON presidencies(individual_id);
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM pg_indexes WHERE indexname = 'idx_holding_individual'
    ) THEN
        CREATE INDEX idx_holding_individual ON position_holdings(individual_id);
    END IF;
END $$;

-- =====================================================================
-- Step 7: Update column comments
-- =====================================================================
COMMENT ON COLUMN presidencies.individual_id IS 'FK to individuals table - the person who served as president';
COMMENT ON COLUMN position_holdings.individual_id IS 'FK to individuals table - the person who held this position';

-- =====================================================================
-- Verification
-- =====================================================================
DO $$ BEGIN
    RAISE NOTICE 'V39 Phase B complete: individual_id is now the primary FK for presidencies and position_holdings';
END $$;
