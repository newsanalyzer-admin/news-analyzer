# Phase 2 Epic Index: Schema.org Enrichment & External Linking

**Phase Status:** READY FOR IMPLEMENTATION
**Created:** 2025-11-25
**Target Duration:** 6-8 weeks (4 sprints)
**Master Plan:** [PHASE_2_IMPLEMENTATION_PLAN.md](./PHASE_2_IMPLEMENTATION_PLAN.md)

---

## Quick Navigation

| Epic | Stories | Status | Sprint |
|------|---------|--------|--------|
| [Epic 2.1: External Entity Linking](#epic-21-external-entity-linking-service) | 4 | Draft | 1-2 |
| [Epic 2.2: Entity Enrichment Pipeline](#epic-22-entity-enrichment-pipeline) | 4 | Draft | 2-3 |
| [Epic 2.3: JSON-LD Publishing](#epic-23-json-ld-publishing) | 3 | Draft | 4 |
| [Epic 2.4: Schema.org Validation](#epic-24-schemaorg-validation) | 3 | Draft | 4 |

---

## Phase 2 Overview

### Business Value

Phase 2 transforms NewsAnalyzer entities from isolated data points into semantically rich, externally validated records by:

1. **Linking** entities to Wikidata and DBpedia knowledge bases
2. **Enriching** entities with authoritative external properties
3. **Publishing** valid JSON-LD for SEO and data interchange
4. **Validating** all data against Schema.org standards

### Success Metrics

| Metric | Target |
|--------|--------|
| Linking Rate | 70%+ of GOVERNMENT_ORG entities linked |
| Enrichment Rate | 5+ properties per linked entity |
| Validation Pass Rate | 90%+ entities valid |
| JSON-LD Valid | 100% pass structured data test |

### Dependencies

- Phase 1.6 Complete (Entity-GovOrg linking pattern)
- Phase 3 Complete (OWL reasoning available)
- External API access (Wikidata SPARQL, DBpedia Lookup)

---

## Epic 2.1: External Entity Linking Service

**Priority:** P0 (Critical Path)
**Sprint:** 1-2
**Story Points:** 21

### Description

Create a Python service module that links extracted entities to external knowledge bases (Wikidata, DBpedia) using fuzzy matching and disambiguation.

### Stories

| # | Story | Points | Status | Assignee |
|---|-------|--------|--------|----------|
| 2.1.1 | [Wikidata Entity Lookup](./2.1.1.wikidata-entity-lookup.md) | 5 | Draft | - |
| 2.1.2 | [DBpedia Entity Lookup](./2.1.2.dbpedia-entity-lookup.md) | 3 | Draft | - |
| 2.1.3 | [Entity Disambiguation](./2.1.3.entity-disambiguation.md) | 8 | Draft | - |
| 2.1.4 | [External Linking API](./2.1.4.external-linking-api.md) | 5 | Draft | - |

### Files to Create

| File | Story | Purpose |
|------|-------|---------|
| `reasoning-service/app/services/wikidata_client.py` | 2.1.1 | Wikidata SPARQL client |
| `reasoning-service/app/services/dbpedia_client.py` | 2.1.2 | DBpedia Lookup client |
| `reasoning-service/app/services/disambiguation.py` | 2.1.3 | Disambiguation algorithm |
| `reasoning-service/app/services/entity_linker.py` | 2.1.4 | Orchestrator service |
| `reasoning-service/tests/test_wikidata_client.py` | 2.1.1 | Unit tests |
| `reasoning-service/tests/test_dbpedia_client.py` | 2.1.2 | Unit tests |
| `reasoning-service/tests/test_disambiguation.py` | 2.1.3 | Unit tests |
| `reasoning-service/tests/test_entity_linker.py` | 2.1.4 | Integration tests |

### Epic 2.1 Acceptance Criteria

- [ ] Can link "EPA" to Wikidata Q217173
- [ ] Can link "Elizabeth Warren" to Wikidata Q434706
- [ ] Disambiguation correctly handles ambiguous names
- [ ] DBpedia used as fallback when Wikidata returns nothing
- [ ] API returns linked entities with external IDs and confidence scores
- [ ] Rate limiting prevents API throttling
- [ ] Results cached for 24 hours

---

## Epic 2.2: Entity Enrichment Pipeline

**Priority:** P0 (Critical Path)
**Sprint:** 2-3
**Story Points:** 21

### Description

Once an entity is linked to an external KB, fetch rich properties and merge them into the entity record using a background pipeline.

### Stories

| # | Story | Points | Status | Assignee |
|---|-------|--------|--------|----------|
| 2.2.1 | [Wikidata Property Extraction](./2.2.1.wikidata-property-extraction.md) | 5 | Draft | - |
| 2.2.2 | [Property Merge Strategy](./2.2.2.property-merge-strategy.md) | 3 | Draft | - |
| 2.2.3 | [Enrichment Pipeline](./2.2.3.enrichment-pipeline.md) | 8 | Draft | - |
| 2.2.4 | [Java Backend Integration](./2.2.4.java-backend-integration.md) | 5 | Draft | - |

### Files to Create/Modify

| File | Story | Action | Purpose |
|------|-------|--------|---------|
| `reasoning-service/app/services/property_extractor.py` | 2.2.1 | Create | Wikidata property fetching |
| `reasoning-service/app/services/property_merger.py` | 2.2.2 | Create | Merge logic & provenance |
| `reasoning-service/app/services/enrichment_pipeline.py` | 2.2.3 | Create | Background pipeline |
| `reasoning-service/app/api/enrichment.py` | 2.2.3 | Create | Enrichment endpoints |
| `backend/.../model/Entity.java` | 2.2.4 | Modify | Add enrichment fields |
| `backend/.../model/EnrichmentStatus.java` | 2.2.4 | Create | Status enum |
| `backend/.../service/EnrichmentService.java` | 2.2.4 | Create | Java integration |
| `backend/.../controller/EnrichmentController.java` | 2.2.4 | Create | REST endpoints |
| `backend/.../db/migration/V5__add_enrichment_fields.sql` | 2.2.4 | Create | DB migration |

### Epic 2.2 Acceptance Criteria

- [ ] Linked entities enriched with 5+ properties from Wikidata
- [ ] Property provenance tracked (source: local, wikidata, dbpedia)
- [ ] Local properties never overwritten by external
- [ ] Background pipeline processes entities asynchronously
- [ ] Entity creation triggers automatic enrichment queueing
- [ ] Enrichment status visible in API responses
- [ ] Failed enrichments retry with exponential backoff

---

## Epic 2.3: JSON-LD Publishing

**Priority:** P1 (Important)
**Sprint:** 4
**Story Points:** 13

### Description

Generate valid, rich JSON-LD representations for entities suitable for web publishing, SEO, and semantic web interoperability.

### Stories

| # | Story | Points | Status | Assignee |
|---|-------|--------|--------|----------|
| 2.3.1 | [JSON-LD Generation Enhancement](./2.3.1.jsonld-generation.md) | 5 | Draft | - |
| 2.3.2 | [Public Entity Pages](./2.3.2.public-entity-pages.md) | 5 | Draft | - |
| 2.3.3 | [Entity Sitemap](./2.3.3.entity-sitemap.md) | 3 | Draft | - |

### Files to Create/Modify

| File | Story | Action | Purpose |
|------|-------|--------|---------|
| `backend/.../service/JsonLdService.java` | 2.3.1 | Create | JSON-LD generation |
| `frontend/src/app/entities/[id]/page.tsx` | 2.3.2 | Create | Public entity page |
| `frontend/src/components/JsonLdEmbed.tsx` | 2.3.2 | Create | Script embedding |
| `frontend/src/components/ExternalLinks.tsx` | 2.3.2 | Create | External link display |
| `backend/.../service/SitemapService.java` | 2.3.3 | Create | Sitemap generation |
| `backend/.../controller/SitemapController.java` | 2.3.3 | Create | Sitemap endpoints |
| `backend/.../scheduler/SitemapScheduler.java` | 2.3.3 | Create | Daily regeneration |

### Epic 2.3 Acceptance Criteria

- [ ] JSON-LD includes @id, sameAs, and all enriched properties
- [ ] Nested Schema.org objects generated correctly
- [ ] Public entity pages display all entity information
- [ ] JSON-LD embedded in page <head> for search engines
- [ ] Open Graph meta tags present for social sharing
- [ ] XML sitemap lists all public entities
- [ ] Sitemap regenerated daily
- [ ] JSON-LD passes Google Structured Data Testing Tool

---

## Epic 2.4: Schema.org Validation

**Priority:** P1 (Important)
**Sprint:** 4
**Story Points:** 11

### Description

Validate all Schema.org data against the official vocabulary to ensure standards compliance and data quality.

### Stories

| # | Story | Points | Status | Assignee |
|---|-------|--------|--------|----------|
| 2.4.1 | [Schema Validation Service](./2.4.1.schema-validation-service.md) | 5 | Draft | - |
| 2.4.2 | [Validation API Endpoint](./2.4.2.validation-api-endpoint.md) | 3 | Draft | - |
| 2.4.3 | [Bulk Validation Report](./2.4.3.bulk-validation-report.md) | 3 | Draft | - |

### Files to Create

| File | Story | Action | Purpose |
|------|-------|--------|---------|
| `reasoning-service/app/services/schema_validator.py` | 2.4.1 | Create | Validation logic |
| `reasoning-service/app/api/validation.py` | 2.4.2 | Create | Validation endpoints |
| `reasoning-service/data/schema-org-vocab.json` | 2.4.1 | Create | Cached vocabulary |
| `backend/.../service/BulkValidationService.java` | 2.4.3 | Create | Bulk validation |
| `backend/.../scheduler/ValidationReportScheduler.java` | 2.4.3 | Create | Weekly reports |
| `backend/.../controller/ValidationReportController.java` | 2.4.3 | Create | Report endpoints |

### Epic 2.4 Acceptance Criteria

- [ ] Validates @context, @type, required properties
- [ ] Validates property value formats (dates, URLs)
- [ ] Supports strict and lenient validation levels
- [ ] Batch validation endpoint handles up to 100 entities
- [ ] Weekly validation report generated automatically
- [ ] Report includes errors by type and property
- [ ] CSV export available for invalid entities
- [ ] 90%+ of entities pass validation

---

## Sprint Plan

### Sprint 1 (Weeks 1-2): External Linking Foundation

**Goal:** Wikidata and DBpedia clients operational, basic linking working

| Story | Points | Priority |
|-------|--------|----------|
| 2.1.1 Wikidata Entity Lookup | 5 | P0 |
| 2.1.2 DBpedia Entity Lookup | 3 | P0 |
| 2.1.4 External Linking API | 5 | P0 |
| **Total** | **13** | |

**Exit Criteria:**
- [ ] Can link "EPA" to Wikidata Q217173
- [ ] Can link "Elizabeth Warren" to Wikidata Q434706
- [ ] API returns linked entity with external IDs

---

### Sprint 2 (Weeks 3-4): Disambiguation & Enrichment

**Goal:** Disambiguation algorithm working, property extraction operational

| Story | Points | Priority |
|-------|--------|----------|
| 2.1.3 Entity Disambiguation | 8 | P0 |
| 2.2.1 Wikidata Property Extraction | 5 | P0 |
| 2.2.2 Property Merge Strategy | 3 | P0 |
| **Total** | **16** | |

**Exit Criteria:**
- [ ] Disambiguation handles "EPA" vs disambiguation page
- [ ] Enriched entity has 5+ properties from Wikidata
- [ ] Property provenance tracked correctly

---

### Sprint 3 (Weeks 5-6): Pipeline & Java Integration

**Goal:** Background enrichment pipeline operational, Java backend integrated

| Story | Points | Priority |
|-------|--------|----------|
| 2.2.3 Enrichment Pipeline | 8 | P0 |
| 2.2.4 Java Backend Integration | 5 | P0 |
| **Total** | **13** | |

**Exit Criteria:**
- [ ] Entity creation triggers background enrichment
- [ ] Enrichment status visible in API response
- [ ] Batch enrichment processes 100 entities in <60 seconds

---

### Sprint 4 (Weeks 7-8): JSON-LD & Validation

**Goal:** JSON-LD publishing complete, validation operational

| Story | Points | Priority |
|-------|--------|----------|
| 2.3.1 JSON-LD Generation | 5 | P1 |
| 2.3.2 Public Entity Pages | 5 | P1 |
| 2.3.3 Entity Sitemap | 3 | P1 |
| 2.4.1 Schema Validation Service | 5 | P1 |
| 2.4.2 Validation API Endpoint | 3 | P1 |
| 2.4.3 Bulk Validation Report | 3 | P1 |
| **Total** | **24** | |

**Exit Criteria:**
- [ ] JSON-LD passes Google Structured Data Testing Tool
- [ ] Entity pages indexed by search engines
- [ ] Validation report shows >90% valid entities

---

## Dependencies Graph

```
Epic 2.1: External Linking
├── 2.1.1 Wikidata Client
├── 2.1.2 DBpedia Client
├── 2.1.3 Disambiguation ──────┐
│       (depends on 2.1.1, 2.1.2)
└── 2.1.4 Linking API ─────────┤
        (depends on 2.1.1-2.1.3)
                               │
Epic 2.2: Enrichment           │
├── 2.2.1 Property Extraction ◀┘
│       (depends on 2.1.4)
├── 2.2.2 Property Merger
│       (depends on 2.2.1)
├── 2.2.3 Pipeline ────────────┐
│       (depends on 2.2.1, 2.2.2)
└── 2.2.4 Java Integration ◀───┘
        (depends on 2.2.3)
                               │
Epic 2.3: JSON-LD Publishing   │
├── 2.3.1 JSON-LD Generation ◀─┘
│       (depends on 2.2.4)
├── 2.3.2 Public Pages
│       (depends on 2.3.1)
└── 2.3.3 Sitemap
        (depends on 2.3.2)

Epic 2.4: Validation
├── 2.4.1 Validation Service
│       (no dependencies)
├── 2.4.2 Validation API
│       (depends on 2.4.1)
└── 2.4.3 Bulk Reports
        (depends on 2.4.2, 2.3.1)
```

---

## Technical Notes

### New Dependencies

```
# Python (reasoning-service/requirements.txt)
requests>=2.31.0      # HTTP client
cachetools>=5.3.0     # TTL cache
rapidfuzz>=3.0.0      # String matching

# Java (backend/pom.xml)
spring-boot-starter-webflux  # WebClient for async HTTP
resilience4j-spring-boot2    # Circuit breaker
```

### Database Migration

**V5__add_enrichment_fields.sql:**
- `enrichment_status` VARCHAR(20)
- `enrichment_error` TEXT
- `enrichment_retry_count` INTEGER
- `enriched_at` TIMESTAMP
- Indexes for enrichment queue and external IDs

### Configuration

```yaml
# application.yml
enrichment:
  service:
    url: ${ENRICHMENT_SERVICE_URL:http://localhost:8000}
    timeout: 5s
validation:
  report:
    schedule: "0 0 3 * * SUN"  # Sunday 3 AM
```

---

## Risk Mitigation

| Risk | Mitigation |
|------|------------|
| External API unavailable | Circuit breaker + queue for retry |
| Wrong entity linked | High confidence threshold (0.8) + manual review |
| External data incorrect | Never overwrite local + provenance tracking |
| Performance impact | Async processing + caching |

---

## Approval

| Role | Name | Status | Date |
|------|------|--------|------|
| Product Owner | Sarah (PO) | APPROVED | 2025-11-25 |
| System Architect | Winston | PENDING | - |
| Tech Lead | TBD | PENDING | - |

---

## Change Log

| Date | Version | Description | Author |
|------|---------|-------------|--------|
| 2025-11-25 | 1.0 | Initial epic index creation | Sarah (PO) |

---

*End of Phase 2 Epic Index*
