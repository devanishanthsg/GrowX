function valueOrDash(value, suffix = "") {
  return value == null || Number.isNaN(Number(value))
    ? "—"
    : `${Number(value).toFixed(Number(value) % 1 === 0 ? 0 : 1)}${suffix}`;
}

function CurrentWeatherCard({ weather }) {
  const current = weather?.current;

  if (!current) return null;

  return (
    <section className="weather-v2-current panel">
      <div className="weather-v2-current-main">
        <div>
          <p className="weather-v2-kicker">CURRENT CONDITIONS</p>
          <h2>{valueOrDash(current.temperature, "°C")}</h2>
          <h3>{current.conditionDescription ?? "Weather data available"}</h3>
          {current.feelsLike != null && (
            <span>Feels like {valueOrDash(current.feelsLike, "°C")}</span>
          )}
        </div>

        <div className="weather-v2-current-icon" aria-hidden="true">
          {current.icon ?? "🌡️"}
        </div>
      </div>

      <div className="weather-v2-current-metrics">
        <div>
          <span>Humidity</span>
          <strong>{valueOrDash(current.humidity, "%")}</strong>
        </div>
        <div>
          <span>Wind</span>
          <strong>{valueOrDash(current.windSpeed, " km/h")}</strong>
        </div>
        <div>
          <span>Wind gust</span>
          <strong>{valueOrDash(current.windGust, " km/h")}</strong>
        </div>
        <div>
          <span>Precipitation</span>
          <strong>{valueOrDash(current.precipitation, " mm")}</strong>
        </div>
        <div>
          <span>Rain probability</span>
          <strong>{valueOrDash(current.rainProbability, "%")}</strong>
        </div>
        <div>
          <span>Cloud cover</span>
          <strong>{valueOrDash(current.cloudCover, "%")}</strong>
        </div>
        <div>
          <span>UV index</span>
          <strong>{valueOrDash(current.uvIndex)}</strong>
        </div>
        <div>
          <span>Visibility</span>
          <strong>{valueOrDash(current.visibilityKm, " km")}</strong>
        </div>
      </div>
    </section>
  );
}

export default CurrentWeatherCard;
