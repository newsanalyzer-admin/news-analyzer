"""
Schema.org Mapper Service

Maps internal entity types to Schema.org vocabulary and generates JSON-LD representations.
"""

from typing import Dict, Any, Optional


class SchemaMapper:
    """Maps NewsAnalyzer entity types to Schema.org types and generates JSON-LD"""

    # Internal entity type to Schema.org type mapping
    ENTITY_TYPE_TO_SCHEMA_ORG = {
        "person": "Person",
        "government_org": "GovernmentOrganization",
        "organization": "Organization",
        "location": "Place",
        "event": "Event",
        "concept": "Thing",
        "legislation": "Legislation",
        "political_party": "PoliticalParty",
        "news_media": "NewsMediaOrganization",
    }

    # spaCy NER labels to internal entity types
    SPACY_LABEL_TO_ENTITY_TYPE = {
        "PERSON": "person",
        "ORG": "organization",
        "GPE": "location",  # Geopolitical Entity
        "LOC": "location",
        "EVENT": "event",
        "NORP": "concept",  # Nationalities, religious, political groups
        "LAW": "legislation",
        "DATE": None,  # Skip dates for now
        "TIME": None,
        "MONEY": None,
        "PERCENT": None,
        "QUANTITY": None,
        "CARDINAL": None,
        "ORDINAL": None,
    }

    # Government-related keywords for organization classification
    GOVERNMENT_KEYWORDS = {
        "senate", "congress", "house", "committee", "agency", "department",
        "administration", "bureau", "commission", "epa", "fbi", "cia", "fda",
        "doj", "treasury", "state department", "defense", "white house",
        "government", "federal", "ministry", "parliament"
    }

    @classmethod
    def get_schema_org_type(cls, entity_type: str) -> str:
        """
        Get Schema.org type for internal entity type

        Args:
            entity_type: Internal entity type (e.g., "person", "government_org")

        Returns:
            Schema.org type (e.g., "Person", "GovernmentOrganization")
        """
        return cls.ENTITY_TYPE_TO_SCHEMA_ORG.get(entity_type, "Thing")

    @classmethod
    def map_spacy_label(cls, label: str, text: str) -> Optional[str]:
        """
        Map spaCy NER label to internal entity type

        Args:
            label: spaCy NER label (e.g., "PERSON", "ORG")
            text: Entity text for context

        Returns:
            Internal entity type or None if should be skipped
        """
        base_type = cls.SPACY_LABEL_TO_ENTITY_TYPE.get(label)

        if base_type is None:
            return None

        # Enhance ORG classification
        if base_type == "organization":
            text_lower = text.lower()
            if any(keyword in text_lower for keyword in cls.GOVERNMENT_KEYWORDS):
                return "government_org"

        return base_type

    @classmethod
    def generate_json_ld(
        cls,
        entity_type: str,
        name: str,
        properties: Optional[Dict[str, Any]] = None,
        entity_id: Optional[str] = None
    ) -> Dict[str, Any]:
        """
        Generate Schema.org JSON-LD representation

        Args:
            entity_type: Internal entity type
            name: Entity name
            properties: Additional properties
            entity_id: Optional entity ID for @id field

        Returns:
            Schema.org JSON-LD dict
        """
        schema_type = cls.get_schema_org_type(entity_type)
        properties = properties or {}

        json_ld: Dict[str, Any] = {
            "@context": "https://schema.org",
            "@type": schema_type,
            "name": name,
        }

        # Add @id if provided
        if entity_id:
            json_ld["@id"] = f"https://newsanalyzer.org/entities/{entity_id}"

        # Add type-specific properties
        if schema_type == "Person":
            json_ld.update(cls._enrich_person(properties))
        elif schema_type == "GovernmentOrganization":
            json_ld.update(cls._enrich_government_org(properties))
        elif schema_type == "Organization":
            json_ld.update(cls._enrich_organization(properties))
        elif schema_type == "Place":
            json_ld.update(cls._enrich_place(properties))
        elif schema_type == "Event":
            json_ld.update(cls._enrich_event(properties))

        return json_ld

    @classmethod
    def _enrich_person(cls, properties: Dict[str, Any]) -> Dict[str, Any]:
        """Add Person-specific Schema.org properties"""
        enriched = {}

        if "job_title" in properties:
            enriched["jobTitle"] = properties["job_title"]
        if "affiliation" in properties:
            enriched["affiliation"] = {
                "@type": "Organization",
                "name": properties["affiliation"]
            }
        if "member_of" in properties:
            enriched["memberOf"] = {
                "@type": "Organization",
                "name": properties["member_of"]
            }
        if "political_party" in properties:
            enriched["memberOf"] = {
                "@type": "PoliticalParty",
                "name": properties["political_party"]
            }

        return enriched

    @classmethod
    def _enrich_government_org(cls, properties: Dict[str, Any]) -> Dict[str, Any]:
        """Add GovernmentOrganization-specific Schema.org properties"""
        enriched = {}

        if "url" in properties:
            enriched["url"] = properties["url"]
        if "parent_org" in properties:
            enriched["parentOrganization"] = {
                "@type": "GovernmentOrganization",
                "name": properties["parent_org"]
            }
        if "jurisdiction" in properties:
            enriched["areaServed"] = {
                "@type": "Place",
                "name": properties["jurisdiction"]
            }

        return enriched

    @classmethod
    def _enrich_organization(cls, properties: Dict[str, Any]) -> Dict[str, Any]:
        """Add Organization-specific Schema.org properties"""
        enriched = {}

        if "url" in properties:
            enriched["url"] = properties["url"]
        if "description" in properties:
            enriched["description"] = properties["description"]
        if "founded_date" in properties:
            enriched["foundingDate"] = properties["founded_date"]

        return enriched

    @classmethod
    def _enrich_place(cls, properties: Dict[str, Any]) -> Dict[str, Any]:
        """Add Place-specific Schema.org properties"""
        enriched = {}

        if "country" in properties:
            enriched["addressCountry"] = properties["country"]
        if "region" in properties:
            enriched["addressRegion"] = properties["region"]
        if "locality" in properties:
            enriched["addressLocality"] = properties["locality"]

        return enriched

    @classmethod
    def _enrich_event(cls, properties: Dict[str, Any]) -> Dict[str, Any]:
        """Add Event-specific Schema.org properties"""
        enriched = {}

        if "start_date" in properties:
            enriched["startDate"] = properties["start_date"]
        if "end_date" in properties:
            enriched["endDate"] = properties["end_date"]
        if "location" in properties:
            enriched["location"] = {
                "@type": "Place",
                "name": properties["location"]
            }

        return enriched
