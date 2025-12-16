# Factbase Expansion: Product Owner Summary

## Overview

This document summarizes research findings on authoritative government data sources that can expand NewsAnalyzer's factbase. The goal is to enable fact-checking across the "4 W's" of government information: **WHO**, **WHAT**, **WHERE**, and **WHEN**.

---

## Business Value

Expanding the factbase with these sources enables NewsAnalyzer to verify claims about:

| Category | Example Verifiable Claims |
|----------|--------------------------|
| **WHO** | "Senator X serves on the Armed Services Committee" |
| **WHO** | "The Secretary of Y was appointed in 2023" |
| **WHAT** | "The EPA issued a rule requiring..." |
| **WHERE** | "Representative Z represents the 5th district of California" |
| **WHEN** | "Congress was in session on [date]" |

---

## Recommended Data Sources (Prioritized)

### Tier 1: High Value, Low Complexity

| # | Source | What It Provides | Why It Matters |
|---|--------|------------------|----------------|
| 1 | **Congress.gov API** | Members of Congress, committees, leadership, votes | Core WHO data - extends current gov orgs |
| 2 | **unitedstates/congress-legislators** | Enriched member data, historical records back to 1789 | Fills gaps, provides cross-references |
| 3 | **Federal Register API** | Regulations, agency actions, effective dates | WHAT + WHEN - verifiable regulatory claims |

### Tier 2: High Value, Medium Complexity

| # | Source | What It Provides | Why It Matters |
|---|--------|------------------|----------------|
| 4 | **OPM PLUM Data** | Executive branch appointees, 8,000+ positions | WHO - Cabinet, agency heads |
| 5 | **FEC OpenFEC API** | Candidates, campaign finance, contributions | WHO + WHAT - follow the money |
| 6 | **Census TIGER/Line** | Congressional district boundaries | WHERE - geographic context |

### Tier 3: Supplementary

| # | Source | What It Provides | Why It Matters |
|---|--------|------------------|----------------|
| 7 | **CourtListener** | Judges, court cases, opinions | WHO - judicial branch |
| 8 | **MIT Election Lab** | Historical election results | WHO + WHEN - electoral context |
| 9 | **OPM Federal Holidays** | Official government calendar | WHEN - session verification |

---

## Critical Discoveries

### Good News
- **Congress.gov API is robust** - Official, well-documented, 5,000 requests/hour
- **Federal Register has NO authentication** - Easy to integrate
- **OPM launched new PLUM website** - Modern, regularly updated appointee data
- **FEC API is comprehensive** - Real campaign finance data, 15-minute updates

### Bad News (Important!)
- **ProPublica Congress API is SHUT DOWN** (July 2024)
- **GovTrack API is SHUT DOWN** (2018)
- **OpenSecrets API is SHUT DOWN** (April 2025)
- These were commonly referenced sources - documentation online is outdated

### Gaps
- **No API for state-level officials** (governors, state legislators)
- **No real-time election results API** (would need AP, which is paid)
- **OPM PLUM has no documented API** (web interface only currently)

---

## Scope Decisions (CONFIRMED)

*Decisions confirmed by stakeholder on November 2024*

| # | Question | Decision | Status |
|---|----------|----------|--------|
| 1 | **Depth vs. Breadth** | Deep first (Congress.gov + congress-legislators), then expand | **CONFIRMED** |
| 2 | **Historical Data** | 1990s-present (matches Federal Register availability) | **CONFIRMED** |
| 3 | **Geographic Integration** | ID linkage only; defer GIS until needed | **CONFIRMED** |
| 4 | **Campaign Finance Scope** | Candidate IDs only in Phase 1; contributions deferred | **CONFIRMED** |
| 5 | **Update Frequency** | Daily sync is acceptable for MVP | **CONFIRMED** |

### Additional Decisions

| Question | Decision | Status |
|----------|----------|--------|
| **User Persona** | Fact-checker | **CONFIRMED** |
| **OPM PLUM Strategy** | Web scraper (no API available) | **CONFIRMED** |
| **Epic Structure** | 3 separate epics | **CONFIRMED** |

---

## Proposed User Stories

### Epic: Expand Factbase with Congressional Data

**Story 1: Member Lookup**
> As a fact-checker, I want to look up current Members of Congress so I can verify claims about who holds office.

**Story 2: Committee Verification**
> As a fact-checker, I want to see committee assignments so I can verify claims like "Senator X is on the Judiciary Committee."

**Story 3: Position History**
> As a fact-checker, I want to see when a member started/ended their term so I can verify claims about timing.

### Epic: Expand Factbase with Executive Branch Data

**Story 4: Appointee Lookup**
> As a fact-checker, I want to look up Cabinet members and agency heads so I can verify claims about executive branch leadership.

### Epic: Expand Factbase with Regulatory Data

**Story 5: Regulation Lookup**
> As a fact-checker, I want to search for federal regulations by agency so I can verify claims about what rules exist.

**Story 6: Effective Date Verification**
> As a fact-checker, I want to see when a regulation took effect so I can verify timing claims.

---

## Implementation Plan (CONFIRMED)

| Phase | Scope | Data Sources | Epic | Relative Effort |
|-------|-------|--------------|------|-----------------|
| **Phase 1** | Congressional Members & Committees | Congress.gov API, congress-legislators | Epic 1: Congressional Data | Small-Medium |
| **Phase 2** | Regulatory Data | Federal Register API | Epic 3: Regulatory Data | Small |
| **Phase 3** | Executive Appointees | OPM PLUM (web scraper) | Epic 2: Executive Branch | Medium-High |
| **Phase 4** | Campaign Finance (candidates) | FEC OpenFEC | Epic 1 (extension) | Medium |
| **Phase 5** | Geographic Context | Census TIGER | Deferred | Medium-Large |
| **Phase 6** | Judicial Data | CourtListener | Deferred | Medium |

---

## Risks & Mitigations

| Risk | Impact | Mitigation |
|------|--------|------------|
| API rate limits exceeded | Service disruption | Implement caching, respect limits |
| Source API deprecated | Loss of data source | Use multiple sources, monitor announcements |
| OPM PLUM has no API | Integration complexity | **Build web scraper** (confirmed approach) |
| Data staleness | Incorrect fact checks | Implement freshness indicators, regular sync |
| Schema complexity | Development delays | Start simple, iterate |

---

## Next Steps

1. ~~**PO Review**: Validate priorities and scope decisions above~~ **COMPLETE**
2. ~~**Architect Handoff**: Detailed findings in `docs/research/AUTHORITATIVE_DATA_SOURCES_RESEARCH_FINDINGS.md`~~ **COMPLETE** (see `docs/architecture/FACTBASE_EXPANSION_ARCHITECT_HANDOFF.md`)
3. ~~**Story Refinement**: Break down stories with acceptance criteria~~ **COMPLETE** (FB-1 stories created)
4. **Architect Review**: Review handoff document and answer design questions
5. **Technical Spike**: Validate Congress.gov API integration approach
6. **Sprint Planning**: Schedule Phase 1 work

---

## Reference Documents

### Research & Architecture

| Document | Location | Purpose |
|----------|----------|---------|
| `AUTHORITATIVE_DATA_SOURCES_RESEARCH_FINDINGS.md` | `docs/research/` | Detailed technical findings for architect |
| `AUTHORITATIVE_DATA_SOURCES_RESEARCH_PROMPT.md` | `docs/research/` | Original research prompt |
| `FACTBASE_EXPANSION_ARCHITECT_HANDOFF.md` | `docs/architecture/` | Technical requirements for architect |
| `GOVINFO_API_FINDINGS.md` | `docs/reference/APIs and Other Sources/` | Prior research on GovInfo |

### Epics & Stories

| Epic | Document | Stories |
|------|----------|---------|
| **FB-1: Congressional Data** | [`FB-1.epic-congressional-data.md`](../stories/FB-1/FB-1.epic-congressional-data.md) | Done (5 stories) |
| **FB-1-UI: Frontend Congressional** | [`FB-1-UI.epic-frontend-congressional.md`](../stories/FB-1-UI/FB-1-UI.epic-frontend-congressional.md) | Done (5 stories) |
| **FB-2-GOV: Gov Org Sync** | [`FB-2-GOV.epic-government-org-sync.md`](../stories/FB-2-GOV/FB-2-GOV.epic-government-org-sync.md) | Done (4 stories) |
| **FB-2: Executive Branch** | [`FB-2.epic-executive-branch-data.md`](../stories/FB-2/FB-2.epic-executive-branch-data.md) | Stories drafted |
| **FB-3: Regulatory Data** | [`FB-3.epic-regulatory-data.md`](../stories/FB-3.epic-regulatory-data.md) | Stories pending |
| **QA-1: API Testing Framework** | [`QA-1.epic-api-testing-framework.md`](../stories/QA-1/QA-1.epic-api-testing-framework.md) | Done (6 stories) |

### FB-1 Stories (Complete)

| Story | Document |
|-------|----------|
| FB-1.1 | [`FB-1.1.member-lookup-integration.md`](../stories/FB-1/FB-1.1.member-lookup-integration.md) |
| FB-1.2 | [`FB-1.2.committee-data-integration.md`](../stories/FB-1/FB-1.2.committee-data-integration.md) |
| FB-1.3 | [`FB-1.3.position-history-tracking.md`](../stories/FB-1/FB-1.3.position-history-tracking.md) |
| FB-1.4 | [`FB-1.4.legislators-enrichment-sync.md`](../stories/FB-1/FB-1.4.legislators-enrichment-sync.md) |

### FB-1-UI Stories (Complete)

| Story | Document | Status |
|-------|----------|--------|
| FB-1-UI.1 | [`FB-1-UI.1.shadcn-setup-types.md`](../stories/FB-1-UI/FB-1-UI.1.shadcn-setup-types.md) | Done |
| FB-1-UI.2 | [`FB-1-UI.2.members-listing-page.md`](../stories/FB-1-UI/FB-1-UI.2.members-listing-page.md) | Done |
| FB-1-UI.3 | [`FB-1-UI.3.committees-listing-page.md`](../stories/FB-1-UI/FB-1-UI.3.committees-listing-page.md) | Done |
| FB-1-UI.4 | [`FB-1-UI.4.member-detail-page.md`](../stories/FB-1-UI/FB-1-UI.4.member-detail-page.md) | Done |
| FB-1-UI.5 | [`FB-1-UI.5.admin-sync-dashboard.md`](../stories/FB-1-UI/FB-1-UI.5.admin-sync-dashboard.md) | Done |

### FB-2-GOV Stories (Complete)

| Story | Document |
|-------|----------|
| FB-2-GOV.1 | [`FB-2-GOV.1.federal-register-api-integration.md`](../stories/FB-2-GOV/FB-2-GOV.1.federal-register-api-integration.md) |
| FB-2-GOV.2 | [`FB-2-GOV.2.admin-dashboard-gov-org-sync-ui.md`](../stories/FB-2-GOV/FB-2-GOV.2.admin-dashboard-gov-org-sync-ui.md) |
| FB-2-GOV.3 | [`FB-2-GOV.3.csv-import-legislative-judicial.md`](../stories/FB-2-GOV/FB-2-GOV.3.csv-import-legislative-judicial.md) |
| FB-2-GOV.4 | [`FB-2-GOV.4.api-integration-tests.md`](../stories/FB-2-GOV/FB-2-GOV.4.api-integration-tests.md) |

### QA-1 Stories (Complete)

| Story | Document |
|-------|----------|
| QA-1.1 | [`QA-1.1.api-test-project-setup.md`](../stories/QA-1/QA-1.1.api-test-project-setup.md) |
| QA-1.2 | [`QA-1.2.backend-api-test-suite.md`](../stories/QA-1/QA-1.2.backend-api-test-suite.md) |
| QA-1.3 | [`QA-1.3.reasoning-service-test-suite.md`](../stories/QA-1/QA-1.3.reasoning-service-test-suite.md) |
| QA-1.4 | [`QA-1.4.database-integration-test-data.md`](../stories/QA-1/QA-1.4.database-integration-test-data.md) |
| QA-1.5 | [`QA-1.5.cicd-pipeline-integration.md`](../stories/QA-1/QA-1.5.cicd-pipeline-integration.md) |
| QA-1.6 | [`QA-1.6.cross-service-integration-tests.md`](../stories/QA-1/QA-1.6.cross-service-integration-tests.md) |

---

*Prepared by: Mary (Business Analyst)*
*Date: November 2024*
*For: NewsAnalyzer Factbase Expansion Initiative*

---

## PO Review Notes

*Reviewed by: Sarah (Product Owner)*
*Review Date: November 2024*

**Status**: All scope decisions confirmed. Document updated with confirmed decisions and revised implementation phasing. Ready for architect handoff.

**Update (November 2024)**:
- Created 3 epic documents (FB-1, FB-2, FB-3)
- Created 4 detailed story files for Epic FB-1 (Congressional Data)
- Created architect handoff document with technical requirements
- FB-1 stories ready for development after architect review
