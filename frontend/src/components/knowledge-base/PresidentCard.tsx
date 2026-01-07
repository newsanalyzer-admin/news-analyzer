'use client';

/**
 * PresidentCard Component
 *
 * Displays the current president prominently with name, party,
 * term start date, and portrait.
 */

import { Crown, Calendar, Users } from 'lucide-react';
import { Card, CardContent } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Skeleton } from '@/components/ui/skeleton';
import type { PresidencyDTO } from '@/hooks/usePresidencySync';

export interface PresidentCardProps {
  presidency: PresidencyDTO | null | undefined;
  isLoading?: boolean;
}

/**
 * Get party color classes for styling
 */
function getPartyStyles(party: string): { bg: string; text: string; border: string } {
  const partyLower = party?.toLowerCase() || '';
  if (partyLower.includes('republican')) {
    return { bg: 'bg-red-100 dark:bg-red-950', text: 'text-red-700 dark:text-red-300', border: 'border-red-200 dark:border-red-800' };
  }
  if (partyLower.includes('democrat')) {
    return { bg: 'bg-blue-100 dark:bg-blue-950', text: 'text-blue-700 dark:text-blue-300', border: 'border-blue-200 dark:border-blue-800' };
  }
  if (partyLower.includes('whig')) {
    return { bg: 'bg-amber-100 dark:bg-amber-950', text: 'text-amber-700 dark:text-amber-300', border: 'border-amber-200 dark:border-amber-800' };
  }
  if (partyLower.includes('federalist')) {
    return { bg: 'bg-purple-100 dark:bg-purple-950', text: 'text-purple-700 dark:text-purple-300', border: 'border-purple-200 dark:border-purple-800' };
  }
  return { bg: 'bg-gray-100 dark:bg-gray-800', text: 'text-gray-700 dark:text-gray-300', border: 'border-gray-200 dark:border-gray-700' };
}

/**
 * Format term start year
 */
function formatTermStart(startDate: string): string {
  const date = new Date(startDate);
  return date.toLocaleDateString('en-US', { month: 'long', day: 'numeric', year: 'numeric' });
}

export function PresidentCard({ presidency, isLoading }: PresidentCardProps) {
  if (isLoading) {
    return (
      <Card className="overflow-hidden">
        <CardContent className="p-0">
          <div className="flex flex-col md:flex-row">
            {/* Portrait skeleton */}
            <div className="w-full md:w-48 h-48 md:h-auto">
              <Skeleton className="w-full h-full min-h-[200px]" />
            </div>
            {/* Content skeleton */}
            <div className="flex-1 p-6 space-y-4">
              <Skeleton className="h-6 w-24" />
              <Skeleton className="h-8 w-64" />
              <Skeleton className="h-5 w-32" />
              <div className="space-y-2">
                <Skeleton className="h-4 w-40" />
                <Skeleton className="h-4 w-36" />
              </div>
            </div>
          </div>
        </CardContent>
      </Card>
    );
  }

  if (!presidency) {
    return (
      <Card className="overflow-hidden">
        <CardContent className="p-6 text-center">
          <Crown className="h-12 w-12 mx-auto text-muted-foreground mb-4" />
          <p className="text-muted-foreground">
            No presidential data available. Sync data from the admin panel.
          </p>
        </CardContent>
      </Card>
    );
  }

  const partyStyles = getPartyStyles(presidency.party);

  return (
    <Card className="overflow-hidden">
      <CardContent className="p-0">
        <div className="flex flex-col md:flex-row">
          {/* Portrait */}
          <div className="w-full md:w-48 lg:w-56 bg-muted flex items-center justify-center">
            {presidency.imageUrl ? (
              <img
                src={presidency.imageUrl}
                alt={`Portrait of ${presidency.presidentFullName}`}
                className="w-full h-full object-cover min-h-[200px]"
              />
            ) : (
              <div className="w-full h-full min-h-[200px] flex items-center justify-center bg-gradient-to-br from-blue-500/10 to-blue-600/20">
                <Crown className="h-16 w-16 text-blue-500/50" />
              </div>
            )}
          </div>

          {/* Content */}
          <div className="flex-1 p-6">
            {/* Badge row */}
            <div className="flex items-center gap-2 mb-3">
              <Badge variant="secondary" className="bg-blue-500/10 text-blue-600 dark:text-blue-400 border-0">
                Current President
              </Badge>
              <Badge variant="outline" className={`${partyStyles.bg} ${partyStyles.text} ${partyStyles.border}`}>
                {presidency.party}
              </Badge>
            </div>

            {/* Name and ordinal */}
            <h2 className="text-2xl md:text-3xl font-bold mb-1">
              {presidency.presidentFullName}
            </h2>
            <p className="text-lg text-muted-foreground mb-4">
              {presidency.ordinalLabel} President of the United States
            </p>

            {/* Details */}
            <div className="space-y-2 text-sm">
              <div className="flex items-center gap-2 text-muted-foreground">
                <Calendar className="h-4 w-4" />
                <span>Inaugurated {formatTermStart(presidency.startDate)}</span>
              </div>
              {presidency.vicePresidents && presidency.vicePresidents.length > 0 && (
                <div className="flex items-center gap-2 text-muted-foreground">
                  <Users className="h-4 w-4" />
                  <span>
                    Vice President: {presidency.vicePresidents.map(vp => vp.fullName).join(', ')}
                  </span>
                </div>
              )}
              {presidency.birthPlace && (
                <p className="text-muted-foreground">
                  Born in {presidency.birthPlace}
                </p>
              )}
            </div>
          </div>
        </div>
      </CardContent>
    </Card>
  );
}
