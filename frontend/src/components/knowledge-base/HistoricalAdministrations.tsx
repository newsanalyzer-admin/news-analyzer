'use client';

/**
 * HistoricalAdministrations Component (KB-2.3)
 *
 * Master-detail layout: scrollable list of all administrations on the left,
 * detail view on the right. Selection managed via URL query params.
 */

import { useMemo, useState } from 'react';
import { useSearchParams, useRouter } from 'next/navigation';
import { ArrowUpDown, X } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Skeleton } from '@/components/ui/skeleton';
import { useAllPresidencies } from '@/hooks/usePresidencySync';
import { AdministrationListItem } from './AdministrationListItem';
import { AdministrationDetail } from './AdministrationDetail';

export function HistoricalAdministrations() {
  const searchParams = useSearchParams();
  const router = useRouter();
  const { data: presidencies, isLoading, error } = useAllPresidencies();
  const [sortAsc, setSortAsc] = useState(false); // default: most recent first

  const selectedNumber = searchParams.get('presidency')
    ? Number(searchParams.get('presidency'))
    : null;

  const sortedPresidencies = useMemo(() => {
    if (!presidencies) return [];
    const sorted = [...presidencies].sort((a, b) =>
      sortAsc ? a.number - b.number : b.number - a.number
    );
    return sorted;
  }, [presidencies, sortAsc]);

  const selectedPresidency = useMemo(() => {
    if (selectedNumber === null || !presidencies) return null;
    return presidencies.find((p) => p.number === selectedNumber) ?? null;
  }, [presidencies, selectedNumber]);

  const selectAdministration = (number: number) => {
    const params = new URLSearchParams(searchParams.toString());
    params.set('presidency', number.toString());
    router.push(`?${params.toString()}`, { scroll: false });
  };

  const clearSelection = () => {
    const params = new URLSearchParams(searchParams.toString());
    params.delete('presidency');
    router.push(`?${params.toString()}`, { scroll: false });
  };

  if (isLoading) {
    return (
      <div className="space-y-3">
        {Array.from({ length: 6 }).map((_, i) => (
          <Skeleton key={i} className="h-16 w-full rounded-lg" />
        ))}
      </div>
    );
  }

  if (error) {
    return (
      <div className="rounded-lg border border-destructive/50 bg-destructive/10 p-6 text-center">
        <p className="text-destructive font-medium">Failed to load administrations</p>
        <p className="text-sm text-muted-foreground mt-1">
          {error instanceof Error ? error.message : 'An unexpected error occurred'}
        </p>
      </div>
    );
  }

  return (
    <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
      {/* List panel */}
      <div className="lg:col-span-1">
        {/* Sort controls */}
        <div className="flex items-center justify-between mb-3">
          <span className="text-sm text-muted-foreground">
            {sortedPresidencies.length} administrations
          </span>
          <Button
            variant="ghost"
            size="sm"
            onClick={() => setSortAsc((prev) => !prev)}
            className="text-xs"
          >
            <ArrowUpDown className="h-3 w-3 mr-1" />
            {sortAsc ? 'Oldest first' : 'Newest first'}
          </Button>
        </div>

        {/* Scrollable list */}
        <div className="space-y-1 max-h-[600px] overflow-y-auto pr-1">
          {sortedPresidencies.map((presidency) => (
            <AdministrationListItem
              key={presidency.id}
              presidency={presidency}
              isSelected={presidency.number === selectedNumber}
              onSelect={selectAdministration}
            />
          ))}
        </div>
      </div>

      {/* Detail panel */}
      <div className="lg:col-span-2">
        {selectedPresidency ? (
          <div>
            <div className="flex items-center justify-between mb-4">
              <h3 className="text-lg font-semibold">
                {selectedPresidency.ordinalLabel} Administration
              </h3>
              <Button variant="ghost" size="sm" onClick={clearSelection}>
                <X className="h-4 w-4 mr-1" />
                Close
              </Button>
            </div>
            <AdministrationDetail
              presidency={selectedPresidency}
              isLoading={false}
            />
          </div>
        ) : (
          <div className="flex items-center justify-center h-64 rounded-lg border border-dashed text-muted-foreground">
            <p>Select an administration from the list to view details</p>
          </div>
        )}
      </div>
    </div>
  );
}
