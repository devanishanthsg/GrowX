package com.growx.entity;

import com.growx.enums.RecommendationStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Records a single crop recommendation request and its result.
 * Input fields (N, P, K, etc.) are stored alongside the ML result so that
 * the farmer can review what data produced which recommendation.
 */
@Entity
@Table(name = "crop_recommendations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CropRecommendation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ── Input parameters (from the frontend form) ────────────────────────────

    /** Nitrogen content in soil (mg/kg) */
    @Column(nullable = false, precision = 8, scale = 2)
    private BigDecimal nitrogen;

    /** Phosphorus content in soil (mg/kg) */
    @Column(nullable = false, precision = 8, scale = 2)
    private BigDecimal phosphorus;

    /** Potassium content in soil (mg/kg) */
    @Column(nullable = false, precision = 8, scale = 2)
    private BigDecimal potassium;

    /** Temperature in Celsius */
    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal temperature;

    /** Humidity percentage (0–100) */
    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal humidity;

    /** Soil pH value (0–14) */
    @Column(nullable = false, precision = 4, scale = 2)
    private BigDecimal ph;

    /** Rainfall in mm */
    @Column(nullable = false, precision = 8, scale = 2)
    private BigDecimal rainfall;

    // ── ML Result fields ──────────────────────────────────────────────────────

    @Column(length = 100)
    private String recommendedCrop;

    /** AI confidence percentage (0–100) */
    @Column(precision = 5, scale = 2)
    private BigDecimal confidence;

    /** Suitable season (e.g., "Kharif", "Rabi") */
    @Column(length = 50)
    private String season;

    /** Expected yield as a descriptive string (e.g., "5.8 tons/hectare") */
    @Column(length = 100)
    private String expectedYield;

    /** Natural-language explanation from the ML service */
    @Column(columnDefinition = "TEXT")
    private String explanation;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private RecommendationStatus status = RecommendationStatus.PENDING;

    // ── Relationships ─────────────────────────────────────────────────────────

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "farm_id", nullable = false)
    private Farm farm;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
