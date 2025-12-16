"""
Tests for Schema.org Mapper Service
"""

import pytest
from app.services.schema_mapper import SchemaMapper


class TestSchemaMapper:
    """Test Schema.org mapper functionality"""

    def test_get_schema_org_type_person(self):
        """Test mapping person to Schema.org Person"""
        result = SchemaMapper.get_schema_org_type("person")
        assert result == "Person"

    def test_get_schema_org_type_government_org(self):
        """Test mapping government_org to Schema.org GovernmentOrganization"""
        result = SchemaMapper.get_schema_org_type("government_org")
        assert result == "GovernmentOrganization"

    def test_get_schema_org_type_unknown(self):
        """Test unknown type defaults to Thing"""
        result = SchemaMapper.get_schema_org_type("unknown_type")
        assert result == "Thing"

    def test_map_spacy_label_person(self):
        """Test mapping spaCy PERSON label"""
        result = SchemaMapper.map_spacy_label("PERSON", "Elizabeth Warren")
        assert result == "person"

    def test_map_spacy_label_org_government(self):
        """Test mapping spaCy ORG label with government keywords"""
        result = SchemaMapper.map_spacy_label("ORG", "U.S. Senate")
        assert result == "government_org"

        result2 = SchemaMapper.map_spacy_label("ORG", "Environmental Protection Agency")
        assert result2 == "government_org"

    def test_map_spacy_label_org_non_government(self):
        """Test mapping spaCy ORG label without government keywords"""
        result = SchemaMapper.map_spacy_label("ORG", "Microsoft Corporation")
        assert result == "organization"

    def test_map_spacy_label_location(self):
        """Test mapping spaCy location labels"""
        result_gpe = SchemaMapper.map_spacy_label("GPE", "United States")
        assert result_gpe == "location"

        result_loc = SchemaMapper.map_spacy_label("LOC", "Washington D.C.")
        assert result_loc == "location"

    def test_map_spacy_label_skip_date(self):
        """Test that DATE labels are skipped"""
        result = SchemaMapper.map_spacy_label("DATE", "January 2024")
        assert result is None

    def test_generate_json_ld_person(self):
        """Test generating JSON-LD for person"""
        json_ld = SchemaMapper.generate_json_ld(
            entity_type="person",
            name="Elizabeth Warren",
            properties={"job_title": "Senator"}
        )

        assert json_ld["@context"] == "https://schema.org"
        assert json_ld["@type"] == "Person"
        assert json_ld["name"] == "Elizabeth Warren"
        assert json_ld["jobTitle"] == "Senator"

    def test_generate_json_ld_government_org(self):
        """Test generating JSON-LD for government organization"""
        json_ld = SchemaMapper.generate_json_ld(
            entity_type="government_org",
            name="Environmental Protection Agency",
            properties={"url": "https://www.epa.gov"}
        )

        assert json_ld["@type"] == "GovernmentOrganization"
        assert json_ld["name"] == "Environmental Protection Agency"
        assert json_ld["url"] == "https://www.epa.gov"

    def test_generate_json_ld_with_entity_id(self):
        """Test generating JSON-LD with @id field"""
        json_ld = SchemaMapper.generate_json_ld(
            entity_type="person",
            name="Joe Biden",
            entity_id="joe-biden-123"
        )

        assert "@id" in json_ld
        assert json_ld["@id"] == "https://newsanalyzer.org/entities/joe-biden-123"

    def test_enrich_person_with_political_party(self):
        """Test enriching person with political party"""
        json_ld = SchemaMapper.generate_json_ld(
            entity_type="person",
            name="Nancy Pelosi",
            properties={"political_party": "Democratic Party"}
        )

        assert "memberOf" in json_ld
        assert json_ld["memberOf"]["@type"] == "PoliticalParty"
        assert json_ld["memberOf"]["name"] == "Democratic Party"

    def test_enrich_place_with_address_components(self):
        """Test enriching place with address components"""
        json_ld = SchemaMapper.generate_json_ld(
            entity_type="location",
            name="Washington",
            properties={
                "country": "US",
                "region": "DC",
                "locality": "Washington"
            }
        )

        assert json_ld["@type"] == "Place"
        assert json_ld["addressCountry"] == "US"
        assert json_ld["addressRegion"] == "DC"
        assert json_ld["addressLocality"] == "Washington"
