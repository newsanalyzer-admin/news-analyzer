-- V9__add_person_social_media.sql
-- Add social media and enrichment tracking fields to persons table
-- Part of FB-1.4: Congress-Legislators Enrichment Sync

-- Add social media JSONB column for Twitter, Facebook, YouTube, Instagram handles
ALTER TABLE persons
ADD COLUMN IF NOT EXISTS social_media JSONB DEFAULT '{}'::jsonb;

-- Add enrichment tracking fields
ALTER TABLE persons
ADD COLUMN IF NOT EXISTS enrichment_source VARCHAR(50);

ALTER TABLE persons
ADD COLUMN IF NOT EXISTS enrichment_version VARCHAR(50);

-- Add index for social media queries (e.g., find by Twitter handle)
CREATE INDEX IF NOT EXISTS idx_persons_social_media ON persons USING gin(social_media);

-- Comments
COMMENT ON COLUMN persons.social_media IS 'Social media handles: {twitter: "...", facebook: "...", youtube: "...", instagram: "..."}';
COMMENT ON COLUMN persons.enrichment_source IS 'Source of enrichment data (e.g., LEGISLATORS_REPO)';
COMMENT ON COLUMN persons.enrichment_version IS 'Git commit SHA or version of the enrichment data source';
