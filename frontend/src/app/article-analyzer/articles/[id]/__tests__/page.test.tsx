import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
import ArticleDetailPage from '../page';

// Mock Next.js navigation
const mockPush = vi.fn();
vi.mock('next/navigation', () => ({
  useRouter: () => ({
    push: mockPush,
  }),
  useParams: () => ({
    id: '123',
  }),
}));

// Mock the useArticle hook
vi.mock('@/hooks/useArticles', () => ({
  useArticle: vi.fn(() => ({
    data: null,
    isLoading: false,
    error: null,
    refetch: vi.fn(),
  })),
}));

describe('ArticleDetailPage', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  describe('Back Navigation', () => {
    it('renders back button', () => {
      render(<ArticleDetailPage />);
      expect(screen.getByRole('button', { name: /back to articles/i })).toBeInTheDocument();
    });

    it('back button navigates to articles list', () => {
      render(<ArticleDetailPage />);
      const backButton = screen.getByRole('button', { name: /back to articles/i });
      fireEvent.click(backButton);
      expect(mockPush).toHaveBeenCalledWith('/article-analyzer/articles');
    });
  });

  describe('Placeholder State (API not available)', () => {
    it('shows article ID in title', () => {
      render(<ArticleDetailPage />);
      expect(screen.getByRole('heading', { name: /article #123/i })).toBeInTheDocument();
    });

    it('shows coming soon message', () => {
      render(<ArticleDetailPage />);
      expect(screen.getByText(/article detail coming soon/i)).toBeInTheDocument();
    });

    it('shows Phase 4 badge', () => {
      render(<ArticleDetailPage />);
      expect(screen.getByText('Phase 4')).toBeInTheDocument();
    });

    it('lists planned features', () => {
      render(<ArticleDetailPage />);
      expect(screen.getByText(/full article text with entity highlighting/i)).toBeInTheDocument();
      expect(screen.getByText(/extracted entities linked to knowledge base/i)).toBeInTheDocument();
      expect(screen.getByText(/bias analysis results/i)).toBeInTheDocument();
    });
  });

  describe('Loading State', () => {
    it('shows loading indicator when loading', async () => {
      const { useArticle } = await import('@/hooks/useArticles');
      vi.mocked(useArticle).mockReturnValueOnce({
        data: undefined,
        isLoading: true,
        error: null,
        refetch: vi.fn(),
      });

      render(<ArticleDetailPage />);
      expect(screen.getByText(/loading article/i)).toBeInTheDocument();
    });
  });

  describe('Error State', () => {
    it('shows error message when fetch fails', async () => {
      const { useArticle } = await import('@/hooks/useArticles');
      vi.mocked(useArticle).mockReturnValueOnce({
        data: undefined,
        isLoading: false,
        error: new Error('Article not found'),
        refetch: vi.fn(),
      });

      render(<ArticleDetailPage />);
      expect(screen.getByText(/failed to load article/i)).toBeInTheDocument();
      expect(screen.getByText(/article not found/i)).toBeInTheDocument();
    });

    it('shows back button on error', async () => {
      const { useArticle } = await import('@/hooks/useArticles');
      vi.mocked(useArticle).mockReturnValueOnce({
        data: undefined,
        isLoading: false,
        error: new Error('Article not found'),
        refetch: vi.fn(),
      });

      render(<ArticleDetailPage />);
      // There are two back buttons - one in header and one in error state
      const backButtons = screen.getAllByRole('button', { name: /back to articles/i });
      expect(backButtons.length).toBeGreaterThanOrEqual(1);
    });
  });

  describe('Article Data State', () => {
    it('shows article title when data exists', async () => {
      const { useArticle } = await import('@/hooks/useArticles');
      vi.mocked(useArticle).mockReturnValueOnce({
        data: {
          id: 123,
          title: 'Test Article Title',
          source_url: 'https://example.com/article',
          source_name: 'Test Source',
          published_date: '2025-12-30T10:00:00Z',
          analyzed_at: '2025-12-30T12:00:00Z',
          entity_count: 10,
          status: 'completed',
        },
        isLoading: false,
        error: null,
        refetch: vi.fn(),
      });

      render(<ArticleDetailPage />);
      expect(screen.getByRole('heading', { name: /test article title/i })).toBeInTheDocument();
    });

    it('shows source name when data exists', async () => {
      const { useArticle } = await import('@/hooks/useArticles');
      vi.mocked(useArticle).mockReturnValueOnce({
        data: {
          id: 123,
          title: 'Test Article Title',
          source_url: 'https://example.com/article',
          source_name: 'Test Source',
          published_date: '2025-12-30T10:00:00Z',
          analyzed_at: '2025-12-30T12:00:00Z',
          entity_count: 10,
          status: 'completed',
        },
        isLoading: false,
        error: null,
        refetch: vi.fn(),
      });

      render(<ArticleDetailPage />);
      expect(screen.getByText('Test Source')).toBeInTheDocument();
    });

    it('shows entity count badge when data exists', async () => {
      const { useArticle } = await import('@/hooks/useArticles');
      vi.mocked(useArticle).mockReturnValueOnce({
        data: {
          id: 123,
          title: 'Test Article Title',
          source_url: 'https://example.com/article',
          source_name: 'Test Source',
          published_date: '2025-12-30T10:00:00Z',
          analyzed_at: '2025-12-30T12:00:00Z',
          entity_count: 10,
          status: 'completed',
        },
        isLoading: false,
        error: null,
        refetch: vi.fn(),
      });

      render(<ArticleDetailPage />);
      expect(screen.getByText('10 entities')).toBeInTheDocument();
    });
  });

  describe('Accessibility', () => {
    it('has descriptive heading', () => {
      render(<ArticleDetailPage />);
      const heading = screen.getByRole('heading', { level: 1 });
      expect(heading).toBeInTheDocument();
    });
  });
});
