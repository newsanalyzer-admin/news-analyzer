'use client';

import { Layers } from 'lucide-react';
import { Card, CardHeader, CardTitle, CardDescription, CardContent } from '@/components/ui/card';
import { AdminBreadcrumb } from '@/components/admin/AdminBreadcrumb';
import { SyncStatusCard } from '@/components/admin/SyncStatusCard';
import { SyncButton } from '@/components/admin/SyncButton';
import { useCommitteeCount } from '@/hooks/useCommittees';

const breadcrumbs = [
  { label: 'Admin', href: '/admin' },
  { label: 'Factbase' },
  { label: 'Legislative', href: '/admin/factbase/legislative' },
  { label: 'Committees' },
];

export default function CommitteesPage() {
  const { data: committeeCount, isLoading: committeeCountLoading, error: committeeCountError } = useCommitteeCount();

  return (
    <main className="container mx-auto py-8 px-4">
      <AdminBreadcrumb items={breadcrumbs} />

      <div className="flex items-center gap-3 mb-6">
        <Layers className="h-8 w-8 text-primary" />
        <h1 className="text-3xl font-bold">Committees</h1>
      </div>

      <p className="text-muted-foreground mb-8">
        Manage Congressional committees and subcommittees synced from Congress.gov.
      </p>

      {/* Status Overview */}
      <section className="mb-8">
        <h2 className="text-xl font-semibold mb-4">Status Overview</h2>
        <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
          <SyncStatusCard
            title="Committees"
            count={committeeCount}
            isLoading={committeeCountLoading}
            error={committeeCountError as Error | null}
          />
        </div>
      </section>

      {/* Sync Actions */}
      <section className="mb-8">
        <Card>
          <CardHeader>
            <CardTitle>Sync Actions</CardTitle>
            <CardDescription>
              Synchronize committee data from Congress.gov.
            </CardDescription>
          </CardHeader>
          <CardContent>
            <div className="flex flex-wrap gap-3">
              <SyncButton
                type="committees"
                title="Sync Committees"
                description="Fetch all committees from Congress.gov API."
                warning="Rate limit: 5,000 requests/hour. Estimated duration: 1-3 minutes."
              />
            </div>
          </CardContent>
        </Card>
      </section>

      {/* Information Section */}
      <section className="mb-8">
        <div className="rounded-lg border bg-card p-6">
          <h3 className="font-semibold mb-2">About Committee Data</h3>
          <p className="text-sm text-muted-foreground mb-4">
            Committee data is sourced from Congress.gov and includes standing committees,
            select committees, joint committees, and their subcommittees for both the
            House and Senate.
          </p>
          <ul className="text-sm text-muted-foreground space-y-1">
            <li>• <strong>Standing Committees:</strong> Permanent committees with legislative authority</li>
            <li>• <strong>Select Committees:</strong> Temporary committees for specific investigations</li>
            <li>• <strong>Joint Committees:</strong> Committees with members from both chambers</li>
            <li>• <strong>Subcommittees:</strong> Specialized divisions within parent committees</li>
          </ul>
        </div>
      </section>
    </main>
  );
}
