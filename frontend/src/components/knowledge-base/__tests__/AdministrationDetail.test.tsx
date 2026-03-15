import { describe, it, expect, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { AdministrationDetail } from '../AdministrationDetail';
import type { PresidencyDTO } from '@/hooks/usePresidencySync';

// Mock hooks used by AdministrationDetail and its children
vi.mock('@/hooks/usePresidencySync', () => ({
  usePresidencyAdministration: () => ({
    data: {
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
      chiefsOfStaff: [],
      cabinetMembers: [],
    },
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

describe('AdministrationDetail', () => {
  it('renders president card with presidency data', () => {
    renderWithQueryClient(
      <AdministrationDetail presidency={mockPresidency} />
    );

    expect(screen.getByText('Donald J. Trump')).toBeInTheDocument();
  });

  it('renders vice president card', () => {
    renderWithQueryClient(
      <AdministrationDetail presidency={mockPresidency} />
    );

    expect(screen.getByText('Vice President')).toBeInTheDocument();
  });

  it('renders executive orders section', () => {
    renderWithQueryClient(
      <AdministrationDetail presidency={mockPresidency} />
    );

    expect(screen.getByText('Executive Orders')).toBeInTheDocument();
  });

  it('renders loading skeletons when isLoading', () => {
    renderWithQueryClient(
      <AdministrationDetail presidency={null} isLoading={true} />
    );

    expect(document.querySelectorAll('.animate-pulse').length).toBeGreaterThan(0);
  });

  it('renders error state when error provided', () => {
    renderWithQueryClient(
      <AdministrationDetail
        presidency={null}
        error={new Error('Network failure')}
      />
    );

    expect(screen.getByText(/failed to load administration/i)).toBeInTheDocument();
    expect(screen.getByText('Network failure')).toBeInTheDocument();
  });

  it('renders empty state when no presidency data', () => {
    renderWithQueryClient(
      <AdministrationDetail presidency={null} isLoading={false} />
    );

    // PresidentCard shows its empty state
    expect(screen.getByText(/no presidential data available/i)).toBeInTheDocument();
  });
});
