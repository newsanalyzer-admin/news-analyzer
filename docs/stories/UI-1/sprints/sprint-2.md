# Sprint 2: UI-1 Public Navigation & UX

## Sprint Overview

| Attribute | Value |
|-----------|-------|
| **Epic** | UI-1: Public Navigation & User Experience |
| **Sprint Number** | 2 of 2 |
| **Start Date** | 2025-12-19 |
| **End Date** | TBD |
| **Duration** | 1 week (estimated) |
| **Status** | Planning |

---

## Sprint Goal

> Complete all remaining public factbase content pages with consistent headers, filters, and read-only data display.

---

## Committed Stories

| # | Story | Title | Points | Priority | Assignee | Status | Gate |
|---|-------|-------|--------|----------|----------|--------|------|
| 1 | UI-1.4 | Content Page Template | 3 | P0 - Critical Path | James | **Done** | PASS |
| 2 | UI-1.5 | Congressional Members Page | 3 | P1 | TBD | Ready | - |
| 3 | UI-1.6 | Executive Appointees Page | 3 | P1 | TBD | Ready | - |
| 4 | UI-1.8 | Federal Government Org Pages | 5 | P1 | TBD | Ready | - |
| | **Total** | | **14** | | | |

---

## Internal Dependencies

| Upstream Story | Downstream Story | Dependency Type | Notes |
|----------------|------------------|-----------------|-------|
| **UI-1.4** | **UI-1.5** | Component Dependency | ContentPageHeader required |
| **UI-1.4** | **UI-1.6** | Component Dependency | ContentPageHeader required |
| **UI-1.4** | **UI-1.8** | Component Dependency | ContentPageHeader required |

**Sprint 1 Dependencies (Resolved):**
- UI-1.9 (Legislative orgs) - **DONE** - 22 orgs imported
- UI-1.10 (Judicial orgs) - **DONE** - 124 orgs imported

---

## Capacity Planning

| Metric | Value | Notes |
|--------|-------|-------|
| Sprint Duration | 5 working days | 1 week |
| Committed Points | 14 | Reduced scope vs Sprint 1 |
| Buffer | ~20% | For integration testing |

### Parallelization Plan

```
Day 1: Foundation
└── UI-1.4 (Content Page Template) ◄── MUST COMPLETE FIRST
    └── Creates: ContentPageHeader, page-descriptions.ts

Day 2-4: Parallel Implementation
├── UI-1.5 (Congressional Members Page)
│   └── Reuses existing MemberTable, MemberFilters
├── UI-1.6 (Executive Appointees Page)
│   └── New components, may need API verification
└── UI-1.8 (Federal Government Org Pages)
    └── 3 pages: Executive, Legislative, Judicial
    └── Data ready from Sprint 1

Day 5: QA & Completion
├── All QA reviews
├── Integration testing
└── Sprint retrospective
```

**Critical Path:** UI-1.4 → (UI-1.5 | UI-1.6 | UI-1.8)

---

## Story Summary

### UI-1.4: Content Page Template (3 pts) - BLOCKER

Creates reusable `ContentPageHeader` component for all factbase pages.

**Key Deliverables:**
- `components/public/ContentPageHeader.tsx`
- `lib/page-descriptions.ts` with all page descriptions
- Breadcrumb support

**ACs:** 8

### UI-1.5: Congressional Members Page (3 pts)

Public page at `/factbase/people/congressional-members`.

**Key Deliverables:**
- Page with filters (chamber, state, search)
- Reuse existing `MemberTable`, `MemberFilters` components
- Member detail modal/panel

**ACs:** 10

### UI-1.6: Executive Appointees Page (3 pts)

Public page at `/factbase/people/executive-appointees`.

**Key Deliverables:**
- Page with filters (type, agency, search)
- AppointeeTable component
- Appointee detail modal/panel

**ACs:** 10
**Risk:** May need to verify `/api/appointees` endpoint exists

### UI-1.8: Federal Government Org Pages (5 pts)

Three public pages for government organizations by branch.

**Key Deliverables:**
- `/factbase/organizations/executive`
- `/factbase/organizations/legislative`
- `/factbase/organizations/judicial`
- Organizations hub page
- Hierarchical tree display

**ACs:** 11 (includes bug fix verification)

---

## Daily Progress

### Day 1 (2025-12-19)
| Story | Progress | Notes |
|-------|----------|-------|
| UI-1.4 | **Done** | ContentPageHeader component, page-descriptions.ts, QA PASS |

### Day 2
| Story | Progress | Notes |
|-------|----------|-------|
| | | |

### Day 3
| Story | Progress | Notes |
|-------|----------|-------|
| | | |

### Day 4
| Story | Progress | Notes |
|-------|----------|-------|
| | | |

### Day 5
| Story | Progress | Notes |
|-------|----------|-------|
| | | |

---

## Blockers & Impediments

| # | Date Raised | Blocker | Impact | Owner | Resolution | Date Resolved |
|---|-------------|---------|--------|-------|------------|---------------|
| 1 | — | None yet | — | — | — | — |

---

## Sprint Risks

| Risk | Likelihood | Impact | Mitigation | Status |
|------|------------|--------|------------|--------|
| `/api/appointees` endpoint missing | Medium | High | Check backend, may need to create | Open |
| Component reuse complications | Low | Medium | Existing components are well-structured | Open |
| Integration issues across 3 org pages | Low | Medium | Test each branch separately | Open |

---

## Key Decisions Log

| Date | Decision | Rationale | Made By |
|------|----------|-----------|------------|
| 2025-12-19 | UI-1.4 is Day 1 blocker | All content pages need ContentPageHeader | Bob (SM) |
| 2025-12-19 | Reuse existing member components | Faster implementation, proven patterns | Sarah (PO) |

---

## Sprint Metrics

### Burndown

| Day | Planned | Actual | Remaining |
|-----|---------|--------|-----------|
| 0 | 14 | — | 14 |
| 1 | 11 | — | — |
| 2 | 8 | — | — |
| 3 | 5 | — | — |
| 4 | 2 | — | — |
| 5 | 0 | — | — |

### Story Completion

| Status | Count | Points |
|--------|-------|--------|
| Done | 1 | 3 |
| In Progress | 0 | 0 |
| Ready | 3 | 11 |
| Blocked | 0 | 0 |
| **Total** | **4** | **14** |

---

## Sprint 2 Deliverables

By end of sprint, the following should be complete:

- [x] ContentPageHeader component created **UI-1.4**
- [x] Page descriptions configuration file **UI-1.4**
- [ ] Congressional Members page at `/factbase/people/congressional-members`
- [ ] Executive Appointees page at `/factbase/people/executive-appointees`
- [ ] Executive Branch orgs page at `/factbase/organizations/executive`
- [ ] Legislative Branch orgs page at `/factbase/organizations/legislative`
- [ ] Judicial Branch orgs page at `/factbase/organizations/judicial`
- [ ] Organizations hub page at `/factbase/organizations`
- [ ] All pages use ContentPageHeader with breadcrumbs
- [ ] All pages are read-only (no admin actions)

---

## Epic Completion Status

After Sprint 2, Epic UI-1 will be **complete**:

| Sprint | Stories | Points | Status |
|--------|---------|--------|--------|
| Sprint 1 | 8 (including UI-1.7 pulled forward) | 33 | **Complete** |
| Sprint 2 | 4 | 14 | Planning |
| **Total** | **12** | **47** | |

---

## Sprint Retrospective

*To be completed at end of sprint*

### What Went Well
- TBD

### What Could Be Improved
- TBD

### Action Items
| Action | Owner | Due Date |
|--------|-------|----------|
| TBD | TBD | TBD |

---

## Change Log

| Date | Change | Author |
|------|--------|--------|
| 2025-12-19 | Sprint 2 planning document created | Bob (SM Agent) |
| 2025-12-19 | UI-1.4 completed, QA PASS - Critical path blocker resolved | James (Dev Agent) |

---

*End of Sprint Document*
