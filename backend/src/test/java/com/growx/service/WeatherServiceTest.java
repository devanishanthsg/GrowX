package com.growx.service;

import com.growx.client.WeatherApiClient;
import com.growx.dto.response.WeatherResponse;
import com.growx.dto.weather.CurrentWeatherDto;
import com.growx.dto.weather.WeatherMetadataDto;
import com.growx.entity.Farm;
import com.growx.enums.WeatherDataStatus;
import com.growx.exception.ResourceNotFoundException;
import com.growx.repository.FarmRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WeatherServiceTest {

    @Mock
    private FarmRepository farmRepository;

    @Mock
    private WeatherApiClient weatherApiClient;

    private WeatherService service;

    @BeforeEach
    void setUp() {
        service = new WeatherService(
                farmRepository,
                weatherApiClient,
                new AgroWeatherService(),
                new WeatherAdvisoryService(),
                new WeatherFeatureService()
        );
        ReflectionTestUtils.setField(service, "cacheTtlMinutes", 10L);
        ReflectionTestUtils.setField(service, "staleAfterMinutes", 60L);
        ReflectionTestUtils.setField(service, "minRefreshIntervalSeconds", 0L);
    }

    @Test
    void successfulProviderResponseIsReturnedAsLive() {
        Farm farm = farmWithCoordinates();
        when(farmRepository.findByIdAndOwnerId(4L, 9L)).thenReturn(Optional.of(farm));
        when(weatherApiClient.getWeatherByCoordinates(anyDouble(), anyDouble()))
                .thenReturn(providerResponse());

        WeatherResponse result = service.getWeatherForFarm(4L, 9L);

        assertEquals(WeatherDataStatus.LIVE, result.getMetadata().getStatus());
        assertEquals("FARM_COORDINATES", result.getCoordinateSource());
        assertEquals(4L, result.getFarmId());
        verify(weatherApiClient).getWeatherByCoordinates(11.0168, 76.9558);
    }

    @Test
    void missingFarmIsRejected() {
        when(farmRepository.findByIdAndOwnerId(99L, 9L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> service.getWeatherForFarm(99L, 9L));
        verifyNoInteractions(weatherApiClient);
    }

    @Test
    void providerFailureWithoutCacheReturnsUnavailableInsteadOfFakeValues() {
        Farm farm = farmWithCoordinates();
        when(farmRepository.findByIdAndOwnerId(4L, 9L)).thenReturn(Optional.of(farm));
        when(weatherApiClient.getWeatherByCoordinates(anyDouble(), anyDouble()))
                .thenThrow(new IllegalStateException("provider down"));

        WeatherResponse result = service.getWeatherForFarm(4L, 9L);

        assertEquals(WeatherDataStatus.UNAVAILABLE, result.getMetadata().getStatus());
        assertNull(result.getCurrent());
    }

    @Test
    void providerFailureFallsBackToRecentCache() {
        Farm farm = farmWithCoordinates();
        when(farmRepository.findByIdAndOwnerId(4L, 9L)).thenReturn(Optional.of(farm));
        when(weatherApiClient.getWeatherByCoordinates(anyDouble(), anyDouble()))
                .thenReturn(providerResponse())
                .thenThrow(new IllegalStateException("temporary provider failure"));

        WeatherResponse first = service.getWeatherForFarm(4L, 9L);
        WeatherResponse fallback = service.refreshWeatherForFarm(4L, 9L);

        assertEquals(WeatherDataStatus.LIVE, first.getMetadata().getStatus());
        assertEquals(WeatherDataStatus.CACHED, fallback.getMetadata().getStatus());
        assertNotNull(fallback.getCurrent());
        assertTrue(fallback.getWarnings().stream().anyMatch(text -> text.contains("cached")));
    }


    @Test
    void partialCoordinatePairIsRejectedBeforeProviderCall() {
        Farm farm = Farm.builder()
                .id(4L)
                .farmName("Demo Farm")
                .location("Coimbatore, Tamil Nadu")
                .latitude(BigDecimal.valueOf(11.0168))
                .longitude(null)
                .build();
        when(farmRepository.findByIdAndOwnerId(4L, 9L)).thenReturn(Optional.of(farm));

        assertThrows(com.growx.exception.BadRequestException.class,
                () -> service.getWeatherForFarm(4L, 9L));
        verifyNoInteractions(weatherApiClient);
    }

    @Test
    void missingCoordinatesUsesLocationGeocodingFallback() {
        Farm farm = Farm.builder()
                .id(4L)
                .farmName("Demo Farm")
                .location("Coimbatore, Tamil Nadu")
                .build();
        when(farmRepository.findByIdAndOwnerId(4L, 9L)).thenReturn(Optional.of(farm));
        when(weatherApiClient.getWeatherByLocation("Coimbatore, Tamil Nadu"))
                .thenReturn(providerResponse());

        WeatherResponse result = service.getWeatherForFarm(4L, 9L);

        assertEquals("GEOCODED_FALLBACK", result.getCoordinateSource());
        verify(weatherApiClient).getWeatherByLocation("Coimbatore, Tamil Nadu");
    }

    private Farm farmWithCoordinates() {
        return Farm.builder()
                .id(4L)
                .farmName("Demo Farm")
                .location("Coimbatore, Tamil Nadu")
                .latitude(BigDecimal.valueOf(11.0168))
                .longitude(BigDecimal.valueOf(76.9558))
                .build();
    }

    private WeatherResponse providerResponse() {
        return WeatherResponse.builder()
                .latitude(11.0168)
                .longitude(76.9558)
                .timezone("Asia/Kolkata")
                .current(CurrentWeatherDto.builder()
                        .temperature(29.0)
                        .humidity(75.0)
                        .precipitation(0.0)
                        .rainProbability(10.0)
                        .windSpeed(8.0)
                        .windGust(12.0)
                        .conditionDescription("Partly cloudy")
                        .icon("⛅")
                        .build())
                .hourly(List.of())
                .daily(List.of())
                .metadata(WeatherMetadataDto.builder()
                        .provider("Open-Meteo")
                        .fetchedAt(OffsetDateTime.now(ZoneOffset.UTC))
                        .status(WeatherDataStatus.LIVE)
                        .stale(false)
                        .build())
                .warnings(List.of())
                .build();
    }
}
