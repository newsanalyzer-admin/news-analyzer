'use client';

/**
 * MemberSocialMedia Component
 *
 * Displays social media links with icons.
 */

import { Twitter, Facebook, Youtube } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { buildSocialUrls, hasSocialMedia } from '@/lib/utils/social-links';
import type { SocialMedia } from '@/types/member';

interface MemberSocialMediaProps {
  socialMedia: SocialMedia | null | undefined;
}

export function MemberSocialMedia({ socialMedia }: MemberSocialMediaProps) {
  if (!hasSocialMedia(socialMedia)) {
    return (
      <div className="flex flex-col items-center justify-center py-8 text-center border rounded-lg bg-gray-50">
        <div className="text-3xl mb-2">&#128279;</div>
        <p className="text-muted-foreground">No social media profiles available</p>
      </div>
    );
  }

  const urls = buildSocialUrls(socialMedia);

  return (
    <div className="space-y-3">
      {urls.twitter && (
        <SocialLink
          href={urls.twitter}
          icon={<Twitter className="h-5 w-5" />}
          label="Twitter"
          handle={`@${socialMedia?.twitter}`}
        />
      )}
      {urls.facebook && (
        <SocialLink
          href={urls.facebook}
          icon={<Facebook className="h-5 w-5" />}
          label="Facebook"
          handle={socialMedia?.facebook || 'Facebook'}
        />
      )}
      {urls.youtube && (
        <SocialLink
          href={urls.youtube}
          icon={<Youtube className="h-5 w-5" />}
          label="YouTube"
          handle={socialMedia?.youtube || 'YouTube Channel'}
        />
      )}
    </div>
  );
}

interface SocialLinkProps {
  href: string;
  icon: React.ReactNode;
  label: string;
  handle: string;
}

function SocialLink({ href, icon, label, handle }: SocialLinkProps) {
  return (
    <a
      href={href}
      target="_blank"
      rel="noopener noreferrer"
      className="flex items-center gap-3 p-3 border rounded-lg hover:bg-gray-50 transition-colors"
    >
      <div className="flex items-center justify-center w-10 h-10 rounded-full bg-gray-100 text-gray-700">
        {icon}
      </div>
      <div className="flex-1 min-w-0">
        <div className="font-medium">{label}</div>
        <div className="text-sm text-muted-foreground truncate">{handle}</div>
      </div>
      <Button variant="ghost" size="sm" asChild>
        <span>Visit</span>
      </Button>
    </a>
  );
}
