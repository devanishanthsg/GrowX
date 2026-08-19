package com.growx.dto.response;

import com.growx.enums.RiskLevel;
import com.growx.enums.WeatherDataStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardResponse {

    private String farmName;
    private String location;
    private String farmerName;

    /** Sensor value only; weather-model soil moisture is never represented as a physical sensor reading. */
    private Double soilMoisture;
    private Double temperature;
    private Double humidity;
    private Double cropHealth;

    private DashboardWeatherSummary weatherSummary;
    private List<RecommendationItem> recommendations;
    private List<ActivityItem> recentActivity;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DashboardWeatherSummary {
        private String location;
        private String icon;
        private Double temperature;
        private String condition;
        private Double humidity;
        private Double windSpeed;
        private Double precipitation;
        private Double rainProbability;
        private RiskLevel overallRisk;
        private String overallRiskReason;
        private WeatherDataStatus dataStatus;
        private OffsetDateTime fetchedAt;
        private Long dataAgeMinutes;
    }

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
