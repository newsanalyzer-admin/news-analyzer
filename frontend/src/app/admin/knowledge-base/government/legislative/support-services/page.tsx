'use client';

import { HeartHandshake } from 'lucide-react';
import { AdminPlaceholderPage } from '@/components/admin/AdminPlaceholderPage';

export default function AdminSupportServicesPage() {
  return (
    <AdminPlaceholderPage
      title="Support Services"
      description="Manage congressional support agencies (LOC, GAO, CBO, etc.)."
      kbPath="/knowledge-base/government/legislative/support-services"
      backPath="/admin/knowledge-base/government/legislative"
      backLabel="Back to Legislative Branch"
      icon={<HeartHandshake className="h-8 w-8" />}
    />
  );
}
