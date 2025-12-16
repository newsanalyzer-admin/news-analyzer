"""Simple test to verify GovInfo API is accessible"""
import requests

API_KEY = "zHPzZYn8Ql9jQTC5PO2irRJEgAIh6H2rbubYWFnY"

# Test 1: List collections
print("Test 1: List all GovInfo collections")
print("=" * 60)

url = "https://api.govinfo.gov/collections"
params = {"api_key": API_KEY}

try:
    response = requests.get(url, params=params, timeout=30)
    print(f"Status Code: {response.status_code}")

    if response.ok:
        data = response.json()
        print(f"\n[OK] Found {len(data.get('collections', []))} collections:")

        for coll in data.get('collections', [])[:20]:
            code = coll.get('collectionCode', 'N/A')
            name = coll.get('collectionName', 'N/A')
            print(f"  - {code:15s} {name}")

            # Check if GOVMAN exists
            if code == 'GOVMAN':
                print(f"\n    [FOUND] Government Manual collection!")
                print(f"            Name: {name}")

    else:
        print(f"[ERROR] {response.status_code}: {response.text[:200]}")

except Exception as e:
    print(f"[ERROR] {e}")

# Test 2: Try specific package search
print("\n\nTest 2: Search for Government Manual in published collection")
print("=" * 60)

url2 = "https://api.govinfo.gov/published"
params2 = {
    "api_key": API_KEY,
    "collection": "GOVMAN",
    "pageSize": 5
}

try:
    response2 = requests.get(url2, params=params2, timeout=30)
    print(f"Status Code: {response2.status_code}")

    if response2.ok:
        data2 = response2.json()
        print(f"\n[OK] Found packages: {data2.get('count', 0)}")

        for pkg in data2.get('packages', []):
            print(f"  - {pkg.get('packageId', 'N/A')}")
            print(f"    Title: {pkg.get('title', 'N/A')[:60]}...")

    else:
        print(f"[ERROR] {response2.status_code}: {response2.text[:200]}")

except Exception as e:
    print(f"[ERROR] {e}")
