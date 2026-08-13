package com.growx.controller;

import com.growx.dto.request.CropRecommendationRequest;
import com.growx.dto.response.ApiResponse;
import com.growx.dto.response.CropRecommendationResponse;
import com.growx.security.UserPrincipal;
import com.growx.service.CropRecommendationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Crop recommendation endpoints — all require a valid JWT.
 *
 * POST /api/crop-recommendation         → submit soil/weather parameters, receive recommendation
 * GET  /api/crop-recommendation/{id}    → get a specific recommendation by ID
 * GET  /api/crop-recommendation/farm/{farmId} → list all recommendations for a farm
 */
@RestController
@RequestMapping("/api/crop-recommendation")
@RequiredArgsConstructor
public class CropRecommendationController {

    private final CropRecommendationService cropRecommendationService;

    @PostMapping
    public ResponseEntity<ApiResponse<CropRecommendationResponse>> requestRecommendation(
            @Valid @RequestBody CropRecommendationRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        CropRecommendationResponse response =
                cropRecommendationService.requestRecommendation(request, principal.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Recommendation request submitted.", response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CropRecommendationResponse>> getRecommendation(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        CropRecommendationResponse response =
                cropRecommendationService.getRecommendationById(id, principal.getId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/farm/{farmId}")
    public ResponseEntity<ApiResponse<List<CropRecommendationResponse>>> getRecommendationsForFarm(
            @PathVariable Long farmId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        List<CropRecommendationResponse> responses =
                cropRecommendationService.getRecommendationsForFarm(farmId, principal.getId());
        return ResponseEntity.ok(ApiResponse.success(responses));
    }
}
