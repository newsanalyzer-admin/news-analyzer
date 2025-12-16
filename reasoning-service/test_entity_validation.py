"""
Test script for entity extraction with government organization validation
"""

import requests
import json

# Test text with various government organizations
test_text = """
The Environmental Protection Agency (EPA) announced new regulations today.
Senator Warren criticized the Department of Defense budget proposal.
The Federal Bureau of Investigation is investigating the case.
Congress passed a bill supported by the Department of Justice.
"""

# API endpoint
url = "http://localhost:8000/entities/extract"

# Make request
print("Testing entity extraction with government org validation...")
print(f"Text: {test_text}\n")

response = requests.post(
    url,
    json={
        "text": test_text,
        "confidence_threshold": 0.7
    }
)

if response.status_code == 200:
    result = response.json()

    print(f"[OK] Successfully extracted {result['total_count']} entities\n")

    # Print each entity
    for entity in result["entities"]:
        print(f"Entity: {entity['text']}")
        print(f"  Type: {entity['entity_type']}")
        print(f"  Confidence: {entity['confidence']}")

        if entity["entity_type"] == "government_org":
            if entity.get("verified"):
                print(f"  [VERIFIED] against database")
                print(f"  Match Type: {entity.get('match_type')}")
                print(f"  Match Confidence: {entity.get('match_confidence')}")
                print(f"  Official Name: {entity['properties'].get('official_name')}")
                print(f"  Acronym: {entity['properties'].get('acronym')}")
                print(f"  Org Type: {entity['properties'].get('org_type')}")
                print(f"  Branch: {entity['properties'].get('branch')}")
                print(f"  Website: {entity['properties'].get('website_url')}")
            else:
                print(f"  [NOT VERIFIED]")
                if entity.get("suggested_matches"):
                    print(f"  Suggestions: {', '.join(entity['suggested_matches'])}")

        print()

    # Print summary
    print(f"\n{'='*60}")
    print("SUMMARY:")
    print(f"Total entities: {result['total_count']}")
    print(f"Validated gov orgs: {result.get('validated_gov_orgs', 0)}")
    print(f"{'='*60}")

else:
    print(f"[ERROR] Request failed with status {response.status_code}")
    print(response.text)
