# Factbase Expansion: Architect Handoff

## Overview

This document provides the architect with confirmed scope, technical requirements, and design considerations for expanding NewsAnalyzer's factbase with authoritative government data sources.

**Status**: Architecture Review Complete - **APPROVED**

---

## Confirmed Scope Decisions

All scope decisions have been confirmed by the product stakeholder:

| Decision | Confirmed Value | Rationale |
|----------|-----------------|-----------|
| **Integration Strategy** | Deep first, then expand | Validates architecture before scaling |
| **Historical Data** | 1990s-present | Matches Federal Register availability, reduces complexity |
| **Geographic Integration** | ID linkage only (defer GIS) | Marginal MVP value vs. complexity |
| **Campaign Finance** | Candidate IDs only (Phase 1) | Core WHO enablement without full FEC complexity |
| **Update Frequency** | Daily sync | Real-time is over-engineering for fact-checking |
| **OPM PLUM Strategy** | Web scraper | No API available; confirmed approach |

---

## Confirmed Implementation Phases

| Phase | Scope | Data Sources | Epic | Effort |
|-------|-------|--------------|------|--------|
| **Phase 1** | Congressional Members & Committees | Congress.gov API, congress-legislators | Epic 1 | Small-Medium |
| **Phase 2** | Regulatory Data | Federal Register API | Epic 3 | Small |
| **Phase 3** | Executive Appointees | OPM PLUM (web scraper) | Epic 2 | Medium-High |
| **Phase 4** | Campaign Finance (candidates) | FEC OpenFEC | Epic 1 ext. | Medium |
| **Phase 5** | Geographic Context | Census TIGER | Deferred | Medium-Large |
| **Phase 6** | Judicial Data | CourtListener | Deferred | Medium |

---

## Phase 1: Congressional Data - Technical Requirements

### Data Sources

#### 1. Congress.gov API (Primary)

| Attribute | Value |
|-----------|-------|
| **URL** | https://api.congress.gov |
| **Auth** | API key (api.data.gov) |
| **Rate Limit** | 5,000 requests/hour |
| **Format** | JSON or XML |
| **Update Frequency** | Daily (votes every 30 min) |

**Key Endpoints**:
```
/v3/member                    - List all members
/v3/member/{bioguideId}       - Specific member details
/v3/committee                 - List committees
/v3/committee/{chamber}/{code} - Committee details
```

**Required Data**:
- Member bioguide ID, name, party, state, district
- Current term start/end dates
- Committee assignments
- Leadership positions

#### 2. unitedstates/congress-legislators (Enrichment)

| Attribute | Value |
|-----------|-------|
| **URL** | https://github.com/unitedstates/congress-legislators |
| **Auth** | None |
| **Format** | YAML, JSON, CSV |
| **Update Frequency** | Weekly sync recommended |

**Key Files**:
- `legislators-current.yaml` - Current members
- `legislators-historical.yaml` - Historical (filter to 1990s+)
- `committee-membership-current.yaml` - Committee assignments

**Enrichment Data**:
- Cross-reference IDs (bioguide, thomas, govtrack, fec, opensecrets)
- Birthday, gender
- Social media accounts
- District office locations

### Design Considerations

#### Entity Model Extensions

```
Current: GovernmentOrganization
         └── (existing entity)

Proposed Extensions:

GovernmentPosition (NEW)
├── positionId (PK)
├── title
├── organizationId (FK → GovernmentOrganization)
├── chamber (SENATE | HOUSE | EXECUTIVE | JUDICIAL)
├── jurisdiction (state, district, etc.)
├── positionType (ELECTED | APPOINTED | CAREER)
└── schema.org type mapping

Person (NEW or extend Entity)
├── personId (PK)
├── bioguideId (unique, nullable)
├── name (first, last, middle, suffix)
├── birthDate
├── party
├── externalIds (JSON: fec, opensecrets, etc.)
└── schema.org type: Person

PositionHolding (NEW - temporal join)
├── holdingId (PK)
├── personId (FK → Person)
├── positionId (FK → GovernmentPosition)
├── startDate
├── endDate (nullable = current)
└── source (CONGRESS_GOV | PLUM | etc.)
```

#### Key Identifier Strategy

| Entity Type | Primary ID | Cross-Reference IDs |
|-------------|------------|---------------------|
| Congress Member | BioGuide ID | thomas, govtrack, fec, opensecrets |
| Committee | Committee Code | thomas_id |
| Executive Appointee | (generated) | plum_id (if available) |
| Candidate | FEC Candidate ID | bioguide (if member) |

**Recommendation**: Store all known IDs in a JSON/JSONB `externalIds` field for future integrations.

#### Sync Architecture

```
┌─────────────────────────────────────────────────────────┐
│                    Sync Scheduler                        │
│                   (Daily @ 2am UTC)                      │
└─────────────────────┬───────────────────────────────────┘
                      │
        ┌─────────────┼─────────────┐
        ▼             ▼             ▼
┌───────────┐  ┌───────────┐  ┌───────────┐
│Congress.gov│  │ congress- │  │  Federal  │
│    API    │  │legislators│  │ Register  │
└─────┬─────┘  └─────┬─────┘  └─────┬─────┘
      │              │              │
      ▼              ▼              ▼
┌─────────────────────────────────────────────────────────┐
│                  Ingestion Service                       │
│  - Rate limiting    - Deduplication    - Validation     │
└─────────────────────┬───────────────────────────────────┘
                      │
                      ▼
┌─────────────────────────────────────────────────────────┐
│                    Factbase DB                           │
│  Person | Position | PositionHolding | Organization     │
└─────────────────────────────────────────────────────────┘
```

#### API Key Management

- Single api.data.gov key works for: Congress.gov, GovInfo, FEC
- Store in environment variable or secrets manager
- Implement exponential backoff for rate limit handling

---

## Phase 2: Regulatory Data - Technical Requirements

### Federal Register API

| Attribute | Value |
|-----------|-------|
| **URL** | https://www.federalregister.gov/developers/documentation/api/v1 |
| **Auth** | None required |
| **Rate Limit** | Undocumented (be reasonable) |
| **Format** | JSON or CSV |

**Key Endpoints**:
```
/documents              - Search/list documents
/documents/{number}     - Specific document
/agencies               - List agencies
```

**Required Data**:
- Document number, title, abstract
- Document type (rule, proposed_rule, notice, presidential_document)
- Agency name(s)
- Publication date
- Effective date
- CFR references

### Design Considerations

#### Regulation Entity

```
Regulation (NEW)
├── regulationId (PK)
├── federalRegisterNumber (unique)
├── title
├── abstract
├── documentType
├── agencyIds (FK[] → GovernmentOrganization)
├── publicationDate
├── effectiveDate
├── cfrReferences (JSON)
├── sourceUrl
└── schema.org type mapping (Legislation?)
```

**Note**: Link regulations to existing GovernmentOrganization entities via agency name matching.

---

## Phase 3: Executive Appointees - Technical Requirements

### OPM PLUM Data (Web Scraper)

| Attribute | Value |
|-----------|-------|
| **URL** | https://www.opm.gov/about-us/open-government/plum-reporting/plum-data/ |
| **Auth** | None |
| **Format** | HTML (requires scraping) |
| **Update Frequency** | Quarterly |

**Required Data**:
- Agency name
- Position title
- Pay plan and grade
- Appointment type (PAS, PA, NA, etc.)
- Incumbent name (if filled)

### Web Scraper Considerations

1. **Respect robots.txt** - Check scraping policies
2. **Rate limiting** - Add delays between requests
3. **Change detection** - Hash page content to detect updates
4. **Error handling** - Graceful degradation if site structure changes
5. **Fallback** - Monitor for API release; sunset scraper when available

### Design Considerations

- Reuse `Person` and `GovernmentPosition` entities from Phase 1
- Add `appointmentType` field to `GovernmentPosition`
- Link positions to `GovernmentOrganization` via agency name

---

## Cross-Cutting Concerns

### Data Freshness

| Source | Sync Frequency | Freshness Indicator |
|--------|----------------|---------------------|
| Congress.gov API | Daily | `lastSyncedAt` timestamp |
| congress-legislators | Weekly | Git commit hash |
| Federal Register | Daily | Publication date |
| OPM PLUM | Quarterly | Scrape timestamp |

**Recommendation**: Expose freshness metadata in API responses for transparency.

### Schema.org Mapping

| Entity | Schema.org Type |
|--------|----------------|
| Person (Congress) | schema:Person, schema:GovernmentMember |
| GovernmentPosition | schema:Role, schema:GovernmentRole |
| Regulation | schema:Legislation |
| GovernmentOrganization | schema:GovernmentOrganization (existing) |

### Error Handling

- **API failures**: Retry with exponential backoff; alert after 3 failures
- **Rate limits**: Queue and throttle; never exceed limits
- **Data validation**: Log invalid records; don't fail entire sync
- **Scraper breakage**: Alert immediately; provide manual fallback

---

## Reference Documents

| Document | Location | Purpose |
|----------|----------|---------|
| **PO Summary** | `docs/analysis/FACTBASE_EXPANSION_PO_SUMMARY.md` | Business context, user stories |
| **Research Findings** | `docs/research/AUTHORITATIVE_DATA_SOURCES_RESEARCH_FINDINGS.md` | Detailed API documentation |
| **Research Prompt** | `docs/research/AUTHORITATIVE_DATA_SOURCES_RESEARCH_PROMPT.md` | Original research scope |

---

## Architectural Decisions (RESOLVED)

*Reviewed by: Winston (Architect) - November 2024*

### Question 1: Entity Model - Person as New Entity or Extend Entity?

**Decision**: **Create NEW separate `Person` table**

**Rationale**:
- Existing `Entity` table is for **transient, extracted entities** from news content (note `source`, `confidenceScore`, `verified` fields)
- `Person` represents **authoritative master data** - fundamentally different purpose
- Follows same pattern as `GovernmentOrganization` - master data separate from extracted entities
- Link via FK: `Entity.personId` → `Person.id` (similar to existing `Entity.governmentOrganization`)

**Implementation**:
```java
// Person.java - NEW authoritative master data table
@Entity
@Table(name = "persons")
public class Person {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "bioguide_id", unique = true, length = 20)
    private String bioguideId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "external_ids", columnDefinition = "jsonb")
    private JsonNode externalIds;  // {fec: [...], govtrack: 123, ...}

    // Follow existing GovernmentOrganization patterns (Lombok, validation, etc.)
}

// Entity.java - ADD reference for linking extracted to master
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "person_id")
private Person person;
```

---

### Question 2: Temporal Data - Separate Table or Versioned Records?

**Decision**: **Separate `PositionHolding` table (as proposed)**

**Rationale**:
- Standard temporal join pattern for many-to-many with time dimension
- Enables efficient queries: "Who held this position on date X?"
- Supports overlapping positions (member on multiple committees)
- Cleaner than versioning Person entity itself

**Implementation**:
```sql
CREATE INDEX idx_position_holding_dates
ON position_holding (position_id, start_date, end_date);

-- Query pattern: Who was in office on date X?
SELECT p.* FROM persons p
JOIN position_holding ph ON p.id = ph.person_id
WHERE ph.start_date <= :date
  AND (ph.end_date IS NULL OR ph.end_date >= :date);
```

---

### Question 3: Service Boundary - Microservice or Existing Backend?

**Decision**: **Part of existing Java backend (monolith-first)**

**Rationale**:
- Ingestion is scheduled background task, not user-facing service
- No independent scaling requirements - daily sync doesn't need horizontal scale
- Shared database with existing entities (GovernmentOrganization)
- Avoid distributed system complexity for batch process

**Implementation**:
```
backend/
├── src/main/java/org/newsanalyzer/
│   ├── ingestion/           # NEW package
│   │   ├── congress/        # Congress.gov client, sync service
│   │   ├── federalregister/ # Federal Register client
│   │   └── scheduler/       # Scheduled sync jobs
│   ├── model/               # Extended with Person, Position, etc.
│   └── controller/          # New /api/members, /api/committees
```

**Future**: Extract to separate service only if ingestion becomes complex (many sources, high volume).

---

### Question 4: Caching Strategy - Redis or In-Memory?

**Decision**: **In-memory caching (Caffeine) for Phase 1**

**Rationale**:
- Primary need is rate limit protection for Congress.gov API
- 5,000 requests/hour is generous - daily sync won't stress it
- Member data changes slowly - 1 hour TTL sufficient
- No distributed cache needed (single instance backend)
- Caffeine is standard Spring Boot choice, zero infrastructure

**Implementation**:
```java
@Configuration
@EnableCaching
public class CacheConfig {
    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager manager = new CaffeineCacheManager();
        manager.setCaffeine(Caffeine.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(1, TimeUnit.HOURS));
        return manager;
    }
}

@Service
public class CongressApiClient {
    @Cacheable("congress-members")
    public MemberResponse getMember(String bioguideId) { ... }
}
```

**Phase 3+**: Consider Redis if scrape results need cross-restart persistence.

---

### Question 5: Bulk Import - Bulk or Standard API?

**Decision**: **Hybrid approach**

**Initial Load**: Bulk import via `congress-legislators` YAML files
- 535 current + ~5,000 historical (1990s+) members
- Single HTTP fetch, parse in-memory, batch insert
- Completes in minutes, no rate limit concerns

**Ongoing Sync**: Incremental API sync from Congress.gov
- Daily check for new/updated members
- Only fetch changed records
- Respects rate limits naturally

**Implementation**:
```java
@Service
public class MemberSyncService {

    // Initial bulk load from GitHub
    public void bulkImportFromLegislatorsRepo() {
        String yaml = fetchFromGitHub("legislators-current.yaml");
        List<Person> persons = parseYaml(yaml);
        personRepository.saveAll(persons); // Batch insert
    }

    // Daily incremental sync
    @Scheduled(cron = "0 0 2 * * *")
    public void incrementalSync() {
        LocalDate lastSync = getLastSyncDate();
        List<Member> updated = congressApi.getMembersUpdatedSince(lastSync);
        // Upsert logic...
    }
}
```

---

## Additional Architectural Guidance

### Error Handling & Resilience

Implement circuit breaker for external API calls:
```java
@Retryable(
    value = {HttpServerErrorException.class},
    maxAttempts = 3,
    backoff = @Backoff(delay = 1000, multiplier = 2)
)
@CircuitBreaker(name = "congress-api", fallbackMethod = "fallback")
public MemberResponse getMember(String bioguideId) { ... }
```

### API Key Configuration Pattern

```yaml
# application.yml
external-apis:
  congress:
    base-url: https://api.congress.gov/v3
    api-key: ${CONGRESS_API_KEY}
    rate-limit: 5000
  federal-register:
    base-url: https://www.federalregister.gov/api/v1
    # No key needed
```

### Committee Design Decision

**Decision**: Separate `Committee` entity (not part of GovernmentOrganization)

**Rationale**:
- Committees have unique attributes (chamber, committee code, subcommittee hierarchy)
- Keeps models clean and focused
- Can consolidate later if patterns emerge

---

## Acceptance Criteria for Architecture Design

- [x] Entity model extensions defined
- [x] API integration patterns documented
- [x] Sync architecture designed
- [x] Error handling strategy defined
- [x] Schema.org mappings confirmed
- [x] Service boundaries clarified
- [x] Data freshness approach documented

---

## Decision Summary

| Question | Decision |
|----------|----------|
| Person entity | NEW separate `persons` table |
| Temporal data | Separate `position_holding` join table |
| Service boundary | Part of existing Java backend |
| Caching | In-memory (Caffeine) |
| Bulk import | Hybrid (bulk initial + incremental daily) |
| Committee | Separate entity |

---

*Prepared by: Sarah (Product Owner)*
*Date: November 2024*

*Reviewed by: Winston (Architect)*
*Review Date: November 2024*
*Status: **APPROVED** - Ready for Implementation*
