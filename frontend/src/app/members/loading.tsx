/**
 * Members Loading State
 *
 * Skeleton UI displayed while members page is loading.
 */

import { Skeleton } from '@/components/ui/skeleton';

export default function MembersLoading() {
  return (
    <main className="container mx-auto py-8 px-4">
      <div className="mb-8">
        <Skeleton className="h-10 w-64 mb-2" />
        <Skeleton className="h-5 w-96" />
      </div>

      {/* Stats skeleton */}
      <div className="flex gap-4 mb-6">
        <Skeleton className="h-8 w-32" />
        <Skeleton className="h-8 w-24" />
        <Skeleton className="h-8 w-24" />
        <Skeleton className="h-8 w-24" />
      </div>

      {/* Filters skeleton */}
      <div className="flex flex-wrap gap-4 mb-6">
        <Skeleton className="h-10 w-48" />
        <Skeleton className="h-10 w-40" />
        <Skeleton className="h-10 w-48" />
        <Skeleton className="h-10 w-64" />
      </div>

      {/* Table skeleton */}
      <div className="border rounded-lg">
        <div className="p-4 border-b">
          <div className="flex gap-4">
            <Skeleton className="h-6 w-12" />
            <Skeleton className="h-6 w-32" />
            <Skeleton className="h-6 w-24" />
            <Skeleton className="h-6 w-16" />
            <Skeleton className="h-6 w-20" />
          </div>
        </div>
        {Array.from({ length: 10 }).map((_, i) => (
          <div key={i} className="p-4 border-b flex items-center gap-4">
            <Skeleton className="h-10 w-10 rounded-full" />
            <Skeleton className="h-5 w-40" />
            <Skeleton className="h-6 w-20" />
            <Skeleton className="h-5 w-8" />
            <Skeleton className="h-5 w-16" />
          </div>
        ))}
      </div>

      {/* Pagination skeleton */}
      <div className="flex justify-between items-center mt-4">
        <Skeleton className="h-5 w-48" />
        <div className="flex gap-2">
          <Skeleton className="h-10 w-24" />
          <Skeleton className="h-10 w-24" />
        </div>
      </div>
    </main>
  );
}
