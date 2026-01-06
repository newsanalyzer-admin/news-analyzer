# Technical Debt Tracking

This document tracks known technical debt items that need to be addressed.

## Active Items

### TD-001: Remove Legacy Admin Factbase Pages

**Status:** PENDING
**Created:** 2026-01-06
**Priority:** LOW (cleanup after migration)
**Owner:** TBD

**Description:**
The admin section has been reorganized from `/admin/factbase/` to `/admin/knowledge-base/government/` to mirror the public Knowledge Base structure. The legacy factbase pages should be removed once the new admin pages have full CRUD functionality.

**Location:**
- `frontend/src/app/admin/factbase/` - Entire directory and subdirectories
- `frontend/src/components/admin/AdminSidebar.tsx` - Remove "Factbase (Legacy)" menu section

**Legacy Routes to Remove:**
```
/admin/factbase/executive/agencies
/admin/factbase/executive/positions
/admin/factbase/executive/govman
/admin/factbase/legislative/members
/admin/factbase/legislative/members/search
/admin/factbase/legislative/legislators-repo
/admin/factbase/legislative/committees
/admin/factbase/judicial/courts
/admin/factbase/regulations/federal-register
/admin/factbase/regulations/search
/admin/factbase/regulations/us-code
```

**New Structure (Placeholder Pages):**
```
/admin/knowledge-base/government/
├── executive/
│   ├── president/
│   ├── vice-president/
│   ├── eop/
│   ├── cabinet/
│   ├── independent-agencies/
│   └── corporations/
├── legislative/
│   ├── senate/
│   ├── house/
│   ├── support-services/
│   └── committees/
├── judicial/
│   ├── supreme-court/
│   ├── courts-of-appeals/
│   ├── district-courts/
│   └── specialized-courts/
└── us-code/
```

**Why Keep Legacy Pages:**
- Reference for implementing CRUD functionality in new pages
- Some features (search, imports) may be reusable
- Avoid breaking existing workflows during transition

**When to Delete:**
Delete legacy factbase pages when ALL of these conditions are met:
1. All new admin KB pages have full CRUD functionality
2. All data import features are migrated (GOVMAN, Congress.gov search, etc.)
3. All tests updated to use new routes
4. Product Owner approval

**Related:**
- UI-6 Epic (Public KB Executive Branch)
- AdminSidebar.tsx (marked "Legacy" in menu label)

---

## Resolved Items

*(None yet)*

---

## How to Add New Items

Use this template:
```markdown
### TD-XXX: [Short Title]

**Status:** PENDING | IN_PROGRESS | RESOLVED
**Created:** YYYY-MM-DD
**Priority:** LOW | MEDIUM | HIGH | CRITICAL
**Owner:** [Name or TBD]

**Description:**
[What is the technical debt and why does it exist?]

**Location:**
[File paths or areas of codebase affected]

**Resolution Plan:**
[Steps to resolve this debt]

**When to Address:**
[Conditions that trigger cleanup]
```
