package com.growx.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Request body for PUT /api/users/me.
 * Maps to the Profile page form fields:
 * name, email, phone, farmName, location, landArea, mainCrop.
 *
 * Note: farmName, location, landArea, and mainCrop are included here
 * for a simplified "combined profile + primary farm update" endpoint.
 * If multi-farm support is fully active, these farm fields move to the
 * Farm update endpoint instead.
 */
@Data
public class UpdateProfileRequest {

    @NotBlank(message = "Name is required")
    @Size(max = 100, message = "Name must be at most 100 characters")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email address")
    @Size(max = 150, message = "Email must be at most 150 characters")
    private String email;

    @Size(max = 20, message = "Phone number must be at most 20 characters")
    private String phone;

    // Farm-level fields (for the primary farm)

    @Size(max = 150, message = "Farm name must be at most 150 characters")
    private String farmName;

    @Size(max = 200, message = "Location must be at most 200 characters")
    private String location;

    /** Land area in acres (matches the frontend "Land area in acres" field) */
    private Double landArea;

    @Size(max = 100, message = "Main crop must be at most 100 characters")
    private String mainCrop;
}
