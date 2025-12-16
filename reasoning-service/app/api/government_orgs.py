"""
Government Organizations API

FastAPI endpoints for government organization data ingestion and processing.

@author Winston (Architect Agent)
@since 2.0.0
"""

import logging
from typing import Dict, Any, Optional, List
from datetime import datetime

from fastapi import APIRouter, HTTPException, BackgroundTasks, Query
from pydantic import BaseModel, Field

logger = logging.getLogger(__name__)

router = APIRouter()


# =====================================================================
# Request/Response Models
# =====================================================================

class IngestionRequest(BaseModel):
    """Request model for triggering ingestion"""
    year: int = Field(2024, ge=1995, le=2030, description="Year of Government Manual")
    save_to_file: bool = Field(False, description="Save intermediate results to files")
    output_dir: Optional[str] = Field(None, description="Directory for output files")


class IngestionResponse(BaseModel):
    """Response model for ingestion operation"""
    status: str = Field(..., description="Status: in_progress, completed, failed")
    year: int
    total_organizations: int = 0
    packages_processed: int = 0
    packages_total: int = 0
    error_count: int = 0
    duration_seconds: Optional[float] = None
    started_at: str
    completed_at: Optional[str] = None
    message: str = ""


class PackageProcessRequest(BaseModel):
    """Request model for processing single package"""
    package_id: str = Field(..., description="GovInfo package ID")


class PackageProcessResponse(BaseModel):
    """Response model for package processing"""
    package_id: str
    organizations: List[Dict[str, Any]]
    count: int
    processed_at: str


class OrganizationEnrichmentRequest(BaseModel):
    """Request model for enriching entity with government org data"""
    entity_text: str = Field(..., description="Entity text to enrich")
    entity_type: str = Field(..., description="Entity type")
    confidence: float = Field(1.0, ge=0.0, le=1.0)
    properties: Dict[str, Any] = Field(default_factory=dict)


class OrganizationEnrichmentResponse(BaseModel):
    """Response model for entity enrichment"""
    entity_text: str
    entity_type: str
    confidence: float
    is_government_org: bool
    validation_result: Optional[Dict[str, Any]] = None
    enrichment_data: Optional[Dict[str, Any]] = None
    reasoning_applied: bool = False


# =====================================================================
# API Endpoints
# =====================================================================

@router.post("/ingest", response_model=IngestionResponse)
async def ingest_government_manual(
    request: IngestionRequest,
    background_tasks: BackgroundTasks
):
    """
    Trigger ingestion of US Government Manual data from GovInfo API.

    This endpoint fetches, parses, and processes Government Manual data for the
    specified year. Processing happens in the background for large datasets.

    Example:
        POST /government-orgs/ingest
        {
            "year": 2024,
            "save_to_file": false
        }

    Returns:
        Ingestion status and preliminary results
    """
    try:
        from app.services.gov_org_ingestion import get_ingestion_service

        service = get_ingestion_service()

        # For demo, run synchronously (in production, use background task)
        result = service.ingest_year(
            year=request.year,
            save_to_file=request.save_to_file,
            output_dir=request.output_dir
        )

        return IngestionResponse(
            status="completed",
            year=result['year'],
            total_organizations=result['total_organizations'],
            packages_processed=result['packages_processed'],
            packages_total=result['packages_total'],
            error_count=result['error_count'],
            duration_seconds=result['duration_seconds'],
            started_at=result['started_at'],
            completed_at=result['completed_at'],
            message=f"Successfully ingested {result['total_organizations']} organizations"
        )

    except ImportError as e:
        raise HTTPException(
            status_code=503,
            detail=f"Ingestion service not available: {str(e)}"
        )
    except ValueError as e:
        raise HTTPException(
            status_code=400,
            detail=f"Invalid request: {str(e)}"
        )
    except Exception as e:
        logger.error(f"Ingestion failed: {e}", exc_info=True)
        raise HTTPException(
            status_code=500,
            detail=f"Ingestion failed: {str(e)}"
        )


@router.post("/process-package", response_model=PackageProcessResponse)
async def process_package(request: PackageProcessRequest):
    """
    Process a single Government Manual package.

    Fetches and processes a specific GovInfo package by ID.

    Example:
        POST /government-orgs/process-package
        {
            "package_id": "GOVMAN-2024-12-01"
        }

    Returns:
        Processed organizations from the package
    """
    try:
        from app.services.gov_org_ingestion import get_ingestion_service

        service = get_ingestion_service()
        result = service.process_package(request.package_id)

        return PackageProcessResponse(**result)

    except ImportError as e:
        raise HTTPException(
            status_code=503,
            detail=f"Ingestion service not available: {str(e)}"
        )
    except Exception as e:
        logger.error(f"Package processing failed: {e}", exc_info=True)
        raise HTTPException(
            status_code=500,
            detail=f"Package processing failed: {str(e)}"
        )


@router.get("/fetch-packages")
async def fetch_packages(
    year: int = Query(2024, ge=1995, le=2030, description="Year of Government Manual"),
    page_size: int = Query(100, ge=1, le=100, description="Results per page"),
    offset: int = Query(0, ge=0, description="Pagination offset")
):
    """
    Fetch available Government Manual packages from GovInfo API.

    Lists available packages without processing them.

    Example:
        GET /government-orgs/fetch-packages?year=2024&page_size=10

    Returns:
        List of available packages with metadata
    """
    try:
        from app.services.gov_org_ingestion import GovInfoAPIClient

        client = GovInfoAPIClient()
        data = client.get_collection_packages(
            collection="GOVMAN",
            year=year,
            page_size=page_size,
            offset=offset
        )

        return {
            "year": year,
            "packages": data.get('packages', []),
            "count": data.get('count', 0),
            "offset": offset,
            "page_size": page_size
        }

    except ValueError as e:
        raise HTTPException(
            status_code=400,
            detail=f"Invalid request: {str(e)}"
        )
    except Exception as e:
        logger.error(f"Fetch packages failed: {e}", exc_info=True)
        raise HTTPException(
            status_code=500,
            detail=f"Failed to fetch packages: {str(e)}"
        )


@router.post("/enrich-entity", response_model=OrganizationEnrichmentResponse)
async def enrich_entity_with_gov_data(request: OrganizationEnrichmentRequest):
    """
    Enrich entity with official government organization data.

    Validates entity text against official government organizations and
    enriches with additional metadata if match is found.

    Example:
        POST /government-orgs/enrich-entity
        {
            "entity_text": "EPA",
            "entity_type": "government_org",
            "confidence": 0.95
        }

    Returns:
        Enriched entity with validation result and official data
    """
    # This endpoint would integrate with the Java backend
    # For now, return structure for future implementation

    return OrganizationEnrichmentResponse(
        entity_text=request.entity_text,
        entity_type=request.entity_type,
        confidence=request.confidence,
        is_government_org=request.entity_type == "government_org",
        validation_result={
            "status": "not_implemented",
            "message": "Validation requires Java backend integration"
        },
        enrichment_data=None,
        reasoning_applied=False
    )


@router.get("/health")
async def health_check():
    """
    Health check endpoint for government organization ingestion service.

    Returns:
        Service status and configuration
    """
    import os

    api_key_configured = bool(os.getenv('GOVINFO_API_KEY'))

    return {
        "service": "Government Organization Ingestion",
        "status": "healthy",
        "api_key_configured": api_key_configured,
        "features": {
            "ingestion": True,
            "parsing": True,
            "schema_org_transformation": True
        },
        "timestamp": datetime.utcnow().isoformat()
    }


@router.get("/test-api-connection")
async def test_api_connection():
    """
    Test connection to GovInfo API.

    Verifies API key and connectivity.

    Returns:
        Connection test result
    """
    try:
        from app.services.gov_org_ingestion import GovInfoAPIClient

        client = GovInfoAPIClient()

        # Test API with a simple request (get 2024 packages, limit 1)
        data = client.get_collection_packages(
            collection="GOVMAN",
            year=2024,
            page_size=1,
            offset=0
        )

        return {
            "status": "success",
            "message": "GovInfo API connection successful",
            "api_accessible": True,
            "sample_package_count": data.get('count', 0),
            "timestamp": datetime.utcnow().isoformat()
        }

    except ValueError as e:
        return {
            "status": "error",
            "message": f"API key not configured: {str(e)}",
            "api_accessible": False,
            "timestamp": datetime.utcnow().isoformat()
        }
    except Exception as e:
        logger.error(f"API connection test failed: {e}", exc_info=True)
        return {
            "status": "error",
            "message": f"Connection failed: {str(e)}",
            "api_accessible": False,
            "timestamp": datetime.utcnow().isoformat()
        }
