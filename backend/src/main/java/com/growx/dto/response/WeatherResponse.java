package com.growx.dto.response;

import com.growx.dto.weather.AgriculturalWeatherDto;
import com.growx.dto.weather.CurrentWeatherDto;
import com.growx.dto.weather.DailyWeatherDto;
import com.growx.dto.weather.HourlyWeatherDto;
import com.growx.dto.weather.WeatherAdvisoryDto;
import com.growx.dto.weather.WeatherFeatureHintsDto;
import com.growx.dto.weather.WeatherMetadataDto;
import com.growx.enums.RiskLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Provider-neutral GrowX Agro Weather Intelligence response.
 * External-provider JSON never leaks past the WeatherApiClient layer.
 */
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class WeatherResponse {
    private Long farmId;
    private String farmName;
    private String locationName;
    private Double latitude;
    private Double longitude;
    private String timezone;
    private String coordinateSource;

    private CurrentWeatherDto current;
    private List<HourlyWeatherDto> hourly;
    private List<DailyWeatherDto> daily;
    private AgriculturalWeatherDto agriculture;
    private List<WeatherAdvisoryDto> advisories;

    private RiskLevel overallRisk;
    private String overallRiskReason;

    private WeatherFeatureHintsDto cropFeatureHints;
    private WeatherMetadataDto metadata;
    private List<String> warnings;
}
