package com.growx.constants;

/**
 * Security-related constants for GrowX.
 */
public final class SecurityConstants {

    private SecurityConstants() {}

    /** JWT Authorization header name */
    public static final String AUTH_HEADER = "Authorization";

    /** JWT token prefix */
    public static final String BEARER_PREFIX = "Bearer ";

    /** Default JWT expiration: 24 hours in milliseconds */
    public static final long DEFAULT_EXPIRATION_MS = 86_400_000L;

    /** Minimum password length (enforced at DTO validation level) */
    public static final int MIN_PASSWORD_LENGTH = 6;
}
