/**
 * Appointees React Query Hooks
 *
 * React Query hooks for appointee-related API calls.
 */

import { useQuery } from '@tanstack/react-query';
import { appointeesApi, type AppointeeListParams } from '@/lib/api/appointees';
import type { AppointmentType } from '@/types/appointee';

/**
 * Query key factory for appointees
 */
export const appointeeKeys = {
  all: ['appointees'] as const,
  lists: () => [...appointeeKeys.all, 'list'] as const,
  list: (params: AppointeeListParams) => [...appointeeKeys.lists(), params] as const,
  details: () => [...appointeeKeys.all, 'detail'] as const,
  detail: (id: string) => [...appointeeKeys.details(), id] as const,
  search: (query: string, limit?: number) =>
    [...appointeeKeys.all, 'search', { query, limit }] as const,
  byAgency: (orgId: string) => [...appointeeKeys.all, 'by-agency', orgId] as const,
  byType: (type: AppointmentType) => [...appointeeKeys.all, 'by-type', type] as const,
  cabinet: () => [...appointeeKeys.all, 'cabinet'] as const,
  count: () => [...appointeeKeys.all, 'count'] as const,
};

/**
 * Hook to fetch paginated list of appointees
 */
export function useAppointees(params: AppointeeListParams = {}) {
  return useQuery({
    queryKey: appointeeKeys.list(params),
    queryFn: () => appointeesApi.list(params),
  });
}

/**
 * Hook to fetch a single appointee by ID
 */
export function useAppointee(id: string) {
  return useQuery({
    queryKey: appointeeKeys.detail(id),
    queryFn: () => appointeesApi.getById(id),
    enabled: !!id,
  });
}

/**
 * Hook to search appointees by name or position title
 */
export function useAppointeeSearch(query: string, limit: number = 20) {
  return useQuery({
    queryKey: appointeeKeys.search(query, limit),
    queryFn: () => appointeesApi.search(query, limit),
    enabled: !!query && query.length >= 2,
  });
}

/**
 * Hook to fetch appointees by agency
 */
export function useAppointeesByAgency(orgId: string) {
  return useQuery({
    queryKey: appointeeKeys.byAgency(orgId),
    queryFn: () => appointeesApi.getByAgency(orgId),
    enabled: !!orgId,
  });
}

/**
 * Hook to fetch appointees by type
 */
export function useAppointeesByType(type: AppointmentType) {
  return useQuery({
    queryKey: appointeeKeys.byType(type),
    queryFn: () => appointeesApi.getByType(type),
    enabled: !!type,
  });
}

/**
 * Hook to fetch Cabinet members
 */
export function useCabinetMembers() {
  return useQuery({
    queryKey: appointeeKeys.cabinet(),
    queryFn: () => appointeesApi.getCabinet(),
  });
}

/**
 * Hook to fetch appointee count
 */
export function useAppointeeCount() {
  return useQuery({
    queryKey: appointeeKeys.count(),
    queryFn: () => appointeesApi.getCount(),
  });
}
