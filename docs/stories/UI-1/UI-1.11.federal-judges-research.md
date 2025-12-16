# Story UI-1.11: Federal Judges Data Research & Import

## Status

**In Progress** - CRITICAL PATH (Research Complete, Implementation In Progress)

---

## Story

**As a** data administrator,
**I want** to research and implement a data source for federal judges,
**so that** users can browse information about judges serving on federal courts.

---

## Critical Path Notice

This story is on the **critical path** because:
- UI-1.7 (Federal Judges Page) is **BLOCKED** until this completes
- Research outcome may affect story point estimates
- If no viable data source exists, UI-1.7 may be descoped

**Recommendation:** Start this story early in the sprint.

---

## Acceptance Criteria

1. Research completed on Federal Judicial Center (FJC) API
2. Document API capabilities, rate limits, and data format
3. Determine if FJC API meets requirements (name, court, dates, status)
4. If viable: Implement backend service to fetch and store judge data
5. If viable: Create API endpoint `/api/judges` with filtering
6. If viable: Import current federal judges into database
7. If not viable: Document alternative options and recommend next steps
8. Decision documented in story completion notes

---

## Tasks / Subtasks

- [ ] Research FJC Biographical Directory (AC: 1, 2)
  - [ ] Access https://www.fjc.gov/history/judges
  - [ ] Explore API/data export options
  - [ ] Document available fields
  - [ ] Check for bulk download or API endpoint

- [ ] Evaluate data completeness (AC: 3)
  - [ ] Verify data includes: judge name, court, appointment date
  - [ ] Verify data includes: appointing president, status (active/senior/deceased)
  - [ ] Check data freshness (how often updated)
  - [ ] Document any gaps

- [ ] If FJC viable - Backend implementation (AC: 4, 5)
  - [ ] Create `FederalJudgesClient` service
  - [ ] Create `Judge` model or extend `Person` model
  - [ ] Implement data fetching and parsing
  - [ ] Create database migration if needed
  - [ ] Create `/api/judges` endpoint with filters

- [ ] If FJC viable - Data import (AC: 6)
  - [ ] Import all current (active + senior) federal judges
  - [ ] Verify count matches official numbers (~870 Article III judges)
  - [ ] Verify data quality

- [ ] If FJC not viable (AC: 7)
  - [ ] Document why FJC doesn't work
  - [ ] Research alternatives: Ballotpedia, manual curation, court websites
  - [ ] Recommend path forward
  - [ ] Update UI-1.7 status accordingly

- [ ] Document decision (AC: 8)
  - [ ] Update this story with findings
  - [ ] Update UI-1.7 blocked status if resolved
  - [ ] Communicate to team

---

## Dev Notes

### Federal Judicial Center (FJC) - Primary Option

**Website:** https://www.fjc.gov/history/judges

**Biographical Directory:**
- Historical data since 1789
- Includes all Article III federal judges
- Fields: name, birth/death, court, dates of service, appointing president

**Potential Data Access:**
1. **Web scraping** (less preferred)
2. **Bulk download** - Check for CSV/JSON export
3. **API** - Check developer documentation

### Expected Judge Data Model

```java
// Option A: Extend Person model
@Entity
@Table(name = "persons")
public class Person {
    // ... existing fields ...

    // New judicial fields
    private String courtName;
    private String courtLevel;        // SUPREME, APPEALS, DISTRICT
    private String circuit;
    private String appointingPresident;
    private LocalDate commissionDate;
    private LocalDate serviceEnd;
    private String judicialStatus;    // ACTIVE, SENIOR, DECEASED, RETIRED
}

// Option B: Separate Judge entity
@Entity
@Table(name = "judges")
public class Judge {
    @Id
    private UUID id;

    @OneToOne
    private Person person;

    private String courtName;
    // ... judicial-specific fields
}
```

### Expected API Endpoint

```
GET /api/judges
Query params:
  - page, size (pagination)
  - courtLevel: SUPREME | APPEALS | DISTRICT
  - circuit: 1-11, DC, FEDERAL
  - status: ACTIVE | SENIOR | ALL
  - search: name search

Response: Page<JudgeDTO>
```

### Current Federal Judge Counts (Approximate)

| Court | Authorized | Active |
|-------|------------|--------|
| Supreme Court | 9 | 9 |
| Courts of Appeals | 179 | ~170 |
| District Courts | 673 | ~650 |
| **Total** | **861** | **~830** |

Plus senior judges (semi-retired): ~400+

### Alternative Data Sources

If FJC doesn't work:

1. **Ballotpedia** - https://ballotpedia.org/Federal_judges
   - Comprehensive but may require scraping
   - Terms of service check needed

2. **US Courts Official** - https://www.uscourts.gov
   - Has judge directories per court
   - No unified API

3. **Manual Curation** - Senate confirmations
   - Labor intensive
   - Harder to keep current

4. **Wikipedia** - Lists of federal judges
   - Accuracy concerns
   - Good for validation

### Risk Assessment

| Risk | Likelihood | Impact | Mitigation |
|------|------------|--------|------------|
| FJC has no API | Medium | High | Explore bulk download, scraping |
| Data incomplete | Low | Medium | Supplement with other sources |
| Rate limiting | Medium | Low | Implement caching, batch imports |
| Legal/TOS issues | Low | High | Review terms before scraping |

---

## Research Output Template

### FJC Research Findings (Completed 2025-12-15)

**API Available:** No dedicated REST API

**Bulk Download:** Yes - CSV and Excel formats
- Direct download URL: https://www.fjc.gov/sites/default/files/history/judges.csv
- Export page: https://www.fjc.gov/history/judges/biographical-directory-article-iii-federal-judges-export

**Data Format:**
- Flat file (judges.csv) with 288 columns
- Also available in relational format with separate CSVs per category

**Data Fields Available:**
- [x] Judge name (First, Middle, Last, Suffix)
- [x] Court name
- [x] Court level (Court Type field: Supreme, Appeals, District, etc.)
- [x] Circuit (embedded in Court Name)
- [x] Appointment date (Commission Date, Confirmation Date)
- [x] Appointing president
- [x] Status (Termination field + Senior Status Date)
- [x] Service end date (Termination Date)
- [x] Additional: Birth/death dates, gender, race/ethnicity
- [x] Additional: Education (up to 5 schools)
- [x] Additional: Professional career history
- [x] Additional: ABA Rating, Senate vote counts

**Data Freshness:** Updated regularly by FJC (appears current to present)

**Access Method Recommended:** Direct CSV download + periodic sync

**Implementation Estimate:** 5 story points (for backend service + API endpoint)

**Recommendation:** PROCEED

**Sources:**
- [FJC Biographical Directory Export](https://www.fjc.gov/history/judges/biographical-directory-article-iii-federal-judges-export)
- [FJC Judges Search](https://www.fjc.gov/history/judges)

---

## Testing

### Research Validation
- Verify data source contains expected information
- Test API/download with sample queries
- Validate data matches official court rosters

### Implementation Testing (if proceeding)
- Unit tests for FederalJudgesClient
- Integration tests for /api/judges endpoint
- Verify filter functionality
- Verify pagination

---

## Change Log

| Date | Version | Description | Author |
|------|---------|-------------|--------|
| 2025-12-15 | 1.0 | Initial story creation | Winston (Architect) |
| 2025-12-15 | 1.1 | Research complete - FJC bulk CSV available, PROCEED decision | James (Dev Agent) |

---

## Dev Agent Record

### Agent Model Used
Claude Opus 4.5 (claude-opus-4-5-20251101)

### Debug Log References
N/A - Research phase complete

### Completion Notes List
1. **Research Phase Complete (2025-12-15):**
   - FJC Biographical Directory provides comprehensive data via bulk CSV download
   - Direct URL: https://www.fjc.gov/sites/default/files/history/judges.csv
   - 288 columns available including all required fields
   - No API rate limits (bulk download)
   - Decision: PROCEED with implementation

### File List
- `data/judicial-branch-orgs.csv` - Created (124 judicial organizations)
- `data/legislative-branch-orgs.csv` - Created (22 legislative organizations)
- `backend/src/main/java/org/newsanalyzer/dto/JudgeDTO.java` - Created
- `backend/src/main/java/org/newsanalyzer/dto/FjcJudgeCsvRecord.java` - Created
- `backend/src/main/java/org/newsanalyzer/dto/FjcImportResult.java` - Created
- `backend/src/main/java/org/newsanalyzer/service/FjcCsvImportService.java` - Created
- `backend/src/main/java/org/newsanalyzer/service/JudgeService.java` - Created
- `backend/src/main/java/org/newsanalyzer/controller/JudgeController.java` - Created
- `backend/src/main/java/org/newsanalyzer/model/DataSource.java` - Modified (added FJC)
- `backend/src/main/java/org/newsanalyzer/repository/PersonRepository.java` - Modified
- `backend/src/main/java/org/newsanalyzer/repository/GovernmentPositionRepository.java` - Modified
- `backend/src/main/java/org/newsanalyzer/repository/PositionHoldingRepository.java` - Modified

---

## QA Results
*To be filled after QA review*
