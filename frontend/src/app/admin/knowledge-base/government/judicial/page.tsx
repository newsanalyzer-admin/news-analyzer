'use client';

import Link from 'next/link';
import {
  Scale,
  ChevronRight,
  ArrowLeft,
  Gavel,
  Building,
  MapPin,
  Briefcase,
} from 'lucide-react';
import { Button } from '@/components/ui/button';
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
 * Admin - Judicial Branch hub page.
 * Provides navigation to manage Judicial Branch entity data.
 */
export default function AdminJudicialPage() {
  const sections: SectionCardProps[] = [
    {
      title: 'Supreme Court',
      description: 'Manage Supreme Court justices and case data.',
      href: '/admin/knowledge-base/government/judicial/supreme-court',
      icon: <Gavel className="h-6 w-6" />,
    },
    {
      title: 'Courts of Appeals',
      description: 'Manage circuit courts and judges data.',
      href: '/admin/knowledge-base/government/judicial/courts-of-appeals',
      icon: <Building className="h-6 w-6" />,
    },
    {
      title: 'District Courts',
      description: 'Manage district courts and judges data.',
      href: '/admin/knowledge-base/government/judicial/district-courts',
      icon: <MapPin className="h-6 w-6" />,
    },
    {
      title: 'Specialized Courts',
      description: 'Manage specialized federal courts data.',
      href: '/admin/knowledge-base/government/judicial/specialized-courts',
      icon: <Briefcase className="h-6 w-6" />,
    },
  ];

  return (
    <div className="container py-8">
      {/* Back link */}
      <div className="mb-4">
        <Button variant="ghost" size="sm" asChild className="-ml-2">
          <Link href="/admin/knowledge-base/government">
            <ArrowLeft className="h-4 w-4 mr-2" />
            Back to Government
          </Link>
        </Button>
      </div>

      {/* Header */}
      <div className="mb-8">
        <div className="flex items-center gap-3 mb-4">
          <div className="p-3 rounded-lg bg-primary/10 text-primary">
            <Scale className="h-8 w-8" />
          </div>
          <h1 className="text-3xl font-bold">Judicial Branch</h1>
        </div>
        <p className="text-muted-foreground">
          Manage data for the Judicial Branch (Federal Courts) of the U.S. federal government.
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
