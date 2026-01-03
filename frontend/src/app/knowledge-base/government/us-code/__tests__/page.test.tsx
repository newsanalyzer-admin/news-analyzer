import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import UsCodePage from '../page';

// Mock fetch
const mockFetch = vi.fn();
global.fetch = mockFetch;

// Mock UsCodeTreeView component
vi.mock('@/components/admin/UsCodeTreeView', () => ({
  UsCodeTreeView: ({ titleNumber, titleName }: { titleNumber: number; titleName: string }) => (
    <div data-testid="uscode-tree-view">
      <span>Tree View for Title {titleNumber}: {titleName}</span>
    </div>
  ),
}));

// Wrapper with QueryClient for tests
function TestWrapper({ children }: { children: React.ReactNode }) {
  const queryClient = new QueryClient({
    defaultOptions: {
      queries: {
        retry: false,
      },
    },
  });
  return <QueryClientProvider client={queryClient}>{children}</QueryClientProvider>;
}

const mockTitles = [
  { titleNumber: 1, titleName: 'General Provisions', sectionCount: 150 },
  { titleNumber: 5, titleName: 'Government Organization and Employees', sectionCount: 2500 },
  { titleNumber: 18, titleName: 'Crimes and Criminal Procedure', sectionCount: 3200 },
];

describe('UsCodePage', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  describe('Rendering', () => {
    it('renders page with correct heading', async () => {
      mockFetch.mockResolvedValueOnce({
        ok: true,
        json: () => Promise.resolve([]),
      });

      render(
        <TestWrapper>
          <UsCodePage />
        </TestWrapper>
      );

      expect(screen.getByRole('heading', { name: /u\.s\. code/i })).toBeInTheDocument();
    });

    it('renders page description', async () => {
      mockFetch.mockResolvedValueOnce({
        ok: true,
        json: () => Promise.resolve([]),
      });

      render(
        <TestWrapper>
          <UsCodePage />
        </TestWrapper>
      );

      expect(
        screen.getByText(/official codification of federal statutory law/i)
      ).toBeInTheDocument();
    });

    it('renders external links to official sources', async () => {
      mockFetch.mockResolvedValueOnce({
        ok: true,
        json: () => Promise.resolve([]),
      });

      render(
        <TestWrapper>
          <UsCodePage />
        </TestWrapper>
      );

      await waitFor(() => {
        expect(screen.queryByText(/loading/i)).not.toBeInTheDocument();
      });

      const olrcLink = screen.getByRole('link', { name: /office of the law revision counsel/i });
      expect(olrcLink).toHaveAttribute('href', 'https://uscode.house.gov/');
      expect(olrcLink).toHaveAttribute('rel', 'noopener noreferrer');

      const aboutLink = screen.getByRole('link', { name: /about the u\.s\. code/i });
      expect(aboutLink).toHaveAttribute('href', 'https://uscode.house.gov/about.xhtml');
    });
  });

  describe('Loading State', () => {
    it('shows loading spinner while fetching', () => {
      mockFetch.mockImplementation(
        () => new Promise(() => {}) // Never resolves
      );

      render(
        <TestWrapper>
          <UsCodePage />
        </TestWrapper>
      );

      expect(screen.getByText(/loading titles/i)).toBeInTheDocument();
    });
  });

  describe('Error State', () => {
    it('shows error message when fetch fails', async () => {
      mockFetch.mockResolvedValueOnce({
        ok: false,
      });

      render(
        <TestWrapper>
          <UsCodePage />
        </TestWrapper>
      );

      await waitFor(() => {
        expect(screen.getByText(/failed to load/i)).toBeInTheDocument();
      });
    });

    it('shows retry button on error', async () => {
      mockFetch.mockResolvedValueOnce({
        ok: false,
      });

      render(
        <TestWrapper>
          <UsCodePage />
        </TestWrapper>
      );

      await waitFor(() => {
        expect(screen.getByRole('button', { name: /retry/i })).toBeInTheDocument();
      });
    });

    it('retry button refetches data', async () => {
      mockFetch
        .mockResolvedValueOnce({ ok: false })
        .mockResolvedValueOnce({
          ok: true,
          json: () => Promise.resolve(mockTitles),
        });

      render(
        <TestWrapper>
          <UsCodePage />
        </TestWrapper>
      );

      await waitFor(() => {
        expect(screen.getByRole('button', { name: /retry/i })).toBeInTheDocument();
      });

      fireEvent.click(screen.getByRole('button', { name: /retry/i }));

      await waitFor(() => {
        expect(screen.getByText('General Provisions')).toBeInTheDocument();
      });

      expect(mockFetch).toHaveBeenCalledTimes(2);
    });
  });

  describe('Empty State', () => {
    it('shows empty state when no titles imported', async () => {
      mockFetch.mockResolvedValueOnce({
        ok: true,
        json: () => Promise.resolve([]),
      });

      render(
        <TestWrapper>
          <UsCodePage />
        </TestWrapper>
      );

      await waitFor(() => {
        expect(screen.getByText(/no u\.s\. code data available/i)).toBeInTheDocument();
      });
    });

    it('empty state has link to official source', async () => {
      mockFetch.mockResolvedValueOnce({
        ok: true,
        json: () => Promise.resolve([]),
      });

      render(
        <TestWrapper>
          <UsCodePage />
        </TestWrapper>
      );

      await waitFor(() => {
        const link = screen.getByRole('link', { name: /browse the official u\.s\. code/i });
        expect(link).toHaveAttribute('href', 'https://uscode.house.gov/');
      });
    });
  });

  describe('Populated State', () => {
    it('renders list of titles', async () => {
      mockFetch.mockResolvedValueOnce({
        ok: true,
        json: () => Promise.resolve(mockTitles),
      });

      render(
        <TestWrapper>
          <UsCodePage />
        </TestWrapper>
      );

      await waitFor(() => {
        expect(screen.getByText('General Provisions')).toBeInTheDocument();
        expect(screen.getByText('Government Organization and Employees')).toBeInTheDocument();
        expect(screen.getByText('Crimes and Criminal Procedure')).toBeInTheDocument();
      });
    });

    it('shows title numbers', async () => {
      mockFetch.mockResolvedValueOnce({
        ok: true,
        json: () => Promise.resolve(mockTitles),
      });

      render(
        <TestWrapper>
          <UsCodePage />
        </TestWrapper>
      );

      await waitFor(() => {
        expect(screen.getByText('1')).toBeInTheDocument();
        expect(screen.getByText('5')).toBeInTheDocument();
        expect(screen.getByText('18')).toBeInTheDocument();
      });
    });

    it('shows section counts', async () => {
      mockFetch.mockResolvedValueOnce({
        ok: true,
        json: () => Promise.resolve(mockTitles),
      });

      render(
        <TestWrapper>
          <UsCodePage />
        </TestWrapper>
      );

      await waitFor(() => {
        expect(screen.getByText('150 sections')).toBeInTheDocument();
        expect(screen.getByText('2,500 sections')).toBeInTheDocument();
        expect(screen.getByText('3,200 sections')).toBeInTheDocument();
      });
    });
  });

  describe('Title Expansion', () => {
    it('clicking title expands it', async () => {
      mockFetch.mockResolvedValueOnce({
        ok: true,
        json: () => Promise.resolve(mockTitles),
      });

      render(
        <TestWrapper>
          <UsCodePage />
        </TestWrapper>
      );

      await waitFor(() => {
        expect(screen.getByText('General Provisions')).toBeInTheDocument();
      });

      // Initially no tree view visible
      expect(screen.queryByTestId('uscode-tree-view')).not.toBeInTheDocument();

      // Click title to expand
      fireEvent.click(screen.getByText('General Provisions'));

      // Tree view should appear
      expect(screen.getByTestId('uscode-tree-view')).toBeInTheDocument();
      expect(screen.getByText(/tree view for title 1/i)).toBeInTheDocument();
    });

    it('clicking expanded title collapses it', async () => {
      mockFetch.mockResolvedValueOnce({
        ok: true,
        json: () => Promise.resolve(mockTitles),
      });

      render(
        <TestWrapper>
          <UsCodePage />
        </TestWrapper>
      );

      await waitFor(() => {
        expect(screen.getByText('General Provisions')).toBeInTheDocument();
      });

      // Expand
      fireEvent.click(screen.getByText('General Provisions'));
      expect(screen.getByTestId('uscode-tree-view')).toBeInTheDocument();

      // Collapse
      fireEvent.click(screen.getByText('General Provisions'));
      expect(screen.queryByTestId('uscode-tree-view')).not.toBeInTheDocument();
    });

    it('only one title expanded at a time', async () => {
      mockFetch.mockResolvedValueOnce({
        ok: true,
        json: () => Promise.resolve(mockTitles),
      });

      render(
        <TestWrapper>
          <UsCodePage />
        </TestWrapper>
      );

      await waitFor(() => {
        expect(screen.getByText('General Provisions')).toBeInTheDocument();
      });

      // Expand first title
      fireEvent.click(screen.getByText('General Provisions'));
      expect(screen.getByText(/tree view for title 1/i)).toBeInTheDocument();

      // Expand second title - first should collapse
      fireEvent.click(screen.getByText('Government Organization and Employees'));
      expect(screen.getByText(/tree view for title 5/i)).toBeInTheDocument();
      expect(screen.queryByText(/tree view for title 1/i)).not.toBeInTheDocument();
    });

    it('expanded title has aria-expanded true', async () => {
      mockFetch.mockResolvedValueOnce({
        ok: true,
        json: () => Promise.resolve(mockTitles),
      });

      render(
        <TestWrapper>
          <UsCodePage />
        </TestWrapper>
      );

      await waitFor(() => {
        expect(screen.getByText('General Provisions')).toBeInTheDocument();
      });

      const titleButton = screen.getByText('General Provisions').closest('button');

      // Initially not expanded
      expect(titleButton).toHaveAttribute('aria-expanded', 'false');

      // Expand
      fireEvent.click(titleButton!);
      expect(titleButton).toHaveAttribute('aria-expanded', 'true');
    });
  });

  describe('Accessibility', () => {
    it('title list items are focusable buttons', async () => {
      mockFetch.mockResolvedValueOnce({
        ok: true,
        json: () => Promise.resolve(mockTitles),
      });

      render(
        <TestWrapper>
          <UsCodePage />
        </TestWrapper>
      );

      await waitFor(() => {
        expect(screen.getByText('General Provisions')).toBeInTheDocument();
      });

      const buttons = screen.getAllByRole('button');
      // Should have expand/collapse buttons for each title
      expect(buttons.length).toBeGreaterThanOrEqual(3);
    });

    it('external links have rel="noopener noreferrer"', async () => {
      mockFetch.mockResolvedValueOnce({
        ok: true,
        json: () => Promise.resolve([]),
      });

      render(
        <TestWrapper>
          <UsCodePage />
        </TestWrapper>
      );

      await waitFor(() => {
        const externalLinks = screen.getAllByRole('link');
        externalLinks.forEach((link) => {
          if (link.getAttribute('target') === '_blank') {
            expect(link).toHaveAttribute('rel', 'noopener noreferrer');
          }
        });
      });
    });
  });

  describe('Read-Only Behavior', () => {
    it('does not show import or upload buttons', async () => {
      mockFetch.mockResolvedValueOnce({
        ok: true,
        json: () => Promise.resolve(mockTitles),
      });

      render(
        <TestWrapper>
          <UsCodePage />
        </TestWrapper>
      );

      await waitFor(() => {
        expect(screen.getByText('General Provisions')).toBeInTheDocument();
      });

      // Should not have any import/upload buttons (text may appear in "Imported Titles" heading)
      expect(screen.queryByRole('button', { name: /import/i })).not.toBeInTheDocument();
      expect(screen.queryByRole('button', { name: /upload/i })).not.toBeInTheDocument();
    });
  });
});
