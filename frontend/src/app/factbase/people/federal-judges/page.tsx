'use client';

/**
 * Federal Judges Page
 *
 * Browse and search federal judges across all court levels.
 * Route: /factbase/people/federal-judges
 */

import { useSearchParams, useRouter, usePathname } from 'next/navigation';
import { Suspense, useState } from 'react';
import { useJudges } from '@/hooks/useJudges';
import { JudgeFilters } from '@/components/judicial/JudgeFilters';
import { JudgeTable } from '@/components/judicial/JudgeTable';
import { JudgeStats } from '@/components/judicial/JudgeStats';
import { JudgeDetailPanel } from './JudgeDetailPanel';
import type { Judge } from '@/types/judge';

function FederalJudgesContent() {
  const searchParams = useSearchParams();
  const router = useRouter();
  const pathname = usePathname();
  const [selectedJudge, setSelectedJudge] = useState<Judge | null>(null);

  // Parse URL params
  const courtLevel = searchParams.get('courtLevel') || undefined;
  const circuit = searchParams.get('circuit') || undefined;
  const status = searchParams.get('status') || undefined;
  const search = searchParams.get('search') || undefined;
  const page = parseInt(searchParams.get('page') || '0', 10);

  // Query judges with filters
  const judgesQuery = useJudges({
    courtLevel: courtLevel !== 'ALL' ? courtLevel : undefined,
    circuit: circuit !== 'ALL' ? circuit : undefined,
    status: status !== 'ALL' ? status : undefined,
    search,
    page,
    size: 20,
    sortBy: 'lastName',
    sortDir: 'asc',
  });

  const handlePageChange = (newPage: number) => {
    const params = new URLSearchParams(searchParams.toString());
    params.set('page', newPage.toString());
    router.push(`${pathname}?${params.toString()}`);
  };

  const handleRetry = () => {
    judgesQuery.refetch();
  };

  const handleSelectJudge = (judge: Judge) => {
    setSelectedJudge(judge);
  };

  const handleCloseDetail = () => {
    setSelectedJudge(null);
  };

  return (
    <main className="container mx-auto py-8 px-4">
      <div className="mb-8">
        <h1 className="text-3xl font-bold mb-2">Federal Judges</h1>
        <p className="text-muted-foreground">
          Browse federal judges serving on the Supreme Court, Courts of Appeals, and District Courts.
          Learn about their appointments, service history, and professional backgrounds.
        </p>
      </div>

      <JudgeStats />

      <JudgeFilters />

      <JudgeTable
        data={judgesQuery.data}
        isLoading={judgesQuery.isLoading}
        error={judgesQuery.error as Error | null}
        onRetry={handleRetry}
        currentPage={page}
        onPageChange={handlePageChange}
        onSelectJudge={handleSelectJudge}
      />

      {/* Detail Panel */}
      {selectedJudge && (
        <JudgeDetailPanel judge={selectedJudge} onClose={handleCloseDetail} />
      )}
    </main>
  );
}

export default function FederalJudgesPage() {
  return (
    <Suspense fallback={<FederalJudgesPageSkeleton />}>
      <FederalJudgesContent />
    </Suspense>
  );
}

function FederalJudgesPageSkeleton() {
  return (
    <main className="container mx-auto py-8 px-4">
      <div className="mb-8">
        <div className="h-10 w-64 bg-gray-200 rounded mb-2 animate-pulse" />
        <div className="h-5 w-[500px] bg-gray-100 rounded animate-pulse" />
      </div>
      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4 mb-6">
        {Array.from({ length: 4 }).map((_, i) => (
          <div key={i} className="h-24 bg-gray-100 rounded-lg animate-pulse" />
        ))}
      </div>
    </main>
  );
}
