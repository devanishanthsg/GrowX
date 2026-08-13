package com.growx.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a farm owned by a GrowX user.
 * Kept separate from User so one user can eventually own multiple farms.
 * Farm location drives weather data; soilType and mainCrop support crop recommendations.
 */
@Entity
@Table(name = "farms")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Farm {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String farmName;

    /**
     * Human-readable location string (e.g. "Coimbatore, Tamil Nadu").
     * Used as the label when displaying weather.
     */
    @Column(nullable = false, length = 200)
    private String location;

    /** Geographic coordinates — used to call the weather API. */
    @Column(precision = 9, scale = 6)
    private BigDecimal latitude;

    @Column(precision = 9, scale = 6)
    private BigDecimal longitude;

    /** Farm area — number only; unit stored in areaUnit. */
    @Column(precision = 10, scale = 2)
    private BigDecimal area;

    /** Unit of area measurement: e.g. "acres", "hectares". */
    @Column(length = 20)
    @Builder.Default
    private String areaUnit = "acres";

    @Column(length = 50)
    private String soilType;

    @Column(length = 100)
    private String mainCrop;

    /** The farmer who owns this farm. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User owner;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "farm", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<CropRecommendation> cropRecommendations = new ArrayList<>();

    @OneToMany(mappedBy = "farm", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<DiseaseDetection> diseaseDetections = new ArrayList<>();

    @OneToMany(mappedBy = "farm", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Report> reports = new ArrayList<>();
}
