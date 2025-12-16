-- V8: Create committee_membership table
-- Join table linking persons to committees with their roles
-- Part of FB-1.2: Committee Data Integration

-- Membership role enum
CREATE TYPE membership_role AS ENUM ('CHAIR', 'VICE_CHAIR', 'RANKING_MEMBER', 'MEMBER', 'EX_OFFICIO');

-- Committee membership join table
CREATE TABLE committee_memberships (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    -- Foreign keys
    person_id UUID NOT NULL,
    committee_code VARCHAR(20) NOT NULL,

    -- Membership details
    role membership_role NOT NULL DEFAULT 'MEMBER',
    congress INTEGER NOT NULL,                    -- Congressional session (e.g., 118, 119)
    start_date DATE,
    end_date DATE,

    -- Data source tracking
    congress_last_sync TIMESTAMP,
    data_source VARCHAR(50) DEFAULT 'CONGRESS_GOV',

    -- Audit fields
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by VARCHAR(100) DEFAULT 'system',
    updated_by VARCHAR(100) DEFAULT 'system',

    -- Foreign key constraints
    CONSTRAINT fk_membership_person
        FOREIGN KEY (person_id)
        REFERENCES persons(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_membership_committee
        FOREIGN KEY (committee_code)
        REFERENCES committees(committee_code)
        ON DELETE CASCADE,

    -- Unique constraint: one membership per person/committee/congress
    CONSTRAINT uq_person_committee_congress
        UNIQUE (person_id, committee_code, congress)
);

-- Indexes
CREATE INDEX idx_membership_person ON committee_memberships(person_id);
CREATE INDEX idx_membership_committee ON committee_memberships(committee_code);
CREATE INDEX idx_membership_congress ON committee_memberships(congress);
CREATE INDEX idx_membership_role ON committee_memberships(role);
CREATE INDEX idx_membership_person_congress ON committee_memberships(person_id, congress);

-- Comments
COMMENT ON TABLE committee_memberships IS 'Links persons to committees with their roles per congressional session';
COMMENT ON COLUMN committee_memberships.congress IS 'Congressional session number (e.g., 118, 119)';
COMMENT ON COLUMN committee_memberships.role IS 'Member role on the committee (CHAIR, RANKING_MEMBER, etc.)';
