package com.growx.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Request body for POST /api/farms.
 */
@Data
public class CreateFarmRequest {

    @NotBlank(message = "Farm name is required")
    @Size(max = 150, message = "Farm name must be at most 150 characters")
    private String farmName;

    @NotBlank(message = "Location is required")
    @Size(max = 200, message = "Location must be at most 200 characters")
    private String location;

    private Double latitude;
    private Double longitude;
    private Double area;
    private String areaUnit;
    private String soilType;
    private String mainCrop;
}
