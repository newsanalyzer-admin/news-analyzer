"""
Wikidata Entity Lookup Client

Queries Wikidata SPARQL endpoint to find matching entities by name and type.
Returns candidate entities with Wikidata QIDs and confidence scores.
"""

import logging
import time
import hashlib
from dataclasses import dataclass, field
from enum import Enum
from typing import Dict, List, Optional, Any
from urllib.parse import quote

import httpx
from cachetools import TTLCache
from rapidfuzz import fuzz

logger = logging.getLogger(__name__)


class EntityType(str, Enum):
    """Entity types supported for Wikidata lookup"""
    PERSON = "person"
    ORGANIZATION = "organization"
    GOVERNMENT_ORG = "government_org"
    LOCATION = "location"
    EVENT = "event"


# Mapping from NewsAnalyzer entity types to Wikidata instance-of (P31) QIDs
ENTITY_TYPE_TO_WIKIDATA: Dict[EntityType, List[str]] = {
    EntityType.PERSON: ["Q5"],  # human
    EntityType.ORGANIZATION: ["Q43229", "Q4830453"],  # organization, business
    EntityType.GOVERNMENT_ORG: ["Q327333", "Q43229", "Q7210356"],  # government agency, organization, political organization
    EntityType.LOCATION: ["Q515", "Q6256", "Q82794", "Q35657"],  # city, country, geographic region, state
    EntityType.EVENT: ["Q1190554", "Q1656682"],  # event, occurrence
}


@dataclass
class WikidataCandidate:
    """A candidate entity match from Wikidata"""
    qid: str
    label: str
    description: Optional[str] = None
    aliases: List[str] = field(default_factory=list)
    instance_of: List[str] = field(default_factory=list)
    confidence: float = 0.0

    def to_dict(self) -> Dict[str, Any]:
        """Convert to dictionary for JSON serialization"""
        return {
            "qid": self.qid,
            "label": self.label,
            "description": self.description,
            "aliases": self.aliases,
            "instance_of": self.instance_of,
            "confidence": round(self.confidence, 3),
            "wikidata_url": f"https://www.wikidata.org/wiki/{self.qid}",
        }


@dataclass
class WikidataSearchResult:
    """Result of a Wikidata entity search"""
    query: str
    entity_type: Optional[EntityType]
    candidates: List[WikidataCandidate] = field(default_factory=list)
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


class WikidataClient:
    """Client for querying Wikidata SPARQL endpoint"""

    SPARQL_ENDPOINT = "https://query.wikidata.org/sparql"
    WIKIDATA_API = "https://www.wikidata.org/w/api.php"

    # Rate limiting: 1 request per second for public endpoint
    MIN_REQUEST_INTERVAL = 1.0

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
        Initialize the Wikidata client.

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
                "User-Agent": "NewsAnalyzer/2.0 (https://github.com/newsanalyzer; contact@newsanalyzer.org)",
                "Accept": "application/sparql-results+json",
            }
        )
        logger.info(f"WikidataClient initialized with cache_ttl={cache_ttl}s, maxsize={cache_maxsize}")

    def _get_cache_key(self, query: str, entity_type: Optional[EntityType]) -> str:
        """Generate cache key from query and entity type"""
        normalized_query = query.lower().strip()
        type_str = entity_type.value if entity_type else "any"
        key_str = f"wikidata:{type_str}:{normalized_query}"
        return hashlib.md5(key_str.encode()).hexdigest()

    def _rate_limit(self) -> None:
        """Enforce rate limiting between requests"""
        elapsed = time.time() - self._last_request_time
        if elapsed < self.MIN_REQUEST_INTERVAL:
            sleep_time = self.MIN_REQUEST_INTERVAL - elapsed
            logger.debug(f"Rate limiting: sleeping {sleep_time:.2f}s")
            time.sleep(sleep_time)
        self._last_request_time = time.time()

    def _build_sparql_query(
        self,
        search_term: str,
        entity_type: Optional[EntityType] = None,
        limit: int = 10
    ) -> str:
        """
        Build SPARQL query for entity search.

        Args:
            search_term: Entity name to search for
            entity_type: Optional type filter
            limit: Maximum results

        Returns:
            SPARQL query string
        """
        # Escape search term for SPARQL
        escaped_term = search_term.replace('"', '\\"').replace("'", "\\'")

        # Build type filter if specified
        type_filter = ""
        if entity_type and entity_type in ENTITY_TYPE_TO_WIKIDATA:
            type_qids = ENTITY_TYPE_TO_WIKIDATA[entity_type]
            type_values = " ".join(f"wd:{qid}" for qid in type_qids)
            type_filter = f"""
            VALUES ?typeFilter {{ {type_values} }}
            ?item wdt:P31/wdt:P279* ?typeFilter .
            """

        query = f"""
        SELECT DISTINCT ?item ?itemLabel ?itemDescription ?itemAltLabel WHERE {{
            SERVICE wikibase:mwapi {{
                bd:serviceParam wikibase:endpoint "www.wikidata.org";
                                wikibase:api "EntitySearch";
                                mwapi:search "{escaped_term}";
                                mwapi:language "en".
                ?item wikibase:apiOutputItem mwapi:item.
            }}
            {type_filter}
            SERVICE wikibase:label {{
                bd:serviceParam wikibase:language "en,es,fr,de".
            }}
        }}
        LIMIT {limit}
        """
        return query

    def _build_simple_search_query(
        self,
        search_term: str,
        entity_type: Optional[EntityType] = None,
        limit: int = 10
    ) -> str:
        """
        Build a simpler SPARQL query using label matching.
        Fallback when EntitySearch API is slow or unavailable.
        """
        escaped_term = search_term.replace('"', '\\"')

        type_filter = ""
        if entity_type and entity_type in ENTITY_TYPE_TO_WIKIDATA:
            type_qids = ENTITY_TYPE_TO_WIKIDATA[entity_type]
            type_values = " ".join(f"wd:{qid}" for qid in type_qids)
            type_filter = f"""
            VALUES ?typeFilter {{ {type_values} }}
            ?item wdt:P31/wdt:P279* ?typeFilter .
            """

        query = f"""
        SELECT DISTINCT ?item ?itemLabel ?itemDescription WHERE {{
            ?item rdfs:label "{escaped_term}"@en .
            {type_filter}
            SERVICE wikibase:label {{
                bd:serviceParam wikibase:language "en".
            }}
        }}
        LIMIT {limit}
        """
        return query

    def _execute_sparql(self, query: str) -> Optional[Dict[str, Any]]:
        """
        Execute SPARQL query against Wikidata endpoint.

        Args:
            query: SPARQL query string

        Returns:
            JSON response or None on error
        """
        self._rate_limit()

        try:
            response = self._client.get(
                self.SPARQL_ENDPOINT,
                params={"query": query, "format": "json"}
            )

            if response.status_code == 429:
                logger.warning("Wikidata rate limit hit (429)")
                # Wait and retry once
                time.sleep(5)
                self._rate_limit()
                response = self._client.get(
                    self.SPARQL_ENDPOINT,
                    params={"query": query, "format": "json"}
                )

            response.raise_for_status()
            return response.json()

        except httpx.TimeoutException:
            logger.error(f"Wikidata query timed out after {self._timeout}s")
            return None
        except httpx.HTTPStatusError as e:
            logger.error(f"Wikidata HTTP error: {e.response.status_code}")
            return None
        except Exception as e:
            logger.error(f"Wikidata query failed: {e}")
            return None

    def _use_wbsearchentities_api(
        self,
        search_term: str,
        entity_type: Optional[EntityType] = None,
        limit: int = 10
    ) -> Optional[List[Dict[str, Any]]]:
        """
        Use Wikidata wbsearchentities API as a faster alternative to SPARQL.

        Args:
            search_term: Entity name to search for
            entity_type: Optional type filter (applied post-search)
            limit: Maximum results

        Returns:
            List of search results or None on error
        """
        self._rate_limit()

        try:
            response = self._client.get(
                self.WIKIDATA_API,
                params={
                    "action": "wbsearchentities",
                    "search": search_term,
                    "language": "en",
                    "format": "json",
                    "limit": limit * 2,  # Get extra to allow for type filtering
                    "type": "item",
                }
            )
            response.raise_for_status()
            data = response.json()
            return data.get("search", [])

        except Exception as e:
            logger.error(f"Wikidata API search failed: {e}")
            return None

    def _calculate_confidence(
        self,
        query: str,
        label: str,
        description: Optional[str],
        aliases: List[str],
        type_match: bool
    ) -> float:
        """
        Calculate confidence score for a candidate match.

        Args:
            query: Original search query
            label: Candidate label
            description: Candidate description
            aliases: Candidate aliases
            type_match: Whether entity type matches filter

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

        # Check aliases for potential matches
        alias_bonus = 0.0
        for alias in aliases:
            alias_ratio = fuzz.ratio(query_lower, alias.lower()) / 100.0
            if alias_ratio > 0.9:
                alias_bonus = max(alias_bonus, 0.1)

        # Type match bonus
        type_bonus = 0.1 if type_match else 0.0

        # Combine scores
        confidence = min(1.0, base_score + alias_bonus + type_bonus)

        return confidence

    def search(
        self,
        query: str,
        entity_type: Optional[EntityType] = None,
        bypass_cache: bool = False
    ) -> WikidataSearchResult:
        """
        Search Wikidata for entities matching the query.

        Args:
            query: Entity name to search for
            entity_type: Optional type to filter results
            bypass_cache: Skip cache lookup

        Returns:
            WikidataSearchResult with candidates
        """
        if not query or not query.strip():
            return WikidataSearchResult(
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

        logger.info(f"Searching Wikidata for: '{query}' (type={entity_type})")

        # Try the faster wbsearchentities API first
        api_results = self._use_wbsearchentities_api(query, entity_type)

        candidates: List[WikidataCandidate] = []

        if api_results:
            for item in api_results:
                qid = item.get("id", "")
                label = item.get("label", "")
                description = item.get("description")
                aliases = item.get("aliases", [])

                if not qid or not label:
                    continue

                # For now, we can't easily filter by type with wbsearchentities
                # Type filtering would require additional queries
                type_match = entity_type is None  # Assume match if no type filter

                confidence = self._calculate_confidence(
                    query=query,
                    label=label,
                    description=description,
                    aliases=aliases if isinstance(aliases, list) else [],
                    type_match=type_match
                )

                candidates.append(WikidataCandidate(
                    qid=qid,
                    label=label,
                    description=description,
                    aliases=aliases if isinstance(aliases, list) else [],
                    confidence=confidence
                ))
        else:
            # Fallback to SPARQL if API fails
            sparql_query = self._build_simple_search_query(query, entity_type)
            sparql_result = self._execute_sparql(sparql_query)

            if sparql_result and "results" in sparql_result:
                for binding in sparql_result["results"]["bindings"]:
                    item_uri = binding.get("item", {}).get("value", "")
                    qid = item_uri.split("/")[-1] if item_uri else ""
                    label = binding.get("itemLabel", {}).get("value", "")
                    description = binding.get("itemDescription", {}).get("value")

                    if not qid or not label or not qid.startswith("Q"):
                        continue

                    confidence = self._calculate_confidence(
                        query=query,
                        label=label,
                        description=description,
                        aliases=[],
                        type_match=entity_type is not None
                    )

                    candidates.append(WikidataCandidate(
                        qid=qid,
                        label=label,
                        description=description,
                        confidence=confidence
                    ))

        # Sort by confidence and limit results
        candidates.sort(key=lambda c: c.confidence, reverse=True)
        candidates = candidates[:self.MAX_CANDIDATES]

        result = WikidataSearchResult(
            query=query,
            entity_type=entity_type,
            candidates=candidates,
            from_cache=False
        )

        # Cache the result
        self._cache[cache_key] = result

        logger.info(f"Found {len(candidates)} candidates for '{query}'")
        return result

    def get_entity_details(self, qid: str) -> Optional[Dict[str, Any]]:
        """
        Get detailed information about a specific Wikidata entity.

        Args:
            qid: Wikidata QID (e.g., "Q30" for United States)

        Returns:
            Entity details or None if not found
        """
        if not qid or not qid.startswith("Q"):
            return None

        cache_key = f"wikidata:entity:{qid}"
        if cache_key in self._cache:
            return self._cache[cache_key]

        self._rate_limit()

        try:
            response = self._client.get(
                self.WIKIDATA_API,
                params={
                    "action": "wbgetentities",
                    "ids": qid,
                    "languages": "en",
                    "format": "json",
                }
            )
            response.raise_for_status()
            data = response.json()

            entities = data.get("entities", {})
            if qid not in entities:
                return None

            entity_data = entities[qid]

            # Extract useful properties
            labels = entity_data.get("labels", {})
            descriptions = entity_data.get("descriptions", {})
            aliases = entity_data.get("aliases", {})
            claims = entity_data.get("claims", {})

            result = {
                "qid": qid,
                "label": labels.get("en", {}).get("value"),
                "description": descriptions.get("en", {}).get("value"),
                "aliases": [a["value"] for a in aliases.get("en", [])],
                "claims": claims,
                "wikidata_url": f"https://www.wikidata.org/wiki/{qid}",
            }

            self._cache[cache_key] = result
            return result

        except Exception as e:
            logger.error(f"Failed to get entity details for {qid}: {e}")
            return None

    def close(self) -> None:
        """Close the HTTP client"""
        self._client.close()

    def __enter__(self):
        return self

    def __exit__(self, exc_type, exc_val, exc_tb):
        self.close()


# Singleton instance
_client_instance: Optional[WikidataClient] = None


def get_wikidata_client() -> WikidataClient:
    """
    Get or create singleton WikidataClient instance.

    Returns:
        WikidataClient instance
    """
    global _client_instance
    if _client_instance is None:
        _client_instance = WikidataClient()
    return _client_instance
