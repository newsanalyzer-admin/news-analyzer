'use client';

import Link from 'next/link';
import { Briefcase, ExternalLink, ArrowLeft } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { KBBreadcrumbs, HierarchyView } from '@/components/knowledge-base';
import { useGovernmentOrgsHierarchy } from '@/hooks/useGovernmentOrgs';
import type { HierarchyConfig } from '@/lib/config/entityTypes';

/**
 * Hierarchy configuration for Cabinet departments
 */
const cabinetHierarchyConfig: HierarchyConfig = {
  labelField: 'officialName',
  metaFields: ['acronym'],
  childrenField: 'children',
  idField: 'id',
  defaultExpandDepth: 1,
  showChildCount: true,
};

/**
 * The 15 Cabinet-level executive departments in order of succession
 */
const cabinetDepartments = [
  { name: 'Department of State', year: 1789 },
  { name: 'Department of the Treasury', year: 1789 },
  { name: 'Department of Defense', year: 1947 },
  { name: 'Department of Justice', year: 1870 },
  { name: 'Department of the Interior', year: 1849 },
  { name: 'Department of Agriculture', year: 1889 },
  { name: 'Department of Commerce', year: 1913 },
  { name: 'Department of Labor', year: 1913 },
  { name: 'Department of Health and Human Services', year: 1953 },
  { name: 'Department of Housing and Urban Development', year: 1965 },
  { name: 'Department of Transportation', year: 1967 },
  { name: 'Department of Energy', year: 1977 },
  { name: 'Department of Education', year: 1979 },
  { name: 'Department of Veterans Affairs', year: 1989 },
  { name: 'Department of Homeland Security', year: 2002 },
];

/**
 * Cabinet Departments page (UI-6.3).
 *
 * Shows the 15 executive departments that make up the President's Cabinet,
 * each headed by a Secretary who serves as a principal advisor.
 */
export default function CabinetPage() {
  // Fetch executive branch organizations
  const { data, isLoading, error, refetch } = useGovernmentOrgsHierarchy('executive');

  // Filter to show only Cabinet-level departments (orgType = department, orgLevel = 1)
  const cabinetData = data?.filter((org) => {
    const name = org.officialName?.toLowerCase() || '';
    return name.startsWith('department of') || name === 'department of defense';
  }) || [];

  return (
    <div className="container py-8">
      {/* Breadcrumbs */}
      <KBBreadcrumbs className="mb-6" />

      {/* Back link */}
      <div className="mb-6">
        <Button variant="ghost" size="sm" asChild className="-ml-2">
          <Link href="/knowledge-base/government/executive">
            <ArrowLeft className="h-4 w-4 mr-2" />
            Back to Executive Branch
          </Link>
        </Button>
      </div>

      {/* Header */}
      <div className="mb-8">
        <div className="flex items-center gap-3 mb-4">
          <div className="p-3 rounded-lg bg-purple-500/10 text-purple-600 dark:text-purple-400">
            <Briefcase className="h-8 w-8" />
          </div>
          <h1 className="text-3xl font-bold">Cabinet Departments</h1>
        </div>

        <p className="text-lg text-muted-foreground mb-4">
          The Cabinet includes the Vice President and the heads of 15 executive departments.
          Cabinet members are nominated by the President and must be confirmed by the Senate.
          They serve at the pleasure of the President and advise on policy matters.
        </p>

        <div className="bg-muted/50 border rounded-lg p-4 mb-6">
          <p className="text-sm text-muted-foreground">
            <strong>Line of Succession:</strong> Cabinet members are in the presidential
            line of succession after the Vice President and Speaker of the House, in the
            order their departments were created.
          </p>
        </div>
      </div>

      {/* Organizations Hierarchy or Static List */}
      <div className="mb-8">
        <h2 className="text-xl font-semibold mb-4">The 15 Executive Departments</h2>

        {cabinetData.length > 0 ? (
          <HierarchyView
            data={cabinetData}
            config={cabinetHierarchyConfig}
            entityType="organizations"
            isLoading={isLoading}
            error={error?.message}
            onRetry={() => refetch()}
          />
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
            {cabinetDepartments.map((dept) => (
              <div
                key={dept.name}
                className="bg-card border rounded-lg p-4 hover:border-purple-500 transition-colors"
              >
                <h3 className="font-medium mb-1">{dept.name}</h3>
                <p className="text-sm text-muted-foreground">Established {dept.year}</p>
              </div>
            ))}
          </div>
        )}
      </div>

      {/* External Links */}
      <div className="bg-muted/50 border rounded-lg p-6">
        <h2 className="text-xl font-semibold mb-4">Official Resources</h2>
        <div className="flex flex-wrap gap-4">
          <a
            href="https://www.whitehouse.gov/administration/cabinet/"
            target="_blank"
            rel="noopener noreferrer"
            className="inline-flex items-center gap-2 text-primary hover:underline"
          >
            The Cabinet - White House
            <ExternalLink className="h-4 w-4" />
          </a>
          <a
            href="https://www.usa.gov/federal-agencies"
            target="_blank"
            rel="noopener noreferrer"
            className="inline-flex items-center gap-2 text-primary hover:underline"
          >
            A-Z Index of Federal Agencies
            <ExternalLink className="h-4 w-4" />
          </a>
        </div>
      </div>
    </div>
  );
}
