'use client';

/**
 * Executive Appointees Page (Public Factbase)
 *
 * Browse and filter Executive Branch appointees with click-to-view detail panel.
 */

import { Suspense, useState, useMemo } from 'react';
import { useSearchParams, useRouter, usePathname } from 'next/navigation';
import { ContentPageHeader } from '@/components/public';
import { getPageDescriptionOrDefault } from '@/lib/page-descriptions';
import { useAppointees, useAppointeeSearch } from '@/hooks/useAppointees';
import { AppointeeDetailPanel } from './AppointeeDetailPanel';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { Input } from '@/components/ui/input';
import { Skeleton } from '@/components/ui/skeleton';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table';
import { useDebounce } from '@/hooks/useDebounce';
import type { Appointee, AppointmentType } from '@/types/appointee';

const APPOINTMENT_TYPES = [
  { value: 'ALL', label: 'All Types' },
  { value: 'PAS', label: 'PAS - Senate Confirmed' },
  { value: 'PA', label: 'PA - Presidential' },
  { value: 'NA', label: 'NA - Noncareer' },
  { value: 'CA', label: 'CA - Career' },
  { value: 'XS', label: 'XS - Schedule C' },
];

const appointmentTypeColors: Record<string, string> = {
  PAS: 'bg-purple-100 text-purple-800',
  PA: 'bg-blue-100 text-blue-800',
  NA: 'bg-amber-100 text-amber-800',
  CA: 'bg-green-100 text-green-800',
  XS: 'bg-orange-100 text-orange-800',
};

function ExecutiveAppointeesContent() {
  const searchParams = useSearchParams();
  const router = useRouter();
  const pathname = usePathname();
  const [selectedAppointee, setSelectedAppointee] = useState<Appointee | null>(null);
  const [searchInput, setSearchInput] = useState(searchParams.get('search') || '');

  const { title, description } = getPageDescriptionOrDefault('/factbase/people/executive-appointees');

  // Parse URL params
  const typeParam = searchParams.get('type');
  const appointmentType = typeParam && typeParam !== 'ALL' ? typeParam as AppointmentType : undefined;
  const page = parseInt(searchParams.get('page') || '0', 10);

  const debouncedSearch = useDebounce(searchInput, 300);

  // Queries
  const appointeesQuery = useAppointees({
    type: appointmentType,
    page,
    size: 20,
  });

  const searchQuery = useAppointeeSearch(debouncedSearch, 50);

  // Use search results if search query is provided
  const isSearching = debouncedSearch && debouncedSearch.length >= 2;

  // Filter data by appointment type when searching (search API doesn't support type filter)
  const filteredSearchResults = useMemo(() => {
    if (!searchQuery.data) return [];
    if (!appointmentType) return searchQuery.data;
    return searchQuery.data.filter(a => a.appointmentType === appointmentType);
  }, [searchQuery.data, appointmentType]);

  const data = isSearching ? filteredSearchResults : appointeesQuery.data?.content;
  const isLoading = isSearching ? searchQuery.isLoading : appointeesQuery.isLoading;
  const error = isSearching ? searchQuery.error : appointeesQuery.error;

  const updateParams = (key: string, value: string) => {
    const params = new URLSearchParams(searchParams.toString());
    if (value === 'ALL' || value === '') {
      params.delete(key);
    } else {
      params.set(key, value);
    }
    params.delete('page'); // Reset page when filters change
    router.push(`${pathname}?${params.toString()}`);
  };

  const handlePageChange = (newPage: number) => {
    const params = new URLSearchParams(searchParams.toString());
    params.set('page', newPage.toString());
    router.push(`${pathname}?${params.toString()}`);
  };

  const handleAppointeeClick = (appointee: Appointee) => {
    setSelectedAppointee(appointee);
  };

  const handleClosePanel = () => {
    setSelectedAppointee(null);
  };

  const handleClearFilters = () => {
    setSearchInput('');
    router.push(pathname);
  };

  const hasFilters = typeParam !== null || searchInput !== '';

  return (
    <div className="container mx-auto px-6 py-8 max-w-7xl">
      <ContentPageHeader
        title={title}
        description={description}
        breadcrumbs={[
          { label: 'Factbase', href: '/factbase' },
          { label: 'People', href: '/factbase/people' },
          { label: 'Executive Appointees' },
        ]}
      />

      {/* Filters */}
      <div className="flex flex-wrap gap-4 mb-6">
        {/* Search Input */}
        <div className="flex-1 min-w-[200px] max-w-md">
          <Input
            type="text"
            placeholder="Search by name or position..."
            value={searchInput}
            onChange={(e) => setSearchInput(e.target.value)}
            className="w-full"
          />
        </div>

        {/* Appointment Type Filter */}
        <Select
          value={typeParam || 'ALL'}
          onValueChange={(v) => updateParams('type', v)}
        >
          <SelectTrigger className="w-[200px]">
            <SelectValue placeholder="Appointment Type" />
          </SelectTrigger>
          <SelectContent>
            {APPOINTMENT_TYPES.map((t) => (
              <SelectItem key={t.value} value={t.value}>
                {t.label}
              </SelectItem>
            ))}
          </SelectContent>
        </Select>

        {/* Clear Filters */}
        {hasFilters && (
          <Button variant="outline" onClick={handleClearFilters}>
            Clear Filters
          </Button>
        )}
      </div>

      {/* Loading State */}
      {isLoading && <AppointeeTableSkeleton />}

      {/* Error State */}
      {error && !isLoading && (
        <div className="flex flex-col items-center justify-center py-12 text-center border rounded-lg">
          <div className="text-4xl mb-4">⚠️</div>
          <h3 className="text-lg font-semibold mb-2">Failed to load appointees</h3>
          <p className="text-muted-foreground mb-4">{(error as Error).message}</p>
          <Button onClick={() => isSearching ? searchQuery.refetch() : appointeesQuery.refetch()}>
            Try Again
          </Button>
        </div>
      )}

      {/* Empty State */}
      {!isLoading && !error && (!data || data.length === 0) && (
        <div className="flex flex-col items-center justify-center py-12 text-center border rounded-lg">
          <div className="text-4xl mb-4">🔍</div>
          <h3 className="text-lg font-semibold mb-2">No appointees found</h3>
          <p className="text-muted-foreground">
            Try adjusting your search or filter criteria.
          </p>
        </div>
      )}

      {/* Data Table */}
      {!isLoading && !error && data && data.length > 0 && (
        <div>
          {/* Desktop Table */}
          <div className="border rounded-lg overflow-hidden hidden md:block">
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Name</TableHead>
                  <TableHead>Position</TableHead>
                  <TableHead className="w-[200px]">Agency</TableHead>
                  <TableHead className="w-[100px]">Type</TableHead>
                  <TableHead className="w-[100px]">Status</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {data.map((appointee) => (
                  <TableRow
                    key={appointee.id}
                    className="cursor-pointer hover:bg-muted/50"
                    onClick={() => handleAppointeeClick(appointee)}
                  >
                    <TableCell className="font-medium">
                      {appointee.fullName || `${appointee.firstName || ''} ${appointee.lastName || ''}`.trim() || '-'}
                    </TableCell>
                    <TableCell className="max-w-[300px] truncate">
                      {appointee.positionTitle || '-'}
                    </TableCell>
                    <TableCell className="truncate">
                      {appointee.agencyName || '-'}
                    </TableCell>
                    <TableCell>
                      {appointee.appointmentType && (
                        <Badge
                          className={appointmentTypeColors[appointee.appointmentType] || 'bg-gray-100 text-gray-800'}
                          variant="outline"
                        >
                          {appointee.appointmentType}
                        </Badge>
                      )}
                    </TableCell>
                    <TableCell>
                      <Badge
                        variant="outline"
                        className={appointee.status === 'Filled'
                          ? 'bg-green-100 text-green-800'
                          : 'bg-red-100 text-red-800'}
                      >
                        {appointee.status}
                      </Badge>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </div>

          {/* Mobile Card View */}
          <div className="md:hidden space-y-4">
            {data.map((appointee) => (
              <div
                key={appointee.id}
                className="border rounded-lg p-4 cursor-pointer hover:bg-muted/50"
                onClick={() => handleAppointeeClick(appointee)}
              >
                <div className="font-medium">
                  {appointee.fullName || `${appointee.firstName || ''} ${appointee.lastName || ''}`.trim() || '-'}
                </div>
                <div className="text-sm text-muted-foreground mt-1 truncate">
                  {appointee.positionTitle || '-'}
                </div>
                <div className="text-sm text-muted-foreground truncate">
                  {appointee.agencyName || '-'}
                </div>
                <div className="flex items-center gap-2 mt-2">
                  {appointee.appointmentType && (
                    <Badge
                      className={appointmentTypeColors[appointee.appointmentType] || 'bg-gray-100 text-gray-800'}
                      variant="outline"
                    >
                      {appointee.appointmentType}
                    </Badge>
                  )}
                  <Badge
                    variant="outline"
                    className={appointee.status === 'Filled'
                      ? 'bg-green-100 text-green-800'
                      : 'bg-red-100 text-red-800'}
                  >
                    {appointee.status}
                  </Badge>
                </div>
              </div>
            ))}
          </div>

          {/* Pagination - only for non-search results */}
          {!isSearching && appointeesQuery.data && (
            <div className="flex flex-col sm:flex-row justify-between items-center mt-4 gap-4">
              <p className="text-sm text-muted-foreground">
                Showing {appointeesQuery.data.number * appointeesQuery.data.size + 1} to{' '}
                {Math.min((appointeesQuery.data.number + 1) * appointeesQuery.data.size, appointeesQuery.data.totalElements)} of{' '}
                {appointeesQuery.data.totalElements} appointees
              </p>
              <div className="flex gap-2">
                <Button
                  variant="outline"
                  onClick={() => handlePageChange(page - 1)}
                  disabled={appointeesQuery.data.first}
                >
                  Previous
                </Button>
                <Button
                  variant="outline"
                  onClick={() => handlePageChange(page + 1)}
                  disabled={appointeesQuery.data.last}
                >
                  Next
                </Button>
              </div>
            </div>
          )}

          {/* Search results count */}
          {isSearching && (
            <div className="mt-4">
              <p className="text-sm text-muted-foreground">
                Found {data.length} appointee{data.length !== 1 ? 's' : ''} matching your search
              </p>
            </div>
          )}
        </div>
      )}

      {/* Detail Panel */}
      {selectedAppointee && (
        <AppointeeDetailPanel appointee={selectedAppointee} onClose={handleClosePanel} />
      )}
    </div>
  );
}

function AppointeeTableSkeleton() {
  return (
    <div className="border rounded-lg">
      <div className="p-4 border-b">
        <div className="flex gap-4">
          <Skeleton className="h-6 w-32" />
          <Skeleton className="h-6 w-48" />
          <Skeleton className="h-6 w-40" />
          <Skeleton className="h-6 w-16" />
          <Skeleton className="h-6 w-20" />
        </div>
      </div>
      {Array.from({ length: 10 }).map((_, i) => (
        <div key={i} className="p-4 border-b flex items-center gap-4">
          <Skeleton className="h-5 w-40" />
          <Skeleton className="h-5 w-64" />
          <Skeleton className="h-5 w-48" />
          <Skeleton className="h-6 w-12" />
          <Skeleton className="h-6 w-16" />
        </div>
      ))}
    </div>
  );
}

export default function ExecutiveAppointeesPage() {
  return (
    <Suspense fallback={<PageSkeleton />}>
      <ExecutiveAppointeesContent />
    </Suspense>
  );
}

function PageSkeleton() {
  return (
    <div className="container mx-auto px-6 py-8 max-w-7xl">
      <div className="mb-8">
        <Skeleton className="h-4 w-48 mb-4" />
        <Skeleton className="h-10 w-72 mb-4" />
        <Skeleton className="h-20 w-full max-w-3xl" />
      </div>
      <AppointeeTableSkeleton />
    </div>
  );
}
