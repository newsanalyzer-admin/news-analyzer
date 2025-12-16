/**
 * useSearchImport Hook
 *
 * React Query hook for searching external APIs with debounced input.
 * Supports pagination and dynamic filters.
 */

import { useQuery } from '@tanstack/react-query';
import { useDebounce } from './useDebounce';
import type {
  SearchResponse,
  SearchQueryParams,
  FilterValues,
} from '@/types/search-import';

const API_BASE = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080';

/**
 * Query key factory for search imports
 */
export const searchImportKeys = {
  all: ['search-import'] as const,
  search: (endpoint: string, params: SearchQueryParams) =>
    [...searchImportKeys.all, endpoint, params] as const,
};

/**
 * Build query string from search parameters
 */
function buildQueryString(params: SearchQueryParams): string {
  const searchParams = new URLSearchParams();

  if (params.q) {
    searchParams.set('q', params.q);
  }
  searchParams.set('page', String(params.page));
  searchParams.set('pageSize', String(params.pageSize));

  // Add filter values
  Object.entries(params.filters).forEach(([key, value]) => {
    if (value === undefined || value === '') return;

    if (Array.isArray(value)) {
      // Multi-select: join with comma
      if (value.length > 0) {
        searchParams.set(key, value.join(','));
      }
    } else if (typeof value === 'object' && 'from' in value) {
      // Date range
      if (value.from) {
        searchParams.set(`${key}From`, value.from.toISOString().split('T')[0]);
      }
      if (value.to) {
        searchParams.set(`${key}To`, value.to.toISOString().split('T')[0]);
      }
    } else {
      searchParams.set(key, String(value));
    }
  });

  return searchParams.toString();
}

/**
 * Fetch search results from the API
 */
async function fetchSearchResults<T>(
  endpoint: string,
  params: SearchQueryParams
): Promise<SearchResponse<T>> {
  const queryString = buildQueryString(params);
  const url = `${API_BASE}${endpoint}${queryString ? `?${queryString}` : ''}`;

  const response = await fetch(url);

  if (!response.ok) {
    const errorData = await response.json().catch(() => ({}));
    throw new Error(errorData.message || 'Search request failed');
  }

  return response.json();
}

/**
 * Hook options for useSearchImport
 */
export interface UseSearchImportOptions {
  /** Search endpoint (e.g., "/api/search/members") */
  endpoint: string;
  /** Search query string */
  query: string;
  /** Current page (1-indexed) */
  page?: number;
  /** Items per page */
  pageSize?: number;
  /** Filter values */
  filters?: FilterValues;
  /** Debounce delay in milliseconds */
  debounceMs?: number;
  /** Whether to enable the query */
  enabled?: boolean;
}

/**
 * Hook to search external APIs with debounced input
 */
export function useSearchImport<T>({
  endpoint,
  query,
  page = 1,
  pageSize = 10,
  filters = {},
  debounceMs = 300,
  enabled = true,
}: UseSearchImportOptions) {
  // Debounce the search query
  const debouncedQuery = useDebounce(query, debounceMs);

  // Build search params
  const searchParams: SearchQueryParams = {
    q: debouncedQuery,
    page,
    pageSize,
    filters,
  };

  // Create stable query key
  const queryKey = searchImportKeys.search(endpoint, searchParams);

  const queryResult = useQuery({
    queryKey,
    queryFn: () => fetchSearchResults<T>(endpoint, searchParams),
    enabled: enabled && debouncedQuery.length > 0,
    staleTime: 30 * 1000, // 30 seconds
    gcTime: 5 * 60 * 1000, // 5 minutes
    retry: 1,
  });

  return {
    results: queryResult.data?.results ?? [],
    total: queryResult.data?.total ?? 0,
    currentPage: queryResult.data?.page ?? page,
    pageSize: queryResult.data?.pageSize ?? pageSize,
    isLoading: queryResult.isLoading,
    isFetching: queryResult.isFetching,
    isError: queryResult.isError,
    error: queryResult.error,
    refetch: queryResult.refetch,
  };
}
