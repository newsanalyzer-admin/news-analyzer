'use client';

/**
 * GOVMAN Import Page
 *
 * Admin page for importing government organizations from GOVMAN XML files.
 */

import { FileUp, Info } from 'lucide-react';
import { Card, CardHeader, CardTitle, CardDescription, CardContent } from '@/components/ui/card';
import { AdminBreadcrumb } from '@/components/admin/AdminBreadcrumb';
import { GovmanImportButton } from '@/components/admin/GovmanImportButton';
import { useGovmanStatus } from '@/hooks/useGovmanImport';

const breadcrumbs = [
  { label: 'Admin', href: '/admin' },
  { label: 'Factbase' },
  { label: 'Executive Branch', href: '/admin/factbase/executive' },
  { label: 'GOVMAN Import' },
];

export default function GovmanImportPage() {
  const { data: status } = useGovmanStatus();

  return (
    <main className="container mx-auto py-8 px-4">
      <AdminBreadcrumb items={breadcrumbs} />

      <div className="flex items-center gap-3 mb-6">
        <FileUp className="h-8 w-8 text-primary" />
        <h1 className="text-3xl font-bold">GOVMAN XML Import</h1>
      </div>

      <p className="text-muted-foreground mb-8 max-w-2xl">
        Import official government organization structure from the Government Manual (GOVMAN) XML files
        published by GovInfo. This imports executive, legislative, and judicial branch organizations
        with their hierarchical relationships.
      </p>

      <div className="grid gap-6 lg:grid-cols-2">
        {/* Import Card */}
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <FileUp className="h-5 w-5" />
              Import GOVMAN File
            </CardTitle>
            <CardDescription>
              Upload a Government Manual XML file to import government organizations
            </CardDescription>
          </CardHeader>
          <CardContent className="space-y-4">
            <GovmanImportButton />

            <div className="text-sm text-muted-foreground space-y-2">
              <p>
                <strong>What gets imported:</strong>
              </p>
              <ul className="list-disc list-inside space-y-1 ml-2">
                <li>Government organizations (agencies, departments, bureaus)</li>
                <li>Branch categorization (Executive, Legislative, Judicial)</li>
                <li>Parent-child organizational relationships</li>
                <li>Mission statements and website URLs</li>
              </ul>
            </div>
          </CardContent>
        </Card>

        {/* Info Card */}
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <Info className="h-5 w-5" />
              About GOVMAN
            </CardTitle>
            <CardDescription>
              Government Manual XML format from GovInfo
            </CardDescription>
          </CardHeader>
          <CardContent className="space-y-4 text-sm text-muted-foreground">
            <p>
              The United States Government Manual is the official handbook of the Federal Government.
              It provides comprehensive information about agencies of the legislative, judicial, and
              executive branches.
            </p>

            <div>
              <p className="font-medium text-foreground mb-2">Import behavior:</p>
              <ul className="list-disc list-inside space-y-1 ml-2">
                <li>New organizations are created</li>
                <li>Existing GOVMAN records are updated</li>
                <li>Manually-created records are preserved</li>
                <li>Duplicate names are detected and skipped</li>
              </ul>
            </div>

            <div>
              <p className="font-medium text-foreground mb-2">Source:</p>
              <a
                href="https://www.govinfo.gov/app/collection/govman"
                target="_blank"
                rel="noopener noreferrer"
                className="text-primary hover:underline"
              >
                GovInfo - Government Manual Collection →
              </a>
            </div>

            {status?.lastImport && (
              <div className="mt-4 pt-4 border-t">
                <p className="font-medium text-foreground mb-2">Last Import:</p>
                <div className="grid grid-cols-2 gap-2 text-xs">
                  <span>Total processed:</span>
                  <span className="font-medium">{status.lastImport.total}</span>
                  <span>Imported:</span>
                  <span className="font-medium text-green-600">{status.lastImport.imported}</span>
                  <span>Updated:</span>
                  <span className="font-medium text-blue-600">{status.lastImport.updated}</span>
                  <span>Errors:</span>
                  <span className={`font-medium ${status.lastImport.errors > 0 ? 'text-red-600' : ''}`}>
                    {status.lastImport.errors}
                  </span>
                </div>
              </div>
            )}
          </CardContent>
        </Card>
      </div>
    </main>
  );
}
