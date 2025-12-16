"""
Entity Linker Orchestrator

Coordinates Wikidata and DBpedia clients to link entities to external
knowledge bases, using disambiguation to select the best match.
"""

import asyncio
import logging
from dataclasses import dataclass, field
from enum import Enum
from typing import Dict, List, Optional, Any

from app.services.wikidata_client import (
    WikidataClient,
    WikidataCandidate,
    get_wikidata_client,
    EntityType as WikidataEntityType,
)
from app.services.dbpedia_client import (
    DBpediaClient,
    DBpediaCandidate,
    get_dbpedia_client,
    EntityType as DBpediaEntityType,
)
from app.services.disambiguation import (
    DisambiguationService,
    Candidate,
    DisambiguationResult,
    EntityType,
    get_disambiguation_service,
)

logger = logging.getLogger(__name__)


class LinkingSource(str, Enum):
    """Source selection for entity linking"""
    WIKIDATA = "wikidata"
    DBPEDIA = "dbpedia"
    BOTH = "both"


class LinkingStatus(str, Enum):
    """Status of entity linking"""
    LINKED = "linked"
    NEEDS_REVIEW = "needs_review"
    NOT_FOUND = "not_found"
    ERROR = "error"


@dataclass
class LinkedEntity:
    """Result of linking a single entity"""
    text: str
    entity_type: str
    wikidata_id: Optional[str] = None
    wikidata_url: Optional[str] = None
    dbpedia_uri: Optional[str] = None
    linking_confidence: float = 0.0
    linking_source: Optional[str] = None
    linking_status: LinkingStatus = LinkingStatus.NOT_FOUND
    needs_review: bool = False
    is_ambiguous: bool = False
    candidates: List[Dict[str, Any]] = field(default_factory=list)
    error: Optional[str] = None

    def to_dict(self) -> Dict[str, Any]:
        """Convert to dictionary for JSON serialization"""
        return {
            "text": self.text,
            "entity_type": self.entity_type,
            "wikidata_id": self.wikidata_id,
            "wikidata_url": self.wikidata_url,
            "dbpedia_uri": self.dbpedia_uri,
            "linking_confidence": round(self.linking_confidence, 3),
            "linking_source": self.linking_source,
            "linking_status": self.linking_status.value,
            "needs_review": self.needs_review,
            "is_ambiguous": self.is_ambiguous,
            "candidates": self.candidates,
            "error": self.error,
        }


@dataclass
class LinkingStatistics:
    """Statistics for a batch linking operation"""
    total: int = 0
    linked: int = 0
    needs_review: int = 0
    not_found: int = 0
    errors: int = 0

    def to_dict(self) -> Dict[str, Any]:
        """Convert to dictionary for JSON serialization"""
        return {
            "total": self.total,
            "linked": self.linked,
            "needs_review": self.needs_review,
            "not_found": self.not_found,
            "errors": self.errors,
            "success_rate": round(self.linked / self.total, 3) if self.total > 0 else 0.0,
        }


@dataclass
class LinkingResult:
    """Result of batch entity linking"""
    linked_entities: List[LinkedEntity]
    statistics: LinkingStatistics

    def to_dict(self) -> Dict[str, Any]:
        """Convert to dictionary for JSON serialization"""
        return {
            "linked_entities": [e.to_dict() for e in self.linked_entities],
            "statistics": self.statistics.to_dict(),
        }


class EntityLinker:
    """
    Orchestrates entity linking using Wikidata and DBpedia.

    Coordinates the external KB clients and disambiguation service
    to find the best matching external entity for each input.
    """

    # Confidence threshold for automatic linking
    CONFIDENCE_THRESHOLD = 0.7

    def __init__(
        self,
        wikidata_client: Optional[WikidataClient] = None,
        dbpedia_client: Optional[DBpediaClient] = None,
        disambiguation_service: Optional[DisambiguationService] = None
    ):
        """
        Initialize the EntityLinker.

        Args:
            wikidata_client: Optional WikidataClient instance
            dbpedia_client: Optional DBpediaClient instance
            disambiguation_service: Optional DisambiguationService instance
        """
        self._wikidata = wikidata_client or get_wikidata_client()
        self._dbpedia = dbpedia_client or get_dbpedia_client()
        self._disambiguator = disambiguation_service or get_disambiguation_service()
        logger.info("EntityLinker initialized")

    def _map_entity_type(self, entity_type: str) -> Optional[EntityType]:
        """Map string entity type to EntityType enum"""
        type_map = {
            "person": EntityType.PERSON,
            "organization": EntityType.ORGANIZATION,
            "government_org": EntityType.GOVERNMENT_ORG,
            "location": EntityType.LOCATION,
            "event": EntityType.EVENT,
        }
        return type_map.get(entity_type.lower())

    def _map_to_wikidata_type(self, entity_type: str) -> Optional[WikidataEntityType]:
        """Map string entity type to Wikidata EntityType enum"""
        type_map = {
            "person": WikidataEntityType.PERSON,
            "organization": WikidataEntityType.ORGANIZATION,
            "government_org": WikidataEntityType.GOVERNMENT_ORG,
            "location": WikidataEntityType.LOCATION,
            "event": WikidataEntityType.EVENT,
        }
        return type_map.get(entity_type.lower())

    def _map_to_dbpedia_type(self, entity_type: str) -> Optional[DBpediaEntityType]:
        """Map string entity type to DBpedia EntityType enum"""
        type_map = {
            "person": DBpediaEntityType.PERSON,
            "organization": DBpediaEntityType.ORGANIZATION,
            "government_org": DBpediaEntityType.GOVERNMENT_ORG,
            "location": DBpediaEntityType.LOCATION,
            "event": DBpediaEntityType.EVENT,
        }
        return type_map.get(entity_type.lower())

    def _convert_wikidata_candidates(
        self,
        wikidata_candidates: List[WikidataCandidate]
    ) -> List[Candidate]:
        """Convert WikidataCandidate objects to disambiguation Candidate objects"""
        return [
            Candidate(
                id=c.qid,
                label=c.label,
                description=c.description,
                types=c.instance_of,
                aliases=c.aliases,
                source="wikidata",
                original_confidence=c.confidence
            )
            for c in wikidata_candidates
        ]

    def _convert_dbpedia_candidates(
        self,
        dbpedia_candidates: List[DBpediaCandidate]
    ) -> List[Candidate]:
        """Convert DBpediaCandidate objects to disambiguation Candidate objects"""
        return [
            Candidate(
                id=c.uri,
                label=c.label,
                description=c.description,
                types=c.types,
                aliases=[],
                source="dbpedia",
                original_confidence=c.confidence
            )
            for c in dbpedia_candidates
        ]

    def link_entity(
        self,
        text: str,
        entity_type: str,
        context: Optional[str] = None,
        sources: LinkingSource = LinkingSource.BOTH,
        min_confidence: float = CONFIDENCE_THRESHOLD,
        max_candidates: int = 5
    ) -> LinkedEntity:
        """
        Link a single entity to external knowledge bases.

        Args:
            text: Entity text to link
            entity_type: Type of entity (person, organization, etc.)
            context: Optional surrounding text for disambiguation
            sources: Which KB sources to query
            min_confidence: Minimum confidence for automatic linking
            max_candidates: Maximum candidates to return

        Returns:
            LinkedEntity with external IDs and confidence
        """
        logger.info(f"Linking entity: '{text}' (type={entity_type}, sources={sources})")

        candidates: List[Candidate] = []
        wikidata_type = self._map_to_wikidata_type(entity_type)
        dbpedia_type = self._map_to_dbpedia_type(entity_type)
        mapped_type = self._map_entity_type(entity_type)

        try:
            # Query Wikidata first
            if sources in [LinkingSource.WIKIDATA, LinkingSource.BOTH]:
                logger.debug(f"Querying Wikidata for '{text}'")
                wikidata_result = self._wikidata.search(text, wikidata_type)
                candidates.extend(
                    self._convert_wikidata_candidates(wikidata_result.candidates)
                )
                logger.debug(f"Found {len(wikidata_result.candidates)} Wikidata candidates")

            # Query DBpedia as fallback or if requested
            if sources == LinkingSource.DBPEDIA or (
                sources == LinkingSource.BOTH and len(candidates) == 0
            ):
                logger.debug(f"Querying DBpedia for '{text}'")
                dbpedia_result = self._dbpedia.search(text, dbpedia_type)
                candidates.extend(
                    self._convert_dbpedia_candidates(dbpedia_result.candidates)
                )
                logger.debug(f"Found {len(dbpedia_result.candidates)} DBpedia candidates")

            # No candidates found
            if not candidates:
                logger.info(f"No candidates found for '{text}'")
                return LinkedEntity(
                    text=text,
                    entity_type=entity_type,
                    linking_confidence=0.0,
                    linking_status=LinkingStatus.NOT_FOUND,
                    needs_review=True
                )

            # Disambiguate to select best match
            logger.debug(f"Disambiguating {len(candidates)} candidates for '{text}'")
            disambiguation_result = self._disambiguator.disambiguate(
                entity_text=text,
                entity_type=mapped_type,
                candidates=candidates,
                context=context
            )

            # Build result
            linked_entity = LinkedEntity(
                text=text,
                entity_type=entity_type,
                linking_confidence=disambiguation_result.confidence,
                needs_review=disambiguation_result.needs_review,
                is_ambiguous=disambiguation_result.is_ambiguous,
            )

            # Set external IDs if we have a match
            if disambiguation_result.match:
                match = disambiguation_result.match
                linked_entity.linking_source = match.source

                if match.source == "wikidata":
                    linked_entity.wikidata_id = match.id
                    linked_entity.wikidata_url = f"https://www.wikidata.org/wiki/{match.id}"
                elif match.source == "dbpedia":
                    linked_entity.dbpedia_uri = match.id

                # Determine status
                if disambiguation_result.confidence >= min_confidence:
                    linked_entity.linking_status = LinkingStatus.LINKED
                else:
                    linked_entity.linking_status = LinkingStatus.NEEDS_REVIEW

            # Include candidates for review if needed
            if disambiguation_result.needs_review:
                linked_entity.candidates = [
                    c.to_dict() for c in disambiguation_result.all_candidates[:max_candidates]
                ]

            logger.info(
                f"Linked '{text}' -> "
                f"{linked_entity.wikidata_id or linked_entity.dbpedia_uri or 'no match'} "
                f"(confidence={linked_entity.linking_confidence:.2f}, "
                f"status={linked_entity.linking_status.value})"
            )

            return linked_entity

        except Exception as e:
            logger.error(f"Error linking entity '{text}': {e}")
            return LinkedEntity(
                text=text,
                entity_type=entity_type,
                linking_status=LinkingStatus.ERROR,
                needs_review=True,
                error=str(e)
            )

    def link_batch(
        self,
        entities: List[Dict[str, Any]],
        sources: LinkingSource = LinkingSource.BOTH,
        min_confidence: float = CONFIDENCE_THRESHOLD,
        max_candidates: int = 5
    ) -> LinkingResult:
        """
        Link a batch of entities to external knowledge bases.

        Args:
            entities: List of entity dicts with text, entity_type, and optional context
            sources: Which KB sources to query
            min_confidence: Minimum confidence for automatic linking
            max_candidates: Maximum candidates per entity

        Returns:
            LinkingResult with all linked entities and statistics
        """
        logger.info(f"Batch linking {len(entities)} entities")

        linked_entities: List[LinkedEntity] = []
        stats = LinkingStatistics(total=len(entities))

        for entity in entities:
            result = self.link_entity(
                text=entity.get("text", ""),
                entity_type=entity.get("entity_type", ""),
                context=entity.get("context"),
                sources=sources,
                min_confidence=min_confidence,
                max_candidates=max_candidates
            )

            linked_entities.append(result)

            # Update statistics
            if result.linking_status == LinkingStatus.LINKED:
                stats.linked += 1
            elif result.linking_status == LinkingStatus.NEEDS_REVIEW:
                stats.needs_review += 1
            elif result.linking_status == LinkingStatus.NOT_FOUND:
                stats.not_found += 1
            elif result.linking_status == LinkingStatus.ERROR:
                stats.errors += 1

        logger.info(
            f"Batch linking complete: {stats.linked} linked, "
            f"{stats.needs_review} need review, {stats.not_found} not found, "
            f"{stats.errors} errors"
        )

        return LinkingResult(
            linked_entities=linked_entities,
            statistics=stats
        )

    async def link_batch_async(
        self,
        entities: List[Dict[str, Any]],
        sources: LinkingSource = LinkingSource.BOTH,
        min_confidence: float = CONFIDENCE_THRESHOLD,
        max_candidates: int = 5
    ) -> LinkingResult:
        """
        Asynchronously link a batch of entities.

        Note: Current implementation processes sequentially to respect rate limits.
        Future enhancement could use semaphores for controlled parallelism.

        Args:
            entities: List of entity dicts
            sources: Which KB sources to query
            min_confidence: Minimum confidence threshold
            max_candidates: Maximum candidates per entity

        Returns:
            LinkingResult with all linked entities and statistics
        """
        # For now, delegate to synchronous implementation
        # Rate limiting in the clients makes true parallelism problematic
        return self.link_batch(
            entities=entities,
            sources=sources,
            min_confidence=min_confidence,
            max_candidates=max_candidates
        )


# Singleton instance
_linker_instance: Optional[EntityLinker] = None


def get_entity_linker() -> EntityLinker:
    """
    Get or create singleton EntityLinker instance.

    Returns:
        EntityLinker instance
    """
    global _linker_instance
    if _linker_instance is None:
        _linker_instance = EntityLinker()
    return _linker_instance
