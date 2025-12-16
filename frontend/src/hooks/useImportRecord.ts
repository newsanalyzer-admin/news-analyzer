/**
 * useImportRecord Hook
 *
 * React Query mutation hook for importing records from external sources.
 * Supports generic payload types and automatic query invalidation.
 */

import { useMutation, useQueryClient } from '@tanstack/react-query';
import type { ImportResult } from '@/types/search-import';
import { searchImportKeys } from './useSearchImport';

const API_BASE = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080';

/**
 * Import request payload
 */
export interface ImportPayload<T> {
  /** The data to import */
  data: T;
  /** Source of the data (e.g., "Congress.gov") */
  source: string;
}

/**
 * Perform import request
 */
async function importRecord<T>(
  endpoint: string,
  payload: ImportPayload<T>
): Promise<ImportResult> {
  const response = await fetch(`${API_BASE}${endpoint}`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(payload),
  });

  const data = await response.json();

  if (!response.ok) {
    throw new Error(data.message || data.error || 'Import failed');
  }

  return data;
}

/**
 * Hook options for useImportRecord
 */
export interface UseImportRecordOptions {
  /** Import endpoint (e.g., "/api/admin/import/members") */
  endpoint: string;
  /** Query keys to invalidate on success */
  invalidateKeys?: readonly unknown[][];
  /** Called on successful import */
  onSuccess?: (result: ImportResult) => void;
  /** Called on import error */
  onError?: (error: Error) => void;
}

/**
 * Hook to import a record from external source
 */
export function useImportRecord<T>({
  endpoint,
  invalidateKeys = [],
  onSuccess,
  onError,
}: UseImportRecordOptions) {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (payload: ImportPayload<T>) => importRecord<T>(endpoint, payload),
    onSuccess: (result) => {
      // Invalidate search queries to refresh results
      queryClient.invalidateQueries({ queryKey: searchImportKeys.all });

      // Invalidate additional specified query keys
      invalidateKeys.forEach((key) => {
        queryClient.invalidateQueries({ queryKey: key });
      });

      onSuccess?.(result);
    },
    onError: (error: Error) => {
      onError?.(error);
    },
  });
}

/**
 * Merge request payload
 */
export interface MergePayload<T> {
  /** ID of the existing record */
  existingId: string;
  /** The incoming data to merge */
  incoming: T;
  /** Fields to take from the incoming record */
  selectedFields: (keyof T)[];
  /** Source of the incoming data */
  source: string;
}

/**
 * Perform merge request
 */
async function mergeRecord<T>(
  endpoint: string,
  payload: MergePayload<T>
): Promise<ImportResult> {
  const response = await fetch(`${API_BASE}${endpoint}`, {
    method: 'PUT',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(payload),
  });

  const data = await response.json();

  if (!response.ok) {
    throw new Error(data.message || data.error || 'Merge failed');
  }

  return data;
}

/**
 * Hook options for useMergeRecord
 */
export interface UseMergeRecordOptions {
  /** Merge endpoint (e.g., "/api/admin/merge/members") */
  endpoint: string;
  /** Query keys to invalidate on success */
  invalidateKeys?: readonly unknown[][];
  /** Called on successful merge */
  onSuccess?: (result: ImportResult) => void;
  /** Called on merge error */
  onError?: (error: Error) => void;
}

/**
 * Hook to merge an incoming record with an existing one
 */
export function useMergeRecord<T>({
  endpoint,
  invalidateKeys = [],
  onSuccess,
  onError,
}: UseMergeRecordOptions) {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (payload: MergePayload<T>) => mergeRecord<T>(endpoint, payload),
    onSuccess: (result) => {
      // Invalidate search queries to refresh results
      queryClient.invalidateQueries({ queryKey: searchImportKeys.all });

      // Invalidate additional specified query keys
      invalidateKeys.forEach((key) => {
        queryClient.invalidateQueries({ queryKey: key });
      });

      onSuccess?.(result);
    },
    onError: (error: Error) => {
      onError?.(error);
    },
  });
}
