'use client';

/**
 * Sync Status Card Component
 *
 * Displays the current sync status for a data type (Members, Committees, etc.)
 */

import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Skeleton } from '@/components/ui/skeleton';

interface SyncStatusCardProps {
  title: string;
  count?: number;
  total?: number;
  isLoading: boolean;
  error: Error | null;
  showPercentage?: boolean;
}

export function SyncStatusCard({
  title,
  count,
  total,
  isLoading,
  error,
  showPercentage = false,
}: SyncStatusCardProps) {
  const percentage = total && count !== undefined ? Math.round((count / total) * 100) : null;

  const getStatusColor = () => {
    if (error) return 'bg-red-100 text-red-800 border-red-200';
    if (count === undefined) return 'bg-gray-100 text-gray-600 border-gray-200';
    if (showPercentage && percentage !== null && percentage < 100) {
      return 'bg-yellow-100 text-yellow-800 border-yellow-200';
    }
    return 'bg-green-100 text-green-800 border-green-200';
  };

  const getStatusText = () => {
    if (error) return 'Error';
    if (count === undefined) return 'Unknown';
    return 'Available';
  };

  return (
    <Card>
      <CardHeader className="pb-2">
        <div className="flex items-center justify-between">
          <CardTitle className="text-sm font-medium text-muted-foreground">
            {title}
          </CardTitle>
          {!isLoading && (
            <Badge variant="outline" className={getStatusColor()}>
              {getStatusText()}
            </Badge>
          )}
        </div>
      </CardHeader>
      <CardContent>
        {isLoading ? (
          <Skeleton className="h-10 w-24" />
        ) : error ? (
          <p className="text-sm text-destructive">Failed to load</p>
        ) : (
          <div className="space-y-1">
            <p className="text-3xl font-bold">
              {count?.toLocaleString() ?? '-'}
            </p>
            {showPercentage && total !== undefined && percentage !== null && (
              <p className="text-sm text-muted-foreground">
                {percentage}% of {total.toLocaleString()} total
              </p>
            )}
          </div>
        )}
      </CardContent>
    </Card>
  );
}
