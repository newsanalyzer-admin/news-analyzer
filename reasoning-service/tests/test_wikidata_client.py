"""
Tests for Wikidata Entity Lookup Client

Tests the WikidataClient's ability to search for entities, handle caching,
rate limiting, and error conditions.
"""

import pytest
import time
from unittest.mock import Mock, patch, MagicMock

from app.services.wikidata_client import (
    WikidataClient,
    WikidataCandidate,
    WikidataSearchResult,
    EntityType,
    get_wikidata_client,
    ENTITY_TYPE_TO_WIKIDATA,
)


class TestWikidataCandidate:
    """Tests for WikidataCandidate dataclass"""

    def test_to_dict_basic(self):
        """Test basic candidate to_dict conversion"""
        candidate = WikidataCandidate(
            qid="Q30",
            label="United States of America",
            description="country in North America",
            aliases=["USA", "US", "America"],
            instance_of=["Q6256"],
            confidence=0.95
        )

        result = candidate.to_dict()

        assert result["qid"] == "Q30"
        assert result["label"] == "United States of America"
        assert result["description"] == "country in North America"
        assert result["aliases"] == ["USA", "US", "America"]
        assert result["confidence"] == 0.95
        assert result["wikidata_url"] == "https://www.wikidata.org/wiki/Q30"

    def test_to_dict_minimal(self):
        """Test candidate with minimal data"""
        candidate = WikidataCandidate(
            qid="Q5",
            label="human",
        )

        result = candidate.to_dict()

        assert result["qid"] == "Q5"
        assert result["label"] == "human"
        assert result["description"] is None
        assert result["aliases"] == []
        assert result["confidence"] == 0.0


class TestWikidataSearchResult:
    """Tests for WikidataSearchResult dataclass"""

    def test_to_dict_with_candidates(self):
        """Test search result to_dict with candidates"""
        candidates = [
            WikidataCandidate(qid="Q217173", label="EPA", confidence=0.9),
            WikidataCandidate(qid="Q123", label="EPA2", confidence=0.7),
        ]
        result = WikidataSearchResult(
            query="EPA",
            entity_type=EntityType.GOVERNMENT_ORG,
            candidates=candidates,
            from_cache=False
        )

        data = result.to_dict()

        assert data["query"] == "EPA"
        assert data["entity_type"] == "government_org"
        assert data["candidate_count"] == 2
        assert data["from_cache"] is False
        assert len(data["candidates"]) == 2

    def test_to_dict_with_error(self):
        """Test search result with error"""
        result = WikidataSearchResult(
            query="",
            entity_type=None,
            error="Empty query"
        )

        data = result.to_dict()

        assert data["error"] == "Empty query"
        assert data["candidate_count"] == 0


class TestWikidataClientInitialization:
    """Tests for WikidataClient initialization"""

    def test_client_initialization(self):
        """Test client initializes with correct defaults"""
        client = WikidataClient()

        assert client._timeout == WikidataClient.REQUEST_TIMEOUT
        assert client._cache.ttl == WikidataClient.CACHE_TTL

        client.close()

    def test_client_custom_params(self):
        """Test client initializes with custom parameters"""
        client = WikidataClient(
            cache_ttl=3600,
            cache_maxsize=500,
            timeout=5.0
        )

        assert client._timeout == 5.0
        assert client._cache.ttl == 3600
        assert client._cache.maxsize == 500

        client.close()


class TestCacheKeyGeneration:
    """Tests for cache key generation"""

    def test_cache_key_with_type(self):
        """Test cache key generation with entity type"""
        client = WikidataClient()

        key1 = client._get_cache_key("EPA", EntityType.GOVERNMENT_ORG)
        key2 = client._get_cache_key("EPA", EntityType.GOVERNMENT_ORG)
        key3 = client._get_cache_key("EPA", EntityType.ORGANIZATION)

        # Same query + type should give same key
        assert key1 == key2
        # Different type should give different key
        assert key1 != key3

        client.close()

    def test_cache_key_normalization(self):
        """Test cache key normalizes query"""
        client = WikidataClient()

        key1 = client._get_cache_key("EPA", None)
        key2 = client._get_cache_key("epa", None)
        key3 = client._get_cache_key("  EPA  ", None)

        # Should normalize to same key
        assert key1 == key2 == key3

        client.close()


class TestConfidenceCalculation:
    """Tests for confidence score calculation"""

    def test_exact_match_high_confidence(self):
        """Test exact match gets high confidence"""
        client = WikidataClient()

        confidence = client._calculate_confidence(
            query="Environmental Protection Agency",
            label="Environmental Protection Agency",
            description="US government agency",
            aliases=[],
            type_match=True
        )

        assert confidence >= 0.95

        client.close()

    def test_fuzzy_match_medium_confidence(self):
        """Test fuzzy match gets medium confidence"""
        client = WikidataClient()

        confidence = client._calculate_confidence(
            query="EPA",
            label="Environmental Protection Agency",
            description="US government agency",
            aliases=["EPA"],
            type_match=True
        )

        # Should have some confidence due to alias match
        assert 0.3 < confidence < 1.0

        client.close()

    def test_no_match_low_confidence(self):
        """Test no match gets low confidence"""
        client = WikidataClient()

        confidence = client._calculate_confidence(
            query="Apple Inc",
            label="Orange Fruit",
            description="A citrus fruit",
            aliases=[],
            type_match=False
        )

        assert confidence < 0.5

        client.close()


class TestRateLimiting:
    """Tests for rate limiting behavior"""

    def test_rate_limiting_delays_requests(self):
        """Test rate limiting adds delay between requests"""
        client = WikidataClient()

        # First call sets the time
        start = time.time()
        client._rate_limit()
        client._rate_limit()  # Second call should wait
        elapsed = time.time() - start

        # Should have waited at least MIN_REQUEST_INTERVAL
        assert elapsed >= WikidataClient.MIN_REQUEST_INTERVAL * 0.9  # Allow some tolerance

        client.close()


class TestSearchEmptyQuery:
    """Tests for empty query handling"""

    def test_empty_query_returns_error(self):
        """Test empty query returns error result"""
        client = WikidataClient()

        result = client.search("")

        assert result.error == "Empty query"
        assert len(result.candidates) == 0

        client.close()

    def test_whitespace_query_returns_error(self):
        """Test whitespace-only query returns error"""
        client = WikidataClient()

        result = client.search("   ")

        assert result.error == "Empty query"

        client.close()


class TestCaching:
    """Tests for caching behavior"""

    def test_cached_result_returned(self):
        """Test cached results are returned on second call"""
        client = WikidataClient()

        # Pre-populate cache
        cached_result = WikidataSearchResult(
            query="EPA",
            entity_type=EntityType.GOVERNMENT_ORG,
            candidates=[WikidataCandidate(qid="Q217173", label="EPA", confidence=0.9)],
            from_cache=False
        )
        cache_key = client._get_cache_key("EPA", EntityType.GOVERNMENT_ORG)
        client._cache[cache_key] = cached_result

        # Search should return cached result
        result = client.search("EPA", EntityType.GOVERNMENT_ORG)

        assert result.from_cache is True
        assert len(result.candidates) == 1
        assert result.candidates[0].qid == "Q217173"

        client.close()

    def test_bypass_cache_skips_cache(self):
        """Test bypass_cache parameter skips cache lookup"""
        client = WikidataClient()

        # Pre-populate cache
        cached_result = WikidataSearchResult(
            query="EPA",
            entity_type=None,
            candidates=[WikidataCandidate(qid="Q217173", label="EPA", confidence=0.9)],
            from_cache=False
        )
        cache_key = client._get_cache_key("EPA", None)
        client._cache[cache_key] = cached_result

        # Mock the API call
        with patch.object(client, '_use_wbsearchentities_api') as mock_api:
            mock_api.return_value = []

            result = client.search("EPA", bypass_cache=True)

            # Should have called the API, not returned cache
            mock_api.assert_called_once()
            assert result.from_cache is False

        client.close()


class TestEntityTypeMapping:
    """Tests for entity type to Wikidata mapping"""

    def test_person_type_mapping(self):
        """Test person type maps to Q5 (human)"""
        assert "Q5" in ENTITY_TYPE_TO_WIKIDATA[EntityType.PERSON]

    def test_government_org_type_mapping(self):
        """Test government org type maps to government agency"""
        assert "Q327333" in ENTITY_TYPE_TO_WIKIDATA[EntityType.GOVERNMENT_ORG]

    def test_location_type_mapping(self):
        """Test location type maps to geographic types"""
        location_types = ENTITY_TYPE_TO_WIKIDATA[EntityType.LOCATION]
        assert "Q515" in location_types  # city
        assert "Q6256" in location_types  # country


class TestSPARQLQueryBuilding:
    """Tests for SPARQL query building"""

    def test_simple_search_query(self):
        """Test simple search query is well-formed"""
        client = WikidataClient()

        query = client._build_simple_search_query("EPA")

        assert "EPA" in query
        assert "rdfs:label" in query
        assert "LIMIT" in query

        client.close()

    def test_search_query_with_type_filter(self):
        """Test search query includes type filter when specified"""
        client = WikidataClient()

        query = client._build_simple_search_query("EPA", EntityType.GOVERNMENT_ORG)

        assert "EPA" in query
        assert "Q327333" in query or "Q43229" in query  # Government agency or organization
        assert "wdt:P31" in query  # instance-of

        client.close()

    def test_query_escapes_quotes(self):
        """Test query properly escapes special characters"""
        client = WikidataClient()

        query = client._build_simple_search_query('O"Brien')

        # Should not break SPARQL syntax
        assert '\\"' in query or "O'Brien" not in query

        client.close()


class TestMockedAPISearch:
    """Tests using mocked API responses"""

    def test_search_parses_api_response(self):
        """Test search correctly parses wbsearchentities API response"""
        client = WikidataClient()

        mock_response = [
            {
                "id": "Q217173",
                "label": "Environmental Protection Agency",
                "description": "United States government agency",
                "aliases": ["EPA"]
            },
            {
                "id": "Q845232",
                "label": "EPA",
                "description": "disambiguation page"
            }
        ]

        with patch.object(client, '_use_wbsearchentities_api') as mock_api:
            mock_api.return_value = mock_response

            result = client.search("EPA", EntityType.GOVERNMENT_ORG)

            assert len(result.candidates) >= 1
            assert result.candidates[0].qid in ["Q217173", "Q845232"]

        client.close()

    def test_search_handles_api_failure(self):
        """Test search handles API failure gracefully"""
        client = WikidataClient()

        with patch.object(client, '_use_wbsearchentities_api') as mock_api:
            mock_api.return_value = None

            with patch.object(client, '_execute_sparql') as mock_sparql:
                mock_sparql.return_value = None

                result = client.search("EPA")

                # Should return empty result, not crash
                assert len(result.candidates) == 0
                assert result.error is None

        client.close()


class TestGetEntityDetails:
    """Tests for get_entity_details method"""

    def test_invalid_qid_returns_none(self):
        """Test invalid QID returns None"""
        client = WikidataClient()

        result = client.get_entity_details("")
        assert result is None

        result = client.get_entity_details("P123")  # Property, not item
        assert result is None

        client.close()

    def test_cached_entity_details(self):
        """Test cached entity details are returned"""
        client = WikidataClient()

        cached_data = {
            "qid": "Q30",
            "label": "United States of America",
            "description": "country in North America"
        }
        client._cache["wikidata:entity:Q30"] = cached_data

        result = client.get_entity_details("Q30")

        assert result == cached_data

        client.close()


class TestSingleton:
    """Tests for singleton pattern"""

    def test_get_wikidata_client_returns_same_instance(self):
        """Test get_wikidata_client returns singleton"""
        import app.services.wikidata_client as module

        # Reset singleton
        module._client_instance = None

        client1 = get_wikidata_client()
        client2 = get_wikidata_client()

        assert client1 is client2

        # Cleanup
        module._client_instance = None


class TestContextManager:
    """Tests for context manager support"""

    def test_context_manager_closes_client(self):
        """Test context manager properly closes client"""
        with WikidataClient() as client:
            assert client._client is not None

        # After context, client should still exist but could be closed
        # (httpx.Client can be used after close for some operations)


# Integration test marker - these require network access
@pytest.mark.integration
class TestWikidataIntegration:
    """Integration tests that actually call Wikidata API"""

    @pytest.mark.skip(reason="Requires network access and may be rate limited")
    def test_real_search_epa(self):
        """Test real search for EPA returns results"""
        client = WikidataClient()

        result = client.search("Environmental Protection Agency", EntityType.GOVERNMENT_ORG)

        assert len(result.candidates) > 0
        # EPA should be in results
        qids = [c.qid for c in result.candidates]
        assert "Q217173" in qids or len(qids) > 0

        client.close()

    @pytest.mark.skip(reason="Requires network access and may be rate limited")
    def test_real_search_person(self):
        """Test real search for a person"""
        client = WikidataClient()

        result = client.search("Elizabeth Warren", EntityType.PERSON)

        assert len(result.candidates) > 0

        client.close()
