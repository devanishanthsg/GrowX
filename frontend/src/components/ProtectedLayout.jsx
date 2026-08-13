/**
 * ProtectedLayout.jsx
 * Guards all authenticated routes.
 *
 * Behavior:
 * - Shows a loading spinner while AuthContext validates the stored token on startup.
 * - Redirects to /login if no valid authentication is found.
 * - Renders Sidebar + page content when authenticated.
 */

import { Navigate, Outlet } from "react-router-dom";
import Sidebar from "./Sidebar";
import { useAuth } from "../hooks/useAuth.js";

function ProtectedLayout() {
  const { isAuthenticated, loading } = useAuth();

  // Show a neutral loading state while JWT is being validated.
  // This prevents flashing /login briefly on page reload.
  if (loading) {
    return (
      <div className="auth-loading">
        <div className="auth-loading-spinner" />
        <p>Loading GrowX…</p>
      </div>
    );
  }

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  return (
    <div className="dashboard-layout">
      <Sidebar />
      <main className="dashboard-main">
        <Outlet />
      </main>
    </div>
  );
}

export default ProtectedLayout;