package com.growx.mapper;

import com.growx.dto.response.DiseaseDetectionResponse;
import com.growx.entity.DiseaseDetection;
import com.growx.enums.DiseaseSeverity;
import org.springframework.stereotype.Component;

/**
 * Converts DiseaseDetection entities to DiseaseDetectionResponse DTOs.
 */
@Component
public class DiseaseDetectionMapper {

    public DiseaseDetectionResponse toResponse(DiseaseDetection entity) {
        if (entity == null) return null;
        return DiseaseDetectionResponse.builder()
                .id(entity.getId())
                .disease(entity.getDiseaseName())
                .confidence(entity.getConfidence())
                .severity(entity.getSeverity())
                .severityLabel(toSeverityLabel(entity.getSeverity()))
                .treatment(entity.getTreatment())
                .prevention(entity.getPrevention())
                .farmId(entity.getFarm() != null ? entity.getFarm().getId() : null)
                .detectedAt(entity.getDetectedAt())
                .build();
    }

    private String toSeverityLabel(DiseaseSeverity severity) {
        if (severity == null) return null;
        return switch (severity) {
            case LOW -> "Low";
            case MODERATE -> "Moderate";
            case HIGH -> "High";
            case CRITICAL -> "Critical";
        };
    }
}
