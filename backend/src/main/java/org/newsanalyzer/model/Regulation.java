package org.newsanalyzer.model;

import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Represents a Federal Register regulatory document.
 * Stores rules, proposed rules, notices, and presidential documents.
 *
 * @author James (Dev Agent)
 * @since 2.0.0
 */
@jakarta.persistence.Entity
@Table(name = "regulations",
       indexes = {
           @Index(name = "idx_regulation_document_number", columnList = "document_number"),
           @Index(name = "idx_regulation_publication_date", columnList = "publication_date"),
           @Index(name = "idx_regulation_effective_on", columnList = "effective_on"),
           @Index(name = "idx_regulation_document_type", columnList = "document_type")
       })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Regulation {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    /**
     * Federal Register document number (unique identifier).
     * Example: "2024-12345"
     */
    @Column(name = "document_number", unique = true, nullable = false, length = 50)
    private String documentNumber;

    /**
     * Title of the regulation.
     */
    @Column(name = "title", nullable = false, length = 1000)
    private String title;

    /**
     * Abstract/summary of the regulation.
     * Named documentAbstract to avoid Java keyword conflict.
     */
    @Column(name = "abstract", columnDefinition = "TEXT")
    private String documentAbstract;

    /**
     * Type of document (RULE, PROPOSED_RULE, NOTICE, etc.).
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "document_type", nullable = false, length = 30)
    private DocumentType documentType;

    /**
     * Date the document was published in the Federal Register.
     */
    @Column(name = "publication_date", nullable = false)
    private LocalDate publicationDate;

    /**
     * Date the rule becomes effective (for final rules).
     */
    @Column(name = "effective_on")
    private LocalDate effectiveOn;

    /**
     * Date the document was signed (for presidential documents).
     */
    @Column(name = "signing_date")
    private LocalDate signingDate;

    /**
     * Regulation Identifier Number (RIN) assigned by OMB.
     */
    @Column(name = "regulation_id_number", length = 20)
    private String regulationIdNumber;

    /**
     * Code of Federal Regulations references (stored as JSONB).
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "cfr_references", columnDefinition = "jsonb")
    private List<CfrReference> cfrReferences;

    /**
     * Docket IDs associated with the regulation (stored as JSONB).
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "docket_ids", columnDefinition = "jsonb")
    private List<String> docketIds;

    /**
     * URL to the Federal Register source page.
     */
    @Column(name = "source_url", length = 500)
    private String sourceUrl;

    /**
     * URL to the PDF version.
     */
    @Column(name = "pdf_url", length = 500)
    private String pdfUrl;

    /**
     * URL to the HTML version.
     */
    @Column(name = "html_url", length = 500)
    private String htmlUrl;

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
