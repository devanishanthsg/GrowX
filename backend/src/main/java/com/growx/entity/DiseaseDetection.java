package com.growx.entity;

import com.growx.enums.DiseaseSeverity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Records a single plant disease detection event.
 * The uploaded image is stored on disk (or eventually cloud storage);
 * imagePath holds the relative path or object key.
 */
@Entity
@Table(name = "disease_detections")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DiseaseDetection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Relative path or storage key of the uploaded image.
     * Example: "disease-images/farm-12/2026-08-13_leaf.jpg"
     */
    @Column(nullable = false, length = 500)
    private String imagePath;

    // ── ML Result fields ──────────────────────────────────────────────────────

    @Column(length = 150)
    private String diseaseName;

    /** AI confidence percentage (0–100) */
    @Column(precision = 5, scale = 2)
    private BigDecimal confidence;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private DiseaseSeverity severity;

    @Column(columnDefinition = "TEXT")
    private String treatment;

    @Column(columnDefinition = "TEXT")
    private String prevention;

    // ── Relationships ─────────────────────────────────────────────────────────

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "farm_id", nullable = false)
    private Farm farm;

    /** Timestamp of when the detection was performed. */
    @Column(nullable = false, updatable = false)
    private LocalDateTime detectedAt;

    @PrePersist
    protected void onPersist() {
        if (detectedAt == null) {
            detectedAt = LocalDateTime.now();
        }
    }
}
