'use client';

import { useState, useEffect, useCallback } from 'react';
import Link from 'next/link';
import { ScrollText, RefreshCw, AlertCircle, Search, ExternalLink, FileText } from 'lucide-react';
import { Card, CardHeader, CardTitle, CardDescription, CardContent } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { AdminBreadcrumb } from '@/components/admin/AdminBreadcrumb';

const breadcrumbs = [
  { label: 'Admin', href: '/admin' },
  { label: 'Factbase' },
  { label: 'Federal Laws & Regulations' },
  { label: 'Regulations' },
];

interface RegulationStats {
  totalRegulations: number;
  byDocumentType: Record<string, number>;
  agenciesLinked: number;
  lastImportDate: string | null;
}

export default function FederalRegisterPage() {
  const [stats, setStats] = useState<RegulationStats | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const fetchData = useCallback(async () => {
    try {
      setLoading(true);
      setError(null);

      // Fetch regulation statistics
      const response = await fetch('/api/regulations/stats');
      if (response.ok) {
        const data = await response.json();
        setStats(data);
      } else if (response.status === 404) {
        // No regulations imported yet
        setStats({
          totalRegulations: 0,
          byDocumentType: {},
          agenciesLinked: 0,
          lastImportDate: null,
        });
      }
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to load data');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchData();
  }, [fetchData]);

  return (
    <main className="container mx-auto py-8 px-4">
      <AdminBreadcrumb items={breadcrumbs} />

      <div className="flex items-center justify-between mb-6">
        <div className="flex items-center gap-3">
          <ScrollText className="h-8 w-8 text-primary" />
          <h1 className="text-3xl font-bold">Federal Register</h1>
        </div>
        <Button
          variant="outline"
          size="sm"
          onClick={fetchData}
          disabled={loading}
        >
          <RefreshCw className={`h-4 w-4 mr-2 ${loading ? 'animate-spin' : ''}`} />
          Refresh
        </Button>
      </div>

      <p className="text-muted-foreground mb-8">
        The Federal Register is the official daily publication for rules, proposed rules, and notices
        of Federal agencies and organizations. Search and import regulations to track regulatory activity.
      </p>

      {/* Error State */}
      {error && (
        <Card className="border-destructive mb-6">
          <CardContent className="py-4">
            <div className="flex items-center gap-2 text-destructive">
              <AlertCircle className="h-5 w-5" />
              <span>{error}</span>
            </div>
          </CardContent>
        </Card>
      )}

      {/* Stats Cards */}
      <div className="grid gap-6 md:grid-cols-3 mb-8">
        <Card>
          <CardHeader className="pb-2">
            <CardTitle className="text-sm font-medium text-muted-foreground">
              Total Regulations
            </CardTitle>
          </CardHeader>
          <CardContent>
            <div className="text-3xl font-bold">
              {loading ? '...' : stats?.totalRegulations.toLocaleString() ?? 0}
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="pb-2">
            <CardTitle className="text-sm font-medium text-muted-foreground">
              Agencies Linked
            </CardTitle>
          </CardHeader>
          <CardContent>
            <div className="text-3xl font-bold">
              {loading ? '...' : stats?.agenciesLinked ?? 0}
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="pb-2">
            <CardTitle className="text-sm font-medium text-muted-foreground">
              Last Import
            </CardTitle>
          </CardHeader>
          <CardContent>
            <div className="text-xl font-bold">
              {loading ? '...' : stats?.lastImportDate
                ? new Date(stats.lastImportDate).toLocaleDateString()
                : 'Never'}
            </div>
          </CardContent>
        </Card>
      </div>

      {/* Document Types Breakdown */}
      {stats && stats.byDocumentType && Object.keys(stats.byDocumentType).length > 0 && (
        <Card className="mb-8">
          <CardHeader>
            <CardTitle>By Document Type</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
              {Object.entries(stats.byDocumentType).map(([type, count]) => (
                <div key={type} className="p-3 bg-muted rounded-lg">
                  <div className="text-2xl font-bold">{count.toLocaleString()}</div>
                  <div className="text-sm text-muted-foreground">{type}</div>
                </div>
              ))}
            </div>
          </CardContent>
        </Card>
      )}

      {/* Actions */}
      <div className="grid gap-6 md:grid-cols-2">
        {/* Search Card */}
        <Link href="/admin/factbase/regulations/search">
          <Card className="h-full hover:border-primary transition-colors cursor-pointer">
            <CardHeader>
              <div className="flex items-center gap-3">
                <Search className="h-6 w-6 text-primary" />
                <CardTitle>Search Federal Register</CardTitle>
              </div>
              <CardDescription>
                Search the Federal Register API and import documents
              </CardDescription>
            </CardHeader>
            <CardContent>
              <ul className="text-sm text-muted-foreground space-y-1">
                <li>• Search by keyword, agency, or date</li>
                <li>• Filter by document type (Rule, Notice, etc.)</li>
                <li>• Import documents with automatic agency linkage</li>
              </ul>
            </CardContent>
          </Card>
        </Link>

        {/* Browse/View Card */}
        <Card className="h-full">
          <CardHeader>
            <div className="flex items-center gap-3">
              <FileText className="h-6 w-6 text-primary" />
              <CardTitle>Imported Regulations</CardTitle>
            </div>
            <CardDescription>
              Browse and manage imported Federal Register documents
            </CardDescription>
          </CardHeader>
          <CardContent>
            {stats?.totalRegulations === 0 ? (
              <p className="text-sm text-muted-foreground">
                No regulations imported yet. Use the search feature to find and import documents.
              </p>
            ) : (
              <p className="text-sm text-muted-foreground">
                {stats?.totalRegulations.toLocaleString()} regulations in database.
                Browse functionality coming in future release.
              </p>
            )}
          </CardContent>
        </Card>
      </div>

      {/* External Links */}
      <div className="mt-8 flex gap-4 text-sm">
        <a
          href="https://www.federalregister.gov/"
          target="_blank"
          rel="noopener noreferrer"
          className="flex items-center gap-1 text-primary hover:underline"
        >
          <ExternalLink className="h-4 w-4" />
          Federal Register Website
        </a>
        <a
          href="https://www.federalregister.gov/developers/documentation/api/v1"
          target="_blank"
          rel="noopener noreferrer"
          className="flex items-center gap-1 text-primary hover:underline"
        >
          <ExternalLink className="h-4 w-4" />
          Federal Register API
        </a>
      </div>
    </main>
  );
}
