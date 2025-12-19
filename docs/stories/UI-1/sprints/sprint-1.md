# Sprint 1: UI-1 Public Navigation & UX

## Sprint Overview

| Attribute | Value |
|-----------|-------|
| **Epic** | UI-1: Public Navigation & User Experience |
| **Sprint Number** | 1 of 2 (estimated) |
| **Start Date** | 2025-12-18 |
| **End Date** | TBD |
| **Duration** | 2 weeks |
| **Status** | In Progress |

---

## Sprint Goal

> Establish the foundation for public factbase navigation by completing shared sidebar infrastructure, data imports for Legislative/Judicial branches, and initiating critical path research for Federal Judges data.

---

## Committed Stories

| # | Story | Title | Points | Priority | Assignee | Status | Gate |
|---|-------|-------|--------|----------|----------|--------|------|
| 1 | UI-1.11 | Federal Judges Data Research & Import | 8 | P0 - Critical Path | James | **Done** | PASS |
| 2 | UI-1.1 | Shared Sidebar Components | 5 | P1 | James | **Done** | PASS |
| 3 | UI-1.9 | Populate Legislative Branch Orgs | 3 | P1 | James | **Done** | PASS |
| 4 | UI-1.10 | Populate Judicial Branch Orgs | 3 | P1 | James | **Done** | PASS |
| 5 | UI-1.2 | Factbase Layout & Landing Update | 5 | P2 | TBD | Ready | - |
| 6 | UI-1.3 | Menu Configuration System | 3 | P2 | TBD | Ready | - |
| 7 | UI-1.12 | Admin Access Link | 1 | P3 | TBD | Ready | - |
| | **Total** | | **28** | | | |

### Bonus: Pulled Forward from Sprint 2

| # | Story | Title | Points | Priority | Assignee | Status | Gate |
|---|-------|-------|--------|----------|----------|--------|------|
| 8 | UI-1.7 | Federal Judges Page | 5 | P1 | James | **Done** | PASS |

**Note:** UI-1.7 was originally planned for Sprint 2 but was pulled forward after its blocker (UI-1.11) was resolved.

### ⚠️ Internal Dependencies (Updated 2025-12-18)

| Upstream Story | Downstream Story | Dependency Type | Notes |
|----------------|------------------|-----------------|-------|
| **UI-1.10** | **UI-1.11** (AC 6 only) | Data Dependency | Judicial orgs must be imported before FJC judge import can link courts |

**Impact:** UI-1.11 research (AC 1-5) can run in parallel with UI-1.10, but the judge import step (AC 6) must wait for UI-1.10 to complete.

---

## Capacity Planning

| Metric | Value | Notes |
|--------|-------|-------|
| Sprint Duration | 10 working days | 2 weeks |
| Team Velocity | TBD | First sprint - establishing baseline |
| Committed Points | 28 | ~70% of typical capacity |
| Buffer | ~20% | For unknowns in UI-1.11 research |

### Parallelization Plan (Updated 2025-12-18)

```
Day 1-3: Parallel Start
├── UI-1.11 Research (AC 1-5) ─── Research spike, critical path
├── UI-1.1 (Sidebar components)
├── UI-1.9 (Legislative orgs CSV import)
└── UI-1.10 (Judicial orgs CSV import) ◄── MUST COMPLETE before UI-1.11 AC 6

Day 4-7: Infrastructure + Judge Import
├── UI-1.2 (Factbase layout) - after UI-1.1
├── UI-1.3 (Menu config) - after UI-1.2
└── UI-1.11 Import (AC 6) ─── after UI-1.10 completes
                           └── Verify: GET /api/government-organizations?branch=JUDICIAL returns 120+ orgs

Day 8-10: Completion
├── UI-1.12 (Admin link) - trivial
└── UI-1.11 verification & cleanup
```

**⚠️ Critical Sequencing:**
- UI-1.10 → UI-1.11 (AC 6): FJC import needs judicial orgs in DB for court cache

---

## Daily Progress

### Day 1 (2025-12-18)
| Story | Progress | Notes |
|-------|----------|-------|
| UI-1.9 | **Done** | 22 legislative orgs imported, QA PASS |
| UI-1.10 | **Done** | 124 judicial orgs imported, QA PASS |
| UI-1.11 | **Done** | FJC CSV research complete, 25 backend tests pass, QA PASS |
| UI-1.7 | **Done** | Frontend page complete, pulled from Sprint 2, QA PASS |
| UI-1.1 | **Done** | Shared sidebar components (from prior session), QA PASS |

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

### Day 6
| Story | Progress | Notes |
|-------|----------|-------|
| | | |

### Day 7
| Story | Progress | Notes |
|-------|----------|-------|
| | | |

### Day 8
| Story | Progress | Notes |
|-------|----------|-------|
| | | |

### Day 9
| Story | Progress | Notes |
|-------|----------|-------|
| | | |

### Day 10
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
| UI-1.11 FJC API not viable | Medium | High | Fallback to "Coming Soon" for Federal Judges | Resolved ✅ |
| Sidebar refactor breaks admin | Low | Medium | Test admin sidebar after UI-1.1 changes | Open |
| UI-1.10 delayed blocks UI-1.11 import | Low | Medium | Prioritize UI-1.10 completion by Day 3; import is trivial | Open |
| FJC import produces null court links | Medium | Medium | Verify UI-1.10 complete before running FJC import | Open |

---

## Key Decisions Log

| Date | Decision | Rationale | Made By |
|------|----------|-----------|---------|
| 2025-12-15 | Start UI-1.11 Day 1 | Critical path - blocks UI-1.7 in Sprint 2 | Bob (SM) |
| 2025-12-15 | Include UI-1.12 (1pt) | Quick win, trivial effort | Bob (SM) |
| 2025-12-18 | UI-1.10 must complete before UI-1.11 AC 6 | FJC import needs judicial orgs in DB for court cache | Sarah (PO) |
| 2025-12-18 | Split UI-1.11 execution: Research (Day 1-3) / Import (Day 4+) | Allows parallel work while respecting dependency | Sarah (PO) |

---

## Sprint Metrics

### Burndown

| Day | Planned | Actual | Remaining |
|-----|---------|--------|-----------|
| 0 | 28 | — | 28 |
| 1 | 25 | — | — |
| 2 | 22 | — | — |
| 3 | 20 | — | — |
| 4 | 17 | — | — |
| 5 | 14 | — | — |
| 6 | 11 | — | — |
| 7 | 8 | — | — |
| 8 | 6 | — | — |
| 9 | 3 | — | — |
| 10 | 0 | — | — |

### Story Completion

| Status | Count | Points |
|--------|-------|--------|
| Done | 5 | 24 |
| In Progress | 0 | 0 |
| Ready | 3 | 9 |
| Blocked | 0 | 0 |
| **Total** | **8** | **33** |

*Note: Includes UI-1.7 (5 pts) pulled forward from Sprint 2*

---

## Sprint 1 Deliverables

By end of sprint, the following should be complete:

- [x] Shared sidebar components extracted and working (`components/sidebar/`) **UI-1.1**
- [x] Admin sidebar still functional after refactor **UI-1.1**
- [ ] Factbase layout created at `/factbase`
- [ ] Hero page has "Explore Factbase" CTA
- [ ] Public sidebar menu configuration defined
- [x] Legislative branch orgs imported (≥15 orgs) ✅ **22 orgs**
- [x] Judicial branch orgs imported (≥120 orgs) ✅ **124 orgs**
- [x] Federal Judges data source decision made (UI-1.11) ✅ **FJC CSV**
- [ ] Admin gear icon in sidebar footer

### Bonus Deliverables (Pulled Forward)

- [x] Federal Judges page at `/factbase/people/federal-judges` ✅ **UI-1.7**

---

## Carryover to Sprint 2

Stories not in Sprint 1 (planned for Sprint 2):

| Story | Title | Points | Notes |
|-------|-------|--------|-------|
| UI-1.4 | Content Page Template | 3 | |
| UI-1.5 | Congressional Members Page | 3 | |
| UI-1.6 | Executive Appointees Page | 3 | |
| ~~UI-1.7~~ | ~~Federal Judges Page~~ | ~~5~~ | ✅ Pulled into Sprint 1 |
| UI-1.8 | Federal Government Org Pages | 5 | Needs UI-1.9, UI-1.10 data (now ready) |
| **Total** | | **14** | |

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
| 2025-12-15 | Sprint 1 planning document created | Bob (SM Agent) |
| 2025-12-18 | Added internal dependency: UI-1.10 → UI-1.11 (AC 6) | Sarah (PO Agent) |
| 2025-12-18 | Updated parallelization plan to reflect dependency sequencing | Sarah (PO Agent) |
| 2025-12-18 | Added 2 new risks related to dependency | Sarah (PO Agent) |
| 2025-12-18 | Marked FJC API risk as Resolved (research confirmed CSV available) | Sarah (PO Agent) |
| 2025-12-18 | Sprint started - Day 1: UI-1.9, UI-1.10, UI-1.11 completed | James (Dev Agent) |
| 2025-12-18 | UI-1.7 pulled forward from Sprint 2, completed and QA passed | James (Dev Agent) |
| 2025-12-18 | Updated story completion metrics (4 done, 19 pts) | James (Dev Agent) |
| 2025-12-18 | UI-1.1 QA review complete, PASS gate created (5 done, 24 pts) | Quinn (Test Architect) |

---

*End of Sprint Document*
