import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { PresidencySyncCard } from '../PresidencySyncCard';
import type { PresidencySyncStatus, PresidencySyncResult } from '@/hooks/usePresidencySync';

// Mock the usePresidencySync hooks
const mockRefetch = vi.fn();
const mockMutateAsync = vi.fn();

vi.mock('@/hooks/usePresidencySync', () => ({
  usePresidencySyncStatus: vi.fn(() => ({
    data: null,
    isLoading: false,
    error: null,
    refetch: mockRefetch,
  })),
  usePresidencySync: vi.fn(() => ({
    mutateAsync: mockMutateAsync,
    isPending: false,
  })),
}));

// Mock the useToast hook
const mockToast = vi.fn();
vi.mock('@/hooks/use-toast', () => ({
  useToast: () => ({
    toast: mockToast,
  }),
}));

// Import mocked hooks for manipulation
import { usePresidencySyncStatus, usePresidencySync } from '@/hooks/usePresidencySync';
const mockUsePresidencySyncStatus = vi.mocked(usePresidencySyncStatus);
const mockUsePresidencySync = vi.mocked(usePresidencySync);

describe('PresidencySyncCard', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    mockUsePresidencySyncStatus.mockReturnValue({
      data: null,
      isLoading: false,
      error: null,
      refetch: mockRefetch,
    } as ReturnType<typeof usePresidencySyncStatus>);
    mockUsePresidencySync.mockReturnValue({
      mutateAsync: mockMutateAsync,
      isPending: false,
    } as unknown as ReturnType<typeof usePresidencySync>);
  });

  // ====== Loading State Tests ======

  describe('Loading State', () => {
    it('renders loading skeleton when status is loading', () => {
      mockUsePresidencySyncStatus.mockReturnValue({
        data: null,
        isLoading: true,
        error: null,
        refetch: mockRefetch,
      } as ReturnType<typeof usePresidencySyncStatus>);

      render(<PresidencySyncCard />);

      expect(screen.getByText('Presidential Data')).toBeInTheDocument();
      // Skeletons should be present (we check for the card structure)
      expect(screen.queryByRole('button')).not.toBeInTheDocument();
    });
  });

  // ====== Error State Tests ======

  describe('Error State', () => {
    it('renders error state when status fetch fails', () => {
      mockUsePresidencySyncStatus.mockReturnValue({
        data: null,
        isLoading: false,
        error: new Error('Failed to fetch'),
        refetch: mockRefetch,
      } as ReturnType<typeof usePresidencySyncStatus>);

      render(<PresidencySyncCard />);

      expect(screen.getByText('Failed to load status')).toBeInTheDocument();
      expect(screen.getByRole('button', { name: /retry/i })).toBeInTheDocument();
    });

    it('calls refetch when retry button is clicked', async () => {
      mockUsePresidencySyncStatus.mockReturnValue({
        data: null,
        isLoading: false,
        error: new Error('Failed to fetch'),
        refetch: mockRefetch,
      } as ReturnType<typeof usePresidencySyncStatus>);

      render(<PresidencySyncCard />);

      await userEvent.click(screen.getByRole('button', { name: /retry/i }));
      expect(mockRefetch).toHaveBeenCalled();
    });
  });

  // ====== Never Synced State Tests ======

  describe('Never Synced State', () => {
    it('renders empty state when no presidencies exist', () => {
      mockUsePresidencySyncStatus.mockReturnValue({
        data: {
          inProgress: false,
          totalPresidencies: 0,
        },
        isLoading: false,
        error: null,
        refetch: mockRefetch,
      } as ReturnType<typeof usePresidencySyncStatus>);

      render(<PresidencySyncCard />);

      expect(screen.getByText(/no presidential data imported/i)).toBeInTheDocument();
      expect(screen.getByRole('button', { name: /sync presidential data/i })).toBeInTheDocument();
    });

    it('shows "Never Synced" badge when no data', () => {
      mockUsePresidencySyncStatus.mockReturnValue({
        data: {
          inProgress: false,
          totalPresidencies: 0,
        },
        isLoading: false,
        error: null,
        refetch: mockRefetch,
      } as ReturnType<typeof usePresidencySyncStatus>);

      render(<PresidencySyncCard />);

      expect(screen.getByText('Never Synced')).toBeInTheDocument();
    });
  });

  // ====== Synced State Tests ======

  describe('Synced State', () => {
    const syncedStatus: PresidencySyncStatus = {
      inProgress: false,
      totalPresidencies: 47,
      lastSync: {
        presidenciesAdded: 47,
        presidenciesUpdated: 0,
        totalPresidencies: 47,
        personsAdded: 79,
        personsUpdated: 0,
        vpHoldingsAdded: 52,
        errors: 0,
      },
    };

    it('renders synced state with presidency count', () => {
      mockUsePresidencySyncStatus.mockReturnValue({
        data: syncedStatus,
        isLoading: false,
        error: null,
        refetch: mockRefetch,
      } as ReturnType<typeof usePresidencySyncStatus>);

      render(<PresidencySyncCard />);

      expect(screen.getByText('47')).toBeInTheDocument();
      expect(screen.getByText('Ready')).toBeInTheDocument();
    });

    it('shows persons and VP holdings counts', () => {
      mockUsePresidencySyncStatus.mockReturnValue({
        data: syncedStatus,
        isLoading: false,
        error: null,
        refetch: mockRefetch,
      } as ReturnType<typeof usePresidencySyncStatus>);

      render(<PresidencySyncCard />);

      expect(screen.getByText('79')).toBeInTheDocument(); // Persons
      expect(screen.getByText('52')).toBeInTheDocument(); // VP Holdings
    });
  });

  // ====== Sync In Progress Tests ======

  describe('Sync In Progress', () => {
    it('shows syncing badge when sync is in progress', () => {
      mockUsePresidencySyncStatus.mockReturnValue({
        data: {
          inProgress: true,
          totalPresidencies: 0,
        },
        isLoading: false,
        error: null,
        refetch: mockRefetch,
      } as ReturnType<typeof usePresidencySyncStatus>);

      render(<PresidencySyncCard />);

      expect(screen.getByText('Syncing')).toBeInTheDocument();
    });

    it('disables sync button when sync is in progress', () => {
      mockUsePresidencySyncStatus.mockReturnValue({
        data: {
          inProgress: true,
          totalPresidencies: 0,
        },
        isLoading: false,
        error: null,
        refetch: mockRefetch,
      } as ReturnType<typeof usePresidencySyncStatus>);

      render(<PresidencySyncCard />);

      expect(screen.getByRole('button', { name: /syncing/i })).toBeDisabled();
    });
  });

  // ====== Sync Dialog Tests ======

  describe('Sync Dialog', () => {
    beforeEach(() => {
      mockUsePresidencySyncStatus.mockReturnValue({
        data: {
          inProgress: false,
          totalPresidencies: 0,
        },
        isLoading: false,
        error: null,
        refetch: mockRefetch,
      } as ReturnType<typeof usePresidencySyncStatus>);
    });

    it('opens confirmation dialog when sync button is clicked', async () => {
      render(<PresidencySyncCard />);

      await userEvent.click(screen.getByRole('button', { name: /sync presidential data/i }));

      expect(screen.getByText(/sync presidential data\?/i)).toBeInTheDocument();
      expect(screen.getByText(/this will import all 47 u\.s\. presidencies/i)).toBeInTheDocument();
    });

    it('closes dialog when cancel is clicked', async () => {
      render(<PresidencySyncCard />);

      await userEvent.click(screen.getByRole('button', { name: /sync presidential data/i }));
      await userEvent.click(screen.getByRole('button', { name: /cancel/i }));

      await waitFor(() => {
        expect(screen.queryByText(/sync presidential data\?/i)).not.toBeInTheDocument();
      });
    });

    it('triggers sync when confirm is clicked', async () => {
      mockMutateAsync.mockResolvedValue({
        presidenciesAdded: 47,
        presidenciesUpdated: 0,
        personsAdded: 79,
        personsUpdated: 0,
        vpHoldingsAdded: 52,
        errors: 0,
        errorMessages: [],
      } as PresidencySyncResult);

      render(<PresidencySyncCard />);

      await userEvent.click(screen.getByRole('button', { name: /sync presidential data/i }));
      await userEvent.click(screen.getByRole('button', { name: /confirm sync/i }));

      await waitFor(() => {
        expect(mockMutateAsync).toHaveBeenCalled();
      });
    });

    it('shows success toast on successful sync', async () => {
      mockMutateAsync.mockResolvedValue({
        presidenciesAdded: 47,
        presidenciesUpdated: 0,
        personsAdded: 79,
        personsUpdated: 0,
        vpHoldingsAdded: 52,
        errors: 0,
        errorMessages: [],
      } as PresidencySyncResult);

      render(<PresidencySyncCard />);

      await userEvent.click(screen.getByRole('button', { name: /sync presidential data/i }));
      await userEvent.click(screen.getByRole('button', { name: /confirm sync/i }));

      await waitFor(() => {
        expect(mockToast).toHaveBeenCalledWith(
          expect.objectContaining({
            title: 'Presidential Sync Complete',
            variant: 'success',
          })
        );
      });
    });

    it('shows error toast on sync failure', async () => {
      mockMutateAsync.mockRejectedValue(new Error('Sync failed'));

      render(<PresidencySyncCard />);

      await userEvent.click(screen.getByRole('button', { name: /sync presidential data/i }));
      await userEvent.click(screen.getByRole('button', { name: /confirm sync/i }));

      await waitFor(() => {
        expect(mockToast).toHaveBeenCalledWith(
          expect.objectContaining({
            title: 'Presidential Sync Failed',
            variant: 'destructive',
          })
        );
      });
    });
  });

  // ====== Error Count Display Tests ======

  describe('Error Count Display', () => {
    it('shows error count when sync had errors', () => {
      mockUsePresidencySyncStatus.mockReturnValue({
        data: {
          inProgress: false,
          totalPresidencies: 46,
          lastSync: {
            presidenciesAdded: 46,
            presidenciesUpdated: 0,
            totalPresidencies: 46,
            personsAdded: 78,
            personsUpdated: 0,
            vpHoldingsAdded: 51,
            errors: 1,
          },
        },
        isLoading: false,
        error: null,
        refetch: mockRefetch,
      } as ReturnType<typeof usePresidencySyncStatus>);

      render(<PresidencySyncCard />);

      expect(screen.getByText('1')).toBeInTheDocument();
      expect(screen.getByText('Errors:')).toBeInTheDocument();
    });

    it('shows warning toast when sync completes with errors', async () => {
      mockMutateAsync.mockResolvedValue({
        presidenciesAdded: 46,
        presidenciesUpdated: 0,
        personsAdded: 78,
        personsUpdated: 0,
        vpHoldingsAdded: 51,
        errors: 1,
        errorMessages: ['Error syncing presidency #47'],
      } as PresidencySyncResult);

      render(<PresidencySyncCard />);

      await userEvent.click(screen.getByRole('button', { name: /sync presidential data/i }));
      await userEvent.click(screen.getByRole('button', { name: /confirm sync/i }));

      await waitFor(() => {
        expect(mockToast).toHaveBeenCalledWith(
          expect.objectContaining({
            title: 'Sync Warnings',
          })
        );
      });
    });
  });
});
