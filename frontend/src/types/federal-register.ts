/**
 * Federal Register Search Types
 *
 * Type definitions for Federal Register document search and import.
 */

import type { SearchResponse, ImportResult } from './search-import';

/**
 * Federal Register document search result from backend
 */
export interface FederalRegisterSearchResult {
  documentNumber: string;
  title: string;
  documentType: string;
  publicationDate: string; // ISO date string
  agencies: string[];
  htmlUrl: string;
}

/**
 * Federal Register document detailed information for preview
 */
export interface FederalRegisterDocumentDetail extends FederalRegisterSearchResult {
  documentAbstract?: string;
  effectiveDate?: string;
  signingDate?: string;
  cfrReferences?: CfrReference[];
  docketIds?: string[];
  regulationIdNumber?: string;
  pdfUrl?: string;
}

/**
 * CFR (Code of Federal Regulations) reference
 */
export interface CfrReference {
  title?: number;
  part?: number;
  section?: string;
}

/**
 * Federal Register agency from the agencies endpoint
 */
export interface FederalRegisterAgency {
  id: number;
  name: string;
  shortName?: string;
  slug?: string;
  url?: string;
  parentId?: number;
}

/**
 * Filter values for Federal Register search
 */
export interface FederalRegisterFilters {
  keyword?: string;
  agencyId?: number;
  documentType?: string;
  dateFrom?: string;
  dateTo?: string;
}

/**
 * Search parameters for Federal Register API
 */
export interface FederalRegisterSearchParams {
  keyword?: string;
  agencyId?: number;
  documentType?: string;
  dateFrom?: string;
  dateTo?: string;
  page?: number;
  pageSize?: number;
}

/**
 * Response from Federal Register search endpoint
 */
export interface FederalRegisterSearchResponse extends SearchResponse<FederalRegisterSearchResult> {
  // No additional rate limit fields for Federal Register
}

/**
 * Import request for Federal Register document
 */
export interface FederalRegisterImportRequest {
  documentNumber: string;
  forceOverwrite?: boolean;
}

/**
 * Import result specific to Federal Register documents
 */
export interface FederalRegisterImportResult extends ImportResult {
  documentNumber?: string;
  title?: string;
  linkedAgencies?: number;
  linkedAgencyNames?: string[];
  unmatchedAgencyNames?: string[];
}

/**
 * Document type options
 */
export const DOCUMENT_TYPE_OPTIONS = [
  { value: 'Rule', label: 'Rule' },
  { value: 'Proposed Rule', label: 'Proposed Rule' },
  { value: 'Notice', label: 'Notice' },
  { value: 'Presidential Document', label: 'Presidential Document' },
] as const;

/**
 * Format a CFR reference for display
 */
export function formatCfrReference(ref: CfrReference): string {
  const parts: string[] = [];
  if (ref.title) parts.push(`${ref.title} CFR`);
  if (ref.part) parts.push(`Part ${ref.part}`);
  if (ref.section) parts.push(`Section ${ref.section}`);
  return parts.join(' ');
}

/**
 * Format a date string for display
 */
export function formatDate(dateStr: string | undefined): string {
  if (!dateStr) return 'N/A';
  try {
    return new Date(dateStr).toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
    });
  } catch {
    return dateStr;
  }
}
