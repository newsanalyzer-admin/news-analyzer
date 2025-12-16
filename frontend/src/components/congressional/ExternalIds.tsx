'use client';

/**
 * ExternalIds Component
 *
 * Displays external resource links (FEC, GovTrack, Wikipedia, etc.).
 */

import { ExternalLink } from 'lucide-react';
import { Card, CardContent } from '@/components/ui/card';
import {
  getExternalUrl,
  externalIdLabels,
  formatExternalIdValue,
  supportedExternalIdTypes,
} from '@/lib/utils/external-links';

interface ExternalIdsProps {
  externalIds?: Record<string, unknown>;
  bioguideId: string;
}

export function ExternalIds({ externalIds, bioguideId }: ExternalIdsProps) {
  // Always include BioGuide as it's the primary ID
  const links = buildExternalLinks(externalIds, bioguideId);

  if (links.length === 0) {
    return (
      <div className="flex flex-col items-center justify-center py-8 text-center border rounded-lg bg-gray-50">
        <div className="text-3xl mb-2">&#128279;</div>
        <p className="text-muted-foreground">No external resource links available</p>
      </div>
    );
  }

  return (
    <div className="grid gap-3 sm:grid-cols-2">
      {links.map((link) => (
        <ExternalLinkCard key={link.type} link={link} />
      ))}
    </div>
  );
}

interface ExternalLinkInfo {
  type: string;
  label: string;
  value: string;
  url?: string;
}

function buildExternalLinks(
  externalIds: Record<string, unknown> | undefined,
  bioguideId: string
): ExternalLinkInfo[] {
  const links: ExternalLinkInfo[] = [];

  // Always add BioGuide
  links.push({
    type: 'bioguide',
    label: 'BioGuide',
    value: bioguideId,
    url: getExternalUrl('bioguide', bioguideId),
  });

  if (!externalIds) return links;

  // Add supported external IDs that have values
  for (const type of supportedExternalIdTypes) {
    if (type === 'bioguide') continue; // Already added

    const value = externalIds[type];
    if (value !== undefined && value !== null) {
      links.push({
        type,
        label: externalIdLabels[type] || type,
        value: formatExternalIdValue(type, value),
        url: getExternalUrl(type, value),
      });
    }
  }

  // Add other IDs without URLs (for reference)
  const otherIds = ['icpsr', 'thomas', 'lis', 'house_history', 'maplight'];
  for (const type of otherIds) {
    const value = externalIds[type];
    if (value !== undefined && value !== null) {
      links.push({
        type,
        label: externalIdLabels[type] || type,
        value: formatExternalIdValue(type, value),
        url: undefined,
      });
    }
  }

  return links;
}

function ExternalLinkCard({ link }: { link: ExternalLinkInfo }) {
  const content = (
    <Card className={link.url ? 'hover:bg-gray-50 transition-colors' : ''}>
      <CardContent className="p-4">
        <div className="flex items-center justify-between gap-2">
          <div className="flex-1 min-w-0">
            <div className="text-sm text-muted-foreground">{link.label}</div>
            <div className="font-medium truncate">{link.value}</div>
          </div>
          {link.url && (
            <ExternalLink className="h-4 w-4 text-muted-foreground flex-shrink-0" />
          )}
        </div>
      </CardContent>
    </Card>
  );

  if (link.url) {
    return (
      <a
        href={link.url}
        target="_blank"
        rel="noopener noreferrer"
        className="block"
      >
        {content}
      </a>
    );
  }

  return content;
}
