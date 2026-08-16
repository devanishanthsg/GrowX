/**
 * validators.js
 * Reusable frontend form validation helpers.
 *
 * Backend and AI validation are still authoritative.
 * These checks improve UX by rejecting invalid values
 * before the request is sent.
 */

/**
 * True if the string is a syntactically valid email.
 */
export function isValidEmail(value) {
  return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(
    String(value).trim(),
  );
}

/**
 * True if value is a finite number
 * inside the inclusive range.
 */
export function isInRange(value, min, max) {
  const number = Number(value);

  return (
    Number.isFinite(number) &&
    number >= min &&
    number <= max
  );
}

/**
 * True if value is a finite non-negative number.
 */
export function isNonNegativeNumber(value) {
  const number = Number(value);

  return (
    Number.isFinite(number) &&
    number >= 0
  );
}

/**
 * Accepted image formats for disease detection.
 */
export function isAcceptedImage(file) {
  if (!file) return false;

  const accepted = [
    "image/jpeg",
    "image/jpg",
    "image/png",
  ];

  return accepted.includes(file.type);
}

/**
 * Check image/file size.
 *
 * Default limit: 10 MB
 */
export function isFileSizeOk(
  file,
  maxMb = 10,
) {
  if (!file) return false;

  return (
    file.size <=
    maxMb * 1024 * 1024
  );
}

/**
 * Registration form validation.
 */
export function validateRegister({
  name,
  email,
  location,
  password,
}) {
  const errors = {};

  if (!name || !name.trim()) {
    errors.name =
      "Name is required.";
  }

  if (
    !email ||
    !isValidEmail(email)
  ) {
    errors.email =
      "Please enter a valid email address.";
  }

  if (
    !location ||
    !location.trim()
  ) {
    errors.location =
      "Farm location is required.";
  }

  if (
    !password ||
    password.length < 6
  ) {
    errors.password =
      "Password must be at least 6 characters.";
  }

  return errors;
}

/**
 * Login form validation.
 */
export function validateLogin({
  email,
  password,
}) {
  const errors = {};

  if (
    !email ||
    !isValidEmail(email)
  ) {
    errors.email =
      "Please enter a valid email address.";
  }

  if (!password) {
    errors.password =
      "Password is required.";
  }

  return errors;
}

/**
 * Crop recommendation validation.
 *
 * These ranges match the current GrowX
 * Python AI input validation:
 *
 * Nitrogen:    0 - 200
 * Phosphorus:  0 - 200
 * Potassium:   0 - 250
 * Temperature: 0 - 55 °C
 * Humidity:    0 - 100 %
 * pH:          2.5 - 11
 * Rainfall:    0 - 500 mm
 * Sowing month: 1 - 12
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
    "sowingMonth",
  ];

  // -----------------------------------------
  // Required fields
  // -----------------------------------------

  required.forEach((field) => {
    if (
      form[field] === "" ||
      form[field] === undefined ||
      form[field] === null
    ) {
      errors[field] =
        "This field is required.";
    }
  });

  // -----------------------------------------
  // Nitrogen
  // -----------------------------------------

  if (
    !errors.nitrogen &&
    !isInRange(
      form.nitrogen,
      0,
      200,
    )
  ) {
    errors.nitrogen =
      "Must be between 0 and 200.";
  }

  // -----------------------------------------
  // Phosphorus
  // -----------------------------------------

  if (
    !errors.phosphorus &&
    !isInRange(
      form.phosphorus,
      0,
      200,
    )
  ) {
    errors.phosphorus =
      "Must be between 0 and 200.";
  }

  // -----------------------------------------
  // Potassium
  // -----------------------------------------

  if (
    !errors.potassium &&
    !isInRange(
      form.potassium,
      0,
      250,
    )
  ) {
    errors.potassium =
      "Must be between 0 and 250.";
  }

  // -----------------------------------------
  // Temperature
  // -----------------------------------------

  if (
    !errors.temperature &&
    !isInRange(
      form.temperature,
      0,
      55,
    )
  ) {
    errors.temperature =
      "Must be between 0 and 55°C.";
  }

  // -----------------------------------------
  // Humidity
  // -----------------------------------------

  if (
    !errors.humidity &&
    !isInRange(
      form.humidity,
      0,
      100,
    )
  ) {
    errors.humidity =
      "Must be between 0 and 100.";
  }

  // -----------------------------------------
  // Soil pH
  // -----------------------------------------

  if (
    !errors.ph &&
    !isInRange(
      form.ph,
      2.5,
      11,
    )
  ) {
    errors.ph =
      "Must be between 2.5 and 11.";
  }

  // -----------------------------------------
  // Rainfall
  // -----------------------------------------

  if (
    !errors.rainfall &&
    !isInRange(
      form.rainfall,
      0,
      500,
    )
  ) {
    errors.rainfall =
      "Must be between 0 and 500 mm.";
  }

  // -----------------------------------------
  // Sowing month
  // -----------------------------------------

  if (!errors.sowingMonth) {
    const month =
      Number(form.sowingMonth);

    if (
      !Number.isInteger(month) ||
      month < 1 ||
      month > 12
    ) {
      errors.sowingMonth =
        "Please select a valid sowing month.";
    }
  }

  return errors;
}