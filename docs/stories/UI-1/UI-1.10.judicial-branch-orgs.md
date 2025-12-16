# Story UI-1.10: Populate Judicial Branch Organizations

## Status

**Ready**

---

## Story

**As a** data administrator,
**I want** Judicial Branch organizations imported into the database,
**so that** users can browse federal courts and judicial administrative offices.

---

## Acceptance Criteria

1. CSV file created with Judicial Branch organizations
2. CSV includes Supreme Court, all Circuit Courts, and District Courts
3. CSV includes judicial administrative agencies
4. CSV follows existing GovernmentOrganization import format
5. All organizations have `branch` = `judicial`
6. Parent-child relationships are correctly established (Districts under Circuits)
7. At least 120 organizations are imported
8. Import is verified via API query

---

## Tasks / Subtasks

- [ ] Research Judicial Branch organizations (AC: 2, 3)
  - [ ] Document Supreme Court
  - [ ] Document all 13 Circuit Courts of Appeals
  - [ ] Document all 94 District Courts
  - [ ] Document specialized courts (Bankruptcy, Tax, etc.)
  - [ ] Document administrative agencies (AO, FJC, etc.)

- [ ] Create CSV file (AC: 1, 4, 5, 6, 7)
  - [ ] Create `data/judicial-branch-orgs.csv`
  - [ ] Include required columns per import schema
  - [ ] Set `branch` = `judicial` for all rows
  - [ ] Define Circuit → District parent relationships

- [ ] Import organizations (AC: 4)
  - [ ] Use existing admin import endpoint or direct CSV import
  - [ ] Verify no duplicate entries

- [ ] Verify import (AC: 8)
  - [ ] Query `/api/government-organizations?branch=judicial`
  - [ ] Verify count >= 120
  - [ ] Verify hierarchy is correct

---

## Dev Notes

### Judicial Branch Organization Structure

```
Federal Judiciary
├── Supreme Court of the United States
├── Courts of Appeals (13)
│   ├── 1st Circuit (Boston)
│   ├── 2nd Circuit (New York)
│   ├── 3rd Circuit (Philadelphia)
│   ├── 4th Circuit (Richmond)
│   ├── 5th Circuit (New Orleans)
│   ├── 6th Circuit (Cincinnati)
│   ├── 7th Circuit (Chicago)
│   ├── 8th Circuit (St. Louis)
│   ├── 9th Circuit (San Francisco)
│   ├── 10th Circuit (Denver)
│   ├── 11th Circuit (Atlanta)
│   ├── D.C. Circuit (Washington)
│   └── Federal Circuit (Washington)
├── District Courts (94)
│   └── (organized by state/territory under circuits)
├── Bankruptcy Courts
├── Specialized Courts
│   ├── U.S. Court of International Trade
│   ├── U.S. Court of Federal Claims
│   └── U.S. Tax Court
└── Administrative Agencies
    ├── Administrative Office of the U.S. Courts (AO)
    ├── Federal Judicial Center (FJC)
    └── U.S. Sentencing Commission
```

### Courts of Appeals (13 total)

| Circuit | Headquarters | States/Territories Covered |
|---------|--------------|---------------------------|
| 1st | Boston | ME, MA, NH, RI, PR |
| 2nd | New York | CT, NY, VT |
| 3rd | Philadelphia | DE, NJ, PA, VI |
| 4th | Richmond | MD, NC, SC, VA, WV |
| 5th | New Orleans | LA, MS, TX |
| 6th | Cincinnati | KY, MI, OH, TN |
| 7th | Chicago | IL, IN, WI |
| 8th | St. Louis | AR, IA, MN, MO, NE, ND, SD |
| 9th | San Francisco | AK, AZ, CA, HI, ID, MT, NV, OR, WA, Guam, N. Mariana |
| 10th | Denver | CO, KS, NM, OK, UT, WY |
| 11th | Atlanta | AL, FL, GA |
| D.C. | Washington | DC |
| Federal | Washington | Nationwide (patent, trade, claims) |

### District Courts (94 total)

Each state has 1-4 districts. Example for California:
- Northern District of California (San Francisco)
- Eastern District of California (Sacramento)
- Central District of California (Los Angeles)
- Southern District of California (San Diego)

### CSV Format

```csv
officialName,shortName,acronym,branch,orgType,parentOfficialName,websiteUrl,jurisdictionArea,active
"Federal Judiciary","Federal Courts","","judicial","JUDICIAL_SYSTEM","","https://uscourts.gov","",true
"Supreme Court of the United States","Supreme Court","SCOTUS","judicial","COURT","Federal Judiciary","https://supremecourt.gov","",true
"U.S. Court of Appeals for the 9th Circuit","9th Circuit","","judicial","COURT","Federal Judiciary","https://ca9.uscourts.gov","AK,AZ,CA,HI,ID,MT,NV,OR,WA",true
"U.S. District Court for the Northern District of California","N.D. Cal.","","judicial","COURT","U.S. Court of Appeals for the 9th Circuit","https://cand.uscourts.gov","CA",true
...
```

### Data Sources

- **US Courts Official**: https://www.uscourts.gov/about-federal-courts/court-website-links
- **Court Locator**: https://www.uscourts.gov/federal-court-finder/search

### Import Endpoint

```
POST /api/government-organizations/import/csv
Content-Type: multipart/form-data

file: judicial-branch-orgs.csv
```

### Expected Count

| Category | Count |
|----------|-------|
| Supreme Court | 1 |
| Circuit Courts | 13 |
| District Courts | 94 |
| Bankruptcy Courts | ~90 |
| Specialized Courts | 3 |
| Administrative | 3+ |
| **Minimum Total** | **120+** |

Note: For MVP, include Supreme Court, all Circuits, and all Districts. Bankruptcy courts can be added later.

---

## Testing

### Manual Verification Steps

1. Import CSV via admin UI or API
2. Query API for judicial branch orgs
3. Verify count: `totalElements >= 120`
4. Verify hierarchy: Districts have Circuit as parent
5. Check `/factbase/organizations/judicial` displays data (after UI-1.8)

### Data Validation

- All orgs have `branch` = `judicial`
- Supreme Court has no parent (or Federal Judiciary as parent)
- Circuit Courts have Federal Judiciary as parent
- District Courts have their Circuit as parent
- Website URLs are valid

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
