-- ============================================================================
-- V11: Create position_holdings table
-- Story: FB-1.3 Position History & Term Tracking
-- Author: James (Dev Agent)
-- ============================================================================

-- Position holdings (who held what position when)
CREATE TABLE position_holdings (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    -- Relationships
    person_id UUID NOT NULL REFERENCES persons(id) ON DELETE CASCADE,
    position_id UUID NOT NULL REFERENCES government_positions(id) ON DELETE CASCADE,

    -- Temporal information
    start_date DATE NOT NULL,
    end_date DATE,
    congress INTEGER CHECK (congress IS NULL OR congress > 0),

    -- Data source tracking
    data_source VARCHAR(50) NOT NULL CHECK (data_source IN ('CONGRESS_GOV', 'GOVINFO', 'LEGISLATORS_REPO', 'MANUAL')),
    source_reference VARCHAR(200),

    -- Audit fields
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Constraints
    CONSTRAINT chk_date_order CHECK (end_date IS NULL OR end_date >= start_date)
);

-- Indexes for common queries
CREATE INDEX idx_holding_person ON position_holdings(person_id);
CREATE INDEX idx_holding_position ON position_holdings(position_id);
CREATE INDEX idx_holding_dates ON position_holdings(start_date, end_date);
CREATE INDEX idx_holding_congress ON position_holdings(congress) WHERE congress IS NOT NULL;

-- Index for "who was in office on date X" queries
CREATE INDEX idx_holding_date_range ON position_holdings(start_date, end_date)
    WHERE end_date IS NOT NULL;

-- Index for current holdings (end_date is null)
CREATE INDEX idx_holding_current ON position_holdings(person_id, position_id)
    WHERE end_date IS NULL;

-- Comments
COMMENT ON TABLE position_holdings IS 'Temporal join table tracking who held what position when';
COMMENT ON COLUMN position_holdings.start_date IS 'Date the person started holding the position';
COMMENT ON COLUMN position_holdings.end_date IS 'Date the person stopped holding the position (null = current)';
COMMENT ON COLUMN position_holdings.congress IS 'Congress number (e.g., 118 for 118th Congress)';
COMMENT ON COLUMN position_holdings.data_source IS 'Where this data came from';
