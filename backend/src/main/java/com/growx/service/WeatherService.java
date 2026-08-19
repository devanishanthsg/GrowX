package com.growx.service;

import com.growx.client.WeatherApiClient;
import com.growx.dto.response.WeatherResponse;
import com.growx.dto.weather.WeatherMetadataDto;
import com.growx.entity.Farm;
import com.growx.enums.WeatherDataStatus;
import com.growx.exception.BadRequestException;
import com.growx.exception.ResourceNotFoundException;
import com.growx.repository.FarmRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Single source of truth for farm weather in GrowX.
 *
 * Responsibilities:
 * - verifies farm ownership
 * - prefers stored WGS84 coordinates and falls back to location geocoding
 * - prevents repeated provider calls through a short-lived in-memory cache
 * - returns cached/stale data when the provider fails
 * - enriches normalized provider data with farmer advisories and crop-input hints
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WeatherService {

    private final FarmRepository farmRepository;
    private final WeatherApiClient weatherApiClient;
    private final AgroWeatherService agroWeatherService;
    private final WeatherAdvisoryService weatherAdvisoryService;
    private final WeatherFeatureService weatherFeatureService;

    private final Map<String, CacheEntry> cache = new ConcurrentHashMap<>();

    @Value("${growx.weather.cache-ttl-minutes:10}")
    private long cacheTtlMinutes;

    @Value("${growx.weather.stale-after-minutes:60}")
    private long staleAfterMinutes;

    @Value("${growx.weather.min-refresh-interval-seconds:60}")
    private long minRefreshIntervalSeconds;

    @Transactional(readOnly = true)
    public WeatherResponse getWeatherForFarm(Long farmId, Long userId) {
        Farm farm = findOwnedFarm(farmId, userId);
        return loadWeather(farm, false);
    }

    @Transactional(readOnly = true)
    public WeatherResponse refreshWeatherForFarm(Long farmId, Long userId) {
        Farm farm = findOwnedFarm(farmId, userId);
        return loadWeather(farm, true);
    }

    private Farm findOwnedFarm(Long farmId, Long userId) {
        return farmRepository.findByIdAndOwnerId(farmId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Farm", farmId));
    }

    private WeatherResponse loadWeather(Farm farm, boolean forceRefresh) {
        validateStoredCoordinates(farm);

        String key = cacheKey(farm);
        CacheEntry cached = cache.get(key);
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);

        if (cached != null) {
            long ageSeconds = Math.max(0, Duration.between(cached.fetchedAt(), now).getSeconds());
            long ageMinutes = ageSeconds / 60;

            if (!forceRefresh && ageMinutes < Math.max(1, cacheTtlMinutes)) {
                log.debug("Serving cached weather for farm {} ({} minutes old)", farm.getId(), ageMinutes);
                return withDataStatus(cached.response(), WeatherDataStatus.CACHED, ageMinutes, false, null);
            }

            if (forceRefresh
                    && minRefreshIntervalSeconds > 0
                    && ageSeconds < minRefreshIntervalSeconds) {
                String warning = "Weather was refreshed recently. The cached result is reused to avoid unnecessary provider requests.";
                return withDataStatus(cached.response(), WeatherDataStatus.CACHED, ageMinutes, false, warning);
            }
        }

        try {
            WeatherResponse providerResponse;
            boolean hasCoordinates = hasUsableCoordinates(farm);

            if (hasCoordinates) {
                providerResponse = weatherApiClient.getWeatherByCoordinates(
                        farm.getLatitude().doubleValue(),
                        farm.getLongitude().doubleValue()
                );
            } else {
                log.info("Farm {} has no stored coordinates; using location-name geocoding fallback", farm.getId());
                providerResponse = weatherApiClient.getWeatherByLocation(farm.getLocation());
            }

            WeatherResponse live = enrich(providerResponse, farm, hasCoordinates);
            OffsetDateTime fetchedAt = live.getMetadata() != null && live.getMetadata().getFetchedAt() != null
                    ? live.getMetadata().getFetchedAt()
                    : now;

            cache.put(key, new CacheEntry(live, fetchedAt));
            log.info("Live weather updated for farm {} from {}", farm.getId(), live.getMetadata().getProvider());
            return live;

        } catch (RuntimeException ex) {
            log.warn("Weather provider failed for farm {}: {}", farm.getId(), ex.getMessage());

            if (cached != null) {
                long ageMinutes = Math.max(0, Duration.between(cached.fetchedAt(), now).toMinutes());
                boolean stale = ageMinutes > Math.max(cacheTtlMinutes, staleAfterMinutes);
                WeatherDataStatus status = stale ? WeatherDataStatus.STALE : WeatherDataStatus.CACHED;
                String warning = stale
                        ? "Live weather is unavailable. Showing older cached forecast data; conditions may have changed."
                        : "Live weather is temporarily unavailable. Showing the most recent cached forecast.";
                log.info("Falling back to {} weather for farm {} ({} minutes old)", status, farm.getId(), ageMinutes);
                return withDataStatus(cached.response(), status, ageMinutes, stale, warning);
            }

            return unavailableResponse(farm,
                    hasUsableCoordinates(farm)
                            ? "Live weather is temporarily unavailable and no cached data exists yet."
                            : "Farm coordinates are missing and the saved location could not be resolved reliably. Add latitude/longitude in Farm Profile for precise local weather."
            );
        }
    }

    private WeatherResponse enrich(WeatherResponse providerResponse, Farm farm, boolean usedStoredCoordinates) {
        List<String> warnings = new ArrayList<>();
        if (providerResponse.getWarnings() != null) {
            warnings.addAll(providerResponse.getWarnings());
        }

        String coordinateSource = usedStoredCoordinates
                ? "FARM_COORDINATES"
                : "GEOCODED_FALLBACK";

        if (!usedStoredCoordinates) {
            warnings.add("Weather coordinates were estimated from the farm location name. Add exact latitude/longitude for more precise local weather.");
        }

        WeatherResponse farmWeather = providerResponse.toBuilder()
                .farmId(farm.getId())
                .farmName(farm.getFarmName())
                .locationName(farm.getLocation())
                .coordinateSource(coordinateSource)
                .warnings(warnings)
                .build();

        AgroWeatherService.AgroWeatherSummary metrics = agroWeatherService.summarize(farmWeather);
        WeatherAdvisoryService.AdvisoryBundle advisoryBundle = weatherAdvisoryService.build(farmWeather, metrics);

        WeatherMetadataDto providerMetadata = providerResponse.getMetadata();
        WeatherMetadataDto liveMetadata = WeatherMetadataDto.builder()
                .provider(providerMetadata == null || providerMetadata.getProvider() == null
                        ? "Open-Meteo"
                        : providerMetadata.getProvider())
                .fetchedAt(providerMetadata == null || providerMetadata.getFetchedAt() == null
                        ? OffsetDateTime.now(ZoneOffset.UTC)
                        : providerMetadata.getFetchedAt())
                .dataAgeMinutes(0L)
                .status(WeatherDataStatus.LIVE)
                .stale(false)
                .build();

        return farmWeather.toBuilder()
                .daily(advisoryBundle.daily())
                .advisories(advisoryBundle.advisories())
                .overallRisk(advisoryBundle.overallRisk())
                .overallRiskReason(advisoryBundle.overallRiskReason())
                .cropFeatureHints(weatherFeatureService.buildHints(farmWeather))
                .metadata(liveMetadata)
                .build();
    }

    private WeatherResponse withDataStatus(
            WeatherResponse source,
            WeatherDataStatus status,
            long ageMinutes,
            boolean stale,
            String warning
    ) {
        WeatherMetadataDto previous = source.getMetadata();
        WeatherMetadataDto metadata = WeatherMetadataDto.builder()
                .provider(previous == null ? "Open-Meteo" : previous.getProvider())
                .fetchedAt(previous == null ? null : previous.getFetchedAt())
                .dataAgeMinutes(ageMinutes)
                .status(status)
                .stale(stale)
                .build();

        List<String> warnings = new ArrayList<>();
        if (source.getWarnings() != null) warnings.addAll(source.getWarnings());
        if (warning != null && !warning.isBlank()) warnings.add(warning);

        return source.toBuilder()
                .metadata(metadata)
                .warnings(warnings)
                .build();
    }

    private WeatherResponse unavailableResponse(Farm farm, String warning) {
        return WeatherResponse.builder()
                .farmId(farm.getId())
                .farmName(farm.getFarmName())
                .locationName(farm.getLocation())
                .latitude(farm.getLatitude() == null ? null : farm.getLatitude().doubleValue())
                .longitude(farm.getLongitude() == null ? null : farm.getLongitude().doubleValue())
                .coordinateSource(hasUsableCoordinates(farm) ? "FARM_COORDINATES" : "MISSING_COORDINATES")
                .warnings(List.of(warning))
                .metadata(WeatherMetadataDto.builder()
                        .provider("Open-Meteo")
                        .fetchedAt(null)
                        .dataAgeMinutes(null)
                        .status(WeatherDataStatus.UNAVAILABLE)
                        .stale(false)
                        .build())
                .build();
    }

    private String cacheKey(Farm farm) {
        if (hasUsableCoordinates(farm)) {
            return String.format("farm:%d:%.6f:%.6f",
                    farm.getId(),
                    farm.getLatitude().doubleValue(),
                    farm.getLongitude().doubleValue());
        }
        return "farm:" + farm.getId() + ":location:" + String.valueOf(farm.getLocation()).trim().toLowerCase();
    }

    private boolean hasUsableCoordinates(Farm farm) {
        return farm.getLatitude() != null && farm.getLongitude() != null;
    }

    private void validateStoredCoordinates(Farm farm) {
        if (farm.getLatitude() == null && farm.getLongitude() == null) return;

        if (farm.getLatitude() == null || farm.getLongitude() == null) {
            throw new BadRequestException("Both farm latitude and longitude must be provided together.");
        }

        double latitude = farm.getLatitude().doubleValue();
        double longitude = farm.getLongitude().doubleValue();
        if (latitude < -90 || latitude > 90) {
            throw new BadRequestException("Farm latitude must be between -90 and 90.");
        }
        if (longitude < -180 || longitude > 180) {
            throw new BadRequestException("Farm longitude must be between -180 and 180.");
        }
    }

    private record CacheEntry(WeatherResponse response, OffsetDateTime fetchedAt) {
    }
}
