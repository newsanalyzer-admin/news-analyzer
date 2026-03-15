import { describe, it, expect, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { AdministrationExecutiveOrders } from '../AdministrationExecutiveOrders';
import type { ExecutiveOrderPage } from '@/hooks/usePresidencySync';

// Mock the hook
const mockUsePresidencyExecutiveOrders = vi.fn();
vi.mock('@/hooks/usePresidencySync', () => ({
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

const mockEOPage: ExecutiveOrderPage = {
  content: [
    {
      id: 'eo-1',
      presidencyId: 'pres-1',
      eoNumber: 14001,
      title: 'Protecting American Jobs',
      signingDate: '2025-01-20',
      summary: null,
      federalRegisterCitation: '90 FR 1234',
      federalRegisterUrl: 'https://example.com/eo1',
      status: 'ACTIVE',
      revokedByEo: null,
    },
    {
      id: 'eo-2',
      presidencyId: 'pres-1',
      eoNumber: 14002,
      title: 'Border Security Measures',
      signingDate: '2025-01-21',
      summary: null,
      federalRegisterCitation: null,
      federalRegisterUrl: null,
      status: 'REVOKED',
      revokedByEo: 14010,
    },
  ],
  totalElements: 2,
  totalPages: 1,
  size: 10,
  number: 0,
};

const mockMultiPageEO: ExecutiveOrderPage = {
  content: [mockEOPage.content[0]],
  totalElements: 15,
  totalPages: 2,
  size: 10,
  number: 0,
};

describe('AdministrationExecutiveOrders', () => {
  beforeEach(() => {
    mockUsePresidencyExecutiveOrders.mockReset();
  });

  describe('Loading State', () => {
    it('renders loading skeletons when loading', () => {
      mockUsePresidencyExecutiveOrders.mockReturnValue({
        data: undefined,
        isLoading: true,
        error: null,
      });

      renderWithQueryClient(<AdministrationExecutiveOrders presidencyId="pres-1" />);

      expect(document.querySelectorAll('.animate-pulse').length).toBeGreaterThan(0);
    });
  });

  describe('Empty State', () => {
    it('renders empty state when no EOs', () => {
      mockUsePresidencyExecutiveOrders.mockReturnValue({
        data: { content: [], totalElements: 0, totalPages: 0, size: 10, number: 0 },
        isLoading: false,
        error: null,
      });

      renderWithQueryClient(<AdministrationExecutiveOrders presidencyId="pres-1" />);

      expect(screen.getByText(/no executive orders recorded/i)).toBeInTheDocument();
    });
  });

  describe('Error State', () => {
    it('renders error message on fetch failure', () => {
      mockUsePresidencyExecutiveOrders.mockReturnValue({
        data: undefined,
        isLoading: false,
        error: new Error('Network error'),
      });

      renderWithQueryClient(<AdministrationExecutiveOrders presidencyId="pres-1" />);

      expect(screen.getByText(/failed to load executive orders/i)).toBeInTheDocument();
    });
  });

  describe('EO Display', () => {
    it('renders executive orders with number and title', () => {
      mockUsePresidencyExecutiveOrders.mockReturnValue({
        data: mockEOPage,
        isLoading: false,
        error: null,
      });

      renderWithQueryClient(<AdministrationExecutiveOrders presidencyId="pres-1" />);

      expect(screen.getByText('EO 14001')).toBeInTheDocument();
      expect(screen.getByText('Protecting American Jobs')).toBeInTheDocument();
      expect(screen.getByText('EO 14002')).toBeInTheDocument();
      expect(screen.getByText('Border Security Measures')).toBeInTheDocument();
    });

    it('renders signing date', () => {
      mockUsePresidencyExecutiveOrders.mockReturnValue({
        data: mockEOPage,
        isLoading: false,
        error: null,
      });

      renderWithQueryClient(<AdministrationExecutiveOrders presidencyId="pres-1" />);

      expect(screen.getByText('2025-01-20')).toBeInTheDocument();
    });

    it('renders REVOKED status badge', () => {
      mockUsePresidencyExecutiveOrders.mockReturnValue({
        data: mockEOPage,
        isLoading: false,
        error: null,
      });

      renderWithQueryClient(<AdministrationExecutiveOrders presidencyId="pres-1" />);

      expect(screen.getByText('REVOKED')).toBeInTheDocument();
    });

    it('renders Federal Register link when available', () => {
      mockUsePresidencyExecutiveOrders.mockReturnValue({
        data: mockEOPage,
        isLoading: false,
        error: null,
      });

      renderWithQueryClient(<AdministrationExecutiveOrders presidencyId="pres-1" />);

      const frLink = screen.getByText('FR');
      expect(frLink.closest('a')).toHaveAttribute('href', 'https://example.com/eo1');
    });

    it('renders total count', () => {
      mockUsePresidencyExecutiveOrders.mockReturnValue({
        data: mockEOPage,
        isLoading: false,
        error: null,
      });

      renderWithQueryClient(<AdministrationExecutiveOrders presidencyId="pres-1" />);

      expect(screen.getByText('2 orders')).toBeInTheDocument();
    });
  });

  describe('Pagination', () => {
    it('renders pagination controls when multiple pages', () => {
      mockUsePresidencyExecutiveOrders.mockReturnValue({
        data: mockMultiPageEO,
        isLoading: false,
        error: null,
      });

      renderWithQueryClient(<AdministrationExecutiveOrders presidencyId="pres-1" />);

      expect(screen.getByText('Page 1 of 2')).toBeInTheDocument();
      expect(screen.getByText('Previous')).toBeDisabled();
      expect(screen.getByText('Next')).not.toBeDisabled();
    });

    it('does not render pagination for single page', () => {
      mockUsePresidencyExecutiveOrders.mockReturnValue({
        data: mockEOPage,
        isLoading: false,
        error: null,
      });

      renderWithQueryClient(<AdministrationExecutiveOrders presidencyId="pres-1" />);

      expect(screen.queryByText(/page \d+ of/i)).not.toBeInTheDocument();
    });

    it('calls hook with next page when Next clicked', async () => {
      mockUsePresidencyExecutiveOrders.mockReturnValue({
        data: mockMultiPageEO,
        isLoading: false,
        error: null,
      });

      renderWithQueryClient(<AdministrationExecutiveOrders presidencyId="pres-1" />);

      const user = userEvent.setup();
      await user.click(screen.getByText('Next'));

      // The hook should have been called with page=1
      const lastCall = mockUsePresidencyExecutiveOrders.mock.calls.at(-1);
      expect(lastCall?.[1]).toBe(1);
    });
  });
});
