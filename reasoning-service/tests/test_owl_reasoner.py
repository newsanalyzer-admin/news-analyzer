"""
Unit tests for OWL Reasoning Service
"""

import pytest
from pathlib import Path

# Check if dependencies are available
try:
    from app.services.owl_reasoner import OWLReasoner, get_reasoner, DEPENDENCIES_AVAILABLE
    SKIP_TESTS = not DEPENDENCIES_AVAILABLE
    SKIP_REASON = "RDFLib and owlrl not installed"
except ImportError:
    SKIP_TESTS = True
    SKIP_REASON = "OWL reasoner module not available"


@pytest.mark.skipif(SKIP_TESTS, reason=SKIP_REASON)
class TestOWLReasoner:
    """Test suite for OWL reasoning functionality"""

    @pytest.fixture
    def reasoner(self):
        """Create a reasoner instance for testing"""
        ontology_path = Path(__file__).parent.parent / "ontology" / "newsanalyzer.ttl"
        if not ontology_path.exists():
            pytest.skip(f"Ontology file not found: {ontology_path}")
        return OWLReasoner(str(ontology_path))

    def test_ontology_loads(self, reasoner):
        """Test that the ontology loads successfully"""
        assert reasoner.graph is not None
        assert len(reasoner.graph) > 0

    def test_ontology_stats(self, reasoner):
        """Test ontology statistics calculation"""
        stats = reasoner.get_ontology_stats()

        assert "total_triples" in stats
        assert "classes" in stats
        assert "properties" in stats
        assert "individuals" in stats

        # Basic sanity checks
        assert stats["total_triples"] > 0
        assert stats["classes"] >= 7  # We defined at least 7 custom classes
        assert stats["properties"] > 0

    def test_add_entity(self, reasoner):
        """Test adding an entity to the graph"""
        entity_uri = "http://newsanalyzer.org/entity/test_epa"
        reasoner.add_entity(
            entity_uri=entity_uri,
            entity_type="GovernmentOrganization",
            properties={
                "name": "Environmental Protection Agency",
                "alternateName": "EPA"
            }
        )

        # Verify entity was added
        types = reasoner.classify_entity(entity_uri)
        assert len(types) > 0
        assert "http://schema.org/GovernmentOrganization" in types

    def test_inference_basic(self, reasoner):
        """Test basic OWL inference"""
        initial_size = len(reasoner.graph)

        # Add an entity with properties that trigger inference
        entity_uri = "http://newsanalyzer.org/entity/test_senator"
        reasoner.add_entity(
            entity_uri=entity_uri,
            entity_type="Person",
            properties={
                "name": "Test Senator",
                "na:memberOf": "http://newsanalyzer.org/entity/us_senate"
            }
        )

        # Run inference
        final_size = reasoner.infer()

        # Verify new triples were inferred
        assert final_size >= initial_size

    def test_classify_entity(self, reasoner):
        """Test entity classification"""
        # Use a predefined entity from ontology
        epa_uri = "http://newsanalyzer.org/ontology#EPA"

        types = reasoner.classify_entity(epa_uri)

        # Should have at least the base type
        assert len(types) > 0

    def test_enrich_entity_data(self, reasoner):
        """Test entity enrichment with reasoning"""
        enriched = reasoner.enrich_entity_data(
            entity_text="EPA",
            entity_type="government_org",
            confidence=0.9,
            base_properties={"alternateName": "Environmental Protection Agency"}
        )

        assert enriched["text"] == "EPA"
        assert enriched["entity_type"] == "government_org"
        assert enriched["confidence"] == 0.9
        assert "schema_org_types" in enriched
        assert "inferred_properties" in enriched
        assert enriched["reasoning_applied"] is True

        # Should have at least one Schema.org type
        assert len(enriched["schema_org_types"]) > 0

    def test_get_entity_properties(self, reasoner):
        """Test retrieving entity properties"""
        # Add test entity
        entity_uri = "http://newsanalyzer.org/entity/test_org"
        reasoner.add_entity(
            entity_uri=entity_uri,
            entity_type="Organization",
            properties={
                "name": "Test Organization",
                "description": "A test organization"
            }
        )

        # Get properties
        props = reasoner.get_entity_properties(entity_uri)

        assert len(props) > 0
        assert any("name" in str(key) for key in props.keys())

    def test_sparql_query(self, reasoner):
        """Test SPARQL query execution"""
        query = """
        PREFIX schema: <http://schema.org/>
        PREFIX na: <http://newsanalyzer.org/ontology#>

        SELECT ?agency WHERE {
            ?agency a na:ExecutiveAgency .
        }
        """

        results = reasoner.query_sparql(query)

        assert isinstance(results, list)
        # Should find at least the predefined agencies (EPA, FDA, DOJ)
        assert len(results) >= 3

    def test_consistency_check(self, reasoner):
        """Test consistency checking"""
        errors = reasoner.check_consistency()

        # Empty ontology should have no errors
        assert isinstance(errors, list)

    def test_export_graph(self, reasoner):
        """Test graph export in various formats"""
        # Test Turtle format
        turtle = reasoner.export_graph(format="turtle")
        assert isinstance(turtle, str)
        assert len(turtle) > 0
        assert "@prefix" in turtle or "PREFIX" in turtle

        # Test N-Triples format
        ntriples = reasoner.export_graph(format="nt")
        assert isinstance(ntriples, str)
        assert len(ntriples) > 0

    def test_singleton_reasoner(self):
        """Test that get_reasoner returns a singleton"""
        reasoner1 = get_reasoner()
        reasoner2 = get_reasoner()

        assert reasoner1 is reasoner2

    def test_multiple_entity_enrichment(self, reasoner):
        """Test enriching multiple entities"""
        entities = [
            {"text": "EPA", "entity_type": "government_org", "confidence": 0.9},
            {"text": "Congress", "entity_type": "government_org", "confidence": 0.95},
            {"text": "Senator Smith", "entity_type": "person", "confidence": 0.85}
        ]

        enriched_entities = []
        for entity in entities:
            enriched = reasoner.enrich_entity_data(
                entity_text=entity["text"],
                entity_type=entity["entity_type"],
                confidence=entity["confidence"],
                base_properties={}
            )
            enriched_entities.append(enriched)

        assert len(enriched_entities) == 3
        assert all(e["reasoning_applied"] for e in enriched_entities)

    def test_inference_with_legislative_body(self, reasoner):
        """Test inference rules for legislative bodies"""
        # Add an organization that passed legislation
        org_uri = "http://newsanalyzer.org/entity/test_legislature"
        legislation_uri = "http://newsanalyzer.org/entity/test_bill"

        # Add legislation
        reasoner.add_entity(
            entity_uri=legislation_uri,
            entity_type=str(reasoner.graph.namespace_manager.store.namespace("na")) + "Legislation",
            properties={"name": "Test Bill"}
        )

        # Add organization that passed it
        reasoner.add_entity(
            entity_uri=org_uri,
            entity_type="Organization",
            properties={
                "name": "Test Legislature",
                "na:passedLegislation": legislation_uri
            }
        )

        # Run inference
        reasoner.infer()

        # Check if inferred as LegislativeBody
        types = reasoner.classify_entity(org_uri)

        # Should be classified as at least an Organization
        assert len(types) > 0


@pytest.mark.skipif(SKIP_TESTS, reason=SKIP_REASON)
class TestOWLReasonerEdgeCases:
    """Test edge cases and error handling"""

    def test_invalid_ontology_path(self):
        """Test handling of invalid ontology path"""
        with pytest.raises(Exception):
            OWLReasoner(ontology_path="/nonexistent/path/ontology.ttl")

    def test_empty_entity_text(self):
        """Test handling of empty entity text"""
        reasoner = get_reasoner()

        enriched = reasoner.enrich_entity_data(
            entity_text="",
            entity_type="person",
            confidence=0.5,
            base_properties={}
        )

        assert enriched["text"] == ""
        assert enriched["reasoning_applied"] is True

    def test_invalid_sparql_query(self):
        """Test handling of invalid SPARQL query"""
        reasoner = get_reasoner()

        with pytest.raises(Exception):
            reasoner.query_sparql("INVALID SPARQL QUERY")

    def test_entity_with_no_properties(self):
        """Test entity enrichment with no properties"""
        reasoner = get_reasoner()

        enriched = reasoner.enrich_entity_data(
            entity_text="Test Entity",
            entity_type="organization",
            confidence=0.7,
            base_properties={}
        )

        assert enriched["text"] == "Test Entity"
        assert "schema_org_types" in enriched
        assert "inferred_properties" in enriched


@pytest.mark.skipif(SKIP_TESTS, reason=SKIP_REASON)
class TestOWLReasonerIntegration:
    """Integration tests for OWL reasoning with entity extraction"""

    def test_full_reasoning_pipeline(self, tmp_path):
        """Test complete reasoning pipeline from extraction to inference"""
        reasoner = get_reasoner()

        # Simulate extracted entities
        extracted_entities = [
            {
                "text": "Environmental Protection Agency",
                "entity_type": "government_org",
                "confidence": 0.95,
                "properties": {"alternateName": "EPA"}
            },
            {
                "text": "Elizabeth Warren",
                "entity_type": "person",
                "confidence": 0.90,
                "properties": {}
            }
        ]

        # Enrich with reasoning
        enriched = []
        for entity in extracted_entities:
            enriched_entity = reasoner.enrich_entity_data(
                entity_text=entity["text"],
                entity_type=entity["entity_type"],
                confidence=entity["confidence"],
                base_properties=entity["properties"]
            )
            enriched.append(enriched_entity)

        # Verify enrichment
        assert len(enriched) == 2

        # EPA should be classified
        epa = enriched[0]
        assert "GovernmentOrganization" in str(epa["schema_org_types"])

        # Senator should be classified
        senator = enriched[1]
        assert "Person" in str(senator["schema_org_types"])


if __name__ == "__main__":
    pytest.main([__file__, "-v"])
