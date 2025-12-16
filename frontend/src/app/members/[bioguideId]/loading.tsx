/**
 * Member Detail Loading State
 *
 * Skeleton UI displayed while member detail page is loading.
 */

import { Skeleton } from '@/components/ui/skeleton';

export default function MemberDetailLoading() {
  return (
    <main className="container mx-auto py-8 px-4">
      <div className="space-y-6">
        {/* Back link skeleton */}
        <Skeleton className="h-5 w-32" />

        {/* Profile header skeleton */}
        <div className="flex flex-col sm:flex-row gap-6">
          <Skeleton className="h-32 w-32 rounded-full mx-auto sm:mx-0" />
          <div className="flex-1 space-y-3 text-center sm:text-left">
            <Skeleton className="h-8 w-64 mx-auto sm:mx-0" />
            <Skeleton className="h-5 w-48 mx-auto sm:mx-0" />
            <div className="flex gap-2 justify-center sm:justify-start">
              <Skeleton className="h-6 w-20" />
              <Skeleton className="h-6 w-20" />
              <Skeleton className="h-6 w-24" />
            </div>
          </div>
        </div>

        {/* Tabs skeleton */}
        <Skeleton className="h-10 w-full max-w-md" />

        {/* Content skeleton */}
        <div className="grid gap-6 md:grid-cols-2">
          <div className="space-y-4">
            <Skeleton className="h-6 w-32" />
            <div className="space-y-3">
              <Skeleton className="h-10 w-full" />
              <Skeleton className="h-10 w-full" />
              <Skeleton className="h-10 w-full" />
            </div>
          </div>
          <div className="space-y-4">
            <Skeleton className="h-6 w-32" />
            <div className="space-y-2">
              <Skeleton className="h-6 w-full" />
              <Skeleton className="h-6 w-full" />
              <Skeleton className="h-6 w-full" />
              <Skeleton className="h-6 w-full" />
            </div>
          </div>
        </div>
      </div>
    </main>
  );
}
