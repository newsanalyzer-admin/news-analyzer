package com.newsanalyzer.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity representing a news source with reliability and bias metrics.
 *
 * This entity tracks news outlets, blogs, and other information sources,
 * maintaining historical accuracy scores and political bias indicators.
 */
@Entity
@Table(name = "news_sources")
@EntityListeners(AuditingEntityListener.class)
public class NewsSource {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "source_id")
    private UUID sourceId;

    @NotBlank(message = "Source name is required")
    @Size(max = 255, message = "Source name must not exceed 255 characters")
    @Column(name = "source_name", nullable = false)
    private String sourceName;

    @Size(max = 255, message = "Domain must not exceed 255 characters")
    @Column(name = "domain", unique = true)
    private String domain;

    @DecimalMin(value = "0.0", message = "Reliability score must be between 0.0 and 1.0")
    @DecimalMax(value = "1.0", message = "Reliability score must be between 0.0 and 1.0")
    @Column(name = "reliability_score", precision = 3, scale = 2)
    private BigDecimal reliabilityScore;

    @DecimalMin(value = "-1.0", message = "Political bias score must be between -1.0 and 1.0")
    @DecimalMax(value = "1.0", message = "Political bias score must be between -1.0 and 1.0")
    @Column(name = "political_bias_score", precision = 3, scale = 2)
    private BigDecimal politicalBiasScore;

    @Column(name = "founded_date")
    private LocalDate foundedDate;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_type")
    private SourceType sourceType = SourceType.NEWS;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Constructors
    public NewsSource() {}

    public NewsSource(String sourceName, String domain) {
        this.sourceName = sourceName;
        this.domain = domain;
    }

    // Getters and Setters
    public UUID getSourceId() {
        return sourceId;
    }

    public void setSourceId(UUID sourceId) {
        this.sourceId = sourceId;
    }

    public String getSourceName() {
        return sourceName;
    }

    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public BigDecimal getReliabilityScore() {
        return reliabilityScore;
    }

    public void setReliabilityScore(BigDecimal reliabilityScore) {
        this.reliabilityScore = reliabilityScore;
    }

    public BigDecimal getPoliticalBiasScore() {
        return politicalBiasScore;
    }

    public void setPoliticalBiasScore(BigDecimal politicalBiasScore) {
        this.politicalBiasScore = politicalBiasScore;
    }

    public LocalDate getFoundedDate() {
        return foundedDate;
    }

    public void setFoundedDate(LocalDate foundedDate) {
        this.foundedDate = foundedDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public SourceType getSourceType() {
        return sourceType;
    }

    public void setSourceType(SourceType sourceType) {
        this.sourceType = sourceType;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    /**
     * Enum defining different types of news sources
     */
    public enum SourceType {
        NEWS("news"),
        BLOG("blog"),
        SOCIAL_MEDIA("social_media"),
        GOVERNMENT("government"),
        ACADEMIC("academic");

        private final String value;

        SourceType(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NewsSource)) return false;
        NewsSource that = (NewsSource) o;
        return sourceId != null && sourceId.equals(that.sourceId);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "NewsSource{" +
                "sourceId=" + sourceId +
                ", sourceName='" + sourceName + '\'' +
                ", domain='" + domain + '\'' +
                ", reliabilityScore=" + reliabilityScore +
                ", sourceType=" + sourceType +
                ", isActive=" + isActive +
                '}';
    }
}