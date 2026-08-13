/**
 * farmApi.js
 * Farm management API calls.
 * Endpoints: GET /api/farms, GET /api/farms/{id}, PUT /api/farms/{id}
 * All require a valid JWT.
 */

import { request } from "./apiClient.js";

/**
 * Get all farms belonging to the authenticated user.
 * @returns {Promise<Array<FarmResponse>>}
 */
export async function getFarms() {
  return request("/api/farms");
}

/**
 * Get a specific farm by ID.
 * @param {number} farmId
 * @returns {Promise<FarmResponse>}
 */
export async function getFarm(farmId) {
  return request(`/api/farms/${farmId}`);
}

/**
 * Update a farm by ID.
 * All fields are optional — only non-null values will be applied by the backend.
 *
 * @param {number} farmId
 * @param {{
 *   farmName?: string,
 *   location?: string,
 *   latitude?: number,
 *   longitude?: number,
 *   area?: number,
 *   areaUnit?: string,
 *   soilType?: string,
 *   mainCrop?: string
 * }} farmData
 * @returns {Promise<FarmResponse>}
 */
export async function updateFarm(farmId, farmData) {
  return request(`/api/farms/${farmId}`, {
    method: "PUT",
    body: farmData,
  });
}
