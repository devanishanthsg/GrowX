/**
 * diseaseApi.js
 * Disease detection API calls.
 * Endpoints:
 *   POST /api/disease-detection               (multipart/form-data)
 *   GET  /api/disease-detection/farm/{farmId}
 * All require a valid JWT.
 *
 * Multipart field names confirmed from DiseaseDetectionController:
 *   farmId → RequestParam("farmId") — sent as a text field
 *   image  → RequestParam("image")  — sent as the file
 *
 * Note: Disease ML is not yet implemented. The backend saves the image
 * and returns null for disease, confidence, severity, treatment, prevention.
 * The frontend shows "Analysis pending" — never substitutes fake results.
 */

import { request } from "./apiClient.js";

/**
 * Upload a plant image for disease analysis.
 * @param {number} farmId
 * @param {File}   imageFile
 * @returns {Promise<DiseaseDetectionResponse>}
 */
export async function analyzeImage(farmId, imageFile) {
  const formData = new FormData();
  // Field names must match @RequestParam names in DiseaseDetectionController
  formData.append("farmId", String(farmId));
  formData.append("image", imageFile);

  return request("/api/disease-detection", {
    method: "POST",
    formData,
  });
}

/**
 * Get all disease detections for a specific farm.
 * @param {number} farmId
 * @returns {Promise<Array<DiseaseDetectionResponse>>}
 */
export async function getDetectionsForFarm(farmId) {
  return request(`/api/disease-detection/farm/${farmId}`);
}
