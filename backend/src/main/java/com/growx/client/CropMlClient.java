package com.growx.client;

import com.growx.dto.request.CropRecommendationRequest;
import com.growx.dto.response.CropRecommendationResponse;

/**
 * Abstraction for the Crop Recommendation ML service.
 *
 * GrowX acts as a proxy between the React frontend and the ML microservice/model.
 * The React app never calls the ML service directly.
 *
 * Expected flow:
 *   React → POST /api/crop-recommendation (Spring Boot)
 *               → CropMlClient.predict()
 *                   → ML Service (Python/Flask or similar)
 *               ← CropRecommendationResponse
 *           ← ApiResponse<CropRecommendationResponse>
 *
 * Implementation must:
 * - Read the ML service URL from environment variables / application properties
 * - Never hardcode service URLs
 * - Handle connection failures and return sensible errors
 */
public interface CropMlClient {

    /**
     * Sends soil/weather parameters to the ML service and returns a prediction.
     *
     * @param request the crop recommendation input parameters
     * @return prediction result (crop, confidence, season, yield, explanation)
     */
    CropRecommendationResponse predict(CropRecommendationRequest request);
}
