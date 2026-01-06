'use client';

import Link from 'next/link';
import {
  Building,
  ChevronRight,
  ArrowLeft,
  Crown,
  UserCircle,
  Briefcase,
  Building2,
  Factory,
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
 * Admin - Executive Branch hub page.
 * Provides navigation to manage Executive Branch entity data.
 */
export default function AdminExecutivePage() {
  const sections: SectionCardProps[] = [
    {
      title: 'President',
      description: 'Manage President of the United States data.',
      href: '/admin/knowledge-base/government/executive/president',
      icon: <Crown className="h-6 w-6" />,
    },
    {
      title: 'Vice President',
      description: 'Manage Vice President of the United States data.',
      href: '/admin/knowledge-base/government/executive/vice-president',
      icon: <UserCircle className="h-6 w-6" />,
    },
    {
      title: 'Executive Office of the President',
      description: 'Manage EOP offices and staff data.',
      href: '/admin/knowledge-base/government/executive/eop',
      icon: <Building className="h-6 w-6" />,
    },
    {
      title: 'Cabinet Departments',
      description: 'Manage the 15 executive departments data.',
      href: '/admin/knowledge-base/government/executive/cabinet',
      icon: <Briefcase className="h-6 w-6" />,
    },
    {
      title: 'Independent Agencies',
      description: 'Manage independent federal agencies data.',
      href: '/admin/knowledge-base/government/executive/independent-agencies',
      icon: <Building2 className="h-6 w-6" />,
    },
    {
      title: 'Government Corporations',
      description: 'Manage federally chartered corporations data.',
      href: '/admin/knowledge-base/government/executive/corporations',
      icon: <Factory className="h-6 w-6" />,
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
            <Building className="h-8 w-8" />
          </div>
          <h1 className="text-3xl font-bold">Executive Branch</h1>
        </div>
        <p className="text-muted-foreground">
          Manage data for the Executive Branch of the U.S. federal government.
        </p>
      </div>

      {/* Section Grid */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        {sections.map((section) => (
          <SectionCard key={section.href} {...section} />
        ))}
      </div>
    </div>
  );
}
