-- News Analyzer Database Schema
-- Version: 1.0
-- PostgreSQL initialization script

-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Create news sources table
CREATE TABLE news_sources (
    source_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    source_name VARCHAR(255) NOT NULL,
    domain VARCHAR(255) UNIQUE,
    reliability_score DECIMAL(3,2) CHECK (reliability_score >= 0 AND reliability_score <= 1),
    political_bias_score DECIMAL(3,2) CHECK (political_bias_score >= -1 AND political_bias_score <= 1),
    founded_date DATE,
    description TEXT,
    source_type VARCHAR(50) DEFAULT 'news', -- news, blog, social_media, government, academic
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create accuracy records table
CREATE TABLE accuracy_records (
    record_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    source_id UUID REFERENCES news_sources(source_id) ON DELETE CASCADE,
    claim_text TEXT NOT NULL,
    verification_result VARCHAR(50) CHECK (verification_result IN
        ('accurate', 'inaccurate', 'misleading', 'unverifiable', 'partially_accurate')),
    verification_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    authoritative_source_url TEXT,
    confidence_score DECIMAL(3,2) CHECK (confidence_score >= 0 AND confidence_score <= 1),
    verified_by VARCHAR(255) DEFAULT 'system', -- system, expert, crowd
    verification_notes TEXT,
    claim_category VARCHAR(100), -- politics, health, science, economics, etc.
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create users table
CREATE TABLE users (
    user_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    username VARCHAR(100) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    email_verified BOOLEAN DEFAULT FALSE,
    is_active BOOLEAN DEFAULT TRUE,
    user_role VARCHAR(50) DEFAULT 'user', -- user, admin, expert
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_login TIMESTAMP
);

-- Create user workbenches table
CREATE TABLE user_workbenches (
    workbench_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID REFERENCES users(user_id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    is_public BOOLEAN DEFAULT FALSE,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create workbench claims table
CREATE TABLE workbench_claims (
    claim_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    workbench_id UUID REFERENCES user_workbenches(workbench_id) ON DELETE CASCADE,
    claim_text TEXT NOT NULL,
    verification_status VARCHAR(50) CHECK (verification_status IN
        ('verified', 'unverified', 'disputed', 'investigating')),
    source_urls TEXT[],
    user_notes TEXT,
    position_x INTEGER DEFAULT 0,
    position_y INTEGER DEFAULT 0,
    claim_type VARCHAR(100), -- fact, opinion, prediction, etc.
    importance_score INTEGER CHECK (importance_score >= 1 AND importance_score <= 5),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create claim connections table
CREATE TABLE claim_connections (
    connection_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    from_claim_id UUID REFERENCES workbench_claims(claim_id) ON DELETE CASCADE,
    to_claim_id UUID REFERENCES workbench_claims(claim_id) ON DELETE CASCADE,
    relationship_type VARCHAR(50) CHECK (relationship_type IN
        ('supports', 'contradicts', 'related', 'questions', 'elaborates', 'causes', 'caused_by')),
    strength DECIMAL(2,1) CHECK (strength >= 0 AND strength <= 1) DEFAULT 0.5,
    user_notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    -- Prevent self-references and duplicate connections
    CONSTRAINT no_self_reference CHECK (from_claim_id != to_claim_id),
    CONSTRAINT unique_connection UNIQUE (from_claim_id, to_claim_id)
);

-- Create external API sources table
CREATE TABLE external_api_sources (
    api_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(255) NOT NULL,
    base_url TEXT NOT NULL,
    api_type VARCHAR(50), -- government, academic, fact_checker, news_api
    description TEXT,
    requires_auth BOOLEAN DEFAULT FALSE,
    rate_limit_per_hour INTEGER,
    reliability_score DECIMAL(3,2) CHECK (reliability_score >= 0 AND reliability_score <= 1),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create analysis requests table (for tracking processing)
CREATE TABLE analysis_requests (
    request_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID REFERENCES users(user_id),
    content_url TEXT,
    content_hash VARCHAR(64), -- SHA-256 hash of content
    request_type VARCHAR(50), -- full_analysis, quick_check, bias_only, fact_only
    status VARCHAR(50) DEFAULT 'pending', -- pending, processing, completed, failed
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP,
    processing_time_ms INTEGER,
    error_message TEXT
);

-- Create indexes for performance
CREATE INDEX idx_sources_domain ON news_sources(domain);
CREATE INDEX idx_sources_reliability ON news_sources(reliability_score DESC) WHERE is_active = TRUE;
CREATE INDEX idx_accuracy_source_date ON accuracy_records(source_id, verification_date DESC);
CREATE INDEX idx_accuracy_result ON accuracy_records(verification_result);
CREATE INDEX idx_accuracy_category ON accuracy_records(claim_category);
CREATE INDEX idx_users_email ON users(email) WHERE is_active = TRUE;
CREATE INDEX idx_users_username ON users(username) WHERE is_active = TRUE;
CREATE INDEX idx_workbenches_user ON user_workbenches(user_id) WHERE is_active = TRUE;
CREATE INDEX idx_workbenches_public ON user_workbenches(created_at DESC) WHERE is_public = TRUE AND is_active = TRUE;
CREATE INDEX idx_claims_workbench ON workbench_claims(workbench_id);
CREATE INDEX idx_claims_status ON workbench_claims(verification_status);
CREATE INDEX idx_connections_from ON claim_connections(from_claim_id);
CREATE INDEX idx_connections_to ON claim_connections(to_claim_id);
CREATE INDEX idx_analysis_user ON analysis_requests(user_id, created_at DESC);
CREATE INDEX idx_analysis_hash ON analysis_requests(content_hash);
CREATE INDEX idx_analysis_status ON analysis_requests(status, created_at);

-- Composite indexes for complex queries
CREATE INDEX idx_accuracy_source_result_date ON accuracy_records(source_id, verification_result, verification_date DESC);
CREATE INDEX idx_claims_workbench_type ON workbench_claims(workbench_id, claim_type);

-- Create trigger to update updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Apply update trigger to relevant tables
CREATE TRIGGER update_news_sources_updated_at BEFORE UPDATE ON news_sources
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_user_workbenches_updated_at BEFORE UPDATE ON user_workbenches
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_workbench_claims_updated_at BEFORE UPDATE ON workbench_claims
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_external_api_sources_updated_at BEFORE UPDATE ON external_api_sources
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Create materialized view for source performance analytics
CREATE MATERIALIZED VIEW source_performance_summary AS
SELECT
    s.source_id,
    s.source_name,
    s.domain,
    COUNT(ar.record_id) as total_claims_checked,
    COUNT(CASE WHEN ar.verification_result = 'accurate' THEN 1 END) as accurate_claims,
    COUNT(CASE WHEN ar.verification_result = 'inaccurate' THEN 1 END) as inaccurate_claims,
    COUNT(CASE WHEN ar.verification_result = 'misleading' THEN 1 END) as misleading_claims,
    ROUND(
        COUNT(CASE WHEN ar.verification_result = 'accurate' THEN 1 END)::DECIMAL /
        NULLIF(COUNT(ar.record_id), 0) * 100, 2
    ) as accuracy_percentage,
    AVG(ar.confidence_score) as avg_confidence_score,
    MAX(ar.verification_date) as last_checked,
    DATE_TRUNC('month', CURRENT_DATE) as report_month
FROM news_sources s
LEFT JOIN accuracy_records ar ON s.source_id = ar.source_id
WHERE s.is_active = TRUE
GROUP BY s.source_id, s.source_name, s.domain;

-- Create unique index on materialized view
CREATE UNIQUE INDEX idx_source_performance_summary_source_month
ON source_performance_summary(source_id, report_month);

-- Add comments for documentation
COMMENT ON TABLE news_sources IS 'Stores information about news sources and their reliability metrics';
COMMENT ON TABLE accuracy_records IS 'Historical record of fact-checking results for claims from various sources';
COMMENT ON TABLE users IS 'User accounts for the news analysis platform';
COMMENT ON TABLE user_workbenches IS 'Personal workspaces where users organize and analyze claims';
COMMENT ON TABLE workbench_claims IS 'Individual claims stored in user workbenches with verification status';
COMMENT ON TABLE claim_connections IS 'Relationships between claims showing how they support, contradict, or relate to each other';
COMMENT ON TABLE external_api_sources IS 'Configuration for external APIs used for fact verification';
COMMENT ON TABLE analysis_requests IS 'Tracking table for analysis processing requests';

COMMENT ON COLUMN news_sources.reliability_score IS 'Overall reliability score from 0.0 to 1.0 based on historical accuracy';
COMMENT ON COLUMN news_sources.political_bias_score IS 'Political bias score from -1.0 (left) to 1.0 (right), 0.0 is neutral';
COMMENT ON COLUMN accuracy_records.confidence_score IS 'Confidence level of the verification from 0.0 to 1.0';
COMMENT ON COLUMN claim_connections.strength IS 'Strength of the relationship from 0.0 (weak) to 1.0 (strong)';