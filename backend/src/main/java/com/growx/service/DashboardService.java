package com.growx.service;

import com.growx.dto.response.DashboardResponse;
import com.growx.entity.CropRecommendation;
import com.growx.entity.DiseaseDetection;
import com.growx.entity.Farm;
import com.growx.exception.ResourceNotFoundException;
import com.growx.repository.CropRecommendationRepository;
import com.growx.repository.DiseaseDetectionRepository;
import com.growx.repository.FarmRepository;
import com.growx.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Aggregates data from multiple GrowX modules into a single Dashboard response.
 * The Dashboard does not have its own database table — it pulls from existing entities.
 *
 * The service builds:
 * - Farm & farmer overview
 * - Stat placeholders (soil moisture, temperature, humidity, crop health)
 *   — these will be real values once sensor/weather integration is active
 * - AI recommendations list (from latest crop recommendations)
 * - Recent activity (from crop recommendations and disease detections)
 */
@Service
@RequiredArgsConstructor
public class DashboardService {

    private final FarmRepository farmRepository;
    private final UserRepository userRepository;
    private final CropRecommendationRepository cropRecommendationRepository;
    private final DiseaseDetectionRepository diseaseDetectionRepository;

    private static final DateTimeFormatter DISPLAY_FORMATTER =
            DateTimeFormatter.ofPattern("MMM d, yyyy h:mm a");

    @Transactional(readOnly = true)
    public DashboardResponse getDashboard(Long userId) {
        // Use the user's primary farm
        List<Farm> farms = farmRepository.findByOwnerId(userId);
        if (farms.isEmpty()) {
            throw new ResourceNotFoundException(
                    "No farm found for this account. Please add a farm first.");
        }
        Farm farm = farms.get(0);

        // Build recent activity from DB (latest 5 events)
        List<DashboardResponse.ActivityItem> activities = buildRecentActivity(farm.getId());

        // Build AI recommendation items from latest crop recommendation
        List<DashboardResponse.RecommendationItem> recommendations = buildRecommendations(farm.getId());

        return DashboardResponse.builder()
                .farmName(farm.getFarmName())
                .location(farm.getLocation())
                .farmerName(farm.getOwner().getName())
                // Stat values will be populated from sensors/weather when integrated
                .soilMoisture(null)
                .temperature(null)
                .humidity(null)
                .cropHealth(null)
                // Weather summary will be populated from WeatherService when API key is configured
                .weatherSummary(null)
                .recommendations(recommendations)
                .recentActivity(activities)
                .build();
    }

    private List<DashboardResponse.ActivityItem> buildRecentActivity(Long farmId) {
        List<DashboardResponse.ActivityItem> items = new ArrayList<>();

        // Crop recommendations
        cropRecommendationRepository.findByFarmIdOrderByCreatedAtDesc(farmId)
                .stream()
                .limit(3)
                .forEach(rec -> items.add(DashboardResponse.ActivityItem.builder()
                        .action("Crop recommendation generated")
                        .time(rec.getCreatedAt() != null
                                ? rec.getCreatedAt().format(DISPLAY_FORMATTER)
                                : "—")
                        .build()));

        // Disease detections
        diseaseDetectionRepository.findByFarmIdOrderByDetectedAtDesc(farmId)
                .stream()
                .limit(2)
                .forEach(det -> items.add(DashboardResponse.ActivityItem.builder()
                        .action("Disease image analysed")
                        .time(det.getDetectedAt() != null
                                ? det.getDetectedAt().format(DISPLAY_FORMATTER)
                                : "—")
                        .build()));

        // Sort by time (descending) — simplified approach using list order from DB queries above
        return items.stream().limit(5).toList();
    }

    private List<DashboardResponse.RecommendationItem> buildRecommendations(Long farmId) {
        List<DashboardResponse.RecommendationItem> items = new ArrayList<>();

        cropRecommendationRepository.findByFarmIdOrderByCreatedAtDesc(farmId)
                .stream()
                .findFirst()
                .ifPresent(rec -> {
                    if (rec.getRecommendedCrop() != null) {
                        items.add(DashboardResponse.RecommendationItem.builder()
                                .title("Recommended Crop")
                                .message(rec.getRecommendedCrop() + " — " +
                                        (rec.getSeason() != null ? rec.getSeason() + " season" : ""))
                                .icon("🌾")
                                .build());
                    }
                });

        // Fallback static recommendation if no data yet
        if (items.isEmpty()) {
            items.add(DashboardResponse.RecommendationItem.builder()
                    .title("Crop Recommendation")
                    .message("Run a crop recommendation analysis to see suggestions here.")
                    .icon("🌾")
                    .build());
        }

        return items;
    }
}
