package com.growx.dto.response;

import com.growx.enums.RecommendationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Crop recommendation result returned to the frontend.
 *
 * V2 adds reliability metadata so the frontend can distinguish:
 * - a real recommendation
 * - a low-confidence candidate ranking
 * - an out-of-distribution result
 *
 * These V2 reliability fields are currently response-only metadata and
 * are not persisted in the CropRecommendation entity.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CropRecommendationResponse {

    private Long id;

    // ------------------------------------------------
    // Input parameters
    // ------------------------------------------------

    private BigDecimal nitrogen;
    private BigDecimal phosphorus;
    private BigDecimal potassium;
    private BigDecimal temperature;
    private BigDecimal humidity;
    private BigDecimal ph;
    private BigDecimal rainfall;

    // ------------------------------------------------
    // Crop ML prediction
    // ------------------------------------------------

    /**
     * True only when the AI reliability gates approve a firm recommendation.
     */
    private Boolean recommendationAvailable;

    /**
     * Human-readable explanation of whether a firm recommendation was issued.
     */
    private String recommendationMessage;

    /**
     * Recommended crop name.
     *
     * IMPORTANT:
     * This is null when recommendationAvailable is false.
     */
    private String recommendedCrop;

    /**
     * Highest-ranked raw model candidate.
     *
     * This is NOT automatically a recommendation.
     */
    private CropCandidate topCandidate;

    /**
     * Top ranked model candidates for transparency.
     */
    private List<CropCandidate> candidates;

    /**
     * Existing alternative predictions returned by the AI service.
     */
    private List<CropCandidate> alternatives;

    /**
     * Highest model probability, percentage 0-100.
     *
     * For an OOD/uncertain case this is the top candidate's score,
     * not the confidence of an approved recommendation.
     */
    private BigDecimal confidence;

    /**
     * Difference between the first and second ranked candidate scores,
     * percentage points.
     */
    private BigDecimal confidenceMargin;

    // ------------------------------------------------
    // Reliability / domain checks
    // ------------------------------------------------

    /**
     * Examples:
     * CONFIDENT
     * LOW_CONFIDENCE
     * UNCERTAIN
     * OUT_OF_DISTRIBUTION
     */
    private String reliabilityStatus;

    private String reliabilityMessage;

    /**
     * Examples:
     * IN_DISTRIBUTION
     * OUT_OF_DISTRIBUTION
     */
    private String domainStatus;

    /**
     * Distance-like domain score produced by the V2 input-domain detector.
     */
    private BigDecimal domainScore;

    /**
     * Individual features outside the training data's observed ranges.
     */
    private List<String> domainOutsideFeatures;

    // ------------------------------------------------
    // Season information
    // ------------------------------------------------

    private String season;
    private String seasonStatus;
    private String seasonMessage;

    // ------------------------------------------------
    // Yield information
    // ------------------------------------------------

    private String expectedYield;
    private String yieldStatus;
    private String yieldMessage;

    // ------------------------------------------------
    // Explainability
    // ------------------------------------------------

    private String explanation;

    // ------------------------------------------------
    // Model metadata
    // ------------------------------------------------

    private String modelVersion;

    // ------------------------------------------------
    // Backend persistence status
    // ------------------------------------------------

    private RecommendationStatus status;

    // ------------------------------------------------
    // Relationships / metadata
    // ------------------------------------------------

    private Long farmId;
    private LocalDateTime createdAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CropCandidate {
        private String crop;
        private BigDecimal confidence;
    }
}
