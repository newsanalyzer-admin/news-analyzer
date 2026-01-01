import useSWR from 'swr';

const API_BASE = process.env.NEXT_PUBLIC_API_URL || '';

/**
 * Article type representing an analyzed article
 */
export interface Article {
  id: number;
  title: string;
  source_url: string;
  source_name?: string;
  published_date?: string;
  analyzed_at: string;
  entity_count?: number;
  status?: 'pending' | 'analyzing' | 'completed' | 'failed';
}

/**
 * Sort options for articles list
 */
export type ArticleSortField = 'analyzed_at' | 'published_date' | 'title';
export type SortDirection = 'asc' | 'desc';

/**
 * Fetcher function for SWR
 */
async function fetcher<T>(url: string): Promise<T> {
  const response = await fetch(url);
  if (!response.ok) {
    // If 404, return empty array (API not implemented yet)
    if (response.status === 404) {
      return [] as T;
    }
    throw new Error(`HTTP error! status: ${response.status}`);
  }
  return response.json();
}

/**
 * Hook to fetch all articles
 */
export function useArticles() {
  const { data, error, isLoading, mutate } = useSWR<Article[]>(
    `${API_BASE}/api/articles`,
    fetcher,
    {
      // Return empty array on error (API may not exist yet)
      onErrorRetry: (error, key, config, revalidate, { retryCount }) => {
        // Don't retry on 404
        if (error.message.includes('404')) return;
        // Only retry up to 3 times
        if (retryCount >= 3) return;
        // Retry after 5 seconds
        setTimeout(() => revalidate({ retryCount }), 5000);
      },
    }
  );

  return {
    data: data ?? [],
    isLoading,
    error,
    refetch: mutate,
  };
}

/**
 * Hook to fetch a single article by ID
 */
export function useArticle(id: number | null) {
  const { data, error, isLoading, mutate } = useSWR<Article>(
    id ? `${API_BASE}/api/articles/${id}` : null,
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
 * Hook to search articles by title
 */
export function useArticleSearch(query: string) {
  const { data, error, isLoading, mutate } = useSWR<Article[]>(
    query.length >= 2 ? `${API_BASE}/api/articles/search?q=${encodeURIComponent(query)}` : null,
    fetcher
  );

  return {
    data: data ?? [],
    isLoading,
    error,
    refetch: mutate,
  };
}
