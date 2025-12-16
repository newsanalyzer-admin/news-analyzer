-- Fix entity_type constraint to accept uppercase enum values from Java
-- The Java EntityType enum uses uppercase (PERSON, GOVERNMENT_ORG, etc.)
-- but the original constraint only allowed lowercase

-- Drop the old constraint
ALTER TABLE entities DROP CONSTRAINT IF EXISTS check_entity_type;

-- Add new constraint that accepts uppercase values (matching Java enum)
ALTER TABLE entities ADD CONSTRAINT check_entity_type CHECK (entity_type IN (
    'PERSON', 'GOVERNMENT_ORG', 'ORGANIZATION',
    'LOCATION', 'EVENT', 'CONCEPT'
));

-- Update any existing lowercase values to uppercase (if any exist)
UPDATE entities SET entity_type = UPPER(entity_type) WHERE entity_type != UPPER(entity_type);
