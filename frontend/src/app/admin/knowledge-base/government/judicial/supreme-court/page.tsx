'use client';

import { Gavel } from 'lucide-react';
import { AdminPlaceholderPage } from '@/components/admin/AdminPlaceholderPage';

export default function AdminSupremeCourtPage() {
  return (
    <AdminPlaceholderPage
      title="Supreme Court"
      description="Manage Supreme Court justices and case data."
      kbPath="/knowledge-base/government/judicial/supreme-court"
      backPath="/admin/knowledge-base/government/judicial"
      backLabel="Back to Judicial Branch"
      icon={<Gavel className="h-8 w-8" />}
    />
  );
}
