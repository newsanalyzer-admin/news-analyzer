import {
  Database,
  Users,
  Users2,
  Building,
  Building2,
  Landmark,
  Scale,
  Gavel,
  FileText,
  List,
  GitBranch,
  BookOpen,
} from 'lucide-react';
import { MenuItemData } from '@/components/sidebar/types';

/**
 * Public sidebar menu configuration for Knowledge Base.
 *
 * Structure matches the actual KB route hierarchy:
 * - U.S. Federal Government → /knowledge-base/government
 *   - Branches (non-clickable grouping) → executive/legislative/judicial
 *   - U.S. Code (Federal Laws) → /knowledge-base/government/us-code
 * - People → /knowledge-base/people with subtypes
 * - Committees → /knowledge-base/committees
 * - Organizations → /knowledge-base/organizations (flat list view)
 */
export const publicMenuConfig: MenuItemData[] = [
  {
    label: 'Knowledge Base',
    icon: Database,
    children: [
      {
        label: 'U.S. Federal Government',
        icon: Building,
        href: '/knowledge-base/government',
        children: [
          {
            label: 'Branches',
            icon: GitBranch,
            // No href - non-clickable grouping
            children: [
              {
                label: 'Executive Branch',
                icon: Building,
                href: '/knowledge-base/government/executive',
              },
              {
                label: 'Legislative Branch',
                icon: Landmark,
                href: '/knowledge-base/government/legislative',
              },
              {
                label: 'Judicial Branch',
                icon: Scale,
                href: '/knowledge-base/government/judicial',
              },
            ],
          },
          {
            label: 'U.S. Code (Federal Laws)',
            icon: BookOpen,
            href: '/knowledge-base/government/us-code',
          },
        ],
      },
      {
        label: 'People',
        icon: Users,
        href: '/knowledge-base/people',
        children: [
          {
            label: 'Federal Judges',
            icon: Gavel,
            href: '/knowledge-base/people?type=judges',
          },
          {
            label: 'Congressional Members',
            icon: Users,
            href: '/knowledge-base/people?type=members',
          },
          {
            label: 'Executive Appointees',
            icon: Users,
            href: '/knowledge-base/people?type=appointees',
          },
        ],
      },
      {
        label: 'Committees',
        icon: Users2,
        href: '/knowledge-base/committees',
      },
      {
        label: 'Organizations',
        icon: Building2,
        href: '/knowledge-base/organizations',
      },
    ],
  },
];

/**
 * Flattened menu config without the Knowledge Base wrapper.
 * Use this when the sidebar header already displays "Knowledge Base".
 */
export const publicMenuItemsFlat: MenuItemData[] = publicMenuConfig[0]?.children ?? [];

/**
 * Article Analyzer sidebar menu configuration.
 * Matches the routes in /article-analyzer section.
 */
export const articleAnalyzerMenuItems: MenuItemData[] = [
  {
    label: 'Analyze Article',
    icon: FileText,
    href: '/article-analyzer/analyze',
    disabled: true, // Phase 4 feature
  },
  {
    label: 'Articles',
    icon: List,
    href: '/article-analyzer/articles',
  },
  {
    label: 'Entities',
    icon: Database,
    href: '/article-analyzer/entities',
  },
];
