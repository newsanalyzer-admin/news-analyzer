'use client';

import { UserCircle } from 'lucide-react';
import { AdminPlaceholderPage } from '@/components/admin/AdminPlaceholderPage';

export default function AdminVicePresidentPage() {
  return (
    <AdminPlaceholderPage
      title="Vice President"
      description="Manage Vice President of the United States data."
      kbPath="/knowledge-base/government/executive/vice-president"
      backPath="/admin/knowledge-base/government/executive"
      backLabel="Back to Executive Branch"
      icon={<UserCircle className="h-8 w-8" />}
    />
  );
}
