'use client';

/**
 * US Code Tree View Component
 *
 * Displays US Code statutes in a collapsible hierarchical tree structure:
 * Title -> Chapter -> Section
 */

import { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import axios from 'axios';
import {
  ChevronRight,
  ChevronDown,
  Folder,
  FolderOpen,
  FileText,
  Loader2,
  AlertCircle,
} from 'lucide-react';
import { cn } from '@/lib/utils';

const API_BASE = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080';

interface SectionSummary {
  id: string;
  sectionNumber: string;
  heading: string;
  contentPreview: string;
  uscIdentifier: string;
}

interface Chapter {
  chapterNumber: string;
  chapterName: string;
  sectionCount: number;
  sections: SectionSummary[];
}

interface TitleHierarchy {
  titleNumber: number;
  titleName: string;
  sectionCount: number;
  chapters: Chapter[];
}

interface UsCodeTreeViewProps {
  titleNumber: number;
  titleName: string;
  onSectionSelect?: (section: SectionSummary) => void;
}

export function UsCodeTreeView({ titleNumber, titleName, onSectionSelect }: UsCodeTreeViewProps) {
  const [expandedChapters, setExpandedChapters] = useState<Set<string>>(new Set());
  const [selectedSectionId, setSelectedSectionId] = useState<string | null>(null);
  const [expandedSection, setExpandedSection] = useState<string | null>(null);

  const { data: hierarchy, isLoading, error } = useQuery({
    queryKey: ['statute-hierarchy', titleNumber],
    queryFn: async (): Promise<TitleHierarchy> => {
      const response = await axios.get<TitleHierarchy>(
        `${API_BASE}/api/statutes/title/${titleNumber}/hierarchy`
      );
      return response.data;
    },
  });

  const toggleChapter = (chapterNumber: string) => {
    const newExpanded = new Set(expandedChapters);
    if (newExpanded.has(chapterNumber)) {
      newExpanded.delete(chapterNumber);
    } else {
      newExpanded.add(chapterNumber);
    }
    setExpandedChapters(newExpanded);
  };

  const handleSectionClick = (section: SectionSummary) => {
    setSelectedSectionId(section.id);
    if (expandedSection === section.id) {
      setExpandedSection(null);
    } else {
      setExpandedSection(section.id);
    }
    onSectionSelect?.(section);
  };

  if (isLoading) {
    return (
      <div className="flex items-center justify-center py-8">
        <Loader2 className="h-6 w-6 animate-spin text-muted-foreground" />
        <span className="ml-2 text-sm text-muted-foreground">Loading title structure...</span>
      </div>
    );
  }

  if (error) {
    return (
      <div className="flex items-center justify-center py-8 text-destructive">
        <AlertCircle className="h-5 w-5 mr-2" />
        <span className="text-sm">Failed to load title structure</span>
      </div>
    );
  }

  if (!hierarchy || hierarchy.chapters.length === 0) {
    return (
      <div className="text-center py-8 text-muted-foreground">
        <FileText className="h-8 w-8 mx-auto mb-2 opacity-50" />
        <p className="text-sm">No sections found in this title</p>
      </div>
    );
  }

  return (
    <div className="rounded-lg border bg-card">
      {/* Title Header */}
      <div className="p-4 border-b bg-muted/50">
        <div className="flex items-center gap-2">
          <FolderOpen className="h-5 w-5 text-primary" />
          <span className="font-mono text-sm font-medium">Title {titleNumber}</span>
          <span className="text-muted-foreground">—</span>
          <span className="font-medium">{titleName}</span>
        </div>
        <p className="text-xs text-muted-foreground mt-1">
          {hierarchy.sectionCount.toLocaleString()} sections in {hierarchy.chapters.length} chapters
        </p>
      </div>

      {/* Chapter List */}
      <div className="divide-y">
        {hierarchy.chapters.map((chapter) => (
          <div key={chapter.chapterNumber}>
            {/* Chapter Row */}
            <button
              onClick={() => toggleChapter(chapter.chapterNumber)}
              className="w-full flex items-center gap-2 px-4 py-3 hover:bg-muted/50 transition-colors text-left"
            >
              {expandedChapters.has(chapter.chapterNumber) ? (
                <ChevronDown className="h-4 w-4 text-muted-foreground flex-shrink-0" />
              ) : (
                <ChevronRight className="h-4 w-4 text-muted-foreground flex-shrink-0" />
              )}
              {expandedChapters.has(chapter.chapterNumber) ? (
                <FolderOpen className="h-4 w-4 text-amber-600 flex-shrink-0" />
              ) : (
                <Folder className="h-4 w-4 text-amber-600 flex-shrink-0" />
              )}
              <span className="font-mono text-xs bg-muted px-1.5 py-0.5 rounded">
                Ch. {chapter.chapterNumber}
              </span>
              <span className="font-medium text-sm truncate">{chapter.chapterName}</span>
              <span className="ml-auto text-xs text-muted-foreground flex-shrink-0">
                {chapter.sectionCount} §
              </span>
            </button>

            {/* Sections */}
            {expandedChapters.has(chapter.chapterNumber) && (
              <div className="bg-muted/20">
                {chapter.sections.map((section) => (
                  <div key={section.id}>
                    <button
                      onClick={() => handleSectionClick(section)}
                      className={cn(
                        'w-full flex items-start gap-2 pl-10 pr-4 py-2 hover:bg-muted/50 transition-colors text-left',
                        selectedSectionId === section.id && 'bg-primary/10'
                      )}
                    >
                      <FileText className="h-4 w-4 text-muted-foreground flex-shrink-0 mt-0.5" />
                      <div className="min-w-0 flex-1">
                        <div className="flex items-center gap-2">
                          <span className="font-mono text-xs text-primary">
                            § {section.sectionNumber}
                          </span>
                          <span className="text-sm truncate">{section.heading}</span>
                        </div>
                        {expandedSection === section.id && section.contentPreview && (
                          <p className="text-xs text-muted-foreground mt-1 line-clamp-3">
                            {section.contentPreview}
                          </p>
                        )}
                      </div>
                      {expandedSection === section.id ? (
                        <ChevronDown className="h-4 w-4 text-muted-foreground flex-shrink-0" />
                      ) : (
                        <ChevronRight className="h-4 w-4 text-muted-foreground flex-shrink-0" />
                      )}
                    </button>
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
