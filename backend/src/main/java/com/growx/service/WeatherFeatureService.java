package com.growx.service;

import com.growx.dto.response.WeatherResponse;
import com.growx.dto.weather.DailyWeatherDto;
import com.growx.dto.weather.WeatherFeatureHintsDto;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * Produces weather-derived helper values for the existing crop recommendation form.
 *
 * Rainfall is deliberately NOT auto-filled: the repository's crop training data does
 * not document the rainfall accumulation period (daily/monthly/seasonal/annual), so a
 * 24-hour or 7-day forecast total would be semantically unsafe to substitute.
 */
@Service
public class WeatherFeatureService {

    public WeatherFeatureHintsDto buildHints(WeatherResponse weather) {
        List<DailyWeatherDto> daily = weather.getDaily() == null ? List.of() : weather.getDaily();

        Double representativeTemperature = daily.stream()
                .filter(day -> day.getMinTemperature() != null && day.getMaxTemperature() != null)
                .mapToDouble(day -> (day.getMinTemperature() + day.getMaxTemperature()) / 2.0)
                .average()
                .stream()
                .map(this::round)
                .boxed()
                .findFirst()
                .orElse(null);

        Double representativeHumidity = daily.stream()
                .map(DailyWeatherDto::getAverageHumidity)
                .filter(Objects::nonNull)
                .filter(Double::isFinite)
                .mapToDouble(Double::doubleValue)
                .average()
                .stream()
                .map(this::round)
                .boxed()
                .findFirst()
                .orElse(null);

        return WeatherFeatureHintsDto.builder()
                .temperature(representativeTemperature)
                .temperatureSource("7-day representative temperature (mean of daily min/max midpoints)")
                .humidity(representativeHumidity)
                .humiditySource("7-day average forecast humidity")
                .rainfall(null)
                .rainfallAutofillEnabled(false)
                .rainfallMessage(
                        "Rainfall remains manual because the crop training dataset does not document " +
                        "the accumulation period represented by its rainfall feature. Rain probability is never used as rainfall amount."
                )
                .compatibilityWarning(
                        "Weather-derived temperature and humidity are convenience estimates. Review them before submitting because " +
                        "the crop dataset does not document the exact climate averaging period used during training."
                )
                .build();
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
