import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { PresidencyTable } from '../PresidencyTable';
import type { PresidencyDTO } from '@/hooks/usePresidencySync';

// Mock the PresidencyExpandedRow component
vi.mock('../PresidencyExpandedRow', () => ({
  PresidencyExpandedRow: ({ presidency }: { presidency: PresidencyDTO }) => (
    <div data-testid="expanded-row">Expanded: {presidency.presidentFullName}</div>
  ),
}));

const mockPresidencies: PresidencyDTO[] = [
  {
    id: '1',
    number: 47,
    ordinalLabel: '47th',
    personId: 'p1',
    presidentFullName: 'Donald J. Trump',
    presidentFirstName: 'Donald',
    presidentLastName: 'Trump',
    imageUrl: null,
    birthDate: '1946-06-14',
    deathDate: null,
    birthPlace: 'Queens, New York',
    party: 'Republican',
    startDate: '2025-01-20',
    endDate: null,
    termLabel: '2025-present',
    termDays: null,
    electionYear: 2024,
    endReason: null,
    executiveOrderCount: null,
    vicePresidents: [{ personId: 'vp1', fullName: 'JD Vance', firstName: 'JD', lastName: 'Vance', startDate: '2025-01-20', endDate: null, termLabel: '2025-present' }],
    predecessorId: null,
    successorId: null,
    current: true,
    living: true,
  },
  {
    id: '2',
    number: 46,
    ordinalLabel: '46th',
    personId: 'p2',
    presidentFullName: 'Joseph R. Biden Jr.',
    presidentFirstName: 'Joseph',
    presidentLastName: 'Biden',
    imageUrl: null,
    birthDate: '1942-11-20',
    deathDate: null,
    birthPlace: 'Scranton, Pennsylvania',
    party: 'Democratic',
    startDate: '2021-01-20',
    endDate: '2025-01-20',
    termLabel: '2021-2025',
    termDays: 1461,
    electionYear: 2020,
    endReason: 'TERM_END',
    executiveOrderCount: 162,
    vicePresidents: [{ personId: 'vp2', fullName: 'Kamala Harris', firstName: 'Kamala', lastName: 'Harris', startDate: '2021-01-20', endDate: '2025-01-20', termLabel: '2021-2025' }],
    predecessorId: null,
    successorId: null,
    current: false,
    living: true,
  },
  {
    id: '3',
    number: 1,
    ordinalLabel: '1st',
    personId: 'p3',
    presidentFullName: 'George Washington',
    presidentFirstName: 'George',
    presidentLastName: 'Washington',
    imageUrl: null,
    birthDate: '1732-02-22',
    deathDate: '1799-12-14',
    birthPlace: 'Westmoreland County, Virginia',
    party: 'Independent',
    startDate: '1789-04-30',
    endDate: '1797-03-04',
    termLabel: '1789-1797',
    termDays: 2865,
    electionYear: 1788,
    endReason: 'TERM_END',
    executiveOrderCount: 8,
    vicePresidents: [{ personId: 'vp3', fullName: 'John Adams', firstName: 'John', lastName: 'Adams', startDate: '1789-04-21', endDate: '1797-03-04', termLabel: '1789-1797' }],
    predecessorId: null,
    successorId: null,
    current: false,
    living: false,
  },
];

describe('PresidencyTable', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  describe('Loading State', () => {
    it('renders loading skeletons when isLoading is true', () => {
      render(<PresidencyTable presidencies={[]} isLoading={true} />);

      expect(document.querySelectorAll('.animate-pulse').length).toBeGreaterThan(0);
    });
  });

  describe('Empty State', () => {
    it('renders empty state when no presidencies', () => {
      render(<PresidencyTable presidencies={[]} isLoading={false} />);

      expect(screen.getByText(/no presidencies found/i)).toBeInTheDocument();
    });
  });

  describe('Table Rendering', () => {
    it('renders all presidencies', () => {
      render(<PresidencyTable presidencies={mockPresidencies} />);

      expect(screen.getByText('Donald J. Trump')).toBeInTheDocument();
      expect(screen.getByText('Joseph R. Biden Jr.')).toBeInTheDocument();
      expect(screen.getByText('George Washington')).toBeInTheDocument();
    });

    it('renders ordinal labels', () => {
      render(<PresidencyTable presidencies={mockPresidencies} />);

      expect(screen.getByText('47th')).toBeInTheDocument();
      expect(screen.getByText('46th')).toBeInTheDocument();
      expect(screen.getByText('1st')).toBeInTheDocument();
    });

    it('renders party names', () => {
      render(<PresidencyTable presidencies={mockPresidencies} />);

      expect(screen.getAllByText('Republican').length).toBeGreaterThan(0);
      expect(screen.getAllByText('Democratic').length).toBeGreaterThan(0);
      expect(screen.getAllByText('Independent').length).toBeGreaterThan(0);
    });

    it('shows Current badge for current president', () => {
      render(<PresidencyTable presidencies={mockPresidencies} />);

      expect(screen.getByText('Current')).toBeInTheDocument();
    });

    it('renders term labels', () => {
      render(<PresidencyTable presidencies={mockPresidencies} />);

      expect(screen.getByText('2025-present')).toBeInTheDocument();
      expect(screen.getByText('2021-2025')).toBeInTheDocument();
      expect(screen.getByText('1789-1797')).toBeInTheDocument();
    });
  });

  describe('Sorting', () => {
    it('initially sorts by number descending', () => {
      render(<PresidencyTable presidencies={mockPresidencies} />);

      const rows = screen.getAllByRole('row');
      // First data row (after header) should be 47th
      expect(within(rows[1]).getByText('47th')).toBeInTheDocument();
    });

    it('toggles sort direction when clicking same column', async () => {
      const user = userEvent.setup();
      render(<PresidencyTable presidencies={mockPresidencies} />);

      const numberHeader = screen.getByRole('button', { name: /#/i });
      await user.click(numberHeader);

      // After clicking, should be ascending (1st at top)
      const rows = screen.getAllByRole('row');
      expect(within(rows[1]).getByText('1st')).toBeInTheDocument();
    });

    it('sorts by name when clicking name column', async () => {
      const user = userEvent.setup();
      render(<PresidencyTable presidencies={mockPresidencies} />);

      const nameHeader = screen.getByRole('button', { name: /president/i });
      await user.click(nameHeader);

      // Should sort alphabetically by last name (Biden, Trump, Washington)
      const rows = screen.getAllByRole('row');
      expect(within(rows[1]).getByText('Joseph R. Biden Jr.')).toBeInTheDocument();
    });

    it('sorts by party when clicking party column', async () => {
      const user = userEvent.setup();
      render(<PresidencyTable presidencies={mockPresidencies} />);

      const partyHeader = screen.getByRole('button', { name: /party/i });
      await user.click(partyHeader);

      // Should sort alphabetically by party (Democratic, Independent, Republican)
      const rows = screen.getAllByRole('row');
      expect(within(rows[1]).getByText('Joseph R. Biden Jr.')).toBeInTheDocument();
    });

    it('sorts by term when clicking term column', async () => {
      const user = userEvent.setup();
      render(<PresidencyTable presidencies={mockPresidencies} />);

      const termHeader = screen.getByRole('button', { name: /term/i });
      await user.click(termHeader);

      // Should sort by start date ascending (1789 first)
      const rows = screen.getAllByRole('row');
      expect(within(rows[1]).getByText('George Washington')).toBeInTheDocument();
    });
  });

  describe('Expandable Rows', () => {
    it('expands row when clicked', async () => {
      const user = userEvent.setup();
      render(<PresidencyTable presidencies={mockPresidencies} />);

      // Click on the first row
      const firstRow = screen.getByText('Donald J. Trump').closest('tr');
      await user.click(firstRow!);

      expect(screen.getByTestId('expanded-row')).toBeInTheDocument();
      expect(screen.getByText('Expanded: Donald J. Trump')).toBeInTheDocument();
    });

    it('collapses row when clicked again', async () => {
      const user = userEvent.setup();
      render(<PresidencyTable presidencies={mockPresidencies} />);

      const firstRow = screen.getByText('Donald J. Trump').closest('tr');
      await user.click(firstRow!);
      expect(screen.getByTestId('expanded-row')).toBeInTheDocument();

      await user.click(firstRow!);
      expect(screen.queryByTestId('expanded-row')).not.toBeInTheDocument();
    });

    it('only expands one row at a time', async () => {
      const user = userEvent.setup();
      render(<PresidencyTable presidencies={mockPresidencies} />);

      // Click first row
      const firstRow = screen.getByText('Donald J. Trump').closest('tr');
      await user.click(firstRow!);
      expect(screen.getByText('Expanded: Donald J. Trump')).toBeInTheDocument();

      // Click second row
      const secondRow = screen.getByText('Joseph R. Biden Jr.').closest('tr');
      await user.click(secondRow!);

      // First should be collapsed, second should be expanded
      expect(screen.queryByText('Expanded: Donald J. Trump')).not.toBeInTheDocument();
      expect(screen.getByText('Expanded: Joseph R. Biden Jr.')).toBeInTheDocument();
    });
  });

  describe('Party Colors', () => {
    it('applies Republican color class', () => {
      render(<PresidencyTable presidencies={mockPresidencies} />);

      // Find Republican text in table cell (not in mobile hidden div)
      const republicanCells = screen.getAllByText('Republican');
      const tableCell = republicanCells.find(el => el.closest('td'));
      expect(tableCell).toHaveClass('text-red-600');
    });

    it('applies Democratic color class', () => {
      render(<PresidencyTable presidencies={mockPresidencies} />);

      const democraticCells = screen.getAllByText('Democratic');
      const tableCell = democraticCells.find(el => el.closest('td'));
      expect(tableCell).toHaveClass('text-blue-600');
    });
  });
});
