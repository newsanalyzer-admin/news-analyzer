/**
 * GOVMAN Import React Query Hooks
 *
 * React Query hooks for GOVMAN XML import operations.
 */

import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import type { GovmanImportResult, GovmanImportStatus } from '@/types/govman';
import { govOrgKeys } from './useGovernmentOrgs';

const API_BASE = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080';

/**
 * Query key factory for GOVMAN import operations
 */
export const govmanKeys = {
  all: ['govman-import'] as const,
  status: () => [...govmanKeys.all, 'status'] as const,
  lastResult: () => [...govmanKeys.all, 'last-result'] as const,
};

/**
 * Fetch GOVMAN import status from the API
 */
async function fetchGovmanStatus(): Promise<GovmanImportStatus> {
  const response = await fetch(`${API_BASE}/api/admin/import/govman/status`);
  if (!response.ok) {
    throw new Error('Failed to fetch GOVMAN import status');
  }
  return response.json();
}

/**
 * Fetch last GOVMAN import result from the API
 */
async function fetchGovmanLastResult(): Promise<GovmanImportResult | null> {
  const response = await fetch(`${API_BASE}/api/admin/import/govman/last-result`);
  if (response.status === 404) {
    return null;
  }
  if (!response.ok) {
    throw new Error('Failed to fetch last GOVMAN import result');
  }
  return response.json();
}

/**
 * Import government organizations from GOVMAN XML file
 */
async function importGovmanXml(file: File): Promise<GovmanImportResult> {
  const formData = new FormData();
  formData.append('file', file);

  const response = await fetch(`${API_BASE}/api/admin/import/govman`, {
    method: 'POST',
    body: formData,
  });

  // Handle specific error codes
  if (response.status === 409) {
    throw new Error('Import already in progress. Please wait for the current import to complete.');
  }
  if (response.status === 413) {
    throw new Error('File too large. Maximum file size is 10MB.');
  }
  if (response.status === 400) {
    throw new Error('Invalid file. Please upload a valid XML file.');
  }

  const data = await response.json();

  if (!response.ok) {
    throw new Error(data.errorDetails?.[0] || 'GOVMAN import failed');
  }

  return data as GovmanImportResult;
}

/**
 * Hook to fetch GOVMAN import status
 */
export function useGovmanStatus() {
  return useQuery({
    queryKey: govmanKeys.status(),
    queryFn: fetchGovmanStatus,
    staleTime: 30 * 1000, // 30 seconds
    refetchOnWindowFocus: true,
  });
}

/**
 * Hook to fetch last GOVMAN import result
 */
export function useGovmanLastResult() {
  return useQuery({
    queryKey: govmanKeys.lastResult(),
    queryFn: fetchGovmanLastResult,
    staleTime: 60 * 1000, // 60 seconds
  });
}

/**
 * Hook to import GOVMAN XML file (admin only)
 */
export function useGovmanImport() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: importGovmanXml,
    onSuccess: () => {
      // Invalidate GOVMAN status queries
      queryClient.invalidateQueries({ queryKey: govmanKeys.status() });
      queryClient.invalidateQueries({ queryKey: govmanKeys.lastResult() });
      // Also invalidate government organization queries to reflect new data
      queryClient.invalidateQueries({ queryKey: govOrgKeys.all });
    },
  });
}
