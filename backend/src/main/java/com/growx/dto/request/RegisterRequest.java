package com.growx.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Request body for POST /api/auth/register.
 * Maps to the Register page form fields: name, email, location, password.
 */
@Data
public class RegisterRequest {

    @NotBlank(message = "Name is required")
    @Size(max = 100, message = "Name must be at most 100 characters")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email address")
    @Size(max = 150, message = "Email must be at most 150 characters")
    private String email;

    /**
     * Farm location provided at registration (e.g., "Coimbatore").
     * Used to create the farmer's initial farm record.
     */
    @NotBlank(message = "Farm location is required")
    @Size(max = 200, message = "Location must be at most 200 characters")
    private String location;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;
}
