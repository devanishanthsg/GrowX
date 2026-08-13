package com.growx.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Farm details returned to the frontend.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FarmResponse {

    private Long id;
    private String farmName;
    private String location;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private BigDecimal area;
    private String areaUnit;
    private String soilType;
    private String mainCrop;
    private Long ownerId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
