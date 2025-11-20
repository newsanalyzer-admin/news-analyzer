"""
Logical Fallacy Detection API

Detects common logical fallacies and cognitive biases in text using NLP and Prolog rules.
"""

from fastapi import APIRouter
from pydantic import BaseModel, Field
from typing import List, Optional

router = APIRouter()


class FallacyDetectionRequest(BaseModel):
    """Request model for fallacy detection"""
    text: str = Field(..., description="Text to analyze for fallacies")
    context: Optional[str] = Field(None, description="Additional context")


class Fallacy(BaseModel):
    """Detected fallacy model"""
    type: str = Field(..., description="Type of fallacy (e.g., ad_hominem, strawman)")
    excerpt: str = Field(..., description="Text excerpt containing the fallacy")
    explanation: str = Field(..., description="Explanation of why this is a fallacy")
    confidence: float = Field(..., ge=0.0, le=1.0)


class BiasDetection(BaseModel):
    """Detected cognitive bias"""
    type: str = Field(..., description="Type of bias (e.g., confirmation_bias, framing)")
    excerpt: str = Field(..., description="Text excerpt showing the bias")
    explanation: str = Field(..., description="Explanation of the bias")
    confidence: float = Field(..., ge=0.0, le=1.0)


class FallacyDetectionResponse(BaseModel):
    """Response model for fallacy detection"""
    fallacies: List[Fallacy]
    biases: List[BiasDetection]
    overall_quality_score: float = Field(..., ge=0.0, le=1.0)


@router.post("/detect", response_model=FallacyDetectionResponse)
async def detect_fallacies(request: FallacyDetectionRequest):
    """
    Detect logical fallacies and cognitive biases in text.

    Common fallacies detected:
    - Ad hominem (attacking the person)
    - Strawman (misrepresenting argument)
    - False dilemma (only two options presented)
    - Slippery slope (chain of unlikely events)
    - Appeal to authority (invalid authority)
    - Red herring (irrelevant distraction)
    - Circular reasoning (conclusion in premise)
    - Hasty generalization (insufficient evidence)

    Common biases detected:
    - Confirmation bias
    - Framing effects
    - Emotional manipulation
    - Cherry-picking evidence
    - False balance
    """
    # TODO: Implement fallacy detection using NLP + Prolog rules
    return FallacyDetectionResponse(
        fallacies=[],
        biases=[],
        overall_quality_score=0.0
    )
