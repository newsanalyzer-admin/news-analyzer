"""
Tests for DBpedia Entity Lookup Client

Tests the DBpediaClient's ability to search for entities, handle caching,
and error conditions.
"""

import pytest
from unittest.mock import patch, MagicMock

from app.services.dbpedia_client import (
    DBpediaClient,
    DBpediaCandidate,
    DBpediaSearchResult,
    EntityType,
    get_dbpedia_client,
    ENTITY_TYPE_TO_DBPEDIA,
)


class TestDBpediaCandidate:
    """Tests for DBpediaCandidate dataclass"""

    def test_to_dict_basic(self):
        """Test basic candidate to_dict conversion"""
        candidate = DBpediaCandidate(
            uri="http://dbpedia.org/resource/United_States_Environmental_Protection_Agency",
            label="United States Environmental Protection Agency",
            description="Government agency of the United States",
            types=["GovernmentAgency", "Organisation"],
            ref_count=1250,
            confidence=0.95
        )

        result = candidate.to_dict()

        assert result["uri"] == "http://dbpedia.org/resource/United_States_Environmental_Protection_Agency"
        assert result["label"] == "United States Environmental Protection Agency"
        assert result["description"] == "Government agency of the United States"
        assert result["types"] == ["GovernmentAgency", "Organisation"]
        assert result["ref_count"] == 1250
        assert result["confidence"] == 0.95
        assert result["resource_name"] == "United States Environmental Protection Agency"

    def test_resource_name_extraction(self):
        """Test resource name is correctly extracted from URI"""
        candidate = DBpediaCandidate(
            uri="http://dbpedia.org/resource/New_York_City",
            label="New York City"
        )

        assert candidate.resource_name == "New York City"

    def test_to_dict_minimal(self):
        """Test candidate with minimal data"""
        candidate = DBpediaCandidate(
            uri="http://dbpedia.org/resource/Test",
            label="Test",
        )

        result = candidate.to_dict()

        assert result["uri"] == "http://dbpedia.org/resource/Test"
        assert result["label"] == "Test"
        assert result["description"] is None
        assert result["types"] == []
        assert result["ref_count"] == 0
        assert result["confidence"] == 0.0


class TestDBpediaSearchResult:
    """Tests for DBpediaSearchResult dataclass"""

    def test_to_dict_with_candidates(self):
        """Test search result to_dict with candidates"""
        candidates = [
            DBpediaCandidate(
                uri="http://dbpedia.org/resource/EPA",
                label="EPA",
                confidence=0.9
            ),
        ]
        result = DBpediaSearchResult(
            query="EPA",
            entity_type=EntityType.GOVERNMENT_ORG,
            candidates=candidates,
            from_cache=False
        )

        data = result.to_dict()

        assert data["query"] == "EPA"
        assert data["entity_type"] == "government_org"
        assert data["candidate_count"] == 1
        assert data["from_cache"] is False
        assert len(data["candidates"]) == 1

    def test_to_dict_with_error(self):
        """Test search result with error"""
        result = DBpediaSearchResult(
            query="",
            entity_type=None,
            error="Empty query"
        )

        data = result.to_dict()

        assert data["error"] == "Empty query"
        assert data["candidate_count"] == 0


class TestDBpediaClientInitialization:
    """Tests for DBpediaClient initialization"""

    def test_client_initialization(self):
        """Test client initializes with correct defaults"""
        client = DBpediaClient()

        assert client._timeout == DBpediaClient.REQUEST_TIMEOUT
        assert client._cache.ttl == DBpediaClient.CACHE_TTL

        client.close()

    def test_client_custom_params(self):
        """Test client initializes with custom parameters"""
        client = DBpediaClient(
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
        client = DBpediaClient()

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
        client = DBpediaClient()

        key1 = client._get_cache_key("EPA", None)
        key2 = client._get_cache_key("epa", None)
        key3 = client._get_cache_key("  EPA  ", None)

        # Should normalize to same key
        assert key1 == key2 == key3

        client.close()


class TestTypeMapping:
    """Tests for entity type to DBpedia class mapping"""

    def test_person_type_mapping(self):
        """Test person type maps to Person"""
        assert "Person" in ENTITY_TYPE_TO_DBPEDIA[EntityType.PERSON]

    def test_government_org_type_mapping(self):
        """Test government org type maps to GovernmentAgency"""
        assert "GovernmentAgency" in ENTITY_TYPE_TO_DBPEDIA[EntityType.GOVERNMENT_ORG]

    def test_location_type_mapping(self):
        """Test location type maps to Place"""
        location_types = ENTITY_TYPE_TO_DBPEDIA[EntityType.LOCATION]
        assert "Place" in location_types
        assert "City" in location_types

    def test_get_dbpedia_type_filter(self):
        """Test type filter extraction"""
        client = DBpediaClient()

        assert client._get_dbpedia_type_filter(EntityType.PERSON) == "Person"
        assert client._get_dbpedia_type_filter(EntityType.GOVERNMENT_ORG) == "GovernmentAgency"

        client.close()


class TestConfidenceCalculation:
    """Tests for confidence score calculation"""

    def test_exact_match_high_confidence(self):
        """Test exact match gets high confidence"""
        client = DBpediaClient()

        confidence = client._calculate_confidence(
            query="Environmental Protection Agency",
            label="Environmental Protection Agency",
            ref_count=1000,
            type_match=True
        )

        assert confidence >= 0.95

        client.close()

    def test_fuzzy_match_medium_confidence(self):
        """Test fuzzy match gets medium confidence"""
        client = DBpediaClient()

        confidence = client._calculate_confidence(
            query="EPA",
            label="Environmental Protection Agency",
            ref_count=100,
            type_match=True
        )

        # Should have some confidence
        assert 0.2 < confidence < 1.0

        client.close()

    def test_high_ref_count_boosts_confidence(self):
        """Test high ref_count increases confidence"""
        client = DBpediaClient()

        low_ref_confidence = client._calculate_confidence(
            query="Test",
            label="Test Entity",
            ref_count=10,
            type_match=True
        )

        high_ref_confidence = client._calculate_confidence(
            query="Test",
            label="Test Entity",
            ref_count=10000,
            type_match=True
        )

        assert high_ref_confidence > low_ref_confidence

        client.close()


class TestSearchEmptyQuery:
    """Tests for empty query handling"""

    def test_empty_query_returns_error(self):
        """Test empty query returns error result"""
        client = DBpediaClient()

        result = client.search("")

        assert result.error == "Empty query"
        assert len(result.candidates) == 0

        client.close()

    def test_whitespace_query_returns_error(self):
        """Test whitespace-only query returns error"""
        client = DBpediaClient()

        result = client.search("   ")

        assert result.error == "Empty query"

        client.close()


class TestCaching:
    """Tests for caching behavior"""

    def test_cached_result_returned(self):
        """Test cached results are returned on second call"""
        client = DBpediaClient()

        # Pre-populate cache
        cached_result = DBpediaSearchResult(
            query="EPA",
            entity_type=EntityType.GOVERNMENT_ORG,
            candidates=[DBpediaCandidate(
                uri="http://dbpedia.org/resource/EPA",
                label="EPA",
                confidence=0.9
            )],
            from_cache=False
        )
        cache_key = client._get_cache_key("EPA", EntityType.GOVERNMENT_ORG)
        client._cache[cache_key] = cached_result

        # Search should return cached result
        result = client.search("EPA", EntityType.GOVERNMENT_ORG)

        assert result.from_cache is True
        assert len(result.candidates) == 1

        client.close()

    def test_bypass_cache_skips_cache(self):
        """Test bypass_cache parameter skips cache lookup"""
        client = DBpediaClient()

        # Pre-populate cache
        cached_result = DBpediaSearchResult(
            query="EPA",
            entity_type=None,
            candidates=[DBpediaCandidate(
                uri="http://dbpedia.org/resource/EPA",
                label="EPA",
                confidence=0.9
            )],
            from_cache=False
        )
        cache_key = client._get_cache_key("EPA", None)
        client._cache[cache_key] = cached_result

        # Mock the API call
        with patch.object(client, '_execute_search') as mock_search:
            mock_search.return_value = {"docs": []}

            result = client.search("EPA", bypass_cache=True)

            # Should have called the API, not returned cache
            mock_search.assert_called_once()
            assert result.from_cache is False

        client.close()


class TestResponseParsing:
    """Tests for DBpedia response parsing"""

    def test_parse_valid_response(self):
        """Test parsing a valid DBpedia response"""
        client = DBpediaClient()

        response = {
            "docs": [
                {
                    "resource": ["http://dbpedia.org/resource/United_States_Environmental_Protection_Agency"],
                    "label": ["United States Environmental Protection Agency"],
                    "type": ["http://dbpedia.org/ontology/GovernmentAgency"],
                    "comment": ["The EPA is an independent executive agency..."],
                    "refCount": [1250]
                },
                {
                    "resource": ["http://dbpedia.org/resource/EPA_New_England"],
                    "label": ["EPA New England"],
                    "type": ["http://dbpedia.org/ontology/Organisation"],
                    "refCount": [50]
                }
            ]
        }

        candidates = client._parse_response(response)

        assert len(candidates) == 2
        assert candidates[0].label == "United States Environmental Protection Agency"
        assert candidates[0].ref_count == 1250
        assert "GovernmentAgency" in candidates[0].types
        assert candidates[1].label == "EPA New England"

        client.close()

    def test_parse_empty_response(self):
        """Test parsing empty response"""
        client = DBpediaClient()

        response = {"docs": []}

        candidates = client._parse_response(response)

        assert len(candidates) == 0

        client.close()

    def test_parse_malformed_response(self):
        """Test parsing malformed response doesn't crash"""
        client = DBpediaClient()

        response = {
            "docs": [
                {
                    "resource": [],  # Empty resource
                    "label": ["Test"]
                },
                {
                    # Missing resource
                    "label": ["Test2"]
                }
            ]
        }

        candidates = client._parse_response(response)

        # Should skip malformed entries
        assert len(candidates) == 0

        client.close()


class TestMockedSearch:
    """Tests using mocked API responses"""

    def test_search_parses_api_response(self):
        """Test search correctly parses API response"""
        client = DBpediaClient()

        mock_response = {
            "docs": [
                {
                    "resource": ["http://dbpedia.org/resource/EPA"],
                    "label": ["Environmental Protection Agency"],
                    "comment": ["US government agency"],
                    "type": ["http://dbpedia.org/ontology/GovernmentAgency"],
                    "refCount": [500]
                }
            ]
        }

        with patch.object(client, '_execute_search') as mock_search:
            mock_search.return_value = mock_response

            result = client.search("EPA", EntityType.GOVERNMENT_ORG)

            assert len(result.candidates) == 1
            assert result.candidates[0].label == "Environmental Protection Agency"
            assert result.candidates[0].confidence > 0

        client.close()

    def test_search_handles_api_failure(self):
        """Test search handles API failure gracefully"""
        client = DBpediaClient()

        with patch.object(client, '_execute_search') as mock_search:
            mock_search.return_value = None

            result = client.search("EPA")

            # Should return empty result, not crash
            assert len(result.candidates) == 0
            assert result.error is None

        client.close()

    def test_search_sorts_by_confidence(self):
        """Test search results are sorted by confidence"""
        client = DBpediaClient()

        mock_response = {
            "docs": [
                {
                    "resource": ["http://dbpedia.org/resource/Low_Match"],
                    "label": ["Something Different"],
                    "refCount": [10]
                },
                {
                    "resource": ["http://dbpedia.org/resource/EPA"],
                    "label": ["EPA"],
                    "refCount": [1000]
                }
            ]
        }

        with patch.object(client, '_execute_search') as mock_search:
            mock_search.return_value = mock_response

            result = client.search("EPA")

            # EPA should be first due to exact match
            assert result.candidates[0].label == "EPA"

        client.close()


class TestGetResourceInfo:
    """Tests for get_resource_info method"""

    def test_invalid_uri_returns_none(self):
        """Test invalid URI returns None"""
        client = DBpediaClient()

        result = client.get_resource_info("")
        assert result is None

        result = client.get_resource_info("http://example.com/test")
        assert result is None

        client.close()

    def test_cached_resource_info(self):
        """Test cached resource info is returned"""
        client = DBpediaClient()

        uri = "http://dbpedia.org/resource/Test"
        cached_data = {"uri": uri, "data": {"label": "Test"}}

        import hashlib
        cache_key = f"dbpedia:resource:{hashlib.md5(uri.encode()).hexdigest()}"
        client._cache[cache_key] = cached_data

        result = client.get_resource_info(uri)

        assert result == cached_data

        client.close()


class TestSingleton:
    """Tests for singleton pattern"""

    def test_get_dbpedia_client_returns_same_instance(self):
        """Test get_dbpedia_client returns singleton"""
        import app.services.dbpedia_client as module

        # Reset singleton
        module._client_instance = None

        client1 = get_dbpedia_client()
        client2 = get_dbpedia_client()

        assert client1 is client2

        # Cleanup
        module._client_instance = None


class TestContextManager:
    """Tests for context manager support"""

    def test_context_manager_closes_client(self):
        """Test context manager properly closes client"""
        with DBpediaClient() as client:
            assert client._client is not None


# Integration test marker
@pytest.mark.integration
class TestDBpediaIntegration:
    """Integration tests that actually call DBpedia API"""

    @pytest.mark.skip(reason="Requires network access")
    def test_real_search_epa(self):
        """Test real search for EPA returns results"""
        client = DBpediaClient()

        result = client.search("Environmental Protection Agency", EntityType.GOVERNMENT_ORG)

        assert len(result.candidates) > 0

        client.close()

    @pytest.mark.skip(reason="Requires network access")
    def test_real_search_person(self):
        """Test real search for a person"""
        client = DBpediaClient()

        result = client.search("Barack Obama", EntityType.PERSON)

        assert len(result.candidates) > 0

        client.close()
