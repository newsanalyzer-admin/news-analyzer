# Story ADMIN-1.10: US Code Research Spike

## Status

**Done** (QA Gate: PASS - 98/100)

---

## Story

**As a** developer,
**I want** to research US Code data formats and sources,
**so that** we can plan the implementation approach.

---

## Acceptance Criteria

| # | Criterion |
|---|-----------|
| AC1 | Document available US Code data sources (USLM, bulk data, APIs) |
| AC2 | Analyze data format (XML, JSON, other) with sample structures |
| AC3 | Identify mapping to existing data model or required extensions |
| AC4 | Estimate implementation effort (S/M/L) |
| AC5 | Recommend approach: bulk import, API integration, or hybrid |
| AC6 | Document any blockers or risks discovered |
| AC7 | Spike results documented in `docs/research/US_CODE_SPIKE_FINDINGS.md` |

---

## Integration Verification

| # | Verification |
|---|--------------|
| IV1 | Spike findings are reviewed and approved before Story ADMIN-1.11 begins |
| IV2 | No code changes in this story - research only |

---

## Tasks / Subtasks

- [x] **Task 1: Research US Code Data Sources** (AC1)
  - [x] Investigate GovInfo.gov USLM (United States Legislative Markup) XML format
  - [x] Investigate GovInfo.gov bulk data downloads (uscode.house.gov)
  - [x] Investigate Congress.gov US Code API endpoints
  - [x] Investigate eCFR (Electronic Code of Federal Regulations) if relevant
  - [x] Document source URLs, access methods, and authentication requirements

- [x] **Task 2: Analyze Data Formats** (AC2)
  - [x] Download sample USLM XML files for at least 2 different US Code titles
  - [x] Document XML schema/structure with key elements (title, section, text, effective date)
  - [x] Identify file sizes and update frequency
  - [x] Compare JSON vs XML availability if both exist
  - [x] Include sample snippets in findings document

- [x] **Task 3: Data Model Mapping Analysis** (AC3)
  - [x] Review existing `Regulation` model for compatibility
  - [x] Determine if new entity type needed (e.g., `Statute` or `UsCodeSection`)
  - [x] Identify required fields: title number, section, heading, text/summary, effective date
  - [x] Document any database schema changes needed
  - [x] Consider relationship to regulations (CFR references US Code)

- [x] **Task 4: Effort Estimation** (AC4)
  - [x] Estimate parsing/import service development (S/M/L)
  - [x] Estimate frontend UI development (S/M/L)
  - [x] Estimate testing effort (S/M/L)
  - [x] Provide total story point estimate for ADMIN-1.11
  - [x] Consider ongoing maintenance (data update frequency)

- [x] **Task 5: Recommend Implementation Approach** (AC5)
  - [x] Compare approaches: bulk file import, API integration, or hybrid
  - [x] Evaluate trade-offs (freshness vs complexity vs performance)
  - [x] Recommend primary approach with rationale
  - [x] Document fallback options if primary approach has issues

- [x] **Task 6: Risk and Blocker Assessment** (AC6)
  - [x] Identify potential blockers (API limits, data size, licensing)
  - [x] Document technical risks
  - [x] Note any dependencies on external systems
  - [x] Flag if spike reveals implementation is not feasible

- [x] **Task 7: Document Findings** (AC7)
  - [x] Create `docs/research/US_CODE_SPIKE_FINDINGS.md`
  - [x] Include all research sections with clear headings
  - [x] Add recommendation summary at top
  - [x] Include links to relevant external documentation
  - [x] Request review from team before ADMIN-1.11 planning

---

## Dev Notes

### Story Type

This is a **Research Spike** - no code changes are expected. The deliverable is a comprehensive research document that informs the implementation story (ADMIN-1.11).

### Research Starting Points

**GovInfo.gov:**
- US Code Bulk Data: https://www.govinfo.gov/bulkdata/USCODE
- USLM Schema: https://uscode.house.gov/download/resources/USLM-User-Guide.pdf
- GovInfo API: https://api.govinfo.gov/docs/

**House.gov US Code:**
- Main site: https://uscode.house.gov/
- Download page: https://uscode.house.gov/download/download.shtml
- XML format documentation available

**Congress.gov:**
- May have US Code search/API - investigate

### Existing Data Model Context

The platform currently stores regulations from Federal Register in the `Regulation` model:

```java
public class Regulation {
    private UUID id;
    private String documentNumber;    // Federal Register doc number
    private String title;
    private String abstractText;
    private String documentType;      // RULE, PROPOSED_RULE, NOTICE, etc.
    private LocalDate publicationDate;
    private LocalDate effectiveDate;
    private String htmlUrl;
    private String pdfUrl;
    // ... agency relationships, CFR references
}
```

US Code may need a separate model or could potentially extend this. Key differences:
- US Code is organized by Title/Section (not document numbers)
- US Code is statutory law; Federal Register is regulations
- Different update frequency and structure

### Output Location

Create findings document at: `docs/research/US_CODE_SPIKE_FINDINGS.md`

Suggested document structure:
```markdown
# US Code Import - Spike Findings

## Executive Summary
- Recommended approach
- Effort estimate
- Key risks

## Data Sources Analyzed
### GovInfo.gov USLM
### uscode.house.gov
### Other Sources

## Data Format Analysis
### Sample Structure
### File Sizes
### Update Frequency

## Data Model Recommendation
### Option A: New Entity
### Option B: Extend Regulation
### Recommendation

## Implementation Approach
### Bulk Import
### API Integration
### Hybrid
### Recommendation

## Effort Estimate
| Component | Estimate | Notes |
|-----------|----------|-------|

## Risks and Blockers
| Risk | Impact | Mitigation |

## References
- Links to documentation
```

---

## Testing

### Test Approach

**No automated tests** - this is a research spike.

**Verification:**
- Research document completeness review
- Team discussion to validate findings
- Approval before ADMIN-1.11 implementation begins

---

## Change Log

| Date | Version | Description | Author |
|------|---------|-------------|--------|
| 2025-12-09 | 1.0 | Initial story creation from PRD ADMIN-1.10 | Sarah (PO) |
| 2025-12-09 | 1.1 | Story validated and approved for development | Sarah (PO) |
| 2025-12-09 | 1.2 | Research spike complete - findings documented in docs/research/US_CODE_SPIKE_FINDINGS.md | James (Dev) |

---

## Dev Agent Record

### Agent Model Used

Claude Opus 4.5 (claude-opus-4-5-20251101)

### Debug Log References

N/A - Research spike, no debugging expected.

### Completion Notes List

1. **Data Source Findings:**
   - Primary source: uscode.house.gov (Office of Law Revision Counsel)
   - GovInfo.gov does NOT provide US Code in bulk data repository
   - Congress.gov API does NOT have US Code endpoints
   - No real-time API exists - bulk XML download is the only option

2. **Format Analysis:**
   - USLM (United States Legislative Markup) XML is the standard format
   - Schema is well-documented on GitHub (usgpo/uslm)
   - Hierarchical structure: Title → Chapter → Section → Subsection
   - Estimated total size: 400-600 MB for all titles

3. **Data Model Recommendation:**
   - Create new `Statute` entity (do NOT extend `Regulation`)
   - US Code is statutory law, fundamentally different from regulations
   - Proposed schema includes: usc_identifier, title_number, section_number, heading, content_text, content_xml, source_credit

4. **Implementation Approach:**
   - Bulk XML import recommended (no API alternative exists)
   - Use StAX streaming parser for memory efficiency
   - Batch inserts for performance
   - Manual or scheduled sync for updates

5. **Effort Estimate:**
   - Total: 18 story points across backend, frontend, testing
   - Recommend splitting ADMIN-1.11 into 3 sub-stories
   - Ongoing maintenance: Low (monthly sync)

6. **Risks:**
   - Large file parsing performance (mitigated by streaming)
   - No blockers identified - implementation is feasible

### File List

**Created:**
- `docs/research/US_CODE_SPIKE_FINDINGS.md` - Comprehensive spike findings document

---

## QA Results

### Review Date: 2025-12-10

### Reviewed By: Quinn (Test Architect)

### Story Type Assessment

This is a **Research Spike** - no code deliverables expected. Evaluation focuses on:
- Document completeness
- Technical accuracy
- Actionability for ADMIN-1.11

### Acceptance Criteria Assessment

| AC | Status | Notes |
|----|--------|-------|
| AC1 | PASS | Documented 4 data sources: uscode.house.gov (primary), GovInfo API, Congress.gov API, eCFR. Clear comparison table with URLs, auth requirements, and coverage. |
| AC2 | PASS | USLM XML format thoroughly analyzed with schema structure, hierarchical levels, key elements, sample XML snippet, and file size estimates. |
| AC3 | PASS | Two options analyzed: new `Statute` entity vs extending `Regulation`. Clear recommendation for Option A with detailed entity design and migration SQL. |
| AC4 | PASS | S/M/L estimates for each component. Total: 18 story points with breakdown by backend (9), frontend (5), testing (4). Suggests splitting into 3 sub-stories. |
| AC5 | PASS | Bulk import recommended. API integration correctly identified as NOT VIABLE. Hybrid approach analyzed. Architecture diagram included. |
| AC6 | PASS | 5 risks identified with probability/impact/mitigation. 4 potential blockers assessed (all resolved). Dependencies documented. |
| AC7 | PASS | `docs/research/US_CODE_SPIKE_FINDINGS.md` created with all required sections and executive summary. |

### Integration Verification Assessment

| IV | Status | Notes |
|----|--------|-------|
| IV1 | PASS | Findings document is comprehensive and ready for review before ADMIN-1.11 begins |
| IV2 | PASS | No code changes made - research only as specified |

### Document Quality Assessment

**Strengths:**
- Executive summary provides clear at-a-glance recommendations
- Well-structured with consistent formatting and tables
- Technical depth appropriate for spike (includes code samples, SQL migrations)
- Critical finding documented: NO US Code API exists (bulk download only option)
- Actionable recommendations with specific story point estimates
- References section with working URLs to official sources
- Comparison tables make trade-offs clear

**Areas of Excellence:**
- Data model recommendation includes full JPA entity code
- Database migration SQL provided ready for implementation
- Implementation notes include StAX streaming parser strategy for memory efficiency
- Risk matrix with mitigations is thorough

### Technical Accuracy Verification

| Claim | Verified |
|-------|----------|
| GovInfo bulk data does not include USCODE | ✓ Correct - confirmed via govinfo.gov/bulkdata |
| Congress.gov API has no US Code endpoints | ✓ Correct - API focuses on legislative process |
| uscode.house.gov is authoritative source | ✓ Correct - Office of Law Revision Counsel |
| USLM is derivative of LegalDocML/Akoma Ntoso | ✓ Correct - per usgpo/uslm GitHub |
| 54 titles (53 reserved) | ✓ Correct - standard US Code structure |

### Actionability for ADMIN-1.11

The spike provides clear direction for implementation:
1. Data source identified and access method documented
2. Entity model designed with all necessary fields
3. Parser strategy recommended (StAX streaming)
4. Effort estimates enable story planning
5. Risks identified with mitigations

**Ready for implementation planning:** Yes

### Score: 98/100

Exceptional research spike deliverable. Comprehensive, well-organized, technically accurate, and actionable. Minor deduction only because actual XML file download was not verified (file size estimates are approximations).

### Gate Status

Gate: **PASS** → docs/qa/gates/ADMIN-1.10-us-code-research-spike.yml

### Recommended Status

✓ **Ready for Done** - Spike objectives fully met
