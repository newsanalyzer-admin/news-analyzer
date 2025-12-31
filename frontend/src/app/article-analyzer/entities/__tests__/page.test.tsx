import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import ExtractedEntitiesPage from '../page';

// Mock Next.js navigation
const mockPush = vi.fn();
const mockReplace = vi.fn();
vi.mock('next/navigation', () => ({
  useRouter: () => ({
    push: mockPush,
    replace: mockReplace,
  }),
  usePathname: () => '/article-analyzer/entities',
  useSearchParams: () => new URLSearchParams(),
}));

// Mock the hooks
const mockAllEntitiesData = [
  {
    id: 1,
    name: 'John Doe',
    entity_type: 'person',
    schema_org_type: 'Person',
    confidence_score: 0.95,
    verified: true,
  },
  {
    id: 2,
    name: 'Acme Corp',
    entity_type: 'organization',
    schema_org_type: 'Organization',
    confidence_score: 0.85,
    verified: false,
  },
  {
    id: 3,
    name: 'Department of Energy',
    entity_type: 'government_org',
    schema_org_type: 'GovernmentOrganization',
    confidence_score: 0.78,
    verified: true,
  },
];

vi.mock('@/hooks/useExtractedEntities', () => ({
  useExtractedEntities: vi.fn(() => ({
    data: mockAllEntitiesData,
    isLoading: false,
    error: null,
    refetch: vi.fn(),
  })),
  useExtractedEntitiesByType: vi.fn(() => ({
    data: null,
    isLoading: false,
    error: null,
    refetch: vi.fn(),
  })),
  useExtractedEntitySearch: vi.fn(() => ({
    data: null,
    isLoading: false,
    error: null,
    refetch: vi.fn(),
  })),
}));

describe('ExtractedEntitiesPage', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  describe('Page Header', () => {
    it('renders the page title', () => {
      render(<ExtractedEntitiesPage />);
      expect(screen.getByRole('heading', { name: /extracted entities/i })).toBeInTheDocument();
    });

    it('renders the description text', () => {
      render(<ExtractedEntitiesPage />);
      expect(
        screen.getByText(/entities extracted from analyzed articles/i)
      ).toBeInTheDocument();
    });

    it('renders the info banner with note about extracted entities', () => {
      render(<ExtractedEntitiesPage />);
      expect(screen.getByText(/note:/i)).toBeInTheDocument();
      expect(
        screen.getByText(/extracted entities may have varying confidence levels/i)
      ).toBeInTheDocument();
    });

    it('info banner has link to Knowledge Base', () => {
      render(<ExtractedEntitiesPage />);
      const kbLink = screen.getByRole('link', { name: /knowledge base/i });
      expect(kbLink).toBeInTheDocument();
      expect(kbLink).toHaveAttribute('href', '/knowledge-base');
    });
  });

  describe('Stats Row', () => {
    it('shows total entity count', () => {
      render(<ExtractedEntitiesPage />);
      expect(screen.getByText(/total: 3/i)).toBeInTheDocument();
    });

    it('shows entity counts by type', () => {
      render(<ExtractedEntitiesPage />);
      // Check that the stats badges show counts (e.g., "Person: 1")
      // These are distinct from the entity type badges in the table
      expect(screen.getByText(/person: 1/i)).toBeInTheDocument();
      expect(screen.getByText(/organization: 1/i)).toBeInTheDocument();
      expect(screen.getByText(/government: 1/i)).toBeInTheDocument();
    });
  });

  describe('Filters', () => {
    it('renders the search input', () => {
      render(<ExtractedEntitiesPage />);
      expect(screen.getByPlaceholderText(/search entities/i)).toBeInTheDocument();
    });

    it('renders the type filter select', () => {
      render(<ExtractedEntitiesPage />);
      expect(screen.getByRole('combobox')).toBeInTheDocument();
    });

    it('search input updates on user input', () => {
      render(<ExtractedEntitiesPage />);
      const searchInput = screen.getByPlaceholderText(/search entities/i);
      fireEvent.change(searchInput, { target: { value: 'test query' } });
      expect(searchInput).toHaveValue('test query');
    });
  });

  describe('Entity Table', () => {
    it('renders the entity table', () => {
      render(<ExtractedEntitiesPage />);
      expect(screen.getByRole('table')).toBeInTheDocument();
    });

    it('renders table headers', () => {
      render(<ExtractedEntitiesPage />);
      expect(screen.getByText('Name')).toBeInTheDocument();
      expect(screen.getByText('Type')).toBeInTheDocument();
    });

    it('renders entity rows', () => {
      render(<ExtractedEntitiesPage />);
      expect(screen.getByText('John Doe')).toBeInTheDocument();
      expect(screen.getByText('Acme Corp')).toBeInTheDocument();
      expect(screen.getByText('Department of Energy')).toBeInTheDocument();
    });

    it('entity rows are clickable', () => {
      render(<ExtractedEntitiesPage />);
      const row = screen.getByText('John Doe').closest('tr');
      expect(row).toHaveClass('cursor-pointer');
    });

    it('clicking entity row navigates to entity detail', () => {
      render(<ExtractedEntitiesPage />);
      const row = screen.getByText('John Doe').closest('tr');
      if (row) {
        fireEvent.click(row);
        expect(mockPush).toHaveBeenCalledWith('/article-analyzer/entities/1');
      }
    });

    it('shows confidence score visually', () => {
      render(<ExtractedEntitiesPage />);
      // Check that confidence bars are rendered (looking for the percentage text)
      expect(screen.getByText('95%')).toBeInTheDocument();
      expect(screen.getByText('85%')).toBeInTheDocument();
    });

    it('shows verified status badges', () => {
      render(<ExtractedEntitiesPage />);
      const yesButtons = screen.getAllByText('Yes');
      const noButtons = screen.getAllByText('No');
      expect(yesButtons.length).toBeGreaterThan(0);
      expect(noButtons.length).toBeGreaterThan(0);
    });
  });

  describe('Loading State', () => {
    it('shows loading indicator when loading', async () => {
      const { useExtractedEntities } = await import('@/hooks/useExtractedEntities');
      vi.mocked(useExtractedEntities).mockReturnValueOnce({
        data: undefined,
        isLoading: true,
        error: null,
        refetch: vi.fn(),
      });

      render(<ExtractedEntitiesPage />);
      expect(screen.getByText(/loading entities/i)).toBeInTheDocument();
    });
  });

  describe('Error State', () => {
    it('shows error message when fetch fails', async () => {
      const { useExtractedEntities } = await import('@/hooks/useExtractedEntities');
      vi.mocked(useExtractedEntities).mockReturnValueOnce({
        data: undefined,
        isLoading: false,
        error: new Error('Network error'),
        refetch: vi.fn(),
      });

      render(<ExtractedEntitiesPage />);
      expect(screen.getByText(/failed to load entities/i)).toBeInTheDocument();
      expect(screen.getByText(/network error/i)).toBeInTheDocument();
    });

    it('shows retry button on error', async () => {
      const { useExtractedEntities } = await import('@/hooks/useExtractedEntities');
      vi.mocked(useExtractedEntities).mockReturnValueOnce({
        data: undefined,
        isLoading: false,
        error: new Error('Network error'),
        refetch: vi.fn(),
      });

      render(<ExtractedEntitiesPage />);
      expect(screen.getByRole('button', { name: /try again/i })).toBeInTheDocument();
    });
  });

  describe('Empty State', () => {
    it('shows empty message when no entities', async () => {
      const { useExtractedEntities } = await import('@/hooks/useExtractedEntities');
      vi.mocked(useExtractedEntities).mockReturnValueOnce({
        data: [],
        isLoading: false,
        error: null,
        refetch: vi.fn(),
      });

      render(<ExtractedEntitiesPage />);
      expect(screen.getByText(/no entities found/i)).toBeInTheDocument();
    });
  });

  describe('Accessibility', () => {
    it('search input has accessible label via placeholder', () => {
      render(<ExtractedEntitiesPage />);
      const searchInput = screen.getByPlaceholderText(/search entities/i);
      expect(searchInput).toHaveAttribute('type', 'search');
    });

    it('type filter combobox is accessible', () => {
      render(<ExtractedEntitiesPage />);
      const combobox = screen.getByRole('combobox');
      expect(combobox).toBeInTheDocument();
    });

    it('table has proper structure', () => {
      render(<ExtractedEntitiesPage />);
      const table = screen.getByRole('table');
      expect(table.querySelector('thead')).toBeInTheDocument();
      expect(table.querySelector('tbody')).toBeInTheDocument();
    });
  });

  describe('Data Layer Labeling (AC4)', () => {
    it('indicates these are extracted entities in title', () => {
      render(<ExtractedEntitiesPage />);
      expect(screen.getByRole('heading', { name: /extracted entities/i })).toBeInTheDocument();
    });

    it('description mentions not authoritative data', () => {
      render(<ExtractedEntitiesPage />);
      expect(
        screen.getByText(/not authoritative reference data/i)
      ).toBeInTheDocument();
    });

    it('info banner mentions confidence levels', () => {
      render(<ExtractedEntitiesPage />);
      expect(
        screen.getByText(/varying confidence levels/i)
      ).toBeInTheDocument();
    });
  });
});
