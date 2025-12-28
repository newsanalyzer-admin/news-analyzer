'use client';

import { memo, useCallback } from 'react';
import { ChevronRight, ChevronDown } from 'lucide-react';
import { cn } from '@/lib/utils';
import type { HierarchyNode, HierarchyConfig } from '@/lib/config/entityTypes';

/**
 * Props for the TreeNode component
 */
export interface TreeNodeProps {
  /** Node data */
  node: HierarchyNode;
  /** Hierarchy configuration */
  config: HierarchyConfig;
  /** Current depth level (0 = root) */
  depth: number;
  /** Entity type for navigation URLs */
  entityType: string;
  /** Whether this node is expanded */
  isExpanded: boolean;
  /** Whether this node is focused (for keyboard nav) */
  isFocused: boolean;
  /** Set of expanded node IDs */
  expandedIds: Set<string>;
  /** Toggle node expansion */
  onToggle: (nodeId: string) => void;
  /** Select node for navigation */
  onSelect: (node: HierarchyNode) => void;
  /** Focus a node */
  onFocus: (nodeId: string) => void;
  /** Whether this is the last child in its parent */
  isLastChild?: boolean;
}

/**
 * TreeNode - Renders a single node in the hierarchy tree with connector lines.
 * Recursive component that renders its children when expanded.
 */
export const TreeNode = memo(function TreeNode({
  node,
  config,
  depth,
  entityType,
  isExpanded,
  isFocused,
  expandedIds,
  onToggle,
  onSelect,
  onFocus,
  isLastChild = false,
}: TreeNodeProps) {
  const nodeId = String(node[config.idField || 'id']);
  const label = getNestedValue(node, config.labelField);
  const children = (node[config.childrenField] as HierarchyNode[] | undefined) || [];
  const hasChildren = children.length > 0;

  // Get metadata fields
  const metaValues = config.metaFields
    ?.map((field) => getNestedValue(node, field))
    .filter((v) => v !== null && v !== undefined && v !== '');

  const handleToggleClick = useCallback(
    (e: React.MouseEvent) => {
      e.stopPropagation();
      if (hasChildren) {
        onToggle(nodeId);
      }
    },
    [hasChildren, nodeId, onToggle]
  );

  const handleLabelClick = useCallback(() => {
    onSelect(node);
  }, [node, onSelect]);

  const handleFocus = useCallback(() => {
    onFocus(nodeId);
  }, [nodeId, onFocus]);

  // Indentation and connector line styling
  const indentPx = depth * 24;

  return (
    <div
      role="treeitem"
      aria-expanded={hasChildren ? isExpanded : undefined}
      aria-level={depth + 1}
      aria-selected={isFocused}
      className="relative"
    >
      {/* Node row */}
      <div
        className={cn(
          'group flex items-center py-1.5 pr-2 rounded-md transition-colors cursor-pointer',
          'hover:bg-muted/50',
          isFocused && 'bg-primary/10 ring-1 ring-primary/20'
        )}
        style={{ paddingLeft: `${indentPx + 8}px` }}
        onClick={handleLabelClick}
        onFocus={handleFocus}
        tabIndex={isFocused ? 0 : -1}
      >
        {/* Connector lines */}
        {depth > 0 && (
          <>
            {/* Vertical line from parent */}
            <div
              className="absolute border-l border-border"
              style={{
                left: `${indentPx - 12}px`,
                top: 0,
                bottom: isLastChild ? '50%' : 0,
              }}
            />
            {/* Horizontal line to node */}
            <div
              className="absolute border-t border-border"
              style={{
                left: `${indentPx - 12}px`,
                top: '50%',
                width: '12px',
              }}
            />
          </>
        )}

        {/* Expand/collapse button */}
        <button
          type="button"
          onClick={handleToggleClick}
          className={cn(
            'flex items-center justify-center w-5 h-5 mr-1 rounded-sm transition-colors flex-shrink-0',
            hasChildren
              ? 'hover:bg-muted text-muted-foreground hover:text-foreground'
              : 'invisible'
          )}
          aria-label={isExpanded ? 'Collapse' : 'Expand'}
          tabIndex={-1}
        >
          {hasChildren &&
            (isExpanded ? (
              <ChevronDown className="h-4 w-4" />
            ) : (
              <ChevronRight className="h-4 w-4" />
            ))}
        </button>

        {/* Node content */}
        <div className="flex items-center gap-2 min-w-0 flex-1">
          {/* Label */}
          <span
            className={cn(
              'font-medium truncate',
              'group-hover:text-primary transition-colors'
            )}
          >
            {formatValue(label)}
          </span>

          {/* Metadata */}
          {metaValues && metaValues.length > 0 && (
            <span className="text-sm text-muted-foreground truncate">
              ({metaValues.map((v) => formatValue(v)).join(', ')})
            </span>
          )}

          {/* Child count */}
          {config.showChildCount && hasChildren && (
            <span className="text-xs text-muted-foreground bg-muted px-1.5 py-0.5 rounded">
              {children.length}
            </span>
          )}

          {/* Badge */}
          {config.renderBadge && (
            <span className="flex-shrink-0">{config.renderBadge(node)}</span>
          )}
        </div>
      </div>

      {/* Children */}
      {hasChildren && isExpanded && (
        <div role="group" className="relative">
          {children.map((child, index) => {
            const childId = String(child[config.idField || 'id']);
            return (
              <TreeNode
                key={childId}
                node={child}
                config={config}
                depth={depth + 1}
                entityType={entityType}
                isExpanded={expandedIds.has(childId)}
                isFocused={false}
                expandedIds={expandedIds}
                onToggle={onToggle}
                onSelect={onSelect}
                onFocus={onFocus}
                isLastChild={index === children.length - 1}
              />
            );
          })}
        </div>
      )}
    </div>
  );
});

/**
 * Get a nested value from an object using dot notation
 */
function getNestedValue(obj: Record<string, unknown>, path: string): unknown {
  return path.split('.').reduce((current, key) => {
    if (current === null || current === undefined) return undefined;
    return (current as Record<string, unknown>)[key];
  }, obj as unknown);
}

/**
 * Format a value for display
 */
function formatValue(value: unknown): string {
  if (value === null || value === undefined) {
    return '-';
  }
  return String(value);
}
