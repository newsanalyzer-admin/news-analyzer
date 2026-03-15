'use client';

/**
 * AdministrationListItem Component (KB-2.3)
 *
 * A single row in the historical administrations list.
 * Displays presidency number, president name, term, and party.
 */

import { Badge } from '@/components/ui/badge';
import { cn } from '@/lib/utils';
import type { PresidencyDTO } from '@/hooks/usePresidencySync';

export interface AdministrationListItemProps {
  presidency: PresidencyDTO;
  isSelected: boolean;
  onSelect: (number: number) => void;
}

/**
 * Get a compact party color class for the list item badge
 */
function getPartyColor(party: string): string {
  const p = party?.toLowerCase() || '';
  if (p.includes('republican')) return 'bg-red-100 text-red-700 dark:bg-red-950 dark:text-red-300';
  if (p.includes('democrat')) return 'bg-blue-100 text-blue-700 dark:bg-blue-950 dark:text-blue-300';
  if (p.includes('whig')) return 'bg-amber-100 text-amber-700 dark:bg-amber-950 dark:text-amber-300';
  if (p.includes('federalist')) return 'bg-purple-100 text-purple-700 dark:bg-purple-950 dark:text-purple-300';
  return 'bg-gray-100 text-gray-700 dark:bg-gray-800 dark:text-gray-300';
}

export function AdministrationListItem({
  presidency,
  isSelected,
  onSelect,
}: AdministrationListItemProps) {
  return (
    <button
      type="button"
      onClick={() => onSelect(presidency.number)}
      className={cn(
        'w-full text-left p-3 rounded-lg border transition-colors',
        'hover:bg-muted/50 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring',
        isSelected
          ? 'border-primary bg-primary/5 shadow-sm'
          : 'border-transparent',
      )}
    >
      <div className="flex items-center gap-3">
        {/* Presidency number */}
        <span className="text-lg font-bold text-muted-foreground w-8 text-right flex-shrink-0">
          {presidency.number}
        </span>

        {/* Name and details */}
        <div className="flex-1 min-w-0">
          <div className="flex items-center gap-2">
            <span className="font-medium truncate">{presidency.presidentFullName}</span>
            {presidency.current && (
              <Badge variant="secondary" className="bg-green-100 text-green-700 dark:bg-green-950 dark:text-green-300 text-xs flex-shrink-0">
                Current
              </Badge>
            )}
          </div>
          <div className="flex items-center gap-2 mt-0.5">
            <span className="text-xs text-muted-foreground">{presidency.termLabel}</span>
            <Badge variant="outline" className={cn('text-xs px-1.5 py-0', getPartyColor(presidency.party))}>
              {presidency.party}
            </Badge>
          </div>
        </div>
      </div>
    </button>
  );
}
