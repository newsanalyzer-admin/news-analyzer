/**
 * Committees API Client
 *
 * Client for interacting with the Java backend /api/committees endpoints.
 */

import { backendClient } from './client';
import type {
  Committee,
  CommitteeMembership,
  CommitteeChamber,
} from '@/types/committee';
import type { Page, PaginationParams } from '@/types/pagination';
import type { SyncJobStatus } from '@/types/sync';

/**
 * Committee list parameters
 */
export interface CommitteeListParams extends PaginationParams {
  chamber?: CommitteeChamber;
  type?: string;
}

/**
 * Committees API client
 */
export const committeesApi = {
  /**
   * List committees with pagination
   * GET /api/committees
   */
  list: async (params: CommitteeListParams = {}): Promise<Page<Committee>> => {
    const response = await backendClient.get<Page<Committee>>('/api/committees', { params });
    return response.data;
  },

  /**
   * Get committee by code
   * GET /api/committees/{code}
   */
  getByCode: async (code: string): Promise<Committee> => {
    const response = await backendClient.get<Committee>(`/api/committees/${code}`);
    return response.data;
  },

  /**
   * Get committee members
   * GET /api/committees/{code}/members
   */
  getMembers: async (
    code: string,
    params: PaginationParams = {}
  ): Promise<Page<CommitteeMembership>> => {
    const response = await backendClient.get<Page<CommitteeMembership>>(
      `/api/committees/${code}/members`,
      { params }
    );
    return response.data;
  },

  /**
   * Get committee subcommittees
   * GET /api/committees/{code}/subcommittees
   */
  getSubcommittees: async (
    code: string,
    params: PaginationParams = {}
  ): Promise<Page<Committee>> => {
    const response = await backendClient.get<Page<Committee>>(
      `/api/committees/${code}/subcommittees`,
      { params }
    );
    return response.data;
  },

  /**
   * Get committees by chamber
   * GET /api/committees/by-chamber/{chamber}
   */
  getByChamber: async (
    chamber: CommitteeChamber,
    params: PaginationParams = {}
  ): Promise<Page<Committee>> => {
    const response = await backendClient.get<Page<Committee>>(
      `/api/committees/by-chamber/${chamber}`,
      { params }
    );
    return response.data;
  },

  /**
   * Search committees by name
   * GET /api/committees/search
   */
  search: async (
    name: string,
    params: PaginationParams = {}
  ): Promise<Page<Committee>> => {
    const response = await backendClient.get<Page<Committee>>('/api/committees/search', {
      params: { name, ...params },
    });
    return response.data;
  },

  /**
   * Get total committee count
   * GET /api/committees/count
   */
  getCount: async (): Promise<number> => {
    const response = await backendClient.get<number>('/api/committees/count');
    return response.data;
  },

  /**
   * Trigger committee sync from Congress.gov (async — returns job status)
   * POST /api/committees/sync → 202 Accepted
   */
  triggerSync: async (): Promise<SyncJobStatus> => {
    const response = await backendClient.post<SyncJobStatus>('/api/committees/sync');
    return response.data;
  },

  /**
   * Trigger committee membership sync (async — returns job status)
   * POST /api/committees/sync/memberships → 202 Accepted
   */
  triggerMembershipSync: async (congress?: number): Promise<SyncJobStatus> => {
    const response = await backendClient.post<SyncJobStatus>('/api/committees/sync/memberships', null, {
      params: congress ? { congress } : undefined,
    });
    return response.data;
  },
};
