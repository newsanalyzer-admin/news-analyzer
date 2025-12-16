-- V17__create_regulation_table.sql
-- Federal Register regulatory documents table

CREATE TABLE regulations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    document_number VARCHAR(50) NOT NULL UNIQUE,
    title VARCHAR(1000) NOT NULL,
    abstract TEXT,
    document_type VARCHAR(30) NOT NULL,
    publication_date DATE NOT NULL,
    effective_on DATE,
    signing_date DATE,
    regulation_id_number VARCHAR(20),
    cfr_references JSONB,
    docket_ids JSONB,
    source_url VARCHAR(500),
    pdf_url VARCHAR(500),
    html_url VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for common queries
CREATE INDEX idx_regulation_document_number ON regulations(document_number);
CREATE INDEX idx_regulation_publication_date ON regulations(publication_date);
CREATE INDEX idx_regulation_effective_on ON regulations(effective_on);
CREATE INDEX idx_regulation_document_type ON regulations(document_type);

-- JSONB GIN index for CFR reference queries (Architect recommendation)
CREATE INDEX idx_regulation_cfr_references ON regulations USING GIN(cfr_references);

-- Table and column comments
COMMENT ON TABLE regulations IS 'Federal Register regulatory documents';
COMMENT ON COLUMN regulations.document_number IS 'Federal Register document number (unique identifier)';
COMMENT ON COLUMN regulations.abstract IS 'Summary/abstract of the regulation';
COMMENT ON COLUMN regulations.document_type IS 'Type: RULE, PROPOSED_RULE, NOTICE, PRESIDENTIAL_DOCUMENT, OTHER';
COMMENT ON COLUMN regulations.publication_date IS 'Date published in Federal Register';
COMMENT ON COLUMN regulations.effective_on IS 'Date rule becomes effective (for final rules)';
COMMENT ON COLUMN regulations.signing_date IS 'Date signed (for presidential documents)';
COMMENT ON COLUMN regulations.regulation_id_number IS 'Regulation Identifier Number (RIN) from OMB';
COMMENT ON COLUMN regulations.cfr_references IS 'Code of Federal Regulations citations (JSONB array)';
COMMENT ON COLUMN regulations.docket_ids IS 'Associated docket identifiers (JSONB array)';
