'use client';

/**
 * JudgeStats Component
 *
 * Displays statistics about federal judges.
 */

import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Skeleton } from '@/components/ui/skeleton';
import { useJudgeStats } from '@/hooks/useJudges';
import { Scale, Users, Building2, Gavel } from 'lucide-react';

export function JudgeStats() {
  const { data: stats, isLoading, error } = useJudgeStats();

  if (isLoading) {
    return <JudgeStatsSkeleton />;
  }

  if (error || !stats) {
    return null; // Silently fail for stats - not critical
  }

  const statCards = [
    {
      title: 'Total Judges',
      value: stats.totalJudges || 0,
      icon: Users,
      description: 'All federal judges in database',
    },
    {
      title: 'Active Judges',
      value: stats.activeJudges || 0,
      icon: Gavel,
      description: 'Currently serving',
    },
    {
      title: 'Senior Judges',
      value: stats.seniorJudges || 0,
      icon: Scale,
      description: 'Senior status',
    },
    {
      title: 'Courts',
      value: Object.keys(stats.byCourtLevel || {}).length || 3,
      icon: Building2,
      description: 'Court levels covered',
    },
  ];

  return (
    <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4 mb-6">
      {statCards.map((stat) => (
        <Card key={stat.title}>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">{stat.title}</CardTitle>
            <stat.icon className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{stat.value.toLocaleString()}</div>
            <p className="text-xs text-muted-foreground">{stat.description}</p>
          </CardContent>
        </Card>
      ))}
    </div>
  );
}

function JudgeStatsSkeleton() {
  return (
    <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4 mb-6">
      {Array.from({ length: 4 }).map((_, i) => (
        <Card key={i}>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <Skeleton className="h-4 w-24" />
            <Skeleton className="h-4 w-4" />
          </CardHeader>
          <CardContent>
            <Skeleton className="h-8 w-16 mb-1" />
            <Skeleton className="h-3 w-32" />
          </CardContent>
        </Card>
      ))}
    </div>
  );
}
