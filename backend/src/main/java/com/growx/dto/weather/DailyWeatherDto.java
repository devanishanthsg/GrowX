package com.growx.dto.weather;

import com.growx.enums.RiskLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class DailyWeatherDto {
    private LocalDate date;
    private String day;
    private Integer weatherCode;
    private String conditionDescription;
    private String icon;
    private Double minTemperature;
    private Double maxTemperature;
    private Double averageHumidity;
    private Double rainProbability;
    private Double rainfallMm;
    private Double maxWindSpeed;
    private Double referenceEvapotranspiration;
    private RiskLevel farmRisk;
    private String farmRiskReason;
}
