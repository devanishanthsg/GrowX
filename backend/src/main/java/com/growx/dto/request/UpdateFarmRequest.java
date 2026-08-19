package com.growx.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
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

    @DecimalMin(value = "-90.0", message = "Latitude must be between -90 and 90")
    @DecimalMax(value = "90.0", message = "Latitude must be between -90 and 90")
    private Double latitude;

    @DecimalMin(value = "-180.0", message = "Longitude must be between -180 and 180")
    @DecimalMax(value = "180.0", message = "Longitude must be between -180 and 180")
    private Double longitude;
    private Double area;
    private String areaUnit;
    private String soilType;
    private String mainCrop;
}
