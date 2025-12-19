'use client';

/**
 * JudgeTable Component
 *
 * Table display of federal judges with pagination.
 */

import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { Skeleton } from '@/components/ui/skeleton';
import type { Judge } from '@/types/judge';
import type { Page } from '@/types/pagination';
import { getStatusColor, getPartyColor } from '@/types/judge';

interface JudgeTableProps {
  data?: Page<Judge>;
  isLoading: boolean;
  error: Error | null;
  onRetry: () => void;
  currentPage: number;
  onPageChange: (page: number) => void;
  onSelectJudge?: (judge: Judge) => void;
}

export function JudgeTable({
  data,
  isLoading,
  error,
  onRetry,
  currentPage,
  onPageChange,
  onSelectJudge,
}: JudgeTableProps) {
  if (isLoading) {
    return <JudgeTableSkeleton />;
  }

  if (error) {
    return (
      <div className="flex flex-col items-center justify-center py-12 text-center border rounded-lg">
        <div className="text-4xl mb-4">⚠️</div>
        <h3 className="text-lg font-semibold mb-2">Failed to load judges</h3>
        <p className="text-muted-foreground mb-4">{error.message}</p>
        <Button onClick={onRetry}>Try Again</Button>
      </div>
    );
  }

  if (!data || data.content.length === 0) {
    return (
      <div className="flex flex-col items-center justify-center py-12 text-center border rounded-lg">
        <div className="text-4xl mb-4">⚖️</div>
        <h3 className="text-lg font-semibold mb-2">No judges found</h3>
        <p className="text-muted-foreground">
          Try adjusting your search or filter criteria.
        </p>
      </div>
    );
  }

  return (
    <div>
      {/* Desktop Table */}
      <div className="border rounded-lg overflow-hidden hidden md:block">
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>Name</TableHead>
              <TableHead className="w-[200px]">Court</TableHead>
              <TableHead className="w-[100px]">Circuit</TableHead>
              <TableHead className="w-[150px]">Appointing President</TableHead>
              <TableHead className="w-[120px]">Commission Date</TableHead>
              <TableHead className="w-[100px]">Status</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {data.content.map((judge) => (
              <TableRow
                key={judge.id}
                className={onSelectJudge ? 'cursor-pointer hover:bg-muted/50' : ''}
                onClick={() => onSelectJudge?.(judge)}
              >
                <TableCell className="font-medium">
                  {judge.fullName || `${judge.firstName} ${judge.lastName}`}
                  {judge.suffix && ` ${judge.suffix}`}
                </TableCell>
                <TableCell className="text-sm">
                  {formatCourtName(judge.courtName)}
                </TableCell>
                <TableCell>
                  {judge.circuit ? (
                    <Badge variant="outline">
                      {formatCircuit(judge.circuit)}
                    </Badge>
                  ) : (
                    '-'
                  )}
                </TableCell>
                <TableCell>
                  <div className="flex flex-col gap-1">
                    <span className="text-sm">{judge.appointingPresident || '-'}</span>
                    {judge.partyOfAppointingPresident && (
                      <Badge
                        className={getPartyColor(judge.partyOfAppointingPresident)}
                        variant="outline"
                      >
                        {judge.partyOfAppointingPresident.charAt(0)}
                      </Badge>
                    )}
                  </div>
                </TableCell>
                <TableCell className="text-sm">
                  {judge.commissionDate ? formatDate(judge.commissionDate) : '-'}
                </TableCell>
                <TableCell>
                  <Badge className={getStatusColor(judge.judicialStatus)} variant="outline">
                    {judge.judicialStatus || (judge.current ? 'Active' : 'Inactive')}
                  </Badge>
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </div>

      {/* Mobile Card View */}
      <div className="md:hidden space-y-4">
        {data.content.map((judge) => (
          <div
            key={judge.id}
            className={`border rounded-lg p-4 ${onSelectJudge ? 'cursor-pointer hover:bg-muted/50' : ''}`}
            onClick={() => onSelectJudge?.(judge)}
          >
            <div className="flex justify-between items-start mb-2">
              <div className="font-medium">
                {judge.fullName || `${judge.firstName} ${judge.lastName}`}
              </div>
              <Badge className={getStatusColor(judge.judicialStatus)} variant="outline">
                {judge.judicialStatus || (judge.current ? 'Active' : 'Inactive')}
              </Badge>
            </div>
            <div className="text-sm text-muted-foreground space-y-1">
              <div>{formatCourtName(judge.courtName)}</div>
              <div className="flex items-center gap-2">
                {judge.circuit && (
                  <Badge variant="outline" className="text-xs">
                    {formatCircuit(judge.circuit)}
                  </Badge>
                )}
                {judge.appointingPresident && (
                  <span>
                    Appointed by {judge.appointingPresident}
                    {judge.partyOfAppointingPresident && (
                      <span className="ml-1">
                        ({judge.partyOfAppointingPresident.charAt(0)})
                      </span>
                    )}
                  </span>
                )}
              </div>
              {judge.commissionDate && (
                <div>Commissioned: {formatDate(judge.commissionDate)}</div>
              )}
            </div>
          </div>
        ))}
      </div>

      {/* Pagination */}
      <div className="flex flex-col sm:flex-row justify-between items-center mt-4 gap-4">
        <p className="text-sm text-muted-foreground">
          Showing {data.number * data.size + 1} to{' '}
          {Math.min((data.number + 1) * data.size, data.totalElements)} of{' '}
          {data.totalElements} judges
        </p>
        <div className="flex gap-2">
          <Button
            variant="outline"
            onClick={() => onPageChange(currentPage - 1)}
            disabled={data.first}
          >
            Previous
          </Button>
          <Button
            variant="outline"
            onClick={() => onPageChange(currentPage + 1)}
            disabled={data.last}
          >
            Next
          </Button>
        </div>
      </div>
    </div>
  );
}

function JudgeTableSkeleton() {
  return (
    <div className="border rounded-lg">
      <div className="p-4 border-b">
        <div className="flex gap-4">
          <Skeleton className="h-6 w-32" />
          <Skeleton className="h-6 w-48" />
          <Skeleton className="h-6 w-20" />
          <Skeleton className="h-6 w-32" />
          <Skeleton className="h-6 w-24" />
          <Skeleton className="h-6 w-20" />
        </div>
      </div>
      {Array.from({ length: 10 }).map((_, i) => (
        <div key={i} className="p-4 border-b flex items-center gap-4">
          <Skeleton className="h-5 w-40" />
          <Skeleton className="h-5 w-48" />
          <Skeleton className="h-6 w-20" />
          <Skeleton className="h-5 w-32" />
          <Skeleton className="h-5 w-24" />
          <Skeleton className="h-6 w-16" />
        </div>
      ))}
    </div>
  );
}

// Helper functions
function formatCourtName(name?: string | null): string {
  if (!name) return '-';
  // Shorten long court names for display
  return name
    .replace('United States ', 'U.S. ')
    .replace('U.S. Court of Appeals for the ', '')
    .replace('U.S. District Court for the ', '');
}

function formatCircuit(circuit?: string | null): string {
  if (!circuit) return '-';
  if (circuit === 'DC') return 'D.C.';
  if (circuit === 'FEDERAL') return 'Fed.';
  return `${circuit}${getOrdinalSuffix(parseInt(circuit))}`;
}

function getOrdinalSuffix(n: number): string {
  const s = ['th', 'st', 'nd', 'rd'];
  const v = n % 100;
  return s[(v - 20) % 10] || s[v] || s[0];
}

function formatDate(dateStr?: string | null): string {
  if (!dateStr) return '-';
  try {
    const date = new Date(dateStr);
    return date.toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
    });
  } catch {
    return dateStr;
  }
}
