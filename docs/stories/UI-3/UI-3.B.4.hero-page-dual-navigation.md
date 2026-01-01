# Story UI-3.B.4: Hero Page Dual Navigation

## Status

**Ready for Review**

## Story

**As a** first-time visitor to NewsAnalyzer,
**I want** clear navigation options on the hero page,
**So that** I can easily access either the Knowledge Base or Article Analyzer.

## Acceptance Criteria

| # | Criterion |
|---|-----------|
| AC1 | Hero page shows prominent "Knowledge Base" and "Article Analyzer" navigation buttons |
| AC2 | Both buttons are visually balanced (neither appears more important than the other) |
| AC3 | Buttons link to correct routes (`/knowledge-base` and `/article-analyzer`) |
| AC4 | Legacy quick-links removed or updated to point to new routes |
| AC5 | Feature cards accurately describe current functionality |

## Tasks / Subtasks

- [x] **Task 1: Verify dual navigation buttons exist** (AC: 1, 2, 3)
  - [x] Confirm hero page has Knowledge Base button
  - [x] Confirm hero page has Article Analyzer button
  - [x] Verify buttons link to correct routes
  - [x] Verify visual balance between buttons

- [x] **Task 2: Clean up legacy quick-links** (AC: 4)
  - [x] Review existing quick-link buttons
  - [x] Remove or update outdated routes (/entities, /members, /committees)
  - [x] Ensure all links point to valid routes

- [x] **Task 3: Update feature cards if needed** (AC: 5)
  - [x] Review feature card descriptions
  - [x] Update any outdated descriptions (kept as-is, accurate)

- [x] **Task 4: Write/update tests** (AC: 1-5)
  - [x] Test hero page renders dual navigation
  - [x] Test navigation links are correct
  - [x] Test no broken links exist

## Dev Notes

### Current State (from UI-3.B.1)

The hero page already has dual navigation buttons added in UI-3.B.1:
- "Explore Knowledge Base →" (primary styling)
- "Article Analyzer →" (secondary styling)

### Legacy Links to Review

Current quick-links that may be outdated:
- `/entities` - Should redirect to `/article-analyzer/entities` or remove
- `/members` - Should redirect to `/knowledge-base/people?type=members` or remove
- `/committees` - Should redirect to `/knowledge-base/committees` or remove

### Key Files

| File | Action | Description |
|------|--------|-------------|
| `frontend/src/app/page.tsx` | MODIFY | Update hero page links |

## Testing

### Test File Locations
- `frontend/src/app/__tests__/page.test.tsx` (create if doesn't exist)

### Testing Standards
- Verify all links point to valid routes
- Verify buttons are accessible

## Change Log

| Date | Version | Description | Author |
|------|---------|-------------|--------|
| 2025-12-31 | 1.0 | Initial story creation | James (Dev) |

---

## Dev Agent Record

### Agent Model Used

Claude Opus 4.5 (claude-opus-4-5-20251101)

### Debug Log References

- No issues encountered

### Completion Notes List

1. **Task 1: Verified dual navigation buttons**
   - Both "Explore Knowledge Base" and "Article Analyzer" buttons present
   - Updated to have equal visual weight (both use `bg-primary` styling)
   - Added `focus-visible:ring-2` for accessibility
   - Removed arrows from button text for cleaner look

2. **Task 2: Cleaned up legacy quick-links**
   - Removed old routes: `/entities`, `/members`, `/committees`
   - Added new quick-links pointing to correct routes:
     - Government Organizations → `/knowledge-base/government`
     - People → `/knowledge-base/people`
     - Committees → `/knowledge-base/committees`
     - Extracted Entities → `/article-analyzer/entities`
   - Styled as subtle muted links below primary CTAs

3. **Task 3: Reviewed feature cards**
   - Feature cards are accurate and don't need updates
   - Kept existing descriptions for Factual Accuracy, Logical Fallacies, Cognitive Biases, Source Reliability

4. **Task 4: Wrote 21 tests for hero page**
   - Tests cover: header, primary navigation, quick links, feature cards, footer, accessibility, link validation
   - Validates no legacy routes exist
   - Validates all links have valid prefixes

### File List

| File | Action | Description |
|------|--------|-------------|
| `frontend/src/app/page.tsx` | MODIFIED | Updated hero page with balanced CTAs, new quick-links |
| `frontend/src/app/__tests__/page.test.tsx` | CREATED | 21 tests for hero page |

---

## QA Results

_To be filled by QA agent_
