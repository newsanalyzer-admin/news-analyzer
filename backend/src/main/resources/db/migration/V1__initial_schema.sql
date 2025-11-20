-- NewsAnalyzer v2 - Initial Database Schema
-- Unified entity model (fixes V1's government-entity-first mistake)

-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Enable full-text search support
CREATE EXTENSION IF NOT EXISTS pg_trgm;

-- =============================================================================
-- USERS & AUTHENTICATION
-- =============================================================================

CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'USER',
    verified BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_username ON users(username);

-- =============================================================================
-- UNIFIED ENTITY MODEL
-- =============================================================================

CREATE TABLE entities (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    entity_type VARCHAR(50) NOT NULL,
    name VARCHAR(500) NOT NULL,
    properties JSONB DEFAULT '{}'::jsonb,
    schema_org_type VARCHAR(255),
    schema_org_data JSONB,
    source VARCHAR(100),
    confidence_score REAL DEFAULT 1.0,
    verified BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT check_entity_type CHECK (entity_type IN (
        'government_org', 'person', 'organization',
        'location', 'event', 'concept'
    )),
    CONSTRAINT check_confidence_score CHECK (confidence_score BETWEEN 0 AND 1)
);

-- Full-text search support
ALTER TABLE entities ADD COLUMN search_vector tsvector
    GENERATED ALWAYS AS (
        setweight(to_tsvector('english', coalesce(name, '')), 'A') ||
        setweight(to_tsvector('english', coalesce(properties->>'description', '')), 'B')
    ) STORED;

CREATE INDEX idx_entities_search_vector ON entities USING GIN(search_vector);
CREATE INDEX idx_entities_properties ON entities USING GIN(properties);
CREATE INDEX idx_entities_type ON entities(entity_type);
CREATE INDEX idx_entities_name ON entities(name);
CREATE INDEX idx_entities_verified ON entities(verified);

-- =============================================================================
-- ENTITY RELATIONSHIPS (Graph structure in PostgreSQL)
-- =============================================================================

CREATE TABLE entity_relationships (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    source_entity_id UUID NOT NULL REFERENCES entities(id) ON DELETE CASCADE,
    target_entity_id UUID NOT NULL REFERENCES entities(id) ON DELETE CASCADE,
    relationship_type VARCHAR(100) NOT NULL,
    properties JSONB DEFAULT '{}'::jsonb,
    confidence_score REAL DEFAULT 1.0,
    source VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT check_relationship_confidence CHECK (confidence_score BETWEEN 0 AND 1),
    CONSTRAINT check_not_self_reference CHECK (source_entity_id != target_entity_id)
);

CREATE INDEX idx_relationships_source ON entity_relationships(source_entity_id);
CREATE INDEX idx_relationships_target ON entity_relationships(target_entity_id);
CREATE INDEX idx_relationships_type ON entity_relationships(relationship_type);
CREATE INDEX idx_relationships_properties ON entity_relationships USING GIN(properties);

-- =============================================================================
-- ARTICLES
-- =============================================================================

CREATE TABLE articles (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    url TEXT UNIQUE NOT NULL,
    title VARCHAR(500) NOT NULL,
    content TEXT,
    published_date TIMESTAMP,
    author VARCHAR(255),
    source_domain VARCHAR(255),
    analysis_status VARCHAR(50) DEFAULT 'pending',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT check_analysis_status CHECK (analysis_status IN (
        'pending', 'processing', 'completed', 'failed'
    ))
);

CREATE INDEX idx_articles_url ON articles(url);
CREATE INDEX idx_articles_source ON articles(source_domain);
CREATE INDEX idx_articles_status ON articles(analysis_status);
CREATE INDEX idx_articles_published ON articles(published_date DESC);

-- Full-text search for articles
ALTER TABLE articles ADD COLUMN search_vector tsvector
    GENERATED ALWAYS AS (
        setweight(to_tsvector('english', coalesce(title, '')), 'A') ||
        setweight(to_tsvector('english', coalesce(content, '')), 'B')
    ) STORED;

CREATE INDEX idx_articles_search_vector ON articles USING GIN(search_vector);

-- =============================================================================
-- ARTICLE-ENTITY MENTIONS
-- =============================================================================

CREATE TABLE article_entities (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    article_id UUID NOT NULL REFERENCES articles(id) ON DELETE CASCADE,
    entity_id UUID NOT NULL REFERENCES entities(id) ON DELETE CASCADE,
    mention_count INTEGER DEFAULT 1,
    context_snippets JSONB DEFAULT '[]'::jsonb,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    UNIQUE(article_id, entity_id)
);

CREATE INDEX idx_article_entities_article ON article_entities(article_id);
CREATE INDEX idx_article_entities_entity ON article_entities(entity_id);

-- =============================================================================
-- CLAIMS & FACT-CHECKING
-- =============================================================================

CREATE TABLE claims (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    article_id UUID REFERENCES articles(id) ON DELETE CASCADE,
    claim_text TEXT NOT NULL,
    claim_type VARCHAR(50),
    verification_status VARCHAR(50) DEFAULT 'unverified',
    verification_evidence JSONB,
    confidence_score REAL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT check_claim_verification CHECK (verification_status IN (
        'unverified', 'true', 'mostly_true', 'mixed',
        'mostly_false', 'false', 'unverifiable'
    )),
    CONSTRAINT check_claim_confidence CHECK (confidence_score IS NULL OR
        (confidence_score BETWEEN 0 AND 1))
);

CREATE INDEX idx_claims_article ON claims(article_id);
CREATE INDEX idx_claims_status ON claims(verification_status);

-- =============================================================================
-- LOGICAL FALLACIES
-- =============================================================================

CREATE TABLE fallacies (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    article_id UUID REFERENCES articles(id) ON DELETE CASCADE,
    fallacy_type VARCHAR(100) NOT NULL,
    text_excerpt TEXT,
    explanation TEXT,
    confidence_score REAL DEFAULT 1.0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT check_fallacy_confidence CHECK (confidence_score BETWEEN 0 AND 1)
);

CREATE INDEX idx_fallacies_article ON fallacies(article_id);
CREATE INDEX idx_fallacies_type ON fallacies(fallacy_type);

-- =============================================================================
-- COGNITIVE BIASES
-- =============================================================================

CREATE TABLE biases (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    article_id UUID REFERENCES articles(id) ON DELETE CASCADE,
    bias_type VARCHAR(100) NOT NULL,
    text_excerpt TEXT,
    explanation TEXT,
    confidence_score REAL DEFAULT 1.0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT check_bias_confidence CHECK (confidence_score BETWEEN 0 AND 1)
);

CREATE INDEX idx_biases_article ON biases(article_id);
CREATE INDEX idx_biases_type ON biases(bias_type);

-- =============================================================================
-- SOURCE RELIABILITY TRACKING
-- =============================================================================

CREATE TABLE source_reliability (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    domain VARCHAR(255) UNIQUE NOT NULL,
    reliability_score REAL,
    total_articles INTEGER DEFAULT 0,
    accurate_articles INTEGER DEFAULT 0,
    factual_accuracy_rate REAL,
    bias_tendency VARCHAR(50),
    notes TEXT,
    last_evaluated TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT check_reliability_score CHECK (reliability_score IS NULL OR
        (reliability_score BETWEEN 0 AND 1))
);

CREATE INDEX idx_source_reliability_domain ON source_reliability(domain);
CREATE INDEX idx_source_reliability_score ON source_reliability(reliability_score DESC);

-- =============================================================================
-- AUDIT LOG
-- =============================================================================

CREATE TABLE audit_log (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID REFERENCES users(id) ON DELETE SET NULL,
    action VARCHAR(100) NOT NULL,
    entity_type VARCHAR(50),
    entity_id UUID,
    changes JSONB,
    ip_address VARCHAR(45),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_audit_log_user ON audit_log(user_id);
CREATE INDEX idx_audit_log_entity ON audit_log(entity_type, entity_id);
CREATE INDEX idx_audit_log_created ON audit_log(created_at DESC);
