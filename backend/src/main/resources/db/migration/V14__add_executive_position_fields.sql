-- V14__add_executive_position_fields.sql
-- FB-2.2: Add additional executive position fields from PLUM data

ALTER TABLE government_positions
ADD COLUMN pay_plan VARCHAR(10),
ADD COLUMN pay_grade VARCHAR(10),
ADD COLUMN location VARCHAR(255),
ADD COLUMN expiration_date DATE;

CREATE INDEX idx_position_expiration ON government_positions(expiration_date);

COMMENT ON COLUMN government_positions.pay_plan IS 'Pay plan code (EX, ES, GS, etc.)';
COMMENT ON COLUMN government_positions.pay_grade IS 'Pay grade/level (I, II, III, etc.)';
COMMENT ON COLUMN government_positions.location IS 'Work location';
COMMENT ON COLUMN government_positions.expiration_date IS 'Term expiration date for appointed positions';
