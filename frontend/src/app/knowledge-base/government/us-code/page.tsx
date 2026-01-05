'use client';

/**
 * Public U.S. Code Browse Page
 *
 * Read-only browse page for U.S. Code statutes in the Knowledge Base.
 * Displays imported titles with expandable tree view (Title -> Chapter -> Section).
 */

import { useState, useEffect, useCallback } from 'react';
import Link from 'next/link';
import {
  BookOpen,
  RefreshCw,
  AlertCircle,
  ExternalLink,
  ChevronRight,
  ChevronDown,
  ArrowLeft,
} from 'lucide-react';
import { Card, CardHeader, CardTitle, CardDescription, CardContent } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { UsCodeTreeView } from '@/components/admin/UsCodeTreeView';
import { KBBreadcrumbs } from '@/components/knowledge-base';

interface TitleInfo {
  titleNumber: number;
  titleName: string;
  sectionCount: number;
}

export default function UsCodePage() {
  const [titles, setTitles] = useState<TitleInfo[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [expandedTitle, setExpandedTitle] = useState<number | null>(null);

  const API_BASE = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080';

  const fetchTitles = useCallback(async () => {
    try {
      setLoading(true);
      setError(null);

      const response = await fetch(`${API_BASE}/api/statutes/titles`);
      if (response.ok) {
        setTitles(await response.json());
      } else {
        throw new Error('Failed to load titles');
      }
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to load data');
    } finally {
      setLoading(false);
    }
  }, [API_BASE]);

  useEffect(() => {
    fetchTitles();
  }, [fetchTitles]);

  const toggleTitle = (titleNumber: number) => {
    if (expandedTitle === titleNumber) {
      setExpandedTitle(null);
    } else {
      setExpandedTitle(titleNumber);
    }
  };

  return (
    <main className="container py-8">
      {/* Back link */}
      <div className="mb-4">
        <Button variant="ghost" size="sm" asChild className="-ml-2">
          <Link href="/knowledge-base/government">
            <ArrowLeft className="h-4 w-4 mr-2" />
            Back to U.S. Federal Government
          </Link>
        </Button>
      </div>

      {/* Breadcrumbs */}
      <KBBreadcrumbs className="mb-6" />

      {/* Page Header */}
      <div className="flex items-center gap-3 mb-4">
        <BookOpen className="h-8 w-8 text-primary" />
        <h1 className="text-3xl font-bold">U.S. Code</h1>
      </div>

      <p className="text-muted-foreground mb-6">
        The United States Code is the official codification of federal statutory law.
        Browse imported statutes organized by Title, Chapter, and Section.
      </p>

      {/* Error State */}
      {error && (
        <Card className="border-destructive mb-6">
          <CardContent className="py-4">
            <div className="flex items-center justify-between">
              <div className="flex items-center gap-2 text-destructive">
                <AlertCircle className="h-5 w-5" />
                <span>{error}</span>
              </div>
              <Button variant="outline" size="sm" onClick={fetchTitles}>
                <RefreshCw className="h-4 w-4 mr-2" />
                Retry
              </Button>
            </div>
          </CardContent>
        </Card>
      )}

      {/* Titles List */}
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
            <div className="py-12 text-center">
              <BookOpen className="h-12 w-12 text-muted-foreground mx-auto mb-4" />
              <h3 className="text-lg font-semibold mb-2">No U.S. Code Data Available</h3>
              <p className="text-muted-foreground mb-4">
                U.S. Code statutes have not been imported yet.
              </p>
              <a
                href="https://uscode.house.gov/"
                target="_blank"
                rel="noopener noreferrer"
                className="text-primary hover:underline inline-flex items-center gap-1"
              >
                <ExternalLink className="h-4 w-4" />
                Browse the official U.S. Code
              </a>
            </div>
          ) : (
            <div className="divide-y divide-border">
              {titles.map((title) => (
                <div key={title.titleNumber}>
                  {/* Title Row */}
                  <button
                    onClick={() => toggleTitle(title.titleNumber)}
                    className="flex items-center justify-between w-full py-3 hover:bg-muted/50 transition-colors px-2 -mx-2 rounded text-left"
                    aria-expanded={expandedTitle === title.titleNumber}
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
                      {title.sectionCount.toLocaleString()} section
                      {title.sectionCount !== 1 ? 's' : ''}
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

      {/* External Links Footer */}
      <div className="mt-8 p-4 bg-muted/30 rounded-lg">
        <h3 className="text-sm font-medium mb-3">Official Sources</h3>
        <div className="flex flex-wrap gap-4 text-sm">
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
            href="https://uscode.house.gov/about.xhtml"
            target="_blank"
            rel="noopener noreferrer"
            className="flex items-center gap-1 text-primary hover:underline"
          >
            <ExternalLink className="h-4 w-4" />
            About the U.S. Code
          </a>
        </div>
      </div>
    </main>
  );
}
