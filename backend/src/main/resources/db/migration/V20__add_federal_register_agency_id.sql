-- V20__add_federal_register_agency_id.sql
-- Add Federal Register agency ID for high-confidence agency matching

ALTER TABLE government_organizations
ADD COLUMN IF NOT EXISTS federal_register_agency_id INTEGER;

CREATE INDEX IF NOT EXISTS idx_gov_org_federal_register_id
ON government_organizations(federal_register_agency_id)
WHERE federal_register_agency_id IS NOT NULL;

COMMENT ON COLUMN government_organizations.federal_register_agency_id
IS 'Federal Register API agency ID for high-confidence matching';
