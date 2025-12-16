'use client';

import { useState, useEffect } from 'react';
import Link from 'next/link';
import { Gavel, Scale, Building } from 'lucide-react';
import { Card, CardHeader, CardTitle, CardDescription, CardContent } from '@/components/ui/card';
import { AdminBreadcrumb } from '@/components/admin/AdminBreadcrumb';

const breadcrumbs = [
  { label: 'Admin', href: '/admin' },
  { label: 'Factbase' },
  { label: 'Judicial Branch' },
];

interface BranchCount {
  executive: number;
  legislative: number;
  judicial: number;
}

interface SyncStatus {
  totalOrganizations: number;
  countByBranch: BranchCount;
}

export default function JudicialBranchPage() {
  const [judicialCount, setJudicialCount] = useState<number | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchJudicialCount = async () => {
      try {
        const response = await fetch('/api/government-organizations/sync/status');
        if (response.ok) {
          const status: SyncStatus = await response.json();
          setJudicialCount(status.countByBranch?.judicial ?? 0);
        }
      } catch (error) {
        console.error('Failed to fetch judicial org count:', error);
      } finally {
        setLoading(false);
      }
    };

    fetchJudicialCount();
  }, []);

  return (
    <main className="container mx-auto py-8 px-4">
      <AdminBreadcrumb items={breadcrumbs} />

      <div className="flex items-center gap-3 mb-6">
        <Gavel className="h-8 w-8 text-primary" />
        <h1 className="text-3xl font-bold">Judicial Branch</h1>
      </div>

      <p className="text-muted-foreground mb-8">
        Manage federal courts and judicial branch organizations. The Judicial Branch interprets
        the meaning of laws, applies laws to individual cases, and decides if laws violate the Constitution.
      </p>

      {/* Summary Stats */}
      <div className="mb-8 p-4 bg-muted/50 rounded-lg">
        <div className="flex items-center gap-2 text-sm text-muted-foreground">
          <Building className="h-4 w-4" />
          <span>
            {loading ? (
              'Loading...'
            ) : (
              <>
                <strong className="text-foreground">{judicialCount}</strong> judicial organization{judicialCount !== 1 ? 's' : ''} in database
              </>
            )}
          </span>
        </div>
      </div>

      <div className="grid gap-6 md:grid-cols-2 lg:grid-cols-3">
        {/* Courts Card */}
        <Link href="/admin/factbase/judicial/courts">
          <Card className="h-full hover:border-primary transition-colors cursor-pointer">
            <CardHeader>
              <div className="flex items-center gap-3">
                <Scale className="h-6 w-6 text-primary" />
                <CardTitle>Courts</CardTitle>
              </div>
              <CardDescription>
                View and manage federal courts and judicial organizations
              </CardDescription>
            </CardHeader>
            <CardContent>
              <ul className="text-sm text-muted-foreground space-y-1">
                <li>• Supreme Court of the United States</li>
                <li>• Federal appellate courts</li>
                <li>• District courts and specialized courts</li>
              </ul>
            </CardContent>
          </Card>
        </Link>
      </div>
    </main>
  );
}
