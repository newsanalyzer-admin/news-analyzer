'use client';

/**
 * Enrichment Status Component
 *
 * Displays detailed enrichment status including progress bar visualization.
 */

import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Skeleton } from '@/components/ui/skeleton';
import { Progress } from '@/components/ui/progress';
import { Button } from '@/components/ui/button';
import type { EnrichmentStatus as EnrichmentStatusType } from '@/types/member';

interface EnrichmentStatusProps {
  status?: EnrichmentStatusType;
  isLoading: boolean;
  error: Error | null;
}

function formatRelativeTime(isoString?: string): string {
  if (!isoString) return 'Never';

  const date = new Date(isoString);
  const now = new Date();
  const diffMs = now.getTime() - date.getTime();
  const diffMins = Math.floor(diffMs / 60000);
  const diffHours = Math.floor(diffMins / 60);
  const diffDays = Math.floor(diffHours / 24);

  if (diffMins < 1) return 'Just now';
  if (diffMins < 60) return `${diffMins} minute${diffMins === 1 ? '' : 's'} ago`;
  if (diffHours < 24) return `${diffHours} hour${diffHours === 1 ? '' : 's'} ago`;
  if (diffDays < 7) return `${diffDays} day${diffDays === 1 ? '' : 's'} ago`;

  return date.toLocaleDateString();
}

export function EnrichmentStatus({ status, isLoading, error }: EnrichmentStatusProps) {
  const percentage = status?.totalMembers
    ? Math.round((status.enrichedMembers / status.totalMembers) * 100)
    : 0;

  if (isLoading) {
    return (
      <Card>
        <CardHeader>
          <Skeleton className="h-6 w-40 mb-2" />
          <Skeleton className="h-4 w-64" />
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="grid gap-4 sm:grid-cols-2">
            {[1, 2, 3, 4].map((i) => (
              <div key={i} className="space-y-2">
                <Skeleton className="h-4 w-24" />
                <Skeleton className="h-6 w-32" />
              </div>
            ))}
          </div>
          <Skeleton className="h-4 w-full" />
        </CardContent>
      </Card>
    );
  }

  if (error) {
    return (
      <Card className="border-destructive">
        <CardHeader>
          <CardTitle>Enrichment Status</CardTitle>
          <CardDescription className="text-destructive">
            Failed to load enrichment status
          </CardDescription>
        </CardHeader>
        <CardContent>
          <p className="text-sm text-muted-foreground mb-4">
            {error.message || 'Unable to fetch enrichment status from the server.'}
          </p>
          <Button variant="outline" onClick={() => window.location.reload()}>
            Retry
          </Button>
        </CardContent>
      </Card>
    );
  }

  return (
    <Card>
      <CardHeader>
        <CardTitle>Enrichment Status</CardTitle>
        <CardDescription>
          Member enrichment from unitedstates/congress-legislators repository
        </CardDescription>
      </CardHeader>
      <CardContent className="space-y-6">
        <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
          <div>
            <p className="text-sm font-medium text-muted-foreground">Total Members</p>
            <p className="text-2xl font-bold">
              {status?.totalMembers?.toLocaleString() ?? '-'}
            </p>
          </div>
          <div>
            <p className="text-sm font-medium text-muted-foreground">Enriched</p>
            <p className="text-2xl font-bold text-green-600">
              {status?.enrichedMembers?.toLocaleString() ?? '-'}
              {status?.totalMembers && (
                <span className="text-sm font-normal text-muted-foreground ml-2">
                  ({percentage}%)
                </span>
              )}
            </p>
          </div>
          <div>
            <p className="text-sm font-medium text-muted-foreground">Pending</p>
            <p className="text-2xl font-bold text-amber-600">
              {status?.pendingMembers?.toLocaleString() ?? '-'}
            </p>
          </div>
          <div>
            <p className="text-sm font-medium text-muted-foreground">Last Sync</p>
            <p className="text-2xl font-bold">
              {formatRelativeTime(status?.lastSyncTime)}
            </p>
          </div>
        </div>

        {/* Progress Bar */}
        <div className="space-y-2">
          <div className="flex justify-between text-sm">
            <span className="text-muted-foreground">Enrichment Progress</span>
            <span className="font-medium">{percentage}%</span>
          </div>
          <Progress value={percentage} className="h-3" />
        </div>
      </CardContent>
    </Card>
  );
}
