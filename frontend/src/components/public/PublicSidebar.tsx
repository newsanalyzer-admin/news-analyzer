'use client';

import { Settings } from 'lucide-react';
import { usePublicSidebarStore } from '@/stores/publicSidebarStore';
import { BaseSidebar } from '@/components/sidebar';
import { publicMenuItemsFlat } from '@/lib/menu-config';
import { cn } from '@/lib/utils';
import Link from 'next/link';

interface PublicSidebarProps {
  className?: string;
}

export function PublicSidebar({ className }: PublicSidebarProps) {
  const { isCollapsed, toggle, closeMobile } = usePublicSidebarStore();

  const adminFooter = (
    <Link
      href="/admin"
      className={cn(
        'flex items-center gap-3 w-full rounded-md py-2 px-3 text-sm font-medium',
        'hover:bg-accent hover:text-accent-foreground transition-colors',
        isCollapsed && 'justify-center px-0'
      )}
      title="Admin"
    >
      <Settings className="h-5 w-5 shrink-0" />
      {!isCollapsed && <span>Admin</span>}
    </Link>
  );

  return (
    <BaseSidebar
      menuItems={publicMenuItemsFlat}
      isCollapsed={isCollapsed}
      onToggle={toggle}
      header={
        <Link href="/factbase" className="font-semibold text-lg hover:text-primary transition-colors">
          Factbase
        </Link>
      }
      footer={adminFooter}
      className={className}
      ariaLabel="Factbase navigation"
      onNavigate={closeMobile}
    />
  );
}
