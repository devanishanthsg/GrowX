package com.growx.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Weather data response for the Weather page and Dashboard weather summary.
 * Designed to support any external weather API (e.g. OpenWeatherMap).
 * API keys and URLs must come from environment variables, never hardcoded.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WeatherResponse {

    // ── Current conditions ────────────────────────────────────────────────────

    private String location;

    /** Temperature in Celsius */
    private Double temperature;

    /** Feels-like temperature in Celsius */
    private Double feelsLike;

    /** Weather condition label, e.g. "Sunny", "Partly Cloudy" */
    private String condition;

    /** Emoji icon representing the condition */
    private String icon;

    /** Humidity percentage */
    private Double humidity;

    /** Wind speed in km/h */
    private Double windSpeed;

    /** Rain probability percentage (0–100) */
    private Double rainProbability;

    /** UV index value */
    private Double uvIndex;

    /** Human-readable UV category, e.g. "Moderate" */
    private String uvCategory;

    // ── 5-day forecast ────────────────────────────────────────────────────────

    private List<ForecastDay> forecast;

    // ── Farm weather alert ────────────────────────────────────────────────────

    private String alertMessage;

    // ── Inner value types ──────────────────────────────────────────────────────

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ForecastDay {
        private String day;
        private String icon;
        private String condition;
        private Double temperature;
        private Double rainProbability;
    }
}
