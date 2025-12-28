'use client';

import { useState, useCallback, useMemo } from 'react';
import { notFound, useRouter, useSearchParams } from 'next/navigation';
import { getEntityTypeConfig, type SortDirection, type ViewMode } from '@/lib/config/entityTypes';
import { EntityBrowser, EntityFilters, HierarchyView, ViewModeSelector } from '@/components/knowledge-base';
import {
  useGovernmentOrgsList,
  useGovernmentOrgsHierarchy,
  useGovernmentOrgsSearch,
  type GovOrgListParams,
} from '@/hooks/useGovernmentOrgs';

interface EntityBrowserPageProps {
  params: {
    entityType: string;
  };
}

const DEFAULT_PAGE_SIZE = 20;

/**
 * Entity Browser page - displays entities for a given entity type.
 * Uses the EntityBrowser pattern component for configuration-driven rendering.
 * Supports list and hierarchy view modes.
 */
export default function EntityBrowserPage({ params }: EntityBrowserPageProps) {
  const router = useRouter();
  const searchParams = useSearchParams();

  // Get entity configuration
  const entityConfig = getEntityTypeConfig(params.entityType);
  if (!entityConfig) {
    notFound();
  }

  // Parse URL search params
  const viewModeParam = searchParams.get('view') as ViewMode | null;
  const viewMode: ViewMode = viewModeParam && entityConfig.supportedViews.includes(viewModeParam)
    ? viewModeParam
    : entityConfig.defaultView;
  const initialPage = parseInt(searchParams.get('page') || '0', 10);
  const initialSort = searchParams.get('sort') || entityConfig.defaultSort?.column || 'id';
  const initialDirection = (searchParams.get('dir') as SortDirection) || entityConfig.defaultSort?.direction || 'asc';
  const searchQuery = searchParams.get('q') || '';

  // State for pagination, sorting, and filtering
  const [currentPage, setCurrentPage] = useState(initialPage);
  const [sortColumn, setSortColumn] = useState(initialSort);
  const [sortDirection, setSortDirection] = useState<SortDirection>(initialDirection);
  const [filterValues, setFilterValues] = useState<Record<string, string | string[] | undefined>>({});

  // Build query params for API call (organizations-specific for now)
  const queryParams = useMemo((): GovOrgListParams => {
    const params: GovOrgListParams = {
      page: currentPage,
      size: DEFAULT_PAGE_SIZE,
      sort: sortColumn,
      direction: sortDirection,
    };

    // Map filter values to API params
    if (filterValues.branch && typeof filterValues.branch === 'string') {
      params.branch = filterValues.branch;
    }
    if (filterValues.orgType && typeof filterValues.orgType === 'string') {
      params.type = filterValues.orgType;
    }
    if (filterValues.active && typeof filterValues.active === 'string') {
      params.active = filterValues.active;
    }

    return params;
  }, [currentPage, sortColumn, sortDirection, filterValues]);

  // Data fetching - currently only organizations
  // Use search endpoint when there's a search query, otherwise use paginated list
  const isSearching = !!searchQuery;

  // Paginated list query (used when not searching)
  const {
    data: listData,
    isLoading: isListLoading,
    error: listError,
    refetch: refetchList,
  } = useGovernmentOrgsList(queryParams);

  // Search query (used when searching)
  const {
    data: searchData,
    isLoading: isSearchLoading,
    error: searchError,
    refetch: refetchSearch,
  } = useGovernmentOrgsSearch(searchQuery);

  // Combine results based on search state
  const data = isSearching ? searchData : listData?.content;
  const isLoading = isSearching ? isSearchLoading : isListLoading;
  const error = isSearching ? searchError : listError;
  const refetch = isSearching ? refetchSearch : refetchList;

  // Hierarchy data fetching (only when in hierarchy view)
  const {
    data: hierarchyData,
    isLoading: isHierarchyLoading,
    error: hierarchyError,
    refetch: refetchHierarchy,
  } = useGovernmentOrgsHierarchy(
    filterValues.branch as 'executive' | 'legislative' | 'judicial' | undefined
  );

  // Extract total count for EntityBrowser
  // When searching, we get all results (no pagination), so count is the array length
  // When browsing, we get paginated results with totalElements
  const totalCount = isSearching
    ? (searchData?.length || 0)
    : (listData?.totalElements || 0);

  // Handle page change
  const handlePageChange = useCallback((page: number) => {
    setCurrentPage(page);
  }, []);

  // Handle sort change
  const handleSortChange = useCallback((column: string, direction: SortDirection) => {
    setSortColumn(column);
    setSortDirection(direction);
    setCurrentPage(0); // Reset to first page on sort change
  }, []);

  // Handle filter change
  const handleFilterChange = useCallback((values: Record<string, string | string[] | undefined>) => {
    setFilterValues(values);
    setCurrentPage(0); // Reset to first page on filter change
  }, []);

  // Handle row click - navigate to detail view
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  const handleRowClick = useCallback(
    (item: any) => {
      router.push(`/knowledge-base/${params.entityType}/${item.id}`);
    },
    [router, params.entityType]
  );

  // Render organization browser
  // Note: People entity type will need similar implementation
  if (params.entityType === 'organizations') {
    return (
      <div className="p-6 space-y-6">
        {/* Header with View Mode Toggle */}
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-3">
            <entityConfig.icon className="h-8 w-8 text-primary" />
            <h1 className="text-2xl font-bold">{entityConfig.label}</h1>
          </div>

          {/* View Mode Selector */}
          {entityConfig.supportedViews.length > 1 && (
            <ViewModeSelector />
          )}
        </div>

        {/* Filters */}
        {entityConfig.filters && entityConfig.filters.length > 0 && (
          <EntityFilters
            filters={entityConfig.filters}
            values={filterValues}
            onChange={handleFilterChange}
          />
        )}

        {/* Content - List or Hierarchy View */}
        {viewMode === 'hierarchy' ? (
          <HierarchyView
            data={hierarchyData || []}
            config={entityConfig.hierarchyConfig!}
            entityType={params.entityType}
            isLoading={isHierarchyLoading}
            error={hierarchyError?.message || null}
            onRetry={() => refetchHierarchy()}
          />
        ) : (
          <EntityBrowser
            config={entityConfig}
            data={data || []}
            totalCount={totalCount}
            isLoading={isLoading}
            error={error?.message || null}
            currentPage={isSearching ? 0 : currentPage}
            pageSize={DEFAULT_PAGE_SIZE}
            sortColumn={sortColumn}
            sortDirection={sortDirection}
            viewMode={viewMode}
            onPageChange={handlePageChange}
            onSortChange={handleSortChange}
            onRowClick={handleRowClick}
            onRetry={() => refetch()}
            searchQuery={searchQuery}
          />
        )}
      </div>
    );
  }

  // Placeholder for other entity types
  const Icon = entityConfig.icon;
  return (
    <div className="p-6">
      <div className="flex items-center gap-3 mb-6">
        <Icon className="h-8 w-8 text-primary" />
        <h1 className="text-2xl font-bold">{entityConfig.label}</h1>
      </div>

      <div className="border rounded-lg p-8 text-center text-muted-foreground">
        <p className="mb-2">EntityBrowser for <strong>{entityConfig.label}</strong></p>
        <p className="text-sm">
          Data fetching hooks for this entity type will be added in future stories.
        </p>
        <p className="text-sm mt-2">
          Supported views: {entityConfig.supportedViews.join(', ')}
        </p>
      </div>
    </div>
  );
}
