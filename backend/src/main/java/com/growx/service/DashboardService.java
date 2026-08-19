package com.growx.service;

import com.growx.dto.response.DashboardResponse;
import com.growx.dto.response.WeatherResponse;
import com.growx.dto.weather.CurrentWeatherDto;
import com.growx.entity.Farm;
import com.growx.enums.WeatherDataStatus;
import com.growx.exception.ResourceNotFoundException;
import com.growx.repository.CropRecommendationRepository;
import com.growx.repository.DiseaseDetectionRepository;
import com.growx.repository.FarmRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final FarmRepository farmRepository;
    private final CropRecommendationRepository cropRecommendationRepository;
    private final DiseaseDetectionRepository diseaseDetectionRepository;
    private final WeatherService weatherService;

    private static final DateTimeFormatter DISPLAY_FORMATTER =
            DateTimeFormatter.ofPattern("MMM d, yyyy h:mm a");

    @Transactional(readOnly = true)
    public DashboardResponse getDashboard(Long userId) {
        List<Farm> farms = farmRepository.findByOwnerId(userId);
        if (farms.isEmpty()) {
            throw new ResourceNotFoundException(
                    "No farm found for this account. Please add a farm first.");
        }
        Farm farm = farms.get(0);

        List<DashboardResponse.ActivityItem> activities = buildRecentActivity(farm.getId());
        List<DashboardResponse.RecommendationItem> recommendations = buildRecommendations(farm.getId());

        WeatherResponse weather = weatherService.getWeatherForFarm(farm.getId(), userId);
        CurrentWeatherDto current = weather.getCurrent();
        boolean weatherAvailable = weather.getMetadata() != null
                && weather.getMetadata().getStatus() != WeatherDataStatus.UNAVAILABLE
                && current != null;

        DashboardResponse.DashboardWeatherSummary weatherSummary = DashboardResponse.DashboardWeatherSummary.builder()
                .location(weather.getLocationName())
                .icon(current == null ? null : current.getIcon())
                .temperature(current == null ? null : current.getTemperature())
                .condition(current == null ? null : current.getConditionDescription())
                .humidity(current == null ? null : current.getHumidity())
                .windSpeed(current == null ? null : current.getWindSpeed())
                .precipitation(current == null ? null : current.getPrecipitation())
                .rainProbability(current == null ? null : current.getRainProbability())
                .overallRisk(weather.getOverallRisk())
                .overallRiskReason(weather.getOverallRiskReason())
                .dataStatus(weather.getMetadata() == null ? null : weather.getMetadata().getStatus())
                .fetchedAt(weather.getMetadata() == null ? null : weather.getMetadata().getFetchedAt())
                .dataAgeMinutes(weather.getMetadata() == null ? null : weather.getMetadata().getDataAgeMinutes())
                .build();

        return DashboardResponse.builder()
                .farmName(farm.getFarmName())
                .location(farm.getLocation())
                .farmerName(farm.getOwner().getName())
                // Keep sensor-only fields distinct from weather model estimates.
                .soilMoisture(null)
                .temperature(weatherAvailable ? current.getTemperature() : null)
                .humidity(weatherAvailable ? current.getHumidity() : null)
                .cropHealth(null)
                .weatherSummary(weatherSummary)
                .recommendations(recommendations)
                .recentActivity(activities)
                .build();
    }

    private List<DashboardResponse.ActivityItem> buildRecentActivity(Long farmId) {
        List<DashboardResponse.ActivityItem> items = new ArrayList<>();

        cropRecommendationRepository.findByFarmIdOrderByCreatedAtDesc(farmId)
                .stream()
                .limit(3)
                .forEach(rec -> items.add(DashboardResponse.ActivityItem.builder()
                        .action("Crop recommendation generated")
                        .time(rec.getCreatedAt() != null
                                ? rec.getCreatedAt().format(DISPLAY_FORMATTER)
                                : "—")
                        .build()));

        diseaseDetectionRepository.findByFarmIdOrderByDetectedAtDesc(farmId)
                .stream()
                .limit(2)
                .forEach(det -> items.add(DashboardResponse.ActivityItem.builder()
                        .action("Disease image analysed")
                        .time(det.getDetectedAt() != null
                                ? det.getDetectedAt().format(DISPLAY_FORMATTER)
                                : "—")
                        .build()));

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
