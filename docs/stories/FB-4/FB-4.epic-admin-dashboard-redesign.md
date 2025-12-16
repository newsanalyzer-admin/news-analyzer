# Epic FB-4: Admin Dashboard Redesign

## Epic Overview

| Field | Value |
|-------|-------|
| **Epic ID** | FB-4 |
| **Epic Name** | Admin Dashboard Redesign |
| **Epic Type** | Feature / UI Enhancement |
| **Priority** | HIGH |
| **Status** | **Superseded by ADMIN-1** |
| **Created** | 2025-12-02 |
| **Updated** | 2025-12-12 |
| **Owner** | Sarah (PO) |
| **Depends On** | None |

> **SUPERSEDED (2025-12-12):** This draft epic was never formally approved. All planned functionality was implemented through the **ADMIN-1** story set (stories 1.1, 1.2, 1.3, 1.12). See [ADMIN-1 Epic](../ADMIN-1/ADMIN-1.epic-admin-dashboard-improvements.md) for completion details.

## Executive Summary

Restructure the NewsAnalyzer Admin Dashboard with a hierarchical sidebar navigation that supports current factbase management and scales for future features (Article Search, Workflows, Entity Extraction, Reporting). The current flat card-based layout is un-intuitive and doesn't accommodate the platform's growth trajectory.

## Business Value

### Why This Epic Matters

1. **Scalable Architecture** - Supports 2+ years of feature growth without UI redesign
2. **Improved Usability** - Hierarchical navigation reduces cognitive load
3. **Unified Experience** - Consistent patterns across all admin functions
4. **Future-Ready** - Placeholder structure for Article Search, Workflows, etc.

### Success Metrics

| Metric | Target | Measurement |
|--------|--------|-------------|
| Navigation Depth | ≤2 clicks | Max clicks to any function |
| Admin Task Completion | <30 seconds | Time to initiate common tasks |
| Feature Discoverability | 100% | All features visible in nav |

## Scope

### In Scope

- Sidebar navigation component with collapsible sections
- Main menu: Factbase, Article Search (placeholder), Workflows (placeholder)
- Factbase submenu: Government Entities, Federal Laws & Regulations
- Government Entities breakdown: Executive, Legislative, Judicial branches
- Responsive design (sidebar collapses on mobile)
- Breadcrumb navigation for context
- Preserve all existing admin functionality

### Out of Scope

- Article Search implementation (placeholder only)
- Workflows implementation (placeholder only)
- Non-government entity categories (Universities, Charities, etc.)
- Mobile-specific admin app

## Architecture

### Navigation Structure

```
Admin Dashboard
├── Factbase
│   ├── Government Entities
│   │   ├── Executive Branch
│   │   │   ├── Agencies & Departments
│   │   │   ├── Positions & Appointees
│   │   │   └── PLUM Import
│   │   ├── Legislative Branch
│   │   │   ├── Congress Structure
│   │   │   ├── Members
│   │   │   ├── Committees
│   │   │   └── Legislators Sync
│   │   └── Judicial Branch
│   │       ├── Courts
│   │       └── Judges
│   └── Federal Laws & Regulations
│       ├── Regulations (Federal Register)
│       └── US Code (future)
├── Article Search (placeholder)
└── Workflows (placeholder)
```

### Component Architecture

```
AdminLayout
├── AdminSidebar
│   ├── SidebarSection (collapsible)
│   │   ├── SidebarItem
│   │   └── SidebarSubItem
│   └── SidebarFooter (user info, logout)
├── AdminBreadcrumb
└── AdminContent (page content area)
```

### Technology

| Component | Technology |
|-----------|------------|
| Sidebar | Shadcn/UI + custom components |
| State | URL-based (Next.js App Router) |
| Icons | Lucide React |
| Animations | Tailwind CSS transitions |

## Stories

### Story Summary

| ID | Story | Priority | Estimate | Dependencies |
|----|-------|----------|----------|--------------|
| FB-4.1 | Sidebar Navigation Component | P0 | 1 sprint | None |
| FB-4.2 | Factbase Menu Structure | P0 | 0.5 sprint | FB-4.1 |
| FB-4.3 | Government Entities Pages | P0 | 1 sprint | FB-4.2 |
| FB-4.4 | Migrate Existing Admin Features | P1 | 0.5 sprint | FB-4.3 |
| FB-4.5 | Placeholder Pages (Article Search, Workflows) | P2 | 0.25 sprint | FB-4.2 |

### Dependency Graph

```
FB-4.1 (Sidebar Component)
    │
    ▼
FB-4.2 (Menu Structure)
    │
    ├──────────────────┐
    ▼                  ▼
FB-4.3 (Gov Entities)  FB-4.5 (Placeholders)
    │
    ▼
FB-4.4 (Migrate Existing)
```

## Acceptance Criteria (Epic Level)

1. **Sidebar Navigation**: Collapsible sidebar with hierarchical menu
2. **Factbase Section**: Fully navigable Government Entities structure
3. **Branch Organization**: Executive, Legislative, Judicial clearly separated
4. **Existing Features**: All current admin functions accessible from new nav
5. **Placeholders**: Article Search and Workflows show "Coming Soon" pages
6. **Responsive**: Sidebar collapses to hamburger on mobile
7. **Breadcrumbs**: User always knows current location

## Risks & Mitigations

| Risk | Impact | Probability | Mitigation |
|------|--------|-------------|------------|
| Migration breaks existing features | High | Low | Comprehensive testing, phased rollout |
| Over-engineering sidebar | Medium | Medium | Keep it simple, iterate later |
| Scope creep to build placeholders | Low | Medium | Strict "Coming Soon" only |

## Definition of Done

- [ ] All 5 stories completed and merged
- [ ] Sidebar navigation functional on all screen sizes
- [ ] All existing admin features migrated
- [ ] Breadcrumb navigation working
- [ ] Placeholder pages for future sections
- [ ] UI consistent with existing design system
- [ ] Accessibility: keyboard navigation works

## Related Documentation

- [Project Brief](../analysis/ADMIN_DASHBOARD_IMPROVEMENTS_PROJECT_BRIEF.md)
- [Current Admin Code](../../frontend/src/app/admin/)

## Change Log

| Date | Version | Description | Author |
|------|---------|-------------|--------|
| 2025-12-02 | 1.0 | Initial epic creation | Mary (Analyst) |

## Approval

| Role | Name | Date | Status |
|------|------|------|--------|
| Product Owner | _Pending_ | _Pending_ | _Pending_ |
| Architect | _Pending_ | _Pending_ | _Pending_ |

---

*End of Epic Document*
