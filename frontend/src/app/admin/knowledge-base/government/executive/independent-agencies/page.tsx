'use client';

import { Building2 } from 'lucide-react';
import { AdminPlaceholderPage } from '@/components/admin/AdminPlaceholderPage';

export default function AdminIndependentAgenciesPage() {
  return (
    <AdminPlaceholderPage
      title="Independent Agencies"
      description="Manage independent federal agencies data."
      kbPath="/knowledge-base/government/executive/independent-agencies"
      backPath="/admin/knowledge-base/government/executive"
      backLabel="Back to Executive Branch"
      icon={<Building2 className="h-8 w-8" />}
    />
  );
}
