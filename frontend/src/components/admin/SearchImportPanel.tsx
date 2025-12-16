'use client';

/**
 * SearchImportPanel Component
 *
 * Reusable component for searching external APIs and importing records.
 * Supports configurable filters, duplicate detection, and merge conflicts.
 */

import { useState, useCallback } from 'react';
import { Search, Loader2, AlertCircle, SearchX, RefreshCw } from 'lucide-react';
import { Input } from '@/components/ui/input';
import { Button } from '@/components/ui/button';
import { ScrollArea } from '@/components/ui/scroll-area';
import { useToast } from '@/hooks/use-toast';
import { useSearchImport } from '@/hooks/useSearchImport';
import { SearchFilters } from './SearchFilters';
import { SearchResultCard } from './SearchResultCard';
import { ImportPreviewModal } from './ImportPreviewModal';
import { MergeConflictModal } from './MergeConflictModal';
import type {
  SearchImportPanelProps,
  FilterValues,
  SearchResult,
  ImportPreviewData,
  MergeConflictData,
  MergeResolution,
  ImportResult,
} from '@/types/search-import';

/**
 * SearchImportPanel - Main reusable search/import component
 */
export function SearchImportPanel<T extends Record<string, unknown>>({
  apiName,
  searchEndpoint,
  filterConfig,
  resultRenderer,
  onImport,
  duplicateChecker,
  getExistingRecord,
  onMerge,
  searchPlaceholder = 'Search...',
  debounceMs = 300,
  pageSize = 10,
  emptyMessage = 'No results found. Try a different search term.',
}: SearchImportPanelProps<T>) {
  const { toast } = useToast();

  // Search state
  const [searchQuery, setSearchQuery] = useState('');
  const [currentPage, setCurrentPage] = useState(1);
  const [filterValues, setFilterValues] = useState<FilterValues>(() => {
    const initial: FilterValues = {};
    filterConfig.forEach((f) => {
      if (f.defaultValue !== undefined) {
        initial[f.id] = f.defaultValue;
      } else if (f.type === 'multi-select') {
        initial[f.id] = [];
      } else if (f.type === 'date-range') {
        initial[f.id] = {};
      } else {
        initial[f.id] = '';
      }
    });
    return initial;
  });

  // Modal state
  const [previewData, setPreviewData] = useState<ImportPreviewData<T> | null>(null);
  const [conflictData, setConflictData] = useState<MergeConflictData<T> | null>(null);
  const [isPreviewOpen, setIsPreviewOpen] = useState(false);
  const [isConflictOpen, setIsConflictOpen] = useState(false);

  // Import state
  const [importingId, setImportingId] = useState<string | null>(null);
  const [isImporting, setIsImporting] = useState(false);
  const [isMerging, setIsMerging] = useState(false);

  // Search hook
  const {
    results,
    total,
    isLoading,
    isFetching,
    isError,
    error,
    refetch,
  } = useSearchImport<T>({
    endpoint: searchEndpoint,
    query: searchQuery,
    page: currentPage,
    pageSize,
    filters: filterValues,
    debounceMs,
    enabled: searchQuery.length > 0,
  });

  // Handle filter changes
  const handleFilterChange = useCallback((values: FilterValues) => {
    setFilterValues(values);
    setCurrentPage(1); // Reset to first page on filter change
  }, []);

  // Handle filter clear
  const handleFilterClear = useCallback(() => {
    setCurrentPage(1);
  }, []);

  // Handle preview click
  const handlePreview = useCallback((result: SearchResult<T>) => {
    setPreviewData({
      data: result.data,
      source: result.source,
    });
    setIsPreviewOpen(true);
  }, []);

  // Handle direct import click
  const handleImportClick = useCallback(async (result: SearchResult<T>) => {
    const itemId = (result.data as Record<string, unknown>).id as string ||
                   JSON.stringify(result.data).slice(0, 20);
    setImportingId(itemId);

    try {
      // Check for duplicates first
      if (duplicateChecker) {
        const existingId = await duplicateChecker(result.data);
        if (existingId && getExistingRecord) {
          const existing = await getExistingRecord(existingId);
          if (existing) {
            // Find differing fields
            const differingFields = (Object.keys(result.data) as (keyof T)[]).filter(
              (key) => JSON.stringify(result.data[key]) !== JSON.stringify(existing[key])
            );

            setConflictData({
              existing,
              incoming: result.data,
              source: result.source,
              differingFields,
            });
            setIsConflictOpen(true);
            setImportingId(null);
            return;
          }
        }
      }

      // No duplicate, proceed with import
      const importResult = await onImport(result.data, result.source);

      if (importResult.error) {
        toast({
          title: 'Import Failed',
          description: importResult.error,
          variant: 'destructive',
        });
      } else {
        toast({
          title: 'Import Successful',
          description: `Record ${importResult.created ? 'created' : 'updated'} successfully.`,
        });
        refetch();
      }
    } catch (err) {
      toast({
        title: 'Import Failed',
        description: err instanceof Error ? err.message : 'An error occurred during import.',
        variant: 'destructive',
      });
    } finally {
      setImportingId(null);
    }
  }, [duplicateChecker, getExistingRecord, onImport, refetch, toast]);

  // Handle compare click (when duplicate is detected)
  const handleCompare = useCallback(async (result: SearchResult<T>) => {
    if (!result.duplicateId || !getExistingRecord) return;

    try {
      const existing = await getExistingRecord(result.duplicateId);
      if (existing) {
        const differingFields = (Object.keys(result.data) as (keyof T)[]).filter(
          (key) => JSON.stringify(result.data[key]) !== JSON.stringify(existing[key])
        );

        setConflictData({
          existing,
          incoming: result.data,
          source: result.source,
          differingFields,
        });
        setIsConflictOpen(true);
      }
    } catch (err) {
      toast({
        title: 'Error',
        description: 'Failed to load existing record for comparison.',
        variant: 'destructive',
      });
    }
  }, [getExistingRecord, toast]);

  // Handle import from preview modal
  const handleConfirmImport = useCallback(async (data: T) => {
    if (!previewData) return;

    setIsImporting(true);
    try {
      const importResult = await onImport(data, previewData.source);

      if (importResult.error) {
        toast({
          title: 'Import Failed',
          description: importResult.error,
          variant: 'destructive',
        });
      } else {
        toast({
          title: 'Import Successful',
          description: `Record ${importResult.created ? 'created' : 'updated'} successfully.`,
        });
        setIsPreviewOpen(false);
        setPreviewData(null);
        refetch();
      }
    } catch (err) {
      toast({
        title: 'Import Failed',
        description: err instanceof Error ? err.message : 'An error occurred during import.',
        variant: 'destructive',
      });
    } finally {
      setIsImporting(false);
    }
  }, [previewData, onImport, refetch, toast]);

  // Handle merge conflict resolution
  const handleMergeResolve = useCallback(async (
    resolution: MergeResolution,
    selectedFields?: (keyof T)[]
  ) => {
    if (!conflictData) return;

    setIsMerging(true);
    try {
      let importResult: ImportResult;

      if (resolution === 'keep-existing') {
        // No action needed, just close
        setIsConflictOpen(false);
        setConflictData(null);
        setIsMerging(false);
        return;
      } else if (resolution === 'replace-with-new') {
        // Replace existing with incoming
        importResult = await onImport(conflictData.incoming, conflictData.source);
      } else if (resolution === 'merge-selected' && selectedFields && onMerge) {
        // Merge selected fields
        importResult = await onMerge(
          conflictData.existing,
          conflictData.incoming,
          selectedFields
        );
      } else {
        throw new Error('Invalid merge resolution');
      }

      if (importResult.error) {
        toast({
          title: 'Merge Failed',
          description: importResult.error,
          variant: 'destructive',
        });
      } else {
        toast({
          title: 'Merge Successful',
          description: 'Record updated successfully.',
        });
        setIsConflictOpen(false);
        setConflictData(null);
        refetch();
      }
    } catch (err) {
      toast({
        title: 'Merge Failed',
        description: err instanceof Error ? err.message : 'An error occurred during merge.',
        variant: 'destructive',
      });
    } finally {
      setIsMerging(false);
    }
  }, [conflictData, onImport, onMerge, refetch, toast]);

  // Calculate pagination
  const totalPages = Math.ceil(total / pageSize);
  const hasResults = results.length > 0;
  const showEmptyState = !isLoading && !isError && searchQuery.length > 0 && !hasResults;
  const showInitialState = searchQuery.length === 0;

  return (
    <div className="flex h-full flex-col space-y-4">
      {/* Search input */}
      <div className="relative">
        <Search className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
        <Input
          type="search"
          placeholder={searchPlaceholder}
          value={searchQuery}
          onChange={(e) => {
            setSearchQuery(e.target.value);
            setCurrentPage(1);
          }}
          className="pl-10"
        />
      </div>

      {/* Filters */}
      {filterConfig.length > 0 && (
        <SearchFilters
          filters={filterConfig}
          values={filterValues}
          onChange={handleFilterChange}
          onClear={handleFilterClear}
        />
      )}

      {/* Results area */}
      <div className="flex-1">
        {/* Loading state */}
        {isLoading && (
          <div className="flex h-40 items-center justify-center">
            <Loader2 className="h-8 w-8 animate-spin text-muted-foreground" />
            <span className="ml-2 text-muted-foreground">Searching {apiName}...</span>
          </div>
        )}

        {/* Error state */}
        {isError && (
          <div className="flex h-40 flex-col items-center justify-center space-y-4">
            <AlertCircle className="h-10 w-10 text-destructive" />
            <div className="text-center">
              <p className="font-medium">Search failed</p>
              <p className="text-sm text-muted-foreground">
                {error?.message || 'An error occurred while searching.'}
              </p>
            </div>
            <Button variant="outline" onClick={() => refetch()}>
              <RefreshCw className="mr-2 h-4 w-4" />
              Retry
            </Button>
          </div>
        )}

        {/* Initial state (no search query) */}
        {showInitialState && !isLoading && !isError && (
          <div className="flex h-40 flex-col items-center justify-center text-muted-foreground">
            <Search className="h-10 w-10 opacity-50" />
            <p className="mt-2">Enter a search term to find records from {apiName}</p>
          </div>
        )}

        {/* Empty state */}
        {showEmptyState && (
          <div className="flex h-40 flex-col items-center justify-center text-muted-foreground">
            <SearchX className="h-10 w-10 opacity-50" />
            <p className="mt-2">{emptyMessage}</p>
          </div>
        )}

        {/* Results list */}
        {hasResults && !isLoading && (
          <>
            <div className="mb-2 flex items-center justify-between text-sm text-muted-foreground">
              <span>
                {total} result{total !== 1 ? 's' : ''} found
                {isFetching && <Loader2 className="ml-2 inline h-3 w-3 animate-spin" />}
              </span>
              {totalPages > 1 && (
                <span>
                  Page {currentPage} of {totalPages}
                </span>
              )}
            </div>

            <ScrollArea className="h-[calc(100vh-400px)] pr-4">
              <div className="space-y-3">
                {results.map((result, index) => {
                  const itemId = (result.data as Record<string, unknown>).id as string ||
                                 `result-${index}`;
                  return (
                    <SearchResultCard
                      key={itemId}
                      result={result}
                      resultRenderer={resultRenderer}
                      onPreview={handlePreview}
                      onImport={handleImportClick}
                      onCompare={getExistingRecord ? handleCompare : undefined}
                      isImporting={importingId === itemId}
                    />
                  );
                })}
              </div>
            </ScrollArea>

            {/* Pagination */}
            {totalPages > 1 && (
              <div className="mt-4 flex items-center justify-center gap-2">
                <Button
                  variant="outline"
                  size="sm"
                  onClick={() => setCurrentPage((p) => Math.max(1, p - 1))}
                  disabled={currentPage === 1 || isFetching}
                >
                  Previous
                </Button>
                <span className="text-sm text-muted-foreground">
                  {currentPage} / {totalPages}
                </span>
                <Button
                  variant="outline"
                  size="sm"
                  onClick={() => setCurrentPage((p) => Math.min(totalPages, p + 1))}
                  disabled={currentPage === totalPages || isFetching}
                >
                  Next
                </Button>
              </div>
            )}
          </>
        )}
      </div>

      {/* Preview Modal */}
      <ImportPreviewModal
        open={isPreviewOpen}
        onClose={() => {
          setIsPreviewOpen(false);
          setPreviewData(null);
        }}
        previewData={previewData}
        onConfirmImport={handleConfirmImport}
        isImporting={isImporting}
      />

      {/* Merge Conflict Modal */}
      <MergeConflictModal
        open={isConflictOpen}
        onClose={() => {
          setIsConflictOpen(false);
          setConflictData(null);
        }}
        conflictData={conflictData}
        onResolve={handleMergeResolve}
        isMerging={isMerging}
      />
    </div>
  );
}
