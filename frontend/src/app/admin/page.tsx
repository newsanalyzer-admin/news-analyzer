'use client';

/**
 * Admin Dashboard Page
 *
 * Displays sync status and provides manual sync controls for Congressional data.
 * Accessible only to admin users via direct URL navigation.
 */

import Link from 'next/link';
import { Landmark, Scale, Gavel } from 'lucide-react';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { useIsAdmin } from '@/hooks/useIsAdmin';
import { useMemberCount, useEnrichmentStatus } from '@/hooks/useMembers';
import { useCommitteeCount } from '@/hooks/useCommittees';
import { SyncStatusCard } from '@/components/admin/SyncStatusCard';
import { SyncButton } from '@/components/admin/SyncButton';
import { EnrichmentStatus } from '@/components/admin/EnrichmentStatus';
import { GovOrgSyncStatusCard } from '@/components/admin/GovOrgSyncStatusCard';
import { PlumSyncCard } from '@/components/admin/PlumSyncCard';
import { CsvImportButton } from '@/components/admin/CsvImportButton';

function AccessDenied() {
  return (
    <div className="min-h-screen flex items-center justify-center">
      <Card className="max-w-md p-8 text-center">
        <div className="text-6xl mb-4">&#128274;</div>
        <h1 className="text-2xl font-bold mb-2">Access Denied</h1>
        <p className="text-muted-foreground mb-4">
          You don&apos;t have permission to access the admin dashboard.
        </p>
        <Button asChild variant="outline">
          <Link href="/">Return to Home</Link>
        </Button>
      </Card>
    </div>
  );
}

function AdminDashboard() {
  const { data: memberCount, isLoading: memberCountLoading, error: memberCountError } = useMemberCount();
  const { data: committeeCount, isLoading: committeeCountLoading, error: committeeCountError } = useCommitteeCount();
  const { data: enrichmentStatus, isLoading: enrichmentLoading, error: enrichmentError } = useEnrichmentStatus();

  const isLoading = memberCountLoading || committeeCountLoading || enrichmentLoading;
  const hasError = memberCountError || committeeCountError || enrichmentError;

  return (
    <main className="container mx-auto py-8 px-4">
      <div className="mb-8">
        <h1 className="text-3xl font-bold mb-2">Admin Dashboard</h1>
        <p className="text-muted-foreground">
          Congressional Data Sync Management
        </p>
      </div>

      {/* Data Overview Section */}
      <section className="mb-8">
        <h2 className="text-xl font-semibold mb-4">Data Overview</h2>
        <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-5">
          <SyncStatusCard
            title="Members"
            count={memberCount}
            isLoading={memberCountLoading}
            error={memberCountError as Error | null}
          />
          <SyncStatusCard
            title="Committees"
            count={committeeCount}
            isLoading={committeeCountLoading}
            error={committeeCountError as Error | null}
          />
          <SyncStatusCard
            title="Enriched Members"
            count={enrichmentStatus?.enrichedMembers}
            total={enrichmentStatus?.totalMembers}
            isLoading={enrichmentLoading}
            error={enrichmentError as Error | null}
            showPercentage
          />
          <GovOrgSyncStatusCard />
          <PlumSyncCard />
        </div>
      </section>

      {/* Quick Navigation Section */}
      <section className="mb-8">
        <h2 className="text-xl font-semibold mb-4">Factbase Management</h2>
        <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
          <Link href="/admin/factbase/executive">
            <Card className="h-full hover:border-primary transition-colors cursor-pointer">
              <CardHeader className="pb-3">
                <div className="flex items-center gap-2">
                  <Landmark className="h-5 w-5 text-primary" />
                  <CardTitle className="text-base">Executive Branch</CardTitle>
                </div>
              </CardHeader>
              <CardContent>
                <p className="text-sm text-muted-foreground">
                  Agencies, departments, and political appointees
                </p>
              </CardContent>
            </Card>
          </Link>
          <Link href="/admin/factbase/legislative">
            <Card className="h-full hover:border-primary transition-colors cursor-pointer">
              <CardHeader className="pb-3">
                <div className="flex items-center gap-2">
                  <Scale className="h-5 w-5 text-primary" />
                  <CardTitle className="text-base">Legislative Branch</CardTitle>
                </div>
              </CardHeader>
              <CardContent>
                <p className="text-sm text-muted-foreground">
                  Members of Congress and committees
                </p>
              </CardContent>
            </Card>
          </Link>
          <Link href="/admin/factbase/judicial">
            <Card className="h-full hover:border-primary transition-colors cursor-pointer">
              <CardHeader className="pb-3">
                <div className="flex items-center gap-2">
                  <Gavel className="h-5 w-5 text-primary" />
                  <CardTitle className="text-base">Judicial Branch</CardTitle>
                </div>
              </CardHeader>
              <CardContent>
                <p className="text-sm text-muted-foreground">
                  Federal courts and judges
                </p>
              </CardContent>
            </Card>
          </Link>
        </div>
      </section>

      {/* Manual Sync Actions Section */}
      <section className="mb-8">
        <Card>
          <CardHeader>
            <CardTitle>Manual Sync Actions</CardTitle>
            <CardDescription>
              Trigger data synchronization with external sources. These operations may take several minutes.
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
                type="committees"
                title="Sync Committees"
                description="Fetch all committees from Congress.gov API."
                warning="Rate limit: 5,000 requests/hour. Estimated duration: 1-3 minutes."
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
              <SyncButton
                type="gov-orgs"
                title="Sync Government Orgs"
                description="This will fetch ~300 agencies from the Federal Register API and update the database."
                warning="May take 30-60 seconds to complete. Existing manually curated data will be preserved. Only executive branch agencies will be synced."
              />
            </div>
          </CardContent>
        </Card>
      </section>

      {/* Data Import Section */}
      <section className="mb-8">
        <Card>
          <CardHeader>
            <CardTitle>Data Import</CardTitle>
            <CardDescription>
              Import government organization data from CSV files. Use this for Legislative and Judicial branches.
            </CardDescription>
          </CardHeader>
          <CardContent>
            <div className="flex flex-wrap gap-3">
              <CsvImportButton />
            </div>
          </CardContent>
        </Card>
      </section>

      {/* Enrichment Status Section */}
      <section className="mb-8">
        <EnrichmentStatus
          status={enrichmentStatus}
          isLoading={enrichmentLoading}
          error={enrichmentError as Error | null}
        />
      </section>

      {/* Error Display */}
      {hasError && !isLoading && (
        <Card className="border-destructive">
          <CardHeader>
            <CardTitle className="text-destructive">Error Loading Data</CardTitle>
          </CardHeader>
          <CardContent>
            <p className="text-muted-foreground mb-4">
              Some data failed to load. Please check your backend connection.
            </p>
            <Button
              variant="outline"
              onClick={() => window.location.reload()}
            >
              Retry
            </Button>
          </CardContent>
        </Card>
      )}
    </main>
  );
}

export default function AdminPage() {
  const isAdmin = useIsAdmin();

  // TODO: When auth system is integrated, this check will use actual role verification
  if (!isAdmin) {
    return <AccessDenied />;
  }

  return <AdminDashboard />;
}
