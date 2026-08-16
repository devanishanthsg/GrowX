package com.growx.client;

import com.growx.dto.request.CropRecommendationRequest;
import com.growx.dto.response.CropRecommendationResponse;

/**
 * Client abstraction for communicating with
 * the GrowX Python AI crop recommendation service.
 */
public interface CropMlClient {

    /**
     * Sends crop/soil/weather information to the AI service.
     *
     * @param request  crop recommendation input
     * @param district farm district used by the season/yield engines
     * @return AI-generated crop recommendation
     */
    CropRecommendationResponse predict(
            CropRecommendationRequest request,
            String district
    );
}