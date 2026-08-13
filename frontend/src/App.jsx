import { Route, Routes } from "react-router-dom";
import "./App.css";

import Home from "./pages/Home";
import Login from "./pages/Login";
import Register from "./pages/Register";
import Dashboard from "./pages/Dashboard";
import CropRecommendation from "./pages/CropRecommendation";
import DiseaseDetection from "./pages/DiseaseDetection";
import Weather from "./pages/Weather";
import Reports from "./pages/Reports";
import Profile from "./pages/Profile";
import ProtectedLayout from "./components/ProtectedLayout";

function App() {
  return (
    <Routes>
      <Route path="/" element={<Home />} />
      <Route path="/login" element={<Login />} />
      <Route
        path="/register"
        element={<Register />}
      />

      <Route element={<ProtectedLayout />}>
        <Route
          path="/dashboard"
          element={<Dashboard />}
        />

        <Route
          path="/crop-recommendation"
          element={<CropRecommendation />}
        />

        <Route
          path="/disease-detection"
          element={<DiseaseDetection />}
        />

        <Route
          path="/weather"
          element={<Weather />}
        />

        <Route
          path="/reports"
          element={<Reports />}
        />

        <Route
          path="/profile"
          element={<Profile />}
        />
      </Route>

      <Route
        path="*"
        element={
          <div className="not-found">
            <h1>404</h1>
            <p>Page not found</p>
          </div>
        }
      />
    </Routes>
  );
}

export default App;