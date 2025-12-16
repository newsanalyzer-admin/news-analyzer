'use client';

/**
 * Member Detail Error Boundary
 *
 * Error UI displayed when member detail page encounters an error.
 */

import { useEffect } from 'react';
import { Button } from '@/components/ui/button';

export default function MemberDetailError({
  error,
  reset,
}: {
  error: Error & { digest?: string };
  reset: () => void;
}) {
  useEffect(() => {
    console.error('Member detail page error:', error);
  }, [error]);

  return (
    <main className="container mx-auto py-8 px-4">
      <div className="flex flex-col items-center justify-center min-h-[400px] text-center">
        <div className="text-6xl mb-4">&#9888;&#65039;</div>
        <h1 className="text-2xl font-bold mb-2">Something went wrong</h1>
        <p className="text-muted-foreground mb-6 max-w-md">
          We encountered an error while loading this member&apos;s details.
          This might be a temporary issue with the server.
        </p>
        <Button onClick={reset} variant="default">
          Try Again
        </Button>
      </div>
    </main>
  );
}
