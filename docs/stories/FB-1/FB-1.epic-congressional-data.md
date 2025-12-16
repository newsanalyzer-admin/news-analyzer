# Epic FB-1: Expand Factbase with Congressional Data

## Epic Overview

| Field | Value |
|-------|-------|
| **Epic ID** | FB-1 |
| **Epic Name** | Expand Factbase with Congressional Data |
| **Epic Type** | Feature / Data Integration |
| **Priority** | HIGH |
| **Status** | Done |
| **Created** | 2024-11-27 |
| **Owner** | Sarah (PO) |
| **Phase** | Phase 1 |

## Executive Summary

Integrate authoritative Congressional data into NewsAnalyzer's factbase, enabling fact-checkers to verify claims about Members of Congress, committee assignments, and position history. This epic leverages the Congress.gov API and unitedstates/congress-legislators dataset to establish the foundation for the "WHO" dimension of government fact-checking.

## Business Value

### Why This Epic Matters

1. **Core WHO Data** - Members of Congress are frequently mentioned in news; verifiable data is essential
2. **Foundation for Expansion** - Establishes entity model patterns for future data sources
3. **Cross-Reference Capability** - BioGuide IDs enable linking to FEC, voting records, and more
4. **Historical Context** - Term history allows temporal fact-checking ("Was X in office when...")

### Success Metrics

| Metric | Target | Measurement |
|--------|--------|-------------|
| Member Coverage | 100% current Congress | Count vs. official roster |
| Committee Coverage | 100% standing committees | Count vs. Congress.gov |
| Data Freshness | <24 hours | Sync timestamp delta |
| Query Response Time | <500ms | API latency monitoring |

## Scope

### In Scope

- Congress.gov API integration for member and committee data
- unitedstates/congress-legislators dataset sync for enrichment
- Person entity type with Congressional member data
- GovernmentPosition entity for Congressional positions
- PositionHolding temporal join for term history
- Committee entity and membership relationships
- Daily sync scheduler for data freshness
- API endpoints for member and committee lookup

### Out of Scope

- Bill and vote data (future enhancement)
- Sponsored legislation tracking (future enhancement)
- Campaign finance linkage (Phase 4, Epic extension)
- State-level legislators (no federal API available)
- Real-time updates (daily sync confirmed sufficient)

## Architecture

### Entity Model Extensions

```
Existing:
├── Entity (base)
└── GovernmentOrganization

New/Extended:
├── Person (NEW)
│   ├── personId (PK)
│   ├── bioguideId (unique, indexed)
│   ├── firstName, lastName, middleName, suffix
│   ├── birthDate
│   ├── gender
│   ├── party
│   ├── externalIds (JSON: fec, govtrack, opensecrets, etc.)
│   └── imageUrl
│
├── GovernmentPosition (NEW)
│   ├── positionId (PK)
│   ├── title
│   ├── chamber (SENATE | HOUSE)
│   ├── state
│   ├── district (nullable, House only)
│   ├── positionType (ELECTED)
│   └── organizationId (FK → GovernmentOrganization)
│
├── PositionHolding (NEW - temporal join)
│   ├── holdingId (PK)
│   ├── personId (FK → Person)
│   ├── positionId (FK → GovernmentPosition)
│   ├── startDate
│   ├── endDate (nullable = current)
│   ├── congress (e.g., 118, 119)
│   └── source (CONGRESS_GOV | LEGISLATORS_REPO)
│
└── Committee (NEW)
    ├── committeeId (PK)
    ├── committeeCode (unique)
    ├── name
    ├── chamber
    ├── committeeType (Standing, Select, Joint, etc.)
    ├── parentCommitteeId (FK, nullable - for subcommittees)
    └── thomasId
```

### Data Flow

```
┌─────────────────────────────────────────────────────────────┐
│                   Daily Sync Scheduler                       │
│                     (2:00 AM UTC)                            │
└─────────────────────┬───────────────────────────────────────┘
                      │
        ┌─────────────┴─────────────┐
        ▼                           ▼
┌───────────────┐           ┌───────────────┐
│ Congress.gov  │           │  congress-    │
│     API       │           │  legislators  │
│  (Primary)    │           │  (Enrichment) │
└───────┬───────┘           └───────┬───────┘
        │                           │
        │    ┌──────────────────────┘
        ▼    ▼
┌─────────────────────────────────────────────────────────────┐
│                  Ingestion Service                           │
│  - Rate limiting (5000/hr)    - ID cross-referencing        │
│  - Deduplication by BioGuide  - Historical data (1990s+)    │
└─────────────────────┬───────────────────────────────────────┘
                      │
                      ▼
┌─────────────────────────────────────────────────────────────┐
│                     Factbase DB                              │
│   Person | Position | PositionHolding | Committee           │
└─────────────────────────────────────────────────────────────┘
                      │
                      ▼
┌─────────────────────────────────────────────────────────────┐
│                   REST API Endpoints                         │
│  /api/members  |  /api/committees  |  /api/positions        │
└─────────────────────────────────────────────────────────────┘
```

### Technology Stack

| Component | Technology | Notes |
|-----------|------------|-------|
| API Client | Java HttpClient or RestTemplate | For Congress.gov API |
| Data Parser | Jackson | JSON parsing |
| Scheduler | Spring @Scheduled | Daily sync |
| Database | PostgreSQL | Existing |
| Caching | TBD (Architect decision) | Rate limit protection |

## Stories

### Story Summary

| ID | Story | Priority | Estimate | Dependencies |
|----|-------|----------|----------|--------------|
| FB-1.0 | [API Integration Test Coverage](FB-1.0.api-test-integration.md) | P0 | 0.5 sprint | FB-1.1 |
| FB-1.1 | [Member Lookup API Integration](FB-1.1.member-lookup-integration.md) | P0 | 1 sprint | None |
| FB-1.2 | [Committee Data Integration](FB-1.2.committee-data-integration.md) | P0 | 1 sprint | FB-1.1 |
| FB-1.3 | [Position History & Term Tracking](FB-1.3.position-history-tracking.md) | P1 | 1 sprint | FB-1.1 |
| FB-1.4 | [Congress-Legislators Enrichment Sync](FB-1.4.legislators-enrichment-sync.md) | P1 | 0.5 sprint | FB-1.1 |

### Dependency Graph

```
FB-1.1 (Member Lookup)
    │
    ├──────────────┬──────────────┬──────────────┐
    ▼              ▼              ▼              ▼
FB-1.0         FB-1.2         FB-1.3         FB-1.4
(API Tests)  (Committees)   (History)    (Enrichment)
```

### Implementation Order

**Sprint 1:**
- FB-1.1: Member Lookup API Integration (complete)
- FB-1.0: API Integration Test Coverage (ready)
- FB-1.4: Congress-Legislators Enrichment Sync (start)

**Sprint 2:**
- FB-1.4: Congress-Legislators Enrichment Sync (complete)
- FB-1.2: Committee Data Integration
- FB-1.3: Position History & Term Tracking

## API Endpoints (New)

### Member Endpoints

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/members` | GET | List all current members |
| `/api/members/{bioguideId}` | GET | Get member by BioGuide ID |
| `/api/members/search` | GET | Search members by name |
| `/api/members/by-state/{state}` | GET | List members by state |
| `/api/members/by-chamber/{chamber}` | GET | List by SENATE or HOUSE |
| `/api/members/{bioguideId}/committees` | GET | Get member's committees |
| `/api/members/{bioguideId}/terms` | GET | Get member's term history |

### Committee Endpoints

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/committees` | GET | List all committees |
| `/api/committees/{code}` | GET | Get committee by code |
| `/api/committees/{code}/members` | GET | List committee members |
| `/api/committees/{code}/subcommittees` | GET | List subcommittees |
| `/api/committees/by-chamber/{chamber}` | GET | List by chamber |

## Data Sources

### Congress.gov API

| Attribute | Value |
|-----------|-------|
| **Base URL** | https://api.congress.gov/v3 |
| **Auth** | API key (api.data.gov) |
| **Rate Limit** | 5,000 requests/hour |
| **Key Endpoints** | /member, /committee |

### unitedstates/congress-legislators

| Attribute | Value |
|-----------|-------|
| **URL** | https://github.com/unitedstates/congress-legislators |
| **Format** | YAML, JSON |
| **Files** | legislators-current.yaml, legislators-historical.yaml |
| **Enrichment** | Social media, external IDs, district offices |

## Acceptance Criteria (Epic Level)

1. **Member Coverage**: All 535+ current Members of Congress are in the factbase
2. **Committee Coverage**: All standing, select, and joint committees are represented
3. **Term History**: Position history available for members serving since 1990s
4. **Data Freshness**: Daily sync runs successfully with <24hr data lag
5. **API Availability**: All new endpoints return data with <500ms response time
6. **Cross-References**: BioGuide, FEC, and GovTrack IDs stored for each member
7. **Search Functionality**: Members searchable by name, state, and chamber

## Risks & Mitigations

| Risk | Impact | Probability | Mitigation |
|------|--------|-------------|------------|
| API rate limit exceeded | High | Medium | Implement caching, batch requests |
| Congress.gov API changes | Medium | Low | Monitor changelog, version API calls |
| Historical data gaps | Low | Medium | Document known gaps, use multiple sources |
| BioGuide ID conflicts | Low | Low | Validate uniqueness, alert on conflicts |

## Definition of Done

- [x] All 5 stories completed and merged
- [x] 100% of current Congress members in factbase
- [x] All standing committees represented
- [x] Daily sync scheduler operational
- [x] API endpoints documented in OpenAPI spec
- [x] Integration tests passing
- [x] Data freshness monitoring in place
- [x] README documentation complete

## Related Documentation

- [Architect Handoff](../architecture/FACTBASE_EXPANSION_ARCHITECT_HANDOFF.md)
- [Research Findings](../research/AUTHORITATIVE_DATA_SOURCES_RESEARCH_FINDINGS.md)
- [PO Summary](../analysis/FACTBASE_EXPANSION_PO_SUMMARY.md)
- [Congress.gov API Docs](https://github.com/LibraryOfCongress/api.congress.gov)

## Change Log

| Date | Version | Description | Author |
|------|---------|-------------|--------|
| 2024-11-27 | 1.0 | Initial epic creation | Sarah (PO) |
| 2024-11-27 | 1.1 | Created all 4 story files with full detail | Sarah (PO) |
| 2024-11-27 | 1.2 | Architecture approved by Winston | Winston (Architect) |
| 2024-11-28 | 1.3 | SM review complete: All stories validated, status → Ready | Bob (SM) |
| 2024-11-28 | 1.4 | Added FB-1.0 (API Integration Test Coverage) story | Sarah (PO) |
| 2024-11-28 | 1.5 | FB-1.4 (Enrichment Sync) completed - QA PASS 95/100 | Sarah (PO) |
| 2025-11-30 | 1.6 | Epic status → Done: All 5 stories completed, DoD fulfilled | Sarah (PO) |

## Approval

| Role | Name | Date | Signature |
|------|------|------|-----------|
| Product Owner | Sarah (PO) | 2024-11-27 | Drafted |
| Architect | Winston | 2024-11-27 | **APPROVED** |
| Tech Lead | _TBD_ | _Pending_ | _Pending_ |

---

*End of Epic Document*
