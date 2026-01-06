'use client';

import { Crown } from 'lucide-react';
import { AdminPlaceholderPage } from '@/components/admin/AdminPlaceholderPage';

export default function AdminPresidentPage() {
  return (
    <AdminPlaceholderPage
      title="President"
      description="Manage President of the United States data."
      kbPath="/knowledge-base/government/executive/president"
      backPath="/admin/knowledge-base/government/executive"
      backLabel="Back to Executive Branch"
      icon={<Crown className="h-8 w-8" />}
    />
  );
}
