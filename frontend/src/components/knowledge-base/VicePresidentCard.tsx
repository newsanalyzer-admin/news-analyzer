'use client';

/**
 * VicePresidentCard Component (KB-2.2)
 *
 * Displays the Vice President alongside the President card.
 * Accepts OfficeholderDTO from the administration endpoint
 * which includes imageUrl and positionTitle.
 */

import { Users, Calendar } from 'lucide-react';
import { Card, CardContent } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Skeleton } from '@/components/ui/skeleton';
import type { OfficeholderDTO } from '@/hooks/usePresidencySync';

export interface VicePresidentCardProps {
  vicePresident: OfficeholderDTO | null | undefined;
  isLoading?: boolean;
}

/**
 * Format a date string to a readable format
 */
function formatDate(dateStr: string): string {
  const date = new Date(dateStr);
  return date.toLocaleDateString('en-US', { month: 'long', day: 'numeric', year: 'numeric' });
}

export function VicePresidentCard({ vicePresident, isLoading }: VicePresidentCardProps) {
  if (isLoading) {
    return (
      <Card className="overflow-hidden">
        <CardContent className="p-0">
          <div className="flex flex-col md:flex-row">
            <div className="w-full md:w-48 h-48 md:h-auto">
              <Skeleton className="w-full h-full min-h-[200px]" />
            </div>
            <div className="flex-1 p-6 space-y-4">
              <Skeleton className="h-6 w-24" />
              <Skeleton className="h-8 w-48" />
              <Skeleton className="h-5 w-32" />
              <Skeleton className="h-4 w-40" />
            </div>
          </div>
        </CardContent>
      </Card>
    );
  }

  if (!vicePresident) {
    return (
      <Card className="overflow-hidden">
        <CardContent className="p-6 text-center">
          <Users className="h-12 w-12 mx-auto text-muted-foreground mb-4" />
          <p className="text-muted-foreground">
            No Vice President data available.
          </p>
        </CardContent>
      </Card>
    );
  }

  return (
    <Card className="overflow-hidden">
      <CardContent className="p-0">
        <div className="flex flex-col md:flex-row">
          {/* Portrait */}
          <div className="w-full md:w-48 lg:w-56 bg-muted flex items-center justify-center">
            {vicePresident.imageUrl ? (
              <img
                src={vicePresident.imageUrl}
                alt={`Portrait of ${vicePresident.fullName}`}
                className="w-full h-full object-cover min-h-[200px]"
              />
            ) : (
              <div className="w-full h-full min-h-[200px] flex items-center justify-center bg-gradient-to-br from-indigo-500/10 to-indigo-600/20">
                <Users className="h-16 w-16 text-indigo-500/50" />
              </div>
            )}
          </div>

          {/* Content */}
          <div className="flex-1 p-6">
            {/* Badge */}
            <div className="flex items-center gap-2 mb-3">
              <Badge variant="secondary" className="bg-indigo-500/10 text-indigo-600 dark:text-indigo-400 border-0">
                Vice President
              </Badge>
            </div>

            {/* Name */}
            <h2 className="text-2xl md:text-3xl font-bold mb-1">
              {vicePresident.fullName}
            </h2>
            <p className="text-lg text-muted-foreground mb-4">
              {vicePresident.positionTitle}
            </p>

            {/* Details */}
            <div className="space-y-2 text-sm">
              <div className="flex items-center gap-2 text-muted-foreground">
                <Calendar className="h-4 w-4" />
                <span>Since {formatDate(vicePresident.startDate)}</span>
              </div>
              {vicePresident.termLabel && (
                <p className="text-muted-foreground">
                  Term: {vicePresident.termLabel}
                </p>
              )}
            </div>
          </div>
        </div>
      </CardContent>
    </Card>
  );
}
