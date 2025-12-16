-- V26__add_plum_csv_data_source.sql
-- Add PLUM_CSV to allowed data sources for position_holdings and persons tables

-- Update position_holdings data_source constraint
ALTER TABLE position_holdings DROP CONSTRAINT IF EXISTS position_holdings_data_source_check;
ALTER TABLE position_holdings ADD CONSTRAINT position_holdings_data_source_check
    CHECK (data_source IN ('CONGRESS_GOV', 'GOVINFO', 'LEGISLATORS_REPO', 'PLUM_CSV', 'MANUAL'));

-- Update persons data_source constraint if it exists
ALTER TABLE persons DROP CONSTRAINT IF EXISTS persons_data_source_check;
ALTER TABLE persons ADD CONSTRAINT persons_data_source_check
    CHECK (data_source IN ('CONGRESS_GOV', 'GOVINFO', 'LEGISLATORS_REPO', 'PLUM_CSV', 'MANUAL'));

COMMENT ON CONSTRAINT position_holdings_data_source_check ON position_holdings IS
    'Allowed data sources: CONGRESS_GOV, GOVINFO, LEGISLATORS_REPO, PLUM_CSV, MANUAL';
