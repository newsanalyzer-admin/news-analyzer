import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { HistoricalAdministrations } from '../HistoricalAdministrations';
import type { PresidencyDTO } from '@/hooks/usePresidencySync';

// Mock next/navigation
const mockPush = vi.fn();
let mockSearchParams = new URLSearchParams();

vi.mock('next/navigation', () => ({
  useSearchParams: () => mockSearchParams,
  useRouter: () => ({ push: mockPush }),
}));

// Mock hooks
const mockUseAllPresidencies = vi.fn();
vi.mock('@/hooks/usePresidencySync', () => ({
  useAllPresidencies: () => mockUseAllPresidencies(),
  usePresidencyAdministration: () => ({
    data: undefined,
    isLoading: false,
  }),
  usePresidencyExecutiveOrders: () => ({
    data: { content: [], totalElements: 0, totalPages: 0, size: 10, number: 0 },
    isLoading: false,
    error: null,
  }),
}));

function renderWithQueryClient(ui: React.ReactElement) {
  const queryClient = new QueryClient({
    defaultOptions: { queries: { retry: false } },
  });
  return render(
    <QueryClientProvider client={queryClient}>{ui}</QueryClientProvider>
  );
}

const mockPresidencies: PresidencyDTO[] = [
  {
    id: 'pres-1',
    number: 1,
    ordinalLabel: '1st',
    individualId: 'ind-washington',
    presidentFullName: 'George Washington',
    presidentFirstName: 'George',
    presidentLastName: 'Washington',
    imageUrl: null,
    birthDate: null,
    deathDate: null,
    birthPlace: null,
    party: 'None',
    startDate: '1789-04-30',
    endDate: '1797-03-04',
    termLabel: '1789-1797',
    termDays: 2865,
    electionYear: 1788,
    endReason: null,
    executiveOrderCount: null,
    vicePresidents: [],
    predecessorId: null,
    successorId: null,
    current: false,
    living: false,
  },
  {
    id: 'pres-22',
    number: 22,
    ordinalLabel: '22nd',
    individualId: 'ind-cleveland',
    presidentFullName: 'Grover Cleveland',
    presidentFirstName: 'Grover',
    presidentLastName: 'Cleveland',
    imageUrl: null,
    birthDate: null,
    deathDate: null,
    birthPlace: null,
    party: 'Democratic',
    startDate: '1885-03-04',
    endDate: '1889-03-04',
    termLabel: '1885-1889',
    termDays: 1461,
    electionYear: 1884,
    endReason: null,
    executiveOrderCount: null,
    vicePresidents: [],
    predecessorId: null,
    successorId: null,
    current: false,
    living: false,
  },
  {
    id: 'pres-24',
    number: 24,
    ordinalLabel: '24th',
    individualId: 'ind-cleveland',
    presidentFullName: 'Grover Cleveland',
    presidentFirstName: 'Grover',
    presidentLastName: 'Cleveland',
    imageUrl: null,
    birthDate: null,
    deathDate: null,
    birthPlace: null,
    party: 'Democratic',
    startDate: '1893-03-04',
    endDate: '1897-03-04',
    termLabel: '1893-1897',
    termDays: 1461,
    electionYear: 1892,
    endReason: null,
    executiveOrderCount: null,
    vicePresidents: [],
    predecessorId: null,
    successorId: null,
    current: false,
    living: false,
  },
  {
    id: 'pres-47',
    number: 47,
    ordinalLabel: '47th',
    individualId: 'ind-trump',
    presidentFullName: 'Donald J. Trump',
    presidentFirstName: 'Donald',
    presidentLastName: 'Trump',
    imageUrl: null,
    birthDate: null,
    deathDate: null,
    birthPlace: null,
    party: 'Republican',
    startDate: '2025-01-20',
    endDate: null,
    termLabel: '2025-present',
    termDays: null,
    electionYear: 2024,
    endReason: null,
    executiveOrderCount: null,
    vicePresidents: [],
    predecessorId: null,
    successorId: null,
    current: true,
    living: true,
  },
];

describe('HistoricalAdministrations', () => {
  beforeEach(() => {
    mockUseAllPresidencies.mockReset();
    mockPush.mockReset();
    mockSearchParams = new URLSearchParams();
  });

  describe('Loading State', () => {
    it('renders loading skeletons', () => {
      mockUseAllPresidencies.mockReturnValue({
        data: undefined,
        isLoading: true,
        error: null,
      });

      renderWithQueryClient(<HistoricalAdministrations />);

      expect(document.querySelectorAll('.animate-pulse').length).toBeGreaterThan(0);
    });
  });

  describe('Error State', () => {
    it('renders error message', () => {
      mockUseAllPresidencies.mockReturnValue({
        data: undefined,
        isLoading: false,
        error: new Error('Failed to fetch'),
      });

      renderWithQueryClient(<HistoricalAdministrations />);

      expect(screen.getByText(/failed to load administrations/i)).toBeInTheDocument();
    });
  });

  describe('List Rendering', () => {
    beforeEach(() => {
      mockUseAllPresidencies.mockReturnValue({
        data: mockPresidencies,
        isLoading: false,
        error: null,
      });
    });

    it('renders all administrations', () => {
      renderWithQueryClient(<HistoricalAdministrations />);

      expect(screen.getByText('George Washington')).toBeInTheDocument();
      expect(screen.getByText('Donald J. Trump')).toBeInTheDocument();
      expect(screen.getByText('4 administrations')).toBeInTheDocument();
    });

    it('shows non-consecutive terms as separate entries (Cleveland 22 & 24)', () => {
      renderWithQueryClient(<HistoricalAdministrations />);

      // Both Cleveland entries should appear
      expect(screen.getAllByText('Grover Cleveland')).toHaveLength(2);
      expect(screen.getByText('22')).toBeInTheDocument();
      expect(screen.getByText('24')).toBeInTheDocument();
    });

    it('highlights current administration', () => {
      renderWithQueryClient(<HistoricalAdministrations />);

      expect(screen.getByText('Current')).toBeInTheDocument();
    });

    it('shows empty selection state by default', () => {
      renderWithQueryClient(<HistoricalAdministrations />);

      expect(screen.getByText(/select an administration/i)).toBeInTheDocument();
    });
  });

  describe('Sort Toggle', () => {
    beforeEach(() => {
      mockUseAllPresidencies.mockReturnValue({
        data: mockPresidencies,
        isLoading: false,
        error: null,
      });
    });

    it('defaults to newest first', () => {
      renderWithQueryClient(<HistoricalAdministrations />);

      expect(screen.getByText('Newest first')).toBeInTheDocument();
    });

    it('toggles to oldest first when clicked', async () => {
      renderWithQueryClient(<HistoricalAdministrations />);

      const user = userEvent.setup();
      await user.click(screen.getByText('Newest first'));

      expect(screen.getByText('Oldest first')).toBeInTheDocument();
    });
  });

  describe('Selection', () => {
    beforeEach(() => {
      mockUseAllPresidencies.mockReturnValue({
        data: mockPresidencies,
        isLoading: false,
        error: null,
      });
    });

    it('updates URL when administration is clicked', async () => {
      renderWithQueryClient(<HistoricalAdministrations />);

      const user = userEvent.setup();
      // Click on Washington (number 1)
      await user.click(screen.getByText('George Washington'));

      expect(mockPush).toHaveBeenCalledWith('?presidency=1', { scroll: false });
    });

    it('shows detail view when presidency query param is present', () => {
      mockSearchParams = new URLSearchParams('presidency=47');

      renderWithQueryClient(<HistoricalAdministrations />);

      // Should show the detail heading and close button
      expect(screen.getByText('47th Administration')).toBeInTheDocument();
      expect(screen.getByText('Close')).toBeInTheDocument();
    });

    it('clears selection when Close is clicked', async () => {
      mockSearchParams = new URLSearchParams('presidency=47');

      renderWithQueryClient(<HistoricalAdministrations />);

      const user = userEvent.setup();
      await user.click(screen.getByText('Close'));

      expect(mockPush).toHaveBeenCalledWith('?', { scroll: false });
    });
  });
});
