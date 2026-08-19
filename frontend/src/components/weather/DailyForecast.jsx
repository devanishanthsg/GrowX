function riskClass(risk) {
  return `risk-${String(risk ?? "LOW").toLowerCase()}`;
}

function DailyForecast({ daily = [] }) {
  if (!daily.length) return null;

  return (
    <section className="panel weather-v2-daily-panel">
      <div className="panel-heading">
        <div>
          <h2>7-Day Farm Forecast</h2>
          <p>Rain probability and rainfall amount are shown separately.</p>
        </div>
      </div>

      <div className="weather-v2-daily-list">
        {daily.map((day, index) => (
          <article className="weather-v2-day-row" key={day.date ?? index}>
            <div className="weather-v2-day-name">
              <strong>{day.day ?? "—"}</strong>
              <small>{day.date ?? ""}</small>
            </div>

            <span className="weather-v2-day-icon">{day.icon ?? "🌡️"}</span>

            <div className="weather-v2-day-condition">
              <strong>{day.conditionDescription ?? "—"}</strong>
              <small>
                {day.minTemperature != null ? Math.round(day.minTemperature) : "—"}° / {day.maxTemperature != null ? Math.round(day.maxTemperature) : "—"}°C
              </small>
            </div>

            <div className="weather-v2-day-humidity">
              <span>Humidity</span>
              <strong>{day.averageHumidity != null ? `${Math.round(day.averageHumidity)}%` : "—"}</strong>
            </div>

            <div className="weather-v2-day-rain-chance">
              <span>Rain chance</span>
              <strong>{day.rainProbability != null ? `${Math.round(day.rainProbability)}%` : "—"}</strong>
            </div>

            <div className="weather-v2-day-rain-amount">
              <span>Rain amount</span>
              <strong>{day.rainfallMm != null ? `${Number(day.rainfallMm).toFixed(1)} mm` : "—"}</strong>
            </div>

            <div className="weather-v2-day-wind">
              <span>Max wind</span>
              <strong>{day.maxWindSpeed != null ? `${Math.round(day.maxWindSpeed)} km/h` : "—"}</strong>
            </div>

            <div className={`weather-v2-day-risk ${riskClass(day.farmRisk)}`}>
              <span>Farm risk</span>
              <strong>{day.farmRisk ?? "LOW"}</strong>
            </div>
          </article>
        ))}
      </div>
    </section>
  );
}

export default DailyForecast;
