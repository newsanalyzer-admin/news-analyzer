-- ============================================================================
-- V10: Create government_positions table
-- Story: FB-1.3 Position History & Term Tracking
-- Author: James (Dev Agent)
-- ============================================================================

-- Government positions (Congressional seats)
CREATE TABLE government_positions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    -- Position information
    title VARCHAR(100) NOT NULL,
    chamber VARCHAR(20) NOT NULL CHECK (chamber IN ('SENATE', 'HOUSE')),
    state VARCHAR(2) NOT NULL,
    district INTEGER,
    senate_class INTEGER CHECK (senate_class IS NULL OR senate_class BETWEEN 1 AND 3),
    position_type VARCHAR(20) NOT NULL CHECK (position_type IN ('ELECTED', 'APPOINTED', 'CAREER')),

    -- Organization link
    organization_id UUID REFERENCES government_organizations(id),

    -- Descriptive information
    description VARCHAR(500),

    -- Audit fields
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Constraints
    CONSTRAINT uk_position_seat UNIQUE (chamber, state, district),
    CONSTRAINT chk_senate_class CHECK (
        (chamber = 'SENATE' AND senate_class IS NOT NULL AND district IS NULL) OR
        (chamber = 'HOUSE' AND senate_class IS NULL AND district IS NOT NULL)
    )
);

-- Indexes for common queries
CREATE INDEX idx_position_chamber ON government_positions(chamber);
CREATE INDEX idx_position_state ON government_positions(state);
CREATE INDEX idx_position_org ON government_positions(organization_id) WHERE organization_id IS NOT NULL;

-- Comments
COMMENT ON TABLE government_positions IS 'Congressional seats (Senate and House positions)';
COMMENT ON COLUMN government_positions.chamber IS 'SENATE or HOUSE';
COMMENT ON COLUMN government_positions.state IS '2-letter state code';
COMMENT ON COLUMN government_positions.district IS 'Congressional district number (House only, null for Senate)';
COMMENT ON COLUMN government_positions.senate_class IS 'Senate class 1, 2, or 3 (Senate only, null for House)';
COMMENT ON COLUMN government_positions.position_type IS 'ELECTED for Congressional seats';
