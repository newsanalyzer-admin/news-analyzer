'use client';

/**
 * CommitteeStats Component
 *
 * Statistics section showing total committee count and type/chamber distribution.
 * Stats are computed client-side from the committee list data.
 */

import { useMemo } from 'react';
import { Badge } from '@/components/ui/badge';
import { Skeleton } from '@/components/ui/skeleton';
import { computeCommitteeStats } from '@/lib/utils/committee-stats';
import type { Committee, CommitteeType, CommitteeChamber } from '@/types/committee';

interface CommitteeStatsProps {
  committees: Committee[];
  isLoading: boolean;
}

const typeColors: Record<CommitteeType, string> = {
  STANDING: 'bg-green-100 text-green-800 border-green-200',
  SELECT: 'bg-blue-100 text-blue-800 border-blue-200',
  SPECIAL: 'bg-purple-100 text-purple-800 border-purple-200',
  JOINT: 'bg-orange-100 text-orange-800 border-orange-200',
  SUBCOMMITTEE: 'bg-gray-100 text-gray-800 border-gray-200',
  OTHER: 'bg-gray-100 text-gray-600 border-gray-200',
};

const typeLabels: Record<CommitteeType, string> = {
  STANDING: 'Standing',
  SELECT: 'Select',
  SPECIAL: 'Special',
  JOINT: 'Joint',
  SUBCOMMITTEE: 'Subcommittee',
  OTHER: 'Other',
};

const chamberColors: Record<CommitteeChamber, string> = {
  SENATE: 'bg-blue-100 text-blue-800 border-blue-200',
  HOUSE: 'bg-red-100 text-red-800 border-red-200',
  JOINT: 'bg-purple-100 text-purple-800 border-purple-200',
};

const chamberLabels: Record<CommitteeChamber, string> = {
  SENATE: 'Senate',
  HOUSE: 'House',
  JOINT: 'Joint',
};

export function CommitteeStats({ committees, isLoading }: CommitteeStatsProps) {
  const stats = useMemo(() => {
    if (!committees || committees.length === 0) {
      return null;
    }
    return computeCommitteeStats(committees);
  }, [committees]);

  if (isLoading) {
    return (
      <div className="flex flex-wrap gap-4 mb-6">
        <Skeleton className="h-8 w-36" />
        <div className="flex flex-wrap gap-2">
          <Skeleton className="h-8 w-24" />
          <Skeleton className="h-8 w-24" />
          <Skeleton className="h-8 w-24" />
        </div>
      </div>
    );
  }

  if (!stats) {
    return null;
  }

  // Get non-zero type stats
  const typeStats = Object.entries(stats.byType)
    .filter(([, count]) => count > 0)
    .sort(([, a], [, b]) => b - a);

  // Get non-zero chamber stats
  const chamberStats = Object.entries(stats.byChamber)
    .filter(([, count]) => count > 0)
    .sort(([, a], [, b]) => b - a);

  return (
    <div className="flex flex-col sm:flex-row sm:items-center gap-4 mb-6">
      <div className="text-lg font-semibold">
        {stats.total.toLocaleString()} Committees
      </div>

      {/* Chamber Distribution */}
      {chamberStats.length > 0 && (
        <div className="flex flex-wrap gap-2">
          {chamberStats.map(([chamber, count]) => (
            <Badge
              key={chamber}
              variant="outline"
              className={chamberColors[chamber as CommitteeChamber]}
            >
              {chamberLabels[chamber as CommitteeChamber]}: {count}
            </Badge>
          ))}
        </div>
      )}

      {/* Type Distribution */}
      {typeStats.length > 0 && (
        <div className="flex flex-wrap gap-2">
          {typeStats.map(([type, count]) => (
            <Badge
              key={type}
              variant="outline"
              className={typeColors[type as CommitteeType]}
            >
              {typeLabels[type as CommitteeType]}: {count}
            </Badge>
          ))}
        </div>
      )}
    </div>
  );
}
