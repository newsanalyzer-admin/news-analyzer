'use client';

/**
 * Government Organization Sync Status Card
 *
 * Displays the current sync status for government organizations including:
 * - Total organization count
 * - Breakdown by branch (Executive, Legislative, Judicial)
 * - Federal Register API availability indicator
 */

import { RefreshCw } from 'lucide-react';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { Skeleton } from '@/components/ui/skeleton';
import { useGovernmentOrgSyncStatus } from '@/hooks/useGovernmentOrgs';

export function GovOrgSyncStatusCard() {
  const { data, isLoading, error, refetch } = useGovernmentOrgSyncStatus();

  const getApiStatusColor = () => {
    if (error) return 'bg-red-100 text-red-800 border-red-200';
    if (!data) return 'bg-gray-100 text-gray-600 border-gray-200';
    return data.federalRegisterAvailable
      ? 'bg-green-100 text-green-800 border-green-200'
      : 'bg-red-100 text-red-800 border-red-200';
  };

  const getApiStatusText = () => {
    if (error) return 'Error';
    if (!data) return 'Unknown';
    return data.federalRegisterAvailable ? 'API Available' : 'API Unavailable';
  };

  if (isLoading) {
    return (
      <Card>
        <CardHeader className="pb-2">
          <div className="flex items-center justify-between">
            <CardTitle className="text-sm font-medium text-muted-foreground">
              Government Organizations
            </CardTitle>
            <Skeleton className="h-5 w-24" />
          </div>
        </CardHeader>
        <CardContent>
          <Skeleton className="h-10 w-24 mb-3" />
          <div className="space-y-2">
            <Skeleton className="h-4 w-32" />
            <Skeleton className="h-4 w-28" />
            <Skeleton className="h-4 w-24" />
          </div>
        </CardContent>
      </Card>
    );
  }

  if (error) {
    return (
      <Card>
        <CardHeader className="pb-2">
          <div className="flex items-center justify-between">
            <CardTitle className="text-sm font-medium text-muted-foreground">
              Government Organizations
            </CardTitle>
            <Badge variant="outline" className={getApiStatusColor()}>
              {getApiStatusText()}
            </Badge>
          </div>
        </CardHeader>
        <CardContent>
          <p className="text-sm text-destructive mb-3">Failed to load status</p>
          <Button
            variant="outline"
            size="sm"
            onClick={() => refetch()}
            className="gap-2"
          >
            <RefreshCw className="h-4 w-4" />
            Retry
          </Button>
        </CardContent>
      </Card>
    );
  }

  return (
    <Card>
      <CardHeader className="pb-2">
        <div className="flex items-center justify-between">
          <CardTitle className="text-sm font-medium text-muted-foreground">
            Government Organizations
          </CardTitle>
          <Badge variant="outline" className={getApiStatusColor()}>
            {getApiStatusText()}
          </Badge>
        </div>
      </CardHeader>
      <CardContent>
        <p className="text-3xl font-bold mb-3">
          {data?.totalOrganizations.toLocaleString() ?? '-'}
        </p>
        {data?.countByBranch && (
          <div className="space-y-1 text-sm text-muted-foreground">
            <div className="flex justify-between">
              <span>Executive</span>
              <span className="font-medium text-foreground">
                {data.countByBranch.executive?.toLocaleString() ?? 0}
              </span>
            </div>
            <div className="flex justify-between">
              <span>Legislative</span>
              <span className="font-medium text-foreground">
                {data.countByBranch.legislative?.toLocaleString() ?? 0}
              </span>
            </div>
            <div className="flex justify-between">
              <span>Judicial</span>
              <span className="font-medium text-foreground">
                {data.countByBranch.judicial?.toLocaleString() ?? 0}
              </span>
            </div>
          </div>
        )}
        {data?.lastSync && (
          <p className="text-xs text-muted-foreground mt-3">
            Last sync: {new Date(data.lastSync).toLocaleString()}
          </p>
        )}
      </CardContent>
    </Card>
  );
}
