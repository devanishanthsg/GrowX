/**
 * apiClient.js
 * Centralized HTTP client for all GrowX API calls.
 *
 * - Reads base URL from VITE_API_BASE_URL environment variable
 * - Attaches JWT Authorization header automatically
 * - Parses the unified ApiResponse wrapper { success, message, data }
 * - Throws a structured ApiError on failure
 * - Handles 401/403 by clearing stored token and reloading to /login
 */

import { storage } from "../utils/storage.js";

const BASE_URL = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";

/** Structured error thrown when the API returns a non-success response */
export class ApiError extends Error {
  constructor(message, status, data = null) {
    super(message);
    this.name = "ApiError";
    this.status = status;
    this.data = data; // may contain field-level validation errors
  }
}

/**
 * Build common request headers.
 * If includeAuth=true and a token is stored, adds Authorization: Bearer <token>.
 * Does NOT set Content-Type for multipart requests (let browser handle boundary).
 */
function buildHeaders(includeAuth = true, isJson = true) {
  const headers = {};
  if (isJson) {
    headers["Content-Type"] = "application/json";
  }
  if (includeAuth) {
    const token = storage.getToken();
    if (token) {
      headers["Authorization"] = `Bearer ${token}`;
    }
  }
  return headers;
}

/**
 * Handle 401/403: clear token and navigate to login.
 * Uses window.location to avoid circular dependency with React Router.
 */
function handleAuthFailure() {
  storage.clearToken();
  if (!window.location.pathname.startsWith("/login")) {
    window.location.href = "/login";
  }
}

/**
 * Core request function.
 *
 * @param {string} path   - API path, e.g. "/api/auth/login"
 * @param {object} options
 * @param {string}   options.method     - HTTP method (default "GET")
 * @param {any}      options.body       - JSON body object (will be serialized)
 * @param {FormData} options.formData   - multipart body (mutually exclusive with body)
 * @param {boolean}  options.auth       - send JWT header (default true)
 * @returns {Promise<any>} The `data` field from the ApiResponse wrapper
 */
export async function request(path, { method = "GET", body, formData, auth = true } = {}) {
  const isMultipart = Boolean(formData);
  const headers = buildHeaders(auth, !isMultipart);

  const init = {
    method,
    headers,
  };

  if (body !== undefined) {
    init.body = JSON.stringify(body);
  } else if (formData) {
    init.body = formData;
  }

  let response;
  try {
    response = await fetch(`${BASE_URL}${path}`, init);
  } catch (networkError) {
    throw new ApiError(
      "Unable to reach the server. Please check your connection.",
      0,
    );
  }

  // Handle auth failures globally
  if (response.status === 401 || response.status === 403) {
    handleAuthFailure();
    throw new ApiError("Session expired. Please log in again.", response.status);
  }

  // Try to parse JSON — backend always returns JSON
  let parsed;
  try {
    parsed = await response.json();
  } catch {
    throw new ApiError(
      `Server returned an unexpected response (HTTP ${response.status}).`,
      response.status,
    );
  }

  // Success path: return the data payload
  if (response.ok && parsed.success) {
    return parsed.data;
  }

  // Error path: throw structured error with backend message
  throw new ApiError(
    parsed.message ?? `Request failed (HTTP ${response.status}).`,
    response.status,
    parsed.data ?? null,
  );
}
