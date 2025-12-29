import { redirect } from 'next/navigation';

/**
 * Factbase landing page - redirects to Knowledge Base
 *
 * This page exists for backwards compatibility with old bookmarks.
 * All factbase routes now redirect to the Knowledge Base.
 */
export default function FactbasePage() {
  redirect('/knowledge-base');
}
