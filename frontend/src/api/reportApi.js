/**
 * reportApi.js
 * Report API calls.
 * Endpoints:
 *   GET  /api/reports/farm/{farmId}
 *   GET  /api/reports/farm/{farmId}/stats
 *   POST /api/reports/farm/{farmId}/generate?type=REPORT_TYPE
 * All require a valid JWT.
 *
 * Valid ReportType values (from ReportType enum):
 *   CROP_RECOMMENDATION | DISEASE_DETECTION | FARM_SUMMARY | WEATHER
 *
 * Note: PDF/download is not yet implemented.
 * Reports will have downloadable: false. The frontend must disable download actions.
 */

import { request } from "./apiClient.js";

/**
 * Get all reports for a farm.
 * @param {number} farmId
 * @returns {Promise<Array<ReportResponse>>}
 */
export async function getReports(farmId) {
  return request(`/api/reports/farm/${farmId}`);
}

/**
 * Get aggregate report stats for a farm.
 * Returns a map like: { total: 5, CROP_RECOMMENDATION: 3, DISEASE_DETECTION: 2, ... }
 * @param {number} farmId
 * @returns {Promise<Record<string, number>>}
 */
export async function getReportStats(farmId) {
  return request(`/api/reports/farm/${farmId}/stats`);
}

/**
 * Generate a new report for a farm.
 * @param {number} farmId
 * @param {"CROP_RECOMMENDATION" | "DISEASE_DETECTION" | "FARM_SUMMARY" | "WEATHER"} type
 * @returns {Promise<ReportResponse>}
 */
export async function generateReport(farmId, type) {
  return request(`/api/reports/farm/${farmId}/generate?type=${type}`, {
    method: "POST",
  });
}
