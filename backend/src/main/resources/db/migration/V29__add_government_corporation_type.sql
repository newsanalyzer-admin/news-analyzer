-- V29: Add government_corporation organization type
-- Part of UI-6.0: Executive Organization Classification Updates
--
-- This migration classifies known government corporations in the executive branch.
-- Government corporations are federally chartered entities that operate like businesses
-- but serve public purposes.

-- Update known government corporations by name pattern
-- These are well-known federally chartered corporations

UPDATE government_organizations
SET org_type = 'government_corporation',
    updated_at = NOW()
WHERE branch = 'executive'
  AND org_type != 'government_corporation'
  AND (
    -- Postal Service
    official_name ILIKE '%United States Postal Service%'
    OR official_name ILIKE '%USPS%'

    -- Rail and Transportation
    OR official_name ILIKE '%Amtrak%'
    OR official_name ILIKE '%National Railroad Passenger Corporation%'

    -- Power and Utilities
    OR official_name ILIKE '%Tennessee Valley Authority%'
    OR official_name ILIKE '%TVA%'

    -- Financial Corporations
    OR official_name ILIKE '%Federal Deposit Insurance Corporation%'
    OR official_name ILIKE '%FDIC%'
    OR official_name ILIKE '%Export-Import Bank%'
    OR official_name ILIKE '%Overseas Private Investment Corporation%'
    OR official_name ILIKE '%OPIC%'
    OR official_name ILIKE '%Pension Benefit Guaranty Corporation%'
    OR official_name ILIKE '%PBGC%'

    -- Broadcasting
    OR official_name ILIKE '%Corporation for Public Broadcasting%'
    OR official_name ILIKE '%CPB%'

    -- Community Service
    OR official_name ILIKE '%Corporation for National and Community Service%'
    OR official_name ILIKE '%AmeriCorps%'

    -- Legal Services
    OR official_name ILIKE '%Legal Services Corporation%'

    -- Other Corporations
    OR official_name ILIKE '%Commodity Credit Corporation%'
    OR official_name ILIKE '%Federal Financing Bank%'
    OR official_name ILIKE '%Federal Prison Industries%'
    OR official_name ILIKE '%UNICOR%'
    OR official_name ILIKE '%Government National Mortgage Association%'
    OR official_name ILIKE '%Ginnie Mae%'
    OR official_name ILIKE '%Saint Lawrence Seaway Development Corporation%'

    -- Generic pattern for corporations (careful - may need manual review)
    OR (
      official_name ILIKE '%Corporation%'
      AND official_name NOT ILIKE '%Commission%'
      AND official_name NOT ILIKE '%Council%'
      AND org_type = 'independent_agency'
    )
  );

-- Log the update count (PostgreSQL will show this in migration output)
DO $$
DECLARE
    corp_count INTEGER;
BEGIN
    SELECT COUNT(*) INTO corp_count
    FROM government_organizations
    WHERE org_type = 'government_corporation';

    RAISE NOTICE 'Government corporations classified: %', corp_count;
END $$;
