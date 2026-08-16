package com.growx.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

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

    @NotNull(message = "Farm ID is required")
    private Long farmId;

    /*
     * Intended sowing month.
     *
     * 1  = January
     * 2  = February
     * ...
     * 12 = December
     */
    @NotNull(message = "Sowing month is required")
    @Min(value = 1, message = "Sowing month must be between 1 and 12")
    @Max(value = 12, message = "Sowing month must be between 1 and 12")
    private Integer sowingMonth;
}