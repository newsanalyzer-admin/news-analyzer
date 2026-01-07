-- V32__extend_person_for_potus.sql
-- KB-1.1: Extend Person entity with death_date and birth_place for POTUS data

-- =====================================================================
-- 1. Add death_date column to persons
-- =====================================================================
ALTER TABLE persons ADD COLUMN IF NOT EXISTS death_date DATE;

COMMENT ON COLUMN persons.death_date IS 'Date of death (null if living)';

-- =====================================================================
-- 2. Add birth_place column to persons
-- =====================================================================
ALTER TABLE persons ADD COLUMN IF NOT EXISTS birth_place VARCHAR(200);

COMMENT ON COLUMN persons.birth_place IS 'City, State of birth (e.g., "Westmoreland County, Virginia")';
