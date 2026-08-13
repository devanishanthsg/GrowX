package com.growx.mapper;

import com.growx.dto.response.CropRecommendationResponse;
import com.growx.entity.CropRecommendation;
import org.springframework.stereotype.Component;

/**
 * Converts CropRecommendation entities to CropRecommendationResponse DTOs.
 */
@Component
public class CropRecommendationMapper {

    public CropRecommendationResponse toResponse(CropRecommendation entity) {
        if (entity == null) return null;
        return CropRecommendationResponse.builder()
                .id(entity.getId())
                .nitrogen(entity.getNitrogen())
                .phosphorus(entity.getPhosphorus())
                .potassium(entity.getPotassium())
                .temperature(entity.getTemperature())
                .humidity(entity.getHumidity())
                .ph(entity.getPh())
                .rainfall(entity.getRainfall())
                .recommendedCrop(entity.getRecommendedCrop())
                .confidence(entity.getConfidence())
                .season(entity.getSeason())
                .expectedYield(entity.getExpectedYield())
                .explanation(entity.getExplanation())
                .status(entity.getStatus())
                .farmId(entity.getFarm() != null ? entity.getFarm().getId() : null)
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
