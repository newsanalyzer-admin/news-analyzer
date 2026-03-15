'use client';

/**
 * AdministrationExecutiveOrders Component (KB-2.2)
 *
 * Displays a paginated list of executive orders for an administration.
 * Uses the usePresidencyExecutiveOrders hook for data fetching.
 */

import { useState } from 'react';
import { FileText, ChevronLeft, ChevronRight, ExternalLink } from 'lucide-react';
import { Card, CardHeader, CardTitle, CardContent } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { Skeleton } from '@/components/ui/skeleton';
import { usePresidencyExecutiveOrders } from '@/hooks/usePresidencySync';
import type { ExecutiveOrderDTO } from '@/hooks/usePresidencySync';

export interface AdministrationExecutiveOrdersProps {
  presidencyId: string | null;
}

const PAGE_SIZE = 10;

export function AdministrationExecutiveOrders({ presidencyId }: AdministrationExecutiveOrdersProps) {
  const [page, setPage] = useState(0);
  const { data, isLoading, error } = usePresidencyExecutiveOrders(presidencyId, page, PAGE_SIZE);

  if (isLoading) {
    return (
      <Card>
        <CardHeader>
          <Skeleton className="h-6 w-48" />
        </CardHeader>
        <CardContent className="space-y-3">
          {Array.from({ length: 5 }).map((_, i) => (
            <div key={i} className="flex items-center gap-3">
              <Skeleton className="h-8 w-16" />
              <Skeleton className="h-4 w-full max-w-md" />
              <Skeleton className="h-4 w-24 ml-auto" />
            </div>
          ))}
        </CardContent>
      </Card>
    );
  }

  if (error) {
    return (
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <FileText className="h-5 w-5" />
            Executive Orders
          </CardTitle>
        </CardHeader>
        <CardContent>
          <p className="text-destructive">Failed to load executive orders.</p>
        </CardContent>
      </Card>
    );
  }

  const orders = data?.content ?? [];
  const totalPages = data?.totalPages ?? 0;
  const totalElements = data?.totalElements ?? 0;

  return (
    <Card>
      <CardHeader>
        <div className="flex items-center justify-between">
          <CardTitle className="flex items-center gap-2">
            <FileText className="h-5 w-5" />
            Executive Orders
          </CardTitle>
          {totalElements > 0 && (
            <span className="text-sm text-muted-foreground">
              {totalElements} order{totalElements !== 1 ? 's' : ''}
            </span>
          )}
        </div>
      </CardHeader>
      <CardContent>
        {orders.length === 0 ? (
          <p className="text-muted-foreground">No executive orders recorded for this administration.</p>
        ) : (
          <>
            <div className="space-y-2">
              {orders.map((eo) => (
                <ExecutiveOrderRow key={eo.id} order={eo} />
              ))}
            </div>

            {/* Pagination */}
            {totalPages > 1 && (
              <div className="flex items-center justify-between mt-4 pt-4 border-t">
                <Button
                  variant="outline"
                  size="sm"
                  onClick={() => setPage((p) => Math.max(0, p - 1))}
                  disabled={page === 0}
                >
                  <ChevronLeft className="h-4 w-4 mr-1" />
                  Previous
                </Button>
                <span className="text-sm text-muted-foreground">
                  Page {page + 1} of {totalPages}
                </span>
                <Button
                  variant="outline"
                  size="sm"
                  onClick={() => setPage((p) => Math.min(totalPages - 1, p + 1))}
                  disabled={page >= totalPages - 1}
                >
                  Next
                  <ChevronRight className="h-4 w-4 ml-1" />
                </Button>
              </div>
            )}
          </>
        )}
      </CardContent>
    </Card>
  );
}

function ExecutiveOrderRow({ order }: { order: ExecutiveOrderDTO }) {
  const statusColor = order.status === 'REVOKED'
    ? 'bg-red-100 text-red-700 dark:bg-red-950 dark:text-red-300'
    : '';

  return (
    <div className="flex items-start gap-3 p-2 rounded-md hover:bg-muted/50 transition-colors">
      <Badge variant="outline" className="font-mono text-xs flex-shrink-0 mt-0.5">
        EO {order.eoNumber}
      </Badge>
      <div className="flex-1 min-w-0">
        <p className="text-sm font-medium leading-snug">{order.title}</p>
        <div className="flex items-center gap-2 mt-1">
          <span className="text-xs text-muted-foreground">{order.signingDate}</span>
          {order.status && order.status !== 'ACTIVE' && (
            <Badge variant="secondary" className={`text-xs ${statusColor}`}>
              {order.status}
            </Badge>
          )}
          {order.federalRegisterUrl && (
            <a
              href={order.federalRegisterUrl}
              target="_blank"
              rel="noopener noreferrer"
              className="text-xs text-primary hover:underline inline-flex items-center gap-1"
            >
              FR <ExternalLink className="h-3 w-3" />
            </a>
          )}
        </div>
      </div>
    </div>
  );
}
