-- V12__add_branch_and_nullable_fields.sql
-- FB-2.2: Enable executive branch positions in GovernmentPosition entity

-- Add branch column with default for existing legislative data
ALTER TABLE government_positions
ADD COLUMN branch VARCHAR(20) DEFAULT 'LEGISLATIVE';

-- Set all existing positions to LEGISLATIVE
UPDATE government_positions SET branch = 'LEGISLATIVE';

-- Make branch required going forward
ALTER TABLE government_positions
ALTER COLUMN branch SET NOT NULL;

-- Make chamber nullable (executive positions don't have chamber)
ALTER TABLE government_positions ALTER COLUMN chamber DROP NOT NULL;

-- Make state nullable (executive positions are federal-level)
ALTER TABLE government_positions ALTER COLUMN state DROP NOT NULL;

-- Drop existing unique constraint if it exists
ALTER TABLE government_positions DROP CONSTRAINT IF EXISTS uk_position_seat;

-- Create partial index for legislative positions (chamber + state + district)
CREATE UNIQUE INDEX uk_legislative_seat
ON government_positions (chamber, state, district)
WHERE branch = 'LEGISLATIVE';

-- Create partial index for executive positions (title + organization)
CREATE UNIQUE INDEX uk_executive_position
ON government_positions (title, organization_id)
WHERE branch = 'EXECUTIVE';

-- Add index on branch column for filtering
CREATE INDEX idx_position_branch ON government_positions(branch);

COMMENT ON COLUMN government_positions.branch IS
    'Government branch: LEGISLATIVE, EXECUTIVE, or JUDICIAL';
