package com.newsanalyzer.dto;

import com.newsanalyzer.model.NewsSource;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Data Transfer Object for NewsSource entities.
 *
 * Used for API requests and responses to transfer news source data
 * while maintaining separation between the API layer and domain model.
 */
public class NewsSourceDto {

    private UUID sourceId;

    @NotBlank(message = "Source name is required")
    @Size(max = 255, message = "Source name must not exceed 255 characters")
    private String sourceName;

    @Size(max = 255, message = "Domain must not exceed 255 characters")
    private String domain;

    @DecimalMin(value = "0.0", message = "Reliability score must be between 0.0 and 1.0")
    @DecimalMax(value = "1.0", message = "Reliability score must be between 0.0 and 1.0")
    private BigDecimal reliabilityScore;

    @DecimalMin(value = "-1.0", message = "Political bias score must be between -1.0 and 1.0")
    @DecimalMax(value = "1.0", message = "Political bias score must be between -1.0 and 1.0")
    private BigDecimal politicalBiasScore;

    private LocalDate foundedDate;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    private NewsSource.SourceType sourceType;

    private Boolean isActive;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    // Additional fields for API responses
    private Long totalClaimsChecked;
    private Double accuracyPercentage;
    private String reliabilityLevel;
    private String biasLevel;

    // Constructors
    public NewsSourceDto() {}

    public NewsSourceDto(String sourceName, String domain) {
        this.sourceName = sourceName;
        this.domain = domain;
        this.isActive = true;
        this.sourceType = NewsSource.SourceType.NEWS;
    }

    // Static factory method to create from entity
    public static NewsSourceDto fromEntity(NewsSource newsSource) {
        NewsSourceDto dto = new NewsSourceDto();
        dto.setSourceId(newsSource.getSourceId());
        dto.setSourceName(newsSource.getSourceName());
        dto.setDomain(newsSource.getDomain());
        dto.setReliabilityScore(newsSource.getReliabilityScore());
        dto.setPoliticalBiasScore(newsSource.getPoliticalBiasScore());
        dto.setFoundedDate(newsSource.getFoundedDate());
        dto.setDescription(newsSource.getDescription());
        dto.setSourceType(newsSource.getSourceType());
        dto.setIsActive(newsSource.getIsActive());
        dto.setCreatedAt(newsSource.getCreatedAt());
        dto.setUpdatedAt(newsSource.getUpdatedAt());

        // Set derived fields
        dto.setReliabilityLevel(calculateReliabilityLevel(newsSource.getReliabilityScore()));
        dto.setBiasLevel(calculateBiasLevel(newsSource.getPoliticalBiasScore()));

        return dto;
    }

    // Convert to entity
    public NewsSource toEntity() {
        NewsSource newsSource = new NewsSource();
        newsSource.setSourceId(this.sourceId);
        newsSource.setSourceName(this.sourceName);
        newsSource.setDomain(this.domain);
        newsSource.setReliabilityScore(this.reliabilityScore);
        newsSource.setPoliticalBiasScore(this.politicalBiasScore);
        newsSource.setFoundedDate(this.foundedDate);
        newsSource.setDescription(this.description);
        newsSource.setSourceType(this.sourceType != null ? this.sourceType : NewsSource.SourceType.NEWS);
        newsSource.setIsActive(this.isActive != null ? this.isActive : true);
        return newsSource;
    }

    // Helper methods for calculated fields
    private static String calculateReliabilityLevel(BigDecimal score) {
        if (score == null) return "Unrated";
        double value = score.doubleValue();
        if (value >= 0.9) return "Excellent";
        if (value >= 0.8) return "Good";
        if (value >= 0.7) return "Fair";
        if (value >= 0.5) return "Poor";
        return "Very Poor";
    }

    private static String calculateBiasLevel(BigDecimal score) {
        if (score == null) return "Unknown";
        double value = Math.abs(score.doubleValue());
        if (value <= 0.1) return "Minimal";
        if (value <= 0.3) return "Slight";
        if (value <= 0.5) return "Moderate";
        if (value <= 0.7) return "High";
        return "Extreme";
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

    public NewsSource.SourceType getSourceType() {
        return sourceType;
    }

    public void setSourceType(NewsSource.SourceType sourceType) {
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

    public Long getTotalClaimsChecked() {
        return totalClaimsChecked;
    }

    public void setTotalClaimsChecked(Long totalClaimsChecked) {
        this.totalClaimsChecked = totalClaimsChecked;
    }

    public Double getAccuracyPercentage() {
        return accuracyPercentage;
    }

    public void setAccuracyPercentage(Double accuracyPercentage) {
        this.accuracyPercentage = accuracyPercentage;
    }

    public String getReliabilityLevel() {
        return reliabilityLevel;
    }

    public void setReliabilityLevel(String reliabilityLevel) {
        this.reliabilityLevel = reliabilityLevel;
    }

    public String getBiasLevel() {
        return biasLevel;
    }

    public void setBiasLevel(String biasLevel) {
        this.biasLevel = biasLevel;
    }

    @Override
    public String toString() {
        return "NewsSourceDto{" +
                "sourceId=" + sourceId +
                ", sourceName='" + sourceName + '\'' +
                ", domain='" + domain + '\'' +
                ", reliabilityScore=" + reliabilityScore +
                ", sourceType=" + sourceType +
                ", reliabilityLevel='" + reliabilityLevel + '\'' +
                ", biasLevel='" + biasLevel + '\'' +
                '}';
    }
}