'use client';

import Link from 'next/link';
import { Scale, Users, Layers } from 'lucide-react';
import { Card, CardHeader, CardTitle, CardDescription, CardContent } from '@/components/ui/card';
import { AdminBreadcrumb } from '@/components/admin/AdminBreadcrumb';

const breadcrumbs = [
  { label: 'Admin', href: '/admin' },
  { label: 'Factbase' },
  { label: 'Legislative Branch' },
];

export default function LegislativeBranchPage() {
  return (
    <main className="container mx-auto py-8 px-4">
      <AdminBreadcrumb items={breadcrumbs} />

      <div className="flex items-center gap-3 mb-6">
        <Scale className="h-8 w-8 text-primary" />
        <h1 className="text-3xl font-bold">Legislative Branch</h1>
      </div>

      <p className="text-muted-foreground mb-8">
        Manage Congressional members, committees, and committee memberships from Congress.gov.
      </p>

      <div className="grid gap-6 md:grid-cols-2">
        {/* Members Card */}
        <Link href="/admin/factbase/legislative/members">
          <Card className="h-full hover:border-primary transition-colors cursor-pointer">
            <CardHeader>
              <div className="flex items-center gap-3">
                <Users className="h-6 w-6 text-primary" />
                <CardTitle>Members</CardTitle>
              </div>
              <CardDescription>
                Manage Congressional members from Congress.gov
              </CardDescription>
            </CardHeader>
            <CardContent>
              <ul className="text-sm text-muted-foreground space-y-1">
                <li>• Sync member data from Congress.gov</li>
                <li>• Enrich with social media and external IDs</li>
                <li>• Manage committee memberships</li>
              </ul>
            </CardContent>
          </Card>
        </Link>

        {/* Committees Card */}
        <Link href="/admin/factbase/legislative/committees">
          <Card className="h-full hover:border-primary transition-colors cursor-pointer">
            <CardHeader>
              <div className="flex items-center gap-3">
                <Layers className="h-6 w-6 text-primary" />
                <CardTitle>Committees</CardTitle>
              </div>
              <CardDescription>
                Manage Congressional committees and subcommittees
              </CardDescription>
            </CardHeader>
            <CardContent>
              <ul className="text-sm text-muted-foreground space-y-1">
                <li>• Sync committee data from Congress.gov</li>
                <li>• View committee hierarchy</li>
                <li>• Track committee types and chambers</li>
              </ul>
            </CardContent>
          </Card>
        </Link>
      </div>
    </main>
  );
}
