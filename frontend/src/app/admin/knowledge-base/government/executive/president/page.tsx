'use client';

import { useState } from 'react';
import Link from 'next/link';
import { Crown, ArrowLeft, ChevronLeft, ChevronRight } from 'lucide-react';
import { AdminBreadcrumb } from '@/components/admin/AdminBreadcrumb';
import { PresidencySyncCard } from '@/components/admin/PresidencySyncCard';
import { Button } from '@/components/ui/button';
import { Skeleton } from '@/components/ui/skeleton';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table';
import { usePresidencies } from '@/hooks/usePresidencySync';

const breadcrumbs = [
  { label: 'Admin', href: '/admin' },
  { label: 'Knowledge Base' },
  { label: 'Government', href: '/admin/knowledge-base/government' },
  { label: 'Executive', href: '/admin/knowledge-base/government/executive' },
  { label: 'President' },
];

export default function AdminPresidentPage() {
  const [page, setPage] = useState(0);
  const pageSize = 15;
  const { data, isLoading, error } = usePresidencies(page, pageSize);

  return (
    <main className="container mx-auto py-8 px-4">
      <AdminBreadcrumb items={breadcrumbs} />

      <div className="flex items-center justify-between mb-6">
        <div className="flex items-center gap-3">
          <Crown className="h-8 w-8 text-primary" />
          <h1 className="text-3xl font-bold">President of the United States</h1>
        </div>
        <Link href="/admin/knowledge-base/government/executive">
          <Button variant="ghost" size="sm" className="gap-2">
            <ArrowLeft className="h-4 w-4" />
            Back to Executive Branch
          </Button>
        </Link>
      </div>

      <p className="text-muted-foreground mb-8">
        Manage presidential data including all 47 presidencies, vice presidents, and executive orders.
      </p>

      {/* Sync Status Card */}
      <section className="mb-8">
        <h2 className="text-xl font-semibold mb-4">Data Sync</h2>
        <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
          <PresidencySyncCard />
        </div>
      </section>

      {/* Presidencies Table */}
      <section>
        <div className="flex items-center justify-between mb-4">
          <h2 className="text-xl font-semibold">Presidencies</h2>
          {data && (
            <p className="text-sm text-muted-foreground">
              Showing {data.content.length} of {data.totalElements} presidencies
            </p>
          )}
        </div>

        {isLoading ? (
          <div className="rounded-md border">
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead className="w-16">#</TableHead>
                  <TableHead>President</TableHead>
                  <TableHead>Party</TableHead>
                  <TableHead>Term</TableHead>
                  <TableHead>Vice President(s)</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {Array.from({ length: 5 }).map((_, i) => (
                  <TableRow key={i}>
                    <TableCell><Skeleton className="h-4 w-8" /></TableCell>
                    <TableCell><Skeleton className="h-4 w-40" /></TableCell>
                    <TableCell><Skeleton className="h-4 w-24" /></TableCell>
                    <TableCell><Skeleton className="h-4 w-28" /></TableCell>
                    <TableCell><Skeleton className="h-4 w-32" /></TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </div>
        ) : error ? (
          <div className="rounded-md border p-8 text-center">
            <p className="text-destructive mb-2">Failed to load presidencies</p>
            <p className="text-sm text-muted-foreground">
              Make sure the backend is running and try syncing presidential data.
            </p>
          </div>
        ) : data && data.content.length > 0 ? (
          <>
            <div className="rounded-md border">
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead className="w-16">#</TableHead>
                    <TableHead>President</TableHead>
                    <TableHead>Party</TableHead>
                    <TableHead>Term</TableHead>
                    <TableHead>Vice President(s)</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {data.content.map((presidency) => (
                    <TableRow key={presidency.id}>
                      <TableCell className="font-medium">
                        {presidency.ordinalLabel}
                      </TableCell>
                      <TableCell>
                        <div className="font-medium">{presidency.presidentFullName}</div>
                        {presidency.current && (
                          <span className="text-xs text-green-600 font-medium">Current</span>
                        )}
                      </TableCell>
                      <TableCell>
                        <span className={getPartyColor(presidency.party)}>
                          {presidency.party}
                        </span>
                      </TableCell>
                      <TableCell>{presidency.termLabel}</TableCell>
                      <TableCell>
                        {presidency.vicePresidents && presidency.vicePresidents.length > 0 ? (
                          <div className="space-y-1">
                            {presidency.vicePresidents.map((vp, idx) => (
                              <div key={idx} className="text-sm">
                                {vp.fullName}
                                <span className="text-muted-foreground text-xs ml-1">
                                  ({vp.termLabel})
                                </span>
                              </div>
                            ))}
                          </div>
                        ) : (
                          <span className="text-muted-foreground text-sm">None</span>
                        )}
                      </TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </div>

            {/* Pagination */}
            {data.totalPages > 1 && (
              <div className="flex items-center justify-between mt-4">
                <p className="text-sm text-muted-foreground">
                  Page {data.number + 1} of {data.totalPages}
                </p>
                <div className="flex gap-2">
                  <Button
                    variant="outline"
                    size="sm"
                    onClick={() => setPage((p) => Math.max(0, p - 1))}
                    disabled={data.number === 0}
                  >
                    <ChevronLeft className="h-4 w-4" />
                    Previous
                  </Button>
                  <Button
                    variant="outline"
                    size="sm"
                    onClick={() => setPage((p) => p + 1)}
                    disabled={data.number >= data.totalPages - 1}
                  >
                    Next
                    <ChevronRight className="h-4 w-4" />
                  </Button>
                </div>
              </div>
            )}
          </>
        ) : (
          <div className="rounded-md border p-8 text-center">
            <Crown className="h-12 w-12 mx-auto text-muted-foreground mb-4" />
            <p className="font-medium mb-2">No presidencies found</p>
            <p className="text-sm text-muted-foreground mb-4">
              Click &quot;Sync Presidential Data&quot; above to import all 47 U.S. presidencies.
            </p>
          </div>
        )}
      </section>

      {/* Information Section */}
      <section className="mt-8">
        <div className="rounded-lg border bg-card p-6">
          <h3 className="font-semibold mb-2">About Presidential Data</h3>
          <p className="text-sm text-muted-foreground mb-4">
            This page manages data for all 47 U.S. presidencies from George Washington (1789)
            to the current president. Data includes president and vice president biographical
            information, term dates, party affiliation, and executive orders.
          </p>
          <ul className="text-sm text-muted-foreground space-y-1">
            <li>• <strong>Presidencies:</strong> Unique term records (1-47)</li>
            <li>• <strong>Persons:</strong> President and VP biographical records</li>
            <li>• <strong>VP Holdings:</strong> Vice President position assignments</li>
            <li>• <strong>Non-consecutive terms:</strong> Cleveland (22nd/24th) and Trump (45th/47th)</li>
          </ul>
        </div>
      </section>
    </main>
  );
}

/**
 * Get party color class for styling
 */
function getPartyColor(party: string): string {
  const partyLower = party?.toLowerCase() || '';
  if (partyLower.includes('republican')) {
    return 'text-red-600';
  }
  if (partyLower.includes('democrat')) {
    return 'text-blue-600';
  }
  if (partyLower.includes('whig')) {
    return 'text-amber-600';
  }
  if (partyLower.includes('federalist')) {
    return 'text-purple-600';
  }
  return 'text-gray-600';
}
