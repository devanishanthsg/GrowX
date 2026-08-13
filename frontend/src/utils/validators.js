/**
 * validators.js
 * Reusable frontend form validation helpers.
 * Backend validation is still authoritative — these improve UX only.
 */

/** True if the string is a syntactically valid email address */
export function isValidEmail(value) {
  return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(String(value).trim());
}

/** True if value is a finite number within [min, max] (inclusive) */
export function isInRange(value, min, max) {
  const n = Number(value);
  return Number.isFinite(n) && n >= min && n <= max;
}

/** True if value is a finite number >= 0 */
export function isNonNegativeNumber(value) {
  const n = Number(value);
  return Number.isFinite(n) && n >= 0;
}

/** True if the file is an accepted image type for disease detection */
export function isAcceptedImage(file) {
  if (!file) return false;
  const accepted = ["image/jpeg", "image/jpg", "image/png"];
  return accepted.includes(file.type);
}

/** True if file size is within limit (default 10 MB) */
export function isFileSizeOk(file, maxMb = 10) {
  if (!file) return false;
  return file.size <= maxMb * 1024 * 1024;
}

/**
 * Validate registration form fields.
 * Returns an errors object — empty means valid.
 */
export function validateRegister({ name, email, location, password }) {
  const errors = {};
  if (!name || !name.trim()) errors.name = "Name is required.";
  if (!email || !isValidEmail(email)) errors.email = "Please enter a valid email address.";
  if (!location || !location.trim()) errors.location = "Farm location is required.";
  if (!password || password.length < 6)
    errors.password = "Password must be at least 6 characters.";
  return errors;
}

/**
 * Validate login form fields.
 * Returns an errors object — empty means valid.
 */
export function validateLogin({ email, password }) {
  const errors = {};
  if (!email || !isValidEmail(email)) errors.email = "Please enter a valid email address.";
  if (!password) errors.password = "Password is required.";
  return errors;
}

/**
 * Validate crop recommendation form fields.
 * Returns an errors object — empty means valid.
 */
export function validateCropForm(form) {
  const errors = {};
  const required = [
    "nitrogen",
    "phosphorus",
    "potassium",
    "temperature",
    "humidity",
    "ph",
    "rainfall",
  ];

  required.forEach((field) => {
    if (form[field] === "" || form[field] === undefined) {
      errors[field] = "This field is required.";
    }
  });

  if (!errors.nitrogen && !isNonNegativeNumber(form.nitrogen))
    errors.nitrogen = "Must be a positive number.";
  if (!errors.phosphorus && !isNonNegativeNumber(form.phosphorus))
    errors.phosphorus = "Must be a positive number.";
  if (!errors.potassium && !isNonNegativeNumber(form.potassium))
    errors.potassium = "Must be a positive number.";
  if (!errors.temperature && !isInRange(form.temperature, -20, 60))
    errors.temperature = "Must be between -20 and 60°C.";
  if (!errors.humidity && !isInRange(form.humidity, 0, 100))
    errors.humidity = "Must be between 0 and 100.";
  if (!errors.ph && !isInRange(form.ph, 0, 14))
    errors.ph = "Must be between 0 and 14.";
  if (!errors.rainfall && !isNonNegativeNumber(form.rainfall))
    errors.rainfall = "Must be a positive number.";

  return errors;
}
