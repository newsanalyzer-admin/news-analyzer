# Government Organizations Database Migration Guide

**Component:** Database Schema Migration
**Version:** 1.0
**Date:** 2025-11-21
**Migrations:** V2.9, V3

---

## Overview

This guide provides instructions for deploying the government organizations database schema to support US Government Manual integration in NewsAnalyzer v2.

### Created Migrations

1. **V2.9__enable_pg_extensions.sql** - Enable PostgreSQL extensions
2. **V3__create_government_organizations.sql** - Create government org schema

---

## Prerequisites

### Software Requirements
- PostgreSQL 14+
- Flyway (included in Spring Boot)
- Database user with extension creation privileges

### Required PostgreSQL Extensions
- `pg_trgm` - Fuzzy text matching
- `uuid-ossp` - UUID generation
- `btree_gin` - Composite GIN indexes

---

## Migration Components

### V2.9: PostgreSQL Extensions

**File:** `V2.9__enable_pg_extensions.sql`

**What it does:**
- Enables `pg_trgm` for fuzzy organization name matching
- Enables `uuid-ossp` for UUID primary keys
- Enables `btree_gin` for JSONB indexing

**Size:** ~1 KB

### V3: Government Organizations Schema

**File:** `V3__create_government_organizations.sql`

**What it creates:**

#### Tables (6)
1. **government_organizations** - Main table for org data
   - 30+ columns including metadata, hierarchy, JSONB fields
   - 10 indexes (B-tree, GIN, trigram, full-text)
   - Unique constraints on name and acronym

2. **government_organization_aliases** - Alternate names
   - Tracks acronyms, former names, colloquial names
   - Temporal validity (valid_from, valid_to)

3. **government_organization_relationships** - Non-hierarchical relationships
   - Coordination, regulation, funding relationships
   - Bidirectional support

4. **government_organization_jurisdictions** - Areas of authority
   - Regulatory, programmatic, advisory jurisdictions
   - Geographic scope tracking

5. **government_organization_history** - Change tracking
   - Mergers, splits, reorganizations, dissolutions
   - Before/after state snapshots

6. **government_organization_sync_log** - Ingestion audit log
   - Tracks GovInfo API sync jobs
   - Performance metrics and error logging

#### Views (4)
1. **vw_active_government_organizations** - Currently active orgs only
2. **vw_cabinet_departments** - 15 Cabinet departments
3. **vw_independent_agencies** - Independent agencies (EPA, NASA, etc.)
4. **vw_organization_hierarchy** - Recursive hierarchy tree

#### Functions (3)
1. **get_child_organizations(UUID)** - Get all descendants
2. **get_organization_ancestry(UUID)** - Get all ancestors
3. **search_government_organizations(TEXT)** - Fuzzy search

#### Triggers (4)
- Auto-update `updated_at` timestamp on all main tables

#### Seed Data
- 15 Cabinet departments (State, Treasury, Defense, etc.)
- 5 major independent agencies (EPA, NASA, CIA, FBI, SSA)
- Executive Branch placeholder

**Size:** ~40 KB

---

## Deployment Instructions

### Local Development

#### Step 1: Ensure PostgreSQL is Running
```bash
# Check PostgreSQL status
docker ps | grep postgres
# OR
pg_isready -h localhost -p 5432
```

#### Step 2: Run Flyway Migration
```bash
cd backend

# Development profile
./mvnw flyway:migrate -Dspring.profiles.active=dev

# Expected output:
# Flyway: Migrating schema "public" to version "2.9 - enable pg extensions"
# Flyway: Migrating schema "public" to version "3 - create government organizations"
# Flyway: Successfully applied 2 migrations to schema "public"
```

#### Step 3: Verify Migration
```bash
# Connect to database
psql -U newsanalyzer -d newsanalyzer

# Check migrations
SELECT * FROM flyway_schema_history ORDER BY installed_rank DESC LIMIT 5;

# Verify tables exist
\dt government_*

# Check seed data
SELECT count(*) FROM government_organizations;
-- Expected: 20 (15 departments + 5 agencies)

# Test views
SELECT * FROM vw_cabinet_departments;

# Test functions
SELECT * FROM get_child_organizations(
    (SELECT id FROM government_organizations WHERE acronym = 'DOD')
);
```

#### Step 4: Run Spring Boot Application
```bash
./mvnw spring-boot:run -Dspring.profiles.active=dev

# Check logs for:
# "Flyway migration completed successfully"
# "Validated 20 government organizations"
```

### Production Deployment

#### Option 1: Flyway Maven Plugin

```bash
# Production database
./mvnw flyway:migrate -Dflyway.url=jdbc:postgresql://production-host:5432/newsanalyzer \
                       -Dflyway.user=newsanalyzer \
                       -Dflyway.password=${DB_PASSWORD}
```

#### Option 2: Spring Boot Startup

```yaml
# application-prod.yml
spring:
  flyway:
    enabled: true
    locations: classpath:db/migration
    baseline-on-migrate: true
    validate-on-migrate: true
```

```bash
# Start application (runs migrations automatically)
java -jar newsanalyzer-backend.jar --spring.profiles.active=prod
```

#### Option 3: Manual SQL Execution

```bash
# If Flyway is disabled, run manually
psql -U newsanalyzer -d newsanalyzer -f V2.9__enable_pg_extensions.sql
psql -U newsanalyzer -d newsanalyzer -f V3__create_government_organizations.sql
```

---

## Validation Checklist

### Post-Migration Validation

**1. Tables Created**
```sql
SELECT table_name
FROM information_schema.tables
WHERE table_schema = 'public'
  AND table_name LIKE 'government_%'
ORDER BY table_name;

-- Expected: 6 tables
-- government_organizations
-- government_organization_aliases
-- government_organization_history
-- government_organization_jurisdictions
-- government_organization_relationships
-- government_organization_sync_log
```

**2. Indexes Created**
```sql
SELECT tablename, indexname
FROM pg_indexes
WHERE schemaname = 'public'
  AND tablename LIKE 'government_%'
ORDER BY tablename, indexname;

-- Expected: 25+ indexes
```

**3. Views Created**
```sql
SELECT table_name
FROM information_schema.views
WHERE table_schema = 'public'
  AND table_name LIKE 'vw_%government%'
ORDER BY table_name;

-- Expected: 4 views
```

**4. Functions Created**
```sql
SELECT routine_name
FROM information_schema.routines
WHERE routine_schema = 'public'
  AND routine_name LIKE '%government%'
ORDER BY routine_name;

-- Expected: 3+ functions
```

**5. Seed Data Loaded**
```sql
-- Check Cabinet departments
SELECT count(*) FROM government_organizations WHERE org_type = 'department';
-- Expected: 15

-- Check independent agencies
SELECT count(*) FROM government_organizations WHERE org_type = 'independent_agency';
-- Expected: 5

-- Check all active organizations
SELECT count(*) FROM vw_active_government_organizations;
-- Expected: 20
```

**6. Extensions Enabled**
```sql
SELECT extname, extversion
FROM pg_extension
WHERE extname IN ('pg_trgm', 'uuid-ossp', 'btree_gin');

-- Expected: All 3 extensions present
```

**7. Test Fuzzy Search**
```sql
SELECT * FROM search_government_organizations('Environmental');
-- Expected: EPA (Environmental Protection Agency) at top

SELECT * FROM search_government_organizations('NASA');
-- Expected: Exact match on acronym
```

**8. Test Hierarchy Functions**
```sql
-- Test ancestry (should return Executive Branch)
SELECT * FROM get_organization_ancestry(
    (SELECT id FROM government_organizations WHERE acronym = 'DOD')
);

-- Test children (if DOD had sub-orgs)
SELECT * FROM get_child_organizations(
    (SELECT id FROM government_organizations WHERE acronym = 'DOD')
);
```

---

## Rollback Procedures

### Rollback V3 (Government Organizations)

```sql
-- Drop in reverse order (dependencies first)
DROP TRIGGER IF EXISTS update_government_organization_jurisdictions_updated_at ON government_organization_jurisdictions;
DROP TRIGGER IF EXISTS update_government_organization_relationships_updated_at ON government_organization_relationships;
DROP TRIGGER IF EXISTS update_government_organization_aliases_updated_at ON government_organization_aliases;
DROP TRIGGER IF EXISTS update_government_organizations_updated_at ON government_organizations;

DROP FUNCTION IF EXISTS update_updated_at_column();
DROP FUNCTION IF EXISTS search_government_organizations(TEXT);
DROP FUNCTION IF EXISTS get_organization_ancestry(UUID);
DROP FUNCTION IF EXISTS get_child_organizations(UUID);

DROP VIEW IF EXISTS vw_organization_hierarchy;
DROP VIEW IF EXISTS vw_independent_agencies;
DROP VIEW IF EXISTS vw_cabinet_departments;
DROP VIEW IF EXISTS vw_active_government_organizations;

DROP TABLE IF EXISTS government_organization_sync_log;
DROP TABLE IF EXISTS government_organization_history;
DROP TABLE IF EXISTS government_organization_jurisdictions;
DROP TABLE IF EXISTS government_organization_relationships;
DROP TABLE IF EXISTS government_organization_aliases;
DROP TABLE IF EXISTS government_organizations CASCADE;

-- Remove Flyway entry
DELETE FROM flyway_schema_history WHERE version = '3';
```

### Rollback V2.9 (Extensions)

```sql
-- WARNING: Only if no other features depend on these extensions
DROP EXTENSION IF EXISTS btree_gin;
DROP EXTENSION IF EXISTS pg_trgm;
-- DO NOT drop uuid-ossp if other tables use it

-- Remove Flyway entry
DELETE FROM flyway_schema_history WHERE version = '2.9';
```

---

## Performance Considerations

### Index Usage

The migration creates multiple index types:

1. **B-tree indexes** (default)
   - Fast equality and range queries
   - Used for: parent_id, org_type, branch, acronym

2. **GIN indexes** (Generalized Inverted Index)
   - Full-text search and array operations
   - Used for: JSONB fields, text arrays, trigram matching

3. **Trigram indexes** (pg_trgm)
   - Fuzzy text matching with similarity()
   - Used for: Organization name search

### Query Performance Estimates

| Operation | Expected Performance |
|-----------|---------------------|
| Get org by ID | < 1ms (B-tree index) |
| Search by acronym | < 5ms (B-tree index) |
| Fuzzy name search | < 50ms (trigram index) |
| Get hierarchy | < 20ms (recursive CTE) |
| Full-text search | < 100ms (GIN index) |

### Optimization Tips

**For large datasets (>10,000 organizations):**
```sql
-- Update statistics for better query planning
ANALYZE government_organizations;
ANALYZE government_organization_aliases;

-- Reindex if needed
REINDEX TABLE government_organizations;
```

**Monitor slow queries:**
```sql
-- Enable slow query logging
ALTER DATABASE newsanalyzer SET log_min_duration_statement = 1000; -- Log queries >1s

-- Check index usage
SELECT
    schemaname,
    tablename,
    indexname,
    idx_scan,
    idx_tup_read,
    idx_tup_fetch
FROM pg_stat_user_indexes
WHERE schemaname = 'public'
  AND tablename LIKE 'government_%'
ORDER BY idx_scan;
```

---

## Data Maintenance

### Regular Maintenance Tasks

**Weekly:**
```sql
-- Vacuum and analyze (reclaim space, update statistics)
VACUUM ANALYZE government_organizations;
VACUUM ANALYZE government_organization_aliases;
```

**Monthly:**
```sql
-- Check for orphaned records
SELECT count(*)
FROM government_organization_aliases a
LEFT JOIN government_organizations o ON a.organization_id = o.id
WHERE o.id IS NULL;
-- Expected: 0

-- Check for circular hierarchies (should not exist)
WITH RECURSIVE hierarchy AS (
    SELECT id, parent_id, ARRAY[id] AS path
    FROM government_organizations
    WHERE parent_id IS NOT NULL
    UNION ALL
    SELECT o.id, o.parent_id, h.path || o.id
    FROM government_organizations o
    JOIN hierarchy h ON o.parent_id = h.id
    WHERE o.id = ANY(h.path) -- Circular reference
)
SELECT * FROM hierarchy WHERE id = ANY(path[2:]);
-- Expected: Empty (no circular references)
```

**Quarterly:**
```sql
-- Archive old sync logs (keep 1 year)
DELETE FROM government_organization_sync_log
WHERE sync_start_time < NOW() - INTERVAL '1 year';

-- Archive dissolved organizations (keep 10 years)
-- (Move to archive table if needed)
```

---

## Troubleshooting

### Common Issues

**Issue 1: Extension Creation Failed**
```
ERROR: permission denied to create extension "pg_trgm"
```

**Solution:**
```sql
-- Grant extension creation to user
ALTER USER newsanalyzer CREATEEXTROLE;
-- OR run as superuser
sudo -u postgres psql -d newsanalyzer -c "CREATE EXTENSION pg_trgm;"
```

**Issue 2: Migration Checksum Mismatch**
```
ERROR: Migration checksum mismatch for migration version 3
```

**Solution:**
```sql
-- Repair Flyway (if file was modified after deployment)
UPDATE flyway_schema_history
SET checksum = <new_checksum>
WHERE version = '3';
-- OR re-run migration with: ./mvnw flyway:repair
```

**Issue 3: Unique Constraint Violation**
```
ERROR: duplicate key value violates unique constraint "unique_official_name"
```

**Solution:**
```sql
-- Find duplicates
SELECT official_name, count(*)
FROM government_organizations
GROUP BY official_name
HAVING count(*) > 1;

-- Remove duplicates (keep most recent)
DELETE FROM government_organizations a
USING government_organizations b
WHERE a.id < b.id
  AND a.official_name = b.official_name;
```

**Issue 4: Slow Fuzzy Search**
```
Query takes >5 seconds on search_government_organizations()
```

**Solution:**
```sql
-- Rebuild trigram index
REINDEX INDEX idx_gov_org_name_trgm;

-- Increase shared_buffers in postgresql.conf (if needed)
-- shared_buffers = 256MB (or 25% of RAM)
```

---

## Testing

### Unit Tests (SQL)

```sql
-- Test 1: Hierarchy integrity
SELECT count(*)
FROM government_organizations o
LEFT JOIN government_organizations p ON o.parent_id = p.id
WHERE o.parent_id IS NOT NULL AND p.id IS NULL;
-- Expected: 0 (no orphaned parents)

-- Test 2: Acronym uniqueness per branch
SELECT branch, acronym, count(*)
FROM government_organizations
WHERE acronym IS NOT NULL
GROUP BY branch, acronym
HAVING count(*) > 1;
-- Expected: Empty (no duplicate acronyms per branch)

-- Test 3: Date validity
SELECT count(*)
FROM government_organizations
WHERE dissolved_date IS NOT NULL
  AND dissolved_date < established_date;
-- Expected: 0 (dissolved date after established date)

-- Test 4: View consistency
SELECT
    (SELECT count(*) FROM government_organizations WHERE dissolved_date IS NULL) AS active_direct,
    (SELECT count(*) FROM vw_active_government_organizations) AS active_view;
-- Expected: Both counts equal
```

### Integration Tests (Java)

```java
@Test
public void testMigrationCompleted() {
    long orgCount = governmentOrgRepository.count();
    assertThat(orgCount).isGreaterThanOrEqualTo(20); // 15 departments + 5 agencies
}

@Test
public void testCabinetDepartments() {
    List<GovernmentOrganization> departments = governmentOrgRepository
        .findByOrgTypeAndBranchAndOrgLevel("department", "executive", 1);
    assertThat(departments).hasSize(15);
}

@Test
public void testFuzzySearch() {
    List<GovernmentOrganization> results = governmentOrgRepository
        .searchByName("Environmental");
    assertThat(results).isNotEmpty();
    assertThat(results.get(0).getAcronym()).isEqualTo("EPA");
}
```

---

## Monitoring

### Metrics to Track

**Database Size:**
```sql
SELECT
    pg_size_pretty(pg_total_relation_size('government_organizations')) AS total_size,
    pg_size_pretty(pg_relation_size('government_organizations')) AS table_size,
    pg_size_pretty(pg_total_relation_size('government_organizations') - pg_relation_size('government_organizations')) AS index_size;
```

**Row Counts:**
```sql
SELECT
    'organizations' AS table_name,
    count(*) AS row_count
FROM government_organizations
UNION ALL
SELECT 'aliases', count(*) FROM government_organization_aliases
UNION ALL
SELECT 'relationships', count(*) FROM government_organization_relationships
UNION ALL
SELECT 'jurisdictions', count(*) FROM government_organization_jurisdictions
UNION ALL
SELECT 'history', count(*) FROM government_organization_history
UNION ALL
SELECT 'sync_log', count(*) FROM government_organization_sync_log;
```

**Index Health:**
```sql
SELECT
    indexrelname AS index_name,
    idx_scan AS scans,
    pg_size_pretty(pg_relation_size(indexrelid)) AS size
FROM pg_stat_user_indexes
WHERE schemaname = 'public'
  AND tablename LIKE 'government_%'
ORDER BY idx_scan DESC;
```

---

## Next Steps

After successful migration:

1. **Verify** all tables, views, functions created
2. **Test** fuzzy search and hierarchy queries
3. **Implement** Java entities and repositories
4. **Build** REST API endpoints
5. **Create** Python ingestion service
6. **Schedule** GovInfo sync job

See: `docs/architecture/government-org-chart-integration.md` for full implementation plan.

---

*Migration Guide prepared by Winston, Architect Agent*
*Date: 2025-11-21*
*Status: Ready for Deployment*
