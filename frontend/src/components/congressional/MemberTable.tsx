'use client';

/**
 * MemberTable Component
 *
 * Table display of Congressional members with pagination.
 */

import Link from 'next/link';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { Skeleton } from '@/components/ui/skeleton';
import { MemberPhoto } from './MemberPhoto';
import type { Person } from '@/types/member';
import type { Page } from '@/types/pagination';

interface MemberTableProps {
  data?: Page<Person>;
  isLoading: boolean;
  error: Error | null;
  onRetry: () => void;
  currentPage: number;
  onPageChange: (page: number) => void;
}

const partyColors: Record<string, string> = {
  Democrat: 'bg-blue-100 text-blue-800',
  Republican: 'bg-red-100 text-red-800',
  Independent: 'bg-purple-100 text-purple-800',
};

export function MemberTable({
  data,
  isLoading,
  error,
  onRetry,
  currentPage,
  onPageChange,
}: MemberTableProps) {
  if (isLoading) {
    return <MemberTableSkeleton />;
  }

  if (error) {
    return (
      <div className="flex flex-col items-center justify-center py-12 text-center border rounded-lg">
        <div className="text-4xl mb-4">⚠️</div>
        <h3 className="text-lg font-semibold mb-2">Failed to load members</h3>
        <p className="text-muted-foreground mb-4">{error.message}</p>
        <Button onClick={onRetry}>Try Again</Button>
      </div>
    );
  }

  if (!data || data.content.length === 0) {
    return (
      <div className="flex flex-col items-center justify-center py-12 text-center border rounded-lg">
        <div className="text-4xl mb-4">🔍</div>
        <h3 className="text-lg font-semibold mb-2">No members found</h3>
        <p className="text-muted-foreground">
          Try adjusting your search or filter criteria.
        </p>
      </div>
    );
  }

  return (
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
              <TableHead className="w-[100px]">Actions</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {data.content.map((member) => (
              <TableRow key={member.bioguideId}>
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
                <TableCell>{member.chamber || '-'}</TableCell>
                <TableCell>
                  <Link
                    href={`/members/${member.bioguideId}`}
                    className="text-blue-600 hover:underline text-sm"
                  >
                    View →
                  </Link>
                </TableCell>
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
            className="border rounded-lg p-4 flex items-center gap-4"
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
                  {member.state} • {member.chamber}
                </span>
              </div>
            </div>
            <Link
              href={`/members/${member.bioguideId}`}
              className="text-blue-600 hover:underline text-sm"
            >
              View
            </Link>
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
            onClick={() => onPageChange(currentPage - 1)}
            disabled={data.first}
          >
            Previous
          </Button>
          <Button
            variant="outline"
            onClick={() => onPageChange(currentPage + 1)}
            disabled={data.last}
          >
            Next
          </Button>
        </div>
      </div>
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
