import { NavLink } from "react-router-dom";
import { useAuth } from "../hooks/useAuth.js";

const menuItems = [
  {
    name: "Dashboard",
    path: "/dashboard",
    icon: "🏠",
  },
  {
    name: "Crop Recommendation",
    path: "/crop-recommendation",
    icon: "🌾",
  },
  {
    name: "Disease Detection",
    path: "/disease-detection",
    icon: "🍃",
  },
  {
    name: "Weather",
    path: "/weather",
    icon: "🌦️",
  },
  {
    name: "Reports",
    path: "/reports",
    icon: "📊",
  },
  {
    name: "Profile",
    path: "/profile",
    icon: "👨‍🌾",
  },
];

function Sidebar() {
  const { logout } = useAuth();

  return (
    <aside className="sidebar">
      <NavLink to="/" className="sidebar-logo">
        <span>🌱</span>
        GrowX
      </NavLink>

      <p className="sidebar-label">
        FARM MANAGEMENT
      </p>

      <nav className="sidebar-menu">
        {menuItems.map((item) => (
          <NavLink
            key={item.path}
            to={item.path}
            className={({ isActive }) =>
              isActive
                ? "sidebar-item sidebar-item-active"
                : "sidebar-item"
            }
          >
            <span>{item.icon}</span>
            {item.name}
          </NavLink>
        ))}
      </nav>

      <button
        type="button"
        className="logout-button"
        onClick={logout}
      >
        <span>🚪</span>
        Logout
      </button>
    </aside>
  );
}

export default Sidebar;