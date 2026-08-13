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
 * Maps to what the Crop Recommendation page displays in the result panel.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CropRecommendationResponse {

    private Long id;

    // ── Input parameters (echoed back) ────────────────────────────────────────
    private BigDecimal nitrogen;
    private BigDecimal phosphorus;
    private BigDecimal potassium;
    private BigDecimal temperature;
    private BigDecimal humidity;
    private BigDecimal ph;
    private BigDecimal rainfall;

    // ── ML Result ─────────────────────────────────────────────────────────────

    /** Recommended crop name, e.g. "Rice" */
    private String recommendedCrop;

    /** AI confidence percentage 0–100 */
    private BigDecimal confidence;

    /** Suitable season, e.g. "Kharif" */
    private String season;

    /** Expected yield string, e.g. "5.8 tons/hectare" */
    private String expectedYield;

    /** Natural-language explanation */
    private String explanation;

    private RecommendationStatus status;

    private Long farmId;
    private LocalDateTime createdAt;
}
