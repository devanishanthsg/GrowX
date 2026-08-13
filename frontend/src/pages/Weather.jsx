import { useEffect, useState } from "react";
import { useAuth } from "../hooks/useAuth.js";
import { getWeatherForFarm } from "../api/weatherApi.js";

function Weather() {
  const { currentFarm } = useAuth();

  const [weather, setWeather] = useState(null);
  const [loading, setLoading] = useState(true);
  const [unavailable, setUnavailable] = useState(false);
  const [error, setError] = useState("");

  useEffect(() => {
    if (!currentFarm) return;

    async function fetchWeather() {
      setLoading(true);
      setUnavailable(false);
      setError("");

      try {
        const data = await getWeatherForFarm(currentFarm.id);
        setWeather(data);
      } catch (err) {
        // 500 means WeatherApiClient is not yet implemented
        // Show a friendly unavailable state — NOT hardcoded weather data
        if (err.status === 500 || err.status === 0) {
          setUnavailable(true);
        } else {
          setError(err.message ?? "Failed to load weather.");
        }
      } finally {
        setLoading(false);
      }
    }

    fetchWeather();
  }, [currentFarm]);

  if (!currentFarm && !loading) {
    return (
      <div>
        <header className="page-header">
          <div>
            <p className="page-label">FARM WEATHER</p>
            <h1>Weather Intelligence</h1>
          </div>
        </header>
        <p className="page-error">
          ⚠️ No farm linked to your account. Please update your Farm Profile first.
        </p>
      </div>
    );
  }

  if (loading) {
    return (
      <div>
        <header className="page-header">
          <div>
            <p className="page-label">FARM WEATHER</p>
            <h1>Weather Intelligence</h1>
            <p>
              View current conditions and plan farm activities
              using the forecast.
            </p>
          </div>
        </header>
        <div className="auth-loading" style={{ minHeight: "50vh" }}>
          <div className="auth-loading-spinner" />
          <p>Loading weather data…</p>
        </div>
      </div>
    );
  }

  // Weather service not yet implemented in backend
  if (unavailable || !weather) {
    return (
      <div>
        <header className="page-header">
          <div>
            <p className="page-label">FARM WEATHER</p>
            <h1>Weather Intelligence</h1>
            <p>
              View current conditions and plan farm activities
              using the forecast.
            </p>
          </div>
        </header>

        <section className="panel">
          <div className="service-unavailable">
            <span className="service-icon">🌦️</span>
            <h2>Weather service is being configured</h2>
            <p>
              Farm weather data is not yet available. The weather
              intelligence service will be active soon. No action
              is required on your part.
            </p>
            {currentFarm?.location && (
              <p style={{ fontSize: "13px" }}>
                Your farm location: <strong>{currentFarm.location}</strong>
              </p>
            )}
          </div>
        </section>

        {error && <p className="page-error">{error}</p>}
      </div>
    );
  }

  // Weather data is available — render live data
  return (
    <div>
      <header className="page-header">
        <div>
          <p className="page-label">FARM WEATHER</p>
          <h1>Weather Intelligence</h1>
          <p>
            View current conditions and plan farm activities
            using the forecast.
          </p>
        </div>
      </header>

      <section className="current-weather-card">
        <div>
          <p>{weather.location ?? currentFarm?.location ?? "—"}</p>

          <h2>
            {weather.temperature != null
              ? `${weather.temperature}°C`
              : "—"}
          </h2>

          <h3>{weather.condition ?? "—"}</h3>

          {weather.feelsLike != null && (
            <span>Feels like {weather.feelsLike}°C</span>
          )}
        </div>

        <div className="current-weather-icon">
          {weather.icon ?? "🌡️"}
        </div>

        <div className="current-weather-details">
          <div>
            <span>Humidity</span>
            <strong>
              {weather.humidity != null ? `${weather.humidity}%` : "—"}
            </strong>
          </div>

          <div>
            <span>Wind speed</span>
            <strong>
              {weather.windSpeed != null
                ? `${weather.windSpeed} km/h`
                : "—"}
            </strong>
          </div>

          <div>
            <span>Rain probability</span>
            <strong>
              {weather.rainProbability != null
                ? `${weather.rainProbability}%`
                : "—"}
            </strong>
          </div>

          {weather.uvIndex != null && (
            <div>
              <span>UV index</span>
              <strong>
                {weather.uvIndex} {weather.uvCategory ?? ""}
              </strong>
            </div>
          )}
        </div>
      </section>

      {Array.isArray(weather.forecast) && weather.forecast.length > 0 && (
        <section className="panel">
          <div className="panel-heading">
            <div>
              <h2>Five-day forecast</h2>
              <p>Expected weather conditions for your farm</p>
            </div>
          </div>

          <div className="forecast-grid">
            {weather.forecast.map((item, index) => (
              <article
                className="forecast-card"
                key={item.day ?? index}
              >
                <strong>{item.day ?? "—"}</strong>
                <span className="forecast-icon">
                  {item.icon ?? "🌡️"}
                </span>
                <h3>
                  {item.temperature != null
                    ? `${item.temperature}°C`
                    : "—"}
                </h3>
                <p>{item.condition ?? "—"}</p>
                {item.rainProbability != null && (
                  <span>Rain: {item.rainProbability}%</span>
                )}
              </article>
            ))}
          </div>
        </section>
      )}

      {weather.alertMessage && (
        <section className="weather-alert">
          <span>🌧️</span>
          <div>
            <strong>Farm weather alert</strong>
            <p>{weather.alertMessage}</p>
          </div>
        </section>
      )}
    </div>
  );
}

export default Weather;