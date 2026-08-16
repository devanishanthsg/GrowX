package com.growx.dto.response;

import com.growx.enums.RecommendationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Crop recommendation result returned to the frontend.
 *
 * Includes:
 * - ML crop prediction
 * - confidence score
 * - season verification metadata
 * - yield reference metadata
 * - explanation
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
     * Recommended crop name.
     * Example: rice, banana, maize
     */
    private String recommendedCrop;

    /**
     * AI confidence percentage.
     * Range: 0 - 100
     */
    private BigDecimal confidence;


    // ------------------------------------------------
    // Season information
    // ------------------------------------------------

    /**
     * Verified season when available.
     * Example: Early Samba
     */
    private String season;

    /**
     * Season-engine status.
     *
     * Examples:
     * VERIFIED
     * NO_MATCH
     * NOT_VERIFIED
     * LOCATION_REQUIRED
     * MONTH_REQUIRED
     */
    private String seasonStatus;

    /**
     * Human-readable season-engine explanation.
     */
    private String seasonMessage;


    // ------------------------------------------------
    // Yield information
    // ------------------------------------------------

    /**
     * Reference yield range.
     *
     * Example:
     * 4.0-6.0 tonnes/hectare
     *
     * This is a reference range,
     * not a guaranteed AI yield prediction.
     */
    private String expectedYield;

    /**
     * Yield-engine status.
     *
     * Examples:
     * REFERENCE_AVAILABLE
     * NOT_VERIFIED
     * LOCATION_REQUIRED
     * SEASON_REQUIRED
     */
    private String yieldStatus;

    /**
     * Human-readable yield-engine explanation.
     */
    private String yieldMessage;


    // ------------------------------------------------
    // Explainability
    // ------------------------------------------------

    /**
     * Explanation describing why the crop
     * was recommended.
     */
    private String explanation;


    // ------------------------------------------------
    // Model metadata
    // ------------------------------------------------

    /**
     * Python AI model version.
     * Example: 1.0.0
     */
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
}