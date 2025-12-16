'use client';

import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import axios from 'axios';

const API_BASE = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080';

export interface UsCodeImportResult {
  titleNumber: number | null;
  releasePoint: string;
  sectionsInserted: number;
  sectionsUpdated: number;
  sectionsFailed: number;
  totalProcessed: number;
  startedAt: string;
  completedAt: string | null;
  success: boolean;
  errorMessage: string | null;
  errors: string[];
  durationFormatted: string;
}

export interface UsCodeImportStatus {
  inProgress: boolean;
  lastImport?: {
    startedAt: string;
    completedAt: string | null;
    titleNumber: number | null;
    releasePoint: string;
    sectionsInserted: number;
    sectionsUpdated: number;
    sectionsFailed: number;
    totalProcessed: number;
    success: boolean;
    duration: string;
  };
}

export function useUsCodeImport() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async (file: File): Promise<UsCodeImportResult> => {
      const formData = new FormData();
      formData.append('file', file);

      const response = await axios.post<UsCodeImportResult>(
        `${API_BASE}/api/admin/import/statutes/upload`,
        formData,
        {
          headers: {
            'Content-Type': 'multipart/form-data',
          },
          timeout: 300000, // 5 minutes for large files
        }
      );

      return response.data;
    },
    onSuccess: () => {
      // Invalidate related queries to refresh data
      queryClient.invalidateQueries({ queryKey: ['statutes'] });
      queryClient.invalidateQueries({ queryKey: ['statute-stats'] });
      queryClient.invalidateQueries({ queryKey: ['statute-titles'] });
      queryClient.invalidateQueries({ queryKey: ['uscode-import-status'] });
    },
  });
}

export function useUsCodeImportStatus() {
  return useQuery({
    queryKey: ['uscode-import-status'],
    queryFn: async (): Promise<UsCodeImportStatus> => {
      const response = await axios.get<UsCodeImportStatus>(
        `${API_BASE}/api/admin/import/statutes/status`
      );
      return response.data;
    },
    refetchInterval: (data) => {
      // Poll more frequently when import is in progress
      return data?.state.data?.inProgress ? 2000 : false;
    },
  });
}
