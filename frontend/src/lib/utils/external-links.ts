/**
 * External ID URL Builders
 *
 * Constants and utilities for building URLs to external resources.
 */

/**
 * URL builders for external resources by ID type
 */
export const externalIdUrls = {
  fec: (id: string) => `https://www.fec.gov/data/candidate/${id}/`,
  govtrack: (id: number) => `https://www.govtrack.us/congress/members/${id}`,
  opensecrets: (id: string) => `https://www.opensecrets.org/members-of-congress/summary?cid=${id}`,
  votesmart: (id: number) => `https://votesmart.org/candidate/${id}`,
  wikipedia: (title: string) => `https://en.wikipedia.org/wiki/${encodeURIComponent(title)}`,
  ballotpedia: (name: string) => `https://ballotpedia.org/${encodeURIComponent(name)}`,
  cspan: (id: number) => `https://www.c-span.org/person/?${id}`,
  bioguide: (id: string) => `https://bioguide.congress.gov/search/bio/${id}`,
};

/**
 * Display labels for external ID types
 */
export const externalIdLabels: Record<string, string> = {
  fec: 'FEC',
  govtrack: 'GovTrack',
  opensecrets: 'OpenSecrets',
  votesmart: 'VoteSmart',
  wikipedia: 'Wikipedia',
  ballotpedia: 'Ballotpedia',
  cspan: 'C-SPAN',
  bioguide: 'BioGuide',
  icpsr: 'ICPSR',
  thomas: 'THOMAS',
  lis: 'LIS',
  house_history: 'House History',
  maplight: 'MapLight',
};

/**
 * Supported external ID types that have URL builders
 */
export const supportedExternalIdTypes = [
  'bioguide',
  'fec',
  'govtrack',
  'opensecrets',
  'votesmart',
  'wikipedia',
  'ballotpedia',
  'cspan',
] as const;

export type SupportedExternalIdType = typeof supportedExternalIdTypes[number];

/**
 * Check if an external ID type is supported for linking
 */
export function isSupported(type: string): type is SupportedExternalIdType {
  return supportedExternalIdTypes.includes(type as SupportedExternalIdType);
}

/**
 * Get URL for an external ID
 */
export function getExternalUrl(type: string, value: unknown): string | undefined {
  if (!value) return undefined;

  switch (type) {
    case 'bioguide':
      return typeof value === 'string' ? externalIdUrls.bioguide(value) : undefined;
    case 'fec':
      // FEC IDs can be arrays - use first one
      if (Array.isArray(value) && value.length > 0) {
        return externalIdUrls.fec(String(value[0]));
      }
      return typeof value === 'string' ? externalIdUrls.fec(value) : undefined;
    case 'govtrack':
      return typeof value === 'number' ? externalIdUrls.govtrack(value) : undefined;
    case 'opensecrets':
      return typeof value === 'string' ? externalIdUrls.opensecrets(value) : undefined;
    case 'votesmart':
      return typeof value === 'number' ? externalIdUrls.votesmart(value) : undefined;
    case 'wikipedia':
      return typeof value === 'string' ? externalIdUrls.wikipedia(value) : undefined;
    case 'ballotpedia':
      return typeof value === 'string' ? externalIdUrls.ballotpedia(value) : undefined;
    case 'cspan':
      return typeof value === 'number' ? externalIdUrls.cspan(value) : undefined;
    default:
      return undefined;
  }
}

/**
 * Format external ID value for display
 */
export function formatExternalIdValue(type: string, value: unknown): string {
  if (Array.isArray(value)) {
    return value.join(', ');
  }
  return String(value);
}
