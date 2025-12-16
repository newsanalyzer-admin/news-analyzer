/**
 * Social Media URL Builders
 *
 * Utilities for building social media profile URLs from handles/IDs.
 */

import type { SocialMedia } from '@/types/member';

export interface SocialUrls {
  twitter?: string;
  facebook?: string;
  youtube?: string;
}

/**
 * Build social media URLs from social media data
 */
export function buildSocialUrls(socialMedia: SocialMedia | null | undefined): SocialUrls {
  if (!socialMedia) return {};

  return {
    twitter: socialMedia.twitter
      ? `https://twitter.com/${socialMedia.twitter}`
      : undefined,
    facebook: socialMedia.facebook
      ? `https://facebook.com/${socialMedia.facebook}`
      : undefined,
    // Prefer youtube_id (channel ID) over youtube (handle) for reliable URLs
    youtube: socialMedia.youtube_id
      ? `https://youtube.com/channel/${socialMedia.youtube_id}`
      : socialMedia.youtube
        ? `https://youtube.com/@${socialMedia.youtube}`
        : undefined,
  };
}

/**
 * Check if any social media links are available
 */
export function hasSocialMedia(socialMedia: SocialMedia | null | undefined): boolean {
  if (!socialMedia) return false;
  return !!(socialMedia.twitter || socialMedia.facebook || socialMedia.youtube || socialMedia.youtube_id);
}
