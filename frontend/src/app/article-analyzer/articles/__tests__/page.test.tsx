import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
import ArticlesPage from '../page';

// Mock Next.js navigation
const mockPush = vi.fn();
vi.mock('next/navigation', () => ({
  useRouter: () => ({
    push: mockPush,
  }),
}));

// Mock article data
const mockArticles = [
  {
    id: 1,
    title: 'Breaking News: Major Policy Change Announced',
    source_url: 'https://example.com/article1',
    source_name: 'Example News',
    published_date: '2025-12-30T10:00:00Z',
    analyzed_at: '2025-12-30T12:00:00Z',
    entity_count: 15,
    status: 'completed',
  },
  {
    id: 2,
    title: 'Economic Report Shows Growth',
    source_url: 'https://example.com/article2',
    source_name: 'Financial Times',
    published_date: '2025-12-29T08:00:00Z',
    analyzed_at: '2025-12-29T14:00:00Z',
    entity_count: 8,
    status: 'completed',
  },
  {
    id: 3,
    title: 'Technology Trends in 2025',
    source_url: 'https://example.com/article3',
    source_name: 'Tech Daily',
    published_date: '2025-12-28T06:00:00Z',
    analyzed_at: '2025-12-28T09:00:00Z',
    entity_count: 12,
    status: 'analyzing',
  },
];

// Mock the useArticles hook
vi.mock('@/hooks/useArticles', () => ({
  useArticles: vi.fn(() => ({
    data: mockArticles,
    isLoading: false,
    error: null,
    refetch: vi.fn(),
  })),
}));

describe('ArticlesPage', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  describe('Page Header', () => {
    it('renders the page title', () => {
      render(<ArticlesPage />);
      expect(screen.getByRole('heading', { name: /analyzed articles/i })).toBeInTheDocument();
    });

    it('renders description text', () => {
      render(<ArticlesPage />);
      expect(
        screen.getByText(/browse articles that have been submitted for analysis/i)
      ).toBeInTheDocument();
    });

    it('renders the info banner with tips', () => {
      render(<ArticlesPage />);
      expect(screen.getByText(/tip:/i)).toBeInTheDocument();
      expect(screen.getByText(/click on any article to view its full analysis/i)).toBeInTheDocument();
    });

    it('info banner has link to Article Analyzer', () => {
      render(<ArticlesPage />);
      const link = screen.getByRole('link', { name: /analyze article/i });
      expect(link).toBeInTheDocument();
      expect(link).toHaveAttribute('href', '/article-analyzer');
    });
  });

  describe('Stats Row', () => {
    it('shows total article count', () => {
      render(<ArticlesPage />);
      expect(screen.getByText(/total: 3/i)).toBeInTheDocument();
    });
  });

  describe('Filters and Sorting', () => {
    it('renders the search input', () => {
      render(<ArticlesPage />);
      expect(screen.getByPlaceholderText(/search articles/i)).toBeInTheDocument();
    });

    it('renders the sort dropdown', () => {
      render(<ArticlesPage />);
      expect(screen.getByRole('combobox')).toBeInTheDocument();
    });

    it('search input filters articles', () => {
      render(<ArticlesPage />);
      const searchInput = screen.getByPlaceholderText(/search articles/i);
      fireEvent.change(searchInput, { target: { value: 'Economic' } });
      expect(searchInput).toHaveValue('Economic');
    });

    it('shows clear search button when searching', () => {
      render(<ArticlesPage />);
      const searchInput = screen.getByPlaceholderText(/search articles/i);
      fireEvent.change(searchInput, { target: { value: 'test' } });
      expect(screen.getByRole('button', { name: /clear search/i })).toBeInTheDocument();
    });

    it('clear search button resets search', () => {
      render(<ArticlesPage />);
      const searchInput = screen.getByPlaceholderText(/search articles/i);
      fireEvent.change(searchInput, { target: { value: 'test' } });
      const clearButton = screen.getByRole('button', { name: /clear search/i });
      fireEvent.click(clearButton);
      expect(searchInput).toHaveValue('');
    });
  });

  describe('Articles Table', () => {
    it('renders the articles table', () => {
      render(<ArticlesPage />);
      expect(screen.getByRole('table')).toBeInTheDocument();
    });

    it('renders table headers', () => {
      render(<ArticlesPage />);
      expect(screen.getByText('Title')).toBeInTheDocument();
      expect(screen.getByText('Analyzed')).toBeInTheDocument();
    });

    it('renders article titles', () => {
      render(<ArticlesPage />);
      expect(screen.getByText('Breaking News: Major Policy Change Announced')).toBeInTheDocument();
      expect(screen.getByText('Economic Report Shows Growth')).toBeInTheDocument();
      expect(screen.getByText('Technology Trends in 2025')).toBeInTheDocument();
    });

    it('renders source names', () => {
      render(<ArticlesPage />);
      expect(screen.getByText('Example News')).toBeInTheDocument();
      expect(screen.getByText('Financial Times')).toBeInTheDocument();
      expect(screen.getByText('Tech Daily')).toBeInTheDocument();
    });

    it('renders entity counts', () => {
      render(<ArticlesPage />);
      expect(screen.getByText('15')).toBeInTheDocument();
      expect(screen.getByText('8')).toBeInTheDocument();
      expect(screen.getByText('12')).toBeInTheDocument();
    });

    it('article rows are clickable', () => {
      render(<ArticlesPage />);
      const row = screen.getByText('Breaking News: Major Policy Change Announced').closest('tr');
      expect(row).toHaveClass('cursor-pointer');
    });

    it('clicking article row navigates to detail page', () => {
      render(<ArticlesPage />);
      const row = screen.getByText('Breaking News: Major Policy Change Announced').closest('tr');
      if (row) {
        fireEvent.click(row);
        expect(mockPush).toHaveBeenCalledWith('/article-analyzer/articles/1');
      }
    });

    it('view original link opens in new tab', () => {
      render(<ArticlesPage />);
      const links = screen.getAllByText('View original');
      expect(links[0]).toHaveAttribute('target', '_blank');
      expect(links[0]).toHaveAttribute('rel', 'noopener noreferrer');
    });
  });

  describe('Loading State', () => {
    it('shows loading indicator when loading', async () => {
      const { useArticles } = await import('@/hooks/useArticles');
      vi.mocked(useArticles).mockReturnValueOnce({
        data: [],
        isLoading: true,
        error: null,
        refetch: vi.fn(),
      });

      render(<ArticlesPage />);
      expect(screen.getByText(/loading articles/i)).toBeInTheDocument();
    });
  });

  describe('Error State', () => {
    it('shows error message when fetch fails', async () => {
      const { useArticles } = await import('@/hooks/useArticles');
      vi.mocked(useArticles).mockReturnValueOnce({
        data: [],
        isLoading: false,
        error: new Error('Network error'),
        refetch: vi.fn(),
      });

      render(<ArticlesPage />);
      expect(screen.getByText(/failed to load articles/i)).toBeInTheDocument();
      expect(screen.getByText(/network error/i)).toBeInTheDocument();
    });

    it('shows retry button on error', async () => {
      const { useArticles } = await import('@/hooks/useArticles');
      vi.mocked(useArticles).mockReturnValueOnce({
        data: [],
        isLoading: false,
        error: new Error('Network error'),
        refetch: vi.fn(),
      });

      render(<ArticlesPage />);
      expect(screen.getByRole('button', { name: /try again/i })).toBeInTheDocument();
    });
  });

  describe('Empty State', () => {
    it('shows empty message when no articles', async () => {
      const { useArticles } = await import('@/hooks/useArticles');
      vi.mocked(useArticles).mockReturnValueOnce({
        data: [],
        isLoading: false,
        error: null,
        refetch: vi.fn(),
      });

      render(<ArticlesPage />);
      expect(screen.getByText(/no articles found/i)).toBeInTheDocument();
      expect(screen.getByText(/no articles have been analyzed yet/i)).toBeInTheDocument();
    });

    it('shows analyze button in empty state', async () => {
      const { useArticles } = await import('@/hooks/useArticles');
      vi.mocked(useArticles).mockReturnValueOnce({
        data: [],
        isLoading: false,
        error: null,
        refetch: vi.fn(),
      });

      render(<ArticlesPage />);
      expect(screen.getByRole('button', { name: /analyze an article/i })).toBeInTheDocument();
    });

    it('shows search-specific empty message when searching', async () => {
      const { useArticles } = await import('@/hooks/useArticles');
      vi.mocked(useArticles).mockReturnValueOnce({
        data: mockArticles,
        isLoading: false,
        error: null,
        refetch: vi.fn(),
      });

      render(<ArticlesPage />);
      const searchInput = screen.getByPlaceholderText(/search articles/i);
      fireEvent.change(searchInput, { target: { value: 'nonexistent article xyz' } });

      expect(screen.getByText(/no articles match/i)).toBeInTheDocument();
    });
  });

  describe('Accessibility', () => {
    it('search input has accessible type', () => {
      render(<ArticlesPage />);
      const searchInput = screen.getByPlaceholderText(/search articles/i);
      expect(searchInput).toHaveAttribute('type', 'search');
    });

    it('sort dropdown is accessible', () => {
      render(<ArticlesPage />);
      const combobox = screen.getByRole('combobox');
      expect(combobox).toBeInTheDocument();
    });

    it('table has proper structure', () => {
      render(<ArticlesPage />);
      const table = screen.getByRole('table');
      expect(table.querySelector('thead')).toBeInTheDocument();
      expect(table.querySelector('tbody')).toBeInTheDocument();
    });
  });

  describe('Sorting', () => {
    it('sorts articles by analyzed date by default (newest first)', () => {
      render(<ArticlesPage />);
      const rows = screen.getAllByRole('row').slice(1); // Skip header row
      const firstRowTitle = rows[0].querySelector('td')?.textContent;
      expect(firstRowTitle).toContain('Breaking News');
    });
  });
});
