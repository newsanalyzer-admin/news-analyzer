/**
 * Congress.gov Search API Client
 *
 * Client for searching and importing Congressional member data via the backend proxy.
 */

import axios, { AxiosResponse } from 'axios';
import type {
  CongressSearchParams,
  CongressSearchResponse,
  CongressMemberSearchResult,
  CongressMemberDetail,
  CongressMemberImportRequest,
  CongressImportResult,
} from '@/types/congress-search';
import type { Member } from '@/types/member';

const BACKEND_URL = process.env.NEXT_PUBLIC_BACKEND_URL || 'http://localhost:8080';

const api = axios.create({
  baseURL: BACKEND_URL,
  timeout: 30000, // Congress.gov can be slow
});

/**
 * Rate limit info extracted from response headers
 */
export interface RateLimitInfo {
  remaining: number | null;
  resetSeconds: number | null;
}

/**
 * Congress Search API client
 */
export const congressSearchApi = {
  /**
   * Search Congress.gov members
   * GET /api/admin/search/congress/members
   */
  searchMembers: async (
    params: CongressSearchParams
  ): Promise<{ data: CongressSearchResponse; rateLimit: RateLimitInfo }> => {
    const response: AxiosResponse<CongressSearchResponse> = await api.get(
      '/api/admin/search/congress/members',
      {
        params: {
          name: params.name || undefined,
          state: params.state || undefined,
          party: params.party || undefined,
          chamber: params.chamber || undefined,
          congress: params.congress || undefined,
          page: params.page || 1,
          pageSize: params.pageSize || 20,
        },
      }
    );

    // Extract rate limit info from headers
    const rateLimit: RateLimitInfo = {
      remaining: response.headers['x-ratelimit-remaining']
        ? parseInt(response.headers['x-ratelimit-remaining'], 10)
        : response.data.rateLimitRemaining ?? null,
      resetSeconds: response.headers['x-ratelimit-reset']
        ? parseInt(response.headers['x-ratelimit-reset'], 10)
        : response.data.rateLimitResetSeconds ?? null,
    };

    return { data: response.data, rateLimit };
  },

  /**
   * Get member details from Congress.gov
   * GET /api/admin/search/congress/members/{bioguideId}
   */
  getMemberDetail: async (bioguideId: string): Promise<CongressMemberDetail> => {
    const response = await api.get<CongressMemberDetail>(
      `/api/admin/search/congress/members/${bioguideId}`
    );
    return response.data;
  },

  /**
   * Import member from Congress.gov
   * POST /api/admin/import/congress/member
   */
  importMember: async (
    request: CongressMemberImportRequest
  ): Promise<CongressImportResult> => {
    const response = await api.post<CongressImportResult>(
      '/api/admin/import/congress/member',
      request
    );
    return response.data;
  },

  /**
   * Check if member exists locally
   * GET /api/admin/import/congress/member/{bioguideId}/exists
   */
  checkMemberExists: async (
    bioguideId: string
  ): Promise<{ exists: boolean; id: string | null; name: string | null }> => {
    const response = await api.get<{
      exists: boolean;
      id: string | null;
      name: string | null;
    }>(`/api/admin/import/congress/member/${bioguideId}/exists`);
    return response.data;
  },

  /**
   * Get existing local member by bioguide ID
   * GET /api/members/{bioguideId}
   */
  getLocalMember: async (bioguideId: string): Promise<Member | null> => {
    try {
      const response = await api.get<Member>(`/api/members/${bioguideId}`);
      return response.data;
    } catch {
      return null;
    }
  },
};

/**
 * Helper to convert CongressMemberSearchResult to a format compatible with SearchImportPanel
 */
export function toSearchResult(
  member: CongressMemberSearchResult,
  duplicateId?: string
) {
  return {
    data: member,
    source: 'Congress.gov',
    sourceUrl: member.url || `https://www.congress.gov/member/${member.bioguideId}`,
    duplicateId,
  };
}
