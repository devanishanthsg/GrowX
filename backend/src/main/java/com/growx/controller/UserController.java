package com.growx.controller;

import com.growx.dto.request.UpdateProfileRequest;
import com.growx.dto.response.ApiResponse;
import com.growx.dto.response.UserResponse;
import com.growx.security.UserPrincipal;
import com.growx.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * User profile endpoints — all require a valid JWT.
 *
 * GET /api/users/me  → get the authenticated user's profile
 * PUT /api/users/me  → update profile (name, email, phone, farm fields)
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getProfile(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        UserResponse response = userService.getUserById(principal.getId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> updateProfile(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody UpdateProfileRequest request
    ) {
        UserResponse response = userService.updateProfile(principal.getId(), request);
        return ResponseEntity.ok(ApiResponse.success("Profile updated successfully.", response));
    }
}
