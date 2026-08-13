/**
 * formatters.js
 * Display formatting helpers used across pages.
 */

/**
 * Format a ISO date string or LocalDateTime for display.
 * Example: "2026-07-31T10:30:00" → "Jul 31, 2026, 10:30 AM"
 */
export function formatDateTime(dateString) {
  if (!dateString) return "—";
  try {
    return new Date(dateString).toLocaleString("en-IN", {
      year: "numeric",
      month: "short",
      day: "numeric",
      hour: "2-digit",
      minute: "2-digit",
    });
  } catch {
    return dateString;
  }
}

/**
 * Format a ISO date string for report table display.
 * Example: "2026-07-31T10:30:00" → "Jul 31, 2026"
 */
export function formatDate(dateString) {
  if (!dateString) return "—";
  try {
    return new Date(dateString).toLocaleDateString("en-IN", {
      year: "numeric",
      month: "short",
      day: "numeric",
    });
  } catch {
    return dateString;
  }
}

/**
 * Format a numeric sensor value with a unit.
 * Returns "—" when value is null or undefined.
 * Example: formatSensorValue(68.4, "%") → "68.4%"
 */
export function formatSensorValue(value, unit = "") {
  if (value === null || value === undefined) return "—";
  return `${Number(value).toFixed(1)}${unit}`;
}

/**
 * Format a percentage value (0–100) for display.
 * Returns "—" when value is null.
 * Example: formatPercent(94.2) → "94.2%"
 */
export function formatPercent(value) {
  return formatSensorValue(value, "%");
}

/**
 * Extract initials from a display name for avatar.
 * Example: "Devanishanth" → "D" | "John Doe" → "JD"
 */
export function getInitials(name) {
  if (!name) return "?";
  return name
    .trim()
    .split(/\s+/)
    .map((word) => word[0].toUpperCase())
    .slice(0, 2)
    .join("");
}

/**
 * Map RecommendationStatus enum to a human-readable label.
 */
export function formatRecommendationStatus(status) {
  const map = {
    PENDING: "Pending",
    COMPLETED: "Completed",
    FAILED: "Failed",
  };
  return map[status] ?? status ?? "—";
}

/**
 * Map ReportType enum to a human-readable label.
 */
export function formatReportType(type) {
  const map = {
    CROP_RECOMMENDATION: "Crop Recommendation",
    DISEASE_DETECTION: "Disease Detection",
    FARM_SUMMARY: "Farm Summary",
    WEATHER: "Weather",
  };
  return map[type] ?? type ?? "—";
}
