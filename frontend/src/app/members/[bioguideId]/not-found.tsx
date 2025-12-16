/**
 * Member Not Found Page
 *
 * 404 page displayed when member is not found.
 */

import Link from 'next/link';
import { Button } from '@/components/ui/button';

export default function MemberNotFound() {
  return (
    <main className="container mx-auto py-8 px-4">
      <div className="flex flex-col items-center justify-center min-h-[400px] text-center">
        <div className="text-6xl mb-4">&#128373;</div>
        <h1 className="text-3xl font-bold mb-2">Member Not Found</h1>
        <p className="text-muted-foreground mb-6 max-w-md">
          We couldn&apos;t find a Congressional member with that BioGuide ID.
          The member may have been removed or the ID may be incorrect.
        </p>
        <Link href="/members">
          <Button>Back to Members List</Button>
        </Link>
      </div>
    </main>
  );
}
