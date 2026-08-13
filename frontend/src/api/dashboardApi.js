/**
 * dashboardApi.js
 * Dashboard API calls.
 * Endpoint: GET /api/dashboard
 * Requires a valid JWT.
 *
 * Note: soilMoisture, temperature, humidity, cropHealth, and weatherSummary
 * may currently return null because sensor/weather integrations are pending.
 * The frontend must handle nulls gracefully — never substitute fake values.
 */

import { request } from "./apiClient.js";

/**
 * Get the aggregated dashboard data for the authenticated farmer's primary farm.
 * @returns {Promise<DashboardResponse>}
 */
export async function getDashboard() {
  return request("/api/dashboard");
}
