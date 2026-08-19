function HourlyForecast({ hourly = [] }) {
  if (!hourly.length) return null;

  return (
    <section className="panel weather-v2-hourly-panel">
      <div className="panel-heading">
        <div>
          <h2>Next 24 Hours</h2>
          <p>Rain, wind and temperature timing for farm operations.</p>
        </div>
      </div>

      <div className="weather-v2-hourly-scroll">
        {hourly.map((item, index) => {
          const date = item.timestamp ? new Date(item.timestamp) : null;
          return (
            <article className="weather-v2-hour-card" key={item.timestamp ?? index}>
              <strong>
                {date
                  ? date.toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" })
                  : "—"}
              </strong>
              <span className="weather-v2-hour-icon">{item.icon ?? "🌡️"}</span>
              <b>{item.temperature != null ? `${Number(item.temperature).toFixed(1)}°` : "—"}</b>
              <small>{item.precipitationProbability != null ? `Rain ${Math.round(item.precipitationProbability)}%` : "Rain —"}</small>
              <small>{item.precipitation != null ? `${Number(item.precipitation).toFixed(1)} mm` : "— mm"}</small>
              <small>{item.windSpeed != null ? `Wind ${Number(item.windSpeed).toFixed(0)} km/h` : "Wind —"}</small>
            </article>
          );
        })}
      </div>
    </section>
  );
}

export default HourlyForecast;
