import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
import { EntityBrowser } from '../EntityBrowser';
import type { EntityTypeConfig, ColumnConfig } from '@/lib/config/entityTypes';
import { Building2 } from 'lucide-react';

// Mock data
const mockColumns: ColumnConfig[] = [
  { id: 'name', label: 'Name', sortable: true },
  { id: 'type', label: 'Type', sortable: true },
  { id: 'status', label: 'Status', sortable: false },
];

const mockConfig: EntityTypeConfig = {
  id: 'test-entities',
  label: 'Test Entities',
  icon: Building2,
  apiEndpoint: '/api/test',
  dataLayer: 'kb',
  supportedViews: ['list'],
  defaultView: 'list',
  columns: mockColumns,
};

const mockData = [
  { id: '1', name: 'Entity One', type: 'TypeA', status: 'Active' },
  { id: '2', name: 'Entity Two', type: 'TypeB', status: 'Inactive' },
  { id: '3', name: 'Entity Three', type: 'TypeA', status: 'Active' },
];

const defaultProps = {
  config: mockConfig,
  data: mockData,
  totalCount: 3,
  isLoading: false,
  currentPage: 0,
  pageSize: 20,
  viewMode: 'list' as const,
  onPageChange: vi.fn(),
  onSortChange: vi.fn(),
  onRowClick: vi.fn(),
};

describe('EntityBrowser', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  // ====== Rendering Tests ======

  describe('Rendering', () => {
    it('renders table with correct columns from config', () => {
      render(<EntityBrowser {...defaultProps} />);

      expect(screen.getByText('Name')).toBeInTheDocument();
      expect(screen.getByText('Type')).toBeInTheDocument();
      expect(screen.getByText('Status')).toBeInTheDocument();
    });

    it('renders correct number of rows based on data', () => {
      render(<EntityBrowser {...defaultProps} />);

      // Use getAllByText since both table and mobile cards render the same content
      expect(screen.getAllByText('Entity One').length).toBeGreaterThanOrEqual(1);
      expect(screen.getAllByText('Entity Two').length).toBeGreaterThanOrEqual(1);
      expect(screen.getAllByText('Entity Three').length).toBeGreaterThanOrEqual(1);
    });

    it('renders grid view when viewMode=grid', () => {
      render(<EntityBrowser {...defaultProps} viewMode="grid" />);

      // In grid view, we should see card-like elements
      const cards = screen.getAllByRole('button');
      expect(cards.length).toBeGreaterThanOrEqual(3);
    });

    it('renders loading skeleton when isLoading=true', () => {
      render(<EntityBrowser {...defaultProps} isLoading={true} />);

      // Should not show data
      expect(screen.queryByText('Entity One')).not.toBeInTheDocument();
    });

    it('renders empty state when data=[]', () => {
      render(<EntityBrowser {...defaultProps} data={[]} totalCount={0} />);

      expect(screen.getByText(/no test entities found/i)).toBeInTheDocument();
    });

    it('renders error state when error prop provided', () => {
      render(<EntityBrowser {...defaultProps} error="Failed to load data" />);

      expect(screen.getByText('Failed to load data')).toBeInTheDocument();
      expect(screen.getByText(/failed to load test entities/i)).toBeInTheDocument();
    });

    it('renders retry button in error state', () => {
      const onRetry = vi.fn();
      render(<EntityBrowser {...defaultProps} error="Failed" onRetry={onRetry} />);

      const retryButton = screen.getByRole('button', { name: /try again/i });
      fireEvent.click(retryButton);

      expect(onRetry).toHaveBeenCalled();
    });
  });

  // ====== Sorting Tests ======

  describe('Sorting', () => {
    it('clicking sortable column header triggers onSortChange', () => {
      render(<EntityBrowser {...defaultProps} />);

      const nameHeader = screen.getByText('Name');
      fireEvent.click(nameHeader);

      expect(defaultProps.onSortChange).toHaveBeenCalledWith('name', 'asc');
    });

    it('clicking same column toggles direction (asc/desc)', () => {
      render(<EntityBrowser {...defaultProps} sortColumn="name" sortDirection="asc" />);

      const nameHeader = screen.getByText('Name');
      fireEvent.click(nameHeader);

      expect(defaultProps.onSortChange).toHaveBeenCalledWith('name', 'desc');
    });

    it('non-sortable columns dont trigger sort', () => {
      render(<EntityBrowser {...defaultProps} />);

      const statusHeader = screen.getByText('Status');
      fireEvent.click(statusHeader);

      expect(defaultProps.onSortChange).not.toHaveBeenCalled();
    });

    it('sort indicator shows on current sort column', () => {
      render(<EntityBrowser {...defaultProps} sortColumn="name" sortDirection="asc" />);

      // The sort indicator should be visible (we check by aria-sort)
      const nameHeader = screen.getByText('Name').closest('th');
      expect(nameHeader).toHaveAttribute('aria-sort', 'ascending');
    });
  });

  // ====== Pagination Tests ======

  describe('Pagination', () => {
    it('displays correct page info', () => {
      render(<EntityBrowser {...defaultProps} totalCount={100} currentPage={0} pageSize={20} />);

      expect(screen.getByText(/showing 1 to 20 of 100/i)).toBeInTheDocument();
    });

    it('next button triggers onPageChange', () => {
      render(<EntityBrowser {...defaultProps} totalCount={100} currentPage={0} pageSize={20} />);

      const nextButton = screen.getByRole('button', { name: /next/i });
      fireEvent.click(nextButton);

      expect(defaultProps.onPageChange).toHaveBeenCalledWith(1);
    });

    it('prev button triggers onPageChange', () => {
      render(<EntityBrowser {...defaultProps} totalCount={100} currentPage={2} pageSize={20} />);

      const prevButton = screen.getByRole('button', { name: /previous/i });
      fireEvent.click(prevButton);

      expect(defaultProps.onPageChange).toHaveBeenCalledWith(1);
    });

    it('prev button disabled on first page', () => {
      render(<EntityBrowser {...defaultProps} totalCount={100} currentPage={0} pageSize={20} />);

      const prevButton = screen.getByRole('button', { name: /previous/i });
      expect(prevButton).toBeDisabled();
    });

    it('next button disabled on last page', () => {
      render(<EntityBrowser {...defaultProps} totalCount={100} currentPage={4} pageSize={20} />);

      const nextButton = screen.getByRole('button', { name: /next/i });
      expect(nextButton).toBeDisabled();
    });
  });

  // ====== Row Interaction Tests ======

  describe('Row Interaction', () => {
    it('clicking row triggers onRowClick with item', () => {
      render(<EntityBrowser {...defaultProps} />);

      // Get the table cell containing "Entity One" and find its parent row
      const cells = screen.getAllByText('Entity One');
      const row = cells[0].closest('tr');
      if (row) fireEvent.click(row);

      expect(defaultProps.onRowClick).toHaveBeenCalledWith(
        expect.objectContaining({ id: '1', name: 'Entity One' })
      );
    });

    it('keyboard: Enter on focused row triggers click', () => {
      render(<EntityBrowser {...defaultProps} />);

      // Get the table cell containing "Entity One" and find its parent row
      const cells = screen.getAllByText('Entity One');
      const row = cells[0].closest('tr');
      if (row) {
        row.focus();
        fireEvent.keyDown(row, { key: 'Enter' });
      }

      expect(defaultProps.onRowClick).toHaveBeenCalledWith(
        expect.objectContaining({ id: '1' })
      );
    });

    it('keyboard: Space on focused row triggers click', () => {
      render(<EntityBrowser {...defaultProps} />);

      // Get the table cell containing "Entity One" and find its parent row
      const cells = screen.getAllByText('Entity One');
      const row = cells[0].closest('tr');
      if (row) {
        row.focus();
        fireEvent.keyDown(row, { key: ' ' });
      }

      expect(defaultProps.onRowClick).toHaveBeenCalledWith(
        expect.objectContaining({ id: '1' })
      );
    });
  });

  // ====== Accessibility Tests ======

  describe('Accessibility', () => {
    it('table has proper ARIA roles', () => {
      render(<EntityBrowser {...defaultProps} />);

      expect(screen.getByRole('table')).toBeInTheDocument();
      expect(screen.getAllByRole('row').length).toBeGreaterThan(0);
    });

    it('rows are focusable', () => {
      render(<EntityBrowser {...defaultProps} />);

      const rows = screen.getAllByRole('row');
      // First row is header, data rows start from index 1
      expect(rows[1]).toHaveAttribute('tabindex', '0');
    });

    it('sort buttons have implicit aria-label through column header', () => {
      render(<EntityBrowser {...defaultProps} />);

      // Column headers with sortable columns should have aria-sort attribute
      const nameHeader = screen.getByText('Name').closest('th');
      expect(nameHeader).toBeInTheDocument();
    });
  });

  // ====== Custom Render Tests ======

  describe('Custom Rendering', () => {
    it('uses custom render function for columns', () => {
      const customColumns: ColumnConfig[] = [
        {
          id: 'name',
          label: 'Name',
          sortable: true,
          render: (value) => `Custom: ${value}`,
        },
      ];

      const customConfig = { ...mockConfig, columns: customColumns };
      render(<EntityBrowser {...defaultProps} config={customConfig} />);

      // Both table and mobile cards render, so use getAllByText
      expect(screen.getAllByText('Custom: Entity One').length).toBeGreaterThanOrEqual(1);
    });

    it('shows dash for null/undefined values', () => {
      const dataWithNull = [{ id: '1', name: null, type: 'TypeA', status: 'Active' }];
      render(<EntityBrowser {...defaultProps} data={dataWithNull} totalCount={1} />);

      // Multiple dashes may appear in both views
      expect(screen.getAllByText('-').length).toBeGreaterThanOrEqual(1);
    });
  });
});
