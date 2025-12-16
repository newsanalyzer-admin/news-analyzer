"""
DBpedia Entity Lookup Client

Queries DBpedia Lookup API to find matching entities by name and type.
Serves as a fallback when Wikidata returns no results.
"""

import logging
import time
import hashlib
from dataclasses import dataclass, field
from enum import Enum
from typing import Dict, List, Optional, Any

import httpx
from cachetools import TTLCache
from rapidfuzz import fuzz

logger = logging.getLogger(__name__)


class EntityType(str, Enum):
    """Entity types supported for DBpedia lookup"""
    PERSON = "person"
    ORGANIZATION = "organization"
    GOVERNMENT_ORG = "government_org"
    LOCATION = "location"
    EVENT = "event"


# Mapping from NewsAnalyzer entity types to DBpedia ontology classes
ENTITY_TYPE_TO_DBPEDIA: Dict[EntityType, List[str]] = {
    EntityType.PERSON: ["Person"],
    EntityType.ORGANIZATION: ["Organisation", "Company"],
    EntityType.GOVERNMENT_ORG: ["GovernmentAgency", "Organisation"],
    EntityType.LOCATION: ["Place", "City", "Country", "PopulatedPlace"],
    EntityType.EVENT: ["Event"],
}


@dataclass
class DBpediaCandidate:
    """A candidate entity match from DBpedia"""
    uri: str
    label: str
    description: Optional[str] = None
    types: List[str] = field(default_factory=list)
    ref_count: int = 0
    confidence: float = 0.0

    @property
    def resource_name(self) -> str:
        """Extract resource name from URI"""
        if self.uri:
            return self.uri.split("/")[-1].replace("_", " ")
        return ""

    def to_dict(self) -> Dict[str, Any]:
        """Convert to dictionary for JSON serialization"""
        return {
            "uri": self.uri,
            "label": self.label,
            "description": self.description,
            "types": self.types,
            "ref_count": self.ref_count,
            "confidence": round(self.confidence, 3),
            "resource_name": self.resource_name,
            "dbpedia_url": self.uri,
        }


@dataclass
class DBpediaSearchResult:
    """Result of a DBpedia entity search"""
    query: str
    entity_type: Optional[EntityType]
    candidates: List[DBpediaCandidate] = field(default_factory=list)
    from_cache: bool = False
    error: Optional[str] = None

    def to_dict(self) -> Dict[str, Any]:
        """Convert to dictionary for JSON serialization"""
        return {
            "query": self.query,
            "entity_type": self.entity_type.value if self.entity_type else None,
            "candidates": [c.to_dict() for c in self.candidates],
            "candidate_count": len(self.candidates),
            "from_cache": self.from_cache,
            "error": self.error,
        }


class DBpediaClient:
    """Client for querying DBpedia Lookup API"""

    LOOKUP_API = "https://lookup.dbpedia.org/api/search"

    # Rate limiting: be conservative with DBpedia
    MIN_REQUEST_INTERVAL = 0.5

    # Request timeout
    REQUEST_TIMEOUT = 10.0

    # Cache TTL: 24 hours
    CACHE_TTL = 86400

    # Max candidates to return
    MAX_CANDIDATES = 5

    def __init__(
        self,
        cache_ttl: int = CACHE_TTL,
        cache_maxsize: int = 1000,
        timeout: float = REQUEST_TIMEOUT
    ):
        """
        Initialize the DBpedia client.

        Args:
            cache_ttl: Cache time-to-live in seconds (default: 24 hours)
            cache_maxsize: Maximum cache entries
            timeout: HTTP request timeout in seconds
        """
        self._cache: TTLCache = TTLCache(maxsize=cache_maxsize, ttl=cache_ttl)
        self._last_request_time: float = 0.0
        self._timeout = timeout
        self._client = httpx.Client(
            timeout=timeout,
            headers={
                "User-Agent": "NewsAnalyzer/2.0 (https://github.com/newsanalyzer)",
                "Accept": "application/json",
            }
        )
        logger.info(f"DBpediaClient initialized with cache_ttl={cache_ttl}s, maxsize={cache_maxsize}")

    def _get_cache_key(self, query: str, entity_type: Optional[EntityType]) -> str:
        """Generate cache key from query and entity type"""
        normalized_query = query.lower().strip()
        type_str = entity_type.value if entity_type else "any"
        key_str = f"dbpedia:{type_str}:{normalized_query}"
        return hashlib.md5(key_str.encode()).hexdigest()

    def _rate_limit(self) -> None:
        """Enforce rate limiting between requests"""
        elapsed = time.time() - self._last_request_time
        if elapsed < self.MIN_REQUEST_INTERVAL:
            sleep_time = self.MIN_REQUEST_INTERVAL - elapsed
            logger.debug(f"Rate limiting: sleeping {sleep_time:.2f}s")
            time.sleep(sleep_time)
        self._last_request_time = time.time()

    def _get_dbpedia_type_filter(self, entity_type: EntityType) -> Optional[str]:
        """
        Get DBpedia type filter for entity type.

        Args:
            entity_type: NewsAnalyzer entity type

        Returns:
            Primary DBpedia class for filtering
        """
        if entity_type not in ENTITY_TYPE_TO_DBPEDIA:
            return None
        classes = ENTITY_TYPE_TO_DBPEDIA[entity_type]
        return classes[0] if classes else None

    def _execute_search(
        self,
        query: str,
        entity_type: Optional[EntityType] = None,
        max_results: int = 10
    ) -> Optional[Dict[str, Any]]:
        """
        Execute search against DBpedia Lookup API.

        Args:
            query: Search term
            entity_type: Optional type filter
            max_results: Maximum results to return

        Returns:
            JSON response or None on error
        """
        self._rate_limit()

        params = {
            "query": query,
            "maxResults": max_results,
            "format": "json",
        }

        # Add type filter if specified
        if entity_type:
            dbpedia_type = self._get_dbpedia_type_filter(entity_type)
            if dbpedia_type:
                params["type"] = dbpedia_type

        try:
            response = self._client.get(self.LOOKUP_API, params=params)
            response.raise_for_status()
            return response.json()

        except httpx.TimeoutException:
            logger.error(f"DBpedia query timed out after {self._timeout}s")
            return None
        except httpx.HTTPStatusError as e:
            logger.error(f"DBpedia HTTP error: {e.response.status_code}")
            return None
        except Exception as e:
            logger.error(f"DBpedia query failed: {e}")
            return None

    def _parse_response(self, response: Dict[str, Any]) -> List[DBpediaCandidate]:
        """
        Parse DBpedia Lookup API response into candidates.

        Args:
            response: JSON response from API

        Returns:
            List of DBpediaCandidate objects
        """
        candidates = []
        docs = response.get("docs", [])

        for doc in docs:
            # DBpedia returns arrays for most fields
            resource = doc.get("resource", [])
            uri = resource[0] if resource else ""

            label_list = doc.get("label", [])
            label = label_list[0] if label_list else ""

            comment_list = doc.get("comment", [])
            description = comment_list[0] if comment_list else None

            # Types are URIs
            type_list = doc.get("type", [])
            types = [t.split("/")[-1] for t in type_list if isinstance(t, str)]

            ref_count_list = doc.get("refCount", [])
            ref_count = ref_count_list[0] if ref_count_list else 0

            if uri and label:
                candidates.append(DBpediaCandidate(
                    uri=uri,
                    label=label,
                    description=description,
                    types=types,
                    ref_count=ref_count
                ))

        return candidates

    def _calculate_confidence(
        self,
        query: str,
        label: str,
        ref_count: int,
        type_match: bool,
        max_ref_count: int = 10000
    ) -> float:
        """
        Calculate confidence score for a candidate match.

        Args:
            query: Original search query
            label: Candidate label
            ref_count: DBpedia reference count (popularity)
            type_match: Whether entity type matches filter
            max_ref_count: Normalization factor for ref_count

        Returns:
            Confidence score between 0.0 and 1.0
        """
        query_lower = query.lower().strip()
        label_lower = label.lower().strip()

        # Exact match gets highest score
        if query_lower == label_lower:
            base_score = 1.0
        else:
            # Use fuzzy matching for similarity
            ratio = fuzz.ratio(query_lower, label_lower) / 100.0
            token_ratio = fuzz.token_sort_ratio(query_lower, label_lower) / 100.0
            partial_ratio = fuzz.partial_ratio(query_lower, label_lower) / 100.0

            # Weighted combination
            base_score = (ratio * 0.4) + (token_ratio * 0.4) + (partial_ratio * 0.2)

        # Popularity bonus (normalized ref_count)
        popularity_bonus = min(ref_count / max_ref_count, 0.15) if ref_count > 0 else 0.0

        # Type match bonus
        type_bonus = 0.1 if type_match else 0.0

        # Combine scores
        confidence = min(1.0, base_score + popularity_bonus + type_bonus)

        return confidence

    def search(
        self,
        query: str,
        entity_type: Optional[EntityType] = None,
        bypass_cache: bool = False
    ) -> DBpediaSearchResult:
        """
        Search DBpedia for entities matching the query.

        Args:
            query: Entity name to search for
            entity_type: Optional type to filter results
            bypass_cache: Skip cache lookup

        Returns:
            DBpediaSearchResult with candidates
        """
        if not query or not query.strip():
            return DBpediaSearchResult(
                query=query,
                entity_type=entity_type,
                error="Empty query"
            )

        cache_key = self._get_cache_key(query, entity_type)

        # Check cache first
        if not bypass_cache and cache_key in self._cache:
            logger.debug(f"Cache hit for query: {query}")
            cached_result = self._cache[cache_key]
            cached_result.from_cache = True
            return cached_result

        logger.info(f"Searching DBpedia for: '{query}' (type={entity_type})")

        # Execute search
        response = self._execute_search(query, entity_type)

        candidates: List[DBpediaCandidate] = []

        if response:
            candidates = self._parse_response(response)

            # Calculate confidence scores
            # Find max ref_count for normalization
            max_ref = max((c.ref_count for c in candidates), default=1)

            for candidate in candidates:
                candidate.confidence = self._calculate_confidence(
                    query=query,
                    label=candidate.label,
                    ref_count=candidate.ref_count,
                    type_match=entity_type is not None,
                    max_ref_count=max(max_ref, 1)
                )

            # Sort by confidence and limit results
            candidates.sort(key=lambda c: c.confidence, reverse=True)
            candidates = candidates[:self.MAX_CANDIDATES]

        result = DBpediaSearchResult(
            query=query,
            entity_type=entity_type,
            candidates=candidates,
            from_cache=False
        )

        # Cache the result
        self._cache[cache_key] = result

        logger.info(f"Found {len(candidates)} DBpedia candidates for '{query}'")
        return result

    def get_resource_info(self, uri: str) -> Optional[Dict[str, Any]]:
        """
        Get detailed information about a specific DBpedia resource.

        Args:
            uri: DBpedia resource URI

        Returns:
            Resource details or None if not found
        """
        if not uri or "dbpedia.org/resource" not in uri:
            return None

        cache_key = f"dbpedia:resource:{hashlib.md5(uri.encode()).hexdigest()}"
        if cache_key in self._cache:
            return self._cache[cache_key]

        # Use DBpedia SPARQL endpoint for detailed info
        # This is a simplified implementation
        self._rate_limit()

        try:
            # Try to get JSON-LD representation
            resource_name = uri.split("/")[-1]
            json_url = f"https://dbpedia.org/data/{resource_name}.json"

            response = self._client.get(json_url)
            response.raise_for_status()
            data = response.json()

            # Parse the JSON-LD
            result = {
                "uri": uri,
                "data": data.get(uri, {}),
            }

            self._cache[cache_key] = result
            return result

        except Exception as e:
            logger.error(f"Failed to get resource info for {uri}: {e}")
            return None

    def close(self) -> None:
        """Close the HTTP client"""
        self._client.close()

    def __enter__(self):
        return self

    def __exit__(self, exc_type, exc_val, exc_tb):
        self.close()


# Singleton instance
_client_instance: Optional[DBpediaClient] = None


def get_dbpedia_client() -> DBpediaClient:
    """
    Get or create singleton DBpediaClient instance.

    Returns:
        DBpediaClient instance
    """
    global _client_instance
    if _client_instance is None:
        _client_instance = DBpediaClient()
    return _client_instance
