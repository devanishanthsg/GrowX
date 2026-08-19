package com.growx.dto.weather;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgriculturalWeatherDto {
    /** Latest hourly FAO-56 reference evapotranspiration, millimetres. */
    private Double referenceEvapotranspiration;

    /** Latest hourly modelled actual evapotranspiration, millimetres. */
    private Double evapotranspiration;

    /** Modelled volumetric soil-water content (m3/m3), not a physical farm sensor reading. */
    private Double modelledSoilMoisture;

    /** Modelled near-surface soil temperature in Celsius. */
    private Double soilTemperature;

    /** Vapour pressure deficit in kPa. */
    private Double vapourPressureDeficit;
}
