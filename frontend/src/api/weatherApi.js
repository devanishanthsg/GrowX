/**
 * weatherApi.js
 * Weather API calls.
 * Endpoint: GET /api/weather/farm/{farmId}
 * Requires a valid JWT.
 *
 * IMPORTANT: WeatherApiClient is not yet implemented in the backend.
 * This endpoint currently throws a 500 error.
 * The frontend must catch this and show a "service unavailable" message.
 * DO NOT call OpenWeatherMap directly from the frontend.
 * DO NOT display hardcoded weather data.
 */

import { request } from "./apiClient.js";

/**
 * Get current weather and forecast for a farm's location.
 * @param {number} farmId
 * @returns {Promise<WeatherResponse>}
 * @throws {ApiError} Will throw when backend weather service is unavailable (500)
 */
export async function getWeatherForFarm(farmId) {
  return request(`/api/weather/farm/${farmId}`);
}
