-- =====================================================================
-- Migration: V2.9__enable_pg_extensions.sql
-- Description: Enable PostgreSQL extensions for advanced features
-- Author: Winston (Architect)
-- Date: 2025-11-21
-- Purpose: Prepare database for V3 government organizations migration
-- =====================================================================

-- Enable pg_trgm for fuzzy text matching (similarity, trigram indexes)
-- Used for: Fuzzy search of government organization names
CREATE EXTENSION IF NOT EXISTS pg_trgm;

-- Enable uuid-ossp for UUID generation (if not already enabled)
-- Used for: Primary key generation
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Enable btree_gin for composite indexes on JSONB + regular columns
-- Used for: Efficient JSONB queries with other columns
CREATE EXTENSION IF NOT EXISTS btree_gin;

-- Verify extensions
DO $$
BEGIN
    RAISE NOTICE 'PostgreSQL extensions enabled successfully:';
    RAISE NOTICE '  - pg_trgm: Fuzzy text matching and similarity';
    RAISE NOTICE '  - uuid-ossp: UUID generation';
    RAISE NOTICE '  - btree_gin: Composite GIN indexes';
END $$;
