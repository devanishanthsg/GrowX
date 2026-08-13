package com.growx.dto.response;

import com.growx.enums.ReportType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Report summary returned to the frontend Reports table.
 * Maps to the columns: Report (title), Type, Date, Status, Action (download).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportResponse {

    private Long id;
    private String title;
    private ReportType type;

    /** Display label for the type (e.g., "AI Prediction", "Disease Detection") */
    private String typeLabel;

    private String status;

    /** Whether a downloadable file is available */
    private boolean downloadable;

    private Long farmId;
    private LocalDateTime createdAt;
}
