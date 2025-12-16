/**
 * Search Import Types
 *
 * Generic type definitions for the reusable SearchImportPanel component.
 * Supports searching external APIs and importing records with duplicate detection.
 */

import type { ReactNode } from 'react';

/**
 * Filter configuration for dynamic filter rendering
 */
export interface FilterConfig {
  /** Unique identifier for the filter */
  id: string;
  /** Display label */
  label: string;
  /** Filter input type */
  type: 'text' | 'select' | 'multi-select' | 'date-range';
  /** Options for select types */
  options?: FilterOption[];
  /** Placeholder text */
  placeholder?: string;
  /** Default value */
  defaultValue?: string | string[];
}

/**
 * Option for select/multi-select filters
 */
export interface FilterOption {
  value: string;
  label: string;
}

/**
 * Filter values state (keyed by filter id)
 */
export type FilterValues = Record<string, string | string[] | DateRange | undefined>;

/**
 * Date range for date-range filter type
 */
export interface DateRange {
  from?: Date;
  to?: Date;
}

/**
 * Search result wrapper with source attribution
 */
export interface SearchResult<T> {
  /** The actual data from external API */
  data: T;
  /** Source name (e.g., "Congress.gov") */
  source: string;
  /** Link to original record in source system */
  sourceUrl?: string;
  /** If duplicate detected, the existing record's ID */
  duplicateId?: string;
}

/**
 * Search response from the API
 */
export interface SearchResponse<T> {
  results: SearchResult<T>[];
  total: number;
  page: number;
  pageSize: number;
}

/**
 * Import preview data for the preview modal
 */
export interface ImportPreviewData<T> {
  /** The data to preview/import */
  data: T;
  /** Source of the data */
  source: string;
  /** Fields that can be edited before import */
  editableFields?: (keyof T)[];
}

/**
 * Merge conflict data for side-by-side comparison
 */
export interface MergeConflictData<T> {
  /** The existing record in our database */
  existing: T;
  /** The new record from external source */
  incoming: T;
  /** Source of the incoming data */
  source: string;
  /** Fields that differ between records */
  differingFields: (keyof T)[];
}

/**
 * Merge resolution options
 */
export type MergeResolution = 'keep-existing' | 'replace-with-new' | 'merge-selected';

/**
 * Props for the SearchImportPanel component
 */
export interface SearchImportPanelProps<T> {
  /** Name of the API source (e.g., "Congress.gov") */
  apiName: string;
  /** Base endpoint for search requests */
  searchEndpoint: string;
  /** Filter configuration for the search UI */
  filterConfig: FilterConfig[];
  /** Render function for displaying each result item */
  resultRenderer: (item: T) => ReactNode;
  /** Function called when user confirms an import */
  onImport: (item: T, source: string) => Promise<ImportResult>;
  /** Optional function to check for duplicates before import */
  duplicateChecker?: (item: T) => Promise<string | null>;
  /** Optional function to get existing record for comparison */
  getExistingRecord?: (id: string) => Promise<T | null>;
  /** Optional function called when merge is selected */
  onMerge?: (existing: T, incoming: T, selectedFields: (keyof T)[]) => Promise<ImportResult>;
  /** Search input placeholder */
  searchPlaceholder?: string;
  /** Debounce delay in milliseconds */
  debounceMs?: number;
  /** Results per page */
  pageSize?: number;
  /** Optional custom empty state message */
  emptyMessage?: string;
}

/**
 * Import result from the import operation
 */
export interface ImportResult {
  /** ID of the created/updated record */
  id: string;
  /** Whether a new record was created */
  created: boolean;
  /** Whether an existing record was updated */
  updated: boolean;
  /** Error message if import failed */
  error?: string;
}

/**
 * Props for SearchResultCard component
 */
export interface SearchResultCardProps<T> {
  /** The search result data */
  result: SearchResult<T>;
  /** Render function for the result content */
  resultRenderer: (item: T) => ReactNode;
  /** Called when Preview button is clicked */
  onPreview: (result: SearchResult<T>) => void;
  /** Called when Import button is clicked */
  onImport: (result: SearchResult<T>) => void;
  /** Called when Compare button is clicked (only if duplicateId exists) */
  onCompare?: (result: SearchResult<T>) => void;
  /** Whether import is currently in progress for this item */
  isImporting?: boolean;
}

/**
 * Props for ImportPreviewModal component
 */
export interface ImportPreviewModalProps<T> {
  /** Whether the modal is open */
  open: boolean;
  /** Called when modal should close */
  onClose: () => void;
  /** The data to preview */
  previewData: ImportPreviewData<T> | null;
  /** Render function for displaying field values */
  fieldRenderer?: (field: keyof T, value: T[keyof T]) => ReactNode;
  /** Fields that can be edited */
  editableFields?: (keyof T)[];
  /** Called when user confirms import */
  onConfirmImport: (data: T) => Promise<void>;
  /** Whether import is in progress */
  isImporting?: boolean;
}

/**
 * Props for MergeConflictModal component
 */
export interface MergeConflictModalProps<T> {
  /** Whether the modal is open */
  open: boolean;
  /** Called when modal should close */
  onClose: () => void;
  /** The merge conflict data */
  conflictData: MergeConflictData<T> | null;
  /** Render function for displaying field values */
  fieldRenderer?: (field: keyof T, value: T[keyof T]) => ReactNode;
  /** Called when user chooses a resolution */
  onResolve: (resolution: MergeResolution, selectedFields?: (keyof T)[]) => Promise<void>;
  /** Whether merge is in progress */
  isMerging?: boolean;
}

/**
 * Props for SearchFilters component
 */
export interface SearchFiltersProps {
  /** Filter configuration */
  filters: FilterConfig[];
  /** Current filter values */
  values: FilterValues;
  /** Called when filter values change */
  onChange: (values: FilterValues) => void;
  /** Called when filters are cleared */
  onClear?: () => void;
}

/**
 * Search query parameters
 */
export interface SearchQueryParams {
  /** Search term */
  q: string;
  /** Current page (1-indexed) */
  page: number;
  /** Items per page */
  pageSize: number;
  /** Filter values */
  filters: FilterValues;
}

/**
 * Search state for the useSearchImport hook
 */
export interface SearchState<T> {
  /** Search results */
  results: SearchResult<T>[];
  /** Total number of results */
  total: number;
  /** Current page */
  page: number;
  /** Whether search is loading */
  isLoading: boolean;
  /** Whether there was an error */
  isError: boolean;
  /** Error object if any */
  error: Error | null;
  /** Refetch function */
  refetch: () => void;
}
