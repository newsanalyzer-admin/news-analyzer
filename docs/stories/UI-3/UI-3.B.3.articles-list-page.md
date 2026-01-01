# Story UI-3.B.3: Articles List Page

## Status

**Ready for Review**

## Story

**As a** user browsing analyzed articles,
**I want** a dedicated articles list page under Article Analyzer,
**So that** I can see all articles that have been analyzed and explore their details.

## Acceptance Criteria

| # | Criterion |
|---|-----------|
| AC1 | `/article-analyzer/articles` route exists and displays list of analyzed articles |
| AC2 | Article list shows key metadata (title, source, date analyzed, entity count) |
| AC3 | List supports sorting by date analyzed (newest first by default) |
| AC4 | List supports filtering by date range or search by title |
| AC5 | Clicking an article navigates to article detail page (placeholder if not exists) |
| AC6 | Empty state shown when no articles have been analyzed |

## Tasks / Subtasks

- [x] **Task 1: Create articles list page** (AC: 1, 2, 6)
  - [x] Create `/article-analyzer/articles/page.tsx`
  - [x] Create `useArticles` hook for fetching articles from `/api/articles`
  - [x] Display article cards/rows with title, source, date, entity count
  - [x] Implement empty state with helpful message

- [x] **Task 2: Add sorting and filtering** (AC: 3, 4)
  - [x] Add sort dropdown (Date Analyzed, Title)
  - [x] Add search input for title filtering
  - [x] Add date range filter (optional - implemented as client-side search)

- [x] **Task 3: Implement article navigation** (AC: 5)
  - [x] Make article rows/cards clickable
  - [x] Navigate to `/article-analyzer/articles/[id]` on click
  - [x] Create placeholder detail page if needed

- [x] **Task 4: Write tests** (AC: 1-6)
  - [x] Test articles page renders with article list
  - [x] Test sorting functionality
  - [x] Test search/filter functionality
  - [x] Test empty state
  - [x] Test navigation to article detail

## Dev Notes

### API Endpoint

The backend should have an `/api/articles` endpoint. Check if it exists:
- If exists: Use it directly
- If not: Create a simple hook that returns empty array or mock data

### Expected Article Fields

Based on `articles` table in architecture:
- `id` - Article ID
- `title` - Article headline
- `source_url` - Original URL
- `source_name` - News source name (may need join)
- `published_date` - When article was published
- `analyzed_at` - When analysis completed
- Entity count - Derived from `entity_mentions` or `entities` join

### Key Files

| File | Action | Description |
|------|--------|-------------|
| `frontend/src/app/article-analyzer/articles/page.tsx` | CREATE | Articles list page |
| `frontend/src/hooks/useArticles.ts` | CREATE | SWR hook for articles |
| `frontend/src/app/article-analyzer/articles/[id]/page.tsx` | CREATE | Article detail placeholder |

### UI Reference

Follow patterns from:
- Extracted Entities page (`/article-analyzer/entities/page.tsx`)
- Knowledge Base pages for consistent styling

## Testing

### Test File Locations
- `frontend/src/app/article-analyzer/articles/__tests__/page.test.tsx`

### Testing Standards
- Minimum 80% coverage for new components
- Test loading, error, empty states
- Test user interactions (sort, filter, click)

## Change Log

| Date | Version | Description | Author |
|------|---------|-------------|--------|
| 2025-12-31 | 1.0 | Initial story creation | James (Dev) |

---

## Dev Agent Record

### Agent Model Used

Claude Opus 4.5 (claude-opus-4-5-20251101)

### Debug Log References

- Fixed test failure for multiple "Back to Articles" buttons in error state (used `getAllByRole` instead of `getByRole`)
- No articles API exists yet; hook gracefully handles 404 with empty array

### Completion Notes List

1. **Task 1: Created articles list page**
   - Created `useArticles.ts` hook with SWR for fetching articles
   - Implements `useArticles()`, `useArticle(id)`, `useArticleSearch(query)`
   - Gracefully handles 404 (API not implemented) by returning empty array
   - Created `page.tsx` with full article list functionality
   - Table displays: title, source, published date, analyzed date, entity count, status
   - Empty state encourages users to analyze articles

2. **Task 2: Added sorting and filtering**
   - Sort dropdown: Date Analyzed, Published Date, Title
   - Sort direction toggle button (asc/desc)
   - Search input filters by title and source name (client-side)
   - Default sort: newest analyzed first

3. **Task 3: Implemented article navigation**
   - Article rows are clickable, navigate to `/article-analyzer/articles/[id]`
   - Created placeholder detail page with "Coming Soon" content
   - Detail page shows Phase 4 planned features list
   - External link to original article opens in new tab

4. **Task 4: Wrote 41 tests (28 for list, 13 for detail)**
   - Tests cover: header, filters, table, loading/error/empty states, navigation, accessibility, sorting

### File List

| File | Action | Description |
|------|--------|-------------|
| `frontend/src/hooks/useArticles.ts` | CREATED | SWR hooks for fetching articles |
| `frontend/src/app/article-analyzer/articles/page.tsx` | CREATED | Articles list page with sorting and filtering |
| `frontend/src/app/article-analyzer/articles/[id]/page.tsx` | CREATED | Article detail placeholder page |
| `frontend/src/app/article-analyzer/articles/__tests__/page.test.tsx` | CREATED | 28 tests for articles list page |
| `frontend/src/app/article-analyzer/articles/[id]/__tests__/page.test.tsx` | CREATED | 13 tests for article detail page |

---

## QA Results

_To be filled by QA agent_
