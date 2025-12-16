/**
 * PLUM (Policy and Supporting Positions) Types
 *
 * Types for executive branch appointee data from OPM.
 */

/**
 * Status of PLUM sync operation
 */
export interface PlumSyncStatus {
  inProgress: boolean;
  csvUrl: string;
  lastImport?: PlumLastImport;
}

/**
 * Summary of last PLUM import
 */
export interface PlumLastImport {
  startTime: string;
  endTime: string;
  totalRecords: number;
  personsCreated: number;
  personsUpdated: number;
  positionsCreated: number;
  positionsUpdated: number;
  holdingsCreated: number;
  holdingsUpdated: number;
  errors: number;
  durationSeconds: number;
  successRate: number;
}

/**
 * Full PLUM import result with error details
 */
export interface PlumImportResult {
  startTime: string;
  endTime: string;
  totalRecords: number;
  personsCreated: number;
  personsUpdated: number;
  positionsCreated: number;
  positionsUpdated: number;
  holdingsCreated: number;
  holdingsUpdated: number;
  vacantPositions: number;
  skipped: number;
  errors: number;
  unmatchedAgencies: number;
  errorDetails: PlumImportError[];
  unmatchedAgencyNames: string[];
}

/**
 * Individual import error
 */
export interface PlumImportError {
  lineNumber: number;
  message: string;
  record: string;
}
