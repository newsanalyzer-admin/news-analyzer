'use client';

import { Briefcase } from 'lucide-react';
import { AdminPlaceholderPage } from '@/components/admin/AdminPlaceholderPage';

export default function AdminSpecializedCourtsPage() {
  return (
    <AdminPlaceholderPage
      title="Specialized Courts"
      description="Manage specialized federal courts data."
      kbPath="/knowledge-base/government/judicial/specialized-courts"
      backPath="/admin/knowledge-base/government/judicial"
      backLabel="Back to Judicial Branch"
      icon={<Briefcase className="h-8 w-8" />}
    />
  );
}
