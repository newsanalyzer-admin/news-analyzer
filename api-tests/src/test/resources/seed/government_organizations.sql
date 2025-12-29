-- Government Organizations Seed Data
-- Insert government organizations for API integration tests
-- Must be inserted before entities due to foreign key constraints

-- Cabinet Departments (executive branch)
INSERT INTO government_organizations (id, official_name, acronym, org_type, branch, website_url, mission_statement, created_at, updated_at)
VALUES
    ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'Department of Justice', 'DOJ', 'DEPARTMENT', 'executive', 'https://www.justice.gov', 'To enforce the law and defend the interests of the United States', NOW(), NOW()),
    ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'Department of Defense', 'DOD', 'DEPARTMENT', 'executive', 'https://www.defense.gov', 'To provide the military forces needed to deter war and ensure national security', NOW(), NOW()),
    ('cccccccc-cccc-cccc-cccc-cccccccccccc', 'Department of State', 'DOS', 'DEPARTMENT', 'executive', 'https://www.state.gov', 'To lead Americas foreign policy through diplomacy, advocacy, and assistance', NOW(), NOW())
ON CONFLICT (official_name) DO UPDATE SET updated_at = NOW();

-- Independent Agencies
INSERT INTO government_organizations (id, official_name, acronym, org_type, branch, website_url, mission_statement, created_at, updated_at)
VALUES
    ('dddddddd-dddd-dddd-dddd-dddddddddddd', 'Environmental Protection Agency', 'EPA', 'AGENCY', 'executive', 'https://www.epa.gov', 'To protect human health and the environment', NOW(), NOW()),
    ('eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee', 'National Aeronautics and Space Administration', 'NASA', 'AGENCY', 'executive', 'https://www.nasa.gov', 'To explore space for the benefit of humanity', NOW(), NOW()),
    ('ffffffff-ffff-ffff-ffff-ffffffffffff', 'Central Intelligence Agency', 'CIA', 'AGENCY', 'executive', 'https://www.cia.gov', 'To collect, analyze, and disseminate intelligence', NOW(), NOW())
ON CONFLICT (official_name) DO UPDATE SET updated_at = NOW();

-- Bureaus (without parent relationships to avoid FK issues with existing data)
INSERT INTO government_organizations (id, official_name, acronym, org_type, branch, website_url, mission_statement, created_at, updated_at)
VALUES
    ('11111111-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'Federal Bureau of Investigation', 'FBI', 'BUREAU', 'executive', 'https://www.fbi.gov', 'To protect the American people and uphold the Constitution', NOW(), NOW()),
    ('22222222-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'Bureau of Alcohol, Tobacco, Firearms and Explosives', 'ATF', 'BUREAU', 'executive', 'https://www.atf.gov', 'To protect communities from violent criminals and illegal trafficking', NOW(), NOW())
ON CONFLICT (official_name) DO UPDATE SET updated_at = NOW();
