import { request } from "./apiClient.js";

/**
 * All weather-provider traffic stays behind the Spring Boot backend.
 * React never receives or stores a provider API key.
 */
export async function getWeatherForFarm(farmId) {
  return request(`/api/weather/farm/${farmId}`);
}

export async function refreshWeatherForFarm(farmId) {
  return request(`/api/weather/farm/${farmId}/refresh`, {
    method: "POST",
  });
}
