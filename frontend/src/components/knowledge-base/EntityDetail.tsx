'use client';

import { Skeleton } from '@/components/ui/skeleton';
import { Button } from '@/components/ui/button';
import { cn } from '@/lib/utils';
import { EntityDetailHeader } from './EntityDetailHeader';
import { DetailSection } from './DetailSection';
import { RelatedEntities } from './RelatedEntities';
import type { EntityTypeConfig, EntityDetailConfig } from '@/lib/config/entityTypes';

/**
 * Props for the EntityDetail component
 */
export interface EntityDetailProps<T> {
  /** Entity type configuration */
  entityConfig: EntityTypeConfig<T>;
  /** Detail view configuration */
  detailConfig: EntityDetailConfig<T>;
  /** Entity data */
  data: T | null;
  /** Loading state */
  isLoading: boolean;
  /** Error message */
  error?: string | null;
  /** Back URL (optional, defaults to router.back()) */
  backUrl?: string;
  /** Retry callback for error state */
  onRetry?: () => void;
  /** Optional className */
  className?: string;
}

/**
 * EntityDetail - A reusable component for displaying entity details.
 * Renders header, sections, and related entities based on configuration.
 */
export function EntityDetail<T extends Record<string, unknown>>({
  entityConfig,
  detailConfig,
  data,
  isLoading,
  error,
  backUrl,
  onRetry,
  className,
}: EntityDetailProps<T>) {
  // Loading state
  if (isLoading) {
    return <EntityDetailSkeleton />;
  }

  // Error state
  if (error) {
    return (
      <div className={cn('p-6', className)}>
        <div className="flex flex-col items-center justify-center py-12 text-center border rounded-lg">
          <div className="text-4xl mb-4">&#x26a0;&#xfe0f;</div>
          <h3 className="text-lg font-semibold mb-2">
            Failed to load {entityConfig.label.toLowerCase().slice(0, -1)}
          </h3>
          <p className="text-muted-foreground mb-4">{error}</p>
          {onRetry && <Button onClick={onRetry}>Try Again</Button>}
        </div>
      </div>
    );
  }

  // Not found state
  if (!data) {
    return (
      <div className={cn('p-6', className)}>
        <div className="flex flex-col items-center justify-center py-12 text-center border rounded-lg">
          <div className="text-4xl mb-4">&#x1f50d;</div>
          <h3 className="text-lg font-semibold mb-2">
            {entityConfig.label.slice(0, -1)} not found
          </h3>
          <p className="text-muted-foreground">
            The requested {entityConfig.label.toLowerCase().slice(0, -1)} could not be found.
          </p>
        </div>
      </div>
    );
  }

  return (
    <div className={cn('p-6 space-y-8', className)}>
      {/* Header */}
      <EntityDetailHeader
        config={detailConfig.header}
        data={data}
        entityTypeLabel={entityConfig.label}
        backUrl={backUrl}
      />

      {/* Divider */}
      <hr className="border-border" />

      {/* Main Content */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
        {/* Primary Content - Sections */}
        <div className="lg:col-span-2 space-y-8">
          {detailConfig.sections.map((section) => (
            <DetailSection key={section.id} config={section} data={data} />
          ))}
        </div>

        {/* Sidebar - Related Entities */}
        {detailConfig.relatedEntities && detailConfig.relatedEntities.length > 0 && (
          <aside className="space-y-6">
            {detailConfig.relatedEntities.map((relatedConfig) => (
              <RelatedEntities
                key={relatedConfig.field}
                config={relatedConfig}
                data={data}
              />
            ))}
          </aside>
        )}
      </div>
    </div>
  );
}

/**
 * Loading skeleton for EntityDetail
 */
function EntityDetailSkeleton() {
  return (
    <div className="p-6 space-y-8">
      {/* Header skeleton */}
      <div className="space-y-4">
        <Skeleton className="h-8 w-32" />
        <Skeleton className="h-10 w-3/4" />
        <Skeleton className="h-6 w-1/4" />
        <div className="flex gap-2">
          <Skeleton className="h-6 w-20" />
          <Skeleton className="h-6 w-24" />
        </div>
      </div>

      <hr className="border-border" />

      {/* Content skeleton */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
        <div className="lg:col-span-2 space-y-8">
          {/* Section 1 */}
          <div className="space-y-4">
            <Skeleton className="h-6 w-32" />
            <div className="space-y-3">
              <Skeleton className="h-5 w-full" />
              <Skeleton className="h-5 w-3/4" />
              <Skeleton className="h-5 w-1/2" />
            </div>
          </div>

          {/* Section 2 */}
          <div className="space-y-4">
            <Skeleton className="h-6 w-40" />
            <div className="space-y-3">
              <Skeleton className="h-5 w-full" />
              <Skeleton className="h-5 w-2/3" />
            </div>
          </div>
        </div>

        {/* Sidebar skeleton */}
        <aside className="space-y-6">
          <div className="space-y-3">
            <Skeleton className="h-6 w-32" />
            <div className="flex flex-wrap gap-2">
              <Skeleton className="h-6 w-24" />
              <Skeleton className="h-6 w-20" />
              <Skeleton className="h-6 w-28" />
            </div>
          </div>
        </aside>
      </div>
    </div>
  );
}
