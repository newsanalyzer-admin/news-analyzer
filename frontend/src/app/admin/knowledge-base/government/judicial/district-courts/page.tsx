'use client';

import { MapPin } from 'lucide-react';
import { AdminPlaceholderPage } from '@/components/admin/AdminPlaceholderPage';

export default function AdminDistrictCourtsPage() {
  return (
    <AdminPlaceholderPage
      title="District Courts"
      description="Manage district courts and judges data."
      kbPath="/knowledge-base/government/judicial/district-courts"
      backPath="/admin/knowledge-base/government/judicial"
      backLabel="Back to Judicial Branch"
      icon={<MapPin className="h-8 w-8" />}
    />
  );
}
