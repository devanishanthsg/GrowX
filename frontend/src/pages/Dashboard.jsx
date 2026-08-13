import { useEffect, useState } from "react";
import StatCard from "../components/StatCard";
import { getDashboard } from "../api/dashboardApi.js";
import { useAuth } from "../hooks/useAuth.js";
import { formatSensorValue, getInitials } from "../utils/formatters.js";

function Dashboard() {
  const { user, currentFarm } = useAuth();

  const [dashboard, setDashboard] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    async function fetchDashboard() {
      setLoading(true);
      setError("");
      try {
        const data = await getDashboard();
        setDashboard(data);
      } catch (err) {
        setError(err.message ?? "Failed to load dashboard.");
      } finally {
        setLoading(false);
      }
    }
    fetchDashboard();
  }, []);

  const farmerName = dashboard?.farmerName ?? user?.name ?? "Farmer";
  const farmName = dashboard?.farmName ?? currentFarm?.farmName ?? "Your Farm";
  const farmLocation =
    dashboard?.location ?? currentFarm?.location ?? "—";

  // Stat values — show "—" when backend returns null (sensor/IoT not integrated)
  const soilMoisture = formatSensorValue(dashboard?.soilMoisture, "%");
  const temperature = formatSensorValue(dashboard?.temperature, "°C");
  const humidity = formatSensorValue(dashboard?.humidity, "%");
  const cropHealth = formatSensorValue(dashboard?.cropHealth, "%");

  // Determine greeting based on local time
  const hour = new Date().getHours();
  const greeting =
    hour < 12 ? "Good morning" : hour < 17 ? "Good afternoon" : "Good evening";

  if (loading) {
    return (
      <div className="auth-loading" style={{ minHeight: "60vh" }}>
        <div className="auth-loading-spinner" />
        <p>Loading dashboard…</p>
      </div>
    );
  }

  return (
    <div>
      <header className="page-header">
        <div>
          <p className="page-label">OVERVIEW</p>
          <h1>
            {greeting}, {farmerName} 👋
          </h1>
          <p>Here is the latest information about your farm.</p>
        </div>

        <div className="header-profile">
          <div>
            <strong>{farmName}</strong>
            <span>{farmLocation}</span>
          </div>
          <div className="profile-avatar">
            {getInitials(farmerName)}
          </div>
        </div>
      </header>

      {error && (
        <p className="page-error">{error}</p>
      )}

      <section className="stat-grid">
        <StatCard
          icon="💧"
          title="Soil Moisture"
          value={soilMoisture}
          description={
            dashboard?.soilMoisture != null
              ? "Current soil moisture"
              : "Awaiting sensor data"
          }
          status={dashboard?.soilMoisture != null ? "success" : "normal"}
        />

        <StatCard
          icon="🌡️"
          title="Temperature"
          value={temperature}
          description={
            dashboard?.temperature != null
              ? "Current farm temperature"
              : "Awaiting weather data"
          }
          status={dashboard?.temperature != null ? "warning" : "normal"}
        />

        <StatCard
          icon="💨"
          title="Humidity"
          value={humidity}
          description={
            dashboard?.humidity != null
              ? "Current humidity level"
              : "Awaiting weather data"
          }
          status="normal"
        />

        <StatCard
          icon="🌿"
          title="Crop Health"
          value={cropHealth}
          description={
            dashboard?.cropHealth != null
              ? "Current crop health score"
              : "Awaiting AI analysis"
          }
          status={dashboard?.cropHealth != null ? "success" : "normal"}
        />
      </section>

      <section className="dashboard-grid">
        {/* Weather summary panel */}
        <article className="panel weather-summary">
          <div className="panel-heading">
            <div>
              <h2>Weather</h2>
              <p>
                {dashboard?.weatherSummary?.location ??
                  farmLocation}
              </p>
            </div>
            <span className="weather-big-icon">
              {dashboard?.weatherSummary?.icon ?? "🌡️"}
            </span>
          </div>

          {dashboard?.weatherSummary ? (
            <>
              <div className="weather-temperature">
                {dashboard.weatherSummary.temperature != null
                  ? `${dashboard.weatherSummary.temperature}°C`
                  : "—"}
              </div>
              <p>{dashboard.weatherSummary.condition ?? "—"}</p>
              <div className="weather-details">
                <div>
                  <span>Humidity</span>
                  <strong>
                    {dashboard.weatherSummary.humidity != null
                      ? `${dashboard.weatherSummary.humidity}%`
                      : "—"}
                  </strong>
                </div>
                <div>
                  <span>Wind</span>
                  <strong>
                    {dashboard.weatherSummary.windSpeed != null
                      ? `${dashboard.weatherSummary.windSpeed} km/h`
                      : "—"}
                  </strong>
                </div>
                <div>
                  <span>Rain</span>
                  <strong>
                    {dashboard.weatherSummary.rainProbability != null
                      ? `${dashboard.weatherSummary.rainProbability}%`
                      : "—"}
                  </strong>
                </div>
              </div>
            </>
          ) : (
            <div className="service-unavailable" style={{ padding: "24px 0" }}>
              <span>🌦️</span>
              <p>Weather service is being configured.</p>
            </div>
          )}
        </article>

        {/* Recommendations panel */}
        <article className="panel">
          <div className="panel-heading">
            <div>
              <h2>AI recommendations</h2>
              <p>Suggestions based on farm data</p>
            </div>
          </div>

          <div className="recommendation-list">
            {Array.isArray(dashboard?.recommendations) &&
            dashboard.recommendations.length > 0 ? (
              dashboard.recommendations.map((item, index) => (
                <div
                  className="recommendation-item"
                  key={item.title ?? index}
                >
                  <span>{item.icon ?? "💡"}</span>
                  <div>
                    <strong>{item.title}</strong>
                    <p>{item.message}</p>
                  </div>
                </div>
              ))
            ) : (
              <div className="empty-state">
                <p>No recommendations available yet.</p>
              </div>
            )}
          </div>
        </article>
      </section>

      <section className="dashboard-grid">
        {/* Recent activity panel */}
        <article className="panel">
          <div className="panel-heading">
            <div>
              <h2>Recent activity</h2>
              <p>Latest actions in your account</p>
            </div>
          </div>

          <div className="activity-list">
            {Array.isArray(dashboard?.recentActivity) &&
            dashboard.recentActivity.length > 0 ? (
              dashboard.recentActivity.map((activity, index) => (
                <div
                  className="activity-item"
                  key={activity.action ?? index}
                >
                  <span className="activity-dot" />
                  <div>
                    <strong>{activity.action}</strong>
                    <p>{activity.time}</p>
                  </div>
                </div>
              ))
            ) : (
              <div className="empty-state">
                <p>No recent activity to show.</p>
              </div>
            )}
          </div>
        </article>
      </section>
    </div>
  );
}

export default Dashboard;