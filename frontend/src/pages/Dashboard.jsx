import StatCard from "../components/StatCard";

const recommendations = [
  {
    title: "Irrigation",
    message:
      "Soil moisture is currently suitable. Check again tomorrow evening.",
    icon: "💧",
  },
  {
    title: "Weather",
    message:
      "Moderate rainfall may occur within the next two days.",
    icon: "🌦️",
  },
  {
    title: "Crop Health",
    message:
      "No major disease risk detected from the latest farm record.",
    icon: "🌿",
  },
];

const activities = [
  {
    action: "Crop recommendation generated",
    time: "Today, 10:30 AM",
  },
  {
    action: "Farm information updated",
    time: "Yesterday, 4:15 PM",
  },
  {
    action: "Disease image analysed",
    time: "July 29, 2026",
  },
];

function Dashboard() {
  return (
    <div>
      <header className="page-header">
        <div>
          <p className="page-label">OVERVIEW</p>
          <h1>Good evening, Farmer 👋</h1>
          <p>
            Here is the latest information about your
            farm.
          </p>
        </div>

        <div className="header-profile">
          <div>
            <strong>Green Valley Farm</strong>
            <span>Tamil Nadu</span>
          </div>

          <div className="profile-avatar">GF</div>
        </div>
      </header>

      <section className="stat-grid">
        <StatCard
          icon="💧"
          title="Soil Moisture"
          value="68%"
          description="Optimal moisture level"
          status="success"
        />

        <StatCard
          icon="🌡️"
          title="Temperature"
          value="29°C"
          description="Current farm temperature"
          status="warning"
        />

        <StatCard
          icon="💨"
          title="Humidity"
          value="74%"
          description="Moderate humidity level"
          status="normal"
        />

        <StatCard
          icon="🌿"
          title="Crop Health"
          value="92%"
          description="Healthy crop condition"
          status="success"
        />
      </section>

      <section className="dashboard-grid">
        <article className="panel large-panel">
          <div className="panel-heading">
            <div>
              <h2>Farm condition overview</h2>
              <p>Values recorded during this week</p>
            </div>

            <select defaultValue="week">
              <option value="week">This week</option>
              <option value="month">This month</option>
            </select>
          </div>

          <div className="mock-chart">
            <div
              className="chart-bar"
              style={{ height: "55%" }}
            >
              <span>Mon</span>
            </div>

            <div
              className="chart-bar"
              style={{ height: "70%" }}
            >
              <span>Tue</span>
            </div>

            <div
              className="chart-bar"
              style={{ height: "64%" }}
            >
              <span>Wed</span>
            </div>

            <div
              className="chart-bar"
              style={{ height: "80%" }}
            >
              <span>Thu</span>
            </div>

            <div
              className="chart-bar"
              style={{ height: "68%" }}
            >
              <span>Fri</span>
            </div>

            <div
              className="chart-bar"
              style={{ height: "75%" }}
            >
              <span>Sat</span>
            </div>

            <div
              className="chart-bar"
              style={{ height: "60%" }}
            >
              <span>Sun</span>
            </div>
          </div>
        </article>

        <article className="panel weather-summary">
          <div className="panel-heading">
            <div>
              <h2>Weather</h2>
              <p>Coimbatore, Tamil Nadu</p>
            </div>

            <span className="weather-big-icon">
              ☀️
            </span>
          </div>

          <div className="weather-temperature">
            29°C
          </div>

          <p>Sunny with moderate humidity</p>

          <div className="weather-details">
            <div>
              <span>Humidity</span>
              <strong>74%</strong>
            </div>

            <div>
              <span>Wind</span>
              <strong>11 km/h</strong>
            </div>

            <div>
              <span>Rain</span>
              <strong>20%</strong>
            </div>
          </div>
        </article>
      </section>

      <section className="dashboard-grid">
        <article className="panel">
          <div className="panel-heading">
            <div>
              <h2>AI recommendations</h2>
              <p>Suggestions based on farm data</p>
            </div>
          </div>

          <div className="recommendation-list">
            {recommendations.map((item) => (
              <div
                className="recommendation-item"
                key={item.title}
              >
                <span>{item.icon}</span>

                <div>
                  <strong>{item.title}</strong>
                  <p>{item.message}</p>
                </div>
              </div>
            ))}
          </div>
        </article>

        <article className="panel">
          <div className="panel-heading">
            <div>
              <h2>Recent activity</h2>
              <p>Latest actions in your account</p>
            </div>
          </div>

          <div className="activity-list">
            {activities.map((activity) => (
              <div
                className="activity-item"
                key={activity.action}
              >
                <span className="activity-dot" />

                <div>
                  <strong>{activity.action}</strong>
                  <p>{activity.time}</p>
                </div>
              </div>
            ))}
          </div>
        </article>
      </section>
    </div>
  );
}

export default Dashboard;