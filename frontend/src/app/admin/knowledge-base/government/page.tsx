'use client';

import Link from 'next/link';
import { Building2, ChevronRight, Building, Landmark, Scale, BookOpen } from 'lucide-react';
import { Card, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { cn } from '@/lib/utils';

interface SectionCardProps {
  title: string;
  description: string;
  href: string;
  icon: React.ReactNode;
}

function SectionCard({ title, description, href, icon }: SectionCardProps) {
  return (
    <Link href={href}>
      <Card className={cn(
        'group hover:border-primary hover:shadow-md transition-all cursor-pointer',
        'focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring'
      )}>
        <CardHeader>
          <div className="flex items-start justify-between">
            <div className="p-2 rounded-lg bg-primary/10 text-primary">
              {icon}
            </div>
            <ChevronRight className="h-5 w-5 text-muted-foreground group-hover:text-primary transition-colors" />
          </div>
          <CardTitle className="group-hover:text-primary transition-colors">
            {title}
          </CardTitle>
          <CardDescription>{description}</CardDescription>
        </CardHeader>
      </Card>
    </Link>
  );
}

/**
 * Admin Knowledge Base - U.S. Federal Government hub page.
 * Provides navigation to manage government entity data.
 */
export default function AdminGovernmentPage() {
  const sections: SectionCardProps[] = [
    {
      title: 'Executive Branch',
      description: 'Manage President, Vice President, EOP, Cabinet, Agencies, and Corporations data.',
      href: '/admin/knowledge-base/government/executive',
      icon: <Building className="h-6 w-6" />,
    },
    {
      title: 'Legislative Branch',
      description: 'Manage Senate, House, Support Services, and Committees data.',
      href: '/admin/knowledge-base/government/legislative',
      icon: <Landmark className="h-6 w-6" />,
    },
    {
      title: 'Judicial Branch',
      description: 'Manage Supreme Court, Appeals Courts, District Courts, and Specialized Courts data.',
      href: '/admin/knowledge-base/government/judicial',
      icon: <Scale className="h-6 w-6" />,
    },
    {
      title: 'U.S. Code',
      description: 'Manage federal statutory law data imports and updates.',
      href: '/admin/knowledge-base/government/us-code',
      icon: <BookOpen className="h-6 w-6" />,
    },
  ];

  return (
    <div className="container py-8">
      {/* Header */}
      <div className="mb-8">
        <div className="flex items-center gap-3 mb-4">
          <div className="p-3 rounded-lg bg-primary/10 text-primary">
            <Building2 className="h-8 w-8" />
          </div>
          <h1 className="text-3xl font-bold">U.S. Federal Government</h1>
        </div>
        <p className="text-muted-foreground">
          Manage authoritative reference data for the U.S. federal government structure.
        </p>
      </div>

      {/* Section Grid */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        {sections.map((section) => (
          <SectionCard key={section.href} {...section} />
        ))}
      </div>
    </div>
  );
}
