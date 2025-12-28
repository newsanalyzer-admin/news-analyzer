import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
import { EntityFilters } from '../EntityFilters';
import type { FilterConfig } from '@/lib/config/entityTypes';

// Mock filter configurations
const mockFilters: FilterConfig[] = [
  {
    id: 'branch',
    label: 'Branch',
    type: 'select',
    apiParam: 'branch',
    options: [
      { value: 'executive', label: 'Executive' },
      { value: 'legislative', label: 'Legislative' },
      { value: 'judicial', label: 'Judicial' },
    ],
  },
  {
    id: 'status',
    label: 'Status',
    type: 'select',
    apiParam: 'active',
    options: [
      { value: 'true', label: 'Active' },
      { value: 'false', label: 'Inactive' },
    ],
  },
  {
    id: 'search',
    label: 'Name',
    type: 'text',
    apiParam: 'q',
    placeholder: 'Search by name...',
  },
];

describe('EntityFilters', () => {
  const defaultProps = {
    filters: mockFilters,
    values: {},
    onChange: vi.fn(),
  };

  beforeEach(() => {
    vi.clearAllMocks();
  });

  // ====== Rendering Tests ======

  describe('Rendering', () => {
    it('renders filter controls based on config', () => {
      render(<EntityFilters {...defaultProps} />);

      expect(screen.getByText('Branch')).toBeInTheDocument();
      expect(screen.getByText('Status')).toBeInTheDocument();
      expect(screen.getByText('Name')).toBeInTheDocument();
    });

    it('renders nothing when filters array is empty', () => {
      const { container } = render(
        <EntityFilters {...defaultProps} filters={[]} />
      );

      expect(container.firstChild).toBeNull();
    });

    it('renders select filters with correct options', () => {
      render(<EntityFilters {...defaultProps} />);

      // Find the select by looking for its container with the label
      const branchLabel = screen.getByText('Branch');
      const branchContainer = branchLabel.closest('div');
      const branchSelect = branchContainer?.querySelector('[role="combobox"]');

      expect(branchSelect).toBeInTheDocument();
      if (branchSelect) {
        fireEvent.click(branchSelect);
      }

      // Check options are present
      expect(screen.getByText('Executive')).toBeInTheDocument();
      expect(screen.getByText('Legislative')).toBeInTheDocument();
      expect(screen.getByText('Judicial')).toBeInTheDocument();
    });

    it('renders text input with placeholder', () => {
      render(<EntityFilters {...defaultProps} />);

      const searchInput = screen.getByPlaceholderText('Search by name...');
      expect(searchInput).toBeInTheDocument();
    });
  });

  // ====== Filter Interaction Tests ======

  describe('Filter Interactions', () => {
    it('changing select filter triggers onChange', () => {
      render(<EntityFilters {...defaultProps} />);

      // Find the select by looking for its container with the label
      const branchLabel = screen.getByText('Branch');
      const branchContainer = branchLabel.closest('div');
      const branchSelect = branchContainer?.querySelector('[role="combobox"]');

      if (branchSelect) {
        fireEvent.click(branchSelect);
      }

      // Select an option
      const executiveOption = screen.getByText('Executive');
      fireEvent.click(executiveOption);

      expect(defaultProps.onChange).toHaveBeenCalledWith(
        expect.objectContaining({ branch: 'executive' })
      );
    });

    it('typing in text filter triggers onChange', () => {
      render(<EntityFilters {...defaultProps} />);

      const searchInput = screen.getByPlaceholderText('Search by name...');
      fireEvent.change(searchInput, { target: { value: 'test query' } });

      expect(defaultProps.onChange).toHaveBeenCalledWith(
        expect.objectContaining({ search: 'test query' })
      );
    });

    it('clearing text filter sets value to undefined', () => {
      render(<EntityFilters {...defaultProps} values={{ search: 'existing' }} />);

      const searchInput = screen.getByDisplayValue('existing');
      fireEvent.change(searchInput, { target: { value: '' } });

      expect(defaultProps.onChange).toHaveBeenCalledWith(
        expect.objectContaining({ search: undefined })
      );
    });

    it('selecting "All" option clears filter value', () => {
      render(<EntityFilters {...defaultProps} values={{ branch: 'executive' }} />);

      // Find the select by looking for its container with the label
      const branchLabel = screen.getByText('Branch');
      const branchContainer = branchLabel.closest('div');
      const branchSelect = branchContainer?.querySelector('[role="combobox"]');

      if (branchSelect) {
        fireEvent.click(branchSelect);
      }

      // Select "All"
      const allOption = screen.getByRole('option', { name: 'All' });
      fireEvent.click(allOption);

      expect(defaultProps.onChange).toHaveBeenCalledWith(
        expect.objectContaining({ branch: undefined })
      );
    });
  });

  // ====== Clear All Tests ======

  describe('Clear All', () => {
    it('does not show clear all button when no filters active', () => {
      render(<EntityFilters {...defaultProps} values={{}} />);

      expect(screen.queryByRole('button', { name: /clear all/i })).not.toBeInTheDocument();
    });

    it('shows clear all button when filters are active', () => {
      render(<EntityFilters {...defaultProps} values={{ branch: 'executive' }} />);

      expect(screen.getByRole('button', { name: /clear all/i })).toBeInTheDocument();
    });

    it('shows active filter count on clear all button', () => {
      render(
        <EntityFilters
          {...defaultProps}
          values={{ branch: 'executive', status: 'true' }}
        />
      );

      // Should show count badge with "2"
      expect(screen.getByText('2')).toBeInTheDocument();
    });

    it('clicking clear all resets all filters', () => {
      render(
        <EntityFilters
          {...defaultProps}
          values={{ branch: 'executive', status: 'true', search: 'test' }}
        />
      );

      const clearButton = screen.getByRole('button', { name: /clear all/i });
      fireEvent.click(clearButton);

      expect(defaultProps.onChange).toHaveBeenCalledWith({});
    });
  });

  // ====== Multi-Select Tests ======

  describe('Multi-Select Filter', () => {
    it('handles multi-select filter type', () => {
      const multiSelectFilters: FilterConfig[] = [
        {
          id: 'types',
          label: 'Types',
          type: 'multi-select',
          apiParam: 'types',
          options: [
            { value: 'department', label: 'Department' },
            { value: 'agency', label: 'Agency' },
          ],
        },
      ];

      render(
        <EntityFilters
          filters={multiSelectFilters}
          values={{}}
          onChange={defaultProps.onChange}
        />
      );

      expect(screen.getByText('Types')).toBeInTheDocument();
    });
  });

  // ====== Value Display Tests ======

  describe('Value Display', () => {
    it('displays current filter value in select', () => {
      render(<EntityFilters {...defaultProps} values={{ branch: 'executive' }} />);

      // The select should show "Executive" as selected
      expect(screen.getByText('Executive')).toBeInTheDocument();
    });

    it('displays current filter value in text input', () => {
      render(<EntityFilters {...defaultProps} values={{ search: 'test query' }} />);

      const input = screen.getByDisplayValue('test query');
      expect(input).toBeInTheDocument();
    });
  });
});
