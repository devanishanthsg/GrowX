package com.growx.client;

import com.growx.dto.response.WeatherResponse;

/**
 * Provider abstraction for GrowX weather data. Implementations normalize
 * provider-specific JSON into WeatherResponse before returning it.
 */
public interface WeatherApiClient {

    /** Location-name lookup is a fallback when stored farm coordinates are unavailable. */
    WeatherResponse getWeatherByLocation(String location);

    /** Preferred lookup using the farm's WGS84 coordinates. */
    WeatherResponse getWeatherByCoordinates(double latitude, double longitude);
}
