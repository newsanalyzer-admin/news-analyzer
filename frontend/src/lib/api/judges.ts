/**
 * Judges API Client
 *
 * Client for interacting with the Java backend /api/judges endpoints.
 */

import { backendClient } from './client';
import type { Judge, JudgeStats, CourtLevel, Circuit } from '@/types/judge';
import type { Page, PaginationParams } from '@/types/pagination';

/**
 * Judge list parameters
 */
export interface JudgeListParams extends PaginationParams {
  courtLevel?: CourtLevel | string;
  circuit?: Circuit | string;
  status?: string;
  search?: string;
  sortBy?: string;
  sortDir?: 'asc' | 'desc';
}

/**
 * Judges API client
 */
export const judgesApi = {
  /**
   * List judges with pagination and filters
   * GET /api/judges
   */
  list: async (params: JudgeListParams = {}): Promise<Page<Judge>> => {
    const response = await backendClient.get<Page<Judge>>('/api/judges', { params });
    return response.data;
  },

  /**
   * Get judge by ID
   * GET /api/judges/{id}
   */
  getById: async (id: string): Promise<Judge> => {
    const response = await backendClient.get<Judge>(`/api/judges/${id}`);
    return response.data;
  },

  /**
   * Search judges by name
   * GET /api/judges/search
   */
  search: async (q: string): Promise<Judge[]> => {
    const response = await backendClient.get<Judge[]>('/api/judges/search', {
      params: { q },
    });
    return response.data;
  },

  /**
   * Get judge statistics
   * GET /api/judges/stats
   */
  getStats: async (): Promise<JudgeStats> => {
    const response = await backendClient.get<JudgeStats>('/api/judges/stats');
    return response.data;
  },
};
