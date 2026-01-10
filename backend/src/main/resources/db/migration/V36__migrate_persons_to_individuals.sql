-- V36__migrate_persons_to_individuals.sql
-- ARCH-1.3: Migrate existing person data to individuals table
-- This migration populates the individuals table from persons and links them.

-- =====================================================================
-- Step 1: Insert unique individuals from persons (deduplicated)
-- =====================================================================
-- Uses DISTINCT ON to handle duplicates with same name + birth_date
-- Takes the earliest created_at record as the "primary" in case of duplicates

INSERT INTO individuals (
    id,
    first_name,
    last_name,
    middle_name,
    suffix,
    birth_date,
    death_date,
    birth_place,
    gender,
    image_url,
    party,
    external_ids,
    social_media,
    primary_data_source,
    created_at,
    updated_at
)
SELECT
    gen_random_uuid(),
    first_name,
    last_name,
    middle_name,
    suffix,
    birth_date,
    death_date,
    birth_place,
    gender,
    image_url,
    party,
    external_ids,
    social_media,
    data_source::VARCHAR,
    COALESCE(created_at, NOW()),
    NOW()
FROM (
    SELECT DISTINCT ON (
        LOWER(first_name),
        LOWER(last_name),
        COALESCE(birth_date, '1800-01-01'::DATE)
    )
        first_name,
        last_name,
        middle_name,
        suffix,
        birth_date,
        death_date,
        birth_place,
        gender,
        image_url,
        party,
        external_ids,
        social_media,
        data_source,
        created_at
    FROM persons
    ORDER BY
        LOWER(first_name),
        LOWER(last_name),
        COALESCE(birth_date, '1800-01-01'::DATE),
        created_at ASC  -- Take earliest record
) AS deduped
ON CONFLICT DO NOTHING;

-- =====================================================================
-- Step 2: Add individual_id column to persons (if not exists)
-- =====================================================================
ALTER TABLE persons ADD COLUMN IF NOT EXISTS individual_id UUID;

-- =====================================================================
-- Step 3: Populate individual_id by matching name + birth_date
-- =====================================================================
-- Match with birth_date
UPDATE persons p
SET individual_id = i.id
FROM individuals i
WHERE LOWER(p.first_name) = LOWER(i.first_name)
  AND LOWER(p.last_name) = LOWER(i.last_name)
  AND p.birth_date = i.birth_date
  AND p.individual_id IS NULL;

-- Match without birth_date (both null)
UPDATE persons p
SET individual_id = i.id
FROM individuals i
WHERE LOWER(p.first_name) = LOWER(i.first_name)
  AND LOWER(p.last_name) = LOWER(i.last_name)
  AND p.birth_date IS NULL
  AND i.birth_date IS NULL
  AND p.individual_id IS NULL;

-- Match persons without birth_date to individuals with birth_date
-- (only if there's exactly one match by name)
UPDATE persons p
SET individual_id = (
    SELECT i.id
    FROM individuals i
    WHERE LOWER(p.first_name) = LOWER(i.first_name)
      AND LOWER(p.last_name) = LOWER(i.last_name)
    LIMIT 1
)
WHERE p.individual_id IS NULL
  AND p.birth_date IS NULL
  AND (
      SELECT COUNT(*)
      FROM individuals i
      WHERE LOWER(p.first_name) = LOWER(i.first_name)
        AND LOWER(p.last_name) = LOWER(i.last_name)
  ) = 1;

-- =====================================================================
-- Step 4: Create index on individual_id for faster joins
-- =====================================================================
CREATE INDEX IF NOT EXISTS idx_persons_individual_id ON persons(individual_id);

-- =====================================================================
-- Step 5: Verification - Log any remaining unlinked records
-- =====================================================================
DO $$
DECLARE
    unlinked_count INTEGER;
    unlinked_record RECORD;
BEGIN
    SELECT COUNT(*) INTO unlinked_count FROM persons WHERE individual_id IS NULL;

    IF unlinked_count > 0 THEN
        RAISE WARNING 'Migration notice: % persons have NULL individual_id', unlinked_count;

        -- Log the first few unlinked records for debugging
        FOR unlinked_record IN
            SELECT id, first_name, last_name, bioguide_id
            FROM persons
            WHERE individual_id IS NULL
            LIMIT 5
        LOOP
            RAISE WARNING 'Unlinked: % % (bioguide: %, id: %)',
                unlinked_record.first_name,
                unlinked_record.last_name,
                unlinked_record.bioguide_id,
                unlinked_record.id;
        END LOOP;

        -- For now, create individuals for any remaining unlinked persons
        INSERT INTO individuals (
            id, first_name, last_name, middle_name, suffix,
            birth_date, death_date, birth_place, gender, image_url,
            party, external_ids, social_media, primary_data_source,
            created_at, updated_at
        )
        SELECT
            gen_random_uuid(),
            p.first_name, p.last_name, p.middle_name, p.suffix,
            p.birth_date, p.death_date, p.birth_place, p.gender, p.image_url,
            p.party, p.external_ids, p.social_media, p.data_source::VARCHAR,
            COALESCE(p.created_at, NOW()), NOW()
        FROM persons p
        WHERE p.individual_id IS NULL;

        -- Link these newly created individuals
        UPDATE persons p
        SET individual_id = i.id
        FROM individuals i
        WHERE LOWER(p.first_name) = LOWER(i.first_name)
          AND LOWER(p.last_name) = LOWER(i.last_name)
          AND (p.birth_date = i.birth_date OR (p.birth_date IS NULL AND i.birth_date IS NULL))
          AND p.individual_id IS NULL;
    END IF;
END $$;

-- =====================================================================
-- Step 6: Final verification - fail if any unlinked records remain
-- =====================================================================
DO $$
DECLARE
    unlinked_count INTEGER;
BEGIN
    SELECT COUNT(*) INTO unlinked_count FROM persons WHERE individual_id IS NULL;

    IF unlinked_count > 0 THEN
        RAISE EXCEPTION 'Migration failed: % persons still have NULL individual_id after all attempts', unlinked_count;
    END IF;

    RAISE NOTICE 'Migration successful: All persons linked to individuals';
END $$;

-- =====================================================================
-- Verification queries (for manual checking)
-- =====================================================================
-- These are comments for reference, not executed

-- Count comparison:
-- SELECT
--     (SELECT COUNT(*) FROM persons) as persons_count,
--     (SELECT COUNT(*) FROM individuals) as individuals_count,
--     (SELECT COUNT(DISTINCT individual_id) FROM persons) as linked_individual_count;

-- Check for orphaned records:
-- SELECT * FROM persons WHERE individual_id IS NULL;

-- Check for duplicates in individuals (should be none due to unique index):
-- SELECT first_name, last_name, birth_date, COUNT(*)
-- FROM individuals
-- GROUP BY first_name, last_name, birth_date
-- HAVING COUNT(*) > 1;

COMMENT ON COLUMN persons.individual_id IS 'FK to individuals table - links congressional member to their biographical record';
