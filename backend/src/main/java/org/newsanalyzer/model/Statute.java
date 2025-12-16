package org.newsanalyzer.model;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Represents a US Code statutory section.
 * US Code is organized hierarchically: Title → Chapter → Section
 * Data imported from uscode.house.gov (Office of Law Revision Counsel).
 *
 * @author James (Dev Agent)
 * @since 2.0.0
 */
@jakarta.persistence.Entity
@Table(name = "statutes",
       indexes = {
           @Index(name = "idx_statutes_title", columnList = "title_number"),
           @Index(name = "idx_statutes_chapter", columnList = "title_number, chapter_number"),
           @Index(name = "idx_statutes_section", columnList = "section_number")
       })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Statute {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    /**
     * Unique identifier in USC citation format.
     * Example: "/us/usc/t5/s101" for Title 5, Section 101.
     */
    @Column(name = "usc_identifier", unique = true, nullable = false, length = 100)
    private String uscIdentifier;

    /**
     * US Code title number (1-54).
     */
    @Column(name = "title_number", nullable = false)
    private Integer titleNumber;

    /**
     * Title name/heading.
     * Example: "GOVERNMENT ORGANIZATION AND EMPLOYEES"
     */
    @Column(name = "title_name", length = 500)
    private String titleName;

    /**
     * Chapter number within the title.
     * May be alphanumeric (e.g., "5A").
     */
    @Column(name = "chapter_number", length = 20)
    private String chapterNumber;

    /**
     * Chapter name/heading.
     */
    @Column(name = "chapter_name", length = 500)
    private String chapterName;

    /**
     * Section number within the title.
     * May include letters/symbols (e.g., "101", "101a", "101-1").
     */
    @Column(name = "section_number", nullable = false, length = 50)
    private String sectionNumber;

    /**
     * Section heading/title.
     * Example: "Executive departments"
     */
    @Column(name = "heading", length = 1000)
    private String heading;

    /**
     * Plain text content of the section (for search/display).
     */
    @Column(name = "content_text", columnDefinition = "TEXT")
    private String contentText;

    /**
     * Original USLM XML content (for preserving structure).
     */
    @Column(name = "content_xml", columnDefinition = "TEXT")
    private String contentXml;

    /**
     * Source credit line (legislative history).
     * Example: "(Pub. L. 89–554, Sept. 6, 1966, 80 Stat. 378.)"
     * Note: Can be very long for frequently amended sections.
     */
    @Column(name = "source_credit", columnDefinition = "TEXT")
    private String sourceCredit;

    /**
     * URL to the official source at uscode.house.gov.
     */
    @Column(name = "source_url", length = 500)
    private String sourceUrl;

    /**
     * Release point identifier for tracking updates.
     * Example: "@119-22" for 119th Congress, 22nd release.
     */
    @Column(name = "release_point", length = 20)
    private String releasePoint;

    /**
     * Effective date of this version (if specified).
     */
    @Column(name = "effective_date")
    private LocalDate effectiveDate;

    /**
     * Data import source identifier.
     * Value: 'USCODE' for uscode.house.gov imports.
     */
    @Column(name = "import_source", length = 50)
    private String importSource;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
