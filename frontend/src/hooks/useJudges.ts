/**
 * Judges React Query Hooks
 *
 * React Query hooks for judge-related API calls.
 */

import { useQuery } from '@tanstack/react-query';
import { judgesApi, type JudgeListParams } from '@/lib/api/judges';

/**
 * Query key factory for judges
 */
export const judgeKeys = {
  all: ['judges'] as const,
  lists: () => [...judgeKeys.all, 'list'] as const,
  list: (params: JudgeListParams) => [...judgeKeys.lists(), params] as const,
  details: () => [...judgeKeys.all, 'detail'] as const,
  detail: (id: string) => [...judgeKeys.details(), id] as const,
  search: (query: string) => [...judgeKeys.all, 'search', query] as const,
  stats: () => [...judgeKeys.all, 'stats'] as const,
};

/**
 * Hook to fetch paginated list of judges with filters
 */
export function useJudges(params: JudgeListParams = {}) {
  return useQuery({
    queryKey: judgeKeys.list(params),
    queryFn: () => judgesApi.list(params),
  });
}

/**
 * Hook to fetch a single judge by ID
 */
export function useJudge(id: string) {
  return useQuery({
    queryKey: judgeKeys.detail(id),
    queryFn: () => judgesApi.getById(id),
    enabled: !!id,
  });
}

/**
 * Hook to search judges by name
 */
export function useJudgeSearch(query: string) {
  return useQuery({
    queryKey: judgeKeys.search(query),
    queryFn: () => judgesApi.search(query),
    enabled: !!query && query.length >= 2,
  });
}

/**
 * Hook to fetch judge statistics
 */
export function useJudgeStats() {
  return useQuery({
    queryKey: judgeKeys.stats(),
    queryFn: () => judgesApi.getStats(),
  });
}
