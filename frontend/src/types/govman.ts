/**
 * GOVMAN Import Types
 *
 * Type definitions for GOVMAN XML import operations.
 */

/**
 * Result of a GOVMAN XML import operation.
 * Matches the backend GovmanImportResult DTO.
 */
export interface GovmanImportResult {
  startTime: string | null;
  endTime: string | null;
  total: number;
  imported: number;
  updated: number;
  skipped: number;
  errors: number;
  errorDetails: string[];
  durationSeconds: number | null;
  successRate: number;
}

/**
 * Status of GOVMAN import operations.
 * Returned by GET /api/admin/import/govman/status
 */
export interface GovmanImportStatus {
  inProgress: boolean;
  lastImport?: {
    startTime: string;
    endTime: string;
    total: number;
    imported: number;
    updated: number;
    skipped: number;
    errors: number;
    durationSeconds: number;
    successRate: number;
  };
}
