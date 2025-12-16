-- V25__fix_senate_class_constraint.sql
-- Fix: Allow executive/judicial branch positions (chamber IS NULL)
-- The original constraint only allowed SENATE or HOUSE chamber values,
-- blocking all executive branch positions from being created.

-- Drop the existing overly restrictive constraint
ALTER TABLE government_positions DROP CONSTRAINT IF EXISTS chk_senate_class;

-- Add updated constraint that:
-- 1. For SENATE: senate_class required, district must be null
-- 2. For HOUSE: senate_class must be null, district required
-- 3. For NULL chamber (Executive/Judicial): senate_class and district must both be null
ALTER TABLE government_positions ADD CONSTRAINT chk_senate_class CHECK (
    (chamber = 'SENATE' AND senate_class IS NOT NULL AND district IS NULL) OR
    (chamber = 'HOUSE' AND senate_class IS NULL AND district IS NOT NULL) OR
    (chamber IS NULL AND senate_class IS NULL AND district IS NULL)
);

COMMENT ON CONSTRAINT chk_senate_class ON government_positions IS
    'Ensures consistent chamber/senate_class/district for legislative positions, and null values for executive/judicial';
