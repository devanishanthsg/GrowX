package com.growx.util;

/**
 * API endpoint constants for GrowX.
 * Central reference for all base paths to avoid string duplication.
 */
public final class ApiConstants {

    private ApiConstants() {}

    public static final String API_BASE = "/api";

    public static final String AUTH_BASE = API_BASE + "/auth";
    public static final String AUTH_REGISTER = AUTH_BASE + "/register";
    public static final String AUTH_LOGIN = AUTH_BASE + "/login";

    public static final String USERS_BASE = API_BASE + "/users";
    public static final String USERS_ME = USERS_BASE + "/me";

    public static final String FARMS_BASE = API_BASE + "/farms";

    public static final String CROP_RECOMMENDATION_BASE = API_BASE + "/crop-recommendation";

    public static final String DISEASE_DETECTION_BASE = API_BASE + "/disease-detection";

    public static final String WEATHER_BASE = API_BASE + "/weather";

    public static final String DASHBOARD_BASE = API_BASE + "/dashboard";

    public static final String REPORTS_BASE = API_BASE + "/reports";
}
