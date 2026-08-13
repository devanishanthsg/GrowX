import { Navigate, Outlet } from "react-router-dom";
import Sidebar from "./Sidebar";

function ProtectedLayout() {
  const storedUser = localStorage.getItem("growx-user");

  if (!storedUser) {
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