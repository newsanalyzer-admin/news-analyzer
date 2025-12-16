# Coding Standards

**Document Version:** 1.0
**Last Updated:** 2025-11-27
**Applies To:** NewsAnalyzer v2

---

## Overview

This document defines the coding standards for all NewsAnalyzer services. Adherence ensures consistency, maintainability, and quality across the codebase.

---

## General Principles

1. **Readability over cleverness** - Write code that's easy to understand
2. **Consistency** - Follow established patterns within each service
3. **Self-documenting code** - Use meaningful names; comments explain "why", not "what"
4. **Single Responsibility** - Each function/class does one thing well
5. **DRY (Don't Repeat Yourself)** - Extract common logic into reusable components
6. **YAGNI (You Aren't Gonna Need It)** - Don't add features until needed

---

## Java Backend Standards

### Code Style

- **Formatter:** Use IDE default (IntelliJ/Eclipse) or Checkstyle
- **Indentation:** 4 spaces (no tabs)
- **Line length:** 120 characters max
- **Braces:** K&R style (opening brace on same line)

```java
// Good
public void processEntity(Entity entity) {
    if (entity != null) {
        // ...
    }
}

// Bad
public void processEntity(Entity entity)
{
    if (entity != null)
    {
        // ...
    }
}
```

### Naming Conventions

| Element | Convention | Example |
|---------|------------|---------|
| Classes | PascalCase | `EntityService` |
| Interfaces | PascalCase | `EntityRepository` |
| Methods | camelCase | `findByName()` |
| Variables | camelCase | `entityType` |
| Constants | UPPER_SNAKE_CASE | `MAX_RETRY_COUNT` |
| Packages | lowercase | `org.newsanalyzer.service` |
| Database columns | snake_case | `entity_type` |

### Class Organization

```java
public class EntityService {
    // 1. Static fields
    private static final Logger log = LoggerFactory.getLogger(EntityService.class);

    // 2. Instance fields (private final first)
    private final EntityRepository entityRepository;
    private final SchemaOrgMapper schemaOrgMapper;

    // 3. Constructor(s)
    public EntityService(EntityRepository entityRepository, SchemaOrgMapper mapper) {
        this.entityRepository = entityRepository;
        this.schemaOrgMapper = mapper;
    }

    // 4. Public methods
    public Entity createEntity(CreateEntityRequest request) { }

    // 5. Protected methods

    // 6. Private methods
    private void validateRequest(CreateEntityRequest request) { }
}
```

### Spring Boot Patterns

**Controllers:**
```java
@RestController
@RequestMapping("/api/entities")
@Tag(name = "Entities", description = "Entity management")
public class EntityController {

    private final EntityService entityService;

    // Constructor injection (not field injection)
    public EntityController(EntityService entityService) {
        this.entityService = entityService;
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get entity by ID")
    public ResponseEntity<EntityDTO> getEntity(@PathVariable UUID id) {
        return ResponseEntity.ok(entityService.findById(id));
    }
}
```

**Services:**
```java
@Service
@Transactional(readOnly = true)  // Default read-only
public class EntityService {

    @Transactional  // Override for write operations
    public Entity createEntity(CreateEntityRequest request) {
        // ...
    }
}
```

**Repositories:**
```java
public interface EntityRepository extends JpaRepository<Entity, UUID> {

    // Use @Query for complex queries
    @Query("SELECT e FROM Entity e WHERE e.entityType = :type AND e.verified = true")
    List<Entity> findVerifiedByType(@Param("type") EntityType type);

    // Spring Data query methods for simple cases
    List<Entity> findByNameContainingIgnoreCase(String name);
}
```

### Hibernate Lazy Loading & JSON Serialization

When entities have lazy-loaded relationships, use `@JsonIgnore` to prevent serialization issues:

```java
@Entity
public class GovernmentOrganization {

    @Column(name = "parent_id")
    private UUID parentId;  // Use ID field for API responses

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id", insertable = false, updatable = false)
    @JsonIgnore  // Prevents Hibernate proxy serialization errors
    private GovernmentOrganization parent;  // Use only for JPA navigation
}
```

**Key Points:**
- Use `@JsonIgnore` on lazy-loaded entity relationships
- Expose the foreign key ID field (e.g., `parentId`) for API consumers
- The `JacksonConfig` registers `Hibernate6Module` for additional proxy handling
- For null-safe comparisons with wrapper types: `Integer.valueOf(1).equals(orgLevel)` not `orgLevel == 1`

### Exception Handling

```java
// Define custom exceptions
public class EntityNotFoundException extends RuntimeException {
    public EntityNotFoundException(UUID id) {
        super("Entity not found: " + id);
    }
}

// Use @ControllerAdvice for global handling
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(EntityNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(new ErrorResponse(ex.getMessage()));
    }
}
```

### Testing Standards

```java
@SpringBootTest
class EntityServiceTest {

    @Mock
    private EntityRepository entityRepository;

    @InjectMocks
    private EntityService entityService;

    @Test
    void createEntity_validRequest_returnsEntity() {
        // Given (Arrange)
        CreateEntityRequest request = new CreateEntityRequest();
        request.setName("EPA");
        when(entityRepository.save(any())).thenReturn(new Entity());

        // When (Act)
        Entity result = entityService.createEntity(request);

        // Then (Assert)
        assertThat(result).isNotNull();
        verify(entityRepository).save(any());
    }
}
```

---

## TypeScript/React Standards (Frontend)

### Code Style

- **Formatter:** Prettier (via ESLint)
- **Indentation:** 2 spaces
- **Line length:** 100 characters max
- **Semicolons:** Required
- **Quotes:** Single quotes for strings

### Naming Conventions

| Element | Convention | Example |
|---------|------------|---------|
| Components | PascalCase | `EntityCard` |
| Functions | camelCase | `fetchEntities` |
| Variables | camelCase | `entityList` |
| Constants | UPPER_SNAKE_CASE | `API_BASE_URL` |
| Types/Interfaces | PascalCase | `EntityDTO` |
| Files (components) | PascalCase | `EntityCard.tsx` |
| Files (utilities) | camelCase | `apiClient.ts` |
| CSS classes | kebab-case | `entity-card` |

### Component Structure

```tsx
// 1. Imports (external first, then internal)
import { useState, useEffect } from 'react';
import { Entity } from '@/types/entity';
import { fetchEntity } from '@/lib/api/entities';

// 2. Types/Interfaces
interface EntityCardProps {
  entityId: string;
  onSelect?: (entity: Entity) => void;
}

// 3. Component
export function EntityCard({ entityId, onSelect }: EntityCardProps) {
  // State
  const [entity, setEntity] = useState<Entity | null>(null);
  const [loading, setLoading] = useState(true);

  // Effects
  useEffect(() => {
    fetchEntity(entityId).then(setEntity).finally(() => setLoading(false));
  }, [entityId]);

  // Handlers
  const handleClick = () => {
    if (entity && onSelect) {
      onSelect(entity);
    }
  };

  // Early returns
  if (loading) return <div>Loading...</div>;
  if (!entity) return <div>Not found</div>;

  // Render
  return (
    <div className="entity-card" onClick={handleClick}>
      <h3>{entity.name}</h3>
      <span className="badge">{entity.schemaOrgType}</span>
    </div>
  );
}
```

### Type Definitions

```typescript
// types/entity.ts

// Use interfaces for object shapes
export interface Entity {
  id: string;
  name: string;
  entityType: EntityType;
  schemaOrgType: string;
  schemaOrgData: Record<string, unknown>;
  properties: Record<string, unknown>;
  verified: boolean;
  confidenceScore: number;
  createdAt: string;
  updatedAt: string;
}

// Use enums sparingly; prefer union types
export type EntityType =
  | 'PERSON'
  | 'GOVERNMENT_ORG'
  | 'ORGANIZATION'
  | 'LOCATION'
  | 'EVENT'
  | 'CONCEPT';

// Use type for unions and computed types
export type EntityCreateRequest = Pick<Entity, 'name' | 'entityType'> & {
  source?: string;
};
```

### API Client Pattern

```typescript
// lib/api/entities.ts

import axios from 'axios';
import { Entity, EntityCreateRequest } from '@/types/entity';

const api = axios.create({
  baseURL: process.env.NEXT_PUBLIC_API_URL,
  timeout: 10000,
});

export async function getEntity(id: string): Promise<Entity> {
  const { data } = await api.get<Entity>(`/api/entities/${id}`);
  return data;
}

export async function createEntity(request: EntityCreateRequest): Promise<Entity> {
  const { data } = await api.post<Entity>('/api/entities', request);
  return data;
}
```

### Tailwind CSS Guidelines

```tsx
// Use Tailwind utility classes
<div className="flex items-center gap-4 p-4 bg-white rounded-lg shadow">

// Use clsx for conditional classes
import { clsx } from 'clsx';

<div className={clsx(
  'p-4 rounded-lg',
  isActive && 'bg-blue-500 text-white',
  isDisabled && 'opacity-50 cursor-not-allowed'
)}>

// Extract repeated patterns to components, not @apply
// Good: Create <Badge> component
// Bad: @apply bg-blue-500 text-white px-2 py-1 rounded
```

---

## Python Standards (Reasoning Service)

### Code Style

- **Formatter:** Black (line length 88)
- **Linter:** Ruff
- **Type Checker:** mypy (strict mode)
- **Indentation:** 4 spaces
- **Quotes:** Double quotes for strings

### Naming Conventions

| Element | Convention | Example |
|---------|------------|---------|
| Classes | PascalCase | `EntityExtractor` |
| Functions | snake_case | `extract_entities` |
| Variables | snake_case | `entity_type` |
| Constants | UPPER_SNAKE_CASE | `MAX_ENTITIES` |
| Modules | snake_case | `entity_extractor.py` |
| Private | _prefix | `_internal_method` |

### Module Structure

```python
"""
Entity Extractor Service

Extracts named entities from text using spaCy NLP.
"""

# 1. Standard library imports
from typing import List, Dict, Any, Optional
from dataclasses import dataclass
from enum import Enum

# 2. Third-party imports
import spacy
from pydantic import BaseModel

# 3. Local imports
from app.services.schema_mapper import map_to_schema_org

# 4. Constants
DEFAULT_MODEL = "en_core_web_sm"
CONFIDENCE_THRESHOLD = 0.7

# 5. Type definitions
class EntityType(str, Enum):
    PERSON = "person"
    ORGANIZATION = "organization"
    LOCATION = "location"

# 6. Data classes / Pydantic models
@dataclass
class ExtractedEntity:
    text: str
    entity_type: EntityType
    confidence: float
    start: int
    end: int

# 7. Main class / functions
class EntityExtractor:
    """Extracts entities from text using spaCy."""

    def __init__(self, model: str = DEFAULT_MODEL):
        self.nlp = spacy.load(model)

    def extract(self, text: str) -> List[ExtractedEntity]:
        """Extract entities from text."""
        doc = self.nlp(text)
        return [self._to_entity(ent) for ent in doc.ents]

    def _to_entity(self, ent) -> ExtractedEntity:
        """Convert spaCy entity to ExtractedEntity."""
        return ExtractedEntity(
            text=ent.text,
            entity_type=self._map_type(ent.label_),
            confidence=0.9,
            start=ent.start_char,
            end=ent.end_char,
        )

# 8. Module-level singleton (if needed)
_extractor_instance: Optional[EntityExtractor] = None

def get_entity_extractor() -> EntityExtractor:
    """Get or create singleton extractor instance."""
    global _extractor_instance
    if _extractor_instance is None:
        _extractor_instance = EntityExtractor()
    return _extractor_instance
```

### FastAPI Patterns

```python
# api/entities.py

from fastapi import APIRouter, HTTPException, Depends
from pydantic import BaseModel, Field
from typing import List

router = APIRouter(prefix="/entities", tags=["Entities"])

# Request/Response models
class EntityExtractionRequest(BaseModel):
    text: str = Field(..., min_length=1, description="Text to analyze")
    confidence_threshold: float = Field(0.7, ge=0.0, le=1.0)

class EntityResponse(BaseModel):
    text: str
    entity_type: str
    confidence: float

class ExtractionResponse(BaseModel):
    entities: List[EntityResponse]
    total_count: int

# Endpoints
@router.post("/extract", response_model=ExtractionResponse)
async def extract_entities(request: EntityExtractionRequest):
    """Extract entities from text."""
    extractor = get_entity_extractor()
    entities = extractor.extract(request.text)

    filtered = [e for e in entities if e.confidence >= request.confidence_threshold]

    return ExtractionResponse(
        entities=[EntityResponse(**e.__dict__) for e in filtered],
        total_count=len(filtered),
    )
```

### Testing Standards

```python
# tests/test_entity_extractor.py

import pytest
from app.services.entity_extractor import EntityExtractor, ExtractedEntity

class TestEntityExtractor:
    """Tests for EntityExtractor service."""

    @pytest.fixture
    def extractor(self):
        """Create extractor instance for tests."""
        return EntityExtractor()

    def test_extract_person(self, extractor):
        """Test extraction of person entity."""
        # Arrange
        text = "Elizabeth Warren is a senator."

        # Act
        entities = extractor.extract(text)

        # Assert
        assert len(entities) >= 1
        assert any(e.text == "Elizabeth Warren" for e in entities)

    def test_extract_empty_text(self, extractor):
        """Test extraction from empty text returns empty list."""
        entities = extractor.extract("")
        assert entities == []

@pytest.mark.asyncio
async def test_extract_endpoint():
    """Integration test for extraction endpoint."""
    from fastapi.testclient import TestClient
    from app.main import app

    client = TestClient(app)
    response = client.post(
        "/entities/extract",
        json={"text": "The EPA announced new regulations."}
    )

    assert response.status_code == 200
    assert response.json()["total_count"] >= 1
```

---

## Database Standards

### Naming Conventions

| Element | Convention | Example |
|---------|------------|---------|
| Tables | snake_case, plural | `entities`, `government_organizations` |
| Columns | snake_case | `entity_type`, `schema_org_data` |
| Primary keys | `id` | `id UUID` |
| Foreign keys | `{table}_id` | `government_org_id` |
| Indexes | `idx_{table}_{columns}` | `idx_entities_type` |
| Constraints | `{type}_{table}_{columns}` | `fk_entities_government_org` |

### Migration Standards

```sql
-- V{version}__{description}.sql
-- Example: V5__add_enrichment_fields.sql

-- Always include comments explaining purpose
-- Add enrichment tracking for external KB linking

-- Use IF NOT EXISTS where appropriate
ALTER TABLE entities
ADD COLUMN IF NOT EXISTS enrichment_status VARCHAR(20) DEFAULT 'PENDING';

-- Create indexes with meaningful names
CREATE INDEX idx_entities_enrichment_status
ON entities(enrichment_status)
WHERE enrichment_status IN ('PENDING', 'FAILED');

-- Add column comments
COMMENT ON COLUMN entities.enrichment_status IS
'Status of external knowledge base enrichment';
```

### JSONB Usage

```sql
-- Store flexible properties in JSONB
CREATE TABLE entities (
    id UUID PRIMARY KEY,
    properties JSONB DEFAULT '{}'::jsonb,
    schema_org_data JSONB DEFAULT '{}'::jsonb
);

-- Create indexes for frequently queried JSONB fields
CREATE INDEX idx_entities_wikidata_id
ON entities ((properties->>'wikidata_id'))
WHERE properties->>'wikidata_id' IS NOT NULL;

-- Query JSONB
SELECT * FROM entities
WHERE properties->>'entity_type' = 'government_org'
  AND (properties->>'confidence')::float > 0.8;
```

---

## Git Standards

### Branch Naming

| Type | Pattern | Example |
|------|---------|---------|
| Feature | `feature/{story-id}-{short-desc}` | `feature/2.1.1-wikidata-client` |
| Bugfix | `fix/{issue-id}-{short-desc}` | `fix/123-entity-validation` |
| Hotfix | `hotfix/{desc}` | `hotfix/security-patch` |
| Release | `release/{version}` | `release/1.0.0` |

### Commit Messages

```
<type>(<scope>): <subject>

<body>

<footer>
```

**Types:** feat, fix, docs, style, refactor, test, chore

**Examples:**
```
feat(entity): add Wikidata entity linking

Implement WikidataClient to query Wikidata SPARQL endpoint
for entity matching. Includes caching and rate limiting.

Closes #42

---

fix(api): handle null entity type in extraction

Return 400 Bad Request instead of 500 when entity type
is missing from request body.

---

docs(readme): update installation instructions
```

### Pull Request Standards

- Title: `[Story 2.1.1] Implement Wikidata Entity Lookup`
- Description: Summary, changes list, testing done
- Labels: `feature`, `backend`, `python`, etc.
- Link to story/issue
- Screenshots for UI changes

---

## Documentation Standards

### Code Comments

```java
// Good: Explains WHY
// Use fuzzy matching because entity names from NER may have typos
List<GovernmentOrganization> matches = fuzzySearch(entityText);

// Bad: Explains WHAT (obvious from code)
// Loop through entities
for (Entity entity : entities) {
```

### JavaDoc / Docstrings

```java
/**
 * Validates an extracted entity against the government organization master data.
 *
 * @param entityText The entity name to validate (e.g., "EPA", "Department of Defense")
 * @param entityType The type of entity (must be GOVERNMENT_ORG for validation to proceed)
 * @return ValidationResult containing match status, confidence, and suggestions
 * @throws IllegalArgumentException if entityText is null or empty
 */
public ValidationResult validateEntity(String entityText, String entityType) {
```

```python
def extract_entities(text: str, confidence_threshold: float = 0.7) -> List[Entity]:
    """
    Extract named entities from text using spaCy NLP.

    Args:
        text: The text to analyze for entities
        confidence_threshold: Minimum confidence score (0.0-1.0) to include entity

    Returns:
        List of extracted entities meeting the confidence threshold

    Raises:
        ValueError: If text is empty or confidence_threshold is out of range

    Example:
        >>> entities = extract_entities("Senator Warren met with EPA officials.")
        >>> print(entities[0].text)
        'Warren'
    """
```

---

## Security Standards

### Input Validation

- Validate all user input at API boundaries
- Use Pydantic/Bean Validation for request DTOs
- Sanitize data before database queries
- Never trust client-side validation alone

### Authentication/Authorization

- Use JWT for API authentication
- Implement role-based access control
- Never expose internal IDs in errors
- Log authentication failures

### Secrets Management

- Never commit secrets to git
- Use environment variables in production
- Rotate secrets regularly
- Use different secrets per environment

---

## Performance Standards

### Database

- Use indexes for frequently queried columns
- Avoid N+1 queries (use JOINs or batch fetching)
- Use pagination for large result sets
- Cache frequently accessed, rarely changed data

### API

- Implement request timeouts
- Use async where appropriate (Python)
- Compress responses (gzip)
- Rate limit external API calls

### Frontend

- Lazy load components and routes
- Optimize images and assets
- Use React Query for server state caching
- Minimize bundle size

---

## Change Log

| Date | Version | Description | Author |
|------|---------|-------------|--------|
| 2025-11-25 | 1.0 | Initial coding standards | James (Dev) |

---

*End of Coding Standards*
