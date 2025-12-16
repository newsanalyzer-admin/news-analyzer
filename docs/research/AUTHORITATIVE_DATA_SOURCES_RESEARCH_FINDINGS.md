# Authoritative Government Data Sources: Research Findings

## Executive Summary

This research surveyed authoritative U.S. government data sources to expand NewsAnalyzer's "factbase" beyond the current government organizations dataset. The findings identify **10+ viable data sources** covering WHO (organizations, positions, officeholders), WHAT (regulatory filings), WHERE (geographic/jurisdictional data), and WHEN (calendars, schedules).

### Key Findings

| Priority | Domain | Recommended Source | Status |
|----------|--------|-------------------|--------|
| **P1** | Congressional Members & Committees | Congress.gov API | Active, well-documented |
| **P1** | Congressional Members (enriched) | unitedstates/congress-legislators | Active, comprehensive |
| **P2** | Executive Appointees | OPM PLUM Data | Active, new website |
| **P3** | Campaign Finance | FEC OpenFEC API | Active, official |
| **P4** | Geographic Boundaries | Census TIGER/Line | Active, updated annually |
| **P5** | Regulatory Filings | Federal Register API | Active, no auth required |
| **P6** | Court/Judge Data | CourtListener API | Active, free tier |
| **P7** | Election Results | MIT Election Lab | Datasets, not API |
| **P8** | Federal Holidays | OPM.gov | ICS download, no API |

### Critical Discoveries

1. **ProPublica Congress API is SHUT DOWN** (as of July 2024) - Use Congress.gov API instead
2. **GovTrack API is SHUT DOWN** (as of 2018) - Use Congress.gov API instead
3. **OpenSecrets API is SHUT DOWN** (as of April 2025) - Use FEC directly or bulk data
4. **GOVMAN collection doesn't exist** in GovInfo (confirmed in prior research)
5. **OPM PLUM website is new** (launched 2024, will replace Plum Book in 2026)

---

## Detailed Source Findings

---

## 1. Congress.gov API (Official - Library of Congress)

**URL**: https://api.congress.gov
**Documentation**: https://github.com/LibraryOfCongress/api.congress.gov
**Authority Level**: Official (Library of Congress)
**Data Category**: WHO (members, committees), WHAT (bills, votes)

### Coverage
- All Members of Congress (current and historical)
- Committee and subcommittee information
- Committee assignments and leadership
- Bills, amendments, votes
- Nominations and treaties
- Congressional Record

### Access Method
- **API Type**: REST API (v3)
- **Authentication**: API key required (free via api.data.gov)
- **Rate Limits**: 5,000 requests/hour
- **Bulk Download**: Yes, via GovInfo

### Data Format
- **Response Format**: JSON or XML
- **Schema Documentation**: GitHub repository
- **Update Frequency**: Daily (votes updated every 30 minutes)

### Key Endpoints
```
/v3/member                    - List all members
/v3/member/{bioguideId}       - Specific member details
/v3/member/{bioguideId}/sponsored-legislation
/v3/committee                 - List committees
/v3/committee/{chamber}/{committeeCode}
/v3/committee-meeting         - Committee meetings
```

### Integration Notes
- **Pros**: Official source, comprehensive, well-documented, stable
- **Cons**: Rate limited, requires key management
- **Dependencies**: BioGuide IDs for member lookups
- **Complexity**: Low-Medium

### Links
- API Portal: https://api.congress.gov
- GitHub: https://github.com/LibraryOfCongress/api.congress.gov
- API Key Signup: https://api.data.gov/signup/

---

## 2. unitedstates/congress-legislators (GitHub Dataset)

**URL**: https://github.com/unitedstates/congress-legislators
**Authority Level**: Community-maintained, official data sources
**Data Category**: WHO (members, presidents, VP)

### Coverage
- All Members of Congress 1789-Present
- Committee membership (current and historical from 93rd Congress)
- Presidents and Vice Presidents
- Social media accounts (official only)
- District office locations
- Cross-references to other databases (BioGuide, Thomas, GovTrack, etc.)

### Access Method
- **Format**: YAML, JSON, CSV files
- **Authentication**: None (public GitHub)
- **Rate Limits**: GitHub API limits if using API
- **Bulk Download**: Yes (clone repository)

### Data Structure
```yaml
# Each legislator record contains:
- id:           # bioguide, thomas, govtrack, opensecrets, etc.
- name:         # first, last, middle, suffix, nickname
- bio:          # birthday, gender
- terms:        # type, start, end, state, district, party, etc.
```

### Key Files
- `legislators-current.yaml` - Current members
- `legislators-historical.yaml` - All historical members
- `committees-current.yaml` - Current committees
- `committee-membership-current.yaml` - Current assignments

### Integration Notes
- **Pros**: Comprehensive historical data, multiple ID cross-references, public domain
- **Cons**: Requires periodic sync (not real-time API), community maintained
- **Dependencies**: None
- **Complexity**: Low

---

## 3. OPM PLUM Data (Appointed Positions)

**URL**: https://www.opm.gov/about-us/open-government/plum-reporting/plum-data/
**Documentation**: https://www.opm.gov/about-us/open-government/plum-reporting/data-information/
**Authority Level**: Official (Office of Personnel Management)
**Data Category**: WHO (appointed positions, officeholders)

### Coverage
- 8,000+ federal civil service leadership positions
- Policy and supporting positions in executive branch
- Cabinet members, agency heads, administrators
- Senate-confirmed positions
- Political appointees

### Access Method
- **Format**: Searchable web interface
- **Authentication**: None for viewing
- **Bulk Download**: Not currently documented (check for CSV export)
- **API**: Not documented

### Data Fields
- Agency name and type
- Position title
- Pay plan and grade
- Appointment type
- Incumbent name (where filled)

### Integration Notes
- **Pros**: Authoritative source for executive branch positions, regularly updated
- **Cons**: No documented API, may require scraping or manual export
- **Important**: Will replace printed Plum Book starting January 2026
- **Complexity**: Medium-High (no API)

### Links
- PLUM Data: https://www.opm.gov/about-us/open-government/plum-reporting/plum-data/
- 2024 Plum Book (PDF): https://www.govinfo.gov/features/2024-plum-book

---

## 4. FEC OpenFEC API (Campaign Finance)

**URL**: https://api.open.fec.gov/developers/
**Authority Level**: Official (Federal Election Commission)
**Data Category**: WHO (candidates), WHAT (contributions, expenditures)

### Coverage
- All federal candidates (Presidential, Senate, House)
- Political committees and PACs
- Individual contributions
- Expenditures and disbursements
- Filing information

### Access Method
- **API Type**: REST API
- **Authentication**: API key required (free)
- **Rate Limits**: 1,000 requests/hour (default)
- **Bulk Download**: Yes (bulk data files available)

### Data Format
- **Response Format**: JSON
- **Schema Documentation**: OpenAPI spec available
- **Update Frequency**: Electronic filings updated every 15 minutes

### Key Endpoints
```
/candidates/           - List candidates
/candidates/{id}/      - Candidate details
/committees/           - List committees
/schedules/schedule_a/ - Individual contributions
/schedules/schedule_b/ - Disbursements
/filings/              - Filing information
```

### Integration Notes
- **Pros**: Official source, comprehensive campaign finance data, well-documented API
- **Cons**: Complex data model, requires understanding of FEC filing types
- **Dependencies**: FEC candidate/committee IDs
- **Complexity**: Medium

### Links
- API Documentation: https://api.open.fec.gov/developers/
- Bulk Data: https://www.fec.gov/data/browse-data/?tab=bulk-data

---

## 5. Federal Register API (Regulatory Data)

**URL**: https://www.federalregister.gov/developers/documentation/api/v1
**Authority Level**: Official (National Archives)
**Data Category**: WHAT (regulations), WHEN (effective dates)

### Coverage
- All Federal Register contents since 1994
- Proposed rules (NPRMs)
- Final rules
- Notices
- Executive Orders
- Presidential Documents
- Agency information

### Access Method
- **API Type**: REST API
- **Authentication**: None required
- **Rate Limits**: Not specified (be reasonable)
- **Bulk Download**: Yes

### Data Format
- **Response Format**: JSON or CSV
- **Schema Documentation**: Interactive API docs
- **Update Frequency**: Daily publication

### Key Endpoints
```
/documents              - Search/list documents
/documents/{number}     - Specific document
/agencies               - List agencies
/public-inspection      - Pre-publication documents
```

### Key Fields
- Document type, title, abstract
- Agency names and IDs
- Publication date
- Effective date
- CFR references
- Docket numbers

### Integration Notes
- **Pros**: No authentication required, comprehensive regulatory data, effective dates for "WHEN"
- **Cons**: Results limited to 2000 without date filtering
- **Dependencies**: None
- **Complexity**: Low

### Related: Regulations.gov API
- URL: https://open.gsa.gov/api/regulationsgov/
- Provides public comments and docket data
- Requires API key

---

## 6. Census Bureau Geographic Data (TIGER/Line)

**URL**: https://www.census.gov/geographies/mapping-files/time-series/geo/tiger-line-file.html
**Authority Level**: Official (Census Bureau)
**Data Category**: WHERE (boundaries, districts)

### Coverage
- Congressional district boundaries (119th Congress current)
- State legislative districts
- County and municipality boundaries
- Federal court districts (via separate source)
- Census tracts, block groups, blocks

### Access Method
- **Format**: Shapefiles, Geodatabases, GeoPackages, KML
- **API**: Census Geocoding API, TIGERweb services
- **Bulk Download**: Yes (annual releases)

### Data Availability
- 119th Congressional Districts (2024) available
- Updated after redistricting
- Historical boundaries available

### Key Resources
```
TIGER/Line Shapefiles:
- Congressional Districts (CD)
- State Legislative Districts (SLDU, SLDL)
- Counties and States
- Places (cities, towns)

APIs:
- TIGERweb: https://tigerweb.geo.census.gov/tigerwebmain/TIGERweb_main.html
- Geocoding API: https://geocoding.geo.census.gov/
- Census Data API: https://api.census.gov/
```

### Integration Notes
- **Pros**: Authoritative boundary data, multiple formats, free
- **Cons**: Large file sizes, requires GIS processing capability
- **Dependencies**: GIS libraries (GDAL, Shapely, etc.)
- **Complexity**: Medium-High

---

## 7. USGS Geographic Names (GNIS)

**URL**: https://www.usgs.gov/tools/geographic-names-information-system-gnis
**Authority Level**: Official (U.S. Geological Survey)
**Data Category**: WHERE (place names, features)

### Coverage
- 1+ million domestic geographic features
- Populated places
- Physical features (lakes, streams, summits, valleys)
- Historical names
- Official coordinates

### Access Method
- **API**: ArcGIS MapServer endpoint
- **Bulk Download**: Yes (via National Map Downloader)
- **Web Query**: https://geonames.usgs.gov/

### Data Format
- Multiple formats available
- Refreshed every other month
- Includes coordinates, state, county, USGS map references

### MapServer Endpoint
```
https://carto.nationalmap.gov/arcgis/rest/services/geonames/MapServer
```

### Integration Notes
- **Pros**: Authoritative place names, coordinates, federal standard
- **Cons**: Does not define region boundaries (only points/names)
- **Dependencies**: None for downloads, ArcGIS for services
- **Complexity**: Low-Medium

---

## 8. GovInfo API (Congressional Directory - CDIR)

**URL**: https://api.govinfo.gov
**Documentation**: https://github.com/usgpo/api
**Authority Level**: Official (Government Publishing Office)
**Data Category**: WHO (members), supplementary data

### Coverage
- Congressional Directory (CDIR) - 237 packages, 15,024 granules
- Includes social media information for members
- 41 total collections available

### Access Method
- **API Type**: REST API
- **Authentication**: API key (api.data.gov)
- **Rate Limits**: Standard api.data.gov limits

### CDIR Endpoints
```
# Search by member name
collection:cdir member:pelosi

# Search by state
collection:cdir state:ca

# Search by district
collection:cdir state:ca district:5

# Granule summary
https://api.govinfo.gov/packages/CDIR-2018-10-29/granules/CDIR-2018-10-29-CA-H-33/summary
```

### Integration Notes
- **Pros**: Social media data, official directory information
- **Cons**: CDIR updates less frequently than Congress.gov API
- **Dependencies**: API key
- **Complexity**: Low-Medium

---

## 9. CourtListener API (Court/Judge Data)

**URL**: https://www.courtlistener.com/help/api/rest/
**Authority Level**: Non-profit aggregator (Free Law Project)
**Data Category**: WHO (judges), WHAT (cases, opinions)

### Coverage
- Federal and state case law
- PACER data (RECAP Archive)
- Judge biographical information
- Political affiliations
- Education and employment histories
- Oral argument recordings

### Access Method
- **API Type**: REST API (v4.3)
- **Authentication**: Free tier available, paid for higher limits
- **Bulk Download**: Yes (for some datasets)

### Key Features
- FJC Integrated Database metadata
- Links judges to cases they've heard
- Parties, attorneys, and law firms data

### Integration Notes
- **Pros**: Comprehensive judicial data, free tier, well-documented
- **Cons**: Not official government source, aggregated data
- **Dependencies**: None
- **Complexity**: Low-Medium

### Alternative: PACER Direct
- URL: https://pacer.uscourts.gov/
- Official source but requires account and has per-page fees ($0.10/page)
- Fees waived if under $30/quarter

---

## 10. MIT Election Data + Science Lab (MEDSL)

**URL**: https://electionlab.mit.edu/data
**GitHub**: https://github.com/MEDSL
**Authority Level**: Academic (MIT), sourced from official data
**Data Category**: WHO (candidates, winners), WHEN (election dates)

### Coverage
- Presidential elections 1976-2020 (state-level)
- Presidential elections 2000-2024 (county-level)
- U.S. House elections 1976-2024
- U.S. Senate elections 1976-2020
- 2020/2024 precinct-level data
- Cast vote records (42.7 million voters, 20 states)

### Access Method
- **Format**: Downloadable datasets (CSV, etc.)
- **API**: No traditional API
- **Bulk Download**: Yes (GitHub, Harvard Dataverse)

### Integration Notes
- **Pros**: Clean, research-quality election data, historical coverage
- **Cons**: Not real-time, dataset downloads not API
- **Dependencies**: None
- **Complexity**: Low

---

## 11. Official Calendars & Schedules

### Federal Holidays (OPM)
- **URL**: https://www.opm.gov/policy-data-oversight/pay-leave/federal-holidays/
- **Format**: Web page, ICS download
- **API**: FederalPay.org offers unofficial API

### Congressional Calendar
- **URL**: https://www.congress.gov/calendars-and-schedules
- **Coverage**: Session dates, recess periods
- **API**: Not documented (web-based)
- **Alternative**: GovInfo CCAL collection

### Integration Notes
- **Workaround**: 18F/us-federal-holidays Node.js package
- GitHub: https://github.com/18F/us-federal-holidays

---

## Source Comparison Matrix

| Source | Category | Auth Required | Format | Update Freq | API Available | Complexity | Priority |
|--------|----------|---------------|--------|-------------|---------------|------------|----------|
| Congress.gov API | WHO | API Key | JSON/XML | Daily | Yes | Low-Med | P1 |
| congress-legislators | WHO | None | YAML/JSON | Weekly | No (files) | Low | P1 |
| OPM PLUM | WHO | None | Web | Quarterly | No | Med-High | P2 |
| FEC OpenFEC | WHO/WHAT | API Key | JSON | 15 min | Yes | Medium | P3 |
| Census TIGER | WHERE | None | Shapefile | Annual | Partial | Med-High | P4 |
| Federal Register | WHAT/WHEN | None | JSON/CSV | Daily | Yes | Low | P5 |
| USGS GNIS | WHERE | None | Various | Bi-monthly | Partial | Low-Med | P6 |
| GovInfo CDIR | WHO | API Key | JSON/XML | Periodic | Yes | Low-Med | P5 |
| CourtListener | WHO/WHAT | Optional | JSON | Varies | Yes | Low-Med | P6 |
| MIT Election Lab | WHO/WHEN | None | CSV | Post-election | No (files) | Low | P7 |
| OPM Holidays | WHEN | None | ICS | Annual | No | Low | P8 |

---

## Gap Analysis

### Data Available
- Congressional members and committees (comprehensive)
- Federal campaign finance (comprehensive)
- Regulatory filings and effective dates (comprehensive)
- Geographic boundaries for districts (comprehensive)
- Executive appointees (via PLUM, limited API)
- Judicial data (via aggregator)
- Election results (historical datasets)

### Data Gaps Identified

| Gap | Description | Potential Workaround |
|-----|-------------|---------------------|
| **State-level officials** | No federal API for governors, state legislators | OpenStates.org, Ballotpedia |
| **Real-time election results** | No official API for live results | AP Election API (paid) |
| **Agency organizational charts** | No API for internal org structure | Manual curation, USA.gov |
| **Historical appointees** | PLUM is recent; historical harder | Plum Book PDFs, Wikipedia |
| **Court jurisdiction boundaries** | Not in Census TIGER | Manual GIS compilation |

### Sources Explored but Unavailable/Unsuitable

| Source | Status | Reason |
|--------|--------|--------|
| ProPublica Congress API | **Shut down July 2024** | Use Congress.gov instead |
| GovTrack API | **Shut down 2018** | Use Congress.gov instead |
| OpenSecrets API | **Shut down April 2025** | Use FEC directly |
| GovInfo GOVMAN | **Does not exist** | Use PLUM, Congress.gov |

---

## Recommended Implementation Phases

### Phase 1: Core Congressional Data
1. **Congress.gov API** - Members, committees, leadership
2. **unitedstates/congress-legislators** - Enriched member data, historical
3. **GovInfo CDIR** - Social media, contact info

### Phase 2: Positions & Appointees
4. **OPM PLUM Data** - Executive branch positions
5. **FEC OpenFEC** - Candidates, link to campaign finance

### Phase 3: Geographic & Regulatory
6. **Census TIGER/Line** - District boundaries
7. **Federal Register API** - Regulations, effective dates
8. **USGS GNIS** - Place names, coordinates

### Phase 4: Courts & Elections
9. **CourtListener API** - Judges, cases
10. **MIT Election Lab** - Historical election results

### Phase 5: Calendars & Schedules
11. **OPM Holidays** - Federal holidays
12. **Congress.gov Calendars** - Session schedules

---

## Technical Recommendations for Architect

### API Key Management
- Register single api.data.gov key for: Congress.gov, GovInfo, FEC
- Store keys securely (environment variables, secrets manager)
- Implement rate limiting to stay within quotas

### Data Synchronization Strategy
- **Daily**: Congress.gov (members, votes), Federal Register
- **Weekly**: congress-legislators repo, FEC filings
- **Monthly/Quarterly**: PLUM data, CDIR updates
- **Annually**: Census TIGER boundaries, MIT election data

### Entity Linking
- Use **BioGuide ID** as primary key for Congressional members
- Cross-reference with FEC candidate IDs, OpenSecrets IDs
- Store multiple IDs per entity for future integrations

### Schema Considerations
- Extend Entity model to support Position relationships
- Add temporal validity (start/end dates) for positions
- Consider separate GovernmentPosition entity type
- Plan for geographic relationships (represents district X)

---

## Sources

### Official Government APIs
- [Congress.gov API](https://api.congress.gov)
- [FEC OpenFEC API](https://api.open.fec.gov/developers/)
- [Federal Register API](https://www.federalregister.gov/developers/documentation/api/v1)
- [GovInfo API](https://www.govinfo.gov/features/api)
- [Regulations.gov API](https://open.gsa.gov/api/regulationsgov/)
- [Census TIGERweb](https://tigerweb.geo.census.gov/)

### Official Government Data Portals
- [OPM PLUM Data](https://www.opm.gov/about-us/open-government/plum-reporting/plum-data/)
- [Census TIGER/Line](https://www.census.gov/geographies/mapping-files/time-series/geo/tiger-line-file.html)
- [USGS GNIS](https://www.usgs.gov/tools/geographic-names-information-system-gnis)
- [OPM Federal Holidays](https://www.opm.gov/policy-data-oversight/pay-leave/federal-holidays/)
- [Congress.gov Calendars](https://www.congress.gov/calendars-and-schedules)

### Community/Academic Resources
- [unitedstates/congress-legislators](https://github.com/unitedstates/congress-legislators)
- [CourtListener API](https://www.courtlistener.com/help/api/rest/)
- [MIT Election Lab](https://electionlab.mit.edu/data)
- [18F/us-federal-holidays](https://github.com/18F/us-federal-holidays)

---

*Research conducted: November 2024*
*For: NewsAnalyzer Project - Factbase Expansion*
