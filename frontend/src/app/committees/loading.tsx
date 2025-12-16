/**
 * Committees Loading State
 *
 * Skeleton UI displayed while committees page is loading.
 */

import { Skeleton } from '@/components/ui/skeleton';

export default function CommitteesLoading() {
  return (
    <main className="container mx-auto py-8 px-4">
      <div className="mb-8">
        <Skeleton className="h-10 w-72 mb-2" />
        <Skeleton className="h-5 w-96" />
      </div>

      {/* Stats skeleton */}
      <div className="flex flex-wrap gap-4 mb-6">
        <Skeleton className="h-8 w-36" />
        <Skeleton className="h-8 w-24" />
        <Skeleton className="h-8 w-24" />
        <Skeleton className="h-8 w-24" />
      </div>

      {/* Filters skeleton */}
      <div className="flex flex-wrap gap-4 mb-6">
        <Skeleton className="h-10 w-40" />
        <Skeleton className="h-10 w-48" />
        <Skeleton className="h-10 w-64" />
      </div>

      {/* Hierarchy skeleton - Chamber sections */}
      {['Senate', 'House', 'Joint'].map((chamber) => (
        <div key={chamber} className="mb-4">
          <Skeleton className="h-12 w-full rounded-lg mb-2" />
          <div className="pl-4 space-y-2">
            {Array.from({ length: 3 }).map((_, i) => (
              <Skeleton key={i} className="h-20 w-full rounded-lg" />
            ))}
          </div>
        </div>
      ))}
    </main>
  );
}
