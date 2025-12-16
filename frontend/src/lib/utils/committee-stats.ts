/**
 * Committee Statistics Utilities
 *
 * Client-side computation of committee statistics from committee list data.
 */

import type { Committee, CommitteeType, CommitteeChamber } from '@/types/committee';

export interface CommitteeStats {
  total: number;
  byType: Record<CommitteeType, number>;
  byChamber: Record<CommitteeChamber, number>;
}

/**
 * Compute statistics from a list of committees
 */
export function computeCommitteeStats(committees: Committee[]): CommitteeStats {
  const byType: Record<string, number> = {};
  const byChamber: Record<string, number> = {};

  committees.forEach((c) => {
    byType[c.committeeType] = (byType[c.committeeType] || 0) + 1;
    byChamber[c.chamber] = (byChamber[c.chamber] || 0) + 1;
  });

  return {
    total: committees.length,
    byType: byType as Record<CommitteeType, number>,
    byChamber: byChamber as Record<CommitteeChamber, number>,
  };
}

/**
 * Committee with children for hierarchy building
 */
export interface CommitteeWithChildren extends Committee {
  subcommittees: CommitteeWithChildren[];
}

/**
 * Build hierarchy from flat committee list
 */
export function buildCommitteeHierarchy(committees: Committee[]): CommitteeWithChildren[] {
  const parentCommittees = committees.filter(c => !c.parentCommitteeCode);
  return parentCommittees.map(parent => ({
    ...parent,
    subcommittees: committees
      .filter(c => c.parentCommitteeCode === parent.committeeCode)
      .map(sub => ({ ...sub, subcommittees: [] })),
  }));
}

/**
 * Group committees by chamber
 */
export function groupCommitteesByChamber(
  committees: CommitteeWithChildren[]
): Record<CommitteeChamber, CommitteeWithChildren[]> {
  const grouped: Record<CommitteeChamber, CommitteeWithChildren[]> = {
    SENATE: [],
    HOUSE: [],
    JOINT: [],
  };

  committees.forEach(committee => {
    grouped[committee.chamber].push(committee);
  });

  return grouped;
}
