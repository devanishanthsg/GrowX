package com.growx.controller;

import com.growx.dto.request.CreateFarmRequest;
import com.growx.dto.request.UpdateFarmRequest;
import com.growx.dto.response.ApiResponse;
import com.growx.dto.response.FarmResponse;
import com.growx.security.UserPrincipal;
import com.growx.service.FarmService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Farm management endpoints — all require a valid JWT.
 *
 * GET    /api/farms           → list all farms for the authenticated user
 * GET    /api/farms/{id}      → get a specific farm
 * POST   /api/farms           → create a new farm
 * PUT    /api/farms/{id}      → update a farm
 * DELETE /api/farms/{id}      → delete a farm
 */
@RestController
@RequestMapping("/api/farms")
@RequiredArgsConstructor
public class FarmController {

    private final FarmService farmService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<FarmResponse>>> getUserFarms(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        List<FarmResponse> farms = farmService.getFarmsForUser(principal.getId());
        return ResponseEntity.ok(ApiResponse.success(farms));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<FarmResponse>> getFarm(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        FarmResponse farm = farmService.getFarmById(id, principal.getId());
        return ResponseEntity.ok(ApiResponse.success(farm));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<FarmResponse>> createFarm(
            @Valid @RequestBody CreateFarmRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        FarmResponse farm = farmService.createFarm(request, principal.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Farm created successfully.", farm));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<FarmResponse>> updateFarm(
            @PathVariable Long id,
            @Valid @RequestBody UpdateFarmRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        FarmResponse farm = farmService.updateFarm(id, request, principal.getId());
        return ResponseEntity.ok(ApiResponse.success("Farm updated successfully.", farm));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteFarm(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        farmService.deleteFarm(id, principal.getId());
        return ResponseEntity.ok(ApiResponse.success("Farm deleted successfully.", null));
    }
}
