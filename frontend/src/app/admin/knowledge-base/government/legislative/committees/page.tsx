'use client';

import { Users2 } from 'lucide-react';
import { AdminPlaceholderPage } from '@/components/admin/AdminPlaceholderPage';

export default function AdminCommitteesPage() {
  return (
    <AdminPlaceholderPage
      title="Committees"
      description="Manage congressional committee data."
      kbPath="/knowledge-base/government/legislative/committees"
      backPath="/admin/knowledge-base/government/legislative"
      backLabel="Back to Legislative Branch"
      icon={<Users2 className="h-8 w-8" />}
    />
  );
}
