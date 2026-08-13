package com.growx.client;

import com.growx.dto.response.WeatherResponse;

/**
 * Abstraction for the external weather data provider.
 *
 * This interface allows swapping the underlying weather API (e.g. OpenWeatherMap,
 * WeatherAPI.com) without changing the service or controller layer.
 *
 * Implementation must:
 * - Read API keys from environment variables / application properties
 * - Never hardcode keys or URLs in the implementation class
 * - Handle API errors gracefully and translate them to GrowX exceptions
 */
public interface WeatherApiClient {

    /**
     * Fetches current weather and a 5-day forecast for the given location name.
     *
     * @param location human-readable location string (e.g., "Coimbatore, Tamil Nadu")
     * @return WeatherResponse populated from the external API
     */
    WeatherResponse getWeatherByLocation(String location);

    /**
     * Fetches current weather and a 5-day forecast using GPS coordinates.
     * Preferred when a farm has stored latitude/longitude.
     *
     * @param latitude  farm latitude
     * @param longitude farm longitude
     * @return WeatherResponse populated from the external API
     */
    WeatherResponse getWeatherByCoordinates(double latitude, double longitude);
}
