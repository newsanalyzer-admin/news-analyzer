"""
NewsAnalyzer Reasoning Service

FastAPI service for entity extraction, logical reasoning, and Prolog inference.
Replaces V1's slow Java subprocess integration (500ms) with fast HTTP API (50ms).
"""

from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import JSONResponse

from app.api import entities, reasoning, fallacies

app = FastAPI(
    title="NewsAnalyzer Reasoning Service",
    description="Entity extraction, logical reasoning, and Prolog inference for news analysis",
    version="2.0.0",
    docs_url="/docs",
    redoc_url="/redoc",
)

# CORS middleware
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],  # Configure in production
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)


@app.get("/")
async def root():
    """Health check endpoint"""
    return {
        "service": "NewsAnalyzer Reasoning Service",
        "version": "2.0.0",
        "status": "healthy",
    }


@app.get("/health")
async def health():
    """Detailed health check"""
    return JSONResponse(
        content={
            "status": "healthy",
            "services": {
                "spacy": "loaded",
                "prolog": "available",
            }
        }
    )


# Include routers
app.include_router(entities.router, prefix="/entities", tags=["entities"])
app.include_router(reasoning.router, prefix="/reasoning", tags=["reasoning"])
app.include_router(fallacies.router, prefix="/fallacies", tags=["fallacies"])


if __name__ == "__main__":
    import uvicorn
    uvicorn.run("app.main:app", host="0.0.0.0", port=8000, reload=True)
