'use client';

/**
 * Committees Listing Page
 *
 * Browse Congressional committees organized by chamber with subcommittee hierarchy.
 */

import { Suspense } from 'react';
import { useSearchParams } from 'next/navigation';
import { useCommittees, useCommitteeSearch } from '@/hooks/useCommittees';
import { CommitteeFilters } from '@/components/congressional/CommitteeFilters';
import { CommitteeHierarchy } from '@/components/congressional/CommitteeHierarchy';
import { CommitteeStats } from '@/components/congressional/CommitteeStats';
import type { CommitteeChamber, CommitteeType, Committee } from '@/types/committee';

function CommitteesContent() {
  const searchParams = useSearchParams();

  // Parse URL params
  const chamberParam = searchParams.get('chamber');
  const chamber = chamberParam && chamberParam !== 'ALL' ? chamberParam as CommitteeChamber : undefined;
  const typeParam = searchParams.get('type');
  const committeeType = typeParam && typeParam !== 'ALL' ? typeParam as CommitteeType : undefined;
  const search = searchParams.get('search') || undefined;

  // Fetch committees - get large page to build hierarchy client-side
  const committeesQuery = useCommittees({
    chamber,
    type: committeeType,
    size: 500, // Fetch all to build hierarchy
  });

  const searchQuery = useCommitteeSearch(search || '', { size: 500 });

  // Use search results if search query is provided
  const isSearching = search && search.length >= 2;
  const activeQuery = isSearching ? searchQuery : committeesQuery;

  // Filter committees based on type if specified
  const filterCommittees = (committees: Committee[]): Committee[] => {
    if (!committeeType) return committees;
    return committees.filter(c => c.committeeType === committeeType);
  };

  const filteredCommittees = activeQuery.data?.content
    ? filterCommittees(activeQuery.data.content)
    : [];

  const handleRetry = () => {
    activeQuery.refetch();
  };

  return (
    <main className="container mx-auto py-8 px-4">
      <div className="mb-8">
        <h1 className="text-3xl font-bold mb-2">Congressional Committees</h1>
        <p className="text-muted-foreground">
          Browse committees by chamber with subcommittee hierarchy
        </p>
      </div>

      <CommitteeStats committees={filteredCommittees} isLoading={activeQuery.isLoading} />

      <CommitteeFilters />

      <CommitteeHierarchy
        committees={filteredCommittees}
        isLoading={activeQuery.isLoading}
        error={activeQuery.error as Error | null}
        onRetry={handleRetry}
        chamberFilter={chamber}
      />
    </main>
  );
}

export default function CommitteesPage() {
  return (
    <Suspense fallback={<CommitteesPageSkeleton />}>
      <CommitteesContent />
    </Suspense>
  );
}

function CommitteesPageSkeleton() {
  return (
    <main className="container mx-auto py-8 px-4">
      <div className="mb-8">
        <div className="h-10 w-72 bg-gray-200 rounded mb-2" />
        <div className="h-5 w-96 bg-gray-100 rounded" />
      </div>
    </main>
  );
}
