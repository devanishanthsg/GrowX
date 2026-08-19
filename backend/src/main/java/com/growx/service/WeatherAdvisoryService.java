package com.growx.service;

import com.growx.dto.response.WeatherResponse;
import com.growx.dto.weather.DailyWeatherDto;
import com.growx.dto.weather.HourlyWeatherDto;
import com.growx.dto.weather.WeatherAdvisoryDto;
import com.growx.enums.RiskLevel;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Deterministic farmer-facing weather advisory engine.
 * These advisories are decision support, not agronomic guarantees.
 */
@Service
public class WeatherAdvisoryService {

    public AdvisoryBundle build(WeatherResponse weather, AgroWeatherService.AgroWeatherSummary metrics) {
        List<WeatherAdvisoryDto> advisories = new ArrayList<>();

        WeatherAdvisoryDto irrigation = irrigation(metrics);
        WeatherAdvisoryDto spraying = spraying(weather.getHourly(), metrics);
        WeatherAdvisoryDto disease = diseaseRisk(metrics);
        WeatherAdvisoryDto heat = heatRisk(metrics);
        WeatherAdvisoryDto rain = rainAlert(weather.getHourly(), metrics);

        advisories.add(irrigation);
        advisories.add(spraying);
        advisories.add(disease);
        advisories.add(heat);
        advisories.add(rain);

        List<DailyWeatherDto> decoratedDaily = decorateDaily(weather.getDaily());

        RiskLevel overall = RiskLevel.LOW;
        String reason = "No major weather concern is dominant in the available forecast.";
        for (WeatherAdvisoryDto advisory : advisories) {
            if (rank(advisory.getSeverity()) > rank(overall)) {
                overall = advisory.getSeverity();
                reason = advisory.getExplanation();
            }
        }

        return new AdvisoryBundle(advisories, decoratedDaily, overall, reason);
    }

    private WeatherAdvisoryDto irrigation(AgroWeatherService.AgroWeatherSummary m) {
        Double rain = m.precipitationNext24Mm();
        Double probability = m.maxRainProbabilityNext24();
        Double et0 = m.referenceEt0Next24Mm();
        Double soil = m.averageModelledSoilMoisture();

        Map<String, Double> metrics = metrics(
                "forecastRain24hMm", rain,
                "maxRainProbability24hPct", probability,
                "referenceEt0Next24hMm", et0,
                "modelledSoilMoistureM3M3", soil
        );

        if (rain == null && probability == null && et0 == null && soil == null) {
            return advisory("IRRIGATION", "INSUFFICIENT_DATA", RiskLevel.MODERATE,
                    "Irrigation data is limited",
                    "The forecast does not contain enough rainfall, ET0 or modelled soil-moisture information for a useful irrigation signal.",
                    "Use field observations or soil measurements before making an irrigation decision.", metrics);
        }

        if (value(rain) >= 10.0) {
            return advisory("IRRIGATION", "DELAY", RiskLevel.HIGH,
                    "Consider delaying irrigation",
                    String.format("About %.1f mm of precipitation is forecast during the next 24 hours.", value(rain)),
                    "Reassess field moisture after the forecast rainfall before irrigating.", metrics);
        }

        if (value(rain) >= 5.0 || value(probability) >= 70.0) {
            return advisory("IRRIGATION", "DELAY", RiskLevel.MODERATE,
                    "Rain may reduce irrigation need",
                    "Meaningful rainfall or a high rain probability is forecast during the next 24 hours.",
                    "Consider waiting for the forecast period and reassess soil moisture before irrigating.", metrics);
        }

        if (et0 != null && et0 >= 4.0 && value(rain) < 2.0 && (soil == null || soil < 0.22)) {
            return advisory("IRRIGATION", "RECOMMENDED", RiskLevel.MODERATE,
                    "Irrigation may be worth considering",
                    "Forecast evaporative demand is elevated while little rainfall is expected.",
                    "Check actual root-zone soil moisture and crop stage, then consider irrigation if the field is dry.", metrics);
        }

        if (soil != null && soil < 0.18 && value(rain) < 3.0) {
            return advisory("IRRIGATION", "CONSIDER", RiskLevel.MODERATE,
                    "Modelled soil moisture is low",
                    "The weather model indicates relatively low near-surface soil moisture and limited rainfall.",
                    "Confirm with field or sensor measurements before deciding how much to irrigate.", metrics);
        }

        return advisory("IRRIGATION", "CONSIDER", RiskLevel.LOW,
                "No strong irrigation signal",
                "The next 24 hours do not show a strong rainfall or evaporative-demand trigger.",
                "Use crop stage and actual field moisture as the primary irrigation guide.", metrics);
    }

    private WeatherAdvisoryDto spraying(List<HourlyWeatherDto> hourly, AgroWeatherService.AgroWeatherSummary m) {
        Map<String, Double> metrics = metrics(
                "forecastRain6hMm", m.precipitationNext6Mm(),
                "maxRainProbability6hPct", m.maxRainProbabilityNext6(),
                "maxWindSpeed24hKmh", m.maxWindSpeedNext24(),
                "maxWindGust24hKmh", m.maxWindGustNext24()
        );

        SprayWindow window = findSprayWindow(hourly);

        if (value(m.precipitationNext6Mm()) >= 0.5
                || value(m.maxRainProbabilityNext6()) >= 60.0
                || value(m.maxWindGustNext24()) >= 30.0
                || value(m.maxWindSpeedNext24()) >= 20.0) {
            WeatherAdvisoryDto result = advisory("SPRAYING", "NOT_RECOMMENDED", RiskLevel.HIGH,
                    "Spraying conditions appear poor",
                    "Rain or elevated wind may reduce spray effectiveness and increase drift risk.",
                    window == null
                            ? "Avoid spraying during the risky period and follow the product label and local agricultural guidance."
                            : "Avoid the risky period. A comparatively calmer window is shown below, but always follow the product label.",
                    metrics);
            if (window != null) {
                result.setWindowStart(window.start());
                result.setWindowEnd(window.end());
            }
            return result;
        }

        if (window != null) {
            WeatherAdvisoryDto result = advisory("SPRAYING", "GOOD", RiskLevel.LOW,
                    "Conditions appear more suitable for spraying",
                    "A low-rain, lower-wind period is available in the upcoming hourly forecast.",
                    "Use the suggested window only as weather guidance; confirm the pesticide label, crop condition and local restrictions.",
                    metrics);
            result.setWindowStart(window.start());
            result.setWindowEnd(window.end());
            return result;
        }

        return advisory("SPRAYING", "MODERATE", RiskLevel.MODERATE,
                "Spraying conditions are mixed",
                "The forecast is not clearly unsuitable, but no three-hour low-rain and low-wind window was identified.",
                "Wait for a calmer, drier period where practical and follow the product label.", metrics);
    }

    private WeatherAdvisoryDto diseaseRisk(AgroWeatherService.AgroWeatherSummary m) {
        Map<String, Double> metrics = metrics(
                "averageHumidity24hPct", m.averageHumidityNext24(),
                "forecastRain24hMm", m.precipitationNext24Mm(),
                "wetHours24h", (double) m.wetHoursNext24(),
                "averageTemperature24hC", m.averageTemperatureNext24()
        );

        if ((value(m.averageHumidityNext24()) >= 85.0 && value(m.precipitationNext24Mm()) >= 3.0)
                || m.wetHoursNext24() >= 10) {
            return advisory("DISEASE_RISK", "HIGH", RiskLevel.HIGH,
                    "Weather-related disease risk is elevated",
                    "Sustained humid or wet conditions may favour some fungal and bacterial crop diseases.",
                    "Monitor leaves and crop canopy for symptoms. Use the Disease Detection module for image-based diagnosis if symptoms appear.", metrics);
        }

        if (value(m.averageHumidityNext24()) >= 75.0
                && (value(m.precipitationNext24Mm()) >= 1.0 || m.wetHoursNext24() >= 6)) {
            return advisory("DISEASE_RISK", "MODERATE", RiskLevel.MODERATE,
                    "Monitor for weather-related disease pressure",
                    "Humidity and wet periods may moderately increase environmental disease pressure.",
                    "Keep monitoring the crop, especially dense or poorly ventilated foliage.", metrics);
        }

        return advisory("DISEASE_RISK", "LOW", RiskLevel.LOW,
                "Weather-related disease pressure is currently lower",
                "The next 24 hours do not show prolonged wet and humid conditions in the available forecast.",
                "Continue routine crop monitoring; this is environmental risk information, not a disease diagnosis.", metrics);
    }

    private WeatherAdvisoryDto heatRisk(AgroWeatherService.AgroWeatherSummary m) {
        Double maxTemperature = m.maxTemperatureNext24();
        Map<String, Double> metrics = metrics(
                "maxTemperature24hC", maxTemperature,
                "averageHumidity24hPct", m.averageHumidityNext24()
        );

        if (maxTemperature == null) {
            return advisory("HEAT_STRESS", "INSUFFICIENT_DATA", RiskLevel.MODERATE,
                    "Heat stress data is limited",
                    "The forecast does not contain enough temperature data to assess heat stress.",
                    "Monitor local field conditions.", metrics);
        }

        if (maxTemperature >= 42.0) {
            return advisory("HEAT_STRESS", "EXTREME", RiskLevel.EXTREME,
                    "Extreme heat stress is possible",
                    String.format("Temperatures may reach about %.1f°C during the next 24 hours.", maxTemperature),
                    "Avoid unnecessary crop stress and monitor water availability closely.", metrics);
        }
        if (maxTemperature >= 36.0) {
            return advisory("HEAT_STRESS", "HIGH", RiskLevel.HIGH,
                    "High heat stress is possible",
                    String.format("Temperatures may reach about %.1f°C during the next 24 hours.", maxTemperature),
                    "Monitor crop wilting and soil moisture, and avoid avoidable field stress during the hottest period.", metrics);
        }
        if (maxTemperature >= 32.0) {
            return advisory("HEAT_STRESS", "MODERATE", RiskLevel.MODERATE,
                    "Moderate heat stress is possible",
                    String.format("Temperatures may reach about %.1f°C.", maxTemperature),
                    "Monitor sensitive crops and field moisture during the warmer hours.", metrics);
        }

        return advisory("HEAT_STRESS", "LOW", RiskLevel.LOW,
                "Heat stress risk is currently lower",
                "Forecast temperatures remain below the general high-heat thresholds used by this weather advisory.",
                "Continue normal monitoring because crop-specific heat tolerance can differ.", metrics);
    }

    private WeatherAdvisoryDto rainAlert(List<HourlyWeatherDto> hourly, AgroWeatherService.AgroWeatherSummary m) {
        Map<String, Double> metrics = metrics(
                "forecastRain24hMm", m.precipitationNext24Mm(),
                "maxRainProbability24hPct", m.maxRainProbabilityNext24()
        );

        LocalDateTime firstRainTime = firstRainTime(hourly);
        double rain = value(m.precipitationNext24Mm());
        double probability = value(m.maxRainProbabilityNext24());

        WeatherAdvisoryDto result;
        if (rain >= 30.0) {
            result = advisory("RAIN_ALERT", "HEAVY_RAIN_RISK", RiskLevel.HIGH,
                    "Heavy rainfall is possible",
                    String.format("Approximately %.1f mm of precipitation is forecast over the next 24 hours.", rain),
                    "Check drainage-sensitive areas and postpone weather-sensitive work where practical.", metrics);
        } else if (rain >= 10.0) {
            result = advisory("RAIN_ALERT", "MODERATE_RAIN", RiskLevel.MODERATE,
                    "Moderate rainfall is expected",
                    String.format("Approximately %.1f mm of precipitation is forecast over the next 24 hours.", rain),
                    "Plan irrigation and field operations around the expected wet period.", metrics);
        } else if (rain >= 1.0 || probability >= 60.0) {
            result = advisory("RAIN_ALERT", "RAIN_EXPECTED", RiskLevel.MODERATE,
                    "Rain is possible soon",
                    String.format("The next 24 hours show %.1f mm forecast precipitation with a maximum rain probability of %.0f%%.", rain, probability),
                    "Check the hourly forecast before irrigation, spraying or harvesting activities.", metrics);
        } else {
            result = advisory("RAIN_ALERT", "LOW", RiskLevel.LOW,
                    "No significant rain signal",
                    "The available 24-hour forecast does not show a strong rainfall signal.",
                    "Continue to monitor forecast updates because rainfall timing can change.", metrics);
        }

        if (firstRainTime != null) {
            result.setWindowStart(firstRainTime);
        }
        return result;
    }

    private List<DailyWeatherDto> decorateDaily(List<DailyWeatherDto> daily) {
        if (daily == null) return List.of();
        List<DailyWeatherDto> result = new ArrayList<>();
        for (DailyWeatherDto day : daily) {
            RiskLevel risk = RiskLevel.LOW;
            String reason = "No dominant farm-weather concern.";

            if (value(day.getRainfallMm()) >= 30.0) {
                risk = RiskLevel.HIGH;
                reason = "Heavy forecast rainfall.";
            } else if (value(day.getMaxTemperature()) >= 36.0) {
                risk = RiskLevel.HIGH;
                reason = "High forecast temperature.";
            } else if (value(day.getRainfallMm()) >= 10.0
                    || value(day.getRainProbability()) >= 70.0
                    || value(day.getMaxWindSpeed()) >= 25.0
                    || (value(day.getAverageHumidity()) >= 85.0 && value(day.getRainfallMm()) >= 2.0)) {
                risk = RiskLevel.MODERATE;
                reason = "Rain, humidity, heat or wind may affect farm activities.";
            }

            result.add(day.toBuilder().farmRisk(risk).farmRiskReason(reason).build());
        }
        return result;
    }

    private SprayWindow findSprayWindow(List<HourlyWeatherDto> hourly) {
        if (hourly == null || hourly.size() < 3) return null;

        for (int start = 0; start <= hourly.size() - 3; start++) {
            boolean suitable = true;
            for (int offset = 0; offset < 3; offset++) {
                HourlyWeatherDto hour = hourly.get(start + offset);
                if (value(hour.getPrecipitation()) >= 0.2
                        || value(hour.getPrecipitationProbability()) >= 30.0
                        || value(hour.getWindSpeed()) > 12.0
                        || value(hour.getWindGust()) > 20.0
                        || (hour.getTemperature() != null && (hour.getTemperature() < 10.0 || hour.getTemperature() > 32.0))) {
                    suitable = false;
                    break;
                }
            }
            if (suitable) {
                LocalDateTime windowStart = hourly.get(start).getTimestamp();
                LocalDateTime windowEnd = hourly.get(start + 2).getTimestamp();
                if (windowEnd != null) windowEnd = windowEnd.plusHours(1);
                return new SprayWindow(windowStart, windowEnd);
            }
        }
        return null;
    }

    private LocalDateTime firstRainTime(List<HourlyWeatherDto> hourly) {
        if (hourly == null) return null;
        return hourly.stream()
                .filter(hour -> value(hour.getPrecipitation()) >= 0.2
                        || value(hour.getPrecipitationProbability()) >= 60.0)
                .map(HourlyWeatherDto::getTimestamp)
                .filter(java.util.Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

    private WeatherAdvisoryDto advisory(
            String type,
            String status,
            RiskLevel severity,
            String title,
            String explanation,
            String action,
            Map<String, Double> metrics
    ) {
        return WeatherAdvisoryDto.builder()
                .type(type)
                .status(status)
                .severity(severity)
                .title(title)
                .explanation(explanation)
                .recommendedAction(action)
                .metrics(metrics)
                .build();
    }

    private Map<String, Double> metrics(Object... values) {
        Map<String, Double> result = new LinkedHashMap<>();
        for (int index = 0; index < values.length; index += 2) {
            String key = (String) values[index];
            Double value = (Double) values[index + 1];
            if (value != null && Double.isFinite(value)) {
                result.put(key, Math.round(value * 100.0) / 100.0);
            }
        }
        return result;
    }

    private double value(Double value) {
        return value == null || !Double.isFinite(value) ? 0.0 : value;
    }

    private int rank(RiskLevel risk) {
        if (risk == null) return 0;
        return switch (risk) {
            case LOW -> 1;
            case MODERATE -> 2;
            case HIGH -> 3;
            case EXTREME -> 4;
        };
    }

    private record SprayWindow(LocalDateTime start, LocalDateTime end) {
    }

    public record AdvisoryBundle(
            List<WeatherAdvisoryDto> advisories,
            List<DailyWeatherDto> daily,
            RiskLevel overallRisk,
            String overallRiskReason
    ) {
    }
}
