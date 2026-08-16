/**
 * apiClient.js
 * Centralized HTTP client for all GrowX API calls.
 *
 * Responsibilities:
 * - Reads API base URL from VITE_API_BASE_URL
 * - Automatically attaches JWT for protected endpoints
 * - Supports JSON and multipart requests
 * - Parses backend ApiResponse { success, message, data }
 * - Handles expired JWT correctly
 * - Does NOT treat login failure as session expiry
 */

import { storage } from "../utils/storage.js";

const BASE_URL =
  import.meta.env.VITE_API_BASE_URL ??
  "http://localhost:8080";

/**
 * Structured error used by frontend pages.
 */
export class ApiError extends Error {
  constructor(message, status, data = null) {
    super(message);

    this.name = "ApiError";
    this.status = status;
    this.data = data;
  }
}

/**
 * Build common request headers.
 *
 * includeAuth:
 * true  -> attach JWT if available
 * false -> public endpoint such as login/register
 *
 * isJson:
 * true  -> Content-Type application/json
 * false -> multipart request, browser sets boundary
 */
function buildHeaders(
  includeAuth = true,
  isJson = true,
) {
  const headers = {};

  if (isJson) {
    headers["Content-Type"] =
      "application/json";
  }

  if (includeAuth) {
    const token = storage.getToken();

    if (token) {
      headers["Authorization"] =
        `Bearer ${token}`;
    }
  }

  return headers;
}

/**
 * Clear an expired JWT and redirect to login.
 *
 * Only call this for protected requests that
 * receive HTTP 401.
 */
function handleExpiredSession() {
  storage.clearToken();

  if (
    !window.location.pathname.startsWith(
      "/login",
    )
  ) {
    window.location.href = "/login";
  }
}

/**
 * Main API request function.
 *
 * @param {string} path
 * @param {{
 *   method?: string,
 *   body?: any,
 *   formData?: FormData,
 *   auth?: boolean
 * }} options
 *
 * @returns {Promise<any>}
 */
export async function request(
  path,
  {
    method = "GET",
    body,
    formData,
    auth = true,
  } = {},
) {
  const isMultipart =
    Boolean(formData);

  const headers = buildHeaders(
    auth,
    !isMultipart,
  );

  const init = {
    method,
    headers,
  };

  // -----------------------------------------
  // Request body
  // -----------------------------------------

  if (body !== undefined) {
    init.body =
      JSON.stringify(body);
  } else if (formData) {
    init.body = formData;
  }

  // -----------------------------------------
  // Perform HTTP request
  // -----------------------------------------

  let response;

  try {
    response = await fetch(
      `${BASE_URL}${path}`,
      init,
    );
  } catch {
    throw new ApiError(
      "Unable to reach the server. Please check your connection.",
      0,
    );
  }

  // -----------------------------------------
  // Parse response JSON
  // -----------------------------------------

  let parsed = null;

  try {
    parsed =
      await response.json();
  } catch {
    throw new ApiError(
      `Server returned an unexpected response (HTTP ${response.status}).`,
      response.status,
    );
  }

  // -----------------------------------------
  // Expired / invalid JWT
  // -----------------------------------------
  //
  // IMPORTANT:
  //
  // Only protected requests with auth=true
  // should trigger session-expired handling.
  //
  // Login/register use auth=false, so a 401
  // there simply means invalid credentials.
  // -----------------------------------------

  if (
    response.status === 401 &&
    auth
  ) {
    handleExpiredSession();

    throw new ApiError(
      "Session expired. Please log in again.",
      401,
      parsed?.data ?? null,
    );
  }

  // -----------------------------------------
  // Successful response
  // -----------------------------------------

  if (
    response.ok &&
    parsed?.success
  ) {
    return parsed.data;
  }

  // -----------------------------------------
  // Backend returned a normal JSON error
  // -----------------------------------------

  throw new ApiError(
    parsed?.message ??
      `Request failed (HTTP ${response.status}).`,
    response.status,
    parsed?.data ?? null,
  );
}