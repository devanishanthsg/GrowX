package com.growx.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Request body for POST /api/crop-recommendation.
 * Matches the 7 form fields on the Crop Recommendation page.
 */
@Data
public class CropRecommendationRequest {

    @NotNull(message = "Nitrogen value is required")
    @DecimalMin(value = "0", message = "Nitrogen must be a positive value")
    private BigDecimal nitrogen;

    @NotNull(message = "Phosphorus value is required")
    @DecimalMin(value = "0", message = "Phosphorus must be a positive value")
    private BigDecimal phosphorus;

    @NotNull(message = "Potassium value is required")
    @DecimalMin(value = "0", message = "Potassium must be a positive value")
    private BigDecimal potassium;

    @NotNull(message = "Temperature is required")
    @DecimalMin(value = "-20", message = "Temperature is out of expected range")
    @DecimalMax(value = "60", message = "Temperature is out of expected range")
    private BigDecimal temperature;

    @NotNull(message = "Humidity is required")
    @DecimalMin(value = "0", message = "Humidity must be between 0 and 100")
    @DecimalMax(value = "100", message = "Humidity must be between 0 and 100")
    private BigDecimal humidity;

    @NotNull(message = "Soil pH is required")
    @DecimalMin(value = "0", message = "pH must be between 0 and 14")
    @DecimalMax(value = "14", message = "pH must be between 0 and 14")
    private BigDecimal ph;

    @NotNull(message = "Rainfall value is required")
    @DecimalMin(value = "0", message = "Rainfall must be a positive value")
    private BigDecimal rainfall;

    /**
     * The farm this recommendation is associated with.
     * Required to store the result and link it to the correct farm.
     */
    @NotNull(message = "Farm ID is required")
    private Long farmId;
}
