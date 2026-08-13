package com.growx.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Dashboard aggregated data response.
 * Powers the main Dashboard page which shows farm stats,
 * a weather summary, AI recommendations, and recent activity.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardResponse {

    // ── Farm Overview ─────────────────────────────────────────────────────────

    private String farmName;
    private String location;
    private String farmerName;

    // ── Stat Cards ────────────────────────────────────────────────────────────

    /** Soil moisture percentage — sourced from sensors or entered manually */
    private Double soilMoisture;

    /** Current temperature in Celsius */
    private Double temperature;

    /** Humidity percentage */
    private Double humidity;

    /** Crop health score 0–100 */
    private Double cropHealth;

    // ── Weather Summary ───────────────────────────────────────────────────────

    private WeatherResponse weatherSummary;

    // ── AI Recommendations ────────────────────────────────────────────────────

    /** Short advisory items displayed in the "AI recommendations" panel */
    private List<RecommendationItem> recommendations;

    // ── Recent Activity ───────────────────────────────────────────────────────

    private List<ActivityItem> recentActivity;

    // ── Inner classes (avoids creating separate files for simple value types) ─

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecommendationItem {
        private String title;
        private String message;
        private String icon;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ActivityItem {
        private String action;
        private String time;
    }
}
