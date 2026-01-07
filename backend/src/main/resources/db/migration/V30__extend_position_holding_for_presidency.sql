-- V30__extend_position_holding_for_presidency.sql
-- KB-1.0: Extend PositionHolding for presidency support
-- Add presidency_id column for linking appointments to specific presidencies
-- Add new data sources for presidential data

-- =====================================================================
-- 1. Add presidency_id column to position_holdings
-- =====================================================================
-- FK constraint to presidencies table will be added in V33 after that table exists
ALTER TABLE position_holdings ADD COLUMN IF NOT EXISTS presidency_id UUID;

-- Create index for efficient presidency-based queries
CREATE INDEX IF NOT EXISTS idx_position_holdings_presidency ON position_holdings(presidency_id);

COMMENT ON COLUMN position_holdings.presidency_id IS
    'Optional link to a specific presidency for executive branch appointments (VP, Cabinet, CoS)';

-- =====================================================================
-- 2. Extend data_source constraints with new values
-- =====================================================================
-- Update position_holdings data_source constraint
ALTER TABLE position_holdings DROP CONSTRAINT IF EXISTS position_holdings_data_source_check;
ALTER TABLE position_holdings ADD CONSTRAINT position_holdings_data_source_check
    CHECK (data_source IN (
        'CONGRESS_GOV', 'GOVINFO', 'LEGISLATORS_REPO', 'PLUM_CSV', 'FJC', 'MANUAL',
        'USA_GOV', 'FEDERAL_REGISTER', 'WHITE_HOUSE_HISTORICAL'
    ));

-- Update persons data_source constraint
ALTER TABLE persons DROP CONSTRAINT IF EXISTS persons_data_source_check;
ALTER TABLE persons ADD CONSTRAINT persons_data_source_check
    CHECK (data_source IN (
        'CONGRESS_GOV', 'GOVINFO', 'LEGISLATORS_REPO', 'PLUM_CSV', 'FJC', 'MANUAL',
        'USA_GOV', 'FEDERAL_REGISTER', 'WHITE_HOUSE_HISTORICAL'
    ));

COMMENT ON CONSTRAINT position_holdings_data_source_check ON position_holdings IS
    'Allowed data sources: CONGRESS_GOV, GOVINFO, LEGISLATORS_REPO, PLUM_CSV, FJC, MANUAL, USA_GOV, FEDERAL_REGISTER, WHITE_HOUSE_HISTORICAL';

COMMENT ON CONSTRAINT persons_data_source_check ON persons IS
    'Allowed data sources: CONGRESS_GOV, GOVINFO, LEGISLATORS_REPO, PLUM_CSV, FJC, MANUAL, USA_GOV, FEDERAL_REGISTER, WHITE_HOUSE_HISTORICAL';
