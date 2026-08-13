package com.growx.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Request body for PUT /api/farms/{id}.
 * All fields are optional — only non-null values will be applied.
 */
@Data
public class UpdateFarmRequest {

    @Size(max = 150, message = "Farm name must be at most 150 characters")
    private String farmName;

    @Size(max = 200, message = "Location must be at most 200 characters")
    private String location;

    private Double latitude;
    private Double longitude;
    private Double area;
    private String areaUnit;
    private String soilType;
    private String mainCrop;
}
