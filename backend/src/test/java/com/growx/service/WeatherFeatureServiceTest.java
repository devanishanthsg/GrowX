package com.growx.service;

import com.growx.dto.response.WeatherResponse;
import com.growx.dto.weather.DailyWeatherDto;
import com.growx.dto.weather.WeatherFeatureHintsDto;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class WeatherFeatureServiceTest {

    private final WeatherFeatureService service = new WeatherFeatureService();

    @Test
    void producesRepresentativeTemperatureAndHumidityButNeverAutofillsRainfall() {
        WeatherResponse weather = WeatherResponse.builder()
                .daily(List.of(
                        day(20, 30, 70),
                        day(22, 32, 80)
                ))
                .build();

        WeatherFeatureHintsDto hints = service.buildHints(weather);

        assertEquals(26.0, hints.getTemperature());
        assertEquals(75.0, hints.getHumidity());
        assertFalse(hints.isRainfallAutofillEnabled());
        assertNull(hints.getRainfall());
        assertTrue(hints.getRainfallMessage().contains("Rain probability is never used as rainfall amount"));
    }

    private DailyWeatherDto day(double min, double max, double humidity) {
        return DailyWeatherDto.builder()
                .date(LocalDate.of(2026, 8, 19))
                .minTemperature(min)
                .maxTemperature(max)
                .averageHumidity(humidity)
                .build();
    }
}
