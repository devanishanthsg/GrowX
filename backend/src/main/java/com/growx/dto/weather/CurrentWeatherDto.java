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
public class CurrentWeatherDto {
    private LocalDateTime timestamp;
    private Double temperature;
    private Double feelsLike;
    private Double humidity;
    private Integer weatherCode;
    private String conditionDescription;
    private String icon;
    private Double windSpeed;
    private Double windGust;
    private Double precipitation;
    private Double rainProbability;
    private Double cloudCover;
    private Double uvIndex;
    private Double visibilityKm;
}
