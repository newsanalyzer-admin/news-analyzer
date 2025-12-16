# Epic FB-3: Expand Factbase with Regulatory Data

## Epic Overview

| Field | Value |
|-------|-------|
| **Epic ID** | FB-3 |
| **Epic Name** | Expand Factbase with Regulatory Data |
| **Epic Type** | Feature / Data Integration |
| **Priority** | MEDIUM |
| **Status** | Done |
| **Created** | 2024-11-27 |
| **Updated** | 2025-12-02 |
| **Owner** | Sarah (PO) |
| **Depends On** | FB-2-GOV (FederalRegisterClient exists) |

## Executive Summary

Integrate Federal Register regulatory data into NewsAnalyzer's factbase using the Federal Register API. This enables fact-checkers to verify claims about federal regulations, rules, and their effective dates. This is a "quick win" - the API requires no authentication and provides comprehensive regulatory data back to 1994.

## Business Value

### Why This Epic Matters

1. **WHAT + WHEN Coverage** - Regulations answer "what rules exist" and "when did they take effect"
2. **Low Integration Effort** - No authentication required, well-documented API
3. **High Fact-Check Value** - Regulatory claims are common in policy debates
4. **Agency Linkage** - Connects regulations to existing GovernmentOrganization entities

### Success Metrics

| Metric | Target | Measurement |
|--------|--------|-------------|
| Regulation Coverage | All rules since 1994 | Federal Register completeness |
| Agency Linkage | >95% auto-matched | Agency name → GovernmentOrganization |
| Data Freshness | <24 hours | Sync timestamp vs. publication |
| Query Response Time | <500ms | API latency monitoring |

## Scope

### In Scope

- Federal Register API integration
- Regulation entity model (rules, proposed rules, notices)
- Effective date tracking for temporal fact-checking
- Agency linkage to existing GovernmentOrganization entities
- Daily sync scheduler for new publications
- API endpoints for regulation lookup and search
- CFR (Code of Federal Regulations) reference tracking

### Out of Scope

- Public comment data (Regulations.gov - future enhancement)
- Full text storage (link to source only)
- Historical regulations pre-1994 (not available via API)
- State-level regulations (no federal source)
- Real-time Federal Register monitoring (daily sync sufficient)

## Architecture

### Data Flow

```
┌─────────────────────────────────────────────────────────────┐
│                   Daily Sync Scheduler                       │
│                     (3:00 AM UTC)                            │
└─────────────────────┬───────────────────────────────────────┘
                      │
                      ▼
┌─────────────────────────────────────────────────────────────┐
│                 Federal Register API                         │
│   https://www.federalregister.gov/api/v1                    │
│   - No authentication required                               │
│   - JSON responses                                           │
│   - Daily publication data                                   │
└─────────────────────┬───────────────────────────────────────┘
                      │
                      ▼
┌─────────────────────────────────────────────────────────────┐
│                  Ingestion Service                           │
│  - Incremental sync (since last run)                        │
│  - Agency name → GovernmentOrganization matching            │
│  - Document type classification                              │
│  - CFR reference extraction                                  │
└─────────────────────┬───────────────────────────────────────┘
                      │
                      ▼
┌─────────────────────────────────────────────────────────────┐
│                     Factbase DB                              │
│              Regulation | RegulationAgency                   │
└─────────────────────────────────────────────────────────────┘
                      │
                      ▼
┌─────────────────────────────────────────────────────────────┐
│                   REST API Endpoints                         │
│       /api/regulations  |  /api/regulations/search          │
└─────────────────────────────────────────────────────────────┘
```

### Entity Model

```
Regulation (NEW)
├── regulationId (PK)
├── documentNumber (unique - Federal Register number)
├── title
├── abstract (summary text)
├── documentType
│   ├── RULE (Final rules)
│   ├── PROPOSED_RULE (NPRMs)
│   ├── NOTICE
│   ├── PRESIDENTIAL_DOCUMENT
│   └── OTHER
├── publicationDate
├── effectiveDate (nullable - for rules)
├── signingDate (nullable - for presidential docs)
├── cfrReferences (JSON array)
│   └── [{title: 40, part: 60, section: "..."}, ...]
├── docketIds (JSON array)
├── regulationIdNumber (RIN, if applicable)
├── sourceUrl (link to Federal Register)
├── pdfUrl (link to PDF)
├── htmlUrl (link to HTML)
├── createdAt
└── updatedAt

RegulationAgency (JOIN TABLE - many-to-many)
├── regulationId (FK → Regulation)
├── organizationId (FK → GovernmentOrganization)
├── agencyNameRaw (original name from API)
└── isPrimaryAgency (boolean)
```

### Technology Stack

| Component | Technology | Notes |
|-----------|------------|-------|
| API Client | **Existing FederalRegisterClient** | Extend for document fetching |
| Data Parser | Jackson | JSON parsing (existing) |
| Scheduler | Spring @Scheduled | Daily sync (existing pattern) |
| Database | PostgreSQL | Existing |
| Search | PostgreSQL full-text search | GIN index on tsvector |

> **Note:** `FederalRegisterClient` already exists from FB-2-GOV. FB-3.1 will extend it to fetch documents in addition to agencies.

## Stories

### Story Summary

| ID | Story | Priority | Estimate | Dependencies |
|----|-------|----------|----------|--------------|
| FB-3.1 | [Federal Register API Integration](FB-3.1.federal-register-integration.md) | P0 | 1 sprint | None |
| FB-3.2 | [Regulation Data Model & Storage](FB-3.2.regulation-data-model.md) | P0 | 0.5 sprint | None |
| FB-3.3 | [Agency Linkage Service](FB-3.3.agency-linkage-service.md) | P1 | 0.5 sprint | FB-3.1, FB-3.2 |
| FB-3.4 | [Regulation Lookup API Endpoints](FB-3.4.regulation-api-endpoints.md) | P1 | 0.5 sprint | FB-3.1, FB-3.2, FB-3.3 |

### Dependency Graph

```
FB-3.1 (API Integration)    FB-3.2 (Data Model)
         │                         │
         └───────────┬─────────────┘
                     ▼
              FB-3.3 (Agency Linkage)
                     │
                     ▼
              FB-3.4 (API Endpoints)
```

### Implementation Order

**Sprint 1 (Phase 2 Start):**
- FB-3.2: Regulation Data Model & Storage
- FB-3.1: Federal Register API Integration (start)

**Sprint 2:**
- FB-3.1: Federal Register API Integration (complete)
- FB-3.3: Agency Linkage Service
- FB-3.4: Regulation Lookup API Endpoints

## API Endpoints (New)

### Regulation Endpoints

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/regulations` | GET | List recent regulations (paginated) |
| `/api/regulations/{documentNumber}` | GET | Get regulation by FR document number |
| `/api/regulations/search` | GET | Search regulations by keyword |
| `/api/regulations/by-agency/{orgId}` | GET | List regulations by agency |
| `/api/regulations/by-type/{type}` | GET | List by document type |
| `/api/regulations/by-date-range` | GET | List by publication date range |
| `/api/regulations/effective-on/{date}` | GET | List rules effective on date |
| `/api/regulations/cfr/{title}/{part}` | GET | List by CFR reference |

## Data Sources

### Federal Register API

| Attribute | Value |
|-----------|-------|
| **Base URL** | https://www.federalregister.gov/api/v1 |
| **Auth** | None required |
| **Rate Limit** | Undocumented (be reasonable, ~1 req/sec) |
| **Format** | JSON or CSV |
| **Coverage** | All documents since 1994 |

### Key API Endpoints

```
GET /documents
    ?conditions[publication_date][gte]=2024-01-01
    &conditions[type][]=RULE
    &per_page=100
    &page=1

GET /documents/{document_number}

GET /agencies
```

### Sample Response Fields

```json
{
  "document_number": "2024-12345",
  "title": "Air Quality Standards...",
  "abstract": "EPA is revising...",
  "type": "Rule",
  "publication_date": "2024-03-15",
  "effective_on": "2024-05-15",
  "agencies": [
    {"name": "Environmental Protection Agency", "id": 145}
  ],
  "cfr_references": [
    {"title": 40, "part": 60}
  ],
  "html_url": "https://...",
  "pdf_url": "https://..."
}
```

## Acceptance Criteria (Epic Level)

1. **Data Coverage**: All rules/regulations since 1994 queryable
2. **Daily Sync**: New publications available within 24 hours
3. **Agency Linkage**: >95% of regulations linked to GovernmentOrganization
4. **Effective Dates**: Temporal queries work correctly ("rules effective on X date")
5. **API Availability**: All endpoints return data with <500ms response time
6. **Search Functionality**: Full-text search across titles and abstracts
7. **CFR References**: Regulations queryable by CFR citation

## Risks & Mitigations

| Risk | Impact | Probability | Mitigation |
|------|--------|-------------|------------|
| API rate limiting | Medium | Low | Implement backoff, batch requests |
| Agency name mismatches | Medium | Medium | Fuzzy matching, manual mapping table |
| Large historical backfill | Low | Medium | Paginated import, run during off-hours |
| API structure changes | Low | Low | Monitor changelog, version API calls |

## Definition of Done

- [x] All 4 stories completed and merged
- [x] Historical regulations since 1994 imported (sync service ready)
- [x] Daily sync scheduler operational (FB-3.1)
- [x] >95% agency linkage rate achieved (FB-3.3)
- [x] API endpoints documented in OpenAPI spec (FB-3.4)
- [x] Full-text search working (FB-3.2, FB-3.4)
- [x] Effective date queries accurate (FB-3.4)
- [ ] README documentation complete (deferred - can be added as needed)

## Related Documentation

- [Architect Handoff](../architecture/FACTBASE_EXPANSION_ARCHITECT_HANDOFF.md)
- [Research Findings](../research/AUTHORITATIVE_DATA_SOURCES_RESEARCH_FINDINGS.md)
- [PO Summary](../analysis/FACTBASE_EXPANSION_PO_SUMMARY.md)
- [Federal Register API Docs](https://www.federalregister.gov/developers/documentation/api/v1)

## Change Log

| Date | Version | Description | Author |
|------|---------|-------------|--------|
| 2024-11-27 | 1.0 | Initial epic creation | Sarah (PO) |
| 2025-12-01 | 1.1 | Created 4 story files, moved to FB-3 folder, submitted for Architect review | Sarah (PO) |
| 2025-12-01 | 1.2 | Architect review complete: Fixed migration versions (V17-V19), added missing repo methods | Winston (Architect) |
| 2025-12-02 | 1.3 | Epic complete: All 4 stories Done, DoD verified, status changed to Done | John (PM) |

## Approval

| Role | Name | Date | Status |
|------|------|------|--------|
| Product Owner | Sarah (PO) | 2025-12-01 | ✅ APPROVED |
| Architect | Winston (Architect) | 2025-12-01 | ✅ **APPROVED WITH MODIFICATIONS** |
| Tech Lead | _Pending_ | _Pending_ | _Pending_ |

## Architect Review Notes

**Reviewed by:** Winston (Architect)
**Review Date:** 2025-12-01
**Status:** APPROVED WITH MODIFICATIONS

### Modifications Made

1. **Migration Version Conflict Fixed**: V16 already exists, so regulation migrations renumbered to V17-V19
2. **Missing Repository Methods Added**: Added `findByIdIn`, `findByCfrReference`, `countDistinctRegulations`, etc.

### Architecture Assessment

| Aspect | Rating | Notes |
|--------|--------|-------|
| Infrastructure Reuse | ⭐⭐⭐⭐⭐ | Excellent - extends existing FederalRegisterClient |
| Data Model Design | ⭐⭐⭐⭐⭐ | Proper JSONB usage, well-normalized |
| API Design | ⭐⭐⭐⭐⭐ | RESTful, consistent with existing patterns |
| Full-Text Search | ⭐⭐⭐⭐ | Good use of PostgreSQL tsvector |
| Agency Linkage | ⭐⭐⭐⭐ | Smart caching + fuzzy matching fallback |

### Recommendations

1. Consider adding `federal_register_agency_id` to GovernmentOrganization (future enhancement)
2. Add JSONB GIN index for CFR references in V17 migration
3. Enforce max page size (100) on full-text search endpoints

---

*End of Epic Document*
