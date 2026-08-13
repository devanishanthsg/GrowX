package com.growx.client;

import com.growx.dto.response.DiseaseDetectionResponse;
import org.springframework.web.multipart.MultipartFile;

/**
 * Abstraction for the Plant Disease Detection ML service.
 *
 * Expected flow:
 *   React → POST /api/disease-detection (multipart image)
 *               → DiseaseMlClient.detect()
 *                   → ML Service (Python/Flask, TensorFlow Serving, etc.)
 *               ← DiseaseDetectionResponse
 *           ← ApiResponse<DiseaseDetectionResponse>
 *
 * Implementation must:
 * - Read the ML service URL from environment variables / application properties
 * - Never hardcode service URLs
 * - Handle connection failures and return sensible errors
 */
public interface DiseaseMlClient {

    /**
     * Sends the uploaded image to the ML model and returns a disease prediction.
     *
     * @param image the uploaded multipart image file
     * @return detection result (disease name, confidence, severity, treatment, prevention)
     */
    DiseaseDetectionResponse detect(MultipartFile image);
}
