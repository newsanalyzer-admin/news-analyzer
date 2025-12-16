# US Government Organizational Chart Integration

**Component:** Government Organization Data Ingestion System
**Version:** 1.0
**Date:** 2025-11-21
**Status:** 🎯 DESIGN PROPOSAL
**Phase:** Phase 2 Enhancement (External Knowledge Integration)

---

## Executive Summary

This document outlines the architecture for integrating **US Government Manual** organizational chart data into NewsAnalyzer v2, providing authoritative government structure information to enhance entity recognition, validation, and reasoning capabilities.

### Key Goals

1. **Authoritative Data Source** - Use official US Government Manual as ground truth
2. **Automated Ingestion** - Periodic sync of organizational structure updates
3. **OWL Integration** - Enrich existing NewsAnalyzer ontology with official org structure
4. **Entity Validation** - Validate detected government entities against official data
5. **Relationship Mapping** - Capture hierarchical and lateral organizational relationships

### Strategic Value

- **Accuracy** - Detect when news mentions incorrect agency names or structures
- **Context** - Understand which agencies have jurisdiction over what topics
- **Evolution** - Track organizational changes over time (agencies created/dissolved)
- **Reasoning** - Enable queries like "which agencies report to Department of Defense?"

---

## 1. Data Source Analysis

### US Government Manual Overview

**Official Source:** https://www.govinfo.gov/app/collection/govman/

**Format Options:**
1. **HTML Pages** - Human-readable, requires scraping
2. **XML/SGML** - Structured data via GovInfo API
3. **PDF** - Official publication format (harder to parse)

**Data Structure:**
```
US Government
├── Legislative Branch
│   ├── Congress
│   │   ├── Senate
│   │   └── House of Representatives
│   ├── Government Accountability Office (GAO)
│   ├── Government Publishing Office (GPO)
│   └── Library of Congress
├── Executive Branch
│   ├── Executive Office of the President
│   ├── Departments (15 Cabinet departments)
│   │   ├── Department of State
│   │   ├── Department of Defense
│   │   ├── ...
│   └── Independent Agencies (~60+)
│       ├── EPA
│       ├── NASA
│       ├── ...
└── Judicial Branch
    ├── Supreme Court
    ├── Courts of Appeals
    └── District Courts
```

**Key Data Points:**
- **Agency Name** (official)
- **Acronyms** (EPA, NASA, DOD)
- **Parent Organization** (hierarchical relationship)
- **Establishment Date**
- **Authorizing Legislation**
- **Mission Statement**
- **Organizational Structure** (sub-agencies, bureaus, offices)
- **Leadership** (Cabinet secretaries, agency heads)
- **Contact Information**
- **Website URLs**

### GovInfo Bulk Data API

**API Base:** `https://api.govinfo.gov/`

**Collections:**
- `GOVMAN` - United States Government Manual
- `BUDGET` - Budget documents (supplementary data)
- `FR` - Federal Register (agency rule-making)

**Access:**
- **API Key Required** - Free registration at api.data.gov
- **Rate Limits** - 1,000 requests/hour (demo key), 10,000/hour (production)
- **Formats** - JSON, XML, HTML, PDF
- **Historical Data** - Archives back to 1995

---

## 2. System Architecture

### High-Level Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                    NewsAnalyzer v2                               │
│             Government Org Chart Integration                     │
└─────────────────────────────────────────────────────────────────┘

┌──────────────────┐         ┌──────────────────┐
│  GovInfo API     │────────▶│  Ingestion       │
│  (govinfo.gov)   │         │  Service         │
└──────────────────┘         │  (Python)        │
                             └──────────────────┘
                                      │
                                      ▼
                             ┌──────────────────┐
                             │  Data Transform  │
                             │  & Validation    │
                             └──────────────────┘
                                      │
                    ┌─────────────────┼─────────────────┐
                    ▼                 ▼                 ▼
            ┌──────────────┐  ┌──────────────┐  ┌──────────────┐
            │  PostgreSQL  │  │  OWL Ontology│  │  Java Backend│
            │  (org data)  │  │  (RDFLib)    │  │  Entity API  │
            └──────────────┘  └──────────────┘  └──────────────┘
                    │                 │                 │
                    └─────────────────┴─────────────────┘
                                      ▼
                             ┌──────────────────┐
                             │  Entity          │
                             │  Recognition &   │
                             │  Validation      │
                             └──────────────────┘
```

### Component Breakdown

#### **A. Government Data Ingestion Service**
**Location:** `reasoning-service/app/services/gov_org_ingestion.py`

**Responsibilities:**
- Fetch US Government Manual data from GovInfo API
- Parse XML/JSON organizational structure
- Extract agencies, departments, hierarchies
- Handle incremental updates and changes
- Error handling and retry logic

**Key Methods:**
```python
class GovOrgIngestionService:
    def fetch_gov_manual(year: int) -> Dict
    def parse_organizational_structure(xml_data: str) -> List[Organization]
    def extract_agency_metadata(agency_node: Element) -> AgencyMetadata
    def build_hierarchy_tree(agencies: List) -> OrgTree
    def detect_changes(old_data, new_data) -> ChangeSet
```

#### **B. Data Transform & Validation Layer**
**Location:** `reasoning-service/app/services/gov_org_transformer.py`

**Responsibilities:**
- Transform GovInfo data → Schema.org format
- Map to NewsAnalyzer ontology classes
- Generate RDF triples for OWL reasoner
- Validate data consistency
- Enrich with additional metadata

**Mapping:**
```python
GovInfo Agency → Schema.org GovernmentOrganization
GovInfo Department → NewsAnalyzer ExecutiveAgency
GovInfo Bureau → Schema.org GovernmentOrganization (subOrganization)
GovInfo Hierarchy → owl:parentOrganization + na:hasJurisdiction
```

#### **C. PostgreSQL Storage**
**Location:** `backend/src/main/resources/db/migration/V3__government_organizations.sql`

**New Tables:**

```sql
-- Government organizations from US Government Manual
CREATE TABLE government_organizations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    official_name VARCHAR(500) NOT NULL,
    acronym VARCHAR(50),
    org_type VARCHAR(100), -- 'department', 'agency', 'bureau', 'office'
    parent_id UUID REFERENCES government_organizations(id),
    branch VARCHAR(50), -- 'executive', 'legislative', 'judicial'
    established_date DATE,
    authorizing_legislation TEXT,
    mission_statement TEXT,
    website_url VARCHAR(500),
    contact_info JSONB,
    metadata JSONB, -- GovInfo source data
    schema_org_data JSONB, -- Schema.org JSON-LD
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW(),
    govinfo_last_sync TIMESTAMP,
    CONSTRAINT unique_official_name UNIQUE (official_name)
);

-- Indexes
CREATE INDEX idx_gov_org_acronym ON government_organizations(acronym);
CREATE INDEX idx_gov_org_parent ON government_organizations(parent_id);
CREATE INDEX idx_gov_org_type ON government_organizations(org_type);
CREATE INDEX idx_gov_org_branch ON government_organizations(branch);

-- Alternate names and historical names
CREATE TABLE government_organization_aliases (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id UUID REFERENCES government_organizations(id) ON DELETE CASCADE,
    alias_name VARCHAR(500) NOT NULL,
    alias_type VARCHAR(50), -- 'acronym', 'former_name', 'colloquial'
    valid_from DATE,
    valid_to DATE,
    CONSTRAINT unique_org_alias UNIQUE (organization_id, alias_name)
);

-- Organizational relationships (beyond parent-child)
CREATE TABLE government_organization_relationships (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    source_org_id UUID REFERENCES government_organizations(id) ON DELETE CASCADE,
    target_org_id UUID REFERENCES government_organizations(id) ON DELETE CASCADE,
    relationship_type VARCHAR(100), -- 'reports_to', 'coordinates_with', 'regulates', 'funds'
    description TEXT,
    valid_from DATE,
    valid_to DATE
);

-- Jurisdictions and responsibilities
CREATE TABLE government_organization_jurisdictions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id UUID REFERENCES government_organizations(id) ON DELETE CASCADE,
    jurisdiction_area VARCHAR(200), -- 'environmental_protection', 'national_defense', etc.
    description TEXT,
    priority INTEGER DEFAULT 1,
    CONSTRAINT unique_org_jurisdiction UNIQUE (organization_id, jurisdiction_area)
);

-- Historical changes (agency reorganizations, mergers, splits)
CREATE TABLE government_organization_history (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id UUID REFERENCES government_organizations(id) ON DELETE CASCADE,
    change_type VARCHAR(100), -- 'created', 'renamed', 'merged', 'dissolved', 'reorganized'
    change_date DATE NOT NULL,
    description TEXT,
    related_organizations JSONB, -- IDs of orgs involved in merger/split
    source_document VARCHAR(500)
);
```

#### **D. OWL Ontology Integration**
**Location:** `reasoning-service/ontology/newsanalyzer.ttl` (extend existing)

**New OWL Classes:**
```turtle
###  Government Organization Hierarchy
:GovernmentDepartment rdf:type owl:Class ;
    rdfs:subClassOf :ExecutiveAgency ;
    rdfs:label "Cabinet-Level Department"@en ;
    rdfs:comment "A Cabinet-level executive department (15 total)"@en .

:IndependentAgency rdf:type owl:Class ;
    rdfs:subClassOf :ExecutiveAgency ;
    rdfs:label "Independent Agency"@en ;
    rdfs:comment "An executive agency outside Cabinet departments"@en .

:GovernmentBureau rdf:type owl:Class ;
    rdfs:subClassOf schema:GovernmentOrganization ;
    rdfs:label "Bureau or Office"@en ;
    rdfs:comment "A sub-unit within a department or agency"@en .
```

**New Properties:**
```turtle
###  Organizational Hierarchy
:reportsTo rdf:type owl:ObjectProperty ;
    rdfs:domain schema:GovernmentOrganization ;
    rdfs:range schema:GovernmentOrganization ;
    rdfs:label "reports to"@en .

:oversees rdf:type owl:ObjectProperty ;
    rdfs:domain schema:GovernmentOrganization ;
    rdfs:range schema:GovernmentOrganization ;
    rdfs:label "oversees"@en ;
    owl:inverseOf :reportsTo .

:hasSubOrganization rdf:type owl:ObjectProperty ;
    rdfs:domain schema:GovernmentOrganization ;
    rdfs:range schema:GovernmentOrganization ;
    rdfs:label "has sub-organization"@en .

:establishedByLegislation rdf:type owl:ObjectProperty ;
    rdfs:domain schema:GovernmentOrganization ;
    rdfs:range :Legislation ;
    rdfs:label "established by legislation"@en .

###  Data Properties
:officialAcronym rdf:type owl:DatatypeProperty ;
    rdfs:domain schema:GovernmentOrganization ;
    rdfs:range xsd:string ;
    rdfs:label "official acronym"@en .

:establishedDate rdf:type owl:DatatypeProperty ;
    rdfs:domain schema:GovernmentOrganization ;
    rdfs:range xsd:date ;
    rdfs:label "established date"@en .

:govBranch rdf:type owl:DatatypeProperty ;
    rdfs:domain schema:GovernmentOrganization ;
    rdfs:range xsd:string ;
    rdfs:label "government branch"@en ;
    rdfs:comment "executive, legislative, or judicial"@en .
```

#### **E. Java Backend API Endpoints**
**Location:** `backend/src/main/java/org/newsanalyzer/controller/GovernmentOrgController.java`

**New REST Endpoints:**
```java
@RestController
@RequestMapping("/api/government-organizations")
public class GovernmentOrgController {

    // GET /api/government-organizations
    // List all government organizations
    @GetMapping
    public Page<GovernmentOrganization> listAll(Pageable pageable);

    // GET /api/government-organizations/{id}
    // Get organization details
    @GetMapping("/{id}")
    public GovernmentOrganization getById(@PathVariable UUID id);

    // GET /api/government-organizations/search
    // Search by name or acronym
    @GetMapping("/search")
    public List<GovernmentOrganization> search(@RequestParam String query);

    // GET /api/government-organizations/{id}/hierarchy
    // Get organizational hierarchy (parents + children)
    @GetMapping("/{id}/hierarchy")
    public OrgHierarchy getHierarchy(@PathVariable UUID id);

    // GET /api/government-organizations/{id}/jurisdiction
    // Get jurisdiction areas
    @GetMapping("/{id}/jurisdiction")
    public List<Jurisdiction> getJurisdiction(@PathVariable UUID id);

    // POST /api/government-organizations/validate-entity
    // Validate if entity name matches official government org
    @PostMapping("/validate-entity")
    public ValidationResult validateEntity(@RequestBody EntityValidationRequest request);

    // POST /api/government-organizations/sync
    // Trigger sync from GovInfo API (admin only)
    @PostMapping("/sync")
    @PreAuthorize("hasRole('ADMIN')")
    public SyncResult triggerSync(@RequestParam int year);
}
```

#### **F. Python Reasoning Service Integration**
**Location:** `reasoning-service/app/api/government_orgs.py`

**New Endpoints:**
```python
@router.post("/government-orgs/ingest")
async def ingest_gov_manual(year: int = 2024):
    """Fetch and process US Government Manual for given year"""

@router.post("/government-orgs/enrich-entity")
async def enrich_entity_with_gov_data(entity: Dict) -> Dict:
    """Enrich entity with official government org data"""

@router.get("/government-orgs/hierarchy/{org_id}")
async def get_org_hierarchy(org_id: str) -> Dict:
    """Get RDF hierarchy for organization"""

@router.post("/government-orgs/query-sparql")
async def query_gov_org_structure(query: SPARQLRequest) -> List[Dict]:
    """Query government org structure using SPARQL"""
```

---

## 3. Data Ingestion Workflow

### Process Flow

```
┌─────────────────────────────────────────────────────────────────┐
│           Government Org Chart Ingestion Workflow                │
└─────────────────────────────────────────────────────────────────┘

STEP 1: Trigger Ingestion
    ├─ Manual: Admin clicks "Sync Government Data"
    ├─ Scheduled: Cron job runs quarterly
    └─ Webhook: GovInfo API update notification (future)

STEP 2: Fetch Data from GovInfo API
    ├─ GET /collections/GOVMAN/{year}
    ├─ Parse XML organizational structure
    ├─ Download detailed agency metadata
    └─ Handle pagination and rate limits

STEP 3: Parse & Transform
    ├─ Extract agencies, departments, bureaus
    ├─ Build hierarchical tree structure
    ├─ Map to Schema.org types
    ├─ Generate unique identifiers
    └─ Validate data completeness

STEP 4: Detect Changes
    ├─ Compare with existing database
    ├─ Identify new agencies
    ├─ Detect renamed agencies
    ├─ Find dissolved agencies
    └─ Track structural changes

STEP 5: Update Database
    ├─ Transaction-based updates
    ├─ Insert new organizations
    ├─ Update modified organizations
    ├─ Create historical records
    └─ Update relationships

STEP 6: Update OWL Ontology
    ├─ Generate RDF triples
    ├─ Load into OWL reasoner
    ├─ Run inference
    └─ Update knowledge graph

STEP 7: Validation & Reconciliation
    ├─ Check referential integrity
    ├─ Validate Schema.org JSON-LD
    ├─ Test SPARQL queries
    └─ Generate sync report
```

### Implementation: Ingestion Service

```python
# reasoning-service/app/services/gov_org_ingestion.py

import requests
from typing import Dict, List, Optional
from datetime import datetime
import xml.etree.ElementTree as ET
from rdflib import Graph, Namespace, URIRef, Literal
from rdflib.namespace import RDF, RDFS, OWL

class GovOrgIngestionService:
    """Service for ingesting US Government Manual organizational data"""

    def __init__(self, api_key: str):
        self.api_key = api_key
        self.base_url = "https://api.govinfo.gov"
        self.session = requests.Session()
        self.session.headers.update({"X-Api-Key": api_key})

    def fetch_gov_manual(self, year: int = 2024) -> Dict:
        """
        Fetch US Government Manual for specified year

        Returns:
            Dict with organizational structure and metadata
        """
        # Get collection metadata
        url = f"{self.base_url}/collections/GOVMAN/{year}"
        response = self.session.get(url, params={"pageSize": 100})
        response.raise_for_status()

        data = response.json()

        # Fetch detailed XML for each package
        packages = []
        for package in data.get("packages", []):
            package_id = package["packageId"]
            xml_data = self._fetch_package_xml(package_id)
            packages.append({
                "id": package_id,
                "title": package.get("title"),
                "xml": xml_data,
                "dateIssued": package.get("dateIssued")
            })

        return {
            "year": year,
            "packages": packages,
            "fetched_at": datetime.utcnow().isoformat()
        }

    def _fetch_package_xml(self, package_id: str) -> str:
        """Fetch XML content for a specific package"""
        url = f"{self.base_url}/packages/{package_id}/xml"
        response = self.session.get(url)
        response.raise_for_status()
        return response.text

    def parse_organizational_structure(self, xml_data: str) -> List[Dict]:
        """
        Parse XML to extract organizational structure

        Returns:
            List of organizations with metadata and hierarchy
        """
        root = ET.fromstring(xml_data)
        organizations = []

        # Parse executive branch agencies
        for agency in root.findall(".//AGENCY"):
            org = self._parse_agency_node(agency)
            organizations.append(org)

        # Parse departments
        for dept in root.findall(".//DEPARTMENT"):
            org = self._parse_department_node(dept)
            organizations.append(org)

        return organizations

    def _parse_agency_node(self, node: ET.Element) -> Dict:
        """Extract agency metadata from XML node"""
        return {
            "type": "agency",
            "name": node.findtext("NAME", "").strip(),
            "acronym": node.findtext("ACRONYM", "").strip(),
            "established": node.findtext("ESTABLISHED", "").strip(),
            "mission": node.findtext("MISSION", "").strip(),
            "website": node.findtext("WEBSITE", "").strip(),
            "parent": node.findtext("PARENT", "").strip(),
            "sub_organizations": [
                self._parse_subunit(sub)
                for sub in node.findall(".//SUBUNIT")
            ]
        }

    def _parse_department_node(self, node: ET.Element) -> Dict:
        """Extract department metadata from XML node"""
        return {
            "type": "department",
            "name": node.findtext("NAME", "").strip(),
            "acronym": node.findtext("ACRONYM", "").strip(),
            "secretary": node.findtext("SECRETARY", "").strip(),
            "established": node.findtext("ESTABLISHED", "").strip(),
            "authorizing_law": node.findtext("AUTHORIZING_LAW", "").strip(),
            "website": node.findtext("WEBSITE", "").strip(),
            "bureaus": [
                self._parse_bureau(bureau)
                for bureau in node.findall(".//BUREAU")
            ]
        }

    def transform_to_schema_org(self, org: Dict) -> Dict:
        """
        Transform parsed organization to Schema.org GovernmentOrganization

        Returns:
            Schema.org JSON-LD representation
        """
        schema_org = {
            "@context": "https://schema.org",
            "@type": "GovernmentOrganization",
            "name": org["name"],
            "legalName": org["name"],
            "url": org.get("website"),
            "foundingDate": org.get("established"),
            "description": org.get("mission")
        }

        # Add acronym as alternate name
        if org.get("acronym"):
            schema_org["alternateName"] = org["acronym"]

        # Add parent organization
        if org.get("parent"):
            schema_org["parentOrganization"] = {
                "@type": "GovernmentOrganization",
                "name": org["parent"]
            }

        # Add sub-organizations
        if org.get("sub_organizations"):
            schema_org["subOrganization"] = [
                {
                    "@type": "GovernmentOrganization",
                    "name": sub["name"]
                }
                for sub in org["sub_organizations"]
            ]

        return schema_org

    def generate_rdf_triples(self, org: Dict) -> Graph:
        """
        Generate RDF triples for OWL ontology integration

        Returns:
            RDFLib Graph with organization triples
        """
        g = Graph()
        NA = Namespace("http://newsanalyzer.org/ontology#")
        SCHEMA = Namespace("http://schema.org/")

        g.bind("na", NA)
        g.bind("schema", SCHEMA)

        # Create organization URI
        org_uri = URIRef(f"http://newsanalyzer.org/entity/gov/{org['acronym'].lower()}")

        # Add type
        if org["type"] == "department":
            g.add((org_uri, RDF.type, NA.GovernmentDepartment))
        elif org["type"] == "agency":
            g.add((org_uri, RDF.type, NA.IndependentAgency))

        # Add properties
        g.add((org_uri, SCHEMA.name, Literal(org["name"])))

        if org.get("acronym"):
            g.add((org_uri, NA.officialAcronym, Literal(org["acronym"])))

        if org.get("established"):
            g.add((org_uri, NA.establishedDate, Literal(org["established"])))

        if org.get("website"):
            g.add((org_uri, SCHEMA.url, Literal(org["website"])))

        # Add hierarchical relationships
        if org.get("parent"):
            parent_uri = URIRef(f"http://newsanalyzer.org/entity/gov/{org['parent'].lower().replace(' ', '_')}")
            g.add((org_uri, NA.reportsTo, parent_uri))

        return g
```

---

## 4. Entity Validation & Enrichment

### Use Cases

#### Use Case 1: Validate Detected Entity
```
User Input: "The EPA announced new regulations..."

1. spaCy extracts: "EPA" (type: government_org)
2. System queries government_organizations table
3. Finds match: "Environmental Protection Agency"
4. Enriches entity with:
   - Official name
   - Parent organization (Executive Branch)
   - Jurisdiction areas (environmental protection)
   - Website, contact info
   - Related agencies
```

#### Use Case 2: Detect Incorrect References
```
User Input: "The Department of Homeland Protection..."

1. System searches for "Department of Homeland Protection"
2. No exact match found
3. Fuzzy match suggests: "Department of Homeland Security"
4. Flag as potential error in news article
5. Store for fact-checking report
```

#### Use Case 3: Hierarchical Context
```
User Input: "The Bureau of Land Management..."

1. Validate: Bureau of Land Management exists
2. Enrich with hierarchy:
   - Parent: Department of the Interior
   - Grandparent: Executive Branch
   - Related: U.S. Forest Service, National Park Service
3. Add to entity graph for reasoning
```

### Implementation: Entity Validator

```python
# reasoning-service/app/services/gov_entity_validator.py

from typing import Dict, Optional, List
from fuzzywuzzy import fuzz
import sqlalchemy as sa

class GovEntityValidator:
    """Validate and enrich government entities against official data"""

    def __init__(self, db_session, owl_reasoner):
        self.db = db_session
        self.reasoner = owl_reasoner

    def validate_entity(self, entity_text: str, entity_type: str) -> Dict:
        """
        Validate entity against government organizations database

        Returns:
            {
                "is_valid": bool,
                "confidence": float,
                "official_name": str,
                "match_type": "exact" | "acronym" | "fuzzy" | "none",
                "suggestions": List[str],
                "enrichment_data": Dict
            }
        """
        if entity_type != "government_org":
            return {"is_valid": None, "confidence": 0.0}

        # Try exact match
        exact_match = self._exact_match(entity_text)
        if exact_match:
            return {
                "is_valid": True,
                "confidence": 1.0,
                "match_type": "exact",
                "official_name": exact_match["official_name"],
                "enrichment_data": exact_match
            }

        # Try acronym match
        acronym_match = self._acronym_match(entity_text)
        if acronym_match:
            return {
                "is_valid": True,
                "confidence": 0.95,
                "match_type": "acronym",
                "official_name": acronym_match["official_name"],
                "enrichment_data": acronym_match
            }

        # Try fuzzy match
        fuzzy_matches = self._fuzzy_match(entity_text, threshold=85)
        if fuzzy_matches:
            best_match = fuzzy_matches[0]
            return {
                "is_valid": True,
                "confidence": best_match["score"] / 100,
                "match_type": "fuzzy",
                "official_name": best_match["official_name"],
                "suggestions": [m["official_name"] for m in fuzzy_matches[:3]],
                "enrichment_data": best_match
            }

        # No match found
        return {
            "is_valid": False,
            "confidence": 0.0,
            "match_type": "none",
            "suggestions": self._get_suggestions(entity_text)
        }

    def enrich_entity(self, entity: Dict, gov_org_data: Dict) -> Dict:
        """
        Enrich entity with official government org data

        Returns:
            Enhanced entity with official metadata, hierarchy, jurisdiction
        """
        enriched = entity.copy()

        # Add official information
        enriched["official_name"] = gov_org_data["official_name"]
        enriched["acronym"] = gov_org_data.get("acronym")
        enriched["org_type"] = gov_org_data["org_type"]
        enriched["branch"] = gov_org_data["branch"]
        enriched["website"] = gov_org_data.get("website_url")

        # Add hierarchy
        enriched["hierarchy"] = self._get_hierarchy(gov_org_data["id"])

        # Add jurisdiction
        enriched["jurisdiction"] = self._get_jurisdiction(gov_org_data["id"])

        # Add Schema.org data
        enriched["schema_org_data"] = gov_org_data["schema_org_data"]

        # Add OWL reasoning data
        enriched["owl_types"] = self._get_owl_types(gov_org_data)

        return enriched

    def _exact_match(self, text: str) -> Optional[Dict]:
        """Find exact match in database"""
        result = self.db.execute(
            sa.text("SELECT * FROM government_organizations WHERE official_name = :name"),
            {"name": text}
        ).fetchone()
        return dict(result) if result else None

    def _acronym_match(self, text: str) -> Optional[Dict]:
        """Find match by acronym"""
        result = self.db.execute(
            sa.text("SELECT * FROM government_organizations WHERE acronym = :acronym"),
            {"acronym": text.upper()}
        ).fetchone()
        return dict(result) if result else None

    def _fuzzy_match(self, text: str, threshold: int = 80) -> List[Dict]:
        """Find fuzzy matches using Levenshtein distance"""
        all_orgs = self.db.execute(
            sa.text("SELECT * FROM government_organizations")
        ).fetchall()

        matches = []
        for org in all_orgs:
            score = fuzz.ratio(text.lower(), org["official_name"].lower())
            if score >= threshold:
                org_dict = dict(org)
                org_dict["score"] = score
                matches.append(org_dict)

        return sorted(matches, key=lambda x: x["score"], reverse=True)
```

---

## 5. API Integration Examples

### Example 1: Sync Government Data

```bash
# Trigger ingestion of 2024 Government Manual
POST /api/government-organizations/sync?year=2024

Response:
{
  "status": "success",
  "year": 2024,
  "organizations_added": 142,
  "organizations_updated": 38,
  "organizations_removed": 2,
  "processing_time_seconds": 45,
  "sync_report": {
    "new_agencies": ["Office of the National Cyber Director"],
    "renamed": ["Bureau of Indian Affairs → Bureau of Indian Education"],
    "dissolved": []
  }
}
```

### Example 2: Validate Entity

```bash
# Validate extracted entity
POST /api/government-organizations/validate-entity

Request:
{
  "entity_text": "EPA",
  "entity_type": "government_org",
  "context": "The EPA announced new regulations..."
}

Response:
{
  "is_valid": true,
  "confidence": 1.0,
  "match_type": "acronym",
  "official_name": "Environmental Protection Agency",
  "enrichment_data": {
    "id": "8d7a3b21-...",
    "acronym": "EPA",
    "org_type": "independent_agency",
    "branch": "executive",
    "parent": null,
    "established_date": "1970-12-02",
    "website": "https://www.epa.gov",
    "mission": "Protect human health and the environment",
    "jurisdiction_areas": [
      "environmental_protection",
      "pollution_control",
      "climate_policy"
    ]
  }
}
```

### Example 3: Get Organizational Hierarchy

```bash
# Get hierarchy for an organization
GET /api/government-organizations/{epa-id}/hierarchy

Response:
{
  "organization": {
    "id": "8d7a3b21-...",
    "name": "Environmental Protection Agency",
    "acronym": "EPA"
  },
  "parents": [
    {
      "level": 1,
      "name": "Executive Branch",
      "type": "branch"
    }
  ],
  "children": [
    {
      "name": "Office of Air and Radiation",
      "type": "office"
    },
    {
      "name": "Office of Water",
      "type": "office"
    },
    {
      "name": "Office of Land and Emergency Management",
      "type": "office"
    }
  ],
  "peers": [
    {
      "name": "National Aeronautics and Space Administration",
      "acronym": "NASA",
      "relationship": "independent_agency"
    }
  ]
}
```

### Example 4: SPARQL Query on Government Structure

```bash
# Query organizational structure using SPARQL
POST /entities/query/sparql

Request:
{
  "query": "
    PREFIX na: <http://newsanalyzer.org/ontology#>
    PREFIX schema: <http://schema.org/>

    SELECT ?agency ?name ?parent
    WHERE {
      ?agency a na:ExecutiveAgency .
      ?agency schema:name ?name .
      ?agency na:reportsTo ?parent .
      ?parent schema:name 'Department of Defense' .
    }
  "
}

Response:
{
  "results": [
    {
      "agency": "http://newsanalyzer.org/entity/gov/darpa",
      "name": "Defense Advanced Research Projects Agency",
      "parent": "http://newsanalyzer.org/entity/gov/dod"
    },
    {
      "agency": "http://newsanalyzer.org/entity/gov/dhs",
      "name": "Defense Health Agency",
      "parent": "http://newsanalyzer.org/entity/gov/dod"
    }
  ],
  "count": 2
}
```

---

## 6. Deployment & Operations

### Configuration

```yaml
# application.yml
government:
  data:
    govinfo:
      api_key: ${GOVINFO_API_KEY}
      base_url: https://api.govinfo.gov
      rate_limit: 1000  # requests per hour
      timeout: 30  # seconds
    sync:
      enabled: true
      schedule: "0 0 1 */3 * ?"  # Quarterly (1st day, midnight)
      year: 2024
      auto_update: true
    validation:
      fuzzy_threshold: 85
      confidence_threshold: 0.7
    cache:
      ttl: 2592000  # 30 days in seconds
```

### Scheduled Sync Job

```java
// backend/src/main/java/org/newsanalyzer/jobs/GovOrgSyncJob.java

@Component
@ConditionalOnProperty(name = "government.data.sync.enabled", havingValue = "true")
public class GovOrgSyncJob {

    @Scheduled(cron = "${government.data.sync.schedule}")
    public void syncGovernmentOrganizations() {
        logger.info("Starting scheduled government organization sync");

        int currentYear = LocalDate.now().getYear();

        try {
            SyncResult result = govOrgSyncService.syncFromGovInfo(currentYear);

            logger.info("Sync completed: {} added, {} updated, {} removed",
                result.getAdded(), result.getUpdated(), result.getRemoved());

            // Send notification to admins
            notificationService.notifyAdmins("Gov Org Sync Complete", result);

        } catch (Exception e) {
            logger.error("Government organization sync failed", e);
            alertService.sendAlert("Gov Org Sync Failed", e.getMessage());
        }
    }
}
```

### Monitoring & Observability

```java
// Metrics to track
@Timed(value = "gov_org.sync.duration")
@Counted(value = "gov_org.sync.count")
public SyncResult syncFromGovInfo(int year) {
    // Sync logic
}

@Timed(value = "gov_org.validation.duration")
@Counted(value = "gov_org.validation.count")
public ValidationResult validateEntity(String entityText) {
    // Validation logic
}

// Alerts
- GovInfo API rate limit approaching (>80%)
- Sync job failures
- Data validation errors >10%
- Database inconsistencies detected
```

---

## 7. Testing Strategy

### Unit Tests

```python
# tests/test_gov_org_ingestion.py

def test_fetch_gov_manual():
    """Test fetching Government Manual from GovInfo API"""
    service = GovOrgIngestionService(api_key="test_key")
    data = service.fetch_gov_manual(year=2024)

    assert data["year"] == 2024
    assert len(data["packages"]) > 0
    assert "fetched_at" in data

def test_parse_agency_xml():
    """Test parsing agency data from XML"""
    xml_data = """
    <AGENCY>
        <NAME>Environmental Protection Agency</NAME>
        <ACRONYM>EPA</ACRONYM>
        <ESTABLISHED>1970-12-02</ESTABLISHED>
    </AGENCY>
    """

    service = GovOrgIngestionService(api_key="test_key")
    orgs = service.parse_organizational_structure(xml_data)

    assert len(orgs) == 1
    assert orgs[0]["name"] == "Environmental Protection Agency"
    assert orgs[0]["acronym"] == "EPA"

def test_entity_validation_exact_match():
    """Test exact match entity validation"""
    validator = GovEntityValidator(db_session, owl_reasoner)
    result = validator.validate_entity(
        "Environmental Protection Agency",
        "government_org"
    )

    assert result["is_valid"] == True
    assert result["confidence"] == 1.0
    assert result["match_type"] == "exact"
```

### Integration Tests

```java
// GovernmentOrgIntegrationTest.java

@Test
public void testSyncAndValidateWorkflow() {
    // 1. Trigger sync
    SyncResult syncResult = govOrgSyncService.syncFromGovInfo(2024);
    assertThat(syncResult.getStatus()).isEqualTo("success");

    // 2. Validate entity
    ValidationResult validation = govOrgController.validateEntity(
        new EntityValidationRequest("EPA", "government_org")
    );
    assertThat(validation.isValid()).isTrue();
    assertThat(validation.getOfficialName()).isEqualTo("Environmental Protection Agency");

    // 3. Get hierarchy
    OrgHierarchy hierarchy = govOrgController.getHierarchy(validation.getOrganizationId());
    assertThat(hierarchy.getParents()).isEmpty(); // EPA is independent
    assertThat(hierarchy.getChildren()).isNotEmpty();
}
```

---

## 8. Future Enhancements

### Phase 2+: Advanced Features

**1. Federal Register Integration**
- Track agency rule-making activity
- Link regulations to agencies
- Monitor policy changes

**2. Congressional Directory Integration**
- Link legislators to committees
- Track legislative jurisdiction
- Connect bills to agencies

**3. USASpending.gov Integration**
- Track agency budgets and spending
- Analyze contract awards
- Monitor grants and funding

**4. Real-Time Updates**
- GovInfo webhook subscriptions
- Incremental daily updates
- Change notifications

**5. Historical Analysis**
- Track agency evolution over time
- Analyze organizational changes
- Study merger/dissolution patterns

**6. International Expansion**
- UK government structure
- EU agencies and institutions
- Other democratic governments

---

## 9. Security & Privacy

### Data Protection

- **API Key Security** - Store GovInfo API key in secrets manager
- **Rate Limiting** - Respect GovInfo API rate limits
- **Data Validation** - Sanitize all external data
- **Access Control** - Restrict sync endpoint to admins only

### Compliance

- **Public Domain** - US Government Manual is public domain
- **Attribution** - Credit GovInfo as data source
- **Terms of Service** - Comply with GovInfo API ToS
- **GDPR** - No personal data collected (organizational data only)

---

## 10. Success Metrics

### KPIs

**Data Quality:**
- Entity validation accuracy: >95%
- False positive rate: <5%
- Data freshness: <90 days
- Coverage: >90% of mentioned agencies

**Performance:**
- Validation latency: <100ms
- Sync job duration: <5 minutes
- API response time: <200ms

**Usage:**
- Entity validations per day
- Enrichment requests per day
- SPARQL queries per day
- API error rate: <1%

---

## Conclusion

This architecture provides a robust, scalable solution for integrating authoritative US Government organizational data into NewsAnalyzer v2. By leveraging the GovInfo API, we ensure access to official, up-to-date government structure information that enhances entity recognition, validation, and reasoning capabilities.

**Next Steps:**
1. Obtain GovInfo API key
2. Implement ingestion service (Phase 2.1)
3. Create database migrations (Phase 2.1)
4. Extend OWL ontology (Phase 2.1)
5. Build validation service (Phase 2.2)
6. Add REST API endpoints (Phase 2.2)
7. Implement scheduled sync job (Phase 2.3)
8. Deploy and monitor (Phase 2.3)

**Estimated Implementation Time:** 2-3 weeks (one developer)

**Dependencies:**
- GovInfo API key (free)
- PostgreSQL database (existing)
- OWL reasoner (Phase 3, complete)
- Python FastAPI service (existing)

---

*Document prepared by Winston, Architect Agent*
*Date: 2025-11-21*
*Status: Ready for Review & Approval*
