-- V27__add_fjc_data_source.sql
-- Add FJC (Federal Judicial Center) to allowed data sources for position_holdings and persons tables

-- Update position_holdings data_source constraint
ALTER TABLE position_holdings DROP CONSTRAINT IF EXISTS position_holdings_data_source_check;
ALTER TABLE position_holdings ADD CONSTRAINT position_holdings_data_source_check
    CHECK (data_source IN ('CONGRESS_GOV', 'GOVINFO', 'LEGISLATORS_REPO', 'PLUM_CSV', 'FJC', 'MANUAL'));

-- Update persons data_source constraint
ALTER TABLE persons DROP CONSTRAINT IF EXISTS persons_data_source_check;
ALTER TABLE persons ADD CONSTRAINT persons_data_source_check
    CHECK (data_source IN ('CONGRESS_GOV', 'GOVINFO', 'LEGISLATORS_REPO', 'PLUM_CSV', 'FJC', 'MANUAL'));

COMMENT ON CONSTRAINT position_holdings_data_source_check ON position_holdings IS
    'Allowed data sources: CONGRESS_GOV, GOVINFO, LEGISLATORS_REPO, PLUM_CSV, FJC, MANUAL';

COMMENT ON CONSTRAINT persons_data_source_check ON persons IS
    'Allowed data sources: CONGRESS_GOV, GOVINFO, LEGISLATORS_REPO, PLUM_CSV, FJC, MANUAL';
