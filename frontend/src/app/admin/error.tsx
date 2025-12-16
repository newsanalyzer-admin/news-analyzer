'use client';

/**
 * Admin Dashboard Error Boundary
 *
 * Displays error state when admin dashboard fails to load.
 */

import { useEffect } from 'react';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';

export default function AdminError({
  error,
  reset,
}: {
  error: Error & { digest?: string };
  reset: () => void;
}) {
  useEffect(() => {
    console.error('Admin page error:', error);
  }, [error]);

  return (
    <main className="container mx-auto py-8 px-4">
      <div className="flex items-center justify-center min-h-[400px]">
        <Card className="max-w-md text-center">
          <CardHeader>
            <div className="text-6xl mb-4">&#9888;&#65039;</div>
            <CardTitle>Something went wrong</CardTitle>
          </CardHeader>
          <CardContent className="space-y-4">
            <p className="text-muted-foreground">
              An error occurred while loading the admin dashboard.
            </p>
            {error.message && (
              <p className="text-sm text-destructive bg-destructive/10 p-3 rounded">
                {error.message}
              </p>
            )}
            <div className="flex gap-3 justify-center">
              <Button onClick={reset}>Try Again</Button>
              <Button variant="outline" asChild>
                <a href="/">Return to Home</a>
              </Button>
            </div>
          </CardContent>
        </Card>
      </div>
    </main>
  );
}
