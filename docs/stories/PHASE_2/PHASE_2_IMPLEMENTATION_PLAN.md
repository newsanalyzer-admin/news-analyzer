# Phase 2 Implementation Plan: Schema.org Enrichment & External Linking

**Document Version:** 1.0
**Created:** 2025-11-25
**Status:** READY FOR IMPLEMENTATION
**Priority:** HIGH - Required for MVP
**Estimated Effort:** 3-4 sprints (6-8 weeks)

---

## Executive Summary

Phase 2 extends NewsAnalyzer's entity model with external knowledge base linking (Wikidata, DBpedia) and Schema.org enrichment. This phase transforms extracted entities from isolated data points into interconnected, semantically rich records with authoritative external references.

### Business Value

| Benefit | Impact |
|---------|--------|
| **Data Quality** | External validation increases entity accuracy from ~85% to >95% |
| **Entity Deduplication** | "EPA", "E.P.A.", "Environmental Protection Agency" → single Wikidata ID |
| **Rich Properties** | Automatic enrichment with 10+ properties per linked entity |
| **Web Standards** | Valid JSON-LD for SEO and data interchange |
| **Research Platform** | External IDs enable academic research and citation |

### Dependencies

| Dependency | Status | Notes |
|------------|--------|-------|
| Phase 1 (Entity Model) | Complete | Full CRUD, Schema.org fields exist |
| Phase 1.6 (GovOrg Linking) | Complete | Pattern established for entity linking |
| Phase 3 (OWL Reasoning) | Complete | Can leverage inference for entity types |
| External API Access | Required | Wikidata SPARQL (free), DBpedia (free) |

---

## Table of Contents

1. [Architecture Overview](#architecture-overview)
2. [Epic 2.1: External Entity Linking Service](#epic-21-external-entity-linking-service)
3. [Epic 2.2: Entity Enrichment Pipeline](#epic-22-entity-enrichment-pipeline)
4. [Epic 2.3: JSON-LD Publishing](#epic-23-json-ld-publishing)
5. [Epic 2.4: Schema.org Validation](#epic-24-schemaorg-validation)
6. [Technical Specifications](#technical-specifications)
7. [Database Changes](#database-changes)
8. [API Specifications](#api-specifications)
9. [Testing Strategy](#testing-strategy)
10. [Rollout Plan](#rollout-plan)
11. [Success Criteria](#success-criteria)
12. [Risks & Mitigations](#risks--mitigations)

---

## Architecture Overview

### Current State (Post-Phase 1.6)

```
┌─────────────────┐     ┌─────────────────┐     ┌─────────────────┐
│    Frontend     │────▶│  Java Backend   │────▶│   PostgreSQL    │
│   (Next.js)     │     │ (Spring Boot)   │     │    (JSONB)      │
└─────────────────┘     └─────────────────┘     └─────────────────┘
                               │
                               ▼
                        ┌─────────────────┐
                        │ Python Reasoning│
                        │   (FastAPI)     │
                        └─────────────────┘
```

### Target State (Post-Phase 2)

```
┌─────────────────┐     ┌─────────────────┐     ┌─────────────────┐
│    Frontend     │────▶│  Java Backend   │────▶│   PostgreSQL    │
│   (Next.js)     │     │ (Spring Boot)   │     │    (JSONB)      │
└─────────────────┘     └─────────────────┘     └─────────────────┘
        │                      │                        ▲
        │                      ▼                        │
        │               ┌─────────────────┐             │
        │               │ Python Reasoning│─────────────┘
        │               │   (FastAPI)     │    (enriched data)
        │               └─────────────────┘
        │                      │
        │                      ▼
        │               ┌─────────────────┐
        │               │ External Linker │
        │               │   (New Module)  │
        │               └─────────────────┘
        │                      │
        ▼                      ▼
┌───────────────────────────────────────────────┐
│              External Knowledge Bases          │
├───────────────┬───────────────┬───────────────┤
│   Wikidata    │   DBpedia     │  Schema.org   │
│   (SPARQL)    │   (RDF/REST)  │  (Validation) │
└───────────────┴───────────────┴───────────────┘
```

### Data Flow

```
1. Entity Extracted → 2. Save to DB → 3. Queue for Linking
                                              │
                                              ▼
4. Query Wikidata ◀──────────────────── External Linker
                                              │
                                              ▼
5. Match Found? ─── YES ──▶ 6. Fetch Properties
       │                           │
       NO                          ▼
       │                    7. Merge into Entity
       ▼                           │
8. Try DBpedia ◀───────────────────┘
       │
       ▼
9. Update Entity with External IDs + Enriched Properties
```

---

## Epic 2.1: External Entity Linking Service

**Priority:** P0 (Critical Path)
**Estimated:** 2 sprints

### Overview

Create a Python service module that links extracted entities to external knowledge bases (Wikidata, DBpedia) using fuzzy matching and disambiguation.

### User Stories

#### Story 2.1.1: Wikidata Entity Lookup

**As a** system
**I want to** query Wikidata for matching entities
**So that** I can link extracted entities to authoritative external records

**Acceptance Criteria:**
- [ ] Query Wikidata SPARQL endpoint for entity by name
- [ ] Support multiple entity types (Person, Organization, Place, etc.)
- [ ] Return Wikidata QID (e.g., Q30 for United States)
- [ ] Handle rate limiting (1 request/second for public endpoint)
- [ ] Cache results for 24 hours (Redis or in-memory)
- [ ] Return top 5 candidates with confidence scores

**Technical Notes:**
```python
# Wikidata SPARQL endpoint
WIKIDATA_ENDPOINT = "https://query.wikidata.org/sparql"

# Example query for "Environmental Protection Agency"
QUERY = """
SELECT ?item ?itemLabel ?itemDescription WHERE {
  ?item rdfs:label "Environmental Protection Agency"@en.
  SERVICE wikibase:label { bd:serviceParam wikibase:language "en". }
}
LIMIT 5
"""
```

**Story Points:** 5

---

#### Story 2.1.2: DBpedia Entity Lookup

**As a** system
**I want to** query DBpedia for matching entities
**So that** I can have a fallback when Wikidata returns no results

**Acceptance Criteria:**
- [ ] Query DBpedia Lookup API for entity by name
- [ ] Support entity type filtering (Person, Organisation, Place)
- [ ] Return DBpedia URI (e.g., dbr:United_States_Environmental_Protection_Agency)
- [ ] Handle API errors gracefully
- [ ] Cache results for 24 hours
- [ ] Return top 5 candidates with confidence scores

**Technical Notes:**
```python
# DBpedia Lookup API
DBPEDIA_LOOKUP = "https://lookup.dbpedia.org/api/search"

# Example: GET /api/search?query=EPA&type=Organisation&maxResults=5
```

**Story Points:** 3

---

#### Story 2.1.3: Entity Disambiguation

**As a** system
**I want to** disambiguate between multiple matching entities
**So that** I link to the correct external record

**Acceptance Criteria:**
- [ ] Use entity type as primary disambiguation signal
- [ ] Use context (article text) for additional signals
- [ ] Score candidates based on type match, name similarity, and context
- [ ] Return confidence score (0.0-1.0) for final match
- [ ] Flag low-confidence matches (<0.7) for manual review
- [ ] Support manual disambiguation override via API

**Technical Notes:**
```
Disambiguation Algorithm:
1. Type Match Score (0.4 weight)
   - Person → Wikidata Q5 (human) = 1.0
   - Organization → Wikidata Q43229 (organization) = 1.0
   - Mismatch = 0.0

2. Name Similarity Score (0.3 weight)
   - Levenshtein distance normalized to 0-1
   - Handle acronyms: "EPA" matches "Environmental Protection Agency"

3. Context Score (0.3 weight)
   - Compare article text to Wikidata description
   - Use TF-IDF or embedding similarity
```

**Story Points:** 8

---

#### Story 2.1.4: External Linking Service API

**As a** developer
**I want to** call a REST API to link entities to external KBs
**So that** I can integrate linking into the entity processing pipeline

**Acceptance Criteria:**
- [ ] `POST /entities/link` endpoint in Python service
- [ ] Accept single entity or batch of entities
- [ ] Return linked entity with external IDs
- [ ] Support async processing for large batches
- [ ] Return linking status and confidence
- [ ] Log all linking attempts for debugging

**API Specification:**
```yaml
POST /entities/link
Request:
  entities:
    - text: "EPA"
      entity_type: "government_org"
      context: "The EPA announced new regulations..."
  options:
    sources: ["wikidata", "dbpedia"]  # Which KBs to query
    min_confidence: 0.7               # Minimum confidence to auto-link
    max_candidates: 5                 # Max candidates to return

Response:
  linked_entities:
    - text: "EPA"
      entity_type: "government_org"
      wikidata_id: "Q217173"
      wikidata_url: "https://www.wikidata.org/wiki/Q217173"
      dbpedia_uri: "http://dbpedia.org/resource/Environmental_Protection_Agency"
      linking_confidence: 0.95
      linking_source: "wikidata"
      candidates:
        - qid: "Q217173"
          label: "United States Environmental Protection Agency"
          confidence: 0.95
        - qid: "Q12345"
          label: "EPA (disambiguation)"
          confidence: 0.3
```

**Story Points:** 5

---

### Epic 2.1 Files to Create

| File | Purpose |
|------|---------|
| `reasoning-service/app/services/wikidata_client.py` | Wikidata SPARQL client |
| `reasoning-service/app/services/dbpedia_client.py` | DBpedia Lookup client |
| `reasoning-service/app/services/entity_linker.py` | Main linking service |
| `reasoning-service/app/services/disambiguation.py` | Disambiguation algorithm |
| `reasoning-service/tests/test_wikidata_client.py` | Wikidata client tests |
| `reasoning-service/tests/test_dbpedia_client.py` | DBpedia client tests |
| `reasoning-service/tests/test_entity_linker.py` | Linker integration tests |

---

## Epic 2.2: Entity Enrichment Pipeline

**Priority:** P0 (Critical Path)
**Estimated:** 1.5 sprints

### Overview

Once an entity is linked to an external KB, fetch rich properties and merge them into the entity record.

### User Stories

#### Story 2.2.1: Wikidata Property Extraction

**As a** system
**I want to** fetch properties from Wikidata for linked entities
**So that** entities have rich, authoritative metadata

**Acceptance Criteria:**
- [ ] Fetch key properties for each entity type
- [ ] Map Wikidata properties to Schema.org properties
- [ ] Handle missing properties gracefully
- [ ] Support property value types (string, date, URL, entity reference)
- [ ] Cache property data with entity

**Property Mappings:**

| Entity Type | Wikidata Property | Schema.org Property |
|-------------|-------------------|---------------------|
| Person | P569 (birthDate) | schema:birthDate |
| Person | P27 (citizenship) | schema:nationality |
| Person | P106 (occupation) | schema:jobTitle |
| Person | P39 (position held) | schema:hasOccupation |
| Organization | P571 (inception) | schema:foundingDate |
| Organization | P856 (website) | schema:url |
| Organization | P159 (headquarters) | schema:location |
| Organization | P749 (parent org) | schema:parentOrganization |
| Place | P625 (coordinates) | schema:geo |
| Place | P17 (country) | schema:containedInPlace |

**Story Points:** 5

---

#### Story 2.2.2: Property Merge Strategy

**As a** system
**I want to** merge external properties with existing entity properties
**So that** external data enriches without overwriting local data

**Acceptance Criteria:**
- [ ] Define merge priority: local > external
- [ ] Append external properties that don't exist locally
- [ ] Track property provenance (source: "wikidata", "local", etc.)
- [ ] Support conflict resolution configuration
- [ ] Log all property changes for audit

**Merge Rules:**
```
1. Local property exists → Keep local (don't overwrite)
2. External property only → Add with source tag
3. Both exist, different values → Keep both, mark conflict
4. Arrays (e.g., alternateName) → Merge and deduplicate
```

**Story Points:** 3

---

#### Story 2.2.3: Enrichment Pipeline Orchestration

**As a** system
**I want to** run enrichment as a background pipeline
**So that** entity extraction is not blocked by external API calls

**Acceptance Criteria:**
- [ ] Queue entities for enrichment after extraction
- [ ] Process queue asynchronously (background worker)
- [ ] Support batch processing (50 entities at a time)
- [ ] Retry failed enrichments with exponential backoff
- [ ] Track enrichment status per entity
- [ ] Provide enrichment statistics endpoint

**Pipeline States:**
```
PENDING → LINKING → LINKED → ENRICHING → ENRICHED → COMPLETE
                ↓           ↓
           LINK_FAILED  ENRICH_FAILED
```

**Story Points:** 8

---

#### Story 2.2.4: Java Backend Integration

**As a** developer
**I want to** call enrichment from the Java backend
**So that** entity creation triggers automatic enrichment

**Acceptance Criteria:**
- [ ] Add `enrichmentStatus` field to Entity model
- [ ] Call Python enrichment API after entity creation
- [ ] Store enrichment results in entity record
- [ ] Support manual re-enrichment via API
- [ ] Add enrichment fields to EntityDTO

**New Entity Fields:**
```java
// Enrichment tracking
private String enrichmentStatus;  // PENDING, ENRICHING, COMPLETE, FAILED
private LocalDateTime enrichedAt;

// External identifiers
// Stored in properties JSONB:
// "wikidata_id": "Q217173"
// "dbpedia_uri": "http://dbpedia.org/resource/..."
// "enrichment_source": "wikidata"
// "enrichment_confidence": 0.95
```

**Story Points:** 5

---

### Epic 2.2 Files to Create/Modify

| File | Action | Purpose |
|------|--------|---------|
| `reasoning-service/app/services/property_extractor.py` | Create | Wikidata property fetching |
| `reasoning-service/app/services/property_merger.py` | Create | Property merge logic |
| `reasoning-service/app/services/enrichment_pipeline.py` | Create | Pipeline orchestration |
| `reasoning-service/app/api/enrichment.py` | Create | Enrichment API endpoints |
| `backend/.../model/Entity.java` | Modify | Add enrichment fields |
| `backend/.../service/EnrichmentService.java` | Create | Java enrichment integration |
| `backend/.../controller/EnrichmentController.java` | Create | REST endpoints |

---

## Epic 2.3: JSON-LD Publishing

**Priority:** P1 (Important)
**Estimated:** 1 sprint

### Overview

Generate valid, rich JSON-LD representations for entities suitable for web publishing and SEO.

### User Stories

#### Story 2.3.1: JSON-LD Generation Enhancement

**As a** system
**I want to** generate complete JSON-LD with external references
**So that** entity data is fully interoperable with the semantic web

**Acceptance Criteria:**
- [ ] Include `@id` with canonical entity URL
- [ ] Include `sameAs` array with external URLs (Wikidata, DBpedia)
- [ ] Include all enriched Schema.org properties
- [ ] Support nested objects (e.g., Person → worksFor → Organization)
- [ ] Validate output against JSON-LD spec

**Example Output:**
```json
{
  "@context": "https://schema.org",
  "@type": "GovernmentOrganization",
  "@id": "https://newsanalyzer.org/entities/q217173",
  "name": "United States Environmental Protection Agency",
  "alternateName": ["EPA", "U.S. EPA", "Environmental Protection Agency"],
  "url": "https://www.epa.gov",
  "foundingDate": "1970-12-02",
  "sameAs": [
    "https://www.wikidata.org/wiki/Q217173",
    "http://dbpedia.org/resource/United_States_Environmental_Protection_Agency"
  ],
  "parentOrganization": {
    "@type": "GovernmentOrganization",
    "name": "United States federal government"
  },
  "location": {
    "@type": "Place",
    "name": "Washington, D.C."
  }
}
```

**Story Points:** 5

---

#### Story 2.3.2: Public Entity Pages

**As a** user
**I want to** view entity details on a public web page
**So that** I can see all information about an entity in one place

**Acceptance Criteria:**
- [ ] Create `/entities/{id}` public page in frontend
- [ ] Display entity details in human-readable format
- [ ] Embed JSON-LD in `<script type="application/ld+json">`
- [ ] Show external links (Wikidata, DBpedia, official website)
- [ ] Support canonical URLs for entities
- [ ] Add Open Graph meta tags for social sharing

**Story Points:** 5

---

#### Story 2.3.3: Entity Sitemap Generation

**As a** search engine
**I want to** discover all entity pages via sitemap
**So that** entity pages are indexed and searchable

**Acceptance Criteria:**
- [ ] Generate XML sitemap for all public entities
- [ ] Include lastmod timestamps
- [ ] Support sitemap pagination (50,000 entries per file)
- [ ] Add sitemap to robots.txt
- [ ] Regenerate sitemap daily (scheduled job)

**Story Points:** 3

---

### Epic 2.3 Files to Create/Modify

| File | Action | Purpose |
|------|--------|---------|
| `backend/.../service/JsonLdService.java` | Create | JSON-LD generation |
| `frontend/src/app/entities/[id]/page.tsx` | Create | Public entity page |
| `frontend/src/components/JsonLdEmbed.tsx` | Create | JSON-LD script tag |
| `backend/.../controller/SitemapController.java` | Create | Sitemap generation |

---

## Epic 2.4: Schema.org Validation

**Priority:** P1 (Important)
**Estimated:** 0.5 sprint

### Overview

Validate all Schema.org data against the official vocabulary to ensure compliance.

### User Stories

#### Story 2.4.1: Schema.org Validation Service

**As a** system
**I want to** validate entity JSON-LD against Schema.org
**So that** all entity data is standards-compliant

**Acceptance Criteria:**
- [ ] Validate `@type` is valid Schema.org type
- [ ] Validate required properties per type
- [ ] Validate property value types (string, date, URL, etc.)
- [ ] Return detailed validation errors
- [ ] Support batch validation
- [ ] Cache Schema.org vocabulary locally

**Validation Rules:**
```
1. Type Validation:
   - @type must be valid Schema.org type
   - Custom types must use NewsAnalyzer namespace

2. Property Validation:
   - Required: name (for all types)
   - Type-specific: url (for Organization), birthDate (for Person)

3. Value Validation:
   - Dates: ISO 8601 format
   - URLs: Valid URL format
   - References: Must be object with @type or string URL
```

**Story Points:** 5

---

#### Story 2.4.2: Validation API Endpoint

**As a** developer
**I want to** validate entities via API
**So that** I can check data quality before publishing

**Acceptance Criteria:**
- [ ] `POST /entities/validate` endpoint
- [ ] Accept single entity or batch
- [ ] Return validation result with errors/warnings
- [ ] Support validation level (strict, lenient)
- [ ] Integrate with entity creation (optional strict mode)

**Story Points:** 3

---

#### Story 2.4.3: Bulk Validation Report

**As an** administrator
**I want to** run validation on all existing entities
**So that** I can identify and fix data quality issues

**Acceptance Criteria:**
- [ ] Create validation job for all entities
- [ ] Generate report with error counts by type
- [ ] Identify entities needing manual correction
- [ ] Provide export of validation results (CSV)
- [ ] Schedule weekly validation report

**Story Points:** 3

---

### Epic 2.4 Files to Create

| File | Action | Purpose |
|------|--------|---------|
| `reasoning-service/app/services/schema_validator.py` | Create | Validation logic |
| `reasoning-service/app/api/validation.py` | Create | Validation endpoints |
| `reasoning-service/data/schema-org-vocab.json` | Create | Cached vocabulary |

---

## Technical Specifications

### External API Rate Limits

| Service | Rate Limit | Strategy |
|---------|------------|----------|
| Wikidata SPARQL | ~60 req/min (public) | Queue + throttle |
| DBpedia Lookup | No official limit | Batch requests |
| Schema.org | N/A (static vocab) | Cache locally |

### Caching Strategy

```yaml
Entity Linking Cache:
  Key: "link:{entity_type}:{normalized_name}"
  TTL: 24 hours
  Storage: Redis (or in-memory for dev)

Property Cache:
  Key: "props:{wikidata_id}"
  TTL: 7 days
  Storage: Redis

Schema.org Vocabulary:
  Key: "schemaorg:vocab"
  TTL: 30 days
  Storage: File + memory
```

### Error Handling

```python
# Retry policy for external APIs
RETRY_CONFIG = {
    "max_retries": 3,
    "backoff_factor": 2,  # 1s, 2s, 4s
    "retry_on": [429, 500, 502, 503, 504],
}

# Circuit breaker for API failures
CIRCUIT_BREAKER = {
    "failure_threshold": 5,
    "recovery_timeout": 60,  # seconds
}
```

---

## Database Changes

### Migration V5: Add Enrichment Fields

```sql
-- V5__add_enrichment_fields.sql

-- Add enrichment tracking columns
ALTER TABLE entities
ADD COLUMN enrichment_status VARCHAR(20) DEFAULT 'PENDING',
ADD COLUMN enriched_at TIMESTAMP;

-- Add index for enrichment queue
CREATE INDEX idx_entities_enrichment_status
ON entities(enrichment_status)
WHERE enrichment_status IN ('PENDING', 'FAILED');

-- Add index for external ID lookup (via JSONB)
CREATE INDEX idx_entities_wikidata_id
ON entities ((properties->>'wikidata_id'))
WHERE properties->>'wikidata_id' IS NOT NULL;

COMMENT ON COLUMN entities.enrichment_status IS
'Enrichment pipeline status: PENDING, LINKING, ENRICHING, COMPLETE, FAILED';
```

### Properties JSONB Structure (Post-Enrichment)

```json
{
  "wikidata_id": "Q217173",
  "wikidata_url": "https://www.wikidata.org/wiki/Q217173",
  "dbpedia_uri": "http://dbpedia.org/resource/United_States_Environmental_Protection_Agency",
  "enrichment_source": "wikidata",
  "enrichment_confidence": 0.95,
  "enriched_properties": {
    "foundingDate": "1970-12-02",
    "website": "https://www.epa.gov",
    "headquarters": "Washington, D.C."
  },
  "property_provenance": {
    "name": "local",
    "foundingDate": "wikidata",
    "website": "wikidata"
  }
}
```

---

## API Specifications

### New Python Endpoints

```yaml
# Entity Linking
POST /entities/link:
  description: Link entities to external knowledge bases
  request:
    entities: List[EntityLinkRequest]
    options: LinkingOptions
  response:
    linked_entities: List[LinkedEntity]
    statistics: LinkingStats

# Entity Enrichment
POST /entities/enrich:
  description: Enrich linked entities with external properties
  request:
    entity_ids: List[str]
  response:
    enriched_entities: List[EnrichedEntity]
    statistics: EnrichmentStats

# Enrichment Status
GET /entities/{id}/enrichment-status:
  description: Get enrichment pipeline status for entity
  response:
    status: str  # PENDING, LINKING, ENRICHING, COMPLETE, FAILED
    linked_at: datetime
    enriched_at: datetime
    error: str | null

# Schema.org Validation
POST /entities/validate:
  description: Validate entity JSON-LD against Schema.org
  request:
    entities: List[EntityDTO]
    level: str  # strict | lenient
  response:
    results: List[ValidationResult]
    valid_count: int
    error_count: int
```

### New Java Endpoints

```yaml
# Trigger Enrichment
POST /api/entities/{id}/enrich:
  description: Queue entity for enrichment
  response:
    status: str
    message: str

# Get Enriched Entity
GET /api/entities/{id}/enriched:
  description: Get entity with full enrichment data
  response:
    EntityDTO with all enrichment fields

# Bulk Enrichment
POST /api/entities/enrich/bulk:
  description: Queue multiple entities for enrichment
  request:
    entity_ids: List[UUID]
  response:
    queued_count: int

# JSON-LD Export
GET /api/entities/{id}/jsonld:
  description: Get entity as pure JSON-LD
  response:
    application/ld+json
```

---

## Testing Strategy

### Unit Tests

| Component | Test Coverage Target |
|-----------|---------------------|
| WikidataClient | 90% |
| DBpediaClient | 90% |
| EntityLinker | 95% |
| DisambiguationService | 95% |
| PropertyExtractor | 90% |
| PropertyMerger | 95% |
| SchemaValidator | 95% |

### Integration Tests

```python
# Test external API integration (with mocks for CI)
def test_wikidata_integration():
    # Uses recorded responses (VCR cassettes)

def test_full_enrichment_pipeline():
    # End-to-end test with test database
```

### Test Data

```yaml
# Test entities for linking
test_entities:
  - name: "Environmental Protection Agency"
    type: "government_org"
    expected_wikidata: "Q217173"

  - name: "Elizabeth Warren"
    type: "person"
    expected_wikidata: "Q434706"

  - name: "Washington, D.C."
    type: "location"
    expected_wikidata: "Q61"
```

---

## Rollout Plan

### Sprint 1: External Linking Foundation (Weeks 1-2)

**Goals:**
- Wikidata client operational
- DBpedia client operational
- Basic linking endpoint working

**Deliverables:**
- Stories 2.1.1, 2.1.2, 2.1.4
- Unit tests for clients
- Manual testing with sample entities

**Exit Criteria:**
- [ ] Can link "EPA" to Wikidata Q217173
- [ ] Can link "Elizabeth Warren" to Wikidata Q434706
- [ ] API returns linked entity with external IDs

---

### Sprint 2: Disambiguation & Enrichment (Weeks 3-4)

**Goals:**
- Disambiguation algorithm working
- Property extraction from Wikidata
- Property merge strategy implemented

**Deliverables:**
- Stories 2.1.3, 2.2.1, 2.2.2
- Disambiguation tests with edge cases
- Property mapping complete

**Exit Criteria:**
- [ ] Disambiguation correctly handles "EPA" (agency vs disambiguation page)
- [ ] Enriched entity has 5+ properties from Wikidata
- [ ] Property provenance tracked correctly

---

### Sprint 3: Pipeline & Java Integration (Weeks 5-6)

**Goals:**
- Background enrichment pipeline operational
- Java backend integrated
- Database migrations applied

**Deliverables:**
- Stories 2.2.3, 2.2.4
- Migration V5
- Integration tests passing

**Exit Criteria:**
- [ ] New entity creation triggers background enrichment
- [ ] Enrichment status visible in API response
- [ ] Batch enrichment processes 100 entities in <60 seconds

---

### Sprint 4: JSON-LD & Validation (Weeks 7-8)

**Goals:**
- JSON-LD publishing complete
- Schema.org validation operational
- Public entity pages live

**Deliverables:**
- Stories 2.3.1, 2.3.2, 2.3.3, 2.4.1, 2.4.2, 2.4.3
- Frontend entity pages
- Validation reports

**Exit Criteria:**
- [ ] JSON-LD passes Google Structured Data Testing Tool
- [ ] Entity pages indexed by search engines
- [ ] Validation report shows >90% valid entities

---

## Success Criteria

### Phase 2 Complete When:

| Criteria | Target | Measurement |
|----------|--------|-------------|
| **Linking Rate** | 70%+ of GOVERNMENT_ORG entities linked | DB query |
| **Enrichment Rate** | Linked entities have 5+ properties | DB query |
| **Validation Pass Rate** | 90%+ entities valid | Validation report |
| **JSON-LD Valid** | 100% pass structured data test | Google tool |
| **Test Coverage** | 85%+ Python, 80%+ Java | Coverage reports |
| **Performance** | <100ms linking, <500ms enrichment | API metrics |

### Business Metrics

| Metric | Target |
|--------|--------|
| Entity accuracy improvement | +10% (85% → 95%) |
| Unique entities (post-dedup) | Reduce duplicates by 50% |
| Properties per entity | Average 8+ (from 3) |

---

## Risks & Mitigations

### Risk 1: External API Availability

**Risk:** Wikidata/DBpedia may be unavailable or rate-limited
**Probability:** Medium
**Impact:** High

**Mitigations:**
- Implement circuit breaker pattern
- Queue and retry with exponential backoff
- Cache aggressively (24h+ TTL)
- Support manual linking as fallback
- Monitor API health continuously

---

### Risk 2: Disambiguation Accuracy

**Risk:** Wrong entity linked due to ambiguous names
**Probability:** Medium
**Impact:** Medium

**Mitigations:**
- Use context (article text) for disambiguation
- Set high confidence threshold (0.8) for auto-linking
- Flag low-confidence matches for review
- Allow manual override via admin API
- Track and learn from corrections

---

### Risk 3: Data Quality Degradation

**Risk:** External data may be incorrect or outdated
**Probability:** Low
**Impact:** Medium

**Mitigations:**
- Track property provenance (local vs external)
- Never overwrite local data with external
- Validate external data before merge
- Support property rollback
- Regular data quality audits

---

### Risk 4: Performance Impact

**Risk:** External API calls slow down entity processing
**Probability:** Medium
**Impact:** Medium

**Mitigations:**
- Process enrichment asynchronously (background queue)
- Batch external API calls
- Cache all external responses
- Set timeout limits (5s per request)
- Monitor and alert on latency spikes

---

## Appendix A: Wikidata Property Reference

### Person Properties (P5 - human)

| Property | ID | Schema.org |
|----------|-----|------------|
| Date of birth | P569 | birthDate |
| Place of birth | P19 | birthPlace |
| Citizenship | P27 | nationality |
| Occupation | P106 | jobTitle |
| Position held | P39 | hasOccupation |
| Member of political party | P102 | memberOf |
| Official website | P856 | url |
| Image | P18 | image |

### Organization Properties (Q43229)

| Property | ID | Schema.org |
|----------|-----|------------|
| Inception date | P571 | foundingDate |
| Dissolution date | P576 | dissolutionDate |
| Headquarters | P159 | location |
| Official website | P856 | url |
| Parent organization | P749 | parentOrganization |
| Subsidiary | P355 | subOrganization |
| Industry | P452 | industry |

### Government Organization Properties (Q327333)

| Property | ID | Schema.org/Custom |
|----------|-----|-------------------|
| Country | P17 | areaServed |
| Jurisdiction | P1001 | na:hasJurisdiction |
| Official name | P1448 | name |
| Head of government | P6 | employee (head) |

---

## Appendix B: Sample SPARQL Queries

### Find Entity by Name

```sparql
SELECT ?item ?itemLabel ?itemDescription ?type WHERE {
  ?item rdfs:label "Environmental Protection Agency"@en .
  ?item wdt:P31 ?type .
  SERVICE wikibase:label { bd:serviceParam wikibase:language "en". }
}
LIMIT 10
```

### Get Entity Properties

```sparql
SELECT ?prop ?propLabel ?value ?valueLabel WHERE {
  wd:Q217173 ?prop ?value .
  ?property wikibase:directClaim ?prop .
  SERVICE wikibase:label { bd:serviceParam wikibase:language "en". }
}
LIMIT 50
```

### Find Person by Name with Disambiguation

```sparql
SELECT ?item ?itemLabel ?itemDescription ?occupation ?occupationLabel WHERE {
  ?item wdt:P31 wd:Q5 .  # Instance of human
  ?item rdfs:label "Elizabeth Warren"@en .
  OPTIONAL { ?item wdt:P106 ?occupation . }
  SERVICE wikibase:label { bd:serviceParam wikibase:language "en". }
}
LIMIT 10
```

---

## Document History

| Version | Date | Author | Changes |
|---------|------|--------|---------|
| 1.0 | 2025-11-25 | Sarah (PO Agent) | Initial implementation plan |

---

## Approval

| Role | Name | Status | Date |
|------|------|--------|------|
| Product Owner | Sarah | APPROVED | 2025-11-25 |
| System Architect | Winston | PENDING | - |
| Tech Lead | TBD | PENDING | - |

---

*End of Phase 2 Implementation Plan*
