import useSWR from 'swr';
import type { Entity, EntityType } from '@/types/entity';

const API_BASE = process.env.NEXT_PUBLIC_API_URL || '';

/**
 * Fetcher function for SWR
 */
async function fetcher<T>(url: string): Promise<T> {
  const response = await fetch(url);
  if (!response.ok) {
    throw new Error(`HTTP error! status: ${response.status}`);
  }
  return response.json();
}

/**
 * Hook to fetch all extracted entities
 */
export function useExtractedEntities() {
  const { data, error, isLoading, mutate } = useSWR<Entity[]>(
    `${API_BASE}/api/entities`,
    fetcher
  );

  return {
    data,
    isLoading,
    error,
    refetch: mutate,
  };
}

/**
 * Hook to fetch extracted entities by type
 */
export function useExtractedEntitiesByType(entityType: EntityType | null) {
  const { data, error, isLoading, mutate } = useSWR<Entity[]>(
    entityType ? `${API_BASE}/api/entities/type/${entityType}` : null,
    fetcher
  );

  return {
    data,
    isLoading,
    error,
    refetch: mutate,
  };
}

/**
 * Hook to search extracted entities
 */
export function useExtractedEntitySearch(query: string) {
  const { data, error, isLoading, mutate } = useSWR<Entity[]>(
    query.length >= 2 ? `${API_BASE}/api/entities/search?q=${encodeURIComponent(query)}` : null,
    fetcher
  );

  return {
    data,
    isLoading,
    error,
    refetch: mutate,
  };
}
