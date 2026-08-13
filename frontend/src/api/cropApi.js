/**
 * cropApi.js
 * Crop recommendation API calls.
 * Endpoints:
 *   POST /api/crop-recommendation
 *   GET  /api/crop-recommendation/farm/{farmId}
 * All require a valid JWT.
 *
 * Note: ML is not yet implemented. The backend persists the request and
 * returns status=PENDING with null ML fields (recommendedCrop, confidence, etc.).
 * The frontend displays a pending state — never substitutes fake values.
 */

import { request } from "./apiClient.js";

/**
 * Submit a crop recommendation request.
 * @param {{
 *   farmId: number,
 *   nitrogen: number,
 *   phosphorus: number,
 *   potassium: number,
 *   temperature: number,
 *   humidity: number,
 *   ph: number,
 *   rainfall: number
 * }} data
 * @returns {Promise<CropRecommendationResponse>}
 */
export async function submitRecommendation(data) {
  return request("/api/crop-recommendation", {
    method: "POST",
    body: data,
  });
}

/**
 * Get all crop recommendations for a specific farm.
 * @param {number} farmId
 * @returns {Promise<Array<CropRecommendationResponse>>}
 */
export async function getRecommendationsForFarm(farmId) {
  return request(`/api/crop-recommendation/farm/${farmId}`);
}
