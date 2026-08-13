package com.growx.dto.response;

import com.growx.enums.DiseaseSeverity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Disease detection result returned to the frontend.
 * Maps to the result panel of the Disease Detection page:
 * disease name, confidence, severity, treatment, prevention.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiseaseDetectionResponse {

    private Long id;

    /** Name of the detected disease, e.g. "Leaf Blight" */
    private String disease;

    /** AI confidence percentage 0–100 */
    private BigDecimal confidence;

    /** Severity level */
    private DiseaseSeverity severity;

    /** Severity as a display string (LOW → "Low") */
    private String severityLabel;

    /** Suggested treatment text */
    private String treatment;

    /** Prevention guidance text */
    private String prevention;

    private Long farmId;
    private LocalDateTime detectedAt;
}
