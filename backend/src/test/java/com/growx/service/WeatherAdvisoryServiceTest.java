package com.growx.service;

import com.growx.dto.response.WeatherResponse;
import com.growx.dto.weather.HourlyWeatherDto;
import com.growx.dto.weather.WeatherAdvisoryDto;
import com.growx.enums.RiskLevel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class WeatherAdvisoryServiceTest {

    private AgroWeatherService agroWeatherService;
    private WeatherAdvisoryService advisoryService;

    @BeforeEach
    void setUp() {
        agroWeatherService = new AgroWeatherService();
        advisoryService = new WeatherAdvisoryService();
    }

    @Test
    void meaningfulRainDelaysIrrigation() {
        WeatherResponse weather = weatherWith(hours(24, 27, 78, 0.6, 75, 8, 12, 0.12, 0.24));

        WeatherAdvisoryDto irrigation = advisory(weather, "IRRIGATION");

        assertEquals("DELAY", irrigation.getStatus());
        assertNotNull(irrigation.getMetrics().get("forecastRain24hMm"));
    }

    @Test
    void rainAndStrongWindMakeSprayingPoor() {
        WeatherResponse weather = weatherWith(hours(24, 28, 72, 0.2, 70, 24, 34, 0.10, 0.25));

        WeatherAdvisoryDto spraying = advisory(weather, "SPRAYING");

        assertEquals("NOT_RECOMMENDED", spraying.getStatus());
        assertEquals(RiskLevel.HIGH, spraying.getSeverity());
    }

    @Test
    void sustainedHumidityAndWetConditionsElevateDiseaseWeatherRisk() {
        WeatherResponse weather = weatherWith(hours(24, 27, 90, 0.3, 65, 7, 11, 0.08, 0.31));

        WeatherAdvisoryDto disease = advisory(weather, "DISEASE_RISK");

        assertEquals("HIGH", disease.getStatus());
        assertEquals(RiskLevel.HIGH, disease.getSeverity());
    }

    @Test
    void veryHotForecastElevatesHeatStress() {
        WeatherResponse weather = weatherWith(hours(24, 39, 55, 0.0, 5, 8, 12, 0.30, 0.15));

        WeatherAdvisoryDto heat = advisory(weather, "HEAT_STRESS");

        assertEquals("HIGH", heat.getStatus());
        assertEquals(RiskLevel.HIGH, heat.getSeverity());
    }

    private WeatherAdvisoryDto advisory(WeatherResponse weather, String type) {
        AgroWeatherService.AgroWeatherSummary summary = agroWeatherService.summarize(weather);
        return advisoryService.build(weather, summary).advisories().stream()
                .filter(item -> type.equals(item.getType()))
                .findFirst()
                .orElseThrow();
    }

    private WeatherResponse weatherWith(List<HourlyWeatherDto> hourly) {
        return WeatherResponse.builder()
                .hourly(hourly)
                .daily(List.of())
                .build();
    }

    private List<HourlyWeatherDto> hours(
            int count,
            double temperature,
            double humidity,
            double precipitation,
            double precipitationProbability,
            double windSpeed,
            double windGust,
            double et0,
            double soilMoisture
    ) {
        List<HourlyWeatherDto> result = new ArrayList<>();
        LocalDateTime start = LocalDateTime.of(2026, 8, 19, 6, 0);
        for (int i = 0; i < count; i++) {
            result.add(HourlyWeatherDto.builder()
                    .timestamp(start.plusHours(i))
                    .temperature(temperature)
                    .humidity(humidity)
                    .precipitation(precipitation)
                    .precipitationProbability(precipitationProbability)
                    .windSpeed(windSpeed)
                    .windGust(windGust)
                    .referenceEvapotranspiration(et0)
                    .modelledSoilMoisture(soilMoisture)
                    .build());
        }
        return result;
    }
}
