# Schema.org and OWL Integration Guide

## Overview

NewsAnalyzer v2 uses **Schema.org JSON-LD** as the native format for entity representation, with optional OWL (Web Ontology Language) for advanced reasoning. This document explains the integration strategy.

---

## Important: "Entity" vs "Thing" Terminology

**Schema.org uses "Thing" as the root class**, not "Entity". This is intentional and correct:

- **"Entity"** = Our **internal database concept** (table name: `entities`)
- **"Thing"** = **Schema.org's root class** (all Schema.org types inherit from Thing)

**Our internal `entity_type` maps to Schema.org types:**
```
NewsAnalyzer Internal    →  Schema.org Hierarchy
─────────────────────────────────────────────────
entities (table)         →  Thing (root class)
├── entity_type = "person"         → Person
├── entity_type = "government_org" → GovernmentOrganization
├── entity_type = "organization"   → Organization
├── entity_type = "location"       → Place
├── entity_type = "event"          → Event
└── entity_type = "concept"        → Thing or CreativeWork
```

This separation provides:
- **Database optimization** via `entity_type` (indexes, filtering, business logic)
- **Semantic web standards** via `schema_org_type` (interoperability, LLMs, SEO)

---

## 1. Architecture Integration Points

### 1.1 Data Storage (PostgreSQL)

**Entities Table:**
```sql
CREATE TABLE entities (
    id UUID PRIMARY KEY,

    -- Internal classification (for our queries/business logic)
    entity_type VARCHAR(50) NOT NULL,  -- 'government_org', 'person', etc.
    name VARCHAR(500) NOT NULL,
    properties JSONB DEFAULT '{}'::jsonb,

    -- Schema.org semantic layer (standardized vocabulary)
    schema_org_type VARCHAR(255),      -- 'Person', 'GovernmentOrganization', etc.
    schema_org_data JSONB,             -- Full JSON-LD representation

    source VARCHAR(100),
    confidence_score REAL DEFAULT 1.0,
    verified BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);
```

**Dual-Layer Design Benefits:**
- `entity_type` = Fast queries, internal business logic, database optimization
- `schema_org_type` = Standard vocabulary, external interoperability, future-proof

**Why JSONB?**
- Stores Schema.org JSON-LD natively
- Queryable with PostgreSQL JSON operators
- No rigid schema enforcement
- Supports Schema.org's flexible vocabulary
- Easily extensible as Schema.org evolves

### 1.2 Backend (Spring Boot)

**Entity Model:**
```java
@Entity
@Table(name = "entities")
public class Entity {
    @Id
    @GeneratedValue
    private UUID id;

    @Enumerated(EnumType.STRING)
    private EntityType entityType;

    private String name;

    @Type(JsonBinaryType.class)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> properties;

    // Schema.org support
    @Column(name = "schema_org_type")
    private String schemaOrgType;

    @Type(JsonBinaryType.class)
    @Column(name = "schema_org_data", columnDefinition = "jsonb")
    private Map<String, Object> schemaOrgData;
}
```

### 1.3 Reasoning Service (Python/FastAPI)

**Entity Extraction & Schema.org Mapping:**
```python
from rdflib import Graph, Namespace, Literal, URIRef
from rdflib.namespace import RDF, RDFS, OWL

SCHEMA = Namespace("http://schema.org/")

def extract_and_map_entity(text: str) -> dict:
    """
    Extract entity from text and map to Schema.org JSON-LD
    """
    # Step 1: Extract entity using NLP (spaCy)
    entity_text, entity_type = extract_with_spacy(text)

    # Step 2: Map to Schema.org type
    schema_type = map_to_schema_org(entity_type)

    # Step 3: Generate JSON-LD
    json_ld = {
        "@context": "https://schema.org",
        "@type": schema_type,
        "name": entity_text,
        # Additional properties...
    }

    return {
        "entity_type": entity_type,
        "schema_org_type": schema_type,
        "schema_org_data": json_ld
    }
```

---

## 2. Schema.org Type Mappings

### 2.1 Government Entities

| Internal Type | Schema.org Type | Example |
|---------------|-----------------|---------|
| government_org | GovernmentOrganization | U.S. Senate, EPA |
| government_service | GovernmentService | Medicare, Social Security |
| legislation | Legislation | H.R. 1234, S. 567 |

**Example JSON-LD:**
```json
{
  "@context": "https://schema.org",
  "@type": "GovernmentOrganization",
  "@id": "https://newsanalyzer.org/entities/us-senate",
  "name": "United States Senate",
  "alternateName": ["U.S. Senate", "Senate"],
  "url": "https://www.senate.gov",
  "parentOrganization": {
    "@type": "GovernmentOrganization",
    "name": "United States Congress"
  },
  "address": {
    "@type": "PostalAddress",
    "addressLocality": "Washington",
    "addressRegion": "DC",
    "addressCountry": "US"
  }
}
```

### 2.2 People

| Internal Type | Schema.org Type | Example |
|---------------|-----------------|---------|
| person | Person | Joe Biden, Nancy Pelosi |
| politician | Person (with role) | Senator, Representative |

**Example JSON-LD:**
```json
{
  "@context": "https://schema.org",
  "@type": "Person",
  "@id": "https://newsanalyzer.org/entities/joe-biden",
  "name": "Joseph R. Biden Jr.",
  "alternateName": ["Joe Biden", "President Biden"],
  "jobTitle": "President of the United States",
  "worksFor": {
    "@type": "GovernmentOrganization",
    "name": "Executive Office of the President"
  },
  "memberOf": {
    "@type": "PoliticalParty",
    "name": "Democratic Party"
  }
}
```

### 2.3 Organizations

| Internal Type | Schema.org Type | Example |
|---------------|-----------------|---------|
| organization | Organization | ACLU, NRA |
| political_party | PoliticalParty | Democratic Party |
| news_media | NewsMediaOrganization | New York Times |

### 2.4 Events

| Internal Type | Schema.org Type | Example |
|---------------|-----------------|---------|
| event | Event | Presidential Debate |
| legislative_event | Event (with legislativeSession) | Senate Hearing |
| election | Event | 2024 Presidential Election |

---

## 3. OWL Integration (Advanced Reasoning)

### 3.1 When to Use OWL vs Schema.org

**Use Schema.org (JSON-LD) for:**
- ✅ Entity representation and storage
- ✅ LLM consumption
- ✅ Web publishing and SEO
- ✅ Data interchange

**Use OWL for:**
- ✅ Advanced logical reasoning
- ✅ Inference rules (if A, then B)
- ✅ Consistency checking
- ✅ Complex relationship modeling

### 3.2 OWL Ontology Structure

**NewsAnalyzer Custom Ontology:**
```turtle
@prefix na: <https://newsanalyzer.org/ontology#> .
@prefix schema: <http://schema.org/> .
@prefix owl: <http://www.w3.org/2002/07/owl#> .
@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> .

# NewsAnalyzer Ontology Definition
na:NewsAnalyzerOntology a owl:Ontology ;
    rdfs:label "NewsAnalyzer Ontology" ;
    rdfs:comment "Extends Schema.org for news analysis" .

# Extend Schema.org GovernmentOrganization
na:LegislativeBody a owl:Class ;
    rdfs:subClassOf schema:GovernmentOrganization ;
    rdfs:label "Legislative Body" ;
    rdfs:comment "A legislative branch organization (e.g., Congress, Senate)" .

na:ExecutiveAgency a owl:Class ;
    rdfs:subClassOf schema:GovernmentOrganization ;
    rdfs:label "Executive Agency" ;
    rdfs:comment "An executive branch agency (e.g., EPA, FBI)" .

# Custom Properties
na:hasJurisdiction a owl:ObjectProperty ;
    rdfs:domain schema:GovernmentOrganization ;
    rdfs:range schema:Place ;
    rdfs:label "has jurisdiction" .

na:passedLegislation a owl:ObjectProperty ;
    rdfs:domain na:LegislativeBody ;
    rdfs:range schema:Legislation ;
    rdfs:label "passed legislation" .

# Inference Rules (via OWL reasoning)
# If a Person works for a LegislativeBody, they are a Legislator
na:Legislator a owl:Class ;
    rdfs:subClassOf schema:Person ;
    owl:equivalentClass [
        a owl:Restriction ;
        owl:onProperty schema:worksFor ;
        owl:someValuesFrom na:LegislativeBody
    ] .
```

### 3.3 Python Implementation with RDFLib + OWL

**Install Dependencies:**
```bash
pip install rdflib owlrl
```

**Example: OWL Reasoning with Inference:**
```python
from rdflib import Graph, Namespace, Literal, URIRef
from rdflib.namespace import RDF, RDFS, OWL
import owlrl

# Load NewsAnalyzer ontology
g = Graph()
g.parse("newsanalyzer-ontology.ttl", format="turtle")

# Add entity data (from database)
NA = Namespace("https://newsanalyzer.org/ontology#")
SCHEMA = Namespace("http://schema.org/")

# Example: Joe Biden works for Executive Office
biden = URIRef("https://newsanalyzer.org/entities/joe-biden")
g.add((biden, RDF.type, SCHEMA.Person))
g.add((biden, SCHEMA.name, Literal("Joe Biden")))
g.add((biden, SCHEMA.worksFor, URIRef("https://newsanalyzer.org/entities/executive-office")))

# Run OWL reasoner
owlrl.DeductiveClosure(owlrl.OWLRL_Semantics).expand(g)

# Query inferred facts
query = """
    PREFIX schema: <http://schema.org/>
    PREFIX na: <https://newsanalyzer.org/ontology#>

    SELECT ?person ?type WHERE {
        ?person a ?type .
        FILTER(?type IN (na:Legislator, na:ExecutiveOfficial))
    }
"""

results = g.query(query)
for row in results:
    print(f"{row.person} is a {row.type}")
```

---

## 4. Implementation Roadmap

### Phase 1: Schema.org Foundation (MVP)
1. ✅ Database schema with `schema_org_type` and `schema_org_data` columns (DONE)
2. ⏳ Java Entity model with Schema.org fields
3. ⏳ Python service: Entity extraction → Schema.org mapping
4. ⏳ Backend service: Store and retrieve JSON-LD
5. ⏳ Frontend: Display Schema.org entities

### Phase 2: Schema.org Enrichment
1. External entity linking (Wikidata, DBpedia)
2. Schema.org property expansion
3. JSON-LD publishing for SEO
4. Schema.org validation

### Phase 3: OWL Reasoning (Advanced)
1. Define NewsAnalyzer custom ontology (.ttl file)
2. Python service: RDFLib + OWL-RL reasoner
3. Inference rules for entity classification
4. Consistency checking
5. Relationship inference

---

## 5. Example: Full Entity Lifecycle

### Step 1: Article Analysis
```
Article text: "Senator Elizabeth Warren criticized the EPA's new regulations..."
```

### Step 2: Entity Extraction (Python/FastAPI)
```python
POST /entities/extract
{
  "text": "Senator Elizabeth Warren criticized the EPA's new regulations..."
}

Response:
{
  "entities": [
    {
      "text": "Senator Elizabeth Warren",
      "entity_type": "person",
      "schema_org_type": "Person",
      "schema_org_data": {
        "@context": "https://schema.org",
        "@type": "Person",
        "name": "Elizabeth Warren",
        "jobTitle": "United States Senator",
        "worksFor": {
          "@type": "GovernmentOrganization",
          "name": "United States Senate"
        }
      }
    },
    {
      "text": "EPA",
      "entity_type": "government_org",
      "schema_org_type": "GovernmentOrganization",
      "schema_org_data": {
        "@context": "https://schema.org",
        "@type": "GovernmentOrganization",
        "name": "Environmental Protection Agency",
        "alternateName": "EPA",
        "url": "https://www.epa.gov"
      }
    }
  ]
}
```

### Step 3: Store in PostgreSQL (Spring Boot)
```java
@PostMapping("/api/entities")
public Entity createEntity(@RequestBody EntityDTO dto) {
    Entity entity = new Entity();
    entity.setName(dto.getName());
    entity.setEntityType(dto.getEntityType());
    entity.setSchemaOrgType(dto.getSchemaOrgType());
    entity.setSchemaOrgData(dto.getSchemaOrgData());

    return entityRepository.save(entity);
}
```

### Step 4: Query and Reason
```sql
-- Query entities by Schema.org type
SELECT name, schema_org_data
FROM entities
WHERE schema_org_type = 'GovernmentOrganization';

-- Query JSON-LD properties
SELECT name, schema_org_data->>'url' as url
FROM entities
WHERE schema_org_data->>'@type' = 'GovernmentOrganization';
```

---

## 6. Benefits Over V1

| Feature | V1 (Failed) | V2 (Success) |
|---------|-------------|--------------|
| Ontology Support | Late retrofit | Built-in from day 1 |
| Data Format | Rigid tables | Flexible JSON-LD |
| Schema.org | Not supported | Native format |
| OWL Reasoning | Impossible | Optional, via Python |
| LLM Integration | Difficult | Natural (JSON-LD) |
| Extensibility | Requires migrations | Just add JSON properties |

---

## 7. Code Examples to Implement

### 7.1 Java: Schema.org Mapper Service
```java
package org.newsanalyzer.service;

@Service
public class SchemaOrgMapper {

    public Map<String, Object> toJsonLd(Entity entity) {
        Map<String, Object> jsonLd = new HashMap<>();
        jsonLd.put("@context", "https://schema.org");
        jsonLd.put("@type", entity.getSchemaOrgType());
        jsonLd.put("@id", "https://newsanalyzer.org/entities/" + entity.getId());
        jsonLd.put("name", entity.getName());

        // Add type-specific properties
        if ("Person".equals(entity.getSchemaOrgType())) {
            addPersonProperties(jsonLd, entity);
        } else if ("GovernmentOrganization".equals(entity.getSchemaOrgType())) {
            addGovOrgProperties(jsonLd, entity);
        }

        return jsonLd;
    }
}
```

### 7.2 Python: Schema.org Entity Extractor
```python
# reasoning-service/app/services/schema_mapper.py

from typing import Dict, Any

ENTITY_TYPE_TO_SCHEMA_ORG = {
    "person": "Person",
    "government_org": "GovernmentOrganization",
    "organization": "Organization",
    "location": "Place",
    "event": "Event",
    "legislation": "Legislation",
}

def map_to_schema_org(entity_type: str, entity_data: Dict[str, Any]) -> Dict[str, Any]:
    """
    Convert internal entity representation to Schema.org JSON-LD
    """
    schema_type = ENTITY_TYPE_TO_SCHEMA_ORG.get(entity_type, "Thing")

    json_ld = {
        "@context": "https://schema.org",
        "@type": schema_type,
        "name": entity_data.get("name"),
    }

    # Add type-specific mappings
    if schema_type == "Person":
        json_ld["jobTitle"] = entity_data.get("job_title")
        json_ld["affiliation"] = entity_data.get("affiliation")

    elif schema_type == "GovernmentOrganization":
        json_ld["url"] = entity_data.get("url")
        json_ld["parentOrganization"] = entity_data.get("parent_org")

    return json_ld
```

---

## 8. Next Steps

1. **Implement Schema.org mapper in Python service** (`reasoning-service/app/services/schema_mapper.py`)
2. **Create Schema.org utility in Spring Boot** (`backend/src/main/java/org/newsanalyzer/util/SchemaOrgMapper.java`)
3. **Define NewsAnalyzer custom ontology** (`docs/ontology/newsanalyzer.ttl`)
4. **Add OWL reasoning endpoint** (Phase 3)

---

## References

- **Schema.org Documentation:** https://schema.org
- **JSON-LD Specification:** https://json-ld.org
- **OWL 2 Primer:** https://www.w3.org/TR/owl2-primer/
- **RDFLib Documentation:** https://rdflib.readthedocs.io
- **OWL-RL Reasoner:** https://owl-rl.readthedocs.io
