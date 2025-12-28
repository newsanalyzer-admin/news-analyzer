'use client';

import { useState, useCallback, useMemo, useEffect } from 'react';
import { notFound, useRouter, useSearchParams, usePathname } from 'next/navigation';
import {
  getEntityTypeConfig,
  type SortDirection,
  type ViewMode,
  type SubtypeConfig,
} from '@/lib/config/entityTypes';
import { getPeopleSubtypeConfig, getDefaultPeopleSubtype } from '@/lib/config/peopleConfig';
import { EntityBrowser, EntityFilters, HierarchyView, ViewModeSelector } from '@/components/knowledge-base';
import {
  useGovernmentOrgsList,
  useGovernmentOrgsHierarchy,
  useGovernmentOrgsSearch,
  type GovOrgListParams,
} from '@/hooks/useGovernmentOrgs';
import { useJudges, useJudgeSearch, useJudgeStats } from '@/hooks/useJudges';
import { useMembers, useMemberSearch } from '@/hooks/useMembers';
import { useAppointees, useAppointeeSearch } from '@/hooks/useAppointees';
import type { JudgeListParams } from '@/lib/api/judges';
import type { MemberListParams } from '@/lib/api/members';
import type { AppointeeListParams } from '@/lib/api/appointees';
import { Button } from '@/components/ui/button';
import { cn } from '@/lib/utils';

interface EntityBrowserPageProps {
  params: {
    entityType: string;
  };
}

const DEFAULT_PAGE_SIZE = 20;

/**
 * Subtype selector component for People entity
 */
function SubtypeSelector({
  subtypes,
  currentSubtype,
  onChange,
}: {
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  subtypes: SubtypeConfig<any>[];
  currentSubtype: string;
  onChange: (subtypeId: string) => void;
}) {
  return (
    <div className="flex flex-wrap gap-2">
      {subtypes.map((subtype) => (
        <Button
          key={subtype.id}
          variant={currentSubtype === subtype.id ? 'default' : 'outline'}
          size="sm"
          onClick={() => onChange(subtype.id)}
          className={cn(
            'transition-colors',
            currentSubtype === subtype.id && 'pointer-events-none'
          )}
        >
          {subtype.label}
        </Button>
      ))}
    </div>
  );
}

/**
 * Entity Browser page - displays entities for a given entity type.
 * Uses the EntityBrowser pattern component for configuration-driven rendering.
 * Supports list and hierarchy view modes, plus subtypes for People.
 */
export default function EntityBrowserPage({ params }: EntityBrowserPageProps) {
  const router = useRouter();
  const searchParams = useSearchParams();
  const pathname = usePathname();

  // Get entity configuration
  const entityConfig = getEntityTypeConfig(params.entityType);
  if (!entityConfig) {
    notFound();
  }

  // Parse URL search params
  const viewModeParam = searchParams.get('view') as ViewMode | null;
  const viewMode: ViewMode =
    viewModeParam && entityConfig.supportedViews.includes(viewModeParam)
      ? viewModeParam
      : entityConfig.defaultView;
  const initialPage = parseInt(searchParams.get('page') || '0', 10);
  const searchQuery = searchParams.get('q') || '';

  // Get current subtype for People entity
  const subtypeParam = searchParams.get('type');
  const currentSubtypeId = entityConfig.hasSubtypes
    ? subtypeParam || entityConfig.defaultSubtype || 'judges'
    : null;
  const currentSubtypeConfig = currentSubtypeId
    ? getPeopleSubtypeConfig(currentSubtypeId)
    : null;

  // Determine active configuration (subtype config or entity config)
  const activeConfig = currentSubtypeConfig || entityConfig;
  const activeFilters = currentSubtypeConfig?.filters || entityConfig.filters || [];
  const activeColumns = currentSubtypeConfig?.columns || entityConfig.columns || [];
  const activeDefaultSort = currentSubtypeConfig?.defaultSort || entityConfig.defaultSort;

  const initialSort = searchParams.get('sort') || activeDefaultSort?.column || 'id';
  const initialDirection =
    (searchParams.get('dir') as SortDirection) || activeDefaultSort?.direction || 'asc';

  // Parse initial filter values from URL params
  const initialFilterValues = useMemo(() => {
    const values: Record<string, string | string[] | undefined> = {};
    // Read filter values from URL for each configured filter
    activeFilters.forEach((filter) => {
      const paramValue = searchParams.get(filter.id);
      if (paramValue) {
        values[filter.id] = paramValue;
      }
    });
    return values;
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [searchParams, currentSubtypeId]);

  // State for pagination, sorting, and filtering
  const [currentPage, setCurrentPage] = useState(initialPage);
  const [sortColumn, setSortColumn] = useState(initialSort);
  const [sortDirection, setSortDirection] = useState<SortDirection>(initialDirection);
  const [filterValues, setFilterValues] = useState<Record<string, string | string[] | undefined>>(
    initialFilterValues
  );

  // Reset state when subtype changes
  useEffect(() => {
    setCurrentPage(0);
    setSortColumn(activeDefaultSort?.column || 'id');
    setSortDirection(activeDefaultSort?.direction || 'asc');
    setFilterValues({});
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [currentSubtypeId]);

  // Sync filter values when URL params change (browser back/forward)
  useEffect(() => {
    const currentFilters: Record<string, string | string[] | undefined> = {};
    activeFilters.forEach((filter) => {
      const paramValue = searchParams.get(filter.id);
      if (paramValue) {
        currentFilters[filter.id] = paramValue;
      }
    });
    // Only update if filters have actually changed
    const hasChanged =
      Object.keys(currentFilters).length !== Object.keys(filterValues).length ||
      Object.entries(currentFilters).some(([key, value]) => filterValues[key] !== value);
    if (hasChanged) {
      setFilterValues(currentFilters);
    }
  }, [searchParams, activeFilters, filterValues]);

  // =====================================================================
  // Organizations Data Fetching
  // =====================================================================

  const orgQueryParams = useMemo((): GovOrgListParams => {
    const p: GovOrgListParams = {
      page: currentPage,
      size: DEFAULT_PAGE_SIZE,
      sort: sortColumn,
      direction: sortDirection,
    };

    if (filterValues.branch && typeof filterValues.branch === 'string') {
      p.branch = filterValues.branch;
    }
    if (filterValues.orgType && typeof filterValues.orgType === 'string') {
      p.type = filterValues.orgType;
    }
    if (filterValues.active && typeof filterValues.active === 'string') {
      p.active = filterValues.active;
    }

    return p;
  }, [currentPage, sortColumn, sortDirection, filterValues]);

  const isOrganizations = params.entityType === 'organizations';
  const isSearching = !!searchQuery;

  const orgListQuery = useGovernmentOrgsList(isOrganizations ? orgQueryParams : { page: 0, size: 1 });
  const orgSearchQuery = useGovernmentOrgsSearch(isOrganizations && isSearching ? searchQuery : '');
  const orgHierarchyQuery = useGovernmentOrgsHierarchy(
    isOrganizations && viewMode === 'hierarchy'
      ? (filterValues.branch as 'executive' | 'legislative' | 'judicial' | undefined)
      : undefined
  );

  // =====================================================================
  // Judges Data Fetching
  // =====================================================================

  const judgeQueryParams = useMemo((): JudgeListParams => {
    const p: JudgeListParams = {
      page: currentPage,
      size: DEFAULT_PAGE_SIZE,
      sortBy: sortColumn,
      sortDir: sortDirection,
    };

    if (filterValues.courtLevel && typeof filterValues.courtLevel === 'string') {
      p.courtLevel = filterValues.courtLevel;
    }
    if (filterValues.circuit && typeof filterValues.circuit === 'string') {
      p.circuit = filterValues.circuit;
    }
    if (filterValues.status && typeof filterValues.status === 'string') {
      p.status = filterValues.status;
    }

    return p;
  }, [currentPage, sortColumn, sortDirection, filterValues]);

  const isJudges = params.entityType === 'people' && currentSubtypeId === 'judges';

  const judgesListQuery = useJudges(isJudges && !isSearching ? judgeQueryParams : { page: 0, size: 1 });
  const judgeSearchQuery = useJudgeSearch(isJudges && isSearching ? searchQuery : '');
  const judgeStatsQuery = useJudgeStats();

  // =====================================================================
  // Members Data Fetching
  // =====================================================================

  const memberQueryParams = useMemo((): MemberListParams => {
    const p: MemberListParams = {
      page: currentPage,
      size: DEFAULT_PAGE_SIZE,
    };

    if (filterValues.chamber && typeof filterValues.chamber === 'string') {
      p.chamber = filterValues.chamber as 'SENATE' | 'HOUSE';
    }
    if (filterValues.party && typeof filterValues.party === 'string') {
      p.party = filterValues.party;
    }
    if (filterValues.state && typeof filterValues.state === 'string') {
      p.state = filterValues.state;
    }

    return p;
  }, [currentPage, filterValues]);

  const isMembers = params.entityType === 'people' && currentSubtypeId === 'members';

  const membersListQuery = useMembers(isMembers && !isSearching ? memberQueryParams : { page: 0, size: 1 });
  const memberSearchQuery = useMemberSearch(isMembers && isSearching ? searchQuery : '');

  // =====================================================================
  // Appointees Data Fetching
  // =====================================================================

  const appointeeQueryParams = useMemo((): AppointeeListParams => {
    const p: AppointeeListParams = {
      page: currentPage,
      size: DEFAULT_PAGE_SIZE,
    };

    if (filterValues.type && typeof filterValues.type === 'string') {
      p.type = filterValues.type as 'PAS' | 'PA' | 'NA' | 'CA' | 'XS';
    }

    return p;
  }, [currentPage, filterValues]);

  const isAppointees = params.entityType === 'people' && currentSubtypeId === 'appointees';

  const appointeesListQuery = useAppointees(
    isAppointees && !isSearching ? appointeeQueryParams : { page: 0, size: 1 }
  );
  const appointeeSearchQuery = useAppointeeSearch(isAppointees && isSearching ? searchQuery : '');

  // =====================================================================
  // Combined Data Selection
  // =====================================================================

  const { data, totalCount, isLoading, error, refetch } = useMemo(() => {
    if (isOrganizations) {
      if (isSearching) {
        return {
          data: orgSearchQuery.data || [],
          totalCount: orgSearchQuery.data?.length || 0,
          isLoading: orgSearchQuery.isLoading,
          error: orgSearchQuery.error?.message || null,
          refetch: orgSearchQuery.refetch,
        };
      }
      return {
        data: orgListQuery.data?.content || [],
        totalCount: orgListQuery.data?.totalElements || 0,
        isLoading: orgListQuery.isLoading,
        error: orgListQuery.error?.message || null,
        refetch: orgListQuery.refetch,
      };
    }

    if (isJudges) {
      if (isSearching) {
        return {
          data: judgeSearchQuery.data || [],
          totalCount: judgeSearchQuery.data?.length || 0,
          isLoading: judgeSearchQuery.isLoading,
          error: judgeSearchQuery.error?.message || null,
          refetch: judgeSearchQuery.refetch,
        };
      }
      return {
        data: judgesListQuery.data?.content || [],
        totalCount: judgesListQuery.data?.totalElements || 0,
        isLoading: judgesListQuery.isLoading,
        error: judgesListQuery.error?.message || null,
        refetch: judgesListQuery.refetch,
      };
    }

    if (isMembers) {
      if (isSearching) {
        return {
          data: memberSearchQuery.data?.content || [],
          totalCount: memberSearchQuery.data?.totalElements || 0,
          isLoading: memberSearchQuery.isLoading,
          error: memberSearchQuery.error?.message || null,
          refetch: memberSearchQuery.refetch,
        };
      }
      return {
        data: membersListQuery.data?.content || [],
        totalCount: membersListQuery.data?.totalElements || 0,
        isLoading: membersListQuery.isLoading,
        error: membersListQuery.error?.message || null,
        refetch: membersListQuery.refetch,
      };
    }

    if (isAppointees) {
      if (isSearching) {
        return {
          data: appointeeSearchQuery.data || [],
          totalCount: appointeeSearchQuery.data?.length || 0,
          isLoading: appointeeSearchQuery.isLoading,
          error: appointeeSearchQuery.error?.message || null,
          refetch: appointeeSearchQuery.refetch,
        };
      }
      return {
        data: appointeesListQuery.data?.content || [],
        totalCount: appointeesListQuery.data?.totalElements || 0,
        isLoading: appointeesListQuery.isLoading,
        error: appointeesListQuery.error?.message || null,
        refetch: appointeesListQuery.refetch,
      };
    }

    return {
      data: [],
      totalCount: 0,
      isLoading: false,
      error: null,
      refetch: () => Promise.resolve({} as ReturnType<typeof orgListQuery.refetch>),
    };
  }, [
    isOrganizations,
    isJudges,
    isMembers,
    isAppointees,
    isSearching,
    orgSearchQuery,
    orgListQuery,
    judgeSearchQuery,
    judgesListQuery,
    memberSearchQuery,
    membersListQuery,
    appointeeSearchQuery,
    appointeesListQuery,
  ]);

  // =====================================================================
  // Event Handlers
  // =====================================================================

  const handlePageChange = useCallback((page: number) => {
    setCurrentPage(page);
  }, []);

  const handleSortChange = useCallback((column: string, direction: SortDirection) => {
    setSortColumn(column);
    setSortDirection(direction);
    setCurrentPage(0);
  }, []);

  const handleFilterChange = useCallback(
    (values: Record<string, string | string[] | undefined>) => {
      setFilterValues(values);
      setCurrentPage(0);

      const newParams = new URLSearchParams(searchParams.toString());
      newParams.delete('page');

      activeFilters.forEach((filter) => {
        const value = values[filter.id];
        if (value !== undefined && value !== '') {
          newParams.set(filter.id, Array.isArray(value) ? value.join(',') : value);
        } else {
          newParams.delete(filter.id);
        }
      });

      const queryString = newParams.toString();
      router.replace(queryString ? `${pathname}?${queryString}` : pathname);
    },
    [searchParams, pathname, router, activeFilters]
  );

  const handleSubtypeChange = useCallback(
    (subtypeId: string) => {
      const newParams = new URLSearchParams();
      newParams.set('type', subtypeId);
      router.replace(`${pathname}?${newParams.toString()}`);
    },
    [pathname, router]
  );

  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  const handleRowClick = useCallback(
    (item: any) => {
      // Use the appropriate ID field based on entity/subtype
      let itemId = item.id;
      if (isMembers && item.bioguideId) {
        itemId = item.bioguideId;
      }
      router.push(`/knowledge-base/${params.entityType}/${itemId}`);
    },
    [router, params.entityType, isMembers]
  );

  // =====================================================================
  // Render: Organizations
  // =====================================================================

  if (isOrganizations) {
    return (
      <div className="p-6 space-y-6">
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-3">
            <entityConfig.icon className="h-8 w-8 text-primary" />
            <h1 className="text-2xl font-bold">{entityConfig.label}</h1>
          </div>
          {entityConfig.supportedViews.length > 1 && <ViewModeSelector />}
        </div>

        {entityConfig.filters && entityConfig.filters.length > 0 && (
          <EntityFilters
            filters={entityConfig.filters}
            values={filterValues}
            onChange={handleFilterChange}
          />
        )}

        {viewMode === 'hierarchy' ? (
          <HierarchyView
            data={orgHierarchyQuery.data || []}
            config={entityConfig.hierarchyConfig!}
            entityType={params.entityType}
            isLoading={orgHierarchyQuery.isLoading}
            error={orgHierarchyQuery.error?.message || null}
            onRetry={() => orgHierarchyQuery.refetch()}
          />
        ) : (
          <EntityBrowser
            config={entityConfig}
            data={data || []}
            totalCount={totalCount}
            isLoading={isLoading}
            error={error}
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

  // =====================================================================
  // Render: People (with subtypes)
  // =====================================================================

  if (params.entityType === 'people' && entityConfig.subtypes) {
    // Build a config object that EntityBrowser can use
    const peopleEntityConfig = {
      ...entityConfig,
      columns: activeColumns,
      filters: activeFilters,
      defaultSort: activeDefaultSort,
      detailConfig: currentSubtypeConfig?.detailConfig,
    };

    return (
      <div className="p-6 space-y-6">
        <div className="flex flex-col gap-4 md:flex-row md:items-center md:justify-between">
          <div className="flex items-center gap-3">
            <entityConfig.icon className="h-8 w-8 text-primary" />
            <h1 className="text-2xl font-bold">{entityConfig.label}</h1>
          </div>

          {/* Subtype Selector */}
          <SubtypeSelector
            subtypes={entityConfig.subtypes}
            currentSubtype={currentSubtypeId || 'judges'}
            onChange={handleSubtypeChange}
          />
        </div>

        {/* Judge Stats (when viewing judges) */}
        {isJudges && judgeStatsQuery.data && (
          <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
            <div className="border rounded-lg p-4 text-center">
              <div className="text-2xl font-bold">{judgeStatsQuery.data.totalJudges}</div>
              <div className="text-sm text-muted-foreground">Total Judges</div>
            </div>
            <div className="border rounded-lg p-4 text-center">
              <div className="text-2xl font-bold text-green-600">
                {judgeStatsQuery.data.activeJudges}
              </div>
              <div className="text-sm text-muted-foreground">Active</div>
            </div>
            <div className="border rounded-lg p-4 text-center">
              <div className="text-2xl font-bold text-blue-600">
                {judgeStatsQuery.data.seniorJudges}
              </div>
              <div className="text-sm text-muted-foreground">Senior Status</div>
            </div>
            <div className="border rounded-lg p-4 text-center">
              <div className="text-2xl font-bold">
                {Object.keys(judgeStatsQuery.data.byCircuit || {}).length}
              </div>
              <div className="text-sm text-muted-foreground">Circuits</div>
            </div>
          </div>
        )}

        {/* Filters */}
        {activeFilters.length > 0 && (
          <EntityFilters
            filters={activeFilters}
            values={filterValues}
            onChange={handleFilterChange}
          />
        )}

        {/* Entity Browser */}
        <EntityBrowser
          config={peopleEntityConfig}
          data={data || []}
          totalCount={totalCount}
          isLoading={isLoading}
          error={error}
          currentPage={isSearching ? 0 : currentPage}
          pageSize={DEFAULT_PAGE_SIZE}
          sortColumn={sortColumn}
          sortDirection={sortDirection}
          viewMode="list"
          onPageChange={handlePageChange}
          onSortChange={handleSortChange}
          onRowClick={handleRowClick}
          onRetry={() => refetch()}
          searchQuery={searchQuery}
        />
      </div>
    );
  }

  // =====================================================================
  // Render: Placeholder for other entity types
  // =====================================================================

  const Icon = entityConfig.icon;
  return (
    <div className="p-6">
      <div className="flex items-center gap-3 mb-6">
        <Icon className="h-8 w-8 text-primary" />
        <h1 className="text-2xl font-bold">{entityConfig.label}</h1>
      </div>

      <div className="border rounded-lg p-8 text-center text-muted-foreground">
        <p className="mb-2">
          EntityBrowser for <strong>{entityConfig.label}</strong>
        </p>
        <p className="text-sm">
          Data fetching hooks for this entity type will be added in future stories.
        </p>
        <p className="text-sm mt-2">Supported views: {entityConfig.supportedViews.join(', ')}</p>
      </div>
    </div>
  );
}
