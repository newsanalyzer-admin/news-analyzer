'use client';

import {
  Database,
  Building2,
  Landmark,
  Scale,
  Gavel,
  ScrollText,
  Search,
  Github,
} from 'lucide-react';
import { useSidebarStore } from '@/stores/sidebarStore';
import { BaseSidebar, SidebarMenuItem, MenuItemData } from '@/components/sidebar';

// Menu structure per PRD
const menuItems: MenuItemData[] = [
  {
    label: 'Factbase',
    icon: Database,
    children: [
      {
        label: 'Government Entities',
        icon: Building2,
        children: [
          {
            label: 'Executive Branch',
            icon: Landmark,
            children: [
              { label: 'Agencies & Departments', href: '/admin/factbase/executive/agencies' },
              { label: 'Positions & Appointees', href: '/admin/factbase/executive/positions' },
              { label: 'GOVMAN Import', href: '/admin/factbase/executive/govman' },
            ],
          },
          {
            label: 'Legislative Branch',
            icon: Scale,
            children: [
              { label: 'Members', href: '/admin/factbase/legislative/members' },
              { label: 'Search Congress.gov', href: '/admin/factbase/legislative/members/search', icon: Search },
              { label: 'Legislators Repo', href: '/admin/factbase/legislative/legislators-repo', icon: Github },
              { label: 'Committees', href: '/admin/factbase/legislative/committees' },
            ],
          },
          {
            label: 'Judicial Branch',
            icon: Gavel,
            children: [
              { label: 'Courts', href: '/admin/factbase/judicial/courts' },
            ],
          },
        ],
      },
      {
        label: 'Federal Laws & Regulations',
        icon: ScrollText,
        children: [
          { label: 'Regulations (Federal Register)', href: '/admin/factbase/regulations/federal-register' },
          { label: 'Search Federal Register', href: '/admin/factbase/regulations/search', icon: Search },
          { label: 'US Code', href: '/admin/factbase/regulations/us-code' },
        ],
      },
    ],
  },
];

interface AdminSidebarProps {
  className?: string;
}

export function AdminSidebar({ className }: AdminSidebarProps) {
  const { isCollapsed, toggle, closeMobile } = useSidebarStore();

  return (
    <BaseSidebar
      menuItems={menuItems}
      isCollapsed={isCollapsed}
      onToggle={toggle}
      header={<span className="font-semibold text-lg">Admin</span>}
      footer={
        <SidebarMenuItem
          item={{
            label: 'Dashboard',
            href: '/admin',
            icon: Database,
          }}
          isCollapsed={isCollapsed}
          onNavigate={closeMobile}
        />
      }
      className={className}
      ariaLabel="Admin navigation"
      onNavigate={closeMobile}
    />
  );
}
