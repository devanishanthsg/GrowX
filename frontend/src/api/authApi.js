/**
 * authApi.js
 * Authentication API calls — register and login.
 * Endpoints: POST /api/auth/register, POST /api/auth/login
 * No JWT required for these endpoints.
 */

import { request } from "./apiClient.js";

/**
 * Register a new farmer account.
 * @param {{ name: string, email: string, location: string, password: string }} data
 * @returns {Promise<{ token: string, tokenType: string, user: object }>} AuthResponse data
 */
export async function register(data) {
  return request("/api/auth/register", {
    method: "POST",
    body: data,
    auth: false,
  });
}

/**
 * Authenticate with email and password.
 * @param {{ email: string, password: string }} credentials
 * @returns {Promise<{ token: string, tokenType: string, user: object }>} AuthResponse data
 */
export async function login(credentials) {
  return request("/api/auth/login", {
    method: "POST",
    body: credentials,
    auth: false,
  });
}
