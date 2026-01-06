# Epic KB-1: President of the United States Data

## Epic Overview

| Field | Value |
|-------|-------|
| **Epic ID** | KB-1 |
| **Epic Name** | President of the United States Data |
| **Epic Type** | Data Integration + KB Enhancement |
| **Priority** | HIGH |
| **Status** | APPROVED |
| **Created** | 2026-01-06 |
| **Owner** | Sarah (PO) |
| **Depends On** | UI-6 Complete (Executive Branch sub-pages exist) |
| **Triggered By** | Need for comprehensive POTUS data in Knowledge Base |

## Executive Summary

Implement comprehensive data management for Presidents of the United States, including a new **Presidency** entity that separates the office/term from the individual person. This enables proper handling of non-consecutive terms (Cleveland 22nd/24th, Trump 45th/47th) and provides rich relational data for the reasoning-service.

### Current State

- KB has placeholder page at `/knowledge-base/government/executive/president`
- Admin has placeholder at `/admin/knowledge-base/government/executive/president`
- `Person` entity exists but focused on Congressional members
- No Presidency or Executive Order entities
- No presidential data sync capability

### Target State

- **New `Presidency` entity** linking Person to their presidential term(s)
- **New `ExecutiveOrder` entity** linked to each Presidency
- **Extended `PositionHolding`** with `presidency_id` for VP, CoS, Cabinet links
- **Admin sync button** fetches all 47 presidencies from static seed + API enrichment
- **KB President page** shows:
  - Current president (47th) with presidency number
  - Sortable table of all 47 presidencies
  - Per-presidency: VP(s), Chiefs of Staff, Cabinet Secretaries, Executive Orders
- **Reasoning-service ready** data model with proper semantic relationships

## Business Value

### Why This Epic Matters

1. **Authoritative Reference** - Comprehensive POTUS data as foundational KB content
2. **Semantic Data Model** - Person/Presidency separation enables reasoning about non-consecutive terms
3. **Educational UX** - Users understand presidential history and administration structure
4. **Rich Relationships** - Cabinet, VP, CoS, EOs linked per-presidency for analysis
5. **Foundation for Executive Branch** - Pattern reusable for VP, Cabinet pages

### Success Metrics

| Metric | Target |
|--------|--------|
| All 47 presidencies imported | Yes |
| Person/Presidency separation working | Yes |
| Non-consecutive terms handled correctly | Yes (Cleveland, Trump) |
| Admin sync button functional | Yes |
| KB page displays current + historical | Yes |
| Executive Orders linked per-presidency | Yes |
| VP changes mid-term tracked correctly | Yes (Ford/Agnew under Nixon) |

## Data Model

### Entity Relationship Diagram

```
┌─────────────────┐       ┌──────────────────┐       ┌───────────────────┐
│     Person      │       │    Presidency    │       │  ExecutiveOrder   │
├─────────────────┤       ├──────────────────┤       ├───────────────────┤
│ id (UUID)       │◄──────│ person_id (FK)   │       │ id (UUID)         │
│ first_name      │       │ id (UUID)        │◄──────│ presidency_id(FK) │
│ last_name       │       │ number (1-47)    │       │ eo_number         │
│ birth_date      │       │ start_date       │       │ title             │
│ death_date *NEW │       │ end_date         │       │ signing_date      │
│ birth_place*NEW │       │ party            │       │ summary           │
│ ...             │       │ election_year    │       │ federal_register  │
└─────────────────┘       │ end_reason       │       │ status            │
        ▲                 │ predecessor_id   │       └───────────────────┘
        │                 │ successor_id     │
        │                 └──────────────────┘
        │                          ▲
        │                          │ presidency_id (NEW FK)
        │                          │
┌───────┴─────────────────────────┴──────────────────┐
│                  PositionHolding                    │
├────────────────────────────────────────────────────┤
│ id (UUID)                                          │
│ person_id (FK) ──────► Person                      │
│ position_id (FK) ────► GovernmentPosition          │
│ presidency_id (FK) ──► Presidency (NEW, nullable)  │
│ start_date                                         │
│ end_date                                           │
│ data_source                                        │
└────────────────────────────────────────────────────┘
        │
        │ Links to GovernmentPosition for:
        │ - Vice President of the United States
        │ - White House Chief of Staff
        │ - Cabinet Secretary positions
        ▼
┌────────────────────────────────────────────────────┐
│              GovernmentPosition                     │
├────────────────────────────────────────────────────┤
│ id (UUID)                                          │
│ title: "Vice President of the United States"       │
│ branch: EXECUTIVE                                  │
│ position_type: ELECTED / PAS                       │
│ organization_id ──► GovernmentOrganization         │
└────────────────────────────────────────────────────┘
```

### New Entities

#### 1. Presidency

| Field | Type | Description |
|-------|------|-------------|
| `id` | UUID | Primary key |
| `person_id` | UUID (FK) | Link to Person (the president) |
| `number` | Integer | Presidency number (1-47), unique |
| `start_date` | LocalDate | Inauguration date |
| `end_date` | LocalDate | End of term (null if current) |
| `party` | String | Political party at time of presidency |
| `election_year` | Integer | Year elected (null if succeeded to office) |
| `end_reason` | Enum | TERM_END, DEATH, RESIGNATION, SUCCESSION |
| `predecessor_id` | UUID (FK) | Previous Presidency (null for #1) |
| `successor_id` | UUID (FK) | Next Presidency (null if current) |

> **Architect Note:** VP is NOT stored as FK on Presidency. VPs are tracked via `PositionHolding` with `presidency_id` to handle mid-term changes (e.g., Ford replacing Agnew).

#### 2. ExecutiveOrder

| Field | Type | Description |
|-------|------|-------------|
| `id` | UUID | Primary key |
| `presidency_id` | UUID (FK) | Link to Presidency |
| `eo_number` | Integer | Executive Order number |
| `title` | String | Title of the order |
| `signing_date` | LocalDate | Date signed |
| `summary` | Text | Abstract/summary (not full text) |
| `federal_register_citation` | String | FR citation |
| `federal_register_url` | String | Link to Federal Register |
| `status` | Enum | ACTIVE, REVOKED, SUPERSEDED |
| `revoked_by_eo` | Integer | EO number that revoked this (if applicable) |

### Model Extensions

#### Person (extend existing)

| New Field | Type | Description |
|-----------|------|-------------|
| `death_date` | LocalDate | Date of death (null if living) |
| `birth_place` | String | City, State of birth |

#### PositionHolding (extend existing)

| New Field | Type | Description |
|-----------|------|-------------|
| `presidency_id` | UUID (FK) | Links appointment to specific presidency (nullable) |

> **Architect Note:** This enables querying "all VPs during the 37th presidency" or "all Cabinet secretaries under Biden".

#### DataSource (extend enum)

| New Value | Description |
|-----------|-------------|
| `USA_GOV` | Data from USA.gov |
| `FEDERAL_REGISTER` | Data from Federal Register API |
| `WHITE_HOUSE_HISTORICAL` | White House historical data |

## API Integration

### Data Source: Static Seed + Government APIs

**Primary Sources:**
- **Presidents List**: Static seed JSON (reliable for all 47 historical presidencies)
- **Enrichment**: Wikipedia structured data or White House historical archives
- **Executive Orders**: Federal Register API (`https://www.federalregister.gov/api/v1/`)
- **Biographical Data**: Congress.gov (for those who served in Congress)

**Sync Strategy:**
1. Initial bulk import from static seed data (all 47 presidencies)
2. API enrichment for images, additional biographical data
3. Incremental sync for current presidency (new EOs, appointments)
4. Manual trigger via Admin sync button

### Federal Register API for Executive Orders

```
GET https://www.federalregister.gov/api/v1/documents.json
?conditions[type][]=PRESDOCU
&conditions[presidential_document_type][]=executive_order
&conditions[president][]={president_name}
&fields[]=executive_order_number,title,signing_date,abstract,html_url
```

## Scope

### In Scope

1. **Backend Data Model**
   - New `Presidency` entity with full schema
   - New `ExecutiveOrder` entity
   - Extend `Person` with death_date, birth_place
   - Extend `PositionHolding` with presidency_id
   - Extend `DataSource` enum with new values
   - Flyway migrations

2. **Backend Services**
   - `PresidencyService` - CRUD + sync logic
   - `ExecutiveOrderService` - CRUD + Federal Register sync
   - `PresidentialSyncService` - Orchestrates full sync

3. **Backend API Endpoints**
   - `GET /api/presidencies` - List all (paginated)
   - `GET /api/presidencies/current` - Current presidency
   - `GET /api/presidencies/{id}` - Single presidency with relations
   - `GET /api/presidencies/number/{number}` - Presidency by number (1-47)
   - `GET /api/presidencies/{id}/executive-orders` - EOs for presidency
   - `GET /api/presidencies/{id}/administration` - VP, CoS, Cabinet for presidency
   - `POST /api/admin/sync/presidencies` - Trigger sync

4. **Admin UI**
   - Sync button on `/admin/knowledge-base/government/executive/president`
   - Sync status indicator
   - Basic data table (view, not full edit initially)

5. **KB Frontend**
   - Current president highlight card with presidency number
   - Sortable table of all 47 presidencies
   - Expandable rows showing: VP(s), Chiefs of Staff, Cabinet, EO count
   - Link to detailed presidency page (future epic)

### Out of Scope

- Full CRUD admin for presidencies (this epic: sync + view only)
- Detailed individual presidency pages (future KB-2 epic)
- VP data page implementation (separate epic)
- Full executive order text storage (metadata + summary only)
- Historical cabinet secretary appointment dates (link to existing data)

## Stories

### Story Summary

| ID | Story | Priority | Estimate | Status |
|----|-------|----------|----------|--------|
| KB-1.0 | Extend PositionHolding and DataSource for Presidency Support | P0 (Blocker) | 1 pt | Draft |
| KB-1.1 | Create Presidency and ExecutiveOrder Entities | P0 | 4 pts | Draft |
| KB-1.2 | Implement Presidential Data Sync Service | P0 | 5 pts | Draft |
| KB-1.3 | Create Presidency API Endpoints | P0 | 3 pts | Draft |
| KB-1.4 | Build Admin Sync UI for President Page | P1 | 2 pts | Draft |
| KB-1.5 | Implement KB President Page with Historical Table | P1 | 6 pts | Draft |
| KB-1.6 | Integrate Executive Orders Sync | P2 | 3 pts | Draft |

**Epic Total:** 24 story points

### Dependency Graph

```
KB-1.0 (PositionHolding + DataSource Extension) ◄── BLOCKER
    │
    ▼
KB-1.1 (Presidency + EO Entities) ─────────────────┐
    │                                               │
    ▼                                               ▼
KB-1.2 (Sync Service) ──► KB-1.3 (API) ──► KB-1.5 (KB Frontend)
    │                         │
    ▼                         ▼
KB-1.6 (EO Sync)         KB-1.4 (Admin Sync UI)
```

---

## Story Details

### KB-1.0: Extend PositionHolding and DataSource for Presidency Support

**Status:** Draft | **Estimate:** 1 pt | **Priority:** P0 (Blocker)

**As a** developer,
**I want** PositionHolding to support presidency linking and new data sources,
**So that** VP, CoS, and Cabinet appointments can be tracked per-presidency.

#### Acceptance Criteria

| # | Criterion |
|---|-----------|
| AC1 | `PositionHolding` entity has new nullable `presidency_id` UUID field |
| AC2 | Foreign key constraint to `presidencies` table (deferred until KB-1.1 creates table) |
| AC3 | `DataSource` enum extended with `USA_GOV`, `FEDERAL_REGISTER`, `WHITE_HOUSE_HISTORICAL` |
| AC4 | Index added on `position_holdings.presidency_id` |
| AC5 | Flyway migration is backward compatible (nullable field) |
| AC6 | Existing tests pass |

#### Technical Notes

**Files to Modify:**
- `backend/src/main/java/org/newsanalyzer/model/PositionHolding.java` - Add `presidencyId` field
- `backend/src/main/java/org/newsanalyzer/model/DataSource.java` - Add enum values
- `backend/src/main/resources/db/migration/V30__extend_position_holding_for_presidency.sql`

**Migration SQL:**
```sql
-- Add presidency_id column (FK added later after presidencies table exists)
ALTER TABLE position_holdings ADD COLUMN presidency_id UUID;
CREATE INDEX idx_position_holdings_presidency ON position_holdings(presidency_id);

-- Extend data_source enum (PostgreSQL)
ALTER TYPE data_source ADD VALUE IF NOT EXISTS 'usa_gov';
ALTER TYPE data_source ADD VALUE IF NOT EXISTS 'federal_register';
ALTER TYPE data_source ADD VALUE IF NOT EXISTS 'white_house_historical';
```

---

### KB-1.1: Create Presidency and ExecutiveOrder Entities

**Status:** Draft | **Estimate:** 4 pts | **Priority:** P0

**As a** developer,
**I want** Presidency and ExecutiveOrder JPA entities with proper relationships,
**So that** presidential data can be persisted and queried.

#### Acceptance Criteria

| # | Criterion |
|---|-----------|
| AC1 | `Presidency` entity created with all fields from data model |
| AC2 | `ExecutiveOrder` entity created with all fields from data model |
| AC3 | `Person` entity extended with `death_date`, `birth_place` fields |
| AC4 | Flyway migration creates tables with proper constraints |
| AC5 | Foreign key from `position_holdings.presidency_id` to `presidencies.id` added |
| AC6 | Repository interfaces created with standard query methods |
| AC7 | `PresidencyRepository.findByNumber(int)` method for lookup by presidency number |
| AC8 | Non-consecutive presidency handling verified (same person, multiple presidencies) |
| AC9 | Unit tests pass for entity validation |

#### Technical Notes

**Files to Create:**
- `backend/src/main/java/org/newsanalyzer/model/Presidency.java`
- `backend/src/main/java/org/newsanalyzer/model/ExecutiveOrder.java`
- `backend/src/main/java/org/newsanalyzer/model/PresidencyEndReason.java` (enum)
- `backend/src/main/java/org/newsanalyzer/model/ExecutiveOrderStatus.java` (enum)
- `backend/src/main/java/org/newsanalyzer/repository/PresidencyRepository.java`
- `backend/src/main/java/org/newsanalyzer/repository/ExecutiveOrderRepository.java`
- `backend/src/main/resources/db/migration/V31__create_presidency_tables.sql`

**Files to Modify:**
- `backend/src/main/java/org/newsanalyzer/model/Person.java` (add death_date, birth_place)
- `backend/src/main/resources/db/migration/V32__extend_person_for_potus.sql`
- `backend/src/main/resources/db/migration/V33__add_presidency_fk_to_position_holdings.sql`

**Key Repository Methods:**
```java
public interface PresidencyRepository extends JpaRepository<Presidency, UUID> {
    Optional<Presidency> findByNumber(Integer number);
    Optional<Presidency> findFirstByEndDateIsNullOrderByNumberDesc(); // current
    List<Presidency> findByPersonId(UUID personId); // for non-consecutive terms
    List<Presidency> findAllByOrderByNumberDesc();
}
```

---

### KB-1.2: Implement Presidential Data Sync Service

**Status:** Draft | **Estimate:** 5 pts | **Priority:** P0

**As a** system administrator,
**I want** a service that syncs presidential data from seed file and APIs,
**So that** the KB has authoritative POTUS data.

#### Acceptance Criteria

| # | Criterion |
|---|-----------|
| AC1 | `PresidentialSyncService` imports all 47 presidencies from seed JSON |
| AC2 | Person records created/updated for each president |
| AC3 | Presidency records created with correct number, dates, party |
| AC4 | Non-consecutive terms create separate Presidency records linked to same Person |
| AC5 | Vice President `PositionHolding` records created with `presidency_id` link |
| AC6 | Sync is idempotent (re-running doesn't duplicate data) |
| AC7 | Sync logs progress and errors |
| AC8 | Integration test verifies sync produces expected 47 presidencies |
| AC9 | Cleveland (22 & 24) and Trump (45 & 47) verified as same Person, different Presidencies |

#### Technical Notes

**Data Source Strategy:**
- Start with static seed data for initial 47 presidencies (reliable, fast)
- Seed includes: president name, VP name(s), party, dates, end_reason
- API enrichment for images, additional bio data (future enhancement)
- Federal Register API for Executive Orders (KB-1.6)

**Files to Create:**
- `backend/src/main/java/org/newsanalyzer/service/PresidentialSyncService.java`
- `backend/src/main/java/org/newsanalyzer/service/dto/PresidencySeedData.java`
- `backend/src/main/resources/data/presidencies-seed.json` (static seed data)
- `backend/src/test/java/org/newsanalyzer/service/PresidentialSyncServiceTest.java`

**Seed Data Structure:**
```json
{
  "presidencies": [
    {
      "number": 1,
      "president": {
        "firstName": "George",
        "lastName": "Washington",
        "birthDate": "1732-02-22",
        "deathDate": "1799-12-14",
        "birthPlace": "Westmoreland County, Virginia"
      },
      "party": "Independent",
      "startDate": "1789-04-30",
      "endDate": "1797-03-04",
      "electionYear": 1788,
      "endReason": "TERM_END",
      "vicePresidents": [
        {
          "firstName": "John",
          "lastName": "Adams",
          "startDate": "1789-04-21",
          "endDate": "1797-03-04"
        }
      ]
    }
  ]
}
```

---

### KB-1.3: Create Presidency API Endpoints

**Status:** Draft | **Estimate:** 3 pts | **Priority:** P0

**As a** frontend developer,
**I want** REST endpoints for presidency data,
**So that** the KB can display presidential information.

#### Acceptance Criteria

| # | Criterion |
|---|-----------|
| AC1 | `GET /api/presidencies` returns paginated list |
| AC2 | `GET /api/presidencies/current` returns current (47th) presidency |
| AC3 | `GET /api/presidencies/{id}` returns single presidency with person details |
| AC4 | `GET /api/presidencies/number/{number}` returns presidency by number (1-47) |
| AC5 | `GET /api/presidencies/{id}/executive-orders` returns EOs (paginated) |
| AC6 | `GET /api/presidencies/{id}/administration` returns VP, CoS holdings for presidency |
| AC7 | Response DTOs include computed fields (full name, term length, term label) |
| AC8 | OpenAPI documentation generated |
| AC9 | Controller tests verify all endpoints |

#### Technical Notes

**Files to Create:**
- `backend/src/main/java/org/newsanalyzer/controller/PresidencyController.java`
- `backend/src/main/java/org/newsanalyzer/dto/PresidencyDTO.java`
- `backend/src/main/java/org/newsanalyzer/dto/PresidencyListDTO.java`
- `backend/src/main/java/org/newsanalyzer/dto/PresidencyAdministrationDTO.java`
- `backend/src/main/java/org/newsanalyzer/service/PresidencyService.java`
- `backend/src/test/java/org/newsanalyzer/controller/PresidencyControllerTest.java`

**DTO Structure:**
```java
public record PresidencyDTO(
    UUID id,
    Integer number,
    String presidentFullName,
    String party,
    LocalDate startDate,
    LocalDate endDate,
    String termLabel,      // "1789-1797"
    Long termDays,         // computed
    String endReason,
    String imageUrl,
    List<VicePresidentDTO> vicePresidents,
    Integer executiveOrderCount
) {}
```

---

### KB-1.4: Build Admin Sync UI for President Page

**Status:** Draft | **Estimate:** 2 pts | **Priority:** P1

**As an** administrator,
**I want** a sync button on the Admin President page,
**So that** I can trigger presidential data import.

#### Acceptance Criteria

| # | Criterion |
|---|-----------|
| AC1 | Admin President page (`/admin/knowledge-base/government/executive/president`) shows sync UI |
| AC2 | "Sync Presidential Data" button triggers API call to `POST /api/admin/sync/presidencies` |
| AC3 | Loading state shown during sync |
| AC4 | Success/error toast notifications displayed |
| AC5 | Last sync timestamp displayed |
| AC6 | Basic data table shows imported presidencies (number, name, term, party) |
| AC7 | Tests cover sync button interaction |

#### Technical Notes

**Files to Modify:**
- `frontend/src/app/admin/knowledge-base/government/executive/president/page.tsx`

**New Components:**
- `frontend/src/components/admin/SyncButton.tsx` (reusable for future sync pages)
- `frontend/src/hooks/usePresidencySync.ts`
- `frontend/src/hooks/useAdminPresidencies.ts`

---

### KB-1.5: Implement KB President Page with Historical Table

**Status:** Draft | **Estimate:** 6 pts | **Priority:** P1

**As a** Knowledge Base user,
**I want** the President page to show current president and historical list,
**So that** I can explore presidential data.

#### Acceptance Criteria

| # | Criterion |
|---|-----------|
| AC1 | Current president displayed prominently with presidency number (47th) |
| AC2 | Current president card shows: name, party, term start, portrait |
| AC3 | Historical table lists all 47 presidencies |
| AC4 | Table columns: #, Name, Party, Term, VP(s) |
| AC5 | Table is sortable by number, name, party, term dates |
| AC6 | Non-consecutive terms shown correctly (Cleveland 22nd & 24th as separate rows) |
| AC7 | Expandable rows show: VP list, Chiefs of Staff, Cabinet summary, EO count |
| AC8 | VP data fetched via `/api/presidencies/{id}/administration` endpoint |
| AC9 | Empty state handled gracefully if no data synced |
| AC10 | Mobile responsive design |
| AC11 | Tests cover rendering and interactions |

#### Technical Notes

**Files to Modify:**
- `frontend/src/app/knowledge-base/government/executive/president/page.tsx`

**New Components:**
- `frontend/src/components/knowledge-base/PresidentCard.tsx`
- `frontend/src/components/knowledge-base/PresidencyTable.tsx`
- `frontend/src/components/knowledge-base/PresidencyExpandedRow.tsx`
- `frontend/src/hooks/usePresidencies.ts`
- `frontend/src/hooks/usePresidencyAdministration.ts`

**Data Fetching:**
- Use TanStack Query for data fetching
- Lazy load expanded row data (VP, CoS, Cabinet) on expand

---

### KB-1.6: Integrate Executive Orders Sync

**Status:** Draft | **Estimate:** 3 pts | **Priority:** P2

**As a** system administrator,
**I want** Executive Orders synced from Federal Register,
**So that** each presidency shows its EOs.

#### Acceptance Criteria

| # | Criterion |
|---|-----------|
| AC1 | Federal Register API integration fetches EOs by president name |
| AC2 | EO metadata stored: number, title, signing date, summary |
| AC3 | Federal Register URL linked for full text |
| AC4 | EOs correctly linked to Presidency entity |
| AC5 | Sync handles API rate limits gracefully (exponential backoff) |
| AC6 | EO count displayed per presidency in KB table |
| AC7 | Integration test verifies EO sync for at least one presidency |

#### Technical Notes

**API Endpoint:**
```
GET https://www.federalregister.gov/api/v1/documents.json
?conditions[type][]=PRESDOCU
&conditions[presidential_document_type][]=executive_order
&conditions[president][]={president_name}
&fields[]=executive_order_number,title,signing_date,abstract,html_url
&per_page=100
```

**Files to Create:**
- `backend/src/main/java/org/newsanalyzer/service/FederalRegisterClient.java`
- `backend/src/main/java/org/newsanalyzer/service/ExecutiveOrderSyncService.java`
- `backend/src/main/java/org/newsanalyzer/service/dto/FederalRegisterResponse.java`
- `backend/src/test/java/org/newsanalyzer/service/ExecutiveOrderSyncServiceTest.java`

**Rate Limit Handling:**
- Federal Register API: 1000 requests/hour
- Implement exponential backoff on 429 responses
- Batch by presidency to limit concurrent requests

---

## Risks & Mitigations

| Risk | Impact | Probability | Mitigation |
|------|--------|-------------|------------|
| Static seed data inaccuracies | MEDIUM | LOW | Cross-reference with Wikipedia, White House archives |
| Federal Register rate limits | MEDIUM | MEDIUM | Implement backoff, batch requests, cache responses |
| Historical data inconsistencies | LOW | MEDIUM | Manual curation for edge cases |
| Non-consecutive term confusion | LOW | LOW | Clear UI labeling, tooltips |
| VP mid-term changes complexity | MEDIUM | LOW | PositionHolding pattern handles temporal changes |

## Definition of Done

- [ ] KB-1.0: PositionHolding and DataSource extended
- [ ] KB-1.1: Entities and migrations deployed
- [ ] KB-1.2: All 47 presidencies synced successfully
- [ ] KB-1.3: API endpoints tested and documented
- [ ] KB-1.4: Admin sync UI functional
- [ ] KB-1.5: KB President page displays data correctly
- [ ] KB-1.6: Executive Orders linked to presidencies
- [ ] All tests pass (backend + frontend)
- [ ] Non-consecutive terms verified (Cleveland, Trump)
- [ ] VP mid-term changes verified (Ford/Agnew under Nixon)
- [ ] ROADMAP.md updated with KB-1 entry

## Related Documentation

- [UI-6 Epic (Executive Branch Pages)](../UI-6/UI-6.epic-executive-branch-hierarchy.md)
- [Person Entity](../../backend/src/main/java/org/newsanalyzer/model/Person.java)
- [PositionHolding Entity](../../backend/src/main/java/org/newsanalyzer/model/PositionHolding.java)
- [GovernmentPosition Entity](../../backend/src/main/java/org/newsanalyzer/model/GovernmentPosition.java)
- [Federal Register API Docs](https://www.federalregister.gov/developers/documentation/api/v1)

## Change Log

| Date | Version | Description | Author |
|------|---------|-------------|--------|
| 2026-01-06 | 1.0 | Initial epic creation | Sarah (PO) |
| 2026-01-06 | 1.1 | Architect review: Added KB-1.0 story, updated data model (VP via PositionHolding), added DataSource enum extension, updated estimates (21→24 pts) | Winston (Architect) |

## Approval

| Role | Name | Date | Status |
|------|------|------|--------|
| Product Owner | Sarah (PO) | 2026-01-06 | DRAFTED |
| Architect | Winston | 2026-01-06 | **APPROVED WITH MODIFICATIONS** |
| Developer | - | - | Pending |

### Architect Review Notes

**Review Date:** 2026-01-06

**Verdict:** APPROVED WITH MODIFICATIONS

**Key Modifications Applied:**

1. **VP Handling via PositionHolding** - Removed `vice_president_id` FK from Presidency. VPs now tracked via `PositionHolding` with `presidency_id` to handle mid-term changes (Ford replacing Agnew)

2. **Added KB-1.0 Story (1 pt)** - Preparatory migration to extend `PositionHolding` with `presidency_id` and add `DataSource` enum values

3. **Updated Estimates** - KB-1.1: 3→4 pts (added FK migration), KB-1.5: 5→6 pts (VP/CoS queries via PositionHolding joins)

4. **Data Source Strategy** - Clarified static seed as primary source for reliability, API for enrichment

5. **Additional API Endpoint** - Added `GET /api/presidencies/{id}/administration` for VP/CoS/Cabinet queries

**Architecture Alignment:** ✅ Aligned with existing `PositionHolding` temporal pattern and `DataSource` enum convention

---

*End of Epic Document*
