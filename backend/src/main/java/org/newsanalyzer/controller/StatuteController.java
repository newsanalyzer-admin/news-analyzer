package org.newsanalyzer.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.newsanalyzer.dto.StatuteDTO;
import org.newsanalyzer.dto.UsCodeHierarchyDTO;
import org.newsanalyzer.model.Statute;
import org.newsanalyzer.repository.StatuteRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * REST controller for US Code statute lookup endpoints.
 * Provides query APIs for statutory law lookup and search.
 *
 * Base Path: /api/statutes
 *
 * @author James (Dev Agent)
 * @since 2.0.0
 */
@RestController
@RequestMapping("/api/statutes")
@Tag(name = "Statutes", description = "US Code statute lookup and search endpoints")
@RequiredArgsConstructor
@Slf4j
@Validated
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"})
public class StatuteController {

    private final StatuteRepository statuteRepository;

    // =====================================================================
    // List Statutes
    // =====================================================================

    @GetMapping
    @Operation(
            summary = "List statutes",
            description = "Returns a paginated list of US Code statutes, sorted by title and section number"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully retrieved statutes"),
            @ApiResponse(responseCode = "400", description = "Invalid pagination parameters",
                    content = @Content(schema = @Schema(hidden = true)))
    })
    public Page<StatuteDTO> listStatutes(
            @Parameter(description = "Filter by title number (1-54)")
            @RequestParam(required = false) Integer titleNumber,

            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(defaultValue = "0") @Min(0) int page,

            @Parameter(description = "Page size (1-100)", example = "20")
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {

        log.info("GET /api/statutes - titleNumber={}, page={}, size={}", titleNumber, page, size);

        PageRequest pageRequest = PageRequest.of(page, size,
                Sort.by(Sort.Direction.ASC, "titleNumber", "sectionNumber"));

        Page<Statute> statutes;
        if (titleNumber != null) {
            statutes = statuteRepository.findByTitleNumber(titleNumber, pageRequest);
        } else {
            statutes = statuteRepository.findAll(pageRequest);
        }

        return statutes.map(StatuteDTO::forList);
    }

    // =====================================================================
    // Get by ID
    // =====================================================================

    @GetMapping("/{id}")
    @Operation(
            summary = "Get statute by ID",
            description = "Returns a single statute by its UUID"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Statute found"),
            @ApiResponse(responseCode = "404", description = "Statute not found",
                    content = @Content(schema = @Schema(hidden = true)))
    })
    public ResponseEntity<StatuteDTO> getById(
            @Parameter(description = "Statute UUID", example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID id) {

        log.info("GET /api/statutes/{}", id);
        return statuteRepository.findById(id)
                .map(statute -> ResponseEntity.ok(StatuteDTO.from(statute)))
                .orElse(ResponseEntity.notFound().build());
    }

    // =====================================================================
    // Get by USC Citation
    // =====================================================================

    @GetMapping("/by-citation/{uscIdentifier}")
    @Operation(
            summary = "Get statute by USC identifier",
            description = "Returns a statute by its USC identifier path (e.g., /us/usc/t5/s101)"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Statute found"),
            @ApiResponse(responseCode = "404", description = "Statute not found",
                    content = @Content(schema = @Schema(hidden = true)))
    })
    public ResponseEntity<StatuteDTO> getByCitation(
            @Parameter(description = "USC identifier", example = "/us/usc/t5/s101")
            @PathVariable String uscIdentifier) {

        // Handle URL encoding - the path may have slashes encoded
        String normalizedId = uscIdentifier.startsWith("/") ? uscIdentifier : "/" + uscIdentifier;
        normalizedId = normalizedId.replace("%2F", "/").replace("%2f", "/");

        log.info("GET /api/statutes/by-citation/{}", normalizedId);
        return statuteRepository.findByUscIdentifier(normalizedId)
                .map(statute -> ResponseEntity.ok(StatuteDTO.from(statute)))
                .orElse(ResponseEntity.notFound().build());
    }

    // =====================================================================
    // Get by Title and Section
    // =====================================================================

    @GetMapping("/title/{titleNumber}/section/{sectionNumber}")
    @Operation(
            summary = "Get statute by title and section",
            description = "Returns a statute by its title number and section number"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Statute found"),
            @ApiResponse(responseCode = "404", description = "Statute not found",
                    content = @Content(schema = @Schema(hidden = true)))
    })
    public ResponseEntity<StatuteDTO> getByTitleAndSection(
            @Parameter(description = "Title number", example = "5")
            @PathVariable @Min(1) @Max(54) Integer titleNumber,

            @Parameter(description = "Section number", example = "101")
            @PathVariable String sectionNumber) {

        log.info("GET /api/statutes/title/{}/section/{}", titleNumber, sectionNumber);
        return statuteRepository.findByTitleNumberAndSectionNumber(titleNumber, sectionNumber)
                .map(statute -> ResponseEntity.ok(StatuteDTO.from(statute)))
                .orElse(ResponseEntity.notFound().build());
    }

    // =====================================================================
    // List by Title
    // =====================================================================

    @GetMapping("/title/{titleNumber}")
    @Operation(
            summary = "List statutes in a title",
            description = "Returns paginated statutes for a specific US Code title"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Statutes for the title"),
            @ApiResponse(responseCode = "400", description = "Invalid title number",
                    content = @Content(schema = @Schema(hidden = true)))
    })
    public Page<StatuteDTO> getByTitle(
            @Parameter(description = "Title number (1-54)", example = "5")
            @PathVariable @Min(1) @Max(54) Integer titleNumber,

            @Parameter(description = "Filter by chapter number")
            @RequestParam(required = false) String chapterNumber,

            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(defaultValue = "0") @Min(0) int page,

            @Parameter(description = "Page size (1-100)", example = "20")
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {

        log.info("GET /api/statutes/title/{} - chapter={}, page={}, size={}",
                titleNumber, chapterNumber, page, size);

        PageRequest pageRequest = PageRequest.of(page, size,
                Sort.by(Sort.Direction.ASC, "sectionNumber"));

        Page<Statute> statutes;
        if (chapterNumber != null) {
            statutes = statuteRepository.findByTitleNumberAndChapterNumber(titleNumber, chapterNumber, pageRequest);
        } else {
            statutes = statuteRepository.findByTitleNumber(titleNumber, pageRequest);
        }

        return statutes.map(StatuteDTO::forList);
    }

    // =====================================================================
    // Full-Text Search
    // =====================================================================

    @GetMapping("/search")
    @Operation(
            summary = "Search statutes",
            description = "Full-text search across statute content using PostgreSQL text search"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Search results"),
            @ApiResponse(responseCode = "400", description = "Invalid query or pagination parameters",
                    content = @Content(schema = @Schema(hidden = true)))
    })
    public Page<StatuteDTO> searchStatutes(
            @Parameter(description = "Search query", example = "executive departments", required = true)
            @RequestParam @NotBlank String q,

            @Parameter(description = "Filter by title number")
            @RequestParam(required = false) Integer titleNumber,

            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(defaultValue = "0") @Min(0) int page,

            @Parameter(description = "Page size (1-100)", example = "20")
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {

        log.info("GET /api/statutes/search - q='{}', titleNumber={}, page={}, size={}",
                q, titleNumber, page, size);

        PageRequest pageRequest = PageRequest.of(page, size);

        Page<Statute> statutes;
        if (titleNumber != null) {
            statutes = statuteRepository.searchByContentTextAndTitle(q, titleNumber, pageRequest);
        } else {
            statutes = statuteRepository.searchByContentText(q, pageRequest);
        }

        return statutes.map(StatuteDTO::forList);
    }

    // =====================================================================
    // Title Index
    // =====================================================================

    @GetMapping("/titles")
    @Operation(
            summary = "List all titles",
            description = "Returns distinct US Code title numbers and names"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Title list")
    })
    public List<TitleInfo> listTitles() {
        log.info("GET /api/statutes/titles");

        return statuteRepository.findDistinctTitles().stream()
                .map(row -> new TitleInfo(
                        (Integer) row[0],
                        (String) row[1],
                        statuteRepository.countByTitleNumber((Integer) row[0])
                ))
                .collect(Collectors.toList());
    }

    /**
     * Title information for index response.
     */
    public record TitleInfo(
            Integer titleNumber,
            String titleName,
            long sectionCount
    ) {}

    // =====================================================================
    // Hierarchy View (for Tree Display)
    // =====================================================================

    @GetMapping("/title/{titleNumber}/hierarchy")
    @Operation(
            summary = "Get title hierarchy for tree view",
            description = "Returns a hierarchical structure of chapters and sections for tree display"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Hierarchy returned"),
            @ApiResponse(responseCode = "404", description = "Title not found or has no sections",
                    content = @Content(schema = @Schema(hidden = true)))
    })
    public ResponseEntity<UsCodeHierarchyDTO> getTitleHierarchy(
            @Parameter(description = "Title number (1-54)", example = "5")
            @PathVariable @Min(1) @Max(54) Integer titleNumber) {

        log.info("GET /api/statutes/title/{}/hierarchy", titleNumber);

        List<Statute> statutes = statuteRepository.findByTitleNumber(titleNumber);

        if (statutes.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        UsCodeHierarchyDTO hierarchy = buildHierarchy(titleNumber, statutes);
        return ResponseEntity.ok(hierarchy);
    }

    /**
     * Build hierarchical structure from flat list of statutes.
     */
    private UsCodeHierarchyDTO buildHierarchy(Integer titleNumber, List<Statute> statutes) {
        // Get title name from first statute
        String titleName = statutes.isEmpty() ? null : statutes.get(0).getTitleName();

        // Group by chapter, preserving insertion order
        Map<String, List<Statute>> byChapter = new LinkedHashMap<>();
        for (Statute s : statutes) {
            String chapterKey = s.getChapterNumber() != null ? s.getChapterNumber() : "UNKNOWN";
            byChapter.computeIfAbsent(chapterKey, k -> new ArrayList<>()).add(s);
        }

        // Build chapter DTOs
        List<UsCodeHierarchyDTO.ChapterDTO> chapters = new ArrayList<>();
        for (Map.Entry<String, List<Statute>> entry : byChapter.entrySet()) {
            List<Statute> chapterStatutes = entry.getValue();
            String chapterName = chapterStatutes.isEmpty() ? null : chapterStatutes.get(0).getChapterName();

            // Sort sections by section number
            chapterStatutes.sort((a, b) -> compareSectionNumbers(a.getSectionNumber(), b.getSectionNumber()));

            List<UsCodeHierarchyDTO.SectionSummaryDTO> sections = chapterStatutes.stream()
                    .map(this::toSectionSummary)
                    .collect(Collectors.toList());

            chapters.add(UsCodeHierarchyDTO.ChapterDTO.builder()
                    .chapterNumber(entry.getKey())
                    .chapterName(chapterName)
                    .sectionCount(sections.size())
                    .sections(sections)
                    .build());
        }

        return UsCodeHierarchyDTO.builder()
                .titleNumber(titleNumber)
                .titleName(titleName)
                .sectionCount(statutes.size())
                .chapters(chapters)
                .build();
    }

    /**
     * Convert statute to section summary with truncated content preview.
     */
    private UsCodeHierarchyDTO.SectionSummaryDTO toSectionSummary(Statute statute) {
        String contentPreview = null;
        if (statute.getContentText() != null) {
            contentPreview = statute.getContentText().length() > 200
                    ? statute.getContentText().substring(0, 200) + "..."
                    : statute.getContentText();
        }

        return UsCodeHierarchyDTO.SectionSummaryDTO.builder()
                .id(statute.getId())
                .sectionNumber(statute.getSectionNumber())
                .heading(statute.getHeading())
                .contentPreview(contentPreview)
                .uscIdentifier(statute.getUscIdentifier())
                .build();
    }

    /**
     * Compare section numbers, handling numeric and alphanumeric patterns.
     * Examples: "101" < "102" < "102a" < "103"
     */
    private int compareSectionNumbers(String a, String b) {
        if (a == null && b == null) return 0;
        if (a == null) return -1;
        if (b == null) return 1;

        // Try numeric comparison first
        try {
            // Extract leading numeric part
            String numA = a.replaceAll("[^0-9].*", "");
            String numB = b.replaceAll("[^0-9].*", "");

            if (!numA.isEmpty() && !numB.isEmpty()) {
                int numCompare = Integer.compare(Integer.parseInt(numA), Integer.parseInt(numB));
                if (numCompare != 0) return numCompare;
            }
        } catch (NumberFormatException ignored) {
            // Fall through to string comparison
        }

        return a.compareToIgnoreCase(b);
    }

    // =====================================================================
    // Statistics
    // =====================================================================

    @GetMapping("/stats")
    @Operation(
            summary = "Get statute statistics",
            description = "Returns counts and statistics for imported statutes"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Statistics")
    })
    public StatuteStats getStats() {
        log.info("GET /api/statutes/stats");

        long total = statuteRepository.countAll();
        List<Object[]> byTitle = statuteRepository.countByTitle();
        List<String> releasePoints = statuteRepository.findDistinctReleasePoints();

        return new StatuteStats(
                total,
                byTitle.size(),
                releasePoints.isEmpty() ? null : releasePoints.get(0)
        );
    }

    /**
     * Statistics response record.
     */
    public record StatuteStats(
            long totalSections,
            int titlesLoaded,
            String latestReleasePoint
    ) {}
}
