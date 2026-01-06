'use client';

import { BookOpen } from 'lucide-react';
import { AdminPlaceholderPage } from '@/components/admin/AdminPlaceholderPage';

export default function AdminUSCodePage() {
  return (
    <AdminPlaceholderPage
      title="U.S. Code"
      description="Manage federal statutory law data imports and updates."
      kbPath="/knowledge-base/government/us-code"
      backPath="/admin/knowledge-base/government"
      backLabel="Back to Government"
      icon={<BookOpen className="h-8 w-8" />}
    />
  );
}
