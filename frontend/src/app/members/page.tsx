'use client';

/**
 * Members Listing Page
 *
 * Browse, search, and filter Congressional members.
 */

import { useSearchParams, useRouter, usePathname } from 'next/navigation';
import { Suspense } from 'react';
import { useMembers, useMemberSearch } from '@/hooks/useMembers';
import { MemberFilters } from '@/components/congressional/MemberFilters';
import { MemberTable } from '@/components/congressional/MemberTable';
import { MemberStats } from '@/components/congressional/MemberStats';
import type { Chamber } from '@/types/member';

function MembersContent() {
  const searchParams = useSearchParams();
  const router = useRouter();
  const pathname = usePathname();

  // Parse URL params
  const chamberParam = searchParams.get('chamber');
  const chamber = chamberParam && chamberParam !== 'ALL' ? chamberParam as Chamber : undefined;
  const state = searchParams.get('state') !== 'ALL' ? searchParams.get('state') || undefined : undefined;
  const party = searchParams.get('party') !== 'ALL' ? searchParams.get('party') || undefined : undefined;
  const search = searchParams.get('search') || undefined;
  const page = parseInt(searchParams.get('page') || '0', 10);

  // Determine which query to use based on search
  const membersQuery = useMembers({
    chamber,
    state: state || undefined,
    party: party || undefined,
    page,
    size: 20,
  });

  const searchQuery = useMemberSearch(search || '', {
    page,
    size: 20,
  });

  // Use search results if search query is provided, otherwise use main list
  const isSearching = search && search.length >= 2;
  const activeQuery = isSearching ? searchQuery : membersQuery;

  const handlePageChange = (newPage: number) => {
    const params = new URLSearchParams(searchParams.toString());
    params.set('page', newPage.toString());
    router.push(`${pathname}?${params.toString()}`);
  };

  const handleRetry = () => {
    activeQuery.refetch();
  };

  return (
    <main className="container mx-auto py-8 px-4">
      <div className="mb-8">
        <h1 className="text-3xl font-bold mb-2">Congressional Members</h1>
        <p className="text-muted-foreground">
          Browse and search current members of Congress
        </p>
      </div>

      <MemberStats />

      <MemberFilters />

      <MemberTable
        data={activeQuery.data}
        isLoading={activeQuery.isLoading}
        error={activeQuery.error as Error | null}
        onRetry={handleRetry}
        currentPage={page}
        onPageChange={handlePageChange}
      />
    </main>
  );
}

export default function MembersPage() {
  return (
    <Suspense fallback={<MembersPageSkeleton />}>
      <MembersContent />
    </Suspense>
  );
}

function MembersPageSkeleton() {
  return (
    <main className="container mx-auto py-8 px-4">
      <div className="mb-8">
        <div className="h-10 w-64 bg-gray-200 rounded mb-2" />
        <div className="h-5 w-96 bg-gray-100 rounded" />
      </div>
    </main>
  );
}
