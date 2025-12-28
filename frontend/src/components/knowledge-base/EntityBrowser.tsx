'use client';

import { useCallback, useRef } from 'react';
import { ChevronUp, ChevronDown, ChevronsUpDown } from 'lucide-react';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table';
import { Button } from '@/components/ui/button';
import { Skeleton } from '@/components/ui/skeleton';
import { cn } from '@/lib/utils';
import type {
  EntityTypeConfig,
  ColumnConfig,
  SortDirection,
} from '@/lib/config/entityTypes';

/**
 * Base type constraint for entities - must be indexable
 */
// eslint-disable-next-line @typescript-eslint/no-explicit-any
type EntityType = Record<string, any>;

/**
 * Props for the EntityBrowser component
 */
export interface EntityBrowserProps<T extends EntityType> {
  /** Entity type configuration */
  config: EntityTypeConfig<T>;
  /** Data array to display */
  data: T[];
  /** Total count for pagination */
  totalCount: number;
  /** Loading state */
  isLoading: boolean;
  /** Error message */
  error?: string | null;
  /** Current page (0-indexed) */
  currentPage: number;
  /** Page size */
  pageSize: number;
  /** Current sort column */
  sortColumn?: string;
  /** Current sort direction */
  sortDirection?: SortDirection;
  /** Current view mode */
  viewMode: 'list' | 'grid';
  /** Page change callback */
  onPageChange: (page: number) => void;
  /** Sort change callback */
  onSortChange: (column: string, direction: SortDirection) => void;
  /** Row click callback */
  onRowClick: (item: T) => void;
  /** Retry callback for error state */
  onRetry?: () => void;
}

/**
 * EntityBrowser - A reusable component for browsing entities in list or grid view.
 * Supports sorting, pagination, and configuration-driven columns.
 */
export function EntityBrowser<T extends EntityType>({
  config,
  data,
  totalCount,
  isLoading,
  error,
  currentPage,
  pageSize,
  sortColumn,
  sortDirection,
  viewMode,
  onPageChange,
  onSortChange,
  onRowClick,
  onRetry,
}: EntityBrowserProps<T>) {
  const tableRef = useRef<HTMLTableElement>(null);
  const focusedRowIndex = useRef<number>(-1);

  const columns = config.columns || [];
  const idField = config.idField || 'id';

  // Get cell value from row
  const getCellValue = useCallback(
    (row: T, column: ColumnConfig<T>): unknown => {
      if (column.accessor) {
        return column.accessor(row);
      }
      return row[column.id];
    },
    []
  );

  // Render cell content
  const renderCell = useCallback(
    (row: T, column: ColumnConfig<T>): React.ReactNode => {
      const value = getCellValue(row, column);
      if (column.render) {
        return column.render(value, row);
      }
      if (value === null || value === undefined) {
        return '-';
      }
      return String(value);
    },
    [getCellValue]
  );

  // Handle sort header click
  const handleSortClick = useCallback(
    (column: ColumnConfig<T>) => {
      if (!column.sortable) return;

      const newDirection: SortDirection =
        sortColumn === column.id && sortDirection === 'asc' ? 'desc' : 'asc';
      onSortChange(column.id, newDirection);
    },
    [sortColumn, sortDirection, onSortChange]
  );

  // Handle row keyboard navigation
  const handleRowKeyDown = useCallback(
    (event: React.KeyboardEvent, item: T, index: number) => {
      switch (event.key) {
        case 'Enter':
        case ' ':
          event.preventDefault();
          onRowClick(item);
          break;
        case 'ArrowDown':
          event.preventDefault();
          if (index < data.length - 1) {
            focusedRowIndex.current = index + 1;
            const rows = tableRef.current?.querySelectorAll('tbody tr');
            (rows?.[index + 1] as HTMLElement)?.focus();
          }
          break;
        case 'ArrowUp':
          event.preventDefault();
          if (index > 0) {
            focusedRowIndex.current = index - 1;
            const rows = tableRef.current?.querySelectorAll('tbody tr');
            (rows?.[index - 1] as HTMLElement)?.focus();
          }
          break;
      }
    },
    [data.length, onRowClick]
  );

  // Calculate pagination info
  const startItem = currentPage * pageSize + 1;
  const endItem = Math.min((currentPage + 1) * pageSize, totalCount);
  const totalPages = Math.ceil(totalCount / pageSize);
  const isFirstPage = currentPage === 0;
  const isLastPage = currentPage >= totalPages - 1;

  // Loading state
  if (isLoading) {
    return <EntityBrowserSkeleton columns={columns} viewMode={viewMode} />;
  }

  // Error state
  if (error) {
    return (
      <div className="flex flex-col items-center justify-center py-12 text-center border rounded-lg">
        <div className="text-4xl mb-4">⚠️</div>
        <h3 className="text-lg font-semibold mb-2">Failed to load {config.label.toLowerCase()}</h3>
        <p className="text-muted-foreground mb-4">{error}</p>
        {onRetry && (
          <Button onClick={onRetry}>Try Again</Button>
        )}
      </div>
    );
  }

  // Empty state
  if (data.length === 0) {
    return (
      <div className="flex flex-col items-center justify-center py-12 text-center border rounded-lg">
        <div className="text-4xl mb-4">🔍</div>
        <h3 className="text-lg font-semibold mb-2">No {config.label.toLowerCase()} found</h3>
        <p className="text-muted-foreground">
          Try adjusting your search or filter criteria.
        </p>
      </div>
    );
  }

  // Grid view
  if (viewMode === 'grid') {
    return (
      <div>
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4">
          {data.map((item) => (
            <EntityCard
              key={String(item[idField])}
              item={item}
              config={config}
              onClick={() => onRowClick(item)}
            />
          ))}
        </div>

        {/* Pagination */}
        <Pagination
          startItem={startItem}
          endItem={endItem}
          totalCount={totalCount}
          label={config.label.toLowerCase()}
          isFirstPage={isFirstPage}
          isLastPage={isLastPage}
          onPrevious={() => onPageChange(currentPage - 1)}
          onNext={() => onPageChange(currentPage + 1)}
        />
      </div>
    );
  }

  // List/table view
  return (
    <div>
      {/* Desktop Table */}
      <div className="border rounded-lg overflow-hidden hidden md:block">
        <Table ref={tableRef}>
          <TableHeader>
            <TableRow>
              {columns.map((column) => (
                <TableHead
                  key={column.id}
                  style={{ width: column.width }}
                  className={cn(
                    column.sortable && 'cursor-pointer select-none hover:bg-muted/50'
                  )}
                  onClick={() => handleSortClick(column)}
                  aria-sort={
                    sortColumn === column.id
                      ? sortDirection === 'asc'
                        ? 'ascending'
                        : 'descending'
                      : undefined
                  }
                >
                  <div className="flex items-center gap-1">
                    <span>{column.label}</span>
                    {column.sortable && (
                      <SortIndicator
                        active={sortColumn === column.id}
                        direction={sortColumn === column.id ? sortDirection : undefined}
                      />
                    )}
                  </div>
                </TableHead>
              ))}
            </TableRow>
          </TableHeader>
          <TableBody>
            {data.map((item, index) => (
              <TableRow
                key={String(item[idField])}
                tabIndex={0}
                className="cursor-pointer focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-inset"
                onClick={() => onRowClick(item)}
                onKeyDown={(e) => handleRowKeyDown(e, item, index)}
                role="row"
              >
                {columns.map((column) => (
                  <TableCell
                    key={column.id}
                    className={cn(column.hideOnMobile && 'hidden md:table-cell')}
                  >
                    {renderCell(item, column)}
                  </TableCell>
                ))}
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </div>

      {/* Mobile Card View */}
      <div className="md:hidden space-y-4">
        {data.map((item) => (
          <EntityCard
            key={String(item[idField])}
            item={item}
            config={config}
            onClick={() => onRowClick(item)}
          />
        ))}
      </div>

      {/* Pagination */}
      <Pagination
        startItem={startItem}
        endItem={endItem}
        totalCount={totalCount}
        label={config.label.toLowerCase()}
        isFirstPage={isFirstPage}
        isLastPage={isLastPage}
        onPrevious={() => onPageChange(currentPage - 1)}
        onNext={() => onPageChange(currentPage + 1)}
      />
    </div>
  );
}

/**
 * Sort indicator for column headers
 */
function SortIndicator({
  active,
  direction,
}: {
  active: boolean;
  direction?: SortDirection;
}) {
  if (!active) {
    return <ChevronsUpDown className="h-4 w-4 text-muted-foreground/50" />;
  }
  if (direction === 'asc') {
    return <ChevronUp className="h-4 w-4" />;
  }
  return <ChevronDown className="h-4 w-4" />;
}

/**
 * Pagination controls
 */
function Pagination({
  startItem,
  endItem,
  totalCount,
  label,
  isFirstPage,
  isLastPage,
  onPrevious,
  onNext,
}: {
  startItem: number;
  endItem: number;
  totalCount: number;
  label: string;
  isFirstPage: boolean;
  isLastPage: boolean;
  onPrevious: () => void;
  onNext: () => void;
}) {
  return (
    <div className="flex flex-col sm:flex-row justify-between items-center mt-4 gap-4">
      <p className="text-sm text-muted-foreground">
        Showing {startItem} to {endItem} of {totalCount} {label}
      </p>
      <div className="flex gap-2">
        <Button
          variant="outline"
          onClick={onPrevious}
          disabled={isFirstPage}
        >
          Previous
        </Button>
        <Button
          variant="outline"
          onClick={onNext}
          disabled={isLastPage}
        >
          Next
        </Button>
      </div>
    </div>
  );
}

/**
 * Entity card for grid view and mobile
 */
function EntityCard<T extends EntityType>({
  item,
  config,
  onClick,
}: {
  item: T;
  config: EntityTypeConfig<T>;
  onClick: () => void;
}) {
  const cardConfig = config.cardConfig;
  const columns = config.columns || [];

  // Get title and subtitle from config or first two columns
  const titleField = cardConfig?.titleField || columns[0]?.id || 'name';
  const subtitleField = cardConfig?.subtitleField || columns[1]?.id;

  const title = item[titleField];
  const subtitle = subtitleField ? item[subtitleField] : null;

  return (
    <div
      className="border rounded-lg p-4 cursor-pointer hover:bg-muted/50 transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring"
      onClick={onClick}
      tabIndex={0}
      onKeyDown={(e) => {
        if (e.key === 'Enter' || e.key === ' ') {
          e.preventDefault();
          onClick();
        }
      }}
      role="button"
    >
      <div className="flex justify-between items-start mb-2">
        <div className="font-medium truncate flex-1">
          {String(title || '-')}
        </div>
        {cardConfig?.renderBadge && cardConfig.renderBadge(item)}
      </div>
      {subtitle && (
        <div className="text-sm text-muted-foreground truncate">
          {String(subtitle)}
        </div>
      )}
      {cardConfig?.renderContent && (
        <div className="mt-2">
          {cardConfig.renderContent(item)}
        </div>
      )}
      {!cardConfig?.renderContent && columns.length > 2 && (
        <div className="mt-2 text-sm text-muted-foreground space-y-1">
          {columns.slice(2, 4).map((col) => {
            const value = item[col.id];
            if (value === null || value === undefined) return null;
            return (
              <div key={col.id} className="truncate">
                <span className="font-medium">{col.label}:</span> {String(value)}
              </div>
            );
          })}
        </div>
      )}
    </div>
  );
}

/**
 * Loading skeleton for EntityBrowser
 */
function EntityBrowserSkeleton<T>({
  columns,
  viewMode,
}: {
  columns: ColumnConfig<T>[];
  viewMode: 'list' | 'grid';
}) {
  if (viewMode === 'grid') {
    return (
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4">
        {Array.from({ length: 8 }).map((_, i) => (
          <div key={i} className="border rounded-lg p-4">
            <Skeleton className="h-5 w-3/4 mb-2" />
            <Skeleton className="h-4 w-1/2 mb-3" />
            <Skeleton className="h-4 w-full" />
          </div>
        ))}
      </div>
    );
  }

  return (
    <div className="border rounded-lg">
      <div className="p-4 border-b">
        <div className="flex gap-4">
          {columns.map((col) => (
            <Skeleton
              key={col.id}
              className="h-5"
              style={{ width: col.width || '120px' }}
            />
          ))}
        </div>
      </div>
      {Array.from({ length: 10 }).map((_, i) => (
        <div key={i} className="p-4 border-b flex items-center gap-4">
          {columns.map((col) => (
            <Skeleton
              key={col.id}
              className="h-5"
              style={{ width: col.width || '120px' }}
            />
          ))}
        </div>
      ))}
    </div>
  );
}
