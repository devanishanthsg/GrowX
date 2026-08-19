function WeatherStatusBadge({ metadata }) {
  const status = metadata?.status ?? "UNAVAILABLE";
  const age = metadata?.dataAgeMinutes;

  const label =
    status === "LIVE"
      ? "Live"
      : status === "CACHED"
        ? "Cached"
        : status === "STALE"
          ? "Stale"
          : "Unavailable";

  const ageText =
    age == null
      ? ""
      : age < 1
        ? "Updated just now"
        : `Updated ${age} min ago`;

  return (
    <div className={`weather-v2-status weather-v2-status-${status.toLowerCase()}`}>
      <span>{label}</span>
      {ageText && <small>{ageText}</small>}
    </div>
  );
}

export default WeatherStatusBadge;
