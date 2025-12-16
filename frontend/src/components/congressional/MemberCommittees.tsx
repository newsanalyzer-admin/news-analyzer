'use client';

/**
 * MemberCommittees Component
 *
 * Displays committee assignments for a member.
 */

import Link from 'next/link';
import { Badge } from '@/components/ui/badge';
import { Card, CardContent } from '@/components/ui/card';
import { Skeleton } from '@/components/ui/skeleton';
import type { CommitteeMembership, MembershipRole } from '@/types/committee';

interface MemberCommitteesProps {
  committees: CommitteeMembership[];
  isLoading?: boolean;
}

const roleColors: Record<MembershipRole, string> = {
  CHAIR: 'bg-yellow-100 text-yellow-800 border-yellow-200',
  VICE_CHAIR: 'bg-blue-100 text-blue-800 border-blue-200',
  RANKING_MEMBER: 'bg-purple-100 text-purple-800 border-purple-200',
  MEMBER: 'bg-gray-100 text-gray-700 border-gray-200',
  EX_OFFICIO: 'bg-orange-100 text-orange-800 border-orange-200',
};

const roleLabels: Record<MembershipRole, string> = {
  CHAIR: 'Chair',
  VICE_CHAIR: 'Vice Chair',
  RANKING_MEMBER: 'Ranking Member',
  MEMBER: 'Member',
  EX_OFFICIO: 'Ex Officio',
};

const roleOrder: Record<MembershipRole, number> = {
  CHAIR: 0,
  VICE_CHAIR: 1,
  RANKING_MEMBER: 2,
  EX_OFFICIO: 3,
  MEMBER: 4,
};

export function MemberCommittees({ committees, isLoading }: MemberCommitteesProps) {
  if (isLoading) {
    return <MemberCommitteesSkeleton />;
  }

  if (!committees || committees.length === 0) {
    return (
      <div className="flex flex-col items-center justify-center py-8 text-center border rounded-lg bg-gray-50">
        <div className="text-3xl mb-2">&#127970;</div>
        <p className="text-muted-foreground">No committee assignments found</p>
      </div>
    );
  }

  // Sort by role importance
  const sortedCommittees = [...committees].sort(
    (a, b) => (roleOrder[a.role] ?? 99) - (roleOrder[b.role] ?? 99)
  );

  return (
    <div className="space-y-3">
      {sortedCommittees.map((membership) => (
        <Card key={membership.id}>
          <CardContent className="p-4">
            <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2">
              <div className="flex-1 min-w-0">
                <Link
                  href="/committees"
                  className="font-medium hover:underline line-clamp-2"
                >
                  {membership.committee.name}
                </Link>
                <div className="flex flex-wrap gap-2 mt-2">
                  <Badge
                    variant="outline"
                    className="bg-gray-100 text-gray-700 border-gray-200"
                  >
                    {membership.committee.chamber}
                  </Badge>
                  {membership.committee.committeeType !== 'OTHER' && (
                    <Badge
                      variant="outline"
                      className="bg-gray-50 text-gray-600 border-gray-200"
                    >
                      {membership.committee.committeeType.replace('_', ' ')}
                    </Badge>
                  )}
                </div>
              </div>
              <Badge
                variant="outline"
                className={`${roleColors[membership.role]} self-start sm:self-auto`}
              >
                {roleLabels[membership.role]}
              </Badge>
            </div>
          </CardContent>
        </Card>
      ))}
    </div>
  );
}

function MemberCommitteesSkeleton() {
  return (
    <div className="space-y-3">
      {Array.from({ length: 4 }).map((_, i) => (
        <Card key={i}>
          <CardContent className="p-4">
            <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2">
              <div className="flex-1">
                <Skeleton className="h-5 w-64 mb-2" />
                <div className="flex gap-2">
                  <Skeleton className="h-5 w-16" />
                  <Skeleton className="h-5 w-20" />
                </div>
              </div>
              <Skeleton className="h-6 w-20" />
            </div>
          </CardContent>
        </Card>
      ))}
    </div>
  );
}
