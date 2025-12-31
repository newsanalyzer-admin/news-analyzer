# Story UI-3.B.2: Move Extracted Entities to Article Analyzer

## Status

**Ready for Review**

## Story

**As a** user viewing extracted entities,
**I want** them under Article Analyzer (not Knowledge Base),
**So that** I understand these are analysis results, not authoritative data.

## Acceptance Criteria

| # | Criterion |
|---|-----------|
| AC1 | `/article-analyzer/entities` shows EntityBrowser with `entities` table data |
| AC2 | Old `/knowledge-base/[entityType]` routes for extracted entities redirect to new location |
| AC3 | EntityBrowser configuration preserved from UI-2 |
| AC4 | Clear labeling indicates these are "Extracted Entities" |

## Tasks / Subtasks

- [x] **Task 1: Create entities page under Article Analyzer** (AC: 1, 3, 4)
  - [x] Create `/article-analyzer/entities/page.tsx`
  - [x] Configure EntityBrowser to use `entities` table (dataLayer: 'analysis')
  - [x] Add "Extracted Entities" header with explanatory text
  - [x] Preserve all EntityBrowser features (filtering, sorting, pagination)

- [x] **Task 2: Configure entity type redirects** (AC: 2)
  - [x] Add redirects for extracted entity types (person, organization, event, location)
  - [x] Redirect `/knowledge-base/person` → `/article-analyzer/entities?type=person`
  - [x] Redirect `/knowledge-base/organization` → `/article-analyzer/entities?type=organization`
  - [x] Redirect `/knowledge-base/event` → `/article-analyzer/entities?type=event`
  - [x] Redirect `/knowledge-base/location` → `/article-analyzer/entities?type=location`

- [x] **Task 3: Write tests** (AC: 1-4)
  - [x] Test entities page renders with EntityBrowser
  - [x] Test redirect configuration for extracted entity types
  - [x] Test labeling indicates "Extracted Entities"

## Dev Notes

### Data Layer Distinction

Per architecture v2.5:
- **Knowledge Base** (dataLayer: 'kb') → `persons`, `committees`, `government_organizations` tables
- **Article Analyzer** (dataLayer: 'analysis') → `entities` table (extracted from articles)

### Entity Types

**Extracted Entity Types** (move to Article Analyzer):
- `person` - People mentioned in articles
- `organization` - Organizations mentioned in articles
- `event` - Events mentioned in articles
- `location` - Locations mentioned in articles

**KB Entity Types** (stay in Knowledge Base):
- `people` - Authoritative person records (judges, members, appointees)
- `organizations` - Government organizations
- `committees` - Congressional committees

### Redirect Strategy

```javascript
// next.config.js - Add these redirects
{
  source: '/knowledge-base/:type(person|organization|event|location)',
  destination: '/article-analyzer/entities?type=:type',
  permanent: false, // 307 during transition
}
```

### Key Files

| File | Action | Description |
|------|--------|-------------|
| `frontend/src/app/article-analyzer/entities/page.tsx` | CREATE | Entities page with EntityBrowser |
| `frontend/next.config.js` | MODIFY | Add extracted entity redirects |
| `frontend/src/lib/config/entityTypes.ts` | VERIFY | Ensure analysis entity configs exist |

## Testing

### Test File Locations
- `frontend/src/app/article-analyzer/entities/__tests__/page.test.tsx`
- `frontend/src/lib/config/__tests__/redirects.test.ts` (update existing)

### Testing Standards
- Verify EntityBrowser renders with correct configuration
- Verify redirects work for all extracted entity types
- Verify labeling is clear about "Extracted" nature

## Change Log

| Date | Version | Description | Author |
|------|---------|-------------|--------|
| 2025-12-31 | 1.0 | Initial story creation | John (PM) |

---

## Dev Agent Record

### Agent Model Used

Claude Opus 4.5 (claude-opus-4-5-20251101)

### Debug Log References

- Fixed test failure for `/person/i` matching multiple elements by using more specific regex `/person: 1/i`

### Completion Notes List

1. **Task 1: Created entities page under Article Analyzer**
   - Created `useExtractedEntities.ts` hook with SWR for fetching entities
   - Implements `useExtractedEntities()`, `useExtractedEntitiesByType()`, `useExtractedEntitySearch()`
   - Created `page.tsx` with full entity browser functionality
   - Features: type filter dropdown, search input, stats badges showing counts by type
   - Table displays: name, type, schema.org type, confidence score bar, verified status
   - Clear labeling as "Extracted Entities" with note about confidence levels
   - Info banner links to Knowledge Base for authoritative data

2. **Task 2: Configured entity type redirects**
   - Added 4 redirects in `next.config.js` for extracted entity types
   - `/knowledge-base/person` → `/article-analyzer/entities?type=person`
   - `/knowledge-base/organization` → `/article-analyzer/entities?type=organization`
   - `/knowledge-base/event` → `/article-analyzer/entities?type=event`
   - `/knowledge-base/location` → `/article-analyzer/entities?type=location`
   - Using `permanent: false` (307) for transition period

3. **Task 3: Wrote 32 tests (26 for page, 6 for redirects)**
   - `page.test.tsx`: Tests for header, info banner, stats row, filters, entity table, loading/error/empty states, accessibility, data layer labeling
   - `redirects.test.ts`: Tests for all 4 entity type redirects and temporary status

### File List

| File | Action | Description |
|------|--------|-------------|
| `frontend/src/hooks/useExtractedEntities.ts` | CREATED | SWR hooks for fetching extracted entities |
| `frontend/src/app/article-analyzer/entities/page.tsx` | CREATED | Entities page with type filter, search, table |
| `frontend/src/app/article-analyzer/entities/__tests__/page.test.tsx` | CREATED | 26 tests for entities page |
| `frontend/next.config.js` | MODIFIED | Added 4 redirects for extracted entity types |
| `frontend/src/lib/config/__tests__/redirects.test.ts` | MODIFIED | Added 6 tests for entity type redirects |

---

## QA Results

_To be filled by QA agent_
