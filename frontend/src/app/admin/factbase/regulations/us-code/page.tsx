'use client';

/**
 * US Code Admin Page
 *
 * Admin page for managing US Code statute imports and browsing imported titles
 * with hierarchical tree view (Title -> Chapter -> Section).
 */

import { useState, useEffect, useCallback } from 'react';
import { BookOpen, RefreshCw, AlertCircle, ExternalLink, FileText, ChevronRight, ChevronDown } from 'lucide-react';
import { Card, CardHeader, CardTitle, CardDescription, CardContent } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { AdminBreadcrumb } from '@/components/admin/AdminBreadcrumb';
import { UsCodeImportButton } from '@/components/admin/UsCodeImportButton';
import { UsCodeTreeView } from '@/components/admin/UsCodeTreeView';

const breadcrumbs = [
  { label: 'Admin', href: '/admin' },
  { label: 'Factbase' },
  { label: 'Federal Laws & Regulations' },
  { label: 'US Code' },
];

interface StatuteStats {
  totalSections: number;
  titlesLoaded: number;
  latestReleasePoint: string | null;
}

interface TitleInfo {
  titleNumber: number;
  titleName: string;
  sectionCount: number;
}

export default function UsCodePage() {
  const [stats, setStats] = useState<StatuteStats | null>(null);
  const [titles, setTitles] = useState<TitleInfo[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [expandedTitle, setExpandedTitle] = useState<number | null>(null);

  const API_BASE = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080';

  const fetchData = useCallback(async () => {
    try {
      setLoading(true);
      setError(null);

      const [statsRes, titlesRes] = await Promise.all([
        fetch(`${API_BASE}/api/statutes/stats`),
        fetch(`${API_BASE}/api/statutes/titles`),
      ]);

      if (statsRes.ok) {
        setStats(await statsRes.json());
      }
      if (titlesRes.ok) {
        setTitles(await titlesRes.json());
      }
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to load data');
    } finally {
      setLoading(false);
    }
  }, [API_BASE]);

  useEffect(() => {
    fetchData();
  }, [fetchData]);

  const toggleTitle = (titleNumber: number) => {
    if (expandedTitle === titleNumber) {
      setExpandedTitle(null);
    } else {
      setExpandedTitle(titleNumber);
    }
  };

  return (
    <main className="container mx-auto py-8 px-4">
      <AdminBreadcrumb items={breadcrumbs} />

      <div className="flex items-center justify-between mb-6">
        <div className="flex items-center gap-3">
          <BookOpen className="h-8 w-8 text-primary" />
          <h1 className="text-3xl font-bold">US Code</h1>
        </div>
        <div className="flex gap-2">
          <Button
            variant="outline"
            size="sm"
            onClick={fetchData}
            disabled={loading}
          >
            <RefreshCw className={`h-4 w-4 mr-2 ${loading ? 'animate-spin' : ''}`} />
            Refresh
          </Button>
          <UsCodeImportButton />
        </div>
      </div>

      <p className="text-muted-foreground mb-8">
        The United States Code is the codification of federal statutory law. Upload XML files
        from uscode.house.gov one title at a time for verification and browse statutes in a hierarchical tree view.
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
              Total Sections
            </CardTitle>
          </CardHeader>
          <CardContent>
            <div className="text-3xl font-bold">
              {loading ? '...' : stats?.totalSections.toLocaleString() ?? 0}
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="pb-2">
            <CardTitle className="text-sm font-medium text-muted-foreground">
              Titles Loaded
            </CardTitle>
          </CardHeader>
          <CardContent>
            <div className="text-3xl font-bold">
              {loading ? '...' : stats?.titlesLoaded ?? 0}
            </div>
            <p className="text-xs text-muted-foreground mt-1">of 54 total titles</p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="pb-2">
            <CardTitle className="text-sm font-medium text-muted-foreground">
              Import Source
            </CardTitle>
          </CardHeader>
          <CardContent>
            <div className="text-xl font-bold">
              {loading ? '...' : stats?.latestReleasePoint ?? 'No imports yet'}
            </div>
            <p className="text-xs text-muted-foreground mt-1">
              Upload XML files from uscode.house.gov
            </p>
          </CardContent>
        </Card>
      </div>

      {/* How to Import Instructions */}
      <Card className="mb-8 bg-muted/30">
        <CardHeader className="pb-2">
          <CardTitle className="text-sm flex items-center gap-2">
            <FileText className="h-4 w-4" />
            How to Import US Code Titles
          </CardTitle>
        </CardHeader>
        <CardContent className="text-sm text-muted-foreground space-y-2">
          <ol className="list-decimal list-inside space-y-1">
            <li>
              Visit{' '}
              <a
                href="https://uscode.house.gov/download/download.shtml"
                target="_blank"
                rel="noopener noreferrer"
                className="text-primary hover:underline"
              >
                uscode.house.gov/download
              </a>
            </li>
            <li>Download the ZIP file for the title you want (e.g., xml_usc05@119-46.zip)</li>
            <li>Extract the XML file from the ZIP archive</li>
            <li>Click &quot;Upload US Code XML&quot; and select the extracted XML file</li>
            <li>Verify the import results before importing the next title</li>
          </ol>
        </CardContent>
      </Card>

      {/* Titles List with Expandable Tree */}
      <Card>
        <CardHeader>
          <CardTitle>Imported Titles</CardTitle>
          <CardDescription>
            Click a title to expand and browse chapters and sections
          </CardDescription>
        </CardHeader>
        <CardContent>
          {loading ? (
            <div className="py-8 text-center text-muted-foreground">
              <RefreshCw className="h-6 w-6 animate-spin mx-auto mb-2" />
              Loading titles...
            </div>
          ) : titles.length === 0 ? (
            <div className="py-8 text-center">
              <FileText className="h-12 w-12 text-muted-foreground mx-auto mb-4" />
              <h3 className="text-lg font-semibold mb-2">No US Code data imported</h3>
              <p className="text-muted-foreground mb-4">
                Use the &quot;Upload US Code XML&quot; button above to import statute sections from
                the official XML files.
              </p>
            </div>
          ) : (
            <div className="divide-y divide-border">
              {titles.map((title) => (
                <div key={title.titleNumber}>
                  {/* Title Row */}
                  <button
                    onClick={() => toggleTitle(title.titleNumber)}
                    className="flex items-center justify-between w-full py-3 hover:bg-muted/50 transition-colors px-2 -mx-2 rounded text-left"
                  >
                    <div className="flex items-center gap-3">
                      {expandedTitle === title.titleNumber ? (
                        <ChevronDown className="h-4 w-4 text-muted-foreground" />
                      ) : (
                        <ChevronRight className="h-4 w-4 text-muted-foreground" />
                      )}
                      <span className="font-mono text-sm bg-muted px-2 py-1 rounded w-10 text-center">
                        {title.titleNumber}
                      </span>
                      <span className="font-medium">{title.titleName}</span>
                    </div>
                    <span className="text-sm text-muted-foreground">
                      {title.sectionCount.toLocaleString()} section{title.sectionCount !== 1 ? 's' : ''}
                    </span>
                  </button>

                  {/* Expanded Tree View */}
                  {expandedTitle === title.titleNumber && (
                    <div className="ml-4 mb-4">
                      <UsCodeTreeView
                        titleNumber={title.titleNumber}
                        titleName={title.titleName}
                      />
                    </div>
                  )}
                </div>
              ))}
            </div>
          )}
        </CardContent>
      </Card>

      {/* External Links */}
      <div className="mt-8 flex gap-4 text-sm">
        <a
          href="https://uscode.house.gov/"
          target="_blank"
          rel="noopener noreferrer"
          className="flex items-center gap-1 text-primary hover:underline"
        >
          <ExternalLink className="h-4 w-4" />
          Office of the Law Revision Counsel
        </a>
        <a
          href="https://uscode.house.gov/download/download.shtml"
          target="_blank"
          rel="noopener noreferrer"
          className="flex items-center gap-1 text-primary hover:underline"
        >
          <ExternalLink className="h-4 w-4" />
          US Code XML Download
        </a>
      </div>
    </main>
  );
}
