import {
  Database,
  Building,
  Building2,
  Landmark,
  Scale,
  FileText,
  List,
  GitBranch,
  BookOpen,
  Crown,
  UserCircle,
  Briefcase,
  Factory,
} from 'lucide-react';
import { MenuItemData } from '@/components/sidebar/types';

/**
 * Public sidebar menu configuration for Knowledge Base.
 *
 * Structure matches the actual KB route hierarchy:
 * - U.S. Federal Government → /knowledge-base/government
 *   - Branches (non-clickable grouping) → executive/legislative/judicial
 *   - U.S. Code (Federal Laws) → /knowledge-base/government/us-code
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
                children: [
                  {
                    label: 'President of the United States',
                    icon: Crown,
                    href: '/knowledge-base/government/executive/president',
                  },
                  {
                    label: 'Vice President of the United States',
                    icon: UserCircle,
                    href: '/knowledge-base/government/executive/vice-president',
                  },
                  {
                    label: 'Executive Office of the President',
                    icon: Building,
                    href: '/knowledge-base/government/executive/eop',
                  },
                  {
                    label: 'Cabinet Departments',
                    icon: Briefcase,
                    href: '/knowledge-base/government/executive/cabinet',
                  },
                  {
                    label: 'Independent Agencies',
                    icon: Building2,
                    href: '/knowledge-base/government/executive/independent-agencies',
                  },
                  {
                    label: 'Government Corporations',
                    icon: Factory,
                    href: '/knowledge-base/government/executive/corporations',
                  },
                ],
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
