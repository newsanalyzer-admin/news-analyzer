-- Persons (Congressional Members) Seed Data
-- Insert sample Congress members for API integration tests
-- Part of FB-1.0: API Integration Test Coverage for Member APIs

-- Senate members (various parties and states)
INSERT INTO persons (id, bioguide_id, first_name, last_name, middle_name, party, state, chamber, birth_date, gender, image_url, data_source, created_at, updated_at)
VALUES
    ('aaaaaaaa-1111-1111-1111-111111111111', 'S000033', 'Bernard', 'Sanders', NULL, 'Independent', 'VT', 'SENATE', '1941-09-08', 'M', 'https://bioguide.congress.gov/bioguide/photo/S/S000033.jpg', 'CONGRESS_GOV', NOW(), NOW()),
    ('bbbbbbbb-1111-1111-1111-111111111111', 'M000355', 'Addison', 'McConnell', 'Mitchell', 'Republican', 'KY', 'SENATE', '1942-02-20', 'M', 'https://bioguide.congress.gov/bioguide/photo/M/M000355.jpg', 'CONGRESS_GOV', NOW(), NOW()),
    ('cccccccc-1111-1111-1111-111111111111', 'W000817', 'Elizabeth', 'Warren', 'Ann', 'Democratic', 'MA', 'SENATE', '1949-06-22', 'F', 'https://bioguide.congress.gov/bioguide/photo/W/W000817.jpg', 'CONGRESS_GOV', NOW(), NOW()),
    ('dddddddd-1111-1111-1111-111111111111', 'C001098', 'Rafael', 'Cruz', 'Edward', 'Republican', 'TX', 'SENATE', '1970-12-22', 'M', 'https://bioguide.congress.gov/bioguide/photo/C/C001098.jpg', 'CONGRESS_GOV', NOW(), NOW())
ON CONFLICT (bioguide_id) DO NOTHING;

-- House members (various parties and states)
INSERT INTO persons (id, bioguide_id, first_name, last_name, middle_name, party, state, chamber, birth_date, gender, image_url, data_source, created_at, updated_at)
VALUES
    ('eeeeeeee-1111-1111-1111-111111111111', 'P000197', 'Nancy', 'Pelosi', NULL, 'Democratic', 'CA', 'HOUSE', '1940-03-26', 'F', 'https://bioguide.congress.gov/bioguide/photo/P/P000197.jpg', 'CONGRESS_GOV', NOW(), NOW()),
    ('ffffffff-1111-1111-1111-111111111111', 'O000172', 'Alexandria', 'Ocasio-Cortez', NULL, 'Democratic', 'NY', 'HOUSE', '1989-10-13', 'F', 'https://bioguide.congress.gov/bioguide/photo/O/O000172.jpg', 'CONGRESS_GOV', NOW(), NOW()),
    ('11111111-2222-1111-1111-111111111111', 'J000289', 'Jim', 'Jordan', NULL, 'Republican', 'OH', 'HOUSE', '1964-02-17', 'M', 'https://bioguide.congress.gov/bioguide/photo/J/J000289.jpg', 'CONGRESS_GOV', NOW(), NOW()),
    ('22222222-2222-1111-1111-111111111111', 'G000553', 'Al', 'Green', NULL, 'Democratic', 'TX', 'HOUSE', '1947-09-01', 'M', 'https://bioguide.congress.gov/bioguide/photo/G/G000553.jpg', 'CONGRESS_GOV', NOW(), NOW())
ON CONFLICT (bioguide_id) DO NOTHING;
