package com.growx.controller;

import com.growx.dto.response.ApiResponse;
import com.growx.dto.response.WeatherResponse;
import com.growx.security.UserPrincipal;
import com.growx.service.WeatherService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * Weather endpoints — all require a valid JWT.
 *
 * GET /api/weather/farm/{farmId} → get current weather and forecast for the farm's location
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
        WeatherResponse response = weatherService.getWeatherForFarm(farmId, principal.getId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
