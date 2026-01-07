'use client';

/**
 * PresidencyTable Component
 *
 * Displays a sortable table of all presidencies with expandable rows.
 */

import { useState, useMemo, Fragment } from 'react';
import { ChevronDown, ChevronUp, ChevronsUpDown, ChevronRight, Crown } from 'lucide-react';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table';
import { Button } from '@/components/ui/button';
import { Skeleton } from '@/components/ui/skeleton';
import { PresidencyExpandedRow } from './PresidencyExpandedRow';
import type { PresidencyDTO } from '@/hooks/usePresidencySync';
import { cn } from '@/lib/utils';

export interface PresidencyTableProps {
  presidencies: PresidencyDTO[];
  isLoading?: boolean;
}

type SortField = 'number' | 'name' | 'party' | 'startDate';
type SortDirection = 'asc' | 'desc';

/**
 * Get party color class for text
 */
function getPartyColor(party: string): string {
  const partyLower = party?.toLowerCase() || '';
  if (partyLower.includes('republican')) return 'text-red-600 dark:text-red-400';
  if (partyLower.includes('democrat')) return 'text-blue-600 dark:text-blue-400';
  if (partyLower.includes('whig')) return 'text-amber-600 dark:text-amber-400';
  if (partyLower.includes('federalist')) return 'text-purple-600 dark:text-purple-400';
  return 'text-gray-600 dark:text-gray-400';
}

export function PresidencyTable({ presidencies, isLoading }: PresidencyTableProps) {
  const [sortField, setSortField] = useState<SortField>('number');
  const [sortDirection, setSortDirection] = useState<SortDirection>('desc');
  const [expandedId, setExpandedId] = useState<string | null>(null);

  // Sort presidencies
  const sortedPresidencies = useMemo(() => {
    const sorted = [...presidencies].sort((a, b) => {
      let comparison = 0;

      switch (sortField) {
        case 'number':
          comparison = a.number - b.number;
          break;
        case 'name':
          comparison = a.presidentLastName.localeCompare(b.presidentLastName);
          break;
        case 'party':
          comparison = a.party.localeCompare(b.party);
          break;
        case 'startDate':
          comparison = new Date(a.startDate).getTime() - new Date(b.startDate).getTime();
          break;
      }

      return sortDirection === 'asc' ? comparison : -comparison;
    });

    return sorted;
  }, [presidencies, sortField, sortDirection]);

  const handleSort = (field: SortField) => {
    if (sortField === field) {
      setSortDirection(sortDirection === 'asc' ? 'desc' : 'asc');
    } else {
      setSortField(field);
      setSortDirection(field === 'number' ? 'desc' : 'asc');
    }
  };

  const toggleExpand = (id: string) => {
    setExpandedId(expandedId === id ? null : id);
  };

  const SortIcon = ({ field }: { field: SortField }) => {
    if (sortField !== field) {
      return <ChevronsUpDown className="ml-1 h-4 w-4 inline opacity-50" />;
    }
    return sortDirection === 'asc'
      ? <ChevronUp className="ml-1 h-4 w-4 inline" />
      : <ChevronDown className="ml-1 h-4 w-4 inline" />;
  };

  if (isLoading) {
    return (
      <div className="rounded-md border">
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead className="w-10"></TableHead>
              <TableHead className="w-16">#</TableHead>
              <TableHead>President</TableHead>
              <TableHead className="hidden sm:table-cell">Party</TableHead>
              <TableHead className="hidden md:table-cell">Term</TableHead>
              <TableHead className="hidden lg:table-cell">Vice President(s)</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {Array.from({ length: 10 }).map((_, i) => (
              <TableRow key={i}>
                <TableCell><Skeleton className="h-4 w-4" /></TableCell>
                <TableCell><Skeleton className="h-4 w-8" /></TableCell>
                <TableCell><Skeleton className="h-4 w-40" /></TableCell>
                <TableCell className="hidden sm:table-cell"><Skeleton className="h-4 w-24" /></TableCell>
                <TableCell className="hidden md:table-cell"><Skeleton className="h-4 w-28" /></TableCell>
                <TableCell className="hidden lg:table-cell"><Skeleton className="h-4 w-32" /></TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </div>
    );
  }

  if (presidencies.length === 0) {
    return (
      <div className="rounded-md border p-8 text-center">
        <Crown className="h-12 w-12 mx-auto text-muted-foreground mb-4" />
        <p className="font-medium mb-2">No presidencies found</p>
        <p className="text-sm text-muted-foreground">
          Presidential data has not been synced yet. Check the admin panel.
        </p>
      </div>
    );
  }

  return (
    <div className="rounded-md border">
      <Table>
        <TableHeader>
          <TableRow>
            <TableHead className="w-10"></TableHead>
            <TableHead className="w-16">
              <Button
                variant="ghost"
                size="sm"
                className="-ml-3 h-8 font-semibold"
                onClick={() => handleSort('number')}
              >
                #
                <SortIcon field="number" />
              </Button>
            </TableHead>
            <TableHead>
              <Button
                variant="ghost"
                size="sm"
                className="-ml-3 h-8 font-semibold"
                onClick={() => handleSort('name')}
              >
                President
                <SortIcon field="name" />
              </Button>
            </TableHead>
            <TableHead className="hidden sm:table-cell">
              <Button
                variant="ghost"
                size="sm"
                className="-ml-3 h-8 font-semibold"
                onClick={() => handleSort('party')}
              >
                Party
                <SortIcon field="party" />
              </Button>
            </TableHead>
            <TableHead className="hidden md:table-cell">
              <Button
                variant="ghost"
                size="sm"
                className="-ml-3 h-8 font-semibold"
                onClick={() => handleSort('startDate')}
              >
                Term
                <SortIcon field="startDate" />
              </Button>
            </TableHead>
            <TableHead className="hidden lg:table-cell">Vice President(s)</TableHead>
          </TableRow>
        </TableHeader>
        <TableBody>
          {sortedPresidencies.map((presidency) => (
            <Fragment key={presidency.id}>
              <TableRow
                className={cn(
                  'cursor-pointer hover:bg-muted/50',
                  expandedId === presidency.id && 'bg-muted/30'
                )}
                onClick={() => toggleExpand(presidency.id)}
              >
                <TableCell className="w-10">
                  <ChevronRight
                    className={cn(
                      'h-4 w-4 text-muted-foreground transition-transform duration-200',
                      expandedId === presidency.id && 'rotate-90'
                    )}
                  />
                </TableCell>
                <TableCell className="font-medium">
                  {presidency.ordinalLabel}
                </TableCell>
                <TableCell>
                  <div>
                    <span className="font-medium">{presidency.presidentFullName}</span>
                    {presidency.current && (
                      <span className="ml-2 text-xs text-green-600 dark:text-green-400 font-medium">
                        Current
                      </span>
                    )}
                  </div>
                  {/* Show party on mobile */}
                  <div className="sm:hidden text-sm text-muted-foreground">
                    <span className={getPartyColor(presidency.party)}>{presidency.party}</span>
                  </div>
                </TableCell>
                <TableCell className={cn('hidden sm:table-cell', getPartyColor(presidency.party))}>
                  {presidency.party}
                </TableCell>
                <TableCell className="hidden md:table-cell text-muted-foreground">
                  {presidency.termLabel}
                </TableCell>
                <TableCell className="hidden lg:table-cell text-muted-foreground">
                  {presidency.vicePresidents && presidency.vicePresidents.length > 0 ? (
                    <span>
                      {presidency.vicePresidents.map(vp => vp.fullName).join(', ')}
                    </span>
                  ) : (
                    <span className="text-muted-foreground/50">None</span>
                  )}
                </TableCell>
              </TableRow>
              {expandedId === presidency.id && (
                <TableRow>
                  <TableCell colSpan={6} className="p-0 border-t-0">
                    <PresidencyExpandedRow presidency={presidency} />
                  </TableCell>
                </TableRow>
              )}
            </Fragment>
          ))}
        </TableBody>
      </Table>
    </div>
  );
}
