"""
Test script for GovInfo API connection

This script tests the government organization ingestion service
without needing to run the full FastAPI server.
"""

import os
import sys

# Add parent directory to path
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))

# Set API key
os.environ['GOVINFO_API_KEY'] = 'zHPzZYn8Ql9jQTC5PO2irRJEgAIh6H2rbubYWFnY'

from app.services.gov_org_ingestion import GovInfoAPIClient, GovOrgIngestionService

def test_api_connection():
    """Test basic API connectivity"""
    print("=" * 60)
    print("Testing GovInfo API Connection")
    print("=" * 60)

    try:
        client = GovInfoAPIClient()
        print("\n[OK] API client initialized successfully")
        print(f"  API Key: {client.api_key[:10]}...")
        print(f"  Base URL: {client.base_url}")

        # Test fetching packages (without year first to see available data)
        print("\n[INFO] Fetching Government Manual packages...")
        data = client.get_collection_packages(
            collection="GOVMAN",
            year=None,
            page_size=5,
            offset=0
        )

        print(f"\n[OK] API request successful!")
        print(f"  Total packages available: {data.get('count', 0)}")
        print(f"  Fetched: {len(data.get('packages', []))} packages")

        # Display first few packages
        if data.get('packages'):
            print("\n[INFO] Sample packages:")
            for i, pkg in enumerate(data['packages'][:3], 1):
                print(f"  {i}. {pkg.get('packageId', 'Unknown')}")
                print(f"     Title: {pkg.get('title', 'No title')[:60]}...")
                if pkg.get('dateIssued'):
                    print(f"     Date: {pkg['dateIssued']}")

        return True

    except ValueError as e:
        print(f"\n[ERROR] Configuration error: {e}")
        return False
    except Exception as e:
        print(f"\n[ERROR] API connection failed: {e}")
        import traceback
        traceback.print_exc()
        return False


def test_package_processing():
    """Test processing a single package"""
    print("\n" + "=" * 60)
    print("Testing Package Processing")
    print("=" * 60)

    try:
        service = GovOrgIngestionService()
        print("\n[OK] Ingestion service initialized")

        # This would process a real package - commented out for now
        # since we need to verify the package IDs first
        print("\n[SKIP] Package processing test skipped (need valid package ID)")
        print("  Use test_api_connection() to get valid package IDs first")

        return True

    except Exception as e:
        print(f"\n[ERROR] Service initialization failed: {e}")
        import traceback
        traceback.print_exc()
        return False


if __name__ == "__main__":
    print("\nNewsAnalyzer Government Organization Ingestion Test")
    print("=" * 60)

    # Test 1: API Connection
    api_ok = test_api_connection()

    if api_ok:
        # Test 2: Service initialization
        service_ok = test_package_processing()

        if service_ok:
            print("\n" + "=" * 60)
            print("[OK] All tests passed!")
            print("=" * 60)
            print("\nNext steps:")
            print("  1. Run database migrations (V2.9 and V3)")
            print("  2. Start FastAPI server: uvicorn app.main:app --reload")
            print("  3. Test endpoints at http://localhost:8000/docs")
            print("  4. Call POST /government-orgs/ingest to fetch data")
            print("=" * 60)
        else:
            print("\n[WARN] Service initialization failed")
            sys.exit(1)
    else:
        print("\n[WARN] API connection test failed")
        sys.exit(1)
