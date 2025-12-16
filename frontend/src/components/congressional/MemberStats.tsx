'use client';

/**
 * MemberStats Component
 *
 * Statistics section showing total member count and party distribution.
 */

import { Badge } from '@/components/ui/badge';
import { Skeleton } from '@/components/ui/skeleton';
import { useMemberCount, useMemberStats } from '@/hooks/useMembers';

const partyColors: Record<string, string> = {
  Democrat: 'bg-blue-100 text-blue-800 border-blue-200',
  Republican: 'bg-red-100 text-red-800 border-red-200',
  Independent: 'bg-purple-100 text-purple-800 border-purple-200',
};

export function MemberStats() {
  const { data: count, isLoading: countLoading } = useMemberCount();
  const { partyStats, isLoading: statsLoading } = useMemberStats();

  const isLoading = countLoading || statsLoading;

  if (isLoading) {
    return (
      <div className="flex flex-wrap gap-4 mb-6">
        <Skeleton className="h-8 w-32" />
        <Skeleton className="h-8 w-24" />
        <Skeleton className="h-8 w-24" />
        <Skeleton className="h-8 w-24" />
      </div>
    );
  }

  return (
    <div className="flex flex-wrap items-center gap-4 mb-6">
      <div className="text-lg font-semibold">
        {count?.toLocaleString() || 0} Members
      </div>
      {partyStats && partyStats.length > 0 && (
        <div className="flex flex-wrap gap-2">
          {partyStats.map((stat) => (
            <Badge
              key={stat.party}
              variant="outline"
              className={partyColors[stat.party] || 'bg-gray-100 text-gray-800'}
            >
              {stat.party}: {stat.count}
            </Badge>
          ))}
        </div>
      )}
    </div>
  );
}
