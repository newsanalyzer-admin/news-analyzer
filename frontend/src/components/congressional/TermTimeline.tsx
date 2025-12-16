'use client';

/**
 * TermTimeline Component
 *
 * Displays Congressional term history in a timeline format.
 */

import { Badge } from '@/components/ui/badge';
import { Skeleton } from '@/components/ui/skeleton';
import {
  isCurrentTerm,
  getTermDisplayInfo,
  formatTermDisplay,
  sortTermsByDate,
} from '@/lib/utils/term-helpers';
import type { Person, PositionHolding } from '@/types/member';

interface TermTimelineProps {
  terms: PositionHolding[];
  person: Person;
  isLoading?: boolean;
}

export function TermTimeline({ terms, person, isLoading }: TermTimelineProps) {
  if (isLoading) {
    return <TermTimelineSkeleton />;
  }

  if (!terms || terms.length === 0) {
    return (
      <div className="flex flex-col items-center justify-center py-8 text-center border rounded-lg bg-gray-50">
        <div className="text-3xl mb-2">&#128197;</div>
        <p className="text-muted-foreground">No term history available</p>
      </div>
    );
  }

  const sortedTerms = sortTermsByDate(terms);

  return (
    <div className="relative">
      {/* Timeline line */}
      <div className="absolute left-4 top-0 bottom-0 w-0.5 bg-gray-200" />

      <div className="space-y-4">
        {sortedTerms.map((term, index) => {
          const isCurrent = isCurrentTerm(term);
          const { label } = getTermDisplayInfo(term, person);
          const description = formatTermDisplay(term, person);

          return (
            <div key={term.id || index} className="relative pl-10">
              {/* Timeline dot */}
              <div
                className={`absolute left-2.5 w-3 h-3 rounded-full border-2 ${
                  isCurrent
                    ? 'bg-green-500 border-green-500'
                    : 'bg-white border-gray-300'
                }`}
                style={{ top: '0.75rem' }}
              />

              {/* Term card */}
              <div
                className={`p-4 rounded-lg border ${
                  isCurrent
                    ? 'bg-green-50 border-green-200'
                    : 'bg-white border-gray-200'
                }`}
              >
                <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-2">
                  <div>
                    <div className="font-semibold flex items-center gap-2 flex-wrap">
                      {label}
                      {isCurrent && (
                        <Badge
                          variant="outline"
                          className="bg-green-100 text-green-800 border-green-200"
                        >
                          CURRENT
                        </Badge>
                      )}
                    </div>
                    <div className="text-sm text-muted-foreground mt-1">
                      {description}
                    </div>
                  </div>
                  {term.congress && (
                    <Badge variant="secondary" className="self-start sm:self-auto">
                      {term.congress}th Congress
                    </Badge>
                  )}
                </div>
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
}

function TermTimelineSkeleton() {
  return (
    <div className="relative">
      <div className="absolute left-4 top-0 bottom-0 w-0.5 bg-gray-200" />
      <div className="space-y-4">
        {Array.from({ length: 4 }).map((_, i) => (
          <div key={i} className="relative pl-10">
            <div
              className="absolute left-2.5 w-3 h-3 rounded-full bg-gray-200 border-2 border-gray-200"
              style={{ top: '0.75rem' }}
            />
            <div className="p-4 rounded-lg border bg-white">
              <Skeleton className="h-5 w-48 mb-2" />
              <Skeleton className="h-4 w-32" />
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}
