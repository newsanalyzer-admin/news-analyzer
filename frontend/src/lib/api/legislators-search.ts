/**
 * Legislators Repo Search API Client
 *
 * Client for searching the unitedstates/congress-legislators GitHub repository
 * and enriching Person records via the backend proxy.
 */

import axios from 'axios';
import type {
  LegislatorsSearchParams,
  LegislatorsSearchResponse,
  LegislatorSearchResult,
  LegislatorDetail,
  LegislatorEnrichmentRequest,
  LegislatorEnrichmentResult,
  EnrichmentPreview,
  LegislatorExistsResponse,
} from '@/types/legislators-search';

const BACKEND_URL = process.env.NEXT_PUBLIC_BACKEND_URL || 'http://localhost:8080';

const api = axios.create({
  baseURL: BACKEND_URL,
  timeout: 30000, // GitHub can be slow on first fetch
});

/**
 * Legislators Repo API client
 */
export const legislatorsSearchApi = {
  /**
   * Search legislators in the Legislators Repo
   * GET /api/admin/search/legislators
   */
  searchLegislators: async (
    params: LegislatorsSearchParams
  ): Promise<LegislatorsSearchResponse> => {
    const response = await api.get<LegislatorsSearchResponse>(
      '/api/admin/search/legislators',
      {
        params: {
          name: params.name || undefined,
          bioguideId: params.bioguideId || undefined,
          state: params.state || undefined,
          page: params.page || 1,
          pageSize: params.pageSize || 20,
        },
      }
    );
    return response.data;
  },

  /**
   * Get legislator detail by BioGuide ID
   * GET /api/admin/search/legislators/{bioguideId}
   */
  getLegislatorDetail: async (bioguideId: string): Promise<LegislatorDetail> => {
    const response = await api.get<LegislatorDetail>(
      `/api/admin/search/legislators/${bioguideId}`
    );
    return response.data;
  },

  /**
   * Preview enrichment for a Person record
   * GET /api/admin/import/legislators/{bioguideId}/preview
   */
  previewEnrichment: async (bioguideId: string): Promise<EnrichmentPreview> => {
    const response = await api.get<EnrichmentPreview>(
      `/api/admin/import/legislators/${bioguideId}/preview`
    );
    return response.data;
  },

  /**
   * Enrich a Person record from Legislators Repo
   * POST /api/admin/import/legislators/enrich
   */
  enrichPerson: async (
    request: LegislatorEnrichmentRequest
  ): Promise<LegislatorEnrichmentResult> => {
    const response = await api.post<LegislatorEnrichmentResult>(
      '/api/admin/import/legislators/enrich',
      request
    );
    return response.data;
  },

  /**
   * Check if legislator exists in repo and/or locally
   * GET /api/admin/import/legislators/{bioguideId}/exists
   */
  checkLegislatorExists: async (
    bioguideId: string
  ): Promise<LegislatorExistsResponse> => {
    const response = await api.get<LegislatorExistsResponse>(
      `/api/admin/import/legislators/${bioguideId}/exists`
    );
    return response.data;
  },
};

/**
 * Helper to convert LegislatorSearchResult to a format compatible with SearchImportPanel
 */
export function toSearchResult(
  legislator: LegislatorSearchResult,
  localMatchId?: string
) {
  return {
    data: legislator,
    source: 'Legislators Repo',
    sourceUrl: `https://github.com/unitedstates/congress-legislators/blob/main/legislators-current.yaml`,
    // Note: localMatchId is for enrichment matching, different from duplicateId which is for conflict detection
    duplicateId: localMatchId,
  };
}

/**
 * Format chamber value for display
 */
export function formatChamber(chamber: string): string {
  switch (chamber.toLowerCase()) {
    case 'senate':
      return 'Senate';
    case 'house':
      return 'House';
    case 'sen':
      return 'Senate';
    case 'rep':
      return 'House';
    default:
      return chamber;
  }
}

/**
 * Format party value for display
 */
export function formatParty(party: string): string {
  switch (party.toLowerCase()) {
    case 'd':
    case 'democrat':
      return 'Democrat';
    case 'r':
    case 'republican':
      return 'Republican';
    case 'i':
    case 'independent':
      return 'Independent';
    default:
      return party;
  }
}

/**
 * Get party color for badge styling
 */
export function getPartyColor(party: string): string {
  switch (party.toLowerCase()) {
    case 'd':
    case 'democrat':
      return 'bg-blue-100 text-blue-800';
    case 'r':
    case 'republican':
      return 'bg-red-100 text-red-800';
    case 'i':
    case 'independent':
      return 'bg-purple-100 text-purple-800';
    default:
      return 'bg-gray-100 text-gray-800';
  }
}
