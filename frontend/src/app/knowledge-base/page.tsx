import { redirect } from 'next/navigation';
import { getDefaultEntityType } from '@/lib/config/entityTypes';

/**
 * Knowledge Base landing page.
 * Redirects to the default entity type (organizations).
 */
export default function KnowledgeBasePage() {
  const defaultEntityType = getDefaultEntityType();
  redirect(`/knowledge-base/${defaultEntityType.id}`);
}
