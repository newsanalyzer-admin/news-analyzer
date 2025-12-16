'use client';

/**
 * CommitteeHierarchy Component
 *
 * Hierarchical display of committees grouped by chamber.
 * Shows parent committees with expandable subcommittee lists.
 */

import { useState } from 'react';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { Skeleton } from '@/components/ui/skeleton';
import { ChevronDown, ChevronRight, Users } from 'lucide-react';
import { CommitteeMembersDialog } from './CommitteeMembersDialog';
import {
  buildCommitteeHierarchy,
  groupCommitteesByChamber,
  type CommitteeWithChildren,
} from '@/lib/utils/committee-stats';
import type { Committee, CommitteeChamber, CommitteeType } from '@/types/committee';

interface CommitteeHierarchyProps {
  committees: Committee[];
  isLoading: boolean;
  error: Error | null;
  onRetry: () => void;
  chamberFilter?: CommitteeChamber;
}

const typeColors: Record<CommitteeType, string> = {
  STANDING: 'bg-green-100 text-green-800 border-green-200',
  SELECT: 'bg-blue-100 text-blue-800 border-blue-200',
  SPECIAL: 'bg-purple-100 text-purple-800 border-purple-200',
  JOINT: 'bg-orange-100 text-orange-800 border-orange-200',
  SUBCOMMITTEE: 'bg-gray-100 text-gray-800 border-gray-200',
  OTHER: 'bg-gray-100 text-gray-600 border-gray-200',
};

const chamberStyles: Record<CommitteeChamber, { color: string; bg: string; label: string }> = {
  SENATE: { color: 'text-blue-700', bg: 'bg-blue-50', label: 'Senate' },
  HOUSE: { color: 'text-red-700', bg: 'bg-red-50', label: 'House' },
  JOINT: { color: 'text-purple-700', bg: 'bg-purple-50', label: 'Joint' },
};

export function CommitteeHierarchy({
  committees,
  isLoading,
  error,
  onRetry,
  chamberFilter,
}: CommitteeHierarchyProps) {
  const [expandedChambers, setExpandedChambers] = useState<Set<CommitteeChamber>>(
    new Set(['SENATE', 'HOUSE', 'JOINT'])
  );
  const [expandedCommittees, setExpandedCommittees] = useState<Set<string>>(new Set());
  const [selectedCommitteeCode, setSelectedCommitteeCode] = useState<string | null>(null);

  if (isLoading) {
    return <CommitteeHierarchySkeleton />;
  }

  if (error) {
    return (
      <div className="flex flex-col items-center justify-center py-12 text-center border rounded-lg">
        <div className="text-4xl mb-4">⚠️</div>
        <h3 className="text-lg font-semibold mb-2">Failed to load committees</h3>
        <p className="text-muted-foreground mb-4">{error.message}</p>
        <Button onClick={onRetry}>Try Again</Button>
      </div>
    );
  }

  if (!committees || committees.length === 0) {
    return (
      <div className="flex flex-col items-center justify-center py-12 text-center border rounded-lg">
        <div className="text-4xl mb-4">🔍</div>
        <h3 className="text-lg font-semibold mb-2">No committees found</h3>
        <p className="text-muted-foreground">
          Try adjusting your search or filter criteria.
        </p>
      </div>
    );
  }

  const hierarchy = buildCommitteeHierarchy(committees);
  const grouped = groupCommitteesByChamber(hierarchy);

  const toggleChamber = (chamber: CommitteeChamber) => {
    setExpandedChambers((prev) => {
      const next = new Set(prev);
      if (next.has(chamber)) {
        next.delete(chamber);
      } else {
        next.add(chamber);
      }
      return next;
    });
  };

  const toggleCommittee = (code: string) => {
    setExpandedCommittees((prev) => {
      const next = new Set(prev);
      if (next.has(code)) {
        next.delete(code);
      } else {
        next.add(code);
      }
      return next;
    });
  };

  // Determine which chambers to display
  const chambersToDisplay: CommitteeChamber[] = chamberFilter
    ? [chamberFilter]
    : ['SENATE', 'HOUSE', 'JOINT'];

  return (
    <div className="space-y-4">
      {chambersToDisplay.map((chamber) => {
        const chamberCommittees = grouped[chamber];
        const style = chamberStyles[chamber];
        const isExpanded = expandedChambers.has(chamber);

        if (chamberCommittees.length === 0) return null;

        return (
          <div key={chamber} className="border rounded-lg overflow-hidden">
            {/* Chamber Header */}
            <button
              onClick={() => toggleChamber(chamber)}
              className={`w-full flex items-center justify-between p-4 ${style.bg} hover:opacity-90 transition-opacity`}
            >
              <div className="flex items-center gap-2">
                {isExpanded ? (
                  <ChevronDown className={`h-5 w-5 ${style.color}`} />
                ) : (
                  <ChevronRight className={`h-5 w-5 ${style.color}`} />
                )}
                <h2 className={`text-lg font-semibold ${style.color}`}>
                  {style.label} Committees
                </h2>
                <Badge variant="outline" className="ml-2">
                  {chamberCommittees.length}
                </Badge>
              </div>
            </button>

            {/* Chamber Committees */}
            {isExpanded && (
              <div className="p-4 space-y-3">
                {chamberCommittees.map((committee) => (
                  <CommitteeCard
                    key={committee.committeeCode}
                    committee={committee}
                    isExpanded={expandedCommittees.has(committee.committeeCode)}
                    onToggle={() => toggleCommittee(committee.committeeCode)}
                    onViewMembers={() => setSelectedCommitteeCode(committee.committeeCode)}
                  />
                ))}
              </div>
            )}
          </div>
        );
      })}

      {/* Members Dialog */}
      <CommitteeMembersDialog
        committeeCode={selectedCommitteeCode}
        isOpen={!!selectedCommitteeCode}
        onClose={() => setSelectedCommitteeCode(null)}
      />
    </div>
  );
}

interface CommitteeCardProps {
  committee: CommitteeWithChildren;
  isExpanded: boolean;
  onToggle: () => void;
  onViewMembers: () => void;
}

function CommitteeCard({ committee, isExpanded, onToggle, onViewMembers }: CommitteeCardProps) {
  const hasSubcommittees = committee.subcommittees.length > 0;

  return (
    <Card>
      <CardHeader className="py-3 px-4">
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2">
          <div className="flex items-center gap-2 flex-1 min-w-0">
            {hasSubcommittees && (
              <button onClick={onToggle} className="p-1 hover:bg-gray-100 rounded">
                {isExpanded ? (
                  <ChevronDown className="h-4 w-4 text-gray-500" />
                ) : (
                  <ChevronRight className="h-4 w-4 text-gray-500" />
                )}
              </button>
            )}
            <CardTitle className="text-base font-medium truncate">
              {committee.name}
            </CardTitle>
          </div>
          <div className="flex items-center gap-2 flex-shrink-0">
            <Badge
              variant="outline"
              className={typeColors[committee.committeeType]}
            >
              {committee.committeeType.replace('_', ' ')}
            </Badge>
            <Button
              variant="ghost"
              size="sm"
              onClick={onViewMembers}
              className="flex items-center gap-1"
            >
              <Users className="h-4 w-4" />
              <span className="hidden sm:inline">Members</span>
            </Button>
          </div>
        </div>
      </CardHeader>

      {/* Subcommittees */}
      {hasSubcommittees && isExpanded && (
        <CardContent className="pt-0 pb-3 px-4">
          <div className="ml-6 border-l-2 border-gray-200 pl-4 space-y-2">
            {committee.subcommittees.map((sub) => (
              <SubcommitteeRow
                key={sub.committeeCode}
                subcommittee={sub}
                onViewMembers={() => {}}
              />
            ))}
          </div>
        </CardContent>
      )}
    </Card>
  );
}

interface SubcommitteeRowProps {
  subcommittee: CommitteeWithChildren;
  onViewMembers: () => void;
}

function SubcommitteeRow({ subcommittee }: SubcommitteeRowProps) {
  const [selectedCode, setSelectedCode] = useState<string | null>(null);

  return (
    <>
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-1 py-2">
        <span className="text-sm text-gray-700 truncate flex-1">
          {subcommittee.name}
        </span>
        <div className="flex items-center gap-2 flex-shrink-0">
          <Badge
            variant="outline"
            className={typeColors[subcommittee.committeeType]}
          >
            Sub
          </Badge>
          <Button
            variant="ghost"
            size="sm"
            onClick={() => setSelectedCode(subcommittee.committeeCode)}
            className="h-7 px-2"
          >
            <Users className="h-3 w-3" />
          </Button>
        </div>
      </div>
      <CommitteeMembersDialog
        committeeCode={selectedCode}
        isOpen={!!selectedCode}
        onClose={() => setSelectedCode(null)}
      />
    </>
  );
}

function CommitteeHierarchySkeleton() {
  return (
    <div className="space-y-4">
      {['Senate', 'House', 'Joint'].map((chamber) => (
        <div key={chamber} className="border rounded-lg overflow-hidden">
          <Skeleton className="h-14 w-full" />
          <div className="p-4 space-y-3">
            {Array.from({ length: 4 }).map((_, i) => (
              <Skeleton key={i} className="h-16 w-full rounded-lg" />
            ))}
          </div>
        </div>
      ))}
    </div>
  );
}
