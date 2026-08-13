package com.growx.controller;

import com.growx.dto.response.ApiResponse;
import com.growx.dto.response.DiseaseDetectionResponse;
import com.growx.security.UserPrincipal;
import com.growx.service.DiseaseDetectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Disease detection endpoints — all require a valid JWT.
 *
 * POST /api/disease-detection                   → upload a plant image and get a disease prediction
 * GET  /api/disease-detection/farm/{farmId}     → list all detections for a farm
 */
@RestController
@RequestMapping("/api/disease-detection")
@RequiredArgsConstructor
public class DiseaseDetectionController {

    private final DiseaseDetectionService diseaseDetectionService;

    /**
     * Accepts a multipart image upload and a farmId parameter.
     * Stores the image and invokes the ML service (when connected).
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<DiseaseDetectionResponse>> analyzeImage(
            @RequestParam("image") MultipartFile image,
            @RequestParam("farmId") Long farmId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        DiseaseDetectionResponse response =
                diseaseDetectionService.analyzeImage(image, farmId, principal.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Image uploaded and analysis initiated.", response));
    }

    @GetMapping("/farm/{farmId}")
    public ResponseEntity<ApiResponse<List<DiseaseDetectionResponse>>> getDetectionsForFarm(
            @PathVariable Long farmId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        List<DiseaseDetectionResponse> responses =
                diseaseDetectionService.getDetectionsForFarm(farmId, principal.getId());
        return ResponseEntity.ok(ApiResponse.success(responses));
    }
}
