/**
 * Term Helper Utilities
 *
 * Utilities for working with Congressional term data.
 */

import type { PositionHolding, Person } from '@/types/member';

/**
 * Check if a term is current (still serving)
 */
export function isCurrentTerm(term: PositionHolding): boolean {
  return term.endDate === null || term.endDate === undefined;
}

/**
 * Check if member is currently serving in any position
 */
export function isCurrentlyServing(terms: PositionHolding[]): boolean {
  return terms.some(isCurrentTerm);
}

/**
 * Get term display information
 */
export function getTermDisplayInfo(term: PositionHolding, person: Person): {
  label: string;
  chamber: 'Senate' | 'House';
  isCurrent: boolean;
} {
  const isCurrent = isCurrentTerm(term);

  // termLabel is pre-computed by backend if available
  const label = term.termLabel || formatTermLabel(term);

  // Chamber from person's current chamber
  const chamber = person.chamber === 'SENATE' ? 'Senate' : 'House';

  return { label, chamber, isCurrent };
}

/**
 * Format term label from term data
 */
function formatTermLabel(term: PositionHolding): string {
  const congressNum = term.congress;
  const startYear = term.startDate ? new Date(term.startDate).getFullYear() : '?';
  const endYear = term.endDate ? new Date(term.endDate).getFullYear() : 'present';

  if (congressNum) {
    return `${congressNum}${getOrdinalSuffix(congressNum)} Congress (${startYear}-${endYear})`;
  }
  return `${startYear}-${endYear}`;
}

/**
 * Get ordinal suffix for congress number
 */
function getOrdinalSuffix(n: number): string {
  const s = ['th', 'st', 'nd', 'rd'];
  const v = n % 100;
  return s[(v - 20) % 10] || s[v] || s[0];
}

/**
 * Format term for display
 */
export function formatTermDisplay(term: PositionHolding, person: Person): string {
  const { chamber } = getTermDisplayInfo(term, person);
  const position = chamber === 'Senate' ? 'Senator' : 'Representative';
  const location = person.state ? `from ${person.state}` : '';

  return `${position} ${location}`.trim();
}

/**
 * Sort terms by date (most recent first)
 */
export function sortTermsByDate(terms: PositionHolding[]): PositionHolding[] {
  return [...terms].sort((a, b) => {
    // Current terms (no end date) come first
    if (isCurrentTerm(a) && !isCurrentTerm(b)) return -1;
    if (!isCurrentTerm(a) && isCurrentTerm(b)) return 1;

    // Then sort by start date descending
    const dateA = new Date(a.startDate).getTime();
    const dateB = new Date(b.startDate).getTime();
    return dateB - dateA;
  });
}
