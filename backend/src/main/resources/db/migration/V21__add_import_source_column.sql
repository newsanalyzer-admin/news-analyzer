-- V21__add_import_source_column.sql
-- Add import_source column to track where government organization records came from

ALTER TABLE government_organizations
ADD COLUMN IF NOT EXISTS import_source VARCHAR(50);

CREATE INDEX IF NOT EXISTS idx_gov_org_import_source
ON government_organizations(import_source)
WHERE import_source IS NOT NULL;

COMMENT ON COLUMN government_organizations.import_source
IS 'Source of the import (e.g., GOVMAN, MANUAL, FEDERAL_REGISTER)';
