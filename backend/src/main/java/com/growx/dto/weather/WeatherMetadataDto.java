package com.growx.dto.weather;

import com.growx.enums.WeatherDataStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WeatherMetadataDto {
    private String provider;
    private OffsetDateTime fetchedAt;
    private Long dataAgeMinutes;
    private WeatherDataStatus status;
    private boolean stale;
}
