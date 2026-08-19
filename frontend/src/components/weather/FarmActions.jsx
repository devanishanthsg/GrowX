const DISPLAY_ORDER = [
  "IRRIGATION",
  "SPRAYING",
  "DISEASE_RISK",
  "HEAT_STRESS",
  "RAIN_ALERT",
];

const ICONS = {
  IRRIGATION: "💧",
  SPRAYING: "🌿",
  DISEASE_RISK: "🛡️",
  HEAT_STRESS: "🌡️",
  RAIN_ALERT: "🌧️",
};

function formatWindow(advisory) {
  if (!advisory?.windowStart) return null;

  const start = new Date(advisory.windowStart);
  const end = advisory.windowEnd ? new Date(advisory.windowEnd) : null;

  const options = {
    weekday: "short",
    hour: "2-digit",
    minute: "2-digit",
  };

  if (!end) return start.toLocaleString([], options);

  return `${start.toLocaleString([], options)} – ${end.toLocaleTimeString([], {
    hour: "2-digit",
    minute: "2-digit",
  })}`;
}

function FarmActions({ advisories = [], overallRisk, overallRiskReason }) {
  const ordered = [...advisories].sort(
    (a, b) => DISPLAY_ORDER.indexOf(a.type) - DISPLAY_ORDER.indexOf(b.type),
  );

  return (
    <section className="panel weather-v2-actions-panel">
      <div className="panel-heading weather-v2-section-heading">
        <div>
          <p className="weather-v2-kicker">FARM ACTIONS</p>
          <h2>Weather decisions at a glance</h2>
          <p>Decision-support guidance based on the current forecast.</p>
        </div>

        <div className={`weather-v2-overall-risk risk-${String(overallRisk ?? "LOW").toLowerCase()}`}>
          <span>Farm weather risk</span>
          <strong>{overallRisk ?? "LOW"}</strong>
        </div>
      </div>

      {overallRiskReason && (
        <p className="weather-v2-risk-reason">{overallRiskReason}</p>
      )}

      <div className="weather-v2-action-grid">
        {ordered.map((item) => {
          const window = formatWindow(item);

          return (
            <article
              className={`weather-v2-action-card severity-${String(item.severity ?? "LOW").toLowerCase()}`}
              key={item.type}
            >
              <div className="weather-v2-action-head">
                <span className="weather-v2-action-icon">{ICONS[item.type] ?? "🌾"}</span>
                <div>
                  <small>{item.type?.replaceAll("_", " ")}</small>
                  <strong>{item.status?.replaceAll("_", " ")}</strong>
                </div>
              </div>

              <h3>{item.title}</h3>
              <p>{item.explanation}</p>

              {item.recommendedAction && (
                <div className="weather-v2-action-recommendation">
                  <span>Consider</span>
                  <p>{item.recommendedAction}</p>
                </div>
              )}

              {window && (
                <div className="weather-v2-window">
                  <span>Possible weather window</span>
                  <strong>{window}</strong>
                </div>
              )}
            </article>
          );
        })}
      </div>
    </section>
  );
}

export default FarmActions;
