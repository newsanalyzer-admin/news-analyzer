'use client';

import Link from 'next/link';
import { Building2, Briefcase, Landmark, FileUp } from 'lucide-react';
import { Card, CardHeader, CardTitle, CardDescription, CardContent } from '@/components/ui/card';
import { AdminBreadcrumb } from '@/components/admin/AdminBreadcrumb';

const breadcrumbs = [
  { label: 'Admin', href: '/admin' },
  { label: 'Factbase' },
  { label: 'Executive Branch' },
];

export default function ExecutiveBranchPage() {
  return (
    <main className="container mx-auto py-8 px-4">
      <AdminBreadcrumb items={breadcrumbs} />

      <div className="flex items-center gap-3 mb-6">
        <Landmark className="h-8 w-8 text-primary" />
        <h1 className="text-3xl font-bold">Executive Branch</h1>
      </div>

      <p className="text-muted-foreground mb-8">
        Manage executive branch government organizations, agencies, departments, and political appointees.
      </p>

      <div className="grid gap-6 md:grid-cols-2 lg:grid-cols-3">
        {/* Agencies & Departments Card */}
        <Link href="/admin/factbase/executive/agencies">
          <Card className="h-full hover:border-primary transition-colors cursor-pointer">
            <CardHeader>
              <div className="flex items-center gap-3">
                <Building2 className="h-6 w-6 text-primary" />
                <CardTitle>Agencies & Departments</CardTitle>
              </div>
              <CardDescription>
                Manage government organizations synced from the Federal Register API
              </CardDescription>
            </CardHeader>
            <CardContent>
              <ul className="text-sm text-muted-foreground space-y-1">
                <li>• View and sync government organizations</li>
                <li>• Import from CSV for Legislative/Judicial branches</li>
                <li>• Track Federal Register API status</li>
              </ul>
            </CardContent>
          </Card>
        </Link>

        {/* Positions & Appointees Card */}
        <Link href="/admin/factbase/executive/positions">
          <Card className="h-full hover:border-primary transition-colors cursor-pointer">
            <CardHeader>
              <div className="flex items-center gap-3">
                <Briefcase className="h-6 w-6 text-primary" />
                <CardTitle>Positions & Appointees</CardTitle>
              </div>
              <CardDescription>
                Manage executive branch appointees from the PLUM Book
              </CardDescription>
            </CardHeader>
            <CardContent>
              <ul className="text-sm text-muted-foreground space-y-1">
                <li>• Sync PLUM data from OPM</li>
                <li>• View presidential appointees</li>
                <li>• Track position holdings</li>
              </ul>
            </CardContent>
          </Card>
        </Link>

        {/* GOVMAN Import Card */}
        <Link href="/admin/factbase/executive/govman">
          <Card className="h-full hover:border-primary transition-colors cursor-pointer">
            <CardHeader>
              <div className="flex items-center gap-3">
                <FileUp className="h-6 w-6 text-primary" />
                <CardTitle>GOVMAN Import</CardTitle>
              </div>
              <CardDescription>
                Import official government organization structure from GOVMAN XML
              </CardDescription>
            </CardHeader>
            <CardContent>
              <ul className="text-sm text-muted-foreground space-y-1">
                <li>• Import from Government Manual XML</li>
                <li>• Create organizational hierarchies</li>
                <li>• Sync official agency data</li>
              </ul>
            </CardContent>
          </Card>
        </Link>
      </div>
    </main>
  );
}
