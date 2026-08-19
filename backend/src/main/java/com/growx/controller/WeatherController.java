package com.growx.controller;

import com.growx.dto.response.ApiResponse;
import com.growx.dto.response.WeatherResponse;
import com.growx.security.UserPrincipal;
import com.growx.service.WeatherService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Authenticated farm-weather endpoints.
 * Farm ownership is re-checked inside WeatherService for every request.
 */
@RestController
@RequestMapping("/api/weather")
@RequiredArgsConstructor
public class WeatherController {

    private final WeatherService weatherService;

    @GetMapping("/farm/{farmId}")
    public ResponseEntity<ApiResponse<WeatherResponse>> getWeatherForFarm(
            @PathVariable Long farmId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                weatherService.getWeatherForFarm(farmId, principal.getId())
        ));
    }

    @PostMapping("/farm/{farmId}/refresh")
    public ResponseEntity<ApiResponse<WeatherResponse>> refreshWeatherForFarm(
            @PathVariable Long farmId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Weather refresh processed.",
                weatherService.refreshWeatherForFarm(farmId, principal.getId())
        ));
    }
}
