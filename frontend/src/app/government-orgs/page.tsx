'use client';

import { useState, useEffect } from 'react';
import Link from 'next/link';

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

interface BranchData {
  name: string;
  displayName: string;
  organizations: GovernmentOrganization[];
  expanded: boolean;
}

export default function GovernmentOrganizationsPage() {
  const [branches, setBranches] = useState<BranchData[]>([
    { name: 'executive', displayName: 'EXECUTIVE', organizations: [], expanded: true },
    { name: 'legislative', displayName: 'LEGISLATIVE', organizations: [], expanded: false },
    { name: 'judicial', displayName: 'JUDICIAL', organizations: [], expanded: false },
  ]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [expandedOrgs, setExpandedOrgs] = useState<Set<string>>(new Set());

  useEffect(() => {
    fetchOrganizations();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const fetchOrganizations = async () => {
    try {
      setLoading(true);
      const response = await fetch('/api/government-organizations', {
        credentials: 'include',
      });

      if (!response.ok) {
        throw new Error('Failed to fetch government organizations');
      }

      const data = await response.json();
      const orgs: GovernmentOrganization[] = data.content || [];

      // Group by branch
      const updatedBranches = branches.map(branch => ({
        ...branch,
        organizations: orgs.filter(org => org.branch === branch.name),
      }));

      setBranches(updatedBranches);
      setError(null);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'An error occurred');
    } finally {
      setLoading(false);
    }
  };

  const toggleBranch = (branchName: string) => {
    setBranches(branches.map(branch =>
      branch.name === branchName ? { ...branch, expanded: !branch.expanded } : branch
    ));
  };

  const toggleOrganization = (orgId: string) => {
    const newExpanded = new Set(expandedOrgs);
    if (newExpanded.has(orgId)) {
      newExpanded.delete(orgId);
    } else {
      newExpanded.add(orgId);
    }
    setExpandedOrgs(newExpanded);
  };

  const buildHierarchy = (orgs: GovernmentOrganization[], parentId: string | null = null): GovernmentOrganization[] => {
    return orgs.filter(org => org.parentId === parentId);
  };

  const renderOrganization = (org: GovernmentOrganization, allOrgs: GovernmentOrganization[], depth: number = 0) => {
    const children = buildHierarchy(allOrgs, org.id);
    const hasChildren = children.length > 0;
    const isExpanded = expandedOrgs.has(org.id);

    return (
      <div key={org.id}>
        <div
          className={`border-b hover:bg-gray-50 cursor-pointer ${depth > 0 ? 'bg-gray-50' : ''}`}
          onClick={() => hasChildren && toggleOrganization(org.id)}
          style={{ paddingLeft: `${depth * 2}rem` }}
        >
          <div className="grid grid-cols-12 gap-4 p-4">
            {/* Name column with expand/collapse indicator */}
            <div className="col-span-4 flex items-center">
              {hasChildren && (
                <span className="mr-2 text-gray-400">
                  {isExpanded ? '▼' : '▶'}
                </span>
              )}
              {!hasChildren && depth > 0 && (
                <span className="mr-2 text-gray-300">•</span>
              )}
              <div>
                <div className="font-medium">{org.officialName}</div>
                {org.acronym && (
                  <div className="text-sm text-gray-500">{org.acronym}</div>
                )}
              </div>
            </div>

            {/* Type */}
            <div className="col-span-2 flex items-center">
              <span className="px-2 py-1 bg-blue-100 text-blue-800 rounded text-xs">
                {org.orgType.replace('_', ' ')}
              </span>
            </div>

            {/* Level */}
            <div className="col-span-1 flex items-center text-gray-600">
              Level {org.orgLevel}
            </div>

            {/* Established */}
            <div className="col-span-2 flex items-center text-sm text-gray-600">
              {org.establishedDate ? new Date(org.establishedDate).getFullYear() : 'N/A'}
            </div>

            {/* Jurisdiction Areas */}
            <div className="col-span-2 flex items-center text-sm">
              {org.jurisdictionAreas && org.jurisdictionAreas.length > 0 ? (
                <span className="text-gray-600">
                  {org.jurisdictionAreas.length} area{org.jurisdictionAreas.length > 1 ? 's' : ''}
                </span>
              ) : (
                <span className="text-gray-400">None</span>
              )}
            </div>

            {/* Website */}
            <div className="col-span-1 flex items-center justify-end">
              {org.websiteUrl && (
                <a
                  href={org.websiteUrl}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="text-blue-600 hover:text-blue-800"
                  onClick={(e) => e.stopPropagation()}
                >
                  🔗
                </a>
              )}
            </div>
          </div>
        </div>

        {/* Render children if expanded */}
        {hasChildren && isExpanded && (
          <div>
            {children.map(child => renderOrganization(child, allOrgs, depth + 1))}
          </div>
        )}
      </div>
    );
  };

  return (
    <main className="min-h-screen p-8">
      <div className="max-w-7xl mx-auto">
        {/* Header */}
        <div className="mb-8">
          <Link href="/" className="text-blue-600 hover:text-blue-800 mb-4 inline-block">
            ← Back to Home
          </Link>
          <h1 className="text-4xl font-bold mb-2">US Government Organizations</h1>
          <p className="text-gray-600">
            Hierarchical view of government entities organized by branch
          </p>
        </div>

        {/* Loading State */}
        {loading && (
          <div className="text-center py-12">
            <div className="text-gray-600">Loading organizations...</div>
          </div>
        )}

        {/* Error State */}
        {error && (
          <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded mb-4">
            Error: {error}
          </div>
        )}

        {/* Table */}
        {!loading && !error && (
          <div className="bg-white rounded-lg shadow">
            {/* Table Header */}
            <div className="grid grid-cols-12 gap-4 p-4 bg-gray-100 font-semibold border-b">
              <div className="col-span-4">Organization Name</div>
              <div className="col-span-2">Type</div>
              <div className="col-span-1">Level</div>
              <div className="col-span-2">Established</div>
              <div className="col-span-2">Jurisdiction</div>
              <div className="col-span-1 text-right">Link</div>
            </div>

            {/* Branches */}
            {branches.map(branch => (
              <div key={branch.name}>
                {/* Branch Header */}
                <div
                  className="bg-gray-200 p-4 cursor-pointer hover:bg-gray-300 border-b-2 border-gray-400"
                  onClick={() => toggleBranch(branch.name)}
                >
                  <div className="flex items-center justify-between">
                    <div className="flex items-center">
                      <span className="mr-3 text-gray-600">
                        {branch.expanded ? '▼' : '▶'}
                      </span>
                      <h2 className="text-xl font-bold">
                        {branch.displayName} Branch
                      </h2>
                      <span className="ml-4 px-3 py-1 bg-gray-700 text-white rounded-full text-sm">
                        {branch.organizations.length} org{branch.organizations.length !== 1 ? 's' : ''}
                      </span>
                    </div>
                  </div>
                </div>

                {/* Branch Organizations */}
                {branch.expanded && (
                  <div>
                    {branch.organizations.length === 0 ? (
                      <div className="p-8 text-center text-gray-500">
                        No organizations found for this branch
                      </div>
                    ) : (
                      buildHierarchy(branch.organizations).map(org =>
                        renderOrganization(org, branch.organizations)
                      )
                    )}
                  </div>
                )}
              </div>
            ))}
          </div>
        )}

        {/* Summary Statistics */}
        {!loading && !error && (
          <div className="mt-8 grid grid-cols-3 gap-4">
            {branches.map(branch => (
              <div key={branch.name} className="bg-white p-6 rounded-lg shadow">
                <div className="text-2xl font-bold mb-2">
                  {branch.organizations.length}
                </div>
                <div className="text-gray-600">
                  {branch.displayName.charAt(0) + branch.displayName.slice(1).toLowerCase()} Branch
                </div>
                <div className="text-sm text-gray-500 mt-1">
                  {branch.organizations.filter(o => o.orgLevel === 1).length} top-level
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </main>
  );
}
