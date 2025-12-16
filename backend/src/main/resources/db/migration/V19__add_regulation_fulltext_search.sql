-- V19__add_regulation_fulltext_search.sql
-- Add full-text search capability on regulation title and abstract

-- Add full-text search vector column (generated/stored)
ALTER TABLE regulations ADD COLUMN search_vector tsvector
    GENERATED ALWAYS AS (
        setweight(to_tsvector('english', coalesce(title, '')), 'A') ||
        setweight(to_tsvector('english', coalesce(abstract, '')), 'B')
    ) STORED;

-- GIN index for fast full-text search
CREATE INDEX idx_regulation_search ON regulations USING GIN(search_vector);

-- Column comment
COMMENT ON COLUMN regulations.search_vector IS 'Full-text search vector for title (weight A) and abstract (weight B)';
