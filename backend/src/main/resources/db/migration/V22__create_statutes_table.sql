-- V22__create_statutes_table.sql
-- US Code statutory sections imported from uscode.house.gov

CREATE TABLE statutes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    usc_identifier VARCHAR(100) UNIQUE NOT NULL,
    title_number INTEGER NOT NULL,
    title_name VARCHAR(500),
    chapter_number VARCHAR(20),
    chapter_name VARCHAR(500),
    section_number VARCHAR(50) NOT NULL,
    heading VARCHAR(1000),
    content_text TEXT,
    content_xml TEXT,
    source_credit VARCHAR(500),
    source_url VARCHAR(500),
    release_point VARCHAR(20),
    effective_date DATE,
    import_source VARCHAR(50),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for common queries
CREATE INDEX idx_statutes_title ON statutes(title_number);
CREATE INDEX idx_statutes_chapter ON statutes(title_number, chapter_number);
CREATE INDEX idx_statutes_section ON statutes(section_number);
CREATE UNIQUE INDEX idx_statutes_usc_identifier ON statutes(usc_identifier);

-- Full-text search index on content_text
CREATE INDEX idx_statutes_content_fts ON statutes USING gin(to_tsvector('english', content_text));

-- Table and column comments
COMMENT ON TABLE statutes IS 'US Code statutory sections from uscode.house.gov';
COMMENT ON COLUMN statutes.usc_identifier IS 'Unique USC citation path (e.g., /us/usc/t5/s101)';
COMMENT ON COLUMN statutes.title_number IS 'US Code title number (1-54)';
COMMENT ON COLUMN statutes.title_name IS 'Title heading (e.g., GOVERNMENT ORGANIZATION AND EMPLOYEES)';
COMMENT ON COLUMN statutes.chapter_number IS 'Chapter number within title (may be alphanumeric)';
COMMENT ON COLUMN statutes.chapter_name IS 'Chapter heading';
COMMENT ON COLUMN statutes.section_number IS 'Section number (may include letters/symbols)';
COMMENT ON COLUMN statutes.heading IS 'Section heading/title';
COMMENT ON COLUMN statutes.content_text IS 'Plain text content for search and display';
COMMENT ON COLUMN statutes.content_xml IS 'Original USLM XML content preserving structure';
COMMENT ON COLUMN statutes.source_credit IS 'Legislative history citation';
COMMENT ON COLUMN statutes.source_url IS 'URL to official source at uscode.house.gov';
COMMENT ON COLUMN statutes.release_point IS 'Release point identifier for tracking updates';
COMMENT ON COLUMN statutes.effective_date IS 'Effective date of this version (if specified)';
COMMENT ON COLUMN statutes.import_source IS 'Data source identifier (USCODE)';
