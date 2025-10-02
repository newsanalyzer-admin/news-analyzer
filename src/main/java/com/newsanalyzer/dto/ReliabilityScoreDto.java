package com.newsanalyzer.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Data Transfer Object for source reliability information.
 *
 * Provides detailed reliability metrics for news sources including
 * historical accuracy, confidence scores, and scoring methodology.
 */
public class ReliabilityScoreDto {

    private BigDecimal score;
    private String level;
    private Long totalClaimsChecked;
    private Long accurateClaims;
    private Long inaccurateClaims;
    private Long misleadingClaims;
    private Double accuracyPercentage;
    private BigDecimal confidenceScore;
    private LocalDateTime lastUpdated;
    private String methodology;
    private ReliabilityTrend trend;

    // Constructors
    public ReliabilityScoreDto() {}

    public ReliabilityScoreDto(BigDecimal score, String level) {
        this.score = score;
        this.level = level;
    }

    // Static factory method for basic reliability score
    public static ReliabilityScoreDto basic(BigDecimal score) {
        ReliabilityScoreDto dto = new ReliabilityScoreDto();
        dto.setScore(score);
        dto.setLevel(calculateLevel(score));
        return dto;
    }

    // Helper method to calculate reliability level
    private static String calculateLevel(BigDecimal score) {
        if (score == null) return "Unrated";
        double value = score.doubleValue();
        if (value >= 0.9) return "Excellent";
        if (value >= 0.8) return "Good";
        if (value >= 0.7) return "Fair";
        if (value >= 0.5) return "Poor";
        return "Very Poor";
    }

    // Calculate accuracy percentage from claims data
    public void calculateAccuracyPercentage() {
        if (totalClaimsChecked != null && totalClaimsChecked > 0 && accurateClaims != null) {
            this.accuracyPercentage = (accurateClaims.doubleValue() / totalClaimsChecked.doubleValue()) * 100.0;
        }
    }

    // Getters and Setters
    public BigDecimal getScore() {
        return score;
    }

    public void setScore(BigDecimal score) {
        this.score = score;
        this.level = calculateLevel(score);
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public Long getTotalClaimsChecked() {
        return totalClaimsChecked;
    }

    public void setTotalClaimsChecked(Long totalClaimsChecked) {
        this.totalClaimsChecked = totalClaimsChecked;
        calculateAccuracyPercentage();
    }

    public Long getAccurateClaims() {
        return accurateClaims;
    }

    public void setAccurateClaims(Long accurateClaims) {
        this.accurateClaims = accurateClaims;
        calculateAccuracyPercentage();
    }

    public Long getInaccurateClaims() {
        return inaccurateClaims;
    }

    public void setInaccurateClaims(Long inaccurateClaims) {
        this.inaccurateClaims = inaccurateClaims;
    }

    public Long getMisleadingClaims() {
        return misleadingClaims;
    }

    public void setMisleadingClaims(Long misleadingClaims) {
        this.misleadingClaims = misleadingClaims;
    }

    public Double getAccuracyPercentage() {
        return accuracyPercentage;
    }

    public void setAccuracyPercentage(Double accuracyPercentage) {
        this.accuracyPercentage = accuracyPercentage;
    }

    public BigDecimal getConfidenceScore() {
        return confidenceScore;
    }

    public void setConfidenceScore(BigDecimal confidenceScore) {
        this.confidenceScore = confidenceScore;
    }

    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public String getMethodology() {
        return methodology;
    }

    public void setMethodology(String methodology) {
        this.methodology = methodology;
    }

    public ReliabilityTrend getTrend() {
        return trend;
    }

    public void setTrend(ReliabilityTrend trend) {
        this.trend = trend;
    }

    /**
     * Enum representing the trend of reliability over time
     */
    public enum ReliabilityTrend {
        IMPROVING("improving"),
        STABLE("stable"),
        DECLINING("declining"),
        INSUFFICIENT_DATA("insufficient_data");

        private final String value;

        ReliabilityTrend(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    @Override
    public String toString() {
        return "ReliabilityScoreDto{" +
                "score=" + score +
                ", level='" + level + '\'' +
                ", totalClaimsChecked=" + totalClaimsChecked +
                ", accuracyPercentage=" + accuracyPercentage +
                ", trend=" + trend +
                ", lastUpdated=" + lastUpdated +
                '}';
    }
}