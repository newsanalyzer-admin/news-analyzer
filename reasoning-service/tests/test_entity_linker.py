"""
Tests for Entity Linker Orchestrator

Tests the EntityLinker's ability to coordinate Wikidata and DBpedia clients
and use disambiguation to select the best match.
"""

import pytest
from unittest.mock import Mock, patch, MagicMock

from app.services.entity_linker import (
    EntityLinker,
    LinkedEntity,
    LinkingResult,
    LinkingStatistics,
    LinkingSource,
    LinkingStatus,
    get_entity_linker,
)
from app.services.wikidata_client import WikidataCandidate, WikidataSearchResult
from app.services.dbpedia_client import DBpediaCandidate, DBpediaSearchResult
from app.services.disambiguation import DisambiguationResult, Candidate, EntityType


class TestLinkedEntityDataclass:
    """Tests for LinkedEntity dataclass"""

    def test_to_dict_with_wikidata(self):
        """Test LinkedEntity to_dict with Wikidata match"""
        entity = LinkedEntity(
            text="Environmental Protection Agency",
            entity_type="government_org",
            wikidata_id="Q217173",
            wikidata_url="https://www.wikidata.org/wiki/Q217173",
            linking_confidence=0.95,
            linking_source="wikidata",
            linking_status=LinkingStatus.LINKED
        )

        result = entity.to_dict()

        assert result["text"] == "Environmental Protection Agency"
        assert result["wikidata_id"] == "Q217173"
        assert result["linking_status"] == "linked"
        assert result["linking_confidence"] == 0.95

    def test_to_dict_with_dbpedia(self):
        """Test LinkedEntity to_dict with DBpedia match"""
        entity = LinkedEntity(
            text="EPA",
            entity_type="government_org",
            dbpedia_uri="http://dbpedia.org/resource/EPA",
            linking_confidence=0.8,
            linking_source="dbpedia",
            linking_status=LinkingStatus.LINKED
        )

        result = entity.to_dict()

        assert result["dbpedia_uri"] == "http://dbpedia.org/resource/EPA"
        assert result["wikidata_id"] is None

    def test_to_dict_not_found(self):
        """Test LinkedEntity to_dict when not found"""
        entity = LinkedEntity(
            text="Unknown Entity",
            entity_type="organization",
            linking_status=LinkingStatus.NOT_FOUND,
            needs_review=True
        )

        result = entity.to_dict()

        assert result["linking_status"] == "not_found"
        assert result["needs_review"] is True


class TestLinkingStatistics:
    """Tests for LinkingStatistics dataclass"""

    def test_to_dict_with_stats(self):
        """Test LinkingStatistics to_dict"""
        stats = LinkingStatistics(
            total=10,
            linked=7,
            needs_review=2,
            not_found=1,
            errors=0
        )

        result = stats.to_dict()

        assert result["total"] == 10
        assert result["linked"] == 7
        assert result["success_rate"] == 0.7

    def test_to_dict_empty(self):
        """Test LinkingStatistics with zero total"""
        stats = LinkingStatistics(total=0)

        result = stats.to_dict()

        assert result["success_rate"] == 0.0


class TestEntityLinkerInitialization:
    """Tests for EntityLinker initialization"""

    def test_linker_initialization(self):
        """Test linker initializes with default clients"""
        with patch('app.services.entity_linker.get_wikidata_client') as mock_wiki:
            with patch('app.services.entity_linker.get_dbpedia_client') as mock_db:
                with patch('app.services.entity_linker.get_disambiguation_service') as mock_dis:
                    mock_wiki.return_value = Mock()
                    mock_db.return_value = Mock()
                    mock_dis.return_value = Mock()

                    linker = EntityLinker()

                    assert linker._wikidata is not None
                    assert linker._dbpedia is not None
                    assert linker._disambiguator is not None

    def test_linker_with_custom_clients(self):
        """Test linker accepts custom clients"""
        custom_wiki = Mock()
        custom_db = Mock()
        custom_dis = Mock()

        linker = EntityLinker(
            wikidata_client=custom_wiki,
            dbpedia_client=custom_db,
            disambiguation_service=custom_dis
        )

        assert linker._wikidata is custom_wiki
        assert linker._dbpedia is custom_db
        assert linker._disambiguator is custom_dis


class TestEntityTypeMapping:
    """Tests for entity type mapping"""

    def test_map_entity_type_person(self):
        """Test mapping person type"""
        linker = EntityLinker(
            wikidata_client=Mock(),
            dbpedia_client=Mock(),
            disambiguation_service=Mock()
        )

        result = linker._map_entity_type("person")
        assert result == EntityType.PERSON

    def test_map_entity_type_government_org(self):
        """Test mapping government_org type"""
        linker = EntityLinker(
            wikidata_client=Mock(),
            dbpedia_client=Mock(),
            disambiguation_service=Mock()
        )

        result = linker._map_entity_type("government_org")
        assert result == EntityType.GOVERNMENT_ORG

    def test_map_entity_type_unknown(self):
        """Test mapping unknown type returns None"""
        linker = EntityLinker(
            wikidata_client=Mock(),
            dbpedia_client=Mock(),
            disambiguation_service=Mock()
        )

        result = linker._map_entity_type("unknown_type")
        assert result is None


class TestCandidateConversion:
    """Tests for candidate conversion"""

    def test_convert_wikidata_candidates(self):
        """Test converting Wikidata candidates"""
        linker = EntityLinker(
            wikidata_client=Mock(),
            dbpedia_client=Mock(),
            disambiguation_service=Mock()
        )

        wikidata_candidates = [
            WikidataCandidate(
                qid="Q217173",
                label="EPA",
                description="US gov agency",
                aliases=["Environmental Protection Agency"],
                instance_of=["Q327333"],
                confidence=0.9
            )
        ]

        result = linker._convert_wikidata_candidates(wikidata_candidates)

        assert len(result) == 1
        assert result[0].id == "Q217173"
        assert result[0].source == "wikidata"
        assert result[0].label == "EPA"

    def test_convert_dbpedia_candidates(self):
        """Test converting DBpedia candidates"""
        linker = EntityLinker(
            wikidata_client=Mock(),
            dbpedia_client=Mock(),
            disambiguation_service=Mock()
        )

        dbpedia_candidates = [
            DBpediaCandidate(
                uri="http://dbpedia.org/resource/EPA",
                label="Environmental Protection Agency",
                types=["GovernmentAgency"],
                confidence=0.85
            )
        ]

        result = linker._convert_dbpedia_candidates(dbpedia_candidates)

        assert len(result) == 1
        assert result[0].id == "http://dbpedia.org/resource/EPA"
        assert result[0].source == "dbpedia"


class TestLinkSingleEntity:
    """Tests for single entity linking"""

    def test_link_entity_wikidata_success(self):
        """Test successful linking via Wikidata"""
        mock_wikidata = Mock()
        mock_wikidata.search.return_value = WikidataSearchResult(
            query="EPA",
            entity_type=None,
            candidates=[
                WikidataCandidate(
                    qid="Q217173",
                    label="Environmental Protection Agency",
                    description="US government agency",
                    confidence=0.9
                )
            ]
        )

        mock_disambiguator = Mock()
        mock_disambiguator.disambiguate.return_value = DisambiguationResult(
            entity_text="EPA",
            entity_type=EntityType.GOVERNMENT_ORG,
            match=Candidate(
                id="Q217173",
                label="Environmental Protection Agency",
                source="wikidata"
            ),
            confidence=0.95,
            needs_review=False,
            all_candidates=[]
        )

        linker = EntityLinker(
            wikidata_client=mock_wikidata,
            dbpedia_client=Mock(),
            disambiguation_service=mock_disambiguator
        )

        result = linker.link_entity(
            text="EPA",
            entity_type="government_org",
            sources=LinkingSource.WIKIDATA
        )

        assert result.wikidata_id == "Q217173"
        assert result.linking_status == LinkingStatus.LINKED
        assert result.linking_confidence >= 0.7

    def test_link_entity_dbpedia_fallback(self):
        """Test DBpedia fallback when Wikidata returns nothing"""
        mock_wikidata = Mock()
        mock_wikidata.search.return_value = WikidataSearchResult(
            query="Test",
            entity_type=None,
            candidates=[]  # No Wikidata results
        )

        mock_dbpedia = Mock()
        mock_dbpedia.search.return_value = DBpediaSearchResult(
            query="Test",
            entity_type=None,
            candidates=[
                DBpediaCandidate(
                    uri="http://dbpedia.org/resource/Test",
                    label="Test Entity",
                    confidence=0.8
                )
            ]
        )

        mock_disambiguator = Mock()
        mock_disambiguator.disambiguate.return_value = DisambiguationResult(
            entity_text="Test",
            entity_type=None,
            match=Candidate(
                id="http://dbpedia.org/resource/Test",
                label="Test Entity",
                source="dbpedia"
            ),
            confidence=0.8,
            needs_review=False,
            all_candidates=[]
        )

        linker = EntityLinker(
            wikidata_client=mock_wikidata,
            dbpedia_client=mock_dbpedia,
            disambiguation_service=mock_disambiguator
        )

        result = linker.link_entity(
            text="Test",
            entity_type="organization",
            sources=LinkingSource.BOTH
        )

        assert result.dbpedia_uri == "http://dbpedia.org/resource/Test"
        assert result.wikidata_id is None

    def test_link_entity_not_found(self):
        """Test entity not found in any KB"""
        mock_wikidata = Mock()
        mock_wikidata.search.return_value = WikidataSearchResult(
            query="Unknown",
            entity_type=None,
            candidates=[]
        )

        mock_dbpedia = Mock()
        mock_dbpedia.search.return_value = DBpediaSearchResult(
            query="Unknown",
            entity_type=None,
            candidates=[]
        )

        linker = EntityLinker(
            wikidata_client=mock_wikidata,
            dbpedia_client=mock_dbpedia,
            disambiguation_service=Mock()
        )

        result = linker.link_entity(
            text="Unknown Entity XYZ",
            entity_type="organization",
            sources=LinkingSource.BOTH
        )

        assert result.linking_status == LinkingStatus.NOT_FOUND
        assert result.needs_review is True

    def test_link_entity_low_confidence_needs_review(self):
        """Test low confidence match is flagged for review"""
        mock_wikidata = Mock()
        mock_wikidata.search.return_value = WikidataSearchResult(
            query="Test",
            entity_type=None,
            candidates=[
                WikidataCandidate(qid="Q1", label="Test", confidence=0.5)
            ]
        )

        mock_disambiguator = Mock()
        mock_disambiguator.disambiguate.return_value = DisambiguationResult(
            entity_text="Test",
            entity_type=None,
            match=Candidate(id="Q1", label="Test", source="wikidata"),
            confidence=0.5,  # Below threshold
            needs_review=True,
            all_candidates=[Candidate(id="Q1", label="Test", source="wikidata")]
        )

        linker = EntityLinker(
            wikidata_client=mock_wikidata,
            dbpedia_client=Mock(),
            disambiguation_service=mock_disambiguator
        )

        result = linker.link_entity(
            text="Test",
            entity_type="organization"
        )

        assert result.linking_status == LinkingStatus.NEEDS_REVIEW
        assert result.needs_review is True
        assert len(result.candidates) > 0

    def test_link_entity_error_handling(self):
        """Test error handling during linking"""
        mock_wikidata = Mock()
        mock_wikidata.search.side_effect = Exception("Network error")

        linker = EntityLinker(
            wikidata_client=mock_wikidata,
            dbpedia_client=Mock(),
            disambiguation_service=Mock()
        )

        result = linker.link_entity(
            text="Test",
            entity_type="organization"
        )

        assert result.linking_status == LinkingStatus.ERROR
        assert result.error is not None


class TestLinkBatch:
    """Tests for batch entity linking"""

    def test_link_batch_multiple_entities(self):
        """Test batch linking processes all entities"""
        mock_wikidata = Mock()
        mock_wikidata.search.return_value = WikidataSearchResult(
            query="Test",
            entity_type=None,
            candidates=[WikidataCandidate(qid="Q1", label="Test", confidence=0.9)]
        )

        mock_disambiguator = Mock()
        mock_disambiguator.disambiguate.return_value = DisambiguationResult(
            entity_text="Test",
            entity_type=None,
            match=Candidate(id="Q1", label="Test", source="wikidata"),
            confidence=0.9,
            needs_review=False,
            all_candidates=[]
        )

        linker = EntityLinker(
            wikidata_client=mock_wikidata,
            dbpedia_client=Mock(),
            disambiguation_service=mock_disambiguator
        )

        entities = [
            {"text": "Entity1", "entity_type": "person"},
            {"text": "Entity2", "entity_type": "organization"},
            {"text": "Entity3", "entity_type": "location"},
        ]

        result = linker.link_batch(entities)

        assert len(result.linked_entities) == 3
        assert result.statistics.total == 3

    def test_link_batch_statistics(self):
        """Test batch statistics are accurate"""
        mock_wikidata = Mock()

        # First two succeed, third fails
        call_count = [0]

        def mock_search(*args, **kwargs):
            call_count[0] += 1
            if call_count[0] <= 2:
                return WikidataSearchResult(
                    query="Test",
                    entity_type=None,
                    candidates=[WikidataCandidate(qid="Q1", label="Test", confidence=0.9)]
                )
            return WikidataSearchResult(
                query="Test",
                entity_type=None,
                candidates=[]
            )

        mock_wikidata.search = mock_search

        mock_dbpedia = Mock()
        mock_dbpedia.search.return_value = DBpediaSearchResult(
            query="Test",
            entity_type=None,
            candidates=[]
        )

        mock_disambiguator = Mock()
        mock_disambiguator.disambiguate.return_value = DisambiguationResult(
            entity_text="Test",
            entity_type=None,
            match=Candidate(id="Q1", label="Test", source="wikidata"),
            confidence=0.9,
            needs_review=False,
            all_candidates=[]
        )

        linker = EntityLinker(
            wikidata_client=mock_wikidata,
            dbpedia_client=mock_dbpedia,
            disambiguation_service=mock_disambiguator
        )

        entities = [
            {"text": "Entity1", "entity_type": "person"},
            {"text": "Entity2", "entity_type": "organization"},
            {"text": "Entity3", "entity_type": "location"},
        ]

        result = linker.link_batch(entities)

        assert result.statistics.total == 3
        assert result.statistics.linked == 2
        assert result.statistics.not_found == 1

    def test_link_batch_with_context(self):
        """Test batch linking passes context"""
        mock_wikidata = Mock()
        mock_wikidata.search.return_value = WikidataSearchResult(
            query="Test",
            entity_type=None,
            candidates=[WikidataCandidate(qid="Q1", label="Test", confidence=0.9)]
        )

        mock_disambiguator = Mock()
        mock_disambiguator.disambiguate.return_value = DisambiguationResult(
            entity_text="Test",
            entity_type=None,
            match=Candidate(id="Q1", label="Test", source="wikidata"),
            confidence=0.9,
            needs_review=False,
            all_candidates=[]
        )

        linker = EntityLinker(
            wikidata_client=mock_wikidata,
            dbpedia_client=Mock(),
            disambiguation_service=mock_disambiguator
        )

        entities = [
            {
                "text": "Washington",
                "entity_type": "location",
                "context": "The city of Washington DC"
            }
        ]

        linker.link_batch(entities)

        # Verify disambiguate was called with context
        call_args = mock_disambiguator.disambiguate.call_args
        assert call_args[1]["context"] == "The city of Washington DC"


class TestSourceSelection:
    """Tests for source selection"""

    def test_wikidata_only(self):
        """Test Wikidata-only source selection"""
        mock_wikidata = Mock()
        mock_wikidata.search.return_value = WikidataSearchResult(
            query="Test",
            entity_type=None,
            candidates=[WikidataCandidate(qid="Q1", label="Test", confidence=0.9)]
        )

        mock_dbpedia = Mock()

        mock_disambiguator = Mock()
        mock_disambiguator.disambiguate.return_value = DisambiguationResult(
            entity_text="Test",
            entity_type=None,
            match=Candidate(id="Q1", label="Test", source="wikidata"),
            confidence=0.9,
            needs_review=False,
            all_candidates=[]
        )

        linker = EntityLinker(
            wikidata_client=mock_wikidata,
            dbpedia_client=mock_dbpedia,
            disambiguation_service=mock_disambiguator
        )

        linker.link_entity(
            text="Test",
            entity_type="organization",
            sources=LinkingSource.WIKIDATA
        )

        # Wikidata should be called, DBpedia should not
        mock_wikidata.search.assert_called_once()
        mock_dbpedia.search.assert_not_called()

    def test_dbpedia_only(self):
        """Test DBpedia-only source selection"""
        mock_wikidata = Mock()

        mock_dbpedia = Mock()
        mock_dbpedia.search.return_value = DBpediaSearchResult(
            query="Test",
            entity_type=None,
            candidates=[DBpediaCandidate(
                uri="http://dbpedia.org/resource/Test",
                label="Test",
                confidence=0.9
            )]
        )

        mock_disambiguator = Mock()
        mock_disambiguator.disambiguate.return_value = DisambiguationResult(
            entity_text="Test",
            entity_type=None,
            match=Candidate(
                id="http://dbpedia.org/resource/Test",
                label="Test",
                source="dbpedia"
            ),
            confidence=0.9,
            needs_review=False,
            all_candidates=[]
        )

        linker = EntityLinker(
            wikidata_client=mock_wikidata,
            dbpedia_client=mock_dbpedia,
            disambiguation_service=mock_disambiguator
        )

        linker.link_entity(
            text="Test",
            entity_type="organization",
            sources=LinkingSource.DBPEDIA
        )

        # DBpedia should be called, Wikidata should not
        mock_wikidata.search.assert_not_called()
        mock_dbpedia.search.assert_called_once()


class TestSingleton:
    """Tests for singleton pattern"""

    def test_get_entity_linker_returns_same_instance(self):
        """Test singleton returns same instance"""
        import app.services.entity_linker as module

        # Reset singleton
        module._linker_instance = None

        with patch.object(module, 'get_wikidata_client'):
            with patch.object(module, 'get_dbpedia_client'):
                with patch.object(module, 'get_disambiguation_service'):
                    linker1 = get_entity_linker()
                    linker2 = get_entity_linker()

                    assert linker1 is linker2

        # Cleanup
        module._linker_instance = None


class TestLinkingResult:
    """Tests for LinkingResult dataclass"""

    def test_to_dict(self):
        """Test LinkingResult to_dict"""
        result = LinkingResult(
            linked_entities=[
                LinkedEntity(
                    text="Test",
                    entity_type="organization",
                    wikidata_id="Q1",
                    linking_confidence=0.9,
                    linking_status=LinkingStatus.LINKED
                )
            ],
            statistics=LinkingStatistics(
                total=1,
                linked=1
            )
        )

        data = result.to_dict()

        assert len(data["linked_entities"]) == 1
        assert data["statistics"]["total"] == 1
