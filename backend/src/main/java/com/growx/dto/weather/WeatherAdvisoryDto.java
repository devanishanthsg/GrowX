package com.growx.dto.weather;

import com.growx.enums.RiskLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WeatherAdvisoryDto {
    private String type;
    private String status;
    private RiskLevel severity;
    private String title;
    private String explanation;
    private String recommendedAction;
    private Map<String, Double> metrics;
    private LocalDateTime windowStart;
    private LocalDateTime windowEnd;
}
