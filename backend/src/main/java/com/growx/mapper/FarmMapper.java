package com.growx.mapper;

import com.growx.dto.response.FarmResponse;
import com.growx.entity.Farm;
import org.springframework.stereotype.Component;

/**
 * Converts Farm entities to FarmResponse DTOs.
 */
@Component
public class FarmMapper {

    public FarmResponse toResponse(Farm farm) {
        if (farm == null) return null;
        return FarmResponse.builder()
                .id(farm.getId())
                .farmName(farm.getFarmName())
                .location(farm.getLocation())
                .latitude(farm.getLatitude())
                .longitude(farm.getLongitude())
                .area(farm.getArea())
                .areaUnit(farm.getAreaUnit())
                .soilType(farm.getSoilType())
                .mainCrop(farm.getMainCrop())
                .ownerId(farm.getOwner() != null ? farm.getOwner().getId() : null)
                .createdAt(farm.getCreatedAt())
                .updatedAt(farm.getUpdatedAt())
                .build();
    }
}
