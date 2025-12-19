'use client';

/**
 * BranchOrgsPage Component
 *
 * Shared component for displaying government organizations by branch.
 */

import { useState, useMemo } from 'react';
import { ChevronRight, ChevronDown, ExternalLink } from 'lucide-react';
import { ContentPageHeader, type BreadcrumbItem } from '@/components/public';
import { useGovernmentOrgsByBranch } from '@/hooks/useGovernmentOrgs';
import { OrgDetailPanel } from './OrgDetailPanel';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { Input } from '@/components/ui/input';
import { Skeleton } from '@/components/ui/skeleton';
import { useDebounce } from '@/hooks/useDebounce';
import type { GovernmentOrganization, GovernmentBranch } from '@/types/government-org';

interface BranchOrgsPageProps {
  branch: GovernmentBranch;
  title: string;
  description: string;
  breadcrumbs: BreadcrumbItem[];
}

const orgTypeColors: Record<string, string> = {
  DEPARTMENT: 'bg-blue-100 text-blue-800',
  AGENCY: 'bg-green-100 text-green-800',
  BUREAU: 'bg-amber-100 text-amber-800',
  OFFICE: 'bg-purple-100 text-purple-800',
  COMMISSION: 'bg-red-100 text-red-800',
  COURT: 'bg-indigo-100 text-indigo-800',
  COMMITTEE: 'bg-orange-100 text-orange-800',
};

export function BranchOrgsPage({ branch, title, description, breadcrumbs }: BranchOrgsPageProps) {
  const [searchInput, setSearchInput] = useState('');
  const [expandedOrgs, setExpandedOrgs] = useState<Set<string>>(new Set());
  const [selectedOrg, setSelectedOrg] = useState<GovernmentOrganization | null>(null);

  const debouncedSearch = useDebounce(searchInput, 300);

  const { data: organizations, isLoading, error, refetch } = useGovernmentOrgsByBranch(branch);

  // Filter organizations by search
  const filteredOrgs = useMemo(() => {
    if (!organizations) return [];
    if (!debouncedSearch) return organizations;

    const searchLower = debouncedSearch.toLowerCase();
    return organizations.filter(
      (org) =>
        org.officialName.toLowerCase().includes(searchLower) ||
        (org.acronym && org.acronym.toLowerCase().includes(searchLower))
    );
  }, [organizations, debouncedSearch]);

  // Build hierarchy from flat list
  const buildHierarchy = (orgs: GovernmentOrganization[], parentId: string | null = null): GovernmentOrganization[] => {
    return orgs.filter((org) => org.parentId === parentId);
  };

  const toggleOrg = (orgId: string) => {
    const newExpanded = new Set(expandedOrgs);
    if (newExpanded.has(orgId)) {
      newExpanded.delete(orgId);
    } else {
      newExpanded.add(orgId);
    }
    setExpandedOrgs(newExpanded);
  };

  const handleClearSearch = () => {
    setSearchInput('');
  };

  // Render a single organization row with its children
  const renderOrg = (org: GovernmentOrganization, allOrgs: GovernmentOrganization[], depth: number = 0) => {
    const children = buildHierarchy(allOrgs, org.id);
    const hasChildren = children.length > 0;
    const isExpanded = expandedOrgs.has(org.id);
    const typeColor = orgTypeColors[org.orgType] || 'bg-gray-100 text-gray-800';

    return (
      <div key={org.id}>
        <div
          className={`border-b hover:bg-muted/50 cursor-pointer transition-colors ${
            depth > 0 ? 'bg-muted/20' : ''
          }`}
          style={{ paddingLeft: `${depth * 1.5 + 1}rem` }}
        >
          <div className="flex items-center gap-4 py-3 pr-4">
            {/* Expand/Collapse */}
            <div className="w-5 flex-shrink-0">
              {hasChildren && (
                <button
                  onClick={(e) => {
                    e.stopPropagation();
                    toggleOrg(org.id);
                  }}
                  className="text-muted-foreground hover:text-foreground"
                >
                  {isExpanded ? (
                    <ChevronDown className="h-4 w-4" />
                  ) : (
                    <ChevronRight className="h-4 w-4" />
                  )}
                </button>
              )}
              {!hasChildren && depth > 0 && (
                <span className="text-muted-foreground/50">•</span>
              )}
            </div>

            {/* Name and Acronym */}
            <div
              className="flex-1 min-w-0"
              onClick={() => setSelectedOrg(org)}
            >
              <div className="font-medium truncate">{org.officialName}</div>
              {org.acronym && (
                <div className="text-sm text-muted-foreground">{org.acronym}</div>
              )}
            </div>

            {/* Type Badge */}
            <Badge className={typeColor} variant="outline">
              {org.orgType.replace(/_/g, ' ')}
            </Badge>

            {/* Level */}
            <div className="text-sm text-muted-foreground w-16 text-right">
              Level {org.orgLevel}
            </div>

            {/* Website */}
            <div className="w-8">
              {org.websiteUrl && (
                <a
                  href={org.websiteUrl}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="text-primary hover:text-primary/80"
                  onClick={(e) => e.stopPropagation()}
                >
                  <ExternalLink className="h-4 w-4" />
                </a>
              )}
            </div>
          </div>
        </div>

        {/* Children */}
        {hasChildren && isExpanded && (
          <div>
            {children.map((child) => renderOrg(child, allOrgs, depth + 1))}
          </div>
        )}
      </div>
    );
  };

  return (
    <div className="container mx-auto px-6 py-8 max-w-7xl">
      <ContentPageHeader
        title={title}
        description={description}
        breadcrumbs={breadcrumbs}
      />

      {/* Search */}
      <div className="flex gap-4 mb-6">
        <div className="flex-1 max-w-md">
          <Input
            type="text"
            placeholder="Search organizations..."
            value={searchInput}
            onChange={(e) => setSearchInput(e.target.value)}
            className="w-full"
          />
        </div>
        {searchInput && (
          <Button variant="outline" onClick={handleClearSearch}>
            Clear
          </Button>
        )}
      </div>

      {/* Loading State */}
      {isLoading && <OrgTableSkeleton />}

      {/* Error State */}
      {error && !isLoading && (
        <div className="flex flex-col items-center justify-center py-12 text-center border rounded-lg">
          <div className="text-4xl mb-4">⚠️</div>
          <h3 className="text-lg font-semibold mb-2">Failed to load organizations</h3>
          <p className="text-muted-foreground mb-4">{(error as Error).message}</p>
          <Button onClick={() => refetch()}>Try Again</Button>
        </div>
      )}

      {/* Empty State */}
      {!isLoading && !error && filteredOrgs.length === 0 && (
        <div className="flex flex-col items-center justify-center py-12 text-center border rounded-lg">
          <div className="text-4xl mb-4">🔍</div>
          <h3 className="text-lg font-semibold mb-2">No organizations found</h3>
          <p className="text-muted-foreground">
            {searchInput
              ? 'Try adjusting your search criteria.'
              : `No ${branch} branch organizations have been imported yet.`}
          </p>
        </div>
      )}

      {/* Organizations Tree */}
      {!isLoading && !error && filteredOrgs.length > 0 && (
        <div className="border rounded-lg overflow-hidden">
          {/* Header */}
          <div className="bg-muted/50 border-b px-4 py-3 font-medium flex items-center gap-4">
            <div className="w-5"></div>
            <div className="flex-1">Organization</div>
            <div className="w-24">Type</div>
            <div className="w-16 text-right">Level</div>
            <div className="w-8">Link</div>
          </div>

          {/* Organizations */}
          <div>
            {buildHierarchy(filteredOrgs).map((org) =>
              renderOrg(org, filteredOrgs)
            )}
          </div>

          {/* Footer with count */}
          <div className="bg-muted/30 border-t px-4 py-2 text-sm text-muted-foreground">
            {filteredOrgs.length} organization{filteredOrgs.length !== 1 ? 's' : ''}
            {searchInput && ` matching "${searchInput}"`}
          </div>
        </div>
      )}

      {/* Detail Panel */}
      {selectedOrg && (
        <OrgDetailPanel org={selectedOrg} onClose={() => setSelectedOrg(null)} />
      )}
    </div>
  );
}

function OrgTableSkeleton() {
  return (
    <div className="border rounded-lg">
      <div className="bg-muted/50 border-b px-4 py-3">
        <div className="flex gap-4">
          <Skeleton className="h-5 w-5" />
          <Skeleton className="h-5 w-64" />
          <Skeleton className="h-5 w-20" />
          <Skeleton className="h-5 w-12" />
          <Skeleton className="h-5 w-8" />
        </div>
      </div>
      {Array.from({ length: 10 }).map((_, i) => (
        <div key={i} className="border-b px-4 py-3 flex items-center gap-4">
          <Skeleton className="h-4 w-4" />
          <Skeleton className="h-5 w-48" />
          <Skeleton className="h-6 w-20" />
          <Skeleton className="h-5 w-12" />
          <Skeleton className="h-4 w-4" />
        </div>
      ))}
    </div>
  );
}
