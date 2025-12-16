'use client';

import { Briefcase } from 'lucide-react';
import { AdminBreadcrumb } from '@/components/admin/AdminBreadcrumb';
import { PlumSyncCard } from '@/components/admin/PlumSyncCard';

const breadcrumbs = [
  { label: 'Admin', href: '/admin' },
  { label: 'Factbase' },
  { label: 'Executive', href: '/admin/factbase/executive' },
  { label: 'Positions & Appointees' },
];

export default function PositionsPage() {
  return (
    <main className="container mx-auto py-8 px-4">
      <AdminBreadcrumb items={breadcrumbs} />

      <div className="flex items-center gap-3 mb-6">
        <Briefcase className="h-8 w-8 text-primary" />
        <h1 className="text-3xl font-bold">Positions & Appointees</h1>
      </div>

      <p className="text-muted-foreground mb-8">
        Manage executive branch appointees from the PLUM Book (Policy and Supporting Positions).
      </p>

      {/* PLUM Sync Status and Actions */}
      <section className="mb-8">
        <h2 className="text-xl font-semibold mb-4">PLUM Data Sync</h2>
        <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
          <PlumSyncCard />
        </div>
      </section>

      {/* Information Section */}
      <section className="mb-8">
        <div className="rounded-lg border bg-card p-6">
          <h3 className="font-semibold mb-2">About PLUM Data</h3>
          <p className="text-sm text-muted-foreground mb-4">
            The PLUM Book (Policy and Supporting Positions) lists over 9,000 federal civil service
            leadership and support positions in the legislative and executive branches. It is
            published alternately by the Senate Committee on Homeland Security and Governmental
            Affairs and the House Committee on Oversight and Reform.
          </p>
          <ul className="text-sm text-muted-foreground space-y-1">
            <li>• <strong>Persons:</strong> Individuals holding positions</li>
            <li>• <strong>Positions:</strong> Government roles and titles</li>
            <li>• <strong>Holdings:</strong> The assignment of persons to positions</li>
          </ul>
        </div>
      </section>
    </main>
  );
}
