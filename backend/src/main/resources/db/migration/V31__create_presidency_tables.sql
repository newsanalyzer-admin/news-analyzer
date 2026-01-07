-- V31__create_presidency_tables.sql
-- KB-1.1: Create Presidency and ExecutiveOrder tables

-- =====================================================================
-- 1. Create presidencies table
-- =====================================================================
CREATE TABLE IF NOT EXISTS presidencies (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    -- Relationships
    person_id UUID NOT NULL,
    predecessor_id UUID,
    successor_id UUID,

    -- Presidency Information
    number INTEGER NOT NULL,
    party VARCHAR(100),
    election_year INTEGER,

    -- Temporal Information
    start_date DATE NOT NULL,
    end_date DATE,
    end_reason VARCHAR(20),

    -- Data Source Tracking
    data_source VARCHAR(50) NOT NULL,
    source_reference VARCHAR(200),

    -- Audit Fields
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Constraints
    CONSTRAINT uk_presidency_number UNIQUE (number),
    CONSTRAINT fk_presidency_person FOREIGN KEY (person_id) REFERENCES persons(id),
    CONSTRAINT fk_presidency_predecessor FOREIGN KEY (predecessor_id) REFERENCES presidencies(id),
    CONSTRAINT fk_presidency_successor FOREIGN KEY (successor_id) REFERENCES presidencies(id),
    CONSTRAINT presidencies_end_reason_check CHECK (end_reason IN ('TERM_END', 'DEATH', 'RESIGNATION', 'SUCCESSION')),
    CONSTRAINT presidencies_data_source_check CHECK (data_source IN (
        'CONGRESS_GOV', 'GOVINFO', 'LEGISLATORS_REPO', 'PLUM_CSV', 'FJC', 'MANUAL',
        'USA_GOV', 'FEDERAL_REGISTER', 'WHITE_HOUSE_HISTORICAL'
    ))
);

-- Indexes for presidencies
CREATE INDEX IF NOT EXISTS idx_presidency_person ON presidencies(person_id);
CREATE INDEX IF NOT EXISTS idx_presidency_number ON presidencies(number);
CREATE INDEX IF NOT EXISTS idx_presidency_dates ON presidencies(start_date, end_date);

COMMENT ON TABLE presidencies IS 'Presidential terms (1-47). Separates office from person for non-consecutive terms (Cleveland, Trump).';
COMMENT ON COLUMN presidencies.number IS 'Presidency number (1-47), unique historical identifier';
COMMENT ON COLUMN presidencies.election_year IS 'Year elected (null if succeeded to office without election, e.g., Ford)';
COMMENT ON COLUMN presidencies.end_reason IS 'How presidency ended: TERM_END, DEATH, RESIGNATION, SUCCESSION';

-- =====================================================================
-- 2. Create executive_orders table
-- =====================================================================
CREATE TABLE IF NOT EXISTS executive_orders (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    -- Relationships
    presidency_id UUID NOT NULL,

    -- Executive Order Information
    eo_number INTEGER NOT NULL,
    title VARCHAR(500) NOT NULL,
    signing_date DATE NOT NULL,
    summary TEXT,

    -- Federal Register Information
    federal_register_citation VARCHAR(100),
    federal_register_url VARCHAR(500),

    -- Status Information
    status VARCHAR(20) NOT NULL,
    revoked_by_eo INTEGER,

    -- Data Source Tracking
    data_source VARCHAR(50) NOT NULL,
    source_reference VARCHAR(200),

    -- Audit Fields
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Constraints
    CONSTRAINT uk_eo_number UNIQUE (eo_number),
    CONSTRAINT fk_eo_presidency FOREIGN KEY (presidency_id) REFERENCES presidencies(id),
    CONSTRAINT executive_orders_status_check CHECK (status IN ('ACTIVE', 'REVOKED', 'SUPERSEDED')),
    CONSTRAINT executive_orders_data_source_check CHECK (data_source IN (
        'CONGRESS_GOV', 'GOVINFO', 'LEGISLATORS_REPO', 'PLUM_CSV', 'FJC', 'MANUAL',
        'USA_GOV', 'FEDERAL_REGISTER', 'WHITE_HOUSE_HISTORICAL'
    ))
);

-- Indexes for executive_orders
CREATE INDEX IF NOT EXISTS idx_eo_presidency ON executive_orders(presidency_id);
CREATE INDEX IF NOT EXISTS idx_eo_number ON executive_orders(eo_number);
CREATE INDEX IF NOT EXISTS idx_eo_signing_date ON executive_orders(signing_date);
CREATE INDEX IF NOT EXISTS idx_eo_status ON executive_orders(status);

COMMENT ON TABLE executive_orders IS 'Presidential Executive Orders with metadata (not full text). Linked to presidencies.';
COMMENT ON COLUMN executive_orders.eo_number IS 'Executive Order number (unique identifier)';
COMMENT ON COLUMN executive_orders.summary IS 'Abstract/summary from Federal Register (not full text)';
COMMENT ON COLUMN executive_orders.revoked_by_eo IS 'EO number that revoked this order (if applicable)';
