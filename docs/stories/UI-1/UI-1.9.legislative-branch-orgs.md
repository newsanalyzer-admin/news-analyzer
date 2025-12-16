# Story UI-1.9: Populate Legislative Branch Organizations

## Status

**Ready**

---

## Story

**As a** data administrator,
**I want** Legislative Branch organizations imported into the database,
**so that** users can browse Congressional support agencies and offices.

---

## Acceptance Criteria

1. CSV file created with Legislative Branch organizations
2. CSV includes all major Congressional support agencies
3. CSV follows existing GovernmentOrganization import format
4. Organizations are imported via existing CSV import endpoint
5. All organizations have `branch` = `legislative`
6. Parent-child relationships are correctly established
7. At least 15 organizations are imported
8. Import is verified via API query

---

## Tasks / Subtasks

- [ ] Research Legislative Branch organizations (AC: 2)
  - [ ] Document all Congressional support agencies
  - [ ] Identify parent-child relationships
  - [ ] Gather official names, acronyms, websites

- [ ] Create CSV file (AC: 1, 3, 5, 6, 7)
  - [ ] Create `data/legislative-branch-orgs.csv`
  - [ ] Include required columns per import schema
  - [ ] Set `branch` = `legislative` for all rows
  - [ ] Define parent relationships where applicable

- [ ] Import organizations (AC: 4)
  - [ ] Use existing admin import endpoint or direct CSV import
  - [ ] Verify no duplicate entries

- [ ] Verify import (AC: 8)
  - [ ] Query `/api/government-organizations?branch=legislative`
  - [ ] Verify count matches CSV rows
  - [ ] Verify hierarchy is correct

---

## Dev Notes

### Legislative Branch Organizations to Include

| Organization | Acronym | Parent | Website |
|--------------|---------|--------|---------|
| United States Congress | — | — | congress.gov |
| United States Senate | — | Congress | senate.gov |
| United States House of Representatives | — | Congress | house.gov |
| Government Accountability Office | GAO | Congress | gao.gov |
| Congressional Budget Office | CBO | Congress | cbo.gov |
| Library of Congress | LOC | Congress | loc.gov |
| Congressional Research Service | CRS | LOC | loc.gov/crsinfo |
| Government Publishing Office | GPO | Congress | gpo.gov |
| United States Capitol Police | USCP | Congress | uscp.gov |
| Office of the Architect of the Capitol | AOC | Congress | aoc.gov |
| Office of Congressional Workplace Rights | OCWR | Congress | ocwr.gov |
| Stennis Center for Public Service | — | Congress | stennis.gov |
| Office of Technology Assessment (defunct) | OTA | Congress | — |
| Medicare Payment Advisory Commission | MedPAC | Congress | medpac.gov |
| Medicaid and CHIP Payment and Access Commission | MACPAC | Congress | macpac.gov |

### CSV Format

Based on existing `GovernmentOrganization` import schema:

```csv
officialName,shortName,acronym,branch,orgType,parentOfficialName,websiteUrl,active
"United States Congress","Congress","","legislative","LEGISLATIVE_BODY","","https://congress.gov",true
"United States Senate","Senate","","legislative","LEGISLATIVE_BODY","United States Congress","https://senate.gov",true
"Government Accountability Office","GAO","GAO","legislative","AGENCY","United States Congress","https://gao.gov",true
...
```

### Import Endpoint

```
POST /api/government-organizations/import/csv
Content-Type: multipart/form-data

file: legislative-branch-orgs.csv
```

Alternatively, use admin UI at `/admin/factbase/executive/agencies` (CSV Import button).

### Parent Relationship Resolution

The import should resolve parent organizations by name. Ensure:
1. Parent orgs are listed before children in CSV, OR
2. Import service handles forward references

### Verification Query

```bash
curl "http://localhost:8080/api/government-organizations?branch=legislative"
```

Expected: 15+ organizations with correct hierarchy.

---

## Testing

### Manual Verification Steps

1. Import CSV via admin UI or API
2. Query API for legislative branch orgs
3. Verify count: `totalElements >= 15`
4. Verify hierarchy: GAO, CBO, LOC have parent = Congress
5. Check `/factbase/organizations/legislative` displays data (after UI-1.8)

### Data Validation

- All orgs have `branch` = `legislative`
- All orgs have `active` = `true` (except OTA)
- Website URLs are valid
- No duplicate organizations

---

## Change Log

| Date | Version | Description | Author |
|------|---------|-------------|--------|
| 2025-12-15 | 1.0 | Initial story creation | Winston (Architect) |

---

## Dev Agent Record

### Agent Model Used
*To be filled during implementation*

### Debug Log References
*To be filled during implementation*

### Completion Notes List
*To be filled during implementation*

### File List
*To be filled during implementation*

---

## QA Results
*To be filled after QA review*
