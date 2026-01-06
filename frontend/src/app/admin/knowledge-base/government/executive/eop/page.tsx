'use client';

import { Building } from 'lucide-react';
import { AdminPlaceholderPage } from '@/components/admin/AdminPlaceholderPage';

export default function AdminEOPPage() {
  return (
    <AdminPlaceholderPage
      title="Executive Office of the President"
      description="Manage EOP offices and staff data."
      kbPath="/knowledge-base/government/executive/eop"
      backPath="/admin/knowledge-base/government/executive"
      backLabel="Back to Executive Branch"
      icon={<Building className="h-8 w-8" />}
    />
  );
}
