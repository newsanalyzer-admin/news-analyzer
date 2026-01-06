'use client';

import { Factory } from 'lucide-react';
import { AdminPlaceholderPage } from '@/components/admin/AdminPlaceholderPage';

export default function AdminCorporationsPage() {
  return (
    <AdminPlaceholderPage
      title="Government Corporations"
      description="Manage federally chartered corporations data."
      kbPath="/knowledge-base/government/executive/corporations"
      backPath="/admin/knowledge-base/government/executive"
      backLabel="Back to Executive Branch"
      icon={<Factory className="h-8 w-8" />}
    />
  );
}
