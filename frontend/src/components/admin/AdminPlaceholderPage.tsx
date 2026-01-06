'use client';

import Link from 'next/link';
import { Construction, ExternalLink, ArrowLeft } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';

interface AdminPlaceholderPageProps {
  title: string;
  description: string;
  kbPath: string;
  backPath: string;
  backLabel: string;
  icon?: React.ReactNode;
}

/**
 * Placeholder page component for admin KB pages under construction.
 * Shows the page title, a "coming soon" message, and links to the
 * corresponding public KB page.
 */
export function AdminPlaceholderPage({
  title,
  description,
  kbPath,
  backPath,
  backLabel,
  icon,
}: AdminPlaceholderPageProps) {
  return (
    <div className="container py-8">
      {/* Back link */}
      <div className="mb-4">
        <Button variant="ghost" size="sm" asChild className="-ml-2">
          <Link href={backPath}>
            <ArrowLeft className="h-4 w-4 mr-2" />
            {backLabel}
          </Link>
        </Button>
      </div>

      {/* Header */}
      <div className="mb-8">
        <div className="flex items-center gap-3 mb-4">
          {icon && (
            <div className="p-3 rounded-lg bg-primary/10 text-primary">
              {icon}
            </div>
          )}
          <h1 className="text-3xl font-bold">{title}</h1>
        </div>
        <p className="text-muted-foreground">{description}</p>
      </div>

      {/* Placeholder Card */}
      <Card className="border-dashed">
        <CardHeader className="text-center">
          <div className="mx-auto mb-4 p-4 rounded-full bg-amber-500/10">
            <Construction className="h-12 w-12 text-amber-500" />
          </div>
          <CardTitle>Under Construction</CardTitle>
          <CardDescription>
            This admin page is being developed. CRUD functionality for managing{' '}
            <strong>{title}</strong> data will be available here.
          </CardDescription>
        </CardHeader>
        <CardContent className="text-center">
          <p className="text-sm text-muted-foreground mb-4">
            View the public Knowledge Base page for reference:
          </p>
          <Button variant="outline" asChild>
            <Link href={kbPath} target="_blank">
              View KB Page
              <ExternalLink className="h-4 w-4 ml-2" />
            </Link>
          </Button>
        </CardContent>
      </Card>
    </div>
  );
}
