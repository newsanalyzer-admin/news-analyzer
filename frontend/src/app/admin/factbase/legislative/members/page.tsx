'use client';

import { Users } from 'lucide-react';
import { Card, CardHeader, CardTitle, CardDescription, CardContent } from '@/components/ui/card';
import { AdminBreadcrumb } from '@/components/admin/AdminBreadcrumb';
import { SyncStatusCard } from '@/components/admin/SyncStatusCard';
import { SyncButton } from '@/components/admin/SyncButton';
import { EnrichmentStatus } from '@/components/admin/EnrichmentStatus';
import { useMemberCount, useEnrichmentStatus } from '@/hooks/useMembers';

const breadcrumbs = [
  { label: 'Admin', href: '/admin' },
  { label: 'Factbase' },
  { label: 'Legislative', href: '/admin/factbase/legislative' },
  { label: 'Members' },
];

export default function MembersPage() {
  const { data: memberCount, isLoading: memberCountLoading, error: memberCountError } = useMemberCount();
  const { data: enrichmentStatus, isLoading: enrichmentLoading, error: enrichmentError } = useEnrichmentStatus();

  return (
    <main className="container mx-auto py-8 px-4">
      <AdminBreadcrumb items={breadcrumbs} />

      <div className="flex items-center gap-3 mb-6">
        <Users className="h-8 w-8 text-primary" />
        <h1 className="text-3xl font-bold">Members</h1>
      </div>

      <p className="text-muted-foreground mb-8">
        Manage Congressional members synced from Congress.gov and enriched from the legislators repository.
      </p>

      {/* Status Overview */}
      <section className="mb-8">
        <h2 className="text-xl font-semibold mb-4">Status Overview</h2>
        <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
          <SyncStatusCard
            title="Members"
            count={memberCount}
            isLoading={memberCountLoading}
            error={memberCountError as Error | null}
          />
          <SyncStatusCard
            title="Enriched Members"
            count={enrichmentStatus?.enrichedMembers}
            total={enrichmentStatus?.totalMembers}
            isLoading={enrichmentLoading}
            error={enrichmentError as Error | null}
            showPercentage
          />
        </div>
      </section>

      {/* Sync Actions */}
      <section className="mb-8">
        <Card>
          <CardHeader>
            <CardTitle>Sync Actions</CardTitle>
            <CardDescription>
              Synchronize member data from external sources.
            </CardDescription>
          </CardHeader>
          <CardContent>
            <div className="flex flex-wrap gap-3">
              <SyncButton
                type="members"
                title="Sync Members"
                description="Fetch all current members from Congress.gov API."
                warning="Rate limit: 5,000 requests/hour. Estimated duration: 2-5 minutes."
              />
              <SyncButton
                type="memberships"
                title="Sync Memberships"
                description="Fetch committee membership data for the 118th Congress."
                warning="Rate limit: 5,000 requests/hour. Estimated duration: 5-10 minutes."
              />
              <SyncButton
                type="enrichment"
                title="Sync Enrichment"
                description="Enrich member data from unitedstates/congress-legislators repository."
                warning="This will update social media links, external IDs, and biographical data."
              />
            </div>
          </CardContent>
        </Card>
      </section>

      {/* Enrichment Status */}
      <section className="mb-8">
        <EnrichmentStatus
          status={enrichmentStatus}
          isLoading={enrichmentLoading}
          error={enrichmentError as Error | null}
        />
      </section>

      {/* Information Section */}
      <section className="mb-8">
        <div className="rounded-lg border bg-card p-6">
          <h3 className="font-semibold mb-2">About Member Data</h3>
          <p className="text-sm text-muted-foreground mb-4">
            Member data is sourced from Congress.gov and enriched with additional information from the
            unitedstates/congress-legislators repository including social media profiles, external
            identifiers (Wikidata, ICPSR, etc.), and biographical data.
          </p>
          <ul className="text-sm text-muted-foreground space-y-1">
            <li>• <strong>Members Sync:</strong> Fetches basic member info from Congress.gov</li>
            <li>• <strong>Memberships Sync:</strong> Links members to their committee assignments</li>
            <li>• <strong>Enrichment Sync:</strong> Adds social media, external IDs, and bio data</li>
          </ul>
        </div>
      </section>
    </main>
  );
}
