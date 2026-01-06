'use client';

import { Briefcase } from 'lucide-react';
import { AdminPlaceholderPage } from '@/components/admin/AdminPlaceholderPage';

export default function AdminCabinetPage() {
  return (
    <AdminPlaceholderPage
      title="Cabinet Departments"
      description="Manage the 15 executive departments data."
      kbPath="/knowledge-base/government/executive/cabinet"
      backPath="/admin/knowledge-base/government/executive"
      backLabel="Back to Executive Branch"
      icon={<Briefcase className="h-8 w-8" />}
    />
  );
}
