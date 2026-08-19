package com.growx.dto.weather;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WeatherFeatureHintsDto {
    private Double temperature;
    private String temperatureSource;
    private Double humidity;
    private String humiditySource;
    private Double rainfall;
    private boolean rainfallAutofillEnabled;
    private String rainfallMessage;
    private String compatibilityWarning;
}
