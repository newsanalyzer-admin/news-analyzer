-- V40__update_committee_membership_fk.sql
-- ARCH-1.5: Update CommitteeMembership to reference CongressionalMember
--
-- Committee membership is semantically Congressional - only Congressional members
-- can serve on Congressional committees. This migration updates the FK accordingly.

-- =====================================================================
-- Step 1: Rename column person_id → congressional_member_id
-- =====================================================================
ALTER TABLE committee_memberships
RENAME COLUMN person_id TO congressional_member_id;

-- =====================================================================
-- Step 2: Drop old FK constraint
-- =====================================================================
-- The old FK was pointing to persons (now congressional_members) via person_id
ALTER TABLE committee_memberships
DROP CONSTRAINT IF EXISTS committee_memberships_person_id_fkey;

-- =====================================================================
-- Step 3: Add new FK constraint with proper name
-- =====================================================================
ALTER TABLE committee_memberships
ADD CONSTRAINT fk_membership_congressional_member
    FOREIGN KEY (congressional_member_id)
    REFERENCES congressional_members(id);

-- =====================================================================
-- Step 4: Drop old unique constraint and add new one
-- =====================================================================
ALTER TABLE committee_memberships
DROP CONSTRAINT IF EXISTS uq_person_committee_congress;

ALTER TABLE committee_memberships
ADD CONSTRAINT uq_member_committee_congress
    UNIQUE (congressional_member_id, committee_code, congress);

-- =====================================================================
-- Step 5: Rename index if it exists
-- =====================================================================
ALTER INDEX IF EXISTS idx_membership_person RENAME TO idx_membership_congressional_member;

-- =====================================================================
-- Step 6: Update column comment
-- =====================================================================
COMMENT ON COLUMN committee_memberships.congressional_member_id IS 'FK to congressional_members table';

DO $$ BEGIN
    RAISE NOTICE 'V40 complete: committee_memberships now references congressional_members';
END $$;
