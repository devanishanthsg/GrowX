/**
 * storage.js
 * Centralized JWT token management.
 * All localStorage access for auth must go through here — never scattered.
 */

const TOKEN_KEY = "growx-token";

export const storage = {
  /** Store the JWT received from login or register */
  setToken(token) {
    localStorage.setItem(TOKEN_KEY, token);
  },

  /** Retrieve the stored JWT, or null if absent */
  getToken() {
    return localStorage.getItem(TOKEN_KEY);
  },

  /** Remove the JWT (logout) */
  clearToken() {
    localStorage.removeItem(TOKEN_KEY);
  },

  /** True if a token string is present in storage */
  hasToken() {
    return Boolean(localStorage.getItem(TOKEN_KEY));
  },
};
