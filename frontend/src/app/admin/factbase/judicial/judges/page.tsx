'use client';

/**
 * Federal Judges Import Page
 *
 * Admin page for importing federal judge data from the Federal Judicial Center (FJC).
 */

import { useState } from 'react';
import Link from 'next/link';
import {
  Gavel,
  Download,
  Info,
  Loader2,
  CheckCircle,
  AlertCircle,
  ChevronDown,
  ChevronUp,
  ExternalLink,
  RefreshCw,
} from 'lucide-react';
import { Card, CardHeader, CardTitle, CardDescription, CardContent } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { AdminBreadcrumb } from '@/components/admin/AdminBreadcrumb';
import { useToast } from '@/hooks/use-toast';

const breadcrumbs = [
  { label: 'Admin', href: '/admin' },
  { label: 'Factbase' },
  { label: 'Judicial', href: '/admin/factbase/judicial' },
  { label: 'Judges Import' },
];

interface FjcImportResult {
  success: boolean;
  totalRecords: number;
  personsCreated: number;
  personsUpdated: number;
  positionsCreated: number;
  holdingsCreated: number;
  holdingsUpdated: number;
  skipped: number;
  errors: number;
  errorMessages: string[];
  summary: string;
}

interface JudgeStats {
  totalJudges: number;
  totalAppointments: number;
  currentJudges: number;
}

export default function JudgesImportPage() {
  const [importing, setImporting] = useState(false);
  const [result, setResult] = useState<FjcImportResult | null>(null);
  const [stats, setStats] = useState<JudgeStats | null>(null);
  const [loadingStats, setLoadingStats] = useState(false);
  const [showErrors, setShowErrors] = useState(false);
  const { toast } = useToast();

  const fetchStats = async () => {
    setLoadingStats(true);
    try {
      const response = await fetch('/api/judges/stats');
      if (response.ok) {
        const data = await response.json();
        setStats(data);
      }
    } catch (error) {
      console.error('Failed to fetch stats:', error);
    } finally {
      setLoadingStats(false);
    }
  };

  // Fetch stats on mount
  useState(() => {
    fetchStats();
  });

  const handleImport = async () => {
    setImporting(true);
    setResult(null);
    setShowErrors(false);

    try {
      const response = await fetch('/api/judges/import/fjc', {
        method: 'POST',
      });

      const data: FjcImportResult = await response.json();
      setResult(data);

      if (data.success) {
        toast({
          title: 'Import successful',
          description: `Created: ${data.personsCreated} judges, ${data.holdingsCreated} appointments`,
          variant: 'success',
        });
        // Refresh stats after successful import
        fetchStats();
      } else {
        toast({
          title: 'Import completed with issues',
          description: `${data.errors} error(s) occurred`,
          variant: 'destructive',
        });
      }
    } catch (error) {
      toast({
        title: 'Import failed',
        description: error instanceof Error ? error.message : 'Unknown error',
        variant: 'destructive',
      });
    } finally {
      setImporting(false);
    }
  };

  return (
    <main className="container mx-auto py-8 px-4">
      <AdminBreadcrumb items={breadcrumbs} />

      <div className="flex items-center gap-3 mb-6">
        <Gavel className="h-8 w-8 text-primary" />
        <h1 className="text-3xl font-bold">Federal Judges Import</h1>
      </div>

      <p className="text-muted-foreground mb-8 max-w-2xl">
        Import federal judge biographical data from the Federal Judicial Center (FJC).
        This includes Article III judges from the Supreme Court, Courts of Appeals,
        and District Courts.
      </p>

      {/* Stats Card */}
      {stats && (
        <div className="mb-6 p-4 bg-muted/50 rounded-lg">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-6 text-sm">
              <div>
                <span className="text-muted-foreground">Total Judges: </span>
                <strong>{stats.totalJudges.toLocaleString()}</strong>
              </div>
              <div>
                <span className="text-muted-foreground">Current: </span>
                <strong className="text-green-600">{stats.currentJudges.toLocaleString()}</strong>
              </div>
              <div>
                <span className="text-muted-foreground">Total Appointments: </span>
                <strong>{stats.totalAppointments.toLocaleString()}</strong>
              </div>
            </div>
            <Button
              variant="ghost"
              size="sm"
              onClick={fetchStats}
              disabled={loadingStats}
            >
              <RefreshCw className={`h-4 w-4 ${loadingStats ? 'animate-spin' : ''}`} />
            </Button>
          </div>
        </div>
      )}

      <div className="grid gap-6 lg:grid-cols-2">
        {/* Import Card */}
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <Download className="h-5 w-5" />
              Import from FJC
            </CardTitle>
            <CardDescription>
              Download and import judge data directly from the Federal Judicial Center
            </CardDescription>
          </CardHeader>
          <CardContent className="space-y-4">
            <Button
              onClick={handleImport}
              disabled={importing}
              className="w-full"
            >
              {importing ? (
                <>
                  <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                  Importing from FJC...
                </>
              ) : (
                <>
                  <Download className="mr-2 h-4 w-4" />
                  Import Federal Judges
                </>
              )}
            </Button>

            {importing && (
              <div className="text-center text-sm text-muted-foreground">
                <p>Downloading from FJC website...</p>
                <p className="text-xs mt-1">This may take 30-60 seconds for ~3,500 judges.</p>
              </div>
            )}

            {/* Result Display */}
            {result && (
              <div
                className={`mt-4 rounded border p-4 ${
                  result.success
                    ? 'border-green-500/50 bg-green-50 dark:bg-green-950/20'
                    : 'border-red-500/50 bg-red-50 dark:bg-red-950/20'
                }`}
              >
                <div
                  className={`mb-3 flex items-center gap-2 text-sm font-medium ${
                    result.success
                      ? 'text-green-700 dark:text-green-400'
                      : 'text-red-700 dark:text-red-400'
                  }`}
                >
                  {result.success ? (
                    <CheckCircle className="h-4 w-4" />
                  ) : (
                    <AlertCircle className="h-4 w-4" />
                  )}
                  {result.success ? 'Import Complete' : 'Import Completed with Errors'}
                </div>

                <div className="grid grid-cols-3 gap-4 text-sm">
                  <div className="text-center">
                    <div className="text-lg font-bold text-green-600">
                      {result.personsCreated}
                    </div>
                    <div className="text-xs text-muted-foreground">Judges Created</div>
                  </div>
                  <div className="text-center">
                    <div className="text-lg font-bold text-blue-600">
                      {result.personsUpdated}
                    </div>
                    <div className="text-xs text-muted-foreground">Judges Updated</div>
                  </div>
                  <div className="text-center">
                    <div className="text-lg font-bold text-purple-600">
                      {result.holdingsCreated}
                    </div>
                    <div className="text-xs text-muted-foreground">Appointments</div>
                  </div>
                </div>

                <div className="mt-3 pt-3 border-t text-xs text-muted-foreground text-center">
                  Total records: {result.totalRecords} | Skipped: {result.skipped} | Errors: {result.errors}
                </div>

                {result.errors > 0 && result.errorMessages.length > 0 && (
                  <div className="mt-4">
                    <button
                      onClick={() => setShowErrors(!showErrors)}
                      className="flex w-full items-center justify-between rounded border border-destructive/50 bg-destructive/10 p-3 text-sm font-medium text-destructive hover:bg-destructive/20"
                    >
                      <div className="flex items-center gap-2">
                        <AlertCircle className="h-4 w-4" />
                        Error Details ({result.errorMessages.length})
                      </div>
                      {showErrors ? (
                        <ChevronUp className="h-4 w-4" />
                      ) : (
                        <ChevronDown className="h-4 w-4" />
                      )}
                    </button>
                    {showErrors && (
                      <div className="mt-2 max-h-48 overflow-y-auto rounded border border-destructive/30 bg-destructive/5 p-3">
                        <ul className="space-y-1 text-xs">
                          {result.errorMessages.slice(0, 50).map((err, idx) => (
                            <li key={idx} className="text-muted-foreground">
                              {err}
                            </li>
                          ))}
                          {result.errorMessages.length > 50 && (
                            <li className="text-muted-foreground font-medium">
                              ... and {result.errorMessages.length - 50} more errors
                            </li>
                          )}
                        </ul>
                      </div>
                    )}
                  </div>
                )}
              </div>
            )}

            <div className="text-sm text-muted-foreground space-y-2">
              <p>
                <strong>What gets imported:</strong>
              </p>
              <ul className="list-disc list-inside space-y-1 ml-2">
                <li>Judge biographical information (name, birth date, gender)</li>
                <li>Court appointments and commission dates</li>
                <li>Termination dates for former judges</li>
                <li>Position titles (Chief Justice, Circuit Judge, etc.)</li>
              </ul>
            </div>
          </CardContent>
        </Card>

        {/* Info Card */}
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <Info className="h-5 w-5" />
              About FJC Data
            </CardTitle>
            <CardDescription>
              Federal Judicial Center Biographical Directory
            </CardDescription>
          </CardHeader>
          <CardContent className="space-y-4 text-sm text-muted-foreground">
            <p>
              The Federal Judicial Center maintains the Biographical Directory of
              Article III Federal Judges, containing information on all judges
              appointed since 1789.
            </p>

            <div>
              <p className="font-medium text-foreground mb-2">Data includes:</p>
              <ul className="list-disc list-inside space-y-1 ml-2">
                <li>~3,500 historical and current judges</li>
                <li>All Article III courts (Supreme, Appeals, District)</li>
                <li>Specialized courts (Court of International Trade, etc.)</li>
                <li>Complete appointment history</li>
              </ul>
            </div>

            <div>
              <p className="font-medium text-foreground mb-2">Import behavior:</p>
              <ul className="list-disc list-inside space-y-1 ml-2">
                <li>New judges are created as Person records</li>
                <li>Existing judges are updated with new data</li>
                <li>Court positions are created/linked</li>
                <li>Appointment history is preserved</li>
              </ul>
            </div>

            <div>
              <p className="font-medium text-foreground mb-2">Prerequisites:</p>
              <ul className="list-disc list-inside space-y-1 ml-2">
                <li>
                  <Link href="/admin/factbase/executive/govman" className="text-primary hover:underline">
                    Import GOVMAN first
                  </Link>{' '}
                  to create court organizations
                </li>
              </ul>
            </div>

            <div className="pt-4 border-t">
              <p className="font-medium text-foreground mb-2">Source:</p>
              <a
                href="https://www.fjc.gov/history/judges/biographical-directory-article-iii-federal-judges-export"
                target="_blank"
                rel="noopener noreferrer"
                className="text-primary hover:underline flex items-center gap-1"
              >
                FJC Biographical Directory
                <ExternalLink className="h-3 w-3" />
              </a>
            </div>
          </CardContent>
        </Card>
      </div>

      {/* Navigation */}
      <div className="mt-8 flex gap-4">
        <Link href="/admin/factbase/judicial/courts">
          <Button variant="outline">View Courts</Button>
        </Link>
        <Link href="/factbase/people/federal-judges">
          <Button variant="outline">View Public Judges Page</Button>
        </Link>
      </div>
    </main>
  );
}
