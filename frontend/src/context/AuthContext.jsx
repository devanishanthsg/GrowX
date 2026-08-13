/**
 * AuthContext.jsx
 * Provides authentication state and actions to the entire React tree.
 *
 * State:
 *   user         — UserResponse from backend, or null if not authenticated
 *   currentFarm  — first FarmResponse for the user, or null
 *   loading      — true while validating an existing token on startup
 *   isAuthenticated — derived: true when user is set
 *
 * Actions:
 *   loginSuccess(authResponse)  — called after successful login/register
 *   logout()                    — clears token and state, redirects to /login
 *   refreshUser()               — re-fetches user from /api/users/me
 *   refreshFarm()               — re-fetches farm list from /api/farms
 */

import {
  createContext,
  useCallback,
  useEffect,
  useState,
} from "react";
import { useNavigate } from "react-router-dom";
import { storage } from "../utils/storage.js";
import { getMe } from "../api/userApi.js";
import { getFarms } from "../api/farmApi.js";

export const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const navigate = useNavigate();

  const [user, setUser] = useState(null);
  const [currentFarm, setCurrentFarm] = useState(null);
  const [loading, setLoading] = useState(true); // true during initial token validation

  /**
   * Fetch and store the user's farms.
   * Selects the first farm as currentFarm for single-farm UI.
   * Structured so multi-farm support can be added later.
   */
  const fetchFarms = useCallback(async () => {
    try {
      const farms = await getFarms();
      if (Array.isArray(farms) && farms.length > 0) {
        setCurrentFarm(farms[0]);
      } else {
        setCurrentFarm(null);
      }
    } catch {
      setCurrentFarm(null);
    }
  }, []);

  /**
   * On application startup: validate any existing token.
   * If valid → set user and farm.
   * If invalid → clear token and leave unauthenticated.
   */
  useEffect(() => {
    async function validateToken() {
      if (!storage.hasToken()) {
        setLoading(false);
        return;
      }

      try {
        const userData = await getMe();
        setUser(userData);
        await fetchFarms();
      } catch {
        // Token is invalid or expired — clean up
        storage.clearToken();
        setUser(null);
        setCurrentFarm(null);
      } finally {
        setLoading(false);
      }
    }

    validateToken();
  }, [fetchFarms]);

  /**
   * Called after a successful login or registration.
   * Stores the JWT, sets user state, fetches farms.
   *
   * @param {{ token: string, user: object }} authData — from AuthResponse.data
   */
  const loginSuccess = useCallback(
    async (authData) => {
      storage.setToken(authData.token);
      setUser(authData.user);
      await fetchFarms();
    },
    [fetchFarms],
  );

  /**
   * Clear authentication state and navigate to /login.
   */
  const logout = useCallback(() => {
    storage.clearToken();
    setUser(null);
    setCurrentFarm(null);
    navigate("/login", { replace: true });
  }, [navigate]);

  /**
   * Re-fetch user profile from /api/users/me.
   * Useful after a profile update.
   */
  const refreshUser = useCallback(async () => {
    try {
      const userData = await getMe();
      setUser(userData);
    } catch {
      // If this fails (401), apiClient will handle the redirect
    }
  }, []);

  /**
   * Re-fetch farm data. Useful after a farm update.
   */
  const refreshFarm = useCallback(async () => {
    await fetchFarms();
  }, [fetchFarms]);

  const value = {
    user,
    currentFarm,
    loading,
    isAuthenticated: Boolean(user),
    loginSuccess,
    logout,
    refreshUser,
    refreshFarm,
  };

  return (
    <AuthContext.Provider value={value}>
      {children}
    </AuthContext.Provider>
  );
}
