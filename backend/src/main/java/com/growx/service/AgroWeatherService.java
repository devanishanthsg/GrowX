package com.growx.service;

import com.growx.dto.response.WeatherResponse;
import com.growx.dto.weather.HourlyWeatherDto;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * Converts normalized weather forecasts into agriculture-oriented metrics.
 * No provider JSON is handled here.
 */
@Service
public class AgroWeatherService {

    public AgroWeatherSummary summarize(WeatherResponse weather) {
        List<HourlyWeatherDto> hourly = weather.getHourly() == null ? List.of() : weather.getHourly();
        List<HourlyWeatherDto> firstSix = hourly.stream().limit(6).toList();

        return new AgroWeatherSummary(
                sum(hourly, HourlyWeatherDto::getPrecipitation),
                max(hourly, HourlyWeatherDto::getPrecipitationProbability),
                sum(firstSix, HourlyWeatherDto::getPrecipitation),
                max(firstSix, HourlyWeatherDto::getPrecipitationProbability),
                max(hourly, HourlyWeatherDto::getWindSpeed),
                max(hourly, HourlyWeatherDto::getWindGust),
                average(hourly, HourlyWeatherDto::getTemperature),
                max(hourly, HourlyWeatherDto::getTemperature),
                average(hourly, HourlyWeatherDto::getHumidity),
                sum(hourly, HourlyWeatherDto::getReferenceEvapotranspiration),
                average(hourly, HourlyWeatherDto::getModelledSoilMoisture),
                hourly.stream()
                        .filter(item -> value(item.getHumidity()) >= 85.0 || value(item.getPrecipitation()) >= 0.2)
                        .count()
        );
    }

    private double value(Double value) {
        return value == null || !Double.isFinite(value) ? 0.0 : value;
    }

    private Double sum(List<HourlyWeatherDto> items, ValueExtractor extractor) {
        double sum = 0.0;
        boolean found = false;
        for (HourlyWeatherDto item : items) {
            Double value = extractor.get(item);
            if (value != null && Double.isFinite(value)) {
                sum += value;
                found = true;
            }
        }
        return found ? round(sum) : null;
    }

    private Double max(List<HourlyWeatherDto> items, ValueExtractor extractor) {
        Double max = null;
        for (HourlyWeatherDto item : items) {
            Double value = extractor.get(item);
            if (value != null && Double.isFinite(value) && (max == null || value > max)) {
                max = value;
            }
        }
        return max == null ? null : round(max);
    }

    private Double average(List<HourlyWeatherDto> items, ValueExtractor extractor) {
        return items.stream()
                .map(extractor::get)
                .filter(Objects::nonNull)
                .filter(Double::isFinite)
                .mapToDouble(Double::doubleValue)
                .average()
                .stream()
                .map(this::round)
                .boxed()
                .findFirst()
                .orElse(null);
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    @FunctionalInterface
    private interface ValueExtractor {
        Double get(HourlyWeatherDto item);
    }

    public record AgroWeatherSummary(
            Double precipitationNext24Mm,
            Double maxRainProbabilityNext24,
            Double precipitationNext6Mm,
            Double maxRainProbabilityNext6,
            Double maxWindSpeedNext24,
            Double maxWindGustNext24,
            Double averageTemperatureNext24,
            Double maxTemperatureNext24,
            Double averageHumidityNext24,
            Double referenceEt0Next24Mm,
            Double averageModelledSoilMoisture,
            long wetHoursNext24
    ) {
    }
}
