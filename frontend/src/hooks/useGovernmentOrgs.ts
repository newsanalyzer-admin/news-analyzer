/**
 * Government Organizations React Query Hooks
 *
 * React Query hooks for government organization sync operations.
 */

import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import type { GovOrgSyncStatus, GovOrgSyncResult, CsvImportResult } from '@/types/government-org';

const API_BASE = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080';

/**
 * Query key factory for government organizations
 */
export const govOrgKeys = {
  all: ['government-organizations'] as const,
  syncStatus: () => [...govOrgKeys.all, 'sync-status'] as const,
};

/**
 * Fetch government organization sync status from the API
 */
async function fetchGovOrgSyncStatus(): Promise<GovOrgSyncStatus> {
  const response = await fetch(`${API_BASE}/api/government-organizations/sync/status`);
  if (!response.ok) {
    throw new Error('Failed to fetch government organization sync status');
  }
  return response.json();
}

/**
 * Trigger government organization sync from Federal Register API
 */
async function triggerGovOrgSync(): Promise<GovOrgSyncResult> {
  const response = await fetch(`${API_BASE}/api/government-organizations/sync/federal-register`, {
    method: 'POST',
  });
  if (!response.ok) {
    const errorData = await response.json().catch(() => ({}));
    throw new Error(errorData.errorMessages?.[0] || 'Government organization sync failed');
  }
  return response.json();
}

/**
 * Hook to fetch government organization sync status
 */
export function useGovernmentOrgSyncStatus() {
  return useQuery({
    queryKey: govOrgKeys.syncStatus(),
    queryFn: fetchGovOrgSyncStatus,
    staleTime: 60 * 1000, // 60 seconds
    refetchOnWindowFocus: true,
  });
}

/**
 * Hook to trigger government organization sync (admin only)
 */
export function useGovernmentOrgSync() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: triggerGovOrgSync,
    onSuccess: () => {
      // Invalidate sync status to refresh counts
      queryClient.invalidateQueries({ queryKey: govOrgKeys.syncStatus() });
      // Also invalidate any government organization list queries
      queryClient.invalidateQueries({ queryKey: govOrgKeys.all });
    },
  });
}

/**
 * Import government organizations from CSV file
 */
async function importGovOrgsFromCsv(file: File): Promise<CsvImportResult> {
  const formData = new FormData();
  formData.append('file', file);

  const response = await fetch(`${API_BASE}/api/government-organizations/import/csv`, {
    method: 'POST',
    body: formData,
  });

  const data = await response.json();

  if (!response.ok) {
    // Return the result which contains validation errors
    return data as CsvImportResult;
  }

  return data as CsvImportResult;
}

/**
 * Hook to import government organizations from CSV (admin only)
 */
export function useGovOrgCsvImport() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: importGovOrgsFromCsv,
    onSuccess: (data) => {
      if (data.success) {
        // Invalidate sync status to refresh counts
        queryClient.invalidateQueries({ queryKey: govOrgKeys.syncStatus() });
        // Also invalidate any government organization list queries
        queryClient.invalidateQueries({ queryKey: govOrgKeys.all });
      }
    },
  });
}
