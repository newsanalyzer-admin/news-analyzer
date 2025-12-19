/**
 * Appointees API Client
 *
 * Client for interacting with the Java backend /api/appointees endpoints.
 */

import axios from 'axios';
import type { Appointee, AppointmentType } from '@/types/appointee';
import type { Page, PaginationParams } from '@/types/pagination';

const BACKEND_URL = process.env.NEXT_PUBLIC_BACKEND_URL || 'http://localhost:8080';

const api = axios.create({
  baseURL: BACKEND_URL,
  timeout: 10000,
});

/**
 * Appointee list parameters
 */
export interface AppointeeListParams extends PaginationParams {
  type?: AppointmentType;
  orgId?: string;
}

/**
 * Appointees API client
 */
export const appointeesApi = {
  /**
   * List appointees with pagination
   * GET /api/appointees
   */
  list: async (params: AppointeeListParams = {}): Promise<Page<Appointee>> => {
    const response = await api.get<Page<Appointee>>('/api/appointees', { params });
    return response.data;
  },

  /**
   * Get appointee by ID
   * GET /api/appointees/{id}
   */
  getById: async (id: string): Promise<Appointee> => {
    const response = await api.get<Appointee>(`/api/appointees/${id}`);
    return response.data;
  },

  /**
   * Search appointees by name or position title
   * GET /api/appointees/search
   */
  search: async (query: string, limit: number = 20): Promise<Appointee[]> => {
    const response = await api.get<Appointee[]>('/api/appointees/search', {
      params: { q: query, limit },
    });
    return response.data;
  },

  /**
   * Get appointees by agency
   * GET /api/appointees/by-agency/{orgId}
   */
  getByAgency: async (orgId: string): Promise<Appointee[]> => {
    const response = await api.get<Appointee[]>(`/api/appointees/by-agency/${orgId}`);
    return response.data;
  },

  /**
   * Get appointees by appointment type
   * GET /api/appointees/by-type/{type}
   */
  getByType: async (type: AppointmentType): Promise<Appointee[]> => {
    const response = await api.get<Appointee[]>(`/api/appointees/by-type/${type}`);
    return response.data;
  },

  /**
   * Get Cabinet members
   * GET /api/appointees/cabinet
   */
  getCabinet: async (): Promise<Appointee[]> => {
    const response = await api.get<Appointee[]>('/api/appointees/cabinet');
    return response.data;
  },

  /**
   * Get appointee count
   * GET /api/appointees/count
   */
  getCount: async (): Promise<number> => {
    const response = await api.get<number>('/api/appointees/count');
    return response.data;
  },
};
