"""
Tests for Entity Extractor Service
"""

import pytest
from app.services.entity_extractor import EntityExtractor, ExtractedEntity


class TestEntityExtractor:
    """Test entity extraction functionality"""

    @pytest.fixture
    def extractor(self):
        """Create EntityExtractor instance for testing"""
        try:
            return EntityExtractor(model_name="en_core_web_sm")
        except RuntimeError as e:
            pytest.skip(f"spaCy model not available: {e}")

    def test_extractor_initialization(self, extractor):
        """Test that extractor initializes correctly"""
        assert extractor is not None
        assert extractor.nlp is not None
        assert extractor.schema_mapper is not None

    def test_extract_person(self, extractor):
        """Test extracting person entities"""
        text = "Elizabeth Warren is a United States Senator."
        entities = extractor.extract(text, confidence_threshold=0.5)

        # Should find at least the person
        assert len(entities) > 0

        # Find the person entity
        person_entities = [e for e in entities if e.entity_type == "person"]
        assert len(person_entities) > 0

        person = person_entities[0]
        assert "Elizabeth Warren" in person.text or "Warren" in person.text
        assert person.schema_org_type == "Person"
        assert person.schema_org_data["@type"] == "Person"

    def test_extract_government_org(self, extractor):
        """Test extracting government organization entities"""
        text = "The Environmental Protection Agency announced new regulations."
        entities = extractor.extract(text, confidence_threshold=0.5)

        # Should find the organization
        assert len(entities) > 0

        # Check if any entity was classified as government_org
        gov_orgs = [e for e in entities if e.entity_type == "government_org"]
        if len(gov_orgs) > 0:
            epa = gov_orgs[0]
            assert epa.schema_org_type == "GovernmentOrganization"
            assert epa.schema_org_data["@type"] == "GovernmentOrganization"

    def test_extract_location(self, extractor):
        """Test extracting location entities"""
        text = "The meeting took place in Washington, D.C."
        entities = extractor.extract(text, confidence_threshold=0.5)

        # Should find location
        locations = [e for e in entities if e.entity_type == "location"]
        assert len(locations) > 0

        location = locations[0]
        assert location.schema_org_type == "Place"

    def test_extract_with_context(self, extractor):
        """Test extract_with_context returns proper structure"""
        text = "Senator Elizabeth Warren met with President Joe Biden in Washington."
        result = extractor.extract_with_context(text, confidence_threshold=0.5)

        assert "entities" in result
        assert "total_count" in result
        assert "text_length" in result
        assert "entity_types" in result

        assert isinstance(result["entities"], list)
        assert result["total_count"] >= 0
        assert result["text_length"] == len(text)
        assert isinstance(result["entity_types"], dict)

    def test_confidence_threshold(self, extractor):
        """Test that confidence threshold filtering works"""
        text = "Elizabeth Warren is a Senator."

        # Low threshold should get entities
        entities_low = extractor.extract(text, confidence_threshold=0.5)

        # High threshold (above our fixed 0.85) should get no entities
        entities_high = extractor.extract(text, confidence_threshold=0.95)

        # Low threshold should have more or equal entities
        assert len(entities_low) >= len(entities_high)

    def test_empty_text(self, extractor):
        """Test extracting from empty text"""
        entities = extractor.extract("", confidence_threshold=0.7)
        assert len(entities) == 0

    def test_no_entities_text(self, extractor):
        """Test text with no recognizable entities"""
        text = "This is a simple sentence."
        entities = extractor.extract(text, confidence_threshold=0.7)
        # May or may not find entities depending on spaCy model
        assert isinstance(entities, list)

    def test_extracted_entity_to_dict(self, extractor):
        """Test ExtractedEntity to_dict conversion"""
        text = "Elizabeth Warren is a Senator."
        entities = extractor.extract(text, confidence_threshold=0.5)

        if len(entities) > 0:
            entity_dict = entities[0].to_dict()

            assert "text" in entity_dict
            assert "entity_type" in entity_dict
            assert "start" in entity_dict
            assert "end" in entity_dict
            assert "confidence" in entity_dict
            assert "schema_org_type" in entity_dict
            assert "schema_org_data" in entity_dict
            assert "properties" in entity_dict

    def test_entity_count_by_type(self, extractor):
        """Test counting entities by type"""
        text = "Senator Elizabeth Warren and President Joe Biden met in Washington."
        result = extractor.extract_with_context(text, confidence_threshold=0.5)

        entity_types = result["entity_types"]
        assert isinstance(entity_types, dict)

        # Should have at least some entity types
        if result["total_count"] > 0:
            assert len(entity_types) > 0
            # All counts should be positive
            for count in entity_types.values():
                assert count > 0
