# Project Brief: Knowledge Explorer UI Refactoring

**Document Version:** 1.0
**Created:** 2025-12-22
**Status:** Draft

---

## Executive Summary

NewsAnalyzer requires a unified **Knowledge Explorer** UI to replace the current fragmented approach to displaying authoritative factbase data. The current implementation evolved organically as a validation tool for admin import functionality, resulting in overlapping entry points ("Explore Factbase" vs "View Government Orgs") and unclear purpose separation.

This refactoring will establish a **pattern-based, extensible UI architecture** that cleanly separates three distinct concerns: the backend factbase service (for AI reasoning), the educational exploration interface (for users), and the data management interface (for admins).

---

## Problem Statement

### Current State & Pain Points

1. **Fragmented Navigation:** The hero page presents multiple overlapping paths to similar content ("Explore Factbase" and "View Government Orgs"), creating user confusion
2. **Organic Growth Without Design Intent:** The factbase display evolved as a way to validate admin import functionality, not as a purposefully designed user experience
3. **Conflated Purposes:** The term "factbase" blurs the line between:
   - Backend data service (for AI consumption)
   - User-facing educational content (for human understanding)
   - Admin data management (for maintenance)
4. **Limited Extensibility:** Current UI is implicitly tied to government data structures, making it difficult to add new fact domains (corporations, universities, etc.)

### Impact

- Users are confused about where to find information and why there are multiple paths
- Development effort is scattered across inconsistent implementations
- Adding new fact types requires significant UI rework rather than configuration
- The educational value of the platform is diminished by poor information architecture

### Why Existing Solutions Fall Short

The current approach lacks:
- A unified conceptual model for presenting diverse fact types
- Reusable UI patterns that adapt to different entity types
- Clear separation between backend data service and frontend presentation
- Intentional design for the four user modes: browse, search, understand relationships, and verify

---

## Proposed Solution

### Core Concept

Retire "factbase" as user-facing terminology. Introduce a **Knowledge Explorer** - a unified, pattern-based interface for exploring authoritative data across all fact domains.

### Key Principles

1. **Single Entry Point:** One "Explore" or "Knowledge Base" section replacing fragmented navigation
2. **Entity-Type Agnostic Patterns:** Reusable components that adapt based on entity type and available data
3. **Multi-Modal Interaction:** Support browsing, searching, relationship understanding, and fact verification
4. **Extensibility First:** New fact domains plug into existing patterns without UI overhaul
5. **Clear Purpose Separation:**
   - "Factbase" = backend/API concept (invisible to users)
   - "Knowledge Explorer" = user-facing educational interface
   - "Admin" = data management (already separate)

### High-Level Architecture

```
┌─────────────────────────────────────────────────────────┐
│                    Knowledge Explorer                    │
├─────────────────────────────────────────────────────────┤
│  Entity Type Selector                                   │
│  [Organizations] [People] [Laws/Regulations] [...]      │
├─────────────────────────────────────────────────────────┤
│  View Mode Selector                                     │
│  [List/Grid] [Hierarchy] [Graph] [Timeline]             │
├─────────────────────────────────────────────────────────┤
│  Search Bar (cross-entity, cross-type)                  │
├─────────────────────────────────────────────────────────┤
│                                                         │
│              Adaptive Content Area                      │
│   (renders based on entity type + view mode selection)  │
│                                                         │
└─────────────────────────────────────────────────────────┘
```

---

## Target Users

### Primary User Segment: Civic Learners

- **Profile:** Citizens interested in understanding how government works; students; journalists; researchers
- **Current Behavior:** May use Wikipedia, government websites, or news sources to piece together understanding
- **Pain Points:** Information is scattered, hard to verify, lacks authoritative sourcing, relationships between entities are unclear
- **Goals:** Develop accurate mental models of government structure; verify facts; understand who holds what position

### Secondary User Segment: Fact-Checkers & Analysts

- **Profile:** Journalists, researchers, policy analysts who need to verify claims against authoritative data
- **Current Behavior:** Cross-reference multiple sources manually
- **Pain Points:** Time-consuming verification process; inconsistent data across sources
- **Goals:** Quickly verify facts; trace relationships; understand historical context

### Tertiary: System Admins

- **Profile:** Data maintainers who import and verify factbase data
- **Note:** Already served by Admin UI - not the focus of this refactoring

---

## Goals & Success Metrics

### Business Objectives

- Reduce user confusion about navigation paths (measure: analytics on navigation patterns)
- Establish extensible architecture that reduces time-to-implement for new fact domains (measure: dev effort for adding new entity type)
- Increase user engagement with educational content (measure: time on page, depth of exploration)

### User Success Metrics

- Users can find specific information within 3 clicks from Knowledge Explorer entry
- Users can understand entity relationships through visual hierarchy/graph views
- Users can verify facts and see authoritative source attribution

### Key Performance Indicators (KPIs)

- **Navigation clarity:** Single primary path to explore content (eliminate duplicate entry points)
- **Extensibility:** New entity type integration requires < 1 day of UI work
- **Engagement:** Average session depth increases after refactoring

---

## MVP Scope

### Core Features (Must Have)

- **Unified Entry Point:** Single "Explore" / "Knowledge Base" navigation item replacing fragmented paths
- **Entity Type Selector:** Toggle between Organizations, People, and other available types
- **List/Grid View Pattern:** Filterable, sortable entity browser (reusable across types)
- **Entity Detail Pattern:** Consistent detail view showing attributes, relationships, and source citations
- **Hierarchy View Pattern:** Tree visualization for hierarchical entities (org structures, etc.)
- **Cross-Entity Search:** Single search bar that queries across all entity types
- **Source Attribution:** Every fact displays its authoritative source

### Out of Scope for MVP

- Relationship graph visualization (complex, defer to Phase 2)
- Timeline view (defer to Phase 2)
- Advanced filtering/faceted search
- Comparison views (side-by-side entities)
- User annotations or bookmarks
- Export functionality

### MVP Success Criteria

- Hero page has ONE clear path to explore authoritative data
- Government orgs and Federal judges render using the same reusable patterns
- Adding a new entity type (e.g., "Regulations") requires only:
  1. Backend API endpoint
  2. Type definition
  3. Configuration (not new components)

---

## Post-MVP Vision

### Phase 2 Features

- **Relationship Graph View:** Interactive network visualization showing connections between entities
- **Timeline View:** Historical view of positions, appointments, organizational changes
- **Advanced Filtering:** Faceted search with multiple filter criteria
- **Entity Comparison:** Side-by-side comparison of entities

### Long-term Vision

- Expand fact domains: corporations, universities, non-profits, international organizations
- Enable user-contributed annotations (with moderation)
- API access for third-party integration
- Embeddable widgets for external sites

### Expansion Opportunities

- State/local government data (beyond federal)
- Historical deep-dives (past administrations, historical legislation)
- Integration with news analysis to show "entities in the news"

---

## Technical Considerations

### Platform Requirements

- **Target Platforms:** Web (desktop and mobile responsive)
- **Browser Support:** Modern browsers (Chrome, Firefox, Safari, Edge - latest 2 versions)
- **Performance Requirements:** Initial page load < 2s; entity list pagination for large datasets

### Technology Preferences

- **Frontend:** Next.js/React (existing stack)
- **Backend:** Spring Boot REST API (existing stack)
- **Database:** PostgreSQL with existing entity models
- **Styling:** Tailwind CSS (existing)

### Architecture Considerations

- **Component Architecture:** Create reusable pattern components:
  - `EntityBrowser` - list/grid with filtering
  - `EntityDetail` - attribute display with source citations
  - `HierarchyView` - tree visualization
  - `EntityTypeSelector` - navigation between types
  - `ViewModeSelector` - switch between visualization modes
- **Configuration-Driven:** Entity types define their available views and attributes via configuration
- **API Design:** Consistent REST patterns across entity types (pagination, filtering, sorting)

---

## Constraints & Assumptions

### Constraints

- **Budget:** Internal development team only
- **Timeline:** Should align with existing sprint cadence
- **Resources:** Existing frontend/backend developers
- **Technical:** Must work within existing Next.js + Spring Boot architecture

### Key Assumptions

- The existing entity data models (Person, GovernmentOrganization, etc.) are sufficient for MVP
- Admin import functionality remains separate and unchanged
- The "factbase" backend service concept remains valid; only user-facing terminology changes
- Reusable patterns will apply across at least 3 entity types (Organizations, People, Regulations)

---

## Risks & Open Questions

### Key Risks

- **Pattern Over-Generalization:** Risk that patterns become too generic and lose usability for specific entity types. Mitigation: Start with 2-3 concrete types and extract patterns, rather than designing abstract patterns first.
- **Scope Creep:** Risk of adding "nice to have" visualizations before core patterns are solid. Mitigation: Strict MVP definition; defer graph/timeline views.
- **Migration Disruption:** Removing existing pages may break bookmarks or user habits. Mitigation: Implement redirects; communicate changes.

### Open Questions

- Should "Admin" section be visually connected to Knowledge Explorer, or remain completely separate?
- What is the right terminology? "Explore" vs "Knowledge Base" vs "Reference" vs something else?
- How should source citations be displayed? Inline? Expandable? Separate tab?

### Areas Needing Further Research

- User testing on navigation patterns (A/B test single entry point vs. current)
- Competitive analysis: How do other civic education platforms organize information?
- Accessibility review of hierarchy/tree visualizations

---

## Appendices

### A. Research Summary

This brief was developed through a facilitated discovery conversation identifying:
- Root cause of current fragmentation (organic growth vs. intentional design)
- Three distinct purposes being conflated under "factbase"
- Reusable pattern approach for extensibility
- Four user interaction modes to support

### B. Current State Reference

Current navigation (to be refactored):
- Hero page → "Explore Factbase" (fragmented)
- Hero page → "View Government Orgs" (overlapping)
- Admin → Import tools (separate, unchanged)

### C. References

- Existing PRD: `docs/prd/`
- Architecture docs: `docs/architecture/`
- Current frontend: `frontend/src/app/factbase/`

---

## Next Steps

### Immediate Actions

1. Review and approve this Project Brief
2. Create epic and stories for Knowledge Explorer refactoring
3. Design component architecture for reusable patterns
4. Implement unified entry point and entity type selector
5. Refactor existing Government Org and Federal Judges views to use new patterns

### PO Handoff

This Project Brief provides the full context for the Knowledge Explorer UI Refactoring. Sarah (PO) should review this brief to create epics and stories that implement the pattern-based architecture while maintaining MVP discipline.

---

*Generated by Mary (Business Analyst) - BMAD Workflow*
