/**
 * Federal Register Search API Client
 *
 * Client for searching and importing Federal Register documents via the backend proxy.
 */

import axios from 'axios';
import type {
  FederalRegisterSearchParams,
  FederalRegisterSearchResponse,
  FederalRegisterDocumentDetail,
  FederalRegisterAgency,
  FederalRegisterImportRequest,
  FederalRegisterImportResult,
} from '@/types/federal-register';

const BACKEND_URL = process.env.NEXT_PUBLIC_BACKEND_URL || 'http://localhost:8080';

const api = axios.create({
  baseURL: BACKEND_URL,
  timeout: 30000,
});

/**
 * Federal Register API client
 */
export const federalRegisterApi = {
  /**
   * Search Federal Register documents
   * GET /api/admin/search/federal-register/documents
   */
  searchDocuments: async (
    params: FederalRegisterSearchParams
  ): Promise<FederalRegisterSearchResponse> => {
    const response = await api.get<FederalRegisterSearchResponse>(
      '/api/admin/search/federal-register/documents',
      {
        params: {
          keyword: params.keyword || undefined,
          agencyId: params.agencyId || undefined,
          documentType: params.documentType || undefined,
          dateFrom: params.dateFrom || undefined,
          dateTo: params.dateTo || undefined,
          page: params.page || 1,
          pageSize: params.pageSize || 20,
        },
      }
    );
    return response.data;
  },

  /**
   * Get document details from Federal Register
   * GET /api/admin/search/federal-register/documents/{documentNumber}
   */
  getDocumentDetail: async (documentNumber: string): Promise<FederalRegisterDocumentDetail> => {
    const response = await api.get<FederalRegisterDocumentDetail>(
      `/api/admin/search/federal-register/documents/${encodeURIComponent(documentNumber)}`
    );
    return response.data;
  },

  /**
   * Get all Federal Register agencies
   * GET /api/admin/search/federal-register/agencies
   */
  getAgencies: async (): Promise<FederalRegisterAgency[]> => {
    const response = await api.get<FederalRegisterAgency[]>(
      '/api/admin/search/federal-register/agencies'
    );
    return response.data;
  },

  /**
   * Import document from Federal Register
   * POST /api/admin/import/federal-register/document
   */
  importDocument: async (
    request: FederalRegisterImportRequest
  ): Promise<FederalRegisterImportResult> => {
    const response = await api.post<FederalRegisterImportResult>(
      '/api/admin/import/federal-register/document',
      request
    );
    return response.data;
  },

  /**
   * Check if document exists locally
   * GET /api/admin/import/federal-register/document/{documentNumber}/exists
   */
  checkDocumentExists: async (
    documentNumber: string
  ): Promise<{ exists: boolean; id: string | null; title: string | null }> => {
    const response = await api.get<{
      exists: boolean;
      id: string | null;
      title: string | null;
    }>(`/api/admin/import/federal-register/document/${encodeURIComponent(documentNumber)}/exists`);
    return response.data;
  },
};

/**
 * Helper to convert FederalRegisterSearchResult to a format compatible with SearchImportPanel
 */
export function toSearchResult(
  doc: FederalRegisterSearchResponse['results'][0]['data'],
  duplicateId?: string
) {
  return {
    data: doc,
    source: 'Federal Register',
    sourceUrl: doc.htmlUrl || `https://www.federalregister.gov/d/${doc.documentNumber}`,
    duplicateId,
  };
}
