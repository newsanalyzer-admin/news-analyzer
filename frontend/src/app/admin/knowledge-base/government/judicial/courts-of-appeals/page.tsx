'use client';

import { Building } from 'lucide-react';
import { AdminPlaceholderPage } from '@/components/admin/AdminPlaceholderPage';

export default function AdminCourtsOfAppealsPage() {
  return (
    <AdminPlaceholderPage
      title="Courts of Appeals"
      description="Manage circuit courts and judges data."
      kbPath="/knowledge-base/government/judicial/courts-of-appeals"
      backPath="/admin/knowledge-base/government/judicial"
      backLabel="Back to Judicial Branch"
      icon={<Building className="h-8 w-8" />}
    />
  );
}
