-- Sample data for development and testing
-- This file populates the database with realistic test data

-- Insert sample external API sources
INSERT INTO external_api_sources (name, base_url, api_type, description, requires_auth, rate_limit_per_hour, reliability_score) VALUES
('Congress.gov API', 'https://api.congress.gov/v3', 'government', 'Official US Congressional data including bills, votes, and member information', true, 5000, 0.98),
('Federal Register API', 'https://www.federalregister.gov/api/v1', 'government', 'Federal regulations and government documents', false, 1000, 0.95),
('CDC Data API', 'https://data.cdc.gov/api', 'government', 'Centers for Disease Control health and medical data', false, 2000, 0.97),
('PubMed API', 'https://eutils.ncbi.nlm.nih.gov/entrez/eutils', 'academic', 'Medical and life science research articles', false, 3, 0.92),
('arXiv API', 'http://export.arxiv.org/api', 'academic', 'Preprint scientific papers', false, 1000, 0.85),
('CrossRef API', 'https://api.crossref.org', 'academic', 'Scholarly publication metadata and DOI resolution', false, 50, 0.90);

-- Insert sample news sources
INSERT INTO news_sources (source_name, domain, reliability_score, political_bias_score, founded_date, description, source_type) VALUES
('Reuters', 'reuters.com', 0.89, 0.05, '1851-01-01', 'International news agency known for factual reporting', 'news'),
('Associated Press', 'apnews.com', 0.91, 0.02, '1846-01-01', 'American news agency with strong fact-checking standards', 'news'),
('BBC News', 'bbc.com', 0.87, -0.10, '1922-01-01', 'British public service broadcaster with global coverage', 'news'),
('NPR', 'npr.org', 0.85, -0.15, '1970-01-01', 'American public radio network with in-depth reporting', 'news'),
('Wall Street Journal', 'wsj.com', 0.83, 0.20, '1889-01-01', 'American business and financial news publication', 'news'),
('The Guardian', 'theguardian.com', 0.81, -0.25, '1821-01-01', 'British daily newspaper with liberal editorial stance', 'news'),
('Fox News', 'foxnews.com', 0.65, 0.45, '1996-01-01', 'American conservative news channel', 'news'),
('CNN', 'cnn.com', 0.73, -0.20, '1980-01-01', 'American news network with liberal editorial stance', 'news'),
('Politico', 'politico.com', 0.78, -0.05, '2007-01-01', 'Political news and analysis publication', 'news'),
('Breitbart', 'breitbart.com', 0.45, 0.65, '2007-01-01', 'Conservative news and opinion website', 'news'),
('HuffPost', 'huffpost.com', 0.58, -0.35, '2005-01-01', 'Liberal news and opinion website', 'news'),
('Medium', 'medium.com', 0.60, 0.00, '2012-01-01', 'Online publishing platform with varied content quality', 'blog'),
('Substack', 'substack.com', 0.65, 0.00, '2017-01-01', 'Newsletter platform with independent writers', 'blog');

-- Insert sample users (passwords are hashed versions of 'password123')
INSERT INTO users (username, email, password_hash, email_verified, user_role) VALUES
('admin', 'admin@newsanalyzer.com', '$2a$10$rWzJ4SrXQjJvH8P2XJmGdeQHrOGRpXA9T9dJ8MvBNEZ0xL5W2P8Wa', true, 'admin'),
('john_analyst', 'john@example.com', '$2a$10$rWzJ4SrXQjJvH8P2XJmGdeQHrOGRpXA9T9dJ8MvBNEZ0xL5W2P8Wa', true, 'user'),
('jane_researcher', 'jane@example.com', '$2a$10$rWzJ4SrXQjJvH8P2XJmGdeQHrOGRpXA9T9dJ8MvBNEZ0xL5W2P8Wa', true, 'expert'),
('test_user', 'test@example.com', '$2a$10$rWzJ4SrXQjJvH8P2XJmGdeQHrOGRpXA9T9dJ8MvBNEZ0xL5W2P8Wa', false, 'user');

-- Get user IDs for workbench creation
DO $$
DECLARE
    john_id UUID;
    jane_id UUID;
BEGIN
    -- Get user IDs
    SELECT user_id INTO john_id FROM users WHERE username = 'john_analyst';
    SELECT user_id INTO jane_id FROM users WHERE username = 'jane_researcher';

    -- Insert sample workbenches
    INSERT INTO user_workbenches (user_id, name, description, is_public) VALUES
    (john_id, 'Climate Change Analysis', 'Tracking claims about climate change and environmental policies', true),
    (john_id, 'Election 2024 Facts', 'Fact-checking claims about the 2024 election', false),
    (jane_id, 'Healthcare Research', 'Medical claims and healthcare policy analysis', true),
    (jane_id, 'Economic Indicators', 'Tracking economic claims and statistics', false);
END $$;

-- Insert sample accuracy records
INSERT INTO accuracy_records (source_id, claim_text, verification_result, authoritative_source_url, confidence_score, claim_category, verification_notes)
SELECT
    s.source_id,
    claims.claim_text,
    claims.verification_result,
    claims.authoritative_source_url,
    claims.confidence_score,
    claims.claim_category,
    claims.verification_notes
FROM news_sources s
CROSS JOIN (
    VALUES
    ('The unemployment rate dropped to 3.7% in October 2023', 'accurate', 'https://www.bls.gov/news.release/empsit.nr0.htm', 0.95, 'economics', 'Verified against Bureau of Labor Statistics official release'),
    ('COVID-19 vaccines are 95% effective against severe illness', 'accurate', 'https://www.cdc.gov/coronavirus/2019-ncov/vaccines/effectiveness/', 0.90, 'health', 'Confirmed by CDC effectiveness studies'),
    ('Solar energy costs have increased 50% since 2020', 'inaccurate', 'https://www.irena.org/publications/2023/Aug/Renewable-Power-Generation-Costs-in-2022', 0.88, 'environment', 'IRENA data shows costs decreased by 13% since 2020'),
    ('The Federal Reserve raised interest rates by 0.75% last month', 'accurate', 'https://www.federalreserve.gov/newsevents/pressreleases/', 0.98, 'economics', 'Official Federal Reserve press release confirmation'),
    ('New study shows masks are completely ineffective', 'misleading', 'https://www.cochrane.org/CD006207/ARI_do-physical-measures-such-hand-washing-or-wearing-masks-stop-or-slow-down-spread-respiratory-viruses', 0.75, 'health', 'Oversimplifies complex research with nuanced findings')
) AS claims(claim_text, verification_result, authoritative_source_url, confidence_score, claim_category, verification_notes)
WHERE s.domain IN ('reuters.com', 'apnews.com', 'bbc.com', 'npr.org', 'wsj.com')
LIMIT 25; -- 5 claims × 5 sources = 25 records

-- Insert additional accuracy records with varying results
INSERT INTO accuracy_records (source_id, claim_text, verification_result, confidence_score, claim_category)
SELECT
    s.source_id,
    'Sample claim for testing reliability scoring',
    CASE
        WHEN s.reliability_score > 0.85 THEN 'accurate'
        WHEN s.reliability_score > 0.70 THEN (ARRAY['accurate', 'accurate', 'partially_accurate'])[floor(random() * 3 + 1)]
        WHEN s.reliability_score > 0.55 THEN (ARRAY['accurate', 'partially_accurate', 'misleading'])[floor(random() * 3 + 1)]
        ELSE (ARRAY['partially_accurate', 'misleading', 'inaccurate'])[floor(random() * 3 + 1)]
    END,
    random() * 0.4 + 0.6, -- Random confidence between 0.6 and 1.0
    (ARRAY['politics', 'health', 'economics', 'science', 'technology'])[floor(random() * 5 + 1)]
FROM news_sources s
WHERE s.is_active = true;

-- Refresh the materialized view
REFRESH MATERIALIZED VIEW source_performance_summary;

-- Insert sample analysis requests
INSERT INTO analysis_requests (user_id, content_url, content_hash, request_type, status, processing_time_ms)
SELECT
    u.user_id,
    'https://example.com/article-' || generate_random_uuid(),
    encode(sha256(random()::text::bytea), 'hex'),
    (ARRAY['full_analysis', 'quick_check', 'bias_only', 'fact_only'])[floor(random() * 4 + 1)],
    (ARRAY['completed', 'completed', 'completed', 'failed'])[floor(random() * 4 + 1)],
    floor(random() * 5000 + 1000)::integer
FROM users u
CROSS JOIN generate_series(1, 3) -- 3 requests per user
WHERE u.is_active = true;

-- Update completed_at for completed requests
UPDATE analysis_requests
SET completed_at = created_at + (processing_time_ms || ' milliseconds')::interval
WHERE status = 'completed';

-- Create some sample workbench claims and connections
DO $$
DECLARE
    workbench_id UUID;
    claim1_id UUID;
    claim2_id UUID;
    claim3_id UUID;
BEGIN
    -- Get a sample workbench
    SELECT wb.workbench_id INTO workbench_id
    FROM user_workbenches wb
    WHERE wb.name = 'Climate Change Analysis'
    LIMIT 1;

    -- Insert sample claims
    INSERT INTO workbench_claims (workbench_id, claim_text, verification_status, source_urls, user_notes, position_x, position_y, claim_type, importance_score)
    VALUES
    (workbench_id, 'Global temperatures have risen by 1.1°C since pre-industrial times', 'verified', ARRAY['https://www.ipcc.ch/report/ar6/wg1/'], 'IPCC Sixth Assessment Report confirms this', 100, 100, 'fact', 5),
    (workbench_id, 'Carbon emissions decreased by 5% in 2020 due to COVID-19', 'verified', ARRAY['https://www.iea.org/reports/global-energy-review-2021'], 'IEA report shows temporary decrease', 300, 100, 'fact', 3),
    (workbench_id, 'Renewable energy will solve climate change completely', 'disputed', ARRAY['https://example.com/opinion-piece'], 'Oversimplified claim, needs nuanced analysis', 200, 250, 'opinion', 2)
    RETURNING claim_id INTO claim1_id, claim2_id, claim3_id;

    -- Create connections between claims
    INSERT INTO claim_connections (from_claim_id, to_claim_id, relationship_type, strength, user_notes)
    VALUES
    (claim1_id, claim2_id, 'related', 0.7, 'Both relate to climate change trends'),
    (claim2_id, claim3_id, 'questions', 0.8, 'Temporary decrease questions oversimplified solution');
END $$;