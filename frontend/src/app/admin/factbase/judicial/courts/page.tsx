'use client';

import { useState, useEffect, useCallback } from 'react';
import Link from 'next/link';
import { Scale, ChevronRight, ChevronDown, ExternalLink, Building, AlertCircle, RefreshCw } from 'lucide-react';
import { Card, CardHeader, CardTitle, CardContent } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { AdminBreadcrumb } from '@/components/admin/AdminBreadcrumb';

const breadcrumbs = [
  { label: 'Admin', href: '/admin' },
  { label: 'Factbase' },
  { label: 'Judicial', href: '/admin/factbase/judicial' },
  { label: 'Courts' },
];

interface GovernmentOrganization {
  id: string;
  officialName: string;
  acronym: string | null;
  orgType: string;
  branch: string;
  parentId: string | null;
  orgLevel: number;
  establishedDate: string | null;
  dissolvedDate: string | null;
  websiteUrl: string | null;
  jurisdictionAreas: string[] | null;
  active: boolean;
}

export default function CourtsPage() {
  const [organizations, setOrganizations] = useState<GovernmentOrganization[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [expandedOrgs, setExpandedOrgs] = useState<Set<string>>(new Set());

  const fetchOrganizations = useCallback(async () => {
    try {
      setLoading(true);
      setError(null);
      const response = await fetch('/api/government-organizations/by-branch?branch=judicial');

      if (!response.ok) {
        throw new Error('Failed to fetch judicial organizations');
      }

      const data: GovernmentOrganization[] = await response.json();
      setOrganizations(data);

      // Auto-expand top-level orgs
      const topLevel = data.filter(org => !org.parentId);
      setExpandedOrgs(new Set(topLevel.map(org => org.id)));
    } catch (err) {
      setError(err instanceof Error ? err.message : 'An error occurred');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchOrganizations();
  }, [fetchOrganizations]);

  const toggleOrganization = (orgId: string) => {
    setExpandedOrgs(prev => {
      const newSet = new Set(prev);
      if (newSet.has(orgId)) {
        newSet.delete(orgId);
      } else {
        newSet.add(orgId);
      }
      return newSet;
    });
  };

  const getChildren = (parentId: string | null): GovernmentOrganization[] => {
    return organizations.filter(org => org.parentId === parentId);
  };

  const renderOrganization = (org: GovernmentOrganization, depth: number = 0) => {
    const children = getChildren(org.id);
    const hasChildren = children.length > 0;
    const isExpanded = expandedOrgs.has(org.id);

    return (
      <div key={org.id}>
        <div
          className={`
            border-b border-border hover:bg-muted/50 transition-colors
            ${hasChildren ? 'cursor-pointer' : ''}
            ${depth > 0 ? 'bg-muted/30' : ''}
          `}
          onClick={() => hasChildren && toggleOrganization(org.id)}
          style={{ paddingLeft: `${depth * 1.5 + 1}rem` }}
        >
          <div className="flex items-center justify-between py-3 pr-4">
            <div className="flex items-center gap-2 flex-1 min-w-0">
              {hasChildren ? (
                isExpanded ? (
                  <ChevronDown className="h-4 w-4 text-muted-foreground shrink-0" />
                ) : (
                  <ChevronRight className="h-4 w-4 text-muted-foreground shrink-0" />
                )
              ) : (
                <span className="w-4 shrink-0" />
              )}
              <div className="min-w-0">
                <div className="font-medium truncate">{org.officialName}</div>
                {org.acronym && (
                  <div className="text-sm text-muted-foreground">{org.acronym}</div>
                )}
              </div>
            </div>

            <div className="flex items-center gap-4 shrink-0">
              <span className="px-2 py-1 bg-primary/10 text-primary rounded text-xs">
                {org.orgType.replace(/_/g, ' ')}
              </span>
              <span className="text-sm text-muted-foreground w-16 text-center">
                Level {org.orgLevel}
              </span>
              {org.websiteUrl && (
                <a
                  href={org.websiteUrl}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="text-primary hover:text-primary/80"
                  onClick={(e) => e.stopPropagation()}
                  aria-label={`Visit ${org.officialName} website`}
                >
                  <ExternalLink className="h-4 w-4" />
                </a>
              )}
            </div>
          </div>
        </div>

        {hasChildren && isExpanded && (
          <div>
            {children.map(child => renderOrganization(child, depth + 1))}
          </div>
        )}
      </div>
    );
  };

  const topLevelOrgs = getChildren(null);

  return (
    <main className="container mx-auto py-8 px-4">
      <AdminBreadcrumb items={breadcrumbs} />

      <div className="flex items-center justify-between mb-6">
        <div className="flex items-center gap-3">
          <Scale className="h-8 w-8 text-primary" />
          <h1 className="text-3xl font-bold">Federal Courts</h1>
        </div>
        <Button
          variant="outline"
          size="sm"
          onClick={fetchOrganizations}
          disabled={loading}
        >
          <RefreshCw className={`h-4 w-4 mr-2 ${loading ? 'animate-spin' : ''}`} />
          Refresh
        </Button>
      </div>

      <p className="text-muted-foreground mb-8">
        The federal court system includes the Supreme Court of the United States, Courts of Appeals,
        District Courts, and various specialized courts. These organizations are imported from the GOVMAN XML.
      </p>

      {/* Loading State */}
      {loading && (
        <Card>
          <CardContent className="py-12">
            <div className="flex flex-col items-center justify-center text-muted-foreground">
              <RefreshCw className="h-8 w-8 animate-spin mb-4" />
              <p>Loading judicial organizations...</p>
            </div>
          </CardContent>
        </Card>
      )}

      {/* Error State */}
      {error && !loading && (
        <Card className="border-destructive">
          <CardContent className="py-8">
            <div className="flex flex-col items-center justify-center text-destructive">
              <AlertCircle className="h-8 w-8 mb-4" />
              <p className="mb-4">{error}</p>
              <Button variant="outline" onClick={fetchOrganizations}>
                Try Again
              </Button>
            </div>
          </CardContent>
        </Card>
      )}

      {/* Empty State */}
      {!loading && !error && organizations.length === 0 && (
        <Card>
          <CardContent className="py-12">
            <div className="flex flex-col items-center justify-center text-center">
              <Building className="h-12 w-12 text-muted-foreground mb-4" />
              <h3 className="text-lg font-semibold mb-2">No judicial organizations found</h3>
              <p className="text-muted-foreground mb-6 max-w-md">
                Import from GOVMAN to populate judicial branch organizations including the Supreme Court,
                Courts of Appeals, and District Courts.
              </p>
              <Link href="/admin/factbase/executive/govman">
                <Button>
                  Go to GOVMAN Import
                </Button>
              </Link>
            </div>
          </CardContent>
        </Card>
      )}

      {/* Organizations List */}
      {!loading && !error && organizations.length > 0 && (
        <Card>
          <CardHeader className="pb-0">
            <CardTitle className="flex items-center justify-between">
              <span>Judicial Organizations</span>
              <span className="text-sm font-normal text-muted-foreground">
                {organizations.length} organization{organizations.length !== 1 ? 's' : ''}
              </span>
            </CardTitle>
          </CardHeader>
          <CardContent className="p-0 mt-4">
            {/* Table Header */}
            <div className="grid grid-cols-12 gap-4 px-4 py-2 bg-muted font-medium text-sm border-y border-border">
              <div className="col-span-6">Organization</div>
              <div className="col-span-3 text-center">Type</div>
              <div className="col-span-2 text-center">Level</div>
              <div className="col-span-1 text-center">Link</div>
            </div>

            {/* Organizations */}
            <div className="divide-y divide-border">
              {topLevelOrgs.map(org => renderOrganization(org))}
            </div>
          </CardContent>
        </Card>
      )}
    </main>
  );
}
