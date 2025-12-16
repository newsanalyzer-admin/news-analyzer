# Epic UI-1: Public Navigation & User Experience

## Status

**Approved - Ready for Development**

*Submitted: 2025-12-15 by Sarah (PO Agent)*
*Architect Review: 2025-12-15 by Winston (Architect Agent)*
*PO Final Review: 2025-12-15 by Sarah (PO Agent)*

---

## Epic Summary

This epic delivers a public-facing navigation system and user experience optimized for users unfamiliar with the government domain. It introduces a collapsible sidebar at `/factbase`, educational content pages with contextual descriptions, and a static menu configuration that can be extended to data-driven visibility in future iterations.

---

## Business Value

- **Improved Discoverability**: Users can navigate the factbase intuitively without prior government knowledge
- **Educational Context**: Each page includes descriptions explaining the data category and its relevance
- **Scalable Navigation**: Menu structure can expand as new data categories are added
- **Clear Information Architecture**: Entity-type based organization (People, Organizations, Events) vs. branch-based
- **Seamless Admin Access**: Power users can access admin functions via gear icon

### Success Metrics

| Metric | Target | Measurement |
|--------|--------|-------------|
| Navigation depth | Any data category reachable in ≤ 3 clicks | Manual testing |
| Broken links | 0 at launch | Automated link checker |
| Keyboard navigability | 100% of interactive elements | Accessibility audit |
| Data completeness | Legislative: ≥15 orgs, Judicial: ≥120 orgs | Database query |
| Responsive breakpoints | Works at 320px, 768px, 1024px, 1440px | Cross-device testing |

---

## Scope

### In Scope

| Category | Items |
|----------|-------|
| **Navigation** | Public sidebar component, factbase layout, gear icon admin link |
| **People Pages** | Congressional Members, Executive Appointees, Federal Judges & Justices |
| **Organization Pages** | Executive Branch, Legislative Branch, Judicial Branch |
| **Data Import** | Legislative branch orgs, Judicial branch orgs, Federal judges data |
| **Content** | AI-generated educational descriptions for each page |

### Out of Scope (Future Epics)

| Category | Deferred Items |
|----------|----------------|
| **People** | Business Leaders, Academia, Artists, Philanthropists, Billionaires, Criminals, Historical Figures |
| **Organizations** | Think Tanks, Charities/Non-Profits, Universities, Fortune 500 |
| **Events** | Elections, Court Rulings, Congressional Votes, Executive Orders, Announcements, IPOs, Scientific Discoveries, Crimes |
| **Event Entity** | New Event data model (requires separate epic) |
| **Data-Driven Menu** | Automatic hiding of empty categories (deferred to future enhancement) |

---

## Architectural Decisions

The following decisions were made during architect review:

| # | Decision | Resolution | Rationale |
|---|----------|------------|-----------|
| 1 | **URL Structure** | Use `/factbase/...` prefix | Allows future `/people/{slug}` entity pages without collision |
| 2 | **Home Page** | Keep `/` as hero, sidebar at `/factbase` | Preserves marketing landing; factbase is one feature of NewsAnalyzer |
| 3 | **Sidebar Components** | Extract shared components to `components/sidebar/` | Avoids duplicating 194 lines of `SidebarMenuItem` code |
| 4 | **Menu Configuration** | Static config for V1, data-driven deferred | Simpler implementation; can iterate later |
| 5 | **Federal Judges Source** | Research FJC (fjc.gov) first | Free API with historical data since 1789 |

---

## Design Decisions

1. **Static Menu Config (V1)**: Menu structure is statically configured; data-driven visibility deferred to future enhancement
2. **Max 3 Levels**: Sidebar navigation limited to 3 levels of nesting
3. **Entity-Type Organization**: Menu organized by People/Organizations/Events (not by government branch)
4. **Read-Only**: Public pages display data only; editing requires admin access
5. **Shared Components**: Sidebar components extracted to shared location for reuse

---

## Navigation Structure

```
/ (Hero landing page with "Explore Factbase" CTA)
│
└── /factbase (Sidebar navigation)
    ├── People
    │   └── Current Government Officials
    │       ├── Congressional Members        → /factbase/people/congressional-members
    │       ├── Executive Appointees         → /factbase/people/executive-appointees
    │       └── Federal Judges & Justices    → /factbase/people/federal-judges
    │
    └── Organizations
        └── Federal Government
            ├── Executive Branch             → /factbase/organizations/executive
            ├── Legislative Branch           → /factbase/organizations/legislative
            └── Judicial Branch              → /factbase/organizations/judicial

    [Gear Icon] → /admin
```

---

## Stories

| Story | Title | Status | Points | Description | Technical Notes |
|-------|-------|--------|--------|-------------|-----------------|
| UI-1.1 | Shared Sidebar Components | Ready | 5 | Extract `SidebarMenuItem` to shared location; create `BaseSidebar` wrapper | Move from `admin/` to `sidebar/` |
| UI-1.2 | Factbase Layout & Landing Update | Ready | 5 | Create `/factbase/layout.tsx` with `PublicSidebar`; update `/` with CTA | Keep `/` as hero page |
| UI-1.3 | Menu Configuration System | Ready | 3 | Static menu configuration for public sidebar | Defer data-driven to future |
| UI-1.4 | Content Page Template | Ready | 3 | Create `ContentPageHeader` with title, description, breadcrumb | Reusable across all pages |
| UI-1.5 | Congressional Members Page | Ready | 3 | Public page at `/factbase/people/congressional-members` | Reuse existing components |
| UI-1.6 | Executive Appointees Page | Ready | 3 | Public page at `/factbase/people/executive-appointees` | Reuse existing components |
| UI-1.7 | Federal Judges Page | Ready (Blocked) | 5 | Public page at `/factbase/people/federal-judges` | **BLOCKED by UI-1.11** |
| UI-1.8 | Federal Government Org Pages | Ready | 5 | Executive/Legislative/Judicial branch org pages | Fix existing branch filter bug |
| UI-1.9 | Populate Legislative Branch Orgs | Ready | 3 | CSV import: ~15 orgs (Congress, GAO, CBO, LOC, GPO, etc.) | Independent, can parallelize |
| UI-1.10 | Populate Judicial Branch Orgs | Ready | 3 | CSV import: ~120 orgs (Supreme Court, Circuits, Districts) | Independent, can parallelize |
| UI-1.11 | Federal Judges Data Research & Import | Ready | 8 | Research FJC API; implement if viable | **Critical path** - start early |
| UI-1.12 | Admin Access Link | Ready | 1 | Settings gear icon in sidebar footer → `/admin` | Trivial, do last |
| **Total** | | **0/12 Done** | **47** | | |

---

## Dependency Graph

```
UI-1.9 (Legislative Orgs CSV) ─────────────────────────┐
UI-1.10 (Judicial Orgs CSV) ───────────────────────────┤
UI-1.11 (Federal Judges Research) ─────────────────────┤
                                                       │
UI-1.1 (Shared Sidebar Components) ────────────────────┤
       │                                               │
       ▼                                               │
UI-1.2 (Factbase Layout) ──────────────────────────────┤
       │                                               │
       ├──► UI-1.12 (Admin Link)                       │
       │                                               │
       ▼                                               │
UI-1.3 (Menu Config) ──────────────────────────────────┤
       │                                               │
       ▼                                               │
UI-1.4 (Page Template) ────────────────────────────────┤
       │                                               │
       ├──► UI-1.5 (Congressional Members) ◄───────────┘
       │           uses existing data
       │
       ├──► UI-1.6 (Executive Appointees)
       │           uses existing data
       │
       ├──► UI-1.7 (Federal Judges) ◄─── BLOCKED by UI-1.11
       │
       └──► UI-1.8 (Gov Org Pages) ◄──── needs UI-1.9, UI-1.10
```

### Recommended Execution Order

**Phase 1 - Data & Infrastructure (Parallel)**
1. UI-1.9: Populate Legislative Branch Orgs (CSV)
2. UI-1.10: Populate Judicial Branch Orgs (CSV)
3. UI-1.11: Federal Judges Research Spike (critical path)
4. UI-1.1: Shared Sidebar Components

**Phase 2 - Layout Infrastructure**
5. UI-1.2: Factbase Layout & Landing Update
6. UI-1.3: Menu Configuration System
7. UI-1.4: Content Page Template

**Phase 3 - Content Pages**
8. UI-1.5: Congressional Members Page
9. UI-1.6: Executive Appointees Page
10. UI-1.8: Federal Government Org Pages

**Phase 4 - Completion**
11. UI-1.7: Federal Judges Page (after UI-1.11 completes)
12. UI-1.12: Admin Access Link

---

## Technical Architecture

### Frontend Structure (Updated per Architect Review)

```
frontend/src/
├── app/
│   ├── page.tsx                              # Hero landing page (add "Explore Factbase" CTA)
│   ├── layout.tsx                            # Root layout
│   └── factbase/
│       ├── layout.tsx                        # Factbase layout with PublicSidebar
│       ├── page.tsx                          # Factbase home/overview
│       ├── people/
│       │   ├── page.tsx                      # People hub
│       │   ├── congressional-members/
│       │   │   └── page.tsx                  # Congressional members list
│       │   ├── executive-appointees/
│       │   │   └── page.tsx                  # Executive appointees list
│       │   └── federal-judges/
│       │       └── page.tsx                  # Federal judges list
│       └── organizations/
│           ├── page.tsx                      # Organizations hub
│           ├── executive/
│           │   └── page.tsx                  # Executive branch orgs
│           ├── legislative/
│           │   └── page.tsx                  # Legislative branch orgs
│           └── judicial/
│               └── page.tsx                  # Judicial branch orgs
├── components/
│   ├── sidebar/                              # SHARED sidebar components (NEW)
│   │   ├── BaseSidebar.tsx                   # Generic sidebar shell
│   │   ├── SidebarMenuItem.tsx               # MOVED from admin/ (already generic)
│   │   └── types.ts                          # MenuItemData interface
│   ├── admin/
│   │   └── AdminSidebar.tsx                  # Uses BaseSidebar + admin menu config
│   ├── public/
│   │   ├── PublicSidebar.tsx                 # Uses BaseSidebar + public menu config
│   │   └── ContentPageHeader.tsx             # Title + description template
│   └── ui/                                   # Existing shadcn components
├── stores/
│   ├── sidebarStore.ts                       # Admin sidebar state (existing)
│   └── publicSidebarStore.ts                 # Public sidebar state (NEW)
└── lib/
    └── menu-config.ts                        # Static menu configurations
```

### Component Reuse Strategy

| Existing Component | Action | New Location |
|--------------------|--------|--------------|
| `admin/SidebarMenuItem.tsx` | **MOVE** (not copy) | `sidebar/SidebarMenuItem.tsx` |
| `admin/AdminSidebar.tsx` | **REFACTOR** to use `BaseSidebar` | `admin/AdminSidebar.tsx` |
| `stores/sidebarStore.ts` | **KEEP** for admin | `stores/sidebarStore.ts` |
| — | **CREATE** `BaseSidebar` wrapper | `sidebar/BaseSidebar.tsx` |
| — | **CREATE** public sidebar | `public/PublicSidebar.tsx` |
| — | **CREATE** public store | `stores/publicSidebarStore.ts` |

### Data Sources

| Data Category | Source | Model | API | Status |
|---------------|--------|-------|-----|--------|
| Congressional Members | Congress.gov API | `Person` | `/api/members` | ✅ Available |
| Executive Appointees | PLUM CSV | `Person` + `PositionHolding` | `/api/appointees` | ✅ Available |
| Federal Judges | FJC API (research needed) | `Person` (extend?) | TBD | ❌ UI-1.11 |
| Executive Branch Orgs | Federal Register API | `GovernmentOrganization` | `/api/government-organizations` | ✅ Available |
| Legislative Branch Orgs | CSV import | `GovernmentOrganization` | `/api/government-organizations` | ❌ UI-1.9 |
| Judicial Branch Orgs | CSV import | `GovernmentOrganization` | `/api/government-organizations` | ❌ UI-1.10 |

### Federal Judges Data Source (UI-1.11 Research)

**Primary Recommendation:** Federal Judicial Center (fjc.gov)
- Biographical Directory of Article III Federal Judges
- Free API available
- Historical data since 1789
- Includes: name, court, appointment date, appointing president, termination

**Potential Model Extensions:**
```java
// May need new fields on Person for judicial-specific data
private String courtName;           // "U.S. Court of Appeals for the 9th Circuit"
private String appointingPresident; // "Joseph R. Biden"
private LocalDate commissionDate;   // Date of commission
private String judicialStatus;      // "Active", "Senior", "Deceased"
```

---

## Page Descriptions (AI-Generated Drafts)

These descriptions will appear at the top of each content page to provide educational context.

### People > Current Government Officials

> **Current Government Officials** are the individuals who hold positions of authority within the United States federal government. This includes elected representatives, presidential appointees, and judges serving on federal courts. Understanding who holds power and their responsibilities is essential for informed civic participation.

### People > Congressional Members

> **Congressional Members** are the 535 elected officials who serve in the United States Congress—100 Senators (2 per state) and 435 Representatives (apportioned by population). Congress is the legislative branch of government, responsible for writing and passing federal laws, controlling the federal budget, and providing oversight of the executive branch. Members serve terms of 6 years (Senate) or 2 years (House).

### People > Executive Appointees

> **Executive Appointees** are individuals appointed by the President to serve in leadership positions across the executive branch. This includes Cabinet secretaries, agency heads, ambassadors, and thousands of other positions that help run the federal government. Some positions require Senate confirmation (PAS - Presidential Appointment with Senate Confirmation), while others do not. The Plum Book, published after each presidential election, catalogs these positions.

### People > Federal Judges & Justices

> **Federal Judges and Justices** are appointed by the President and confirmed by the Senate to serve on the federal judiciary. This includes the 9 Supreme Court Justices, approximately 179 Court of Appeals judges across 13 circuits, and over 670 District Court judges. Federal judges serve lifetime appointments "during good behavior," providing independence from political pressure. They interpret federal law and the Constitution, resolve disputes between states, and review the constitutionality of government actions.

### Organizations > Federal Government

> **Federal Government Organizations** are the agencies, departments, and institutions that comprise the three branches of the United States government. The Executive Branch implements and enforces laws through departments and agencies. The Legislative Branch creates laws through Congress and its supporting offices. The Judicial Branch interprets laws through the federal court system. Together, these organizations employ over 2 million civilian workers and manage trillions of dollars in annual spending.

### Organizations > Executive Branch

> **Executive Branch Organizations** include the 15 Cabinet-level departments (State, Treasury, Defense, etc.), over 100 independent agencies (EPA, NASA, CIA), and numerous boards, commissions, and government corporations. Led by the President, these organizations implement federal laws, conduct foreign policy, manage national defense, and provide public services. The Federal Register publishes daily updates on executive branch activities including proposed and final regulations.

### Organizations > Legislative Branch

> **Legislative Branch Organizations** support the work of Congress in creating federal law. Beyond the Senate and House of Representatives, this includes the Government Accountability Office (GAO) which audits federal programs, the Congressional Budget Office (CBO) which analyzes fiscal impacts, the Library of Congress, the Government Publishing Office, and the Capitol Police. These organizations provide research, analysis, and operational support to elected members.

### Organizations > Judicial Branch

> **Judicial Branch Organizations** comprise the federal court system established by the Constitution and Congress. The Supreme Court sits at the apex, followed by 13 Courts of Appeals (12 regional circuits plus the Federal Circuit), 94 District Courts, and specialized courts including Bankruptcy Courts, the Court of International Trade, and the Court of Federal Claims. The Administrative Office of the U.S. Courts and the Federal Judicial Center provide administrative support and judicial education.

---

## Dependencies

### Blocked By
- None (can begin immediately)

### Internal Blockers
- **UI-1.7** (Federal Judges Page) is **BLOCKED** by **UI-1.11** (Federal Judges Data Research)

### Blocks
- Future People categories (need menu infrastructure)
- Future Organization categories (need menu infrastructure)
- EVENT-1 Epic (Events navigation will use this menu system)

### Related
- ADMIN-1: Admin sidebar components to adapt
- FB-1: Congressional member data
- FB-2: Executive appointee data

---

## Acceptance Criteria

### Epic-Level

- [ ] Hero page (`/`) includes prominent "Explore Factbase" CTA
- [ ] `/factbase` displays collapsible sidebar with navigation
- [ ] All visible menu items link to functional content pages
- [ ] Each content page includes educational description header
- [ ] Gear icon in sidebar footer links to `/admin`
- [ ] Responsive design works at 320px, 768px, 1024px, 1440px breakpoints
- [ ] Legislative branch organizations populated (minimum 15 orgs)
- [ ] Judicial branch organizations populated (minimum 120 orgs)
- [ ] Federal judges data available (conditional - see below)

### Federal Judges Conditional AC (UI-1.11 Outcome)

**If FJC API is viable:**
- [ ] Federal judges data imported (~870 Article III judges)
- [ ] `/factbase/people/federal-judges` page displays judge data
- [ ] Filters work: by court level, circuit, status

**If FJC API is NOT viable:**
- [ ] Federal Judges menu item displays "Coming Soon" badge
- [ ] Clicking shows placeholder page explaining data is pending
- [ ] Alternative data source documented for future epic

### Quality Gates

- [ ] All new components have TypeScript types
- [ ] Shared sidebar components work for both admin and public
- [ ] Responsive breakpoints match admin sidebar behavior
- [ ] Page descriptions reviewed and approved by Product Owner
- [ ] Navigation tested on Chrome, Firefox, Safari, Edge
- [ ] Accessibility: keyboard navigation works throughout

---

## Definition of Done

The epic is complete when:

- [ ] All 12 stories have status "Done"
- [ ] All epic-level acceptance criteria met
- [ ] All quality gates passed
- [ ] Code reviewed and merged to main branch
- [ ] No P1/P2 bugs open against epic stories
- [ ] PO sign-off obtained for each content page
- [ ] Cross-browser testing completed (Chrome, Firefox, Safari, Edge)
- [ ] Accessibility audit passed (keyboard navigation, screen reader basics)
- [ ] Documentation updated (if architectural changes made)

---

## Risks & Mitigations

| Risk | Severity | Likelihood | Mitigation |
|------|----------|------------|------------|
| Federal judges data unavailable (FJC API issues) | High | Medium | Launch without UI-1.7; add "Coming Soon" for Federal Judges only |
| Legislative/Judicial org data incomplete | Medium | Low | Curate minimum viable dataset (~15 legislative, ~120 judicial orgs) |
| Sidebar component extraction breaks admin | Medium | Low | Comprehensive testing; refactor incrementally |
| Menu complexity confuses users | Low | Low | User testing during development; iterate on structure |

---

## Change Log

| Date | Version | Changes |
|------|---------|---------|
| 2025-12-15 | 1.0 | Initial epic draft created (Sarah, PO) |
| 2025-12-15 | 1.1 | Architect review: Added architectural decisions, updated component structure, resolved open questions, added dependency graph, updated story technical notes (Winston, Architect) |
| 2025-12-15 | 1.2 | PO review: Added Success Metrics, enhanced Acceptance Criteria with specific targets, added Federal Judges conditional AC, added Definition of Done (Sarah, PO) |

---

## Approval

| Role | Name | Date | Status |
|------|------|------|--------|
| Product Owner | Sarah (PO Agent) | 2025-12-15 | Approved |
| Architect | Winston (Architect Agent) | 2025-12-15 | **Approved with Modifications** |
| Technical Lead | TBD | TBD | Pending |

---

*End of Epic Document*
