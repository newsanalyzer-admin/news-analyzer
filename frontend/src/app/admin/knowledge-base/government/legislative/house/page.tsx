'use client';

import { Home } from 'lucide-react';
import { AdminPlaceholderPage } from '@/components/admin/AdminPlaceholderPage';

export default function AdminHousePage() {
  return (
    <AdminPlaceholderPage
      title="House of Representatives"
      description="Manage House data including representatives and leadership."
      kbPath="/knowledge-base/government/legislative/house"
      backPath="/admin/knowledge-base/government/legislative"
      backLabel="Back to Legislative Branch"
      icon={<Home className="h-8 w-8" />}
    />
  );
}
