/**
 * Members API Client
 *
 * Client for interacting with the Java backend /api/members endpoints.
 */

import { backendClient } from './client';
import type {
  Member,
  Person,
  PositionHolding,
  PartyStats,
  StateStats,
  EnrichmentStatus,
  Chamber,
} from '@/types/member';
import type { CommitteeMembership } from '@/types/committee';
import type { Page, PaginationParams } from '@/types/pagination';
import type { SyncJobStatus } from '@/types/sync';

/**
 * Member list parameters
 */
export interface MemberListParams extends PaginationParams {
  chamber?: Chamber;
  state?: string;
  party?: string;
}

/**
 * Members API client
 */
export const membersApi = {
  /**
   * List members with pagination
   * GET /api/members
   */
  list: async (params: MemberListParams = {}): Promise<Page<Member>> => {
    const response = await backendClient.get<Page<Member>>('/api/members', { params });
    return response.data;
  },

  /**
   * Get member by bioguide ID
   * GET /api/members/{bioguideId}
   */
  getByBioguideId: async (bioguideId: string): Promise<Member> => {
    const response = await backendClient.get<Member>(`/api/members/${bioguideId}`);
    return response.data;
  },

  /**
   * Search members by name
   * GET /api/members/search
   */
  search: async (
    name: string,
    params: PaginationParams = {}
  ): Promise<Page<Member>> => {
    const response = await backendClient.get<Page<Member>>('/api/members/search', {
      params: { name, ...params },
    });
    return response.data;
  },

  /**
   * Get members by state
   * GET /api/members/by-state/{state}
   */
  getByState: async (
    state: string,
    params: PaginationParams = {}
  ): Promise<Page<Member>> => {
    const response = await backendClient.get<Page<Member>>(
      `/api/members/by-state/${state}`,
      { params }
    );
    return response.data;
  },

  /**
   * Get members by chamber
   * GET /api/members/by-chamber/{chamber}
   */
  getByChamber: async (
    chamber: Chamber,
    params: PaginationParams = {}
  ): Promise<Page<Member>> => {
    const response = await backendClient.get<Page<Member>>(
      `/api/members/by-chamber/${chamber}`,
      { params }
    );
    return response.data;
  },

  /**
   * Get member's term history
   * GET /api/members/{bioguideId}/terms
   */
  getTerms: async (bioguideId: string): Promise<PositionHolding[]> => {
    const response = await backendClient.get<PositionHolding[]>(
      `/api/members/${bioguideId}/terms`
    );
    return response.data;
  },

  /**
   * Get member's committee assignments
   * GET /api/members/{bioguideId}/committees
   */
  getCommittees: async (
    bioguideId: string,
    params: PaginationParams = {}
  ): Promise<Page<CommitteeMembership>> => {
    const response = await backendClient.get<Page<CommitteeMembership>>(
      `/api/members/${bioguideId}/committees`,
      { params }
    );
    return response.data;
  },

  /**
   * Get total member count
   * GET /api/members/count
   */
  getCount: async (): Promise<number> => {
    const response = await backendClient.get<number>('/api/members/count');
    return response.data;
  },

  /**
   * Get party distribution statistics
   * GET /api/members/stats/party
   */
  getPartyStats: async (): Promise<PartyStats[]> => {
    const response = await backendClient.get<PartyStats[]>('/api/members/stats/party');
    return response.data;
  },

  /**
   * Get state distribution statistics
   * GET /api/members/stats/state
   */
  getStateStats: async (): Promise<StateStats[]> => {
    const response = await backendClient.get<StateStats[]>('/api/members/stats/state');
    return response.data;
  },

  /**
   * Trigger member sync from Congress.gov (async — returns job status)
   * POST /api/members/sync → 202 Accepted
   */
  triggerSync: async (): Promise<SyncJobStatus> => {
    const response = await backendClient.post<SyncJobStatus>('/api/members/sync');
    return response.data;
  },

  /**
   * Trigger enrichment sync from legislators repo (async — returns job status)
   * POST /api/members/enrichment-sync → 202 Accepted
   */
  triggerEnrichmentSync: async (force: boolean = false): Promise<SyncJobStatus> => {
    const response = await backendClient.post<SyncJobStatus>('/api/members/enrichment-sync', null, {
      params: { force },
    });
    return response.data;
  },

  /**
   * Get enrichment status
   * GET /api/members/enrichment-status
   */
  getEnrichmentStatus: async (): Promise<EnrichmentStatus> => {
    const response = await backendClient.get<EnrichmentStatus>(
      '/api/members/enrichment-status'
    );
    return response.data;
  },
};
