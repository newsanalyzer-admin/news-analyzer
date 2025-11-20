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
2. **Logical Reasoning** - Use Prolog for fact verification and relationship inference
3. **Fallacy Detection** - Identify logical fallacies and cognitive biases

## Project Structure

```
reasoning-service/
├── app/
│   ├── api/              # API routes
│   │   ├── entities.py   # Entity extraction endpoints
│   │   ├── reasoning.py  # Prolog reasoning endpoints
│   │   └── fallacies.py  # Fallacy detection endpoints
│   ├── services/         # Business logic
│   ├── models/           # Pydantic models
│   └── main.py           # FastAPI application
├── tests/                # Unit and integration tests
├── requirements.txt      # Python dependencies
└── .env.example          # Environment variables template
```

## Setup

### Prerequisites

- Python 3.11+
- SWI-Prolog 8.4+ (for Prolog reasoning)

### 1. Create Virtual Environment

```bash
python -m venv venv
source venv/bin/activate  # Windows: venv\Scripts\activate
```

### 2. Install Dependencies

```bash
pip install -r requirements.txt
```

### 3. Download spaCy Model

```bash
python -m spacy download en_core_web_lg
```

### 4. Configure Environment

```bash
cp .env.example .env
# Edit .env with your settings
```

### 5. Run Development Server

```bash
uvicorn app.main:app --reload --port 8000
```

The API will be available at http://localhost:8000

## API Documentation

Once running, access:
- Swagger UI: http://localhost:8000/docs
- ReDoc: http://localhost:8000/redoc

## API Endpoints

### Entity Extraction

```bash
POST /entities/extract
{
  "text": "Congress passed a bill today...",
  "confidence_threshold": 0.7
}
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
