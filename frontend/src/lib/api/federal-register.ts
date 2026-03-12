/**
 * Federal Register Search API Client
 *
 * Client for searching and importing Federal Register documents via the backend proxy.
 */

import { backendClient } from './client';
import type {
  FederalRegisterSearchParams,
  FederalRegisterSearchResponse,
  FederalRegisterDocumentDetail,
  FederalRegisterAgency,
  FederalRegisterImportRequest,
  FederalRegisterImportResult,
} from '@/types/federal-register';

// External API proxy calls can be slow — override the default 10s client timeout
const SLOW_TIMEOUT = { timeout: 30000 };

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
    const response = await backendClient.get<FederalRegisterSearchResponse>(
      '/api/admin/search/federal-register/documents',
      {
        ...SLOW_TIMEOUT,
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
    const response = await backendClient.get<FederalRegisterDocumentDetail>(
      `/api/admin/search/federal-register/documents/${encodeURIComponent(documentNumber)}`,
      SLOW_TIMEOUT
    );
    return response.data;
  },

  /**
   * Get all Federal Register agencies
   * GET /api/admin/search/federal-register/agencies
   */
  getAgencies: async (): Promise<FederalRegisterAgency[]> => {
    const response = await backendClient.get<FederalRegisterAgency[]>(
      '/api/admin/search/federal-register/agencies',
      SLOW_TIMEOUT
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
    const response = await backendClient.post<FederalRegisterImportResult>(
      '/api/admin/import/federal-register/document',
      request,
      SLOW_TIMEOUT
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
    const response = await backendClient.get<{
      exists: boolean;
      id: string | null;
      title: string | null;
    }>(`/api/admin/import/federal-register/document/${encodeURIComponent(documentNumber)}/exists`, SLOW_TIMEOUT);
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
