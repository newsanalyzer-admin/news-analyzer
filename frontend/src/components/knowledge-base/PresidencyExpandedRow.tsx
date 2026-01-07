'use client';

/**
 * PresidencyExpandedRow Component
 *
 * Shows expanded details for a presidency including VP, Chiefs of Staff,
 * Cabinet summary, and Executive Order count.
 * Lazily fetches administration data.
 */

import { Users, Briefcase, FileText, Calendar, AlertCircle } from 'lucide-react';
import { Skeleton } from '@/components/ui/skeleton';
import { Badge } from '@/components/ui/badge';
import { usePresidencyAdministration } from '@/hooks/usePresidencySync';
import type { PresidencyDTO } from '@/hooks/usePresidencySync';

export interface PresidencyExpandedRowProps {
  presidency: PresidencyDTO;
}

/**
 * Format date for display
 */
function formatDate(dateStr: string | null): string {
  if (!dateStr) return 'Present';
  const date = new Date(dateStr);
  return date.toLocaleDateString('en-US', { month: 'short', year: 'numeric' });
}

export function PresidencyExpandedRow({ presidency }: PresidencyExpandedRowProps) {
  const { data: administration, isLoading, error } = usePresidencyAdministration(presidency.id);

  return (
    <div className="bg-muted/20 p-4 md:p-6 border-t">
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
        {/* Vice Presidents */}
        <div>
          <div className="flex items-center gap-2 mb-3">
            <Users className="h-4 w-4 text-muted-foreground" />
            <h4 className="font-semibold text-sm">Vice Presidents</h4>
          </div>
          {isLoading ? (
            <div className="space-y-2">
              <Skeleton className="h-4 w-32" />
              <Skeleton className="h-3 w-24" />
            </div>
          ) : error ? (
            <p className="text-sm text-muted-foreground">Failed to load</p>
          ) : administration?.vicePresidents && administration.vicePresidents.length > 0 ? (
            <ul className="space-y-2">
              {administration.vicePresidents.map((vp) => (
                <li key={vp.holdingId} className="text-sm">
                  <span className="font-medium">{vp.fullName}</span>
                  <span className="block text-muted-foreground text-xs">
                    {vp.termLabel}
                  </span>
                </li>
              ))}
            </ul>
          ) : presidency.vicePresidents && presidency.vicePresidents.length > 0 ? (
            // Fallback to presidency VP data if administration not loaded
            <ul className="space-y-2">
              {presidency.vicePresidents.map((vp) => (
                <li key={vp.personId} className="text-sm">
                  <span className="font-medium">{vp.fullName}</span>
                  <span className="block text-muted-foreground text-xs">
                    {vp.termLabel}
                  </span>
                </li>
              ))}
            </ul>
          ) : (
            <p className="text-sm text-muted-foreground">None recorded</p>
          )}
        </div>

        {/* Chiefs of Staff */}
        <div>
          <div className="flex items-center gap-2 mb-3">
            <Briefcase className="h-4 w-4 text-muted-foreground" />
            <h4 className="font-semibold text-sm">Chiefs of Staff</h4>
          </div>
          {isLoading ? (
            <div className="space-y-2">
              <Skeleton className="h-4 w-32" />
              <Skeleton className="h-3 w-24" />
            </div>
          ) : error ? (
            <p className="text-sm text-muted-foreground">Failed to load</p>
          ) : administration?.chiefsOfStaff && administration.chiefsOfStaff.length > 0 ? (
            <ul className="space-y-2">
              {administration.chiefsOfStaff.map((cos) => (
                <li key={cos.holdingId} className="text-sm">
                  <span className="font-medium">{cos.fullName}</span>
                  <span className="block text-muted-foreground text-xs">
                    {cos.termLabel}
                  </span>
                </li>
              ))}
            </ul>
          ) : (
            <p className="text-sm text-muted-foreground">
              {presidency.number < 36 ? 'Position created in 1946' : 'None recorded'}
            </p>
          )}
        </div>

        {/* Term Details */}
        <div>
          <div className="flex items-center gap-2 mb-3">
            <Calendar className="h-4 w-4 text-muted-foreground" />
            <h4 className="font-semibold text-sm">Term Details</h4>
          </div>
          <div className="space-y-2 text-sm">
            <div>
              <span className="text-muted-foreground">Started:</span>{' '}
              <span className="font-medium">{formatDate(presidency.startDate)}</span>
            </div>
            <div>
              <span className="text-muted-foreground">Ended:</span>{' '}
              <span className="font-medium">{formatDate(presidency.endDate)}</span>
            </div>
            {presidency.termDays && (
              <div>
                <span className="text-muted-foreground">Duration:</span>{' '}
                <span className="font-medium">
                  {Math.floor(presidency.termDays / 365)} years, {presidency.termDays % 365} days
                </span>
              </div>
            )}
            {presidency.endReason && presidency.endReason !== 'TERM_END' && (
              <div>
                <Badge variant="outline" className="text-xs">
                  {presidency.endReason.replace('_', ' ')}
                </Badge>
              </div>
            )}
            {presidency.electionYear && (
              <div>
                <span className="text-muted-foreground">Elected:</span>{' '}
                <span className="font-medium">{presidency.electionYear}</span>
              </div>
            )}
          </div>
        </div>

        {/* Executive Orders */}
        <div>
          <div className="flex items-center gap-2 mb-3">
            <FileText className="h-4 w-4 text-muted-foreground" />
            <h4 className="font-semibold text-sm">Executive Orders</h4>
          </div>
          {presidency.executiveOrderCount !== null && presidency.executiveOrderCount > 0 ? (
            <div>
              <span className="text-2xl font-bold">{presidency.executiveOrderCount.toLocaleString()}</span>
              <span className="text-sm text-muted-foreground ml-2">orders issued</span>
            </div>
          ) : (
            <p className="text-sm text-muted-foreground">
              Data not yet synced
            </p>
          )}
        </div>
      </div>

      {/* Biographical Info */}
      <div className="mt-6 pt-4 border-t border-muted">
        <div className="flex flex-wrap gap-4 text-sm text-muted-foreground">
          {presidency.birthPlace && (
            <span>Born: {presidency.birthPlace}</span>
          )}
          {presidency.birthDate && (
            <span>Birth Date: {formatDate(presidency.birthDate)}</span>
          )}
          {presidency.living ? (
            <Badge variant="outline" className="text-green-600 border-green-300 dark:border-green-800">
              Living
            </Badge>
          ) : presidency.deathDate && (
            <span>Died: {formatDate(presidency.deathDate)}</span>
          )}
        </div>
      </div>
    </div>
  );
}
