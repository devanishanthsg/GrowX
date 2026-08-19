function show(value, suffix = "") {
  return value == null ? "—" : `${Number(value).toFixed(2)}${suffix}`;
}

function AgriculturalIndicators({ agriculture }) {
  if (!agriculture) return null;

  return (
    <section className="panel weather-v2-agriculture-panel">
      <div className="panel-heading">
        <div>
          <h2>Agricultural Indicators</h2>
          <p>Weather-model estimates that can support farm planning.</p>
        </div>
      </div>

      <div className="weather-v2-indicator-grid">
        <div>
          <span>Reference ET₀</span>
          <strong>{show(agriculture.referenceEvapotranspiration, " mm")}</strong>
          <small>Latest hourly FAO-56 reference evapotranspiration.</small>
        </div>
        <div>
          <span>Evapotranspiration</span>
          <strong>{show(agriculture.evapotranspiration, " mm")}</strong>
          <small>Weather-model estimate for the latest hour.</small>
        </div>
        <div>
          <span>Estimated / Modelled Soil Moisture</span>
          <strong>{show(agriculture.modelledSoilMoisture, " m³/m³")}</strong>
          <small>Not a physical farm sensor measurement.</small>
        </div>
        <div>
          <span>Modelled Soil Temperature</span>
          <strong>{show(agriculture.soilTemperature, "°C")}</strong>
          <small>Near-surface weather-model estimate.</small>
        </div>
        <div>
          <span>Vapour Pressure Deficit</span>
          <strong>{show(agriculture.vapourPressureDeficit, " kPa")}</strong>
          <small>Technical dry-air indicator; crop tolerance varies.</small>
        </div>
      </div>
    </section>
  );
}

export default AgriculturalIndicators;
