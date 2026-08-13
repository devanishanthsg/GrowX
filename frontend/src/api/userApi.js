/**
 * userApi.js
 * User profile API calls.
 * Endpoints: GET /api/users/me, PUT /api/users/me
 * All require a valid JWT.
 */

import { request } from "./apiClient.js";

/**
 * Get the authenticated user's profile.
 * @returns {Promise<{ id, name, email, phone, role, createdAt }>} UserResponse
 */
export async function getMe() {
  return request("/api/users/me");
}

/**
 * Update the authenticated user's profile.
 * IMPORTANT: email is required by the backend even when not changing it.
 *
 * @param {{
 *   name: string,
 *   email: string,
 *   phone?: string,
 *   farmName?: string,
 *   location?: string,
 *   landArea?: number,
 *   mainCrop?: string
 * }} profileData
 * @returns {Promise<{ id, name, email, phone, role, createdAt }>} Updated UserResponse
 */
export async function updateProfile(profileData) {
  return request("/api/users/me", {
    method: "PUT",
    body: profileData,
  });
}
