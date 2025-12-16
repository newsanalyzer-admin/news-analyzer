# NewsAnalyzer Reasoning Service

Python FastAPI service for entity extraction, logical reasoning, and Prolog inference.

**Performance Improvement:** Replaces V1's Java subprocess integration (500ms latency) with fast HTTP API (~50ms).

## Tech Stack

- **Python 3.11**
- **FastAPI 0.109** (async web framework)
- **spaCy 3.7** (NLP and entity extraction)
- **Transformers 4.36** (advanced NLP models)
- **PySwip 0.2.10** (Prolog integration via SWI-Prolog)
- **pytest** (testing)

## Features

1. **Entity Extraction** - Extract government entities, persons, organizations, locations, events, and concepts
2. **Government Organization Validation** - Validate and enrich extracted government entities against authoritative database
3. **Logical Reasoning** - Use Prolog for fact verification and relationship inference
4. **Fallacy Detection** - Identify logical fallacies and cognitive biases

## Project Structure

```
reasoning-service/
├── app/
│   ├── api/                      # API routes
│   │   ├── entities.py           # Entity extraction endpoints
│   │   ├── reasoning.py          # Prolog reasoning endpoints
│   │   └── fallacies.py          # Fallacy detection endpoints
│   ├── services/                 # Business logic
│   │   ├── entity_extractor.py  # spaCy-based entity extraction
│   │   ├── gov_org_validator.py # Government org validation
│   │   └── schema_mapper.py     # Schema.org mapping
│   ├── models/                   # Pydantic models
│   └── main.py                   # FastAPI application
├── tests/                        # Unit and integration tests
├── test_entity_validation.py     # Gov org validation test script
├── requirements.txt              # Python dependencies
├── PYTHON_311_SETUP.md           # Python 3.11 setup guide
└── .env.example                  # Environment variables template
```

## Government Organization Validation

The service integrates with the NewsAnalyzer backend database to validate and enrich extracted government organization entities.

**Matching Strategies:**
1. **Exact Name Match** - "Environmental Protection Agency" → EPA (confidence: 1.0)
2. **Exact Acronym Match** - "EPA" → Environmental Protection Agency (confidence: 1.0)
3. **Fuzzy Match** - "Enviromental Protection Agency" → EPA (confidence: 0.95)
4. **Partial Match** - "Protection Agency" → EPA (confidence: 0.60)

**Enriched Data:**
- Official name and acronym
- Organization type (DEPARTMENT, AGENCY, BUREAU, etc.)
- Branch (EXECUTIVE, LEGISLATIVE, JUDICIAL)
- Government level (FEDERAL, STATE, LOCAL)
- Website URL
- Established date
- Jurisdiction areas
- Active status

See `app/services/gov_org_validator.py` for implementation details.

## Setup

### Prerequisites

- **Python 3.11** (Required - see [PYTHON_311_SETUP.md](./PYTHON_311_SETUP.md))
- **Miniconda** (Recommended for environment management)
- SWI-Prolog 8.4+ (for Prolog reasoning - optional)

### Quick Start (Using Conda)

```powershell
# Create and activate conda environment
conda create -n newsanalyzer python=3.11 -y
conda activate newsanalyzer

# Navigate to reasoning service
cd reasoning-service

# Install dependencies
pip install -r requirements.txt

# Download spaCy model
python -m spacy download en_core_web_sm

# Start the service
uvicorn app.main:app --reload --port 8000
```

The API will be available at http://localhost:8000

For detailed setup instructions, see [PYTHON_311_SETUP.md](./PYTHON_311_SETUP.md)

## API Documentation

Once running, access:
- Swagger UI: http://localhost:8000/docs
- ReDoc: http://localhost:8000/redoc

## API Endpoints

### Entity Extraction

```bash
POST /entities/extract
{
  "text": "The Environmental Protection Agency announced new regulations today.",
  "confidence_threshold": 0.7
}
```

**Response includes:**
- Extracted entities with types (person, organization, location, government_org, etc.)
- Government organization validation status
- Official data for verified government entities (official name, acronym, branch, website, etc.)
- Match confidence and type (exact, fuzzy, partial)
- Suggested matches for unverified entities

**Example:**
```bash
python test_entity_validation.py
```

### Prolog Reasoning

```bash
POST /reasoning/query
{
  "query": "parent(X, john)",
  "knowledge_base": ["parent(john, mary)."]
}
```

### Fact Verification

```bash
POST /reasoning/verify-fact
{
  "claim": "The Senate voted 60-40 on the bill",
  "evidence": ["source1.com", "source2.com"]
}
```

### Fallacy Detection

```bash
POST /fallacies/detect
{
  "text": "We can't trust his argument because he's not an expert.",
  "context": "Debate about climate change"
}
```

## Testing

```bash
# Run all tests
pytest

# Run with coverage
pytest --cov=app tests/

# Run specific test file
pytest tests/test_entities.py
```

## Development

### Code Formatting

```bash
black app/ tests/
```

### Linting

```bash
ruff check app/ tests/
```

### Type Checking

```bash
mypy app/
```

## Docker Build

```bash
docker build -t newsanalyzer-reasoning-service:latest .
```

## Performance

- **Average response time:** ~50ms (vs 500ms in V1's subprocess approach)
- **Concurrent requests:** Handled via FastAPI's async capabilities
- **Throughput:** ~100 requests/second (single instance)

## License

MIT License - See [LICENSE](../LICENSE)
