'use client';

import { useState, useCallback, useMemo, useRef, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { Skeleton } from '@/components/ui/skeleton';
import { Button } from '@/components/ui/button';
import { TreeNode } from './TreeNode';
import { cn } from '@/lib/utils';
import type { HierarchyNode, HierarchyConfig } from '@/lib/config/entityTypes';

/**
 * Props for the HierarchyView component
 */
export interface HierarchyViewProps {
  /** Root nodes of the hierarchy */
  data: HierarchyNode[];
  /** Hierarchy configuration */
  config: HierarchyConfig;
  /** Entity type for navigation URLs */
  entityType: string;
  /** Loading state */
  isLoading?: boolean;
  /** Error message */
  error?: string | null;
  /** Retry callback for error state */
  onRetry?: () => void;
  /** Callback when a node is clicked */
  onNodeClick?: (node: HierarchyNode) => void;
  /** Optional className */
  className?: string;
}

/**
 * HierarchyView - Renders an interactive tree visualization of hierarchical data.
 * Supports expand/collapse, keyboard navigation, and click-to-navigate.
 */
export function HierarchyView({
  data,
  config,
  entityType,
  isLoading = false,
  error,
  onRetry,
  onNodeClick,
  className,
}: HierarchyViewProps) {
  const router = useRouter();
  const containerRef = useRef<HTMLDivElement>(null);

  // Track expanded nodes
  const [expandedIds, setExpandedIds] = useState<Set<string>>(() => {
    // Initialize with nodes expanded based on defaultExpandDepth
    return getInitialExpandedIds(data, config);
  });

  // Track focused node for keyboard navigation
  const [focusedId, setFocusedId] = useState<string | null>(null);

  // Flatten visible nodes for keyboard navigation
  const visibleNodes = useMemo(() => {
    return flattenVisibleNodes(data, config, expandedIds);
  }, [data, config, expandedIds]);

  // Toggle node expansion
  const handleToggle = useCallback((nodeId: string) => {
    setExpandedIds((prev) => {
      const next = new Set(prev);
      if (next.has(nodeId)) {
        next.delete(nodeId);
      } else {
        next.add(nodeId);
      }
      return next;
    });
  }, []);

  // Handle node selection (navigation)
  const handleSelect = useCallback(
    (node: HierarchyNode) => {
      const nodeId = String(node[config.idField || 'id']);
      if (onNodeClick) {
        onNodeClick(node);
      } else {
        router.push(`/knowledge-base/${entityType}/${nodeId}`);
      }
    },
    [config.idField, entityType, onNodeClick, router]
  );

  // Handle focus change
  const handleFocus = useCallback((nodeId: string) => {
    setFocusedId(nodeId);
  }, []);

  // Keyboard navigation
  const handleKeyDown = useCallback(
    (e: React.KeyboardEvent) => {
      if (!focusedId || visibleNodes.length === 0) return;

      const currentIndex = visibleNodes.findIndex((n) => n.id === focusedId);
      if (currentIndex === -1) return;

      const currentNode = visibleNodes[currentIndex];

      switch (e.key) {
        case 'ArrowDown':
          e.preventDefault();
          if (currentIndex < visibleNodes.length - 1) {
            setFocusedId(visibleNodes[currentIndex + 1].id);
          }
          break;

        case 'ArrowUp':
          e.preventDefault();
          if (currentIndex > 0) {
            setFocusedId(visibleNodes[currentIndex - 1].id);
          }
          break;

        case 'ArrowRight':
          e.preventDefault();
          if (currentNode.hasChildren) {
            if (!expandedIds.has(focusedId)) {
              // Expand node
              handleToggle(focusedId);
            } else if (currentIndex < visibleNodes.length - 1) {
              // Already expanded, move to first child
              setFocusedId(visibleNodes[currentIndex + 1].id);
            }
          }
          break;

        case 'ArrowLeft':
          e.preventDefault();
          if (currentNode.hasChildren && expandedIds.has(focusedId)) {
            // Collapse node
            handleToggle(focusedId);
          } else if (currentNode.parentId) {
            // Move to parent
            setFocusedId(currentNode.parentId);
          }
          break;

        case 'Enter':
          e.preventDefault();
          if (currentNode.node) {
            handleSelect(currentNode.node);
          }
          break;

        case ' ':
          e.preventDefault();
          if (currentNode.hasChildren) {
            handleToggle(focusedId);
          }
          break;

        case 'Home':
          e.preventDefault();
          if (visibleNodes.length > 0) {
            setFocusedId(visibleNodes[0].id);
          }
          break;

        case 'End':
          e.preventDefault();
          if (visibleNodes.length > 0) {
            setFocusedId(visibleNodes[visibleNodes.length - 1].id);
          }
          break;
      }
    },
    [focusedId, visibleNodes, expandedIds, handleToggle, handleSelect]
  );

  // Set initial focus when data loads
  useEffect(() => {
    if (data.length > 0 && !focusedId) {
      const firstNode = data[0];
      setFocusedId(String(firstNode[config.idField || 'id']));
    }
  }, [data, config.idField, focusedId]);

  // Loading state
  if (isLoading) {
    return <HierarchyViewSkeleton />;
  }

  // Error state
  if (error) {
    return (
      <div className={cn('p-6', className)}>
        <div className="flex flex-col items-center justify-center py-12 text-center border rounded-lg">
          <div className="text-4xl mb-4">&#x26a0;&#xfe0f;</div>
          <h3 className="text-lg font-semibold mb-2">Failed to load hierarchy</h3>
          <p className="text-muted-foreground mb-4">{error}</p>
          {onRetry && <Button onClick={onRetry}>Try Again</Button>}
        </div>
      </div>
    );
  }

  // Empty state
  if (!data || data.length === 0) {
    return (
      <div className={cn('p-6', className)}>
        <div className="flex flex-col items-center justify-center py-12 text-center border rounded-lg">
          <div className="text-4xl mb-4">&#x1f333;</div>
          <h3 className="text-lg font-semibold mb-2">No hierarchy data</h3>
          <p className="text-muted-foreground">
            No hierarchical data is available to display.
          </p>
        </div>
      </div>
    );
  }

  return (
    <div
      ref={containerRef}
      role="tree"
      aria-label="Organization hierarchy"
      className={cn(
        'border rounded-lg bg-card overflow-x-auto',
        className
      )}
      onKeyDown={handleKeyDown}
    >
      <div className="p-4 min-w-fit">
        {data.map((rootNode, index) => {
          const nodeId = String(rootNode[config.idField || 'id']);
          return (
            <TreeNode
              key={nodeId}
              node={rootNode}
              config={config}
              depth={0}
              entityType={entityType}
              isExpanded={expandedIds.has(nodeId)}
              isFocused={focusedId === nodeId}
              expandedIds={expandedIds}
              onToggle={handleToggle}
              onSelect={handleSelect}
              onFocus={handleFocus}
              isLastChild={index === data.length - 1}
            />
          );
        })}
      </div>
    </div>
  );
}

/**
 * Get initial expanded node IDs based on defaultExpandDepth
 */
function getInitialExpandedIds(
  nodes: HierarchyNode[],
  config: HierarchyConfig
): Set<string> {
  const expanded = new Set<string>();

  function traverse(nodeList: HierarchyNode[], depth: number) {
    if (depth >= config.defaultExpandDepth) return;

    for (const node of nodeList) {
      const nodeId = String(node[config.idField || 'id']);
      const children = (node[config.childrenField] as HierarchyNode[] | undefined) || [];

      if (children.length > 0) {
        expanded.add(nodeId);
        traverse(children, depth + 1);
      }
    }
  }

  traverse(nodes, 0);
  return expanded;
}

/**
 * Flattened node for keyboard navigation
 */
interface FlattenedNode {
  id: string;
  node: HierarchyNode;
  depth: number;
  hasChildren: boolean;
  parentId: string | null;
}

/**
 * Flatten visible nodes for keyboard navigation
 */
function flattenVisibleNodes(
  nodes: HierarchyNode[],
  config: HierarchyConfig,
  expandedIds: Set<string>
): FlattenedNode[] {
  const result: FlattenedNode[] = [];

  function traverse(
    nodeList: HierarchyNode[],
    depth: number,
    parentId: string | null
  ) {
    for (const node of nodeList) {
      const nodeId = String(node[config.idField || 'id']);
      const children = (node[config.childrenField] as HierarchyNode[] | undefined) || [];
      const hasChildren = children.length > 0;

      result.push({
        id: nodeId,
        node,
        depth,
        hasChildren,
        parentId,
      });

      if (hasChildren && expandedIds.has(nodeId)) {
        traverse(children, depth + 1, nodeId);
      }
    }
  }

  traverse(nodes, 0, null);
  return result;
}

/**
 * Loading skeleton for HierarchyView
 */
function HierarchyViewSkeleton() {
  return (
    <div className="border rounded-lg bg-card p-4">
      <div className="space-y-2">
        {/* Root level nodes */}
        {Array.from({ length: 3 }).map((_, i) => (
          <div key={i} className="space-y-2">
            <div className="flex items-center gap-2">
              <Skeleton className="h-5 w-5" />
              <Skeleton className="h-5 w-48" />
              <Skeleton className="h-5 w-16" />
            </div>
            {/* Child nodes (first one expanded) */}
            {i === 0 && (
              <div className="ml-6 space-y-2">
                {Array.from({ length: 2 }).map((_, j) => (
                  <div key={j} className="flex items-center gap-2">
                    <Skeleton className="h-4 w-4" />
                    <Skeleton className="h-4 w-40" />
                  </div>
                ))}
              </div>
            )}
          </div>
        ))}
      </div>
    </div>
  );
}
