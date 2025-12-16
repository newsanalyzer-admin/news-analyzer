"""
Government Organization Validator Service

Validates and enriches extracted government organization entities against
the authoritative government organizations database.
"""

import requests
from typing import Dict, Any, Optional, List
from difflib import SequenceMatcher


class GovernmentOrgValidator:
    """Validates and enriches government organization entities"""

    def __init__(self, backend_url: str = "http://localhost:8080"):
        """
        Initialize the validator

        Args:
            backend_url: Base URL of the NewsAnalyzer backend API
        """
        self.backend_url = backend_url
        self._cache: Dict[str, Any] = {}  # Cache for government orgs

    def validate_entity(
        self,
        entity_text: str,
        entity_type: str
    ) -> Optional[Dict[str, Any]]:
        """
        Validate an extracted entity against the government organizations database

        Args:
            entity_text: The extracted entity text (e.g., "EPA", "Department of Defense")
            entity_type: The entity type (should be "government_org")

        Returns:
            Validation result with matched organization data, or None if not a gov org
        """
        # Only validate government organization entities
        if entity_type != "government_org":
            return None

        # Try to find matching organization
        matched_org = self._find_matching_org(entity_text)

        if matched_org:
            return {
                "is_valid": True,
                "match_type": matched_org["match_type"],
                "confidence": matched_org["confidence"],
                "matched_organization": matched_org["data"],
                "suggestions": []
            }
        else:
            # No match - provide suggestions
            suggestions = self._get_suggestions(entity_text)
            return {
                "is_valid": False,
                "match_type": "none",
                "confidence": 0.0,
                "matched_organization": None,
                "suggestions": suggestions
            }

    def enrich_entity(
        self,
        entity: Dict[str, Any],
        validation_result: Dict[str, Any]
    ) -> Dict[str, Any]:
        """
        Enrich entity with official government organization data

        Args:
            entity: The extracted entity dictionary
            validation_result: Result from validate_entity()

        Returns:
            Enriched entity with official data
        """
        if not validation_result or not validation_result.get("is_valid"):
            # Mark as unverified if no match
            entity["verified"] = False
            entity["validation_status"] = "unverified"
            if validation_result and validation_result.get("suggestions"):
                entity["suggested_matches"] = validation_result["suggestions"]
            return entity

        # Extract official organization data
        org = validation_result["matched_organization"]

        # Enrich entity properties
        entity["verified"] = True
        entity["validation_status"] = "verified"
        entity["match_confidence"] = validation_result["confidence"]
        entity["match_type"] = validation_result["match_type"]

        # Add official data to properties
        entity["properties"] = entity.get("properties", {})
        entity["properties"].update({
            "government_org_id": org["id"],
            "official_name": org["officialName"],
            "acronym": org.get("acronym"),
            "org_type": org["orgType"],
            "branch": org["branch"],
            "org_level": org["orgLevel"],
            "website_url": org.get("websiteUrl"),
            "established_date": org.get("establishedDate"),
            "jurisdiction_areas": org.get("jurisdictionAreas", []),
            "is_active": org["active"],
            "is_cabinet_department": org.get("cabinetDepartment", False),
            "is_independent_agency": org.get("independentAgency", False"),
        })

        # Update Schema.org data with enriched information
        entity["schema_org_data"] = self._generate_enriched_schema_org(org)

        # Update entity text to official name
        entity["text"] = org["officialName"]
        if org.get("acronym"):
            entity["display_text"] = f"{org['officialName']} ({org['acronym']})"
        else:
            entity["display_text"] = org["officialName"]

        return entity

    def _find_matching_org(self, entity_text: str) -> Optional[Dict[str, Any]]:
        """
        Find matching organization in the database

        Returns:
            Dict with match_type, confidence, and matched org data
        """
        # Load government organizations if not cached
        if not self._cache:
            self._load_government_orgs()

        entity_lower = entity_text.lower().strip()

        # Try exact match on official name
        for org in self._cache.values():
            if org["officialName"].lower() == entity_lower:
                return {
                    "match_type": "exact_name",
                    "confidence": 1.0,
                    "data": org
                }

        # Try exact match on acronym
        for org in self._cache.values():
            if org.get("acronym") and org["acronym"].lower() == entity_lower:
                return {
                    "match_type": "exact_acronym",
                    "confidence": 1.0,
                    "data": org
                }

        # Try fuzzy match on official name
        best_match = None
        best_similarity = 0.0

        for org in self._cache.values():
            similarity = self._calculate_similarity(entity_lower, org["officialName"].lower())
            if similarity > best_similarity and similarity >= 0.85:
                best_similarity = similarity
                best_match = org

        if best_match:
            return {
                "match_type": "fuzzy",
                "confidence": best_similarity,
                "data": best_match
            }

        # Try partial match (entity text contains or is contained in org name)
        for org in self._cache.values():
            org_name_lower = org["officialName"].lower()
            if entity_lower in org_name_lower or org_name_lower in entity_lower:
                confidence = len(entity_lower) / max(len(entity_lower), len(org_name_lower))
                if confidence >= 0.6:
                    return {
                        "match_type": "partial",
                        "confidence": confidence,
                        "data": org
                    }

        return None

    def _get_suggestions(self, entity_text: str, limit: int = 3) -> List[str]:
        """Get suggested organization names for unmatched entities"""
        if not self._cache:
            self._load_government_orgs()

        entity_lower = entity_text.lower()
        suggestions = []

        # Calculate similarity for all organizations
        for org in self._cache.values():
            similarity = self._calculate_similarity(entity_lower, org["officialName"].lower())
            if similarity > 0.4:  # Lower threshold for suggestions
                suggestions.append((org["officialName"], similarity))

        # Sort by similarity and return top suggestions
        suggestions.sort(key=lambda x: x[1], reverse=True)
        return [name for name, _ in suggestions[:limit]]

    def _load_government_orgs(self):
        """Load government organizations from backend API"""
        try:
            response = requests.get(
                f"{self.backend_url}/api/government-organizations",
                timeout=5
            )
            response.raise_for_status()

            data = response.json()
            orgs = data.get("content", [])

            # Cache by ID
            self._cache = {org["id"]: org for org in orgs}

        except Exception as e:
            print(f"Warning: Failed to load government organizations: {e}")
            self._cache = {}

    @staticmethod
    def _calculate_similarity(text1: str, text2: str) -> float:
        """Calculate similarity between two strings using SequenceMatcher"""
        return SequenceMatcher(None, text1, text2).ratio()

    @staticmethod
    def _generate_enriched_schema_org(org: Dict[str, Any]) -> Dict[str, Any]:
        """Generate enriched Schema.org JSON-LD for government organization"""
        schema_org = {
            "@context": "https://schema.org",
            "@type": "GovernmentOrganization",
            "@id": f"https://newsanalyzer.org/government-orgs/{org['id']}",
            "name": org["officialName"],
        }

        if org.get("acronym"):
            schema_org["alternateName"] = org["acronym"]

        if org.get("websiteUrl"):
            schema_org["url"] = org["websiteUrl"]

        if org.get("establishedDate"):
            schema_org["foundingDate"] = org["establishedDate"]

        if org.get("dissolvedDate"):
            schema_org["dissolutionDate"] = org["dissolvedDate"]

        if org.get("jurisdictionAreas"):
            schema_org["areaServed"] = {
                "@type": "AdministrativeArea",
                "name": ", ".join(org["jurisdictionAreas"])
            }

        # Add government-specific properties
        schema_org["additionalType"] = f"https://newsanalyzer.org/types/{org['orgType']}"
        schema_org["branch"] = org["branch"]

        return schema_org


# Singleton instance
_validator_instance: Optional[GovernmentOrgValidator] = None


def get_gov_org_validator() -> GovernmentOrgValidator:
    """
    Get or create singleton GovernmentOrgValidator instance

    Returns:
        GovernmentOrgValidator instance
    """
    global _validator_instance
    if _validator_instance is None:
        _validator_instance = GovernmentOrgValidator()
    return _validator_instance
