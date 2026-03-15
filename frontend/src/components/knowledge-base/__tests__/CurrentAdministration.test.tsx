import { describe, it, expect, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { CurrentAdministration } from '../CurrentAdministration';
import type {
  PresidencyDTO,
  PresidencyAdministrationDTO,
  ExecutiveOrderPage,
} from '@/hooks/usePresidencySync';

// Mock all hooks used by CurrentAdministration and its children
const mockUseCurrentPresidency = vi.fn();
const mockUsePresidencyAdministration = vi.fn();
const mockUsePresidencyExecutiveOrders = vi.fn();

vi.mock('@/hooks/usePresidencySync', () => ({
  useCurrentPresidency: () => mockUseCurrentPresidency(),
  usePresidencyAdministration: (...args: unknown[]) => mockUsePresidencyAdministration(...args),
  usePresidencyExecutiveOrders: (...args: unknown[]) => mockUsePresidencyExecutiveOrders(...args),
}));

function renderWithQueryClient(ui: React.ReactElement) {
  const queryClient = new QueryClient({
    defaultOptions: { queries: { retry: false } },
  });
  return render(
    <QueryClientProvider client={queryClient}>{ui}</QueryClientProvider>
  );
}

const mockPresidency: PresidencyDTO = {
  id: 'pres-47',
  number: 47,
  ordinalLabel: '47th',
  individualId: 'ind-trump',
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
  vicePresidents: [
    {
      individualId: 'ind-vance',
      fullName: 'JD Vance',
      firstName: 'JD',
      lastName: 'Vance',
      startDate: '2025-01-20',
      endDate: null,
      termLabel: '2025-present',
    },
  ],
  predecessorId: null,
  successorId: null,
  current: true,
  living: true,
};

const mockAdministration: PresidencyAdministrationDTO = {
  presidencyId: 'pres-47',
  presidencyNumber: 47,
  presidencyLabel: '47th Presidency',
  vicePresidents: [
    {
      holdingId: 'vp-1',
      individualId: 'ind-vance',
      fullName: 'JD Vance',
      firstName: 'JD',
      lastName: 'Vance',
      positionTitle: 'Vice President of the United States',
      startDate: '2025-01-20',
      endDate: null,
      termLabel: '2025-present',
      imageUrl: null,
    },
  ],
  chiefsOfStaff: [
    {
      holdingId: 'cos-1',
      individualId: 'ind-wiles',
      fullName: 'Susie Wiles',
      firstName: 'Susie',
      lastName: 'Wiles',
      positionTitle: 'White House Chief of Staff',
      startDate: '2025-01-20',
      endDate: null,
      termLabel: '2025-present',
      imageUrl: null,
    },
  ],
  cabinetMembers: [
    {
      holdingId: 'cab-1',
      individualId: 'ind-rubio',
      fullName: 'Marco Rubio',
      positionTitle: 'Secretary of State',
      departmentName: 'Department of State',
      departmentId: 'dept-1',
      startDate: '2025-01-20',
      endDate: null,
    },
  ],
};

const mockEmptyEOPage: ExecutiveOrderPage = {
  content: [],
  totalElements: 0,
  totalPages: 0,
  size: 10,
  number: 0,
};

describe('CurrentAdministration', () => {
  beforeEach(() => {
    mockUseCurrentPresidency.mockReset();
    mockUsePresidencyAdministration.mockReset();
    mockUsePresidencyExecutiveOrders.mockReset();
  });

  describe('Loading State', () => {
    it('renders loading skeletons when presidency is loading', () => {
      mockUseCurrentPresidency.mockReturnValue({
        data: undefined,
        isLoading: true,
        error: null,
      });
      mockUsePresidencyAdministration.mockReturnValue({
        data: undefined,
        isLoading: true,
      });
      mockUsePresidencyExecutiveOrders.mockReturnValue({
        data: undefined,
        isLoading: true,
        error: null,
      });

      renderWithQueryClient(<CurrentAdministration />);

      // Should have skeletons from PresidentCard, VPCard, Staff, and EO sections
      expect(document.querySelectorAll('.animate-pulse').length).toBeGreaterThan(0);
    });
  });

  describe('Error State', () => {
    it('renders error state when presidency fetch fails', () => {
      mockUseCurrentPresidency.mockReturnValue({
        data: undefined,
        isLoading: false,
        error: new Error('Network error'),
      });
      mockUsePresidencyAdministration.mockReturnValue({
        data: undefined,
        isLoading: false,
      });
      mockUsePresidencyExecutiveOrders.mockReturnValue({
        data: undefined,
        isLoading: false,
        error: null,
      });

      renderWithQueryClient(<CurrentAdministration />);

      expect(screen.getByText(/failed to load current administration/i)).toBeInTheDocument();
      expect(screen.getByText('Network error')).toBeInTheDocument();
    });
  });

  describe('Full Data Display', () => {
    beforeEach(() => {
      mockUseCurrentPresidency.mockReturnValue({
        data: mockPresidency,
        isLoading: false,
        error: null,
      });
      mockUsePresidencyAdministration.mockReturnValue({
        data: mockAdministration,
        isLoading: false,
      });
      mockUsePresidencyExecutiveOrders.mockReturnValue({
        data: mockEmptyEOPage,
        isLoading: false,
        error: null,
      });
    });

    it('renders president card with data', () => {
      renderWithQueryClient(<CurrentAdministration />);

      expect(screen.getByText('Donald J. Trump')).toBeInTheDocument();
    });

    it('renders vice president card alongside president', () => {
      renderWithQueryClient(<CurrentAdministration />);

      expect(screen.getByText('Vice President')).toBeInTheDocument();
      // VP name appears in both PresidentCard (embedded VP line) and VicePresidentCard
      expect(screen.getAllByText('JD Vance').length).toBeGreaterThanOrEqual(1);
    });

    it('renders staff section with CoS and Cabinet', () => {
      renderWithQueryClient(<CurrentAdministration />);

      expect(screen.getByText('Susie Wiles')).toBeInTheDocument();
      expect(screen.getByText('Marco Rubio')).toBeInTheDocument();
    });

    it('renders executive orders section', () => {
      renderWithQueryClient(<CurrentAdministration />);

      expect(screen.getByText('Executive Orders')).toBeInTheDocument();
    });
  });

  describe('Empty States', () => {
    it('handles no presidency data (empty state)', () => {
      mockUseCurrentPresidency.mockReturnValue({
        data: null,
        isLoading: false,
        error: null,
      });
      mockUsePresidencyAdministration.mockReturnValue({
        data: undefined,
        isLoading: false,
      });
      mockUsePresidencyExecutiveOrders.mockReturnValue({
        data: undefined,
        isLoading: false,
        error: null,
      });

      renderWithQueryClient(<CurrentAdministration />);

      // PresidentCard shows its empty state
      expect(screen.getByText(/no presidential data available/i)).toBeInTheDocument();
      // VP card shows its empty state
      expect(screen.getByText(/no vice president data available/i)).toBeInTheDocument();
    });
  });
});
