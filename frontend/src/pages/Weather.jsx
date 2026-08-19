import { useCallback, useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../hooks/useAuth.js";
import {
  getWeatherForFarm,
  refreshWeatherForFarm,
} from "../api/weatherApi.js";
import WeatherStatusBadge from "../components/weather/WeatherStatusBadge.jsx";
import CurrentWeatherCard from "../components/weather/CurrentWeatherCard.jsx";
import FarmActions from "../components/weather/FarmActions.jsx";
import HourlyForecast from "../components/weather/HourlyForecast.jsx";
import DailyForecast from "../components/weather/DailyForecast.jsx";
import AgriculturalIndicators from "../components/weather/AgriculturalIndicators.jsx";
import "../styles/weatherV2.css";

function Weather() {
  const { currentFarm } = useAuth();
  const navigate = useNavigate();

  const [weather, setWeather] = useState(null);
  const [loading, setLoading] = useState(false);
  const [refreshing, setRefreshing] = useState(false);
  const [error, setError] = useState("");

  const loadWeather = useCallback(async () => {
    if (!currentFarm) {
      setWeather(null);
      setLoading(false);
      return;
    }

    setLoading(true);
    setError("");

    try {
      const data = await getWeatherForFarm(currentFarm.id);
      setWeather(data);
    } catch (err) {
      setError(err.message ?? "Failed to load farm weather.");
      setWeather(null);
    } finally {
      setLoading(false);
    }
  }, [currentFarm]);

  useEffect(() => {
    loadWeather();
  }, [loadWeather]);

  async function handleRefresh() {
    if (!currentFarm || refreshing) return;

    setRefreshing(true);
    setError("");

    try {
      const data = await refreshWeatherForFarm(currentFarm.id);
      setWeather(data);
    } catch (err) {
      setError(err.message ?? "Weather refresh failed.");
    } finally {
      setRefreshing(false);
    }
  }

  if (!currentFarm) {
    return (
      <div className="weather-intelligence-page">
        <header className="page-header">
          <div>
            <p className="page-label">FARM WEATHER</p>
            <h1>Weather Intelligence</h1>
            <p>Local weather and farm-operation guidance for your selected farm.</p>
          </div>
        </header>

        <section className="panel weather-v2-empty-state">
          <span>🌦️</span>
          <h2>No farm selected</h2>
          <p>Add or select a farm before opening local weather intelligence.</p>
          <button className="primary-button" onClick={() => navigate("/profile")}>
            Open Farm Profile
          </button>
        </section>
      </div>
    );
  }

  if (loading) {
    return (
      <div className="weather-intelligence-page">
        <header className="page-header">
          <div>
            <p className="page-label">FARM WEATHER</p>
            <h1>Weather Intelligence</h1>
            <p>Loading the latest farm forecast and agricultural indicators.</p>
          </div>
        </header>
        <div className="auth-loading weather-v2-loading">
          <div className="auth-loading-spinner" />
          <p>Loading weather data…</p>
        </div>
      </div>
    );
  }

  const metadata = weather?.metadata;
  const unavailable = metadata?.status === "UNAVAILABLE" || !weather?.current;
  const missingStoredCoordinates =
    currentFarm.latitude == null || currentFarm.longitude == null;

  return (
    <div className="weather-intelligence-page">
      <header className="page-header weather-v2-header">
        <div>
          <p className="page-label">FARM WEATHER</p>
          <h1>Weather Intelligence</h1>
          <p>
            {currentFarm.farmName} • {currentFarm.location}
          </p>
        </div>

        <div className="weather-v2-header-actions">
          <WeatherStatusBadge metadata={metadata} />
          <button
            type="button"
            className="outline-button"
            onClick={handleRefresh}
            disabled={refreshing}
          >
            {refreshing ? "Refreshing…" : "Refresh Weather"}
          </button>
        </div>
      </header>

      {error && <p className="page-error">{error}</p>}

      {missingStoredCoordinates && (
        <section className="weather-v2-coordinate-warning">
          <div>
            <strong>Using location-name weather fallback</strong>
            <p>
              Exact farm coordinates are not saved. GrowX can estimate the location,
              but latitude and longitude provide more precise local weather lookup.
            </p>
          </div>
          <button className="outline-button" onClick={() => navigate("/profile")}>
            Add Coordinates
          </button>
        </section>
      )}

      {Array.isArray(weather?.warnings) && weather.warnings.length > 0 && (
        <div className="weather-v2-warnings">
          {weather.warnings.map((warning, index) => (
            <p key={`${warning}-${index}`}>⚠️ {warning}</p>
          ))}
        </div>
      )}

      {unavailable ? (
        <section className="panel weather-v2-empty-state">
          <span>🌦️</span>
          <h2>Weather data unavailable</h2>
          <p>
            GrowX could not obtain live or cached weather for this farm. No example
            or hardcoded weather values are shown.
          </p>
          <button className="outline-button" onClick={handleRefresh} disabled={refreshing}>
            {refreshing ? "Trying again…" : "Try Again"}
          </button>
        </section>
      ) : (
        <>
          <CurrentWeatherCard weather={weather} />

          <FarmActions
            advisories={weather.advisories ?? []}
            overallRisk={weather.overallRisk}
            overallRiskReason={weather.overallRiskReason}
          />

          <HourlyForecast hourly={weather.hourly ?? []} />
          <DailyForecast daily={weather.daily ?? []} />
          <AgriculturalIndicators agriculture={weather.agriculture} />
        </>
      )}

      <footer className="weather-v2-source-footer">
        <p>
          Weather data: <strong>{metadata?.provider ?? "Unavailable"}</strong>
        </p>
        {metadata?.fetchedAt && (
          <p>
            Provider fetch: {new Date(metadata.fetchedAt).toLocaleString()}
          </p>
        )}
        <p>Forecast values are model-based estimates and may change.</p>
      </footer>
    </div>
  );
}

export default Weather;
