-- Entities Seed Data
-- Insert entities for API integration tests
-- Note: government_org_id references are omitted to avoid FK conflicts with existing data

-- PERSON entities (politicians, officials)
INSERT INTO entities (id, name, entity_type, schema_org_type, properties, verified, created_at, updated_at)
VALUES
    ('11111111-1111-1111-1111-111111111111', 'Elizabeth Warren', 'PERSON', 'Person', '{"party": "Democratic", "position": "Senator", "state": "Massachusetts"}', true, NOW(), NOW()),
    ('22222222-1111-1111-1111-111111111111', 'Merrick Garland', 'PERSON', 'Person', '{"position": "Attorney General", "appointed": "2021"}', true, NOW(), NOW()),
    ('33333333-1111-1111-1111-111111111111', 'Janet Yellen', 'PERSON', 'Person', '{"position": "Secretary of the Treasury", "previousRole": "Fed Chair"}', true, NOW(), NOW()),
    ('44444444-1111-1111-1111-111111111111', 'Lloyd Austin', 'PERSON', 'Person', '{"position": "Secretary of Defense", "branch": "Army"}', true, NOW(), NOW()),
    ('55555555-1111-1111-1111-111111111111', 'Antony Blinken', 'PERSON', 'Person', '{"position": "Secretary of State"}', true, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET updated_at = NOW();

-- GOVERNMENT_ORG entities (without FK references to avoid conflicts)
INSERT INTO entities (id, name, entity_type, schema_org_type, verified, created_at, updated_at)
VALUES
    ('11111111-2222-2222-2222-222222222222', 'Environmental Protection Agency', 'GOVERNMENT_ORG', 'GovernmentOrganization', true, NOW(), NOW()),
    ('22222222-2222-2222-2222-222222222222', 'Federal Bureau of Investigation', 'GOVERNMENT_ORG', 'GovernmentOrganization', true, NOW(), NOW()),
    ('33333333-2222-2222-2222-222222222222', 'NASA', 'GOVERNMENT_ORG', 'GovernmentOrganization', true, NOW(), NOW()),
    ('44444444-2222-2222-2222-222222222222', 'Department of Justice', 'GOVERNMENT_ORG', 'GovernmentOrganization', true, NOW(), NOW()),
    ('55555555-2222-2222-2222-222222222222', 'Central Intelligence Agency', 'GOVERNMENT_ORG', 'GovernmentOrganization', true, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET updated_at = NOW();

-- ORGANIZATION entities (companies, NGOs)
INSERT INTO entities (id, name, entity_type, schema_org_type, properties, verified, created_at, updated_at)
VALUES
    ('11111111-3333-3333-3333-333333333333', 'Google LLC', 'ORGANIZATION', 'Organization', '{"industry": "Technology", "founded": "1998", "headquarters": "Mountain View, CA"}', true, NOW(), NOW()),
    ('22222222-3333-3333-3333-333333333333', 'American Civil Liberties Union', 'ORGANIZATION', 'Organization', '{"type": "Non-profit", "founded": "1920", "focus": "Civil liberties"}', true, NOW(), NOW()),
    ('33333333-3333-3333-3333-333333333333', 'ExxonMobil Corporation', 'ORGANIZATION', 'Organization', '{"industry": "Energy", "sector": "Oil and Gas"}', true, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET updated_at = NOW();

-- LOCATION entities (cities, states)
INSERT INTO entities (id, name, entity_type, schema_org_type, properties, verified, created_at, updated_at)
VALUES
    ('11111111-4444-4444-4444-444444444444', 'Washington, D.C.', 'LOCATION', 'Place', '{"type": "City", "country": "United States", "isCapital": true}', true, NOW(), NOW()),
    ('22222222-4444-4444-4444-444444444444', 'California', 'LOCATION', 'Place', '{"type": "State", "country": "United States", "population": 39538223}', true, NOW(), NOW()),
    ('33333333-4444-4444-4444-444444444444', 'New York City', 'LOCATION', 'Place', '{"type": "City", "state": "New York", "population": 8336817}', true, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET updated_at = NOW();
