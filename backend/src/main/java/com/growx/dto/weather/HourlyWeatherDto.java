package com.growx.dto.weather;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HourlyWeatherDto {
    private LocalDateTime timestamp;
    private Double temperature;
    private Double humidity;
    private Double precipitation;
    private Double precipitationProbability;
    private Double windSpeed;
    private Double windGust;
    private Integer weatherCode;
    private String conditionDescription;
    private String icon;
    private Double referenceEvapotranspiration;
    private Double modelledSoilMoisture;
}
