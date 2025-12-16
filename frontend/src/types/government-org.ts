/**
 * Government Organization Types
 *
 * Type definitions for government organization sync status and results.
 */

/**
 * Government organization sync status from the backend API
 */
export interface GovOrgSyncStatus {
  lastSync: string | null;
  totalOrganizations: number;
  countByBranch: {
    executive: number;
    legislative: number;
    judicial: number;
  };
  federalRegisterAvailable: boolean;
}

/**
 * Result of a government organization sync operation
 */
export interface GovOrgSyncResult {
  added: number;
  updated: number;
  skipped: number;
  errors: number;
  errorMessages: string[];
}

/**
 * Validation error from CSV import
 */
export interface CsvValidationError {
  line: number;
  field: string;
  value: string;
  message: string;
}

/**
 * Result of a CSV import operation
 */
export interface CsvImportResult {
  success: boolean;
  added: number;
  updated: number;
  skipped: number;
  errors: number;
  validationErrors: CsvValidationError[];
  errorMessages: string[];
}
