"""
Government Organization Ingestion Service

Fetches US Government Manual data from GovInfo API and processes it for NewsAnalyzer.

Data Source: https://api.govinfo.gov
Collection: GOVMAN (United States Government Manual)
Documentation: https://api.govinfo.gov/docs/

@author Winston (Architect Agent)
@since 2.0.0
"""

import os
import logging
import xml.etree.ElementTree as ET
from typing import Dict, List, Optional, Any
from datetime import datetime, timedelta
from pathlib import Path

import requests
from requests.adapters import HTTPAdapter
from urllib3.util.retry import Retry

logger = logging.getLogger(__name__)


class GovInfoAPIClient:
    """
    Client for GovInfo API (https://api.govinfo.gov)

    Handles:
    - API authentication
    - Rate limiting
    - Retry logic
    - Error handling
    """

    def __init__(self, api_key: Optional[str] = None):
        """
        Initialize GovInfo API client

        Args:
            api_key: GovInfo API key. If None, reads from environment variable GOVINFO_API_KEY
        """
        self.api_key = api_key or os.getenv('GOVINFO_API_KEY')
        if not self.api_key:
            raise ValueError(
                "GovInfo API key required. "
                "Set GOVINFO_API_KEY environment variable or pass to constructor."
            )

        self.base_url = "https://api.govinfo.gov"
        self.session = self._create_session()

        # Rate limits (demo key: 1000/hour, production: 10000/hour)
        self.rate_limit = 1000  # requests per hour
        self.request_delay = 3.6  # seconds between requests (to stay under limit)

        logger.info("GovInfo API client initialized")

    def _create_session(self) -> requests.Session:
        """Create requests session with retry logic"""
        session = requests.Session()

        # Retry strategy: 3 retries with exponential backoff
        retry_strategy = Retry(
            total=3,
            backoff_factor=1,
            status_forcelist=[429, 500, 502, 503, 504],
            allowed_methods=["GET", "POST"]
        )

        adapter = HTTPAdapter(max_retries=retry_strategy)
        session.mount("http://", adapter)
        session.mount("https://", adapter)

        # Set headers (note: API key is passed as query param, not header)
        session.headers.update({
            'User-Agent': 'NewsAnalyzer/2.0 (Government Organization Ingestion)',
            'Accept': 'application/json'
        })

        return session

    def get_collection_packages(
        self,
        collection: str = "GOVMAN",
        year: Optional[int] = None,
        page_size: int = 100,
        offset: int = 0
    ) -> Dict[str, Any]:
        """
        Get packages from a GovInfo collection

        Args:
            collection: Collection code (default: GOVMAN)
            year: Filter by year (e.g., 2024)
            page_size: Number of results per page (max 100)
            offset: Pagination offset

        Returns:
            Dict with packages and metadata
        """
        if year:
            url = f"{self.base_url}/collections/{collection}/{year}"
        else:
            url = f"{self.base_url}/collections/{collection}"

        params = {
            'api_key': self.api_key,
            'pageSize': min(page_size, 100),
            'offset': offset
        }

        logger.info(f"Fetching {collection} packages (year={year}, offset={offset})")

        try:
            response = self.session.get(url, params=params, timeout=30)
            response.raise_for_status()

            data = response.json()
            logger.info(
                f"Retrieved {len(data.get('packages', []))} packages "
                f"(total: {data.get('count', 0)})"
            )
            return data

        except requests.exceptions.RequestException as e:
            logger.error(f"Error fetching packages: {e}")
            raise

    def get_package_summary(self, package_id: str) -> Dict[str, Any]:
        """
        Get summary information for a package

        Args:
            package_id: GovInfo package ID

        Returns:
            Package summary metadata
        """
        url = f"{self.base_url}/packages/{package_id}/summary"

        logger.debug(f"Fetching package summary: {package_id}")

        try:
            response = self.session.get(url, params={'api_key': self.api_key}, timeout=30)
            response.raise_for_status()
            return response.json()

        except requests.exceptions.RequestException as e:
            logger.error(f"Error fetching package summary {package_id}: {e}")
            raise

    def get_package_xml(self, package_id: str) -> str:
        """
        Get XML content for a package

        Args:
            package_id: GovInfo package ID

        Returns:
            Raw XML string
        """
        url = f"{self.base_url}/packages/{package_id}/xml"

        logger.debug(f"Fetching package XML: {package_id}")

        try:
            response = self.session.get(url, params={'api_key': self.api_key}, timeout=60)
            response.raise_for_status()
            return response.text

        except requests.exceptions.RequestException as e:
            logger.error(f"Error fetching package XML {package_id}: {e}")
            raise

    def get_package_json(self, package_id: str) -> Dict[str, Any]:
        """
        Get JSON representation of package (if available)

        Args:
            package_id: GovInfo package ID

        Returns:
            Package data as JSON
        """
        url = f"{self.base_url}/packages/{package_id}/granules"

        logger.debug(f"Fetching package granules: {package_id}")

        params = {'api_key': self.api_key}

        try:
            response = self.session.get(url, params=params, timeout=30)
            response.raise_for_status()
            return response.json()

        except requests.exceptions.RequestException as e:
            logger.warning(f"Package {package_id} may not have granules: {e}")
            return {}


class GovernmentManualParser:
    """
    Parser for US Government Manual XML data

    Extracts organizational structure, agencies, departments, and metadata
    """

    def __init__(self):
        self.namespaces = {
            'uslm': 'http://xml.house.gov/schemas/uslm/1.0'
        }

    def parse_xml(self, xml_content: str) -> List[Dict[str, Any]]:
        """
        Parse Government Manual XML to extract organizations

        Args:
            xml_content: Raw XML string

        Returns:
            List of organization dictionaries
        """
        try:
            root = ET.fromstring(xml_content)
            organizations = []

            # Parse different organization types
            organizations.extend(self._parse_executive_branch(root))
            organizations.extend(self._parse_legislative_branch(root))
            organizations.extend(self._parse_judicial_branch(root))

            logger.info(f"Parsed {len(organizations)} organizations from XML")
            return organizations

        except ET.ParseError as e:
            logger.error(f"XML parsing error: {e}")
            raise

    def _parse_executive_branch(self, root: ET.Element) -> List[Dict[str, Any]]:
        """Extract executive branch organizations"""
        organizations = []

        # Find executive departments
        for dept in root.findall(".//department") + root.findall(".//DEPARTMENT"):
            org = self._parse_department(dept)
            if org:
                organizations.append(org)

        # Find independent agencies
        for agency in root.findall(".//agency") + root.findall(".//AGENCY"):
            org = self._parse_agency(agency)
            if org:
                organizations.append(org)

        return organizations

    def _parse_legislative_branch(self, root: ET.Element) -> List[Dict[str, Any]]:
        """Extract legislative branch organizations"""
        # Implementation for legislative branch parsing
        # (Congress, GAO, GPO, Library of Congress, etc.)
        return []

    def _parse_judicial_branch(self, root: ET.Element) -> List[Dict[str, Any]]:
        """Extract judicial branch organizations"""
        # Implementation for judicial branch parsing
        # (Supreme Court, Courts of Appeals, District Courts, etc.)
        return []

    def _parse_department(self, dept_element: ET.Element) -> Optional[Dict[str, Any]]:
        """
        Parse department XML element

        Args:
            dept_element: XML element for department

        Returns:
            Organization dictionary or None
        """
        try:
            org = {
                'org_type': 'department',
                'branch': 'executive',
                'org_level': 1,
                'name': self._get_text(dept_element, 'name', 'NAME'),
                'acronym': self._get_text(dept_element, 'acronym', 'ACRONYM'),
                'secretary': self._get_text(dept_element, 'secretary', 'SECRETARY'),
                'established': self._get_text(dept_element, 'established', 'ESTABLISHED'),
                'authorizing_law': self._get_text(dept_element, 'authorizingLaw', 'AUTHORIZING_LAW'),
                'website': self._get_text(dept_element, 'website', 'WEBSITE'),
                'mission': self._get_text(dept_element, 'mission', 'MISSION'),
                'description': self._get_text(dept_element, 'description', 'DESCRIPTION'),
                'bureaus': []
            }

            # Parse sub-organizations (bureaus, offices)
            for bureau in dept_element.findall(".//bureau") + dept_element.findall(".//BUREAU"):
                bureau_data = self._parse_bureau(bureau, org['name'])
                if bureau_data:
                    org['bureaus'].append(bureau_data)

            return org if org['name'] else None

        except Exception as e:
            logger.error(f"Error parsing department: {e}")
            return None

    def _parse_agency(self, agency_element: ET.Element) -> Optional[Dict[str, Any]]:
        """
        Parse independent agency XML element

        Args:
            agency_element: XML element for agency

        Returns:
            Organization dictionary or None
        """
        try:
            org = {
                'org_type': 'independent_agency',
                'branch': 'executive',
                'org_level': 1,
                'name': self._get_text(agency_element, 'name', 'NAME'),
                'acronym': self._get_text(agency_element, 'acronym', 'ACRONYM'),
                'established': self._get_text(agency_element, 'established', 'ESTABLISHED'),
                'authorizing_law': self._get_text(agency_element, 'authorizingLaw', 'AUTHORIZING_LAW'),
                'website': self._get_text(agency_element, 'website', 'WEBSITE'),
                'mission': self._get_text(agency_element, 'mission', 'MISSION'),
                'description': self._get_text(agency_element, 'description', 'DESCRIPTION'),
                'subunits': []
            }

            # Parse sub-units
            for subunit in agency_element.findall(".//subunit") + agency_element.findall(".//SUBUNIT"):
                subunit_data = self._parse_subunit(subunit, org['name'])
                if subunit_data:
                    org['subunits'].append(subunit_data)

            return org if org['name'] else None

        except Exception as e:
            logger.error(f"Error parsing agency: {e}")
            return None

    def _parse_bureau(
        self,
        bureau_element: ET.Element,
        parent_name: str
    ) -> Optional[Dict[str, Any]]:
        """Parse bureau/office XML element"""
        try:
            return {
                'org_type': 'bureau',
                'branch': 'executive',
                'org_level': 2,
                'parent': parent_name,
                'name': self._get_text(bureau_element, 'name', 'NAME'),
                'acronym': self._get_text(bureau_element, 'acronym', 'ACRONYM'),
                'established': self._get_text(bureau_element, 'established', 'ESTABLISHED'),
                'website': self._get_text(bureau_element, 'website', 'WEBSITE'),
                'mission': self._get_text(bureau_element, 'mission', 'MISSION'),
            }
        except Exception as e:
            logger.error(f"Error parsing bureau: {e}")
            return None

    def _parse_subunit(
        self,
        subunit_element: ET.Element,
        parent_name: str
    ) -> Optional[Dict[str, Any]]:
        """Parse agency sub-unit XML element"""
        try:
            return {
                'org_type': 'office',
                'branch': 'executive',
                'org_level': 2,
                'parent': parent_name,
                'name': self._get_text(subunit_element, 'name', 'NAME'),
                'acronym': self._get_text(subunit_element, 'acronym', 'ACRONYM'),
                'website': self._get_text(subunit_element, 'website', 'WEBSITE'),
            }
        except Exception as e:
            logger.error(f"Error parsing subunit: {e}")
            return None

    def _get_text(self, element: ET.Element, *tag_names: str) -> Optional[str]:
        """
        Get text from first matching tag

        Args:
            element: Parent XML element
            tag_names: Tag names to try (handles case variations)

        Returns:
            Text content or None
        """
        for tag in tag_names:
            found = element.find(f".//{tag}")
            if found is not None and found.text:
                return found.text.strip()
        return None


class SchemaOrgTransformer:
    """
    Transform government organization data to Schema.org JSON-LD format

    Maps GovInfo data to Schema.org GovernmentOrganization type
    """

    def transform(self, org_data: Dict[str, Any]) -> Dict[str, Any]:
        """
        Transform organization data to Schema.org JSON-LD

        Args:
            org_data: Parsed organization data

        Returns:
            Schema.org JSON-LD dictionary
        """
        schema_org = {
            "@context": "https://schema.org",
            "@type": "GovernmentOrganization",
            "name": org_data['name'],
            "legalName": org_data['name']
        }

        # Add alternate name (acronym)
        if org_data.get('acronym'):
            schema_org["alternateName"] = org_data['acronym']

        # Add URL
        if org_data.get('website'):
            schema_org["url"] = org_data['website']

        # Add founding date
        if org_data.get('established'):
            schema_org["foundingDate"] = self._parse_date(org_data['established'])

        # Add description
        if org_data.get('mission'):
            schema_org["description"] = org_data['mission']

        # Add parent organization
        if org_data.get('parent'):
            schema_org["parentOrganization"] = {
                "@type": "GovernmentOrganization",
                "name": org_data['parent']
            }

        # Add sub-organizations
        if org_data.get('bureaus'):
            schema_org["subOrganization"] = [
                {
                    "@type": "GovernmentOrganization",
                    "name": bureau['name'],
                    "alternateName": bureau.get('acronym')
                }
                for bureau in org_data['bureaus']
                if bureau.get('name')
            ]

        if org_data.get('subunits'):
            schema_org["subOrganization"] = [
                {
                    "@type": "GovernmentOrganization",
                    "name": subunit['name'],
                    "alternateName": subunit.get('acronym')
                }
                for subunit in org_data['subunits']
                if subunit.get('name')
            ]

        return schema_org

    def _parse_date(self, date_str: str) -> Optional[str]:
        """
        Parse date string to ISO format

        Args:
            date_str: Date string (various formats)

        Returns:
            ISO date string (YYYY-MM-DD) or original string
        """
        # Try common date formats
        formats = [
            "%Y-%m-%d",
            "%B %d, %Y",
            "%b %d, %Y",
            "%Y",
        ]

        for fmt in formats:
            try:
                dt = datetime.strptime(date_str.strip(), fmt)
                return dt.strftime("%Y-%m-%d")
            except ValueError:
                continue

        # Return original if parsing fails
        return date_str


class GovOrgIngestionService:
    """
    Main service for ingesting government organization data

    Orchestrates:
    - API fetching
    - XML parsing
    - Data transformation
    - (Database sync handled by separate component)
    """

    def __init__(self, api_key: Optional[str] = None):
        """
        Initialize ingestion service

        Args:
            api_key: GovInfo API key
        """
        self.api_client = GovInfoAPIClient(api_key)
        self.parser = GovernmentManualParser()
        self.transformer = SchemaOrgTransformer()

        logger.info("Government Organization Ingestion Service initialized")

    def fetch_government_manual(
        self,
        year: int = 2024
    ) -> Dict[str, Any]:
        """
        Fetch complete Government Manual for a year

        Args:
            year: Year of Government Manual (default: 2024)

        Returns:
            Dict with packages and metadata
        """
        logger.info(f"Fetching Government Manual for year {year}")

        # Get all packages for the year
        all_packages = []
        offset = 0
        page_size = 100

        while True:
            data = self.api_client.get_collection_packages(
                collection="GOVMAN",
                year=year,
                page_size=page_size,
                offset=offset
            )

            packages = data.get('packages', [])
            if not packages:
                break

            all_packages.extend(packages)
            offset += page_size

            # Check if we've retrieved all packages
            if offset >= data.get('count', 0):
                break

        logger.info(f"Retrieved {len(all_packages)} packages for {year}")

        return {
            'year': year,
            'packages': all_packages,
            'count': len(all_packages),
            'fetched_at': datetime.utcnow().isoformat()
        }

    def process_package(
        self,
        package_id: str
    ) -> Dict[str, Any]:
        """
        Process a single Government Manual package

        Args:
            package_id: GovInfo package ID

        Returns:
            Dict with processed organizations
        """
        logger.info(f"Processing package: {package_id}")

        # Fetch package data
        summary = self.api_client.get_package_summary(package_id)
        xml_content = self.api_client.get_package_xml(package_id)

        # Parse XML
        organizations = self.parser.parse_xml(xml_content)

        # Transform to Schema.org
        transformed = []
        for org_data in organizations:
            schema_org = self.transformer.transform(org_data)

            # Combine original and Schema.org data
            org_complete = {
                **org_data,
                'schema_org_data': schema_org,
                'govinfo_package_id': package_id,
                'govinfo_metadata': summary
            }
            transformed.append(org_complete)

        logger.info(f"Processed {len(transformed)} organizations from {package_id}")

        return {
            'package_id': package_id,
            'organizations': transformed,
            'count': len(transformed),
            'processed_at': datetime.utcnow().isoformat()
        }

    def ingest_year(
        self,
        year: int = 2024,
        save_to_file: bool = False,
        output_dir: Optional[str] = None
    ) -> Dict[str, Any]:
        """
        Ingest complete Government Manual for a year

        Args:
            year: Year to ingest
            save_to_file: Whether to save intermediate results to files
            output_dir: Directory for output files

        Returns:
            Dict with all processed organizations and metadata
        """
        logger.info(f"Starting ingestion for year {year}")
        start_time = datetime.utcnow()

        # Fetch packages
        manual_data = self.fetch_government_manual(year)
        packages = manual_data['packages']

        # Process each package
        all_organizations = []
        processed_packages = 0
        errors = []

        for package in packages:
            package_id = package.get('packageId')
            if not package_id:
                continue

            try:
                result = self.process_package(package_id)
                all_organizations.extend(result['organizations'])
                processed_packages += 1

                # Optional: save to file
                if save_to_file and output_dir:
                    self._save_package_result(result, output_dir)

            except Exception as e:
                logger.error(f"Error processing package {package_id}: {e}")
                errors.append({
                    'package_id': package_id,
                    'error': str(e)
                })

        end_time = datetime.utcnow()
        duration = (end_time - start_time).total_seconds()

        result = {
            'year': year,
            'organizations': all_organizations,
            'total_organizations': len(all_organizations),
            'packages_processed': processed_packages,
            'packages_total': len(packages),
            'errors': errors,
            'error_count': len(errors),
            'duration_seconds': duration,
            'started_at': start_time.isoformat(),
            'completed_at': end_time.isoformat()
        }

        logger.info(
            f"Ingestion complete: {len(all_organizations)} organizations "
            f"from {processed_packages} packages in {duration:.2f}s"
        )

        return result

    def _save_package_result(
        self,
        result: Dict[str, Any],
        output_dir: str
    ) -> None:
        """Save package processing result to JSON file"""
        import json

        output_path = Path(output_dir)
        output_path.mkdir(parents=True, exist_ok=True)

        filename = f"{result['package_id']}.json"
        filepath = output_path / filename

        with open(filepath, 'w', encoding='utf-8') as f:
            json.dump(result, f, indent=2, ensure_ascii=False)

        logger.debug(f"Saved result to {filepath}")


# =====================================================================
# Singleton instance
# =====================================================================

_ingestion_service: Optional[GovOrgIngestionService] = None


def get_ingestion_service(api_key: Optional[str] = None) -> GovOrgIngestionService:
    """Get or create singleton ingestion service instance"""
    global _ingestion_service

    if _ingestion_service is None:
        _ingestion_service = GovOrgIngestionService(api_key)

    return _ingestion_service
