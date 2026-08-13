package com.growx.controller;

import com.growx.dto.response.ApiResponse;
import com.growx.dto.response.DashboardResponse;
import com.growx.security.UserPrincipal;
import com.growx.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * Dashboard endpoint — requires a valid JWT.
 *
 * GET /api/dashboard → aggregated overview for the authenticated farmer's primary farm
 */
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping
    public ResponseEntity<ApiResponse<DashboardResponse>> getDashboard(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        DashboardResponse response = dashboardService.getDashboard(principal.getId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
