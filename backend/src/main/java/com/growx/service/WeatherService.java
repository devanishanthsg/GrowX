package com.growx.service;

import com.growx.dto.response.WeatherResponse;
import com.growx.client.WeatherApiClient;
import com.growx.entity.Farm;
import com.growx.exception.ResourceNotFoundException;
import com.growx.repository.FarmRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Fetches weather data for a given farm.
 *
 * When coordinates are available on the farm, they take priority over
 * the location string for more accurate weather data.
 *
 * The WeatherApiClient is not yet connected — wire in an implementation
 * (e.g. OpenWeatherMapClient) when the external API key is available.
 */
@Service
@RequiredArgsConstructor
public class WeatherService {

    private final FarmRepository farmRepository;

    // Weather API client is not yet wired — will be @Autowired when implemented
    // private final WeatherApiClient weatherApiClient;

    /**
     * Returns weather data for a specific farm, using coordinates if available.
     *
     * @param farmId the farm whose location will drive the weather query
     * @param userId the authenticated user (for ownership verification)
     * @return WeatherResponse from the external API
     */
    public WeatherResponse getWeatherForFarm(Long farmId, Long userId) {
        Farm farm = farmRepository.findByIdAndOwnerId(farmId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Farm", farmId));

        // TODO: Uncomment when WeatherApiClient implementation is available
        // if (farm.getLatitude() != null && farm.getLongitude() != null) {
        //     return weatherApiClient.getWeatherByCoordinates(
        //         farm.getLatitude().doubleValue(),
        //         farm.getLongitude().doubleValue()
        //     );
        // }
        // return weatherApiClient.getWeatherByLocation(farm.getLocation());

        // Architecture is ready — return a placeholder until the API client is connected
        throw new UnsupportedOperationException(
            "Weather API client is not yet configured. " +
            "Please implement WeatherApiClient and register it as a Spring bean."
        );
    }
}
