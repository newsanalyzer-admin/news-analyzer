# Government Organizations Schema - Quick Reference

**Version:** 1.0
**Date:** 2025-11-21

---

## Table Structure Summary

### Main Tables

| Table | Primary Purpose | Key Columns | Row Estimate |
|-------|----------------|-------------|--------------|
| `government_organizations` | Official org data | official_name, acronym, org_type, branch, parent_id | ~500-1000 |
| `government_organization_aliases` | Alternate names | alias_name, alias_type, valid_from, valid_to | ~2000 |
| `government_organization_relationships` | Non-hierarchical links | relationship_type, source_org_id, target_org_id | ~1000 |
| `government_organization_jurisdictions` | Authority areas | jurisdiction_area, authority_level | ~3000 |
| `government_organization_history` | Change tracking | change_type, change_date, state_before, state_after | ~500 |
| `government_organization_sync_log` | API sync audit | sync_status, organizations_added/updated/removed | ~100/year |

---

## Key Queries

### Find Organization by Name or Acronym
```sql
-- Exact match
SELECT * FROM government_organizations
WHERE official_name = 'Environmental Protection Agency'
   OR acronym = 'EPA';

-- Fuzzy search (uses trigram similarity)
SELECT * FROM search_government_organizations('Environmental');
```

### Get Organizational Hierarchy
```sql
-- Get all children of an organization
SELECT * FROM get_child_organizations(
    (SELECT id FROM government_organizations WHERE acronym = 'DOD')
);

-- Get all parents/ancestors
SELECT * FROM get_organization_ancestry(
    (SELECT id FROM government_organizations WHERE acronym = 'EPA')
);

-- Full hierarchy tree (recursive view)
SELECT * FROM vw_organization_hierarchy
WHERE official_name LIKE '%Defense%';
```

### Get Active Organizations by Type
```sql
-- Cabinet departments only
SELECT * FROM vw_cabinet_departments;

-- Independent agencies
SELECT * FROM vw_independent_agencies;

-- All active organizations
SELECT * FROM vw_active_government_organizations
WHERE branch = 'executive'
ORDER BY org_level, official_name;
```

### Search by Jurisdiction
```sql
-- Organizations with environmental jurisdiction
SELECT DISTINCT o.*
FROM government_organizations o
JOIN government_organization_jurisdictions j ON o.id = j.organization_id
WHERE 'environmental_protection' = ANY(o.jurisdiction_areas)
   OR j.jurisdiction_area LIKE '%environment%';
```

### Get Relationships
```sql
-- Organizations that report to DOD
SELECT
    o.official_name,
    o.acronym,
    r.relationship_type,
    r.description
FROM government_organization_relationships r
JOIN government_organizations o ON r.source_org_id = o.id
JOIN government_organizations parent ON r.target_org_id = parent.id
WHERE parent.acronym = 'DOD'
  AND r.relationship_type = 'reports_to';
```

### Historical Changes
```sql
-- Organizations created in last year
SELECT
    official_name,
    acronym,
    established_date
FROM government_organizations
WHERE established_date >= NOW() - INTERVAL '1 year'
ORDER BY established_date DESC;

-- Recent reorganizations
SELECT
    o.official_name,
    h.change_type,
    h.change_date,
    h.change_description
FROM government_organization_history h
JOIN government_organizations o ON h.organization_id = o.id
WHERE h.change_date >= NOW() - INTERVAL '1 year'
ORDER BY h.change_date DESC;
```

---

## Common Patterns

### Insert New Organization
```sql
INSERT INTO government_organizations (
    official_name,
    acronym,
    org_type,
    branch,
    parent_id,
    org_level,
    established_date,
    website_url,
    jurisdiction_areas,
    data_quality_score
) VALUES (
    'Office of the National Cyber Director',
    'ONCD',
    'office',
    'executive',
    (SELECT id FROM government_organizations WHERE official_name = 'Executive Branch'),
    2,
    '2021-01-01',
    'https://www.whitehouse.gov/oncd/',
    ARRAY['cybersecurity', 'national_security'],
    1.0
) RETURNING id;
```

### Add Alias/Alternate Name
```sql
INSERT INTO government_organization_aliases (
    organization_id,
    alias_name,
    alias_type,
    valid_from,
    is_official
) VALUES (
    (SELECT id FROM government_organizations WHERE acronym = 'EPA'),
    'Environmental Protection Agency',
    'full_name',
    '1970-12-02',
    true
);
```

### Record Organizational Change
```sql
INSERT INTO government_organization_history (
    organization_id,
    change_type,
    change_date,
    change_description,
    authorizing_document,
    impact_level
) VALUES (
    (SELECT id FROM government_organizations WHERE acronym = 'DHS'),
    'reorganized',
    '2023-01-15',
    'Created Cybersecurity and Infrastructure Security Agency (CISA)',
    'Public Law 115-278',
    'major'
);
```

### Update Jurisdiction
```sql
INSERT INTO government_organization_jurisdictions (
    organization_id,
    jurisdiction_area,
    jurisdiction_category,
    description,
    authority_level,
    priority
) VALUES (
    (SELECT id FROM government_organizations WHERE acronym = 'EPA'),
    'climate_policy',
    'regulatory',
    'Regulation of greenhouse gas emissions under Clean Air Act',
    'primary',
    1
);
```

---

## Index Usage Guide

### When to Use Which Index

| Query Type | Index Used | Performance |
|------------|-----------|-------------|
| `WHERE id = ?` | B-tree PK | < 1ms |
| `WHERE acronym = ?` | B-tree idx_gov_org_acronym | < 5ms |
| `WHERE parent_id = ?` | B-tree idx_gov_org_parent | < 10ms |
| `WHERE official_name LIKE '%text%'` | Trigram idx_gov_org_name_trgm | < 50ms |
| Fuzzy search `similarity()` | Trigram idx_gov_org_name_trgm | < 100ms |
| Full-text search | GIN idx_gov_org_fulltext | < 100ms |
| `WHERE jurisdiction_areas @> ARRAY[...]` | GIN idx_gov_org_jurisdiction | < 50ms |
| `schema_org_data @> '{...}'::jsonb` | GIN idx_gov_org_schema_org | < 100ms |

---

## Function Reference

### get_child_organizations(parent_org_id UUID)
Returns all descendant organizations recursively.

**Example:**
```sql
SELECT * FROM get_child_organizations(
    (SELECT id FROM government_organizations WHERE acronym = 'DOD')
);
```

**Returns:** id, official_name, org_level, depth

### get_organization_ancestry(org_id UUID)
Returns all ancestor organizations (parents) up to top level.

**Example:**
```sql
SELECT * FROM get_organization_ancestry(
    (SELECT id FROM government_organizations WHERE acronym = 'FBI')
);
```

**Returns:** id, official_name, org_level, depth

### search_government_organizations(search_text TEXT)
Fuzzy search using trigram similarity.

**Example:**
```sql
SELECT * FROM search_government_organizations('Homeland');
```

**Returns:** id, official_name, acronym, similarity_score (max 20 results)

---

## View Reference

### vw_active_government_organizations
All currently active organizations (dissolved_date IS NULL).

```sql
SELECT * FROM vw_active_government_organizations
WHERE branch = 'executive';
```

### vw_cabinet_departments
The 15 Cabinet-level executive departments.

```sql
SELECT * FROM vw_cabinet_departments
ORDER BY established_date;
```

### vw_independent_agencies
Independent executive agencies (EPA, NASA, etc.).

```sql
SELECT * FROM vw_independent_agencies
WHERE 'environmental_protection' = ANY(jurisdiction_areas);
```

### vw_organization_hierarchy
Recursive hierarchy showing full path from top level.

```sql
SELECT
    official_name,
    hierarchy_string,
    depth
FROM vw_organization_hierarchy
WHERE depth <= 3
ORDER BY hierarchy_string;
```

---

## Entity Type Reference

### Organization Types (org_type)
- `branch` - Executive/Legislative/Judicial Branch
- `department` - Cabinet-level department
- `independent_agency` - Independent agency (EPA, NASA)
- `bureau` - Bureau within department
- `office` - Office or administration
- `commission` - Regulatory commission
- `board` - Governing board

### Government Branches (branch)
- `executive` - Executive Branch
- `legislative` - Legislative Branch (Congress)
- `judicial` - Judicial Branch (Courts)

### Relationship Types
- `reports_to` - Hierarchical reporting
- `coordinates_with` - Inter-agency coordination
- `regulates` - Regulatory authority over
- `funds` - Provides funding to
- `advises` - Advisory role
- `oversees` - Oversight authority
- `collaborates_with` - Partnership

### Jurisdiction Categories
- `regulatory` - Rule-making authority
- `programmatic` - Program administration
- `advisory` - Advisory role
- `oversight` - Oversight function

### Change Types (history)
- `created` - Organization established
- `renamed` - Name change
- `merged` - Merged with other org(s)
- `split` - Split into multiple orgs
- `dissolved` - Organization dissolved
- `reorganized` - Internal reorganization
- `transferred` - Moved to different parent
- `mission_changed` - Mission/jurisdiction change

---

## Maintenance Commands

### Vacuum and Analyze
```sql
-- Weekly maintenance
VACUUM ANALYZE government_organizations;
VACUUM ANALYZE government_organization_aliases;
VACUUM ANALYZE government_organization_relationships;
```

### Reindex (if needed)
```sql
-- Rebuild all indexes
REINDEX TABLE government_organizations;

-- Rebuild specific index
REINDEX INDEX idx_gov_org_name_trgm;
```

### Check Table Sizes
```sql
SELECT
    tablename,
    pg_size_pretty(pg_total_relation_size(schemaname||'.'||tablename)) AS size
FROM pg_tables
WHERE schemaname = 'public'
  AND tablename LIKE 'government_%'
ORDER BY pg_total_relation_size(schemaname||'.'||tablename) DESC;
```

### Statistics
```sql
-- Row counts
SELECT
    'government_organizations' AS table_name,
    count(*) AS rows,
    count(*) FILTER (WHERE dissolved_date IS NULL) AS active
FROM government_organizations
UNION ALL
SELECT 'aliases', count(*), count(*) FILTER (WHERE valid_to IS NULL)
FROM government_organization_aliases;
```

---

## API Integration Points

### Java Entity
```java
@Entity
@Table(name = "government_organizations")
public class GovernmentOrganization {
    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "official_name")
    private String officialName;

    @Column(name = "acronym")
    private String acronym;

    @Column(name = "org_type")
    @Enumerated(EnumType.STRING)
    private OrgType orgType;

    // ... more fields
}
```

### Repository Methods
```java
public interface GovernmentOrgRepository extends JpaRepository<GovernmentOrganization, UUID> {

    Optional<GovernmentOrganization> findByAcronym(String acronym);

    List<GovernmentOrganization> findByOrgTypeAndBranch(String orgType, String branch);

    @Query("SELECT o FROM GovernmentOrganization o WHERE o.dissolvedDate IS NULL")
    List<GovernmentOrganization> findActive();

    @Query(value = "SELECT * FROM search_government_organizations(?1)", nativeQuery = true)
    List<GovernmentOrganization> fuzzySearch(String searchText);
}
```

---

## Troubleshooting

### Slow Queries
```sql
-- Enable query logging
SET log_min_duration_statement = 1000; -- Log queries > 1s

-- Check slow queries
SELECT query, mean_exec_time, calls
FROM pg_stat_statements
WHERE query LIKE '%government_%'
ORDER BY mean_exec_time DESC
LIMIT 10;
```

### Index Not Used
```sql
-- Explain query plan
EXPLAIN ANALYZE
SELECT * FROM government_organizations
WHERE acronym = 'EPA';

-- Look for "Index Scan" vs "Seq Scan"
```

### Duplicate Data
```sql
-- Find duplicate names
SELECT official_name, count(*)
FROM government_organizations
GROUP BY official_name
HAVING count(*) > 1;

-- Find duplicate acronyms per branch
SELECT branch, acronym, count(*)
FROM government_organizations
WHERE acronym IS NOT NULL
GROUP BY branch, acronym
HAVING count(*) > 1;
```

---

*Quick Reference Guide prepared by Winston, Architect Agent*
*Last Updated: 2025-11-21*
