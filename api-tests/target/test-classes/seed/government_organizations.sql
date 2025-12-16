-- Government Organizations Seed Data
-- Insert government organizations for API integration tests
-- Must be inserted before entities due to foreign key constraints

-- Cabinet Departments (EXECUTIVE branch)
INSERT INTO government_organizations (id, official_name, acronym, organization_type, government_branch, jurisdiction, website, mission, created_at, updated_at)
VALUES
    ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'Department of Justice', 'DOJ', 'DEPARTMENT', 'EXECUTIVE', 'Federal', 'https://www.justice.gov', 'To enforce the law and defend the interests of the United States', NOW(), NOW()),
    ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'Department of Defense', 'DOD', 'DEPARTMENT', 'EXECUTIVE', 'Federal', 'https://www.defense.gov', 'To provide the military forces needed to deter war and ensure national security', NOW(), NOW()),
    ('cccccccc-cccc-cccc-cccc-cccccccccccc', 'Department of State', 'DOS', 'DEPARTMENT', 'EXECUTIVE', 'Federal', 'https://www.state.gov', 'To lead Americas foreign policy through diplomacy, advocacy, and assistance', NOW(), NOW())
ON CONFLICT (id) DO NOTHING;

-- Independent Agencies
INSERT INTO government_organizations (id, official_name, acronym, organization_type, government_branch, jurisdiction, website, mission, created_at, updated_at)
VALUES
    ('dddddddd-dddd-dddd-dddd-dddddddddddd', 'Environmental Protection Agency', 'EPA', 'AGENCY', 'INDEPENDENT', 'Federal', 'https://www.epa.gov', 'To protect human health and the environment', NOW(), NOW()),
    ('eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee', 'National Aeronautics and Space Administration', 'NASA', 'AGENCY', 'INDEPENDENT', 'Federal', 'https://www.nasa.gov', 'To explore space for the benefit of humanity', NOW(), NOW()),
    ('ffffffff-ffff-ffff-ffff-ffffffffffff', 'Central Intelligence Agency', 'CIA', 'AGENCY', 'INDEPENDENT', 'Federal', 'https://www.cia.gov', 'To collect, analyze, and disseminate intelligence', NOW(), NOW())
ON CONFLICT (id) DO NOTHING;

-- Bureaus with parent relationships
INSERT INTO government_organizations (id, official_name, acronym, organization_type, government_branch, parent_id, jurisdiction, website, mission, created_at, updated_at)
VALUES
    ('11111111-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'Federal Bureau of Investigation', 'FBI', 'BUREAU', 'EXECUTIVE', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'Federal', 'https://www.fbi.gov', 'To protect the American people and uphold the Constitution', NOW(), NOW()),
    ('22222222-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'Bureau of Alcohol, Tobacco, Firearms and Explosives', 'ATF', 'BUREAU', 'EXECUTIVE', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'Federal', 'https://www.atf.gov', 'To protect communities from violent criminals and illegal trafficking', NOW(), NOW())
ON CONFLICT (id) DO NOTHING;
