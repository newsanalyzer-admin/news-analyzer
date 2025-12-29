-- Cleanup Script
-- Truncate all test data tables for test isolation
-- Order matters: child tables first (FK constraints), then parent tables

-- Disable triggers temporarily for faster truncate
SET session_replication_role = 'replica';

-- Truncate entities first (references government_organizations)
TRUNCATE TABLE entities CASCADE;

-- Truncate government organizations
TRUNCATE TABLE government_organizations CASCADE;

-- Truncate persons (Congressional members)
TRUNCATE TABLE persons CASCADE;

-- Re-enable triggers
SET session_replication_role = 'origin';

-- Alternative: Delete without truncate (preserves sequence values)
-- DELETE FROM entities;
-- DELETE FROM government_organizations;
-- DELETE FROM persons;
