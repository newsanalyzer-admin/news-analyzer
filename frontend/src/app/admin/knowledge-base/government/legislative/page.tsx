'use client';

import Link from 'next/link';
import {
  Landmark,
  ChevronRight,
  ArrowLeft,
  Building2,
  Home,
  HeartHandshake,
  Users2,
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
 * Admin - Legislative Branch hub page.
 * Provides navigation to manage Legislative Branch entity data.
 */
export default function AdminLegislativePage() {
  const sections: SectionCardProps[] = [
    {
      title: 'Senate',
      description: 'Manage U.S. Senate data including senators and leadership.',
      href: '/admin/knowledge-base/government/legislative/senate',
      icon: <Building2 className="h-6 w-6" />,
    },
    {
      title: 'House of Representatives',
      description: 'Manage House data including representatives and leadership.',
      href: '/admin/knowledge-base/government/legislative/house',
      icon: <Home className="h-6 w-6" />,
    },
    {
      title: 'Support Services',
      description: 'Manage congressional support agencies (LOC, GAO, CBO, etc.).',
      href: '/admin/knowledge-base/government/legislative/support-services',
      icon: <HeartHandshake className="h-6 w-6" />,
    },
    {
      title: 'Committees',
      description: 'Manage congressional committee data.',
      href: '/admin/knowledge-base/government/legislative/committees',
      icon: <Users2 className="h-6 w-6" />,
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
            <Landmark className="h-8 w-8" />
          </div>
          <h1 className="text-3xl font-bold">Legislative Branch</h1>
        </div>
        <p className="text-muted-foreground">
          Manage data for the Legislative Branch (Congress) of the U.S. federal government.
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
