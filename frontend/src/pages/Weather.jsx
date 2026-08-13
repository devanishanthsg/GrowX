const forecast = [
  {
    day: "Saturday",
    icon: "☀️",
    condition: "Sunny",
    temperature: "30°C",
    rain: "10%",
  },
  {
    day: "Sunday",
    icon: "🌤️",
    condition: "Partly cloudy",
    temperature: "29°C",
    rain: "20%",
  },
  {
    day: "Monday",
    icon: "🌧️",
    condition: "Rain",
    temperature: "27°C",
    rain: "75%",
  },
  {
    day: "Tuesday",
    icon: "🌦️",
    condition: "Light rain",
    temperature: "28°C",
    rain: "55%",
  },
  {
    day: "Wednesday",
    icon: "☀️",
    condition: "Sunny",
    temperature: "31°C",
    rain: "10%",
  },
];

function Weather() {
  return (
    <div>
      <header className="page-header">
        <div>
          <p className="page-label">
            FARM WEATHER
          </p>
          <h1>Weather Intelligence</h1>
          <p>
            View current conditions and plan farm
            activities using the forecast.
          </p>
        </div>

        <button className="outline-button">
          Change Location
        </button>
      </header>

      <section className="current-weather-card">
        <div>
          <p>Coimbatore, Tamil Nadu</p>

          <h2>29°C</h2>

          <h3>Sunny</h3>

          <span>
            Feels like 31°C
          </span>
        </div>

        <div className="current-weather-icon">
          ☀️
        </div>

        <div className="current-weather-details">
          <div>
            <span>Humidity</span>
            <strong>74%</strong>
          </div>

          <div>
            <span>Wind speed</span>
            <strong>11 km/h</strong>
          </div>

          <div>
            <span>Rain probability</span>
            <strong>20%</strong>
          </div>

          <div>
            <span>UV index</span>
            <strong>6 Moderate</strong>
          </div>
        </div>
      </section>

      <section className="panel">
        <div className="panel-heading">
          <div>
            <h2>Five-day forecast</h2>
            <p>
              Expected weather conditions for your farm
            </p>
          </div>
        </div>

        <div className="forecast-grid">
          {forecast.map((item) => (
            <article
              className="forecast-card"
              key={item.day}
            >
              <strong>{item.day}</strong>
              <span className="forecast-icon">
                {item.icon}
              </span>
              <h3>{item.temperature}</h3>
              <p>{item.condition}</p>
              <span>Rain: {item.rain}</span>
            </article>
          ))}
        </div>
      </section>

      <section className="weather-alert">
        <span>🌧️</span>

        <div>
          <strong>Farm weather alert</strong>
          <p>
            Rain is likely on Monday. Avoid applying
            fertiliser immediately before rainfall.
          </p>
        </div>
      </section>
    </div>
  );
}

export default Weather;