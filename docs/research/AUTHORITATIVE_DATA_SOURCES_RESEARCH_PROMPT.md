# Deep Research Prompt: Authoritative Government Data Sources for NewsAnalyzer

## Research Objective

Conduct a comprehensive survey of authoritative U.S. government data sources (APIs, bulk data, and structured feeds) that can expand NewsAnalyzer's "factbase" beyond the current government organizations dataset. The research will inform architectural decisions for integrating these sources into the application.

**Primary Goal**: Identify and evaluate authoritative data sources covering the "4 W's" of government information:
- **WHO**: Organizations, positions, officeholders
- **WHAT**: Regulatory filings, official actions
- **WHERE**: Geographic/jurisdictional data
- **WHEN**: Official calendars, timelines, effective dates

**Key Decision This Research Informs**: How to architect a scalable, maintainable system for ingesting and synchronizing multiple authoritative government data sources with daily/weekly update frequency.

## Background Context

### Current State
NewsAnalyzer currently has:
- A "factbase" with government organizations (GovernmentOrganization entity)
- Integration research on GovInfo API (41 collections available, GOVMAN not available)
- Initial exploration of Congress.gov API for legislative data
- Schema.org-based entity modeling

### Existing Reference Materials
- `GOVINFO_API_FINDINGS.md` - Documents available GovInfo collections and GOVMAN unavailability
- `congress.gov/README.md` - Congress.gov API and bulk data options
- `claude_chat.txt` - Initial survey of government data sources

### Constraints
- Data must be from **authoritative/official sources** (primary sources preferred)
- Update frequency target: **daily/weekly synchronization** (not real-time)
- Must support **programmatic access** (API or structured bulk download)
- Should have clear **data provenance** for fact-checking use case

## Research Questions

### Primary Questions (Must Answer)

#### P1: Government Organization Structure
1. What authoritative APIs or data sources provide **federal agency/department organizational structure**?
   - Hierarchy (parent-child relationships)
   - Sub-agencies, bureaus, offices
   - Historical changes (reorganizations, name changes)

#### P2: Elected Official Positions
2. What authoritative APIs exist for **Congressional positions**?
   - Senate and House membership
   - Committee and subcommittee assignments
   - Leadership positions (Speaker, Majority Leader, etc.)
   - Historical membership data

3. What authoritative APIs exist for **state-level elected positions**?
   - Governors, state legislators
   - Is there a federal source, or must this be state-by-state?

#### P3: Appointed Positions & Officeholders
4. What authoritative APIs exist for **executive branch appointees**?
   - Cabinet members
   - Agency heads and administrators
   - Senate-confirmed positions (Plum Book data)
   - Judicial appointments

5. What sources provide **current officeholder data** with:
   - Start/end dates of service
   - Party affiliation
   - Biographical information
   - Contact information

#### P4: Geographic & Jurisdictional Data
6. What authoritative APIs provide **official geographic/jurisdictional data**?
   - Congressional district boundaries
   - Federal court circuit/district boundaries
   - Federal agency regional boundaries
   - State/county/municipality boundaries

7. What sources link **positions to jurisdictions**?
   - Which representative serves which district
   - Which court has jurisdiction where

#### P5: Data Integration Characteristics
8. For each identified source, what are the **technical characteristics**?
   - API type (REST, GraphQL, bulk download)
   - Authentication requirements (API key, OAuth, none)
   - Rate limits and quotas
   - Data formats (JSON, XML, CSV)
   - Update frequency of source data

9. What **coverage gaps** exist across identified sources?
   - Missing data types
   - Historical data availability
   - Timeliness of updates

### Secondary Questions (Nice to Have)

#### P6: Election Data
10. What authoritative sources provide **election results**?
    - Federal elections (Presidential, Congressional)
    - Official/certified results vs. unofficial
    - Historical election data

11. What sources provide **candidate information**?
    - Filed candidates
    - Ballot access status
    - Primary vs. general election

#### P7: Campaign Finance
12. What authoritative sources provide **campaign finance data**?
    - FEC individual contributions
    - PAC/Super PAC data
    - Expenditure data
    - Linkage to candidates/committees

#### P8: Regulatory Data
13. What sources provide **regulatory filing data**?
    - Proposed rules (NPRMs)
    - Final rules
    - Agency responsible
    - Effective dates
    - Public comment data

#### P9: Official Calendars & Timelines
14. What sources provide **official government calendar data**?
    - Congressional session dates
    - Federal holidays
    - Budget/appropriations deadlines
    - Supreme Court term/argument calendars

#### P10: Aggregators & Secondary Sources
15. Are there **reliable aggregator services** that consolidate multiple official sources?
    - ProPublica Congress API
    - OpenSecrets/Center for Responsive Politics
    - GovTrack
    - Ballotpedia
    - What are the tradeoffs (convenience vs. data provenance)?

#### P11: Licensing & Terms
16. What are the **licensing/terms of use** for each source?
    - Public domain vs. restricted use
    - Attribution requirements
    - Commercial use restrictions
    - Rate limit compliance requirements

## Research Methodology

### Information Sources

**Primary Sources (Prioritize These)**:
- Official .gov API documentation
- Government data portals (data.gov, api.data.gov)
- Federal agency developer resources
- Government Publishing Office (GPO)

**Secondary Sources**:
- Academic/research documentation of government data
- Established civic tech projects (e.g., GovTrack, OpenStates documentation)
- Developer community resources (GitHub repos, API directories)

**Approach**:
- Start with api.data.gov as a central registry
- Check each major agency's developer/data portal
- Document both APIs and bulk data options
- Note any sources that were explored but found unsuitable (and why)

### Analysis Frameworks

For each identified data source, evaluate using this framework:

| Criterion | Assessment |
|-----------|------------|
| **Authority** | Is this an official/primary source? |
| **Coverage** | What entities/time periods are covered? |
| **Freshness** | How often is data updated? |
| **Accessibility** | API vs. bulk? Auth requirements? |
| **Format** | JSON/XML/CSV? Schema documentation? |
| **Reliability** | Uptime, stability, deprecation risk |
| **Linkability** | Can entities be linked to other sources? |

### Data Quality Requirements

- Sources should be **official or highly authoritative**
- Data should have **clear provenance** (can trace back to official record)
- Prefer sources with **stable identifiers** for entities
- Prefer sources with **documented schemas/data dictionaries**

## Expected Deliverables

### Executive Summary
- Top 5-10 recommended data sources with rationale
- Critical gaps identified and potential workarounds
- High-level integration complexity assessment
- Recommended prioritization for implementation

### Detailed Findings

#### For Each Data Source Identified:

```
## [Source Name]

**URL**: [Primary documentation URL]
**Authority Level**: Official / Semi-official / Aggregator
**Data Category**: WHO / WHAT / WHERE / WHEN

### Coverage
- What data is available
- Time range covered
- Update frequency

### Access Method
- API endpoint(s)
- Authentication: [None / API Key / OAuth]
- Rate limits: [Requests/period]
- Bulk download: [Yes/No]

### Data Format
- Response format: [JSON / XML / CSV]
- Schema documentation: [URL if available]
- Sample response structure

### Integration Notes
- Pros for NewsAnalyzer use case
- Cons / Limitations
- Dependencies on other sources
- Estimated integration complexity: [Low / Medium / High]

### Links
- API Documentation: [URL]
- Developer Portal: [URL]
- Terms of Service: [URL]
```

#### Source Comparison Matrix

| Source | Category | Auth | Format | Update Freq | Complexity | Priority |
|--------|----------|------|--------|-------------|------------|----------|
| ... | ... | ... | ... | ... | ... | ... |

#### Gap Analysis
- Data types needed but not found in official sources
- Potential workarounds or alternative approaches
- Areas requiring manual data curation

### Supporting Materials

- Links to all API documentation reviewed
- Sample API responses for key sources
- Data model/schema excerpts where relevant
- List of sources explored but rejected (with reasons)

## Success Criteria

The research is successful if it provides:

1. **Comprehensive coverage** of available authoritative sources for the defined scope
2. **Actionable technical details** sufficient for architect to assess integration approaches
3. **Clear prioritization** based on authority, coverage, and integration complexity
4. **Identified gaps** with honest assessment of what's not available
5. **Sufficient documentation** to begin Phase 1 implementation without re-research

## Scope Boundaries

**In Scope**:
- U.S. Federal government data sources
- Official/authoritative sources only
- Programmatically accessible data (API or bulk)
- Sources supporting daily/weekly sync patterns

**Out of Scope**:
- State/local government sources (note if found, but don't deep-dive)
- International government sources
- News or media sources
- Social media data
- Sources requiring manual web scraping only
- Real-time streaming data requirements

## Priority Order for Investigation

1. **Congress.gov API** - Congressional membership, committees, leadership
2. **FEC API** - Campaign finance, candidate data
3. **Federal Register API** - Regulatory filings, agency actions
4. **USGS/Census Geographic APIs** - Jurisdictional boundaries
5. **GovInfo API** - Leverage existing 41 collections
6. **OPM/Plum Book** - Appointed positions
7. **Federal Judicial Center** - Court data
8. **Aggregators** (ProPublica, GovTrack) - Gap fillers

---

## Usage Notes

**For AI Research Assistant**:
Use web search and documentation review to systematically investigate each source. Prioritize official .gov documentation. For each source, attempt to find concrete API endpoint examples and sample data structures.

**For Human Researcher**:
This prompt can guide manual research efforts. Consider creating accounts and testing APIs directly to verify documentation accuracy.

**Integration with NewsAnalyzer**:
Findings should map to the existing entity model (Schema.org-based) and support the factbase architecture. Note any data that would require schema extensions.

---

*Generated for NewsAnalyzer Project*
*Research Focus: Authoritative Government Data Sources*
*Date: 2024*
