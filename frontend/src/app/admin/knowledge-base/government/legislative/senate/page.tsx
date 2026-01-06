'use client';

import { Building2 } from 'lucide-react';
import { AdminPlaceholderPage } from '@/components/admin/AdminPlaceholderPage';

export default function AdminSenatePage() {
  return (
    <AdminPlaceholderPage
      title="Senate"
      description="Manage U.S. Senate data including senators and leadership."
      kbPath="/knowledge-base/government/legislative/senate"
      backPath="/admin/knowledge-base/government/legislative"
      backLabel="Back to Legislative Branch"
      icon={<Building2 className="h-8 w-8" />}
    />
  );
}
