'use client';

/**
 * Legislators Repo Search Page
 *
 * Search the unitedstates/congress-legislators GitHub repository and enrich
 * existing Person records with social media and external ID data.
 *
 * Unlike standard import, this is for ENRICHMENT - adding data to records
 * that already exist locally (matched by bioguideId).
 */

import { useState, useCallback } from 'react';
import { Search, Loader2, AlertCircle, SearchX, RefreshCw, Database, ExternalLink } from 'lucide-react';
import { Input } from '@/components/ui/input';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { ScrollArea } from '@/components/ui/scroll-area';
import { useToast } from '@/hooks/use-toast';
import { SearchFilters } from '@/components/admin/SearchFilters';
import { legislatorsSearchApi, formatChamber, formatParty, getPartyColor } from '@/lib/api/legislators-search';
import { US_STATES } from '@/lib/constants/states';
import { EnrichmentPreviewModal } from './EnrichmentPreviewModal';
import type { FilterConfig, FilterValues } from '@/types/search-import';
import type { LegislatorSearchResult, LegislatorsSearchResponse, EnrichmentPreview } from '@/types/legislators-search';

/**
 * Filter configuration for legislators search
 */
const filterConfig: FilterConfig[] = [
  {
    id: 'state',
    label: 'State',
    type: 'select',
    options: US_STATES,
    placeholder: 'All states',
  },
];

/**
 * Legislators Repo Search Page Component
 */
export default function LegislatorsRepoSearchPage() {
  const { toast } = useToast();

  // Search state
  const [searchQuery, setSearchQuery] = useState('');
  const [currentPage, setCurrentPage] = useState(1);
  const [pageSize] = useState(20);
  const [filterValues, setFilterValues] = useState<FilterValues>({
    state: '',
  });

  // Results state
  const [results, setResults] = useState<LegislatorSearchResult[]>([]);
  const [total, setTotal] = useState(0);
  const [isCached, setIsCached] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  const [isError, setIsError] = useState(false);
  const [error, setError] = useState<Error | null>(null);

  // Enrichment state
  const [selectedLegislator, setSelectedLegislator] = useState<LegislatorSearchResult | null>(null);
  const [enrichmentPreview, setEnrichmentPreview] = useState<EnrichmentPreview | null>(null);
  const [isPreviewOpen, setIsPreviewOpen] = useState(false);
  const [isLoadingPreview, setIsLoadingPreview] = useState(false);
  const [isEnriching, setIsEnriching] = useState(false);

  // Perform search
  const performSearch = useCallback(async () => {
    if (!searchQuery.trim()) {
      setResults([]);
      setTotal(0);
      return;
    }

    setIsLoading(true);
    setIsError(false);
    setError(null);

    try {
      const response: LegislatorsSearchResponse = await legislatorsSearchApi.searchLegislators({
        name: searchQuery,
        state: filterValues.state as string || undefined,
        page: currentPage,
        pageSize,
      });

      setResults(response.results.map(r => r.data));
      setTotal(response.total);
      setIsCached(response.cached);
    } catch (err) {
      setIsError(true);
      setError(err instanceof Error ? err : new Error('Search failed'));
      toast({
        title: 'Search Failed',
        description: err instanceof Error ? err.message : 'An error occurred',
        variant: 'destructive',
      });
    } finally {
      setIsLoading(false);
    }
  }, [searchQuery, filterValues.state, currentPage, pageSize, toast]);

  // Handle search input change with debounce
  const handleSearchChange = useCallback((value: string) => {
    setSearchQuery(value);
    setCurrentPage(1);
  }, []);

  // Handle filter changes
  const handleFilterChange = useCallback((values: FilterValues) => {
    setFilterValues(values);
    setCurrentPage(1);
  }, []);

  // Handle search submit
  const handleSearchSubmit = useCallback((e: React.FormEvent) => {
    e.preventDefault();
    performSearch();
  }, [performSearch]);

  // Handle enrich click - load preview
  const handleEnrichClick = useCallback(async (legislator: LegislatorSearchResult) => {
    setSelectedLegislator(legislator);
    setIsLoadingPreview(true);
    setIsPreviewOpen(true);

    try {
      const preview = await legislatorsSearchApi.previewEnrichment(legislator.bioguideId);
      setEnrichmentPreview(preview);
    } catch (err) {
      toast({
        title: 'Preview Failed',
        description: err instanceof Error ? err.message : 'Failed to load enrichment preview',
        variant: 'destructive',
      });
      setIsPreviewOpen(false);
    } finally {
      setIsLoadingPreview(false);
    }
  }, [toast]);

  // Handle confirm enrichment
  const handleConfirmEnrichment = useCallback(async () => {
    if (!selectedLegislator) return;

    setIsEnriching(true);
    try {
      const result = await legislatorsSearchApi.enrichPerson({
        bioguideId: selectedLegislator.bioguideId,
      });

      if (result.error) {
        toast({
          title: 'Enrichment Failed',
          description: result.error,
          variant: 'destructive',
        });
      } else {
        toast({
          title: 'Enrichment Successful',
          description: `Added ${result.fieldsAdded.length} fields to ${result.personName}`,
        });
        setIsPreviewOpen(false);
        setEnrichmentPreview(null);
        setSelectedLegislator(null);
      }
    } catch (err) {
      toast({
        title: 'Enrichment Failed',
        description: err instanceof Error ? err.message : 'An error occurred',
        variant: 'destructive',
      });
    } finally {
      setIsEnriching(false);
    }
  }, [selectedLegislator, toast]);

  // Close preview modal
  const handleClosePreview = useCallback(() => {
    setIsPreviewOpen(false);
    setEnrichmentPreview(null);
    setSelectedLegislator(null);
  }, []);

  // Calculate pagination
  const totalPages = Math.ceil(total / pageSize);
  const hasResults = results.length > 0;
  const showEmptyState = !isLoading && !isError && searchQuery.length > 0 && !hasResults;
  const showInitialState = searchQuery.length === 0;

  return (
    <div className="container mx-auto max-w-6xl py-6 space-y-6">
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <Database className="h-5 w-5" />
            Legislators Repo Search
          </CardTitle>
          <CardDescription>
            Search the{' '}
            <a
              href="https://github.com/unitedstates/congress-legislators"
              target="_blank"
              rel="noopener noreferrer"
              className="text-primary hover:underline inline-flex items-center gap-1"
            >
              unitedstates/congress-legislators
              <ExternalLink className="h-3 w-3" />
            </a>{' '}
            repository and enrich existing Person records with social media and external IDs.
            Records must already exist locally (imported from Congress.gov) to be enriched.
          </CardDescription>
        </CardHeader>
        <CardContent className="space-y-4">
          {/* Search form */}
          <form onSubmit={handleSearchSubmit}>
            <div className="flex gap-2">
              <div className="relative flex-1">
                <Search className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
                <Input
                  type="search"
                  placeholder="Search legislators by name..."
                  value={searchQuery}
                  onChange={(e) => handleSearchChange(e.target.value)}
                  className="pl-10"
                />
              </div>
              <Button type="submit" disabled={isLoading || !searchQuery.trim()}>
                {isLoading ? (
                  <Loader2 className="h-4 w-4 animate-spin" />
                ) : (
                  <Search className="h-4 w-4" />
                )}
                <span className="ml-2">Search</span>
              </Button>
            </div>
          </form>

          {/* Filters */}
          {filterConfig.length > 0 && (
            <SearchFilters
              filters={filterConfig}
              values={filterValues}
              onChange={handleFilterChange}
            />
          )}

          {/* Cache indicator */}
          {isCached && hasResults && (
            <div className="text-sm text-muted-foreground">
              <Badge variant="outline" className="mr-2">Cached</Badge>
              Results from cached data (15 min TTL)
            </div>
          )}

          {/* Results area */}
          <div className="flex-1">
            {/* Loading state */}
            {isLoading && (
              <div className="flex h-40 items-center justify-center">
                <Loader2 className="h-8 w-8 animate-spin text-muted-foreground" />
                <span className="ml-2 text-muted-foreground">Searching Legislators Repo...</span>
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
                <Button variant="outline" onClick={performSearch}>
                  <RefreshCw className="mr-2 h-4 w-4" />
                  Retry
                </Button>
              </div>
            )}

            {/* Initial state */}
            {showInitialState && !isLoading && !isError && (
              <div className="flex h-40 flex-col items-center justify-center text-muted-foreground">
                <Search className="h-10 w-10 opacity-50" />
                <p className="mt-2">Enter a name to search legislators</p>
              </div>
            )}

            {/* Empty state */}
            {showEmptyState && (
              <div className="flex h-40 flex-col items-center justify-center text-muted-foreground">
                <SearchX className="h-10 w-10 opacity-50" />
                <p className="mt-2">No legislators found. Try a different name.</p>
              </div>
            )}

            {/* Results list */}
            {hasResults && !isLoading && (
              <>
                <div className="mb-2 flex items-center justify-between text-sm text-muted-foreground">
                  <span>
                    {total} result{total !== 1 ? 's' : ''} found
                  </span>
                  {totalPages > 1 && (
                    <span>
                      Page {currentPage} of {totalPages}
                    </span>
                  )}
                </div>

                <ScrollArea className="h-[calc(100vh-500px)] pr-4">
                  <div className="space-y-3">
                    {results.map((legislator) => (
                      <LegislatorResultCard
                        key={legislator.bioguideId}
                        legislator={legislator}
                        onEnrich={() => handleEnrichClick(legislator)}
                      />
                    ))}
                  </div>
                </ScrollArea>

                {/* Pagination */}
                {totalPages > 1 && (
                  <div className="mt-4 flex items-center justify-center gap-2">
                    <Button
                      variant="outline"
                      size="sm"
                      onClick={() => setCurrentPage((p) => Math.max(1, p - 1))}
                      disabled={currentPage === 1 || isLoading}
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
                      disabled={currentPage === totalPages || isLoading}
                    >
                      Next
                    </Button>
                  </div>
                )}
              </>
            )}
          </div>
        </CardContent>
      </Card>

      {/* Enrichment Preview Modal */}
      <EnrichmentPreviewModal
        open={isPreviewOpen}
        onClose={handleClosePreview}
        legislator={selectedLegislator}
        preview={enrichmentPreview}
        isLoading={isLoadingPreview}
        isEnriching={isEnriching}
        onConfirm={handleConfirmEnrichment}
      />
    </div>
  );
}

/**
 * Result card for a single legislator
 */
function LegislatorResultCard({
  legislator,
  onEnrich,
}: {
  legislator: LegislatorSearchResult;
  onEnrich: () => void;
}) {
  const partyColorClass = getPartyColor(legislator.party);

  return (
    <Card className="hover:shadow-md transition-shadow">
      <CardContent className="p-4">
        <div className="flex items-start justify-between">
          <div className="flex-1">
            <div className="flex items-center gap-2 flex-wrap">
              <span className="font-medium">{legislator.name}</span>
              <Badge className={partyColorClass}>
                {formatParty(legislator.party)}
              </Badge>
              <Badge variant="outline">
                {formatChamber(legislator.chamber)}
              </Badge>
              {legislator.currentMember && (
                <Badge variant="outline" className="text-green-600 border-green-600">
                  Current
                </Badge>
              )}
            </div>
            <div className="mt-1 text-sm text-muted-foreground">
              {legislator.state} | BioGuide: {legislator.bioguideId}
            </div>
            <div className="mt-2 flex gap-2">
              {legislator.socialMediaCount > 0 && (
                <Badge variant="secondary">
                  {legislator.socialMediaCount} social
                </Badge>
              )}
              {legislator.externalIdCount > 0 && (
                <Badge variant="secondary">
                  {legislator.externalIdCount} external IDs
                </Badge>
              )}
            </div>
          </div>
          <Button onClick={onEnrich}>
            Enrich
          </Button>
        </div>
      </CardContent>
    </Card>
  );
}
