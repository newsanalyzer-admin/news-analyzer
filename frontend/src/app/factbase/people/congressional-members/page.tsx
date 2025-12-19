'use client';

/**
 * Congressional Members Page (Public Factbase)
 *
 * Browse and filter Congressional members with click-to-view detail panel.
 */

import { Suspense, useState } from 'react';
import { useSearchParams, useRouter, usePathname } from 'next/navigation';
import { ContentPageHeader } from '@/components/public';
import { getPageDescriptionOrDefault } from '@/lib/page-descriptions';
import { MemberFilters } from '@/components/congressional/MemberFilters';
import { MemberPhoto } from '@/components/congressional/MemberPhoto';
import { MemberDetailPanel } from './MemberDetailPanel';
import { useMembers, useMemberSearch } from '@/hooks/useMembers';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { Skeleton } from '@/components/ui/skeleton';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table';
import type { Person, Chamber } from '@/types/member';

const partyColors: Record<string, string> = {
  Democrat: 'bg-blue-100 text-blue-800',
  Democratic: 'bg-blue-100 text-blue-800',
  Republican: 'bg-red-100 text-red-800',
  Independent: 'bg-purple-100 text-purple-800',
};

function CongressionalMembersContent() {
  const searchParams = useSearchParams();
  const router = useRouter();
  const pathname = usePathname();
  const [selectedMember, setSelectedMember] = useState<Person | null>(null);

  const { title, description } = getPageDescriptionOrDefault('/factbase/people/congressional-members');

  // Parse URL params
  const chamberParam = searchParams.get('chamber');
  const chamber = chamberParam && chamberParam !== 'ALL' ? chamberParam as Chamber : undefined;
  const state = searchParams.get('state') !== 'ALL' ? searchParams.get('state') || undefined : undefined;
  const party = searchParams.get('party') !== 'ALL' ? searchParams.get('party') || undefined : undefined;
  const search = searchParams.get('search') || undefined;
  const page = parseInt(searchParams.get('page') || '0', 10);

  // Queries
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

  // Use search results if search query is provided
  const isSearching = search && search.length >= 2;
  const activeQuery = isSearching ? searchQuery : membersQuery;
  const data = activeQuery.data;

  const handlePageChange = (newPage: number) => {
    const params = new URLSearchParams(searchParams.toString());
    params.set('page', newPage.toString());
    router.push(`${pathname}?${params.toString()}`);
  };

  const handleMemberClick = (member: Person) => {
    setSelectedMember(member);
  };

  const handleClosePanel = () => {
    setSelectedMember(null);
  };

  return (
    <div className="container mx-auto px-6 py-8 max-w-7xl">
      <ContentPageHeader
        title={title}
        description={description}
        breadcrumbs={[
          { label: 'Factbase', href: '/factbase' },
          { label: 'People', href: '/factbase/people' },
          { label: 'Congressional Members' },
        ]}
      />

      <MemberFilters />

      {/* Loading State */}
      {activeQuery.isLoading && <MemberTableSkeleton />}

      {/* Error State */}
      {activeQuery.error && (
        <div className="flex flex-col items-center justify-center py-12 text-center border rounded-lg">
          <div className="text-4xl mb-4">⚠️</div>
          <h3 className="text-lg font-semibold mb-2">Failed to load members</h3>
          <p className="text-muted-foreground mb-4">{(activeQuery.error as Error).message}</p>
          <Button onClick={() => activeQuery.refetch()}>Try Again</Button>
        </div>
      )}

      {/* Empty State */}
      {!activeQuery.isLoading && !activeQuery.error && (!data || data.content.length === 0) && (
        <div className="flex flex-col items-center justify-center py-12 text-center border rounded-lg">
          <div className="text-4xl mb-4">🔍</div>
          <h3 className="text-lg font-semibold mb-2">No members found</h3>
          <p className="text-muted-foreground">
            Try adjusting your search or filter criteria.
          </p>
        </div>
      )}

      {/* Data Table */}
      {!activeQuery.isLoading && !activeQuery.error && data && data.content.length > 0 && (
        <div>
          {/* Desktop Table */}
          <div className="border rounded-lg overflow-hidden hidden md:block">
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead className="w-[60px]">Photo</TableHead>
                  <TableHead>Name</TableHead>
                  <TableHead className="w-[120px]">Party</TableHead>
                  <TableHead className="w-[80px]">State</TableHead>
                  <TableHead className="w-[100px]">Chamber</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {data.content.map((member) => (
                  <TableRow
                    key={member.bioguideId}
                    className="cursor-pointer hover:bg-muted/50"
                    onClick={() => handleMemberClick(member)}
                  >
                    <TableCell>
                      <MemberPhoto
                        imageUrl={member.imageUrl}
                        firstName={member.firstName}
                        lastName={member.lastName}
                        size={40}
                      />
                    </TableCell>
                    <TableCell className="font-medium">
                      {member.firstName} {member.lastName}
                      {member.suffix && ` ${member.suffix}`}
                    </TableCell>
                    <TableCell>
                      {member.party && (
                        <Badge
                          className={partyColors[member.party] || 'bg-gray-100 text-gray-800'}
                          variant="outline"
                        >
                          {member.party}
                        </Badge>
                      )}
                    </TableCell>
                    <TableCell>{member.state || '-'}</TableCell>
                    <TableCell>{member.chamber === 'SENATE' ? 'Senate' : member.chamber === 'HOUSE' ? 'House' : member.chamber || '-'}</TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </div>

          {/* Mobile Card View */}
          <div className="md:hidden space-y-4">
            {data.content.map((member) => (
              <div
                key={member.bioguideId}
                className="border rounded-lg p-4 flex items-center gap-4 cursor-pointer hover:bg-muted/50"
                onClick={() => handleMemberClick(member)}
              >
                <MemberPhoto
                  imageUrl={member.imageUrl}
                  firstName={member.firstName}
                  lastName={member.lastName}
                  size={48}
                />
                <div className="flex-1 min-w-0">
                  <div className="font-medium truncate">
                    {member.firstName} {member.lastName}
                  </div>
                  <div className="flex items-center gap-2 mt-1">
                    {member.party && (
                      <Badge
                        className={partyColors[member.party] || 'bg-gray-100 text-gray-800'}
                        variant="outline"
                      >
                        {member.party.charAt(0)}
                      </Badge>
                    )}
                    <span className="text-sm text-muted-foreground">
                      {member.state} • {member.chamber === 'SENATE' ? 'Senate' : 'House'}
                    </span>
                  </div>
                </div>
              </div>
            ))}
          </div>

          {/* Pagination */}
          <div className="flex flex-col sm:flex-row justify-between items-center mt-4 gap-4">
            <p className="text-sm text-muted-foreground">
              Showing {data.number * data.size + 1} to{' '}
              {Math.min((data.number + 1) * data.size, data.totalElements)} of{' '}
              {data.totalElements} members
            </p>
            <div className="flex gap-2">
              <Button
                variant="outline"
                onClick={() => handlePageChange(page - 1)}
                disabled={data.first}
              >
                Previous
              </Button>
              <Button
                variant="outline"
                onClick={() => handlePageChange(page + 1)}
                disabled={data.last}
              >
                Next
              </Button>
            </div>
          </div>
        </div>
      )}

      {/* Detail Panel */}
      {selectedMember && (
        <MemberDetailPanel member={selectedMember} onClose={handleClosePanel} />
      )}
    </div>
  );
}

function MemberTableSkeleton() {
  return (
    <div className="border rounded-lg">
      <div className="p-4 border-b">
        <div className="flex gap-4">
          <Skeleton className="h-6 w-12" />
          <Skeleton className="h-6 w-32" />
          <Skeleton className="h-6 w-24" />
          <Skeleton className="h-6 w-16" />
          <Skeleton className="h-6 w-20" />
        </div>
      </div>
      {Array.from({ length: 10 }).map((_, i) => (
        <div key={i} className="p-4 border-b flex items-center gap-4">
          <Skeleton className="h-10 w-10 rounded-full" />
          <Skeleton className="h-5 w-40" />
          <Skeleton className="h-6 w-20" />
          <Skeleton className="h-5 w-8" />
          <Skeleton className="h-5 w-16" />
        </div>
      ))}
    </div>
  );
}

export default function CongressionalMembersPage() {
  return (
    <Suspense fallback={<PageSkeleton />}>
      <CongressionalMembersContent />
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
      <MemberTableSkeleton />
    </div>
  );
}
