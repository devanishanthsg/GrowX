package com.growx.client.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.growx.client.WeatherApiClient;
import com.growx.dto.response.WeatherResponse;
import com.growx.dto.weather.AgriculturalWeatherDto;
import com.growx.dto.weather.CurrentWeatherDto;
import com.growx.dto.weather.DailyWeatherDto;
import com.growx.dto.weather.HourlyWeatherDto;
import com.growx.dto.weather.WeatherMetadataDto;
import com.growx.enums.WeatherDataStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Open-Meteo implementation of WeatherApiClient.
 *
 * Open-Meteo provider JSON is parsed only here and converted to GrowX DTOs.
 * The service/controller/frontend never depend on provider-specific field names.
 */
@Component
@Slf4j
public class OpenMeteoWeatherClient implements WeatherApiClient {

    private static final String HOURLY_FIELDS = String.join(",",
            "temperature_2m",
            "relative_humidity_2m",
            "precipitation",
            "precipitation_probability",
            "wind_speed_10m",
            "wind_gusts_10m",
            "weather_code",
            "et0_fao_evapotranspiration",
            "evapotranspiration",
            "soil_moisture_0_to_1cm",
            "soil_temperature_0cm",
            "vapour_pressure_deficit",
            "cloud_cover",
            "visibility",
            "uv_index"
    );

    private static final String CURRENT_FIELDS = String.join(",",
            "temperature_2m",
            "apparent_temperature",
            "relative_humidity_2m",
            "precipitation",
            "rain",
            "precipitation_probability",
            "weather_code",
            "cloud_cover",
            "uv_index",
            "visibility",
            "wind_speed_10m",
            "wind_gusts_10m"
    );

    private static final String DAILY_FIELDS = String.join(",",
            "weather_code",
            "temperature_2m_max",
            "temperature_2m_min",
            "precipitation_probability_max",
            "rain_sum",
            "wind_speed_10m_max",
            "et0_fao_evapotranspiration"
    );

    private final RestTemplate restTemplate;

    @Value("${growx.weather.base-url:https://api.open-meteo.com/v1/forecast}")
    private String baseUrl;

    @Value("${growx.weather.geocoding-url:https://geocoding-api.open-meteo.com/v1/search}")
    private String geocodingUrl;

    @Value("${growx.weather.retry-count:1}")
    private int retryCount;

    public OpenMeteoWeatherClient(
            @Qualifier("weatherRestTemplate") RestTemplate restTemplate
    ) {
        this.restTemplate = restTemplate;
    }

    @Override
    public WeatherResponse getWeatherByLocation(String location) {
        if (location == null || location.isBlank()) {
            throw new IllegalArgumentException("Farm location is required when coordinates are unavailable.");
        }

        String url = UriComponentsBuilder.fromHttpUrl(geocodingUrl)
                .queryParam("name", location.trim())
                .queryParam("count", 1)
                .queryParam("language", "en")
                .queryParam("format", "json")
                .build()
                .encode()
                .toUriString();

        JsonNode geocoding = executeWithRetry(
                () -> restTemplate.getForObject(url, JsonNode.class),
                "Open-Meteo geocoding"
        );

        JsonNode results = geocoding == null ? null : geocoding.path("results");
        if (results == null || !results.isArray() || results.isEmpty()) {
            throw new IllegalStateException("Farm location could not be resolved to weather coordinates.");
        }

        JsonNode first = results.get(0);
        double latitude = first.path("latitude").asDouble(Double.NaN);
        double longitude = first.path("longitude").asDouble(Double.NaN);

        if (!Double.isFinite(latitude) || !Double.isFinite(longitude)) {
            throw new IllegalStateException("Weather geocoding returned invalid coordinates.");
        }

        String resolvedName = buildResolvedLocationName(first);
        WeatherResponse response = getWeatherByCoordinates(latitude, longitude);

        return response.toBuilder()
                .locationName(resolvedName)
                .coordinateSource("GEOCODED_FALLBACK")
                .build();
    }

    @Override
    public WeatherResponse getWeatherByCoordinates(double latitude, double longitude) {
        validateCoordinates(latitude, longitude);

        String url = UriComponentsBuilder.fromHttpUrl(baseUrl)
                .queryParam("latitude", latitude)
                .queryParam("longitude", longitude)
                .queryParam("timezone", "auto")
                .queryParam("forecast_days", 7)
                .queryParam("current", CURRENT_FIELDS)
                .queryParam("hourly", HOURLY_FIELDS)
                .queryParam("daily", DAILY_FIELDS)
                .build()
                .encode()
                .toUriString();

        JsonNode root = executeWithRetry(
                () -> restTemplate.getForObject(url, JsonNode.class),
                "Open-Meteo forecast"
        );

        if (root == null || root.isMissingNode() || root.path("error").asBoolean(false)) {
            String reason = root == null ? null : root.path("reason").asText(null);
            throw new IllegalStateException(
                    reason == null ? "Weather provider returned an empty response." : reason
            );
        }

        return normalize(root, latitude, longitude);
    }

    private WeatherResponse normalize(JsonNode root, double requestedLatitude, double requestedLongitude) {
        JsonNode currentNode = root.path("current");
        JsonNode hourlyNode = root.path("hourly");
        JsonNode dailyNode = root.path("daily");

        LocalDateTime currentTime = parseDateTime(currentNode.path("time").asText(null));
        int nearestHourlyIndex = findNearestHourlyIndex(hourlyNode.path("time"), currentTime);

        CurrentWeatherDto current = CurrentWeatherDto.builder()
                .timestamp(currentTime)
                .temperature(doubleValue(currentNode, "temperature_2m"))
                .feelsLike(doubleValue(currentNode, "apparent_temperature"))
                .humidity(doubleValue(currentNode, "relative_humidity_2m"))
                .weatherCode(intValue(currentNode, "weather_code"))
                .conditionDescription(conditionDescription(intValue(currentNode, "weather_code")))
                .icon(conditionIcon(intValue(currentNode, "weather_code")))
                .windSpeed(doubleValue(currentNode, "wind_speed_10m"))
                .windGust(doubleValue(currentNode, "wind_gusts_10m"))
                .precipitation(doubleValue(currentNode, "precipitation"))
                .rainProbability(doubleValue(currentNode, "precipitation_probability"))
                .cloudCover(doubleValue(currentNode, "cloud_cover"))
                .uvIndex(doubleValue(currentNode, "uv_index"))
                .visibilityKm(toKilometres(doubleValue(currentNode, "visibility")))
                .build();

        AgriculturalWeatherDto agriculture = AgriculturalWeatherDto.builder()
                .referenceEvapotranspiration(arrayDouble(hourlyNode, "et0_fao_evapotranspiration", nearestHourlyIndex))
                .evapotranspiration(arrayDouble(hourlyNode, "evapotranspiration", nearestHourlyIndex))
                .modelledSoilMoisture(arrayDouble(hourlyNode, "soil_moisture_0_to_1cm", nearestHourlyIndex))
                .soilTemperature(arrayDouble(hourlyNode, "soil_temperature_0cm", nearestHourlyIndex))
                .vapourPressureDeficit(arrayDouble(hourlyNode, "vapour_pressure_deficit", nearestHourlyIndex))
                .build();

        List<HourlyWeatherDto> hourly = buildNext24Hours(hourlyNode, currentTime);
        Map<LocalDate, Double> dailyHumidity = calculateDailyAverageHumidity(hourlyNode);
        List<DailyWeatherDto> daily = buildDailyForecast(dailyNode, dailyHumidity);

        OffsetDateTime fetchedAt = OffsetDateTime.now(ZoneOffset.UTC);

        return WeatherResponse.builder()
                .locationName(null)
                .latitude(root.path("latitude").isNumber() ? root.path("latitude").asDouble() : requestedLatitude)
                .longitude(root.path("longitude").isNumber() ? root.path("longitude").asDouble() : requestedLongitude)
                .timezone(root.path("timezone").asText(null))
                .coordinateSource("FARM_COORDINATES")
                .current(current)
                .hourly(hourly)
                .daily(daily)
                .agriculture(agriculture)
                .metadata(WeatherMetadataDto.builder()
                        .provider("Open-Meteo")
                        .fetchedAt(fetchedAt)
                        .dataAgeMinutes(0L)
                        .status(WeatherDataStatus.LIVE)
                        .stale(false)
                        .build())
                .warnings(new ArrayList<>())
                .build();
    }

    private List<HourlyWeatherDto> buildNext24Hours(JsonNode hourlyNode, LocalDateTime currentTime) {
        List<HourlyWeatherDto> result = new ArrayList<>();
        JsonNode times = hourlyNode.path("time");
        if (!times.isArray()) {
            return result;
        }

        LocalDateTime threshold = currentTime == null ? null : currentTime.minusMinutes(1);

        for (int index = 0; index < times.size() && result.size() < 24; index++) {
            LocalDateTime time = parseDateTime(times.get(index).asText(null));
            if (time == null) {
                continue;
            }
            if (threshold != null && time.isBefore(threshold)) {
                continue;
            }

            Integer code = arrayInt(hourlyNode, "weather_code", index);
            result.add(HourlyWeatherDto.builder()
                    .timestamp(time)
                    .temperature(arrayDouble(hourlyNode, "temperature_2m", index))
                    .humidity(arrayDouble(hourlyNode, "relative_humidity_2m", index))
                    .precipitation(arrayDouble(hourlyNode, "precipitation", index))
                    .precipitationProbability(arrayDouble(hourlyNode, "precipitation_probability", index))
                    .windSpeed(arrayDouble(hourlyNode, "wind_speed_10m", index))
                    .windGust(arrayDouble(hourlyNode, "wind_gusts_10m", index))
                    .weatherCode(code)
                    .conditionDescription(conditionDescription(code))
                    .icon(conditionIcon(code))
                    .referenceEvapotranspiration(arrayDouble(hourlyNode, "et0_fao_evapotranspiration", index))
                    .modelledSoilMoisture(arrayDouble(hourlyNode, "soil_moisture_0_to_1cm", index))
                    .build());
        }

        return result;
    }

    private List<DailyWeatherDto> buildDailyForecast(
            JsonNode dailyNode,
            Map<LocalDate, Double> dailyHumidity
    ) {
        List<DailyWeatherDto> result = new ArrayList<>();
        JsonNode times = dailyNode.path("time");
        if (!times.isArray()) {
            return result;
        }

        for (int index = 0; index < times.size() && index < 7; index++) {
            LocalDate date = parseDate(times.get(index).asText(null));
            if (date == null) {
                continue;
            }

            Integer code = arrayInt(dailyNode, "weather_code", index);
            DayOfWeek dayOfWeek = date.getDayOfWeek();

            result.add(DailyWeatherDto.builder()
                    .date(date)
                    .day(dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.ENGLISH))
                    .weatherCode(code)
                    .conditionDescription(conditionDescription(code))
                    .icon(conditionIcon(code))
                    .minTemperature(arrayDouble(dailyNode, "temperature_2m_min", index))
                    .maxTemperature(arrayDouble(dailyNode, "temperature_2m_max", index))
                    .averageHumidity(dailyHumidity.get(date))
                    .rainProbability(arrayDouble(dailyNode, "precipitation_probability_max", index))
                    .rainfallMm(arrayDouble(dailyNode, "rain_sum", index))
                    .maxWindSpeed(arrayDouble(dailyNode, "wind_speed_10m_max", index))
                    .referenceEvapotranspiration(arrayDouble(dailyNode, "et0_fao_evapotranspiration", index))
                    .build());
        }

        return result;
    }

    private Map<LocalDate, Double> calculateDailyAverageHumidity(JsonNode hourlyNode) {
        Map<LocalDate, List<Double>> grouped = new HashMap<>();
        JsonNode times = hourlyNode.path("time");
        JsonNode humidity = hourlyNode.path("relative_humidity_2m");

        if (!times.isArray() || !humidity.isArray()) {
            return Map.of();
        }

        int length = Math.min(times.size(), humidity.size());
        for (int index = 0; index < length; index++) {
            LocalDateTime time = parseDateTime(times.get(index).asText(null));
            if (time == null || humidity.get(index).isNull() || !humidity.get(index).isNumber()) {
                continue;
            }
            grouped.computeIfAbsent(time.toLocalDate(), ignored -> new ArrayList<>())
                    .add(humidity.get(index).asDouble());
        }

        Map<LocalDate, Double> averages = new HashMap<>();
        grouped.forEach((date, values) -> averages.put(
                date,
                values.stream().mapToDouble(Double::doubleValue).average().orElse(Double.NaN)
        ));
        averages.entrySet().removeIf(entry -> !Double.isFinite(entry.getValue()));
        return averages;
    }

    private int findNearestHourlyIndex(JsonNode times, LocalDateTime target) {
        if (!times.isArray() || times.isEmpty()) {
            return -1;
        }
        if (target == null) {
            return 0;
        }

        int bestIndex = 0;
        long bestDifference = Long.MAX_VALUE;
        for (int index = 0; index < times.size(); index++) {
            LocalDateTime candidate = parseDateTime(times.get(index).asText(null));
            if (candidate == null) {
                continue;
            }
            long difference = Math.abs(java.time.Duration.between(candidate, target).toMinutes());
            if (difference < bestDifference) {
                bestDifference = difference;
                bestIndex = index;
            }
        }
        return bestIndex;
    }

    private <T> T executeWithRetry(Supplier<T> action, String operation) {
        int attempts = Math.max(1, retryCount + 1);
        RuntimeException last = null;

        for (int attempt = 1; attempt <= attempts; attempt++) {
            try {
                T value = action.get();
                log.debug("{} succeeded on attempt {}", operation, attempt);
                return value;
            } catch (HttpClientErrorException ex) {
                // 4xx errors are not transient; retrying would only create extra provider load.
                log.warn("{} rejected the request with HTTP {}", operation, ex.getStatusCode().value());
                throw ex;
            } catch (HttpServerErrorException | ResourceAccessException ex) {
                last = ex;
                log.warn("{} temporary failure on attempt {}/{}: {}", operation, attempt, attempts, ex.getMessage());
            } catch (RestClientException ex) {
                last = ex;
                log.warn("{} request failure on attempt {}/{}: {}", operation, attempt, attempts, ex.getMessage());
            }

            if (attempt < attempts) {
                try {
                    Thread.sleep(200L * attempt);
                } catch (InterruptedException interrupted) {
                    Thread.currentThread().interrupt();
                    throw new IllegalStateException("Weather provider retry was interrupted.", interrupted);
                }
            }
        }

        throw last == null
                ? new IllegalStateException(operation + " failed.")
                : last;
    }

    private void validateCoordinates(double latitude, double longitude) {
        if (!Double.isFinite(latitude) || latitude < -90 || latitude > 90) {
            throw new IllegalArgumentException("Latitude must be between -90 and 90.");
        }
        if (!Double.isFinite(longitude) || longitude < -180 || longitude > 180) {
            throw new IllegalArgumentException("Longitude must be between -180 and 180.");
        }
    }

    private String buildResolvedLocationName(JsonNode node) {
        String name = node.path("name").asText("");
        String admin1 = node.path("admin1").asText("");
        String country = node.path("country").asText("");

        List<String> parts = new ArrayList<>();
        if (!name.isBlank()) parts.add(name);
        if (!admin1.isBlank() && !admin1.equalsIgnoreCase(name)) parts.add(admin1);
        if (!country.isBlank()) parts.add(country);
        return parts.isEmpty() ? null : String.join(", ", parts);
    }

    private Double doubleValue(JsonNode node, String field) {
        JsonNode value = node.path(field);
        return value.isNumber() ? value.asDouble() : null;
    }

    private Integer intValue(JsonNode node, String field) {
        JsonNode value = node.path(field);
        return value.isNumber() ? value.asInt() : null;
    }

    private Double arrayDouble(JsonNode node, String field, int index) {
        if (index < 0) return null;
        JsonNode array = node.path(field);
        if (!array.isArray() || index >= array.size()) return null;
        JsonNode value = array.get(index);
        return value != null && value.isNumber() ? value.asDouble() : null;
    }

    private Integer arrayInt(JsonNode node, String field, int index) {
        if (index < 0) return null;
        JsonNode array = node.path(field);
        if (!array.isArray() || index >= array.size()) return null;
        JsonNode value = array.get(index);
        return value != null && value.isNumber() ? value.asInt() : null;
    }

    private LocalDateTime parseDateTime(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            return LocalDateTime.parse(value);
        } catch (Exception ignored) {
            return null;
        }
    }

    private LocalDate parseDate(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            return LocalDate.parse(value);
        } catch (Exception ignored) {
            return null;
        }
    }

    private Double toKilometres(Double metres) {
        return metres == null ? null : metres / 1000.0;
    }

    private String conditionDescription(Integer code) {
        if (code == null) return "Unknown";
        return switch (code) {
            case 0 -> "Clear sky";
            case 1 -> "Mainly clear";
            case 2 -> "Partly cloudy";
            case 3 -> "Overcast";
            case 45, 48 -> "Fog";
            case 51 -> "Light drizzle";
            case 53 -> "Moderate drizzle";
            case 55 -> "Dense drizzle";
            case 56, 57 -> "Freezing drizzle";
            case 61 -> "Slight rain";
            case 63 -> "Moderate rain";
            case 65 -> "Heavy rain";
            case 66, 67 -> "Freezing rain";
            case 71 -> "Slight snowfall";
            case 73 -> "Moderate snowfall";
            case 75 -> "Heavy snowfall";
            case 77 -> "Snow grains";
            case 80 -> "Slight rain showers";
            case 81 -> "Moderate rain showers";
            case 82 -> "Violent rain showers";
            case 85, 86 -> "Snow showers";
            case 95 -> "Thunderstorm";
            case 96, 99 -> "Thunderstorm with hail";
            default -> "Variable conditions";
        };
    }

    private String conditionIcon(Integer code) {
        if (code == null) return "🌡️";
        return switch (code) {
            case 0 -> "☀️";
            case 1, 2 -> "🌤️";
            case 3 -> "☁️";
            case 45, 48 -> "🌫️";
            case 51, 53, 55, 56, 57 -> "🌦️";
            case 61, 63, 65, 66, 67, 80, 81, 82 -> "🌧️";
            case 71, 73, 75, 77, 85, 86 -> "🌨️";
            case 95, 96, 99 -> "⛈️";
            default -> "🌡️";
        };
    }
}
