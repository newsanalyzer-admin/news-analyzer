-- V15__add_tenure_to_position_holding.sql
-- FB-2.2: Add tenure field for executive position holdings

ALTER TABLE position_holdings
ADD COLUMN tenure INTEGER;

COMMENT ON COLUMN position_holdings.tenure IS 'Tenure code from PLUM data';
