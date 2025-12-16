-- Migration V4: Add Entity-to-GovernmentOrganization Foreign Key
-- Purpose: Link extracted entities to authoritative government organization records
-- Author: Winston (Architect Agent)
-- Date: 2025-11-23
-- Reference: docs/architecture/entity-vs-government-org-design.md (lines 333-351)

-- Add government_org_id column to entities table
ALTER TABLE entities
ADD COLUMN government_org_id UUID;

-- Add foreign key constraint
ALTER TABLE entities
ADD CONSTRAINT fk_entities_government_org
    FOREIGN KEY (government_org_id)
    REFERENCES government_organizations(id)
    ON DELETE SET NULL;  -- If gov org deleted, set entity link to NULL (don't cascade delete entities)

-- Add index for query performance
CREATE INDEX idx_entities_gov_org_id ON entities(government_org_id);

-- Add composite index for common query: entity_type + government_org_id
CREATE INDEX idx_entities_type_gov_org ON entities(entity_type, government_org_id)
    WHERE entity_type = 'GOVERNMENT_ORG';

-- Add comment for documentation
COMMENT ON COLUMN entities.government_org_id IS 'Foreign key to government_organizations table - links extracted entities to authoritative government organization records (Master Data Management pattern)';

-- Performance note: The WHERE clause creates a partial index only for government_org entity types,
-- reducing index size and improving query performance for the most common use case.
