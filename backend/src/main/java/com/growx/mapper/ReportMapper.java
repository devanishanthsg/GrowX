package com.growx.mapper;

import com.growx.dto.response.ReportResponse;
import com.growx.entity.Report;
import com.growx.enums.ReportType;
import org.springframework.stereotype.Component;

/**
 * Converts Report entities to ReportResponse DTOs.
 */
@Component
public class ReportMapper {

    public ReportResponse toResponse(Report entity) {
        if (entity == null) return null;
        return ReportResponse.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .type(entity.getType())
                .typeLabel(toTypeLabel(entity.getType()))
                .status(entity.getStatus())
                .downloadable(entity.getFilePath() != null)
                .farmId(entity.getFarm() != null ? entity.getFarm().getId() : null)
                .createdAt(entity.getCreatedAt())
                .build();
    }

    private String toTypeLabel(ReportType type) {
        if (type == null) return null;
        return switch (type) {
            case CROP_RECOMMENDATION -> "AI Prediction";
            case DISEASE_DETECTION -> "Disease Detection";
            case FARM_SUMMARY -> "Farm Report";
            case WEATHER -> "Weather Report";
        };
    }
}
