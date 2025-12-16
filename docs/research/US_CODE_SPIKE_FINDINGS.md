# US Code Import - Spike Findings

**Story:** ADMIN-1.10
**Date:** 2025-12-09
**Author:** James (Dev Agent)
**Status:** Complete

---

## Executive Summary

| Aspect | Finding |
|--------|---------|
| **Recommended Approach** | Bulk XML import from uscode.house.gov |
| **Primary Data Source** | Office of the Law Revision Counsel (OLRC) - uscode.house.gov |
| **Data Format** | USLM XML (United States Legislative Markup) |
| **Effort Estimate** | Medium (8-13 story points) |
| **Key Risk** | Large file sizes (~500MB total), complex hierarchical parsing |
| **Feasibility** | Fully feasible with existing tech stack |

### Key Recommendation

Create a new `Statute` entity model (do NOT extend the existing `Regulation` model). US Code is statutory law with a fundamentally different structure than Federal Register regulations.

---

## Data Sources Analyzed

### 1. uscode.house.gov (PRIMARY - RECOMMENDED)

| Attribute | Value |
|-----------|-------|
| **Maintainer** | Office of the Law Revision Counsel (OLRC), U.S. House of Representatives |
| **URL** | https://uscode.house.gov/download/download.shtml |
| **Formats** | XML (USLM), XHTML, PDF, PCC |
| **Coverage** | 54 titles (Title 53 is reserved) |
| **Update Frequency** | Per Public Law enactment ("release points") |
| **Current Release** | Public Law 119-46 (12/02/2025) |
| **Authentication** | None required |
| **Licensing** | Public domain (U.S. Government work) |
| **Historical Data** | Available back to 1994 |

**Advantages:**
- Authoritative source (official House.gov)
- Clean USLM XML format with schema
- No API key or rate limits
- Full bulk download available
- Well-documented schema

**Disadvantages:**
- No API - bulk download only
- Large file sizes
- Manual monitoring for updates required

### 2. GovInfo.gov API

| Attribute | Value |
|-----------|-------|
| **URL** | https://api.govinfo.gov/docs/ |
| **Authentication** | API key required (free from api.data.gov) |
| **Rate Limits** | Not explicitly documented |
| **US Code Support** | Limited - primarily for cross-references |

**Finding:** GovInfo API does NOT provide US Code bulk data. The `/bulkdata` endpoint includes:
- Congressional Bills
- Federal Register
- Code of Federal Regulations (CFR)
- Statutes at Large (beta USLM)
- Public/Private Laws

**USCODE is NOT in the bulk data repository.** GovInfo only provides US Code *references* through the Related Documents API.

### 3. Congress.gov API

| Attribute | Value |
|-----------|-------|
| **URL** | https://api.congress.gov |
| **Authentication** | API key required |
| **Rate Limits** | 5,000 requests/hour |
| **US Code Support** | None - focused on legislative process |

**Finding:** Congress.gov API does NOT provide US Code data. Available endpoints:
- Bills, Amendments, Congresses
- Members, Committees
- Summaries, Reports
- Nominations, Treaties

No US Code search or retrieval endpoint exists.

### 4. eCFR (Electronic Code of Federal Regulations)

| Attribute | Value |
|-----------|-------|
| **URL** | https://www.ecfr.gov |
| **Relevance** | Low - CFR is regulations, not statutes |

**Finding:** Not relevant for US Code. CFR contains regulations (administrative law), not statutes (Congressional law). The platform already has Federal Register integration for regulations.

---

## Data Format Analysis

### USLM XML Schema

**Schema:** United States Legislative Markup (USLM)
**Version:** 2.0.x (current), 2.1.0 (latest)
**Base Standard:** Derivative of LegalDocML (Akoma Ntoso) international standard
**GitHub:** https://github.com/usgpo/uslm

### Document Structure

```
US Code Document
├── metadata (Dublin Core)
│   ├── dc:title
│   ├── dc:identifier
│   ├── dc:date
│   └── ...
├── main
│   └── title
│       ├── num (e.g., "TITLE 5")
│       ├── heading (e.g., "GOVERNMENT ORGANIZATION AND EMPLOYEES")
│       ├── chapter* (optional)
│       │   ├── num
│       │   ├── heading
│       │   └── section*
│       └── section*
│           ├── num (e.g., "§ 101")
│           ├── heading
│           ├── subsection*
│           │   ├── num (e.g., "(a)")
│           │   ├── paragraph*
│           │   └── content
│           └── content
├── appendices (optional)
└── signatures (not used for US Code)
```

### Hierarchical Levels

| Level | Prefix | Example |
|-------|--------|---------|
| Title | `t` | `/us/usc/t5` |
| Subtitle | `stitle` | `/us/usc/t26/stitleA` |
| Chapter | `ch` | `/us/usc/t5/ch1` |
| Subchapter | `sch` | `/us/usc/t5/ch1/schI` |
| Part | `pt` | `/us/usc/t5/ch1/ptA` |
| Section | `s` | `/us/usc/t5/s101` |
| Subsection | `ss` | `/us/usc/t5/s101/a` |
| Paragraph | `p` | `/us/usc/t5/s101/a/1` |

### Key XML Elements

| Element | Purpose |
|---------|---------|
| `<num>` | Numeric designator |
| `<heading>` | Section/chapter title |
| `<content>` | Text content (mixed) |
| `<ref>` | Cross-references |
| `<note>` | Editorial notes |
| `<sourceCredit>` | Public law citations |

### Sample XML Snippet

```xml
<section identifier="/us/usc/t5/s101" temporalId="s101">
  <num>§ 101</num>
  <heading>Executive departments</heading>
  <content>
    <p>The Executive departments are:</p>
    <list>
      <item>
        <num>(1)</num>
        <p>The Department of State.</p>
      </item>
      <item>
        <num>(2)</num>
        <p>The Department of the Treasury.</p>
      </item>
      <!-- ... -->
    </list>
  </content>
  <sourceCredit>(Pub. L. 89–554, Sept. 6, 1966, 80 Stat. 378.)</sourceCredit>
</section>
```

### File Sizes (Estimated)

| Item | Estimated Size |
|------|----------------|
| Single Title (small, e.g., Title 4) | ~500 KB |
| Single Title (large, e.g., Title 26 - Tax) | ~50-100 MB |
| All Titles Combined | ~400-600 MB |
| Individual Section | ~1-10 KB |

### Update Frequency

- **Release Point:** Each time a Public Law is enacted
- **Typical Frequency:** Multiple times per month during Congressional sessions
- **Notification:** No push notification; requires polling or manual check

---

## Data Model Recommendation

### Option A: New `Statute` Entity (RECOMMENDED)

Create a dedicated entity for US Code sections:

```java
@Entity
@Table(name = "statutes")
public class Statute {
    @Id
    private UUID id;

    // Unique identifier in USLM format
    @Column(name = "usc_identifier", unique = true, nullable = false)
    private String uscIdentifier;  // e.g., "/us/usc/t5/s101"

    // Hierarchical location
    @Column(name = "title_number", nullable = false)
    private Integer titleNumber;  // 1-54

    @Column(name = "title_name")
    private String titleName;  // e.g., "GOVERNMENT ORGANIZATION AND EMPLOYEES"

    @Column(name = "chapter_number")
    private String chapterNumber;

    @Column(name = "chapter_name")
    private String chapterName;

    @Column(name = "section_number", nullable = false)
    private String sectionNumber;  // e.g., "101"

    // Content
    @Column(name = "heading")
    private String heading;  // Section heading

    @Column(name = "content_text", columnDefinition = "TEXT")
    private String contentText;  // Plain text extract

    @Column(name = "content_xml", columnDefinition = "TEXT")
    private String contentXml;  // Original XML for rendering

    // Source citation
    @Column(name = "source_credit")
    private String sourceCredit;  // e.g., "Pub. L. 89–554..."

    // URLs
    @Column(name = "source_url")
    private String sourceUrl;  // uscode.house.gov link

    // Metadata
    @Column(name = "release_point")
    private String releasePoint;  // e.g., "119-46"

    @Column(name = "effective_date")
    private LocalDate effectiveDate;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
```

**Advantages:**
- Clean separation from regulations
- Optimized for statutory structure
- Supports hierarchical navigation
- Can store both XML and text

### Option B: Extend `Regulation` Entity (NOT RECOMMENDED)

Adding US Code to the existing `Regulation` model would require:
- New `documentType` values
- Additional nullable columns for title/section structure
- Complex logic to handle two different data types

**Disadvantages:**
- Semantic confusion (regulations ≠ statutes)
- Different search patterns needed
- Schema bloat
- Harder to maintain

### Recommendation: Option A

Create a new `Statute` entity. The data structures are fundamentally different:

| Aspect | Regulation (Federal Register) | Statute (US Code) |
|--------|------------------------------|-------------------|
| Identifier | Document number (e.g., "2024-12345") | USC citation (e.g., "/us/usc/t5/s101") |
| Hierarchy | Flat documents | Title → Chapter → Section |
| Content | Abstract + full text | Structured legal text |
| Updates | Daily publications | Per Public Law |
| Purpose | Administrative rules | Congressional law |

### Database Schema Changes

New migration required:

```sql
-- V22__create_statutes_table.sql

CREATE TABLE statutes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    usc_identifier VARCHAR(100) UNIQUE NOT NULL,
    title_number INTEGER NOT NULL,
    title_name VARCHAR(500),
    chapter_number VARCHAR(20),
    chapter_name VARCHAR(500),
    section_number VARCHAR(50) NOT NULL,
    heading VARCHAR(1000),
    content_text TEXT,
    content_xml TEXT,
    source_credit VARCHAR(500),
    source_url VARCHAR(500),
    release_point VARCHAR(20),
    effective_date DATE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Indexes
CREATE INDEX idx_statutes_title ON statutes(title_number);
CREATE INDEX idx_statutes_chapter ON statutes(title_number, chapter_number);
CREATE INDEX idx_statutes_section ON statutes(section_number);

-- Full-text search
CREATE INDEX idx_statutes_content_fts ON statutes
    USING gin(to_tsvector('english', content_text));
```

### Relationship to Regulations

Consider adding a cross-reference table in the future:

```sql
-- Future: Link CFR regulations to authorizing statutes
CREATE TABLE statute_regulation_refs (
    statute_id UUID REFERENCES statutes(id),
    regulation_id UUID REFERENCES regulations(id),
    reference_type VARCHAR(50),  -- 'AUTHORITY', 'CROSS_REF', etc.
    PRIMARY KEY (statute_id, regulation_id)
);
```

---

## Implementation Approach

### Option 1: Bulk File Import (RECOMMENDED)

**Process:**
1. Download XML zip from uscode.house.gov
2. Parse USLM XML files
3. Extract section data
4. Store in database

**Pros:**
- Complete data set
- No API limits
- Works offline
- Faster for initial load

**Cons:**
- Large download (~500MB)
- Manual update triggering
- Need to detect changes

### Option 2: API Integration

**NOT VIABLE** - No US Code API exists with bulk data.

### Option 3: Hybrid

**Process:**
1. Initial bulk import from XML
2. Future: Poll for new release points
3. Incremental XML diff updates

**This is effectively the same as Option 1** since no real-time API exists.

### Recommended Implementation

**Approach:** Bulk XML Import with scheduled update checks

```
┌─────────────────────┐
│  Admin UI Trigger   │ ←── Manual or Scheduled
└──────────┬──────────┘
           ↓
┌─────────────────────┐
│  Download Service   │ ←── Fetch from uscode.house.gov
└──────────┬──────────┘
           ↓
┌─────────────────────┐
│  XML Parser         │ ←── Parse USLM XML
└──────────┬──────────┘
           ↓
┌─────────────────────┐
│  Import Service     │ ←── Upsert to database
└──────────┬──────────┘
           ↓
┌─────────────────────┐
│  PostgreSQL         │
│  (statutes table)   │
└─────────────────────┘
```

---

## Effort Estimate

### Backend Development

| Component | Estimate | Story Points |
|-----------|----------|--------------|
| Statute entity + repository | S | 1 |
| Database migration | S | 1 |
| XML download service | S | 1 |
| USLM XML parser | M | 3 |
| Import service (with upsert) | M | 2 |
| Admin sync endpoint | S | 1 |
| **Backend Subtotal** | | **9** |

### Frontend Development

| Component | Estimate | Story Points |
|-----------|----------|--------------|
| Statutes list page | S | 1 |
| Statute detail page | S | 1 |
| Search/filter UI | M | 2 |
| Admin sync UI | S | 1 |
| **Frontend Subtotal** | | **5** |

### Testing

| Component | Estimate | Story Points |
|-----------|----------|--------------|
| Parser unit tests | M | 2 |
| Service integration tests | S | 1 |
| API endpoint tests | S | 1 |
| **Testing Subtotal** | | **4** |

### Total Estimate

| Category | Points |
|----------|--------|
| Backend | 9 |
| Frontend | 5 |
| Testing | 4 |
| **Total** | **18** |

**Adjusted for Spike Findings:** 8-13 points for ADMIN-1.11 (first implementation story)

Break into multiple stories:
- ADMIN-1.11a: Backend model + parser + import (8 pts)
- ADMIN-1.11b: Frontend list/detail pages (5 pts)
- ADMIN-1.11c: Admin sync UI + search (5 pts)

### Ongoing Maintenance

| Activity | Frequency | Effort |
|----------|-----------|--------|
| Monitor for new release points | Weekly | Low |
| Run sync (manual trigger) | Monthly | Low |
| Schema updates (rare) | As needed | Medium |

---

## Risks and Blockers

### Identified Risks

| Risk | Probability | Impact | Mitigation |
|------|-------------|--------|------------|
| Large XML file parsing performance | Medium | Medium | Stream parsing (SAX/StAX), batch inserts |
| USLM schema changes | Low | Medium | Version check, schema validation |
| Storage size growth | Low | Low | Index optimization, consider summary-only option |
| Complex section structures | Medium | Low | Robust parser with fallbacks |
| Missing historical data | Low | Low | Start with current release only |

### Potential Blockers

| Blocker | Status | Resolution |
|---------|--------|------------|
| No US Code API | CONFIRMED | Use bulk XML download |
| Authentication required | NOT AN ISSUE | Public download, no auth |
| Rate limiting | NOT AN ISSUE | No API = no limits |
| Data licensing | NOT AN ISSUE | Public domain |

### Dependencies

| Dependency | Type | Status |
|------------|------|--------|
| Java XML parsing (JAXP/StAX) | Built-in | Available |
| PostgreSQL TEXT columns | Built-in | Available |
| Full-text search (pg_tsvector) | Built-in | Available |
| HTTP client for download | Spring WebClient | Available |

---

## Implementation Notes

### Parser Strategy

Use **StAX (Streaming API for XML)** for memory efficiency:

```java
// Streaming approach for large files
XMLInputFactory factory = XMLInputFactory.newInstance();
XMLStreamReader reader = factory.createXMLStreamReader(inputStream);

while (reader.hasNext()) {
    if (reader.isStartElement() && "section".equals(reader.getLocalName())) {
        Statute statute = parseSection(reader);
        statuteRepository.save(statute);
    }
    reader.next();
}
```

### Batch Insert Strategy

```java
@Transactional
public void importTitle(int titleNumber, InputStream xmlStream) {
    List<Statute> batch = new ArrayList<>();

    // Parse and collect
    parseXml(xmlStream, section -> {
        batch.add(mapToStatute(section));
        if (batch.size() >= 100) {
            statuteRepository.saveAll(batch);
            batch.clear();
        }
    });

    // Final batch
    if (!batch.isEmpty()) {
        statuteRepository.saveAll(batch);
    }
}
```

### Update Detection

Check release point before full download:

```java
public boolean hasNewRelease() {
    String currentRelease = statuteRepository.findLatestReleasePoint();
    String latestRelease = fetchLatestReleasePointFromHouseGov();
    return !Objects.equals(currentRelease, latestRelease);
}
```

---

## References

### Official Sources

- [US Code Download Page](https://uscode.house.gov/download/download.shtml) - Primary data source
- [USLM GitHub Repository](https://github.com/usgpo/uslm) - Schema and documentation
- [USLM User Guide (PDF)](https://uscode.house.gov/download/resources/USLM-User-Guide.pdf) - Detailed schema documentation
- [Office of Law Revision Counsel](https://uscode.house.gov/) - Authoritative US Code maintainer

### API Documentation

- [GovInfo API Docs](https://api.govinfo.gov/docs/) - For cross-references (not US Code bulk)
- [GovInfo Developer Hub](https://www.govinfo.gov/developers) - General API info
- [Congress.gov API](https://api.congress.gov) - Legislative data (no US Code)

### Technical Resources

- [USLM Schema XSD](https://github.com/usgpo/uslm/blob/main/USLM.xsd)
- [GovInfo Bulk Data](https://www.govinfo.gov/bulkdata) - Related collections (not US Code)
- [XML House.gov](https://xml.house.gov/) - Legislative XML overview

---

## Conclusion

**The US Code import is fully feasible** using bulk XML downloads from uscode.house.gov. No API integration is possible because no comprehensive US Code API exists - this is a known gap in federal data services.

**Recommended next steps for ADMIN-1.11:**

1. Create `Statute` JPA entity and database migration
2. Implement USLM XML streaming parser
3. Build download and import service
4. Create admin sync endpoint
5. Add frontend list/detail pages

The estimated effort is **Medium (8-13 story points)** for the initial implementation, with low ongoing maintenance burden.
