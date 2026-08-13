package com.growx.entity;

import com.growx.enums.ReportType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Represents a generated report in the GrowX platform.
 * Reports are created automatically when recommendations or detections complete,
 * or manually via "Generate Report" on the Reports page.
 */
@Entity
@Table(name = "reports")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ReportType type;

    /**
     * Processing / availability status.
     * Simple string for flexibility: "Completed", "Processing", "Failed".
     */
    @Column(nullable = false, length = 30)
    @Builder.Default
    private String status = "Completed";

    /**
     * Optional path to a generated PDF file, if PDF generation is implemented.
     * Null until the PDF is produced.
     */
    @Column(length = 500)
    private String filePath;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "farm_id", nullable = false)
    private Farm farm;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
